package com.justchill.android.sudoku;

import android.util.Log;

import java.util.Random;

class BoardBuilder {

    private static final int DEFAULT_NUMBER = 9;

    private int number;

    private int boxRows, boxCols;

    private int[][] boxMap;

    private int[][] solutions;

    Board board;

    BoardBuilder() {
        setNumber(DEFAULT_NUMBER);
    }

    void setNumber(int number) {
        this.number = number;
        calculateBoxSizes();
        this.boxMap = calculateBoxMap();
    }

    void calculateBoxSizes() {
        for(int i = (int)Math.sqrt(number); i > 1; i--) {
            if(number % i == 0) {
                boxRows = i;
                boxCols = number/i;
                return;
            }
        }

        boxRows = 1;
        boxCols = number;

    }

    // TODO: make random (not organized) version of this
    int[][] calculateBoxMap() {
        int[][] boxMap = new int[number][number];

        int currentBoxNumber = 0;

        for(int startRow = 0; startRow < number; startRow += boxRows) {
            for(int startCol = 0; startCol < number; startCol += boxCols) {

                for(int r = 0; r < boxRows; r++) {
                    for(int c = 0; c < boxCols; c++) {
                        boxMap[startRow+r][startCol+c] = currentBoxNumber;
                    }
                }

                currentBoxNumber++;
            }
        }

        return boxMap;
    }

    void generateSolution() {

        for(int i = 0; i < number; i++) {
            for(int j = 0; j < number; j++) {
                for(int k = 1; k <= number; k++) {
                    board.getCell(i, j).addNote(k);
                }
            }
        }


        Random random = new Random();

        int remaining = number*number;

        while(remaining > 0) {
//        for(int z = 0; z < 30; z++) {

            int solveGoal = random.nextInt(remaining);
            int unsolvedCounter = -1;
            boolean exitFromLoops = false;


            // TODO: replace this with function
            for(int i = 0; i < number; i++) {
                for(int j = 0; j < number; j++) {
                    if(board.getCell(i, j).getState() != Cell.STATE_NOT_SOLVED) continue;

                    if(++unsolvedCounter == solveGoal && board.getCell(i, j).getNotes().size() > 0) {
                        solveGoal = random.nextInt(board.getCell(i, j).getNotes().size());
                        unsolvedCounter = -1;
                        for (int note : board.getCell(i, j).getNotes()) {
                            if(++unsolvedCounter == solveGoal) {
                                Solver.solveCell(board, note, i, j);

                                exitFromLoops = true;
                                break;
                            }
                        }

                    }

                    if(exitFromLoops) break;
                }
                if(exitFromLoops) break;
            }

            while(Solver.solveNextStep(board, number, number, number, number));

            remaining = 0;

//            Log.e("#######", "");
//            Log.e("#######", "");
//            Log.e("#######", "");

            for(int r = 0; r < number; r++) {
                String text = "";
                for(int c = 0; c < number; c++) {

                    if(board.getCell(r, c).getState() != Cell.STATE_SOLVED) {
                        remaining++;
                        text += 0 + ", ";
                    } else {
                        text += board.getCell(r, c).getSolution() + ", ";
                    }

                }
//                Log.e("######", text);
            }


        }

    }


    // TODO: this code is too ugly
    // TODO: optimize for big numbers
    boolean dfsGenerateSolution(Random random, int row, int col) {
        if(row < 0 || row >= board.getNumberOfRows() || col < 0 || col >= board.getNumberOfColumns()) return false;


        Board currentBoard = new Board(board);


        while(true) {

            board = new Board(currentBoard);


            if(board.getCell(row, col).getNotes().isEmpty()) return false;

            int solutionNumber = random.nextInt(board.getCell(row, col).getNotes().size());
            int notesCounter = 0;

            for(int note : board.getCell(row, col).getNotes()) {
                if(notesCounter++ < solutionNumber) continue;


                Solver.solveCell(board, note, row, col);
                currentBoard.getCell(row, col).removeNote(note);

                while(Solver.solveNextStep(board, number, number, number, number));

                int notSolvedCounter = 0;

                for(int r = 0; r < board.getNumberOfRows(); r++) {
                    for(int c = 0; c < board.getNumberOfColumns(); c++) {
                        if(board.getCell(r, c).getSolution() == Cell.UNDEFINED_SOLUTION) notSolvedCounter++;
                        if(board.getCell(r, c).getSolution() == Cell.UNDEFINED_SOLUTION && board.getCell(r, c).getNotes().isEmpty()) return false;
                    }
                }

                if(notSolvedCounter <= 0) {
                    board.getCell(row, col).setState(Cell.STATE_START_NUMBER);
                    return true;
                }

                int nextCellNumber = random.nextInt(notSolvedCounter);
                int cellCounter = 0;

                for(int r = 0; r < board.getNumberOfRows(); r++) {
                    for(int c = 0; c < board.getNumberOfColumns(); c++) {
                        if(board.getCell(r, c).getSolution() != Cell.UNDEFINED_SOLUTION) continue;
                        if(cellCounter++ < nextCellNumber) continue;

                        if(dfsGenerateSolution(random, r, c)) {
                            board.getCell(row, col).setState(Cell.STATE_START_NUMBER);
                            return true;
                        }

                        break;
                    }
                    if(cellCounter++ >= nextCellNumber) break;
                }



                break;
            }



        }

    }



