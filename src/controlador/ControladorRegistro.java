package controlador;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import modelo.Aportador;
import modelo.RegistroIngresos;

public class ControladorRegistro {
    // El grupo 1 conserva el valor original, incluso si el campo es incorrecto.
    private static final Pattern CORREO_ETIQUETADO = Pattern.compile(
            "(?iu)\\b(?:correo|e-?mail)\\s*[:=]\\s*([^\\s;|<>]+)");
    private static final Pattern CORREO_SUELTO = Pattern.compile(
            "([^\\s;|<>,()\\[\\]\"]*@[^\\s;|<>,()\\[\\]\"]*)");

    // Los puntos separan segmentos de usuario y etiquetas del dominio.
    private static final String USUARIO = "[A-Za-z0-9_%+\\-]+";
    private static final String DOMINIO = "[A-Za-z0-9](?:[A-Za-z0-9\\-]*[A-Za-z0-9])?";
    private static final Pattern CORREO_VALIDO = Pattern.compile(
            USUARIO + "(?:\\." + USUARIO + ")*@" + DOMINIO
            + "(?:\\." + DOMINIO + ")*\\.[A-Za-z]{2,}");

    private static final Pattern CEDULA_ETIQUETADA = Pattern.compile(
            "(?iu)\\bc[eé]dula\\s*[:=]\\s*([^\\s;|]+)");

    // Los límites impiden sacar 10 dígitos de un bloque más largo o de un decimal.
    private static final String LIMITE_IZQUIERDO = "(?<![\\p{L}\\p{N}+\\-])(?<![0-9][.,])";
    private static final String LIMITE_DERECHO = "(?![\\p{L}\\p{N}]|[.,][0-9])";
    private static final Pattern CEDULA_EXACTA = Pattern.compile(
            LIMITE_IZQUIERDO + "([0-9]{10})" + LIMITE_DERECHO);
    private static final Pattern BLOQUE_NUMERICO = Pattern.compile(
            LIMITE_IZQUIERDO + "([0-9]+)" + LIMITE_DERECHO);
    private static final Pattern CEDULA_VALIDA = Pattern.compile("[0-9]{10}");

    // Los separadores . y , se interpretan como decimales. El signo se conserva.
    private static final String NUMERO = "[+-]?(?:[0-9]+(?:[.,][0-9]+)?|[.,][0-9]+)";
    private static final String MONEDA = "(?:USD\\b|d[oó]lares?\\b|\\$)";
    private static final Pattern MONTO_ETIQUETADO = Pattern.compile(
            "(?iu)\\b(?:monto|importe)\\s*[:=]\\s*([+-]?\\s*\\$\\s*[^\\s;|]+|[^\\s;|]+)");
    private static final Pattern MONTO_CON_PREFIJO = Pattern.compile(
            "([+-]?\\s*\\$\\s*" + NUMERO + LIMITE_DERECHO + ")");
    private static final Pattern MONTO_CON_SUFIJO = Pattern.compile(
            "(?iu)(" + LIMITE_IZQUIERDO + NUMERO + "\\s*" + MONEDA + ")");
    private static final Pattern MONTO_SUELTO = Pattern.compile(
            LIMITE_IZQUIERDO + "(" + NUMERO + ")" + LIMITE_DERECHO);
    private static final Pattern VALOR_MONTO = Pattern.compile(
            "(?iu)(?:([+-]?)\\s*\\$\\s*(" + NUMERO + ")|(" + NUMERO + ")\\s*" + MONEDA + "?)");

    private final RegistroIngresos modelo;

    public ControladorRegistro(RegistroIngresos modelo) {
        this.modelo = modelo;
    }

    public int leerArchivo(File archivo) throws IOException {
        ArrayList<Aportador> leidos = new ArrayList<>();
        try (BufferedReader lector = Files.newBufferedReader(archivo.toPath(), StandardCharsets.UTF_8)) {
            String linea;
            while ((linea = lector.readLine()) != null) {
                linea = linea.replaceFirst("^\\uFEFF", "").trim();
                if (!linea.isEmpty() && !linea.startsWith("#")) {
                    leidos.add(procesarLinea(linea));
                }
            }
        }
        // Se cambia la colección solamente cuando termina la lectura.
        modelo.reemplazar(leidos);
        return leidos.size();
    }

