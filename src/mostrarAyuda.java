public class mostrarAyuda {

    public static void help(){

        System.out.println("""
                Comandos disponibles en MiniShell:\n
                ls - Listar archivos y directorios en el directorio actual.\n
                cd - Cambiar el directorio actual.\n
                pwd - Mostrar el directorio actual.\n
                mkdir - Crear un nuevo directorio.\n
                rm - Eliminar un archivo o directorio.\n
                mv - Mover o renombrar archivos y directorios.\n
                cp - Copiar archivos.\n
                exit - Salir del MiniShell.\n
                help - Mostrar esta ayuda.\n
                
                """);

    }

}
