package com.cxwl.hurry.newdoorlock.utils;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.cxwl.hurry.newdoorlock.callback.AdverErrorCallBack;
import com.cxwl.hurry.newdoorlock.callback.AdverTongJiCallBack;
import com.cxwl.hurry.newdoorlock.config.Constant;
import com.cxwl.hurry.newdoorlock.db.AdTongJiBean;
import com.cxwl.hurry.newdoorlock.entity.GuangGaoBean;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.cxwl.hurry.newdoorlock.ui.activity.MainActivity.MSG_ADVERTISE_IMAGE;


/**
 * Created by simon on 2016/7/30.
 */
public class AdvertiseHandler implements SurfaceHolder.Callback {
    SurfaceView videoView = null;
    SurfaceHolder surfaceHolder = null;
    ImageView imageView = null;
    TextView mTextView = null;
//    LinearLayout videoPane=null;
//    LinearLayout imagePane=null;

    private MediaPlayer mediaPlayer;
    private MediaPlayer voicePlayer;
    private String mediaPlayerSource;
    private List<GuangGaoBean> list = new ArrayList<>();
    private List<AdTongJiBean> listCount = new ArrayList<>();
    private int listIndex = 0;
    ImageDisplayThread imageDialpayThread = null;
    private JSONArray imageList = null;
    private int imageListIndex = 0;
    private int imagePeroid = 5000;
    private boolean surfaceViewCreate = false;
    //统计
    private List<AdTongJiBean> mTongJiBeanList;
    private AdTongJiBean mAdTongJiBean;
    private String start_time;
    private String endTime;
    private AdverTongJiCallBack mAdverTongJiCallBack;

