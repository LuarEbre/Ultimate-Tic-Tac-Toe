package ultimate.ttt;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.shape.Rectangle;

import java.util.Arrays;
import java.util.List;

public class Controller {

    @FXML
    private AnchorPane root, menu;

    @FXML
    private GridPane masterGrid;

    @FXML
    private ImageView restartIcon, menuImage;

    @FXML
    private Button[][][][] allButtons;

    @FXML
    private Button playButton, instructionsButton, quitButton;

    @FXML
    private Text titletext, bluesturn, redsturn, drawtext;

    private Players startingPlayer;

    private UltimateBoard gameBoard;

    @FXML
    public void initialize() {

        // round the corners of main menu png
        Rectangle clip = new Rectangle();

        clip.setArcWidth(30);
        clip.setArcHeight(30);

        clip.widthProperty().bind(root.widthProperty());
        clip.heightProperty().bind(root.heightProperty());

        menuImage.setClip(clip);

        restartIcon.setOpacity(0.0);
        restartIcon.setDisable(true);

        bluesturn.setVisible(true);
        redsturn.setVisible(false);
        drawtext.setVisible(false);

        // initialize all 81 buttons into 4D array
        allButtons = new Button[3][3][3][3];

        // traverse masterGrid's 9 nodes
        for (Node localGridNode : masterGrid.getChildren()) {

            // only operate on instances of GridPane
            if (localGridNode instanceof GridPane localGrid) {

                // convert Integer objects (can be null) to int
                Integer gCol = GridPane.getColumnIndex(localGrid);
                Integer gRow = GridPane.getRowIndex(localGrid);
                int globalCol = (gCol == null) ? 0 : gCol;
                int globalRow = (gRow == null) ? 0 : gRow;

                // traverse each node's 9 internal nodes
                for (Node buttonNode : localGrid.getChildren()) {

                    // only operate on instances of Button
                    if (buttonNode instanceof Button button) {

                        // convert Integer objects (can be null) to int
                        Integer lCol = GridPane.getColumnIndex(button);
                        Integer lRow = GridPane.getRowIndex(button);
                        int localCol = (lCol == null) ? 0 : lCol;
                        int localRow = (lRow == null) ? 0 : lRow;

                        // add instance of Button to 4D array
                        allButtons[globalRow][globalCol][localRow][localCol] = button;
                    }
                }
            }
        }
        // initialize gameBoard with 4D array and starting player
        this.startingPlayer = Players.BLUE;
        gameBoard = new UltimateBoard(allButtons, startingPlayer);
    }

    /**
     * Main UI logic happens here; finds the button that was pressed and executes the game logic according to its position using {@link UltimateBoard#buttonPress(int, int, int, int)}
     * @param event
     */
    @FXML
    public void handleTileClick(ActionEvent event) {
        // figure out which button was clicked
        Button clickedButton = (Button) event.getSource();
        GridPane localGrid = (GridPane) clickedButton.getParent();

        // get local coordinates
        Integer lCol = GridPane.getColumnIndex(clickedButton);
        Integer lRow = GridPane.getRowIndex(clickedButton);
        int localCol = (lCol == null) ? 0 : lCol;
        int localRow = (lRow == null) ? 0 : lRow;

        // get global coordinates
        Integer gCol = GridPane.getColumnIndex(localGrid);
        Integer gRow = GridPane.getRowIndex(localGrid);
        int globalCol = (gCol == null) ? 0 : gCol;
        int globalRow = (gRow == null) ? 0 : gRow;

        Players winner = gameBoard.buttonPress(globalRow, globalCol, localRow, localCol);

        if (winner == Players.NONE) {
            // only need to switch players if there is no winner
            switchPlayers();
            // only check for draw if no there is no winner
            if (gameBoard.checkForDraw()) this.draw();
        }
        // a winner is guaranteed
        else crown(winner);

    }

