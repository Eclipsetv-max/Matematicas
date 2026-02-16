# Sistema de Exámenes Matemáticos con detector de plagio IA

Aplicación en Java (Swing) que permite:

- Resolver exámenes con nombre del estudiante.
- Corregir respuestas automáticamente.
- Estimar riesgo de plagio asociado a texto generado por IA.
- Validar respuestas con un endpoint externo de IA (opcional).
- Guardar puntajes y respuestas en un archivo local.
- Consultar historial completo con detalle por pregunta.

## Ejecución

```bash
mvn package
java -jar target/sistema-examenes-ia-1.0.0.jar
```

## Configuración opcional

Crear `app.properties` en la raíz:

```properties
data.dir=data
exam.questionLimit=10
ai.endpoint=
ai.apiKey=
```

Si `ai.endpoint` queda vacío, la validación externa se desactiva automáticamente.
