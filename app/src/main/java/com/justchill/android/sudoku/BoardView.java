package com.justchill.android.sudoku;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.HashSet;

public class BoardView extends View {

    public final float THICK_LINE_WIDTH = 9;
    public final float SMALL_LINE_WIDTH = 2;
    private final float NUMBER_TEXT_SIZE_SCALE = 0.8f;

    private float numberTextSize = 0;

    private Paint thickLinePaint, smallLinePaint, selectedBackgroundPaint, selectedNumberBackgroundPaint;
    private Paint startNumberPaint, solvedNumberPaint, wrongNumberPaint, noteNumberPaint;

    private Board board;

    // TODO: other data type?
    private HashSet<Pair<Integer, Integer>> drawCellVisited;

    public BoardView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        thickLinePaint = new Paint();
        thickLinePaint.setAntiAlias(true);
        thickLinePaint.setColor(Color.BLACK);
        thickLinePaint.setStyle(Paint.Style.STROKE);
        thickLinePaint.setStrokeWidth(THICK_LINE_WIDTH);

        smallLinePaint = new Paint();
        smallLinePaint.setAntiAlias(true);
        smallLinePaint.setColor(Color.BLACK);
        smallLinePaint.setStyle(Paint.Style.STROKE);
        smallLinePaint.setStrokeWidth(SMALL_LINE_WIDTH);

        startNumberPaint = new Paint();
        startNumberPaint.setAntiAlias(true);
        startNumberPaint.setColor(Color.BLACK);
        startNumberPaint.setTextAlign(Paint.Align.CENTER);

        solvedNumberPaint = new Paint();
        solvedNumberPaint.setAntiAlias(true);
        solvedNumberPaint.setColor(Color.BLUE);
        solvedNumberPaint.setTextAlign(Paint.Align.CENTER);

        wrongNumberPaint = new Paint();
        wrongNumberPaint.setAntiAlias(true);
        wrongNumberPaint.setColor(Color.RED);
        wrongNumberPaint.setTextAlign(Paint.Align.CENTER);

        noteNumberPaint = new Paint();
        noteNumberPaint.setAntiAlias(true);
        noteNumberPaint.setColor(Color.BLACK);
        noteNumberPaint.setTextAlign(Paint.Align.CENTER);

        selectedBackgroundPaint = new Paint();
        selectedBackgroundPaint.setColor(Color.LTGRAY);
        selectedBackgroundPaint.setStyle(Paint.Style.FILL);

        selectedNumberBackgroundPaint = new Paint();
        selectedNumberBackgroundPaint.setColor(0xFF999999);
        selectedNumberBackgroundPaint.setStyle(Paint.Style.FILL);


