package com.example.botcontroller;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import io.github.controlwear.virtual.joystick.android.JoystickView;

public class MainActivity extends AppCompatActivity {

    public Button button;
    public TextView editText;
    public BluetoothAdapter btAdapter;
    public BluetoothDevice btDevice;
    public BluetoothSocket btSocket;

    public static final String SERVICE_ID = "00001101-0000-1000-8000-00805f9b34fb"; //SPP UUID
    public static final String SERVICE_ADDRESS = "00:13:EF:00:55:FA"; // HC-05 BT ADDRESS
/*
    private TextView mTextViewAngleLeft;
    private TextView mTextViewStrengthLeft;

    private TextView mTextViewAngleRight;
    private TextView mTextViewStrengthRight;
    private TextView mTextViewCoordinateRight;
*/

    /*
    //private static final int REQUEST_ENABLE_BT = 12;
    //    private TextView out;
    //    private BluetoothAdapter adapter;
    //
    //    @Override
    //    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    //        // TODO Auto-generated method stub
    //        super.onActivityResult(requestCode, resultCode, data);
    //        out.setText("");
    //        setBluetoothData();
    //    }
    //
    //    @SuppressLint("HardwareIds")
    //    private void setBluetoothData() {
    //
    //        // Getting the Bluetooth adapter
    //        adapter = BluetoothAdapter.getDefaultAdapter();
    ////        out.append("\nAdapter: " + adapter.toString() + "\n\nName: " + adapter.getName() + "\nAddress: " + adapter.getAddress());
    //
    //        // Check for Bluetooth support in the first place
    //        // Emulator doesn't support Bluetooth and will return null
    //
    //        if (adapter == null) {
    //            Toast.makeText(this, "Bluetooth NOT supported. Aborting.",
    //                    Toast.LENGTH_LONG).show();
    //        }
    //
    //        // Starting the device discovery
    ////        out.append("\n\nStarting discovery...");
    //        adapter.startDiscovery();
    ////        out.append("\nDone with discovery...\n");
    //
    //        // Listing paired devices
    //        out.append("\nDevices Paired:");
    //        Set<BluetoothDevice> devices = adapter.getBondedDevices();
    //        for (BluetoothDevice device : devices) {
    //            out.append("\nFound device: " + device.getName() + " Add: " + device.getAddress());
    //        }
    //    }
    */
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.power);
        editText = findViewById(R.id.textView1);

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        btDevice = btAdapter.getRemoteDevice(SERVICE_ADDRESS);

        if (btAdapter == null) {
            Toast.makeText(getApplicationContext(), "Bluetooth not available", Toast.LENGTH_LONG).show();
            editText.setText("Bluetooth not available");
        } else {
            if (!btAdapter.isEnabled()) {
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, 3);
            } else {
                ConnectThread connectThread = new ConnectThread(btDevice);
                connectThread.start();
            }
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btSocket != null) {
                    try{
                        OutputStream out = btSocket.getOutputStream();
                        out.write((editText.getText().toString() + "\r\n").getBytes());
                    }catch(IOException ignored) {
                    }
                }
            }
        });
        /*
//        out = findViewById(R.id.textView1);
//        setBluetoothData();
//
//        if (Connections.blueTooth()) {
//            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
//        }
        */
//        mTextViewAngleLeft = (TextView) findViewById(R.id.textView_angle_left);
//        mTextViewStrengthLeft = (TextView) findViewById(R.id.textView_strength_left);

            JoystickView joystickLeft = findViewById(R.id.joystickView_left);
            joystickLeft.setOnMoveListener(new JoystickView.OnMoveListener() {
                @Override
                public void onMove(int angle, int strength) {
//                mTextViewAngleLeft.setText(angle + "°");
//                mTextViewStrengthLeft.setText(strength + "%");
                }
            });
        }

        private class ConnectThread extends Thread {
            private final BluetoothSocket thisSocket;
            private final BluetoothDevice thisDevice;

            @SuppressLint("SetTextI18n")
            public ConnectThread(BluetoothDevice device) {
                BluetoothSocket tmp = null;
                thisDevice = device;

                try {
                    tmp = thisDevice.createRfcommSocketToServiceRecord(UUID.fromString(SERVICE_ID));
                } catch (IOException e) {
                    editText.setText("Can't connect to service");
                }
                thisSocket = tmp;
            }

            public void run() {
                // Cancel discovery because it otherwise slows down the connection.
                btAdapter.cancelDiscovery();

                try {
                    thisSocket.connect();
                    editText.append("Connected to HC05");
                } catch (IOException connectException) {
                    try {
                        thisSocket.close();
                    } catch (IOException closeException) {
                        editText.append("Can't close socket");
                    }
                    return;
                }

                btSocket = thisSocket;

            }
        }

//        mTextViewAngleRight = (TextView) findViewById(R.id.textView_angle_right);
//        mTextViewStrengthRight = (TextView) findViewById(R.id.textView_strength_right);
//        mTextViewCoordinateRight = findViewById(R.id.textView_coordinate_right);
//
//        final JoystickView joystickRight = (JoystickView) findViewById(R.id.joystickView_right);
//        joystickRight.setOnMoveListener(new JoystickView.OnMoveListener() {
//            @SuppressLint("DefaultLocale")
//            @Override
//            public void onMove(int angle, int strength) {
//                mTextViewAngleRight.setText(angle + "°");
//                mTextViewStrengthRight.setText(strength + "%");
//                mTextViewCoordinateRight.setText(
//                        String.format("x%03d:y%03d",
//                                joystickRight.getNormalizedX(),
//                                joystickRight.getNormalizedY())
//                );
//            }
//        });



}