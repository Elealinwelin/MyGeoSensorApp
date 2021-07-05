package emrd.uja.org.mygeosensorapp.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import emrd.uja.org.mygeosensorapp.restclient.AsynRestSensorData;
import emrd.uja.org.mygeosensorapp.R;
import emrd.uja.org.mygeosensorapp.util.SensorProperty;
import retrofit2.Call;

/**
 * Actividad que, al pulsar sobre el icono del usuario o de su recorrido en el mapa de la activity principal,
 * muestra una interfaz con botones dinámicos de las propiedades relativas a la exposición ambiental del
 * usuario que se encuentran guardadas en su propia base de datos y nos permite elegir la propiedad que
 * queremos visualizar en la gráfica de la siguiente activity.
 */
public class PropiedadesUsuarioActivity extends AppCompatActivity {
    private LinearLayout botoneraLayout;
    private TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_propiedades_usuario);

        // Enlazamos al XML
        title = (TextView) findViewById(R.id.title_prop_usuario);
        botoneraLayout = (LinearLayout) findViewById(R.id.botonera_usuario);

        // Recuperamos los datos del usuario del intent
        String device = getIntent().getStringExtra("device");

        // Cambiamos el título
        title.setText("Datos de exposición de " + device);

        // Obtenemos los datos del server de manera asíncrona para obtener las propiedades del usuario y pintarlas en botones
        Call<SensorProperty[]> call = AsynRestSensorData.init().querySensorProperty(device);
        AsynRestSensorData.MyCall<SensorProperty[]> mycall=new AsynRestSensorData.MyCall<SensorProperty[]>(
                (listaPropiedadesSensores)->{
                    if (listaPropiedadesSensores.length!=0){ // Si hay datos del usuario en la BBDD, pintamos los botones
                        pintarBotones(listaPropiedadesSensores, device);
                    }
                }
        ,this);
        mycall.execute(call);

    }

    /**
     * Función que dibuja un botón por cada propiedad de exposición ambiental guardada en la base de datos del usuario
     * @param listaPropiedadesSensores Array con los "sensores" (mobile) de la base de datos del usuario, incluyendo sus propiedades (en realidad no son sensores, si no medias de exposición ambiental calculadas a partir de datos de sensores, pero las guardamos como sensores con nombre: "mobile")
     * @param device Dispositivo o nombre de la base de datos del usuario de la que queremos consultar los datos
     */
    private void pintarBotones(SensorProperty[] listaPropiedadesSensores, String device) {
        // Creamos las propiedades de layout que tendrán los botones
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        if (listaPropiedadesSensores.length!=0){ // Si hay algún dato en la bbdd del usuario, pintamos los botones de las propiedades
            int cont = 0;
            boolean encontrado = false;
            while (cont<listaPropiedadesSensores.length && !encontrado){ // Nos aseguramos de coger los datos del usuario con sensor = "mobile"
                if (listaPropiedadesSensores[cont].sensor.equals("mobile")){
                    encontrado = true;
                    // Creamos los botones en bucle, uno por cada property
                    for (String property : listaPropiedadesSensores[cont].properties){
                        Button button = new Button(this);
                        // Asignamos propiedades de layout al boton
                        button.setLayoutParams(lp);
                        // Asignamos texto al botón
                        button.setText(property);
                        // Creamos el intent
                        Intent i = new Intent(this, GraficaActivity.class);
                        // Configuramos el listener
                        button.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                // Al pulsar el botón, se mostrará la gráfica con los datos del usuario correspondientes a la propiedad seleccionada
                                // Añadimos los parámetros al intent
                                i.putExtra("device", device);
                                i.putExtra("sensor", "mobile");
                                i.putExtra("property", property);
                                startActivity(i);
                            }
                        });
                        // Añadimos el botón a la botonera
                        botoneraLayout.addView(button);
                    }
                }else{
                    cont++;
                }
            }
        }

    }

}
