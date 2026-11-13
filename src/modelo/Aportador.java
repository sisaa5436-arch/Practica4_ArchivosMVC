package modelo;

import java.math.BigDecimal;

public class Aportador {
    private String cedula;
    private String correo;
    private double monto;
    private boolean cedulaValida;
    private boolean correoValido;
    private boolean montoValido;
    private String montoOriginal = "";

    public Aportador() {
        this("", "", Double.NaN);
    }

    public Aportador(String cedula, String correo, double monto) {
        this.cedula = cedula;
        this.correo = correo;
        this.monto = monto;
    }

    public String getCedula() { return cedula; }
    public void setCedula(String cedula) { this.cedula = cedula; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public double getMonto() { return monto; }
    public void setMonto(double monto) { this.monto = monto; }

    public boolean isCedulaValida() { return cedulaValida; }
    public void setCedulaValida(boolean cedulaValida) { this.cedulaValida = cedulaValida; }

    public boolean isCorreoValido() { return correoValido; }
    public void setCorreoValido(boolean correoValido) { this.correoValido = correoValido; }

    public boolean isMontoValido() { return montoValido; }
    public void setMontoValido(boolean montoValido) { this.montoValido = montoValido; }

    public String getMontoOriginal() { return montoOriginal; }
    public void setMontoOriginal(String montoOriginal) { this.montoOriginal = montoOriginal; }

    public boolean esValido() {
        return cedulaValida && correoValido && montoValido;
    }

    @Override
    public String toString() {
        String importe = montoOriginal;
        if (Double.isFinite(monto)) {
            BigDecimal valor = BigDecimal.valueOf(monto).stripTrailingZeros();
            importe = "$" + valor.setScale(Math.max(2, valor.scale())).toPlainString();
        }
        return String.format("Cédula: %s %s | Correo: %s %s | Monto: %s %s",
                mostrar(cedula), estado(cedulaValida),
                mostrar(correo), estado(correoValido),
                mostrar(importe), estado(montoValido));
    }

    private String mostrar(String texto) {
        return texto == null || texto.isEmpty() ? "(ausente)" : texto;
    }

    private String estado(boolean valido) {
        return valido ? "[VÁLIDO]" : "[INCORRECTO]";
    }
}
