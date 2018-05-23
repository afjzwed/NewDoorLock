package com.cxwl.hurry.newdoorlock;

import android.app.Application;

import com.cxwl.hurry.newdoorlock.db.DaoMaster;
import com.cxwl.hurry.newdoorlock.db.DaoSession;
import com.cxwl.hurry.newdoorlock.face.ArcsoftManager;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.log.LoggerInterceptor;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

/**
 * Application
 * Created by William on 2018/4/26.
 */

public class MainApplication  extends Application {

    private static MainApplication application;

    @Override
    public void onCreate() {
        application = this;

        // TODO: 2018/5/14 这个不用了，人脸识别的数据存本地数据库
         ArcsoftManager.getInstance().initArcsoft(this);//虹软人脸识别初始化

        super.onCreate();

        //okhttp初始化
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new LoggerInterceptor("okhttp",true))
                .connectTimeout(10000L, TimeUnit.MILLISECONDS)
                .readTimeout(10000L, TimeUnit.MILLISECONDS)
                //其他配置
                .build();
        OkHttpUtils.initClient(okHttpClient);
    }
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
