package org.alvaro;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Semaphore;

public class GestorFichero {

    private final Path RUTA_FICHERO;

    private final Semaphore ACCESO_FICHERO = new Semaphore(1, true);
    private final Semaphore MUTEX_LECTORES = new Semaphore(1, true);
    private final Semaphore LIMITE_LECTORES = new Semaphore(4, true);

    private int lectoresActivos = 0;

    // Constructor de la clase
    public GestorFichero(Path RUTA_FICHERO) {
        this.RUTA_FICHERO = RUTA_FICHERO;
    }

    // Solicita acceso compartido para un lector
    public void iniciarLectura() throws InterruptedException {

        LIMITE_LECTORES.acquire();

        try {
            MUTEX_LECTORES.acquire();
        } catch (InterruptedException e) {
            LIMITE_LECTORES.release();
            throw e;
        }

        try {

            lectoresActivos++;

            if (lectoresActivos == 1) {
                ACCESO_FICHERO.acquire();
            }

        } catch (InterruptedException e) {

            lectoresActivos--;
            MUTEX_LECTORES.release();
            LIMITE_LECTORES.release();
            throw e;
        }

        MUTEX_LECTORES.release();
    }

    // Libera el acceso compartido del lector
    public void finalizarLectura() {

        MUTEX_LECTORES.acquireUninterruptibly();

        lectoresActivos--;

        if (lectoresActivos == 0) {
            ACCESO_FICHERO.release();
        }

        MUTEX_LECTORES.release();
        LIMITE_LECTORES.release();
    }

    // Busca secuencialmente la capital de un pais
    public String buscarCapital(String PAIS_BUSCADO) throws IOException {

        final List<String> LINEAS = Files.readAllLines(
                RUTA_FICHERO,
                StandardCharsets.UTF_8
        );

        for (String LINEA : LINEAS) {

            final String[] DATOS = LINEA.split(":", 2);

            if (DATOS.length == 2
                    && DATOS[0].trim().equalsIgnoreCase(PAIS_BUSCADO)) {

                return DATOS[1].trim();
            }
        }

        return null;
    }

    // Muestra el contenido completo del fichero
    public void mostrarContenido() throws IOException {

        final List<String> LINEAS = Files.readAllLines(
                RUTA_FICHERO,
                StandardCharsets.UTF_8
        );

        for (String LINEA : LINEAS) {
            System.out.println(LINEA);
        }
    }
    // Añade un pais y su capital si todavía no existe
    public boolean anadirPais(String PAIS, String CAPITAL)
            throws IOException, InterruptedException {

        ACCESO_FICHERO.acquire();

        try {

            final String CAPITAL_EXISTENTE = buscarCapital(PAIS);

            if (CAPITAL_EXISTENTE != null) {
                System.out.println(
                        "El pais " + PAIS + " ya existe en el fichero."
                );
                return false;
            }

            final String NUEVA_LINEA = PAIS + ":" + CAPITAL;

            Files.writeString(
                    RUTA_FICHERO,
                    System.lineSeparator() + NUEVA_LINEA,
                    StandardCharsets.UTF_8,
                    java.nio.file.StandardOpenOption.APPEND
            );

            System.out.println(
                    "Se ha añadido " + PAIS + ":" + CAPITAL + " al fichero."
            );

            return true;

        } finally {
            ACCESO_FICHERO.release();
        }
    }
}