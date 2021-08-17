package com.mycompany.project;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Optional;

public class App extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("primary.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 834, 467);
        stage.setTitle("Author: Anton Slyzhko");
        stage.setScene(scene);
        stage.show();
        stage.setOnCloseRequest(e ->{
            e.consume();
            closeRequest(stage);
        });
    }

    private void closeRequest(Stage stage){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit agreement");
        alert.setHeaderText("Do you really want to quit?");
        alert.setContentText(null);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.orElse(null) == ButtonType.OK){
            stage.close();
        }
    }

    public static void main(String[] args) {
        launch();
    }

}