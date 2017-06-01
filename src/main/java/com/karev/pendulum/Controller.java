package com.karev.pendulum;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import jssc.*;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Controller {
    private SerialPort serialPort;
    private int compens = 0;

    @FXML
    private Button start;
    @FXML
    private Button stop;
    @FXML
    private Button up;
    @FXML
    private Button down;
    @FXML
    private TextField steps;
    @FXML
    private TextField time;
    @FXML
    private Label status;
    @FXML
    private ComboBox<String> ports;
    @FXML
    private ProgressBar process;



    @FXML
    protected void initialize() {
        steps.textProperty().addListener(new ChangeListener<String>() {
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d*")) {
                    steps.setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });
        time.textProperty().addListener(new ChangeListener<String>() {
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d*")) {
                    time.setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });
        ports.getItems().setAll(SerialPortList.getPortNames());

        setDisableAll(true);


    }

    public void close() {
//        System.out.println("CLOSE");
        try {
            if (serialPort != null) {
                serialPort.removeEventListener();
                serialPort.closePort();
            }
        } catch (SerialPortException e) {
            showException(e);
            e.printStackTrace();
        }
    }

    @FXML
    private void startHandle() {
        int delay = Integer.parseInt(time.getText()) * 1000 / (4 * Integer.parseInt(steps.getText()));
//        int delay = Integer.parseInt(time.getText());
        String data = "1\n" + delay + "\n" + steps.getText() + "\n" + compens;
        System.out.println(data);
        sendSerial(data);
        process.setVisible(true);
    }

    @FXML
    private void stopHandle() {
        sendSerial("0");
        process.setVisible(false);
    }

    @FXML
    private void portsHandle() {
//        ports.getItems().setAll(SerialPortList.getPortNames());
        String name = ports.getSelectionModel().getSelectedItem();
        serialPort = new SerialPort(name);
        try {
            serialPort.openPort();
            serialPort.setParams(SerialPort.BAUDRATE_115200,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
            serialPort.setEventsMask(SerialPort.MASK_RXCHAR);

            serialPort.addEventListener(new SerialPortEventListener() {
                public void serialEvent(SerialPortEvent event) {
                    if(event.isRXCHAR() && event.getEventValue() > 0){
                        try {
                            String str = serialPort.readString();
                            System.out.println(str);
                            int pos = Integer.parseInt(str.substring(0, str.length() - 2));
                            compens = calcCompens(pos);

                        } catch (SerialPortException e) {
                            showException(e);
                            e.printStackTrace();
                        }
                    }
                }
            });
        } catch (SerialPortException e) {
            showException(e);
            e.printStackTrace();
        }
        setDisableAll(false);
        status.setText("Подключено");
    }

    @FXML
    private void upPressedHandle() {
        System.out.println("UP");
        sendSerial("3");
    }

    @FXML
    private void downPressedHandle() {
        System.out.println("DOWN");
        sendSerial("2");
    }

    @FXML
    private void keyPressedHandle(KeyEvent event) {
        switch (event.getCode()) {
            case UP:
                if (!up.isArmed()) {
                    System.out.println("up");
                    up.arm();
                    up.fire();
                }
                break;
            case DOWN:
                if (!down.isArmed()) {
                    System.out.println("down");
                    down.arm();
                    up.fire();
                }
                break;
        }
    }

    @FXML
    private void keyReleasedHandle(KeyEvent event) {
        switch (event.getCode()) {
            case UP:
                System.out.println("up realese");
                up.disarm();
                break;
            case DOWN:
                System.out.println("down realese");
                down.disarm();
                break;
        }
    }

    private void setDisableAll(boolean value) {
        start.setDisable(value);
        stop.setDisable(value);
        steps.setDisable(value);
        time.setDisable(value);
        up.setDisable(value);
        down.setDisable(value);
    }

    private void sendSerial(String data) {
        try {
            serialPort.writeString(data);
        } catch (SerialPortException e) {
            showException(e);
            e.printStackTrace();
        }
    }

    private void showException(Exception ex) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Exception");
        alert.setHeaderText(ex.toString());
        alert.setContentText(ex.getMessage());

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        String exceptionText = sw.toString();

        Label label = new Label("The exception stacktrace was:");

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

        alert.getDialogPane().setExpandableContent(expContent);

        alert.showAndWait();
    }

    int calcCompens(int x){
        if (Math.abs(x % 32) >= 16) {
            if (x >= 0) {
                return x % 32 - 32;
            } else {
                return 32 + x % 32;
            }

        } else {

            return x % 32;
        }
    }



}
