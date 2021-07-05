package emrd.uja.org.mygeosensorapp.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.content.res.ResourcesCompat;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.DelayedMapListener;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.infowindow.InfoWindow;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import emrd.uja.org.mygeosensorapp.restclient.AsynRestSensorData;
import emrd.uja.org.mygeosensorapp.gui.CanvasView;
import emrd.uja.org.mygeosensorapp.util.Constantes;
import emrd.uja.org.mygeosensorapp.gui.CustomInfoWindow;
import emrd.uja.org.mygeosensorapp.util.MediaPropiedad;
import emrd.uja.org.mygeosensorapp.R;
import emrd.uja.org.mygeosensorapp.util.SensorGeoData;
import emrd.uja.org.mygeosensorapp.util.SensorProperty;
import emrd.uja.org.mygeosensorapp.util.Ubicacion;
import retrofit2.Call;

/**
 * Actividad principal que muestra una interfaz con todos los sensores de nuestra base de datos ubicados en
 * un mapa interactivo que permite consultarlos cómodamente y simula al usuario caminando y guardando datos,
 * también consultables, en su propia base de datos referentes a su exposición ambiental, calculada a partir
 * de los sensores más cercanos. También controla la activación/desactivación de un mapa de colores creado
 * con los datos de la propiedad que haya sido elegida en los ajustes de la aplicación, a los que podemos
 * acceder a través de esta activity.
 */
public class MainActivity extends Activity implements MapEventsReceiver {
    static public String city;
    static public String usuario;
    private MapView map;
    protected TextView tv;
    private MapEventsOverlay mapEventsOverlay;
    private long tiempoSimulado;
    private Switch switchMapaColores;
    static public TextView tvSwitch;
    private ArrayList<Ubicacion> ubicaciones;
    private Marker markerUsuario;
    private Handler handler;
    private SensorGeoData[] listaSensoresGeoData;
    private LinearLayout canvasLayout;
    private CanvasView canvasView;
    static public String propiedadMapaColores;
    static public int umbralL1;
    static public int umbralL2;
    static public int tamCuadrados;
    static public boolean usuarioAleatorio;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializamos las variables según nuestras BBDD (variables a cambiar si queremos meter otra BBDD diferente)
        city = "madrid-air"; //Nombre de la base de datos (dispositivo, o ciudad en nuestro caso) que contiene los datos de los geo-sensores.
        usuario = "Elena"; //Nombre de la base de datos (dispositivo, o usuario en nuestro caso) que contiene los datos de la exposición ambiental del usuario (guardados como sensores)
        propiedadMapaColores = "O3"; //Propiedad por defecto a mostrar en el mapa de exposición ambiental por colores
        umbralL1 = 60; //Umbral saludable por defecto a mostrar en el mapa de exposición ambiental por colores
        umbralL2 = 90; //Umbral perjudicial por defecto a mostrar en el mapa de exposición ambiental por colores
        tamCuadrados = 25; //Tamaño por defecto de los cuadros (píxeles) del mapa de exposición ambiental por colores
        usuarioAleatorio = false; //Tipo de recorrido por defecto que realiza el usuario simulado al caminar por el mapa. Puede ser lineal o aleatorio.
        //tiempoSimulado = 1579564800000L;// 22 de Enero de 2020 a las 00:00:00h - 1 día, currentTime simulado
        tiempoSimulado = 1559563200000L;// 4 de Junio de 2019 a las 12:00:00h - 1 día, currentTime simulado
        //tiempoSimulado = 1588168800000L;// 30 de Abril de 2020 a las 14:00:00h - 1 día, currentTime simulado

        // Inicializamos el resto de variables
        ubicaciones = new ArrayList<>();
        markerUsuario = null;
        listaSensoresGeoData = null;
        canvasView = null;
        handler = new Handler();

