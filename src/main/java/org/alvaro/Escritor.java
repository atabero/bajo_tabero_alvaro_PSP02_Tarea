package org.alvaro;


import java.io.IOException;
import java.util.concurrent.Semaphore;

public class Escritor extends Thread {

    private final GestorFichero GESTOR_FICHERO;
    private final String PAIS;
    private final String CAPITAL;
    private final Semaphore FINALIZACION;

    // Constructor del hilo escritor
    public Escritor(
            GestorFichero GESTOR_FICHERO,
            String PAIS,
            String CAPITAL,
            Semaphore FINALIZACION
    ) {
        this.GESTOR_FICHERO = GESTOR_FICHERO;
        this.PAIS = PAIS;
        this.CAPITAL = CAPITAL;
        this.FINALIZACION = FINALIZACION;
    }

    @Override
    public void run() {

        try {

            System.out.println(
                    getName() + " intenta añadir " + PAIS
            );

            GESTOR_FICHERO.anadirPais(PAIS, CAPITAL);

        } catch (InterruptedException e) {

            System.err.println(
                    getName() + ": se ha interrumpido la escritura."
            );

            Thread.currentThread().interrupt();

        } catch (IOException e) {

            System.err.println(
                    getName() + ": error al modificar el fichero: "
                            + e.getMessage()
            );

        } finally {
            FINALIZACION.release();
        }
    }
}
