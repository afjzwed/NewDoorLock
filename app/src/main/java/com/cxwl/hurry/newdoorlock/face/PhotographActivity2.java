package com.cxwl.hurry.newdoorlock.face;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.arcsoft.facetracking.AFT_FSDKEngine;
import com.arcsoft.facetracking.AFT_FSDKError;
import com.arcsoft.facetracking.AFT_FSDKFace;
import com.arcsoft.facetracking.AFT_FSDKVersion;
import com.cxwl.hurry.newdoorlock.R;
import com.cxwl.hurry.newdoorlock.config.DeviceConfig;
import com.guo.android_extend.widget.CameraFrameData;
import com.guo.android_extend.widget.CameraGLSurfaceView;
import com.guo.android_extend.widget.CameraSurfaceView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.cxwl.hurry.newdoorlock.config.Constant.arc_appid;
import static com.cxwl.hurry.newdoorlock.config.Constant.ft_key;
import static com.cxwl.hurry.newdoorlock.config.DeviceConfig.LOCAL_FACE_PATH;

/**
 * Created by William on 2018/5/17.
 */

public class PhotographActivity2 extends AppCompatActivity implements Camera.PictureCallback,
        View.OnClickListener, CameraSurfaceView.OnCameraListener {

    private static final String TAG = PhotographActivity2.class.getSimpleName();

    private ImageView mCaptureButton;
//    private ImageView mDeleteButton;

    private File pictureFile;
    private HandlerThread handlerThread;
    private Handler handler;

    private CameraSurfaceView mSurfaceView;
    private CameraGLSurfaceView mGLSurfaceView;
    private Camera mCamera;
    private AFT_FSDKVersion version = new AFT_FSDKVersion();
    private AFT_FSDKEngine engine = new AFT_FSDKEngine();
    private List<AFT_FSDKFace> result = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photograph2);

        mGLSurfaceView = (CameraGLSurfaceView) findViewById(R.id.glsurfaceView);
        mSurfaceView = (CameraSurfaceView) findViewById(R.id.surfaceView);
        mSurfaceView.setOnCameraListener(this);
        mSurfaceView.setupGLSurafceView(mGLSurfaceView, true, true, 0);
        mSurfaceView.debug_print_fps(true, false);

        AFT_FSDKError err = engine.AFT_FSDK_InitialFaceEngine(arc_appid, ft_key, AFT_FSDKEngine
                .AFT_OPF_0_HIGHER_EXT, 16, 5);
        Log.d(TAG, "AFT_FSDK_InitialFaceEngine =" + err.getCode());
        err = engine.AFT_FSDK_GetVersion(version);
        Log.d(TAG, "AFT_FSDK_GetVersion:" + version.toString() + "," + err.getCode());

        // Add a listener to the Capture button
        mCaptureButton = (ImageView) findViewById(R.id.capture);//拍照按钮
//        mDeleteButton = (ImageView) findViewById(R.id.delete);
        mCaptureButton.setOnClickListener(this);
//        mDeleteButton.setOnClickListener(this);

        //创建一个线程,线程名字：pictureHandlerThread
        handlerThread = new HandlerThread("pictureHandlerThread");
        //开启一个线程
        handlerThread.start();

        //在这个线程中创建一个handler对象
        handler = new Handler(handlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 0:
                        if (savePicture((byte[]) msg.obj)) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(PhotographActivity2.this, "拍照成功", Toast
                                            .LENGTH_LONG).show();
                                    finishActivity();
                                }
                            });
                        }
                        break;
//                    case 1:
//                        deletePicture();
//                        break;
                }
            }
        };
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        //save the picture to sdcard
        pictureFile = getOutputMediaFile();//创建图片文件
        if (pictureFile == null) {
            Log.d(TAG, "Error creating media file, check storage permissions: ");
            return;
        }

        Message message = new Message();
        message.what = 0;
        message.obj = data;
        handler.sendMessageDelayed(message, 100);

        // Restart the preview and re-enable the shutter button so that we can take another picture
        camera.startPreview();//预览照片

//        //See if need to enable or not
//        mCaptureButton.setEnabled(true);
//        mCaptureButton.setVisibility(View.GONE);
//        mDeleteButton.setVisibility(View.VISIBLE);
        Log.v("人脸识别", "onPictureTaken3-->" + "拍照成功");
    }

    @Override
    public void onClick(View v) {
//        int i = v.getId();
//        if (i == R.id.capture) {
//            mCaptureButton.setEnabled(false);
//            // get an image from the camera
//            mCameraSurPreview.takePicture(this);
//        } else if (i == R.id.delete) {
//            handler.sendEmptyMessageDelayed(1, 100);
//        }
    }

    private File getOutputMediaFile() {
//        get the mobile Pictures directory
        File picDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        //get the current time
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String path = picDir.getPath() + File.separator + LOCAL_FACE_PATH  + timeStamp + ".jpg";//File.separator,与系统有关的默认名称分隔符
        ///storage/sdcard/Pictures/arcsoft_20180315154351.jpg

//        String SDCard = Environment.getExternalStorageDirectory() + "";
//        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
//        String path = SDCard + "/" + DeviceConfig.LOCAL_FACE_PATH + "/" + timeStamp + ".jpg";
// /storage/sdcard/myface/20180521164044.jpg
//        /storage/sdcard/Pictures/myface20180521164533.jpg
        Log.v("人脸识别", "getOutputMediaFile-->" + path);
        return new File(path);
    }

    /**
     * 保存照片
     *
     * @param data
     * @return
     */
    private boolean savePicture(byte[] data) {
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            fos.write(data);
            fos.close();
            Log.v("人脸识别", "savePicture1-->" + pictureFile);
            return true;
        } catch (FileNotFoundException e) {
            Log.v("人脸识别", "savePicture2-->" + e.getMessage());
        } catch (IOException e) {
            Log.v("人脸识别", "savePicture3-->" + e.getMessage());
        }
        return false;
    }

