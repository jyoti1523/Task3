package com.example.task3;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

   private Button button;
    //Declaring the constants
    private static final int REQUEST_ENABLE_BT = 1001;
    private static final String TAG = "ConnectThread";
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    //Variables required for Blutooth Communication
    public static BluetoothAdapter mBluetoothAdapter;
    public static BluetoothSocket mBluetoothSocket;

    //UI Elements
    private TextView text_message;
    private ListView lv_paired_devices;
    private ProgressBar progressBar;

    //Variables for storing and displaying paired devices
    private ArrayList<String> list_paired_devices;
    private ArrayAdapter<String> stringArrayAdapter;

    //Boolean variable to store connection status
    private boolean isBluetoothConnected = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openActivity2();
            }
        });
            //Initialize all UI elements

        text_message =findViewById(R.id.text_message);
        lv_paired_devices = findViewById(R.id.lv_paired_devices);
        progressBar = findViewById(R.id.progress_bluetooth);

        //Initialize ArrayList and ArrayAdapter
        list_paired_devices = new ArrayList<>();
        stringArrayAdapter = new ArrayAdapter<>(getApplicationContext()
                , android.R.layout.simple_list_item_1
                , list_paired_devices);

        //Initialize Bluetooth Adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //Enable Bluetooth if not enabled
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        //Get the MAC Address of clicked device from the listview
        lv_paired_devices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Get MAC Address and Name of clicked device
                String data = ((TextView) view).getText().toString().trim();
                String macAddress = data.substring(data.length() - 17);

                //Initiate the connection
                new ConnectBluetooth().execute(macAddress);
            }
        });

    }
    public void openActivity2(){
        Intent intent = new Intent(this, second.class);

        startActivity(intent);
    }

    public void onClickListPairedDevices(View view) {
        //Clear the list of paired devices
        list_paired_devices.clear();

        //Get paired devices
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                //Get name and mac address of each device
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                String stringList = "Name: " + deviceName + "\n" + "MAC Address: " + deviceHardwareAddress;
                list_paired_devices.add(stringList);
            }

            //Set the list view with paired devices
            lv_paired_devices.setAdapter(stringArrayAdapter);
        }
    }

   /* public void onClickSendMessage(View view) {
        String message = text_message.getText().toString().trim();
        if (!message.equals("")) {
            sendMessage(message);
        }
    }*/

    public void sendMessage(String message) {
       // If connection is there, then send message else display error
        if (isBluetoothConnected) {
            boolean isMessageSent = true;
            try {
                //Send data via output stream
                OutputStream os = mBluetoothSocket.getOutputStream();
                os.write(message.getBytes());
            } catch (IOException e) {
                isMessageSent = false;
                e.printStackTrace();
            }

            //Toast message to user
            if (isMessageSent) {
                Toast.makeText(this, "Sent: " + message, Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Sent: " + message);
            } else {
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Error sending data");
            }
        }
        else {
            Toast.makeText(this, "No connection", Toast.LENGTH_SHORT).show();
        }
    }

    public void onClickEnableBluetooth(View view) {
        //Enable Bluetooth if not enabled
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else
            Toast.makeText(this, "Bluetooth is already enabled.", Toast.LENGTH_SHORT).show();

    }

    public class ConnectBluetooth extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(String... strings) {
            //Connect to another Bluetooth device
            try {
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(strings[0]);
                mBluetoothSocket = device.createRfcommSocketToServiceRecord(UUID.fromString(String.valueOf(MY_UUID)));
                mBluetoothAdapter.cancelDiscovery();
                mBluetoothSocket.connect();
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, e.getMessage());
                isBluetoothConnected = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //Toast a message to user
            if (isBluetoothConnected) {
                Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT).show();
                list_paired_devices.clear();






            } else
                Toast.makeText(MainActivity.this, "Connection Failed", Toast.LENGTH_SHORT).show();

            progressBar.setVisibility(View.INVISIBLE);
        }
    }




}

