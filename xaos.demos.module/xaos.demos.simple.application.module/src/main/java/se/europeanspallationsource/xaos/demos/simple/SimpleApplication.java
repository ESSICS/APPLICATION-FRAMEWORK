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
package se.europeanspallationsource.xaos.demos.simple;


import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import se.europeanspallationsource.xaos.app.XAOSApplication;


/**
 * A simple, one-window application.
 *
 * @author claudio.rosati@esss.se
 */
@SuppressWarnings( "ClassWithoutLogger" )
public class SimpleApplication extends XAOSApplication {

	public static void main( String[] args ) {
		launch(args);
	}

	@Override
	protected void startApplication( BorderPane sceneRoot ) throws Exception {
		sceneRoot.setCenter(new Label("S I M P L E   A P P L I C A T I O N"));
	}

}