//    private void deletePicture() {
//        if (pictureFile != null && !pictureFile.exists()) {
//            Toast.makeText(this, "图片不存在", Toast.LENGTH_LONG).show();
//            LogDoor.v(FACE_TAG, "deletePicture1-->");
//        }
//        if (pictureFile.delete()) {
//            Toast.makeText(this, "删除成功", Toast.LENGTH_LONG).show();
//            mDeleteButton.post(new Runnable() {
//                @Override
//                public void run() {
//                    mDeleteButton.setVisibility(View.GONE);
//                    mCaptureButton.setVisibility(View.VISIBLE);
//                }
//            });
//        } else {
//            Toast.makeText(this, "删除失败", Toast.LENGTH_LONG).show();
//        }
//        LogDoor.v(FACE_TAG, "deletePicture2-->");
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AFT_FSDKError err = engine.AFT_FSDK_UninitialFaceEngine();
        Log.d(TAG, "AFT_FSDK_UninitialFaceEngine =" + err.getCode());

        result.clear();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        if (handlerThread != null) {
            handlerThread.quit();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == DeviceConfig.DEVICE_KEYCODE_POUND) {//按确认键拍照
            if (mCaptureButton.getVisibility() == View.VISIBLE) {
                mCaptureButton.setEnabled(false);//控件进入沉睡状态
                // get an image from the camera
                if (mCamera != null) {
                    mCamera.takePicture(null, null, this);
                }
            }
//            else if (mDeleteButton.getVisibility() == View.VISIBLE) {
//                handler.sendEmptyMessageDelayed(1, 100);
//            }
            return true;
        } else if (keyCode == DeviceConfig.DEVICE_KEYCODE_STAR) {//按删除键退出
            finishActivity();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void finishActivity() {
        if (pictureFile != null) {
            Intent intent = new Intent(this, FaceRegisterActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("imagePath", pictureFile.getPath());
            intent.putExtras(bundle);
            startActivity(intent);
        }
        this.finish();
    }

    @Override
    public Camera setupCamera() {
        // TODO Auto-generated method stub
        mCamera = Camera.open();//打开硬件摄像头，这里导包得时候一定要注意是android.hardware.Camera
        try {
            Camera.Parameters parameters = mCamera.getParameters();
//            parameters.setPreviewSize(640, 480);
            parameters.setPreviewFormat(ImageFormat.NV21);

            for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
                Log.v("人脸识别", "SIZE:" + size.width + "x" + size.height);
            }
            for (Integer format : parameters.getSupportedPreviewFormats()) {
                Log.v("人脸识别", "FORMAT:" + format);
            }

            List<int[]> fps = parameters.getSupportedPreviewFpsRange();
            for (int[] count : fps) {
                Log.d(TAG, "T:");
                for (int data : count) {
                    Log.d(TAG, "V=" + data);
                }
            }
            //parameters.setPreviewFpsRange(15000, 30000);
            //parameters.setExposureCompensation(parameters.getMaxExposureCompensation());
            //parameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
            //parameters.setAntibanding(Camera.Parameters.ANTIBANDING_AUTO);
            //parmeters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            //parameters.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
            //parameters.setColorEffect(Camera.Parameters.EFFECT_NONE);
            mCamera.setParameters(parameters);

            mCamera.autoFocus(null);

            int width = mCamera.getParameters().getPreviewSize().width;
            int height = mCamera.getParameters().getPreviewSize().height;
            Log.v("人脸识别", "setupCamera-->SIZE:" + width + "x" + height);
        } catch (Exception e) {
            e.printStackTrace();
            Log.v("人脸识别", "setupCamera-->" + e.getMessage());
        }

        return mCamera;
    }

    @Override
    public void setupChanged(int format, int width, int height) {
        Log.v("人脸识别", "setupChanged-->" + width + "/" + height);
    }

    @Override
    public boolean startPreviewLater() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Object onPreview(byte[] data, int width, int height, int format, long timestamp) {
        AFT_FSDKError err = engine.AFT_FSDK_FaceFeatureDetect(data, width, height, AFT_FSDKEngine
                .CP_PAF_NV21, result);
        Log.d(TAG, "AFT_FSDK_FaceFeatureDetect =" + err.getCode());
        Log.d(TAG, "Face=" + result.size());
        for (AFT_FSDKFace face : result) {
            Log.d(TAG, "Face:" + face.toString());
        }
        //copy rects
        Rect[] rects = new Rect[result.size()];
        for (int i = 0; i < result.size(); i++) {
            rects[i] = new Rect(result.get(i).getRect());
        }
        //clear result.
        result.clear();
        //return the rects for render.
        return rects;
    }

    @Override
    public void onBeforeRender(CameraFrameData data) {

    }

    @Override
    public void onAfterRender(CameraFrameData data) {
        mGLSurfaceView.getGLES2Render().draw_rect((Rect[]) data.getParams(), Color.GREEN, 2);
    }
}
