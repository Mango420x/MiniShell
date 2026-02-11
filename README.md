💻 Java Minishell

Una implementación robusta de un intérprete de comandos (Shell) desarrollado en Java. Este proyecto simula el comportamiento de una terminal Unix, gestionando el ciclo de vida de procesos, tuberías y redirecciones de archivos.

🔥 Funcionalidades Implementadas

Ejecución de Comandos: Lanzamiento de procesos externos del sistema.

Sistema de Pipes (|): Encadenamiento de comandos donde la salida de uno es la entrada del siguiente.

Redirecciones:

    Entrada (<): Lee datos desde un archivo.

    Salida (> y >>): Escribe o concatena la salida en un archivo.

Background Processes (&): Ejecución de tareas en segundo plano sin bloquear el prompt.

Prompt Interactivo: Interfaz de línea de comandos persistente con lectura de System.in.

🛠️ Arquitectura Interna

El minishell sigue el patrón clásico de los intérpretes de comandos: Lectura → Análisis → Ejecución.

Tokenizer: La clase encargada de limpiar el input del usuario, separar los argumentos y detectar símbolos especiales (|, <, >, &).

Process Builder: Uso de la API ProcessBuilder de Java para gestionar los procesos hijos y sus redirecciones de entrada/salida.

Stream Handling: Gestión de flujos de datos para permitir que el Shell no se cuelgue mientras espera la finalización de un comando.

🚀 Guía de Uso

Requisitos

    JDK 11 o superior.

Ejecución

Clona el repositorio:

    git clone https://github.com/sargon494/Minishell.git

Compila el proyecto:

    javac *.java

Lanza el Minishell:

    java Main

Ejemplos de Comandos Soportados
  
    $ ls -l | grep .java
    $ cat input.txt > output.txt
    $ sleep 10 &
    $ help

👥 Autores

 David Perez : https://github.com/DavidPP161

Felipe Coronado : https://github.com/sargon494

Antoine Giz : https://github.com/Mango420x 

📚 Objetivos Académicos

Este proyecto fue desarrollado para la asignatura de Programación de Servicios y Procesos, cubriendo los siguientes puntos:

Gestión de procesos hijos y comunicación interprocesos (IPC).

Manejo de flujos de entrada/salida y errores.

Parsing de strings y lógica de control de flujo.
