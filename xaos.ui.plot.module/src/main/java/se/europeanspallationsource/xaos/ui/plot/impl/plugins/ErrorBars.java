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
package se.europeanspallationsource.xaos.ui.plot.impl.plugins;


import chart.BarChartFX;
import chart.HistogramChartFX;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.Chart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.input.MouseEvent;
import org.apache.commons.lang3.Validate;
import se.europeanspallationsource.xaos.ui.plot.plugins.AbstractBoundedPlugin;
import se.europeanspallationsource.xaos.ui.plot.util.ErrorSeries;
import se.europeanspallationsource.xaos.ui.plot.util.ErrorSeries.ErrorData;


/**
 * Shows horizontal and vertical error bars around chart data points.
 *
 * @param <X> The X type of the series.
 * @param <Y> The Y type of the series.
 * @author claudio.rosati@esss.se
 */
@SuppressWarnings( "ClassWithoutLogger" )
public class ErrorBars<X, Y> extends AbstractBoundedPlugin {

	private final ErrorSeries<X, Y> errorData;
	private Series<?, ?> series = null;

	/**
	 * @param errorData      List of error data for display in chart
	 * @param indexRefSeries Index of series to must own the error data. Note
	 *                       that this value will be set to the given error
	 *                       series, overwriting any previously set value.
	 * @throws NullPointerException If {@code
	 */
	public ErrorBars( ErrorSeries<X, Y> errorData, int indexRefSeries ) {
		
		Validate.notNull(errorData, "Null 'errorData' parameter.");

		this.errorData = errorData;

		errorData.setSeriesRef(indexRefSeries);

	}

	@Override
	public <X, Y> void seriesVisibilityUpdated( Chart chart, Series<X, Y> series, int index, boolean visible ) {
		if ( this.series == series && visible ) {
			drawErrorBars();
		} else {
			hideErrorBars();
		}
	}

	@Override
	protected void boundsChanged() {
		if ( series != null ) {
			seriesVisibilityUpdated(getChart(), series, errorData.getSeriesRef(), isSeriesVisible(series));
		}
	}

	@Override
	protected void chartConnected( Chart chart ) {

		super.chartConnected(chart);

		errorData.getData().forEach(marker -> {

			ObservableList<Node> plotChildren = getPlotChildren();

			if ( marker.isXErrorValid() ) {
				plotChildren.add(marker.getXErrorPath());
			}

			if ( marker.isYErrorValid() ) {
				plotChildren.add(marker.getYErrorPath());
			}

		});

		int index = errorData.getSeriesRef();

		series = ((XYChart<?, ?>) chart).getData().get(index);
		
		seriesVisibilityUpdated(chart, series, index, isSeriesVisible(series));

	}

	@Override
	protected void chartDisconnected( Chart chart ) {

		errorData.getData().forEach(marker -> {

			ObservableList<Node> plotChildren = getPlotChildren();

			if ( marker.isXErrorValid() ) {
				plotChildren.remove(marker.getXErrorPath());
			}

			if ( marker.isYErrorValid() ) {
				plotChildren.remove(marker.getYErrorPath());
			}

		});

		series = null;

		super.chartDisconnected(chart);

	}

	/**
	 * drawErrorBars redraws the error bars already connected to the chart.
	 * Note: Horrible hack in order to collect the display info with function toDisplayPoint(). The other data point is only included
	 * in order for the format to work.
	 */
	private void drawErrorBars() {

		if ( series == null ) {
			return;
		}

		xyError.forEach(series -> {
			series.getData().forEach(data -> {
				data.resetLine();
			});
		});

		for ( ErrorSeries<X, Y> errorSeries : xyError ) {
			XYChart.Series<?, ?> displaySeries = ( (XYChart<?, ?>) getChart() ).getData().get(errorSeries.getSeriesRef());
			for ( Data<?, ?> displayData : displaySeries.getData() ) {
				for ( ErrorData<X, Y> data : errorSeries.getData() ) {
					if ( data.isData(displayData) ) {
						data.getXErrorLine().setStartX(toDisplayPoint(new Data(data.getXErrorBar().get(0), data.getYErrorBar().get(0))).getX());
						data.getXErrorLine().setEndX(toDisplayPoint(new Data<>(data.getXErrorBar().get(1), data.getYErrorBar().get(1))).getX());
						data.getXErrorLine().setStartY(toDisplayPoint(data.getDataPoint()).getY());

						if ( ( (XYChart<?, ?>) getChart() ).getYAxis() instanceof CategoryAxis ) {
							data.getXErrorLine().setStartY(displayData.getNode().getBoundsInParent().getMaxY() - displayData.getNode().getBoundsInParent().getHeight() / 2);
						}

						data.getXErrorLine().setEndY(data.getXErrorLine().getStartY());
						data.getXErrorLine().setStyle("-fx-stroke: -color" + errorSeries.getSeriesRef() + "; -fx-stroke-width: 2px;");

						data.getYErrorLine().setStartY(toDisplayPoint(new Data(data.getXErrorBar().get(0), data.getYErrorBar().get(0))).getY());
						data.getYErrorLine().setEndY(toDisplayPoint(new Data(data.getXErrorBar().get(1), data.getYErrorBar().get(1))).getY());
						data.getYErrorLine().setStartX(toDisplayPoint(data.getDataPoint()).getX());

						if ( ( (XYChart<?, ?>) getChart() ).getXAxis() instanceof CategoryAxis ) {
							data.getYErrorLine().setStartX(displayData.getNode().getBoundsInParent().getMaxX() - displayData.getNode().getBoundsInParent().getWidth() / 2);
						}
						data.getYErrorLine().setEndX(data.getYErrorLine().getStartX());
						data.getYErrorLine().setStyle("-fx-stroke: -color" + errorSeries.getSeriesRef() + "; -fx-stroke-width: 2px;");

						if ( ( ( (XYChart<?, ?>) getChart() ) instanceof BarChartFX ) || ( ( (XYChart<?, ?>) getChart() ) instanceof HistogramChartFX ) ) {
							data.getYErrorLine().setStyle("-fx-stroke: black; -fx-stroke-width: 2px;");
							data.getXErrorLine().setStyle("-fx-stroke: black; -fx-stroke-width: 2px;");
						}
					}
				}
			}
		}
		getLegendChart().getItems().forEach(legendItem -> legendItem.getSymbol().removeEventHandler(MouseEvent.MOUSE_CLICKED, clikcMoveHandler));
		getLegendChart().getItems().forEach(legendItem -> legendItem.getSymbol().addEventHandler(MouseEvent.MOUSE_CLICKED, clikcMoveHandler));


//		errorData.getData().forEach(marker -> {
//			if ( marker.isXErrorValid() ) {
//				marker.getXErrorPath().setVisible(true);
//			}
//			if ( marker.isYErrorValid() ) {
//				marker.getYErrorPath().setVisible(true);
//			}
//		});




	}

	private void hideErrorBars() {

		if ( series == null ) {
			return;
		}

		errorData.getData().forEach(marker -> {
			if ( marker.isXErrorValid() ) {
				marker.getXErrorPath().setVisible(false);
			}
			if ( marker.isYErrorValid() ) {
				marker.getYErrorPath().setVisible(false);
			}
		});

	}

}
