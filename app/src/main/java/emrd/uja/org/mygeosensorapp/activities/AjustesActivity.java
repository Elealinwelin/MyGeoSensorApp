package emrd.uja.org.mygeosensorapp.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import java.util.HashSet;
import emrd.uja.org.mygeosensorapp.restclient.AsynRestSensorData;
import emrd.uja.org.mygeosensorapp.R;
import emrd.uja.org.mygeosensorapp.util.SensorProperty;
import retrofit2.Call;

/**
 * Actividad que muestra una interfaz con los ajustes que pueden ser modificados de manera interactiva por
 * el usuario sobre el mapa de exposición ambiental por colores y sobre el tipo de recorrido simulado en el
 * mapa para cambiar el comportamiento de la aplicación y adaptarse a los parámetros que sean de su preferencia.
 */
public class AjustesActivity extends AppCompatActivity {
    private Spinner spPropiedad;
    private EditText etL1;
    private EditText etL2;
    private EditText etTamPixeles;
    private Switch switchUsuarioRandom;
    private int indicePropiedad;
    private String [] propertyOptions;
    private boolean initialDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ajustes);

        // Inicializamos las variables
        propertyOptions = null;
        initialDisplay = true;

        // Enlazamos al XML
        spPropiedad = (Spinner) findViewById(R.id.sp_propiedad);
        etL1 = (EditText) findViewById(R.id.et_L1);
        etL2 = (EditText) findViewById(R.id.et_L2);
        etTamPixeles = (EditText) findViewById(R.id.et_tam_pixeles);
        switchUsuarioRandom = (Switch) findViewById(R.id.switch_usuario_random);

        // Recuperamos los datos del intent
        String device = getIntent().getStringExtra("device");

        // Creamos el array con las opciones iniciales para el spinner mientras hacemos la consulta al server y se lo asignamos
        String [] initialPropertyOptions = {"<No disponible>"};
        ArrayAdapter<String> adapterInicial = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, initialPropertyOptions);
        spPropiedad.setAdapter(adapterInicial);

        HashSet<String> hashsetPropiedadesSensores = new HashSet<>();
        // Obtenemos los datos del server de manera asíncrona para recuperar las propiedades de la BBDD y mostrar las diferentes opciones en el spinner
        Call<SensorProperty[]> call = AsynRestSensorData.init().querySensorProperty(device);
        AsynRestSensorData.MyCall<SensorProperty[]> mycall=new AsynRestSensorData.MyCall<SensorProperty[]>(
                (listaPropiedadesSensores)->{
                    if(listaPropiedadesSensores.length!=0){ //Si hay sensores en la BBDD seleccionada...
                        // Metemos las propiedades que encontremos en el hashset
                        for (SensorProperty sensor : listaPropiedadesSensores){
                            for (String property : sensor.properties){
                                hashsetPropiedadesSensores.add(property);
                            }
                        }
                        // Creamos el array con las opciones
                        propertyOptions = new String[hashsetPropiedadesSensores.size()];
                        int cont = 0;
                        for (String prop : hashsetPropiedadesSensores){
                            propertyOptions[cont] = prop;
                            cont++;
                        }
                        // Asignamos las propiedades al spinner
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, propertyOptions);
                        spPropiedad.setAdapter(adapter);

                        // Configuramos el listener del spinner
                        spPropiedad.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                                if (initialDisplay) { // Si cambia el spinner a causa de la primera inicialización del array...
                                    // Recuperamos la propiedad a mostrar seleccionada
                                    indicePropiedad = 0;
                                    for (int i = 0; i < propertyOptions.length; i++) {
                                        if (propertyOptions[i].equals(MainActivity.propiedadMapaColores))
                                            indicePropiedad = i;
                                    }
                                    spPropiedad.setSelection(indicePropiedad);
                                    initialDisplay = false;
                                }else{ // Si el usuario ha cambiado su seleción en el spinner...
                                    // Cambiamos la propiedad seleccionada hasta ahora por la nueva
                                    MainActivity.propiedadMapaColores = parentView.getItemAtPosition(position).toString();
                                    indicePropiedad = position;
                                    MainActivity.tvSwitch.setText("Exposición por colores: " + MainActivity.propiedadMapaColores);
                                }
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parentView) {
                                // Do nothing
                            }

                        });

                    }
                }
        ,this);
        mycall.execute(call);

        // Recuperamos el resto de atributos seleccionados y los mostramos en los campos que corresponda
        etL1.setText(Integer.toString(MainActivity.umbralL1), TextView.BufferType.EDITABLE);
        etL1.setFilters(new InputFilter[] {new InputFilter.LengthFilter(9)});

        etL2.setText(Integer.toString(MainActivity.umbralL2), TextView.BufferType.EDITABLE);
        etL2.setFilters(new InputFilter[] {new InputFilter.LengthFilter(9)});

        etTamPixeles.setText(Integer.toString(MainActivity.tamCuadrados), TextView.BufferType.EDITABLE);
        etTamPixeles.setFilters(new InputFilter[] {new InputFilter.LengthFilter(9)});

        if (MainActivity.usuarioAleatorio)
            switchUsuarioRandom.setChecked(true);

        // Configuramos los listeners de todos los campos editables
        etL1.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                // Cambiamos el umbral L1 seleccionado hasta ahora por el nuevo valor
                if (s.length()>0){
                    MainActivity.umbralL1 = Integer.parseInt(s.toString());
                }else{
                    etL1.setError("Introduzca un número");
                    MainActivity.umbralL1 = 0;
                }
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        etL2.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                // Cambiamos el umbral L2 seleccionado hasta ahora por el nuevo valor
                if (s.length()>0){
                    MainActivity.umbralL2 = Integer.parseInt(s.toString());
                }else{
                    etL2.setError("Introduzca un número");
                    MainActivity.umbralL2 = 0;
                }
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        etTamPixeles.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                // Cambiamos el tamaño de los cuadrados seleccionado hasta ahora por el nuevo valor
                if (s.length()>0 && Integer.parseInt(s.toString())>0){
                    MainActivity.tamCuadrados = Integer.parseInt(s.toString());
                }else{
                    etTamPixeles.setError("Introduzca un número mayor que 0");
                    MainActivity.tamCuadrados = 25;
                }
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        switchUsuarioRandom.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // Cambiamos el recorrido del usuario a aleatorio
                    MainActivity.usuarioAleatorio = true;
                } else {
                    // Cambiamos el recorrido del usuario a lineal
                    MainActivity.usuarioAleatorio = false;
                }
            }
        });

    }

}
