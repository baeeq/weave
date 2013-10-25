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

import com.continuuity.weave.api.EventHandler;
import com.continuuity.weave.api.EventHandlerSpecification;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 *
 */
public class DefaultEventHandlerSpecification implements EventHandlerSpecification {

  private final String className;
  private final Map<String, String> configs;

  public DefaultEventHandlerSpecification(String className, Map<String, String> configs) {
    this.className = className;
    this.configs = configs;
  }

  public DefaultEventHandlerSpecification(EventHandler eventHandler) {
    EventHandlerSpecification spec = eventHandler.configure();
    this.className = eventHandler.getClass().getName();
    this.configs = ImmutableMap.copyOf(spec.getConfigs());
  }

  @Override
  public String getClassName() {
    return className;
  }

  @Override
  public Map<String, String> getConfigs() {
    return configs;
  }
}
