package de.robv.android.xposed.callbacks;

public abstract class XCallback {

    protected abstract void call(Param param) throws Throwable;

    public static abstract class Param {
        public final Object[] args = new Object[0];

        public Object getResult() { return null; }
        public void setResult(Object result) {}
        public Throwable getThrowable() { return null; }
        public void setThrowable(Throwable t) {}
        public boolean hasThrowable() { return false; }
    }
}
