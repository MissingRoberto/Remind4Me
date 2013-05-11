package com.swcm.remindme;

import java.util.ArrayList;
import com.swcm.remind4me.R;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ListRemindsActivity extends ListActivity {

	ArrayList<Reminder> reminders;
	private static final String PROX_ALERT_INTENT = "ALERT_ACTION";
	private static final int REQUEST_CONFIG = 0;
	private static final int REQUEST_CREATE = 1;
	private static final int REQUEST_EDIT = 2;
	protected static final int RESULT_SPEECH = 4;
	private static final long MINIMUM_DISTANCECHANGE_FOR_UPDATE = 1;
	private static final long MINIMUM_TIME_BETWEEN_UPDATE = 1000;
	private static int requestAlertCode = 50;
	private int radious = 10;
	private int expiration = -1;
	private ArrayList<PendingIntent> alerts;
	private LocationManager lm;
	private String provider;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_reminds);

		// Carga las preferencias

		loadPrefs();

		// creacion de la conexion a la base de datos y obtencion una lista con
		// todos los recordatorios

		Model db = new Model(this);
		db.open();
		reminders = db.selectAllReminders();
		db.close();
		Log.i("Base datos", "leidos los recordatorios");

		// Rellena el adapter de la listActivity

		ReminderAdapter adapter = new ReminderAdapter(this, reminders);
		setListAdapter(adapter);

		ImageButton btnSpeak = (ImageButton) findViewById(R.id.btnSpeak);

		btnSpeak.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				Intent intent = new Intent(
						RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

				intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");

				try {
					startActivityForResult(intent, RESULT_SPEECH);

				} catch (ActivityNotFoundException a) {
					Toast t = Toast.makeText(getApplicationContext(),
							"Opps! Your device doesn't support Speech to Text",
							Toast.LENGTH_SHORT);
					t.show();
				}
			}
		});

		// Listado de alertas

		alerts = new ArrayList<PendingIntent>();

		// Agrega el context menu

		registerForContextMenu(getListView());

		// Crea las alertas de proximidad para cada elemento de la lista

		lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		provider = chooseProvider(lm);
		lm.requestLocationUpdates(provider, MINIMUM_TIME_BETWEEN_UPDATE,
				MINIMUM_DISTANCECHANGE_FOR_UPDATE, new MyLocationListener());
		Log.i("PROVIDER", provider);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		getMenuInflater().inflate(R.menu.list_reminds, menu);
		return true;

	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.dialog_title);
		builder.setMessage(((ReminderAdapter) getListAdapter()).getItem(
				position).getDescription());
		builder.setPositiveButton(R.string.dialog_accept,
				new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});
		builder.show();
	}

	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.context_menu, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		final Reminder reminder = (Reminder) getListView().getItemAtPosition(
				info.position);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		switch (item.getItemId()) {

		case R.id.edit:
			Intent i = new Intent(this, CreateActivity.class);
			i.putExtra("id", reminder.getRow());
			startActivityForResult(i, REQUEST_EDIT);
			return true;

		case R.id.delete:
			builder.setTitle(R.string.delete_title);
			builder.setMessage(getString(R.string.delete_msg) + " "
					+ reminder.getName());
			builder.setPositiveButton(R.string.dialog_accept,
					new OnClickListener() {
						@SuppressWarnings("unchecked")
						public void onClick(DialogInterface dialog, int which) {
							Model db = new Model(ListRemindsActivity.this);
							db.open();
							db.deleteReminder(reminder);
							db.close();
							reminders.remove(reminder);
							((ArrayAdapter<Reminder>) getListAdapter())
									.notifyDataSetChanged();
							Log.i("DB", "Elemento borrado");
							updateAlerts(lm);
							dialog.cancel();
						}
					});
			builder.setNegativeButton(R.string.dialog_cancel,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							Log.i("Dialogos", "Borrado cancelado.");
							dialog.cancel();
						}
					});
			builder.show();
			return true;
			// Codigo para mandar emails.

		case R.id.send:

			String to = "";
			String subject = "Recordatorio RemindMe: " + reminder.getName();
			String message = "En el lugar " + reminder.getPlace() + ": "
					+ reminder.getDescription();

			Intent email = new Intent(Intent.ACTION_SEND);
			email.putExtra(Intent.EXTRA_EMAIL, new String[] { to });
			email.putExtra(Intent.EXTRA_SUBJECT, subject);
			email.putExtra(Intent.EXTRA_TEXT, message);

			// need this to prompts email client only
			email.setType("message/rfc822");

			startActivity(Intent.createChooser(email,
					getString(R.string.send_chooser)));

			return true;

		case R.id.delete_all:

			builder.setTitle(R.string.delete_title);
			builder.setMessage(R.string.delete_msg_all);
			builder.setPositiveButton(R.string.dialog_accept,
					new OnClickListener() {
						@SuppressWarnings("unchecked")
						public void onClick(DialogInterface dialog, int which) {
							Model db = new Model(ListRemindsActivity.this);
							db.open();
							db.removeAll();
							db.close();
							reminders.clear();
							clearAlertProximities(lm);
							alerts.clear();
							((ArrayAdapter<Reminder>) getListAdapter())
									.notifyDataSetChanged();
							Log.i("DB", "Lista borrada");
							dialog.cancel();
						}
					});
			builder.setNegativeButton(R.string.dialog_cancel,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							Log.i("Dialogos", "Borrado cancelado.");
							dialog.cancel();
						}
					});
			builder.show();
			return true;

		default:
			return super.onContextItemSelected(item);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.about:
			Intent intentabout = new Intent(this, AboutUsActivity.class);
			startActivity(intentabout);
			return true;

		case R.id.help:
			Intent intenthelp = new Intent(this, HelpActivity.class);
			startActivity(intenthelp);
			return true;

		case R.id.action_settings:
			Intent intentsettings = new Intent(this, SettingsActivity.class);
			startActivityForResult(intentsettings, REQUEST_CONFIG);
			return true;

		case R.id.add:

			Intent intentadd = new Intent(this, CreateActivity.class);
			startActivityForResult(intentadd, REQUEST_CREATE);
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CONFIG) {
			if (resultCode == ListActivity.RESULT_OK) {
				Toast.makeText(this, R.string.new_config, Toast.LENGTH_SHORT)
						.show();
				loadPrefs();
				updateAlerts(lm);

			}
		} else if (requestCode == REQUEST_CREATE) {
			if (resultCode == ListActivity.RESULT_OK) {

				Model db = new Model(this);
				db.open();
				ArrayList<Reminder> rms = db.selectAllReminders();
				db.close();

				reminders.clear();
				for (Reminder r : rms) {
					reminders.add(r);
				}
				((ArrayAdapter<Reminder>) getListAdapter())
						.notifyDataSetChanged();
				updateAlerts(lm);
				Toast.makeText(this, R.string.new_elem, Toast.LENGTH_SHORT)
						.show();

			}
		} else if (requestCode == REQUEST_EDIT) {
			if (resultCode == ListActivity.RESULT_OK) {
				Model db = new Model(this);
				db.open();
				ArrayList<Reminder> rms = db.selectAllReminders();
				db.close();

				reminders.clear();
				for (Reminder r : rms) {
					reminders.add(r);
				}
				((ArrayAdapter<Reminder>) getListAdapter())
						.notifyDataSetChanged();
				updateAlerts(lm);
				Toast.makeText(this, R.string.element_updated,
						Toast.LENGTH_SHORT).show();

			}
		} else if (RESULT_SPEECH == requestCode) {
			if (resultCode == RESULT_OK && null != data) {

				ArrayList<String> text = data
						.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

				Toast.makeText(this, text.get(0), Toast.LENGTH_SHORT).show();
			}

		}
	}

	private void loadPrefs() {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(this);
		radious = Integer.parseInt(sp.getString("prefAlertRadious", radious
				+ ""));
		expiration = Integer.parseInt(sp.getString("prefAlertExpiration",
				expiration + ""));

		Log.i("radious", radious + "");
		Log.i("expiration", expiration + "");
		Log.i("Preferences", "updated");
	}

	private void saveProximityAlertPoint(LocationManager locationManager,
			Reminder reminder) {

		Intent intent = new Intent(PROX_ALERT_INTENT);
		intent.putExtra("name", reminder.getName());
		intent.putExtra("place", reminder.getPlace());
		intent.putExtra("description", reminder.getDescription());
		intent.putExtra("icon", reminder.getIcon());
		PendingIntent proximityIntent = PendingIntent.getBroadcast(this,
				requestAlertCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		locationManager.addProximityAlert(reminder.getLat(), reminder.getLng(),
				radious, expiration, proximityIntent);
		alerts.add(proximityIntent);
		requestAlertCode++;
	}

	private void addAlertProximities(LocationManager locationManager) {

		for (Reminder r : reminders) {
			if (r.isActive()) {
				Log.i("Proximity alert", r.getName());
				saveProximityAlertPoint(locationManager, r);
			}
		}
		Log.i("Proximity alerts", "added");
	}

	private void clearAlertProximities(LocationManager locationManager) {
		for (PendingIntent p : alerts) {
			locationManager.removeProximityAlert(p);
		}
		Log.i("Proximity alerts", "cleared");
	}

	private void updateAlerts(LocationManager locationManager) {
		clearAlertProximities(locationManager);
		alerts.clear();
		addAlertProximities(locationManager);
	}

	private String chooseProvider(LocationManager lm) {
		String p = null;
		if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			p = LocationManager.GPS_PROVIDER;
		} else if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			p = LocationManager.NETWORK_PROVIDER;
		} else if (lm.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) {
			p = LocationManager.PASSIVE_PROVIDER;
		}
		return p;
	}

	@Override
	protected void onResume() {
		super.onResume();

		// Inicializa el receptor broadcast
		addAlertProximities(lm);
		IntentFilter filter = new IntentFilter(PROX_ALERT_INTENT);
		registerReceiver(new AlertsReceiver(), filter);
	}

	// ESCUCHADOR DE LOCALIZACIONES ****** TENDR�AMOS QUE A�ADIR CAMBIOS DE
	// PROVIDER

	public class MyLocationListener implements LocationListener {
		public void onLocationChanged(Location location) {
			Log.i("Location changed",
					"mensaje recibido: lat " + location.getLatitude()
							+ " long " + location.getLongitude());
		}

		public void onStatusChanged(String s, int i, Bundle b) {
			Log.i("OnStatuChanged", "status changed" + s);
		}

		public void onProviderDisabled(String s) {
			Log.i("OnProviderDisabled", "provider disable" + s);
		}

		public void onProviderEnabled(String s) {
			Log.i("OnProviderEnabled", "provider enable" + s);
		}
	}

	// CLASE PARA ADAPTAR LA LISTA DE ELEMENTOS

	public class ReminderAdapter extends ArrayAdapter<Reminder> {
		private final Context context;
		private final ArrayList<Reminder> values;

		public ReminderAdapter(Context context, ArrayList<Reminder> values) {

			super(context, R.layout.item_layout, values);
			this.context = context;
			this.values = values;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			final Reminder reminder = values.get(position);

			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View rowView = inflater
					.inflate(R.layout.item_layout, parent, false);
			TextView place = (TextView) rowView.findViewById(R.id.elem_place);
			place.setText(reminder.getPlace());

			TextView title = (TextView) rowView.findViewById(R.id.elem_name);
			title.setText(reminder.getName());

			ImageView icon = (ImageView) rowView.findViewById(R.id.elem_icon);
			int resID = context.getResources().getIdentifier(
					reminder.getIcon(), "drawable", context.getPackageName());
			icon.setImageResource(resID);

			CheckBox checkBox = (CheckBox) rowView.findViewById(R.id.checkBox);
			checkBox.setChecked(reminder.isActive());

			checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@SuppressWarnings("unchecked")
				@Override
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {

					reminder.setActive(buttonView.isChecked());
					Model db = new Model(ListRemindsActivity.this);
					db.open();
					db.updateReminder(reminder);
					db.close();

					((ArrayAdapter<Reminder>) getListAdapter())
							.notifyDataSetChanged();
					updateAlerts(lm);
				}
			});
			return rowView;
		}

		@Override
		public Reminder getItem(int position) {
			return values.get(position);
		}

	}
}
