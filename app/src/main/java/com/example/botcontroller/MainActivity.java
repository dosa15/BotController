package com.example.botcontroller;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ToggleButton;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.UUID;

import io.github.controlwear.virtual.joystick.android.JoystickView;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    public static final String TAG = "MAIN ACTIVITY";
    BluetoothAdapter mBluetoothAdapter;
    Button E_D_Discoverable;
    BluetoothConnectionService mBluetoothConnection;
    Button btnStartConnection;

    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    BluetoothDevice mBTDevice;

    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();
    public DeviceListAdapter mDeviceListAdapter;
    ListView lvNewDevices;

    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(mBluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "onReceive: State OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "mBroadcastReceiver: State Turning OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "mBroadcastReceiver: State ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "mBroadcastReceiver: State Turning ON");
                        break;
                }
            }
        }
    };

    private final BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {
                final int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);

                switch (mode) {
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Enabled");
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability disabled. Able to receive connections");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Disabled. Unable to receive connections");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d(TAG, "mBroadcastReceiver2: Connecting...");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d(TAG, "mBroadcastReceiver2: Connected");
                        break;
                }
            }
        }
    };

    private BroadcastReceiver mBroadcastReceiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "Action Found!");

            if(action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mBTDevices.add(device);
                Log.d(TAG, "onReceive: " + device.getName() + ": " + device.getAddress());
                mDeviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view, mBTDevices);
                lvNewDevices.setAdapter(mDeviceListAdapter);
            }
        }
    };

    private final BroadcastReceiver mBroadcastReceiver4 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //3 cases:
                //case1: bonded already
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED){
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDED.");
                    mBTDevice = mDevice;
                }
                //case2: creating a bone
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDING.");
                }
                //case3: breaking a bond
                if (mDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                    Log.d(TAG, "BroadcastReceiver: BOND_NONE.");
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy() called.");
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver1);
        unregisterReceiver(mBroadcastReceiver2);
        unregisterReceiver(mBroadcastReceiver3);
        unregisterReceiver(mBroadcastReceiver4);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button BTbutton = findViewById(R.id.power);
        E_D_Discoverable = findViewById(R.id.discoverable);
        lvNewDevices = findViewById(R.id.lvNewDevices);
        mBTDevices = new ArrayList<>();

        btnStartConnection = findViewById(R.id.connect);

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver4, filter);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        lvNewDevices.setOnItemClickListener(MainActivity.this);

        BTbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: enabling/disabling bluetooth.");
                enableDisableBT();
            }
        });

        btnStartConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startConnection();
            }
        });

        final JoystickView joystickLeft = findViewById(R.id.joystickView_left);
        final int[] a = new int[1];
        joystickLeft.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                    turnBot(joystickLeft, String.valueOf(angle));
                }
            });
        }


    //create method for starting connection
    //***remember the connection will fail and app will crash if you haven't paired first

    public void botMovement(String i) {
        try {
            if (mBluetoothConnection != null)
                mBluetoothConnection.write(i.getBytes());
        }
        catch (Exception ignored){}
    }

    public void startConnection(){
        startBTConnection(mBTDevice, MY_UUID_INSECURE);
    }

    /**
     * starting chat service method
     */
    public void startBTConnection(BluetoothDevice device, UUID uuid){
        Log.d(TAG, "startBTConnection: Initializing RFCOM Bluetooth Connection.");
        mBluetoothConnection.startClient(device,uuid);
    }


        public void enableDisableBT() {
            if(mBluetoothAdapter == null) {
                Log.d(TAG, "No BT capabilities");
            }
            if(!mBluetoothAdapter.isEnabled()) {
                Log.d(TAG, "enableDisableBT: enabling BT.");
                Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivity(enableBTIntent);

                IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
                registerReceiver(mBroadcastReceiver1, BTIntent);
            }
            if(mBluetoothAdapter.isEnabled()) {
                Log.d(TAG, "enableDisableBT: disabling BT.");
                mBluetoothAdapter.disable();

                IntentFilter BTIntent= new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
                registerReceiver(mBroadcastReceiver1, BTIntent);
            }
        }

    public void enableDisable_Discoverable(View view) {
        Log.d(TAG, "Making device discoverable for 300 seconds");

        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);

        IntentFilter intentFilter = new IntentFilter(mBluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(mBroadcastReceiver2, intentFilter);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void btnDiscover(View view) {
        Log.d(TAG, "Looking for unpaired devices");

        if(mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
            Log.d(TAG, "btnDiscover: Cancelling discovery");

            checkBTPermissions();

            mBluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
        }
        if(!mBluetoothAdapter.isDiscovering()){

            //check BT permissions in manifest
            checkBTPermissions();

            mBluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkBTPermissions() {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {

                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
            }
        }else{
            Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        //first cancel discovery because its very memory intensive.
        mBluetoothAdapter.cancelDiscovery();

        Log.d(TAG, "onItemClick: You Clicked on a device.");
        String deviceName = mBTDevices.get(i).getName();
        String deviceAddress = mBTDevices.get(i).getAddress();

        Log.d(TAG, "onItemClick: deviceName = " + deviceName);
        Log.d(TAG, "onItemClick: deviceAddress = " + deviceAddress);

        //create the bond.
        //NOTE: Requires API 17+? I think this is JellyBean
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2){
            Log.d(TAG, "Trying to pair with " + deviceName);

            mBTDevices.get(i).createBond();
            mBTDevice = mBTDevices.get(i);
            mBluetoothConnection = new BluetoothConnectionService(MainActivity.this);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    public void moveForward(View view) {
        Button btnFWD = findViewById(R.id.forward);
        btnFWD.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) { botMovement("w"); }
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) { botMovement("e"); }
                return true;
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    public void moveBackward(View view) {
        Button btnBWD = findViewById(R.id.backward);
        btnBWD.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) { botMovement("s"); }
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) { botMovement("e"); }
                return true;
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    public void turnBot(JoystickView joystickLeft, final String angle) {
        if (joystickLeft.getButtonDirection() < 0) {
            turnLeft(joystickLeft, angle);
        }
        else if (joystickLeft.getButtonDirection() > 0) {
            turnRight(joystickLeft, angle);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    public void turnLeft(JoystickView joystickLeft, final String angle) {
        joystickLeft.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                String data = "s";
                // Uncomment below line if Arduino can parse the data to process joystick movement using PID
                // data += angle;
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) { botMovement(data); }
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) { botMovement("e"); }
                return true;
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    public void turnRight(JoystickView joystickLeft, final String angle) {
        joystickLeft.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                String data = "d";
                // Uncomment below line if Arduino can parse the data to process joystick movement using PID
                // data += angle;
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) { botMovement(data); }
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) { botMovement("e"); }
                return true;
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    public void frontUp(View view) {
        Button btnFUP = findViewById(R.id.front_up);
        btnFUP.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) { botMovement("t"); }
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) { botMovement("u"); }
                return true;
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    public void frontDown(View view) {
        Button btnFDWN = findViewById(R.id.front_down);
        btnFDWN.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) { botMovement("y"); }
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) { botMovement("u"); }
                return true;
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    public void middleUp(View view) {
        Button btnMUP = findViewById(R.id.middle_up);
        btnMUP.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) { botMovement("v"); }
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) { botMovement("e"); }
                return true;
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    public void middleDown(View view) {
        Button btnMDWN = findViewById(R.id.middle_down);
        btnMDWN.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) { botMovement("b"); }
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) { botMovement("e"); }
                return true;
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    public void backUp(View view) {
        Button btnBUP = findViewById(R.id.back_up);
        btnBUP.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) { botMovement("n"); }
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) { botMovement("e"); }
                return true;
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    public void backDown(View view) {
        Button btnBDWN = findViewById(R.id.back_down);
        btnBDWN.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) { botMovement("m"); }
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) { botMovement("e"); }
                return true;
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    public void useGripper(View view) {
        final ToggleButton btnGRP = findViewById(R.id.gripper_toggle);
        btnGRP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!btnGRP.isChecked()) {
                    btnGRP.setChecked(false);
                    botMovement("g"); }
                else if (btnGRP.isChecked()) {
                    btnGRP.setChecked(true);
                    botMovement("h"); }
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    public void setAutoManual(View view) {
        final ToggleButton btnAM = findViewById(R.id.auto_toggle);
        btnAM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!btnAM.isChecked()) {
                    btnAM.setChecked(false);
                    botMovement("q"); }
                else if (btnAM.isChecked()) {
                    btnAM.setChecked(true);
                    botMovement("z"); }
            }
        });
    }
}
