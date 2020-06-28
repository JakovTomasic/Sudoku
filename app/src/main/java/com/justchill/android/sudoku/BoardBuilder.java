package com.justchill.android.sudoku;

import android.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
        this.boxMap = generateRandomBoxMap();
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

    int[][] generateRandomBoxMap() {
        int[][] boxMap = new int[number][number];
        Graph<Integer> graph = new Graph<>();

        for(int r = 0; r < number; r++) {
            for(int c = 0; c < number-1; c++) {
                graph.addEdge(r*number + c, r*number + c+1, false);
            }
            if(r == number-1) break;
            graph.addEdge(r*number + number-1, (r+1)*number + number-1, false);
            r++;
            for(int c = number-1; c > 0; c--) {
                graph.addEdge(r*number + c, r*number + c-1, false);
            }
            graph.addEdge(r*number, (r+1)*number, false);
        }

//        int[] dirX = new int[]{1, 0, -1, 0};
//        int[] dirY = new int[]{0, 1, 0, -1};
//        int tempCount = 0;
//
//        boolean[][] visited = new boolean[number][number];
//
//        int prevX = 0, prevY = 0;
//        int x = 1, y = 0;
//        visited[prevX][prevY] = true;
//        visited[x][y] = true;
//        graph.addEdge(prevY*number + prevX, y*number + x, false);
//
//        int error = 0;
//
//        while(error < 5) {
//            if(x + dirX[tempCount] >= number || x + dirX[tempCount] < 0 || y + dirY[tempCount] >= number ||
//                    y + dirY[tempCount] < 0 || visited[x + dirX[tempCount]][y + dirY[tempCount]]) {
//                tempCount++;
//                tempCount %= dirX.length;
//                error++;
//                continue;
//            }
//            error = 0;
//            prevX = x;
//            prevY = y;
//            x += dirX[tempCount];
//            y += dirY[tempCount];
//            visited[x][y] = true;
//            graph.addEdge(prevY*number + prevX, y*number + x, false);
//        }


        // TODO: validate at the end and repeat if not good

        for(int i = 0; i < number*3;) {
            i += changeHamiltonianPath(graph) ? 1 : 0;

//            Integer now2 = 0;
//            int count2 = 0;
//
//            int[][] dadada = new int[number][number];
//            for(int j = 0; j < number*number; j++) {
//                if(now2 >= number*number) break;
//                dadada[now2 / number][now2 % number] = count2++;
//                now2 = graph.getNextVertex(now2);
//                if(now2 == null) break;
//            }
//
//            Log.e("#######", "############## DADADA:");
//            for(int j = 0; j < number; j++) {
//                String out = "";
//                for(int k = 0; k < number; k++) {
//                    out += dadada[j][k] + "\t";
//                }
//                Log.e("#####", out);
//            }

        }



        Integer now = 0;

        for(int i = 0; i < number; i++) {
            for(int j = 0; j < number; j++) {
//                Log.e("#######", "######################### 5: " + now);
                if(now >= number*number) {
                    now = null;
                    break;
                }
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

        int[] firstChangeNodes = new int[]{node, node2, node3, node4};
        int[] secondChangeNodes = new int[]{node5, node6, node7, node8};

        for(int i : firstChangeNodes) {
            for(int j : secondChangeNodes) {
                if(i == j) return false;
            }
        }

        // TODO: use this
        List<Pair<Integer, Integer>> added = new ArrayList<>();
        List<Pair<Integer, Integer>> removed = new ArrayList<>();

//        Log.e("##########", "############################## hah 0: " + node + ", " + node2 + ", " + node3 + ", " + node4);
        if(!connectEdges(graph, node, node2, node3, node4, added, removed)) {
//            Log.e("##########", "############################## hah 1: " + node5 + ", " + node6 + ", " + node7 + ", " + node8);
            if(!connectEdges(graph, node5, node6, node7, node8, added, removed)) {
//                for(Pair<Integer, Integer> vertex : added) {
//                    graph.removeEdge(vertex.first, vertex.second, false);
//                }
//                for(Pair<Integer, Integer> vertex : removed) {
//                    graph.addEdge(vertex.first, vertex.second, false);
//                }
//                return false;

//                Log.e("########", graph.toString());
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

        // TODO: boolean[][] for visited instead of map
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
            if(curr != null && visited.get(curr) != null && visited.get(curr)) break;
            if(curr != null) visited.put(curr, true);
            else {
//                Log.e("connectEdges", "##################### START:");
                // TODO: optimize (4 - one on each side instead of number*number times
                for(int i = 0; i < number*number; i++) {
//                    Log.e("connectEdges", "##################### " + i + ", " + prev + ": " + visited.get(i) + ", " + graph.hasEdge(i, prev, false));
                    if(!visited.get(i) && graph.hasEdge(i, prev, false)) {
//                        Log.e("connectEdges", "############################## obrtanje: " + prev + ", " + i);
                        graph.removeEdge(i, prev, false);
                        removed.add(new Pair<>(i, prev));
                        graph.addEdge(prev, i, false);
                        added.add(new Pair<>(prev, i));
                        break;
                    }
                }
                curr = graph.getNextVertex(prev);
                if(curr != null) visited.put(curr, true);
            }
        }

//        Log.e("############", "####################### count: " + count);
        return count == number*number;
    }


    // TODO: this code is too ugly
    // TODO: optimize for big numbers
    private boolean generateSolution(Random random, int row, int col) {
        if(row < 0 || row >= board.getNumberOfRows() || col < 0 || col >= board.getNumberOfColumns()) return false;


        // This is just a backup
        Board currentBoard = new Board(board);


        while(true) {

            board = new Board(currentBoard);
            Solver solver = board.getSolver();


            if(board.getCell(row, col).getNotes().isEmpty()) return false;

            int solutionNumber = random.nextInt(board.getCell(row, col).getNotes().size());
            int notesCounter = 0;

            int note = 0;

            for(int noteIterator : board.getCell(row, col).getNotes()) {
                note = noteIterator;
                if (notesCounter++ >= solutionNumber) break;
            }

            solver.solveCell(note, row, col);
            currentBoard.getCell(row, col).removeNote(note);

            solver.tryToSolveAll();

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

                    if(generateSolution(random, r, c)) {
                        board.getCell(row, col).setState(Cell.STATE_START_NUMBER);
                        return true;
                    }

                    break;
                }
                if(cellCounter++ >= nextCellNumber) break;
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

        generateSolution(random, random.nextInt(board.getNumberOfRows()), random.nextInt(board.getNumberOfColumns()));

        for(int r = 0; r < board.getNumberOfRows(); r++) {
            for(int c = 0; c < board.getNumberOfColumns(); c++) {
                if(board.getCell(r, c).getState() == Cell.STATE_START_NUMBER) continue;
                board.getCell(r, c).setState(Cell.STATE_NOT_SOLVED);
            }
        }

        return board;
    }



}
