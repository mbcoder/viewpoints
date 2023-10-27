/**
 * Copyright 2019 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.mycompany.app;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class App extends Application {

    private MapView mapView;
    private Point gpsPoint;
    private Point findPoint;
    private Graphic findGraphic;

    public static void main(String[] args) {

        Application.launch(args);
    }

    @Override
    public void start(Stage stage) {

        // set the title and size of the stage and show it
        stage.setTitle("My Map App");
        stage.setWidth(800);
        stage.setHeight(700);
        stage.show();

        // create a JavaFX scene with a stack pane as the root node and add it to the scene
        BorderPane borderPane = new BorderPane();

        Scene scene = new Scene(borderPane);
        stage.setScene(scene);

        // Note: it is not best practice to store API keys in source code.
        // An API key is required to enable access to services, web maps, and web scenes hosted in ArcGIS Online.
        // If you haven't already, go to your developer dashboard to get your API key.
        // Please refer to https://developers.arcgis.com/java/get-started/ for more information
        // authentication with an API key or named user is required to access basemaps and other location services
        String yourAPIKey = System.getProperty("apiKey");
        ArcGISRuntimeEnvironment.setApiKey(yourAPIKey);

        // create a MapView to display the map and add it to the stack pane
        mapView = new MapView();
        borderPane.setCenter(mapView);

        HBox hBox = new HBox();
        Slider direction = new Slider(-180,180,0);
        Button drawExtent = new Button("Draw Extent");
        drawExtent.setOnAction(event -> {
            extentGeometry(gpsPoint, findPoint, direction.getValue());
        });
        Button updateExtent = new Button("Update Extent");
        hBox.getChildren().addAll(direction, drawExtent, updateExtent);

        // Graphics overlay for GPS and item we are trying to find
        GraphicsOverlay graphicsOverlay = new GraphicsOverlay();
        mapView.getGraphicsOverlays().add(graphicsOverlay);

        // hard coded GPS position
        gpsPoint = new Point(-2.6,56, SpatialReferences.getWgs84());
        SimpleMarkerSymbol blueMarker = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.BLUE, 10);
        Graphic gpsGraphic = new Graphic(gpsPoint, blueMarker);
        graphicsOverlay.getGraphics().add(gpsGraphic);

        // initial find point
        SimpleMarkerSymbol greenMarker = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.GREEN, 10);
        findPoint = new Point(-2.602, 56.002, SpatialReferences.getWgs84());
        findGraphic = new Graphic(findPoint,greenMarker);
        graphicsOverlay.getGraphics().add(findGraphic);


        // click on map to place location
        mapView.setOnMouseClicked(event -> {
            System.out.println("clicked on map");
            Point2D screenPoint = new Point2D(event.getX(), event.getY());
            findPoint = (Point) GeometryEngine.project(mapView.screenToLocation(screenPoint), SpatialReferences.getWgs84());

            findGraphic.setGeometry(findPoint);
        } );




        borderPane.setTop(hBox);

        // create an ArcGISMap with an imagery basemap
        ArcGISMap map = new ArcGISMap(BasemapStyle.ARCGIS_IMAGERY);

        // display the map by setting the map on the map view
        mapView.setMap(map);

        // initial viewpoint
        mapView.setViewpointCenterAsync(gpsPoint, 10000);

    }

    private void extentGeometry(Point gpsPoint, Point findPoint, double value) {

        // distance in metres
        double distance = GeometryEngine.distanceBetween(
            GeometryEngine.project(gpsPoint, SpatialReferences.getWebMercator()),
            GeometryEngine.project(findPoint, SpatialReferences.getWebMercator()));
        System.out.println("distance = " + distance);


    }

    /**
     * Stops and releases all resources used in application.
     */
    @Override
    public void stop() {

        if (mapView != null) {
            mapView.dispose();
        }
    }
}
