package org.alvaro;

import java.io.IOException;
import java.util.concurrent.Semaphore;

public class Lector extends Thread {

    private final GestorFichero GESTOR_FICHERO;
    private final String PAIS;
    private final Semaphore SEMAFORO_FINALIZACION;

    // Constructor del lector.
    public Lector(
            GestorFichero GESTOR_FICHERO,
            String PAIS,
            Semaphore SEMAFORO_FINALIZACION
    ) {
        this.GESTOR_FICHERO = GESTOR_FICHERO;
        this.PAIS = PAIS;
        this.SEMAFORO_FINALIZACION = SEMAFORO_FINALIZACION;
    }

    // Metodo que ejecuta el hilo.
    @Override
    public void run() {

        try {
            GESTOR_FICHERO.consultarCapital(PAIS);

        } catch (InterruptedException e) {
            System.out.println(
                    "El lector de " + PAIS + " ha sido interrumpido."
            );
            Thread.currentThread().interrupt();

        } catch (IOException e) {
            System.err.println(
                    "Error al consultar " + PAIS + ": " + e.getMessage()
            );

        } finally {
            // Indicamos que el lector ha finalizado.
            SEMAFORO_FINALIZACION.release();
        }
    }
}
