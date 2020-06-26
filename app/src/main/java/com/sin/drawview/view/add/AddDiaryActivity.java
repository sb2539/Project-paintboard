package com.sin.drawview.view.add;


import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import com.sin.drawview.util.SizeFactory;
import com.sin.drawview.R;
import com.sin.drawview.data.ColorData;
import com.sin.drawview.draw.DrawView;
import com.sin.drawview.draw.PathDrawnListener;
import com.sin.drawview.draw.PathRedoUndoCountChangeListener;
import com.sin.drawview.view.common.BasicActivity;


public class AddDiaryActivity extends BasicActivity implements PathRedoUndoCountChangeListener, PathDrawnListener{


    private final int PEN_STYLE = 1;
    private final int CRAYON_STYLE = 2;
    private final int ERASER_STYLE = 3;
    private final int COLOR_STYLE = 4;



    private final String gray = "#eeeeee";
    private final String darkgray = "#aaaaaa";



    // 현재 선택된 색 , 기존의 색 ( 지우개 사용시 사용 )
    private String currentColor = ColorData.color10; // black;

    // 뒤로가기, 저장하기
    private ImageView backButton;
    private TextView saveButton;

    // 그림일기 레이아웃
    private FrameLayout diaryLayout;






    // 그림판
    private DrawView drawView;

    // 글씨
    private RelativeLayout writeLayout, layout;

    private String diarycontent;

    // 하단 버튼들
    private LinearLayout bottomLayout;
    private SeekBar seekbar, eraserSeekbar;
    private ImageView redoButton, undoButton;
    private ImageView pen1, pen2;
    private ImageView eraserButton;
    private ImageView[] colorButtons;
    private ImageView colorButton;
    private Button eraserAllButton;
    private LinearLayout colorLayout, eraserLayout;


