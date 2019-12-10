package pe.andy.multitouchdisabler;

import android.app.Activity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class Application implements IXposedHookLoadPackage {

	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
		
		try {
			XposedHelpers.findAndHookMethod(
						Activity.class,
						"dispatchTouchEvent",
						MotionEvent.class,
						ignoreMultitouch);
			
			XposedHelpers.findAndHookMethod(
					View.class,
					"dispatchTouchEvent",
					MotionEvent.class,
					ignoreMultitouch);
			
			XposedHelpers.findAndHookMethod(
					"com.ridi.books.viewer.reader.activity.b",
					lpparam.classLoader,
					"dispatchTouchEvent",
					MotionEvent.class,
					ignoreMultitouch);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
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
				Log.d("Andy", "dispatchTouchEvent hooking failed.");
				ex.printStackTrace();
			}
			super.beforeHookedMethod(param);
		}
	};

}