    Board build() {
//        boxMap = new int[][] {
//                {0, 0, 0, 1, 1, 1, 2, 2, 2},
//                {0, 0, 0, 1, 1, 1, 2, 2, 2},
//                {0, 0, 0, 1, 1, 1, 2, 2, 2},
//                {3, 3, 3, 4, 4, 4, 5, 5, 5},
//                {3, 3, 3, 4, 4, 4, 5, 5, 5},
//                {3, 3, 3, 4, 4, 4, 5, 5, 5},
//                {6, 6, 6, 7, 7, 7, 8, 8, 8},
//                {6, 6, 6, 7, 7, 7, 8, 8, 8},
//                {6, 6, 6, 7, 7, 7, 8, 8, 8},
//        };

//        boxMap = new int[][] {
//                {0, 0, 0, 0, 1, 1, 2, 2, 2},
//                {0, 0, 1, 1, 1, 1, 2, 2, 2},
//                {0, 0, 0, 4, 1, 1, 2, 5, 2},
//                {3, 3, 3, 4, 4, 1, 5, 5, 2},
//                {3, 3, 3, 4, 4, 4, 5, 5, 5},
//                {6, 3, 3, 7, 4, 4, 5, 5, 5},
//                {6, 3, 6, 7, 7, 4, 8, 8, 8},
//                {6, 6, 6, 7, 7, 7, 7, 8, 8},
//                {6, 6, 6, 7, 7, 8, 8, 8, 8},
//        };

//        boxMap = new int[][] {
//                {0, 0, 0, 1, 1, 1},
//                {0, 0, 0, 1, 1, 1},
//                {2, 2, 2, 3, 3, 3},
//                {2, 2, 2, 3, 3, 3},
//                {4, 4, 4, 5, 5, 5},
//                {4, 4, 4, 5, 5, 5}
//        };


//        solutions = new int[][]{
//                {8, 2, 7, 1, 5, 4, 3, 9, 6},
//                {9, 6, 5, 3, 2, 7, 1, 4, 8},
//                {3, 4, 1, 6, 8, 9, 7, 5, 2},
//                {5, 9, 3, 4, 6, 8, 2, 7, 1},
//                {4, 7, 2, 5, 1, 3, 6, 8, 9},
//                {6, 1, 8, 9, 7, 2, 4, 3, 5},
//                {7, 8, 6, 2, 3, 5, 9, 1, 4},
//                {1, 5, 4, 7, 9, 6, 8, 2, 3},
//                {2, 3, 9, 8, 4, 1, 5, 6, 7}
//        };

        board = new Board(number, number, number, boxRows, boxCols, boxMap);

        for(int r = 0; r < board.getNumberOfRows(); r++) {
            for(int c = 0; c < board.getNumberOfColumns(); c++) {
                for(int k = 1; k <= number; k++) {
                    board.getCell(r, c).addNote(k);
                }
            }
        }

        Random random = new Random();

        dfsGenerateSolution(random, random.nextInt(board.getNumberOfRows()), random.nextInt(board.getNumberOfColumns()));

        for(int r = 0; r < board.getNumberOfRows(); r++) {
            for(int c = 0; c < board.getNumberOfColumns(); c++) {
                if(board.getCell(r, c).getState() == Cell.STATE_START_NUMBER) continue;
                board.getCell(r, c).setState(Cell.STATE_NOT_SOLVED);
            }
        }

        //generateSolution();
        return board;
    }



}
