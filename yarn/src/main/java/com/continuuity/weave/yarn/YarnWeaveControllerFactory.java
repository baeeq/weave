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

import com.continuuity.weave.api.RunId;
import com.continuuity.weave.api.logging.LogHandler;
import com.continuuity.weave.internal.ProcessController;
import com.continuuity.weave.internal.yarn.YarnApplicationReport;

import java.util.concurrent.Callable;

/**
 * Factory for creating {@link YarnWeaveController}.
 */
interface YarnWeaveControllerFactory {

  YarnWeaveController create(RunId runId, Iterable<LogHandler> logHandlers,
                             Callable<ProcessController<YarnApplicationReport>> startUp);
}
