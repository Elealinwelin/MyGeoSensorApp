package emrd.uja.org.mygeosensorapp.gui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import emrd.uja.org.mygeosensorapp.activities.MainActivity;

/**
 * Vista que muestra un canvas con una cuadrícula de colores que varía de verde-naranja-rojo según el
 * nivel de los valores de exposición ambiental de la propiedad elegida calculados en el mapa sobre el
 * que se superpone (siempre que haya sensores que midan esa propiedad). El usuario tiene la opción de
 * cambiar los diferentes parámetros del canvas desde los ajustes de la aplicación: propiedad a mostrar,
 * umbrales L1 y L2, y tamaño de los cuadrados.
 */
public class CanvasView extends View {
    private MainActivity activity = null;

    // Constructores
    public CanvasView (Context context) {
        super(context);
    }

    public CanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CanvasView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    // Setter
    public void setMainActivity(MainActivity activity){
        this.activity = activity;
    }

    /**
     * Función que (re)dibuja un canvas con una cuadrícula de colores según los valores de exposición ambiental
     * calculados en los puntos del mapa equivalentes a los de los cuadrados del canvas. Donde no haya sensores
     * para realizar el cálculo, el color será transparente. El tamaño de los cuadros es dinámico.
     * @param canvas Canvas sobre el que se va a dibujar
     */
    @Override
    protected void onDraw(Canvas canvas) {
        // Dibujar aquí
        canvas.drawColor(Color.TRANSPARENT);
        // Dibujamos una cuadrícula de colores según la exposición ambiental calculada en cada punto
        for(int x=0; x<this.getWidth(); x+=MainActivity.tamCuadrados)
            for(int y=0; y<this.getHeight(); y+=MainActivity.tamCuadrados) {
                Paint pincel = new Paint();
                pincel.setColor(getColor(x, y));
                pincel.setStrokeWidth(8);
                pincel.setStyle(Paint.Style.FILL);
                RectF rect = new RectF(x, y, x+MainActivity.tamCuadrados, y+MainActivity.tamCuadrados);
                canvas.drawRect(rect, pincel);
            }
    }

    /**
     * Función que devuelve un color en función del nivel de exposición ambiental de la propiedad seleccionada
     * en los ajustes calculado sobre el punto del mapa equivalente al punto (x,y) del canvas.
     * @param x Valor de la coordenada X en el canvas
     * @param y Valor de la coordenada Y en el canvas
     * @return Devuelve un entero que representa un color verde, rojo o una mezcla de ambos o devuelve un
     * color aleatorio en caso de que la activity sobre la que se dibuja el canvas tenga valor nulo
     */
    public int getColor(int x, int y){
        if(activity == null)
            return Color.argb(75, (int)(Math.random()*255.0), (int)(Math.random()*255.0), (int)(Math.random()*255.0));  //Color aleatorio

        float exposicion = activity.computeExposition(x,y);
        if (exposicion == -1)
            return Color.TRANSPARENT;
        else{
            float saludable = calcularTrapezoidal(exposicion);
            float toxico = 1 - saludable;
            return Color.argb(80,
                    (int)(255.0*toxico),
                    (int)(255.0*saludable),
                    0);
        }

    }

    /**
     * Función que pone en práctica los principios de la lógica difusa, aplicando un grado de pertenencia
     * a la exposición ambiental de una propiedad calculada en un punto determinado. Los umbrales L1 y L2
     * pueden ser modificados por el usuario en los ajustes de la aplicación.
     * @param exposicion Valor de la exposición ambiental de la propiedad seleccionada calculada en un punto determinado
     * @return Devuelve el valor de pertenencia de la exposición pasada, suavizada si se trata de un valor intermedio del conjunto (al aplicar la raíz)
     */
    private float calcularTrapezoidal(float exposicion) {
        // Recuperamos los niveles de corte para la propiedad seleccionada de las variables estáticas del MainActivity
        float L1 = MainActivity.umbralL1;
        float L2 = MainActivity.umbralL2;

        // Calculamos el grado de pertenencia de x (exposición)
        if (exposicion<=L1)
            return 1;
        if (exposicion>=L2)
            return 0;

        return (float) Math.sqrt((L2-exposicion)/(L2-L1)); // La raíz suaviza la función
    }

}
