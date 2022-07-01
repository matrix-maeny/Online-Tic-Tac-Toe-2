package com.matrix_maeny.onlinetictactoe2;

import java.util.ArrayList;

public class Moves {

    private String firstMove = "";
    private String secondMove = "";
    private String turn = "";
    private int touchCount = -1;
    private String winner = "DRAW";
    private int extraTurn = -1;

    private int player1Score = 0;
    private int player2Score = 0;

    private ArrayList<SingleMoves> gameMoves = new ArrayList<>();
    private ArrayList<MatchedMoves> matchedBoxes = new ArrayList<>();

    public Moves() {

        for (int i = 0; i < 108; i++) {
            gameMoves.add(new SingleMoves());
            matchedBoxes.add(new MatchedMoves());
        }

    }


    public int getExtraTurn() {
        return extraTurn;
    }

    public void setExtraTurn(int extraTurn) {
        this.extraTurn = extraTurn;
    }

    public void createMatch(int player, int r1, int c1, int r2, int c2, int r3, int c3) {
        //1 for host, 2 for client

        int i = 0;

        while (matchedBoxes.get(i).getPlayer() != -1) {
            i++;
        }

        matchedBoxes.get(i).setPlayer(player);
        matchedBoxes.get(i).setR1(r1);
        matchedBoxes.get(i).setR2(r2);
        matchedBoxes.get(i).setR3(r3);
        matchedBoxes.get(i).setC1(c1);
        matchedBoxes.get(i).setC2(c2);
        matchedBoxes.get(i).setC3(c3);


    }

    public ArrayList<MatchedMoves> getMatchedBoxes() {
        return matchedBoxes;
    }

    public void setMatchedBoxes(ArrayList<MatchedMoves> matchedBoxes) {
        this.matchedBoxes = matchedBoxes;
    }

    public void resetMoves() {
        for (int i = 0; i < 108; i++) {
            gameMoves.get(i).setPlayer(-1);
            gameMoves.get(i).setRow(-1);
            gameMoves.get(i).setColumn(-1);

            matchedBoxes.get(i).setPlayer(-1);
            matchedBoxes.get(i).setR3(-1);
            matchedBoxes.get(i).setR2(-1);
            matchedBoxes.get(i).setR1(-1);
            matchedBoxes.get(i).setC3(-1);
            matchedBoxes.get(i).setC2(-1);
            matchedBoxes.get(i).setC1(-1);
        }
    }

    public void setMove(int player, int row, int column) { // for setting moves

        gameMoves.get(touchCount).setPlayer(player);
        gameMoves.get(touchCount).setRow(row);
        gameMoves.get(touchCount).setColumn(column);
    }


    public int getPlayer1Score() {
        return player1Score;
    }

    public void setPlayer1Score(int player1Score) {
        this.player1Score = player1Score;
    }

    public int getPlayer2Score() {
        return player2Score;
    }

    public void setPlayer2Score(int player2Score) {
        this.player2Score = player2Score;
    }

    public ArrayList<SingleMoves> getGameMoves() {
        return gameMoves;
    }

    public void setGameMoves(ArrayList<SingleMoves> gameMoves) {
        this.gameMoves = gameMoves;
    }

    public String getFirstMove() {
        return firstMove;
    }

    public void setFirstMove(String firstMove) {
        this.firstMove = firstMove;
    }

    public String getSecondMove() {
        return secondMove;
    }

    public void setSecondMove(String secondMove) {
        this.secondMove = secondMove;
    }

    public String getTurn() {
        return turn;
    }

    public void setTurn(String turn) {
        this.turn = turn;
    }

    public int getTouchCount() {
        return touchCount;
    }

    public void setTouchCount(int touchCount) {
        this.touchCount = touchCount;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }


    protected static class SingleMoves {

        private int player = -1;
        private int row = -1;
        private int column = -1;

        public SingleMoves() {
        }

        public int getPlayer() {
            return player;
        }

        public void setPlayer(int player) {
            this.player = player;
        }

        public int getRow() {
            return row;
        }

        public void setRow(int row) {
            this.row = row;
        }

        public int getColumn() {
            return column;
        }

        public void setColumn(int column) {
            this.column = column;
        }
    }

    protected static class MatchedMoves {

        private int r1 = -1, r2 = -1, r3 = -1;
        private int c1 = -1, c2 = -1, c3 = -1;
        private int player = -1;

        public MatchedMoves() {
        }

        public int getPlayer() {
            return player;
        }

        public void setPlayer(int player) {
            this.player = player;
        }

        public int getR1() {
            return r1;
        }

        public void setR1(int r1) {
            this.r1 = r1;
        }

        public int getR2() {
            return r2;
        }

        public void setR2(int r2) {
            this.r2 = r2;
        }

        public int getR3() {
            return r3;
        }

        public void setR3(int r3) {
            this.r3 = r3;
        }

        public int getC1() {
            return c1;
        }

        public void setC1(int c1) {
            this.c1 = c1;
        }

        public int getC2() {
            return c2;
        }

        public void setC2(int c2) {
            this.c2 = c2;
        }

        public int getC3() {
            return c3;
        }

        public void setC3(int c3) {
            this.c3 = c3;
        }
    }


}
