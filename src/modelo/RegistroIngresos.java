package modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RegistroIngresos {
    private final ArrayList<Aportador> aportadores = new ArrayList<>();

    public void reemplazar(List<Aportador> nuevos) {
        aportadores.clear();
        aportadores.addAll(nuevos);
    }

    public List<Aportador> getAportadores() {
        return Collections.unmodifiableList(aportadores);
    }
}
