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
package com.continuuity.weave.internal;

import com.continuuity.weave.api.WeaveRunResources;

/**
 *  Straightforward implementation of {@link com.continuuity.weave.api.WeaveRunResources}.
 */
public class DefaultWeaveRunResources implements WeaveRunResources {
  private final String containerId;
  private final int instanceId;
  private final int virtualCores;
  private final int memoryMB;
  private final String host;

  public DefaultWeaveRunResources(int instanceId, String containerId,
                                  int cores, int memoryMB, String host) {
    this.instanceId = instanceId;
    this.containerId = containerId;
    this.virtualCores = cores;
    this.memoryMB = memoryMB;
    this.host = host;
  }

  /**
   * @return instance id of the runnable.
   */
  @Override
  public int getInstanceId() {
    return instanceId;
  }

  /**
   * @return id of the container the runnable is running in.
   */
  @Override
  public String getContainerId() {
    return containerId;
  }

  /**
   * @return number of cores the runnable is allowed to use.  YARN must be at least v2.1.0 and
   *   it must be configured to use cgroups in order for this to be a reflection of truth.
   */
  @Override
  public int getVirtualCores() {
    return virtualCores;
  }

  /**
   * @return amount of memory in MB the runnable is allowed to use.
   */
  @Override
  public int getMemoryMB() {
    return memoryMB;
  }

  /**
   * @return the host the runnable is running on.
   */
  @Override
  public String getHost() {
    return host;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof WeaveRunResources)) {
      return false;
    }
    WeaveRunResources other = (WeaveRunResources) o;
    return (instanceId == other.getInstanceId()) &&
      containerId.equals(other.getContainerId()) &&
      host.equals(other.getHost()) &&
      (virtualCores == other.getVirtualCores()) &&
      (memoryMB == other.getMemoryMB());
  }

  @Override
  public int hashCode() {
    int hash = 17;
    hash = 31 *  hash + containerId.hashCode();
    hash = 31 *  hash + host.hashCode();
    hash = 31 *  hash + (int) (instanceId ^ (instanceId >>> 32));
    hash = 31 *  hash + (int) (virtualCores ^ (virtualCores >>> 32));
    hash = 31 *  hash + (int) (memoryMB ^ (memoryMB >>> 32));
    return hash;
  }

}
