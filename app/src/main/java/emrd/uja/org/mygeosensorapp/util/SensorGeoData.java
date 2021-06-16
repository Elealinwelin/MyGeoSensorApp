package emrd.uja.org.mygeosensorapp.util;

import java.util.Objects;

/**
 * Clase que almacena los datos de un sensor de una base de datos de geo-sensores. Cada dispositivo
 * contiene uno o varios sensores que miden diferentes propiedades.
 */
public class SensorGeoData implements Comparable<SensorGeoData>{
    public String device; // Dispositivo (ciudad en nuestro caso) que contiene uno o varios sensores. Es el nombre de la base de datos que contiene los geo-sensores
    public String sensor; // Nombre de un sensor (o estación) que mide una o varias propiedades
    public String property; // Propiedad medida por un sensor (en nuestro caso: O3, PM10...)
    public float value; // Valor medido por un sensor de una propiedad concreta
    public long timestamp; // Tiempo en milisegundos (desde 1970) en el que un sensor ha realizado una medición
    public Float lat; // Latitud de la localización de un sensor
    public Float lon; // Longitud de la localización de un sensor
    public float w; // Peso utilizado por la aplicación para conocer si un sensor se encuentra más o menos cerca del usuario en un momento determinado. Por defecto, se inicializa a 0

    public SensorGeoData(String device, String sensor, String property, float value, long timestamp, float lat, float lon) {
        this.device = device;
        this.sensor = sensor;
        this.property = property;
        this.value = value;
        this.timestamp = timestamp;
        this.lat = lat;
        this.lon = lon;
        this.w = 0;
    }

    public SensorGeoData(){
        this.w = 0;
    }

    @Override
    public String toString() {
        return "SensorGeoData{" + "device='" + device + '\'' + ", sensor='" + sensor + '\'' + ", property='" + property + '\'' + ", value=" + value + ", timestamp=" + timestamp + ", lat=" + lat + ", lon=" + lon + ", w=" + w + '}';
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + Objects.hashCode(this.device);
        hash = 89 * hash + Objects.hashCode(this.sensor);
        hash = 89 * hash + Objects.hashCode(this.property);
        hash = 89 * hash + Float.floatToIntBits(this.value);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SensorGeoData other = (SensorGeoData) obj;
        if (Float.floatToIntBits(this.value) != Float.floatToIntBits(other.value)) {
            return false;
        }
        if (!Objects.equals(this.device, other.device)) {
            return false;
        }
        if (!Objects.equals(this.sensor, other.sensor)) {
            return false;
        }
        if (!Objects.equals(this.property, other.property)) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(SensorGeoData o) {
        if (timestamp < o.timestamp) {
            return -1;
        }
        if (timestamp > o.timestamp) {
            return 1;
        }
        return 0;
    }

}
