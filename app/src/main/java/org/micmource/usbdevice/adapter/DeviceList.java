package org.micmource.usbdevice.adapter;

import android.hardware.usb.UsbDevice;

/**
 * Created by android on 2017/8/7.
 */

public class DeviceList {
    private String key;
    private UsbDevice value;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public UsbDevice getValue() {
        return value;
    }

    public void setValue(UsbDevice value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof DeviceList) {
            DeviceList obj1 = (DeviceList) obj;
            return this.getKey().equals(obj1.getKey());
        }else
            return false;
    }
}
