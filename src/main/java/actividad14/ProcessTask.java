package actividad14;

import com.google.gson.Gson;

public final class ProcessTask {
    public final int numero;
    public ProcessTask(int numero) { this.numero = numero; }
    public static ProcessTask fromJson(String json) {
        return new Gson().fromJson(json, ProcessTask.class);
    }
}