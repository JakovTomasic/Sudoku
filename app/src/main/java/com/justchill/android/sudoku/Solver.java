package com.justchill.android.sudoku;

import android.util.Log;
import android.util.Pair;

import java.lang.reflect.AccessibleObject;
import java.util.HashSet;

class Solver {


    static private HashSet<Pair<Integer, Integer>> dfsCellVisited = new HashSet<>();

    static void removeNotes(Board board, int guess, int row, int col, int boxNumber) {
        dfsCellVisited.clear();
        removeNotesFromBoxDfs(board, guess, row, col, boxNumber);
        removeNotesFromRow(board, guess, row, Board.UNDEFINED);
        removeNotesFromCol(board, guess, col, Board.UNDEFINED);
    }

    private static void removeNotesFromBoxDfs(Board board, int note, int row, int col, int boxNumber) {

        if(boxNumber == Cell.UNDEFINED_BOX_NUMBER) return;
        if(row < 0 || row >= board.getNumberOfColumns() || col < 0 || col >= board.getNumberOfRows()) return;
        if(board.getBoxNumber(row, col) != boxNumber) return;

        Pair<Integer, Integer> current = new Pair<>(row, col);

        if(dfsCellVisited.contains(current)) return;
        dfsCellVisited.add(current);

        board.getCell(row, col).removeNote(note);

        removeNotesFromBoxDfs(board, note, row+1, col, boxNumber);
        removeNotesFromBoxDfs(board, note, row-1, col, boxNumber);
        removeNotesFromBoxDfs(board, note, row, col+1, boxNumber);
        removeNotesFromBoxDfs(board, note, row, col-1, boxNumber);
    }

    static boolean removeNotesFromRow(Board board, int noteNumber, int row, int exceptFromBox) {
        if(row == Board.UNDEFINED) return false;
        boolean noteRemoved = false;

        for(int i = 0; i < board.getNumberOfColumns(); i++) {
            if(board.getBoxNumber(row, i) == exceptFromBox) continue;

            noteRemoved = board.getCell(row, i).removeNote(noteNumber) || noteRemoved;
        }

        return noteRemoved;
    }

    static boolean removeNotesFromCol(Board board, int noteNumber, int col, int exceptFromBox) {
        if(col == Board.UNDEFINED) return false;
        boolean noteRemoved = false;

        for(int i = 0; i < board.getNumberOfRows(); i++) {
            if(board.getBoxNumber(i, col) == exceptFromBox) continue;

            noteRemoved = board.getCell(i, col).removeNote(noteNumber) || noteRemoved;
        }

        return noteRemoved;
    }

    static boolean removeWrongNotes(Board board, int number, int boxes, int rows, int cols) {
        int[][] numberOfNumberInTheBox = new int[number+1][boxes];
        boolean cellSolved = false;

        // TODO: optimize this

        for(int r = 0; r < rows; r++) {
            for(int c = 0; c < cols; c++) {
                for (int note : board.getCell(r, c).getNotes()) {
                    numberOfNumberInTheBox[note][board.getBoxNumber(r, c)]++;
                }
            }
        }

        // Horizontal
        for(int r = 0; r < rows; r++) {
            int[][] numberOfNumberInTheLine = new int[number+1][boxes];
            for(int c = 0; c < cols; c++) {
                for (int note : board.getCell(r, c).getNotes()) {
                    numberOfNumberInTheLine[note][board.getBoxNumber(r, c)]++;
                }
            }

            for(int i = 1; i <= number; i++) {
                for(int j = 0; j < boxes; j++) {
                    if(numberOfNumberInTheBox[i][j] <= 0 || numberOfNumberInTheLine[i][j] <= 0) continue;
                    if(numberOfNumberInTheBox[i][j] != numberOfNumberInTheLine[i][j]) continue;

                    cellSolved = removeNotesFromRow(board, i, r, j) || cellSolved;
                }
            }
        }

        // Vertical
        for(int c = 0; c < cols; c++) {
            int[][] numberOfNumberInTheLine = new int[number+1][boxes];
            for(int r = 0; r < rows; r++) {
                for (int note : board.getCell(r, c).getNotes()) {
                    numberOfNumberInTheLine[note][board.getBoxNumber(r, c)]++;
                }
            }

            for(int i = 1; i <= number; i++) {
                for(int j = 0; j < boxes; j++) {
                    if(numberOfNumberInTheBox[i][j] <= 0 || numberOfNumberInTheLine[i][j] <= 0) continue;
                    if(numberOfNumberInTheBox[i][j] != numberOfNumberInTheLine[i][j]) continue;

                    cellSolved = removeNotesFromCol(board, i, c, j) || cellSolved;
                }
            }
        }

        return cellSolved;

    }

