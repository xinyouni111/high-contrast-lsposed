package com.highcontrast.lsposed;

public class ColorPreset {
    public final String id;
    public final String name;
    public final int textColor;
    public final int strokeColor;

    private ColorPreset(String id, String name, int textColor, int strokeColor) {
        this.id = id;
        this.name = name;
        this.textColor = textColor;
        this.strokeColor = strokeColor;
    }

    public static final ColorPreset[] PRESETS = {
        new ColorPreset("light",     "Light (Black + White Stroke)", 0xFF000000, 0xFFFFFFFF),
        new ColorPreset("dark",      "Dark (White + Black Stroke)",  0xFFFFFFFF, 0xFF000000),
        new ColorPreset("yellow_bk", "Yellow + Black Stroke",         0xFFFFFF00, 0xFF000000),
        new ColorPreset("green_bk",  "Green + Black Stroke",          0xFF00FF00, 0xFF000000),
        new ColorPreset("red_wh",    "Red + White Stroke",            0xFFFF0000, 0xFFFFFFFF),
        new ColorPreset("blue_wh",   "Blue + White Stroke",           0xFF0088FF, 0xFFFFFFFF),
    };

    public static ColorPreset getById(String id) {
        for (ColorPreset p : PRESETS) {
            if (p.id.equals(id)) return p;
        }
        return PRESETS[0];
    }

    public static String[] getIds() {
        String[] ids = new String[PRESETS.length];
        for (int i = 0; i < PRESETS.length; i++) ids[i] = PRESETS[i].id;
        return ids;
    }

    public static String[] getNames() {
        String[] names = new String[PRESETS.length];
        for (int i = 0; i < PRESETS.length; i++) names[i] = PRESETS[i].name;
        return names;
    }
}
