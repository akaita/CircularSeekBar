package com.akaita.android.circularseekbar.sample;

import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.akaita.android.circularseekbar.CircularSeekBar;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CircularSeekBar seekBar = (CircularSeekBar) findViewById(R.id.seekbar);
        seekBar.setProgressTextFormat(new DecimalFormat("###,###,###,##0.00"));
        seekBar.setProgress(0);
        seekBar.setRingColor(Color.GREEN);

        seekBar.setOnCenterClickedListener(new CircularSeekBar.OnCenterClickedListener() {
            @Override
            public void onCenterClicked(CircularSeekBar seekBar, float progress) {
                Snackbar.make(seekBar, "Reset", Snackbar.LENGTH_SHORT).show();
                seekBar.setProgress(0);
            }
        });
        seekBar.setOnCircularSeekBarChangeListener(new CircularSeekBar.OnCircularSeekBarChangeListener() {
            @Override
            public void onProgressChanged(CircularSeekBar seekBar, float progress, boolean fromUser) {
                if (progress < 20) {
                    seekBar.setRingColor(Color.GREEN);
                } else if (progress < 40) {
                    seekBar.setRingColor(Color.YELLOW);
                } else {
                    seekBar.setRingColor(Color.RED);
                }
            }

            @Override
            public void onStartTrackingTouch(CircularSeekBar seekBar) {
                // Nothing
            }

            @Override
            public void onStopTrackingTouch(CircularSeekBar seekBar) {
                // Nothing
            }
        });
    }
}
