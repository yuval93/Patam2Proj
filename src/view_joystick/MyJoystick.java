package view_joystick;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;

import java.io.IOException;


public class MyJoystick extends BorderPane {

    public DoubleProperty aileron, elevators, rudder, throttle;
    MyJoystickController mjc;

    public MyJoystick() {

        super();
        FXMLLoader fxl = new FXMLLoader();
        aileron = new SimpleDoubleProperty();
        elevators = new SimpleDoubleProperty();
        rudder = new SimpleDoubleProperty();
        throttle = new SimpleDoubleProperty();
        try {
            BorderPane joy = fxl.load(getClass().getResource("MyJoystick.fxml").openStream());
            mjc = fxl.getController();

            mjc.rudder.valueProperty().bind(rudder);
            mjc.throttle.valueProperty().bind(throttle);

            //add aileron and elevators
            mjc.jx.bind(aileron);
            mjc.jy.bind(throttle);
            this.getChildren().add(joy);

        } catch (IOException e) {}
    }
}
