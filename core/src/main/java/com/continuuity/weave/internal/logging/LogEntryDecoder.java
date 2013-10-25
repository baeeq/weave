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

import com.continuuity.weave.api.logging.LogEntry;
import com.continuuity.weave.internal.json.JsonUtils;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

/**
 * A {@link com.google.gson.Gson} decoder for {@link LogEntry}.
 */
public final class LogEntryDecoder implements JsonDeserializer<LogEntry> {

  @Override
  public LogEntry deserialize(JsonElement json, Type typeOfT,
                              JsonDeserializationContext context) throws JsonParseException {
    if (!json.isJsonObject()) {
      return null;
    }
    JsonObject jsonObj = json.getAsJsonObject();

    final String name = JsonUtils.getAsString(jsonObj, "name");
    final String host = JsonUtils.getAsString(jsonObj, "host");
    final long timestamp = JsonUtils.getAsLong(jsonObj, "timestamp", 0);
    LogEntry.Level l;
    try {
      l = LogEntry.Level.valueOf(JsonUtils.getAsString(jsonObj, "level"));
    } catch (Exception e) {
      l = LogEntry.Level.FATAL;
    }
    final LogEntry.Level logLevel = l;
    final String className = JsonUtils.getAsString(jsonObj, "className");
    final String method = JsonUtils.getAsString(jsonObj, "method");
    final String file = JsonUtils.getAsString(jsonObj, "file");
    final String line = JsonUtils.getAsString(jsonObj, "line");
    final String thread = JsonUtils.getAsString(jsonObj, "thread");
    final String message = JsonUtils.getAsString(jsonObj, "message");

    final StackTraceElement[] stackTraces = context.deserialize(jsonObj.get("stackTraces").getAsJsonArray(),
                                                                StackTraceElement[].class);

    return new LogEntry() {
      @Override
      public String getLoggerName() {
        return name;
      }

      @Override
      public String getHost() {
        return host;
      }

      @Override
      public long getTimestamp() {
        return timestamp;
      }

      @Override
      public Level getLogLevel() {
        return logLevel;
      }

      @Override
      public String getSourceClassName() {
        return className;
      }

      @Override
      public String getSourceMethodName() {
        return method;
      }

      @Override
      public String getFileName() {
        return file;
      }

      @Override
      public int getLineNumber() {
        if (line.equals("?")) {
          return -1;
        } else {
          return Integer.parseInt(line);
        }
      }

      @Override
      public String getThreadName() {
        return thread;
      }

      @Override
      public String getMessage() {
        return message;
      }

      @Override
      public StackTraceElement[] getStackTraces() {
        return stackTraces;
      }
    };
  }
}
