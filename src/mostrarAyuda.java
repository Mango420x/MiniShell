public class mostrarAyuda {

    public static void help(){

        System.out.println("""
                Comandos disponibles en MiniShell:\n
                ls - Listar archivos y directorios en el directorio actual.
                cd - Cambiar el directorio actual.
                pwd - Mostrar el directorio actual.
                mkdir - Crear un nuevo directorio.
                rm - Eliminar un archivo o directorio.
                mv - Mover o renombrar archivos y directorios.
                cp - Copiar archivos.
                exit - Salir del MiniShell.
                help - Mostrar esta ayuda.
                
                """);

    }

}