    static boolean solveNextStep(Board board, int number, int boxes, int rows, int cols) {
        return removeWrongNotes(board, number, boxes, rows, cols) || solveCellsWithOneNote(board, rows, cols) ||
                solveBoxes(board, number, boxes, rows, cols) || solveRowsAndCols(board, number, rows, cols);
    }

    static boolean solveCellsWithOneNote(Board board, int rows, int cols) {
        boolean cellSolved = false;
        for(int r = 0; r < rows; r++) {
            for(int c = 0; c < cols; c++) {
                if(board.getCell(r, c).getNotes().size() == 1) {
                    int note = board.getCell(r, c).getNotes().iterator().next();
                    solveCell(board, note, r, c);
                    cellSolved = true;
                    break;
                }
            }
        }

        return cellSolved;
    }

    static boolean solveBoxes(Board board, int number, int boxes, int rows, int cols) {
        int[][] numberOfNumberInTheBox = new int[number+1][boxes];
        boolean cellSolved = false;

        // TODO: optimize this

        for(int r = 0; r < rows; r++) {
            for(int c = 0; c < cols; c++) {
                for (int note : board.getCell(r, c).getNotes()) {
                    numberOfNumberInTheBox[note][board.getBoxNumber(r, c)]++;
                }
            }
        }

        for(int r = 0; r < rows; r++) {
            for(int c = 0; c < cols; c++) {

                for (int note : board.getCell(r, c).getNotes()) {
                    if(numberOfNumberInTheBox[note][board.getBoxNumber(r, c)] == 1) {
                        solveCell(board, note, r, c);
                        cellSolved = true;
                        break;
                    }
                }

            }
        }

        return cellSolved;
    }

    static boolean solveRowsAndCols(Board board, int number, int rows, int cols) {
        int[] numberOfNumber;
        boolean cellSolved = false;

        // TODO: optimize this

        // Horizontal
        for(int r = 0; r < rows; r++) {
            numberOfNumber = new int[number+1];
            for(int c = 0; c < cols; c++) {
                for (int note : board.getCell(r, c).getNotes()) {
                    numberOfNumber[note]++;
                }
            }
            for(int c = 0; c < cols; c++) {
                for (int note : board.getCell(r, c).getNotes()) {
                    if(numberOfNumber[note] == 1) {
                        solveCell(board, note, r, c);
                        cellSolved = true;
                        break;
                    }
                }
            }
        }

        // Vertical
        for(int c = 0; c < cols; c++) {
            numberOfNumber = new int[number+1];
            for(int r = 0; r < rows; r++) {
                for (int note : board.getCell(r, c).getNotes()) {
                    numberOfNumber[note]++;
                }
            }
            for(int r = 0; r < rows; r++) {
                for (int note : board.getCell(r, c).getNotes()) {
                    if(numberOfNumber[note] == 1) {
                        solveCell(board, note, r, c);
                        cellSolved = true;
                        break;
                    }
                }
            }
        }


        return cellSolved;
    }

    static void solveCell(Board board, int guess, int row, int col) {
        board.getCell(row, col).setSolution(guess);
        board.getCell(row, col).setState(Cell.STATE_SOLVED);
        board.getCell(row, col).clearNotes();

        removeNotes(board, guess, row, col, board.getBoxNumber(row, col));
    }


}
