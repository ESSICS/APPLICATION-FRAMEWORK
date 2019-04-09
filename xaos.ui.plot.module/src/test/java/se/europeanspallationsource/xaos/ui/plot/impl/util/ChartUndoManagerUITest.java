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
package se.europeanspallationsource.xaos.ui.plot.impl.util;


import chart.LineChartFX;
import chart.data.DataReducingSeries;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeoutException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit.ApplicationTest;
import se.europeanspallationsource.xaos.ui.plot.plugins.KeyboardAccelerators;
import se.europeanspallationsource.xaos.ui.plot.plugins.Navigator;

import static javafx.geometry.Pos.CENTER;
import static javafx.scene.input.KeyCode.ALT;
import static javafx.scene.input.MouseButton.PRIMARY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.testfx.robot.Motion.DEFAULT;




/**
 * @author claudio.rosati@esss.se
 */
@SuppressWarnings( { "UseOfSystemOutOrSystemErr", "ClassWithoutLogger" } )
public class ChartUndoManagerUITest extends ApplicationTest {

	private static final Point2D PAN_DOWN_POINT = new Point2D(0, 25);
	private static final Point2D PAN_LEFT_POINT = new Point2D(-25, 0);
	private static final Point2D PAN_RIGHT_POINT = new Point2D(25, 0);
	private static final Point2D PAN_UP_POINT = new Point2D(0, -25);
	private static final int POINTS_COUNT = 20;
	private static final Random RANDOM = new Random(System.currentTimeMillis());
	private static final Point2D REDO_POINT = new Point2D(-40, 40);
	private static final Point2D UNDO_POINT = new Point2D(-40, -40);
	private static final Point2D ZOOM_IN_POINT = new Point2D(40, -40);
	private static final Point2D ZOOM_OUT_POINT = new Point2D(40, 40);
	private static final Point2D ZOOM_TO_ONE_POINT = new Point2D(0, 0);

	@BeforeClass
	public static void setUpClass() {
		System.out.println("---- ChartUndoManagerUITest ------------------------------------");
	}

	private LineChartFX<Number, Number> chart;
	private double chartXLowerBound;
	private double chartXUpperBound;
	private double chartYLowerBound;
	private double chartYUpperBound;
	private KeyboardAccelerators keyboardAccelerators;
	private Navigator navigator;

	@Override
	@SuppressWarnings( "NestedAssignment" )
	public void start( Stage stage ) throws IOException {

		final NumberAxis xAxis = new NumberAxis();
		final NumberAxis yAxis = new NumberAxis();

		xAxis.setAnimated(false);
		yAxis.setAnimated(false);

		chart = new LineChartFX<>(xAxis, yAxis);

		chart.setTitle("ChartUndoManagerUITest");
		chart.setAnimated(false);
		chart.getChartPlugins().addAll(
			navigator            = new Navigator(),
			keyboardAccelerators = new KeyboardAccelerators()
//			new CoordinatesLines(),
//			new Zoom(),
//			new Pan(),
//			new CoordinatesLabel(),
//			new DataPointTooltip(),
//			new AreaValueTooltip(),
//			new PropertyMenu()
		);

		DataReducingSeries<Number, Number> series0 = new DataReducingSeries<>();

		series0.setName("Generated test data-horizontal");
		series0.setData(generateData(POINTS_COUNT));
		chart.getData().add(series0.getSeries());

		DataReducingSeries<Number, Number> series1 = new DataReducingSeries<>();

		series1.setName("Generated test data-vertical");
		series1.setData(generateData(POINTS_COUNT));
		chart.getData().add(series1.getSeries());

		DataReducingSeries<Number, Number> series2 = new DataReducingSeries<>();

		series2.setName("Generated test data-longitudinal");
		series2.setData(generateData(POINTS_COUNT));
		chart.getData().add(series2.getSeries());

        chart.setFocusTraversable(true);

		Scene scene = new Scene(new BorderPane(chart), 1200, 900);

		scene.getStylesheets().add(getClass().getResource("/se/europeanspallationsource/xaos/ui/plot/css/modena.css").toExternalForm());
		stage.setScene(scene);
		stage.show();

	}

	@After
	public void tearDown() throws TimeoutException {
		FxToolkit.cleanupStages();
	}

