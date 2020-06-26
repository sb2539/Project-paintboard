package com.sin.drawview.view.main;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.sin.drawview.R;
import com.sin.drawview.view.add.AddDiaryActivity;
import com.sin.drawview.view.common.BasicActivity;

public class MainActivity extends BasicActivity{


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empty);



        checkIsFirst();
    }

    // 첫실행 여부
    private void checkIsFirst(){

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent i = new Intent(MainActivity.this, AddDiaryActivity.class);
                    startActivity(i);
                    finish();
                }
            },2000);

    }

}
