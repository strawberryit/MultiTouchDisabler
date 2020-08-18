package pe.andy.multitouchdisabler

import android.content.res.XModuleResources
import android.view.View
import android.view.View.VISIBLE
import android.widget.FrameLayout
import android.widget.Toast
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers.findAndHookMethod
import de.robv.android.xposed.XposedHelpers.findMethodExact
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import pe.andy.multitouchdisabler.Application.MODULE_PATH
import java.lang.reflect.Method


object PaperLauncherButton {
    private const val packageName = "com.ridi.paper"
    private const val launcherPackageName = "au.radsoft.appdrawer"
    private const val sendAnywherePackageName = "com.estmob.android.sendanywhere"


    lateinit var getBluetoothButton: Method

    fun modCode(lpparam: LoadPackageParam) {

        if (lpparam.packageName != packageName)
            return

        val className = "com.ridi.books.viewer.main.view.MainNavigationBarPaper"
        // getBluetoothButton method
        getBluetoothButton = findMethodExact(
                className,
                lpparam.classLoader,
                "getBluetoothButton")

        findAndHookMethod(
                className,
                lpparam.classLoader,
                "onFinishInflate",
                modBluetoothButton)
    }

    private val modBluetoothButton = object : XC_MethodHook() {
        override fun afterHookedMethod(param: MethodHookParam) {
            val thisLayout = param.thisObject as FrameLayout

            (getBluetoothButton.invoke(thisLayout) as? View)?.run {
                visibility = VISIBLE
                setOnClickListener(null)
                setOnLongClickListener(null)

                // App Drawer
                setOnClickListener {
                    val context = thisLayout.context
                    context.packageManager
                            .getLaunchIntentForPackage(launcherPackageName)
                            ?.run {
                                Toast.makeText(context, "Launch AppDrawer...", Toast.LENGTH_SHORT)
                                        .show()

                                context.startActivity(this)
                            }
                }

                // Send Anywhere
                setOnLongClickListener {
                    val context = thisLayout.context
                    context.packageManager
                            .getLaunchIntentForPackage(sendAnywherePackageName)
                            ?.run {
                                Toast.makeText(context, "Launch SendAnywhere...", Toast.LENGTH_LONG)
                                        .show()

                                context.startActivity(this)
                                true
                            }
                            ?: false
                }

            }
        }
    }

    fun modRes(resparam: InitPackageResourcesParam) {
        if (resparam.packageName != packageName)
            return

        val modRes = XModuleResources.createInstance(MODULE_PATH, resparam.res)
        resparam.res.setReplacement(packageName,
                "drawable", "main_actionbar_btn_bluetooth",
                modRes.fwd(R.drawable.paper)
        )
    }
}