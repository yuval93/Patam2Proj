package view_TimeBoard;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

public class TimeBoard extends AnchorPane {

    public StringProperty altimeter, airSpeed, fd;
    public DoubleProperty pitch, pitchMax, pitchMin, roll, rollMax, rollMin, yaw, yawMax, yawMin;

    public TimeBoard() {
        super();
        FXMLLoader fxl = new FXMLLoader();
        altimeter = new SimpleStringProperty();
        airSpeed = new SimpleStringProperty();
        fd = new SimpleStringProperty();

        pitch = new SimpleDoubleProperty();
        pitchMax = new SimpleDoubleProperty();
        pitchMin = new SimpleDoubleProperty();

        roll = new SimpleDoubleProperty();
        rollMax = new SimpleDoubleProperty();
        rollMin = new SimpleDoubleProperty();

        yaw = new SimpleDoubleProperty();
        yawMax = new SimpleDoubleProperty();
        yawMin = new SimpleDoubleProperty();

        try {
            AnchorPane times = fxl.load(getClass().getResource("TimeBoard.fxml").openStream());
            TimeBoardController tbc = fxl.getController();
            this.getChildren().add(times);

            tbc.altimeter.textProperty().bind(altimeter);
            tbc.airSpeed.textProperty().bind(airSpeed);
            tbc.fd.textProperty().bind(fd);

            this.pitch.addListener((o, ov, nv) -> tbc.pitch.setValue(pitch.doubleValue()));
            pitchMax.addListener((o, ov, nv) -> tbc.pitch.setMaxValue(nv.doubleValue()));
            pitchMin.addListener((o, ov, nv) -> tbc.pitch.setMinValue(nv.doubleValue()));

            this.roll.addListener((o, ov, nv) -> tbc.roll.setValue(roll.doubleValue()));
            rollMax.addListener((o, ov, nv) -> tbc.roll.setMaxValue(nv.doubleValue()));
            rollMin.addListener((o, ov, nv) -> tbc.roll.setMinValue(nv.doubleValue()));

            this.yaw.addListener((o, ov, nv) -> tbc.yaw.setValue(yaw.doubleValue()));
            yawMax.addListener((o, ov, nv) -> tbc.yaw.setMaxValue(nv.doubleValue()));
            yawMin.addListener((o, ov, nv) -> tbc.yaw.setMinValue(nv.doubleValue()));


        } catch (IOException e) {}
    }
}
