package com.sczyh30.todolist;


import com.sczyh30.todolist.entity.Todo;
import static com.sczyh30.todolist.Constants.*;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.redis.RedisClient;
import io.vertx.redis.RedisOptions;

import java.util.*;

/**
 * Vert.x todo Verticle
 */
public class TodoVerticle extends AbstractVerticle {

    RedisClient redis;

    private void initData() {
        final RedisOptions config = new RedisOptions()
                .setHost("127.0.0.1");
        redis = RedisClient.create(vertx, config);

        redis.hset(REDIS_TODO_KEY, "1", Json.encodePrettily(
                new Todo(1, "Something to do...", false, 1)), res -> {
            if (res.failed()) {
                System.out.println("[Error]Redis service is not running!");
                //res.cause().printStackTrace();
            }
        });

        redis.hset(REDIS_TODO_KEY, "2", Json.encodePrettily(
                new Todo(2, "Another thing to do...", false, 2)), res -> {
        });

    }

    private Todo getTodoFromJson(String jsonStr) {
        return Json.decodeValue(jsonStr, Todo.class);
    }

    @Override
    public void start() throws Exception {
        initData();

        Router router = Router.router(vertx);
        // CORS support
        Set<String> allowHeaders = new HashSet<>();
        allowHeaders.add("x-requested-with");
        allowHeaders.add("origin");
        allowHeaders.add("content-type");
        allowHeaders.add("accept");
        Set<HttpMethod> allowMethods = new HashSet<>();
        allowMethods.add(HttpMethod.GET);
        allowMethods.add(HttpMethod.POST);
        allowMethods.add(HttpMethod.DELETE);
        allowMethods.add(HttpMethod.PATCH);

        router.route().handler(CorsHandler.create("*").allowedHeaders(allowHeaders)
            .allowedMethods(allowMethods));

        // routes
        router.get(API_GET).handler(this::handleGetTodo);
        router.get(API_LIST_ALL).handler(this::handleGetAll);
        router.post(API_CREATE).handler(this::handleCreateTodo);
        router.patch(API_UPDATE).handler(this::handleUpdateTodo);
        router.delete(API_DELETE).handler(this::handleDeleteOne);
        router.delete(API_DELETE_ALL).handler(this::handleDeleteAll);

        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(8082);
    }

    private void handleCreateTodo(RoutingContext context) {
        final Todo todo = getTodoFromJson(context.getBodyAsString());
        redis.hset(REDIS_TODO_KEY, String.valueOf(todo.getId()),
                Json.encodePrettily(todo), res -> {
                    if (res.succeeded())
                        context.response()
                                .setStatusCode(201)
                                .putHeader("content-type", "application/json; charset=utf-8")
                                .end(Json.encodePrettily(todo));
                    else
                        sendError(503, context.response());
                });
    }

    private void handleGetTodo(RoutingContext context) {
        String todoID = context.request().getParam("todoId");
        if (todoID == null)
            sendError(400, context.response());
        else {
            redis.hget(REDIS_TODO_KEY, todoID, x -> {
                String result = x.result();
                if (result == null)
                    sendError(404, context.response());
                else {
                    context.response()
                            .putHeader("content-type", "application/json; charset=utf-8")
                            .end(result);
                }
            });
        }
    }

    private void handleGetAll(RoutingContext context) {
        redis.hgetall(REDIS_TODO_KEY, res -> {
            if (res.succeeded())
                context.response()
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end(res.result().encodePrettily());
            else
                sendError(404, context.response());
        });
    }

    private void handleUpdateTodo(RoutingContext context) {
        String todoID = context.request().getParam("todoId");
        final Todo newTodo = getTodoFromJson(context.getBodyAsString());
        // handle error
        if (newTodo == null) {
            sendError(400, context.response());
            return;
        }

        redis.hget(REDIS_TODO_KEY, todoID, x -> {
            String result = x.result();
            if (result == null)
                sendError(404, context.response());
            else {
                Todo oldTodo = getTodoFromJson(result);
                String response = Json.encodePrettily(oldTodo.merge(newTodo));
                redis.hset(REDIS_TODO_KEY, todoID, response, res -> {
                    if (res.succeeded()) {
                        context.response()
                                .putHeader("content-type", "application/json; charset=utf-8")
                                .end(Json.encodePrettily(response));
                    }
                });
            }
        });
    }

    private void handleDeleteOne(RoutingContext context) {
        String todoID = context.request().getParam("todoId");
        redis.hdel(REDIS_TODO_KEY, todoID, res -> {
            if (res.succeeded())
                context.response().setStatusCode(204).end();
            else
                sendError(404, context.response());
        });
    }

    private void handleDeleteAll(RoutingContext context) {
        redis.del(REDIS_TODO_KEY, res -> {
            if (res.succeeded())
                context.response().setStatusCode(204).end();
            else
                sendError(404, context.response());
        });
    }

    private void sendError(int statusCode, HttpServerResponse response) {
        response.setStatusCode(statusCode).end();
    }

}