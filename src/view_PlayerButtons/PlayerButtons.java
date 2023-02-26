package view_PlayerButtons;

import javafx.beans.property.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.*;

import java.io.IOException;

public class PlayerButtons extends AnchorPane {
    public DoubleProperty sliderTime, choiceSpeed;
            public  StringProperty timeFlight, miliSec, seconds, minutes;
    public BooleanProperty onOpenCSVTrain, onOpenCSVTest, onOpenXML, onPlay, onPause, onSpeed, onStop,
            onRewind, onForward,onAnomalyDetector;

    public PlayerButtons() {
        super();
        FXMLLoader fxl = new FXMLLoader();
        sliderTime = new SimpleDoubleProperty();
        choiceSpeed = new SimpleDoubleProperty();

        // Clock:
        miliSec = new SimpleStringProperty();
        seconds = new SimpleStringProperty();
        minutes = new SimpleStringProperty();
        timeFlight = new SimpleStringProperty();

        // Buttons:
        onOpenCSVTrain = new SimpleBooleanProperty();
        onOpenCSVTest = new SimpleBooleanProperty();
        onOpenXML = new SimpleBooleanProperty();
        onAnomalyDetector = new SimpleBooleanProperty();
        onPlay = new SimpleBooleanProperty();
        onPause = new SimpleBooleanProperty();
        onSpeed = new SimpleBooleanProperty();
        onStop = new SimpleBooleanProperty();
        onRewind = new SimpleBooleanProperty();
        onForward = new SimpleBooleanProperty();

        onOpenCSVTrain.setValue(false);
        onOpenCSVTest.setValue(false);
        onOpenXML.setValue(false);
        onAnomalyDetector.setValue(false);
        onPlay.setValue(false);
        onPause.setValue(false);
        onSpeed.setValue(false);
        onStop.setValue(false);
        onRewind.setValue(false);
        onForward.setValue(false);

        try {
            AnchorPane buttons= fxl.load(getClass().getResource("PlayerButtons.fxml").openStream());
            PlayerButtonsController pbc= fxl.getController();
            pbc.init();

            pbc.miliSec.textProperty().bind(miliSec);
            pbc.seconds.textProperty().bind(seconds);
            pbc.minutes.textProperty().bind(minutes);

            pbc.sliderTime.valueProperty().bindBidirectional(sliderTime);
            choiceSpeed.bind(pbc.choiceSpeed.valueProperty());

            onOpenCSVTrain.bind(pbc.onOpenCSVTrain);
            onOpenCSVTest.bind(pbc.onOpenCSVTest);
            onOpenXML.bind(pbc.onOpenXML);
            onAnomalyDetector.bind(pbc.onAnomalyDetector);
            onPlay.bind(pbc.onPlay);
            onPause.bind(pbc.onPause);
            onSpeed.bind(pbc.onSpeed);
            onStop.bind(pbc.onStop);
            onRewind.bind(pbc.onRewind);
            onForward.bind(pbc.onForward);

            this.getChildren().add(buttons);

        } catch (IOException e) {}
    }
}


