package com.justchill.android.sudoku;

import android.util.Log;
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

    /**
     * Number of each number notes in each row/col/box
     */
    private ArrayList<Pair<Integer, Integer>>[][] numbersInTheRow;
    private ArrayList<Pair<Integer, Integer>>[][] numbersInTheCol;
    private ArrayList<Pair<Integer, Integer>>[][] numbersInTheBox;

    public static long timer1 = 0;
    public static long timer2 = 0;
    public static long timer3 = 0;
    public static long timer4 = 0;
    public static long timer5 = 0;
    public static long timer6 = 0;
    public static long timer7 = 0;
    public static long puta1 = 0;
    public static long puta2 = 0;

    Solver(Board board) {
        this.board = board;
        number = board.getNumber();
        boxes = board.getNumberOfBoxes();
        rows = board.getNumberOfRows();
        cols = board.getNumberOfColumns();
        numbersInTheRow = new ArrayList[rows][number+1];
        numbersInTheCol = new ArrayList[cols][number+1];
        numbersInTheBox = new ArrayList[boxes][number+1];
    }

    Solver(Board board, boolean clone) {
        this.board = board;
        number = board.getNumber();
        boxes = board.getNumberOfBoxes();
        rows = board.getNumberOfRows();
        cols = board.getNumberOfColumns();
        numbersInTheRow = new ArrayList[rows][number+1];
        numbersInTheCol = new ArrayList[cols][number+1];
        numbersInTheBox = new ArrayList[boxes][number+1];
        for(int r = 0; r < rows; r++) {
            for(int c = 0; c < cols; c++) {
                for(int note : board.getCell(r, c).getNotes()) {
                    addCellNote(note, r, c);
                }
            }
        }
    }

    /*************** cell note functions *****************/

    void addCellNote(int note, int row, int col) {
        if(numbersInTheRow[row][note] == null) numbersInTheRow[row][note] = new ArrayList<>();
        if(numbersInTheCol[col][note] == null) numbersInTheCol[col][note] = new ArrayList<>();
        if(numbersInTheBox[board.getBoxNumber(row, col)][note] == null) numbersInTheBox[board.getBoxNumber(row, col)][note] = new ArrayList<>();
        numbersInTheRow[row][note].add(new Pair<>(row, col));
        numbersInTheCol[col][note].add(new Pair<>(row, col));
        numbersInTheBox[board.getBoxNumber(row, col)][note].add(new Pair<>(row, col));
    }

    void removeCellNote(int note, int row, int col) {
        if(numbersInTheRow[row][note] != null) numbersInTheRow[row][note].remove(new Pair<>(row, col));
        if(numbersInTheCol[col][note] != null) numbersInTheCol[col][note].remove(new Pair<>(row, col));
        if(numbersInTheBox[board.getBoxNumber(row, col)][note] != null) numbersInTheBox[board.getBoxNumber(row, col)][note].remove(new Pair<>(row, col));
    }

    void removeCellNotes(HashSet<Integer> notes, int row, int col) {
        for(int note : notes) {
            if(numbersInTheRow[row][note] != null) numbersInTheRow[row][note].remove(new Pair<>(row, col));
            if(numbersInTheCol[col][note] != null) numbersInTheCol[col][note].remove(new Pair<>(row, col));
            if(numbersInTheBox[board.getBoxNumber(row, col)][note] != null)
                    numbersInTheBox[board.getBoxNumber(row, col)][note].remove(new Pair<>(row, col));
        }
    }

    void clearCellNotes(int row, int col) {
        for(int note = 1; note <= number; note++) {
            if(numbersInTheRow[row][note] != null) numbersInTheRow[row][note].remove(new Pair<>(row, col));
            if(numbersInTheCol[col][note] != null) numbersInTheCol[col][note].remove(new Pair<>(row, col));
            if(numbersInTheBox[board.getBoxNumber(row, col)][note] != null) numbersInTheBox[board.getBoxNumber(row, col)][note].remove(new Pair<>(row, col));
        }
    }

    /*****************************************************/


    boolean removeNotes(int guess, int row, int col, int boxNumber) {
        puta1++;
        timer1 -= System.currentTimeMillis();
        boolean removed = removeNotesFromBox(guess, boxNumber);
        timer1 += System.currentTimeMillis();
        timer2 -= System.currentTimeMillis();
        removed = removeNotesFromRow(guess, row, Board.UNDEFINED) || removed;
        timer2 += System.currentTimeMillis();
        timer3 -= System.currentTimeMillis();
        removed = removeNotesFromCol(guess, col, Board.UNDEFINED) || removed;
        timer3 += System.currentTimeMillis();
        return removed;
    }

    boolean removeNotesSmart() {
        boolean removed;
//        do {
//            puta2++;
            removed = removeNotesRowInteraction();
            removed = removeNotesBlockInteraction() || removed;
            removed = removeNotesNakedSubset() || removed;
            removed = removeNotesHiddenSubset() || removed;
            removed = removeNotesSwordfishAndXWing() || removed;
            removed = removeNotesForcingChain() || removed;
//        } while (removed);
        return removed;
    }

    private boolean removeNotesFromBox(int note, int boxNumber) {
        if(boxNumber == Board.UNDEFINED) return false;
        if(numbersInTheBox[boxNumber][note] == null) return false;

        ArrayList<Pair<Integer, Integer>> toRemove = new ArrayList<>();
        for(Pair<Integer, Integer> coord : numbersInTheBox[boxNumber][note]) {
            toRemove.add(new Pair<>(coord.first, coord.second));
        }
        for(Pair<Integer, Integer> coord : toRemove) {
            board.removeCellNote(note, coord.first, coord.second);
        }
        return toRemove.size() > 0;
    }

    private boolean removeNotesFromRow(int noteNumber, int row, int exceptFromBox) {
        if(row == Board.UNDEFINED) return false;
        if(numbersInTheRow[row][noteNumber] == null) return false;

        ArrayList<Pair<Integer, Integer>> toRemove = new ArrayList<>();
        for(Pair<Integer, Integer> coord : numbersInTheRow[row][noteNumber]) {
            if(board.getBoxNumber(coord.first, coord.second) == exceptFromBox) continue;
            toRemove.add(new Pair<>(coord.first, coord.second));
        }
        for(Pair<Integer, Integer> coord : toRemove) {
            board.removeCellNote(noteNumber, coord.first, coord.second);
        }
        return toRemove.size() > 0;
    }

    private boolean removeNotesFromCol(int noteNumber, int col, int exceptFromBox) {
        if(col == Board.UNDEFINED) return false;
        if(numbersInTheCol[col][noteNumber] == null) return false;

        ArrayList<Pair<Integer, Integer>> toRemove = new ArrayList<>();
        for(Pair<Integer, Integer> coord : numbersInTheCol[col][noteNumber]) {
            if(board.getBoxNumber(coord.first, coord.second) == exceptFromBox) continue;
            toRemove.add(new Pair<>(coord.first, coord.second));
        }
        for(Pair<Integer, Integer> coord : toRemove) {
            board.removeCellNote(noteNumber, coord.first, coord.second);
        }
        return toRemove.size() > 0;
    }

    /**
     * Technique: Block and column / Row Interaction
     *
     * @return
     */
    private boolean removeNotesRowInteraction() {
        // TODO: optimize (? is box in row/col bool[number][number])
        boolean removed = false;
        for(int num = 1; num <= number; num++) {
            for(int bx = 0; bx < boxes; bx++) {
                if(numbersInTheBox[bx][num] == null) continue;
                if(numbersInTheBox[bx][num].size() < 2) continue;
                int prevRow = Board.UNDEFINED, prevCol = Board.UNDEFINED;
                boolean rowChanged = false, colChanged = false;
                boolean remove = true;
                for(Pair<Integer, Integer> coord : numbersInTheBox[bx][num]) {
                    if(prevRow != Board.UNDEFINED && prevRow != coord.first) rowChanged = true;
                    if(prevCol != Board.UNDEFINED && prevCol != coord.first) colChanged = true;
                    if(rowChanged && colChanged) {
                        remove = false;
                        break;
                    }
                    prevRow = coord.first;
                    prevCol = coord.second;
                }
                if(!remove) continue;
                if(!rowChanged) removed = removeNotesFromRow(num, prevRow, bx) || removed;
                if(!colChanged) removed = removeNotesFromCol(num, prevCol, bx) || removed;
            }
        }
        return removed;
    }

    // TODO: make code smaller/better
    // TODO: optimize
    // TODO: comment
    // TODO: test it
    private boolean removeNotesBlockInteraction() {
        boolean removed = false;
//        for(int num = 1; num <= number; num++) {
//            for(int bx = 0; bx < boxes; bx++) {
//                if(numbersInTheBox[bx][num] == null || numbersInTheBox[bx][num].isEmpty()) continue;
//                ArrayList<Integer> rowsAffected = new ArrayList<>();
//                ArrayList<Integer> colsAffected = new ArrayList<>();
//                ArrayList<Integer> boxesAffected = new ArrayList<>();
//                ArrayList<Integer> boxesToVisit = new ArrayList<>();
//
//                // Horizontal
//                boxesToVisit.add(bx);
//                int currentBox;
//                while(!boxesToVisit.isEmpty()) {
//                    currentBox = boxesToVisit.get(0);
//                    boxesToVisit.remove(0);
//                    if(numbersInTheBox[currentBox][num] == null) continue;
//                    if(boxesAffected.contains(currentBox)) continue;
//                    boxesAffected.add(currentBox);
//                    for(Pair<Integer, Integer> coord : numbersInTheBox[currentBox][num]) {
//                        if(rowsAffected.contains(coord.first)) continue;
//                        rowsAffected.add(coord.first);
//                        for(Pair<Integer, Integer> cell : numbersInTheRow[coord.first][num]) {
//                            if(boxesAffected.contains(board.getBoxNumber(cell.first, cell.second))) continue;
//                            boxesToVisit.add(board.getBoxNumber(cell.first, cell.second));
//                        }
//                    }
//                }
//                if(rowsAffected.size() == boxesAffected.size()) {
//                    for(int r : rowsAffected) {
//                        for(int c = 0; c < cols; c++) {
//                            if(boxesAffected.contains(board.getBoxNumber(r, c))) continue;
//                            board.removeCellNote(num, r, c);
//                            removed = true;
//                        }
//                    }
//                }
//
//                // Vertical
//                boxesAffected.clear();
//                boxesToVisit.add(bx);
//                while(!boxesToVisit.isEmpty()) {
//                    currentBox = boxesToVisit.get(0);
//                    boxesToVisit.remove(0);
//                    if(numbersInTheBox[currentBox][num] == null) continue;
//                    if(boxesAffected.contains(currentBox)) continue;
//                    boxesAffected.add(currentBox);
//                    for(Pair<Integer, Integer> coord : numbersInTheBox[currentBox][num]) {
//                        if(colsAffected.contains(coord.second)) continue;
//                        colsAffected.add(coord.second);
//                        for(Pair<Integer, Integer> cell : numbersInTheCol[coord.second][num]) {
//                            if(boxesAffected.contains(board.getBoxNumber(cell.first, cell.second))) continue;
//                            boxesToVisit.add(board.getBoxNumber(cell.first, cell.second));
//                        }
//                    }
//                }
//                if(colsAffected.size() == boxesAffected.size()) {
//                    for(int c : colsAffected) {
//                        for(int r = 0; r < rows; r++) {
//                            if(boxesAffected.contains(board.getBoxNumber(r, c))) continue;
//                            board.removeCellNote(num, r, c);
//                            removed = true;
//                        }
//                    }
//                }
//            }
//        }
        return removed;
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
    // TODO: optimize
    private boolean removeNotesSwordfishAndXWing() {
        // Horizontal (remove vertical)
        // Vertical (remove horizontal)
//        ArrayList<Integer>[][] yes = new ArrayList[number][cols];
//        for(int i = 0; i < number; i++) {
//            for(int j = 0; j < cols; j++) {
//                yes[i][j] = new ArrayList<>();
//            }
//        }
//        for(int c = 0; c < rows; c++) {
//            for(int r = 0; r < cols; r++) {
//                for(int note : board.getCell(r, c).getNotes()) {
//                    yes[note][c].add(r);
//                }
//            }
//        }

//        for(int numb = 1; numb <= number; numb++) {
//            // Horizontal (remove vertical)
//            // Vertical (remove horizontal)
//            for(int c = 0; c < rows; c++) {
//                for(int r = 0; r < cols; r++) {
//
//                    for(int note : board.getCell(r, c).getNotes()) {
//                        yes[note][c].add(r);
//                    }
//                }
//            }
//        }


        return false;
    }

    // TODO: finish
    private boolean removeNotesForcingChain() {
        return false;
    }

    // TODO: return this back to normal (booleans)
    private boolean solveNextStep() {
//        timer7 -= System.currentTimeMillis();
//        removeNotesSmart();
//        timer7 += System.currentTimeMillis();
//        timer4 -= System.currentTimeMillis();
//        boolean solved = solveCellsWithOneNote();
//        timer4 += System.currentTimeMillis();
//        timer5 -= System.currentTimeMillis();
//        boolean solved2 = solveBoxes();
//        timer5 += System.currentTimeMillis();
//        timer6 -= System.currentTimeMillis();
//        boolean solved3 = solveRowsAndCols();
//        timer6 += System.currentTimeMillis();
//
//        return solved || solved2 || solved3;
        boolean solved = removeNotesSmart();
        solved = solveCellsWithOneNote() || solved;
        solved = solveBoxes() || solved;
        solved = solveRowsAndCols() || solved;
        return solved;
    }

    void tryToSolveAll() {
        while(solveNextStep());
    }

    /**
     * Technique: sole candidate
     * @return
     */
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

    /**
     * Technique: Unique Candidate in boxes
     * @return
     */
    private boolean solveBoxes() {
        boolean cellSolved = false;
        for(int bx = 0; bx < boxes; bx++) {
            for(int num = 1; num <= number; num++) {
                if(numbersInTheBox[bx][num] == null) continue;
                if(numbersInTheBox[bx][num].size() != 1) continue;
                solveCell(num, numbersInTheBox[bx][num].get(0).first, numbersInTheBox[bx][num].get(0).second);
                cellSolved = true;
            }
        }
        return cellSolved;
    }

    /**
     * Technique: Unique Candidate in rows and cols
     * @return
     */
    private boolean solveRowsAndCols() {
        boolean cellSolved = false;
        // Horizontal
        for(int r = 0; r < rows; r++) {
            for(int num = 1; num <= number; num++) {
                if(numbersInTheRow[r][num] == null) continue;
                if(numbersInTheRow[r][num].size() != 1) continue;
                solveCell(num, numbersInTheRow[r][num].get(0).first, numbersInTheRow[r][num].get(0).second);
                cellSolved = true;
            }
        }
        // Vertical
        for(int c = 0; c < cols; c++) {
            for(int num = 1; num <= number; num++) {
                if(numbersInTheCol[c][num] == null) continue;
                if(numbersInTheCol[c][num].size() != 1) continue;
                solveCell(num, numbersInTheCol[c][num].get(0).first, numbersInTheCol[c][num].get(0).second);
                cellSolved = true;
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
