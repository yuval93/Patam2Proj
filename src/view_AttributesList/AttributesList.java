package view_AttributesList;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

public class AttributesList extends AnchorPane {

    public ObservableList lst;
    public StringProperty chosenAttribute;
    public AttributesListController alc;

    public AttributesList() {
        super();
        FXMLLoader fxl = new FXMLLoader();
        lst = FXCollections.observableArrayList();
        chosenAttribute = new SimpleStringProperty();
        try {
            AnchorPane list = fxl.load(getClass().getResource("AttributesList.fxml").openStream());
            alc = fxl.getController();

        chosenAttribute.bind(alc.lv.getSelectionModel().selectedItemProperty());

            this.getChildren().add(list);
        } catch (IOException e) {
        }
    }
}