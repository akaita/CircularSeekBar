package com.akaita.android.circularseekbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.graphics.RectF;
import android.support.annotation.ColorInt;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Displays a touchable and circular SeekBar, an optional central circle
 * and a customizable text in the center.
 * The faster the user moves the finger across the CircularSeekBar, the faster the progress changes.
 */
public class CircularSeekBar extends View {
    /**
     * Listen for touch-events on the ring area
     */
    public interface OnCircularSeekBarChangeListener {
        void onProgressChanged(CircularSeekBar seekBar, float progress, boolean fromUser);

        void onStartTrackingTouch(CircularSeekBar seekBar);

        void onStopTrackingTouch(CircularSeekBar seekBar);
    }

    /**
     * Listen for singletap-events on the inner circle area
     */
    public interface OnCenterClickedListener {
        void onCenterClicked(CircularSeekBar seekBar, float progress);
    }

    // settable by the client through attributes and programmatically
    private @Nullable OnCircularSeekBarChangeListener mOnCircularSeekBarChangeListener = null;
    private @Nullable OnCenterClickedListener mOnCenterClickedListener = null;
    private boolean mEnabled = true;
    private boolean mShowIndicator = true;
    private float mMinValue = 0f;
    private float mMaxValue = 100f;
    private @FloatRange(from=0) float mSpeedMultiplier = 1f;
    private float mProgress = 0f;
    private boolean mShowText = true;
    private @FloatRange(from=0,to=1) float mRingWidthFactor = 0.5f;
    private @Nullable String mProgressText = null;
    private boolean mShowInnerCircle = true;
    private @ColorInt int mRingColor = Color.rgb(192, 255, 140); //LIGHT LIME
    private @ColorInt int mInnerCircleColor = Color.WHITE;
    private @ColorInt int mProgressTextColor = Color.BLACK;
    private @FloatRange(from=0) float mProgressTextSize = Utils.convertDpToPixel(getResources(), 24f);

    // settable by the client programmatically
    private Paint mRingPaint;
    private Paint mInnerCirclePaint;
    private Paint mProgressTextPaint;
    private NumberFormat mProgressTextFormat = new DecimalFormat("###,###,###,##0.0");

    // private
    private RectF mViewBox = new RectF();
    private int mDimAlpha = 80;
    private GestureDetector mGestureDetector;
    private boolean mTouching = false;
    private @FloatRange(from=0,to=360) float mTouchAngle = 0f;
    private AngularVelocityTracker mAngularVelocityTracker;

    //region Constructor
    public CircularSeekBar(Context context) {
        super(context);
        init(context, null, 0);
    }

