package com.cxwl.hurry.newdoorlock.config;


import android.os.Environment;

/**
 * Created by simon on 2016/7/23.
 */
public class DeviceConfig {
    /********residential*****/
//    public static final String SERVER_URL="http://www.lockaxial.com";
    public static final String SERVER_URL = "http://192.168.8.142:8084";

    //    public static final String GET_QINIUTOKEN = "http://wy.iot.xin/qiniu/qiniu_getQiniuToken.action";
    public static final String GET_QINIUTOKEN = "http://192.168.8.133:8080/wygl/qiniu/qiniu_getQiniuToken.action";
    //七牛访问地址
    //   public static final String SERVER_URL = "http://www.lockaxial.com";

    public static final String LOCAL_FILE_PATH = "adv";//广告储存位置
    public static final String LOCAL_ADV_PATH = "adv";//广告视频储存位置
    public static final String LOCAL_ADP_PATH = "adp";//广告图片储存位置
    public static final String LOCAL_FACE_PATH = "myface";//人脸图片储存位置
    public static final String LOCAL_APK_PATH = "myapk";//apk文件储存位置
    public static final String LOCAL_FACEINFO_PATH = "myfaceinfo";
    public static final String LOCAL_IMG_PATH = "img_local";//所有照片


    public static String DEVICE_TYPE = "B"; //C：社区大门门禁 B:楼栋单元门禁

    public static String RFID_PORT = "/dev/ttyS1";//卡阅读器使用

    public static int OPENDOOR_TIME = 1000 * 5;//开门持续时间
    public static int OPENDOOR_STATE = 0;//开门状态 0为关闭门锁 1为打开门锁 默认关闭
    public static int CANCEL_CALL_WAIT_TIME = 1000 * 30;//自动取消呼叫等待时间
    public static int CONNECT_REPORT_WAIT_TIME = 1000 * 60;//心跳间隔时间
    public static int PASSWORD_WAIT_TIME = 1000 * 20;//密码验证线程等待时间

    public static int VOLUME_STREAM_MUSIC = 5;//音乐音量
    public static int VOLUME_STREAM_VOICE_CALL = 5;//通话音量
    public static int VOLUME_STREAM_RING = 5;//铃声音量
    public static int VOLUME_STREAM_SYSTEM = 5;//系统音量


    public static int MOBILE_NO_LENGTH = 11;//手机号长度
    public static int UNIT_NO_LENGTH = 4;//房屋号长度
    public static int BLOCK_NO_LENGTH = 2;
    public static int BLOCK_LENGTH = 8;//楼栋房屋号长度

    public static int OPEN_RENLIAN_STATE = 0;//人脸开门是否开始处理图片并上传日志的状态  0:未开始 1:已开始
    public static int OPEN_CARD_STATE = 0;//卡开门是否开始处理图片并上传日志的状态 0:未开始 1:已开始
    public static int OPEN_PHONE_STATE = 0;//手机一键开门是否开始处理图片并上传日志的状态 0:未开始 1:已开始

    public static int PRINTSCREEN_STATE = 0;//各种方式(人脸/卡)是否开始处理图片并上传日志的状态  0:未开始 1:人脸 2:卡
    
    //    public static final int DEVICE_KEYCODE_POUND = 66;//确认键
//    public static final int DEVICE_KEYCODE_STAR = 67;//删除键
    public static final int DEVICE_KEYCODE_POUND = 30;//确认键
    public static final int DEVICE_KEYCODE_STAR = 32;//删除键


    /*******************************/

}
