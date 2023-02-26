package model;

import algorithms.*;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Alert;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import viewModel.TimeSeries;

import java.beans.ExceptionListener;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.List;
import java.util.concurrent.Callable;

public class Model extends Observable implements SimulatorModel {

    public Socket socket;
    public PrintWriter out;
    public TimeSeries ts_Anomal, ts_reg;
    public Options op;
    Thread displaySetting;
    public FlightSetting properties;
    public Map<String, Attribute> attributeMap;

    private double time = 0;
    public boolean isConnect;
    public String algName;

    public SimpleAnomalyDetector ad;
    public ZScoreAlgorithm zScore;
    public hybridAlgorithm hyperALG;

    public StringProperty attribute1;
    public DoubleProperty timeStep;

    public Model() {
        this.properties = new FlightSetting();
        this.op = new Options();
        this.isConnect = false;
        this.attribute1 = new SimpleStringProperty();
        this.timeStep = new SimpleDoubleProperty();
    }

    @Override
    public boolean ConnectToServer(String ip, double port) {
        try {
            socket = new Socket("127.0.0.1", 5402);
            out = new PrintWriter(socket.getOutputStream());
            return true;

        } catch (IOException e) {
            return false;
        }
    }

    synchronized public void playFile() {
        if (op.afterForward) {  //somehow it does not responded to it and cannot go back to normal rate
            op.afterForward = false;
            properties.setPlaySpeed(100);
        } else if (op.afterRewind) {
            op.rewind = false;
        } else if (op.afterPause) {
            this.notify();
            op.pause = false;
            op.afterPause = false;
        } else if (op.afterStop) {  //creating a new thread to run displayFlight()
            if (isConnect) {
                displaySetting = new Thread(() -> displayFlight(true), "Thread of displaySetting function");
                displaySetting.start();
                op.afterStop = false;
            } else {
                displaySetting = new Thread(() -> displayFlight(false), "Thread of displaySetting function");
                displaySetting.start();
                op.afterStop = false;
            }
        } else {    //first time of Play
            isConnect = ConnectToServer(properties.getIp(), properties.getPort());
            if (isConnect) {
                displaySetting = new Thread(() -> displayFlight(true), "Thread of displaySetting function");
                displaySetting.start();
            } else {    //if not connectToFG
                displaySetting = new Thread(() -> displayFlight(false), "Thread of displaySetting function");
                displaySetting.start();
            }
        }
    }

