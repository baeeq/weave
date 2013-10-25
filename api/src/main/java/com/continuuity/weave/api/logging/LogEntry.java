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
package com.continuuity.weave.api.logging;

/**
 * Represents a log entry emitted by application.
 */
public interface LogEntry {

  /**
   * Log level.
   */
  enum Level {
    FATAL,
    ERROR,
    WARN,
    INFO,
    DEBUG,
    TRACE
  }

  String getLoggerName();

  String getHost();

  long getTimestamp();

  Level getLogLevel();

  String getSourceClassName();

  String getSourceMethodName();

  String getFileName();

  int getLineNumber();

  String getThreadName();

  String getMessage();

  StackTraceElement[] getStackTraces();
}
