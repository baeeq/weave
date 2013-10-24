/*
 * Copyright 2012-2013 Continuuity,Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.continuuity.weave.internal.appmaster;

import com.continuuity.weave.api.ResourceReport;
import com.continuuity.weave.internal.json.ResourceReportAdapter;
import com.google.common.util.concurrent.AbstractIdleService;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferOutputStream;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpContentCompressor;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Webservice that the Application Master will register back to the resource manager
 * for clients to track application progress.  Currently used purely for getting a
 * breakdown of resource usage as a {@link com.continuuity.weave.api.ResourceReport}.
 */
public final class TrackerService extends AbstractIdleService {

  // TODO: This is temporary. When support more REST API, this would get moved.
  public static final String PATH = "/resources";

  private static final Logger LOG  = LoggerFactory.getLogger(TrackerService.class);
  private static final int NUM_BOSS_THREADS = 1;
  private static final int CLOSE_CHANNEL_TIMEOUT = 5;
  private static final int MAX_INPUT_SIZE = 100 * 1024 * 1024;

  private final String host;
  private ServerBootstrap bootstrap;
  private InetSocketAddress bindAddress;
  private URL url;
  private final ChannelGroup channelGroup;
  private final ResourceReport resourceReport;

  /**
   * Initialize the service.
   *
   * @param resourceReport live report that the service will return to clients.
   * @param appMasterHost the application master host.
   */
  public TrackerService(ResourceReport resourceReport, String appMasterHost) {
    this.channelGroup = new DefaultChannelGroup("appMasterTracker");
    this.resourceReport = resourceReport;
    this.host = appMasterHost;
  }

  /**
   * Returns the address this tracker service is bounded to.
   */
  public InetSocketAddress getBindAddress() {
    return bindAddress;
  }

  /**
   * @return tracker url.
   */
  public URL getUrl() {
    return url;
  }

  @Override
  protected void startUp() throws Exception {
    Executor bossThreads = Executors.newFixedThreadPool(NUM_BOSS_THREADS,
                                                        new ThreadFactoryBuilder()
                                                          .setDaemon(true)
                                                          .setNameFormat("boss-thread")
                                                          .build());

    Executor workerThreads = Executors.newCachedThreadPool(new ThreadFactoryBuilder()
                                                             .setDaemon(true)
                                                             .setNameFormat("worker-thread#%d")
                                                             .build());

    ChannelFactory factory = new NioServerSocketChannelFactory(bossThreads, workerThreads);

    bootstrap = new ServerBootstrap(factory);

    bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
      public ChannelPipeline getPipeline() {
        ChannelPipeline pipeline = Channels.pipeline();

        pipeline.addLast("decoder", new HttpRequestDecoder());
        pipeline.addLast("aggregator", new HttpChunkAggregator(MAX_INPUT_SIZE));
        pipeline.addLast("encoder", new HttpResponseEncoder());
        pipeline.addLast("compressor", new HttpContentCompressor());
        pipeline.addLast("handler", new ReportHandler(resourceReport));

        return pipeline;
      }
    });

    Channel channel = bootstrap.bind(new InetSocketAddress(host, 0));
    bindAddress = (InetSocketAddress) channel.getLocalAddress();
    url = URI.create(String.format("http://%s:%d", host, bindAddress.getPort()))
             .resolve(TrackerService.PATH).toURL();
    channelGroup.add(channel);
  }

  @Override
  protected void shutDown() throws Exception {
    try {
      if (!channelGroup.close().await(CLOSE_CHANNEL_TIMEOUT, TimeUnit.SECONDS)) {
        LOG.warn("Timeout when closing all channels.");
      }
    } finally {
      bootstrap.releaseExternalResources();
    }
  }

  /**
   * Handler to return resources used by this application master, which will be available through
   * the host and port set when this application master registered itself to the resource manager.
   */
  public class ReportHandler extends SimpleChannelUpstreamHandler {
    private final ResourceReport report;
    private final ResourceReportAdapter reportAdapter;

    public ReportHandler(ResourceReport report) {
      this.report = report;
      this.reportAdapter = ResourceReportAdapter.create();
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
      HttpRequest request = (HttpRequest) e.getMessage();
      if (!isValid(request)) {
        write404(e);
        return;
      }

      writeResponse(e);
    }

    // only accepts GET on /resources for now
    private boolean isValid(HttpRequest request) {
      return (request.getMethod() == HttpMethod.GET) && PATH.equals(request.getUri());
    }

    private void write404(MessageEvent e) {
      HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);
      ChannelFuture future = e.getChannel().write(response);
      future.addListener(ChannelFutureListener.CLOSE);
    }

    private void writeResponse(MessageEvent e) {
      HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
      response.setHeader(HttpHeaders.Names.CONTENT_TYPE, "application/json; charset=UTF-8");

      ChannelBuffer content = ChannelBuffers.dynamicBuffer();
      Writer writer = new OutputStreamWriter(new ChannelBufferOutputStream(content), CharsetUtil.UTF_8);
      reportAdapter.toJson(report, writer);
      try {
        writer.close();
      } catch (IOException e1) {
        LOG.error("error writing resource report", e1);
      }
      response.setContent(content);
      ChannelFuture future = e.getChannel().write(response);
      future.addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
      e.getChannel().close();
    }
  }
}

