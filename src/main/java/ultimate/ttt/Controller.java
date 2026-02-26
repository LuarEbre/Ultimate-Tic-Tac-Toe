package ultimate.ttt;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Pagination;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.media.AudioClip;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.shape.Rectangle;

import java.time.LocalDate;
import java.time.Year;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Controller {

    @FXML
    private AnchorPane root, menu, tutorialPane, page1, page2, page3, page4, page5;

    @FXML
    private Pagination tutorial;

    @FXML
    private GridPane masterGrid;

    @FXML
    private ImageView restartIcon, menuImage;

    @FXML
    private Button[][][][] allButtons;

    @FXML
    private Button playButton;

    @FXML
    private ToggleButton soundToggle, blueScoreDisplay, redScoreDisplay;

    @FXML
    private Text bluesTurn, redsTurn, drawtext, copyrightText;

    private Player startingPlayer;

    private UltimateBoard gameBoard;

    private AudioClip localWinSound, globalWinSound, globalDrawSound, hoverSound, clickSound, gameStart, tileSound;

    private AudioClip[] allAudios;

    private Random random;

    private int blueScore, redScore;

    private void initializeSounds() {
        this.localWinSound = new AudioClip(getClass().getResource("/Sounds/localVictory.mp3").toExternalForm());
        this.globalWinSound = new AudioClip(getClass().getResource("/Sounds/victory.mp3").toExternalForm());
        this.globalDrawSound = new  AudioClip(getClass().getResource("/Sounds/draw.mp3").toExternalForm());
        this.hoverSound =  new AudioClip(getClass().getResource("/Sounds/swipe.mp3").toExternalForm());
        this.clickSound = new AudioClip(getClass().getResource("/Sounds/button.mp3").toExternalForm());
        this.gameStart = new  AudioClip(getClass().getResource("/Sounds/gameStart.mp3").toExternalForm());
        this.tileSound = new  AudioClip(getClass().getResource("/Sounds/claimTile.mp3").toExternalForm());
        // aggregate all AudioClips into Array
        this.allAudios = new AudioClip[] {localWinSound, globalWinSound, globalDrawSound, hoverSound, clickSound, gameStart, tileSound};
    }

    private void initializeMute() {
        soundToggle.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                // if toggled ON = muted
                for(AudioClip clip : this.allAudios) clip.setVolume(0.0);
            } else {
                // if toggled OFF = unmute
                for(AudioClip clip : this.allAudios) clip.setVolume(1.0);
            }
        });
    }

    private void initializeMainMenu() {
        Rectangle clip = new Rectangle();

        clip.setArcWidth(30);
        clip.setArcHeight(30);

        clip.widthProperty().bind(root.widthProperty());
        clip.heightProperty().bind(root.heightProperty());

        menuImage.setClip(clip);
    }

    private void initializePagination() {

        Node[] pages = {page1, page2, page3, page4, page5};

        tutorial.setPageFactory(pageIndex -> {
            return pages[pageIndex];
        });

        Rectangle clip = new Rectangle();

        clip.setArcWidth(30);
        clip.setArcHeight(30);

        clip.widthProperty().bind(root.widthProperty());
        clip.heightProperty().bind(root.heightProperty());

        tutorialPane.setClip(clip);
    }

    private void initializeIconsAndText() {
        restartIcon.setOpacity(0.0);
        restartIcon.setDisable(true);

        bluesTurn.setVisible(true);
        redsTurn.setVisible(false);
        drawtext.setVisible(false);

        int startYear = 2026;
        int currentYear = Year.now().getValue();

        if (currentYear > startYear) {
            copyrightText.setText("© " + startYear + " - " + currentYear + " Raul Erbe");
        } else {
            copyrightText.setText("© " + startYear + " Raul Erbe");
        }
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
        this.initializeMute();
        this.initializeMainMenu();
        this.initializePagination();
        this.initializeIconsAndText();
        this.initializeGrid();

        this.blueScore = 0;
        this.redScore = 0;
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
        if(clip.getVolume() == 0.0) return;
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

        bluesTurn.setVisible(false);
        redsTurn.setVisible(false);

        String style;
        if (winner == Player.BLUE) {
            style = "-fx-background-color: #007aff";
            bluesTurn.setText("Blue wins!");
            bluesTurn.setVisible(true);
        } else {
            style = "-fx-background-color: #fc3c2f";
            redsTurn.setText("Red wins!");
            redsTurn.setVisible(true);
        }

        globalWinSound.play();
        updateScore(winner);
        animateBoardFill(style);
    }

    /**
     * Displays a text saying "Draw!" and paints all buttons in grey using {@link #animateBoardFill(String)}
     */
    private void draw() {

        bluesTurn.setVisible(false);
        redsTurn.setVisible(false);
        drawtext.setVisible(true);

        globalDrawSound.play();
        animateBoardFill("-fx-background-color: #8e8e93");
    }

    private void updateScore(Player winner) {
        if(winner == Player.BLUE) {
            this.blueScore++;
            blueScoreDisplay.setText(Integer.toString(this.blueScore));
        } else {
            this.redScore++;
            redScoreDisplay.setText(Integer.toString(this.redScore));
        }
        if(blueScore > redScore) {
            blueScoreDisplay.setSelected(true);
            redScoreDisplay.setSelected(false);
        } else if(redScore > blueScore) {
            blueScoreDisplay.setSelected(false);
            redScoreDisplay.setSelected(true);
        } else {
            blueScoreDisplay.setSelected(false);
            redScoreDisplay.setSelected(false);
        }
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
            this.redsTurn.setVisible(false);
            this.bluesTurn.setVisible(false);
            this.redsTurn.setText("Red's turn!");
            this.bluesTurn.setText("Blue's turn!");
            if(startingPlayer== Player.BLUE) {
                startingPlayer = Player.RED;
                redsTurn.setVisible(true);
            } else {
                startingPlayer = Player.BLUE;
                bluesTurn.setVisible(true);
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

    protected void handleEsc() {
        if(!tutorialPane.isDisabled()) showInstructions();
        else hideMenu();
    }
    @FXML
    private void hideMenu() {
        if(menu.isDisabled()) {
            this.opacityTransition(menu, 200, true);
            playButton.setText("Continue");
        } else {
            if(gameStart != null) {
                gameStart.play();
                gameStart = null;
            }
            this.opacityTransition(menu, 350, false);
        }
    }

    @FXML
    private void showInstructions() {
        if(tutorialPane.isDisabled()) {
            this.tutorial.setCurrentPageIndex(0);
            this.opacityTransition(tutorialPane, 200, true);
        } else {
            this.opacityTransition(tutorialPane, 350, false);
        }
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
        bluesTurn.setVisible(!bluesTurn.isVisible());
        redsTurn.setVisible(!redsTurn.isVisible());
        gameBoard.switchPlayers();
    }
}