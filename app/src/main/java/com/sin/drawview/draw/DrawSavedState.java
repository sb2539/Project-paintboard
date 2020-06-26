package com.sin.drawview.draw;

import android.graphics.Paint;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.annotation.IntRange;
import android.view.View;

import com.sin.drawview.data.HistoryPath;
import com.sin.drawview.data.ResizeBehaviour;

import java.util.ArrayList;


/**

    *Created by 허신범
 */
class DrawSavedState extends View.BaseSavedState {

    private ArrayList<HistoryPath> mPaths = new ArrayList<>();
    private ArrayList<HistoryPath> mCanceledPaths = new ArrayList<>();

    private int mPaintColor;
    private int mPaintAlpha;
    private float mPaintWidth;

    private ResizeBehaviour mResizeBehaviour;

    private int mLastDimensionW;
    private int mLastDimensionH;

    DrawSavedState(Parcelable superState, ArrayList<HistoryPath> paths,
                   ArrayList<HistoryPath> canceledPaths, float paintWidth,
                   int paintColor, int paintAlpha, ResizeBehaviour resizeBehaviour,
                   int lastDimensionW, int lastDimensionH) {
        super(superState);

        mPaths = paths;
        mCanceledPaths = canceledPaths;
        mPaintWidth = paintWidth;

        mPaintColor = paintColor;
        mPaintAlpha = paintAlpha;

        mResizeBehaviour = resizeBehaviour;

        mLastDimensionW = lastDimensionW;
        mLastDimensionH = lastDimensionH;
    }

    ArrayList<HistoryPath> getPaths() {
        return mPaths;
    }

    ArrayList<HistoryPath> getCanceledPaths() {
        return mCanceledPaths;
    }

    @ColorInt
    int getPaintColor() {
        return mPaintColor;
    }

    @IntRange(from = 0, to = 255)
    int getPaintAlpha() {
        return mPaintAlpha;
    }

    float getCurrentPaintWidth() {
        return mPaintWidth;
    }

    Paint getCurrentPaint() {

        Paint paint = DrawHelper.createPaint();
        DrawHelper.setupStrokePaint(paint);
        DrawHelper.copyFromValues(paint, mPaintColor, mPaintAlpha, mPaintWidth, true);
        return paint;
    }

    ResizeBehaviour getResizeBehaviour() {
        return mResizeBehaviour;
    }

    int getLastDimensionW() {
        return mLastDimensionW;
    }

    int getLastDimensionH() {
        return mLastDimensionH;
    }


    private DrawSavedState(Parcel in) {
        super(in);

        in.readTypedList(mPaths, HistoryPath.CREATOR);
        in.readTypedList(mCanceledPaths, HistoryPath.CREATOR);

        mPaintColor = in.readInt();
        mPaintAlpha = in.readInt();
        mPaintWidth = in.readFloat();

        mResizeBehaviour = (ResizeBehaviour) in.readSerializable();

        mLastDimensionW = in.readInt();
        mLastDimensionH = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel out, int flags) { // 그림 데이터 전송에 있어서 직렬화를 위해 Parcel 객체에 데이터 값들을 넣어줌
        super.writeToParcel(out, flags);

        out.writeTypedList(mPaths);
        out.writeTypedList(mCanceledPaths);

        out.writeInt(mPaintColor);
        out.writeInt(mPaintAlpha);
        out.writeFloat(mPaintWidth);

        out.writeSerializable(mResizeBehaviour);

        out.writeInt(mLastDimensionW);
        out.writeInt(mLastDimensionH);
    }


    public static final Parcelable.Creator<DrawSavedState> CREATOR =
            new Parcelable.Creator<DrawSavedState>() {
                public DrawSavedState createFromParcel(Parcel in) {
                    return new DrawSavedState(in);
                }

                public DrawSavedState[] newArray(int size) {
                    return new DrawSavedState[size];
                }
            };
}
