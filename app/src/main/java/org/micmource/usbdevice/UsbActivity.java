package org.micmource.usbdevice;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.micmource.usbdevice.adapter.DeviceList;
import org.micmource.usbdevice.adapter.DeviceListAdapter;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class UsbActivity extends Activity {
    private static final String TAG = "UsbActivity";
    private ListView lv_listdevice;
    private TextView tv_info;
    private ImageView iv_head;
    private ArrayList<String> list2;
    private DeviceListAdapter adapterdevice;
    private UsbManager mUsbManager;
    private ArrayList<DeviceList> data;

    public static final String ACTION_DEVICE_PERMISSION = "com.linc.USB_PERMISSION";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usb);
        data = new ArrayList<>();


        lv_listdevice = findViewById(R.id.lv_listdevice);
        tv_info = findViewById(R.id.tv_info);
        iv_head = findViewById(R.id.iv_head);

        initData();
        addListener();
    }

    private void addListener() {

        //region  获取安全模块号指令（PC 到 读卡器）
        findViewById(R.id.btn_safe_id).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int ret = -12;
                byte bysCmdReadSafeCode[] = {(byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0x96, 0x69, 0x00, 0x03, 0x12, (byte)0xFF, (byte)0xEE};

                // 1,发送准备命令
                ret = mDeviceConnection.bulkTransfer(epOut, bysCmdReadSafeCode,
                        bysCmdReadSafeCode.length, 5000);
                Log.i(TAG, "已经发送!");

                // 2,接收发送成功信息
                Receiveytes = new byte[32];
                ret = mDeviceConnection.bulkTransfer(epIn, Receiveytes,
                        Receiveytes.length, 10000);
                Log.i(TAG, "接收返回值:" + String.valueOf(ret));
                if (ret != 32) {
                    DisplayToast("接收返回值" + String.valueOf(ret));
//                return;
                } else {
                    // 查看返回值
                    Log.i(TAG, Receiveytes.toString());
                }
                String s = data2hexstring(Receiveytes, Receiveytes.length);

                String strCode = "";
                String strTemp = null;
                long lTemp = 0;
                int i = 0;
                int j = 0;

                //05.01
                byte bysCode01A[] = new byte[4];
                bysCode01A[0] = 0;
                bysCode01A[1] = 0;
                bysCode01A[2] = Receiveytes[11];
                bysCode01A[3] = Receiveytes[10];
                lTemp = unsigned4BytesToInt(bysCode01A, 0);
                strTemp = Long.toString(lTemp);
                j = 2 - strTemp.length();
                for (i = 0; i < j; i++) {
                    strCode += "0";
                }
                strCode += Long.toString(lTemp);

                strCode += ".";

                byte bysCode01B[] = new byte[4];
                bysCode01B[0] = 0;
                bysCode01B[1] = 0;
                bysCode01B[2] = Receiveytes[13];
                bysCode01B[3] = Receiveytes[12];
                lTemp = unsigned4BytesToInt(bysCode01B, 0);
                strTemp = Long.toString(lTemp);
                j = 2 - strTemp.length();
                for (i = 0; i < j; i++) {
                    strCode += "0";
                }
                strCode += Long.toString(lTemp);

                //分隔符'-'
                strCode += "-";

                //20101129
                byte bysCode02[] = new byte[4];
                bysCode02[0] = Receiveytes[17];
                bysCode02[1] = Receiveytes[16];
                bysCode02[2] = Receiveytes[15];
                bysCode02[3] = Receiveytes[14];
                lTemp = unsigned4BytesToInt(bysCode02, 0);
                strTemp = Long.toString(lTemp);
                strCode += Long.toString(lTemp);

                //分隔符'-'
                strCode += "-";

                //1228293
                byte bysCode03[] = new byte[4];
                bysCode03[0] = Receiveytes[21];
                bysCode03[1] = Receiveytes[20];
                bysCode03[2] = Receiveytes[19];
                bysCode03[3] = Receiveytes[18];
                lTemp = unsigned4BytesToInt(bysCode03, 0);
                strTemp = Long.toString(lTemp);
                j = 10 - strTemp.length();
                for (i = 0; i < j; i++) {
                    strCode += "0";
                }
                strCode += Long.toString(lTemp);

                //分隔符'-'
                strCode += "-";

                //296863149
                byte bysCode04[] = new byte[4];
                bysCode04[0] = Receiveytes[25];
                bysCode04[1] = Receiveytes[24];
                bysCode04[2] = Receiveytes[23];
                bysCode04[3] = Receiveytes[22];
                lTemp = unsigned4BytesToInt(bysCode04, 0);
                strTemp = Long.toString(lTemp);
                j = 10 - strTemp.length();
                for (i = 0; i < j; i++) {
                    strCode += "0";
                }
                strCode += Long.toString(lTemp);
                Log.e("aaaaaaaa",strCode);

                tv_info.append("\n安全模块号"+strCode);
            }
        });
        //endregion

        //region 检测安全模块状态（通常用来寻找读卡器连接的串口号）
        findViewById(R.id.btn_checkstyle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int ret = -12;
                byte bysCmdReadSafeCode[] = {(byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0x96, 0x69, 0x00, 0x03, 0x11, (byte)0xFF, (byte)0xED};

                // 1,发送准备命令
                ret = mDeviceConnection.bulkTransfer(epOut, bysCmdReadSafeCode,
                        bysCmdReadSafeCode.length, 5000);
                Log.i(TAG, "已经发送!");

                // 2,接收发送成功信息
                Receiveytes = new byte[32];
                ret = mDeviceConnection.bulkTransfer(epIn, Receiveytes,
                        Receiveytes.length, 10000);
                Log.i(TAG, "接收返回值:" + String.valueOf(ret));
                if (ret != 32) {
                    DisplayToast("接收返回值" + String.valueOf(ret));
//                return;
                } else {
                    // 查看返回值
                    Log.i(TAG, Receiveytes.toString());
                }
                String s = data2hexstring(Receiveytes, Receiveytes.length);

                tv_info.append("\n安全模块状态"+s);
            }
        });
        //endregion

        //region  寻找卡片
        findViewById(R.id.btn_findcard).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int ret = -12;
                byte bysCmdReadSafeCode[] = {(byte)0xAA, (byte)0xAA, (byte)0xAA, (byte) 0x96, 0x69, 0x00, 0x03, 0x20, 0x01, 0x22};

                // 1,发送准备命令
                ret = mDeviceConnection.bulkTransfer(epOut, bysCmdReadSafeCode,
                        bysCmdReadSafeCode.length, 5000);
                Log.i(TAG, "已经发送!");

                // 2,接收发送成功信息
                Receiveytes = new byte[32];
                ret = mDeviceConnection.bulkTransfer(epIn, Receiveytes,
                        Receiveytes.length, 10000);
                Log.i(TAG, "接收返回值:" + String.valueOf(ret));
                if (ret != 32) {
                    DisplayToast("接收返回值" + String.valueOf(ret));
//                return;
                } else {
                    // 查看返回值
                    Log.i(TAG, Receiveytes.toString());
                }
                String s = data2hexstring(Receiveytes, Receiveytes.length);

                tv_info.append("\n寻找卡片："+s);
            }
        });
        //endregion

        //region  选取卡片
        findViewById(R.id.btn_selectcard).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int ret = -12;
                byte bysCmdReadSafeCode[] = {(byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0x96, 0x69, 0x00, 0x03, 0x20, (byte)0x02, (byte)0x21};

                // 1,发送准备命令
                ret = mDeviceConnection.bulkTransfer(epOut, bysCmdReadSafeCode,
                        bysCmdReadSafeCode.length, 5000);
                Log.i(TAG, "已经发送!");

                // 2,接收发送成功信息
                Receiveytes = new byte[32];
                ret = mDeviceConnection.bulkTransfer(epIn, Receiveytes,
                        Receiveytes.length, 10000);
                Log.i(TAG, "接收返回值:" + String.valueOf(ret));
                if (ret != 32) {
                    DisplayToast("接收返回值" + String.valueOf(ret));
//                return;
                } else {
                    // 查看返回值
                    Log.i(TAG, Receiveytes.toString());
                }
                String s = data2hexstring(Receiveytes, Receiveytes.length);

                tv_info.append("\n选取卡片："+s);
            }
        });
        //endregion

        //region  读取卡片
        findViewById(R.id.btn_readcard).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int ret = -12;
                byte bysCmdReadSafeCode[] = {(byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0x96, 0x69, 0x00, 0x03, 0x30, (byte)0x01, (byte)0x32};

                // 1,发送准备命令
                ret = mDeviceConnection.bulkTransfer(epOut, bysCmdReadSafeCode,
                        bysCmdReadSafeCode.length, 5000);
                Log.i(TAG, "已经发送!");

                // 2,接收发送成功信息
                Receiveytes = new byte[2000];
                ret = mDeviceConnection.bulkTransfer(epIn, Receiveytes,
                        Receiveytes.length, 10000);
                Log.i(TAG, "接收返回值:" + String.valueOf(ret));
                if (ret != 32) {
                    DisplayToast("接收返回值" + String.valueOf(ret));
//                return;
                } else {
                    // 查看返回值
                    Log.i(TAG, Receiveytes.toString());
                }
//                String s = data2hexstring(Receiveytes, Receiveytes.length);


                int i = 0;
                int j = 0;

                int iLen1Len2 = 0;
                int iOffset = 16;
                int iTextSize = 0;
                int iPhotoSize = 0;
                int iFingerSize = 0;

                byte bysName[] = new byte[30];
                byte bysSexCode[] = new byte[2];
                byte bysNationCode[] = new byte[4];
                byte bysBirth[] = new byte[16];
                byte bysAddr[] = new byte[70];
                byte bysIdCode[] = new byte[36];
                byte bysIssue[] = new byte[30];
                byte bysBeginDate[] = new byte[16];
                byte bysEndDate[] = new byte[16];

                iLen1Len2 = Receiveytes[5] << 8;
                iLen1Len2 += Receiveytes[6];

                iTextSize = Receiveytes[10] << 8 + Receiveytes[11];
                iPhotoSize = Receiveytes[12] << 8 + Receiveytes[13];
                iFingerSize = Receiveytes[14] << 8 + Receiveytes[15];

                //截取姓名
                j = 0;
                for (i = iOffset; i < (iOffset + 30); i++) {
                    bysName[j] = Receiveytes[i];
                    j++;
                }
                showString("姓名: ");
                try {
                    showString(new String(bysName, "UTF-16LE"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                showString("\n");
                iOffset += 30;

                //截取性别代码
                j = 0;
                for (i = iOffset; i < (iOffset + 2); i++) {
                    bysSexCode[j] = Receiveytes[i];
                    j++;
                }
                showString("性别代码: ");
                showString(new String(bysSexCode));
                showString("\n");
                iOffset += 2;

                //截取民族代码
                j = 0;
                for (i = iOffset; i < (iOffset + 4); i++) {
                    bysNationCode[j] = Receiveytes[i];
                    j++;
                }
                showString("民族代码: ");
                showString(new String(bysNationCode));
                showString("\n");
                iOffset += 4;

                //截取生日
                j = 0;
                for (i = iOffset; i < (iOffset + 16); i++) {
                    bysBirth[j] = Receiveytes[i];
                    j++;
                }
                showString("出生日期: ");
                showString(new String(bysBirth));
                showString("\n");
                iOffset += 16;

                //截取地址
                j = 0;
                for (i = iOffset; i < (iOffset + 70); i++) {
                    bysAddr[j] = Receiveytes[i];
                    j++;
                }
                showString("地址: ");
                try {
                    showString(new String(bysAddr, "UTF-16LE"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                showString("\n");
                iOffset += 70;

                //截取身份证号
                j = 0;
                for (i = iOffset; i < (iOffset + 36); i++) {
                    bysIdCode[j] = Receiveytes[i];
                    j++;
                }
                showString("身份证号: ");
                showString(new String(bysIdCode));
                showString("\n");
                iOffset += 36;

                //截取签发机关
                j = 0;
                for (i = iOffset; i < (iOffset + 30); i++) {
                    bysIssue[j] = Receiveytes[i];
                    j++;
                }
                showString("签发机关: ");
                try {
                    showString(new String(bysIssue, "UTF-16LE"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                showString("\n");
                iOffset += 30;

                //截取有效期开始日期
                j = 0;
                for (i = iOffset; i < (iOffset + 16); i++) {
                    bysBeginDate[j] = Receiveytes[i];
                    j++;
                }
                showString("有效开始日期: ");
                showString(new String(bysBeginDate));
                showString("\n");
                iOffset += 16;

                //截取有效期结束日期
                j = 0;
                for (i = iOffset; i < (iOffset + 16); i++) {
                    bysEndDate[j] = Receiveytes[i];
                    j++;
                }

                showString("有效截止日期: ");
                if (bysEndDate[0] >= '0' && bysEndDate[0] <= '9') {
                    showString(new String(bysEndDate));
                } else {
                    try {
                        showString(new String(bysEndDate, "UTF-16LE"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                showString("\n");
                iOffset += 16;

                //照片
                byte[] bys_wlt = new byte[1024];
                byte[] bys_bmp = new byte[14 + 40 + 308 * 126];
                for (i = 0; i < iPhotoSize; i++) {
                    bys_wlt[i] = Receiveytes[16 + iTextSize + i];
                }
                showString("\n");

                //指纹
                if (0 == iFingerSize) {
                    showString("指纹: 无.\n");
                }
                showString("指纹: 有.\n");
                

            }
        });
        //endregion

        // region  清空数据
        findViewById(R.id.btn_clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tv_info.setText("");
            }
        });
        //endregion


    }

    protected void showString(final String string) {
        if (tv_info != null) {
            tv_info.append(string);
        }
    }

    protected void showBitmap(final Bitmap bitmap) {
        ImageSpan imgSpan = new ImageSpan(this, bitmap);
        final SpannableString spanString = new SpannableString("icon");
        spanString.setSpan(imgSpan, 0, 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (tv_info != null) {
            tv_info.append(spanString);
        }
    }

    /**
     * 将一个4byte的数组转换成32位的int
     *
     * @param buf bytes buffer
     * @param pos byte[]中开始转换的位置
     * @return convert result
     */
    protected long unsigned4BytesToInt(byte[] buf, int pos) {
        int firstByte = 0;
        int secondByte = 0;
        int thirdByte = 0;
        int fourthByte = 0;
        int index = pos;
        firstByte = (0x000000FF & ((int) buf[index]));
        secondByte = (0x000000FF & ((int) buf[index + 1]));
        thirdByte = (0x000000FF & ((int) buf[index + 2]));
        fourthByte = (0x000000FF & ((int) buf[index + 3]));
        index = index + 4;
        return ((long) (firstByte << 24 | secondByte << 16 | thirdByte << 8 | fourthByte)) & 0xFFFFFFFFL;
    }

    private void initData() {

        adapterdevice = new DeviceListAdapter(data,UsbActivity.this);

        lv_listdevice.setAdapter(adapterdevice);

        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceHashMap = mUsbManager.getDeviceList();
        Iterator<Map.Entry<String, UsbDevice>> iterator1 = deviceHashMap.entrySet().iterator();
        DeviceList mDeviceList;
        while (iterator1.hasNext()){
            Map.Entry<String, UsbDevice> next = iterator1.next();
            mDeviceList = new DeviceList();
            mDeviceList.setKey(next.getKey());
            mDeviceList.setValue(next.getValue());
            data.add(mDeviceList);
            adapterdevice.notifyDataSetChanged();
        }
        lv_listdevice.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                DeviceList deviceList = data.get(i);
                UsbDevice value = deviceList.getValue();


                if(mUsbManager.hasPermission(value)){//批准 进行业务操作
                    business(value);
                }else {
                    PendingIntent mPermissionIntent = PendingIntent.getBroadcast(UsbActivity.this, 0, new Intent(ACTION_DEVICE_PERMISSION), 0);
                    mUsbManager.requestPermission(value,mPermissionIntent);
                }


            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter usbFilter = new IntentFilter();
        usbFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        usbFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        usbFilter.addAction(ACTION_DEVICE_PERMISSION);
        registerReceiver(mUsbReceiver, usbFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mUsbReceiver);
    }

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            System.out.println("BroadcastReceiver in");

            UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                System.out.println("ACTION_USB_DEVICE_ATTACHED");
                if (device != null) {
                    adapterdevice.addDevice(device);
                    adapterdevice.notifyDataSetChanged();
                }
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                System.out.println("ACTION_USB_DEVICE_DETACHED");
                if (device != null) {
                    adapterdevice.reMoveDevice(device);
                    adapterdevice.notifyDataSetChanged();
                }
            } else if (ACTION_DEVICE_PERMISSION.equals(action)) {
                synchronized (this) {
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {//批准 进行业务操作
                            business(device);

                        }
                    } else {//未批准

                    }
                }
            }
        }
    };

    private UsbDeviceConnection mDeviceConnection;

    public void business(final UsbDevice device){
        System.out.println("业务操作");

        UsbDeviceConnection connection = null;
        connection = mUsbManager.openDevice(device);

        for (int i = 0; i < device.getInterfaceCount();) {
            // 获取设备接口，一般都是一个接口，你可以打印getInterfaceCount()方法查看接
            // 口的个数，在这个接口上有两个端点，OUT 和 IN
            UsbInterface intf = device.getInterface(i);
            Log.d(TAG, i + " " + intf);
            mInterface = intf;
            break;
        }

        // 打开设备，获取 UsbDeviceConnection 对象，连接设备，用于后面的通讯
        connection = mUsbManager.openDevice(device);
        if (connection == null) {
            return;
        }
        if (connection.claimInterface(mInterface, true)) {
            Log.i(TAG, "找到接口");
            mDeviceConnection = connection;
            // 用UsbDeviceConnection 与 UsbInterface 进行端点设置和通讯
            getEndpoint(mDeviceConnection, mInterface);
        } else {
            connection.close();
        }


    }

    UsbInterface  mInterface;

    private UsbEndpoint epOut;
    private UsbEndpoint epIn;

    // 用UsbDeviceConnection 与 UsbInterface 进行端点设置和通讯
    private void getEndpoint(UsbDeviceConnection connection, UsbInterface intf) {
        if (intf.getEndpoint(1) != null) {
            epOut = intf.getEndpoint(1);
        }
        if (intf.getEndpoint(0) != null) {
            epIn = intf.getEndpoint(0);
        }
    }

    private byte[] Sendbytes; // 发送信息字节
    private byte[] Receiveytes; // 接收信息字节


    //将数据转换为16进制形式字符串
    protected String data2hexstring(final byte[] buffer, final int size) {
        String dataString = new String();
        String tempString = new String();

        int i = 0;

        for (i = 0; i < size; i++) {
            tempString = Integer.toHexString(buffer[i] & 0xFF);
            if (1 == tempString.length()) {
                dataString += "0";
            }
            dataString += (Integer.toHexString(buffer[i] & 0xFF)+",");
            //dataString += Integer.toString(buffer[i]);

            //dataString += Byte.toString(buffer[i]);
        }
        Log.e(TAG,dataString);
        return dataString;
    }

    public void DisplayToast(CharSequence str) {
        Toast toast = Toast.makeText(this, str, Toast.LENGTH_LONG);
        // 设置Toast显示的位置
        toast.setGravity(Gravity.TOP, 0, 200);
        // 显示Toast
        toast.show();
    }


}
