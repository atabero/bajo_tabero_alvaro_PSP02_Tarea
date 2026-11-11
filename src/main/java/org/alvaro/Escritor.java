package org.alvaro;

import java.io.IOException;

public class Escritor extends Thread {

    private final GestorFichero GESTOR_FICHERO;
    private final String PAIS;
    private final String CAPITAL;

    // Constructor del hilo escritor
    public Escritor(
            GestorFichero GESTOR_FICHERO,
            String PAIS,
            String CAPITAL
    ) {
        this.GESTOR_FICHERO = GESTOR_FICHERO;
        this.PAIS = PAIS;
        this.CAPITAL = CAPITAL;
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
        }
    }
}