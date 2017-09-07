package com.gu.cheng.touchpull.widget;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import com.gu.cheng.touchpull.R;


/**
 * Created by gc on 2017/9/7.
 */
public class TouchPullView extends View {
    private static final String TAG = "TouchPullView";
    private Paint mCirclePaint;
    private float mCircleRadius = 50;//圆半径
    private float mCirclePointX,mCirclePointY;//圆心

    //进度值
    private float mProgress;

    //进度差值器
    private Interpolator mProgressInterpolator = new DecelerateInterpolator();
    //角度差值器
    private Interpolator mTangentAnleInterpolator;

    /**
     * 可拉动的最大距离
     */
    private int mDragHeight = 300;

    //目标宽度
    private int mTargetWidth = 400;
    //贝塞尔曲线的路径和画笔
    private Path mPath = new Path();
    private Paint mPathPaint;

    //重心点的最终高度，决定控制点的Y坐标
    private int mTargetGravityHeight = 10;

    //角度变化0~110
    private int mTargetAngle = 105;

    private Drawable mDrawable = null;
    private int mDrawableMargin = 0;


    public TouchPullView(Context context) {
        super(context);
        init( null);
    }

    public TouchPullView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public TouchPullView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init( attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TouchPullView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init( attrs);
    }

    private void init(AttributeSet attrs) {
        final Context context = getContext();
        TypedArray typedArray = context.obtainStyledAttributes(attrs,R.styleable.TouchPullView);
        int color = typedArray.getColor(R.styleable.TouchPullView_pColor, 0x20000000);
        mCircleRadius = typedArray.getDimension(R.styleable.TouchPullView_pRadius, mCircleRadius);
        mDragHeight = typedArray.getDimensionPixelOffset(R.styleable.TouchPullView_pDragHeight,mDragHeight);
        mTargetAngle = typedArray.getInteger(R.styleable.TouchPullView_pTangentAngle,mTargetAngle);
        mTargetWidth = typedArray.getDimensionPixelOffset(R.styleable.TouchPullView_pTargetWidth, mTargetWidth);
        mTargetGravityHeight = typedArray.getDimensionPixelOffset(R.styleable.TouchPullView_pTargetGravityHeight, mTargetGravityHeight);
        mDrawable = typedArray.getDrawable(R.styleable.TouchPullView_pContentDrawable);
        mDrawableMargin = typedArray.getDimensionPixelOffset(R.styleable.TouchPullView_pContentDrawableMargin, mDrawableMargin);

        typedArray.recycle();

        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setAntiAlias(true);
        p.setDither(true);
        p.setStyle(Paint.Style.FILL);
        p.setColor(color);
        mCirclePaint = p;

        p = new Paint();
        //设置抗锯齿
        p.setAntiAlias(true);
        //设置防抖动
        p.setDither(true);
        //设置为填充方式
        p.setStyle(Paint.Style.FILL);
        //设置画笔颜色
        p.setColor(color);
        mPathPaint = p;

        //切角路径差值器
        mTangentAnleInterpolator = PathInterpolatorCompat.create(
                mCircleRadius * 2.0f/mDragHeight, 90.0f/mTargetAngle
        );

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //当高度变化时进行路径更新
        updatePathLayout();
    }

    /**
     * 更新路径
     */
    private void updatePathLayout() {
        final float progress = mProgressInterpolator.getInterpolation(mProgress);
        //获取可绘制区域
        float h = getValueByLine(0,mDragHeight,mProgress);
        float w = getValueByLine(getWidth(), mTargetWidth, mProgress);
//        float w = getWidth(); //未变换坐标系
        //半径 圆心坐标
        final float cRadius = mCircleRadius;
        final float cPointX = w/2.0f;
        final float cPointY = h - cRadius;

        //控制点 移动结束时 y坐标
        final float endControlY = mTargetGravityHeight;

        //更新圆心坐标
        mCirclePointX = cPointX;
        mCirclePointY = cPointY;

        final Path path = mPath;
        path.reset();
        path.moveTo(0,0);
        //如果不进行坐标变换，那么该点为起点坐标  右侧终点坐标也需要改动
//        float startX = (getWidth() - getValueByLine(getWidth(), mTargetWidth, mProgress)) / 2;
//        path.moveTo(startX,0);

        //左边部分的结束点和控制点
        float lEndPointX, lEndPointY;
        float lControlPointX, lControlPointY;

        //获取当前切线的弧度
        float angle = mTargetAngle * mTangentAnleInterpolator.getInterpolation(progress);
        double radian = Math.toRadians(getValueByLine(0, angle, progress));
        float x = (float) (Math.sin(radian) * cRadius);
        float y = (float) (Math.cos(radian) * cRadius);

        lEndPointX = cPointX - x;
        lEndPointY = cPointY + y;

        //控制点
        lControlPointY = getValueByLine(0,endControlY,progress);
        //控制点与结束点之间的高度
        float tHeight = lEndPointY - lControlPointY;
        //控制点与结束点之间的水平距离
        float tWidth = (float) (tHeight/Math.tan(radian));
        lControlPointX = lEndPointX - tWidth;

        //贝塞尔曲线
        path.quadTo(lControlPointX, lControlPointY, lEndPointX, lEndPointY);
        //链接到右边
        path.lineTo(cPointX + (cPointX - lEndPointX), lEndPointY);
        //画右边的贝塞尔曲线
        path.quadTo(cPointX + (cPointX - lControlPointX), lControlPointY, w , 0);

        //更新内容部分drawable
        updateContentLayout(cPointX, cPointY, cRadius);

    }