    protected Messenger dialMessenger;
    private int position;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0x01:
                    Log.i("AdvertiseHandler", "检测SurfaceView是否被创建");
                    if (surfaceViewCreate) {
                        Log.i("AdvertiseHandler", "检测到SurfaceView已经被创建");
                        mHandler.removeMessages(0x01);
                        handlerStart((AdverErrorCallBack) msg.obj);
                    } else {
                        Log.i("AdvertiseHandler", "检测到SurfaceView未被创建，延时200ms");
                        DLLog.e("AdvertiseHandler", "检测到SurfaceView未被创建，延时200ms");
                        sendHandlerMessage(0x01, msg.obj, 200);
                    }
                    break;
            }
        }
    };

    public AdvertiseHandler() {

    }

    public void init(TextView textView, SurfaceView videoView, ImageView imageView) {
        Log.d("AdvertiseHandler", "UpdateAdvertise: init");
        this.mTextView = textView;
        this.videoView = videoView;
        this.imageView = imageView;
        prepareMediaView();
    }

    public void prepareMediaView() {
        //给SurfaceView添加CallBack监听
        surfaceHolder = videoView.getHolder();
        surfaceHolder.addCallback(this);
        //为了可以播放视频或者使用Camera预览，我们需要指定其Buffer类型
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
        // 当Surface尺寸等参数改变时触发
        Log.d("AdvertiseHandler", "UpdateAdvertise: surfaceChanged");
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //创建
        Log.i("AdvertiseHandler", "SurfaceView 创建成功");
        surfaceViewCreate = true;
        //必须在surface创建后才能初始化MediaPlayer,否则不会显示图像
        //startMediaPlay(mediaPlayerSource);
        // 当SurfaceView中的Surface被创建的时候被调用
        //在这里我们指定MediaPlayer在当前的Surface中进行播放setDisplay(holder)
        //在指定了MediaPlayer播放的容器后，我们就可以使用prepare或者prepareAsync来准备播放了player.prepareAsync()
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        //销毁
        Log.i("AdvertiseHandler", "SurfaceView 销毁成功");
        surfaceViewCreate = false;
    }

    public void initData(List<GuangGaoBean> rows, Messenger dialMessenger, boolean isOnVideo, AdverErrorCallBack
            errorCallBack, AdverTongJiCallBack mCallBack) {
        imageView.setVisibility(View.INVISIBLE);
        videoView.setVisibility(View.VISIBLE);
        this.dialMessenger = dialMessenger;
        initList(rows);
        mAdverTongJiCallBack = mCallBack;
        //initScreen();
        initInterger();
//        initMediaPlayer();
        play();
        if (isOnVideo) {
            pause(errorCallBack);
        }
    }

    private void initList(List<GuangGaoBean> rows) {
        listIndex = 0;
        list.clear();
        list.addAll(rows);
    }

    private void initInterger() {
        listCount.clear();
        for (int i = 0; i < list.size(); i++) {
            AdTongJiBean tongJiBean = new AdTongJiBean();
            listCount.add(tongJiBean);
        }
    }

    public void next() {
        if (listIndex == list.size() - 1) {
            listIndex = 0;
        } else {
            listIndex++;
        }
        Log.e("AdvertiseHandler", list.size() + "----" + listIndex);
        play();
    }

    protected String getCurrentAdType() {
        String adType = "2";
        if (list != null && list.size() > 0) {
            GuangGaoBean item = list.get(listIndex);
            adType = item.getLeixing();
        }
        return adType;
    }

    public void play() {
        GuangGaoBean item = list.get(listIndex);
        String adType = item.getLeixing();
        if (adType.equals("1")) {
            playVideo(item);
        } else if (adType.equals("2")) {
            playImage(item);
        }
    }

    public void playVideo(GuangGaoBean item) {
        try {
            Log.e("AdvertiseHandler广告", item.toString());
            String fileUrls = item.getNeirong();
            if ("defaultVideo".equals(fileUrls)) {
                String source  = HttpUtils.getLocalAdv(fileUrls);
                Log.e("AdvertiseHandler广告", "source " + source);
                if (source != null) {
                    Log.e("AdvertiseHandler", "开始播放 ");
                    start_time = "" + System.currentTimeMillis();
//                    mTextView.setVisibility(View.VISIBLE);
                    videoView.setVisibility(View.VISIBLE);
                    imageView.setVisibility(View.VISIBLE);
//                    videoView.setVisibility(View.VISIBLE);
//                    imageView.setVisibility(View.INVISIBLE);
                    mediaPlayerSource = source;
                    initMediaPlayer();// TODO: 2018/7/13  //这个初始化方法应该放在initData()中
                    startMediaPlay(mediaPlayerSource);
                } else {
                    Constant.RESTART_PHONE_OR_AUDIO = 1;
                    DLLog.e("AdvertiseHandler", "source为null");
//                Log.e("广告", "next");
//                next();
                }
            } else {
                String source  = HttpUtils.getLocalFileFromUrl(fileUrls);
                if (source != null) {
                    start_time = "" + System.currentTimeMillis();
//                    mTextView.setVisibility(View.VISIBLE);
                    videoView.setVisibility(View.VISIBLE);
                    imageView.setVisibility(View.INVISIBLE);
                    mediaPlayerSource = source;
                    initMediaPlayer();// TODO: 2018/7/13  //这个初始化方法应该放在initData()中
                    startMediaPlay(mediaPlayerSource);
                } else {
                    Constant.RESTART_PHONE_OR_AUDIO = 1;
                    DLLog.e("AdvertiseHandler", "source为null");
//                Log.e("广告", "next");
//                next();
                }
            }
        } catch (Exception e) {
            Constant.RESTART_PHONE_OR_AUDIO = 1;
            DLLog.e("AdvertiseHandler", "playVideo error " + e.getMessage() + "  err " + e.toString());
//            Log.e("广告", "next "+e.getMessage()+" "+e.toString());
        }
    }

    public void playImage(GuangGaoBean item) {
        String fileUrls = item.getNeirong();
        String source = HttpUtils.getLocalFileFromUrl(fileUrls);
        if (source != null) {
            videoView.setVisibility(View.INVISIBLE);
            imageView.setVisibility(View.VISIBLE);
            startImageDisplay(item);
            initVoicePlayer();
            startVoicePlay(source);
        } else {
            next();
        }
    }

    private void startImageDisplay(final GuangGaoBean item) {
        stopImageDisplay();
        Log.v("AdvertiseHandler", "------>start image display thread<-------" + new Date());
        imageDialpayThread = new ImageDisplayThread() {
            @Override
            public void run() {
                showImage(item);
                while (!isInterrupted() && isWorking) { //检查线程没有被停止
                    try {
                        sleep(imagePeroid); //等待指定的一个并行时间
                    } catch (InterruptedException e) {
                        Constant.RESTART_PHONE_OR_AUDIO = 1;
                        DLLog.e("AdvertiseHandler", "------>end image display thread<-------" + e.getMessage() + "  " +
                                "err " + e.toString());
                    }
                    if (isWorking) {
                        nextImage(item);
                    }
                }
                Log.v("AdvertiseHandler", "------>end image display thread<-------" + new Date());
                isWorking = false;
                imageDialpayThread = null;
            }
        };
        imageDialpayThread.start();
    }

    public void nextImage(GuangGaoBean item) {
        if (listIndex == list.size() - 1) {
            listIndex = 0;
        } else {
            listIndex++;
        }
        showImage(list.get(listIndex));
        Log.v("AdvertiseHandler", "------>showing image<-------" + new Date());
    }

    public void showImage(GuangGaoBean item) {

        //     JSONObject image = imageList.getJSONObject(imageListIndex);
        String imageFile = item.getNeirong();
        if (dialMessenger != null) {
            sendDialMessenger(MSG_ADVERTISE_IMAGE, imageFile);
        }

    }

    protected void sendDialMessenger(int code, Object object) {
        Message message = Message.obtain();
        message.what = code;
        message.obj = object;
        try {
            dialMessenger.send(message);
        } catch (RemoteException e) {
            Constant.RESTART_PHONE_OR_AUDIO = 1;
            DLLog.e("AdvertiseHandler", "------>sendDialMessenger<-------" + e.getMessage() + "  err " + e.toString());
            e.printStackTrace();
        }
    }

    private void stopImageDisplay() {
        if (imageDialpayThread != null) {
            imageDialpayThread.isWorking = false;
            imageDialpayThread.interrupt();
            imageDialpayThread = null;
        }
    }

    public void initMediaPlayer() {
        //必须在surface创建后才能初始化MediaPlayer,否则不会显示图像
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        }
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setDisplay(surfaceHolder);
        //设置显示视频显示在SurfaceView上
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.i("xiao_", "视频播放完成，继续播放下一个视频文件");
                onMediaPlayerComplete();
            }
        });
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Log.d("AdvertiseHandler", "多媒体报错" + what + " extra " + extra);
                DLLog.d("AdvertiseHandler", "多媒体死亡 " + what + " extra " + extra);
                imageView.setVisibility(View.VISIBLE);
                if (what != 100) {
                    Constant.RESTART_PHONE_OR_AUDIO = 1;
                }
                switch (what) {
                    case 100:
                        DLLog.d("AdvertiseHandler", "多媒体死亡 MEDIA_ERROR_SERVER_DIED");
                        Constant.RESTART_AUDIO = true;
//                        Constant.RESTART_PHONE_OR_AUDIO = 2;
                        break;
                }
                return false;
            }
        });
    }

    public void initVoicePlayer() {
        if (voicePlayer == null) {
            voicePlayer = new MediaPlayer();
        }
        voicePlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                onVoicePlayerComplete();
            }
        });
    }

    protected void onMediaPlayerComplete() {
        //mediaPlayer.release();
        endTime = System.currentTimeMillis() + "";
        mTongJiBeanList = new ArrayList<>();
        mAdTongJiBean = new AdTongJiBean();
        mAdTongJiBean.setStart_time(start_time);
        mAdTongJiBean.setEnd_time(endTime);
        mAdTongJiBean.setAd_id(list.get(listIndex).getId());
        mAdTongJiBean.setMac(MacUtils.getMac());
        if (list.get(listIndex).getId() != -2) {
            mTongJiBeanList.add(mAdTongJiBean);
            DbUtils.getInstans().addAllTongji(mTongJiBeanList);
        } else {

        }
        //  mAdverTongJiCallBack.sendTj(mTongJiBeanList);
        if (Long.parseLong(StringUtils.transferDateToLong(list.get(listIndex).getShixiao_shijian())) < System
                .currentTimeMillis()) {
            //视频已失效
            Log.e("AdvertiseHandler", "当前视频已经失效 list.size()" + list.size());
            if (list.size() > 1) {
                list.remove(list.get(listIndex));
                listIndex = 0;
                Log.e("AdvertiseHandler", "当前视频已经失效 继续播放重第一个开始" + list.size());
            } else {
                list.remove(list.get(listIndex));
                DLLog.e("AdvertiseHandler", "当前视频已经失效 停止播放");
                onDestroy();
                Log.e("AdvertiseHandler", "当前视频已经失效 停止播放" + list.size());
                return;
            }
        }
        next();
    }

    protected void onVoicePlayerComplete() {
        voicePlayer.release();
        stopImageDisplay();
        next();
    }

    public void startMediaPlay(String source) {
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(source);
//            mediaPlayer.prepare();
//            mediaPlayer.start();
            mediaPlayer.prepareAsync();//在使用MediaPlayer准备的时候，最好使用prepareAsync()方法，而不是prepare()
            // 方法，因为前一个方法是异步准备的，不会阻碍主线程，可以解决Prepare failed问题
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mediaPlayer.start();
                }
            });
        } catch (Exception e) {
            Constant.RESTART_PHONE_OR_AUDIO = 1;
            DLLog.e("AdvertiseHandler", "UpdateAdvertise: startMediaPlay error " + e
                    .toString());
            Log.e("AdvertiseHandler", "UpdateAdvertise: startMediaPlay error");
        }
    }

    public void startVoicePlay(String source) {
        try {
            voicePlayer.reset();
            voicePlayer.setDataSource(source);
            voicePlayer.prepare();
            voicePlayer.start();
        } catch (Exception e) {
            Constant.RESTART_PHONE_OR_AUDIO = 1;
            DLLog.e("AdvertiseHandler", "startVoicePlay error " + e.toString());
            e.printStackTrace();
        }
    }

    public void onDestroy() {
        try {
            Log.d("AdvertiseHandler", "UpdateAdvertise: onDestroy");
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
                mediaPlayer = null;
            }
            if (voicePlayer != null) {
                if (voicePlayer.isPlaying()) {
                    voicePlayer.stop();
                }
                voicePlayer.release();
                voicePlayer = null;
            }
        } catch (IllegalStateException e) {
            Log.d("AdvertiseHandler", "UpdateAdvertise: onDestroy error=" + e.toString());
            Constant.RESTART_PHONE_OR_AUDIO = 1;
            DLLog.e("AdvertiseHandler", "UpdateAdvertise: onDestroy error " + e.toString());
        } finally {
            Log.e("AdvertiseHandler", "显示背景图");
            if (videoView != null && imageView != null) {
                mediaPlayer = null;
                voicePlayer = null;
                videoView.setVisibility(View.GONE);
                imageView.setVisibility(View.VISIBLE);
                Log.e("AdvertiseHandler", "停止播放");
            }
        }
    }

    public void onStop() {
        try {
            if (mediaPlayer == null) {
                return;
            }
            if (mediaPlayer.isPlaying()) {
                position = mediaPlayer.getCurrentPosition();
                mediaPlayer.stop();
            }
        } catch (IllegalStateException e) {
            Log.d("AdvertiseHandler", "UpdateAdvertise: onStop error");
        }
    }

    public void onRestart() {
        if (position > 0) {
            try {
                play();
            } catch (IllegalStateException e) {
                Log.d("AdvertiseHandler", "UpdateAdvertise: onRestart error");
            }
            //mediaPlayer.seekTo(position);
            position = 0;
            Log.d("AdvertiseHandler", "UpdateAdvertise: onRestart done");
        }
    }

    public void start(AdverErrorCallBack errorCallBack) {
        sendHandlerMessage(0x01, errorCallBack, 0);
    }

    private void sendHandlerMessage(int what, Object msg, int delay) {
        Message message = new Message();
        message.what = what;
        if (msg != null) {
            message.obj = msg;
        }
        if (delay > 0) {
            mHandler.sendMessageDelayed(message, delay);
        } else {
            mHandler.sendMessage(message);
        }

    }

    private void handlerStart(AdverErrorCallBack errorCallBack) {
        try {
            Log.i("xiao_", "handlerStart->");
            if (mediaPlayer != null) {
                if (!mediaPlayer.isPlaying()) {
                    Log.i("xiao_", "handlerStart->setDisplay");
                    mediaPlayer.setDisplay(surfaceHolder);
                    Log.i("xiao_", "handlerStart->start");
                    int vis = videoView.getVisibility();
                    Log.i("xiao_", "SurfaceView is show = " + vis);
                    mediaPlayer.start();
                }
            } else {
                Log.i("xiao_", "mediaPlayer = null");
            }
            if (getCurrentAdType().equals("V")) {
            } else if (getCurrentAdType().equals("I")) {
                voicePlayer.start();
            }
        } catch (Exception e) {
            Constant.RESTART_PHONE_OR_AUDIO = 1;
            DLLog.e("AdvertiseHandler", "UpdateAdvertise: handlerStart error " + e.toString());
            e.printStackTrace();
            Log.d("AdvertiseHandler", "UpdateAdvertise: start error");
            errorCallBack.ErrorAdver();
        }
    }

    public void pause(AdverErrorCallBack errorCallBack) {
        try {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                }
            }
            if (getCurrentAdType().equals("V")) {

            } else if (getCurrentAdType().equals("I")) {
                voicePlayer.pause();
            }
        } catch (IllegalStateException e) {
            Constant.RESTART_PHONE_OR_AUDIO = 1;
            DLLog.e("AdvertiseHandler", "pause error " + e.toString() + " message " + e.getMessage());
            errorCallBack.ErrorAdver();
        }
    }

    public List<GuangGaoBean> getList() {
        return list;
    }

    public void setList(List<GuangGaoBean> list) {
        this.list = list;
    }
}

class ImageDisplayThread extends Thread {
    public boolean isWorking = true;
}