package app;

import controlador.ControladorRegistro;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import modelo.RegistroIngresos;
import vista.VistaConsola;

public class Main {
    public static void main(String[] args) {
        File archivo = new File(args.length == 0 ? "registro_ingresos.txt" : args[0]);
        RegistroIngresos modelo = new RegistroIngresos();
        ControladorRegistro controlador = new ControladorRegistro(modelo);

        try (Scanner entrada = new Scanner(System.in, "UTF-8")) {
            VistaConsola vista = new VistaConsola(entrada, System.out);
            ejecutarMenu(controlador, vista, archivo);
        }
    }

    private static void ejecutarMenu(ControladorRegistro controlador, VistaConsola vista, File archivo) {
        boolean archivoCargado = false;
        while (true) {
            switch (vista.leerOpcion()) {
                case "1":
                    try {
                        int leidos = controlador.leerArchivo(archivo);
                        archivoCargado = true;
                        vista.mostrarMensaje("Registros procesados: " + leidos);
                    } catch (IOException e) {
                        vista.mostrarMensaje("No se pudo leer " + archivo.getAbsolutePath() + ". " + e.getMessage());
                    }
                    break;
                case "2":
                    if (archivoCargado && !controlador.getAportadores().isEmpty()) {
                        vista.mostrarListado(controlador.getAportadores());
                    } else {
                        vista.mostrarMensaje("No hay registros disponibles. Carga el archivo con la opción 1.");
                    }
                    break;
                case "3":
                    if (archivoCargado) {
                        vista.mostrarResumen(controlador.calcularTotalValido(),
                                controlador.getAportadores().size(), controlador.contarRegistrosValidos(),
                                controlador.contarErrores());
                    } else {
                        vista.mostrarMensaje("Primero carga el archivo con la opción 1.");
                    }
                    break;
                case "4":
                    vista.mostrarMensaje("Programa finalizado.");
                    return;
                default:
                    vista.mostrarMensaje("Opción incorrecta. Elige un número del 1 al 4.");
            }
        }
    }
}
