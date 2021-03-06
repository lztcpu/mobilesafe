package com.example.www.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.www.engine.AddressDao;
import com.example.www.mobilesafe.R;
import com.example.www.utils.ConstantValue;
import com.example.www.utils.SpUtil;

public class AddressService extends Service {


    private TelephonyManager mTelephonyManager;
    private MyPhoneStateListener mMyPhoneStateListener;
    public final String TAG = "AddressService";
    private View mViewToast;
    private WindowManager mWindowManager;
    private String mAddress;
    private TextView mTv_address;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mTv_address.setText(mAddress);
        }
    };
    private InneOutCallReceiver mInneOutCallReceiver;

    public AddressService() {
    }

    @Override
    public void onCreate() {
        // 第一次开启以后就需要管理吐司的显示
        // 监听电弧状态
        Object systemService = getSystemService(Context.TELEPHONY_SERVICE);
        mTelephonyManager = (TelephonyManager) systemService;
        mMyPhoneStateListener = new MyPhoneStateListener();
        mTelephonyManager.listen(mMyPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        //监听拨出电话的广播(动态监听)
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
        // h创建响应的广播接受者
        mInneOutCallReceiver = new InneOutCallReceiver();
        //注册光笔接受者
        registerReceiver(mInneOutCallReceiver, intentFilter);

        super.onCreate();
    }

    class InneOutCallReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            // 接收到此广播后需要显示自定义的吐司，并可侠士归属地号码。
            String phone = getResultData(); // 获取播出电话号码的字符串
            showToast(phone);
        }
    }

    class MyPhoneStateListener extends PhoneStateListener {
        // 手动重写，电话状态改变会触发的方法

        @Override
        public void onCallStateChanged(int state, String phoneNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:
                    // 空闲
                    Log.i(TAG, "onCallStateChanged");
                    if (mViewToast != null && mViewToast != null) {
                        mWindowManager.removeViewImmediate(mViewToast);
                    }
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    // 电话摘机状态。
                    Log.i(TAG, "电话摘机状态");
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    // 响铃状态
                    Log.i(TAG, "响铃状态");
                    showToast(phoneNumber);
                    break;
            }
            super.onCallStateChanged(state, phoneNumber);
        }

    }

    private void showToast(String phone) {
//        Toast.makeText(getApplicationContext(), "dd", Toast.LENGTH_LONG).show();

        // 自定义吐司
        final WindowManager.LayoutParams mParams = new WindowManager.LayoutParams();
        final WindowManager.LayoutParams params = mParams;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.format = PixelFormat.TRANSLUCENT;
        params.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        params.gravity = Gravity.LEFT + Gravity.TOP; // 在左上角显示
        params.setTitle("Toast");

        // 吐司的显示效果
        mViewToast = View.inflate(this, R.layout.toast_view, null);
        mTv_address = (TextView) mViewToast.findViewById(R.id.tv_toast);

        //读取吐司的坐标值
        //左上角的x的坐标
        params.x = SpUtil.getInt(getApplicationContext(), ConstantValue.LOCATION_X, 0);
        params.y = SpUtil.getInt(getApplicationContext(), ConstantValue.LOCATION_Y, 0);

        // 从sp中获取索引，匹配图片
        int[] colorId = {R.drawable.call_locate_white, R.drawable.call_locate_orange, R.drawable.call_locate_blue,
                R.drawable.call_locate_gray, R.drawable.call_locate_green};
        int toastStyleIndex = SpUtil.getInt(getApplicationContext(), ConstantValue.TOAST_STYLE, colorId[0]);
        mTv_address.setBackgroundResource(colorId[toastStyleIndex]);

        //将吐司挂在到window的窗体上
        mWindowManager.addView(mViewToast, mParams);

        // 获取到来电号码以后，需要做来电号码查询
        query(phone);
    }

    private void query(final String phone) {
        new Thread() {
            @Override
            public void run() {
                mAddress = AddressDao.getAddress(phone);
                mHandler.sendEmptyMessage(0);
            }
        }.start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        // 取消电话监听
        if (mTelephonyManager != null && mMyPhoneStateListener != null) {
            mTelephonyManager.listen(mMyPhoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
        // 去电广播接受者的注销
        if(mInneOutCallReceiver != null) {
            unregisterReceiver(mInneOutCallReceiver);
        }
        super.onDestroy();
    }
}
