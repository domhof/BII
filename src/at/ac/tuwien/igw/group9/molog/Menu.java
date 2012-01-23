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
package at.ac.tuwien.igw.group9.molog;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import at.ac.tuwien.igw.group9.molog.db.LogData;
import at.ac.tuwien.igw.group9.molog.log.GraphView;
import at.ac.tuwien.igw.group9.molog.log.MoLog;
import at.ac.tuwien.igw.group9.molog.loggallery.LogGallery;

public class Menu extends Activity {

    private static final String TAG = "SensorGraph";

    // change this to your Bluetooth device address
    private static final String DEVICE_ADDRESS = "00:06:66:05:61:E9"; // "00:06:66:03:73:7B";

    private GraphView gsrGraph;
    private GraphView pulseGraph;
    private TextView mValueTV;

    long lastTime = System.currentTimeMillis();

    Camera camera;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);

        final Button moLogButton = (Button) findViewById(R.id.moLogButton);
        moLogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent userCreationIntent = new Intent(v.getContext(), MoLog.class);
                startActivityForResult(userCreationIntent, 0);
            }
        });
        final Button galleryButton = (Button) findViewById(R.id.galleryButton);
        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent userCreationIntent = new Intent(v.getContext(), LogGallery.class);
                startActivityForResult(userCreationIntent, 0);
            }
        });
        
        final Button clearDbButton = (Button) findViewById(R.id.clearDbButton);
        clearDbButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogData logData = new LogData(Menu.this);
                logData.clearDB();
            }
        });
    }
}
