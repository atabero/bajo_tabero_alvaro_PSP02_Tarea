package org.alvaro;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.concurrent.Semaphore;

public class Main {

    public static void main(String[] args) {

        try {
            // Ruta del fichero de paises.
            final Path RUTA_FICHERO = Path.of("paises.txt");

            // Datos iniciales para que cada ejecucion sea reproducible.
            final List<String> PAISES_INICIALES = List.of(
                    "Austria:Viena",
                    "Francia:Paris",
                    "Japon:Tokio",
                    "Alemania:Berlin",
                    "Portugal:Lisboa",
                    "Italia:Roma"
            );

            // Creamos o restauramos el fichero con los datos iniciales.
            Files.write(
                    RUTA_FICHERO,
                    PAISES_INICIALES,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            );

            // Creamos el gestor compartido por todos los hilos.
            final GestorFichero GESTOR_FICHERO =
                    new GestorFichero(RUTA_FICHERO);

            // Semaforo que permite esperar a que terminen los siete hilos.
            final Semaphore SEMAFORO_FINALIZACION = new Semaphore(0);

            // Creamos los cuatro lectores.
            final Lector LECTOR_1 = new Lector(
                    GESTOR_FICHERO, "Italia", SEMAFORO_FINALIZACION
            );
            final Lector LECTOR_2 = new Lector(
                    GESTOR_FICHERO, "Francia", SEMAFORO_FINALIZACION
            );
            final Lector LECTOR_3 = new Lector(
                    GESTOR_FICHERO, "Portugal", SEMAFORO_FINALIZACION
            );
            final Lector LECTOR_4 = new Lector(
                    GESTOR_FICHERO, "Inglaterra", SEMAFORO_FINALIZACION
            );

            System.out.println("Iniciando los cuatro lectores...");

            LECTOR_1.start();
            LECTOR_2.start();
            LECTOR_3.start();
            LECTOR_4.start();

            // No iniciamos los escritores hasta que Inglaterra este pendiente.
            GESTOR_FICHERO.esperarLectorPendiente("Inglaterra");

            System.out.println("\nIniciando los tres escritores...");

            // Creamos los tres escritores.
            final Escritor ESCRITOR_1 = new Escritor(
                    GESTOR_FICHERO, "Noruega", "Oslo", SEMAFORO_FINALIZACION
            );
            final Escritor ESCRITOR_2 = new Escritor(
                    GESTOR_FICHERO, "Holanda", "Amsterdam", SEMAFORO_FINALIZACION
            );
            final Escritor ESCRITOR_3 = new Escritor(
                    GESTOR_FICHERO, "Inglaterra", "Londres", SEMAFORO_FINALIZACION
            );

            ESCRITOR_1.start();
            ESCRITOR_2.start();
            ESCRITOR_3.start();

            // Esperamos a que finalicen los cuatro lectores y los tres escritores.
            SEMAFORO_FINALIZACION.acquire(7);

            System.out.println("\nTodos los hilos han terminado.");
            System.out.println("\nContenido final del fichero:");

            Files.readAllLines(
                    RUTA_FICHERO,
                    StandardCharsets.UTF_8
            ).forEach(System.out::println);

            System.out.println("\nEJECUCION CORRECTA");

        } catch (InterruptedException e) {
            System.err.println("El hilo principal ha sido interrumpido.");
            Thread.currentThread().interrupt();

        } catch (IOException e) {
            System.err.println(
                    "Error al gestionar el fichero: " + e.getMessage()
            );
        }
    }
}
