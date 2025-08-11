package actividad14;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class WorkerActor extends AbstractBehavior<Object> {

    private final ActorRef<Object> replyTo;

    public static Behavior<Object> create(ActorRef<Object> replyTo) {
        return Behaviors.setup(context -> new WorkerActor(context, replyTo));
    }

    private WorkerActor(ActorContext<Object> context, ActorRef<Object> replyTo) {
        super(context);
        this.replyTo = replyTo;
        context.getLog().info("WorkerActor listo para procesar tareas.");
    }

    @Override
    public Receive<Object> createReceive() {
        return newReceiveBuilder()
            .onMessage(ProcessTask.class, this::onProcessTask)
            .onMessage(SimulateFailure.class, this::onSimulateFailure)
            .build();
    }

    private Behavior<Object> onProcessTask(ProcessTask msg) {
        getContext().getLog().info("WorkerActor: Procesando tarea (número: {}).", msg.numero);
        int resultado = msg.numero + 10;
        replyTo.tell(new TaskResult(resultado));
        return this;
    }

    private Behavior<Object> onSimulateFailure(SimulateFailure msg) {
        getContext().getLog().error("WorkerActor: ¡Error intencional! Simulando un fallo crítico.");
        throw new RuntimeException("El Worker ha fallado.");
    }
}