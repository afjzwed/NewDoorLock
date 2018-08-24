package com.cxwl.hurry.newdoorlock.service;

import android.app.ActivityManager;
import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.cxwl.hurry.newdoorlock.http.API;
import com.cxwl.hurry.newdoorlock.utils.DLLog;
import com.cxwl.hurry.newdoorlock.utils.HttpApi;
import com.cxwl.hurry.newdoorlock.utils.JsonUtil;
import com.cxwl.hurry.newdoorlock.utils.MacUtils;
import com.zhy.http.okhttp.OkHttpUtils;

import org.json.JSONObject;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.MediaType;
import okhttp3.Response;

/**
 * 用于后台接收服务器重启指令的服务类
 * Created by William on 2018/8/22.
 */


@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class BackHttpService extends JobService {

    private Context mContext;
    private String mac;
    private String key;
    private Timer rebootTimer;
    private int kJobId = 0;

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
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("castiel", "jobService启动");
        scheduleJob(getJobInfo());
        return START_NOT_STICKY;
    }

    //将任务作业发送到作业调度中去
    public void scheduleJob(JobInfo t) {
        Log.i("castiel", "调度job");
        JobScheduler tm =(JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        tm.schedule(t);
    }

    public JobInfo getJobInfo(){
        JobInfo.Builder builder = new JobInfo.Builder(kJobId++, new ComponentName(this, BackHttpService.class));
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
        builder.setPersisted(true);
        builder.setRequiresCharging(false);
        builder.setRequiresDeviceIdle(false);
        //间隔100毫秒
        builder.setPeriodic(100);
        return builder.build();
    }

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Log.i("castiel", "执行了onStartJob方法");
        boolean isLocalServiceWork = isServiceWork(this, "com.cxwl.hurry.newdoorlock.service.BackHttpService");
//        boolean isRemoteServiceWork = isServiceWork(this, "com.cxwl.monitor.MonitorService");
//        if(!isLocalServiceWork||!isRemoteServiceWork){
        if(!isLocalServiceWork){
            this.startService(new Intent(this,BackHttpService.class));
//            this.startService(new Intent(this,MonitorService.class));
//            Toast.makeText(this, "进程启动", Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Log.i("castiel", "执行了onStopJob方法");
        scheduleJob(getJobInfo());
        return true;
    }

    // 判断服务是否正在运行
    public boolean isServiceWork(Context mContext, String serviceName) {
        boolean isWork = false;
        ActivityManager myAM = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> myList = myAM.getRunningServices(100);
        if (myList.size() <= 0) {
            return false;
        }
        for (int i = 0; i < myList.size(); i++) {
            String mName = myList.get(i).service.getClassName().toString();
            if (mName.equals(serviceName)) {
                isWork = true;
                break;
            }
        }
        return isWork;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
//        mac = MacUtils.getMac();
//        if (mac == null || mac.length() == 0) {
//            //无法获取设备编号 用mainMessage发送信息给MainActivity显示
//        } else {
//            key = mac.replace(":", "");
//        }
//
//        if (rebootTimer != null) {
//            rebootTimer.cancel();
//            rebootTimer = null;
//        }
//        rebootTimer = new Timer();
//        rebootTimer.schedule(task, 0, 60 * 1000);
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
}
