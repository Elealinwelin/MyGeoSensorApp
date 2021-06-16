package emrd.uja.org.mygeosensorapp.gui;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow;
import emrd.uja.org.mygeosensorapp.R;
import emrd.uja.org.mygeosensorapp.util.SensorProperty;
import emrd.uja.org.mygeosensorapp.activities.GraficaActivity;

/**
 * Vista para personalizar el MarkerInfoWindow que se muestra al pulsar sobre el icono de un sensor en el
 * mapa de la activity principal. Muestra el nombre del sensor y se introducen botones dinámicos que permiten
 * seleccionar la propiedad medida por el sensor que queremos visualizar en la gráfica de la siguiente activity.
 */
public class CustomInfoWindow extends MarkerInfoWindow {
    Marker marker;

    public CustomInfoWindow(Context ctx, MapView mapView, SensorProperty sensor, String device) {
        super(R.layout.bubble_propiedades_sensor, mapView);

        // Enlazamos al XML
        TextView title = (TextView) mView.findViewById(R.id.title_marker);
        LinearLayout linearLayout = (LinearLayout) mView.findViewById(R.id.botonera);

        // Cambiamos el título
        title.setText("Sensor " + sensor.sensor);

        // Creamos las propiedades de layout que tendrán los botones
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        // Creamos los botones en bucle, uno por cada property
        for (String property : sensor.properties){
            Button button = new Button(ctx);
            // Asignamos propiedades de layout al boton
            button.setLayoutParams(lp);
            // Asignamos texto al botón
            button.setText(property);
            // Configuramos el listener
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // Al clickar en el botón, se mostrará la gráfica con los datos del sensor correspondientes a la propiedad seleccionada
                    // Intent con parámetros
                    Intent i = new Intent(ctx, GraficaActivity.class);
                    i.putExtra("device", device);
                    i.putExtra("sensor", sensor.sensor.toString());
                    i.putExtra("property", property);
                    ctx.startActivity(i);
                }
            });
            // Añadimos el botón a la botonera
            linearLayout.addView(button);
        }

    }

    // Métodos de MarkerInfoWindow

    @Override
    public void onOpen(Object item) {
        marker = (Marker) item;
    }

    @Override
    public void onClose() {
        super.onClose();
        mMarkerRef = null;
        // By default, do nothing else
    }

}
