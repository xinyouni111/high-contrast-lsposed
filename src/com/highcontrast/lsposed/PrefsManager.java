package com.highcontrast.lsposed;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefsManager {
    private static final String PREFS_NAME = "high_contrast_prefs";
    private static final String KEY_ENABLED = "enabled";
    private static final String KEY_PRESET = "preset";
    private static final String KEY_STROKE_WIDTH = "stroke_width";
    private static final String KEY_CUSTOM_TEXT_COLOR = "custom_text_color";
    private static final String KEY_CUSTOM_STROKE_COLOR = "custom_stroke_color";

    private final SharedPreferences prefs;

    public PrefsManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_READABLE);
    }

    public boolean isEnabled() {
        return prefs.getBoolean(KEY_ENABLED, true);
    }

    public void setEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_ENABLED, enabled).apply();
    }

    public String getPresetId() {
        return prefs.getString(KEY_PRESET, "light");
    }

    public void setPresetId(String id) {
        prefs.edit().putString(KEY_PRESET, id).apply();
    }

    public int getStrokeWidth() {
        return prefs.getInt(KEY_STROKE_WIDTH, 2);
    }

    public void setStrokeWidth(int width) {
        prefs.edit().putInt(KEY_STROKE_WIDTH, Math.max(1, Math.min(6, width))).apply();
    }

    public int getCustomTextColor() {
        return prefs.getInt(KEY_CUSTOM_TEXT_COLOR, 0xFF000000);
    }

    public int getCustomStrokeColor() {
        return prefs.getInt(KEY_CUSTOM_STROKE_COLOR, 0xFFFFFFFF);
    }

    public void setCustomColors(int textColor, int strokeColor) {
        prefs.edit()
            .putInt(KEY_CUSTOM_TEXT_COLOR, textColor)
            .putInt(KEY_CUSTOM_STROKE_COLOR, strokeColor)
            .apply();
    }
}
