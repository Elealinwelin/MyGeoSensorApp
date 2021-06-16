package emrd.uja.org.mygeosensorapp.util;

import java.util.List;

/**
 * Clase que almacena las propiedades que mide un sensor almacenado en una base de datos de geo-sensores
 * y su localización.
 */
public class SensorProperty {
    public String id; // Identificador único de un sensor en una base de datos de geo-sensores
    public String sensor; // Nombre de un sensor (o estación) que mide una o varias propiedades
    public List<String> properties; // Lista de todas las propiedades que es capaz de medir un sensor (en nuestro caso: O3, PM10...)
    public Float lat; // Latitud de la localización de un sensor
    public Float lon; // Longitud de la localización de un sensor

    public SensorProperty() {}

    public SensorProperty(String id, String sensor, List<String> properties, Float lat, Float lon) {
        this.id = id;
        this.sensor = sensor;
        this.properties = properties;
        this.lat = lat;
        this.lon = lon;
    }

    @Override
    public String toString() {
        return "SensorProperty{" + "id=" + id + ", sensor=" + sensor + ", properties=" + properties + ", lat=" + lat + ", lon=" + lon + '}';
    }

}