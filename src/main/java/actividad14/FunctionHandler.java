package actividad14;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;
import com.google.gson.Gson;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.Behaviors;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class FunctionHandler {

    private static final ActorSystem<Object> actorSystem =
            ActorSystem.create(Behaviors.empty(), "SupervisorSystem");
    private final Gson gson = new Gson();

    @FunctionName("AsigParalelasMicroservicio")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.POST}, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("Petición HTTP recibida.");
        String body = request.getBody().orElse("");

        try {
            CompletableFuture<Object> futureResult = new CompletableFuture<>();
            ActorSystem<Object> tempActorSystem =
                    ActorSystem.create(SupervisorActor.create(futureResult), "TempSystem");

            if (body.isEmpty()) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("El cuerpo de la petición no puede estar vacío.").build();
            }

            if (body.contains("\"simularFallo\":true")) {
                tempActorSystem.tell(new SimulateFailure());
            } else {
                ProcessTask task = gson.fromJson(body, ProcessTask.class);
                tempActorSystem.tell(task);
            }

            TaskResult result = (TaskResult) futureResult.get(Duration.ofSeconds(10).toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS);

            return request.createResponseBuilder(HttpStatus.OK).header("Content-Type", "application/json").body(result.toJson()).build();
        } catch (Exception e) {
            context.getLogger().severe("Error en la ejecución: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"error\":\"" + e.getMessage() + "\"}").build();
        } finally {
            actorSystem.terminate();
        }
    }
}