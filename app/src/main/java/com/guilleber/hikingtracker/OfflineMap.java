package com.guilleber.hikingtracker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.location.Location;
import android.text.TextPaint;
import android.util.AttributeSet;
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
            mRotationAngle = -rotationDetector.getAngle();
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
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
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