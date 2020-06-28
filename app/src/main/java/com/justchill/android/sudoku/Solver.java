package com.justchill.android.sudoku;

import android.util.Pair;

import java.util.ArrayList;
import java.util.HashSet;

// TODO: implement and add terminology to comments: https://www.kristanix.com/sudokuepic/sudoku-solving-techniques.php
// TODO: rename function based on this: https://www.kristanix.com/sudokuepic/sudoku-solving-techniques.php
// TODO?: ??? maybe not so many static functions -> instead implement solver.addBoard

// TODO: difficulty - first get solution like till now - then solve with only selected funcitons/techniques. If something can't be solved with it, solve with others and add that cell to STATE_START_NUMBER

class Solver {

    private Board board;
    private int number, boxes, rows, cols;

    Solver(Board board) {
        this.board = board;
        number = board.getNumber();
        boxes = board.getNumberOfBoxes();
        rows = board.getNumberOfRows();
        cols = board.getNumberOfColumns();
    }

    boolean removeNotes(int guess, int row, int col, int boxNumber) {
        // TODO: other data type? (HashSet)
        boolean removed = removeNotesFromBox(guess, row, col, boxNumber, new HashSet<Pair<Integer, Integer>>());
        removed = removeNotesFromRow(guess, row, Board.UNDEFINED) || removed;
        removed = removeNotesFromCol(guess, col, Board.UNDEFINED) || removed;
        return removed;
    }

    void removeNotesSmart() {
        boolean removed;
        do {
            removed = removeNotesRowInteraction();
            removed = removeNotesBlockInteraction() || removed;
            removed = removeNotesNakedSubset() || removed;
            removed = removeNotesHiddenSubset() || removed;
            removed = removeNotesSwordfishAndXWing() || removed;
            removed = removeNotesForcingChain() || removed;
        } while (removed);
    }

    private boolean removeNotesFromBox(int note, int row, int col, int boxNumber, HashSet<Pair<Integer, Integer>> cellsVisited) {
        if(boxNumber == Cell.UNDEFINED_BOX_NUMBER) return false;
        if(row < 0 || row >= rows || col < 0 || col >= cols) return false;
        if(board.getBoxNumber(row, col) != boxNumber) return false;

        Pair<Integer, Integer> current = new Pair<>(row, col);

        if(cellsVisited.contains(current)) return false;
        cellsVisited.add(current);

        boolean removed = board.getCell(row, col).removeNote(note);

        removed = removeNotesFromBox(note, row+1, col, boxNumber, cellsVisited) || removed;
        removed = removeNotesFromBox(note, row-1, col, boxNumber, cellsVisited) || removed;
        removed = removeNotesFromBox(note, row, col+1, boxNumber, cellsVisited) || removed;
        removed = removeNotesFromBox(note, row, col-1, boxNumber, cellsVisited) || removed;
        return removed;
    }

    private boolean removeNotesFromRow(int noteNumber, int row, int exceptFromBox) {
        if(row == Board.UNDEFINED) return false;
        boolean noteRemoved = false;

        for(int i = 0; i < board.getNumberOfColumns(); i++) {
            if(board.getBoxNumber(row, i) == exceptFromBox) continue;

            noteRemoved = board.getCell(row, i).removeNote(noteNumber) || noteRemoved;
        }

        return noteRemoved;
    }

    private boolean removeNotesFromCol(int noteNumber, int col, int exceptFromBox) {
        if(col == Board.UNDEFINED) return false;
        boolean noteRemoved = false;

        for(int i = 0; i < board.getNumberOfRows(); i++) {
            if(board.getBoxNumber(i, col) == exceptFromBox) continue;

            noteRemoved = board.getCell(i, col).removeNote(noteNumber) || noteRemoved;
        }

        return noteRemoved;
    }

