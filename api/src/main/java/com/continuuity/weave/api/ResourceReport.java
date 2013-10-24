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
package com.continuuity.weave.api;

import java.util.Collection;
import java.util.Map;

/**
 * This interface provides a snapshot of the resources an application is using
 * broken down by each runnable.
 */
public interface ResourceReport {
  /**
   * Get all the run resources being used by all instances of the specified runnable.
   *
   * @param runnableName the runnable name.
   * @return resources being used by all instances of the runnable.
   */
  public Collection<WeaveRunResources> getRunnableResources(String runnableName);

  /**
   * Get all the run resources being used across all runnables.
   *
   * @return all run resources used by all instances of all runnables.
   */
  public Map<String, Collection<WeaveRunResources>> getResources();

  /**
   * Get the resources application master is using.
   *
   * @return resources being used by the application master.
   */
  public WeaveRunResources getAppMasterResources();

  /**
   * Get the id of the application master.
   *
   * @return id of the application master.
   */
  public String getApplicationId();
}
