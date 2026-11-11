package org.alvaro;


import java.io.IOException;

public class Lector extends Thread {

    private final GestorFichero GESTOR_FICHERO;
    private final String PAIS_BUSCADO;

    // Constructor del hilo lector
    public Lector(GestorFichero GESTOR_FICHERO, String PAIS_BUSCADO) {
        this.GESTOR_FICHERO = GESTOR_FICHERO;
        this.PAIS_BUSCADO = PAIS_BUSCADO;
    }

    @Override
    public void run() {

        try {

            System.out.println(
                    getName() + " busca la capital de " + PAIS_BUSCADO
            );

            final String CAPITAL =
                    GESTOR_FICHERO.buscarCapitalEsperando(PAIS_BUSCADO);

            System.out.println(
                    getName() + ": la capital de "
                            + PAIS_BUSCADO + " es " + CAPITAL
            );

        } catch (InterruptedException e) {

            System.err.println(
                    getName() + ": se ha interrumpido la consulta."
            );

            Thread.currentThread().interrupt();

        } catch (IOException e) {

            System.err.println(
                    getName() + ": error al consultar el fichero: "
                            + e.getMessage()
            );
        }
    }
}
