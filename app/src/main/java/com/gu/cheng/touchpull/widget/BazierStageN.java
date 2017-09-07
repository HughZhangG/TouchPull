package com.gu.cheng.touchpull.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
 
/**
 * 
 * N阶贝塞尔曲线
 */
 
public class BazierStageN extends View {
 
 
    public BazierStageN(Context context) {
        super(context);
        init();
    }
 
    public BazierStageN(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
 
    public BazierStageN(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
 
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public BazierStageN(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }
 
    private Path mBazier = new Path();
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
 
 
    /**
     * 初始化
     */
    private void init() {
        Paint paint = mPaint;
        //抗锯齿抗抖动
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        paint.setColor(Color.RED);
 
        new Thread() {
            @Override
            public void run() {
                super.run();
                initBazier();
            }
        }.start();
 
 
    }
 
    private void initBazier() {
        //(0,0) (200, 800) (400, 600) (600, 1000) (900, 200)
        final float xPoint[] = new float[]{0, 200, 400, 600, 900};
        final float yPoint[] = new float[]{0, 800, 600, 1000, 200};
 
        Path path = mBazier;
 
 
        for (int i = 0; i < 1000; i++) {
            float progress = i / (float) 1000;
            float x = calculateBazierPoint(progress, xPoint);
            float y = calculateBazierPoint(progress, yPoint);
            //使用连接的方式 当xy变化足够小时，链接成的就是一条平滑的曲线
            path.lineTo(x, y);
            //刷新
            postInvalidate();
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
 
    }
 
 
    /**
     * 计算某时刻贝塞尔曲线的点（x或y）
     *
     * @param progress 进度 （时间值）（0~1）
     * @param values   xPoint或yPoint
     * @return
     */
    private float calculateBazierPoint(float progress, float... values) {
        int len = values.length;
        for (int i = len - 1; i > 0; i--) {
            for (int j = 0; j < i; j++) {
                values[j] = values[j] + (values[j + 1] - values[j]) * progress;
            }
        }
        //运算结果返回第一位
        return values[0];
    }
 
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
 
        canvas.drawPath(mBazier, mPaint);
    }
}