    /**
     * 对内容部分进行测量和绘制
     * @param cx 圆心x
     * @param cy 圆心y坐标
     * @param radius 半径
     */
    private void updateContentLayout(float cx, float cy, float radius){
        Drawable drawable = mDrawable;
        if (drawable != null){
            int margin = mDrawableMargin;
            //drawable区域
            int l = (int) (cx - radius + margin);
            int r = (int) (cx + radius - margin);
            int t = (int) (cy - radius + margin);
            int b = (int) (cy + radius - margin);
            drawable.setBounds(l, t, r, b);
        }


    }

    @Override
    protected void onDraw(Canvas canvas) {
//        mCirclePointX = getWidth() >>1;
//        mCirclePointY = getHeight() >>1;

        //进行基础坐标参数系改变
        int count = canvas.save();
        float tranX = (getWidth() - getValueByLine(getWidth(), mTargetWidth, mProgress))/2;
        canvas.translate(tranX, 0);


        canvas.drawCircle(mCirclePointX,mCirclePointY,mCircleRadius,mCirclePaint);

        canvas.drawPath(mPath,mPathPaint);

        Drawable drawable = mDrawable;
        if (drawable != null){
            canvas.save();
            //裁剪矩形区域
            canvas.clipRect(drawable.getBounds());
            //绘制drawable
            drawable.draw(canvas);
            canvas.restore();
        }

        canvas.restoreToCount(count);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //宽高大小及模式
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int minWidth = (int) (2 * mCircleRadius + getPaddingLeft() + getPaddingRight());
        int minHeight = (int) (mDragHeight * mProgress + 0.5f) + getPaddingTop() + getPaddingBottom();

        int measureWidth = 0, measureHeight = 0;

        if (widthMode == MeasureSpec.EXACTLY){
            measureWidth = widthSize;
        }else if (widthMode == MeasureSpec.AT_MOST){
            measureWidth = Math.min(widthSize,minWidth);
        }else if(widthMode == MeasureSpec.UNSPECIFIED){
            measureWidth = minWidth;
        }

        if (heightMode == MeasureSpec.EXACTLY){
            measureHeight = heightSize;
        }else if (heightMode == MeasureSpec.AT_MOST){
            measureHeight = Math.min(heightSize,minHeight);
        }else if(heightMode == MeasureSpec.UNSPECIFIED){
            measureHeight = minHeight;
        }

        setMeasuredDimension(measureWidth,measureHeight);
    }


    /**
     * 获取当前值
     * @param start 起始值
     * @param end 结束值
     * @param progress 进度
     * @return
     */
    private float getValueByLine(float start, float end, float progress){
        return start + (end - start) * progress;
    }

    /**
     * 设置进度
     * @param progress
     */
    public void setProgress(float progress){
        Log.d(TAG, "setProgress: "+progress);
        this.mProgress = progress;
        //请求重绘
        requestLayout();
    }

    private ValueAnimator mValueAnimator;
    private void release() {
        if (mValueAnimator == null){
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(mProgress,0f);
            valueAnimator.setInterpolator(new DecelerateInterpolator());
            valueAnimator.setDuration(400);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    Object val = animation.getAnimatedValue();
                    if (val instanceof Float){
                        setProgress((Float)val);
                    }
                }
            });
            mValueAnimator = valueAnimator;
        }else {
            mValueAnimator.cancel();
            mValueAnimator.setFloatValues(mProgress,0f);
        }
        mValueAnimator.start();
    }

    public void releaseView() {
        release();
    }
}
