package com.sin.drawview.util;

import android.app.Activity;
import android.util.DisplayMetrics;



public class SizeFactory {

    public int getWindowWidth(Activity activity){
        DisplayMetrics dm = activity.getApplicationContext().getResources().getDisplayMetrics();

        return dm.widthPixels; // 가로 화소 수
    }
}
