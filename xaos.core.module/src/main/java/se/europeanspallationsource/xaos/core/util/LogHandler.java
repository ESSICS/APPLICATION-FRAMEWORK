/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * Copyright (C) 2018-2019 by European Spallation Source ERIC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.europeanspallationsource.xaos.core.util;


import java.util.logging.Level;


/**
 * Used by {@link LogUtils} to send to the information just logged for further
 * processing.
 *
 * @author claudio.rosati@esss.se
 */
public interface LogHandler {

	/**
	 * Called by {@link LogUtils} log methods to further processing log
	 * information.
	 *
	 * @param level      Log level.
	 * @param className  Name of the class calling one of the {@link LogUtils}
	 *                   log methods.
	 * @param methodName Name of the method calling one of the {@link LogUtils}
	 *                   log methods.
	 * @param message    Log message.
	 * @param thrown     Optional exception causing the logging.
	 */
	void handle( Level level, String className, String methodName, String message, Throwable thrown );

}
