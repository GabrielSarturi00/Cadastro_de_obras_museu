module inf.Trabalho.demo {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens trabalho.controller to javafx.fxml;

    exports trabalho;
}