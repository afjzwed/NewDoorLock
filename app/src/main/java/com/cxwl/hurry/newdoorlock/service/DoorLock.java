package com.cxwl.hurry.newdoorlock.service;

import android.util.Log;

import com.cxwl.hurry.newdoorlock.callback.AccountCallback;
import com.cxwl.hurry.newdoorlock.db.Lian;
import com.cxwl.hurry.newdoorlock.utils.DbUtils;
import com.hurray.plugins.rkctrl;
import com.hurray.plugins.serialport;

/**
 * 昊睿门禁机服务类,DoorLock主要提供开门,关门指令以及上报门开和关闭的事件
 * Created by William on 2018/5/23.
 */

public class DoorLock {
    private String TAG = "DoorLock";

    private serialport m_serial = new serialport();//昊瑞门禁服务类
    private rkctrl m_rkctrl = new rkctrl();//昊瑞门禁服务类
    private String arg = "/dev/ttyS1,9600,N,1,8";
    private Thread pthread = null;
    private boolean isNfcFlag = false;//串口库是否打开的标识（默认失败）
    private int iRead = 0;//串口库返回值(默认0)

//    private MyHandler myHandler = new MyHandler();

    private AccountCallback accountCallback;
    private static DoorLock mServiceInstance = null;

    public static DoorLock getInstance() {
        return mServiceInstance;
    }

    public DoorLock(AccountCallback accountCallback) {
        if (null == mServiceInstance) {
            mServiceInstance = this;
        }
        this.accountCallback = accountCallback;
        initSerial();
    }

    private void initSerial() {
        iRead = m_serial.open(arg);
        if (iRead > 0) {
            isNfcFlag = true;
            Log.i(TAG, "打开串口成功 isNfcFlag " + isNfcFlag);
            runReadSerial(iRead);
        } else {
            isNfcFlag = false;
            Log.i(TAG, "打开串口失败 isNfcFlag " + isNfcFlag);
        }
    }

    private String substring;
    private String str = "";
    private String strData = "";
    Lian lian;
    public void runReadSerial(final int fd) {
        Runnable run = new Runnable() {
            public void run() {
                while (isNfcFlag) {
                    int r = m_serial.select(fd, 1, 0);
                    if (r == 1) {
                        //测试 普通读串口数据
                        byte[] buf = new byte[50];
                        buf = m_serial.read(fd, 100);


                        if (buf == null) break;

                        if (buf.length <= 0) break;

                        strData = byte2HexString(buf);

//                        Log.v(TAG, str);

                        str += strData;
                        if (str.length() > 27) {
                            substring = str.substring(16, 24).toUpperCase();
                            //// TODO: 2018/5/26 测试刷卡 
//                            Log.e(TAG, "卡号 " + substring);
//                            DbUtils.getInstans().deleteAllLian();
//                             lian = new Lian();
//                            lian.setLian_id(substring);
//                            DbUtils.getInstans().insertOneLian(lian);
//                            DbUtils.getInstans().quaryAllLian();
                            accountCallback.onAccountReceived(substring);
                            str = "";
                        }

//                        Message msgpwd = new Message();
//                        msgpwd.what = 1;
//                        Bundle data = new Bundle();
//                        data.putString("data", str);
//                        msgpwd.setData(data);
//                        myHandler.sendMessage(msgpwd);

                    }
                }
                onThreadEnd();
            }
        };
        pthread = new Thread(run);
        pthread.start();
    }

    /**
     * byte[]转换成字符串
     *
     * @param b
     * @return
     */
    public static String byte2HexString(byte[] b) {
        StringBuffer sb = new StringBuffer();
        int length = b.length;
        for (int i = 0; i < b.length; i++) {
            String stmp = Integer.toHexString(b[i] & 0xff);
            if (stmp.length() == 1) sb.append("0" + stmp);
            else sb.append(stmp);
        }
        return sb.toString();
    }

    public void onThreadEnd() {
        Log.e(TAG, "监听串口线程结束");
    }

    public boolean getIsNfcFlag() {
        return isNfcFlag;
    }

    public void setIsNfcFlag(boolean b) {
        isNfcFlag = b;
    }

    /**
     * 控制继电器的方法 返回－1 说明操作节点失败
     * @return
     */
    public int openLock() {
        int i = m_rkctrl.exec_io_cmd(19, 0);// 参数cmd:1 打开 0 关闭
        return i;
    }
    public int closeLock() {
        int i = m_rkctrl.exec_io_cmd(19, 1);// 参数cmd:1 打开 0 关闭
        return i;
    }
/*    public class MyHandler extends Handler {
        public MyHandler() {
        }

        public MyHandler(Looper L) {
            super(L);
        }

        // 子类必须重写此方法,接受数据
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String strData = "";
            // 此处可以更新UI
            switch (msg.what) {
                case 1:
                    strData = msg.getData().getString("data");
                    Log.v(TAG, strData);

//                    accountCallback.onAccountReceived(msg.getData().getString("data"));

                    break;
            }
        }
    }*/

}
