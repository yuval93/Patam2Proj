package viewModel;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import model.Model;

import java.io.File;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Callable;

public class ViewModelController extends Observable implements Observer {

    Model m;
    public Clock clock;
    public TimeSeries ts_reg, ts_Anomal;    //ts-reg
    public DoubleProperty timeStamp, timeStampGraph;
    public DoubleProperty throttle, rudder, aileron, elevators;     // Joystick
    public DoubleProperty sliderTime, choiceSpeed;  // Player buttons
    public DoubleProperty pitch, pitchMax, pitchMin, roll, rollMax, rollMin, yaw, yawMax, yawMin;   //Time board
    public StringProperty altimeter, airSpeed, fd;  // Time board
    public DoubleProperty valueAxis, valueCorrelate;    // Graphs
    public StringProperty timeFlight, chosenAttribute, correlateFeature;
    public IntegerProperty sizeTS;

    public ObservableList<String> attributeList;

    public int numberOfCorrelateAttribute;
    public Boolean xmlFile, csvTestFile, csvTrainFile, algoFile;

    public ViewModelController(Model m) {
        this.m = m;
        clock = new Clock();
        m.addObserver(this);    //add Model as Observable

        xmlFile = false;
        csvTestFile = false;
        csvTrainFile = false;
        algoFile = false;

        timeStamp = new SimpleDoubleProperty();
        timeStampGraph = new SimpleDoubleProperty();

        //Joystick:
        aileron = new SimpleDoubleProperty();
        elevators = new SimpleDoubleProperty();
        rudder = new SimpleDoubleProperty();
        throttle = new SimpleDoubleProperty();
        sliderTime = new SimpleDoubleProperty();
        choiceSpeed = new SimpleDoubleProperty();

        // Time board:
        pitch = new SimpleDoubleProperty();
        pitchMax = new SimpleDoubleProperty();
        pitchMin = new SimpleDoubleProperty();
        roll = new SimpleDoubleProperty();
        rollMax = new SimpleDoubleProperty();
        rollMin = new SimpleDoubleProperty();
        yaw = new SimpleDoubleProperty();
        yawMax = new SimpleDoubleProperty();
        yawMin = new SimpleDoubleProperty();
        altimeter = new SimpleStringProperty();
        airSpeed = new SimpleStringProperty();
        fd = new SimpleStringProperty();

        // Graphs:
        valueAxis = new SimpleDoubleProperty();
        valueCorrelate = new SimpleDoubleProperty();
        chosenAttribute = new SimpleStringProperty();
        correlateFeature = new SimpleStringProperty();
        chosenAttribute.setValue("0");
        correlateFeature.setValue("0");

        timeFlight = new SimpleStringProperty();

        sizeTS = new SimpleIntegerProperty();

        attributeList = FXCollections.observableArrayList();

        choiceSpeed.addListener((o, ov, nv) -> {
            speedPlay();
        });

        sliderTime.addListener((o, ov, nv) -> {
            timeStamp.setValue(nv.doubleValue());
            m.setTime(nv.doubleValue());
            clock.update(nv.intValue() - ov.intValue());
        });

        timeStamp.addListener((o, ov, nv) -> {
            updateDisplayVariables(nv.intValue());
            if(algoFile == true)
                m.setVarivablesTOALG();     //updating the date for the alg graph
        });

        chosenAttribute.addListener((o, ov, nv) -> {
            m.attribute1.bind(chosenAttribute);
            if(algoFile == true)
                m.setVarivablesNamesTOALG();
        });
    }

    public void updateDisplayVariables(int time) {
        DecimalFormat df = new DecimalFormat("0.0##");
        df.setRoundingMode(RoundingMode.DOWN);

        sliderTime.setValue(time);
        timeFlight.setValue(String.valueOf(time));
        aileron.setValue(ts_Anomal.getValueByTime(m.attributeMap.get("aileron").associativeName, time));
        elevators.setValue(ts_Anomal.getValueByTime(m.attributeMap.get("elevators").associativeName, time));
        rudder.setValue(ts_Anomal.getValueByTime(m.attributeMap.get("rudder").associativeName, time));
        throttle.setValue(ts_Anomal.getValueByTime(m.attributeMap.get("throttle").associativeName, time));
        altimeter.setValue(String.valueOf(df.format(ts_Anomal.getValueByTime(m.attributeMap.get("altimeter").associativeName, time))));
        airSpeed.setValue(String.valueOf(df.format(ts_Anomal.getValueByTime(m.attributeMap.get("airSpeed").associativeName, time))));
        fd.setValue(String.valueOf(df.format(ts_Anomal.getValueByTime(m.attributeMap.get("fd").associativeName, time))));
        pitch.setValue(ts_Anomal.getValueByTime(m.attributeMap.get("pitch").associativeName, time));
        roll.setValue(ts_Anomal.getValueByTime(m.attributeMap.get("roll").associativeName, time));
        yaw.setValue(ts_Anomal.getValueByTime(m.attributeMap.get("yaw").associativeName, time));

        /*
            To update the specific chosen attribute
            getting the number of the chosen attribute
            numberOfSpecAttribute = ts.getIndexOfAttribute(chosenAttribute.getValue());
            updating by binding the value of the chosen attribute
         */
        valueAxis.setValue(ts_Anomal.getValueByTime(chosenAttribute.getValue(), time));

        //  Init the name of the correlate attribute
        correlateFeature=getCorrelateFeature();

        //  Getting the col's number of the correlate attribute
        if (correlateFeature.getValue() != null) {
            valueCorrelate.setValue(ts_Anomal.getValueByTime(correlateFeature.getValue(), time));
        } else {
            numberOfCorrelateAttribute = 0;
            valueCorrelate.setValue(0);
        }
    }
    public StringProperty getCorrelateFeature(){
        //  Init the name of the correlate attribute
        correlateFeature.setValue(ts_reg.getCorrelateFeature(chosenAttribute.getValue()));  //need to be according to the ALG
        return correlateFeature;
    }

