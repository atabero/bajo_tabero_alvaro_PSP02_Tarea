package org.alvaro;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        final Path RUTA_FICHERO = Path.of("paises.txt");

        try {

            // Comprobamos que el fichero existe
            if (!Files.exists(RUTA_FICHERO)) {
                System.out.println("No se encuentra el fichero de paises.");
                return;
            }

            // Leemos el contenido del fichero
            final List<String> PAISES = Files.readAllLines(
                    RUTA_FICHERO,
                    StandardCharsets.UTF_8
            );

            // Mostramos los registros almacenados
            System.out.println("Contenido inicial del fichero:\n");

            for (String PAIS : PAISES) {
                System.out.println(PAIS);
            }

        } catch (IOException e) {

            System.err.println(
                    "Error al leer el fichero: " + e.getMessage()
            );
        }
    }
}
