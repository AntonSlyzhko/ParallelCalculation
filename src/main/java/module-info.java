module com.mycompany.project {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.mycompany.project to javafx.fxml;
    exports com.mycompany.project;
}