    private int curruntPen = 1;
    private Animation upAnim1, upAnim2, upAnim3, downAnim1, downAnim2, downAnim3;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_diary);

        init();
    }

    private void init(){

        // 그림일기 세로 사이즈 조정
        diaryLayout = (FrameLayout) findViewById(R.id.fragment_diary_layout);
        SizeFactory sizeFactory = new SizeFactory(); // 화면의 가로 화소 수
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(sizeFactory.getWindowWidth(this), sizeFactory.getWindowWidth(this));
        diaryLayout.setLayoutParams(params);

        backButton = (ImageView) findViewById(R.id.titlebar_backbutton);
        backButton.setVisibility(View.VISIBLE);


        saveButton = (TextView) findViewById(R.id.titlebar_savebutton);
        saveButton.setVisibility(View.VISIBLE);


        drawView = (DrawView) findViewById(R.id.drawview); //  그림 그리는 영역
        drawView.setOnPathDrawnListener(this);
        drawView.setPathRedoUndoCountChangeListener(this);


        initBottomLayout();
    }


    // 팬 스타일, 지우개, 배경색, redo & undo , 굵기, 색
    private void initBottomLayout(){
        // 애니메이션 초기화
        upAnim1 = AnimationUtils.loadAnimation(this, R.anim.anim_up);
        upAnim2 = AnimationUtils.loadAnimation(this, R.anim.anim_up);
        upAnim3 = AnimationUtils.loadAnimation(this, R.anim.anim_up);
        downAnim1 = AnimationUtils.loadAnimation(this, R.anim.anim_down);
        downAnim2 = AnimationUtils.loadAnimation(this, R.anim.anim_down);
        downAnim3 = AnimationUtils.loadAnimation(this, R.anim.anim_down);

        colorLayout = (LinearLayout) findViewById(R.id.colorLayout);
        eraserLayout = (LinearLayout) findViewById(R.id.eraserLayout);
        bottomLayout = (LinearLayout) findViewById(R.id.bottom_button_layout);
        seekbar = (SeekBar) findViewById(R.id.text_size_seekbar);
        seekbar.getThumb().setColorFilter(Color.parseColor("#FF000000"), PorterDuff.Mode.SRC_IN);
        eraserSeekbar = (SeekBar) findViewById(R.id.eraser_seekbar);
        eraserSeekbar.getThumb().setColorFilter(Color.parseColor("#FF000000"), PorterDuff.Mode.SRC_IN);
        eraserAllButton = (Button) findViewById(R.id.eraser_all_button);

        initColorButtons();

        pen1 = (ImageView) findViewById(R.id.bottom_pen1_button);
        pen2 = (ImageView) findViewById(R.id.bottom_pen2_button);
        eraserButton = (ImageView) findViewById(R.id.bottom_pen3_button);
        redoButton = (ImageView) findViewById(R.id.bottom_redo_button);
        undoButton = (ImageView) findViewById(R.id.bottom_undo_button);
        colorButton = (ImageView) findViewById(R.id.bottom_color_button);
        colorButton.setColorFilter(Color.parseColor("#FF000000"));

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                drawView.setPaintWidthDp(i);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        eraserSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                drawView.setPaintWidthDp(i);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        pen1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(curruntPen != PEN_STYLE){
                    setLayoutToPen(PEN_STYLE);
                }
            }
        });
        pen2.startAnimation(downAnim2);
        pen2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(curruntPen != CRAYON_STYLE){
                    setLayoutToPen(CRAYON_STYLE);
                }
            }
        });

        eraserButton.startAnimation(downAnim3);
        eraserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(curruntPen != ERASER_STYLE){
                    setLayoutToPen(ERASER_STYLE);
                }
            }
        });
        eraserAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawView.clearAll();
            }
        });

        redoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawView.undoLast();
            }
        });
        undoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawView.redoLast();
            }
        });

        colorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(curruntPen != COLOR_STYLE){
                    setLayoutToPen(COLOR_STYLE);
                }
            }
        });
    }

    // 컬러 버튼 초기화
    private void initColorButtons(){
        colorButtons = new ImageView[12];

        colorButtons[0] = (ImageView) findViewById(R.id.color00);
        colorButtons[1] = (ImageView) findViewById(R.id.color01);
        colorButtons[2] = (ImageView) findViewById(R.id.color02);
        colorButtons[3] = (ImageView) findViewById(R.id.color03);
        colorButtons[4] = (ImageView) findViewById(R.id.color04);
        colorButtons[5] = (ImageView) findViewById(R.id.color05);
        colorButtons[6] = (ImageView) findViewById(R.id.color06);
        colorButtons[7] = (ImageView) findViewById(R.id.color07);
        colorButtons[8] = (ImageView) findViewById(R.id.color08);
        colorButtons[9] = (ImageView) findViewById(R.id.color09);
        colorButtons[10] = (ImageView) findViewById(R.id.color10);
        colorButtons[11] = (ImageView) findViewById(R.id.color11);

        ColorData colorData = new ColorData();
        colorButtons[0].setOnClickListener(new ColorButtonClickListener(colorData.color00));
        colorButtons[1].setOnClickListener(new ColorButtonClickListener(colorData.color01));
        colorButtons[2].setOnClickListener(new ColorButtonClickListener(colorData.color02));
        colorButtons[3].setOnClickListener(new ColorButtonClickListener(colorData.color03));
        colorButtons[4].setOnClickListener(new ColorButtonClickListener(colorData.color04));
        colorButtons[5].setOnClickListener(new ColorButtonClickListener(colorData.color05));
        colorButtons[6].setOnClickListener(new ColorButtonClickListener(colorData.color06));
        colorButtons[7].setOnClickListener(new ColorButtonClickListener(colorData.color07));
        colorButtons[8].setOnClickListener(new ColorButtonClickListener(colorData.color08));
        colorButtons[9].setOnClickListener(new ColorButtonClickListener(colorData.color09));
        colorButtons[10].setOnClickListener(new ColorButtonClickListener(colorData.color10));
        colorButtons[11].setOnClickListener(new ColorButtonClickListener(colorData.color11));

    }


    private class ColorButtonClickListener implements View.OnClickListener{

        private String color;
        public ColorButtonClickListener(String color){
            this.color = color;
        }

        @Override
        public void onClick(View view) {
//            Log.d(TAG, " color : " + color);
            currentColor = color;
            drawView.setPaintColor(Color.parseColor(color));
            colorButton.setColorFilter(Color.parseColor(color));
        }
    }

    /**
     * 현재 선택된 팬에 따라 레이아웃 변경
     * 1: 연필
     * 2: 크래파스
     * 3: 지우개
     * 4: 컬러
     */
    private void setLayoutToPen(int pen){
        //Log.d(TAG, "current pen : " + curruntPen + " , pen : " + pen);
        setDownAnim(curruntPen);

        bottomLayout.setBackgroundColor(Color.parseColor(gray));
        colorButton.setBackgroundColor(Color.parseColor("#00000000"));

        switch (pen){
            case 1:
                drawView.setPaintStyle(pen);
                drawView.setPaintColor(Color.parseColor(currentColor));
                pen1.startAnimation(upAnim1);

                seekbar.setVisibility(View.VISIBLE);
                drawView.setPaintWidthDp(seekbar.getProgress());
                colorLayout.setVisibility(View.INVISIBLE);
                eraserLayout.setVisibility(View.INVISIBLE);
                break;
            case 2:
                drawView.setPaintStyle(pen);
                drawView.setPaintColor(Color.parseColor(currentColor));
                pen2.startAnimation(upAnim2);

                seekbar.setVisibility(View.VISIBLE);
                drawView.setPaintWidthDp(seekbar.getProgress());
                colorLayout.setVisibility(View.INVISIBLE);
                eraserLayout.setVisibility(View.INVISIBLE);
                break;
            case 3:
                drawView.setPaintStyle(pen);
                eraserButton.startAnimation(upAnim3);

                seekbar.setVisibility(View.INVISIBLE);
                drawView.setPaintWidthDp(eraserSeekbar.getProgress());
                colorLayout.setVisibility(View.INVISIBLE);
                eraserLayout.setVisibility(View.VISIBLE);
                break;
            case 4:
                // color
                bottomLayout.setBackgroundColor(Color.parseColor(darkgray));
                colorButton.setBackgroundColor(Color.parseColor(gray));
                seekbar.setVisibility(View.INVISIBLE);
                drawView.setPaintWidthDp(seekbar.getProgress());
                colorLayout.setVisibility(View.VISIBLE);
                eraserLayout.setVisibility(View.INVISIBLE);
                break;
        }

        this.curruntPen = pen;
    }
    private void setDownAnim(int pen){
        switch (pen){
            case 1:
                pen1.startAnimation(downAnim1);
                break;
            case 2:
                pen2.startAnimation(downAnim2);
                break;
            case 3:
                eraserButton.startAnimation(downAnim3);
                break;
        }
    }


    private static final int REQUEST_PERMISSIONS = 100;







    @Override
    public void onPathStart() {

    }

    @Override
    public void onNewPathDrawn() {

    }

    @Override
    public void onUndoCountChanged(int undoCount) {

    }

    @Override
    public void onRedoCountChanged(int redoCount) {

    }
}
