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


import java.util.logging.Logger;

import static java.util.logging.Level.WARNING;


/**
 * Various utilities for threads.
 *
 * @author claudio.rosati@esss.se
 * @see <a href="https://www.baeldung.com/java-delay-code-execution">How to Delay Code Execution in Java</a>
 */
public class ThreadUtils {

	private static final Logger LOGGER = Logger.getLogger(ThreadUtils.class.getName());

	/**
	 * Invokes {@link Thread#sleep(long)} taking care of possible
	 * {@link InterruptedException}s by propagating the thread interruption.
	 *
	 * @param millis The sleep time in milliseconds.
	 */
	public static void sleep( long millis ) {
		try {
			Thread.sleep(millis);
		} catch ( InterruptedException ex ) {
			LogUtils.log(LOGGER, WARNING, ex, "Sleep interrupted: propagating thread interruption.");
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * Invokes {@link Thread#sleep(long)} taking care of possible possible
	 * {@link InterruptedException}s by propagating the thread interruption.
	 *
	 * @param millis The sleep time in milliseconds.
	 * @param nanos  0-999999 additional nanoseconds to sleep.
	 */
	public static void sleep( long millis, int nanos ) {
		try {
			Thread.sleep(millis, nanos);
		} catch ( InterruptedException ex ) {
			LogUtils.log(LOGGER, WARNING, ex, "Sleep interrupted: propagating thread interruption.");
			Thread.currentThread().interrupt();
		}
	}

	private ThreadUtils() {
		//	Nothing to do.
	}

}
