package com.audiomason.asciiflash;

import java.util.ArrayList;
import java.util.List;

import com.audiomason.asciiflash.R;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class FullscreenActivity extends Activity {

	InputMethodManager imm;
	WifiManager wifiManager;

	Spinner ssidSpinner;
	EditText ssid;
	EditText pass;
	TextView counterLabel;
	Button goButton;
	View clock;
	View word;

	CounterTask counterTask;
	FlasherTask flasherTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fullscreen);

		imm = (InputMethodManager)getSystemService(
			      Context.INPUT_METHOD_SERVICE);

		ssidSpinner = (Spinner) findViewById(R.id.ssidSpinner);
		ssid = (EditText) findViewById(R.id.ssidTextBox);
		pass = (EditText) findViewById(R.id.passwordTextBox);
		counterLabel = (TextView) findViewById(R.id.counterLabel);
		goButton = (Button) findViewById(R.id.goButton);
		clock = (View) findViewById(R.id.clockPane);
		word = (View) findViewById(R.id.wordPane);

		wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

		final List<String> ssidList = new ArrayList<String>();

		for (ScanResult scanResult : wifiManager.getScanResults()) {
			ssidList.add(scanResult.SSID);
		}

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, ssidList);

		ssidSpinner.setAdapter(adapter);
		ssidSpinner.setOnItemSelectedListener(
                new OnItemSelectedListener() {
                    public void onItemSelected(
                            AdapterView<?> parent, View view, int position, long id) {
                        ssid.setText(ssidList.get(position));
                    }

                    public void onNothingSelected(AdapterView<?> parent) {
                        return;
                    }
                });

		counterTask = new CounterTask();
		flasherTask = new FlasherTask();

		goButton.setOnClickListener(goButtonListener);

	}

	private OnClickListener goButtonListener = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
				imm.hideSoftInputFromWindow(goButton.getWindowToken(), 0);
			if (counterTask.getStatus() == AsyncTask.Status.PENDING) {
				counterTask.execute();
			} else {
				counterTask.cancel(true);
				counterTask = new CounterTask();
				counterTask.execute();
			}
		}
	};

	private class CounterTask extends AsyncTask<Void, Integer, Void> {

		@Override
		protected void onPreExecute() {
			if (flasherTask.getStatus() != AsyncTask.Status.PENDING) {
				flasherTask.cancel(true);
				flasherTask = new FlasherTask();
			}
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			for (int i = 5; i > 0; i--) {
				if (isCancelled()) {
					break;
				}
				publishProgress(i);

				long lastTime = System.currentTimeMillis();
				while (System.currentTimeMillis() - lastTime < 1000) {
					if (isCancelled()) {
						break;
					}
				}
			}
			return null;
		}

		protected void onProgressUpdate(Integer... progress) {
			counterLabel.setText(progress[0].toString());
		}

		protected void onPostExecute(Void arg0) {
			counterLabel.setText("");
			flasherTask.execute(ssid.getText() + "\n" + pass.getText());
		}
	}

	private class FlasherTask extends AsyncTask<String, Integer, Void> {

		int black = android.R.color.black;
		int white = android.R.color.white;
		long lastTime = System.currentTimeMillis();
		int flashTime = 50;

		@Override
		protected Void doInBackground(String... message) {

			for (char c : message[0].toCharArray()) {
				for (char b : Integer.toBinaryString(c).toCharArray()) {
					if (isCancelled()) {
						break;
					}
					if (b == '1') {
						publishProgress(white, white);
					} else {
						publishProgress(white, black);
					}
					lastTime = System.currentTimeMillis();
					while (System.currentTimeMillis() - lastTime < flashTime) {
					}

					publishProgress(black, black);

					lastTime = System.currentTimeMillis();
					while (System.currentTimeMillis() - lastTime < flashTime) {
					}
				}
			}
			return null;
		}

		protected void onProgressUpdate(Integer... args) {
			clock.setBackgroundResource(args[0].intValue());
			word.setBackgroundResource(args[1].intValue());
		}

		protected void onPostExecute(Void arg0) {
			clock.setBackgroundResource(black);
			word.setBackgroundResource(black);
		}
	}
}