package emrd.uja.org.mygeosensorapp.gui;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import emrd.uja.org.mygeosensorapp.util.SensorGeoData;

/**
 * Modificador para personalizar el formato del eje X de las gráficas de la biblioteca de MPAndroidChart
 * con el objetivo de que muestren la fecha y la hora para cada valor del sensor introducido en la gráfica
 * con un formato legible para el usuario.
 */
public class GraphXAxisValueFormatter implements IAxisValueFormatter {
    private static int MINUTES_INTERVAL = 5;
    private String[] mValues;
    private int mInterval;

    public GraphXAxisValueFormatter(SensorGeoData[] range, int interval) {
        mValues = new String[range.length];
        mInterval = interval;

        Calendar calendar = Calendar.getInstance();
        // Para cada sensor, traducimos el timestamp al formato de fecha y hora que nos interesa...
        for (int i = 0; i < range.length; i++) {
            // Usamos un Calendar para transformar el timestamp en Date
            calendar.setTimeInMillis(range[i].timestamp);
            int unroundedMinutes = calendar.get(Calendar.MINUTE);
            int mod = unroundedMinutes % MINUTES_INTERVAL;
            calendar.add(Calendar.MINUTE, mod < 8 ? -mod : (MINUTES_INTERVAL - mod));

            // Le damos el formato de fecha y hora
            String s = getDateFromTimestamp(calendar.getTime());

            // Lo guardamos en el array de valores del eje x
            mValues[i] = s;
        }
    }

    // Métodos de IAxisValueFormatter

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        if (value % mInterval == 0 && value >= 0) {
            return mValues[(int) value % mValues.length];
        } else
            return "";
    }

    /**
     * Función que traduce un objeto Date a una cadena de texto de fecha y hora legible por un humano
     * @param timestamp Fecha que queremos traducir al formato de fecha y hora en String
     * @return Devuelve una cadena de texto con la fecha y hora pasadas pero en formato legible
     */
    private String getDateFromTimestamp(Date timestamp) {
        String date = "";
        DateFormat hourFormat = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
        date = hourFormat.format(timestamp);
        return date;
    }

}

