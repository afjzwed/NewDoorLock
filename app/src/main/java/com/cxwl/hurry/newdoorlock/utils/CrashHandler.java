package com.cxwl.hurry.newdoorlock.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;

/**
 * Created by Administrator on 2017/6/15.
 */

public class CrashHandler {

    /**
     * Debug Log tag
     */
    public static final String TAG = "CrashHandler";
    /**
     * 是否开启日志输出,在Debug状态下开启,
     * 在Release状态下关闭以提示程序性能
     */
    public static final boolean DEBUG = true;
    /**
     * CrashHandler实例
     */
    private static CrashHandler INSTANCE;
    /**
     * 程序的Context对象
     */
    private Context mContext;
    /**
     * 使用Properties来保存设备的信息和错误堆栈信息
     */
    private Properties mDeviceCrashInfo = new Properties();
    private Map<String, String> params = new HashMap<>();
    private static final String VERSION_NAME = "versionName";
    private static final String VERSION_CODE = "versionCode";
    private static final String STACK_TRACE = "STACK_TRACE";
    /**
     * 错误报告文件的扩展名
     */
    private static final String CRASH_REPORTER_EXTENSION = ".cr";

    private static Object syncRoot = new Object();
    //自定义的处理异常类
    private static CustomExceptionHandler mCustomExceptionHandler;
    //程序默认的异常处理类，在这里用于处理子线程所出现的异常
    private static Thread.UncaughtExceptionHandler mUncaughtExceptionHandler;

    /**
     * 保证只有一个CrashHandler实例
     */
    private CrashHandler() {
    }