        // Enlazamos al XML
        tv=(TextView) findViewById(R.id.title);
        map = (MapView) findViewById(R.id.map);
        canvasLayout = (LinearLayout) findViewById(R.id.canvas_layout);
        switchMapaColores = (Switch) findViewById(R.id.switch_colores);
        tvSwitch = (TextView) findViewById(R.id.txt_switch);

        // Cambiamos el texto del textview
        tv.setText("Vista con mapa");
        // Cambiamos el texto del switch del mapa de colores
        tvSwitch.setText("Exposición por colores: " + propiedadMapaColores);

        // Configuramos OpenStreetMap
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        map.setTileSource(TileSourceFactory.MAPNIK);
        mapEventsOverlay = new MapEventsOverlay(this, (MapEventsReceiver) this);
        map.getOverlays().add(0, mapEventsOverlay);
        map.setMultiTouchControls(true);
        map.setMinZoomLevel(3.1);

        // Obtenemos los datos del server de manera asíncrona y pintamos las estaciones de la BBDD en el mapa
        Call<SensorProperty[]> call = AsynRestSensorData.init().querySensorProperty(city);
        AsynRestSensorData.MyCall<SensorProperty[]> mycall=new AsynRestSensorData.MyCall<SensorProperty[]>(
                (listaPropiedadesSensores)->{
                    if(listaPropiedadesSensores.length!=0){ //Si hay sensores en la BBDD seleccionada...
                        paintSensorProperty(listaPropiedadesSensores); // Los pintamos en el mapa

                        // Ubicación inicial (Museo del Prado)
                        double lat = 40.415526023298426;
                        double lon = -3.6921037249045345;
                        Ubicacion ubicacionInicial = new Ubicacion((float)lat, (float)lon);
                        ubicaciones.add(ubicacionInicial);

                        // Vamos calculando la exposición a la que se encuentra sometido el usuario cada cierto tiempo, tomando como referencia las estaciones
                        tareaExposicionUsuario();
                    }
                }
        ,this);
        mycall.execute(call);

        // Configuramos los listeners para movernos por el mapa
        map.setMapListener(new DelayedMapListener(new MapListener() {
            public boolean onZoom(final ZoomEvent e) {
                if (canvasView!=null)
                    canvasView.invalidate();
                return true;
            }

            public boolean onScroll(final ScrollEvent e) {
                if (canvasView!=null)
                    canvasView.invalidate();
                return true;
            }
        } ));

