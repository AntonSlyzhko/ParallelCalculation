package com.mycompany.project;

import java.io.IOException;
import javafx.fxml.FXML;

public class Controller {

    @FXML
    private void switchToSecondary() throws IOException {
        App.setRoot("secondary");
    }
}
