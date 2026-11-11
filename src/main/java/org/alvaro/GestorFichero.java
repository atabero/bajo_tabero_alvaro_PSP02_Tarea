package org.alvaro;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.Semaphore;

public class GestorFichero {

    // Controla el acceso exclusivo al fichero.
    private final Semaphore SEMAFORO_ACCESO_FICHERO =
            new Semaphore(1, true);

    // Protege el contador de lectores activos.
    private final Semaphore SEMAFORO_LECTORES =
            new Semaphore(1, true);

    // Limita a cuatro el numero de lectores simultaneos.
    private final Semaphore SEMAFORO_LIMITE_LECTORES =
            new Semaphore(4, true);

    // Protege la cola de solicitudes de lectura pendientes.
    private final Semaphore SEMAFORO_ACTUALIZACION =
            new Semaphore(1, true);

    // Avisa a la clase principal cuando se registra una solicitud pendiente.
    private final Semaphore SEMAFORO_LECTOR_PENDIENTE =
            new Semaphore(0);

    // Cola con las solicitudes de lectura que esperan una actualizacion.
    private final Queue<SolicitudLectura> LECTORES_ESPERANDO =
            new ArrayDeque<>();

    // Ruta del fichero compartido.
    private final Path RUTA_FICHERO;

    // Numero de lectores que estan accediendo al fichero.
    private int LECTORES_ACTIVOS = 0;

    // Representa a un lector que espera la incorporacion de un pais.
    private static class SolicitudLectura {

        private final String PAIS_BUSCADO;
        private final Semaphore SEMAFORO_AVISO = new Semaphore(0);

        private SolicitudLectura(String PAIS_BUSCADO) {
            this.PAIS_BUSCADO = PAIS_BUSCADO;
        }
    }

    // Constructor del gestor.
    public GestorFichero(Path RUTA_FICHERO) {
        this.RUTA_FICHERO = RUTA_FICHERO;
    }

    /**
     * Permite la entrada de un lector.
     * El primer lector bloquea el acceso a los escritores.
     */
    private void entrarLector() throws InterruptedException {

        // Reservamos una de las cuatro plazas disponibles.
        SEMAFORO_LIMITE_LECTORES.acquire();

        try {
            SEMAFORO_LECTORES.acquire();
        } catch (InterruptedException e) {
            SEMAFORO_LIMITE_LECTORES.release();
            throw e;
        }

        try {
            LECTORES_ACTIVOS++;

            // El primer lector adquiere el acceso compartido al fichero.
            if (LECTORES_ACTIVOS == 1) {
                try {
                    SEMAFORO_ACCESO_FICHERO.acquire();
                } catch (InterruptedException e) {
                    LECTORES_ACTIVOS--;
                    SEMAFORO_LIMITE_LECTORES.release();
                    throw e;
                }
            }
        } finally {
            SEMAFORO_LECTORES.release();
        }
    }

    /**
     * Permite la salida de un lector.
     * El ultimo lector libera el acceso a los escritores.
     */
    private void salirLector() {

        SEMAFORO_LECTORES.acquireUninterruptibly();

        try {
            LECTORES_ACTIVOS--;

            // El ultimo lector permite entrar a un escritor.
            if (LECTORES_ACTIVOS == 0) {
                SEMAFORO_ACCESO_FICHERO.release();
            }
        } finally {
            SEMAFORO_LECTORES.release();
            SEMAFORO_LIMITE_LECTORES.release();
        }
    }

    /**
     * Busca secuencialmente la capital de un pais.
     * Devuelve null si el pais no existe.
     */
    private String buscarCapital(String PAIS) throws IOException {

        for (final String LINEA : Files.readAllLines(
                RUTA_FICHERO,
                StandardCharsets.UTF_8
        )) {
            final String[] DATOS = LINEA.split(":", 2);

            if (DATOS.length == 2
                    && DATOS[0].trim().equalsIgnoreCase(PAIS)) {
                return DATOS[1].trim();
            }
        }

        return null;
    }

