package com.sczyh30.todolist;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

/**
 * Todo App
 */
public class VertTodoApp extends AbstractVerticle {

    public static final String API_GET = "/todos/:todoId";
    public static final String API_LIST_ALL = "/todos";
    public static final String API_CREATE= "";
    public static final String API_UPDATE = "";
    public static final String API_DELETE = "";
    public static final String API_DELETE_ALL = "";

    public static void main(String[] args) {

    }

    @Override
    public void start() throws Exception {

        Router router = Router.router(vertx);

        router.route().handler(BodyHandler.create());
        router.get(API_GET).handler(this::handleGetTodo);
        router.get(API_LIST_ALL).handler(this::handleGetAll);
        router.get(API_CREATE).handler(this::handleCreateTodo);
        router.get(API_UPDATE).handler(this::handleUpdateTodo);
        router.get(API_DELETE).handler(this::handleDeleteOne);
        router.get(API_DELETE_ALL).handler(this::handleDeleteAll);

        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(8080);
    }

    private void handleCreateTodo(RoutingContext context) {

    }

    private void handleGetTodo(RoutingContext context) {
        String todoID = context.request().getParam("todoId");
        HttpServerResponse response = context.response();
    }

    private void handleGetAll(RoutingContext context) {

    }

    private void handleUpdateTodo(RoutingContext context) {

    }

    private void handleDeleteOne(RoutingContext context) {

    }

    private void handleDeleteAll(RoutingContext context) {

    }

    private void sendError(int statusCode, HttpServerResponse response) {
        response.setStatusCode(statusCode).end();
    }
}
