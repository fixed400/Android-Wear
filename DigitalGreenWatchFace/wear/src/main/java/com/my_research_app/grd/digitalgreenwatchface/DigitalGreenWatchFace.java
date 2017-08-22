/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

/**
 * Digital watch face with seconds. In ambient mode, the seconds aren't displayed. On devices with
 * low-bit ambient mode, the text is drawn without anti-aliasing in ambient mode.
 */
public class DigitalGreenWatchFace extends CanvasWatchFaceService {

    private static final String TAG = "myLogs";


    boolean isRound;
    boolean isBackgroundChanged;

    private static final Typeface NORMAL_TYPEFACE =
            Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);

    /**
     * Update rate in milliseconds for interactive mode. We update once a second since seconds are
     * displayed in interactive mode.
     */
    private static final long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1);

    /**
     * Handler message id for updating the time periodically in interactive mode.
     */
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

        /**
         * Whether the display supports fewer bits for each color in ambient mode. When true, we
         * disable anti-aliasing in ambient mode.
         */
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
            //   mYOffset = resources.getDimension(isRound
            //           ? R.dimen.digital_y_offset_round : R.dimen.digital_y_offset);

            mBackgroundPaint = new Paint();
            mBackgroundPaint.setColor(resources.getColor(R.color.background));
            mBackgroundPaint.setColor(resources.getColor(R.color.grey));
            mBackgroundPaint.setColor(resources.getColor(R.color.my_background));
            // mBackgroundPaint.setColor(resources.getColor(R.color.my_background2));
            mBackgroundPaint.setColor(resources.getColor(R.color.background));

            mTextPaint = new Paint();
            // mTextPaint = createTextPaint(resources.getColor(R.color.digital_text));
            // mTextPaint = createTextPaint(resources.getColor(R.color.ambient_mode_text));
            mTextPaint = createTextPaint(resources.getColor(R.color.blue));
            mTextPaint = createTextPaint(resources.getColor(R.color.green));
            mTextPaint = createTextPaint(resources.getColor(R.color.background));
            mTextPaint = createTextPaint(resources.getColor(R.color.dark_red));
            mTextPaint = createTextPaint(resources.getColor(R.color.orange));
            mTextPaint = createTextPaint(resources.getColor(R.color.red));
            mTextPaint = createTextPaint(resources.getColor(R.color.grey));
            mTextPaint = createTextPaint(resources.getColor(R.color.dark_blue));
            mTextPaint = createTextPaint(resources.getColor(R.color.green));
            mTextPaint = createTextPaint(resources.getColor(R.color.my_digital_text));
            mTextPaint = createTextPaint(resources.getColor(R.color.my_digital_text2));
            mTextPaint = createTextPaint(resources.getColor(R.color.burgundy));
            mTextPaint = createTextPaint(resources.getColor(R.color.burgundy2));
            mTextPaint = createTextPaint(resources.getColor(R.color.burgundy4));
            mTextPaint = createTextPaint(resources.getColor(R.color.my_custom_color));
            //  mTextPaint = createTextPaint(resources.getColor(R.color.my_custom_color2));
            //   mTextPaint = createTextPaint(resources.getColor(R.color.my_custom_color3));
            //   mTextPaint = createTextPaint(resources.getColor(R.color.my_custom_color3_1));
            //  mTextPaint = createTextPaint(resources.getColor(R.color.my_custom_color4_1));

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
            //Устанавливает шрифт и стиль, в котором должен отображаться текст.
            // android:fontFamily
            // android:typeface
            //  android:textStyle
            paint.setTypeface(NORMAL_TYPEFACE);
            paint.setTypeface(Typeface.SANS_SERIF);
            // paint.setTypeface(Typeface.MONOSPACE);
            paint.setTypeface(Typeface.DEFAULT_BOLD);

            paint.setAntiAlias(true);
            return paint;
        }

        //Called to inform you of the watch face becoming visible or hidden.
        // If you decide to override this method, you must call super.onVisibilityChanged(visible) as the first statement in your override.
        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                registerReceiver();

                // Update time zone in case it changed while we weren't visible.
                mCalendar.setTimeZone(TimeZone.getDefault());
                invalidate();
            } else {
                unregisterReceiver();
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
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

        //Insets -Вставки
        //Returns WindowInsets The insets supplied, minus any insets that were consumed - WindowInsets Вложенные вставки с любыми применяемыми вставками
        // Called when the view should apply WindowInsets according to its internal policy.
        /*
        Этот метод должен быть переопределен представлениями, которые желают применить политику,
         отличную от или по умолчанию к поведению по умолчанию.
         Клиенты, которые хотят заставить поддерево представления применять вставки, должны вызывать dispatchApplyWindowInsets (WindowInsets).


         Клиенты могут предоставлять View.OnApplyWindowInsetsListener для представления.
         Если он установлен, он будет вызываться во время отправки вместо этого метода.
          Слушатель может опционально вызывать этот метод из своей собственной реализации,
          если он хочет применить политику вставки по умолчанию для представления в дополнение к своей собственной.

          Реализации этого метода должны либо возвращать неизмененный параметр вставки, либо новый WindowInsets,
           клонированный из прилагаемых вставок, с любыми вставками, которые использовались для этого представления.
            Это позволяет новым типам вставок, добавленным в будущих версиях платформы, проходить через существующие реализации без изменений,
            не будучи ошибочно использованным. По умолчанию, если установлено свойство fitsSystemWindows представления,
            представление будет потреблять вставки системного окна и применять их в качестве отступов для представления.

         */
        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            super.onApplyWindowInsets(insets);

            // Load resources that have alternate values for round watches.
            Resources resources = DigitalGreenWatchFace.this.getResources();
            //boolean isRound = insets.isRound();
            isRound = insets.isRound();
            // Demension - Измерение

            //  mYOffset = resources.getDimension(R.dimen.digital_y_offset);
            mYOffset = resources.getDimension(isRound
                    ? R.dimen.digital_y_offset_round : R.dimen.digital_y_offset);

            mXOffset = resources.getDimension(isRound
                    ? R.dimen.digital_x_offset_round : R.dimen.digital_x_offset);
            float textSize = resources.getDimension(isRound
                    ? R.dimen.digital_text_size_round : R.dimen.digital_text_size);

            mTextPaint.setTextSize(textSize);
        }


        // Called by an Observable whenever an observable property changes.
        //void onPropertyChanged (Observable sender,int propertyId)

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
        }

        /*
        void onTimeTick ()
        Called periodically to update the time shown by the watch face. This method is called:

        at least once per minute in both ambient and interactive modes
        when date or time has changed
            when timezone has changed
         */
        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        // Called when the device enters or exits ambient mode.
        // The watch face should switch to a black and white display in ambient mode.
        // If the watch face displays seconds, it should hide them in ambient mode.
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

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }

        /**
         * Captures tap event (and tap type) and toggles the background color if the user finishes
         * a tap.
         */


        // format 24 tome or 12 hour
        /*
        @Override
        public void onTapCommand(int tapType, int x, int y, long eventTime) {
            switch (tapType) {
                case TAP_TYPE_TOUCH:
                    // The user has started touching the screen.
                    break;
                case TAP_TYPE_TOUCH_CANCEL:
                    // The user has started a different gesture or otherwise cancelled the tap.
                    break;
                case TAP_TYPE_TAP:
                    // The user has completed the tap gesture.
                    // TODO: Add code to handle the tap gesture.
                   // Toast.makeText(getApplicationContext(), R.string.message, Toast.LENGTH_LONG).show();
                    Resources resources = DigitalGreenWatchFace.this.getResources();
                    if(isBackgroundChanged != true) {
                       // mBackgroundPaint.setColor(resources.getColor(R.color.my_background2));
                         mTextPaint = createTextPaint(resources.getColor(R.color.my_custom_color4_1));
                        // isBackgroundChanged = true;
                        isBackgroundChanged = true;

                    }else{
                       // mBackgroundPaint.setColor(resources.getColor(R.color.my_background));
                         mTextPaint = createTextPaint(resources.getColor(R.color.my_custom_color3_1));
                        isBackgroundChanged = false;
                    }
                    break;
            }
            invalidate();
        }
        */

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {


            // Draw the background.
            if (isInAmbientMode()) {
                canvas.drawColor(Color.GREEN);
                canvas.drawColor(Color.BLACK);
                //------------------------- СМЕЩЕНИЕ -----------------------------------------

                Resources resources = DigitalGreenWatchFace.this.getResources();
                //  boolean isRound = insets.isRound();
                // Demension - Измерение
                mXOffset = resources.getDimension(isRound
                        ? R.dimen.digital_x_offset_round_ambient_mode : R.dimen.digital_x_offset_ambient_mode);
                // /-------------------------------------------------------------------

            } else {
                canvas.drawRect(0, 0, bounds.width(), bounds.height(), mBackgroundPaint);
                //------------------------- СМЕЩЕНИЕ REVERSE FOCUS TIME WITH  Seconds -----------------------------------------

                Resources resources = DigitalGreenWatchFace.this.getResources();
                //  boolean isRound = insets.isRound();
                // Demension - Измерение
                mXOffset = resources.getDimension(isRound
                        ? R.dimen.digital_x_offset_round : R.dimen.digital_x_offset);
                // /-------------------------------------------------------------------

            }

            // Draw H:MM in ambient mode or H:MM:SS in interactive mode.
            long now = System.currentTimeMillis();
            mCalendar.setTimeInMillis(now);

            int rTime = mCalendar.get(Calendar.HOUR);

            /*

            String text = mAmbient
                    ? String.format("0" + "%d:%02d", mCalendar.get(Calendar.HOUR),
                    mCalendar.get(Calendar.MINUTE))
                    : String.format("0" + "%d:%02d:%02d", mCalendar.get(Calendar.HOUR),
                    mCalendar.get(Calendar.MINUTE), mCalendar.get(Calendar.SECOND));
            canvas.drawText(text, mXOffset, mYOffset, mTextPaint);
            */

             // m = 5 >= 4; // истина
            // <= — оператор «меньше или равно».
           // if(10 <= mCalendar.get(Calendar.HOUR))
            //  >= — оператор «больше или равно».
               // if(10 >= mCalendar.get(Calendar.HOUR)) // not work ???
            //if(10 >= 10) // not work ???
           // if(9 > mCalendar.get(Calendar.HOUR)
          //  int ten =10;
          //  if(10 == mCalendar.get(Calendar.HOUR) ||  mCalendar.get(Calendar.HOUR) < 10)
          //  int ten = mCalendar.get(Calendar.HOUR);




              //  if(13 <= 12)
                    if(10 <= mCalendar.get(Calendar.HOUR_OF_DAY))
                      //  if(10 != mCalendar.get(Calendar.HOUR) || 11 != mCalendar.get(Calendar.HOUR) )
            {
                //FOR 24 FORMAT TIME
                String text = mAmbient
                        ? String.format("%d:%02d", mCalendar.get(Calendar.HOUR_OF_DAY),
                        mCalendar.get(Calendar.MINUTE))
                        : String.format("%d:%02d:%02d", mCalendar.get(Calendar.HOUR_OF_DAY),
                        mCalendar.get(Calendar.MINUTE), mCalendar.get(Calendar.SECOND));
                canvas.drawText(text, mXOffset, mYOffset, mTextPaint);
                //   Log.d(TAG, "======================== 00");

            }else{
                        //  FOR 12 FORMAT TIME
                        String text = mAmbient
                                ? String.format("0"+"%d:%02d", mCalendar.get(Calendar.HOUR),
                                mCalendar.get(Calendar.MINUTE))
                                : String.format("0"+"%d:%02d:%02d", mCalendar.get(Calendar.HOUR),
                                mCalendar.get(Calendar.MINUTE), mCalendar.get(Calendar.SECOND));
                        canvas.drawText(text, mXOffset, mYOffset, mTextPaint);
                        //   Log.d(TAG, "======================== 0");
            }


            /*
            //FOR 24 FORMAT TIME
            String text = mAmbient
                    ? String.format("%d:%02d", mCalendar.get(Calendar.HOUR_OF_DAY),
                    mCalendar.get(Calendar.MINUTE))
                    : String.format("%d:%02d:%02d", mCalendar.get(Calendar.HOUR_OF_DAY),
                    mCalendar.get(Calendar.MINUTE), mCalendar.get(Calendar.SECOND));
            canvas.drawText(text, mXOffset, mYOffset, mTextPaint);
            //   Log.d(TAG, "======================== 0");
            */

            /*
            int ten = mCalendar.get(Calendar.HOUR);
            if(12 <= ten)
            {
                String text = mAmbient
                        ? String.format("0" + "%d:%02d", mCalendar.get(Calendar.HOUR),
                        mCalendar.get(Calendar.MINUTE))
                        : String.format("0" + "%d:%02d:%02d", mCalendar.get(Calendar.HOUR),
                        mCalendar.get(Calendar.MINUTE), mCalendar.get(Calendar.SECOND));
                canvas.drawText(text, mXOffset, mYOffset, mTextPaint);
                //   Log.d(TAG, "======================== 00");
            }else{

                String text = mAmbient
                        ? String.format("%d:%02d", mCalendar.get(Calendar.HOUR),
                        mCalendar.get(Calendar.MINUTE))
                        : String.format("%d:%02d:%02d", mCalendar.get(Calendar.HOUR),
                        mCalendar.get(Calendar.MINUTE), mCalendar.get(Calendar.SECOND));
                canvas.drawText(text, mXOffset, mYOffset, mTextPaint);
                //   Log.d(TAG, "======================== 0");

            }
            */

         //   Log.d(TAG, "========================"+rTime);


        }

        /**
         * Starts the {@link #mUpdateTimeHandler} timer if it should be running and isn't currently
         * or stops it if it shouldn't be running but currently is.
         */
        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }

        /**
         * Returns whether the {@link #mUpdateTimeHandler} timer should be running. The timer should
         * only run when we're visible and in interactive mode.
         */
        private boolean shouldTimerBeRunning() {
            return isVisible() && !isInAmbientMode();
        }

        /**
         * Handle updating the time periodically in interactive mode.
         */
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
