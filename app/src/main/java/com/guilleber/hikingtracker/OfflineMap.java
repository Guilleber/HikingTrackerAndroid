package com.guilleber.hikingtracker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.location.Location;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import org.w3c.dom.Text;

import java.util.Vector;

/**
 * TODO: document your custom view class.
 */
public class OfflineMap extends View {

    private Paint mCircleStrokePaint;
    private Paint mCircleFillPaint;
    private int mWidth;
    private int mHeight;
    private final int mCircleRadius = 10;
    private final int mStepSize = 10;
    private final int mPxPerStep = 30;
    private float mConvertRatioX = -1;
    private float mConvertRatioY = -1;

    private int mMarginTop;
    private int mMarginBot;
    private int mMarginLeft;
    private int mMarginRight;

    private Vector<Double> mLatMemory = new Vector<>();
    private Vector<Double> mLngMemory = new Vector<>();

    private ScaleGestureDetector mScaleDetector;
    private float mScaleFactor = 1.0f;

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();
            mScaleFactor = Math.max(0.02f, Math.min(mScaleFactor, 1.0f));
            invalidate();
            return true;
        }
    }

    private RotationGestureDetector mRotationDetector;
    private double mRotationAngle = 0.0;
    private Point mPoint = new Point(0.0, 0.0);

    private class RotationListener implements RotationGestureDetector.OnRotationGestureListener {
        @Override
        public void OnRotation(RotationGestureDetector rotationDetector) {
            mRotationAngle = rotationDetector.getAngle();
            invalidate();
        }
    }

    public OfflineMap(Context context, AttributeSet attrs) {
        super(context, attrs);

        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        mRotationDetector = new RotationGestureDetector(new RotationListener());

        init_colors();
    }

    private void init_colors() {
        mCircleFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCircleFillPaint.setStyle(Paint.Style.FILL);
        mCircleFillPaint.setColor(0xFF434359);
        //mCircleFillPaint.setColor(com.google.android.material.R.attr.colorPrimaryVariant);

        mCircleStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCircleStrokePaint.setStyle(Paint.Style.STROKE);
        mCircleStrokePaint.setStrokeWidth(5);
        mCircleStrokePaint.setColor(0xFF26263A);
        //mCircleStrokePaint.setColor(com.google.android.material.R.attr.colorPrimary);

        setBackgroundColor(0xffeceff1);
    }

    private void drawCircle(Canvas canvas, int x, int y) {
        canvas.drawCircle(x, y, mCircleRadius, mCircleFillPaint);
        canvas.drawCircle(x, y, mCircleRadius, mCircleStrokePaint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int wPad = getPaddingLeft() + getPaddingRight();
        int hPad = getPaddingTop() + getPaddingBottom();
        mWidth = w - wPad;
        mHeight = h - hPad;
        recomputeMargins();
    }

    private void recomputeMargins() {
        mMarginTop = mHeight/2 - 225;
        mMarginBot = mHeight/2 - 325;
        mMarginLeft = mWidth/2 - 20;
        mMarginRight = mWidth/2 - 20;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mScaleDetector.onTouchEvent(event);
        mRotationDetector.onTouchEvent(event);
        return true;
    }

    public void setPosMemory(Vector<Double> latMemory, Vector<Double> lngMemory) {
        assert latMemory.size() == lngMemory.size();
        mLatMemory = latMemory;
        mLngMemory = lngMemory;
    }

    private class Point {
        public Point(double initX, double initY) {
            x = initX;
            y = initY;
        }

        public double x;
        public double y;
    }

    private void adaptForRotation() {
        mPoint.y *= -1;
        if(mPoint.y == 0.0) {
            mPoint.y = mPoint.x*Math.sin(mRotationAngle);
            mPoint.x = mPoint.x*Math.cos(mRotationAngle);
        } else if(mPoint.x == 0.0) {
            mPoint.x = -mPoint.y*Math.sin(mRotationAngle);
            mPoint.y = mPoint.y*Math.cos(mRotationAngle);
        } else {
            double theta0 = Math.atan(mPoint.y / mPoint.x);
            mPoint.x = (mPoint.x * Math.cos(theta0 + mRotationAngle)) / Math.cos(theta0);
            mPoint.y = (mPoint.y * Math.sin(theta0 + mRotationAngle)) / Math.sin(theta0);
        }
        mPoint.y *= -1;
    }

    private void drawBorderMark(Canvas canvas, double angle) {
        if(angle >= 2*Math.PI)
            angle -= 2*Math.PI;
        if(angle < 0.0)
            angle += 2*Math.PI;

        if(angle <= Math.atan(mMarginTop/(float)mMarginRight) || angle >= Math.atan(-mMarginBot/(float)mMarginRight) + 2*Math.PI) {
            int y0 = (int) (mMarginRight*Math.tan(angle));
            int y1 = (int) ((mMarginRight-40)*Math.tan(angle));
            canvas.drawLine(mWidth/2 + mMarginRight, mHeight/2 - y0, mWidth/2 + mMarginRight - 40, mHeight/2 - y1, mCircleStrokePaint);
            return;
        }

        angle -= Math.PI/2;
        if(angle <= Math.atan(mMarginLeft/(float)mMarginTop)) {
            int y0 = (int) (mMarginTop*Math.tan(angle));
            int y1 = (int) ((mMarginTop-50)*Math.tan(angle));
            canvas.drawLine(mWidth/2 - y0, mHeight/2 - mMarginTop, mWidth/2 - y1, mHeight/2 - mMarginTop + 50, mCircleStrokePaint);
            return;
        }

        angle -= Math.PI/2;
        if(angle <= Math.atan(mMarginBot/(float)mMarginLeft)) {
            int y0 = (int) (mMarginLeft*Math.tan(angle));
            int y1 = (int) ((mMarginLeft-40)*Math.tan(angle));
            canvas.drawLine(mWidth/2 - mMarginLeft, mHeight/2 + y0, mWidth/2 - mMarginLeft + 40, mHeight/2 + y1, mCircleStrokePaint);
            return;
        }

        angle -= Math.PI/2;
        if(angle <= Math.atan(mMarginRight/(float)mMarginBot)) {
            int y0 = (int) (mMarginBot*Math.tan(angle));
            int y1 = (int) ((mMarginBot-40)*Math.tan(angle));
            canvas.drawLine(mWidth/2 + y0, mHeight/2 + mMarginBot, mWidth/2 + y1, mHeight/2 + mMarginBot - 40, mCircleStrokePaint);
            return;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBorderMark(canvas, mRotationAngle);
        drawBorderMark(canvas, mRotationAngle + Math.PI/2);
        drawBorderMark(canvas, mRotationAngle + Math.PI);
        drawBorderMark(canvas, mRotationAngle + 3*Math.PI/2);
        if(mLatMemory.size() == 0)
            return;
        if(mConvertRatioX == -1) {
            float[] results = new float[1];
            Location.distanceBetween(0.0, 0.0, 1.0, 0.0, results);
            mConvertRatioY = results[0]*mPxPerStep/mStepSize;
            Location.distanceBetween(mLatMemory.lastElement(), 0.0, mLatMemory.lastElement(), 1.0, results);
            mConvertRatioX = results[0]*mPxPerStep/mStepSize;
        }
        int xMiddle = mWidth/2;
        int yMiddle = mHeight/2;
        for(int i = 0; i < mLatMemory.size(); i++) {
            mPoint.x = mScaleFactor*mConvertRatioX*(mLngMemory.elementAt(i) - mLngMemory.lastElement());
            mPoint.y = mScaleFactor*mConvertRatioY*(mLatMemory.lastElement() - mLatMemory.elementAt(i));
            adaptForRotation();
            mPoint.x = xMiddle + mPoint.x;
            mPoint.y = yMiddle + mPoint.y;
            drawCircle(canvas, (int)mPoint.x, (int)mPoint.y);
        }
    }
}