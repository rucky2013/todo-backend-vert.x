package com.sczyh30.todolist;

import io.vertx.core.Verticle;
import io.vertx.core.Vertx;

/**
 * The todo application
 */
public class Application {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        Verticle todoVerticle = new TodoVerticle();
        vertx.deployVerticle(todoVerticle);
    }
}
