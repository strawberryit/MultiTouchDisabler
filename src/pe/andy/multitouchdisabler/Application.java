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
		
		XposedHelpers.findAndHookMethod(
					Activity.class,
					"dispatchTouchEvent",
					MotionEvent.class,
					new XC_MethodHook() {
						@Override
						protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
							try {
								MotionEvent ev = (MotionEvent) param.args[0];
								if (ev.getPointerCount() > 1 || ((int) ev.getY()) == 910) {
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
					});
		
		XposedHelpers.findAndHookMethod(
				View.class,
				"dispatchTouchEvent",
				MotionEvent.class,
				new XC_MethodHook() {
					@Override
					protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
						try {
							MotionEvent ev = (MotionEvent) param.args[0];
							if (ev.getPointerCount() > 1 || ((int) ev.getY()) == 910) {
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
				});

	}

}
