package com.guilleber.hikingtracker;

import java.util.Vector;

public class RunningAverage {
    private final Vector<Double> mMemory = new Vector<>();
    private double mSum = 0.0;
    private int mWindowSize;

    public RunningAverage(int windowSize) {
        mWindowSize = windowSize;
    }

    public double add(double value) {
        mSum = mSum + value;
        mMemory.add(value);
        if(mMemory.size() > mWindowSize)
            mSum -= mMemory.remove(0);
        return mSum/mMemory.size();
    }
}
