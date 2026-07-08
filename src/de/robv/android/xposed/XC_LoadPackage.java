package de.robv.android.xposed;

import android.content.pm.ApplicationInfo;
import de.robv.android.xposed.callbacks.XCallback;

public final class XC_LoadPackage extends XCallback {

    public static final class LoadPackageParam extends XCallback.Param {
        public String packageName;
        public ClassLoader classLoader;
        public ApplicationInfo appInfo;
        public boolean isFirstApplication;
    }

    @Override
    protected void call(Param param) throws Throwable {}
}
