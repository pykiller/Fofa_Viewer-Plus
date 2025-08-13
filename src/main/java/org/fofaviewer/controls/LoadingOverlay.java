package org.fofaviewer.controls;

import javafx.geometry.Pos;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class LoadingOverlay extends StackPane {

    public LoadingOverlay() {
        // Create a semi-transparent background
        Rectangle background = new Rectangle();
        background.setFill(Color.BLACK);
        background.setOpacity(0.4);

        // Bind background size to the overlay size
        background.widthProperty().bind(this.widthProperty());
        background.heightProperty().bind(this.heightProperty());

        // Create a loading indicator
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxSize(60, 60);

        // Add background and indicator to the pane
        this.getChildren().addAll(background, progressIndicator);
        this.setAlignment(Pos.CENTER);
    }
}
