package com.sin.drawview.draw;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.annotation.FloatRange;
import android.support.annotation.IntRange;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.sin.drawview.R;
import com.sin.drawview.data.HistoryPath;
import com.sin.drawview.data.Point;
import com.sin.drawview.data.ResizeBehaviour;

import java.util.ArrayList;



public class DrawView extends View implements View.OnTouchListener {

    /*
        그림툴에 대한 초기값 설정
     */
    private static final float DEFAULT_STROKE_WIDTH = 4;
    private static final int DEFAULT_COLOR = Color.parseColor("#FF000000");
    private static final int DEFAULT_ALPHA = 255;

    private Paint currentPaint;
    private Paint penPaint, crayonPaint, eraserPaint;
    private Path currentPath;

    private Canvas canvas;
    private Bitmap oldBitmap; // 예전 그림

    private ResizeBehaviour resizeBehaviour;

    private ArrayList<Point> points = new ArrayList<>();
    private ArrayList<HistoryPath> paths = new ArrayList<>(); // 경로가 들어가는 ArrayList<>
    private ArrayList<HistoryPath> cancelPaths = new ArrayList<>(); // 되돌길 경로를 기억하기위한 ArrayList<>

    private int paintStyle = 1; // 1: 펜 , 2: 크레용 , 3: 지우개
    private int paintColor = DEFAULT_COLOR;
    private int paintAlpha = DEFAULT_ALPHA;

    private Bitmap bitmap; // 크래파스 이미지
    private BitmapDrawable bitmapDrawable;

    /*
        undo, redo를 위한 마지막 크기 저장
     */
    private int lastDimensionW = -1;
    private int lastDimensionH = -1;

    private boolean finishPath = false; // 마지막으로 생성된 경로인지 확인하기 위한 boolean값

    private PathDrawnListener mPathDrawnListener;
    private PathRedoUndoCountChangeListener mPathRedoUndoCountChangeListener;

