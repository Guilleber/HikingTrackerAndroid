package com.guilleber.hikingtracker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import java.util.Vector;

public class ElevationMap extends View {
    private Paint mLinePaint;
    private Paint mDashPaint;
    private Paint mFillPaint;
    private TextPaint mTextPaint;

    private Path mFillPath = new Path();

    private int mWidth;
    private int mHeight;
    private int[] mMinMax = new int[4];
    private int mLastMinMaxUpdate = 0;
    private final int mMarginSizeW = 30;
    private final int mMarginSizeH = 50;

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

        mDashPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDashPaint.setStyle(Paint.Style.STROKE);
        mDashPaint.setStrokeWidth(3);
        mDashPaint.setColor(0xFF26263A);
        mDashPaint.setPathEffect(new DashPathEffect(new float[]{20, 10}, 0));

        mFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mFillPaint.setStyle(Paint.Style.FILL);
        mFillPaint.setColor(0x8026263A);

        mFillPath.setFillType(Path.FillType.EVEN_ODD);

        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(0xFF26263A);
        mTextPaint.setTextSize(30);

        setBackgroundColor(0x00FFFFFF);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int wPad = getPaddingLeft() + getPaddingRight();
        int hPad = getPaddingTop() + getPaddingBottom();
        mWidth = w - wPad - 2*mMarginSizeW;
        mHeight = h - hPad - 2*mMarginSizeH;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(mAltMemory.size() < 2)
            return;

        computeMinMax();

        float yRatio = (float)Math.max(mMinMax[3] - mMinMax[1], 50);
        yRatio = mHeight/yRatio;

        int nbPoints = Math.min(mAltMemory.size(), mWidth/4);
        float xRatio = mWidth/(float)(nbPoints - 1);

        if(nbPoints < 2)
            return;

        canvas.drawText(mMinMax[3] + " m", mMarginSizeW, mHeight + mMarginSizeH - yRatio*(mMinMax[3] - mMinMax[1]) - 10, mTextPaint);
        canvas.drawLine(mMarginSizeW, mHeight + mMarginSizeH - yRatio*(mMinMax[3] - mMinMax[1]), mWidth + mMarginSizeW, mHeight + mMarginSizeH - yRatio*(mMinMax[3] - mMinMax[1]), mDashPaint);
        canvas.drawText(mMinMax[1] + " m", mMarginSizeW, mHeight + mMarginSizeH + 30, mTextPaint);
        canvas.drawLine(mMarginSizeW, mHeight + mMarginSizeH, mWidth + mMarginSizeW, mHeight + mMarginSizeH, mDashPaint);

        int xStart, yStart, xEnd, yEnd;
        for(int i = mAltMemory.size() - nbPoints + 1; i < mAltMemory.size(); i++) {
            xStart = mMarginSizeW + (int)(xRatio*(i-1));
            yStart = mHeight + mMarginSizeH - (int)(yRatio*(mAltMemory.elementAt(i-1) - mMinMax[1]));
            xEnd = mMarginSizeW + (int)(xRatio*i);
            yEnd = mHeight + mMarginSizeH - (int)(yRatio*(mAltMemory.elementAt(i) - mMinMax[1]));

            mFillPath.rewind();
            mFillPath.moveTo(xStart, yStart);
            mFillPath.lineTo(xEnd, yEnd);
            mFillPath.lineTo(xEnd, mMarginSizeH + mHeight);
            mFillPath.lineTo(xStart, mMarginSizeH + mHeight);
            mFillPath.lineTo(xStart, yStart);
            canvas.drawPath(mFillPath, mFillPaint);

            canvas.drawLine(xStart, yStart, xEnd, yEnd, mLinePaint);
        }
    }

    public void setAltMemory(Vector<Integer> altMemory) {
        mAltMemory = altMemory;
    }

    private void computeMinMax() {
        for(; mLastMinMaxUpdate < mAltMemory.size(); mLastMinMaxUpdate++) {
            if(mAltMemory.elementAt(mLastMinMaxUpdate) >= mMinMax[3]) {
                mMinMax[2] = mLastMinMaxUpdate;
                mMinMax[3] = mAltMemory.elementAt(mLastMinMaxUpdate);
            }
            if(mAltMemory.elementAt(mLastMinMaxUpdate) >= 0 && mAltMemory.elementAt(mLastMinMaxUpdate) <= mMinMax[1]) {
                mMinMax[0] = mLastMinMaxUpdate;
                mMinMax[1] = mAltMemory.elementAt(mLastMinMaxUpdate);
            }
        }
    }
}
