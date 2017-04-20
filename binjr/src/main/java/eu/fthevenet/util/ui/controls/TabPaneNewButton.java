package eu.fthevenet.util.ui.controls;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * A TabPane container with a button to add a new tab
 *
 * @author Frederic Thevenet
 */
public class TabPaneNewButton extends TabPane {
    private static final Logger logger = LogManager.getLogger(TabPaneNewButton.class);
    private Supplier<Optional<Tab>> newTabFactory = () -> Optional.of(new Tab());

    public TabPaneNewButton() {
        this((Tab[]) null);
    }

    public TabPaneNewButton(Tab... tabs) {
        super(tabs);

        Platform.runLater(this::positionNewTabButton);

        // Prepare to change the button on screen position if the tabs side changes
        sideProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                positionNewTabButton();
            }
        });
    }


    private void positionNewTabButton() {
        Pane tabHeaderBg = (Pane) this.lookup(".tab-header-background");
        if (tabHeaderBg == null) {
            // TabPane is not ready
            return;
        }
        Pane tabHeaderArea = (Pane) this.lookup(".tab-header-area");
        logger.debug("tabHeaderArea.getHeight() = " + tabHeaderArea.getHeight());
        Button newTabButton = (Button) tabHeaderBg.lookup("#newTabButton");

        // Remove the button if it was already present
        if (newTabButton != null) {
            tabHeaderBg.getChildren().remove(newTabButton);
        }
        newTabButton = new Button();
        newTabButton.setId("newTabButton");
        newTabButton.setFocusTraversable(false);
        Pane headersRegion = (Pane) this.lookup(".headers-region");
        logger.debug("headersRegion.getHeight() = " + headersRegion.getHeight());
        logger.debug("headersRegion.getPrefHeight = " + headersRegion.getPrefHeight());
        newTabButton.getStyleClass().add("add-tab-button");
        SVGPath icon = new SVGPath();
        icon.setContent("m 31.25,54.09375 0,2.4375 -2.46875,0 0,0.375 2.46875,0 0,2.46875 0.375,0 0,-2.46875 2.46875,0 0,-0.375 -2.46875,0 0,-2.4375 -0.375,0 z");
        icon.getStyleClass().add("add-tab-button-icon");
        newTabButton.setGraphic(icon);
        newTabButton.setAlignment(Pos.CENTER);
        newTabButton.setOnAction(event -> {
            newTabFactory.get().ifPresent(newTab -> {
                getTabs().add(newTab);
                this.getSelectionModel().select(newTab);
            });
        });


        tabHeaderBg.getChildren().add(newTabButton);
        StackPane.setAlignment(newTabButton, Pos.CENTER_LEFT);
        //  StackPane.setMargin(newTabButton, new Insets(2));

        switch (getSide()) {
            case TOP:
                newTabButton.translateXProperty().bind(
                        headersRegion.widthProperty()//.add(5)
                );
                break;
            case LEFT:
                newTabButton.translateXProperty().bind(
                        tabHeaderBg.widthProperty()
                                .subtract(headersRegion.widthProperty())
                                .subtract(newTabButton.widthProperty())
                        // .subtract(1)
                );
                break;
            case BOTTOM:
                newTabButton.translateXProperty().bind(
                        tabHeaderBg.widthProperty()
                                .subtract(headersRegion.widthProperty())
                                .subtract(newTabButton.widthProperty())
                        // .subtract(5)
                );
                break;
            case RIGHT:
                newTabButton.translateXProperty().bind(
                        headersRegion.widthProperty()//.add(5)
                );
                break;
        }
    }

    public Supplier<Optional<Tab>> getNewTabFactory() {
        return newTabFactory;
    }

    public void setNewTabFactory(Supplier<Optional<Tab>> newTabFactory) {
        this.newTabFactory = newTabFactory;
    }
}