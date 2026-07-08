package com.highcontrast.lsposed;

import android.graphics.Canvas;
import android.graphics.Paint;

import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class HighContrastHook {

    private static volatile boolean sEnabled = true;
    private static int sTextColor = 0xFF000000;
    private static int sStrokeColor = 0xFFFFFFFF;
    private static float sStrokeWidth = 2f;

    public static void updateConfig(boolean enabled, int textColor, int strokeColor, float strokeWidth) {
        sEnabled = enabled;
        sTextColor = textColor;
        sStrokeColor = strokeColor;
        sStrokeWidth = strokeWidth;
    }

    public static void hookCanvasDrawText(ClassLoader classLoader) {
        try {
            Class<?> canvasClass = Canvas.class;

            XposedHelpers.findAndHookMethod(canvasClass, "drawText",
                String.class, float.class, float.class, Paint.class,
                new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                        return drawTextWithStroke(param);
                    }
                });

            XposedHelpers.findAndHookMethod(canvasClass, "drawText",
                CharSequence.class, int.class, int.class, float.class, float.class, Paint.class,
                new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                        return drawTextWithStroke(param);
                    }
                });

            XposedHelpers.findAndHookMethod(canvasClass, "drawText",
                char[].class, int.class, int.class, float.class, float.class, Paint.class,
                new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                        return drawTextWithStroke(param);
                    }
                });

        } catch (Throwable t) {
            android.util.Log.e("HighContrastLSP", "Failed to hook Canvas.drawText", t);
        }
    }

    private static Object drawTextWithStroke(XC_MethodReplacement.MethodHookParam param) throws Throwable {
        if (!sEnabled) {
            return XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);
        }

        Paint paint = (Paint) param.args[param.args.length - 1];
        Canvas canvas = (Canvas) param.thisObject;

        if (paint == null || canvas == null) {
            return XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);
        }

        Paint.Style savedStyle = paint.getStyle();
        float savedStrokeWidth = paint.getStrokeWidth();
        int savedColor = paint.getColor();
        float savedAlpha = (float) paint.getAlpha();

        if (savedAlpha < 10) {
            return XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);
        }

        Object result = null;

        try {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(sStrokeWidth);
            paint.setColor(sStrokeColor);
            paint.setAlpha((int) savedAlpha);

            result = XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(sTextColor);
            paint.setAlpha((int) savedAlpha);

            XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);

        } finally {
            paint.setStyle(savedStyle);
            paint.setStrokeWidth(savedStrokeWidth);
            paint.setColor(savedColor);
            paint.setAlpha((int) savedAlpha);
        }

        return result;
    }
}
