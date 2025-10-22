package shell;

// Imports del paquete tokenizer, java utils y excepciones.
import exceptions.MissingFileException;
import tokenizer.TCommand;
import tokenizer.TLine;
import tokenizer.Tokenizer;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MiniShell {

    public static void  main (String [] args) {
        // Creamos un Scanner para leer entradas de usuario en consola.
        Scanner scanner = new Scanner(System.in);

        while (true) {
            // Obtener el directorio actual y el usuario del sistema (para imprimirlo en cada prompt).
            String cwd = System.getProperty("user.dir");
            String user = System.getProperty("user.name");
            
            // Mostrar el directorio actual
            System.out.print(user + "@:" + cwd + "$ > ");

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
                    pipes(tline);
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

        try {
            // Resuelve la ruta completa y la limpia.
            newDir = newDir.getCanonicalFile();
        // Se verifica si el directorio existe y es válido.
        if (newDir.exists() && newDir.isDirectory()) {
            // Se cambia la propiedad del directorio actual.
            System.setProperty("user.dir", newDir.getAbsolutePath());
        } else {
            // Si no existe, se muestra un mensaje de error.
            System.err.println("cd: directorio no encontrado: " + target);
        }
        } catch (IOException e) {
            System.err.println("cd: error al acceder al directorio: " + e.getMessage());
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

    // Metodo para ejecutar comandos con pipes |.
    public static void pipes(TLine tline)   {
        // Obtiene la lista de comandos individuales (TCommand) a partir de la línea completa.
        List<TCommand> commands = tline.getCommands();
        // Identifica el sistema operativo.
        String ops = System.getProperty("os.name").toLowerCase();
        // Almacena los procesos creados para cada comando en el orden de ejecución.
        List<Process> processes = new ArrayList<>();
        
        try {
            // Mantiene una referencia al proceso anterior.
            Process previous = null;
           
           // Recorre cada comando en la lista de comandos.
            for (TCommand current : commands) {

                // Comando actual.
                // Argumentos del comando actual.
                List<String> argv = current.getArgv();
                // Crea un ProcessBuilder según su SO.
                ProcessBuilder pb;

                if (ops.contains("win")) {
                    // En Windows: usar cmd.exe /c para comandos internos
                    pb = new ProcessBuilder("cmd.exe", "/c", String.join(" ", argv));
                } else {
                    // En Linux/macOS: ejecutar directamente
                    pb = new ProcessBuilder(argv);
                }
                // Establece el directorio de trabajo del proceso.
                pb.directory(new File(System.getProperty("user.dir")));

                // Se inicia el proceso actual.
                Process currentProcess = pb.start();

                // En caso de que no sea el primer comando, se conecta la entrada del proceso actual a la salida del proceso anterior.
                if (previous != null) {
                    // Conecta la salida del proceso anterior a la entrada del proceso actual.
                    try (
                            InputStream is = previous.getInputStream();
                            OutputStream os = currentProcess.getOutputStream()
                    ) {
                        // Buffer temporal para transferir los datos entre procesos.
                        byte[] buffer = new byte[1024];
                        int length;
                        // Lee datos del proceso anterior y los escribe en el siguiente.
                        while ((length = is.read(buffer)) != -1) {
                            os.write(buffer, 0, length);
                        }
                        // Asegura que todos los datos del buffer se envían.
                        os.flush();
                    }
                }
                // Añade el proceso actual a la lista de procesos.
                processes.add(currentProcess);
                previous = currentProcess;

            }
              // Espera al último proceso a completar.
              Process last = processes.get(processes.size() - 1);
              last.waitFor();

              // Captura y muestra la salida del último proceso.  
              try (BufferedReader br = new BufferedReader(new InputStreamReader(last.getInputStream()))) {
                String line;
                
                while((line = br.readLine()) != null) {
        
                    System.out.println(line);
        
                }
              }
              // Espera a que todos los procesos del pipe terminen correctamente.     
              for (Process p : processes) {
                p.waitFor();
              }

        } catch (InterruptedException | IOException e) {
            System.err.println("Error en la ejecución del comando: " + e.getMessage());;

        }

    }
     public static void executesimplewithredandbackground(TLine tLine) {
        TCommand command = tLine.getCommands().getFirst();
        List<String> argv = command.getArgv();

        //Si no hay redirecciones ni se ejecuta en backgroun se manda al executeSimple, para no tener que cambiar el main mucho.
        if (tLine.getRedirectError() == null && tLine.getRedirectOutput() == null && !tLine.isBackground()) {
            executeSimple(tLine);
            return;
        }
        //Igual que en el executesimple , se gestiona el sistema operativo para poder probarlo en casa con windows
        // Identificar el sistema operativo para realizar una lógica distinta en caso de ser Windows, para poder probarlo en casa.
        String os = System.getProperty("os.name").toLowerCase();

        // Se declara un ProcessBuilder
        ProcessBuilder pb;
            System.err.println("Error en la ejecución del comando: " + e.getMessage());;

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


        //Antes de nada , se maneja las redirecciones de entrada , en este caso <
        if (tLine.getRedirectInput() != null) {
            //Se crea un objeto file para lo que leera de entrada
            File inputfile = new File(tLine.getRedirectInput());
            pb.redirectInput(inputfile);
        }
        //Se manejan las redirecciones de salida > y >>
        if (tLine.getRedirectOutput() != null) {
            File outputfile = new File(tLine.getRedirectOutput());
            //Se maneja si es un append o sobrescribir
            if (tLine.isAppendOutput()) {
                // Se hace >> (Usando el .Redirect.appendto)
                pb.redirectOutput(ProcessBuilder.Redirect.appendTo(outputfile));
            } else {
                //Se sobrescribe > (Usando el .redirect.to)
                pb.redirectOutput(ProcessBuilder.Redirect.to(outputfile));
            }
        }
        if (tLine.getRedirectError() != null) {
            File errorfile = new File(tLine.getRedirectError());
            pb.redirectError(errorfile);
        }

        //Se inicia el proceso
        try {
            Process p1 = pb.start();
        //Se controla si esta en background, si es true que el comando esta en background no se ejecuta un waitfor para esperarle.
            if (tLine.isBackground()) {
                //Se muestra el pid del comando si esta en background
                System.out.println("Proceso en background iniciado " + p1.pid());
            } else {
                //Se espera hasta que acabe el proceso para poder usar el shell
                int n = p1.waitFor();
                if (n != 0) {
                    System.out.println("Proceso finalizado : " + n);
                }
            }
        } catch (IOException e) {
            System.err.println("Fallo al ejecutar: " + argv.getFirst() + " - " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Ejecución interrumpida");
        }
    }
}
}
