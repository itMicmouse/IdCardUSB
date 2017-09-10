package org.micmource.usbdevice.adapter;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.view.DragAndDropPermissions;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.micmource.usbdevice.R;

import java.util.ArrayList;

/**
 * Created by android on 2017/8/7.
 */

public class DeviceListAdapter extends BaseAdapter {

    private ArrayList<DeviceList> data;
    LayoutInflater inflater;

    public DeviceListAdapter(ArrayList data,Context mContext) {
        this.data = data;
        inflater=LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int i) {
        return data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        View view = convertView;
        if (convertView == null) {
            view = inflater.inflate(R.layout.item_medicinal_resultnew, null);
        }
        ViewHolder mViewHolder = ViewHolder.getHolder(view);

        DeviceList deviceList = data.get(i);
        UsbDevice device = deviceList.getValue();
        String s = "device name: " + device.getDeviceName() + "device product name:"
                + device.getProductName() + "vendor id:" + device.getVendorId() +
                "device serial: " + device.getSerialNumber();
        mViewHolder.mIndex.setText(deviceList.getKey()+"\n"+s);


        return view;
    }

    public void addDevice(UsbDevice device) {
        DeviceList deviceList = new DeviceList();
        deviceList.setKey(device.getDeviceName());
        deviceList.setValue(device);
        data.add(deviceList);
        notifyDataSetChanged();
    }

    public void reMoveDevice(UsbDevice device) {
        DeviceList deviceList = new DeviceList();
        deviceList.setKey(device.getDeviceName());
        deviceList.setValue(device);
        data.remove(deviceList);
        notifyDataSetChanged();
    }


    static class ViewHolder {
        TextView mIndex;

        public static ViewHolder getHolder(View view) {
            Object tag = view.getTag();
            if (tag != null) {
                return (ViewHolder) tag;
            } else {
                ViewHolder viewHolder = new ViewHolder();
                viewHolder.mIndex = (TextView) view.findViewById(R.id.tv_index);
                view.setTag(viewHolder);
                return viewHolder;
            }
        }

    }
}
