package com.sin.drawview.draw;

import android.content.res.Resources;
import android.graphics.ComposePathEffect;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.support.annotation.NonNull;

import com.sin.drawview.data.Point;

import java.util.List;



public class DrawHelper {


    public static boolean isAPoint(@NonNull List<Point> points) { // 점들이 성공적으로 찍혔는지 안찍혔는지 반환
        if (points.size() == 0)
            return false;

        if (points.size() == 1)
            return true;

        for (int i = 1; i < points.size(); i++) {
            if (points.get(i - 1).x != points.get(i).x || points.get(i - 1).y != points.get(i).y)
                return false;
        }

        return true;
    }

    /*
        페인트 초기 입력값 정의하는 메소드
     */
    public static Paint createPaintAndInitialize(int paintColor, int paintAlpha,
                                          float paintWidth, boolean fill) {
        Paint paint = createPaint();

        initializePaint(paint, paintColor, paintAlpha, paintWidth, fill);

        return paint;
    }

    static Paint createPaint() {
        return new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    static void initializePaint(Paint paint, int paintColor, int paintAlpha, float paintWidth, boolean fill) { // 페인트 초기값 설정 메소드
        if (fill) {
            setupFillPaint(paint);
        } else {
            setupStrokePaint(paint);
        }

        paint.setStrokeWidth(paintWidth);
        paint.setColor(paintColor);
        paint.setAlpha(paintAlpha);
    }

    static void setupFillPaint(Paint paint) {
        paint.setStyle(Paint.Style.FILL);
    }

    static void setupStrokePaint(Paint paint) {
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setPathEffect(new ComposePathEffect(
                new CornerPathEffect(100f),
                new CornerPathEffect(100f)));
        paint.setStyle(Paint.Style.STROKE);
    }



    public static void copyFromValues(Paint to, int color, int alpha, float strokeWidth,
                               boolean copyWidth) { // 그린 path들을 저장하기 위한 메소드 -> 이를 SavedState에서 호출

        to.setColor(color);
        to.setAlpha(alpha);

        if (copyWidth) {
            to.setStrokeWidth(strokeWidth);
        }
    }


    public static float convertDpToPixels(float dp) { // dp값을 픽셀값으로 변환
        return (dp * Resources.getSystem().getDisplayMetrics().density);
    }



}
