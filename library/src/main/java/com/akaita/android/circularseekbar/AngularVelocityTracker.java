package com.akaita.android.circularseekbar;

import android.view.MotionEvent;

class AngularVelocityTracker {

    // TODO use event.pressure (maybe: angle*(1+pressure))
    // TODO whoever uses this class should convert speed to value-change

    private long mInitialTime;
    private long mFinalTime;
    private float mInitialX;
    private float mInitialY;
    private float mFinalX;
    private float mFinalY;
    private final float mCentreX;
    private final float mCentreY;

    AngularVelocityTracker(float centreX, float centreY){
        mCentreX = centreX;
        mCentreY = centreY;
    }

    void addMovement(MotionEvent event){
        mInitialX = mFinalX;
        mInitialY = mFinalY;
        mInitialTime = mFinalTime;
        mFinalX = event.getX();
        mFinalY = event.getY();
        mFinalTime = event.getEventTime();
    }

    float getAngularVelocity(){
        float retVal = 0;
        if (mInitialTime != mFinalTime){
            long timeLapse = mInitialTime - mFinalTime;
            float initialAngle = calcAngle(mInitialX, mInitialY);
            float finalAngle = calcAngle(mFinalX, mFinalY);
            if (Math.abs(finalAngle-initialAngle) < 20) {
                // Avoid strange results from quirks in angle calculation (goes from 0.1 to 359)
                retVal = (finalAngle - initialAngle) / timeLapse;
            }
        }
        return retVal;
    }

    void clear(){
        mInitialX = 0;
        mInitialY = 0;
        mInitialTime =0;
        mFinalX = 0;
        mFinalY = 0;
        mFinalTime = 0;
    }

    private float calcAngle(float x, float y) {
        return (float) Math.toDegrees(Math.atan2(mCentreX - x, mCentreY - y));
    }
}
