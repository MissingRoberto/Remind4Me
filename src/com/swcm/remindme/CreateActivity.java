package com.swcm.remindme;

import com.swcm.remind4me.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class CreateActivity extends Activity {

	Location lastLocation;
	LocationManager locationManager;
	TextView latitudeField;
	TextView longitudeField;
	String provider;

	@SuppressWarnings("unchecked")
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_layout);

		Spinner spinner = (Spinner) findViewById(R.id.spinner);
		// Create an ArrayAdapter using the string array and a default spinner
		// layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.valores_array,
				android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		spinner.setAdapter(adapter);

		latitudeField = ((TextView) findViewById(R.id.lat_value));
		longitudeField = ((TextView) findViewById(R.id.long_value));

		// Extra ID
		if (getIntent().hasExtra("id")) {
			getActionBar().setTitle("Editar Recordatorio");
			Model db = new Model(this);
			db.open();
			Long id = getIntent().getExtras().getLong("id");
			Reminder reminder = db.selectReminder(id);
			db.close();

			((TextView) findViewById(R.id.title_value)).setText(reminder
					.getName());
			((TextView) findViewById(R.id.descrip_value)).setText(reminder
					.getDescription());
			((TextView) findViewById(R.id.place_value)).setText(reminder
					.getPlace());
			latitudeField.setText(String.valueOf(reminder.getLat()));
			longitudeField.setText("" + reminder.getLng());
			spinner.setSelection(((ArrayAdapter<String>) spinner.getAdapter())
					.getPosition(reminder.getIcon()));
		} else {

			// Acquire a reference to the system Location Manager

			locationManager = (LocationManager) this
					.getSystemService(Context.LOCATION_SERVICE);

			provider=chooseProvider(locationManager);
			Log.i("PROVIDER", provider);

			lastLocation = locationManager.getLastKnownLocation(provider);
			if (lastLocation != null) {
				Log.i("CURRENT POSITION",
						"latitude: " + lastLocation.getLatitude()
								+ " longitude: " + lastLocation.getLongitude());
				latitudeField
						.setText(String.valueOf(lastLocation.getLatitude()));
				longitudeField.setText(String.valueOf(lastLocation
						.getLongitude()));
			} else {
				Log.i("Localizacion:", "null");
			}

		}

	}

	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_create, menu);
		this.getActionBar().setDisplayHomeAsUpEnabled(true);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent i = new Intent(this, ListRemindsActivity.class);
		switch (item.getItemId()) {

		case R.id.help:
			Intent intentHelp = new Intent(CreateActivity.this,
					HelpActivity.class);
			startActivity(intentHelp);
			return true;

		case R.id.add:

			// Comprueba que los campos son no nulos
			if (!(((TextView) findViewById(R.id.title_value)).getText()
					.toString().equals(""))
					&& !(((TextView) findViewById(R.id.place_value)).getText()
							.toString().equals(""))
					&& !(((TextView) findViewById(R.id.lat_value)).getText()
							.toString().equals(""))
					&& !(((TextView) findViewById(R.id.long_value)).getText()
							.toString().equals(""))
					&& !(((TextView) findViewById(R.id.descrip_value))
							.getText().toString().equals(""))) {

				String name = ((TextView) findViewById(R.id.title_value))
						.getText().toString();
				String description = ((TextView) findViewById(R.id.descrip_value))
						.getText().toString();
				String place = ((TextView) findViewById(R.id.place_value))
						.getText().toString();
				double lat = Double
						.parseDouble(((TextView) findViewById(R.id.lat_value))
								.getText().toString());
				double lng = Double
						.parseDouble(((TextView) findViewById(R.id.long_value))
								.getText().toString());
				String icon = ((Spinner) findViewById(R.id.spinner))
						.getSelectedItem().toString();
				Reminder reminder = new Reminder(name, place, description,
						icon, lat, lng, false);

				Model db = new Model(this);
				db.open();

				// EXTRA ID
				if (getIntent().hasExtra("id")) {
					reminder.setRow(getIntent().getExtras().getLong("id"));
					db.updateReminder(reminder);
				} else {
					db.insertReminder(reminder);
				}

				db.close();

				// Enviar result de vuelta y volver a ListActivity
				setResult(Activity.RESULT_OK, i);
				this.finish();
			} else {
				Toast.makeText(this, R.string.empty_field, Toast.LENGTH_SHORT)
						.show();
			}
			return true;

		case android.R.id.home:
			setResult(Activity.RESULT_CANCELED, i);
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}


	private String chooseProvider(LocationManager lm){
		String p=null;
		if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			p = LocationManager.GPS_PROVIDER;
		} else if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			p = LocationManager.NETWORK_PROVIDER;
		} else if (lm.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) {
			p = LocationManager.PASSIVE_PROVIDER;
		}
		return p;
	}
}
