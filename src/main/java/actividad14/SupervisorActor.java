package actividad14;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import java.util.concurrent.CompletableFuture;

public class SupervisorActor extends AbstractBehavior<Object> {

    private final CompletableFuture<Object> futureResult;
    private final ActorRef<Object> worker; // Agrega esta línea para guardar la referencia al worker

    public static Behavior<Object> create(CompletableFuture<Object> futureResult) {
        return Behaviors.setup(context -> new SupervisorActor(context, futureResult));
    }

    private SupervisorActor(ActorContext<Object> context, CompletableFuture<Object> futureResult) {
        super(context);
        this.futureResult = futureResult;
        getContext().getLog().info("SupervisorActor iniciado y listo.");

        Behavior<Object> workerBehavior =
            Behaviors.supervise(WorkerActor.create(getContext().getSelf()))
                     .onFailure(RuntimeException.class, SupervisorStrategy.restart());

        // Almacena la referencia del worker en la variable 'worker'
        this.worker = getContext().spawn(workerBehavior, "worker-1");
    }

    @Override
    public Receive<Object> createReceive() {
        return newReceiveBuilder()
            .onMessage(ProcessTask.class, this::onProcessTask)
            .onMessage(SimulateFailure.class, this::onSimulateFailure)
            .onMessage(TaskResult.class, this::onTaskResult)
            .build();
    }

    private Behavior<Object> onProcessTask(ProcessTask msg) {
        getContext().getLog().info("SupervisorActor: Delegando tarea a worker-1.");
        // Usa la variable 'worker' para enviar el mensaje
        worker.tell(msg);
        return this;
    }

    private Behavior<Object> onSimulateFailure(SimulateFailure msg) {
        getContext().getLog().warn("SupervisorActor: Ordenando a worker-1 que falle.");
        // Usa la variable 'worker' para enviar el mensaje
        worker.tell(msg);
        return this;
    }

    private Behavior<Object> onTaskResult(TaskResult msg) {
        getContext().getLog().info("SupervisorActor: Tarea finalizada, resultado recibido: {}. Completando Future.", msg.resultado);
        futureResult.complete(msg);
        return this;
    }
}