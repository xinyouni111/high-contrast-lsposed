package com.highcontrast.lsposed;

import android.util.Log;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_LoadPackage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class ModuleMain implements IXposedHookLoadPackage {

    private static final String TAG = "HighContrastLSP";
    private static volatile boolean sEnabled = true;
    private static volatile int sTextColor = 0xFF000000;
    private static volatile int sStrokeColor = 0xFFFFFFFF;
    private static volatile float sStrokeWidth = 2f;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (lpparam.packageName.equals("com.highcontrast.lsposed")) {
            return;
        }

        try {
            loadConfig();
        } catch (Throwable t) {
            Log.w(TAG, "Config load failed, using defaults", t);
        }

        applyConfig();

        try {
            HighContrastHook.hookCanvasDrawText(lpparam.classLoader);
        } catch (Throwable t) {
            Log.e(TAG, "Hook failed in " + lpparam.packageName, t);
        }

        startConfigWatcher();
    }

    private void loadConfig() {
        File configFile = findConfigFile();
        if (configFile == null) return;

        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(configFile));
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("enabled=")) {
                    sEnabled = "true".equals(line.substring(8));
                } else if (line.startsWith("preset=")) {
                    String presetId = line.substring(7);
                    ColorPreset preset = ColorPreset.getById(presetId);
                    sTextColor = preset.textColor;
                    sStrokeColor = preset.strokeColor;
                } else if (line.startsWith("stroke_width=")) {
                    sStrokeWidth = Float.parseFloat(line.substring(13));
                }
            }
        } catch (Throwable t) {
            Log.w(TAG, "Parse config error", t);
        } finally {
            if (br != null) {
                try { br.close(); } catch (Throwable ignored) {}
            }
        }
    }

    private File findConfigFile() {
        File f = new File(PrefsManager.CONFIG_PATH);
        if (f.exists() && f.canRead()) return f;

        f = new File("/data/data/com.highcontrast.lsposed/files/config.txt");
        if (f.exists() && f.canRead()) return f;

        return null;
    }

    private void startConfigWatcher() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                long lastModified = 0;
                while (true) {
                    try {
                        Thread.sleep(3000);
                        File f = findConfigFile();
                        if (f == null) continue;
                        long mod = f.lastModified();
                        if (mod > lastModified) {
                            lastModified = mod;
                            loadConfig();
                            applyConfig();
                        }
                    } catch (Throwable ignored) {}
                }
            }
        }, "HC-ConfigWatcher").start();
    }

    private void applyConfig() {
        HighContrastHook.updateConfig(sEnabled, sTextColor, sStrokeColor, sStrokeWidth);
    }
}
