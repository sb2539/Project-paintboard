package com.sin.drawview.data;

import android.graphics.Paint;
import android.graphics.Path;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.sin.drawview.draw.DrawHelper;

import java.io.Serializable;
import java.util.ArrayList;



public class HistoryPath implements Parcelable, Serializable{
    static final long serialVersionUID = 41L;



    private ArrayList<Point> points = new ArrayList<>(); // 점들의 ArrayList<>
    private int paintStyle = 1;
    private int paintColor;
    private int paintAlpha;
    private float paintWidth;
    private float originX, originY;
    private boolean isPoint;

    private transient Path path = null;
    private transient Paint paint = null;

    public HistoryPath(@NonNull ArrayList<Point> points, @NonNull Paint paint){ // HistoryPath 생성자(Null값은 받지않음)
        this.paint = paint;
        this.points = new ArrayList<>(points);
        this.paintColor = paint.getColor();
        this.paintAlpha = paint.getAlpha();
        this.paintWidth = paint.getStrokeWidth();
        this.originX = points.get(0).x;
        this.originY = points.get(0).y;
        this.isPoint = DrawHelper.isAPoint(points);

        generatePath(); // 경로 시작
        generatePaint(); // 그리기 시작
    }

    public HistoryPath(@NonNull ArrayList<Point> points, @NonNull Paint paint, int style){ // 크레용일 때 생성자
        switch (style){
            case 2:
                this.paint = paint;
                this.points = new ArrayList<>(points);
                this.paintColor = paint.getColor();
                this.paintAlpha = paint.getAlpha();
                this.paintWidth = paint.getStrokeWidth();
                this.originX = points.get(0).x;
                this.originY = points.get(0).y;
                this.isPoint = DrawHelper.isAPoint(points);
                generatePath();
                break;
        }
    }

    public void generatePath() { // 경로 생성
        path = new Path();

        if (points != null) {
            boolean first = true; // 첫번째 터치 boolean 변수 값 true로 설정

            for (int i = 0; i < points.size(); i++) {

                Point point = points.get(i);

                if (first) {
                    path.moveTo(point.x, point.y);
                    first = false;
                } else {
                    path.lineTo(point.x, point.y);
                }
            }
        }
    }

    private void generatePaint() {
        paint = DrawHelper.createPaintAndInitialize(paintColor, paintAlpha, paintWidth,
                isPoint);
    }

    public Path getPath() {
        if (path == null) {
            generatePath();
        }
        return path;
    }

    public int getPaintStyle(){
        return paintStyle;
    }
    public void setPaintStyle(int paintStyle){
        this.paintStyle = paintStyle;
    }

    public boolean isPoint() {
        return isPoint;
    }
    public void setPoint(boolean point) {
        isPoint = point;
    }

    public float getOriginX() {
        return originX;
    }
    public void setOriginX(float originX) {
        this.originX = originX;
    }

    public float getOriginY() {
        return originY;
    }
    public void setOriginY(float originY) {
        this.originY = originY;
    }



    public float getPaintWidth() {
        return paintWidth;
    }


    public Paint getPaint() {
        if (paint == null) {
            generatePaint();
        }
        return paint;
    }

    public ArrayList<Point> getPoints() {
        return points;
    }



    private HistoryPath(Parcel in) { // Parcel 데이터를 읽어 변수에 할당
        in.readTypedList(points, Point.CREATOR);

        paintColor = in.readInt();
        paintAlpha = in.readInt();
        paintWidth = in.readFloat();

        originX = in.readFloat();
        originY = in.readFloat();

        isPoint = in.readByte() != 0;

        generatePath();
        generatePaint();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(points);

        dest.writeInt(paintColor);
        dest.writeInt(paintAlpha);
        dest.writeFloat(paintWidth);

        dest.writeFloat(originX);
        dest.writeFloat(originY);

        dest.writeByte((byte) (isPoint ? 1 : 0));
    }

    /*
        생성한 점의 객체들의 수만큼 배열을 만들어 루프를 돌려 하나씩 집어넣음(CREATOR의 역할)
     */
    public static final Creator<HistoryPath> CREATOR = new Creator<HistoryPath>() {
        @Override
        public HistoryPath createFromParcel(Parcel in) {
            return new HistoryPath(in);
        }

        @Override
        public HistoryPath[] newArray(int size) {
            return new HistoryPath[size];
        }
    };

    @Override
    public String toString() {
        return "Point: " + isPoint + "\n" +
                "Points: " + points + "\n" +
                "Color: " + paintColor + "\n" +
                "Alpha: " + paintAlpha + "\n" +
                "Width: " + paintWidth;
    }
}
