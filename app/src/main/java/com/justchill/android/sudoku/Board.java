package com.justchill.android.sudoku;

import android.graphics.Canvas;
import android.util.Log;
import android.util.Pair;

import java.util.HashSet;

class Board implements Cloneable {

    public static final int UNDEFINED = -1;

    private int number;
    private int rows, cols;
    private int boxRows, boxCols; // TODO: this may not be needed

    private int[][] boxMap;

    private Cell[][] cells;

    private int selectedRow = UNDEFINED, selectedCol = UNDEFINED;

    private Solver solver;

    Board(int number, int rows, int cols, int boxRows, int boxCols, int[][] boxMap) {
        this.number = number;
        this.rows = rows;
        this.cols = cols;
        this.boxRows = boxRows;
        this.boxCols = boxCols;
        this.boxMap = boxMap;
        this.cells = new Cell[cols][rows];

        for(int i = 0; i < rows; i++) {
            for(int j = 0; j < cols; j++) {
                this.cells[i][j] = new Cell(Cell.UNDEFINED_SOLUTION, Cell.STATE_NOT_SOLVED);
            }
        }

        this.solver = new Solver(this);
    }

    Board(Board cloneFromBoard) {
        this.number = cloneFromBoard.number;
        this.rows = cloneFromBoard.rows;
        this.cols = cloneFromBoard.cols;
        this.boxRows = cloneFromBoard.boxRows;
        this.boxCols = cloneFromBoard.boxCols;
        this.boxMap = cloneFromBoard.boxMap;

        this.selectedRow = cloneFromBoard.selectedRow;
        this.selectedCol = cloneFromBoard.selectedCol;

        this.cells = new Cell[cols][rows];

        for(int i = 0; i < rows; i++) {
            for(int j = 0; j < cols; j++) {
                this.cells[i][j] = new Cell(cloneFromBoard.getCell(i, j));
            }
        }

        this.solver = new Solver(this);
    }

    int getNumber() {
        return number;
    }

    int getNumberOfRows() {
        return rows;
    }

    int getNumberOfColumns() {
        return cols;
    }

    int getBoxNumber(int row, int col) {
        if(col < 0 || col >= cols || row < 0 || row >= rows) return Cell.UNDEFINED_BOX_NUMBER;
        return boxMap[row][col];
    }

    Cell getCell(int row, int col) {
        if(col < 0 || col >= cols || row < 0 || row >= rows) return null;
        return cells[row][col];
    }

    void setSelected(int row, int col) {
        this.selectedRow = row;
        this.selectedCol = col;
    }

    int getSelectedRow() {
        return selectedRow;
    }

    int getSelectedColumn() {
        return selectedCol;
    }

    int getSelectedBoxNumber() {
        return getBoxNumber(getSelectedRow(), getSelectedColumn());
    }

    Cell getSelectedCell() {
        return getCell(getSelectedRow(), getSelectedColumn());
    }

    int getBoxRows() {
        return boxRows;
    }

    int getBoxColumns() {
        return boxCols;
    }

    int getNumberOfBoxes() {
        return number;
    }

    Solver getSolver() {
        return this.solver;
    }

    void onResultGuess(int guess) {

        if(guess == getSelectedCell().getSolution()) {
            getSelectedCell().setState(Cell.STATE_SOLVED);
            this.solver.removeNotes(guess, getSelectedRow(), getSelectedColumn(), getSelectedBoxNumber());
        } else {
            getSelectedCell().setState(Cell.STATE_WRONG);
            getSelectedCell().setGuessNumber(guess);
        }

    }


}
