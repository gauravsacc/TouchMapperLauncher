package es.shyri.touchmapper.client;

import com.sun.javafx.application.PlatformImpl;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.ResourceBundle;

import es.shyri.touchmapper.client.adb.Adb;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

/**
 * Created by shyri on 17/11/2017.
 */
public class MainController implements Initializable {
    @FXML
    Button adbFilePath;
    @FXML
    Button loadConfigFile;
    @FXML
    Button connectAdb;
    @FXML
    Button disconnectAdb;
    @FXML
    Text fileNameText;
    @FXML
    TextField IPTextField;
    @FXML
    TextArea logTextArea;
    @FXML
    ProgressBar loadingProgressBar;
    @FXML
    CheckBox verboseCheckBox;

    final FileChooser fileChooser = new FileChooser();
    public Adb adb;

    private File adbPath;
    private File selectedFile;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initViews();
        loadConfigFile.setOnAction(event -> {
            selectedFile = fileChooser.showOpenDialog(loadConfigFile.getScene()
                                                                    .getWindow());
            if (selectedFile != null) {
                fileNameText.setText("File: " + selectedFile.getPath());
                IPTextField.setDisable(false);
            } else {
                fileNameText.setText("File: none");
                IPTextField.setDisable(true);
            }
        });

        adbFilePath.setOnAction(event -> {
            adbPath = fileChooser.showOpenDialog(loadConfigFile.getScene()
                    .getWindow());
        });

        IPTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if(IPTextField.getText().isEmpty()) {
                connectAdb.setDisable(true);
            } else {
                connectAdb.setDisable(false);
            }
        });
    }

    public void terminate() {
        adb.terminate();
        try {
            adb.disconnect(result -> {
                log(result);
                showConnectEnabled();
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onConnectClick() {
        this.adb = new Adb(adbPath);
        showProgress();
        try {
            adb.connectDevice(IPTextField.getText(), result -> {
                if (result != null && result.contains("connected")) {
                    log(result);
                    try {
                        sendFile();
                    } catch (IOException e) {
                        log(e.getMessage() + "\n");
                        e.printStackTrace();
                        hideProgress();
                    }
                } else {
                    log("Unable to connect device\n");
                    hideProgress();
                }
            });
        } catch (IOException e) {
            hideProgress();
            e.printStackTrace();
            log("Unable to connect device: " + e.getMessage() + "\n");
        }
    }

    @FXML
    public void onDisconnectClick() {
        terminate();
        disconnectAdb.setDisable(true);
    }

    private void sendFile() throws IOException {
        if (selectedFile != null) {
            adb.pushFile(IPTextField.getText(), selectedFile.getPath(),
                         "/storage/self/primary/Android/data/es.shyri.touchmapper/files/mapping.json", result -> {
                        log(result);
                        try {
                            runMapper();
                        } catch (IOException e) {
                            log(e.getMessage());
                            e.printStackTrace();
                            hideProgress();
                        }
                    });
        }
    }

    private void runMapper() throws IOException {
        adb.getApkId(IPTextField.getText(), apkId->{
            if(apkId == null || apkId.isEmpty()) {
                log("Touch Mapper application not found\n");
                hideProgress();
                showConnectEnabled();
                return;
            }
            adb.runMapper(IPTextField.getText(), apkId, result -> {
                hideProgress();
                showDisconnectEnabled();
                if(verboseCheckBox.isSelected()) {
                    log(result);
                }

                // TODO Killed case
                // TODO Unkown case
            });
        });
    }

    private void log(String log) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 1);
        SimpleDateFormat format1 = new SimpleDateFormat("HH:mm:ss");

        // TODO It reaches a point where the amount of log hangs the app but I coulnd't find a way to purge it
        //        if (logTextArea.getText()
        //                       .length() > 1000) {
        //            logTextArea.setText(logTextArea.getText()
        //                                           .substring(1000));
        //        }

        try {
            PlatformImpl.runAndWait(() -> logTextArea.appendText(format1.format(cal.getTime()) + " " + log));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initViews() {
        connectAdb.setDisable(true);
        disconnectAdb.setDisable(true);
        IPTextField.setDisable(true);
    }

    private void showProgress() {
        loadConfigFile.setDisable(true);
        connectAdb.setDisable(true);
        IPTextField.setDisable(true);
        disconnectAdb.setDisable(true);
        loadingProgressBar.setVisible(true);
    }

    private void hideProgress() {
        loadConfigFile.setDisable(false);
        connectAdb.setDisable(false);
        IPTextField.setDisable(false);
        loadingProgressBar.setVisible(false);
    }

    private void showConnectEnabled() {
        loadConfigFile.setDisable(false);
        connectAdb.setDisable(false);
        disconnectAdb.setDisable(true);
        IPTextField.setDisable(false);
    }

    private void showDisconnectEnabled() {
        loadConfigFile.setDisable(true);
        connectAdb.setDisable(true);
        disconnectAdb.setDisable(false);
        IPTextField.setDisable(true);
    }
}
