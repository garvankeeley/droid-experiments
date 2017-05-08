package com.andrognito.patternlockdemo;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.andrognito.patternlockview.utils.PatternLockUtils;
import com.andrognito.patternlockview.utils.ResourceUtils;
import com.andrognito.rxpatternlockview.RxPatternLockView;
import com.andrognito.rxpatternlockview.events.PatternLockCompleteEvent;
import com.andrognito.rxpatternlockview.events.PatternLockCompoundEvent;

import org.w3c.dom.Text;

import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.functions.Consumer;

public class PatternLockActivity extends AppCompatActivity {

    public interface PatternLockListener {
        void patternLockCreated(String patternCode);
        void patternUnlockValidated();
        void patternUnlockTooManyAttempts();
    }

    private static final int MAX_ATTEMPTS = 4;
    private static final int MIN_PATTERN_LENGTH = 3;
    private PatternLockView mPatternLockView;
    private TextView titleView;
    private String unlockCode;
    private Boolean isCreateMode = true;
    private final int SHORT_DELAY = 300;
    private final int LONG_DELAY = 500;
    public PatternLockListener listener;

    private PatternLockViewListener mPatternLockViewListener = new PatternLockViewListener() {
        @Override public void onStarted() {}
        @Override public void onProgress(List<PatternLockView.Dot> progressPattern) {}
        @Override public void onCleared() {}

        private void clearAndSetTitleAfterDelay(final long delayMs, final String title,
                                                final Runnable completion) {
            (new Handler()).postDelayed(new Runnable() {
                public void run() {
                    mPatternLockView.clearPattern();
                    if (!TextUtils.isEmpty(title)) {
                        titleView.setText(title);
                    }
                    if (completion != null) {
                        completion.run();
                    }
                }
            }, delayMs);

        }

        private void flashToIndicateError() {
            int colorFrom = 0x70FFFFFF;
            int colorTo = 0;
            ObjectAnimator.ofObject(mPatternLockView, "backgroundColor", new ArgbEvaluator(), colorFrom, colorTo)
                    .setDuration(600)
                    .start();
        }

        private void blockInteractionBriefly() {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            mPatternLockView.setAlpha(0.3f);

            (new Handler()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    mPatternLockView.setAlpha(1.0f);
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                }
            }, 3000);
        }

        private void onCompleteUnlockMode(String patternString) {
            SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("", Context.MODE_PRIVATE);

            int incorrectPatternCount = 0;

            if (unlockCode.equals(patternString)) {
                titleView.setText("");
                clearAndSetTitleAfterDelay(SHORT_DELAY, null, new Runnable() {
                    @Override public void run() {
                        if (listener != null) {
                            listener.patternUnlockValidated();
                        }
                    }
                });
            } else {
                incorrectPatternCount = sharedPref.getInt("incorrect", 0) + 1;
                String toastMessage = "Incorrect pattern";
                if (incorrectPatternCount == MAX_ATTEMPTS - 1) {
                    titleView.setText("Last Unlock Attempt");
                    toastMessage = "NEXT ATTEMPT WILL RESET THE APP";
                    blockInteractionBriefly();
                } else if (incorrectPatternCount >= MAX_ATTEMPTS) {
                    toastMessage = "Too many attempts";
                    if (listener != null) {
                        listener.patternUnlockTooManyAttempts();
                    }
                }

                flashToIndicateError();
                Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_SHORT).show();
                mPatternLockView.clearPattern();
            }

            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt("incorrect", incorrectPatternCount);
            editor.commit(); // foreground/immediate write
        }

        private void onCompleteCreateMode(String patternString) {
            if (TextUtils.isEmpty(unlockCode)) {
                unlockCode = patternString;
                clearAndSetTitleAfterDelay(SHORT_DELAY, "Repeat Unlock Code", null);
                return;
            }

            if (unlockCode.equals(patternString)) {
                unlockCode = patternString;
                titleView.setText("Unlock Code Set");
                clearAndSetTitleAfterDelay(SHORT_DELAY, null, new Runnable() {
                    @Override public void run() {
                        if (listener != null) {
                            listener.patternLockCreated(unlockCode);
                        }
                    }
                });
            } else {
                unlockCode = null;
                titleView.setText("");
                Toast.makeText(getApplicationContext(), "Pattern did not match", Toast.LENGTH_SHORT).show();
                flashToIndicateError();
                clearAndSetTitleAfterDelay(LONG_DELAY, "Create Unlock Code", null);
            }
        }

        @Override
        public void onComplete(List<PatternLockView.Dot> pattern) {
            String patternString = PatternLockUtils.patternToString(mPatternLockView, pattern);
            Log.d(getClass().getName(), "Pattern complete: " + patternString);

            if (patternString.length() < MIN_PATTERN_LENGTH) {
                mPatternLockView.clearPattern();
                return;
            }

            if (!isCreateMode) {
                onCompleteUnlockMode(patternString);
            } else {
                onCompleteCreateMode(patternString);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        unlockCode = "012";
        isCreateMode = TextUtils.isEmpty(unlockCode);

        titleView = (TextView) findViewById(R.id.unlock_title);

        mPatternLockView = (PatternLockView) findViewById(R.id.patter_lock_view);
        mPatternLockView.setDotCount(3);
        mPatternLockView.setDotNormalSize((int) ResourceUtils.getDimensionInPx(this, R.dimen.pattern_lock_dot_size));
        mPatternLockView.setDotSelectedSize((int) ResourceUtils.getDimensionInPx(this, R.dimen.pattern_lock_dot_selected_size));
        mPatternLockView.setPathWidth((int) ResourceUtils.getDimensionInPx(this, R.dimen.pattern_lock_path_width));
        mPatternLockView.setAspectRatioEnabled(true);
        mPatternLockView.setAspectRatio(PatternLockView.AspectRatio.ASPECT_RATIO_HEIGHT_BIAS);
        mPatternLockView.setViewMode(PatternLockView.PatternViewMode.CORRECT);
        mPatternLockView.setDotAnimationDuration(150);
        mPatternLockView.setPathEndAnimationDuration(100);
        mPatternLockView.setCorrectStateColor(ResourceUtils.getColor(this, R.color.white));
        mPatternLockView.setInStealthMode(false);
        mPatternLockView.setTactileFeedbackEnabled(true);
        mPatternLockView.setInputEnabled(true);
        mPatternLockView.addPatternLockListener(mPatternLockViewListener);

        RxPatternLockView.patternComplete(mPatternLockView)
                .subscribe(new Consumer<PatternLockCompleteEvent>() {
                    @Override
                    public void accept(PatternLockCompleteEvent patternLockCompleteEvent) throws Exception {
                        Log.d(getClass().getName(), "Complete: " + patternLockCompleteEvent.getPattern().toString());
                    }
                });

        RxPatternLockView.patternChanges(mPatternLockView)
                .subscribe(new Consumer<PatternLockCompoundEvent>() {
                    @Override
                    public void accept(PatternLockCompoundEvent event) throws Exception {
                        if (event.getEventType() == PatternLockCompoundEvent.EventType.PATTERN_STARTED) {
                            Log.d(getClass().getName(), "Pattern drawing started");
                        } else if (event.getEventType() == PatternLockCompoundEvent.EventType.PATTERN_PROGRESS) {
                            Log.d(getClass().getName(), "Pattern progress: " +
                                    PatternLockUtils.patternToString(mPatternLockView, event.getPattern()));
                        } else if (event.getEventType() == PatternLockCompoundEvent.EventType.PATTERN_COMPLETE) {
                            Log.d(getClass().getName(), "Pattern complete: " +
                                    PatternLockUtils.patternToString(mPatternLockView, event.getPattern()));
                        } else if (event.getEventType() == PatternLockCompoundEvent.EventType.PATTERN_CLEARED) {
                            Log.d(getClass().getName(), "Pattern has been cleared");
                        }
                    }
                });

        if (!isCreateMode) {
            titleView.setText("Enter Unlock Code");
        }
    }
}