    /**
     * Consulta la capital de un pais.
     * Si no existe, el lector espera a que se incorpore ese pais
     * y vuelve a realizar la busqueda.
     */
    public void consultarCapital(String PAIS)
            throws InterruptedException, IOException {

        while (true) {

            entrarLector();

            String CAPITAL;
            SolicitudLectura SOLICITUD = null;

            try {
                CAPITAL = buscarCapital(PAIS);

                if (CAPITAL == null) {

                    // Registramos la solicitud antes de abandonar la lectura.
                    SEMAFORO_ACTUALIZACION.acquire();

                    try {
                        SOLICITUD = new SolicitudLectura(PAIS);
                        LECTORES_ESPERANDO.add(SOLICITUD);

                        // Avisamos a la clase principal de que hay un lector pendiente.
                        SEMAFORO_LECTOR_PENDIENTE.release();

                    } finally {
                        SEMAFORO_ACTUALIZACION.release();
                    }
                }

            } catch (InterruptedException | IOException e) {
                salirLector();
                throw e;
            }

            // Liberamos el acceso de lectura antes de esperar al escritor.
            salirLector();

            if (CAPITAL != null) {
                System.out.println(
                        Thread.currentThread().getName()
                                + ": la capital de " + PAIS
                                + " es " + CAPITAL
                );
                return;
            }

            System.out.println(
                    Thread.currentThread().getName()
                            + " espera a que se añada " + PAIS
            );

            try {
                // Esperamos a que un escritor incorpore el pais solicitado.
                SOLICITUD.SEMAFORO_AVISO.acquire();

            } catch (InterruptedException e) {

                // Si sigue en la cola, retiramos la solicitud.
                SEMAFORO_ACTUALIZACION.acquireUninterruptibly();

                try {
                    LECTORES_ESPERANDO.remove(SOLICITUD);
                } finally {
                    SEMAFORO_ACTUALIZACION.release();
                }

                throw e;
            }

            // Al despertar, el bucle vuelve a buscar la capital en el fichero.
        }
    }

    /**
     * Añade un pais si todavia no existe.
     * Solamente un escritor puede modificar el fichero cada vez.
     */
    public void anadirPais(String PAIS, String CAPITAL)
            throws InterruptedException, IOException {

        SEMAFORO_ACCESO_FICHERO.acquire();

        try {
            if (buscarCapital(PAIS) != null) {
                System.out.println(
                        "El pais " + PAIS + " ya existe en el fichero."
                );
                return;
            }

            final String CONTENIDO = Files.readString(
                    RUTA_FICHERO,
                    StandardCharsets.UTF_8
            );

            // Añadimos un salto de linea solo si el fichero lo necesita.
            String SEPARADOR = "";

            if (!CONTENIDO.isEmpty()
                    && !CONTENIDO.endsWith("\n")
                    && !CONTENIDO.endsWith("\r")) {
                SEPARADOR = System.lineSeparator();
            }

            final String NUEVA_LINEA = SEPARADOR
                    + PAIS + ":" + CAPITAL
                    + System.lineSeparator();

            Files.writeString(
                    RUTA_FICHERO,
                    NUEVA_LINEA,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );

            System.out.println(
                    "Se ha añadido " + PAIS + ":" + CAPITAL + " al fichero."
            );

            // Despertamos solo a los lectores que esperan este pais.
            SEMAFORO_ACTUALIZACION.acquireUninterruptibly();

            try {
                final Iterator<SolicitudLectura> ITERADOR =
                        LECTORES_ESPERANDO.iterator();

                while (ITERADOR.hasNext()) {
                    final SolicitudLectura SOLICITUD = ITERADOR.next();

                    if (SOLICITUD.PAIS_BUSCADO.equalsIgnoreCase(PAIS)) {
                        ITERADOR.remove();
                        SOLICITUD.SEMAFORO_AVISO.release();
                    }
                }

            } finally {
                SEMAFORO_ACTUALIZACION.release();
            }

        } finally {
            SEMAFORO_ACCESO_FICHERO.release();
        }
    }

    /**
     * Espera hasta que un lector concreto no encuentre su pais.
     * Se utiliza para coordinar el inicio de los escritores en la prueba.
     */
    public void esperarLectorPendiente(String PAIS)
            throws InterruptedException {

        while (true) {

            SEMAFORO_LECTOR_PENDIENTE.acquire();
            SEMAFORO_ACTUALIZACION.acquire();

            try {
                for (final SolicitudLectura SOLICITUD : LECTORES_ESPERANDO) {
                    if (SOLICITUD.PAIS_BUSCADO.equalsIgnoreCase(PAIS)) {
                        return;
                    }
                }
            } finally {
                SEMAFORO_ACTUALIZACION.release();
            }
        }
    }
}
