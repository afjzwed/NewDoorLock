package com.cxwl.hurry.newdoorlock.utils;

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
}
