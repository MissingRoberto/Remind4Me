package com.swcm.remindme;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;

import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class AlertsReceiver extends BroadcastReceiver {

	private Context context;
	private PendingIntent pendingIntent;
	private static final int NOTIFICATION_ID = 1000;
	private String name;
	private String description;
	private String icon;
	private String place;

	@Override
	public void onReceive(Context context, Intent intent) {

		this.context = context;
		Log.i("BROADCAST", "onReceive");

		Bundle extras = intent.getExtras();

		name = extras.getString("name");
		description = extras.getString("description");
		icon = extras.getString("icon");
		place = extras.getString("place");

		String key = LocationManager.KEY_PROXIMITY_ENTERING;
		pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);

		Boolean entering = intent.getBooleanExtra(key, false);

		if (entering) {
			
			Log.d(getClass().getSimpleName(), "entering receiver");
			Notification notification = createNotification();
			notificationManager.notify(NOTIFICATION_ID, notification);

		} else {
			Log.d(getClass().getSimpleName(), "exiting");
		}

	}

	private Notification createNotification() {

		int iconR = context.getResources().getIdentifier(icon, "drawable",
				context.getPackageName());
		Notification notification = new NotificationCompat.Builder(context)
				.setContentIntent(pendingIntent).setTicker(name)
				.setContentTitle(name)
				.setContentText(place + ": " + description).setSmallIcon(iconR)
				//.addAction(iconR, name, pendingIntent)
				.setVibrate(new long[] { 100, 250, 100, 500 }).build();


		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notification.flags |= Notification.FLAG_SHOW_LIGHTS;
		notification.defaults |= Notification.DEFAULT_VIBRATE;
		notification.defaults |= Notification.DEFAULT_LIGHTS;
		notification.defaults |= Notification.DEFAULT_SOUND;
		notification.ledARGB = Color.WHITE;
		notification.ledOnMS = 300;
		notification.ledOffMS = 1500;

		return notification;
	}
}