package com.highcontrast.lsposed;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

public class SettingsActivity extends Activity {

    private static final String TAG = "HighContrastLSP";
    private PrefsManager prefs;
    private Switch switchEnabled;
    private Spinner spinnerPreset;
    private SeekBar seekBarStroke;
    private TextView tvStrokeValue;
    private TextView tvPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            safeOnCreate(savedInstanceState);
        } catch (Throwable t) {
            Log.e(TAG, "SettingsActivity crash", t);
            setContentView(android.R.layout.simple_list_item_1);
            TextView tv = new TextView(this);
            tv.setText("High Contrast Text\n\nError loading settings.\nTry reinstalling the module.");
            tv.setTextSize(16);
            tv.setPadding(32, 32, 32, 32);
            setContentView(tv);
        }
    }

    private void safeOnCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_settings);

        prefs = new PrefsManager(this);

        switchEnabled = (Switch) findViewById(R.id.switch_enabled);
        spinnerPreset = (Spinner) findViewById(R.id.spinner_preset);
        seekBarStroke = (SeekBar) findViewById(R.id.seekbar_stroke);
        tvStrokeValue = (TextView) findViewById(R.id.tv_stroke_value);
        tvPreview = (TextView) findViewById(R.id.tv_preview);

        if (switchEnabled != null) {
            switchEnabled.setChecked(prefs.isEnabled());
            switchEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton button, boolean checked) {
                    prefs.setEnabled(checked);
                    updatePreview();
                }
            });
        }

        if (spinnerPreset != null) {
            String[] names = ColorPreset.getNames();
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, names);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerPreset.setAdapter(adapter);

            String currentId = prefs.getPresetId();
            String[] ids = ColorPreset.getIds();
            for (int i = 0; i < ids.length; i++) {
                if (ids[i].equals(currentId)) {
                    spinnerPreset.setSelection(i);
                    break;
                }
            }

            spinnerPreset.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                    prefs.setPresetId(ColorPreset.getIds()[pos]);
                    updatePreview();
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        }

        if (seekBarStroke != null) {
            seekBarStroke.setMax(5);
            seekBarStroke.setProgress(prefs.getStrokeWidth() - 1);
            seekBarStroke.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    int width = progress + 1;
                    if (tvStrokeValue != null) {
                        tvStrokeValue.setText(width + "px");
                    }
                    if (fromUser) {
                        prefs.setStrokeWidth(width);
                        updatePreview();
                    }
                }
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });
        }

        if (tvStrokeValue != null) {
            tvStrokeValue.setText(prefs.getStrokeWidth() + "px");
        }

        updatePreview();
        prefs.exportConfig();
    }

    private void updatePreview() {
        if (tvPreview == null) return;
        try {
            if (!prefs.isEnabled()) {
                tvPreview.setText("Status: OFF");
                return;
            }
            ColorPreset preset = ColorPreset.getById(prefs.getPresetId());
            tvPreview.setText("ON | " + preset.name
                + " | Stroke: " + prefs.getStrokeWidth() + "px");
        } catch (Throwable t) {
            tvPreview.setText("Status: Error");
        }
    }
}
