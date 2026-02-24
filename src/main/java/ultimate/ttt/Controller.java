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
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.media.AudioClip;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.shape.Rectangle;


import java.util.Arrays;
import java.util.List;
import java.util.Random;

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
    private Button playButton;

    @FXML
    private Text bluesturn, redsturn, drawtext;

    private Player startingPlayer;

    private UltimateBoard gameBoard;

    private AudioClip localWinSound, globalWinSound, globalDrawSound, hoverSound, clickSound, gameStart, tileSound;

    private Random random;

    private void initializeSounds() {
        localWinSound = new AudioClip(getClass().getResource("/Sounds/localVictory.mp3").toExternalForm());
        globalWinSound = new AudioClip(getClass().getResource("/Sounds/victory.mp3").toExternalForm());
        globalDrawSound = new  AudioClip(getClass().getResource("/Sounds/draw.mp3").toExternalForm());
        hoverSound =  new AudioClip(getClass().getResource("/Sounds/swipe.mp3").toExternalForm());
        clickSound = new AudioClip(getClass().getResource("/Sounds/button.mp3").toExternalForm());
        gameStart = new  AudioClip(getClass().getResource("/Sounds/gameStart.mp3").toExternalForm());
        tileSound = new  AudioClip(getClass().getResource("/Sounds/claimTile.mp3").toExternalForm());
    }

    private void initializeMainMenu() {
        Rectangle clip = new Rectangle();

        clip.setArcWidth(30);
        clip.setArcHeight(30);

        clip.widthProperty().bind(root.widthProperty());
        clip.heightProperty().bind(root.heightProperty());

        menuImage.setClip(clip);
    }

    private void initializeIconsAndText() {
        restartIcon.setOpacity(0.0);
        restartIcon.setDisable(true);

        bluesturn.setVisible(true);
        redsturn.setVisible(false);
        drawtext.setVisible(false);
    }

    private void initializeGrid() {
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
    }
    @FXML
    public void initialize() {

        this.startingPlayer = Player.BLUE;

        this.random = new Random();
        this.initializeSounds();
        this.initializeMainMenu();
        this.initializeIconsAndText();
        this.initializeGrid();

        gameBoard = new UltimateBoard(allButtons, startingPlayer, localWinSound);
    }

    @FXML
    private void playHoverSound() {
        playWithRandomPitchAndPan(hoverSound, 0.6, -60.0, 60.0);
    }

    @FXML
    private void playClickSound() {
        playWithRandomPitchAndPan(clickSound, 0.4, -60.0, 60.0);
    }

    @FXML
    private void playTileSound() {
        playWithPlayerPitch(tileSound, 0.6);
    }

    private void playWithRandomPitchAndPan(AudioClip clip, double volume, double pitchLower, double pitchUpper) {
        double cents = pitchLower + ((pitchUpper*2) * random.nextDouble());
        double rate = Math.pow(2.0, cents / 1200.0);
        double pan = -0.3 + (0.6 * random.nextDouble());
        clip.play(volume, pan, rate, 0.0, 0);
    }

    private void playWithPlayerPitch(AudioClip clip, double volume) {
        if(gameBoard.getCurrentPlayer() == Player.BLUE) {
            playWithRandomPitchAndPan(clip, volume, -60.0, 10.0);
        } else {
            playWithRandomPitchAndPan(clip, volume, -10.0, 60.0);
        }
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

        Player winner = gameBoard.buttonPress(globalRow, globalCol, localRow, localCol);

        if (winner == Player.NONE) {
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
    private void crown(Player winner) {
        gameBoard.disableAllBoards();

        bluesturn.setVisible(false);
        redsturn.setVisible(false);

        String style;
        if (winner == Player.BLUE) {
            style = "-fx-background-color: #007aff";
            bluesturn.setText("Blue wins!");
            bluesturn.setVisible(true);
        } else {
            style = "-fx-background-color: #fc3c2f";
            redsturn.setText("Red wins!");
            redsturn.setVisible(true);
        }

        globalWinSound.play();
        animateBoardFill(style);
    }

    /**
     * Displays a text saying "Draw!" and paints all buttons in grey using {@link #animateBoardFill(String)}
     */
    private void draw() {

        bluesturn.setVisible(false);
        redsturn.setVisible(false);
        drawtext.setVisible(true);

        globalDrawSound.play();
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
    protected void restart() {

        if(restartIcon.getOpacity()>0.5) {

            this.drawtext.setVisible(false);
            this.redsturn.setVisible(false);
            this.bluesturn.setVisible(false);
            this.redsturn.setText("Red's turn!");
            this.bluesturn.setText("Blue's turn!");
            if(startingPlayer== Player.BLUE) {
                startingPlayer = Player.RED;
                redsturn.setVisible(true);
            } else {
                startingPlayer = Player.BLUE;
                bluesturn.setVisible(true);
            }

            this.gameBoard = new UltimateBoard(allButtons, startingPlayer, localWinSound);

            List<Button> buttons = Arrays.stream(allButtons)
                    .flatMap(Arrays::stream)
                    .flatMap(Arrays::stream)
                    .flatMap(Arrays::stream)
                    .toList();
            for (Button button : buttons) {
                button.setStyle("");
                button.setDisable(false);
            }

            clickSound.play();
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
        if(gameStart != null) {
            gameStart.play();
            gameStart = null;
        }
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