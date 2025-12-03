package trabalho;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxml = new FXMLLoader(
                HelloApplication.class.getResource("/trabalho/ObraView.fxml")
        );
        Scene scene = new Scene(fxml.load(), 750, 450);
        stage.setTitle("Cadastro de Obras");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}