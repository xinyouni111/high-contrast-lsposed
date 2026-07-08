package com.highcontrast.lsposed;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_LoadPackage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class ModuleMain implements IXposedHookLoadPackage {

    private static volatile boolean sEnabled = true;
    private static volatile int sTextColor = 0xFF000000;
    private static volatile int sStrokeColor = 0xFFFFFFFF;
    private static volatile float sStrokeWidth = 2f;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (lpparam.packageName.equals("com.highcontrast.lsposed")) {
            return;
        }

        loadConfigFromFile();
        applyConfig();
        HighContrastHook.hookCanvasDrawText(lpparam.classLoader);

        new Thread(new Runnable() {
            @Override
            public void run() {
                long lastModified = 0;
                File configFile = new File(PrefsManager.CONFIG_PATH);
                while (true) {
                    try {
                        Thread.sleep(3000);
                        long mod = configFile.lastModified();
                        if (mod > lastModified) {
                            lastModified = mod;
                            loadConfigFromFile();
                            applyConfig();
                        }
                    } catch (Throwable ignored) {}
                }
            }
        }).start();
    }

    private static void loadConfigFromFile() {
        try {
            File configFile = new File(PrefsManager.CONFIG_PATH);
            if (!configFile.exists()) return;

            BufferedReader br = new BufferedReader(new FileReader(configFile));
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
                    sStrokeWidth = (float) Integer.parseInt(line.substring(13));
                }
            }
            br.close();
        } catch (Throwable t) {
            android.util.Log.e("HighContrastLSP", "Config load error", t);
        }
    }

    public static void updateConfig(String presetId, int strokeWidth) {
        sStrokeWidth = (float) strokeWidth;
        ColorPreset preset = ColorPreset.getById(presetId);
        sTextColor = preset.textColor;
        sStrokeColor = preset.strokeColor;
        applyConfig();
    }

    public static void setEnabled(boolean enabled) {
        sEnabled = enabled;
        applyConfig();
    }

    private static void applyConfig() {
        HighContrastHook.updateConfig(sEnabled, sTextColor, sStrokeColor, sStrokeWidth);
    }
}