	/**
	 * Test UNDO and REDO on Navigator.
	 */
	@Test
	public void testNavigator() {

		System.out.println("  Testing ''ChartUndoManager'' on Navigator...");

		FxRobot robot = new FxRobot();
		ChartUndoManager undoManager = ChartUndoManager.get(navigator.getChart());

		//	Get chart's reference bounds...
		chartXLowerBound = navigator.getXValueAxis().getLowerBound();
		chartXUpperBound = navigator.getXValueAxis().getUpperBound();
		chartYLowerBound = navigator.getYValueAxis().getLowerBound();
		chartYUpperBound = navigator.getYValueAxis().getUpperBound();

		//	Activate the tool...
		robot.moveTo(chart);
		robot.type(ALT);

		//	Assert initial conditions...
		assertFalse(undoManager.isRedoable());
		assertFalse(undoManager.isUndoable());

		// Testing PAN DOWN...
		System.out.println("    - Testing PAN DOWN...");
		navigatorResetChartMoveAndClick(robot, PAN_DOWN_POINT);
		assertFalse(undoManager.isRedoable());
		assertTrue(undoManager.isUndoable());
		assertThat(navigator.getXValueAxis().getLowerBound()).isEqualTo(chartXLowerBound);
		assertThat(navigator.getXValueAxis().getUpperBound()).isEqualTo(chartXUpperBound);
		assertThat(navigator.getYValueAxis().getLowerBound()).isNotEqualTo(chartYLowerBound);
		assertThat(navigator.getYValueAxis().getUpperBound()).isNotEqualTo(chartYUpperBound);
		navigatorResetChartMoveAndClick(robot, UNDO_POINT, false);
		assertTrue(undoManager.isRedoable());
		assertFalse(undoManager.isUndoable());
		assertThat(navigator.getXValueAxis().getLowerBound()).isEqualTo(chartXLowerBound);
		assertThat(navigator.getXValueAxis().getUpperBound()).isEqualTo(chartXUpperBound);
		assertThat(navigator.getYValueAxis().getLowerBound()).isEqualTo(chartYLowerBound);
		assertThat(navigator.getYValueAxis().getUpperBound()).isEqualTo(chartYUpperBound);
		navigatorResetChartMoveAndClick(robot, REDO_POINT, false);
		assertFalse(undoManager.isRedoable());
		assertTrue(undoManager.isUndoable());
		assertThat(navigator.getXValueAxis().getLowerBound()).isEqualTo(chartXLowerBound);
		assertThat(navigator.getXValueAxis().getUpperBound()).isEqualTo(chartXUpperBound);
		assertThat(navigator.getYValueAxis().getLowerBound()).isNotEqualTo(chartYLowerBound);
		assertThat(navigator.getYValueAxis().getUpperBound()).isNotEqualTo(chartYUpperBound);
		navigatorResetChartMoveAndClick(robot, UNDO_POINT, false);
		assertTrue(undoManager.isRedoable());
		assertFalse(undoManager.isUndoable());

		// Testing PAN DOWN...
		System.out.println("    - Testing PAN UP...");
		navigatorResetChartMoveAndClick(robot, PAN_UP_POINT);
		assertFalse(undoManager.isRedoable());
		assertTrue(undoManager.isUndoable());
		assertThat(navigator.getXValueAxis().getLowerBound()).isEqualTo(chartXLowerBound);
		assertThat(navigator.getXValueAxis().getUpperBound()).isEqualTo(chartXUpperBound);
		assertThat(navigator.getYValueAxis().getLowerBound()).isNotEqualTo(chartYLowerBound);
		assertThat(navigator.getYValueAxis().getUpperBound()).isNotEqualTo(chartYUpperBound);
		navigatorResetChartMoveAndClick(robot, UNDO_POINT, false);
		assertTrue(undoManager.isRedoable());
		assertFalse(undoManager.isUndoable());
		assertThat(navigator.getXValueAxis().getLowerBound()).isEqualTo(chartXLowerBound);
		assertThat(navigator.getXValueAxis().getUpperBound()).isEqualTo(chartXUpperBound);
		assertThat(navigator.getYValueAxis().getLowerBound()).isEqualTo(chartYLowerBound);
		assertThat(navigator.getYValueAxis().getUpperBound()).isEqualTo(chartYUpperBound);
		navigatorResetChartMoveAndClick(robot, REDO_POINT, false);
		assertFalse(undoManager.isRedoable());
		assertTrue(undoManager.isUndoable());
		assertThat(navigator.getXValueAxis().getLowerBound()).isEqualTo(chartXLowerBound);
		assertThat(navigator.getXValueAxis().getUpperBound()).isEqualTo(chartXUpperBound);
		assertThat(navigator.getYValueAxis().getLowerBound()).isNotEqualTo(chartYLowerBound);
		assertThat(navigator.getYValueAxis().getUpperBound()).isNotEqualTo(chartYUpperBound);
		navigatorResetChartMoveAndClick(robot, UNDO_POINT, false);
		assertTrue(undoManager.isRedoable());
		assertFalse(undoManager.isUndoable());

		// Testing PAN LEFT...
		System.out.println("    - Testing PAN LEFT...");
		navigatorResetChartMoveAndClick(robot, PAN_LEFT_POINT);
		assertFalse(undoManager.isRedoable());
		assertTrue(undoManager.isUndoable());
		assertThat(navigator.getXValueAxis().getLowerBound()).isNotEqualTo(chartXLowerBound);
		assertThat(navigator.getXValueAxis().getUpperBound()).isNotEqualTo(chartXUpperBound);
		assertThat(navigator.getYValueAxis().getLowerBound()).isEqualTo(chartYLowerBound);
		assertThat(navigator.getYValueAxis().getUpperBound()).isEqualTo(chartYUpperBound);
		navigatorResetChartMoveAndClick(robot, UNDO_POINT, false);
		assertTrue(undoManager.isRedoable());
		assertFalse(undoManager.isUndoable());
		assertThat(navigator.getXValueAxis().getLowerBound()).isEqualTo(chartXLowerBound);
		assertThat(navigator.getXValueAxis().getUpperBound()).isEqualTo(chartXUpperBound);
		assertThat(navigator.getYValueAxis().getLowerBound()).isEqualTo(chartYLowerBound);
		assertThat(navigator.getYValueAxis().getUpperBound()).isEqualTo(chartYUpperBound);
		navigatorResetChartMoveAndClick(robot, REDO_POINT, false);
		assertFalse(undoManager.isRedoable());
		assertTrue(undoManager.isUndoable());
		assertThat(navigator.getXValueAxis().getLowerBound()).isNotEqualTo(chartXLowerBound);
		assertThat(navigator.getXValueAxis().getUpperBound()).isNotEqualTo(chartXUpperBound);
		assertThat(navigator.getYValueAxis().getLowerBound()).isEqualTo(chartYLowerBound);
		assertThat(navigator.getYValueAxis().getUpperBound()).isEqualTo(chartYUpperBound);
		navigatorResetChartMoveAndClick(robot, UNDO_POINT, false);
		assertTrue(undoManager.isRedoable());
		assertFalse(undoManager.isUndoable());

		// Testing PAN RIGHT...
		System.out.println("    - Testing PAN RIGHT...");
		navigatorResetChartMoveAndClick(robot, PAN_RIGHT_POINT);
		assertFalse(undoManager.isRedoable());
		assertTrue(undoManager.isUndoable());
		assertThat(navigator.getXValueAxis().getLowerBound()).isNotEqualTo(chartXLowerBound);
		assertThat(navigator.getXValueAxis().getUpperBound()).isNotEqualTo(chartXUpperBound);
		assertThat(navigator.getYValueAxis().getLowerBound()).isEqualTo(chartYLowerBound);
		assertThat(navigator.getYValueAxis().getUpperBound()).isEqualTo(chartYUpperBound);
		navigatorResetChartMoveAndClick(robot, UNDO_POINT, false);
		assertTrue(undoManager.isRedoable());
		assertFalse(undoManager.isUndoable());
		assertThat(navigator.getXValueAxis().getLowerBound()).isEqualTo(chartXLowerBound);
		assertThat(navigator.getXValueAxis().getUpperBound()).isEqualTo(chartXUpperBound);
		assertThat(navigator.getYValueAxis().getLowerBound()).isEqualTo(chartYLowerBound);
		assertThat(navigator.getYValueAxis().getUpperBound()).isEqualTo(chartYUpperBound);
		navigatorResetChartMoveAndClick(robot, REDO_POINT, false);
		assertFalse(undoManager.isRedoable());
		assertTrue(undoManager.isUndoable());
		assertThat(navigator.getXValueAxis().getLowerBound()).isNotEqualTo(chartXLowerBound);
		assertThat(navigator.getXValueAxis().getUpperBound()).isNotEqualTo(chartXUpperBound);
		assertThat(navigator.getYValueAxis().getLowerBound()).isEqualTo(chartYLowerBound);
		assertThat(navigator.getYValueAxis().getUpperBound()).isEqualTo(chartYUpperBound);
		navigatorResetChartMoveAndClick(robot, UNDO_POINT, false);
		assertTrue(undoManager.isRedoable());
		assertFalse(undoManager.isUndoable());

		// Testing ZOOM IN...
		System.out.println("    - Testing ZOOM IN...");
		navigatorResetChartMoveAndClick(robot, ZOOM_IN_POINT);
		assertFalse(undoManager.isRedoable());
		assertTrue(undoManager.isUndoable());
		assertThat(navigator.getXValueAxis().getLowerBound()).isNotEqualTo(chartXLowerBound);
		assertThat(navigator.getXValueAxis().getUpperBound()).isNotEqualTo(chartXUpperBound);
		assertThat(navigator.getYValueAxis().getLowerBound()).isNotEqualTo(chartYLowerBound);
		assertThat(navigator.getYValueAxis().getUpperBound()).isNotEqualTo(chartYUpperBound);
		navigatorResetChartMoveAndClick(robot, UNDO_POINT, false);
		assertTrue(undoManager.isRedoable());
		assertFalse(undoManager.isUndoable());
		assertThat(navigator.getXValueAxis().getLowerBound()).isEqualTo(chartXLowerBound);
		assertThat(navigator.getXValueAxis().getUpperBound()).isEqualTo(chartXUpperBound);
		assertThat(navigator.getYValueAxis().getLowerBound()).isEqualTo(chartYLowerBound);
		assertThat(navigator.getYValueAxis().getUpperBound()).isEqualTo(chartYUpperBound);
		navigatorResetChartMoveAndClick(robot, REDO_POINT, false);
		assertFalse(undoManager.isRedoable());
		assertTrue(undoManager.isUndoable());
		assertThat(navigator.getXValueAxis().getLowerBound()).isNotEqualTo(chartXLowerBound);
		assertThat(navigator.getXValueAxis().getUpperBound()).isNotEqualTo(chartXUpperBound);
		assertThat(navigator.getYValueAxis().getLowerBound()).isNotEqualTo(chartYLowerBound);
		assertThat(navigator.getYValueAxis().getUpperBound()).isNotEqualTo(chartYUpperBound);
		navigatorResetChartMoveAndClick(robot, UNDO_POINT, false);
		assertTrue(undoManager.isRedoable());
		assertFalse(undoManager.isUndoable());

		// Testing ZOOM OUT...
		System.out.println("    - Testing ZOOM OUT...");
		navigatorResetChartMoveAndClick(robot, ZOOM_OUT_POINT);
		assertFalse(undoManager.isRedoable());
		assertTrue(undoManager.isUndoable());
		assertThat(navigator.getXValueAxis().getLowerBound()).isNotEqualTo(chartXLowerBound);
		assertThat(navigator.getXValueAxis().getUpperBound()).isNotEqualTo(chartXUpperBound);
		assertThat(navigator.getYValueAxis().getLowerBound()).isNotEqualTo(chartYLowerBound);
		assertThat(navigator.getYValueAxis().getUpperBound()).isNotEqualTo(chartYUpperBound);
		navigatorResetChartMoveAndClick(robot, UNDO_POINT, false);
		assertTrue(undoManager.isRedoable());
		assertFalse(undoManager.isUndoable());
		assertThat(navigator.getXValueAxis().getLowerBound()).isEqualTo(chartXLowerBound);
		assertThat(navigator.getXValueAxis().getUpperBound()).isEqualTo(chartXUpperBound);
		assertThat(navigator.getYValueAxis().getLowerBound()).isEqualTo(chartYLowerBound);
		assertThat(navigator.getYValueAxis().getUpperBound()).isEqualTo(chartYUpperBound);
		navigatorResetChartMoveAndClick(robot, REDO_POINT, false);
		assertFalse(undoManager.isRedoable());
		assertTrue(undoManager.isUndoable());
		assertThat(navigator.getXValueAxis().getLowerBound()).isNotEqualTo(chartXLowerBound);
		assertThat(navigator.getXValueAxis().getUpperBound()).isNotEqualTo(chartXUpperBound);
		assertThat(navigator.getYValueAxis().getLowerBound()).isNotEqualTo(chartYLowerBound);
		assertThat(navigator.getYValueAxis().getUpperBound()).isNotEqualTo(chartYUpperBound);
		navigatorResetChartMoveAndClick(robot, UNDO_POINT, false);
		assertTrue(undoManager.isRedoable());
		assertFalse(undoManager.isUndoable());

		// Testing ZOOM TO ONE...
		System.out.println("    - Testing ZOOM TO ONE...");
		navigatorResetChartMoveAndClick(robot, ZOOM_OUT_POINT);
		navigatorResetChartMoveAndClick(robot, ZOOM_TO_ONE_POINT, false);
		assertFalse(undoManager.isRedoable());
		assertTrue(undoManager.isUndoable());
		assertThat(navigator.getXValueAxis().getLowerBound()).isEqualTo(chartXLowerBound);
		assertThat(navigator.getXValueAxis().getUpperBound()).isEqualTo(chartXUpperBound);
		assertThat(navigator.getYValueAxis().getLowerBound()).isEqualTo(chartYLowerBound);
		assertThat(navigator.getYValueAxis().getUpperBound()).isEqualTo(chartYUpperBound);
		navigatorResetChartMoveAndClick(robot, UNDO_POINT, false);
		assertTrue(undoManager.isRedoable());
		assertTrue(undoManager.isUndoable());
		assertThat(navigator.getXValueAxis().getLowerBound()).isNotEqualTo(chartXLowerBound);
		assertThat(navigator.getXValueAxis().getUpperBound()).isNotEqualTo(chartXUpperBound);
		assertThat(navigator.getYValueAxis().getLowerBound()).isNotEqualTo(chartYLowerBound);
		assertThat(navigator.getYValueAxis().getUpperBound()).isNotEqualTo(chartYUpperBound);
		navigatorResetChartMoveAndClick(robot, REDO_POINT, false);
		assertFalse(undoManager.isRedoable());
		assertTrue(undoManager.isUndoable());
		assertThat(navigator.getXValueAxis().getLowerBound()).isEqualTo(chartXLowerBound);
		assertThat(navigator.getXValueAxis().getUpperBound()).isEqualTo(chartXUpperBound);
		assertThat(navigator.getYValueAxis().getLowerBound()).isEqualTo(chartYLowerBound);
		assertThat(navigator.getYValueAxis().getUpperBound()).isEqualTo(chartYUpperBound);
		navigatorResetChartMoveAndClick(robot, UNDO_POINT, false);
		assertTrue(undoManager.isRedoable());
		assertTrue(undoManager.isUndoable());
		assertThat(navigator.getXValueAxis().getLowerBound()).isNotEqualTo(chartXLowerBound);
		assertThat(navigator.getXValueAxis().getUpperBound()).isNotEqualTo(chartXUpperBound);
		assertThat(navigator.getYValueAxis().getLowerBound()).isNotEqualTo(chartYLowerBound);
		assertThat(navigator.getYValueAxis().getUpperBound()).isNotEqualTo(chartYUpperBound);
		navigatorResetChartMoveAndClick(robot, UNDO_POINT, false);
		assertTrue(undoManager.isRedoable());
		assertFalse(undoManager.isUndoable());
		assertThat(navigator.getXValueAxis().getLowerBound()).isEqualTo(chartXLowerBound);
		assertThat(navigator.getXValueAxis().getUpperBound()).isEqualTo(chartXUpperBound);
		assertThat(navigator.getYValueAxis().getLowerBound()).isEqualTo(chartYLowerBound);
		assertThat(navigator.getYValueAxis().getUpperBound()).isEqualTo(chartYUpperBound);
		assertTrue(undoManager.isRedoable());
		assertFalse(undoManager.isUndoable());

		//	Testing multiple operations and undo/redo
		System.out.println("    - Testing multiple operation and undo/redo...");
		navigatorResetChartMoveAndClick(robot, ZOOM_OUT_POINT);
		assertThat(undoManager.getAvailableRedoables()).isEqualTo(0);
		assertThat(undoManager.getAvailableUndoables()).isEqualTo(1);
		navigatorResetChartMoveAndClick(robot, PAN_DOWN_POINT, false);
		assertThat(undoManager.getAvailableRedoables()).isEqualTo(0);
		assertThat(undoManager.getAvailableUndoables()).isEqualTo(2);
		navigatorResetChartMoveAndClick(robot, PAN_DOWN_POINT, false);
		assertThat(undoManager.getAvailableRedoables()).isEqualTo(0);
		assertThat(undoManager.getAvailableUndoables()).isEqualTo(3);
		navigatorResetChartMoveAndClick(robot, PAN_LEFT_POINT, false);
		assertThat(undoManager.getAvailableRedoables()).isEqualTo(0);
		assertThat(undoManager.getAvailableUndoables()).isEqualTo(4);
		navigatorResetChartMoveAndClick(robot, ZOOM_OUT_POINT, false);
		assertThat(undoManager.getAvailableRedoables()).isEqualTo(0);
		assertThat(undoManager.getAvailableUndoables()).isEqualTo(5);
		navigatorResetChartMoveAndClick(robot, PAN_DOWN_POINT, false);
		assertThat(undoManager.getAvailableRedoables()).isEqualTo(0);
		assertThat(undoManager.getAvailableUndoables()).isEqualTo(6);
		navigatorResetChartMoveAndClick(robot, PAN_LEFT_POINT, false);
		assertThat(undoManager.getAvailableRedoables()).isEqualTo(0);
		assertThat(undoManager.getAvailableUndoables()).isEqualTo(7);
		navigatorResetChartMoveAndClick(robot, UNDO_POINT, false);
		assertThat(undoManager.getAvailableRedoables()).isEqualTo(1);
		assertThat(undoManager.getAvailableUndoables()).isEqualTo(6);
		navigatorResetChartMoveAndClick(robot, UNDO_POINT, false);
		assertThat(undoManager.getAvailableRedoables()).isEqualTo(2);
		assertThat(undoManager.getAvailableUndoables()).isEqualTo(5);
		navigatorResetChartMoveAndClick(robot, UNDO_POINT, false);
		assertThat(undoManager.getAvailableRedoables()).isEqualTo(3);
		assertThat(undoManager.getAvailableUndoables()).isEqualTo(4);
		navigatorResetChartMoveAndClick(robot, REDO_POINT, false);
		assertThat(undoManager.getAvailableRedoables()).isEqualTo(2);
		assertThat(undoManager.getAvailableUndoables()).isEqualTo(5);
		navigatorResetChartMoveAndClick(robot, PAN_DOWN_POINT, false);
		assertThat(undoManager.getAvailableRedoables()).isEqualTo(0);
		assertThat(undoManager.getAvailableUndoables()).isEqualTo(6);

	}

