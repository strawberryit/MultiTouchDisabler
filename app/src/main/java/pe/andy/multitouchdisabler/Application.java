package pe.andy.multitouchdisabler;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class Application implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {

        Log.d("MultiTouchDisabler", "Start activation");

        try {
            Class<?>[] classes = {Activity.class, View.class};

            for (Class<?> clazz: classes) {
                findAndHookMethod(
                        clazz,
                        "dispatchTouchEvent",
                        MotionEvent.class,
                        ignoreMultitouch);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        try {
            final String packageName = "com.ridi.paper";
            if (TextUtils.equals(lpparam.packageName, packageName)) {
                findAndHookMethod(
                        "com.ridi.books.viewer.reader.activity.b",
                        lpparam.classLoader,
                        "dispatchTouchEvent",
                        MotionEvent.class,
                        ignoreMultitouch);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        Log.d("MultiTouchDisabler", "End activation");
    }

    final XC_MethodHook ignoreMultitouch = new XC_MethodHook() {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            try {
                MotionEvent ev = (MotionEvent) param.args[0];
                if (ev.getPointerCount() > 1) {
                    param.setResult(false);
                    return;
                }
            }
            catch (Exception ex) {
                Log.d("MultiTouchDisabler", "dispatchTouchEvent hooking failed.");
                ex.printStackTrace();
            }
            super.beforeHookedMethod(param);
        }
    };
}
