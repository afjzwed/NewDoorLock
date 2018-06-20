package com.cxwl.hurry.newdoorlock.utils;

import android.util.Log;

import com.qiniu.android.utils.UrlSafeBase64;
import com.qiniu.util.Auth;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author xlei
 * @Date 2018/5/23.
 */

public class StringUtils {

    /**
     * @param str
     * @return boolean
     * @throws
     * @Title: isEmepty
     * @Description: 判断字符串是否为空和空串
     */
    public static boolean isNoEmpty(String str) {
        if (str == null) {
            return false;
        } else if ("".equals(str)) {
            return false;
        }
        return true;
    }
    // 两次点击按钮之间的点击间隔不能少于1000毫秒
    private static final int MIN_CLICK_DELAY_TIME = 4000;
    private static long lastClickTime;

    public static boolean isFastClick() {
        boolean flag = false;
        long curClickTime = System.currentTimeMillis();
        if ((curClickTime - lastClickTime) >= MIN_CLICK_DELAY_TIME) {
            flag = true;
        }
        lastClickTime = curClickTime;
        return flag;
    }

    /**

     * 把毫秒转化成日期

     * @param dateFormat(日期格式，例如：MM/ dd/yyyy HH:mm:ss)

     * @param millSec(毫秒数)

     * @return

     */

    public static String transferLongToDate(String dateFormat, Long millSec) {

        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);

        Date date = new Date(millSec);

        return sdf.format(date);
    }

    /**
     * 日期转毫秒
     * @param dateFormat
     * @return
     */
    public static String transferDateToLong(String dateFormat) {
        String timeStamp = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d;
        try {
            d = sdf.parse(dateFormat);
            long l = d.getTime();
            timeStamp = String.valueOf(l);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return timeStamp;
    }
    public static String getQiniuToken(){
        String qiniuak = "qf_oVTYRDM-06tTN7r2nAL8j0dE5JYSxyb4KWRW7";
        String qiniusk = "OxZU9VOb7wWK1-HkPXvEexuwCJ0rVpK33M-UkfmV";
        String qiniubcname = "weekreport";
        Auth auth = Auth.create(qiniuak,qiniusk);
        String s = auth.uploadToken(qiniubcname);
        Log.i("token",s);
        return s;
    }

    public static String getQnToken(){
        String token="";
        // 1 构造上传策略
        JSONObject _json = new JSONObject();
        long _dataline = System.currentTimeMillis() / 1000 + 3600;
        try { _json.put("deadline", _dataline);// 有效时间为一个小时

            _json.put("scope", "kymobile");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String _encodedPutPolicy = UrlSafeBase64.encodeToString(_json
                .toString().getBytes());
        byte[] _sign = new byte[0];
        try {
            _sign = HmacSHA1Encrypt(_encodedPutPolicy, "OxZU9VOb7wWK1-HkPXvEexuwCJ0rVpK33M-UkfmV");
        } catch (Exception e) {
            e.printStackTrace();
        }
        String _encodedSign = UrlSafeBase64.encodeToString(_sign);
        String _uploadToken = "qf_oVTYRDM-06tTN7r2nAL8j0dE5JYSxyb4KWRW7" + ':' + _encodedSign + ':'
                + _encodedPutPolicy;
        return _uploadToken;
    }
    public static byte[] HmacSHA1Encrypt(String encryptText, String encryptKey)
            throws Exception {
        byte[] data = encryptKey.getBytes();
        // 根据给定的字节数组构造一个密钥,第二参数指定一个密钥算法的名称
        SecretKey secretKey = new SecretKeySpec(data, "HmacSHA1");
        // 生成一个指定 Mac 算法 的 Mac 对象
        Mac mac = Mac.getInstance("HmacSHA1");
        // 用给定密钥初始化 Mac 对象
        mac.init(secretKey);
        byte[] text = encryptText.getBytes();
        // 完成 Mac 操作
        return mac.doFinal(text);
    }
}