    synchronized public void displayFlight(boolean conncetServer) {
        int i = 0;
        int sizeTS = ts_Anomal.getSize();

        for (i = (int) time; i < sizeTS; i++) {
            timeStep.setValue(time);
            while (op.pause || op.scroll || op.afterStop || op.forward || op.rewind)  //pause needs to be replaced with thread( works only one time now)
            {
                try {
                    if (op.afterStop)
                        displaySetting.stop();

                    if (op.afterPause)
                        this.wait();

                    if (op.forward) {
                        if (i < sizeTS - 151)
                            i += 150;
                        else
                            i = sizeTS;
                        op.forward = false;
                    }

                    if (op.rewind) {
                        if ((i - 150) > 0)
                            i -= 150;
                        else
                            i = 1;
                        op.rewind = false;
                    }

                    if (op.scroll) {
                        op.scroll = false;
                        i = (int) time;
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (conncetServer) {
                out.println(ts_Anomal.rows.get(i));
                out.flush();
            }
            time = i;
            setChanged();
            notifyObservers();
            try {
                Thread.sleep(properties.getPlaySpeed());//responsible for the speed of the display
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public double getTime() {
        return this.time;
    }

    public void setTime(double time) {
        this.time = time;
        op.scroll = true;
    }

    @Override
    public void setTimeSeries(TimeSeries ts, String tsType) {
        if (tsType.equals("Train"))
            this.ts_reg = ts;
        else if (tsType.equals("Test"))
            this.ts_Anomal = ts;
    }


    // ***** Buttons Functions ***** //

    public void pauseFile() {
        op.pause = true;
        op.afterPause = true;
    }

    public void stopFile() {
        op.afterStop = true;
        this.time = 0;
    }

    public void rewindFile() {
        op.rewind = true;
    }

    public void forwardFile() {
        op.forward = true;
    }

    // ***** Alerts Functions ***** //

    public void fileNotFoundAlert(String type) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("File not found");
        alert.setContentText("Choose " + type + " file");
        alert.showAndWait();
    }

    public void wrongFileAlert(String type) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Wrong file chosen");
        alert.setContentText("Please choose a " + type + " file");
        alert.showAndWait();
    }

    public void fileUpdateAlert(String type) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("File has been updated");
        alert.setContentText(type + " file has been updated");
        alert.showAndWait();
    }

    // ***** Algorithm Functions ***** //

    public Boolean loadAnomalyDetector(String path, String nameALG) throws Exception {//String input
        algName = nameALG.split("\\.")[0];
        URLClassLoader urlClassLoader = URLClassLoader.newInstance(new URL[]{new URL("file:\\" + path)});
        Class<?> c = urlClassLoader.loadClass("algorithms."+algName);

        if (algName.equals("hybridAlgorithm")) {
            hyperALG = (hybridAlgorithm) c.newInstance();
            new Thread(() -> initData()).start();   //needs if to init data at first time
            hyperALG.learnNormal(ts_reg);
            hyperALG.detect(ts_Anomal);

        } else if (algName.equals("SimpleAnomalyDetector")) {
            ad = (SimpleAnomalyDetector) c.newInstance();
            new Thread(() -> initData()).start();
            ad.learnNormal(ts_reg);
            ad.detect(ts_Anomal);
        } else if (algName.equals("ZScoreAlgorithm")) {
            zScore = (ZScoreAlgorithm) c.newInstance();
            new Thread(() -> initData()).start();
            zScore.learnNormal(ts_reg);
            zScore.detect(ts_Anomal);
        }
        return false;
    }

    public void initData() {
        setVarivablesTOALG();
        setVarivablesNamesTOALG();
    }

    public void setVarivablesTOALG() {//listen to timeStep and init line chart of reg
        if (algName.equals("SimpleAnomalyDetector"))
            ad.timeStep.bind(timeStep);
        else if (algName.equals("ZScoreAlgorithm"))
            zScore.timeStep.bind(timeStep);
        else
            hyperALG.timeStep.bind(timeStep);
    }

    public void setVarivablesNamesTOALG() { //Listen to chosen attribute
        if (algName.equals("SimpleAnomalyDetector"))
            ad.attribute1.bind(attribute1);

        else if (algName.equals("ZScoreAlgorithm"))
            zScore.Attribute.bind(attribute1);
        else
            hyperALG.attribute1.bind(attribute1);
    }

    public Callable<AnchorPane> getPainter() {
        if (algName.equals("SimpleAnomalyDetector"))    // Reg
            return () -> ad.paint();
        else if (algName.equals("ZScoreAlgorithm")) // zScore
            return () -> zScore.paint();
        else    //HyperALG
            return () -> hyperALG.paint();
    }

    // ***** XML File Functions ***** //

    public boolean openXML() {
        FileChooser fc = new FileChooser();
        fc.setTitle("open XML file");
        fc.setInitialDirectory(new File("./"));
        File chosen = fc.showOpenDialog(null);

        if(chosen == null) {
            fileNotFoundAlert("XML");
        } else {
            if (!chosen.getName().contains(".xml"))  //checking the file
            {
                wrongFileAlert("XML");
            } else {
                try {
                    this.properties = readFromXML(chosen.getName());
                    if (this.properties != null) {
                        createMapAttribute();
                        return true;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    public void createMapAttribute() {
        this.attributeMap = new HashMap<>();
        for (Attribute attribute : properties.getAttributes()) {
            attributeMap.put(attribute.name, attribute);
        }
    }

    public void writeToXML(FlightSetting settings) throws IOException {
        FileOutputStream fos = new FileOutputStream("settings.xml");
        XMLEncoder encoder = new XMLEncoder(fos);
        encoder.setExceptionListener(new ExceptionListener() {
            public void exceptionThrown(Exception e) {
                System.out.println("Exception! :" + e.toString());
            }
        });

        List<Attribute> lst = new ArrayList<>();
        lst.add(createAtrribute("aileron", 0, -1, 1));
        lst.add(createAtrribute("elevators", 1, -1, 1));
        lst.add(createAtrribute("rudder", 2, 0, 1));
        lst.add(createAtrribute("throttle", 6, 0, 1));
        lst.add(createAtrribute("altimeter", 25, null, null));
        lst.add(createAtrribute("airSpeed", 24, null, null));
        lst.add(createAtrribute("fd", 36, 0, 360));
        lst.add(createAtrribute("pitch", 29, -10, 17));
        lst.add(createAtrribute("roll", 17, -38, 43));
        lst.add(createAtrribute("yaw", 20, -29, 91));
        settings.setAttributes(lst);
        settings.setPort((double) 5402);
        settings.setIp("127.0.0.1");
        settings.setPlaySpeed(100);

        encoder.writeObject(settings);
        encoder.close();
        fos.close();
    }

    public Attribute createAtrribute(String name, Integer associativeName, Integer min, Integer max) {
        Attribute res = new Attribute();
        res.setName(name);
        res.setAssociativeName(associativeName);
        res.setMax(max);
        res.setMin(min);
        return res;
    }

    public FlightSetting readFromXML(String fileName) throws IOException {
        FileInputStream fis = new FileInputStream(fileName);
        XMLDecoder decoder = new XMLDecoder(fis);
        FlightSetting decodedSettings = (FlightSetting) decoder.readObject();
        decoder.close();
        fis.close();
        return decodedSettings;
    }
}