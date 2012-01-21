/*
  SensorGraph - Example to use with Amarino 2.0
  Copyright (c) 2010 Bonifaz Kaufmann. 
  
  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.

  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public
  License along with this library; if not, write to the Free Software
  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package at.ac.tuwien.igw.group9.molog.log;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import at.abraxas.amarino.Amarino;
import at.abraxas.amarino.AmarinoIntent;
import at.ac.tuwien.igw.group9.molog.R;

public class MoLog extends Activity {

    private static final String TAG = "SensorGraph";

    // change this to your Bluetooth device address
    private static final String DEVICE_ADDRESS = "00:06:66:05:61:E9"; // "00:06:66:03:73:7B";

    private GraphView gsrGraph;
    private GraphView pulseGraph;
    private TextView mValueTV;

    long lastTime = System.currentTimeMillis();

    Camera camera;

    private ArduinoReceiver arduinoReceiver = new ArduinoReceiver();

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.molog);

        // get handles to Views defined in our layout file
        gsrGraph = (GraphView) findViewById(R.id.gsrGraph);
        pulseGraph = (GraphView) findViewById(R.id.pulseGraph);
        mValueTV = (TextView) findViewById(R.id.value);

        gsrGraph.setMaxValue(1024);
        pulseGraph.setMaxValue(1024);
    }

    @Override
    protected void onStart() {
        super.onStart();

        camera = Camera.open();

        camera.startPreview();
        camera.takePicture(shutterCallback, rawCallback, jpegCallback);

        // in order to receive broadcasted intents we need to register our
        // receiver
        registerReceiver(arduinoReceiver, new IntentFilter(AmarinoIntent.ACTION_RECEIVED));

        // this is how you tell Amarino to connect to a specific BT device from
        // within your own code
        Amarino.connect(this, DEVICE_ADDRESS);
    }

    @Override
    protected void onStop() {
        super.onStop();

        camera.release();

        // if you connect in onStart() you must not forget to disconnect when
        // your app is closed
        Amarino.disconnect(this, DEVICE_ADDRESS);

        // do never forget to unregister a registered receiver
        unregisterReceiver(arduinoReceiver);
    }

    /**
     * ArduinoReceiver is responsible for catching broadcasted Amarino events.
     * 
     * It extracts data from the intent and updates the graph accordingly.
     */
    public class ArduinoReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String data = null;

            // the device address from which the data was sent, we don't
            // need it
            // here but to demonstrate how you retrieve it
            final String address = intent.getStringExtra(AmarinoIntent.EXTRA_DEVICE_ADDRESS);

            // the type of data which is added to the intent
            final int dataType = intent.getIntExtra(AmarinoIntent.EXTRA_DATA_TYPE, -1);

            // we only expect String data though, but it is better to check
            // if
            // really string was sent
            // later Amarino will support differnt data types, so far data
            // comes
            // always as string and
            // you have to parse the data to the type you have sent from
            // Arduino, like it is shown below
            if (dataType == AmarinoIntent.STRING_EXTRA) {
                data = intent.getStringExtra(AmarinoIntent.EXTRA_DATA);

                Log.d(TAG, "Received: " + data);

                if (data.length() <= 1)
                    return;

                String type = data.substring(0, 1);

                if (type.equals("G")) {
                    final int value = Integer.parseInt(data.substring(1, data.length()));
                    mValueTV.setText(String.valueOf(value));
                    // since we know that our string value is an int
                    // number
                    // we can parse it to an integer
                    pulseGraph.addDataPoint(value);
                    long currentTimeMillis = System.currentTimeMillis();
                    if (lastTime < currentTimeMillis - 2000) {
                        // camera.startPreview();
                        // camera.takePicture(shutterCallback, rawCallback,
                        // jpegCallback);
                        lastTime = currentTimeMillis;
                    }
                }
            }
        }
    }

    /** Handles data for jpeg picture */
    PictureCallback jpegCallback = new PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            // camera.stopPreview();
            // camera.startPreview();

            FileOutputStream outStream = null;
            try {
                // write to local sandbox file system
                outStream = MoLog.this.openFileOutput("molog.jpg", MODE_WORLD_READABLE);
                // outStream = MoLog.this.openFileOutput("d.jpg", 0);
                // Or write to sdcard
                // outStream = new
                // FileOutputStream(String.format("/CameraImages/d.jpg",
                // System.currentTimeMillis()));
                outStream.write(data);
                outStream.close();
                Log.d(TAG, "onPictureTaken - wrote bytes: " + data.length);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
            }
            Log.d(TAG, "onPictureTaken - jpeg");

            camera.stopPreview();
        }
    };

    ShutterCallback shutterCallback = new ShutterCallback() {
        public void onShutter() {
            Log.d(TAG, "onShutter'd");
        }
    };

    /** Handles data for raw picture */
    PictureCallback rawCallback = new PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d(TAG, "onPictureTaken - raw");
        }
    };
}
