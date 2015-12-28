package com.anton.suprun.simplepins.data;

import com.google.android.gms.maps.model.Marker;

public class PinEntity {
    private static final double DELTA = 0.00001;
    private long id = -1;
    private double latitude;
    private double longitude;
    private String title;

    public PinEntity(double latitude, double longitude, String title) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.title = title;
    }

    public static PinEntity from(Marker marker) {
        double latitude = marker.getPosition().latitude;
        double longitude = marker.getPosition().longitude;
        String title = marker.getTitle();

        return new PinEntity(latitude, longitude, title);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PinEntity pinEntity = (PinEntity) o;

        return (Math.abs(pinEntity.latitude - latitude) <= DELTA) && (Math.abs(pinEntity.longitude - longitude) <= DELTA);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(latitude);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(longitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
