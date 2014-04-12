package com.example.aplikacja_push_to_talk;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.ServerSocket;


import android.os.Bundle;
import android.os.ParcelUuid;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener {
 


    @Override
    public void onBackPressed() {
	// TODO Auto-generated method stub
	super.onBackPressed();
	this.finish();
	return;
    }

    protected static final int REQUEST_ENABLE_BT = 0;
    private TextView powitanie;
    private TextView port;
    private Button bluetooth;
    private Button wifi;

    private static String TAG = "MainActivity";
   

   
    public MainBluetoothActivity mBluetooth_main;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_main);

	powitanie = (TextView) findViewById(R.id.powitanie);
	powitanie.setOnClickListener((OnClickListener) this);

	bluetooth = (Button) findViewById(R.id.bluetooth);
	wifi = (Button) findViewById(R.id.wifi);
	

	bluetooth.setOnClickListener(this);
	wifi.setOnClickListener(this);

	port = (TextView) findViewById(R.id.port);
	try {
	    getPortAdress();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

	UUID();
	

    }
   
    

    // sprawdzenie identyfikatora urzadzenia
    private void UUID() {
	// TODO Auto-generated method stub
	try {
	    BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

	    Method getUuidsMethod = BluetoothAdapter.class.getDeclaredMethod(
		    "getUuids", null);

	    ParcelUuid[] uuids = (ParcelUuid[]) getUuidsMethod.invoke(adapter,
		    null);

	    for (ParcelUuid uuid : uuids) {
		Log.d(TAG, "UUID: " + uuid.getUuid().toString());
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

    private void getPortAdress() throws IOException {
	// TODO Auto-generated method stub
	// ServerSocket s = new ServerSocket(0);
	// port.setText("port: " + s.getLocalPort());

	//String text = "port: 55984";
	//port.setText(text);
	try {
	    ServerSocket s = create(new int[] { 55984 });
	    port.setText("nasluchiwanie na port: " + s.getLocalPort());
	} catch (IOException ex) {
	    port.setText("nie ma dostepnych portow");
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    Log.e("PORT: ", "nie jest dostepny" + e.toString());
	    e.printStackTrace();
	}
	
    }

    private ServerSocket create(int[] ports) throws Exception {
	for (int port : ports) {
	    try {
		return new ServerSocket(port);
	    } catch (IOException ex) {
		continue; // znajdz nastepny dostepny port
	    }
	}
	throw new IOException("nie znaleziono wolnych portow");
    }

    @Override
    public void onClick(View v) {
	// TODO Auto-generated method stub
	switch (v.getId()) {
	case R.id.bluetooth:
	    przeniesienie_do_nowej_aktywnosci();

	    break;
	case R.id.wifi:
	    przeniesienie();
	    break;
	
	}
    }
    
  
    
    private void przeniesienie() {
	// TODO Auto-generated method stub
	Intent intent = new Intent(getApplicationContext(), Obsluga_dzwiek_wifi.class);
	startActivity(intent);
    }

    private void przeniesienie_do_nowej_aktywnosci() {
	// TODO Auto-generated method stub
	Intent intent = new Intent(getApplicationContext(), MainBluetoothActivity.class);
	startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	// Inflate the menu; this adds items to the action bar if it is present.
	getMenuInflater().inflate(R.menu.main, menu);
	return true;
    }

}