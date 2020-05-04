package com.justchill.android.sudoku;

import java.util.HashSet;

class Cell {

    public static final int UNDEFINED_SOLUTION = 0;
    public static final int UNDEFINED_BOX_NUMBER = -1;

    public static final int STATE_START_NUMBER = 0;
    public static final int STATE_SOLVED = 1;
    public static final int STATE_NOT_SOLVED = 2;
    public static final int STATE_WRONG = 3;

    private int solution;

    private int state;

    private HashSet<Integer> notes;

    private int guessNumber = UNDEFINED_SOLUTION;

    Cell(int solution, int state) {
        this.solution = solution;
        this.state = state;

        this.notes = new HashSet<>();
    }

    Cell(Cell cloneFromCell) {
        this.solution = cloneFromCell.solution;
        this.state = cloneFromCell.state;

        this.guessNumber = cloneFromCell.guessNumber;

        this.notes = new HashSet<>();

        for(int note : cloneFromCell.getNotes()) {
            addNote(note);
        }
    }

    int getSolution() {
        return solution;
    }

    void setSolution(int solution) {
        this.solution = solution;
    }

    int getState() {
        return state;
    }

    void setState(int state) {
        this.state = state;
    }

    void addNote(int note) {
        if(this.notes.contains(note)) return;
        this.notes.add(note);
    }

    boolean removeNote(int note) {
        return this.notes.remove(note);
    }

    void clearNotes() {
        this.notes.clear();
    }

    void clickNote(int note) {
        if(removeNote(note)) return;
        addNote(note);
    }

    HashSet<Integer> getNotes() {
        return notes;
    }

    boolean containsNotes(int note) {
        return this.notes.contains(note);
    }

    int getGuessNumber() {
        return guessNumber;
    }

    void setGuessNumber(int guess) {
        this.guessNumber = guess;
    }

    boolean isSolutionVisible() {
        return getState() == Cell.STATE_START_NUMBER || getState() == Cell.STATE_SOLVED;
    }
}