	private void navigatorResetChartMoveAndClick ( FxRobot robot, Point2D offset ) {
		navigatorResetChartMoveAndClick(robot, offset, true);
	}

	private void navigatorResetChartMoveAndClick ( FxRobot robot, Point2D offset, boolean reset ) {

		if ( reset ) {
			navigator.getXValueAxis().setLowerBound(chartXLowerBound);
			navigator.getXValueAxis().setUpperBound(chartXUpperBound);
			navigator.getYValueAxis().setLowerBound(chartYLowerBound);
			navigator.getYValueAxis().setUpperBound(chartYUpperBound);
		}

		robot.moveTo(chart, CENTER, offset, DEFAULT);
		robot.clickOn(PRIMARY);

	}

	private ObservableList<XYChart.Data<Number, Number>> generateData( int pointsCount ) {

		int[] yValues = generateIntArray(0, 5, pointsCount);
		List<XYChart.Data<Number, Number>> data = new ArrayList<>(pointsCount);

		for ( int i = 0; i < yValues.length; i++ ) {
			data.add(new XYChart.Data<>(i, yValues[i]));
		}

		return FXCollections.observableArrayList(data);

	}

	private int[] generateIntArray( int firstValue, int variance, int size ) {

		int[] data = new int[size];

		data[0] = firstValue;

		for ( int i = 1; i < data.length; i++ ) {

			int sign = RANDOM.nextBoolean() ? 1 : -1;

			data[i] = data[i - 1] + (int) ( variance * RANDOM.nextDouble() ) * sign;

		}

		return data;

	}

}
