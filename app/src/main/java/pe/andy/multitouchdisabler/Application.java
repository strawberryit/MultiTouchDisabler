package pe.andy.multitouchdisabler;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static android.view.KeyEvent.KEYCODE_PAGE_DOWN;
import static android.view.KeyEvent.KEYCODE_PAGE_UP;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class Application implements IXposedHookLoadPackage, IXposedHookZygoteInit, IXposedHookInitPackageResources {

    public static String MODULE_PATH;

    @Override
    public void initZygote(StartupParam startupParam) {
        MODULE_PATH = startupParam.modulePath;
    }

    @Override
    public void handleInitPackageResources(InitPackageResourcesParam resparam) {
        PaperLauncherButton.INSTANCE.modRes(resparam);
    }

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

                findAndHookMethod(
                        clazz,
                        "dispatchKeyEvent",
                        KeyEvent.class,
                        ignoreKeyRepeat);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        try {
            final String packageName = "com.ridi.paper";
            if (TextUtils.equals(lpparam.packageName, packageName)) {
                findAndHookMethod(
                        "com.ridi.books.viewer.reader.activity.ReaderActivity",
                        lpparam.classLoader,
                        "dispatchTouchEvent",
                        MotionEvent.class,
                        ignoreMultitouch);

                findAndHookMethod(
                        "com.ridi.books.viewer.reader.activity.ReaderActivity",
                        lpparam.classLoader,
                        "dispatchKeyEvent",
                        KeyEvent.class,
                        ignoreKeyRepeat);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        try {
            PaperLauncherButton.INSTANCE.modCode(lpparam);
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
                if (((int) ev.getY()) == 910 || ev.getPointerCount() > 1) {
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

    final XC_MethodHook ignoreKeyRepeat = new XC_MethodHook() {
        // Method signature
        // public boolean dispatchKeyEvent(KeyEvent event)

        @Override
        protected void beforeHookedMethod(MethodHookParam param) {
            try {
                KeyEvent event = (KeyEvent) param.args[0];

                if (isLeftRightKeyRepeat(event)) {
                    // handled the event
                    param.setResult(true);
                }
            }
            catch (Exception ex) {
                Log.d("MultiTouchDisabler", "dispatchKeyEvent hooking failed.");
                ex.printStackTrace();
            }
        }

        boolean isLeftRightKeyRepeat(KeyEvent event) {
            return (event.getKeyCode() == KEYCODE_PAGE_UP ||   // LEFT
                    event.getKeyCode() == KEYCODE_PAGE_DOWN)  // RIGHT
                && event.getRepeatCount() > 0;
        }
    };
}
