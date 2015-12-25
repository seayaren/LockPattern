package com.jackie.lockpattern;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
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
    /**
     * 圆的画笔
     */
    private Paint mCirclePaint;
    /**
     * 线的画笔
     */
    private Paint mLinePaint;
    /**
     * 圆心数组
     */
    private Point[][] mPointArray = new Point[3][3];
    /**
     * 保存选中点的集合
     */
    private List<Point> mPointList;


    /**
     * 解锁图案的边长
     */
    private int mPatternWidth;

    /**
     * 半径
     */
    private float mRadius;

    /**
     * 第一个点是否选中
     */
    private boolean mIsSelected;
    /**
     * 是否绘制结束
     */
    private boolean mIsFinish;

    /**
     * 正在滑动并且没有任何点选中
     */
    private boolean mIsMovingWithoutCircle = false;

    private float mCurrentX, mCurrentY;

    /**
     * 正常状态的颜色
     */
    private static final int NORMAL_COLOR = 0xFF70DBDB;
    /**
     * 选中状态的颜色
     */
    private static final int SELECTED_COLOR = 0xFF979797;

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
        mLinePaint.setStrokeWidth(20);
        mLinePaint.setColor(SELECTED_COLOR);
        mLinePaint.setStyle(Paint.Style.STROKE);

        mRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, getResources().getDisplayMetrics());

        mPointList = new ArrayList<>();
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
        //画圆
        drawCircle(canvas);

        //将选中的圆重新绘制一遍，保证选中的点和未选中的点有区别
        for (Point point : mPointList) {
            mCirclePaint.setColor(SELECTED_COLOR);
            canvas.drawCircle(point.x, point.y, mRadius, mCirclePaint);
            mCirclePaint.setColor(NORMAL_COLOR);  //每重新绘制一个,将画笔的颜色重置,保证不会影响其他圆的绘制

            //将绘制的点按顺序设置转化成密码

        }

        //画线
        if (mPointList.size() > 0) {
            Point pointA = mPointList.get(0);  //第一个选中的点为A点
            for (int i = 0; i < mPointList.size(); i++) {
                Point pointB = mPointList.get(i);  //其他依次遍历出来的点为B点
                drawLine(canvas, pointA, pointB);
                pointA = pointB;
            }

            //绘制轨迹
            if (mIsMovingWithoutCircle) {
                drawLine(canvas, pointA, new Point((int)mCurrentX, (int)mCurrentY));
            }
        }

        super.onDraw(canvas);
    }

    /**
     * 画圆
     * @param canvas  画布
     */
    private void drawCircle(Canvas canvas) {
        //初始化点的位置
        for (int i = 0; i < mPointArray.length; i++) {
            for (int j = 0; j < mPointArray.length; j++) {
                //圆心的坐标
                int cx = mPatternWidth / 4 * (j + 1);
                int cy = mPatternWidth / 4 * (i + 1);

                //将圆心放在一个点数组中
                mPointArray[i][j] = new Point(cx, cy);
                canvas.drawCircle(cx, cy, mRadius, mCirclePaint);
            }
        }
    }

    /**
     * 画线
     * @param canvas  画布
     * @param pointA  第一个点
     * @param pointB  第二个点
     */
    private void drawLine(Canvas canvas, Point pointA, Point pointB) {
        canvas.drawLine(pointA.x, pointA.y, pointB.x, pointB.y, mLinePaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mCurrentX = event.getX();
        mCurrentY = event.getY();
        Point point = null;

        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPointList.clear();
                mIsFinish = false;

                point = checkSelectPoint();

                if (point != null) {
                    //第一次按下的位置在圆内
                    mIsSelected = true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mIsSelected) {
                    point = checkSelectPoint();
                }

                if (point == null) {
                    mIsMovingWithoutCircle = true;
                } else {
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mIsSelected) {
                    point = checkSelectPoint();
                }

                //如果松手时的坐标不在圆内，那么从最后一个选中的点到当前坐标的一小段直线应该删除
                if (point == null) {

                }
                mIsFinish = true;
                mIsSelected = false;
                break;
        }

        //将选中的点收集起来
        if (!mIsFinish && mIsSelected && point != null) {
            if (!mPointList.contains(point)) {
                mPointList.add(point);
            }
        }

        if (mIsFinish) {
            if (mPointList.size() == 1) {
                mPointList.clear();
            } else if (mPointList.size() < 5 && mPointList.size() > 2) {
                //错误的状态
            }
        }

        invalidate();
        return true;
    }

    /**
     * 判断当前按下的位置是否在圆心数组中
     * @return 返回选中的点
     */
    private Point checkSelectPoint() {
        for (int i = 0; i < mPointArray.length; i++) {
            for (int j = 0; j < mPointArray.length; j++) {
                Point point = mPointArray[i][j];
                if (isWithinCircle(mCurrentX, mCurrentY, point.x, point.y, mRadius)) {
                    return point;
                }
            }
        }

        return null;
    }

    /**
     * 判断点是否在圆内
     * @param x      点X轴坐标
     * @param y      点Y轴坐标
     * @param cx     圆心X坐标
     * @param cy     圆心Y坐标
     * @param radius 半径
     * @return       true表示在圆内，false表示在圆外
     */
    private boolean isWithinCircle(float x, float y, float cx, float cy, float radius) {
        //如果点和圆心的距离小于半径，则证明点在圆内
        if (Math.sqrt(Math.pow(x - cx, 2) + Math.pow(y- cy, 2)) <= radius) {
           return true;
        }

        return false;
    }
}