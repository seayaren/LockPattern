package com.jackie.lockpattern;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jackie on 2015/12/24.
 * 图案解锁
 */
public class LockPatternView extends View {

    //圆的画笔
    private Paint mCirclePaint;
    //线的画笔
    private Paint mLinePaint;
    private Path mPath;
    //保存选中圆的集合
    private List<Point> mPoints;

    /**
     * 解锁图案的边长
     */
    private int mPatternWidth;

    //半径
    private float mRadius;
    //鼠标按下的坐标
    private float mDownX, mDownY;
    //鼠标移动的坐标
    private float mMoveX, mMoveY;

    //正常状态的颜色
    private static final int NORMAL_COLOR = 0xFF70DBDB;
    //选中状态的颜色
    private static final int CHECK_COLOR = 0xFFC0C0C0;

    public LockPatternView(Context context) {
        this(context, null);
    }

    public LockPatternView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mCirclePaint = new Paint();
        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setDither(true);
        mCirclePaint.setColor(NORMAL_COLOR);
        mCirclePaint.setStyle(Paint.Style.FILL);

        mLinePaint = new Paint();
        mLinePaint.setAntiAlias(true);
        mLinePaint.setDither(true);
        mLinePaint.setStrokeWidth(15);
        mLinePaint.setColor(CHECK_COLOR);
        mLinePaint.setStyle(Paint.Style.STROKE);

        mPath = new Path();

        mRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, getResources().getDisplayMetrics());

        mPoints = new ArrayList<>();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        //取屏幕长和宽中的较小值作为图案的边长
        mPatternWidth = Math.min(getMeasuredWidth(), getMeasuredHeight());
        setMeasuredDimension(mPatternWidth, mPatternWidth);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //初始化点的位置
        for (int i = 1; i < 4; i++) {
            for (int j = 1; j < 4; j++) {
                //圆心的坐标
                int cx = mPatternWidth / 4 * i;
                int cy = mPatternWidth / 4 * j;

                if (isWithinCircle(mDownX, mDownY, cx, cy, mRadius) || isWithinCircle(mMoveX, mMoveY, cx, cy, mRadius)) {
                    //将选中的圆放在集合
                    Point point = new Point(cx, cy);
                    mPoints.add(point);
                } else {
                    mCirclePaint.setColor(NORMAL_COLOR);
                }

                canvas.drawCircle(cx, cy, mRadius, mCirclePaint);
            }
        }

        //选中的圆重新绘制
        for (Point point : mPoints) {
            //选中的状态
            mCirclePaint.setColor(CHECK_COLOR);

            //画线
            canvas.drawPath(mPath, mLinePaint);

            canvas.drawCircle(point.x, point.y, mRadius, mCirclePaint);
        }

        super.onDraw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x, y;
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //判断鼠标按下的位置是否图案中的人一个圈圈内
                mDownX = event.getX();
                mDownY = event.getY();

                mPath.moveTo(mDownX, mDownY);
                break;
            case MotionEvent.ACTION_MOVE:
                mMoveX = event.getX();
                mMoveY = event.getY();

                mPath.lineTo(mMoveX, mMoveY);
                break;
            case MotionEvent.ACTION_UP:
                //如果选中的圆小于4个，清空
                if (mPoints.size() > 0 && mPoints.size() < 4) {
                    mPath.reset();
                }
                break;
        }

        invalidate();
        return true;
    }

    /**
     * 判断点是否在圆内
     * @param x      点X坐标
     * @param y      点Y坐标
     * @param cx     圆心X坐标
     * @param cy     圆心Y坐标
     * @param radius 半径
     * @return       true表示在圆内，false表示在圆外
     */
    private boolean isWithinCircle(float x, float y, float cx, float cy, float radius) {
        //如果点和圆心的距离小于半径，则证明点在圆内
        if (Math.sqrt(Math.pow(x - cx, 2) + Math.pow(y - cy, 2)) <= radius) {
           return true;
        }

        return false;
    }
}