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

import com.continuuity.weave.api.ServiceController;
import com.continuuity.weave.internal.state.Message;
import com.google.common.util.concurrent.ListenableFuture;

/**
 * A {@link ServiceController} that allows sending a message directly. Internal use only.
 */
public interface WeaveContainerController extends ServiceController {

  ListenableFuture<Message> sendMessage(Message message);

  /**
   * Calls to indicated that the container that this controller is associated with is completed.
   * Any resources it hold will be releases and all pending futures will be cancelled.
   */
  void completed(int exitStatus);
}
