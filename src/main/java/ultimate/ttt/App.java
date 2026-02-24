package ultimate.ttt;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class App extends Application {

    private double xOffset = 0;
    private double yOffset = 0;

    @Override
    public void start(Stage stage) throws IOException {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/GUI/ultimateTicTacToeGUI.fxml"));
        Parent root = loader.load();

        stage.initStyle(StageStyle.TRANSPARENT);
        Scene scene = new Scene(root, 800, 1000);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(getClass().getResource("/GUI/ultimateTicTacToe.css").toExternalForm());

        stage.setTitle("Ultimate Tic Tac Toe");

        Image appIcon = new Image(getClass().getResourceAsStream("/GUI/Images/logo.png"));
        stage.getIcons().add(appIcon);

        root.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        root.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });

        Controller myController = loader.getController();
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                myController.showMenu();
            }
            if (event.getCode() == KeyCode.R) {
                myController.restart();
            }
        });

        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}