    public CircularSeekBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public CircularSeekBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }
    //endregion

    private void init(Context context, @Nullable AttributeSet attrs, int defStyle) {
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.CircularSeekBar,
                0,
                0);
        try {
            mEnabled = a.getBoolean(R.styleable.CircularSeekBar_enabled, mEnabled);
            mShowIndicator = a.getBoolean(R.styleable.CircularSeekBar_showIndicator, mShowIndicator);
            mMinValue = a.getFloat(R.styleable.CircularSeekBar_min, mMinValue);
            mMaxValue = a.getFloat(R.styleable.CircularSeekBar_max, mMaxValue);
            mSpeedMultiplier = a.getFloat(R.styleable.CircularSeekBar_speedMultiplier, mSpeedMultiplier);
            mProgress = a.getFloat(R.styleable.CircularSeekBar_progress, mProgress);
            mShowText = a.getBoolean(R.styleable.CircularSeekBar_showProgressText, mShowText);
            mRingWidthFactor = a.getFloat(R.styleable.CircularSeekBar_ringWidth, mRingWidthFactor);
            mProgressText = a.getString(R.styleable.CircularSeekBar_progressText);
            mShowInnerCircle = a.getBoolean(R.styleable.CircularSeekBar_showInnerCircle, mShowInnerCircle);
            mRingColor = a.getColor(R.styleable.CircularSeekBar_ringColor, mRingColor);
            mInnerCircleColor = a.getColor(R.styleable.CircularSeekBar_innerCircleColor, mInnerCircleColor);
            mProgressTextColor = a.getColor(R.styleable.CircularSeekBar_progressTextColor, mProgressTextColor);
            mProgressTextSize = Utils.convertDpToPixel(getResources(), a.getFloat(R.styleable.CircularSeekBar_progressTextSize, mProgressTextSize));
        } finally {
            a.recycle();
        }

        mRingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mRingPaint.setStyle(Style.FILL);
        mRingPaint.setColor(mRingColor);

        mInnerCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mInnerCirclePaint.setStyle(Style.FILL);
        mInnerCirclePaint.setColor(mInnerCircleColor);

        mProgressTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mProgressTextPaint.setStyle(Style.STROKE);
        mProgressTextPaint.setTextAlign(Align.CENTER);
        mProgressTextPaint.setColor(mProgressTextColor);
        mProgressTextPaint.setTextSize(mProgressTextSize);

        mGestureDetector = new GestureDetector(getContext(), new GestureListener());
    }

    //region Lifecycle
    @Override
    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld){
        super.onSizeChanged(xNew, yNew, xOld, yOld);

        initViewBox();
        mAngularVelocityTracker = new AngularVelocityTracker(getCenter().x, getCenter().y);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawWholeCircle(canvas);

        if (mShowIndicator && mTouching) {
            drawProgressArc(canvas);
        }

        if (mShowInnerCircle) {
            drawInnerCircle(canvas);
        }

        if (mShowText) {
            if (mProgressText != null) {
                drawCustomText(canvas);
            } else {
                drawProgressText(canvas);
            }
        }
    }
    //endregion

    //region Touches
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mEnabled) {
            // if the detector recognized a gesture, consume it
            if (mGestureDetector.onTouchEvent(event)) {
                return true;
            }

            // get the distance from the touch to the center of the view
            float distance = distanceToCenter(event.getX(), event.getY());
            float outerCircleRadius = getOuterCircleRadius();
            float innerCircleRadius = getInnerCircleRadius();

            // touch gestures only work when touches are made exactly on the bar/arc
            if (distance >= innerCircleRadius && distance < outerCircleRadius) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mTouching = true;
                        trackTouchStart(event);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        mTouching = true;
                        trackTouchMove(event);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        mTouching = false;
                        trackTouchStop();
                        break;
                }
            } else {
                mTouching = false;
                mAngularVelocityTracker.clear();
            }

            invalidate();
            return true;
        } else {
            return super.onTouchEvent(event);
        }
    }

    private void trackTouchStart(MotionEvent event) {
        mAngularVelocityTracker.clear();
        updateProgress(event.getX(), event.getY(), mAngularVelocityTracker.getAngularVelocity());
        if (mOnCircularSeekBarChangeListener != null) {
            mOnCircularSeekBarChangeListener.onStartTrackingTouch(this);
        }
    }

    private void trackTouchMove(MotionEvent event) {
        mAngularVelocityTracker.addMovement(event);
        updateProgress(event.getX(), event.getY(), mAngularVelocityTracker.getAngularVelocity());
        if (mOnCircularSeekBarChangeListener != null) {
            mOnCircularSeekBarChangeListener.onProgressChanged(this, mProgress, true);
        }
    }

    private void trackTouchStop() {
        mAngularVelocityTracker.clear();
        if (mOnCircularSeekBarChangeListener != null) {
            mOnCircularSeekBarChangeListener.onStopTrackingTouch(this);
        }
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            // get the distance from the touch to the center of the view
            float distance = distanceToCenter(event.getX(), event.getY());
            float r = getOuterCircleRadius();

            // touch gestures only work when touches are made exactly on the bar/arc
            if (mOnCenterClickedListener != null
                    && distance <= r - r * mRingWidthFactor) {
                mOnCenterClickedListener.onCenterClicked(CircularSeekBar.this, mProgress);
            }
            return false;
        }
    }
    //endregion

    //region Public listener
    /**
     * Set a listener for touch-events related to the outer ring of the CircularSeekBar
     * @param listener
     */
    public void setOnCircularSeekBarChangeListener(@Nullable OnCircularSeekBarChangeListener listener) {
        mOnCircularSeekBarChangeListener = listener;
    }

    /**
     * Set a listener for singletap-events related to the central ring of the CircularSeekBar
     * @param listener
     */
    public void setOnCenterClickedListener(@Nullable OnCenterClickedListener listener) {
        mOnCenterClickedListener = listener;
    }
    //endregion

    //region Public attribute
    /**
     * Enable/disable the visual indicator shown under the touched area
     * @param enable
     */
    public void setIndicator(boolean enable) {
        mShowIndicator = enable;
        invalidate();
    }

    public boolean isIndicatorEnabled() {
        return mShowIndicator;
    }

    /**
     * Minimum possible value of the progress
     * @param min
     */
    public void setMin(float min) {
        mMinValue = min;
        setProgress(Math.min(mMinValue, mProgress));
    }

    public float getMin() {
        return mMinValue;
    }

    /**
     * Maximum possible value of the progress
     * @param max
     */
    public void setMax(float max) {
        mMaxValue = max;
        setProgress(Math.max(mMaxValue, mProgress));
    }

    public float getMax() {
        return mMaxValue;
    }

    /**
     * Accelerate or decelerate the change in progress relative to the user's circular scrolling movement
     * @param speedMultiplier 0-1 to decrease change, 1+ to increase change
     */
    public void setSpeedMultiplier(@FloatRange(from=0) float speedMultiplier) {
        mSpeedMultiplier = speedMultiplier;
    }

    public float getSpeedMultiplier() {
        return mSpeedMultiplier;
    }

    /**
     * Set current value of the progress
     * @param progress
     */
    public void setProgress(float progress) {
        mProgress = progress;
        if (mOnCircularSeekBarChangeListener != null) {
            mOnCircularSeekBarChangeListener.onProgressChanged(this, mProgress, false);
        }
        invalidate();
    }

    /**
     * Returns the currently displayed value from the view. Depending on the
     * used method to show the value, this value can be percent or actual value.
     *
     * @return
     */
    public float getProgress() {
        return mProgress;
    }

    /**
     * Enable touch gestures on the CircularSeekBar
     * @param enable
     */
    public void setEnabled(boolean enable) {
        mEnabled = enable;
        invalidate();
    }

    public boolean isEnabled() {
        return mEnabled;
    }

    /**
     * Draw a text with the current progress in the center of the view
     * @param enabled
     */
    public void setProgressText(boolean enabled) {
        mShowText = enabled;
        invalidate();
    }

    public boolean isProgressTextEnabled() {
        return mShowText;
    }

    /**
     * Set the thickness of the outer ring (touchable area), relative to the size of the whole view
     * @param factor
     */
    public void setRingWidthFactor(@FloatRange(from=0f,to=1f) float factor) {
        mRingWidthFactor = factor;
        invalidate();
    }

    public float getRingWidthFactor() {
        return mRingWidthFactor;
    }

    /**
     * Set fixed text to be drawn in the center of the view
     * @param text
     */
    public void setProgressText(@Nullable String text) {
        mProgressText = text;
        invalidate();
    }

    public @Nullable String getProgressText() {
        return mProgressText;
    }

    /**
     * Enable/disable inner circle display
     * @param enable
     */
    public void setInnerCircle(boolean enable) {
        mShowInnerCircle = enable;
        invalidate();
    }

    public boolean isInnerCircleEnabled() {
        return mShowInnerCircle;
    }

    /**
     * Set color for the outer ring (touchable area)
     * @param color
     */
    public void setRingColor(@ColorInt int color) {
        mRingColor = color;
        mRingPaint.setColor(mRingColor);
        invalidate();
    }

    public @ColorInt int getRingColor() {
        return mRingColor;
    }

    public void setInnerCircleColor(@ColorInt int color) {
        mInnerCircleColor = color;
        mInnerCirclePaint.setColor(mInnerCircleColor);
        invalidate();
    }

    public @ColorInt int getInnerCircleColor() {
        return mInnerCircleColor;
    }

    public void setProgressTextColor(@ColorInt int color) {
        mProgressTextColor = color;
        mProgressTextPaint.setColor(mProgressTextColor);
        invalidate();
    }

    public @ColorInt int getProgressTextColor() {
        return mProgressTextColor;
    }

    public void setProgressTextSize(@FloatRange(from=0) float pixels) {
        mProgressTextSize = pixels;
        mProgressTextPaint.setTextSize(mProgressTextSize);
        invalidate();
    }

    public float getProgressTextSize() {
        return mProgressTextSize;
    }
    //endregion

    //region Public mutator
    /**
     * Set the Paint used to draw the outer ring (touchable area)
     * @param paint
     */
    public void setRingPaint(@NonNull Paint paint) {
        mRingPaint = paint;
        invalidate();
    }

    /**
     * Set the Paint used to draw the inner circle
     * @param paint
     */
    public void setInnerCirclePaint(@NonNull Paint paint) {
        mInnerCirclePaint = paint;
        invalidate();
    }

    /**
     * Set the Paint used to draw the progress text or fixed custom text
     * @param paint
     */
    public void setProgressTextPaint(@NonNull Paint paint) {
        mProgressTextPaint = paint;
        invalidate();
    }

    /**
     * Set the format of the progress text. <br/>
     * <ul>
     *     <li>"###,###,###,##0.0" will display: 1,234.5</li>
     *     <li>"###,###,###,##0.00" will display: 1,234.56</li>
     * <ul/>
     * @param format
     */
    public void setProgressTextFormat(@NonNull NumberFormat format) {
        mProgressTextFormat = format;
        invalidate();
    }

    public NumberFormat getProgressTextFormat() {
        return mProgressTextFormat;
    }
    //endregion

    //region Private draw
    private void drawWholeCircle(Canvas c) {
        mRingPaint.setAlpha(mDimAlpha);
        c.drawCircle(getWidth() / 2, getHeight() / 2, getOuterCircleRadius(), mRingPaint);
    }

    private void drawInnerCircle(Canvas c) {
        c.drawCircle(getWidth() / 2, getHeight() / 2, getInnerCircleRadius(), mInnerCirclePaint);
    }

    private void drawProgressArc(Canvas c) {
        mRingPaint.setAlpha(255);
        c.drawArc(mViewBox, mTouchAngle - 105, 30, true, mRingPaint);
    }

    private void drawProgressText(Canvas c) {
        if (mAngularVelocityTracker != null) {
            c.drawText(mProgressTextFormat.format(mProgress),
                    getWidth() / 2,
                    getHeight() / 2 + mProgressTextPaint.descent(),
                    mProgressTextPaint);
        }
    }

    private void drawCustomText(Canvas c) {
        c.drawText(mProgressText,
                getWidth() / 2,
                getHeight() / 2 + mProgressTextPaint.descent(),
                mProgressTextPaint);
    }
    //endregion

    //region Private
    private void initViewBox() {
        int width = getWidth();
        int height = getHeight();
        float diameter = getDiameter();

        mViewBox.set(width / 2 - diameter / 2, height / 2 - diameter / 2, width / 2
                + diameter / 2, height / 2 + diameter / 2);
    }

    /**
     * update display with the given touch position
     *
     * @param x
     * @param y
     */
    private void updateProgress(float x, float y, float speed) {
        // calculate the touch-angle
        mTouchAngle = getAngle(x, y);

        // calculate the new value depending on angle
        float newVal = mProgress + mMaxValue / 100 * speed * mSpeedMultiplier;
        newVal = Math.min(newVal, mMaxValue);
        newVal = Math.max(newVal, mMinValue);
        mProgress = newVal;
    }

    private float getDiameter() {
        return Math.min(getWidth(), getHeight());
    }

    private float getOuterCircleRadius() {
        return getDiameter() / 2f;
    }

    private float getInnerCircleRadius() {
        return getOuterCircleRadius() * (1 - mRingWidthFactor);
    }

    private PointF getCenter() {
        return new PointF(getWidth() / 2, getHeight() / 2);
    }

    /**
     * return angle relative to the view center for the given point on the chart in degrees.
     *
     * @param x
     * @param y
     * @return angle in degrees. 0° is NORTH
     */
    private @FloatRange(from=0,to=360) float getAngle(float x, float y) {
        PointF c = getCenter();
        return (float) -Math.toDegrees(Math.atan2(c.x - x, c.y - y));
    }

    private float distanceToCenter(float x, float y) {
        PointF c = getCenter();
        return (float) Math.sqrt(Math.pow(x - c.x, 2.0) + Math.pow(y - c.y, 2.0));
    }
    //endregion
}
