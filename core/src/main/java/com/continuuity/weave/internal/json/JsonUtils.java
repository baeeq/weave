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
package com.continuuity.weave.internal.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Collections of helper functions for json codec.
 */
public final class JsonUtils {

  private JsonUtils() {
  }

  /**
   * Returns a String representation of the given property.
   */
  public static String getAsString(JsonObject json, String property) {
    JsonElement jsonElement = json.get(property);
    if (jsonElement.isJsonNull()) {
      return null;
    }
    if (jsonElement.isJsonPrimitive()) {
      return jsonElement.getAsString();
    }
    return jsonElement.toString();
  }

  /**
   * Returns a long representation of the given property.
   */
  public static long getAsLong(JsonObject json, String property, long defaultValue) {
    try {
      return json.get(property).getAsLong();
    } catch (Exception e) {
      return defaultValue;
    }
  }

  /**
   * Returns a long representation of the given property.
   */
  public static int getAsInt(JsonObject json, String property, int defaultValue) {
    try {
      return json.get(property).getAsInt();
    } catch (Exception e) {
      return defaultValue;
    }
  }
}