    /**
     * Displays the correct winner and paints all buttons in the winner's color using {@link #animateBoardFill(String)}
     * @param winner
     */
    private void crown(Players winner) {
        gameBoard.disableAllBoards();

        bluesturn.setVisible(false);
        redsturn.setVisible(false);

        String style;
        if (winner == Players.BLUE) {
            style = "-fx-background-color: #007aff";
            bluesturn.setText("Blue wins!");
            bluesturn.setVisible(true);
        } else {
            style = "-fx-background-color: #fc3c2f";
            redsturn.setText("Red wins!");
            redsturn.setVisible(true);
        }

        animateBoardFill(style);
    }

    /**
     * Displays a text saying "Draw!" and paints all buttons in grey using {@link #animateBoardFill(String)}
     */
    private void draw() {

        bluesturn.setVisible(false);
        redsturn.setVisible(false);
        drawtext.setVisible(true);

        animateBoardFill("-fx-background-color: #8e8e93");
    }

    /**
     * Helper function to help with Draw / Win animation
     * @param style String containing -fx-background-color which the buttons should be sequentially painted in
     */
    private void animateBoardFill(String style) {
        // flatten the 4D array into a simple List
        List<Button> flatButtons = Arrays.stream(allButtons)
                .flatMap(Arrays::stream)
                .flatMap(Arrays::stream)
                .flatMap(Arrays::stream)
                .toList();

        // create Timeline
        Timeline timeline = new Timeline();

        int delayCounter = 0;
        for (int i = 0; i < flatButtons.size(); i++) {
            Button button = flatButtons.get(i);

            if (button.getStyle().equals(style)) {
                continue;
            }

            KeyFrame frame = new KeyFrame(
                    Duration.millis(delayCounter * 35),
                    event -> button.setStyle(style)
            );

            timeline.getKeyFrames().add(frame);
            delayCounter++;
        }

        timeline.setOnFinished(event -> {
            opacityTransition(restartIcon, 650, true);
        });

        timeline.play();
    }

    @FXML
    protected void restart(MouseEvent event) {

        if(restartIcon.getOpacity()>0.5) {

            this.drawtext.setVisible(false);
            this.redsturn.setVisible(false);
            this.bluesturn.setVisible(false);
            this.redsturn.setText("Red's turn!");
            this.bluesturn.setText("Blue's turn!");
            if(startingPlayer==Players.BLUE) {
                startingPlayer = Players.RED;
                redsturn.setVisible(true);
            } else {
                startingPlayer = Players.BLUE;
                bluesturn.setVisible(true);
            }

            this.gameBoard = new UltimateBoard(allButtons, startingPlayer);




            List<Button> buttons = Arrays.stream(allButtons)
                    .flatMap(Arrays::stream)
                    .flatMap(Arrays::stream)
                    .flatMap(Arrays::stream)
                    .toList();
            for (Button button : buttons) {
                button.setStyle("");
                button.setDisable(false);
            }

            this.opacityTransition(restartIcon, 250, false);
        }
    }

    private void opacityTransition(Node node, int durationMS, boolean in) {

        if(in) node.setDisable(false);

        FadeTransition fade = new FadeTransition(Duration.millis(durationMS), node);

        fade.setFromValue(in ? 0.0 : 1.0);
        fade.setToValue(in ? 1.0 : 0.0);

        if(!in) {
            fade.setOnFinished(event -> node.setDisable(true));
        }

        fade.play();
    }

    protected void showMenu() {
        if(menu.isDisabled()) {
            this.opacityTransition(menu, 200, true);
            playButton.setText("Continue");
        }
    }

    @FXML
    private void hideMenu() {
        this.opacityTransition(menu, 350, false);
    }

    @FXML
    private void showInstructions() {

    }

    @FXML
    private void terminate() {
        int delay = 200;
        this.opacityTransition(root, delay, false);

        PauseTransition pause = new PauseTransition(Duration.millis(delay+5));

        pause.setOnFinished(event -> {
            Stage stage = (Stage) root.getScene().getWindow();
            stage.close();
        });

        pause.play();
    }

    /**
     * Swaps "x's turn" text for "y's turn" text and calls {@link UltimateBoard#switchPlayers()}
     */
    private void switchPlayers() {
        bluesturn.setVisible(!bluesturn.isVisible());
        redsturn.setVisible(!redsturn.isVisible());
        gameBoard.switchPlayers();
    }
}