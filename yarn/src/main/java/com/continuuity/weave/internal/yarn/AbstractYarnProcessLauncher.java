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
package com.continuuity.weave.internal.yarn;

import com.continuuity.weave.api.LocalFile;
import com.continuuity.weave.internal.ProcessController;
import com.continuuity.weave.internal.ProcessLauncher;
import com.continuuity.weave.internal.utils.Paths;
import com.continuuity.weave.yarn.utils.YarnUtils;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.hadoop.security.Credentials;
import org.apache.hadoop.security.token.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Abstract class to help creating different types of process launcher that process on yarn.
 *
 * @param <T> Type of the object that contains information about the container that the process is going to launch.
 */
public abstract class AbstractYarnProcessLauncher<T> implements ProcessLauncher<T> {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractYarnProcessLauncher.class);

  private final T containerInfo;

  protected AbstractYarnProcessLauncher(T containerInfo) {
    this.containerInfo = containerInfo;
  }

  @Override
  public T getContainerInfo() {
    return containerInfo;
  }

  @Override
  public <C> PrepareLaunchContext prepareLaunch(Map<String, String> environments,
                                                Iterable<LocalFile> resources, C credentials) {
    if (credentials != null) {
      Preconditions.checkArgument(credentials instanceof Credentials, "Credentials should be of type %s",
                                  Credentials.class.getName());
    }
    return new PrepareLaunchContextImpl(environments, resources, (Credentials) credentials);
  }

  /**
   * Tells whether to append suffix to localize resource name for archive file type. Default is true.
   */
  protected boolean useArchiveSuffix() {
    return true;
  }

  /**
   * For children class to override to perform actual process launching.
   */
  protected abstract <R> ProcessController<R> doLaunch(YarnLaunchContext launchContext);

  /**
   * Implementation for the {@link PrepareLaunchContext}.
   */
  private final class PrepareLaunchContextImpl implements PrepareLaunchContext {

    private final Credentials credentials;
    private final YarnLaunchContext launchContext;
    private final Map<String, YarnLocalResource> localResources;
    private final Map<String, String> environment;
    private final List<String> commands;

    private PrepareLaunchContextImpl(Map<String, String> env, Iterable<LocalFile> localFiles, Credentials credentials) {
      this.credentials = credentials;
      this.launchContext = YarnUtils.createLaunchContext();
      this.localResources = Maps.newHashMap();
      this.environment = Maps.newHashMap(env);
      this.commands = Lists.newLinkedList();

      for (LocalFile localFile : localFiles) {
        addLocalFile(localFile);
      }
    }

    private void addLocalFile(LocalFile localFile) {
      String name = localFile.getName();
      // Always append the file extension as the resource name so that archive expansion by Yarn could work.
      // Renaming would happen by the Container Launcher.
      if (localFile.isArchive() && useArchiveSuffix()) {
        String path = localFile.getURI().toString();
        String suffix = Paths.getExtension(path);
        if (!suffix.isEmpty()) {
          name += '.' + suffix;
        }
      }
      localResources.put(name, YarnUtils.createLocalResource(localFile));
    }

    @Override
    public ResourcesAdder withResources() {
      return new MoreResourcesImpl();
    }

    @Override
    public AfterResources noResources() {
      return new MoreResourcesImpl();
    }

    private final class MoreResourcesImpl implements MoreResources {

      @Override
      public MoreResources add(LocalFile localFile) {
        addLocalFile(localFile);
        return this;
      }

      @Override
      public EnvironmentAdder withEnvironment() {
        return finish();
      }

      @Override
      public AfterEnvironment noEnvironment() {
        return finish();
      }

      private MoreEnvironmentImpl finish() {
        launchContext.setLocalResources(localResources);
        return new MoreEnvironmentImpl();
      }
    }

    private final class MoreEnvironmentImpl implements MoreEnvironment {

      @Override
      public CommandAdder withCommands() {
        launchContext.setEnvironment(environment);
        return new MoreCommandImpl();
      }

      @Override
      public <V> MoreEnvironment add(String key, V value) {
        environment.put(key, value.toString());
        return this;
      }
    }

    private final class MoreCommandImpl implements MoreCommand, StdOutSetter, StdErrSetter {

      private final StringBuilder commandBuilder = new StringBuilder();

      @Override
      public StdOutSetter add(String cmd, String... args) {
        commandBuilder.append(cmd);
        for (String arg : args) {
          commandBuilder.append(' ').append(arg);
        }
        return this;
      }

      @Override
      public <R> ProcessController<R> launch() {
        if (credentials != null && !credentials.getAllTokens().isEmpty()) {
          for (Token<?> token : credentials.getAllTokens()) {
            LOG.info("Launch with delegation token {}", token);
          }
          launchContext.setCredentials(credentials);
        }
        launchContext.setCommands(commands);
        return doLaunch(launchContext);
      }

      @Override
      public MoreCommand redirectError(String stderr) {
        commandBuilder.append(' ').append("2>").append(stderr);
        return noError();
      }

      @Override
      public MoreCommand noError() {
        commands.add(commandBuilder.toString());
        commandBuilder.setLength(0);
        return this;
      }

      @Override
      public StdErrSetter redirectOutput(String stdout) {
        commandBuilder.append(' ').append("1>").append(stdout);
        return this;
      }

      @Override
      public StdErrSetter noOutput() {
        return this;
      }
    }
  }
}