    public void openCSVTrainFile() {
        FileChooser fc = new FileChooser();
        fc.setTitle("open CSV train file");
        fc.setInitialDirectory(new File("./"));
        File chosen = fc.showOpenDialog(null);

        if(chosen == null) {
            m.fileNotFoundAlert("train");
        } else {
            if (!chosen.getName().contains(".csv"))  // Checking the file
            {
                m.wrongFileAlert("CSV");
            } else {
                ts_reg = new TimeSeries(chosen.getName());
                ts_reg.checkCorrelate(ts_reg);
                m.setTimeSeries(ts_reg, "Train");   // Set timeSeries
                this.csvTrainFile = true;
                m.fileUpdateAlert("Train");
            }
        }
    }

    public void openCSVTestFile() {
        FileChooser fc = new FileChooser();
        fc.setTitle("open CSV test file");
        fc.setInitialDirectory(new File("./"));
        File chosen = fc.showOpenDialog(null);

        if(chosen == null) {
            m.fileNotFoundAlert("test");
        } else {
            if (!chosen.getName().contains(".csv")) {  //checking the file
                m.wrongFileAlert("CSV");
            } else {
                ts_Anomal = new TimeSeries(chosen.getName());
                m.setTimeSeries(ts_Anomal, "Test");

                attributeList.addAll(ts_Anomal.getAttributes());    // Init attributes list
                sizeTS.setValue(ts_Anomal.getSize());
                altimeter.setValue("0");
                airSpeed.setValue("0");
                fd.setValue("0");

                m.fileUpdateAlert("Test");
                this.csvTestFile = true;
            }
        }
    }

    public void openXMLFile() {
        xmlFile = m.openXML();
        if(xmlFile) {
            m.fileUpdateAlert("XML");
            pitchMax.setValue(m.attributeMap.get("pitch").getMax());
            pitchMin.setValue(m.attributeMap.get("pitch").getMin());
            rollMax.setValue(m.attributeMap.get("roll").getMax());
            rollMin.setValue(m.attributeMap.get("roll").getMin());
            yawMax.setValue(m.attributeMap.get("yaw").getMax());
            yawMin.setValue(m.attributeMap.get("yaw").getMin());
        }
    }

    public void play() {
        if (csvTestFile && csvTrainFile && xmlFile) {
            this.m.playFile();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("File is missing");
            alert.setContentText("Please upload csv files and xml file");
            alert.showAndWait();
        }
    }

    public void pause() {
        this.m.pauseFile();
    }

    public void stop() {
        this.m.stopFile();
    }

    public void rewind() {
        this.m.rewindFile();
    }

    public void forward() {
        this.m.forwardFile();
    }

    public boolean loadAnomalyDetector() {
        FileChooser fc = new FileChooser();
        fc.setTitle("open ALG");
        fc.setInitialDirectory(new File("./"));
        File chosen = fc.showOpenDialog(null);

        if(!csvTestFile || !csvTrainFile) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Wrong file chosen");
            alert.setContentText("Please upload csv files first");
            alert.showAndWait();
            return false;
        }

        if(chosen == null) {
            m.fileNotFoundAlert("algorithm");
            return false;
        } else {
            if (!chosen.getName().contains(".class"))  //checking the file
            {
                m.wrongFileAlert("CLASS");
                return false;
            }
            try {
                m.loadAnomalyDetector(chosen.getPath(),chosen.getName().toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            algoFile = true;
            m.fileUpdateAlert("Algorithm");
            return true;
        }
    }

    public void speedPlay() {
        if (choiceSpeed.doubleValue() == 0.5) m.properties.setPlaySpeed(150);
        else if (choiceSpeed.doubleValue() == 1.5) m.properties.setPlaySpeed(75);
        else if (choiceSpeed.doubleValue() == 2) m.properties.setPlaySpeed(50);
        else if (choiceSpeed.doubleValue() == 2.5) m.properties.setPlaySpeed(20);
        else m.properties.setPlaySpeed(100);
    }

    public Callable<AnchorPane> getPainter(){return m.getPainter();}

    @Override
    public void update(Observable o, Object arg) {
        if (o == m) {
            this.timeStamp.setValue(m.getTime());
        }
    }
}
