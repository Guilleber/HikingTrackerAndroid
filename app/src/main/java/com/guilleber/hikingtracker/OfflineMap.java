package com.guilleber.hikingtracker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

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
    private final float mStepSize = 0.00009f;
    private final int mPxPerStep = 30;
    private final float mConvertRatio = mPxPerStep/mStepSize;

    private Vector<Double> mLatMemory = new Vector<>();
    private Vector<Double> mLngMemory = new Vector<>();
    private Vector<Integer> mAltMemory = new Vector<>();

    public OfflineMap(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mCircleFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCircleFillPaint.setStyle(Paint.Style.FILL);
        mCircleFillPaint.setColor(0xffefe5fd);

        mCircleStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCircleStrokePaint.setStyle(Paint.Style.STROKE);
        mCircleStrokePaint.setStrokeWidth(5);
        mCircleStrokePaint.setColor(0xff9866f4);

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

    public void setPosMemory(Vector<Double> latMemory, Vector<Double> lngMemory) {
        assert latMemory.size() == lngMemory.size();
        mLatMemory = latMemory;
        mLngMemory = lngMemory;
    }

    public void setAltMemory(Vector<Integer> altMemory) {
        mAltMemory = altMemory;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int xMiddle = mWidth/2;
        int yMiddle = mHeight/2;
        for(int i = 0; i < mLatMemory.size(); i++) {
            int x = xMiddle + (int) (mConvertRatio*(mLngMemory.elementAt(i) - mLngMemory.lastElement()));
            int y = yMiddle + (int) (mConvertRatio*(mLatMemory.lastElement() - mLatMemory.elementAt(i)));
            drawCircle(canvas, x, y);
        }
    }
}