package com.cxwl.hurry.newdoorlock.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Environment;
import android.support.v8.renderscript.RenderScript;
import android.util.Log;

import com.cxwl.hurry.newdoorlock.MainApplication;
import com.cxwl.hurry.newdoorlock.config.DeviceConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import io.github.silvaren.easyrs.tools.Nv21Image;

/**
 * Created by William on 2018/5/15.
 */

public class BitmapUtils {
    /**
     * @param path
     * @return
     */
    public static Bitmap decodeImage(String path) {
        Bitmap res;
        try {
            ExifInterface exif = new ExifInterface(path);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            BitmapFactory.Options op = new BitmapFactory.Options();
            op.inSampleSize = 1;
            op.inJustDecodeBounds = false;
            //op.inMutable = true;
            res = BitmapFactory.decodeFile(path, op);
            //rotate and scale.
            Matrix matrix = new Matrix();

            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                Log.e("wh", "角度 90");
                matrix.postRotate(90);
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                Log.e("wh", "角度 180");
//                matrix.postRotate(180);
                matrix.postRotate(0);
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                Log.e("wh", "角度 270");
                matrix.postRotate(270);
            } else {
                Log.e("wh", "角度 0");
//                matrix.postRotate(0);
                matrix.postRotate(180);
            }
            matrix.postScale(-1, 1); // 镜像水平翻转

            Bitmap temp = Bitmap.createBitmap(res, 0, 0, res.getWidth(), res.getHeight(), matrix, true);
            Log.v("face", "decodeImage-->" + temp.getWidth() + "/" + temp.getHeight() + "/" + orientation);

            if (!temp.equals(res)) {
                res.recycle();
            }
            return temp;
        } catch (Exception e) {
            e.printStackTrace();
            Log.v("face", "decodeImage/Exception-->" + e.getMessage());
        }
        return null;
    }

    // 等比缩放图片
    public static Bitmap zoomImg(Bitmap bm, int newWidth, int newHeight) {
        // 获得图片的宽高
        int width = bm.getWidth();
        int height = bm.getHeight();
        // 计算缩放比例
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 得到新的图片
        Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
        return newbm;
    }

    /**
     * 随机生产文件名
     *
     * @return
     */
    private static String generateFileName() {
        return UUID.randomUUID().toString();
    }


    private static final String SD_PATH = "/sdcard/dskqxt/pic/";
    private static final String IN_PATH = "/dskqxt/pic/";

    /**
     * 保存bitmap到本地
     *
     * @param mBitmap
     * @return
     */
    public static File saveBitmap(Bitmap mBitmap) {
        File filePic;
        String savePath;
       /* if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            savePath = SD_PATH;
        } else {
            savePath = context.getApplicationContext().getFilesDir().getAbsolutePath()+ IN_PATH;
        }*/
        savePath = Environment.getExternalStorageDirectory() + "" + File.separator + DeviceConfig.LOCAL_IMG_PATH +
                File.separator + System.currentTimeMillis() + ".jpg";
        try {
            filePic = new File(savePath);
            if (!filePic.exists()) {
                filePic.getParentFile().mkdirs();
                filePic.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(filePic);
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        return filePic;
    }

    /**
     * 视频字节数组数据转bitmap
     *
     * @param data
     * @param width
     * @param height
     * @return
     */
    public static Bitmap byteToFile(byte[] data, int width, int height) {
        //防止厂商没有优化YuvImage.compressToJpeg方法导致native层内存溢出，用以下的方法压缩转换图片
        RenderScript rs = RenderScript.create(MainApplication.getApplication()); // where context can be your
// activity, application, etc.
        Bitmap outputBitmap = Nv21Image.nv21ToBitmap(rs, data, width, height); // where nv21ByteArray contains the
// NV21 image data
        return outputBitmap;

//        YuvImage yuv = new YuvImage(data, ImageFormat.NV21, width, height, null);
//        ExtByteArrayOutputStream ops = new ExtByteArrayOutputStream();
//        yuv.compressToJpeg(new Rect(0, 0, width, height), 80, ops);
//        Bitmap bmp = BitmapFactory.decodeByteArray(ops.getByteArray(), 0, ops.getByteArray().length);
//        try {
//            ops.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        yuv = null;
//        return bmp;
    }

    /**
     * Bitmap旋转180度
     *
     * @param bmp
     * @return
     */
    public static Bitmap rotateBitmap(Bitmap bmp) {
        if (null != bmp) {
            Matrix matrix = new Matrix();
            // 缩放原图
            matrix.postScale(1f, 1f);
            // 向左旋转45度，参数为正则向右旋转
            matrix.postRotate(-180);
//            matrix.postScale(-1, 1); // 镜像水平翻转
            //bmp.getWidth(), 500分别表示重绘后的位图宽高
            Bitmap dstbmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(),
                    matrix, true);
            return dstbmp;
        } else {
            return null;
        }
    }

    /**
     * 读取照片exif信息中的旋转角度
     *
     * @param path 照片路径
     * @return角度
     */
    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation =
                    exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

//    int degree = ImageUtil.readPictureDegree(imageUri.getPath());
//    Bitmap bmpOk = ImageUtil.rotateToDegrees(bmp, degree);

    /**
     * 图片旋转
     *
     * @param tmpBitmap
     * @param degrees
     * @return
     */
    public static Bitmap rotateToDegrees(Bitmap tmpBitmap, float degrees) {
        Matrix matrix = new Matrix();
        matrix.reset();
        matrix.setRotate(degrees);
        return tmpBitmap = Bitmap.createBitmap(tmpBitmap, 0, 0, tmpBitmap.getWidth(), tmpBitmap.getHeight(), matrix,
                true);
    }
}
