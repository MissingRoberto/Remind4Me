package com.swcm.remindme;

import com.swcm.remind4me.R;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.Menu;
import android.view.MenuItem;


public class SettingsActivity extends PreferenceActivity {


	@SuppressWarnings("deprecation")
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);

	}

	public boolean onCreateOptionsMenu(Menu menu) {

		getMenuInflater().inflate(R.menu.menu_settings, menu);
		this.getActionBar().setDisplayHomeAsUpEnabled(true);
		return true;

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent i = new Intent(this, ListRemindsActivity.class);
		switch (item.getItemId()) {
		case android.R.id.home:
			setResult(Activity.RESULT_CANCELED, i);
			finish();			
			return true;
			
		case R.id.save_settings:
			setResult(ListActivity.RESULT_OK, i);
			super.finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}