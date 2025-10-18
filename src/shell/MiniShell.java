package shell;

// Imports del paquete tokenizer, java utils y excepciones.
import exceptions.MissingFileException;
import tokenizer.TCommand;
import tokenizer.TLine;
import tokenizer.Tokenizer;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class MiniShell {

    public static void  main (String [] args) {
        // Creamos un Scanner para leer entradas de usuario en consola.
        Scanner scanner = new Scanner(System.in);

        while (true) {
            // Obtener el directorio actual (para imprimirlo en cada prompt).
            String cwd = System.getProperty("user.dir");

            // Mostrar el directorio actual
            System.out.println(cwd + "> ");

            // Leer una línea de texto introducida por el usuario.
            String input;
            try {
                input = scanner.nextLine();
            } catch (Exception e) {
                // Si ocurre un error o se usa (Ctrl + D), salimos del bucle.
                break;
            }

            // Si la entrada es null, se sale del bucle.
            if (input == null) {
                break;
            }

            // Limpiar el input de espacios en blanco al principio y al final.
            input = input.trim();

            // Si el input esta vacío, se vuelve a mostrar el prompt.
            if (input.isEmpty()) {
                continue;
            }

            // Si el usuario escribe "exit", terminamos la shell.
            if (input.equals("exit")) {
                System.out.println("Saliendo...");
                break;
            }

            // Variable donde se guardará el resultado del tokenizer.
            TLine tline;
            try {
                // Llamar al metodo tokenize para dividir la línea en comandos y argumentos.
                tline = Tokenizer.tokenize(input);
            } catch (MissingFileException me) {
                // Capturar error al no especificar archivo.
                System.err.println("Error de redirección: " + me.getMessage());
                continue;
            } catch (Exception e) {
                // Capturar cualquier otro error.
                System.err.println("Error al crear un token: " + e.getMessage());
                continue;
            }

            // Si el tokenizer devuelve null, no hay comandos que ejecutar.
            if (tline == null) {
                continue;
            }

            try {
                // Si el comando es "cd", se manejará mediante un metodo interno.
                if (isCD(tline)) {
                    executeCD(tline);
                }
                // Si es un comando sin pipes, se ejecuta con el ProcessBuilder.
                else if (tline.getNcommands() == 1) {
                    executeSimple(tline);
                }
                // Si es un comando con pipes, *WIP*
                else {
                    System.err.println("Pipes *WIP*");
                }
            } catch (Exception e) {
                // Capturar errores generales.
                System.err.println("Error: " + e.getMessage());
            }
        }

        // Cerrar scanner para ahorrar recursos.
        scanner.close();
    }

    // Comprobar si el comando es "cd".
    public static boolean isCD(TLine tline) {
        // Si hay más de un comando, no puede ser "cd".
        if (tline.getNcommands() != 1) {
            return false;
        }

        // Obtener el primer comando de la línea y si el nombre coincide con "cd".
        TCommand command = tline.getCommands().get(0);
        return "cd".equals(command.getFilename());
    }

    // Metodo para implementar el comando "cd" para cambiar directorios.
    public static void executeCD(TLine tline) {
        // Obtener el comando "cd" y sus argumentos.
        TCommand command = tline.getCommands().get(0);
        List<String> argv = command.getArgv();

        String target;

        // Si el usuario no da argumentos, se va al directorio HOME.
        if (argv.size() < 2) {
            target = System.getProperty("user.home");
        } else {
            // Si hay argumentos, se toma como destino.
            target = argv.get(1);
        }

        // Si el argumento empieza con "~", se sustituye por el directorio HOME.
        if (target.startsWith("~")) {
            target = System.getProperty("user.home") + target.substring(1);
        }

        // Creamos un objeto File con la ruta objetivo.
        File newDir = new File(target);

        // Si la ruta no es absoluta, se convierte a absoluta relativa al directorio actual.
        if (!newDir.isAbsolute()) {
            newDir = new File(System.getProperty("user.dir"), target);
        }

        // Se verifica si el directorio existe y es válido.
        if (newDir.exists() && newDir.isDirectory()) {
            // Se cambia la propiedad del directorio actual.
            System.setProperty("user.dir", newDir.getAbsolutePath());
        } else {
            // Si no existe, se muestra un mensaje de error.
            System.err.println("cd: directorio no encontrado: " + target);
        }
    }

    // Metodo para ejecutar comandos simples sin pipes ni redirecciones.
    public static void executeSimple(TLine tline) {
        // Obtener el comando (nombre + argumentos)
        TCommand command = tline.getCommands().getFirst();
        List<String> argv = command.getArgv();

        // Identificar el sistema operativo para realizar una lógica distinta en caso de ser Windows.
        String os = System.getProperty("os.name").toLowerCase();

        // Se declara un ProcessBuilder
        ProcessBuilder pb;

        if (os.contains("win")) {
            // En caso del sistema operativo ser windows se añade al argumento los prefijos cmd.exe y /c para que se puedan ejecutar comandos directamente.
            pb = new ProcessBuilder("cmd.exe", "/c", String.join(" ", argv));
        } else {
            // Si no es Windows se crea normalmente.
            pb = new ProcessBuilder(argv);
        }

        // Establecer el directorio actual.
        pb.directory(new File(System.getProperty("user.dir")));

        // Heredamos la entrada/salida del proceso actual (para verlo en consola).
        pb.inheritIO();

        try {
            // Iniciar el proceso.
            Process p = pb.start();

            // Esperar a que termine y obtener el código de salida.
            int code = p.waitFor();
        } catch (IOException e) {
            // Si el comando no existe o falla.
            System.err.println("Fallo al ejecutar: " + argv.getFirst() + " - " + e.getMessage());
        } catch (InterruptedException e) {
            // Si el proceso es interrumpido.
            Thread.currentThread().interrupt();
            System.err.println("Ejecución interrumpida");
        }
    }
}
