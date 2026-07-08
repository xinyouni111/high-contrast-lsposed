package de.robv.android.xposed;

import de.robv.android.xposed.callbacks.XCallback;

public abstract class XC_MethodHook extends XCallback {

    @Override
    protected void call(Param param) throws Throwable {
        MethodHookParam mhp = (MethodHookParam) param;
        beforeHookedMethod(mhp);
        afterHookedMethod(mhp);
    }

    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {}
    protected void afterHookedMethod(MethodHookParam param) throws Throwable {}

    public static class MethodHookParam extends XCallback.Param {
        public Object thisObject;
        public java.lang.reflect.Member method;
    }

    public static final class Unhook {
        private final Object method;
        public Unhook(Object method) { this.method = method; }
        public Object getHookedMethod() { return method; }
    }
}
