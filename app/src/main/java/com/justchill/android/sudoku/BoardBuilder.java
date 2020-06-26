package com.justchill.android.sudoku;

import android.app.slice.Slice;
import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

class BoardBuilder {

    private static final int DEFAULT_NUMBER = 9;
    private static final int DUMMY_BOX_NUMBER = -1;
    private static final int ERROR = -2;

    private int number;

    private int boxRows, boxCols;

    private int[][] boxMap;

    private int[][] solutions;

    Board board;

    BoardBuilder() {
        setNumber(DEFAULT_NUMBER);
    }

    void setNumber(int number) {
        if(this.number == number) return;

        this.number = number;
        calculateBoxSizes();
//        this.boxMap = generateBoxMap();
        this.boxMap = generateRandomBoxMapV4();
        while(this.boxMap == null) this.boxMap = generateRandomBoxMapV4();
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
    int[][] generateBoxMap() {
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

    // TODO: optimize this several times
    int[][] generateRandomBoxMap() {
        Log.e("#####", "######################################## generating ###############################################");
        int[][] boxMap = new int[number][number];

        for(int r = 0; r < number; r++) {
            for(int c = 0; c < number; c++) {
                boxMap[r][c] = DUMMY_BOX_NUMBER;
            }
        }

        Random random = new Random();


        for(int currentBoxNumber = 0; currentBoxNumber < number; currentBoxNumber++) {

            Vector<Pair<Integer, Integer>> cellsToProcess = new Vector<>();

            int validNextCell = 0;
            for(int r = 0; r < number; r++) {
                for(int c = 0; c < number; c++) {
                    if(boxMap[r][c] != DUMMY_BOX_NUMBER || isCellBridge(boxMap, DUMMY_BOX_NUMBER, r, c)) continue;

                    validNextCell++;
                }
            }

            int numberOfCellToAdd = random.nextInt(validNextCell);
            int cellNumberCounter = 0;

            Log.e("######", "#################################################### before " + numberOfCellToAdd + " - " + validNextCell);

            for(int r = 0; r < number; r++) {
                for(int c = 0; c < number; c++) {
                    if(boxMap[r][c] != DUMMY_BOX_NUMBER || isCellBridge(boxMap, DUMMY_BOX_NUMBER, r, c)) continue;

                    if(cellNumberCounter++ >= numberOfCellToAdd) {
                        cellsToProcess.add(new Pair<>(r, c));
                        Log.e("######", "######################### " + r + ", " + c + " ####################");

                        r = number;
                        break;
                    }
                }
            }

            Log.e("######", "#################################################### after " + cellNumberCounter);

            for(int cellNumber = 0; cellNumber < number;) {
                if(cellsToProcess.size() <= 0) break;
                int currentCellId = random.nextInt(cellsToProcess.size());
                Pair<Integer, Integer> currentCell = cellsToProcess.remove(currentCellId);

                if(!validCoordinate(currentCell.first, currentCell.second)) continue;
                if(boxMap[currentCell.first][currentCell.second] != DUMMY_BOX_NUMBER) continue;


                if(isCellBridge(boxMap, DUMMY_BOX_NUMBER, currentCell.first, currentCell.second) && cellsToProcess.size() > 1) continue;

                boxMap[currentCell.first][currentCell.second] = currentBoxNumber;



                cellsToProcess.add(new Pair<>(currentCell.first, currentCell.second+1));
                cellsToProcess.add(new Pair<>(currentCell.first, currentCell.second-1));
                cellsToProcess.add(new Pair<>(currentCell.first+1, currentCell.second));
                cellsToProcess.add(new Pair<>(currentCell.first-1, currentCell.second));

                cellNumber++;
            }

        }

        return boxMap;
    }

    int[][] generateRandomBoxMapV2() {
        int[][] boxMap = new int[number][number];

        for(int r = 0; r < number; r++) {
            for(int c = 0; c < number; c++) {
                boxMap[r][c] = DUMMY_BOX_NUMBER;
            }
        }

        Random random = new Random();


        Vector<Pair<Integer, Integer>> bfsCellsToVisit = new Vector<>();


        for(int currentBoxNumber = 0; currentBoxNumber < number; currentBoxNumber++) {

            bfsCellsToVisit.clear();

            int nowRow = 0;
            int nowCol = 0;


            boolean[][] visited = new boolean[number][number];

            Pair<Integer, Integer>[] direction = new Pair[] {new Pair(0, 1), new Pair(1, 0), new Pair(0, -1), new Pair(-1, 0)};

            boolean changed;

            visited[nowRow][nowCol] = true;

            do {

                changed = false;

                for(int dirId = 0; dirId < 4;) {
                    if(isSolidCoordinateCheck(boxMap, nowRow, nowCol) && boxMap[nowRow][nowCol] == DUMMY_BOX_NUMBER) {
                        bfsCellsToVisit.add(new Pair<>(nowRow, nowCol));
                        changed = false;
                        break;
                    }

                    if(!validCoordinate(nowRow + direction[dirId].first, nowCol + direction[dirId].second) ||
                            visited[nowRow + direction[dirId].first][nowCol + direction[dirId].second]) {
                        dirId++;
                        continue;
                    }

                    nowRow += direction[dirId].first;
                    nowCol += direction[dirId].second;
                    visited[nowRow][nowCol] = true;
                    changed = true;
                }

            } while(changed);


            for(int cellNumber = 0; cellNumber < number;) {
                if(bfsCellsToVisit.size() <= 0) break;
                int currentCellId = random.nextInt(bfsCellsToVisit.size());
                Pair<Integer, Integer> currentCell = bfsCellsToVisit.remove(currentCellId);

                if(!validCoordinate(currentCell.first, currentCell.second)) continue;
                if(boxMap[currentCell.first][currentCell.second] != DUMMY_BOX_NUMBER) continue;

                if(!isSolidCoordinateCheck(boxMap, currentCell.first, currentCell.second) &&
                        !isSolidCoordinateCheck(boxMap, currentCell.first, currentCell.second+1) &&
                        !isSolidCoordinateCheck(boxMap, currentCell.first, currentCell.second-1) &&
                        !isSolidCoordinateCheck(boxMap, currentCell.first+1, currentCell.second) &&
                        !isSolidCoordinateCheck(boxMap, currentCell.first-1, currentCell.second)) continue;

                if(isCellBridge(boxMap, DUMMY_BOX_NUMBER, currentCell.first, currentCell.second)) continue;

//                boxMap[currentCell.first][currentCell.second] = currentBoxNumber * number + cellNumber;
                boxMap[currentCell.first][currentCell.second] = currentBoxNumber;


                bfsCellsToVisit.add(new Pair<>(currentCell.first, currentCell.second+1));
                bfsCellsToVisit.add(new Pair<>(currentCell.first, currentCell.second-1));
                bfsCellsToVisit.add(new Pair<>(currentCell.first+1, currentCell.second));
                bfsCellsToVisit.add(new Pair<>(currentCell.first-1, currentCell.second));

                cellNumber++;
            }


        }



        return boxMap;
    }

    int[][] generateRandomBoxMapV3() {
        int[][] boxMap = generateBoxMap();

        // [row][column][box1Number][box2Number]
        boolean[][][][] visited = new boolean[number][number][number][number];

        int[][] exchangePointsCount = new int[number][number];

        // TODO: check if box is bridge

        // Horizontal
        for(int r = 0; r < number; r++) {
            for(int c = 1; c < number; c++) {
                if(boxMap[r][c-1] == boxMap[r][c]) continue;
                if(visited[r][c-1][boxMap[r][c-1]][boxMap[r][c]] || visited[r][c][boxMap[r][c]][boxMap[r][c]]) continue;


            }
        }



        return boxMap;
    }

    int[][] generateRandomBoxMapV4() {
        int[][] boxMap = new int[number][number];
        Graph<Integer> graph = new Graph<>();

        for(int r = 0; r < number; r++) {
            for(int c = 0; c < number-1; c++) {
                graph.addEdge(r*number + c, r*number + c+1, false);
            }
            if(r == 8) break;
            graph.addEdge(r*number + number-1, (r+1)*number + number-1, false);
            r++;
            for(int c = number-1; c > 0; c--) {
                graph.addEdge(r*number + c, r*number + c-1, false);
            }
            graph.addEdge(r*number, (r+1)*number, false);
        }


        for(int i = 0; i < 15;) {
            i += changeHamiltonianPath(graph) ? 1 : 0;

            Integer now = 0;
            int count = 0;

            int[][] dadada = new int[number][number];
            for(int j = 0; j < number*number; j++) {
                dadada[now / number][now % number] = count++;
                now = graph.getNextVertex(now);
                if(now == null) break;
            }

            Log.e("#######", "############## DADADA:");
            for(int j = 0; j < number; j++) {
                String out = "";
                for(int k = 0; k < number; k++) {
                    out += dadada[j][k] + "\t";
                }
                Log.e("#####", out);
            }

        }




        Integer now = 0;
        int count = 0;

        // TODO: fix ArrayIndexOutOfBoundsException: length=9; index=9
        for(int i = 0; i < number; i++) {
            for(int j = 0; j < number; j++) {
                Log.e("#######", "######################### 5: " + now);
                boxMap[now / number][now % number] = i;
                now = graph.getNextVertex(now);
                if(now == null) break;
            }
            if(now == null) break;
        }

        return boxMap;
    }

    /**
     *
     * @param graph
     * @return true if successful
     */
    boolean changeHamiltonianPath(Graph<Integer> graph) {
        Random rand = new Random();

        int node = rand.nextInt(number*number);
        Integer node2 = graph.getNextVertex(node);
        if(node2 == null) return false;


        Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> temp = getParallelEdges(graph, node, node2);
        int node3 = 0, node4 = 1;

        switch (rand.nextInt(2)) {
            case 0:
                if(temp.first != null) {
                    node3 = temp.first.first;
                    node4 = temp.first.second;
                } else if(temp.second != null) {
                    node3 = temp.second.first;
                    node4 = temp.second.second;
                } else return false;
                break;
            case 1:
                if(temp.second != null) {
                    node3 = temp.second.first;
                    node4 = temp.second.second;
                } else if(temp.first != null) {
                    node3 = temp.first.first;
                    node4 = temp.first.second;
                } else return false;
                break;
        }

        int nFirst = -1;

        Integer curr = 0;
        while(curr != null) {
            if(curr == node || curr == node2 || curr == node3 || curr == node4) break;
            curr = graph.getNextVertex(curr);
        }

        if(curr == null) return false;
        if(curr != node && curr != node2 && curr != node3 && curr != node4) return false;

        Map<Integer, Boolean> inside = new HashMap<>();
        for(int i = 0; i < number*number; i++) {
            inside.put(i, false);
        }

        int yupCount = 0;

        curr = graph.getNextVertex(curr);
        while(curr != null) {
            inside.put(curr, true);
            if(curr == node || curr == node2 || curr == node3 || curr == node4) {
                if(++yupCount >= 2) break;
            }
            curr = graph.getNextVertex(curr);
        }


//        Log.e("##########", "############################## 0: " + node + ", " + node2 + ", " + node3 + ", " + node4);

//        for(int i = 0; i < number; i++) {
//            String ohDa = "";
//            for(int j = 0; j < number; j++) {
//                ohDa += inside.get(i*number+j) + " ";
//            }
//            Log.e("##### " + i + ": ", ohDa);
//        }

        curr = 0;
        while(curr != null) {
            curr = graph.getNextVertex(curr);
            if(curr == null) return false;
            if(curr == node || curr == node2 || curr == node3 || curr == node4) break;
        }

        Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> thirdEdge = null;
        int node5 = 0, node6 = 1;
        Integer prev = graph.getNextVertex(curr);
        if(prev == null) return false;
        curr = graph.getNextVertex(prev);
//        Log.e("#####", "################################# 1");
        while(curr != null) {
//            Log.e("#####", "################################# 2: " + curr);
            prev = curr;
            curr = graph.getNextVertex(curr);
            try {
                if(inside.get(prev) == null || inside.get(curr) == null || !inside.get(prev) || !inside.get(curr)) break;
                thirdEdge = getParallelEdges(graph, prev, curr);
                if((thirdEdge.first != null && !inside.get(thirdEdge.first.second) && !inside.get(thirdEdge.first.second)) ||
                        (thirdEdge.second != null && !inside.get(thirdEdge.second.second) && !inside.get(thirdEdge.second.second))) {
                    if(prev != node && prev != node2 && prev != node3 && prev != node4 && curr != node && curr != node2 && curr != node3 && curr != node4) {
                        node5 = prev;
                        node6 = curr;
                        break;
                    } else {
                        thirdEdge = null;
                    }
                } else {
                    thirdEdge = null;
                }
            } catch (Exception e) {
                return false;
            }
        }

        if(thirdEdge == null) return false;

        int node7 = 0, node8 = 1;
        switch (rand.nextInt(2)) {
            case 0:
                if(thirdEdge.first != null && !inside.get(thirdEdge.first.second) && !inside.get(thirdEdge.first.second)) {
                    node7 = thirdEdge.first.first;
                    node8 = thirdEdge.first.second;
                } else if(thirdEdge.second != null && !inside.get(thirdEdge.second.second) && !inside.get(thirdEdge.second.second)) {
                    node7 = thirdEdge.second.first;
                    node8 = thirdEdge.second.second;
                } else return false;
                break;
            case 1:
                if(thirdEdge.second != null && !inside.get(thirdEdge.second.second) && !inside.get(thirdEdge.second.second)) {
                    node7 = thirdEdge.second.first;
                    node8 = thirdEdge.second.second;
                } else if(thirdEdge.first != null && !inside.get(thirdEdge.first.second) && !inside.get(thirdEdge.first.second)) {
                    node7 = thirdEdge.first.first;
                    node8 = thirdEdge.first.second;
                } else return false;
                break;
        }

        int[] ahaDap = new int[]{node, node2, node3, node4};
        int[] ahaDap2 = new int[]{node5, node6, node7, node8};

        for(int i : ahaDap) {
            for(int j : ahaDap2) {
                if(i == j) return false;
            }
        }

        List<Pair<Integer, Integer>> added = new ArrayList<>();
        List<Pair<Integer, Integer>> removed = new ArrayList<>();

        Log.e("##########", "############################## hah 0: " + node + ", " + node2 + ", " + node3 + ", " + node4);
        if(!connectEdges(graph, node, node2, node3, node4, added, removed)) {
            Log.e("##########", "############################## hah 1: " + node5 + ", " + node6 + ", " + node7 + ", " + node8);
            if(!connectEdges(graph, node5, node6, node7, node8, added, removed)) {
                for(Pair<Integer, Integer> vertex : added) {
                    graph.removeEdge(vertex.first, vertex.second, false);
                }
                for(Pair<Integer, Integer> vertex : removed) {
                    graph.addEdge(vertex.first, vertex.second, false);
                }
                return false;
            }
        }


        return true;
    }

    Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> getParallelEdges(Graph<Integer> graph, int n1, int n2) {
        if(n1 + 1 == n2 || n2 + 1 == n1) {
            // Edge is horizontal
            Pair<Integer, Integer> up;
            if(n1 >= number && n2 >= number && graph.hasEdge(n1-number, n2-number, true)) up = new Pair<>(n1-number, n2-number);
            else up = null;
            Pair<Integer, Integer> down;
            if(n1 < (number-1)*number && n2 < (number-1)*number && graph.hasEdge(n1+number, n2+number, true)) down = new Pair<>(n1+number, n2+number);
            else down = null;
            return new Pair<>(up, down);
        } else {
            // Edge is vertical
            Pair<Integer, Integer> left;
            if(n1 % number > 0 && n2 % number > 0 && graph.hasEdge(n1-1, n2-1, true)) left = new Pair<>(n1-1, n2-1);
            else left = null;
            Pair<Integer, Integer> right;
            if(n1 % number < number-1 && n2 % number < number-1 && graph.hasEdge(n1+1, n2+1, true)) right = new Pair<>(n1+1, n2+1);
            else right = null;
            return new Pair<>(left, right);
        }
    }

    boolean connectEdges(Graph<Integer> graph, int n1, int n2, int n11, int n21, List<Pair<Integer, Integer>> added, List<Pair<Integer, Integer>> removed) {
        if(n1+1 == n21 || n1-1 == n21 || n1+number == n21 || n1-number == n21) {
            int temp = n21;
            n21 = n11;
            n11 = temp;
        }

        graph.removeEdge(n1, n2, true);
        removed.add(new Pair<>(n1, n2));
        graph.removeEdge(n11, n21, true);
        removed.add(new Pair<>(n11, n21));

        graph.addEdge(n1, n11, false);
        added.add(new Pair<>(n1, n11));
        graph.addEdge(n2, n21, false);
        added.add(new Pair<>(n2, n21));


        Map<Integer, Boolean> visited = new HashMap<>();
        for(int i = 0; i < number*number; i++) {
            visited.put(i, false);
        }

        int count = 0;

        int prev;
        Integer curr = 0;
        visited.put(curr, true);
        while(curr != null) {
            count++;
            prev = curr;
            curr = graph.getNextVertex(curr);
            if(curr != null && visited.get(curr) != null && visited.get(curr) == true) break;
            visited.put(curr, true);
            if(curr == null) {
//                Log.e("connectEdges", "##################### START:");
                for(int i = 0; i < number*number; i++) {
//                    Log.e("connectEdges", "##################### " + i + ", " + prev + ": " + visited.get(i) + ", " + graph.hasEdge(i, prev, false));
                    if(!visited.get(i) && graph.hasEdge(i, prev, false)) {
//                        Log.e("connectEdges", "##################### JEJ: " + prev + ", " + i);
                        graph.removeEdge(i, prev, false);
                        removed.add(new Pair<>(i, prev));
                        graph.addEdge(prev, i, false);
                        added.add(new Pair<>(prev, i));
                        break;
                    }
                }
                curr = graph.getNextVertex(prev);
            }
        }

        Log.e("############", "####################### count: " + count);
        return count >= 81;
    }

    boolean isCellBridge(int[][] matrix, int travelValue, int row, int col) {
        if(!validCoordinate(row, col)) return false;

        int cellBoxNumberBefore = matrix[row][col];
        int canBeVisitedBefore = canBeVisited(matrix, travelValue, row, col);

        matrix[row][col]++;

        int canBeVisitedAfter = canBeVisited(matrix, travelValue, row, col+1);
        if(canBeVisitedAfter == ERROR) canBeVisitedAfter = canBeVisited(matrix, travelValue, row, col-1);
        if(canBeVisitedAfter == ERROR) canBeVisitedAfter = canBeVisited(matrix, travelValue, row+1, col);
        if(canBeVisitedAfter == ERROR) canBeVisitedAfter = canBeVisited(matrix, travelValue, row-1, col);

        matrix[row][col] = cellBoxNumberBefore;

        // If every cell around the given cell is not valid or already in some box, given cell is not a bridge
        if(canBeVisitedAfter == ERROR) return true;


        return canBeVisitedAfter != canBeVisitedBefore-1;
    }

    int canBeVisited(int[][] matrix, int travelValue, int startRow, int startCol) {
        if(!validCoordinate(startRow, startCol)) return ERROR;
        if(matrix[startRow][startCol] != travelValue) return ERROR;

        int visitedCounter = 0;

        boolean[][] visited = new boolean[number][number];

//        for(int r = 0; r < number; r++) {
//            for(int c = 0; c < number; c++) {
//                visited[r][c] = false;
//            }
//        }

        Vector<Pair<Integer, Integer>> cellsToProcess = new Vector<>();

        cellsToProcess.add(new Pair<>(startRow, startCol));

        while(!cellsToProcess.isEmpty()) {
            Pair<Integer, Integer> currentCell = cellsToProcess.remove(0);


            if(!validCoordinate(currentCell.first, currentCell.second)) continue;

            if(matrix[currentCell.first][currentCell.second] != travelValue) continue;
            if(visited[currentCell.first][currentCell.second]) continue;
            visited[currentCell.first][currentCell.second] = true;
            visitedCounter++;


            cellsToProcess.add(new Pair<>(currentCell.first, currentCell.second+1));
            cellsToProcess.add(new Pair<>(currentCell.first, currentCell.second-1));
            cellsToProcess.add(new Pair<>(currentCell.first+1, currentCell.second));
            cellsToProcess.add(new Pair<>(currentCell.first-1, currentCell.second));
        }


        return visitedCounter;
    }

    private boolean validCoordinate(int row, int col) {
        return row >= 0 && row < number && col >= 0 && col < number;
    }

    private boolean isSolidCoordinate(int[][] grid, int row, int col) {
        if(!validCoordinate(row, col)) return true;
        return grid[row][col] != DUMMY_BOX_NUMBER;
    }

    private boolean isSolidCoordinateCheck(int[][] grid, int row, int col) {
        if(!validCoordinate(row, col)) return false;
//        if(grid[row][col] != DUMMY_BOX_NUMBER) return false;

        int solidNeighbours = 0;
        if(isSolidCoordinate(grid, row, col+1)) solidNeighbours++;
        if(isSolidCoordinate(grid, row, col-1)) solidNeighbours++;
        if(isSolidCoordinate(grid, row+1, col)) solidNeighbours++;
        if(isSolidCoordinate(grid, row-1, col)) solidNeighbours++;

        return solidNeighbours > 1;
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

//        for(int r = 0; r < board.getNumberOfRows(); r++) {
//            for(int c = 0; c < board.getNumberOfColumns(); c++) {
//                for(int k = 1; k <= number; k++) {
//                    board.getCell(r, c).addNote(k);
//                }
//            }
//        }
//
//        Random random = new Random();
//
//        dfsGenerateSolution(random, random.nextInt(board.getNumberOfRows()), random.nextInt(board.getNumberOfColumns()));
//
//        for(int r = 0; r < board.getNumberOfRows(); r++) {
//            for(int c = 0; c < board.getNumberOfColumns(); c++) {
//                if(board.getCell(r, c).getState() == Cell.STATE_START_NUMBER) continue;
//                board.getCell(r, c).setState(Cell.STATE_NOT_SOLVED);
//            }
//        }

        //generateSolution();
        return board;
    }



}
