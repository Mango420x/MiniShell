Minishell – Programación de Servicios y Procesos

Autor: David Perez, Felipe Coronado & Antoine Giz
Asignatura: Programación de Servicios y Procesos
Lenguaje: Java

Descripción

Este proyecto implementa un minishell en Java que simula el comportamiento básico de una terminal Linux.
Permite ejecutar comandos, usar pipes, redirecciones de entrada/salida y ejecutar procesos en segundo plano.

El programa interpreta comandos introducidos por el usuario a través del prompt interactivo, los analiza mediante la clase Tokenizer y los ejecuta creando procesos hijos.

Funcionalidades principales

Ejecución de comandos con uno o varios argumentos.

Pipes (|) entre procesos.

Redirección de entrada, salida y errores:

< archivo → redirige la entrada estándar.

> archivo → redirige la salida estándar (crea).

>> archivo → redirige la salida estándar (concatena).

2> archivo → redirige la salida de error.

Ejecución en segundo plano con &, mostrando el PID del proceso.

Comando interno cd, con rutas absolutas o relativas.

Gestión de errores para comandos inválidos o problemas de redirección.

Ejemplo de uso
# Ejecución simple
> echo Hola Mundo

# Ejecución con redirección
> ls -l > salida.txt

# Ejecución con pipe
> cat archivo.txt | grep palabra | wc -l

# Ejecución en segundo plano
> sleep 10 &
[12345]

Estructura y clases

MiniShell.java: Contiene la lógica principal del programa y la gestión de procesos.

Tokenizer.java: Proporcionada por el enunciado, se encarga de dividir la línea de comandos en tokens.

TCommand / TLine / MissingFileException: Clases auxiliares del Tokenizer para representar y analizar los comandos.

Objetivos alcanzados

Comandos en foreground y background.

Redirecciones de entrada y salida.

Pipes múltiples.

Comando interno cd.

Manejo de errores y excepciones.

Código comentado y documentado.

Ejecución

Compilar y ejecutar desde la terminal:

javac MiniShell.java
java MiniShell
