# Práctica 4 · Archivos y MVC

Aplicación Java de consola que extrae cédulas, correos y montos de registros desordenados. Valida cada campo y suma únicamente los aportes cuyos tres campos son válidos.

## Desarrollar, compilar y ejecutar en Eclipse

1. Descomprime el ZIP. En Eclipse, selecciona **File → Import → General → Existing Projects into Workspace**. En **Select root directory**, elige la carpeta `Practica4_ArchivosMVC` y pulsa **Finish**.
2. Para desarrollar el proyecto desde cero, crea un **Java Project** con el mismo nombre. Dentro de `src`, crea los paquetes `modelo`, `controlador`, `vista` y `app`. Implementa las clases en el orden indicado en el plan de commits.
3. Coloca `registro_ingresos.txt` en la raíz del proyecto, junto a `src`. Guarda los archivos en UTF-8. El proyecto admite Java 8 o posterior.
4. Activa **Project → Build Automatically**. Si tienes desactivada la compilación automática, selecciona el proyecto y usa **Project → Build Project**.
5. Abre `src/app/Main.java` y selecciona **Run As → Java Application**. Escribe **1** para cargar los datos, **2** para ver el listado, **3** para ver el resumen y **4** para salir.
6. Puedes pasar una ruta alternativa en **Run Configurations → Arguments → Program arguments**. Si usas el nombre predeterminado, el directorio de trabajo debe ser la carpeta del proyecto.

Referencias de Eclipse: [Importar proyectos](https://help.eclipse.org/latest/topic/org.eclipse.platform.doc.user/reference/ref-70.htm) y [compilación de proyectos](https://help.eclipse.org/latest/topic/org.eclipse.platform.doc.user/concepts/concepts-22.htm).

## Responsabilidades MVC

| Paquete | Responsabilidad |
|---|---|
| `modelo` | `Aportador` guarda cada registro y sus banderas. `RegistroIngresos` mantiene un `ArrayList<Aportador>`. |
| `controlador` | `ControladorRegistro` lee el TXT con `File`, extrae los campos con `Pattern` y `Matcher`, aplica las validaciones y calcula el resumen. |
| `vista` | `VistaConsola` presenta el menú con `Scanner`, el detalle y el resumen. |
| `app` | `Main` conecta el modelo, la vista y el controlador. |

## Formato de entrada y prueba

Cada línea de datos corresponde a un aportador. El orden de los campos puede variar. Las líneas en blanco y las que comienzan con `#` se omiten. Puedes usar etiquetas como `cédula:`, `correo:` y `monto:`.

- **Cédula.** Se conserva como texto para mantener los ceros iniciales. La regla de esta práctica comprueba diez dígitos exactos. Cuando falta la etiqueta, se buscan bloques numéricos independientes fuera del correo y del monto.
- **Correo.** Se comprueba el formato habitual de usuario, arroba y dominio con extensión.
- **Monto.** Se reconoce `$25.50`, `25,50 USD`, `25 dólares`, `25 $` o `monto: 25`. Usa punto o coma decimal y escribe los millares sin separador, por ejemplo `1250.50 USD`. Solo se aceptan montos positivos y finitos.
- **Recarga.** Una lectura exitosa sustituye el listado. Si hay un error de lectura, se conservan los registros cargados previamente.

El TXT incluido usa datos ficticios. La opción **3** debe mostrar **$42.25**, **3 registros válidos de 7**, **2 cédulas incorrectas**, **2 correos incorrectos** y **2 montos incorrectos**.

## Plan de commits

Para reconstruir el historial en un repositorio vacío llamado `Practica4_ArchivosMVC`, ejecuta cada bloque al terminar su fase. El ZIP entregado ya incluye estos cinco commits. Puedes verlos con `git log --reverse --oneline`.

```bash
git init -b main
git add README.md .project .classpath .settings .gitignore
git commit -m "Documentar la práctica y configurar el proyecto Eclipse"

git add src/modelo
git commit -m "Implementar el modelo de aportadores y su colección"

git add src/controlador registro_ingresos.txt
git commit -m "Procesar ingresos y validar los campos con expresiones regulares"

git add src/vista
git commit -m "Implementar el menú y la presentación de resultados en consola"

git add src/app
git commit -m "Integrar las capas MVC en el punto de entrada"
```
