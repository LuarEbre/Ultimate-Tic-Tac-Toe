package ultimate.ttt;

import javafx.scene.control.Button;

public class UltimateBoard {
    private Board[][] boards;
    private Players currentTurn;

    /**
     * Constructs all local boards and assigns them to {@link #boards}
     * @param buttons 4D array of buttons
     * @param firstTurn {@link Players} who has the first turn
     */
    public UltimateBoard(Button[][][][] buttons, Players firstTurn) {

        this.boards = new Board[3][3];
        this.currentTurn = firstTurn;

        for(int row = 0; row < 3; row++) {
            for(int column = 0; column < 3; column++) {

                Button[][] localButtons = buttons[row][column];

                boards[row][column] = new Board(localButtons);
            }
        }
    }

    /**
     * Processes a player's move on the global Ultimate Tic Tac Toe board. <br>
     * Calls {@link Board#buttonPress} of the appropriate local {@link Board}. <br>
     * After the move is recorded, it calculates which local board the next player
     * is required to play in, based on the local coordinates of the current move.
     * If the target board is already claimed or drawn, it enables all remaining
     * unclaimed boards, granting the next player a "free move". Finally, it evaluates
     * if this move resulted in a victory for the entire game.
     *
     * @param globalRow    The row index (0-2) of the local board within the macro-board.
     * @param globalColumn The column index (0-2) of the local board within the macro-board.
     * @param localRow     The row index (0-2) of the clicked tile within the specific local board.
     * @param localColumn  The column index (0-2) of the clicked tile within the specific local board.
     * @return             The {@link Players} enum representing the overall game winner <br>
     * {@link Players#BLUE} or {@link Players#RED} in case of there being a winner <br>
     * {@link Players#NONE} if the game is still ongoing.
     */
    public Players buttonPress(int globalRow, int globalColumn, int localRow, int localColumn) {
        Board affectedBoard = boards[globalRow][globalColumn];
        affectedBoard.buttonPress(localRow, localColumn, currentTurn);

        Board nextBoard = boards[localRow][localColumn];
        if(nextBoard.getState() == BoardState.UNCLAIMED) {
            this.disableAllBoards();
            nextBoard.enableAllButtons();
        } else {
            this.enableAllBoards();
        }

        return checkForWinner();
    }
    /**
     * Checks for global winners horizontally, vertically, and diagonally (top-right to bottom-left & top-left to bottom-right)
     * @return {@link Players#RED} or {@link Players#BLUE} in case of there being a winner <br>
     * {@link Players#NONE} if there is no winner
     */
    private Players checkForWinner() {

        for (int i = 0; i < 3; i++) {
            // Horizontal check
            if (isBoardClaimedBy(i, 0) != Players.NONE &&
                    isBoardClaimedBy(i, 0) == isBoardClaimedBy(i, 1) &&
                    isBoardClaimedBy(i, 0) == isBoardClaimedBy(i, 2)) {
                return isBoardClaimedBy(i, 0);
            }

            // Vertical check
            if (isBoardClaimedBy(0, i) != Players.NONE &&
                    isBoardClaimedBy(0, i) == isBoardClaimedBy(1, i) &&
                    isBoardClaimedBy(0, i) == isBoardClaimedBy(2, i)) {
                return isBoardClaimedBy(0, i);
            }
        }

        // Diagonal checks
        if (isBoardClaimedBy(0, 0) != Players.NONE &&
                isBoardClaimedBy(0, 0) == isBoardClaimedBy(1, 1) &&
                isBoardClaimedBy(0, 0) == isBoardClaimedBy(2, 2)) {
            return isBoardClaimedBy(0, 0);
        }

        if (isBoardClaimedBy(0, 2) != Players.NONE &&
                isBoardClaimedBy(0, 2) == isBoardClaimedBy(1, 1) &&
                isBoardClaimedBy(0, 2) == isBoardClaimedBy(2, 0)) {
            return isBoardClaimedBy(0, 2);
        }

        return Players.NONE;
    }

    private boolean canPlayerWinGlobal(Players player) {
        BoardState opponentBoard = (player == Players.BLUE) ? BoardState.CLAIMED_RED : BoardState.CLAIMED_BLUE;

        for (int i = 0; i < 3; i++) {
            // Check Rows
            if (!isBlocking(boards[i][0].getState(), opponentBoard) &&
                    !isBlocking(boards[i][1].getState(), opponentBoard) &&
                    !isBlocking(boards[i][2].getState(), opponentBoard)) return true;

            // Check Columns
            if (!isBlocking(boards[0][i].getState(), opponentBoard) &&
                    !isBlocking(boards[1][i].getState(), opponentBoard) &&
                    !isBlocking(boards[2][i].getState(), opponentBoard)) return true;
        }

        // Check Diagonals
        if (!isBlocking(boards[0][0].getState(), opponentBoard) &&
                !isBlocking(boards[1][1].getState(), opponentBoard) &&
                !isBlocking(boards[2][2].getState(), opponentBoard)) return true;

        if (!isBlocking(boards[0][2].getState(), opponentBoard) &&
                !isBlocking(boards[1][1].getState(), opponentBoard) &&
                !isBlocking(boards[2][0].getState(), opponentBoard)) return true;

        return false;
    }

    /**
     * A local board blocks a player's path if the opponent claimed it, OR if it resulted in a draw.
     */
    private boolean isBlocking(BoardState state, BoardState opponentBoard) {
        return state == opponentBoard || state == BoardState.DRAW;
    }

    /**
     * Predicts if the entire game has entered an unwinnable state for both players.
     */
    protected boolean checkForDraw() {
        return !canPlayerWinGlobal(Players.BLUE) && !canPlayerWinGlobal(Players.RED);
    }

    /**
     * Helper function used in {@link #checkForWinner()}
     * @param r row
     * @param c column
     * @return claimee of the tile at (r, c)
     */
    private Players isBoardClaimedBy(int r, int c) {
        BoardState s = boards[r][c].getState();
        if (s == BoardState.CLAIMED_RED) return Players.RED;
        if (s == BoardState.CLAIMED_BLUE) return Players.BLUE;
        return Players.NONE;
    }

    /**
     * Disables all local boards
     */
    protected void disableAllBoards() {
        for(int row = 0; row < 3; row++) {
            for(int column = 0; column < 3; column++) {
                boards[row][column].disableAllButtons();
            }
        }
    }

    /**
     * Enables all unclaimed local boards
     */
    private void enableAllBoards() {
        for(int row = 0; row < 3; row++) {
            for(int column = 0; column < 3; column++) {
                if(boards[row][column].getState() != BoardState.UNCLAIMED) continue;
                boards[row][column].enableAllButtons();
            }
        }
    }

    /**
     * Swaps the player's control
     */
    protected void switchPlayers() {
        if(currentTurn == Players.BLUE) currentTurn = Players.RED;
        else if(currentTurn == Players.RED) currentTurn = Players.BLUE;
    }
}