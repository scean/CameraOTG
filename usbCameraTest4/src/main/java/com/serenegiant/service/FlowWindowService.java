package com.serenegiant.service;

import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.usb.UsbDevice;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.serenegiant.common.BaseService;
import com.serenegiant.serviceclient.CameraClient;
import com.serenegiant.serviceclient.ICameraClient;
import com.serenegiant.serviceclient.ICameraClientCallback;
import com.serenegiant.usb.DeviceFilter;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.otgUI.R;

import java.util.List;

public class FlowWindowService extends BaseService {

    private static final boolean DEBUG = false;
    private static final String TAG = "FlowWindowService";
    private USBMonitor mUSBMonitor;
    private ICameraClient mCameraClient;
    private static final int DEFAULT_WIDTH = 640;
    private static final int DEFAULT_HEIGHT = 480;

    public FlowWindowService() {
    }


    @Override
    public void onCreate() {
        super.onCreate();
        if (DEBUG) Log.d(TAG, "onCreate:");
        if (mUSBMonitor == null) {
            mUSBMonitor = new USBMonitor(getApplicationContext(), mOnDeviceConnectListener);
            final List<DeviceFilter> filters = DeviceFilter.getDeviceFilters(getApplicationContext(), R.xml.device_filter);
            mUSBMonitor.setDeviceFilter(filters);
            mUSBMonitor.register();
        }
        initWindowParams();
    }




    private final USBMonitor.OnDeviceConnectListener mOnDeviceConnectListener = new USBMonitor.OnDeviceConnectListener() {
        @Override
        public void onAttach(final UsbDevice device) {
            if (DEBUG) {
                Log.v(TAG, "OnDeviceConnectListener#onAttach:");
                Toast.makeText(FlowWindowService.this, TAG+">>onAttach", Toast.LENGTH_SHORT).show();
            }
            tryOpenUVCCamera(true);
            addWindowView2Window();
        }

        @Override
        public void onConnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock, final boolean createNew) {
            if (DEBUG) {
                Log.v(TAG, "OnDeviceConnectListener#onConnect:");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(FlowWindowService.this, TAG+">>onConnect", Toast.LENGTH_SHORT).show();
                    }
                },0);
            }
        }

        @Override
        public void onDisconnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock) {
            if (DEBUG) {
                Log.v(TAG, "OnDeviceConnectListener#onDisconnect:");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(FlowWindowService.this, TAG+">>onDisconnect", Toast.LENGTH_SHORT).show();
                    }
                },0);
            }
        }

        @Override
        public void onDettach(final UsbDevice device) {
            if (DEBUG){
                Log.v(TAG, "OnDeviceConnectListener#onDettach:");
                Toast.makeText(FlowWindowService.this, TAG+">>onDettach", Toast.LENGTH_SHORT).show();
            }
            removeWindowView2Window();
            queueEvent(new Runnable() {
                @Override
                public void run() {
                    if (mCameraClient != null) {
                        mCameraClient.disconnect();
                        mCameraClient.release();
                        mCameraClient = null;
                    }
                }
            }, 0);
        }

        @Override
        public void onCancel(final UsbDevice device) {
            if (DEBUG) {
                Log.v(TAG, "OnDeviceConnectListener#onCancel:");
                Toast.makeText(FlowWindowService.this, TAG+">>onCancel", Toast.LENGTH_SHORT).show();
            }

        }
    };


    private void tryOpenUVCCamera(final boolean requestPermission) {
        if (DEBUG) Log.v(TAG, "tryOpenUVCCamera:");
        openUVCCamera(0);
    }

    private void openUVCCamera(final int index) {
        if (DEBUG) Log.v(TAG, "openUVCCamera:index=" + index);
        if (!mUSBMonitor.isRegistered()) return;
        final List<UsbDevice> list = mUSBMonitor.getDeviceList();
        if (DEBUG){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(FlowWindowService.this, TAG+">>list.size() = "+list.size(), Toast.LENGTH_SHORT).show();
                }
            },0);
        }
        if (list.size() > index) {
            if (mCameraClient == null)
                mCameraClient = new CameraClient(FlowWindowService.this, mCameraListener);
            mCameraClient.select(list.get(index));
            mCameraClient.resize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
            mCameraClient.connect();
           // final Intent intent = new Intent(FlowWindowService.this, UVCService.class);
           // FlowWindowService.this.startService(intent);
        }
    }

    private final ICameraClientCallback mCameraListener = new ICameraClientCallback() {
        @Override
        public void onConnect() {
           if (DEBUG){
               Log.v(TAG, "onConnect:");
               runOnUiThread(new Runnable() {
                   @Override
                   public void run() {
                       Toast.makeText(FlowWindowService.this, TAG+">> ICameraClientCallback onConnect", Toast.LENGTH_SHORT).show();
                   }
               },0);
           }
            try{
                mCameraClient.addSurface(mCameraViewSub2.getHolder().getSurface(), false);
            }catch (Exception e){
                e.printStackTrace();
            }
//            // start UVCService
   //         final Intent intent = new Intent(FlowWindowService.this, UVCService.class);
   //         FlowWindowService.this.startService(intent);
        }

        @Override
        public void onDisconnect() {
            if (DEBUG){
                Log.v(TAG, "onDisconnect:");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(FlowWindowService.this, TAG+">> ICameraClientCallback onDisconnect", Toast.LENGTH_SHORT).show();
                    }
                },0);
            }
        }

    };


    //test for windowsManager add view>>>>>>>>>>>>>>>>>>>

    private WindowManager.LayoutParams wmParams;
    private WindowManager mWindowManager;
    private View mWindowView;
    private SurfaceView mCameraViewSub2;

    private void initWindowParams() {
        //if (1==1) return;
        mWindowManager = (WindowManager) getApplication().getSystemService(getApplication().WINDOW_SERVICE);
        wmParams = new WindowManager.LayoutParams();
        wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        wmParams.format = PixelFormat.TRANSLUCENT;
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        wmParams.gravity = Gravity.RIGHT | Gravity.TOP;
        wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowView = LayoutInflater.from(getApplication()).inflate(R.layout.test_fragment_main, null);
        mCameraViewSub2 = (SurfaceView)mWindowView.findViewById(R.id.camera_view_sub2);
      //  mWindowManager.addView(mWindowView, wmParams);
       // mWindowView.setVisibility(View.GONE);
    }


    private void addWindowView2Window() {
       // if (1==1) return;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
               // mWindowView.setVisibility(View.VISIBLE);
                try {
                    mWindowManager.addView(mWindowView, wmParams);
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        },0);

    }

    private void removeWindowView2Window() {
       // if (1==1) return;
        try {
            mCameraClient.removeSurface(mCameraViewSub2.getHolder().getSurface());
        }catch (Exception e){
            e.printStackTrace();
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    mWindowManager.removeView(mWindowView);

                }catch (Exception e) {
                    e.printStackTrace();
                }
              //  mWindowView.setVisibility(View.GONE);
            }
        },0);
    }
    //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<end











    @Override
    public void onDestroy() {
        if (DEBUG) Log.d(TAG, "onDestroy:");
        if (mUSBMonitor != null) {
            mUSBMonitor.unregister();
            mUSBMonitor = null;
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(final Intent intent) {
        if (DEBUG) Log.d(TAG, "onBind:" + intent);
        return null;
    }

    @Override
    public void onRebind(final Intent intent) {
        if (DEBUG) Log.d(TAG, "onRebind:" + intent);
    }

    @Override
    public boolean onUnbind(final Intent intent) {
        return true;
    }
}
