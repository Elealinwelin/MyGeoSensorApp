package emrd.uja.org.mygeosensorapp.util;

/**
 * Clase que almacena la latitud y longitud de una ubicación en un mapa. También permite generar ubicaciones
 * nuevas a partir de otras de referencia.
 */
public class Ubicacion {
    private float lat;
    private float lon;

    public Ubicacion(float lat, float lon) {
        this.lat = lat;
        this.lon = lon;
    }

    /**
     * Constructor que genera una nueva ubicación a partir de la ubicación de referencia pasada con un movimiento
     * lineal o aleatorio.
     * @param ubicacionReferencia Ubicación de referencia a partir de la cual se genera la nueva ubicación
     * @param random Booleano que permite decidir cómo se generará la nueva ubicación: aleatoria, si está a true, o lineal, si está a false
     */
    public Ubicacion(Ubicacion ubicacionReferencia, boolean random) {
        if (random) {
            // Generamos un movimiento aleatorio
            this.lat = (float) (ubicacionReferencia.getLat() + generaMovimientoRandom());
            this.lon = (float) (ubicacionReferencia.getLon() + generaMovimientoRandom());
        } else {
            // Generamos un movimiento lineal
            this.lat = (float) (ubicacionReferencia.getLat() + Constantes.movimientoUsuario);
            this.lon = (float) (ubicacionReferencia.getLon() + Constantes.movimientoUsuario);
        }
    }

    /**
     * Función que genera un número aleatorio entre la distancia que es capaz de recorrer el usuario con
     * cada movimiento simulado y el mismo valor en negativo, para simular un movimiento de latitud o longitud.
     * @return Devuelve un número aleatorio entre [-movimientoUsuario, movimientoUsuario]
     */
    static double generaMovimientoRandom() {
        double numPositivo = (Math.random()*Constantes.movimientoUsuario); // Número random entre [0, movimientoUsuario]
        int potencia = (int) (Math.random()*2+1); // Número random entre 1 y 2
        return Math.pow(-1, potencia)*numPositivo; // Número random entre [-movimientoUsuario, movimientoUsuario]
    }

    public float getLat() {
        return lat;
    }

    public float getLon() {
        return lon;
    }

    public void setLat(float lat) {
        this.lat = lat;
    }

    public void setLon(float lon) {
        this.lon = lon;
    }

    @Override
    public String toString() {
        return "Ubicacion{" + "lat=" + lat + ", lon=" + lon + '}';
    }

}
