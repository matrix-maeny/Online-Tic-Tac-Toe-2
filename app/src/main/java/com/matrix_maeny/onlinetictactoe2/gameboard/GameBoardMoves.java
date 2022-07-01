package com.matrix_maeny.onlinetictactoe2.gameboard;

public class GameBoardMoves {

    private int[][] gameMoves;
    private int[][] gameMatchedBoxes;
    private int selectedRow;
    private int selectedColumn;

    public GameBoardMoves() {
        selectedColumn = -1;
        selectedRow = -1;
        gameMoves = new int[12][9];
        gameMatchedBoxes = new int[12][9];

        for (int i = 0; i < 12; i++) {
            for (int j = 0; j < 9; j++) {
                gameMoves[i][j] = -1;
                gameMatchedBoxes[i][j] = -1;
            }
        }
    }

    public void createMatch(int player,int r1,int c1,int r2,int c2,int r3,int c3){
        //1 for host, 2 for client
        gameMatchedBoxes[r1][c1] = gameMatchedBoxes[r2][c2] = gameMatchedBoxes[r3][c3] = player;
    }

    public int[][] getGameMatchedBoxes() {
        return gameMatchedBoxes;
    }

    public void setGameMatchedBoxes(int[][] gameMatchedBoxes) {
        this.gameMatchedBoxes = gameMatchedBoxes;
    }

    public int[][] getGameMoves() {
        return gameMoves;
    }

    public void setSelectedMove(int player) {
        // 1 for host,2 for client

        gameMoves[selectedRow - 1][selectedColumn - 1] = player;
    }

    public void setMoves(int r, int c, int player) {
        gameMoves[r][c] = player;

    }

    public void resetGame() {

        for (int i = 0; i < 12; i++) {
            for (int j = 0; j < 9; j++) {
                gameMoves[i][j] = -1;
                gameMatchedBoxes[i][j] = -1;
            }
        }

        selectedRow = -1;
        selectedColumn = -1;
    }

    public void setGameMoves(int[][] gameMoves) {
        this.gameMoves = gameMoves;
    }

    public int getSelectedRow() {
        return selectedRow;
    }

    public void setSelectedRow(int selectedRow) {
        this.selectedRow = selectedRow;
    }

    public int getSelectedColumn() {
        return selectedColumn;
    }

    public void setSelectedColumn(int selectedColumn) {
        this.selectedColumn = selectedColumn;
    }
}
