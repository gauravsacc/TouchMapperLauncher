package es.shyri.touchmapper.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("main.fxml"));
        primaryStage.setTitle("Touch Mapper Launcher");
        primaryStage.show();
        Scene scene = new Scene(root, 500, 275);
        primaryStage.setScene(scene);
    }

    public static void main(String[] args) {
        launch(args);
    }
}