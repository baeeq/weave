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
package com.continuuity.weave.yarn;

import com.continuuity.weave.api.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Test server that echoes back what it receives.
 */
public final class EchoServer extends SocketServer {

  private static final Logger LOG = LoggerFactory.getLogger(EchoServer.class);

  @Override
  public void handleRequest(BufferedReader reader, PrintWriter writer) throws IOException {
    String line = reader.readLine();
    LOG.info("Received: " + line);
    if (line != null) {
      writer.println(line);
    }
  }

  @Override
  public void handleCommand(Command command) throws Exception {
    LOG.info("Command received: " + command + " " + getContext().getInstanceCount());
  }
}
