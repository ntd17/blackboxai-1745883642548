package com.example.bluetoothscanner.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bluetoothscanner.R;
import com.example.bluetoothscanner.model.BluetoothDeviceModel;
import com.example.bluetoothscanner.utils.BluetoothUtils;

public class DeviceListAdapter extends ListAdapter<BluetoothDeviceModel, DeviceListAdapter.DeviceViewHolder> {

    public DeviceListAdapter() {
        super(new DeviceDiffCallback());
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_device, parent, false);
        return new DeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        BluetoothDeviceModel device = getItem(position);
        holder.bind(device);
    }

    public void clearList() {
        submitList(null);
    }

    static class DeviceViewHolder extends RecyclerView.ViewHolder {
        private final TextView deviceNameText;
        private final TextView deviceAddressText;
        private final TextView rssiText;
        private final ImageView signalStrengthIcon;
        private final TextView deviceStatusText;

        DeviceViewHolder(View itemView) {
            super(itemView);
            deviceNameText = itemView.findViewById(R.id.deviceName);
            deviceAddressText = itemView.findViewById(R.id.deviceAddress);
            rssiText = itemView.findViewById(R.id.rssiValue);
            signalStrengthIcon = itemView.findViewById(R.id.signalStrengthIcon);
            deviceStatusText = itemView.findViewById(R.id.deviceStatus);
        }

        void bind(BluetoothDeviceModel device) {
            deviceNameText.setText(device.getName());
            deviceAddressText.setText(device.getAddress());
            rssiText.setText(device.getFormattedRssi());
            
            // Set signal strength icon
            signalStrengthIcon.setImageResource(
                BluetoothUtils.getSignalStrengthIcon(device.getRssi())
            );
            
            // Set device status (paired/unpaired)
            deviceStatusText.setText(device.isPaired() ? "Paired" : "Not Paired");
            
            // Set signal strength description
            String signalDesc = BluetoothUtils.getRssiDescription(device.getRssi());
            rssiText.setText(String.format("%s (%s)", device.getFormattedRssi(), signalDesc));
        }
    }

    static class DeviceDiffCallback extends DiffUtil.ItemCallback<BluetoothDeviceModel> {
        @Override
        public boolean areItemsTheSame(@NonNull BluetoothDeviceModel oldItem, 
                                     @NonNull BluetoothDeviceModel newItem) {
            return oldItem.getAddress().equals(newItem.getAddress());
        }

        @Override
        public boolean areContentsTheSame(@NonNull BluetoothDeviceModel oldItem, 
                                        @NonNull BluetoothDeviceModel newItem) {
            return oldItem.getRssi() == newItem.getRssi() &&
                   oldItem.getName().equals(newItem.getName()) &&
                   oldItem.isPaired() == newItem.isPaired();
        }
    }
}