        // Configuramos el listener del switch que muestra la exposición por colores
        switchMapaColores.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // Crear canvas del mapa de colores
                    canvasView = new CanvasView(buttonView.getContext());
                    canvasView.setMainActivity(MainActivity.this);
                    canvasLayout.addView(canvasView);
                    tv.setText("Niveles de exposición de " + propiedadMapaColores + " pintados sobre el mapa");
                    // Obtenemos los datos del server de manera asíncrona y la lista de datos de los sensores para calcular la exposición posteriormente
                    Call<SensorGeoData []> call2 = AsynRestSensorData.init().getSnapShot(city);
                    AsynRestSensorData.MyCall<SensorGeoData[]> mycall2=new AsynRestSensorData.MyCall<SensorGeoData[]>(
                            (listaDatosSensores)->{
                                if (listaDatosSensores.length!=0){ // Si hay sensores en la BBDD, los guardamos
                                    setListaSensoresGeoData(listaDatosSensores);
                                    canvasView.invalidate();
                                }
                            }
                    ,MainActivity.this);
                    mycall2.execute(call2);
                }else{
                    // Eliminar canvas del mapa de colores
                    tv.setText("Exposición por colores desactivada");
                    canvasLayout.removeView(canvasView);
                }
            }
        });

    }

    /**
     * Función que dibuja un marcador en el mapa por cada sensor encontrado en la base de datos. Cada marcador
     * contendrá uno o varios botones dinámicos con las propiedades que mida dicho sensor.
     * @param properties Array con los sensores de la base de datos a dibujar en el mapa, incluyendo su ubicación y propiedades
     */
    public void paintSensorProperty(SensorProperty [] properties){
        for(SensorProperty sensor:properties){ // Para cada sensor pintamos un marcador en el mapa
            GeoPoint startPoint = new GeoPoint(sensor.lat,sensor.lon);
            Marker startMarker = new Marker(map);
            startMarker.setTitle(sensor.properties.toString());
            startMarker.setPosition(startPoint);
            Drawable d = ResourcesCompat.getDrawable(getResources(), R.mipmap.sensor, null);
            Bitmap bitmap = ((BitmapDrawable) d).getBitmap();
            Drawable dr = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, (int) (58.0f * getResources().getDisplayMetrics().density), (int) (58.0f * getResources().getDisplayMetrics().density), true));
            startMarker.setIcon(dr);
            startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            startMarker.setInfoWindow(new CustomInfoWindow(this, map, sensor, city)); //Crear un InfoWindow con un botón para cada property
            // Configuramos el listener
            startMarker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker, MapView mapView) {
                    InfoWindow.closeAllInfoWindowsOn(map);
                    marker.showInfoWindow();
                    mapView.getController().animateTo(marker.getPosition());
                    return true;
                }
            });
            // Añadimos el marcador al mapa
            map.getOverlays().add(startMarker);
            this.tv.setText("Sensores dibujados");
        }

        // Si hay sensores, se hace un zoom a la nube de puntos en el mapa
        if(properties.length>0){
            zoomToBounds(computeArea(properties));
        }
    }

    /**
     * Función que inicia una hebra que se ejecuta cada cierto tiempo con el objetivo de simular al usuario
     * caminando por el mapa, es decir, moviéndose a una nueva ubicación cercana, que puede ser lineal o
     * aleatoria, en una fecha y hora determinada que va incrementando. En cada nueva ubicación, el usuario
     * calcula y guarda en su base de datos su exposición ambiental, utilizando para ello los datos de los
     * sensores de la base de datos de geo-sensores más cercanos.
     */
    public void tareaExposicionUsuario() {
        handler.postDelayed(new Runnable() {
            public void run() { // Hebra a ejecutar cada x segundos
                tv.setText("Usuario caminando y calculando su exposición...");
                // Obtenemos los datos del server de manera asíncrona y llamamos a la función para calcular la exposición del usuario con los datos de las estaciones de alrededor
                Call<SensorGeoData []> call2 = AsynRestSensorData.init().getSnapShot(city);
                AsynRestSensorData.MyCall<SensorGeoData[]> mycall2=new AsynRestSensorData.MyCall<SensorGeoData[]>(
                        (listaDatosSensores)->{
                            if (listaDatosSensores.length!=0){ // Si hay sensores en la BBDD, calculamos la exposición del usuario
                                calcularExposicionUsuario(listaDatosSensores, tiempoSimulado, ubicaciones.get(ubicaciones.size()-1));
                                Ubicacion nuevaUbicacion = new Ubicacion(ubicaciones.get(ubicaciones.size()-1), usuarioAleatorio);
                                ubicaciones.add(nuevaUbicacion);
                                tiempoSimulado+= Constantes.time;
                            }
                        }
                ,MainActivity.this);
                mycall2.execute(call2);

                handler.postDelayed(this, Constantes.time); // Dormimos la hebra unos segundos
            }

        }, Constantes.time);
    }

    /**
     * Función que dibuja cada nuevo punto del recorrido del usuario en el mapa, encuentra los sensores
     * más cercanos a él (si los hay) y calcula una media de exposición para cada propiedad en función
     * de la distancia. El resultado se guarda en la base de datos del usuario (sólo si hay sensores cerca).
     * @param listaSensores Array con los datos de los sensores de la base de datos
     * @param timestamp Tiempo en milisegundos de la fecha y hora actuales (simulada) en ese punto del recorrido del usuario
     * @param ubicacion Ubicación actual (simulada) del usuario
     */
    private void calcularExposicionUsuario(SensorGeoData[] listaSensores, long timestamp, Ubicacion ubicacion) {
        // Obtener ubicación usuario y pintarla, calcular media con sensores y guardar en la BBDD del usuario
        // Cambiamos el marcador anterior del usuario del mapa (si lo hay) por un punto que marque el recorrido
        if(markerUsuario!=null) {
            markerUsuario.closeInfoWindow();
            Drawable d = ResourcesCompat.getDrawable(getResources(), R.mipmap.point, null);
            Bitmap bitmap = ((BitmapDrawable) d).getBitmap();
            Drawable dr = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, (int) (12.0f * getResources().getDisplayMetrics().density), (int) (12.0f * getResources().getDisplayMetrics().density), true));
            markerUsuario.setIcon(dr);
            markerUsuario.setTitle("Recorrido de " + usuario);
        }
        // Pintamos la ubicación del usuario en el mapa
        GeoPoint startPoint = new GeoPoint(ubicacion.getLat(), ubicacion.getLon());
        Marker startMarker = new Marker(map);
        Drawable d = ResourcesCompat.getDrawable(getResources(), R.mipmap.user, null);
        Bitmap bitmap = ((BitmapDrawable) d).getBitmap();
        Drawable dr = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, (int) (48.0f * getResources().getDisplayMetrics().density), (int) (48.0f * getResources().getDisplayMetrics().density), true));
        startMarker.setIcon(dr);
        startMarker.setTitle(usuario);
        startMarker.setPosition(startPoint);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        markerUsuario = startMarker;
        // Creamos el intent
        Intent i = new Intent(this, PropiedadesUsuarioActivity.class);
        // Configuramos el listener del nuevo marcador del usuario
        startMarker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker, MapView mapView) {
                InfoWindow.closeAllInfoWindowsOn(map);
                marker.showInfoWindow();
                mapView.getController().animateTo(marker.getPosition());
                // Pasamos parámetros al intent
                i.putExtra("device", usuario);
                startActivity(i);
                return true;
            }
        });
        // Añadimos el nuevo marcador al mapa y refrescamos
        map.getOverlays().add(startMarker);
        map.invalidate();

        // Calculamos el peso de los sensores ubicados a menos de x km del usuario, el resto tendrán peso 0
        boolean encontrado = false;
        for(SensorGeoData sensor:listaSensores){
            float distancia = calcularDistanciaHaversine(ubicacion.getLon(), ubicacion.getLat(), sensor.lon, sensor.lat);
            if (distancia < Constantes.R){
                sensor.w = 1 - distancia/Constantes.R;
                encontrado = true;
            }else{
                sensor.w = 0;
            }
        }

        if (encontrado){ // Si se encuentra algún sensor cerca del usuario...
            // Calculamos la media de exposición del usuario para cada propiedad que se encuentre (usamos un diccionario)
            Map<String, MediaPropiedad> diccionario = new HashMap<String, MediaPropiedad>();
            for(SensorGeoData sensor:listaSensores){
                if (sensor.w!=0) { // Si se trata de una estación cercana...
                    if (!diccionario.containsKey(sensor.property)){ // Si la propiedad no está en el diccionario, lo metemos con sus valores para calcular la media
                        diccionario.put(sensor.property, new MediaPropiedad(sensor.value*sensor.w, sensor.w));
                    }else{ // Si ya existe, sumamos los nuevos valores para calcular la media
                        MediaPropiedad mediaProp = diccionario.get(sensor.property);
                        mediaProp.sumarADividendo(sensor.value*sensor.w);
                        mediaProp.sumarADivisor(sensor.w);
                    }
                }
            }

            // Recorremos el diccionario para calcular la media de cada propiedad del usuario y la guardamos en la BBDD
            for (Map.Entry<String, MediaPropiedad> entry : diccionario.entrySet()) {
                float media = entry.getValue().calcularMedia();
                // Insertamos los datos del usuario, propiedad y media en el server de manera asíncrona
                Call<SensorGeoData> call = AsynRestSensorData.init().insertSensorData(usuario, "mobile", entry.getKey(), media, timestamp, (float)ubicacion.getLat(), (float)ubicacion.getLon());
                AsynRestSensorData.MyCall<SensorGeoData> mycall=new AsynRestSensorData.MyCall<SensorGeoData>(
                        (sensorInsertado)->{
                            Toast toastMsg = Toast.makeText(this, "Insertado " + sensorInsertado.property + " del usuario = " + MediaPropiedad.formatearDecimales(media, 1) , Toast.LENGTH_SHORT);
                            toastMsg.setGravity(Gravity.BOTTOM, 0, 180);
                            toastMsg.show();
                        }
                ,this);
                mycall.execute(call);
            }

        }

    }

    /**
     * Función que calcula la distancia entre dos puntos en latitud y longitud, usando para ello el método de Haversine.
     * @param lon1 Longitud del punto de origen
     * @param lat1 Latitud del punto de origen
     * @param lon2 Longitud del punto de destino
     * @param lat2 Latitud del punto de destino
     * @return Devuelve la distancia entre los dos puntos pasados en kilómetros
     */
    private float calcularDistanciaHaversine(double lon1, double lat1, double lon2, double lat2) {
        final int R = 6371; // Radio de la Tierra en km

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c;

        return (float) distance;
    }

    /**
     * Función que copia los datos de los sensores de la base de datos a una variable de clase accesible,
     * para ahorrar consultas consecutivas al servidor.
     * @param listaDatosSensores Array con los datos de los sensores de la base de datos
     */
    private void setListaSensoresGeoData(SensorGeoData[] listaDatosSensores) {
        listaSensoresGeoData = Arrays.copyOf(listaDatosSensores, listaDatosSensores.length);
    }

    /**
     * Función que traduce las coordenadas (x,y) del canvas a latitud y longitud en el mapa y calcula la
     * exposición ambiental de la propiedad seleccionada en los ajustes en ese punto.
     * @param x Valor de la coordenada X en el canvas
     * @param y Valor de la coordenada Y en el canvas
     * @return Devuelve la media de exposición para la propiedad elegida en el punto del mapa equivalente
     * al punto (x,y) del canvas o devuelve -1 en caso de no disponer de los datos de los sensores del servidor
     * o de no encontrar estaciones cerca de la propiedad seleccionada
     */
    public float computeExposition(int x, int y){
        // Transformamos el punto (x,y) del canvas en latitud y longitud en el mapa
        Projection proj = map.getProjection();
        GeoPoint loc = (GeoPoint) proj.fromPixels(x,y);
        double lat = ((double)loc.getLatitudeE6())/1000000;
        double lon = ((double)loc.getLongitudeE6())/1000000;

        // Calculamos la exposición en ese punto del mapa
        return calcularExposicionPropiedad(listaSensoresGeoData, new Ubicacion((float)lat, (float)lon), propiedadMapaColores);
    }

    /**
     * Función que calcula la media de la exposición ambiental de la propiedad pasada en una ubicación dada,
     * utilizando para ello los datos de los sensores de la base de datos de geo-sensores más cercanos.
     * @param listaSensores Array con los datos de los sensores de la base de datos
     * @param ubicacion Ubicación en el mapa donde se quiere calcular la exposición ambiental de una propiedad
     * @param propiedad Propiedad medida por los sensores de la que se quiere calcular la exposición ambiental en un punto determinado
     * @return Devuelve la media de exposición para la propiedad elegida en el punto del mapa equivalente
     * al punto (x,y) del canvas o devuelve -1 en caso de no disponer de los datos de los sensores del servidor
     * o de no encontrar estaciones cerca de la propiedad pasada
     */
    private float calcularExposicionPropiedad(SensorGeoData[] listaSensores, Ubicacion ubicacion, String propiedad) {
        float media = -1;

        if (listaSensores!=null){ // Si disponemos de la lista de sensores...
            // Calculamos el peso de los sensores de la propiedad elegida ubicados a menos de 5 km del punto dado que han registrado datos como máximo 6h antes del tiempo actual, el resto tendrán peso 0
            boolean encontrado = false;
            for(SensorGeoData sensor:listaSensores){
                float distancia = calcularDistanciaHaversine(ubicacion.getLon(), ubicacion.getLat(), sensor.lon, sensor.lat);
                if (distancia<Constantes.R && sensor.property.equals(propiedad) && sensor.timestamp>tiempoSimulado-21600000){ //6h
                    sensor.w = 1 - distancia/Constantes.R;
                    encontrado = true;
                }else{
                    sensor.w = 0;
                }
            }

            if (encontrado){ // Si se encuentra algún sensor de la propiedad cerca de la ubicación dada...
                // Calculamos la media de exposición en ese punto para la propiedad elegida
                MediaPropiedad mediaProp = new MediaPropiedad(0,0);
                for(SensorGeoData sensor:listaSensores){
                    if (sensor.w!=0) { // Si se trata de una estación cercana de la propiedad elegida...
                        mediaProp.sumarADividendo(sensor.value*sensor.w);
                        mediaProp.sumarADivisor(sensor.w);
                    }
                }

                if (mediaProp.getDivisor()!=0){
                    media = mediaProp.calcularMedia();
                }

            }
        }

        return media;
    }

    /**
     * Función que pasa a la activity de Ajustes de la aplicación.
     * @param view Vista del botón que llama a esta función
     */
    public void ajustes(View view){
        Intent i = new Intent(this, AjustesActivity.class);
        i.putExtra("device", city);
        startActivity(i);
    }

    /**
     * Función que hace zoom en el mapa para mostrar el cuadro delimitador pasado, donde se encuentran los sensores pintados.
     * @param box Cuadro delimitador calculado a partir de la ubicación de los sensores pintados en el mapa
     */
    public void zoomToBounds(final BoundingBox box) {
        if (map.getHeight() > 0) {
            map.zoomToBoundingBox(box, true);

        } else {
            ViewTreeObserver vto = map.getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

                @Override
                public void onGlobalLayout() {
                    map.zoomToBoundingBox(box, true);
                    ViewTreeObserver vto2 = map.getViewTreeObserver();

                    vto2.removeOnGlobalLayoutListener(this);

                }
            });
        }
    }

    /**
     * Función que calcula un cuadro delimitador a partir de las localizaciones de los sensores de la base de datos pasados.
     * @param properties Array con los sensores de la base de datos dibujados en el mapa, incluyendo su ubicación
     * @return Devuelve un cuadro delimitador calculado a partir de la ubicación de los sensores pasados
     */
    public BoundingBox computeArea(SensorProperty [] properties) {
        double nord = 0, sud = 0, ovest = 0, est = 0;
        int i = 0;

        for(SensorProperty sensor:properties){
            double lat = sensor.lat;
            double lon = sensor.lon;

            if ((i == 0) || (lat > nord)) nord = lat;
            if ((i == 0) || (lat < sud)) sud = lat;
            if ((i == 0) || (lon < ovest)) ovest = lon;
            if ((i == 0) || (lon > est)) est = lon;

            i++;
        }

        return new BoundingBox(nord, est, sud, ovest);
    }

    // Métodos de Android

    public void onResume(){
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    public void onPause(){
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }

    // Métodos de OpenStreetMap

    @Override
    public boolean singleTapConfirmedHelper(GeoPoint p) {
        InfoWindow.closeAllInfoWindowsOn(map);
        return true;
    }

    @Override
    public boolean longPressHelper(GeoPoint p) {
        // DO NOTHING FOR NOW
        return false;
    }

}