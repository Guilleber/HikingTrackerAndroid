package com.guilleber.hikingtracker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.ScaleGestureDetector;
import android.view.View;

import java.util.Vector;

public class ElevationMap extends View {
    private Paint mLinePaint;

    private int mWidth;
    private int mHeight;
    private int[] mMinMax = new int[4];
    private int mLastMinMaxUpdate = 0;

    private Vector<Integer> mAltMemory = new Vector<>();

    public ElevationMap(Context context, AttributeSet attrs) {
        super(context, attrs);

        mMinMax[0] = -1;
        mMinMax[1] = 32767;
        mMinMax[2] = -1;
        mMinMax[3] = 0;

        init_colors();
        invalidate();
    }

    private void init_colors() {
        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeWidth(5);
        mLinePaint.setColor(0xFF26263A);

        setBackgroundColor(0x00FFFFFF);
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
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        computeMinMax();
        if(mMinMax[3] <= mMinMax[1])
            return;

        float yRatio = (float)Math.max(mMinMax[3] - mMinMax[1], 50);
        yRatio = mHeight/yRatio;

        int nbPoints = Math.min(mAltMemory.size(), (int)(mWidth/4.0));
        float xRatio = mWidth/(float)nbPoints;

        if(nbPoints < 2)
            return;

        for(int i = mAltMemory.size() - nbPoints + 1; i < mAltMemory.size(); i++) {
            canvas.drawLine(xRatio*(i-1), mHeight - yRatio*mAltMemory.elementAt(i-1), xRatio*i, mHeight - yRatio*mAltMemory.elementAt(i), mLinePaint);
        }
    }

    public void setAltMemory(Vector<Integer> altMemory) {
        mAltMemory = altMemory;
    }

    private void computeMinMax() {
        for(int i = mLastMinMaxUpdate; i < mAltMemory.size(); i++) {
            if(mAltMemory.elementAt(i) >= mMinMax[3]) {
                mMinMax[2] = i;
                mMinMax[3] = mAltMemory.elementAt(i);
            }
            if(mAltMemory.elementAt(i) <= mMinMax[1]) {
                mMinMax[0] = i;
                mMinMax[1] = mAltMemory.elementAt(i);
            }
        }
    }
}
