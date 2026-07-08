package com.highcontrast.lsposed;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class HighContrastHook {

    private static final String TAG = "HighContrastLSP";
    private static volatile boolean sEnabled = true;
    private static int sTextColor = 0xFF000000;
    private static int sStrokeColor = 0xFFFFFFFF;
    private static float sStrokeWidth = 2f;

    private static final ThreadLocal<Boolean> sDrawing = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return false;
        }
    };

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
                new SafeMethodReplacement());

            XposedHelpers.findAndHookMethod(canvasClass, "drawText",
                CharSequence.class, int.class, int.class, float.class, float.class, Paint.class,
                new SafeMethodReplacement());

            XposedHelpers.findAndHookMethod(canvasClass, "drawText",
                char[].class, int.class, int.class, float.class, float.class, Paint.class,
                new SafeMethodReplacement());

        } catch (Throwable t) {
            Log.e(TAG, "Failed to hook Canvas.drawText", t);
        }
    }

    private static class SafeMethodReplacement extends XC_MethodReplacement {
        @Override
        protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
            try {
                return drawTextWithStroke(param);
            } catch (Throwable t) {
                return XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);
            }
        }
    }

    private static Object drawTextWithStroke(XC_MethodHook.MethodHookParam param) throws Throwable {
        if (!sEnabled || sDrawing.get()) {
            return XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);
        }

        Paint paint;
        try {
            paint = (Paint) param.args[param.args.length - 1];
        } catch (Throwable t) {
            return XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);
        }

        Canvas canvas = (Canvas) param.thisObject;
        if (paint == null || canvas == null) {
            return XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);
        }

        Paint.Style savedStyle = paint.getStyle();
        float savedStrokeWidth = paint.getStrokeWidth();
        int savedColor = paint.getColor();
        int savedAlpha = paint.getAlpha();

        if (savedAlpha < 10) {
            return XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);
        }

        sDrawing.set(true);
        try {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(sStrokeWidth);
            paint.setColor(sStrokeColor);
            paint.setAlpha(savedAlpha);
            XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(sTextColor);
            paint.setAlpha(savedAlpha);
            XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);
        } finally {
            paint.setStyle(savedStyle);
            paint.setStrokeWidth(savedStrokeWidth);
            paint.setColor(savedColor);
            paint.setAlpha(savedAlpha);
            sDrawing.set(false);
        }

        return null;
    }
}
