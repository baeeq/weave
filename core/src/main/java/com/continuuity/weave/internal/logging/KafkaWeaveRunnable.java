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
package com.continuuity.weave.internal.logging;

import com.continuuity.weave.api.Command;
import com.continuuity.weave.api.WeaveContext;
import com.continuuity.weave.api.WeaveRunnable;
import com.continuuity.weave.api.WeaveRunnableSpecification;
import com.continuuity.weave.internal.EnvKeys;
import com.continuuity.weave.internal.kafka.EmbeddedKafkaServer;
import com.continuuity.weave.internal.utils.Networks;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

/**
 * A {@link WeaveRunnable} for managing Kafka server.
 */
public final class KafkaWeaveRunnable implements WeaveRunnable {

  private static final Logger LOG = LoggerFactory.getLogger(KafkaWeaveRunnable.class);

  private final String kafkaDir;
  private EmbeddedKafkaServer server;
  private CountDownLatch stopLatch;

  public KafkaWeaveRunnable(String kafkaDir) {
    this.kafkaDir = kafkaDir;
  }

  @Override
  public WeaveRunnableSpecification configure() {
    return WeaveRunnableSpecification.Builder.with()
      .setName("kafka")
      .withConfigs(ImmutableMap.of("kafkaDir", kafkaDir))
      .build();
  }

  @Override
  public void initialize(WeaveContext context) {
    Map<String, String> args = context.getSpecification().getConfigs();
    String zkConnectStr = System.getenv(EnvKeys.WEAVE_LOG_KAFKA_ZK);
    stopLatch = new CountDownLatch(1);

    try {
      server = new EmbeddedKafkaServer(new File(args.get("kafkaDir")), generateKafkaConfig(zkConnectStr));
      server.startAndWait();
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }

  @Override
  public void handleCommand(Command command) throws Exception {
  }

  @Override
  public void stop() {
    stopLatch.countDown();
  }

  @Override
  public void destroy() {
    server.stopAndWait();
  }

  @Override
  public void run() {
    try {
      stopLatch.await();
    } catch (InterruptedException e) {
      LOG.info("Running thread interrupted, shutting down kafka server.", e);
    }
  }

  private Properties generateKafkaConfig(String zkConnectStr) {
    int port = Networks.getRandomPort();
    Preconditions.checkState(port > 0, "Failed to get random port.");

    Properties prop = new Properties();
    prop.setProperty("log.dir", new File("kafka-logs").getAbsolutePath());
    prop.setProperty("zk.connect", zkConnectStr);
    prop.setProperty("num.threads", "8");
    prop.setProperty("port", Integer.toString(port));
    prop.setProperty("log.flush.interval", "10000");
    prop.setProperty("max.socket.request.bytes", "104857600");
    prop.setProperty("log.cleanup.interval.mins", "1");
    prop.setProperty("log.default.flush.scheduler.interval.ms", "1000");
    prop.setProperty("zk.connectiontimeout.ms", "1000000");
    prop.setProperty("socket.receive.buffer", "1048576");
    prop.setProperty("enable.zookeeper", "true");
    prop.setProperty("log.retention.hours", "168");
    prop.setProperty("brokerid", "0");
    prop.setProperty("socket.send.buffer", "1048576");
    prop.setProperty("num.partitions", "1");
    prop.setProperty("log.file.size", "536870912");
    prop.setProperty("log.default.flush.interval.ms", "1000");
    return prop;
  }
}
