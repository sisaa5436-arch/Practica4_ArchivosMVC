package vista;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;
import modelo.Aportador;

public class VistaConsola {
    private final Scanner entrada;
    private final PrintStream salida;

    public VistaConsola(Scanner entrada, PrintStream salida) {
        this.entrada = entrada;
        this.salida = salida;
    }

    public String leerOpcion() {
        salida.println("\n=== Registro de ingresos ===");
        salida.println("1. Leer y procesar archivo de ingresos");
        salida.println("2. Mostrar listado detallado");
        salida.println("3. Mostrar resumen final");
        salida.println("4. Salir");
        salida.print("Opción: ");
        return entrada.hasNextLine() ? entrada.nextLine().trim() : "4";
    }

    public void mostrarListado(List<Aportador> aportadores) {
        salida.println("\n=== Listado de aportadores ===");
        for (int i = 0; i < aportadores.size(); i++) {
            salida.println((i + 1) + ". " + aportadores.get(i));
        }
    }

    public void mostrarResumen(BigDecimal total, int registros, int validos, int[] errores) {
        BigDecimal importe = total.stripTrailingZeros();
        importe = importe.setScale(Math.max(2, importe.scale()));
        salida.println("\n=== Resumen final ===");
        salida.println("Total válido recaudado: $" + importe.toPlainString());
        salida.println("Registros válidos: " + validos + " de " + registros);
        salida.println("Cédulas incorrectas: " + errores[0]);
        salida.println("Correos incorrectos: " + errores[1]);
        salida.println("Montos incorrectos: " + errores[2]);
    }

    public void mostrarMensaje(String mensaje) {
        salida.println(mensaje);
    }
}