    private Aportador procesarLinea(String linea) {
        Matcher correo = buscar(linea, CORREO_ETIQUETADO, CORREO_SUELTO);
        String sinCorreo = quitarCoincidencia(linea, correo);
        Matcher monto = buscarMonto(sinCorreo);
        String sinMonto = quitarCoincidencia(sinCorreo, monto);
        Matcher cedula = buscar(sinMonto, CEDULA_ETIQUETADA, CEDULA_EXACTA, BLOQUE_NUMERICO);

        String textoCedula = valor(cedula);
        String textoCorreo = valor(correo);
        if (monto == null && CEDULA_VALIDA.matcher(textoCedula).matches()) {
            // Un monto sin etiqueta ni moneda se toma solo si queda un número aislado.
            Matcher restante = MONTO_SUELTO.matcher(quitarCoincidencia(sinMonto, cedula));
            if (restante.find()) {
                String candidato = valor(restante);
                if (!restante.find()) {
                    return crearAportador(textoCedula, textoCorreo, candidato);
                }
            }
        }
        return crearAportador(textoCedula, textoCorreo, valor(monto));
    }

    private Matcher buscarMonto(String linea) {
        Matcher etiquetado = buscar(linea, MONTO_ETIQUETADO);
        if (etiquetado != null) {
            return etiquetado;
        }
        Matcher prefijo = buscar(linea, MONTO_CON_PREFIJO);
        Matcher sufijo = buscar(linea, MONTO_CON_SUFIJO);
        if (prefijo == null || sufijo == null) {
            return prefijo == null ? sufijo : prefijo;
        }
        // Cuando el mismo $ aparece entre dos números, se conserva la
        // interpretación que deja la cédula de 10 dígitos disponible.
        boolean cedulaConPrefijo = CEDULA_EXACTA.matcher(quitarCoincidencia(linea, prefijo)).find();
        boolean cedulaConSufijo = CEDULA_EXACTA.matcher(quitarCoincidencia(linea, sufijo)).find();
        return cedulaConSufijo && !cedulaConPrefijo ? sufijo : prefijo;
    }

    private Aportador crearAportador(String cedula, String correo, String montoOriginal) {
        double monto = convertirMonto(montoOriginal);
        Aportador aportador = new Aportador(cedula, correo, monto);
        aportador.setMontoOriginal(montoOriginal);
        aportador.setCedulaValida(CEDULA_VALIDA.matcher(cedula).matches());
        aportador.setCorreoValido(CORREO_VALIDO.matcher(correo).matches());
        aportador.setMontoValido(Double.isFinite(monto) && monto > 0);
        return aportador;
    }

    private double convertirMonto(String original) {
        Matcher coincidencia = VALOR_MONTO.matcher(original);
        if (!coincidencia.matches()) {
            return Double.NaN;
        }
        String numero = coincidencia.group(3);
        if (numero == null) {
            numero = coincidencia.group(1) + coincidencia.group(2);
        }
        try {
            return Double.parseDouble(numero.replace(',', '.'));
        } catch (NumberFormatException e) {
            return Double.NaN;
        }
    }

    private Matcher buscar(String linea, Pattern... patrones) {
        for (Pattern patron : patrones) {
            Matcher coincidencia = patron.matcher(linea);
            if (coincidencia.find()) {
                return coincidencia;
            }
        }
        return null;
    }

    private String valor(Matcher coincidencia) {
        if (coincidencia == null) {
            return "";
        }
        return coincidencia.group(1).trim().replaceFirst("[.,:]+$", "");
    }

    private String quitarCoincidencia(String linea, Matcher coincidencia) {
        if (coincidencia == null) {
            return linea;
        }
        return linea.substring(0, coincidencia.start()) + " "
                + linea.substring(coincidencia.end());
    }

    public List<Aportador> getAportadores() {
        return modelo.getAportadores();
    }

    public BigDecimal calcularTotalValido() {
        BigDecimal total = BigDecimal.ZERO;
        for (Aportador aportador : modelo.getAportadores()) {
            if (aportador.esValido()) {
                total = total.add(BigDecimal.valueOf(aportador.getMonto()));
            }
        }
        return total;
    }

    public int contarRegistrosValidos() {
        int total = 0;
        for (Aportador aportador : modelo.getAportadores()) {
            if (aportador.esValido()) {
                total++;
            }
        }
        return total;
    }

    // Cada posición cuenta errores de un campo: cédula, correo y monto.
    public int[] contarErrores() {
        int[] errores = new int[3];
        for (Aportador aportador : modelo.getAportadores()) {
            if (!aportador.isCedulaValida()) {
                errores[0]++;
            }
            if (!aportador.isCorreoValido()) {
                errores[1]++;
            }
            if (!aportador.isMontoValido()) {
                errores[2]++;
            }
        }
        return errores;
    }
}
