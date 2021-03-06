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

import com.continuuity.weave.api.WeaveRunnableSpecification;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * Straightforward implementation of {@link WeaveRunnableSpecification}.
 */
public final class DefaultWeaveRunnableSpecification implements WeaveRunnableSpecification {

  private final String className;
  private final String name;
  private final Map<String, String> arguments;

  public DefaultWeaveRunnableSpecification(String className, String name, Map<String, String> arguments) {
    this.className = className;
    this.name = name;
    this.arguments = ImmutableMap.copyOf(arguments);
  }

  public DefaultWeaveRunnableSpecification(String className, WeaveRunnableSpecification other) {
    this.className = className;
    this.name = other.getName();
    this.arguments = ImmutableMap.copyOf(other.getConfigs());
  }

  @Override
  public String getClassName() {
    return className;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Map<String, String> getConfigs() {
    return arguments;
  }
}
