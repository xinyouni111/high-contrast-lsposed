package com.highcontrast.lsposed;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

public class SettingsActivity extends Activity {

    private PrefsManager prefs;
    private Switch switchEnabled;
    private Spinner spinnerPreset;
    private SeekBar seekBarStroke;
    private TextView tvStrokeValue;
    private TextView tvPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = new PrefsManager(this);

        switchEnabled = findViewById(R.id.switch_enabled);
        spinnerPreset = findViewById(R.id.spinner_preset);
        seekBarStroke = findViewById(R.id.seekbar_stroke);
        tvStrokeValue = findViewById(R.id.tv_stroke_value);
        tvPreview = findViewById(R.id.tv_preview);

        switchEnabled.setChecked(prefs.isEnabled());
        switchEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton button, boolean checked) {
                prefs.setEnabled(checked);
                ModuleMain.setEnabled(checked);
                updatePreview();
            }
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, ColorPreset.getNames());
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
                String presetId = ColorPreset.getIds()[pos];
                prefs.setPresetId(presetId);
                ModuleMain.updateConfig(presetId, prefs.getStrokeWidth());
                updatePreview();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        seekBarStroke.setMax(5);
        seekBarStroke.setProgress(prefs.getStrokeWidth() - 1);
        tvStrokeValue.setText(prefs.getStrokeWidth() + "px");

        seekBarStroke.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int width = progress + 1;
                tvStrokeValue.setText(width + "px");
                if (fromUser) {
                    prefs.setStrokeWidth(width);
                    ModuleMain.updateConfig(prefs.getPresetId(), width);
                    updatePreview();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        updatePreview();
    }

    private void updatePreview() {
        if (!prefs.isEnabled()) {
            tvPreview.setText("High Contrast: OFF");
            return;
        }
        ColorPreset preset = ColorPreset.getById(prefs.getPresetId());
        String textHex = Integer.toHexString(preset.textColor).substring(2).toUpperCase();
        String strokeHex = Integer.toHexString(preset.strokeColor).substring(2).toUpperCase();
        tvPreview.setText("ON | Preset: " + preset.name
            + "\nText: #" + textHex
            + "  Stroke: #" + strokeHex
            + "  Width: " + prefs.getStrokeWidth() + "px");
    }
}
