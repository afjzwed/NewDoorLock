package com.cxwl.hurry.newdoorlock;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.cxwl.hurry.newdoorlock.db.DaoMaster;
import com.cxwl.hurry.newdoorlock.db.DaoSession;
import com.cxwl.hurry.newdoorlock.face.ArcsoftManager;
import com.tencent.bugly.crashreport.CrashReport;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.log.LoggerInterceptor;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

/**
 * Application
 * Created by William on 2018/4/26.
 */

public class MainApplication extends Application {

    private static MainApplication application;
    PendingIntent restartIntent;

    @Override
    public void onCreate() {
        application = this;

        // TODO: 2018/5/14 这个不用了，人脸识别的数据存本地数据库
        ArcsoftManager.getInstance().initArcsoft(this);//虹软人脸识别初始化

        super.onCreate();

        //okhttp初始化
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new LoggerInterceptor("okhttp", true))
                .connectTimeout(10000L, TimeUnit.MILLISECONDS)
                .readTimeout(10000L, TimeUnit.MILLISECONDS)
                //其他配置
                .build();
        OkHttpUtils.initClient(okHttpClient);
        //初始化腾讯buggly
        CrashReport.initCrashReport(this, "4e8a21b88b", true);
    }

    //        Intent intent = new Intent();
//        // 参数1：包名，参数2：程序入口的activity
//        intent.setClassName("com.cxwl.hurry.newdoorlock", "com.cxwl.hurry.newdoorlock.ui.activity.MainActivity");
//        restartIntent = PendingIntent.getActivity(getApplicationContext(), 0,
//                intent, Intent.FLAG_ACTIVITY_NEW_TASK);
//        Thread.setDefaultUncaughtExceptionHandler(restartHandler); // 程序崩溃时触发线程
//    }
//
//    public Thread.UncaughtExceptionHandler restartHandler = new Thread.UncaughtExceptionHandler() {
//        @Override
//        public void uncaughtException(Thread thread, Throwable ex) {
//            AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000,
//                    restartIntent); // 1秒钟后重启应用
//            android.os.Process.killProcess(android.os.Process.myPid()); // 自定义方法，关闭当前打开的所有avtivity
//        }
//    };
    static DaoSession mDaoSessin;

    public static DaoSession getGreenDaoSession() {
        if (mDaoSessin == null) {
            DaoMaster.DevOpenHelper devOpenHelper = new DaoMaster.DevOpenHelper(application, "door-db", null);
            DaoMaster daoMaster = new DaoMaster(devOpenHelper.getWritableDatabase());
            mDaoSessin = daoMaster.newSession();
        }
        return mDaoSessin;
    }

    public static MainApplication getApplication() {
        return application;
    }

}
