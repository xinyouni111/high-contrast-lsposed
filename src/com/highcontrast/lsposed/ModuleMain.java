package com.highcontrast.lsposed;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_LoadPackage;

public class ModuleMain implements IXposedHookLoadPackage {

    private static volatile boolean sEnabled = true;
    private static volatile int sTextColor = 0xFF000000;
    private static volatile int sStrokeColor = 0xFFFFFFFF;
    private static volatile float sStrokeWidth = 2f;
    private static String sPresetId = "light";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (lpparam.packageName.equals("android")) {
            loadConfigFromFile(lpparam);
        }

        if (lpparam.packageName.equals("com.highcontrast.lsposed")) {
            return;
        }

        applyConfig();
        HighContrastHook.hookCanvasDrawText(lpparam.classLoader);
    }

    private static void loadConfigFromFile(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            java.io.File prefsFile = new java.io.File(
                "/data/data/com.highcontrast.lsposed/shared_prefs/high_contrast_prefs.xml");
            if (!prefsFile.exists()) return;

            String content = readFileContent(prefsFile);
            if (content == null) return;

            sEnabled = !content.contains("name=\"enabled\" value=\"false\"");
            sStrokeWidth = extractInt(content, "stroke_width", 2);

            sPresetId = extractString(content, "preset", "light");
            ColorPreset preset = ColorPreset.getById(sPresetId);
            sTextColor = preset.textColor;
            sStrokeColor = preset.strokeColor;

        } catch (Throwable t) {
            android.util.Log.e("HighContrastLSP", "Config load error", t);
        }
    }

    private static String readFileContent(java.io.File file) {
        try {
            java.io.BufferedReader br = new java.io.BufferedReader(
                new java.io.FileReader(file));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            br.close();
            return sb.toString();
        } catch (Throwable t) {
            return null;
        }
    }

    private static int extractInt(String xml, String key, int defaultVal) {
        String search = "name=\"" + key + "\" value=\"";
        int idx = xml.indexOf(search);
        if (idx < 0) return defaultVal;
        int start = idx + search.length();
        int end = xml.indexOf("\"", start);
        if (end < 0) return defaultVal;
        try {
            return Integer.parseInt(xml.substring(start, end));
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    private static String extractString(String xml, String key, String defaultVal) {
        String search = "name=\"" + key + "\">";
        int idx = xml.indexOf(search);
        if (idx < 0) {
            search = "name=\"" + key + "\" value=\"";
            idx = xml.indexOf(search);
            if (idx < 0) return defaultVal;
            int start = idx + search.length();
            int end = xml.indexOf("\"", start);
            return (end > start) ? xml.substring(start, end) : defaultVal;
        }
        int start = idx + search.length();
        int end = xml.indexOf("<", start);
        return (end > start) ? xml.substring(start, end) : defaultVal;
    }

    public static void updateConfig(String presetId, int strokeWidth) {
        sPresetId = presetId;
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
