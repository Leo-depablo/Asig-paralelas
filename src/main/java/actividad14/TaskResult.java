package actividad14;

import com.google.gson.Gson;

public final class TaskResult {
    public final int resultado;
    public TaskResult(int resultado) { this.resultado = resultado; }
    public String toJson() {
        return new Gson().toJson(this);
    }
}