    /**
     * 获取CrashHandler实例 ,单例模式
     */
    public static CrashHandler getInstance() {
       /* if (INSTANCE == null) {
            INSTANCE = new CrashHandler();
        }
        return INSTANCE;*/
        // 防止多线程访问安全，这里使用了双重锁
        if (INSTANCE == null) {

            synchronized (syncRoot) {

                if (INSTANCE == null) {
                    INSTANCE = new CrashHandler();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * 初始化,注册Context对象,
     * 获取系统默认的UncaughtException处理器,
     * 设置该CrashHandler为程序的默认处理器
     *
     * @param ctx
     */
    public synchronized void init(Context ctx, CustomExceptionHandler customExceptionHandler) {
        mContext = ctx;
        mCustomExceptionHandler = customExceptionHandler;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Looper.loop();
                    } catch (Throwable e) {
                        sendErrorMsg(e);
                        if (mCustomExceptionHandler != null) {
                            mCustomExceptionHandler.handlerException(Looper.getMainLooper().getThread(), e);
                        }
                        Toast.makeText(mContext, "程序异常...", Toast.LENGTH_SHORT).show();
//                        //收集设备信息
                        collectCrashDeviceInfo(mContext);
//                        //保存错误报告文件
                        saveCrashInfoToFile(e);
//                        //发送错误报告到服务器
//                        sendCrashReportsToServer(mContext);
                    }
                }
            }
        });
        //将原先默认的处理方式保存下来，以便后期如果有需要的话恢复原状态。
        mUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                if (mCustomExceptionHandler != null) {
                    mCustomExceptionHandler.handlerException(t, e);
                }
            }
        });
    }

    /**
     * 自定义的处理异常
     */
    public interface CustomExceptionHandler {
        void handlerException(Thread thread, Throwable throwable);
    }

    public void sendErrorMsg(Throwable e) {
        Writer info = new StringWriter();
        PrintWriter printWriter = new PrintWriter(info);
        e.printStackTrace(printWriter);
        Throwable cause = e.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        String result = info.toString();
        printWriter.close();

/*        SPUtils spUtils = SPUtils.getInstance(mContext);
        params.put("userId", spUtils.getString(SPUtils.USER_ID, ""));
        params.put("phone", spUtils.getString(SPUtils.USER_PHONE, ""));
        params.put("loginfo", result);
        params.put("deviceType", "设备品牌:".concat(Build.BRAND).concat("\n").
                concat("设备型号:").concat(Build.MODEL).concat("\n").
                concat("设备版本号:").concat(Build.ID).concat("\n").
                concat("系统版本:").concat(Build.VERSION.RELEASE).concat("\n").
                concat("SDK版本:").concat(String.valueOf(Build.VERSION.SDK_INT)));
        params.put("appType", "android");
        params.put("systemVersion", BuildConfig.VERSION_NAME);
        Logger.e("Guke", "params:" + String.valueOf(new JSONObject(params)));*/
        try {
            // TODO: 2018/8/3 需要完善尾址
//            new NetUtils(mContext, new NetUtils.NetRequestCallBack() {
//                @Override
//                public void success(String action, BaseBean baseBean, Map tag) {
//                    if (baseBean.isSuccessful()) {
//                        Log.e("Guke", "Code:" + baseBean.getCode() + "   Message:" + baseBean.getMessage());
//                    }
//                }
//
//                @Override
//                public void error(String action, Throwable e, Map tag) {
//
//                }
//            }).post(BuildConfig.addAppLog, params, BaseBean.class, new HashMap<String, Object>(), false, NetUtils.LOG);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    /**
     * 在程序启动时候, 可以调用该函数来发送以前没有发送的报告
     */
    public void sendPreviousReportsToServer() {
        sendCrashReportsToServer(mContext);
    }

    /**
     * 把错误报告发送给服务器,包含新产生的和以前没发送的.
     *
     * @param ctx
     */
    private void sendCrashReportsToServer(Context ctx) {
        String[] crFiles = getCrashReportFiles(ctx);
        if (crFiles != null && crFiles.length > 0) {
            TreeSet<String> sortedFiles = new TreeSet<>();
            sortedFiles.addAll(Arrays.asList(crFiles));
            for (String fileName : sortedFiles) {
                File cr = new File(ctx.getFilesDir(), fileName);
                postReport(cr);
                cr.delete();// 删除已发送的报告
            }
        }
    }

    private void postReport(File file) {
        // TODO 发送错误报告到服务器,下一个版本做
    }

    /**
     * 获取错误报告文件名
     *
     * @param ctx
     * @return
     */
    private String[] getCrashReportFiles(Context ctx) {
        File filesDir = ctx.getFilesDir();
        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(CRASH_REPORTER_EXTENSION);
            }
        };
        return filesDir.list(filter);
    }

    /**
     * 保存错误信息到文件中
     *
     * @param ex
     * @return
     */
    private String saveCrashInfoToFile(Throwable ex) {
        Writer info = new StringWriter();
        PrintWriter printWriter = new PrintWriter(info);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        String result = info.toString();
        printWriter.close();
        mDeviceCrashInfo.put("EXEPTION", ex.getLocalizedMessage());
        mDeviceCrashInfo.put(STACK_TRACE, result);
        try {
            //long timestamp = System.currentTimeMillis();
            Time t = new Time("GMT+8");
            t.setToNow(); // 取得系统时间
            int date = t.year * 10000 + t.month * 100 + t.monthDay;
            int time = t.hour * 10000 + t.minute * 100 + t.second;
            String fileName = "crash-" + date + "-" + time + CRASH_REPORTER_EXTENSION;
            FileOutputStream trace = mContext.openFileOutput(fileName,
                    Context.MODE_PRIVATE);
            mDeviceCrashInfo.store(trace, "");
            trace.flush();
            trace.close();
            return fileName;
        } catch (Exception e) {
            Log.e(TAG, "an error occured while writing report file...", e);
        }
        return null;
    }

    /**
     * 收集程序崩溃的设备信息
     *
     * @param ctx
     */
    public void collectCrashDeviceInfo(Context ctx) {
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(),
                    PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                mDeviceCrashInfo.put(VERSION_NAME,
                        pi.versionName == null ? "not set" : pi.versionName);
                mDeviceCrashInfo.put(VERSION_CODE, "" + pi.versionCode);
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Error while collect package info", e);
        }
        //使用反射来收集设备信息.在Build类中包含各种设备信息,
        //例如: 系统版本号,设备生产商 等帮助调试程序的有用信息
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                mDeviceCrashInfo.put(field.getName(), "" + field.get(null));
                if (DEBUG) {
                    Log.d(TAG, field.getName() + " : " + field.get(null));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error while collect crash info", e);
            }
        }
    }


}
