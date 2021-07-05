package emrd.uja.org.mygeosensorapp.activities;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import java.util.ArrayList;
import java.util.Arrays;
import emrd.uja.org.mygeosensorapp.restclient.AsynRestSensorData;
import emrd.uja.org.mygeosensorapp.gui.GraphXAxisValueFormatter;
import emrd.uja.org.mygeosensorapp.R;
import emrd.uja.org.mygeosensorapp.util.SensorGeoData;
import retrofit2.Call;

/**
 * Actividad que muestra una interfaz con un gráfico interactivo que permite visualizar los datos de una
 * propiedad medida por un sensor de la base de datos, o bien los datos de exposición ambiental referentes
 * a una propiedad de la base de datos del usuario. Se puede cambiar la vista de la gráfica a diferentes
 * períodos de tiempo atrás (1 día, una semana o 1 mes) a partir de la fecha y hora actuales (simulados),
 * según nuestra preferencia.
 */
public class GraficaActivity extends AppCompatActivity {
    private LineChart lineChart;
    private LineDataSet lineDataSet;
    private TextView titulo;
    private RadioGroup rgPeriodo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grafica);

        // Enlazamos al XML
        lineChart = (LineChart)findViewById(R.id.lineChart);
        titulo = (TextView)findViewById(R.id.tv_titulo);
        rgPeriodo = (RadioGroup)findViewById(R.id.rg_periodo);

        // Recuperamos los datos del sensor pulsado en el mapa
        String device = getIntent().getStringExtra("device");
        String sensor = getIntent().getStringExtra("sensor");
        String property = getIntent().getStringExtra("property");

        // Cambiamos el título de la gráfica
        if (sensor.equals("mobile")){
            titulo.setText("Datos de exposición de " + device);
        }else{
            titulo.setText("Datos de " + device + ": sensor " + sensor);
        }

        // Establecemos un marco de tiempo para los datos que queremos mostrar en la gráfica. Por defecto: 1 día atrás con respecto al tiempo actual (simulado)
        long offtime = 86400000L; // Tiempo atrás que queremos consultar: 1 día = 86400000ms | 1 semana = 604800000ms | 1 mes = 2592000000ms | Toda = 60000000000ms
        mostrarDatosServer(device, sensor, property, offtime);

        // Configuramos el listener del radio group
        rgPeriodo.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton radioButton = rgPeriodo.findViewById(checkedId);
                lineChart.clear();
                int index = rgPeriodo.indexOfChild(radioButton);
                switch (index) {
                    case 0: // 1 día
                        mostrarDatosServer(device, sensor, property, 86400000L);
                        break;
                    case 1: // 1 semana
                        mostrarDatosServer(device, sensor, property, 604800000L);
                        break;
                    case 2: // 1 mes
                        mostrarDatosServer(device, sensor, property, 2592000000L);
                        break;
                }
            }
        });

    }

    /**
     * Función que realiza la llamada al servidor para extraer de la base de datos los datos relativos al
     * dispositivo, sensor y propiedad pasados, con timestamp dentro del período de tiempo atrás (1 día,
     * 1 semana o 1 mes) desde el tiempo actual (simulado).
     * @param device Dispositivo o nombre de la base de datos de sensores de la que queremos consultar los datos
     * @param sensor Nombre del sensor del que queremos consultar los datos
     * @param property Propiedad medida por el sensor de la que queremos consultar los datos
     * @param offtime Período de tiempo atrás en milisegundos a partir del cual queremos mostrar los datos consultados
     */
    private void mostrarDatosServer(String device, String sensor, String property, long offtime) {
        //Obtenemos los datos del server de manera asíncrona y llamamos a la función para pintar la gráfica
        Call<SensorGeoData[]> call = AsynRestSensorData.init().querySensorData(device, sensor, property, offtime);
        AsynRestSensorData.MyCall<SensorGeoData[]> mycall=new AsynRestSensorData.MyCall<SensorGeoData[]>(
                (listaDatosSensor)->{ //Si la lista que devuelve la consulta no está vacía, pintamos la gráfica con los datos
                    if (listaDatosSensor.length!=0){
                        pintarGrafica(listaDatosSensor);
                    }
                }
        ,this);
        mycall.execute(call);
    }

    /**
     * Función que dibuja en la gráfica los datos de la propiedad de un determinado sensor de la base de
     * datos referentes a un período de tiempo determinado.
     * @param listaDatosSensor Array con los datos de una propiedad medida por un sensor de la base de datos en un período de tiempo específico
     */
    private void pintarGrafica(SensorGeoData[] listaDatosSensor) {
        // Ordenamos los datos por timestamp
        Arrays.sort(listaDatosSensor);

        // Damos formato y configuramos el eje X de la gráfica
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new GraphXAxisValueFormatter(listaDatosSensor, 1));
        xAxis.setGranularity(1f); // only intervals of 1
        xAxis.setTextSize(8);
        xAxis.setLabelCount(2);
        xAxis.setAvoidFirstLastClipping(true); // Evitamos que desaparezcan las etiquetas inicial y final del eje x

        // Creamos el set de datos de nuestro sensor
        ArrayList<Entry> lineEntries = new ArrayList<Entry>();
        for (int i=0; i<listaDatosSensor.length; i++){
            float y = listaDatosSensor[i].value;
            lineEntries.add(new Entry((float) i, y));
        }

        // Unimos los datos a un dataset y lo configuramos
        lineDataSet = new LineDataSet(lineEntries, listaDatosSensor[0].property);
        lineDataSet.setColor(Color.GREEN);
        lineDataSet.setCircleColor(Color.GREEN);
        lineDataSet.setLineWidth(2f);

        // Asociamos el dataset a la gráfica
        LineData lineData = new LineData(lineDataSet);
        lineChart.setData(lineData);

        // Cambiamos los ajustes de la gráfica
        lineChart.setScaleYEnabled(false); // Desactivamos la interacción con el zoom del eje Y
        lineChart.getDescription().setEnabled(false); // Quitamos el Label Description
        lineChart.invalidate(); // Refrescamos los datos para que se visualizen
        lineChart.fitScreen(); // Quitamos el zoom que pueda haber
    }

}
