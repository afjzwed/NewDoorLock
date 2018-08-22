package com.cxwl.hurry.newdoorlock.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.cxwl.hurry.newdoorlock.http.API;
import com.cxwl.hurry.newdoorlock.utils.DLLog;
import com.cxwl.hurry.newdoorlock.utils.HttpApi;
import com.cxwl.hurry.newdoorlock.utils.JsonUtil;
import com.cxwl.hurry.newdoorlock.utils.MacUtils;
import com.zhy.http.okhttp.OkHttpUtils;

import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import okhttp3.MediaType;
import okhttp3.Response;

/**
 * 用于后台接收服务器重启指令的服务类
 * Created by William on 2018/8/22.
 */

public class BackHttpService extends Service {

    private Context mContext;
    private String mac;
    private String key;
    private Timer rebootTimer;

    private TimerTask task = new TimerTask() {
        @Override
        public void run() {
            boolean result = false;
            result = isREBoot();
            if (result) {
                // TODO: 2018/8/22  执行重启操作
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        mac = MacUtils.getMac();
        if (mac == null || mac.length() == 0) {
            //无法获取设备编号 用mainMessage发送信息给MainActivity显示
        } else {
            key = mac.replace(":", "");
        }

        if (rebootTimer != null) {
            rebootTimer.cancel();
            rebootTimer = null;
        }
        rebootTimer = new Timer();
        rebootTimer.schedule(task, 0, 60 * 1000);
    }

    /**
     * 是否重启接口
     *
     * @return
     */
    private boolean isREBoot() {
        boolean resultValue = false;
        try {
            String url = API.DEVICE_LOGIN;

            JSONObject data = new JSONObject();
            data.put("mac", mac);
            data.put("key", key);
            Log.e("重启", "mac " + mac + " key " + key);

            Response execute = OkHttpUtils.postString().url(url).content(data.toString()).mediaType(MediaType.parse
                    ("application/json; charset=utf-8")).tag(this).build().execute();
            if (null != execute) {
                String response = execute.body().string();
                if (null != response) {
                    String code = JsonUtil.getFieldValue(response, "code");
                    if ("0".equals(code)) {
                        resultValue = true;
                        onReStartVideo();
                    } else {

                    }
                } else {
//                    服务器异常或没有网络
                    HttpApi.e("getClientInfo()->response为空");
                }
            } else {
                HttpApi.e("getClientInfo()->服务器未响应");
            }
        } catch (Exception e) {
            HttpApi.e("getClientInfo()->服务器异常或没有网络");
            e.printStackTrace();
        }
        Log.e("重启", "" + resultValue);
        return resultValue;
    }

    private void onReStartVideo() {
        DLLog.e("BackHttpService", "进行设备的重启");
//        startActivity(new Intent(this, PhotographActivity.class));
        Intent intent1 = new Intent(Intent.ACTION_REBOOT);
        intent1.putExtra("nowait", 1);
        intent1.putExtra("interval", 1);
        intent1.putExtra("window", 0);
        sendBroadcast(intent1);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