        drawCellVisited = new HashSet<>();

    }

    public void setBoard(Board board) {
        this.board = board;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);


        // TODO: edit dimensions

        //noinspection SuspiciousNameCombination
        setMeasuredDimension(widthMeasureSpec, widthMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        numberTextSize = (getHeight() - THICK_LINE_WIDTH*2) / board.getNumberOfRows();
        startNumberPaint.setTextSize(numberTextSize * NUMBER_TEXT_SIZE_SCALE);
        solvedNumberPaint.setTextSize(numberTextSize * NUMBER_TEXT_SIZE_SCALE);
        wrongNumberPaint.setTextSize(numberTextSize * NUMBER_TEXT_SIZE_SCALE);

        drawCellVisited.clear();

        drawSelectedBox(canvas, board.getSelectedRow(), board.getSelectedColumn(), board.getSelectedBoxNumber(), selectedBackgroundPaint);
        drawSelectedRowAndCol(canvas, board.getSelectedRow(), board.getSelectedColumn(), selectedBackgroundPaint);
        drawSelectedNumbers(canvas, board.getSelectedRow(), board.getSelectedColumn(), selectedNumberBackgroundPaint);

        // TODO: make a function
        if(board.getSelectedCell() != null)
            canvas.drawRect(getCellX(board.getSelectedColumn()), getCellY(board.getSelectedRow()),
                getCellX(board.getSelectedColumn()) + getCellSize(), getCellY(board.getSelectedRow()) + getCellSize(),
                selectedNumberBackgroundPaint);

        drawLines(canvas);
        drawNumbers(canvas);

    }

    @SuppressWarnings("SuspiciousNameCombination")
    void drawLines(Canvas canvas) {

        /*
         * Outside lines
         */

        // Left
        canvas.drawLine(THICK_LINE_WIDTH/2, 0,
                THICK_LINE_WIDTH/2, getHeight(), thickLinePaint);
        // Up
        canvas.drawLine(0, THICK_LINE_WIDTH/2,
                getWidth(), THICK_LINE_WIDTH/2, thickLinePaint);

        // Down
        canvas.drawLine(getWidth(), getHeight() - THICK_LINE_WIDTH/2,
                THICK_LINE_WIDTH, getHeight() - THICK_LINE_WIDTH/2, thickLinePaint);
        // Right
        canvas.drawLine(getWidth() - THICK_LINE_WIDTH/2, getHeight(),
                getWidth() - THICK_LINE_WIDTH/2, THICK_LINE_WIDTH, thickLinePaint);

        /*
         * Small grid lines
         */

        // Horizontal
        for(int i = 1; i < board.getNumberOfRows(); i++) {
            float yCoord = ((getHeight() - THICK_LINE_WIDTH*2) / board.getNumberOfRows()) * i + THICK_LINE_WIDTH - SMALL_LINE_WIDTH/2;
            canvas.drawLine(THICK_LINE_WIDTH, yCoord, getWidth() - THICK_LINE_WIDTH, yCoord, smallLinePaint);
        }

        // Vertical
        for(int i = 1; i < board.getNumberOfColumns(); i++) {
            float xCoord = ((getWidth() - THICK_LINE_WIDTH*2) / board.getNumberOfColumns()) * i + THICK_LINE_WIDTH - SMALL_LINE_WIDTH/2;
            canvas.drawLine(xCoord, THICK_LINE_WIDTH, xCoord, getHeight() - THICK_LINE_WIDTH, smallLinePaint);
        }

        /*
         * Box separators
         */


        // Horizontal
        for(int r = 1; r < board.getNumberOfRows(); r++) {
            for(int c = 0; c < board.getNumberOfColumns(); c++) {
                if(board.getBoxNumber(r-1, c) != board.getBoxNumber(r, c)) {
                    canvas.drawLine(getCellX(c) - THICK_LINE_WIDTH/2, getCellY(r),
                            getCellX(c) + getCellSize() + THICK_LINE_WIDTH/2, getCellY(r), thickLinePaint);
                }
            }
        }

        // Vertical
        for(int c = 1; c < board.getNumberOfColumns(); c++) {
            for(int r = 0; r < board.getNumberOfRows(); r++) {
                if(board.getBoxNumber(r, c-1) != board.getBoxNumber(r, c)) {
                    canvas.drawLine(getCellX(c), getCellY(r) - THICK_LINE_WIDTH/2,
                            getCellX(c), getCellY(r) + getCellSize() + THICK_LINE_WIDTH/2, thickLinePaint);
                }
            }
        }

    }

    void drawNumbers(Canvas canvas) {
        for(int r = 0; r < board.getNumberOfRows(); r++) {
            for(int c = 0; c < board.getNumberOfColumns(); c++) {
                Cell currentCell = board.getCell(r, c);

                float yCoord = getCellY(r) + getCellSize()/2 - (startNumberPaint.ascent() + startNumberPaint.descent())/2;
                float xCoord = getCellX(c) + getCellSize()/2;

                // TODO: delete
//                canvas.drawText(String.valueOf(board.getBoxNumber(r, c)), xCoord, yCoord, startNumberPaint);
//                canvas.drawText(String.valueOf(currentCell.getSolution()), xCoord, yCoord, startNumberPaint);

                switch (currentCell.getState()) {
                    case Cell.STATE_START_NUMBER:
                        canvas.drawText(String.valueOf(currentCell.getSolution()), xCoord, yCoord, startNumberPaint);
                        break;
                    case Cell.STATE_SOLVED:
                        canvas.drawText(String.valueOf(currentCell.getSolution()), xCoord, yCoord, solvedNumberPaint);
                        break;
                    case Cell.STATE_NOT_SOLVED:
                        drawNotes(canvas, r, c);
                        break;
                    case Cell.STATE_WRONG:
                        canvas.drawText(String.valueOf(currentCell.getGuessNumber()), xCoord, yCoord, wrongNumberPaint);
                        break;
                }


            }
        }
    }

    void drawNotes(Canvas canvas, int row, int col) {
        int size = (int)Math.ceil(Math.sqrt(board.getNumber()));
        noteNumberPaint.setTextSize(getCellSize()/size * NUMBER_TEXT_SIZE_SCALE);
        for(int r = 0; r < size; r++) {
            for(int c = 0; c < size; c++) {
                int currentNumber = r*size + c + 1;
                if(!board.getCell(row, col).containsNotes(currentNumber)) continue;

                float yCoord = getCellY(row) + getCellSize()/6 - (noteNumberPaint.ascent() + noteNumberPaint.descent())/2;
                float xCoord = getCellX(col) + getCellSize()/6;

                yCoord += getCellSize()/size * r;
                xCoord += getCellSize()/size * c;

                canvas.drawText(String.valueOf(currentNumber), xCoord, yCoord, noteNumberPaint);
            }
        }
    }

    void drawSelectedBox(Canvas canvas, int row, int col, int boxNumber, Paint paint) {

        if(boxNumber == Cell.UNDEFINED_BOX_NUMBER) return;
        if(row < 0 || row >= board.getNumberOfColumns() || col < 0 || col >= board.getNumberOfRows()) return;
        if(board.getBoxNumber(row, col) != boxNumber) return;

        Pair<Integer, Integer> current = new Pair<>(row, col);

        if(drawCellVisited.contains(current)) return;
        drawCellVisited.add(current);

        canvas.drawRect(getCellX(col), getCellY(row), getCellX(col) + getCellSize(), getCellY(row) + getCellSize(), paint);

        drawSelectedBox(canvas, row+1, col, boxNumber, paint);
        drawSelectedBox(canvas, row-1, col, boxNumber, paint);
        drawSelectedBox(canvas, row, col+1, boxNumber, paint);
        drawSelectedBox(canvas, row, col-1, boxNumber, paint);

    }

    void drawSelectedRowAndCol(Canvas canvas, int row, int col, Paint paint) {
        if(row == Board.UNDEFINED || col == Board.UNDEFINED) return;

        // Horizontal
        for(int i = 0; i < board.getNumberOfColumns(); i++) {
            canvas.drawRect(getCellX(i), getCellY(row), getCellX(i) + getCellSize(), getCellY(row) + getCellSize(), paint);
        }

        // Vertical
        for(int i = 0; i < board.getNumberOfRows(); i++) {
            canvas.drawRect(getCellX(col), getCellY(i), getCellX(col) + getCellSize(), getCellY(i) + getCellSize(), paint);
        }

    }

    void drawSelectedNumbers(Canvas canvas, int row, int col, Paint paint) {
        if(row == Board.UNDEFINED || col == Board.UNDEFINED) return;

        if(!board.getCell(row, col).isSolutionVisible()) {

            return;
        }

        for(int r = 0; r < board.getNumberOfRows(); r++) {
            for(int c = 0; c < board.getNumberOfColumns(); c++) {
                if(board.getCell(row, col).getSolution() != board.getCell(r, c).getSolution()) continue;
                if(!board.getCell(r, c).isSolutionVisible()) continue;

                canvas.drawRect(getCellX(c), getCellY(r), getCellX(c) + getCellSize(), getCellY(r) + getCellSize(), paint);
            }
        }
    }

    float getCellSize() {
        return ((getWidth() - THICK_LINE_WIDTH*2) / board.getNumberOfColumns());
    }

    float getCellX(int col) {
        return getCellSize() * col + THICK_LINE_WIDTH;
    }

    float getCellY(int row) {
        return getCellSize() * row + THICK_LINE_WIDTH;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            int row = (int)((event.getY() - THICK_LINE_WIDTH) / getCellSize());
            row = Math.max(row, 0);
            row = Math.min(row, board.getNumberOfRows()-1);
            int col = (int)((event.getX() - THICK_LINE_WIDTH) / getCellSize());
            col = Math.max(col, 0);
            col = Math.min(col, board.getNumberOfColumns()-1);

            board.setSelected(row, col);

            invalidate();
            return true;
        }
        return false;
    }
}