    public DrawView(Context context) {
        this(context, null);
    }
    public DrawView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public DrawView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setOnTouchListener(this);
        initPaints();
    }

    // 종료 전 인스턴스 저장
    @Override
    protected Parcelable onSaveInstanceState() {

        // Get the superclass parcelable state
        Parcelable superState = super.onSaveInstanceState();

        if (points.size() > 0) {// point 크기가 있을 시 point를 path로 만든다
            createHistoryPathFromPoints();
        }

        return new DrawSavedState(superState, paths, cancelPaths,
                getPaintWidth(), getPaintColor(), getPaintAlpha(),
                getResizeBehaviour(), lastDimensionW, lastDimensionH);
    }

    // redo, undo를 위해 저장된 상태를 복구하는 메소드
    @Override
    protected void onRestoreInstanceState(Parcelable state) {


        if (!(state instanceof DrawSavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        DrawSavedState savedState = (DrawSavedState) state;

        super.onRestoreInstanceState(savedState.getSuperState());

        /*
            savedState로부터 현재 paths, cancelPaths, currenPaint값을 넣어줌
         */
        paths = savedState.getPaths();
        cancelPaths = savedState.getCanceledPaths();
        currentPaint = savedState.getCurrentPaint();

        setPaintWidthPx(savedState.getCurrentPaintWidth());
        setPaintColor(savedState.getPaintColor());
        setPaintAlpha(savedState.getPaintAlpha());

        setResizeBehaviour(savedState.getResizeBehaviour());


        lastDimensionW = savedState.getLastDimensionW();
        lastDimensionH = savedState.getLastDimensionH();

        notifyRedoUndoCountChanged(); // redo, undo에 횟수값을 부여하여 돌아가야할 상태를 구분
    }


    public void setPaintStyle(int style){
        paintStyle = style;
        if(style == 3){  // style이 지우개일 때 색상 WHITE값으로 칠하도록 함
            setPaintColor(Color.BLACK);
        }
    }

    public void setPaintColor(@ColorInt int color) {

        invalidate();

        paintColor = color;

        currentPaint.setColor(paintColor);
        currentPaint.setAlpha(paintAlpha); // Restore the previous alpha
    }
    public int getPaintColor(){ return paintColor; }
    public int getPaintColorWithAlpha(){ return currentPaint.getColor(); }


    public void setPaintWidthPx(@FloatRange(from = 0) float widthPx){
        if(widthPx > 0){
            invalidate();
            currentPaint.setStrokeWidth(widthPx);
            crayonPaint.setStrokeWidth(widthPx);
        }
    }
    public void setPaintWidthDp(float dp){ // dpi에서 pixel로 변환된 값으로 펜의 굵기 설정
        setPaintWidthPx(DrawHelper.convertDpToPixels(dp));
    }
    public float getPaintWidth(){
        return currentPaint.getStrokeWidth();
    }


    public void setPaintAlpha(@IntRange(from = 0, to = 255) int alpha){
        invalidate();

        paintAlpha = alpha;
        currentPaint.setAlpha(paintAlpha);
    }
    public int getPaintAlpha(){
        return paintAlpha;
    }


    public void setResizeBehaviour(ResizeBehaviour newBehavior){
        resizeBehaviour = newBehavior;
    }
    public ResizeBehaviour getResizeBehaviour(){
        return resizeBehaviour;
    }



    public void undoLast(){
        if(paths.size() > 0){ // 점과 점이 연결된 선이라면 경로 끝 부울 값 true로 설정
            finishPath = true;
            invalidate();

            cancelPaths.add(paths.get(paths.size()-1)); // cancelPath에 추가
            paths.remove(paths.size()-1); // 현 경로에서 삭제
            invalidate();

            notifyRedoUndoCountChanged();
        }
    }
    public void redoLast(){ // undoLast() 메소드와 반대로 구현
        if(cancelPaths.size() > 0){
            paths.add(cancelPaths.get(cancelPaths.size() - 1));
            cancelPaths.remove(cancelPaths.size()-1);
            invalidate();

            notifyRedoUndoCountChanged();
        }
    }


    //
    public int getUndoCount(){
        return paths.size();
    }

    //
    public int getRedoCount(){
        return paths.size();
    }

    //
    public int getPathCount(boolean drawingPath){
        int size = paths.size();

        if(drawingPath && paths.size()>0){
            size++;
        }
        return size;
    }



    public void setOnPathDrawnListener(PathDrawnListener listener){
        mPathDrawnListener = listener;
    }

    public void setPathRedoUndoCountChangeListener(PathRedoUndoCountChangeListener listener) {
        mPathRedoUndoCountChangeListener = listener;
    }



    public void clearAll(){ // 전체 지우기 : ArrayList를 모두 지우므로 undo, redo 불가능
        oldBitmap = null;
        clearDraw();
        clearHistory();
    }
    private void clearDraw() {
        points = new ArrayList<>();
        paths = new ArrayList<>();

        notifyRedoUndoCountChanged();

        invalidate();
    }
    private void clearHistory() {
        cancelPaths = new ArrayList<>();

        notifyRedoUndoCountChanged();

        invalidate();
    }


    private void notifyPathStart() { // 경로의 시작임을 확인
        if (mPathDrawnListener != null) {
            mPathDrawnListener.onPathStart();
        }
    }
    private void notifyPathDrawn() { // 경로를 그리는중임을 확인
        if (mPathDrawnListener != null) {
            mPathDrawnListener.onNewPathDrawn();
        }
    }
    private void notifyRedoUndoCountChanged() { // redo, undo가 몇번씩 실행됐는지를 구분
        if (mPathRedoUndoCountChangeListener != null) {
            mPathRedoUndoCountChangeListener.onRedoCountChanged(getRedoCount());
            mPathRedoUndoCountChangeListener.onUndoCountChanged(getUndoCount());
        }
    }

    // 예전 그림 저장
    public void setOldBitmap(Bitmap bitmap){
        oldBitmap = bitmap;

        oldBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

        canvas = new Canvas(oldBitmap);

        draw(canvas);

        invalidate();

    }

    private void initPaints(){ // 그림툴 초기화
        currentPaint = DrawHelper.createPaint();

        currentPaint.setColor(DEFAULT_COLOR);
        currentPaint.setAlpha(DEFAULT_ALPHA);
        currentPaint.setStrokeWidth(DEFAULT_STROKE_WIDTH);
        DrawHelper.setupStrokePaint(currentPaint);


        crayonPaint = new Paint();
        ColorFilter filter = new PorterDuffColorFilter(DEFAULT_COLOR, PorterDuff.Mode.SRC_IN);
        crayonPaint.setColorFilter(filter);
        crayonPaint.setStrokeWidth(DEFAULT_STROKE_WIDTH);
        bitmapDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.crayon_blush);
        bitmap = bitmapDrawable.getBitmap();
        bitmap = Bitmap.createScaledBitmap(bitmap, (int)getPaintWidth(), (int)getPaintWidth(), false);
    }




    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(paths.size() == 0 && points.size() == 0) return; //경로 또는 점이 없다면

        final boolean finishedPath = finishPath;
        finishPath = false;


        // 이전 그림이 있다면 이전 그림을 불러온다.
        if(oldBitmap != null){
            canvas.drawBitmap(oldBitmap, 0, 0, currentPaint);
        }

        for(HistoryPath currentPath : paths){
            if(currentPath.getPaintStyle() == 2){
                bitmap = Bitmap.createScaledBitmap(bitmap, (int)currentPath.getPaintWidth(), (int)currentPath.getPaintWidth(), false); // 크레용 draw 방식
            }
            if(currentPath.isPoint()){
                if(currentPath.getPaintStyle() == 1) {
                    canvas.drawCircle(currentPath.getOriginX(), currentPath.getOriginY(),
                            currentPath.getPaint().getStrokeWidth() / 2, currentPath.getPaint()); // 펜은 drawCircle 이용
                }else if (currentPath.getPaintStyle() == 2) {
                    for (int i = 0; i < currentPath.getPoints().size(); i++) {
                        canvas.drawBitmap(bitmap, currentPath.getPoints().get(i).x, currentPath.getPoints().get(i).y, currentPath.getPaint()); // 크레용일 경우 drawBitmp
                    }
                }else if (currentPath.getPaintStyle() == 3){
                    canvas.drawCircle(currentPath.getOriginX(), currentPath.getOriginY(),
                            currentPath.getPaint().getStrokeWidth() / 2, currentPath.getPaint()); // 지우개일 경우 white값으로 drawCircle
                }
            } else {// 그림그리는 과정이 종료된 경우
                if(currentPath.getPaintStyle() == 1) {
                    canvas.drawPath(currentPath.getPath(), currentPath.getPaint());
                }else if(currentPath.getPaintStyle() == 2) {
                    for (int i = 0; i < currentPath.getPoints().size(); i++) {
                        canvas.drawBitmap(bitmap, currentPath.getPoints().get(i).x, currentPath.getPoints().get(i).y, currentPath.getPaint());
                    }
                }else if (currentPath.getPaintStyle() == 3){
                    canvas.drawPath(currentPath.getPath(), currentPath.getPaint());
                }
            }
        }

        if(currentPath == null){     //현재 경로가 없다면
            currentPath = new Path();
        }else{
            currentPath.rewind();
        }

        ColorFilter filter = new PorterDuffColorFilter(paintColor, PorterDuff.Mode.SRC_IN);      // 색 팔레트 객체 src_in mode 형태로 생성
        crayonPaint.setColorFilter(filter);                                                   // 선택한 색
        crayonPaint.setStrokeWidth(getPaintWidth());
        bitmap = Bitmap.createScaledBitmap(bitmap, (int)getPaintWidth(), (int)getPaintWidth(), false);// filter를 false로 하여 가장자리 흐림효과를 줌



        if(points.size() == 1 || DrawHelper.isAPoint(points)){ // isAPoint 메소드를 통해 점이나 선의 존재여부 확인 시
            if(paintStyle == 1){
                canvas.drawCircle(points.get(0).x, points.get(0).y,
                        currentPaint.getStrokeWidth()/2,
                        createAndCopyColorAndAlphaForFillPaint(currentPaint, false));
            }else if(paintStyle == 2) {
                canvas.drawBitmap(bitmap, points.get(0).x, points.get(0).y, crayonPaint);
            }else if(paintStyle == 3){
                canvas.drawCircle(points.get(0).x, points.get(0).y,
                        currentPaint.getStrokeWidth()/2,
                        createAndCopyColorAndAlphaForFillPaint(currentPaint, false));
            }
        }else if(points.size() != 0){ // 기존에 그린 선이나 점이 있는 경우
            boolean first = true;

            for (Point point : points){
                if(first){
                    currentPath.moveTo(point.x, point.y);
                    first = false;
                }else{
                    if(paintStyle == 1){
                        currentPath.lineTo(point.x, point.y);
                    }else if(paintStyle == 2){
                        canvas.drawBitmap(bitmap, point.x, point.y, crayonPaint);
                    }else if(paintStyle == 3){
                        currentPath.lineTo(point.x, point.y);
                    }
                }

            }

            canvas.drawPath(currentPath, currentPaint);
        }

        if(finishedPath && points.size() > 0){ // finishedPath 값이 있을 시 HistoryPath에 저장
            createHistoryPathFromPoints();
        }
    }

    private Paint createAndCopyColorAndAlphaForFillPaint(Paint from, boolean copyWidth) {
        Paint paint = DrawHelper.createPaint();
        DrawHelper.setupFillPaint(paint);
        paint.setColor(from.getColor());
        paint.setAlpha(from.getAlpha());
        if (copyWidth) {
            paint.setStrokeWidth(from.getStrokeWidth());
        }
        return paint;
    }
    private void createHistoryPathFromPoints() {
        HistoryPath historyPath = new HistoryPath(points, new Paint(currentPaint));;
        switch (paintStyle){
            case 1:
                historyPath = new HistoryPath(points, new Paint(currentPaint));
                break;
            case 2:
                historyPath = new HistoryPath(points, new Paint(crayonPaint), paintStyle);
                break;
            case 3:

                break;

        }
        historyPath.setPaintStyle(paintStyle); // 펜의 종류에 따른 경로 추가
        paths.add(historyPath);

        points = new ArrayList<>();

        notifyPathDrawn();
        notifyRedoUndoCountChanged();
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {     // 그림 그릴 때 터치 이벤트
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {     // 터치 다운 시 경로 시작
            notifyPathStart();
        }
        if (getParent() != null) {                                       // 부모 경로 존재한다면
            getParent().requestDisallowInterceptTouchEvent(false);    //부모에게 터치 이벤트 뺏기지 않도록 함
        }

        cancelPaths = new ArrayList<>();                          // 그림 그리기 다시 시작하면 기록 모두 지움

        if ((motionEvent.getAction() != MotionEvent.ACTION_UP) &&
                (motionEvent.getAction() != MotionEvent.ACTION_CANCEL)) { // 손을 때거나, 취소한게 아니라면
            Point point;
            for (int i = 0; i < motionEvent.getHistorySize(); i++) {      // 기록된 경로 크기 만큼 이벤트 반복
                point = new Point();
                point.x = motionEvent.getHistoricalX(i);                  // 기록된 x 좌표
                point.y = motionEvent.getHistoricalY(i);                  // 기록된 y 좌표
                points.add(point);                                        // x,y 좌표 추가
            }
            point = new Point();
            point.x = motionEvent.getX();                                // 모션 이벤트를 통해 얻은 x 좌표
            point.y = motionEvent.getY();                                 // 모션 이벤트를 통해 얻는 y 좌표
            points.add(point);

            finishPath = false;
        } else
            finishPath = true;

        invalidate();
        return true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {            // 크기 변경 메소드
        super.onSizeChanged(w, h, oldw, oldh);

        float xMultiplyFactor = 1;
        float yMultiplyFactor = 1;


        if (lastDimensionW == -1) {                                      // 처음그린 경로라면
            lastDimensionW = w;                                         // 처음 그린 경로의 너비를 설정한다.
        }

        if (lastDimensionH == -1) {
            lastDimensionH = h;
        }

        if (w >= 0 && w != oldw && w != lastDimensionW) {
            xMultiplyFactor = (float) w / lastDimensionW;
            lastDimensionW = w;
        }

        if (h >= 0 && h != oldh && h != lastDimensionH) {
            yMultiplyFactor = (float) h / lastDimensionH;
            lastDimensionH = h;
        }

        multiplyPathsAndPoints(xMultiplyFactor, yMultiplyFactor);
    }


    private void multiplyPathsAndPoints(float xMultiplyFactor, float yMultiplyFactor) {


        if ((xMultiplyFactor == 1 && yMultiplyFactor == 1)
                || (xMultiplyFactor <= 0 || yMultiplyFactor <= 0) ||
                (paths.size() == 0 && cancelPaths.size() == 0 && points.size() == 0)) {       // 경로가 없다면
            return;
        }

        if (resizeBehaviour == ResizeBehaviour.CLEAR) {                   // 수직과 수평이 모두 클리어인 상태라면
            paths = new ArrayList<>();                                    // 경로, 취소경로, 점을 클리어한다.
            cancelPaths = new ArrayList<>();
            points = new ArrayList<>();
            return;
        } else if (resizeBehaviour == ResizeBehaviour.CROP) {
            xMultiplyFactor = yMultiplyFactor = 1;
        }


        for (HistoryPath historyPath : paths) {                          // 그려진 경로 까지 반복

            if (historyPath.isPoint()) {
                historyPath.setOriginX(historyPath.getOriginX() * xMultiplyFactor);    // x 좌표의 경로 기록
                historyPath.setOriginY(historyPath.getOriginY() * yMultiplyFactor);    // y 좌표의 경로 기록
            } else {
                for (Point point : historyPath.getPoints()) {
                    point.x *= xMultiplyFactor;
                    point.y *= yMultiplyFactor;
                }
            }

            historyPath.generatePath();                                         // 경로 생성
        }


        for (HistoryPath historyPath : cancelPaths) {                              // 취소된 경로

            if (historyPath.isPoint()) {
                historyPath.setOriginX(historyPath.getOriginX() * xMultiplyFactor);
                historyPath.setOriginY(historyPath.getOriginY() * yMultiplyFactor);
            } else {
                for (Point point : historyPath.getPoints()) {
                    point.x *= xMultiplyFactor;
                    point.y *= yMultiplyFactor;
                }
            }

            historyPath.generatePath();
        }


        for (Point point : points) {                                         // x,y 좌표 그리기
            point.x *= xMultiplyFactor;
            point.y *= yMultiplyFactor;
        }
    }
}
