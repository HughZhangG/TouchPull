package com.gu.cheng.touchpull;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;

import com.gu.cheng.touchpull.widget.TouchPullView;

public class MainActivity extends AppCompatActivity {

    private static final int TOUCH_MOVE_MAX_Y = 600;
    private float mTouchMoveStartY;

    TouchPullView mPullView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPullView = (TouchPullView) findViewById(R.id.id_touch_pull);
        findViewById(R.id.id_main).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked()){
                    case MotionEvent.ACTION_DOWN:
                        mTouchMoveStartY = event.getY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        float moveY = event.getY();
                        if (moveY >= mTouchMoveStartY){
                            float moveSize = moveY - mTouchMoveStartY;
                            float progress = moveSize >= TOUCH_MOVE_MAX_Y ? 1 : moveSize/TOUCH_MOVE_MAX_Y;
                            mPullView.setProgress(progress);
                            return true;
                        }
                    case MotionEvent.ACTION_UP:
                        mPullView.releaseView();
                        return true;

                }
                return false;
            }
        });
    }
}
