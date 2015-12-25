package com.jackie.lockpattern;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends Activity implements LockPatternView.OnPatternChangeListener {
    private TextView mLockPatternHint;
    private LockPatternView mLockPatternView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLockPatternHint = (TextView) findViewById(R.id.lock_pattern_hint);
        mLockPatternView = (LockPatternView) findViewById(R.id.lock_pattern_view);
        mLockPatternView.setOnPatternChangeListener(this);

    }

    @Override
    public void onPatternChange(String patternPassword) {
        if (patternPassword == null) {
            mLockPatternHint.setText("至少5个点");
        } else {
            mLockPatternHint.setText(patternPassword);
        }
    }

    @Override
    public void onPatternStarted(boolean isStarted) {
        if (isStarted) {
            mLockPatternHint.setText("请绘制图案");
        }
    }
}
