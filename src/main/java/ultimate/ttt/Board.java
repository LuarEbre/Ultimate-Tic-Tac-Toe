package ultimate.ttt;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Button;
import javafx.util.Duration;

public class Board {
    private Button[][] board = new Button[3][3];
    private final ButtonState[][] buttonStates = new ButtonState[3][3];
    private BoardState state;

    /**
     * Constructor sets new board's state to {@link BoardState#UNCLAIMED} and all contained buttons' state to {@link ButtonState#EMPTY}
     * @param board 2D array of {@link Button}
     */
    Board(Button[][] board) {
        this.board = board;
        state = BoardState.UNCLAIMED;

        for(int row=0; row<3; row++) {
            for(int col=0; col<3; col++) {
                buttonStates[row][col] = ButtonState.EMPTY;
            }
        }
    }

    /**
     * Processes a player's move on a local board. <br>
     * This method updates the visual styling and internal state of the clicked tile
     * based on the active player. It then evaluates if the current move results
     * in a victory for this local board. If a winner is found, it locks the board,
     * updates its overall state, and triggers the local victory animation.
     *
     * @param localRow    The row index (0-2) of the clicked button within this local board.
     * @param localColumn The column index (0-2) of the clicked button within this local board.
     * @param player      The {@link Players} enum representing the player making the move (e.g., BLUE or RED).
     */
    public void buttonPress(int localRow, int localColumn, Players player) {

        Button affectedButton = board[localRow][localColumn];
        affectedButton.setDisable(true);

        if(player == Players.BLUE) {
            affectedButton.setStyle("-fx-background-color: #007aff");
            buttonStates[localRow][localColumn] = ButtonState.CLAIMED_BLUE;
        }

        if(player == Players.RED) {
            affectedButton.setStyle("-fx-background-color: #fc3c2f");
            buttonStates[localRow][localColumn] = ButtonState.CLAIMED_RED;
        }

        Players winner = this.checkForWinner();

        // no more operations in case of no winner
        if(winner == Players.NONE) return;

        this.disableAllButtons();
        this.claimAll(winner);
        this.paintAllButtons();
    }

    /**
     * Checks for local winners horizontally, vertically, and diagonally (top-right to bottom-left & top-left to bottom-right)
     * @return {@link Players#RED} or {@link Players#BLUE} in case of there being a winner <br>
     * {@link Players#NONE} if there is no winner
     */
    private Players checkForWinner() {
        Players winner = Players.NONE;

        for (int i = 0; i < 3; i++) {
            // Horizontal check
            if (isClaimed(buttonStates[i][0]) &&
                    buttonStates[i][0] == buttonStates[i][1] &&
                    buttonStates[i][0] == buttonStates[i][2]) {
                winner = (buttonStates[i][0] == ButtonState.CLAIMED_RED) ? Players.RED : Players.BLUE;
            }
            // Vertical check
            else if (isClaimed(buttonStates[0][i]) &&
                    buttonStates[0][i] == buttonStates[1][i] &&
                    buttonStates[0][i] == buttonStates[2][i]) {
                winner = (buttonStates[0][i] == ButtonState.CLAIMED_RED) ? Players.RED : Players.BLUE;
            }
        }

        // Diagonal checks
        if (winner == Players.NONE) {
            if (isClaimed(buttonStates[0][0]) && buttonStates[0][0] == buttonStates[1][1] && buttonStates[0][0] == buttonStates[2][2]) {
                winner = (buttonStates[0][0] == ButtonState.CLAIMED_RED) ? Players.RED : Players.BLUE;
            } else if (isClaimed(buttonStates[0][2]) && buttonStates[0][2] == buttonStates[1][1] && buttonStates[0][2] == buttonStates[2][0]) {
                winner = (buttonStates[0][2] == ButtonState.CLAIMED_RED) ? Players.RED : Players.BLUE;
            }
        }

        // Update BoardState
        if (winner == Players.RED) {
            this.state = BoardState.CLAIMED_RED;
            return Players.RED;
        } else if (winner == Players.BLUE) {
            this.state = BoardState.CLAIMED_BLUE;
            return Players.BLUE;
        }

        // Check for Draw
        boolean isFull = true;
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (buttonStates[r][c] == ButtonState.EMPTY) {
                    isFull = false;
                    break;
                }
            }
        }

        if (isFull) {
            this.state = BoardState.DRAW;
        }

        // No winner or draw
        return Players.NONE;
    }

    /**
     * Sets the board to CLAIMED_X and all of its Buttons to CLAIMED_X
     * @param winner {@link Players#BLUE} or {@link Players#RED}
     */
    private void claimAll(Players winner) {
        ButtonState winningState;
        if (winner == Players.BLUE) {
            this.state = BoardState.CLAIMED_BLUE;
            winningState = ButtonState.CLAIMED_BLUE;
        } else {
            this.state = BoardState.CLAIMED_RED;
            winningState = ButtonState.CLAIMED_RED;
        }
        for(int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                buttonStates[r][c] = winningState;
            }
        }
    }

    /**
     * Disables all buttons of the current local board
     */
    public void disableAllButtons() {
        for(int row = 0; row < 3; row++) {
            for(int column = 0; column < 3; column++) {
                board[row][column].setDisable(true);
            }
        }
    }

    /**
     * Enables all buttons of the current local board, unless they are already claimed
     */
    public void enableAllButtons() {
        for(int row = 0; row < 3; row++) {
            for(int column = 0; column < 3; column++) {
                if(buttonStates[row][column] == ButtonState.CLAIMED_RED || buttonStates[row][column] == ButtonState.CLAIMED_BLUE) continue;
                board[row][column].setDisable(false);
            }
        }
    }

    /**
     * Paints all buttons in the winner's color, using a {@link Timeline} on a 75 ms delay
     */
    private void paintAllButtons() {
        //no painting done if no winner
        if (this.state == BoardState.UNCLAIMED || this.state == BoardState.DRAW) return;

        String style;
        if (this.state == BoardState.CLAIMED_BLUE) {
            style = "-fx-background-color: #007aff";
        } else {
            style = "-fx-background-color: #fc3c2f";
        }

        Timeline timeline = new Timeline();
        int delayCounter = 0;

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {

                Button button = board[row][column];

                if(button.getStyle().equals(style)) {
                    continue;
                }
                // Schedule each button to change after a specific delay
                KeyFrame frame = new KeyFrame(
                        Duration.millis(delayCounter * 75),
                        event -> button.setStyle(style)
                );

                timeline.getKeyFrames().add(frame);
                delayCounter++;
            }
        }

        timeline.play();
    }

    /**
     * Checks if the board has had all its tiles claimed
     * @return false if there are empty tiles <br>
     * true if all tiles have been claimed
     */
    public boolean isFull() {
        for(int row = 0; row < 3; row++) {
            for(int column = 0; column < 3; column++) {
                if(buttonStates[row][column] == ButtonState.EMPTY) {
                    return false;
                }
            }
        }
        // if no empty tile found, return true
        return true;
    }

    private boolean isClaimed(ButtonState s) {
        return s == ButtonState.CLAIMED_RED || s == ButtonState.CLAIMED_BLUE;
    }

    public BoardState getState() {
        return state;
    }
}