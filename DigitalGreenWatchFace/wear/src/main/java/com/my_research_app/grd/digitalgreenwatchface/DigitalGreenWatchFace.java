
//цифровой зеленый - digital green
package com.my_research_app.grd.digitalgreenwatchface;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.WindowInsets;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;


public class DigitalGreenWatchFace extends CanvasWatchFaceService {

    private static final String TAG = "myLogs";


    boolean isRound;
    boolean isBackgroundChanged;

    private static final Typeface NORMAL_TYPEFACE =
            Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);

   
    private static final long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1);

 
    private static final int MSG_UPDATE_TIME = 0;

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    private static class EngineHandler extends Handler {
        private final WeakReference<DigitalGreenWatchFace.Engine> mWeakReference;

        public EngineHandler(DigitalGreenWatchFace.Engine reference) {
            mWeakReference = new WeakReference<>(reference);
        }

        @Override
        public void handleMessage(Message msg) {
            DigitalGreenWatchFace.Engine engine = mWeakReference.get();
            if (engine != null) {
                switch (msg.what) {
                    case MSG_UPDATE_TIME:
                        engine.handleUpdateTimeMessage();
                        break;
                }
            }
        }
    }

    private class Engine extends CanvasWatchFaceService.Engine {
        final Handler mUpdateTimeHandler = new EngineHandler(this);
        boolean mRegisteredTimeZoneReceiver = false;
        Paint mBackgroundPaint;
        Paint mTextPaint;
        boolean mAmbient;
        Calendar mCalendar;
        final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mCalendar.setTimeZone(TimeZone.getDefault());
                invalidate();
            }
        };
        float mXOffset;
        float mYOffset;


        boolean mLowBitAmbient;

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            setWatchFaceStyle(new WatchFaceStyle.Builder(DigitalGreenWatchFace.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_VARIABLE)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false)
                    .setAcceptsTapEvents(true)
                    .build());
            Resources resources = DigitalGreenWatchFace.this.getResources();
            mYOffset = resources.getDimension(R.dimen.digital_y_offset);

            mBackgroundPaint = new Paint();
            mBackgroundPaint.setColor(resources.getColor(R.color.background));
            mBackgroundPaint.setColor(resources.getColor(R.color.grey));
            mBackgroundPaint.setColor(resources.getColor(R.color.my_background));

            mBackgroundPaint.setColor(resources.getColor(R.color.background));

            mTextPaint = new Paint();
            mTextPaint = createTextPaint(resources.getColor(R.color.my_custom_color));


            mCalendar = Calendar.getInstance();
        }

        @Override
        public void onDestroy() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            super.onDestroy();
        }

        private Paint createTextPaint(int textColor) {
            Paint paint = new Paint();
            //Set the paint's color. Note that the color is an int containing alpha as well as r,g,b.
            // This 32bit value is not premultiplied, meaning that its alpha can be any value,
            // regardless of the values of r,g,b. See the Color class for more details.
            paint.setColor(textColor);
            paint.setTypeface(Typeface.DEFAULT_BOLD);

            paint.setAntiAlias(true);
            return paint;
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                registerReceiver();


                mCalendar.setTimeZone(TimeZone.getDefault());
                invalidate();
            } else {
                unregisterReceiver();
            }


            updateTimer();
        }

        private void registerReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            DigitalGreenWatchFace.this.registerReceiver(mTimeZoneReceiver, filter);
        }

        private void unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = false;
            DigitalGreenWatchFace.this.unregisterReceiver(mTimeZoneReceiver);
        }


        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            super.onApplyWindowInsets(insets);


            Resources resources = DigitalGreenWatchFace.this.getResources();

            isRound = insets.isRound();

            mYOffset = resources.getDimension(isRound
                    ? R.dimen.digital_y_offset_round : R.dimen.digital_y_offset);

            mXOffset = resources.getDimension(isRound
                    ? R.dimen.digital_x_offset_round : R.dimen.digital_x_offset);
            float textSize = resources.getDimension(isRound
                    ? R.dimen.digital_text_size_round : R.dimen.digital_text_size);

            mTextPaint.setTextSize(textSize);
        }


        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }


        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);



            if (mAmbient != inAmbientMode) {
                mAmbient = inAmbientMode;
                if (mLowBitAmbient) {
                    mTextPaint.setAntiAlias(!inAmbientMode);
                }
                invalidate();
            }

            updateTimer();
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {


            // Draw the background.
            if (isInAmbientMode()) {
                canvas.drawColor(Color.GREEN);
                canvas.drawColor(Color.BLACK);

                Resources resources = DigitalGreenWatchFace.this.getResources();

                mXOffset = resources.getDimension(isRound
                        ? R.dimen.digital_x_offset_round_ambient_mode : R.dimen.digital_x_offset_ambient_mode);


            } else {
                canvas.drawRect(0, 0, bounds.width(), bounds.height(), mBackgroundPaint);
                Resources resources = DigitalGreenWatchFace.this.getResources();

                mXOffset = resources.getDimension(isRound
                        ? R.dimen.digital_x_offset_round : R.dimen.digital_x_offset);


            }

            long now = System.currentTimeMillis();
            mCalendar.setTimeInMillis(now);

            int rTime = mCalendar.get(Calendar.HOUR);


            if(10 <= mCalendar.get(Calendar.HOUR_OF_DAY))
            {
                String text = mAmbient
                        ? String.format("%d:%02d", mCalendar.get(Calendar.HOUR_OF_DAY),
                        mCalendar.get(Calendar.MINUTE))
                        : String.format("%d:%02d:%02d", mCalendar.get(Calendar.HOUR_OF_DAY),
                        mCalendar.get(Calendar.MINUTE), mCalendar.get(Calendar.SECOND));
                canvas.drawText(text, mXOffset, mYOffset, mTextPaint);

            }else{
                String text = mAmbient
                                ? String.format("0"+"%d:%02d", mCalendar.get(Calendar.HOUR),
                                mCalendar.get(Calendar.MINUTE))
                                : String.format("0"+"%d:%02d:%02d", mCalendar.get(Calendar.HOUR),
                                mCalendar.get(Calendar.MINUTE), mCalendar.get(Calendar.SECOND));
                canvas.drawText(text, mXOffset, mYOffset, mTextPaint);

            }

        }

        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }


        private boolean shouldTimerBeRunning() {
            return isVisible() && !isInAmbientMode();
        }


        private void handleUpdateTimeMessage() {
            invalidate();
            if (shouldTimerBeRunning()) {
                long timeMs = System.currentTimeMillis();
                long delayMs = INTERACTIVE_UPDATE_RATE_MS
                        - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
            }
        }
    }
}
