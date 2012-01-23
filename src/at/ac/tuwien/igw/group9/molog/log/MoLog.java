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

import java.io.File;
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
import at.ac.tuwien.igw.group9.molog.db.LogData;

public class MoLog extends Activity {

    private static final String TMP_JPG = "tmp.jpg";

	private static final String TAG = "SensorGraph";

    // change this to your Bluetooth device address
    private static final String DEVICE_ADDRESS = "00:06:66:05:61:E9"; // "00:06:66:03:73:7B";

    private GraphView gsrGraph;
    private GraphView pulseGraph;
    private TextView mGsrValueTV;
    private TextView mPulseValue;
    
    // current read values;
    int gsrValue = 1023;
	int pulseValue = 255;
	
	// GSR
	int oldCompGsrValue = 0;
	
	private static final int MIN_DELTA_GSR = 40;
	private static final int MAX_DELTA_GSR = 200; 
	
	// Pulse
	int oldCompPulseValue = 0;
	
	private static final int MIN_DELTA_PULSE = 10;
	private static final int MAX_DELTA_PULSE = 30;

    long lastTime = System.currentTimeMillis();

    Camera camera;
    LogData logData;

    private ArduinoReceiver arduinoReceiver = new ArduinoReceiver();

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.molog);

        // get handles to Views defined in our layout file
        gsrGraph = (GraphView) findViewById(R.id.gsrGraph);
        pulseGraph = (GraphView) findViewById(R.id.pulseGraph);
        
        mGsrValueTV = (TextView) findViewById(R.id.gsrValue);
        mPulseValue = (TextView) findViewById(R.id.pulseValue);

        gsrGraph.setMaxValue(1024);
        pulseGraph.setMaxValue(256);
    }

    @Override
    protected void onStart() {
        super.onStart();

        camera = Camera.open();

        // in order to receive broadcasted intents we need to register our
        // receiver
        registerReceiver(arduinoReceiver, new IntentFilter(AmarinoIntent.ACTION_RECEIVED));

        // this is how you tell Amarino to connect to a specific BT device from
        // within your own code
        Amarino.connect(this, DEVICE_ADDRESS);
        
        logData = new LogData(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        camera.release();
        logData.closeDB();
        
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

//                Log.d(TAG, "Received: " + data);

                if (data.length() <= 1)
                    return;

                String type = data.substring(0, 1);

                if (type.equals("G")) {
                    gsrValue = Integer.parseInt(data.substring(1, data.length()));
                    mGsrValueTV.setText("GSR: " + String.valueOf(gsrValue));
                    gsrGraph.addDataPoint(gsrValue);
                    triggerCamera();
                } else if (type.equals("P")) {
                	pulseValue = Integer.parseInt(data.substring(1, data.length()));
                	mPulseValue.setText("Pulse: " + String.valueOf(pulseValue));
                    pulseGraph.addDataPoint(pulseValue);
                    triggerCamera();
                }
            }
        }
    }
    
    private void triggerCamera() {
    	long currentTimeMillis = System.currentTimeMillis();
        if (lastTime < currentTimeMillis - 2000) {
        	
        	int gsrDelta = gsrValue - oldCompGsrValue;
        	int pulseDelta = pulseValue - oldCompPulseValue;
        	
        	if ( (gsrDelta > MIN_DELTA_GSR && gsrDelta < MAX_DELTA_GSR) || (pulseDelta > MIN_DELTA_PULSE && pulseDelta < MAX_DELTA_PULSE) ) {
        		String fileName = currentTimeMillis + ".jpg";
        		File from = this.getFileStreamPath(TMP_JPG);
        		File to = this.getFileStreamPath(fileName);
        		from.renameTo(to); 
        		logData.insert(currentTimeMillis, gsrValue, pulseValue, fileName);
        		Log.e(TAG, "Captured photo. gsrValue=" + gsrValue + ", pulseValue=" + pulseValue + ", fileName=" + fileName);
        	} 
        	
        	oldCompGsrValue = gsrValue;
        	oldCompPulseValue = pulseValue;
        	
			camera.startPreview();
			camera.takePicture(shutterCallback, rawCallback, jpegCallback);
            lastTime = currentTimeMillis;
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
                outStream = MoLog.this.openFileOutput(TMP_JPG, MODE_WORLD_READABLE);
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
