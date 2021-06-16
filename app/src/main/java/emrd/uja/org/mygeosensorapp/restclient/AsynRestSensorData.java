package emrd.uja.org.mygeosensorapp.restclient;

import android.os.AsyncTask;
import android.util.Log;
import java.io.IOException;
import emrd.uja.org.mygeosensorapp.util.SensorGeoData;
import emrd.uja.org.mygeosensorapp.util.SensorProperty;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Clase que implementa la interfaz del cliente Android que consume los servicios de la API REST Java que
 * dan acceso a los datos de geo-sensores y usuarios de la base de datos en MongoDB. Usa la librería
 * Retrofit para consumir los servicios RESTful de la API. Las conexiones con la API se llevan a cabo de
 * manera asíncrona en segundo plano.
 */
public class AsynRestSensorData {

    static public interface ServiceSensorData{

        // Servicio para insertar nuevos datos de un sensor
        @GET("insert/{device}/{sensor}/{property}/{value}/{timestamp}/{lat}/{lon}")
        public Call<SensorGeoData> insertSensorData(
                @Path("device") String device,
                @Path("sensor") String sensor,
                @Path("property") String property,
                @Path("value") Float value,
                @Path("timestamp") Long timestamp,
                @Path("lat") Float lat,
                @Path("lon") Float lon);

        // Servicio para consultar las propiedades de los sensores de un determinado dispositivo (ciudad)
        @GET("query_sensor_properties/{device}")
        public Call<SensorProperty []> querySensorProperty(
                @Path("device") String device);

        // Servicio para consultar los datos de una propiedad concreta de un determinado sensor perteneciente a un dispositivo (ciudad) específico actualizado en los últimos X milisegundos
        @GET("query_device_sensor_property_time/{device}/{sensor}/{property}/{offtime}")
        public Call<SensorGeoData []> querySensorData(
                @Path("device") String device,
                @Path("sensor") String sensor,
                @Path("property") String property,
                @Path("offtime") Long offtime);

        // Servicio para consultar los datos de las últimas actualizaciones de los sensores de un determinado dispositivo (ciudad)
        @GET("query_snapshot/{device}")
        public Call<SensorGeoData []> getSnapShot(
                @Path("device") String device);

    }

    static public ServiceSensorData init(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.0.24:8092/MyGeoServlet/") // IP del portátil
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ServiceSensorData service = retrofit.create(ServiceSensorData.class);
        return service;
    }

    @FunctionalInterface
    static public interface Consume<T>{
        public void consume(T data);
    }

    static public class MyCall<T> extends AsyncTask<Call<T>,T,Boolean>{
        Consume<T> consumer;
        public MyCall(Consume<T> consumer){
            this.consumer=consumer;
        }
        @Override
        protected Boolean doInBackground(Call<T>... calls) {
            for(Call<T> call:calls){
                Log.d("MyGeo", "calling"+call);
                try {
                    Response<T> response=call.execute();
                    Log.d("MyGeo", "body"+response.message());
                    publishProgress(response.body());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return true;
        }
        @Override
        protected void onProgressUpdate(T... responses) {
            for(T response:responses) {
                Log.d("MyGeo", "class" + response.getClass().getName());
                this.consumer.consume(response);
            }
        }
    }

}


