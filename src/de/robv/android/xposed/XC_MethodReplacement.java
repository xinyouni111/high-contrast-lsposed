package de.robv.android.xposed;

public abstract class XC_MethodReplacement extends XC_MethodHook {

    @Override
    protected final void afterHookedMethod(MethodHookParam param) throws Throwable {}

    @Override
    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
        Object result = replaceHookedMethod(param);
        param.setResult(result);
    }

    protected abstract Object replaceHookedMethod(MethodHookParam param) throws Throwable;
}
