package org.alvaro;

import java.io.IOException;
import java.util.concurrent.Semaphore;

public class Escritor extends Thread {

    private final GestorFichero GESTOR_FICHERO;
    private final String PAIS;
    private final String CAPITAL;
    private final Semaphore SEMAFORO_FINALIZACION;

    // Constructor del escritor.
    public Escritor(
            GestorFichero GESTOR_FICHERO,
            String PAIS,
            String CAPITAL,
            Semaphore SEMAFORO_FINALIZACION
    ) {
        this.GESTOR_FICHERO = GESTOR_FICHERO;
        this.PAIS = PAIS;
        this.CAPITAL = CAPITAL;
        this.SEMAFORO_FINALIZACION = SEMAFORO_FINALIZACION;
    }

    // Metodo que ejecuta el hilo.
    @Override
    public void run() {

        try {
            GESTOR_FICHERO.anadirPais(PAIS, CAPITAL);

        } catch (InterruptedException e) {
            System.out.println(
                    "El escritor de " + PAIS + " ha sido interrumpido."
            );
            Thread.currentThread().interrupt();

        } catch (IOException e) {
            System.err.println(
                    "Error al añadir " + PAIS + ": " + e.getMessage()
            );

        } finally {
            // Indicamos que el escritor ha finalizado.
            SEMAFORO_FINALIZACION.release();
        }
    }
}
