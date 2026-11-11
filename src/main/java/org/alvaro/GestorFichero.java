package org.alvaro;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class GestorFichero {

    private final Path RUTA_FICHERO;

    // Constructor de la clase
    public GestorFichero(Path RUTA_FICHERO) {
        this.RUTA_FICHERO = RUTA_FICHERO;
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
}
