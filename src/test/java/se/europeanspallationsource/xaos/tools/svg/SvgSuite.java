/*
 * Copyright 2018 claudiorosati.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.europeanspallationsource.xaos.tools.svg;


import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import static se.europeanspallationsource.xaos.tools.HeadlessUtility.conditionallyHeadless;


/**
 * @author claudio.rosati@esss.se
 */
@RunWith( Suite.class )
@Suite.SuiteClasses( {
	AtomTest.class,
	GasCylinderTest.class,
	SVGFromInputStreamTest.class,
	SVGFromURLTest.class
} )
@SuppressWarnings( { "ClassMayBeInterface", "ClassWithoutLogger", "UtilityClassWithoutPrivateConstructor" } )
public class SvgSuite {

	@BeforeClass
	public static void setUpSuite() {
		conditionallyHeadless();
	}

}