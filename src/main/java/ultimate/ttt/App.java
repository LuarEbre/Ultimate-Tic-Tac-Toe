package ultimate.ttt;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class App extends Application {

    private double xOffset = 0;
    private double yOffset = 0;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/GUI/ultimateTicTacToeGUI.fxml"));
        Parent root = fxmlLoader.load();

        stage.initStyle(StageStyle.TRANSPARENT);

        Scene scene = new Scene(root, 800, 1000);
        scene.setFill(Color.TRANSPARENT);

        scene.getStylesheets().add(getClass().getResource("/GUI/ultimateTicTacToe.css").toExternalForm());

        root.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        root.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });

        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}