package com.highcontrast.lsposed;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import java.io.File;
import java.io.FileWriter;

public class PrefsManager {
    private static final String TAG = "HighContrastLSP";
    private static final String PREFS_NAME = "high_contrast_prefs";
    private static final String KEY_ENABLED = "enabled";
    private static final String KEY_PRESET = "preset";
    private static final String KEY_STROKE_WIDTH = "stroke_width";

    public static final String CONFIG_PATH = "/data/local/tmp/high_contrast_config.txt";

    private final SharedPreferences prefs;
    private final Context context;

    public PrefsManager(Context context) {
        this.context = context;
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
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
        int w = prefs.getInt(KEY_STROKE_WIDTH, 2);
        return Math.max(1, Math.min(6, w));
    }

    public void setStrokeWidth(int width) {
        prefs.edit().putInt(KEY_STROKE_WIDTH, Math.max(1, Math.min(6, width))).apply();
    }

    public void exportConfig() {
        String content = "enabled=" + isEnabled() + "\n"
            + "preset=" + getPresetId() + "\n"
            + "stroke_width=" + getStrokeWidth() + "\n";

        try {
            FileWriter fw = new FileWriter(CONFIG_PATH);
            fw.write(content);
            fw.close();
            new File(CONFIG_PATH).setReadable(true, false);
        } catch (Throwable t) {
            Log.w(TAG, "Cannot write to /data/local/tmp/, trying internal", t);
            try {
                File f = new File(context.getFilesDir(), "config.txt");
                FileWriter fw = new FileWriter(f);
                fw.write(content);
                fw.close();
                f.setReadable(true, false);
            } catch (Throwable t2) {
                Log.e(TAG, "Config export completely failed", t2);
            }
        }
    }
}
