package org.alvaro;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Semaphore;

public class Main {

    public static void main(String[] args) {

        final Path RUTA_FICHERO = Path.of("paises.txt");

        try {

            if (!Files.exists(RUTA_FICHERO)) {
                System.out.println("No se encuentra el fichero de paises.");
                return;
            }

            final GestorFichero GESTOR_FICHERO =
                    new GestorFichero(RUTA_FICHERO);

            final Semaphore FINALIZACION = new Semaphore(0);

            System.out.println("Contenido inicial del fichero:\n");

            GESTOR_FICHERO.mostrarContenido();

            System.out.println("\nIniciando los cuatro lectores:\n");

            final Lector LECTOR_1 =
                    new Lector(GESTOR_FICHERO, "Austria", FINALIZACION);

            final Lector LECTOR_2 =
                    new Lector(GESTOR_FICHERO, "Francia", FINALIZACION);

            final Lector LECTOR_3 =
                    new Lector(GESTOR_FICHERO, "Japon", FINALIZACION);

            final Lector LECTOR_4 =
                    new Lector(GESTOR_FICHERO, "España", FINALIZACION);

            LECTOR_1.start();
            LECTOR_2.start();
            LECTOR_3.start();
            LECTOR_4.start();

            System.out.println(
                    "\nEsperando a que un lector solicite España...\n"
            );

            GESTOR_FICHERO.esperarLectorPendiente();

            System.out.println(
                    "Un lector está esperando por España."
            );

            System.out.println("\nIniciando los tres escritores:\n");

            final Escritor ESCRITOR_1 =
                    new Escritor(
                            GESTOR_FICHERO,
                            "España",
                            "Madrid",
                            FINALIZACION
                    );

            final Escritor ESCRITOR_2 =
                    new Escritor(
                            GESTOR_FICHERO,
                            "Italia",
                            "Roma",
                            FINALIZACION
                    );

            final Escritor ESCRITOR_3 =
                    new Escritor(
                            GESTOR_FICHERO,
                            "Reino Unido",
                            "Londres",
                            FINALIZACION
                    );

            ESCRITOR_1.start();
            ESCRITOR_2.start();
            ESCRITOR_3.start();

            // Espera a que terminen los cuatro lectores y los tres escritores
            FINALIZACION.acquire(7);

            System.out.println("\nTodos los hilos han terminado.");

            System.out.println("\nContenido final del fichero:\n");

            GESTOR_FICHERO.mostrarContenido();

        } catch (InterruptedException e) {

            System.err.println(
                    "El hilo principal ha sido interrumpido."
            );

            Thread.currentThread().interrupt();

        } catch (IOException e) {

            System.err.println(
                    "Error al gestionar el fichero: " + e.getMessage()
            );
        }
    }
}
