package com.matrix_maeny.onlinetictactoe2.gameboard;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.matrix_maeny.onlinetictactoe2.R;

public class GameBoard extends View {

    private final int borderColor;
    private final int boardBackground;
    private final int playerOneDiskColor;
    private final int playerTwoDiskColor;
    private final int playerOneMatchColor;
    private final int playerTwoMatchColor;


    private Paint borderColorPaint = new Paint();
    private Paint linesColorPaint = new Paint();
    private Paint boardBackgroundPaint = new Paint();
    private Paint playerOneDiskColorPaint = new Paint();
    private Paint playerTwoDiskColorPaint = new Paint();
    private Paint playerOneMatchColorPaint = new Paint();
    private Paint playerTwoMatchColorPaint = new Paint();

    private int cellSize;
    private GameBoardMoves boardMoves = new GameBoardMoves();
    private GameBoardListener listener;

    public GameBoard(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.GameBoard, 0, 0);

        try {
            borderColor = a.getInteger(R.styleable.GameBoard_borderColor, 0);
            boardBackground = a.getInteger(R.styleable.GameBoard_boardBackground, 0);
            playerOneDiskColor = a.getInteger(R.styleable.GameBoard_playerOneDiskColor, 0);
            playerTwoDiskColor = a.getInteger(R.styleable.GameBoard_playerTwoDiskColor, 0);
            playerOneMatchColor = a.getInteger(R.styleable.GameBoard_playerOneMatchColor, 0);
            playerTwoMatchColor = a.getInteger(R.styleable.GameBoard_playerTwoMatchColor, 0);
            listener = (GameBoardListener) context;
        } finally {
            a.recycle();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int height = getMeasuredHeight();
        int width = getMeasuredWidth();

        int minDim = Math.min(height, width);

        width = minDim-9;
        height = width + minDim / 3;

        cellSize = width / 9;

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        borderColorPaint.setStyle(Paint.Style.STROKE);
        borderColorPaint.setColor(borderColor);
        borderColorPaint.setStrokeWidth(8);
        borderColorPaint.setAntiAlias(true);

        linesColorPaint.setStyle(Paint.Style.STROKE);
        linesColorPaint.setColor(borderColor);
        linesColorPaint.setStrokeWidth(1);
        linesColorPaint.setAntiAlias(true);

        boardBackgroundPaint.setStyle(Paint.Style.FILL);
        boardBackgroundPaint.setColor(boardBackground);
        boardBackgroundPaint.setStrokeWidth(5);
        boardBackgroundPaint.setAntiAlias(true);

        playerOneDiskColorPaint.setStyle(Paint.Style.FILL);
        playerOneDiskColorPaint.setColor(playerOneDiskColor);
        playerOneDiskColorPaint.setStrokeWidth(5);
        playerOneDiskColorPaint.setAntiAlias(true);

        playerTwoDiskColorPaint.setStyle(Paint.Style.FILL);
        playerTwoDiskColorPaint.setColor(playerTwoDiskColor);
        playerTwoDiskColorPaint.setStrokeWidth(5);
        playerTwoDiskColorPaint.setAntiAlias(true);

        playerOneMatchColorPaint.setStyle(Paint.Style.FILL);
        playerOneMatchColorPaint.setColor(playerOneMatchColor);
        playerOneMatchColorPaint.setStrokeWidth(5);
        playerOneMatchColorPaint.setAntiAlias(true);

        playerTwoMatchColorPaint.setStyle(Paint.Style.FILL);
        playerTwoMatchColorPaint.setColor(playerTwoMatchColor);
        playerTwoMatchColorPaint.setStrokeWidth(5);
        playerTwoMatchColorPaint.setAntiAlias(true);

        canvas.drawRect(0, 0, getWidth(), getHeight(), boardBackgroundPaint);
        canvas.drawRect(0, 0, getWidth(), getHeight(), borderColorPaint);
        fillBoxes(canvas);
        drawLines(canvas);
        drawDisks(canvas);
    }

    private void fillBoxes(Canvas canvas) {

        for (int r = 0; r < 12; r++) {
            for (int c = 0; c < 9; c++) {
                if (boardMoves.getGameMatchedBoxes()[r][c] == 3) {
                    // fill the matched box
//                    canvas.drawRect((c-1)*cellSize,(r-1)*cellSize,(c)*cellSize,(r)*cellSize,playerOneMatchColorPaint);
                    canvas.drawRect((c) * cellSize, (r) * cellSize, (c + 1) * cellSize, (r + 1) * cellSize, playerOneMatchColorPaint);
                } else if (boardMoves.getGameMatchedBoxes()[r][c] == 4) {
//                    canvas.drawRect((c-1)*cellSize,(r-1)*cellSize,(c)*cellSize,(r)*cellSize,playerTwoMatchColorPaint);
                    canvas.drawRect((c) * cellSize, (r) * cellSize, (c + 1) * cellSize, (r + 1) * cellSize, playerTwoMatchColorPaint);
                }
            }
        }
    }

    private void drawDisks(Canvas canvas) {
//            int row = boardMoves.getSelectedRow();
//            int column = boardMoves.getSelectedColumn();

        float height, width;
//            height = (row - 1) * cellSize + (cellSize / 2);
//            width = (column - 1) * cellSize + (cellSize / 2);

        float radius;//= ((float) cellSize / 2 - 5);

        for (int r = 0; r < 12; r++) {
            for (int c = 0; c < 9; c++) {

                height = (r) * cellSize + (cellSize / 2);
                width = (c) * cellSize + (cellSize / 2);

                radius = ((float) cellSize / 2 - 5);


                if (boardMoves.getGameMoves()[r][c] == 1) // if player 1
                    canvas.drawCircle(width, height, radius, playerOneDiskColorPaint);
                else if (boardMoves.getGameMoves()[r][c] == 2)
                    canvas.drawCircle(width, height, radius, playerTwoDiskColorPaint);
            }
        }

    }

    @Override
    public boolean onFilterTouchEventForSecurity(MotionEvent event) {
        boolean isValid = false;
        float x = event.getX();
        float y = event.getY();

        int action = event.getAction();

        if (action == MotionEvent.ACTION_DOWN) {

            boardMoves.setSelectedRow((int) Math.ceil(y / cellSize));
            boardMoves.setSelectedColumn((int) Math.ceil(x / cellSize));
            isValid = true;
            listener.moveHappened();


        }
        return isValid;
    }

    public interface GameBoardListener {
        void moveHappened();
    }

    private void drawLines(Canvas canvas) {
        // we have 9 columns
        // we have 12 rows
        borderColorPaint.setStrokeWidth(2);

        for (int c = 0; c < 9; c++) { // drawing columns
//            canvas.drawLine(c * cellSize, 0, c * cellSize, getHeight(), borderColorPaint);
            canvas.drawLine(cellSize * c, 0, cellSize * c, getHeight(), borderColorPaint);

        }

        for (int r = 0; r < 12; r++) { // drawing rows
            canvas.drawLine(0, r * cellSize, getWidth(), r * cellSize, borderColorPaint);
//            canvas.drawLine(0, cellSize * r, getWidth(), cellSize * r, borderColorPaint);

        }
    }

    public GameBoardMoves getBoardMoves() {
        return boardMoves;
    }
}
