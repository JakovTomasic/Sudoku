package com.justchill.android.sudoku;

import android.util.Log;

class Game {

    private int number;

    Board board;
    BoardView boardView;

    boolean realNumberInput;

    Game(int number) {
        this.number = number;

        BoardBuilder boardBuilder = new BoardBuilder();
        boardBuilder.setNumber(number);

        this.board = boardBuilder.build();

        realNumberInput = true;
    }

    void onNumberClick(int number) {
        if(board.getSelectedCell() == null) return;

        if(realNumberInput) {
            board.onResultGuess(number);
        } else {
            board.getSelectedCell().clickNote(number);
        }

        boardView.invalidate();
    }

    boolean onNotesSwitchClick() {
        return realNumberInput = !realNumberInput;
    }

    void setBoardView(BoardView boardView) {
        this.boardView = boardView;
    }

    Board getBoard() {
        return board;
    }

    void start() {
        boardView.setBoard(board);
    }

}