    /**
     * Technique: Block and column / Row Interaction
     *
     * @return
     */
    private boolean removeNotesRowInteraction() {
        int[][] numberOfNumberInTheBox = new int[number+1][boxes];
        boolean removed = false;

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

                    removed = removeNotesFromRow(i, r, j) || removed;
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

                    removed = removeNotesFromCol(i, c, j) || removed;
                }
            }
        }

        return removed;

    }

    // TODO: finish
    private boolean removeNotesBlockInteraction() {
        return false;
    }

    private boolean removeNotesNakedSubset() {
        boolean removed = false;
        for(int r = 0; r < rows; r++) {
            for(int c = 0; c < cols; c++) {
                ArrayList<Pair<Integer, Integer>> dontRemove = new ArrayList<>();
                // Horizontal
                if(checkNakedSubsetLine(dontRemove, r, 0, 1, 0)) {
                    removed = removeNakedSubsetLine(dontRemove, r, 0, 1, 0) || removed;
                }
                dontRemove.clear();
                // Vertical
                if(checkNakedSubsetLine(dontRemove, 0, c, 0, 1)) {
                    removed = removeNakedSubsetLine(dontRemove, 0, c, 0, 1) || removed;
                }
                dontRemove.clear();
                // Box
                if(checkNakedSubsetBox(dontRemove, board.getCell(r, c), r, c, board.getBoxNumber(r, c), new HashSet<Pair<Integer, Integer>>())) {
                    removed = removeNakedSubsetBox(dontRemove, board.getCell(r, c), r, c, board.getBoxNumber(r, c), new HashSet<Pair<Integer, Integer>>()) > 0 || removed;
                }
            }
        }
        return removed;
    }

    private boolean checkNakedSubsetLine(ArrayList<Pair<Integer, Integer>> dontRemove, int row, int col, int dirX, int dirY) {
        Cell compareWith = board.getCell(row, col);
        dontRemove.add(new Pair<>(row, col));
        row += dirY; col += dirX;
        while(row >= 0 && row < rows && col >= 0 && col < cols) {
            if(compareWith.getNotes().equals(board.getCell(row, col).getNotes()))
                dontRemove.add(new Pair<>(row, col));
            row += dirY;
            col += dirX;
        }
        return dontRemove.size() >= compareWith.getNotes().size();
    }

    private boolean removeNakedSubsetLine(ArrayList<Pair<Integer, Integer>> dontRemove, int row, int col, int dirX, int dirY) {
        boolean removed = false;
        Cell mainCell = board.getCell(row, col);
        row += dirY; col += dirX;
        while(row >= 0 && row < rows && col >= 0 && col < cols) {
            if(!dontRemove.contains(new Pair<>(row, col)))
                removed = board.getCell(row, col).removeNotes(mainCell.getNotes()) || removed;
            row += dirY;
            col += dirX;
        }
        return removed;
    }

    private boolean checkNakedSubsetBox(ArrayList<Pair<Integer, Integer>> dontRemove, Cell startingCell, int row, int col, int box, HashSet<Pair<Integer, Integer>> cellsVisited) {
        if(box == Cell.UNDEFINED_BOX_NUMBER) return false;
        if(row < 0 || row >= rows || col < 0 || col >= cols) return false;
        if(board.getBoxNumber(row, col) != box) return false;

        Pair<Integer, Integer> current = new Pair<>(row, col);

        if(cellsVisited.contains(current)) return false;
        cellsVisited.add(current);

        if(startingCell.getNotes().equals(board.getCell(row, col).getNotes()))
            dontRemove.add(new Pair<>(row, col));


        checkNakedSubsetBox(dontRemove, startingCell, row+1, col, box, cellsVisited);
        checkNakedSubsetBox(dontRemove, startingCell, row-1, col, box, cellsVisited);
        checkNakedSubsetBox(dontRemove, startingCell, row, col+1, box, cellsVisited);
        checkNakedSubsetBox(dontRemove, startingCell, row, col-1, box, cellsVisited);
        return dontRemove.size() >= startingCell.getNotes().size();
    }

    private int removeNakedSubsetBox(ArrayList<Pair<Integer, Integer>> dontRemove, Cell startingCell, int row, int col, int box, HashSet<Pair<Integer, Integer>> cellsVisited) {
        if(box == Cell.UNDEFINED_BOX_NUMBER) return 0;
        if(row < 0 || row >= rows || col < 0 || col >= cols) return 0;
        if(board.getBoxNumber(row, col) != box) return 0;

        Pair<Integer, Integer> current = new Pair<>(row, col);

        if(cellsVisited.contains(current)) return 0;
        cellsVisited.add(current);

        int removed = 0;

        if(!dontRemove.contains(new Pair<>(row, col)))
            removed += board.getCell(row, col).removeNotes(startingCell.getNotes()) ? 1 : 0;

        removed += removeNakedSubsetBox(dontRemove, startingCell, row+1, col, box, cellsVisited);
        removed += removeNakedSubsetBox(dontRemove, startingCell, row-1, col, box, cellsVisited);
        removed += removeNakedSubsetBox(dontRemove, startingCell, row, col+1, box, cellsVisited);
        removed += removeNakedSubsetBox(dontRemove, startingCell, row, col-1, box, cellsVisited);
        return removed;
    }


    // TODO: finish
    private boolean removeNotesHiddenSubset() {
        return false;
    }

    // TODO: finish
    private boolean removeNotesSwordfishAndXWing() {
        return false;
    }

    // TODO: finish
    private boolean removeNotesForcingChain() {
        return false;
    }


    private boolean solveNextStep() {
        removeNotesSmart();
        boolean solved = solveCellsWithOneNote();
        solved = solveBoxes() || solved;
        solved = solveRowsAndCols() || solved;
        return solved;
    }

    void tryToSolveAll() {
        while(solveNextStep());
    }

    private boolean solveCellsWithOneNote() {
        boolean cellSolved = false;
        for(int r = 0; r < rows; r++) {
            for(int c = 0; c < cols; c++) {
                Cell current = board.getCell(r, c);
                if(current.getNotes().size() != 1 || current.getState() == Cell.STATE_START_NUMBER || current.getState() == Cell.STATE_SOLVED) continue;
                int note = board.getCell(r, c).getNotes().iterator().next();
                solveCell(note, r, c);
                cellSolved = true;
            }
        }

        return cellSolved;
    }

    private boolean solveBoxes() {
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
                        solveCell(note, r, c);
                        cellSolved = true;
                        break;
                    }
                }

            }
        }

        return cellSolved;
    }

    private boolean solveRowsAndCols() {
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
                        solveCell(note, r, c);
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
                        solveCell(note, r, c);
                        cellSolved = true;
                        break;
                    }
                }
            }
        }


        return cellSolved;
    }

    void solveCell(int guess, int row, int col) {
        board.getCell(row, col).setSolution(guess);
        board.getCell(row, col).setState(Cell.STATE_SOLVED);
        board.getCell(row, col).clearNotes();

        removeNotes(guess, row, col, board.getBoxNumber(row, col));
    }


}
