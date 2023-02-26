package view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.Model;
import viewModel.ViewModelController;

public class Main extends Application {


    @Override
    public void start(Stage primaryStage) throws Exception {
        //added
        Model m = new Model();
        m.writeToXML(m.properties);
        ViewModelController vmc = new ViewModelController(m);

        FXMLLoader fxl = new FXMLLoader();
        Parent root = fxl.load(getClass().getResource("sample.fxml").openStream());
        primaryStage.setTitle("Flight Gear Simulator");
        ControllerView controllerView = fxl.getController();
        controllerView.init(vmc);

        primaryStage.setScene(new Scene(root, 970, 630));
        primaryStage.show();


    }


    public static void main(String[] args) {
        launch(args);
    }
}
