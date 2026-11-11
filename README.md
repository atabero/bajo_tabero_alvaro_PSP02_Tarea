# Lectores y escritores en Java

## Descripción

Este proyecto implementa el problema clásico de los lectores y escritores utilizando hilos (`Thread`) y semáforos (`Semaphore`) en Java.

El programa trabaja con un fichero de texto llamado `paises.txt`. Cada línea contiene el nombre de un país y su capital, separados por dos puntos (`:`).

Ejemplo:

```text
Austria:Viena
Francia:Paris
Japon:Tokio
```

## Requisitos del ejercicio

La solución cumple los siguientes requisitos:

- Pueden acceder al fichero hasta cuatro lectores simultáneamente.
- Varios lectores pueden consultar el fichero al mismo tiempo.
- No puede haber escritores modificando el fichero mientras haya lectores accediendo.
- Solo un escritor puede modificar el fichero cada vez.
- Un escritor comprueba si el país ya existe antes de añadirlo.
- Si un lector no encuentra el país, espera a que un escritor lo incorpore y vuelve a realizar la búsqueda.
- La sincronización se realiza mediante semáforos.

## Estructura del proyecto

```text
src/
└── org/
    └── org.alvaro/
        ├── Main.java
        ├── GestorFichero.java
        ├── Lector.java
        └── Escritor.java
```

El fichero `paises.txt` se crea en el directorio de trabajo al ejecutar el programa.

## Funcionamiento

### 1. Clase `Main`

La clase principal:

1. Crea o restaura `paises.txt` con seis países iniciales.
2. Crea una instancia compartida de `GestorFichero`.
3. Crea cuatro lectores:
   - Italia
   - Francia
   - Portugal
   - Inglaterra
4. Inicia los lectores.
5. Espera hasta que el lector de Inglaterra registre que no ha encontrado su país.
6. Crea e inicia tres escritores:
   - Noruega: Oslo
   - Holanda: Amsterdam
   - Inglaterra: Londres
7. Espera la finalización de los siete hilos.
8. Muestra el contenido final del fichero.

El fichero se restaura al estado inicial en cada ejecución para que las pruebas sean reproducibles.

### 2. Clase `GestorFichero`

Centraliza el acceso al fichero y la sincronización entre lectores y escritores.

Utiliza los siguientes semáforos:

| Semáforo | Función |
|---|---|
| `SEMAFORO_ACCESO_FICHERO` | Garantiza el acceso exclusivo de los escritores y bloquea la escritura mientras haya lectores. |
| `SEMAFORO_LECTORES` | Protege el contador de lectores activos. |
| `SEMAFORO_LIMITE_LECTORES` | Limita a cuatro el número de lectores simultáneos. |
| `SEMAFORO_ACTUALIZACION` | Protege la cola de lectores que esperan la incorporación de un país. |
| `SEMAFORO_LECTOR_PENDIENTE` | Avisa a `Main` cuando se registra una solicitud de lectura pendiente. |
| `SEMAFORO_AVISO` | Cada solicitud tiene su propio semáforo para esperar la incorporación del país buscado. |
| `SEMAFORO_FINALIZACION` | Compartido por los hilos para que `Main` pueda esperar a que terminen. |

El acceso de lectura se gestiona mediante un contador:

- El primer lector adquiere el acceso al fichero.
- Los siguientes lectores pueden entrar mientras no se supere el límite de cuatro.
- El último lector libera el acceso para que pueda entrar un escritor.

Cuando un lector no encuentra su país, registra una solicitud con el país buscado y libera el acceso de lectura. De esta forma, el escritor puede modificar el fichero. Cuando se incorpora el país, se despierta únicamente a los lectores que estaban esperando ese país.

### 3. Clase `Lector`

Cada objeto `Lector` representa un hilo que consulta la capital de un país mediante `GestorFichero`.

Si el país todavía no existe, el hilo permanece esperando hasta recibir el aviso de actualización.

### 4. Clase `Escritor`

Cada objeto `Escritor` representa un hilo que intenta añadir un país y su capital.

El gestor comprueba si el país ya existe. Si no existe, lo añade al final del fichero y despierta a los lectores que esperan ese país.

## Ejecución de la prueba

Al ejecutar `Main`, se realizan simultáneamente las consultas de los cuatro lectores.

Los lectores de Italia, Francia y Portugal encuentran sus capitales. El lector de Inglaterra no encuentra el país y queda esperando.

A continuación, se inician los escritores. Estos añaden Noruega, Holanda e Inglaterra. Cuando Inglaterra se incorpora al fichero, el lector que estaba esperando se despierta, vuelve a consultar el fichero y obtiene Londres.

El orden exacto de los mensajes puede variar entre ejecuciones debido a la planificación de los hilos.

## Contenido final esperado

El fichero debe contener:

```text
Austria:Viena
Francia:Paris
Japon:Tokio
Alemania:Berlin
Portugal:Lisboa
Italia:Roma
Noruega:Oslo
Holanda:Amsterdam
Inglaterra:Londres
```

## Pruebas recomendadas

- Ejecutar el programa varias veces y comprobar que el fichero siempre parte de los seis países iniciales.
- Comprobar que los cuatro lectores pueden consultar sin superar el límite establecido.
- Comprobar que el lector de Inglaterra espera hasta que se añade el país.
- Comprobar que un país existente no se añade de nuevo.
- Comprobar que los escritores no modifican el fichero mientras hay lectores accediendo.
- Revisar que el fichero final no contiene líneas vacías ni entradas duplicadas.

## Tecnologías utilizadas

- Java
- Hilos (`Thread`)
- Semáforos (`Semaphore`)
- Ficheros de texto (`Files`, `Path`)
- Codificación UTF-8
