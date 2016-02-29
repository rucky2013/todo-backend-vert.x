package com.sczyh30.todolist;

import com.sczyh30.todolist.entity.Todo;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.*;

/**
 * Todo App
 */
public class VertTodoApp extends AbstractVerticle {

    public static final String API_GET = "/todos/:todoId";
    public static final String API_LIST_ALL = "/todos";
    public static final String API_CREATE= "/todos";
    public static final String API_UPDATE = "/todos/:todoId";
    public static final String API_DELETE = "/todos/:todoId";
    public static final String API_DELETE_ALL = "/todos";

    public static void main(String[] args) {
        Runner.runExample(VertTodoApp.class);
    }

    private Map<Integer, Todo> todos = new LinkedHashMap<>();

    private void initData() {
        todos.put(1, new Todo(1, "Something to do...", false, 1));
        todos.put(2, new Todo(2, "Something to do...", false, 2));
    }

    @Override
    public void start() throws Exception {
        System.out.println("Todo service is running at 8080 port.");
        initData();

        Router router = Router.router(vertx);

        router.route().handler(BodyHandler.create());
        router.get(API_GET).handler(this::handleGetTodo);
        router.get(API_LIST_ALL).handler(this::handleGetAll);
        router.post(API_CREATE).handler(this::handleCreateTodo);
        router.patch(API_UPDATE).handler(this::handleUpdateTodo);
        router.delete(API_DELETE).handler(this::handleDeleteOne);
        router.delete(API_DELETE_ALL).handler(this::handleDeleteAll);

        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(8080);
    }

    private void handleCreateTodo(RoutingContext context) {
        final Todo todo = Json.decodeValue(context.getBodyAsString(),
                Todo.class);
        todos.put(todo.getId(), todo);
        context.response()
                .setStatusCode(201)
                .putHeader("content-type", "application/json; charset=utf-8")
                .end(Json.encodePrettily(todo));
    }

    private void handleGetTodo(RoutingContext context) {
        String todoID = context.request().getParam("todoId");
        HttpServerResponse response = context.response();
        if (todoID == null)
            sendError(400, response);
        else {
            Optional<Todo> maybeTodo = getTodoById(Integer.valueOf(todoID));
            if(!maybeTodo.isPresent())
                sendError(404, response);
            else {
                response.putHeader("content-type", "application/json; charset=utf-8")
                        .end(Json.encodePrettily(maybeTodo.get()));
            }

        }
    }

    private void handleGetAll(RoutingContext context) {
        context.response()
                .putHeader("content-type", "application/json; charset=utf-8")
                .end(Json.encodePrettily(todos.values()));
    }

    private void handleUpdateTodo(RoutingContext context) {
        int todoID = Integer.valueOf(context.request().getParam("todoId"));
        final Todo newTodo = Json.decodeValue(context.getBodyAsString(),
                Todo.class);
        HttpServerResponse response = context.response();
        Optional<Todo> maybeTodo = getTodoById(todoID);
        if(!maybeTodo.isPresent())
            sendError(404, response);
        else if(newTodo == null)
            sendError(400, response);

        todos.remove(todoID);
        Todo mergeTodo = maybeTodo.get().merge(newTodo);
        todos.put(todoID, mergeTodo);
        response.putHeader("content-type", "application/json; charset=utf-8")
                .end(Json.encodePrettily(mergeTodo));
    }

    private void handleDeleteOne(RoutingContext context) {
        String todoID = context.request().getParam("todoId");
        HttpServerResponse response = context.response();
        Optional<Todo> maybeTodo = getTodoById(Integer.valueOf(todoID));
        if(!maybeTodo.isPresent())
            sendError(404, response);
        else
            todos.remove(maybeTodo.get());
    }

    private void handleDeleteAll(RoutingContext context) {
        todos.clear();
    }

    private void sendError(int statusCode, HttpServerResponse response) {
        response.setStatusCode(statusCode).end();
    }

    private Optional<Todo> getTodoById(int id) {
        return Optional.ofNullable(todos.get(id));
    }
}
