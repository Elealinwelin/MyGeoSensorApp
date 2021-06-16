package emrd.uja.org.mygeosensorapp.util;

/**
 * Clase que facilita la tarea de calcular la media de exposición ambiental de una propiedad concreta
 * medida por los sensores de la base de datos en un punto determinado.
 */
public class MediaPropiedad {
    private float dividendo;
    private float divisor;

    public MediaPropiedad(float dividendo, float divisor) {
        this.dividendo = dividendo;
        this.divisor = divisor;
    }

    public void sumarADividendo(float valor) {
        this.dividendo = this.dividendo + valor;
    }

    public void sumarADivisor(float valor) {
        this.divisor = this.divisor + valor;
    }

    public float calcularMedia() {
        return this.dividendo/this.divisor;
    }

    public static float formatearDecimales(float numero, int numeroDecimales) {
        return (float) (Math.round(numero * Math.pow(10, numeroDecimales)) / Math.pow(10, numeroDecimales));
    }

    public float getDividendo() {
        return dividendo;
    }

    public float getDivisor() {
        return divisor;
    }

    @Override
    public String toString() {
        return "MediaPropiedad{" + "dividendo=" + dividendo + ", divisor=" + divisor + '}';
    }

}
