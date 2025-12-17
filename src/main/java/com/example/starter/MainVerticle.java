package com.example.starter;

import io.vertx.core.Future;
import io.vertx.core.VerticleBase;
import io.vertx.core.Vertx;

public class MainVerticle extends VerticleBase
{
    String isNull;

    @Override
    public Future<?> start()
    {
        vertx.eventBus().consumer("echo").handler(msg -> {
            msg.reply(msg.body());
        });

        vertx.exceptionHandler(t -> {
            System.out.println("vertx.exceptionHandler");
            t.printStackTrace();
        });

        Vertx.currentContext().exceptionHandler(t -> {
            System.out.println("context.exceptionHandler");
            t.printStackTrace();
        });

        // Future without context
        Future.future(promise ->
            // just some async call
            vertx.eventBus().request("echo", "hi!").onComplete(promise)
        )
        .onComplete(ignore -> {
            System.out.println("Triggering exception, which will be caught by the context exceptionhandler");
            System.out.println(isNull.length());
        });

        Future.future(promise -> vertx.eventBus().request("echo", "hi!").onComplete(promise))
        // two onCompletes changes FutureImpl.listener to a FutureImpl.ListenerArray, which triggers the bug
        // could also add a timeout instead of 2 onCompletes, same behavior
        .onComplete(ignore -> {
            System.out.println("Triggering second exception, not caught by any exceptionhandler");
            System.out.println(isNull.length());
        })
        .onComplete(ignore -> {

        });

        return Future.succeededFuture();
    }
}
