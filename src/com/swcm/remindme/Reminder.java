package com.swcm.remindme;

public class Reminder {

	private long row;
	private String name;
	private String place;
	private double lat;
	private double lng;
	private String description;
	private boolean active;
	private String icon;

	public Reminder(String name, String place, String description, String icon,
			double lat, double lng, boolean active) {

		this.name = name;
		this.place = place;
		this.lat = lat;
		this.lng = lng;
		this.description = description;
		this.active = true;
		this.icon = icon;
		this.active = active;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getIcon() {
		return this.icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPlace() {
		return place;
	}

	public void setPlace(String place) {
		this.place = place;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public double getLng() {
		return lng;
	}

	public void setLng(double lng) {
		this.lng = lng;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public long getRow() {
		return row;
	}

	public void setRow(long row) {
		this.row = row;
	}

	@Override
	public String toString() {
		return "Reminder [row=" + row + ", name=" + name + ", place=" + place
				+ ", lat=" + lat + ", lng=" + lng + ", description="
				+ description + ", active=" + active + ", icon=" + icon + "]";
	}

}
