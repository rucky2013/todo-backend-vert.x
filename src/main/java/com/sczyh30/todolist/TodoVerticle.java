package com.sczyh30.todolist;


import com.sczyh30.todolist.entity.Todo;
import static com.sczyh30.todolist.Constants.*;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.redis.RedisClient;
import io.vertx.redis.RedisOptions;

import java.util.*;

/**
 * Vert.x todo Verticle
 */
public class TodoVerticle extends AbstractVerticle {

    static String HOST = "127.0.0.1";
    static int PORT = 8082;

    RedisClient redis;

    /**
     * Init the redis client and save sample data
     */
    private void initData() {
        RedisOptions config;
        if (System.getenv("OPENSHIFT_VERTX_IP") != null)
            config = new RedisOptions()
                .setHost("127.1.250.130").setPort(16379);
        else
            config = new RedisOptions()
                    .setHost(HOST);

        redis = RedisClient.create(vertx, config);

        redis.hset(REDIS_TODO_KEY, "98", Json.encode(
                new Todo(98, "Something to do...", false, 1, "todo/1")), res -> {
            if (res.failed()) {
                System.err.println("[Error]Redis service is not running!");
                res.cause().printStackTrace();
            }
        });

        redis.hset(REDIS_TODO_KEY, "2", Json.encode(
                new Todo(2, "Another thing to do...", false, 2, "todo/2")), res -> {
        });

    }

    @Override
    public void start(Future<Void> future) throws Exception {
        initData();

        Router router = Router.router(vertx);
        // CORS support
        Set<String> allowHeaders = new HashSet<>();
        allowHeaders.add("x-requested-with");
        allowHeaders.add("Access-Control-Allow-Origin");
        allowHeaders.add("origin");
        allowHeaders.add("Content-Type");
        allowHeaders.add("accept");
        Set<HttpMethod> allowMethods = new HashSet<>();
        allowMethods.add(HttpMethod.GET);
        allowMethods.add(HttpMethod.POST);
        allowMethods.add(HttpMethod.DELETE);
        allowMethods.add(HttpMethod.PATCH);

        router.route().handler(BodyHandler.create());
        router.route().handler(CorsHandler.create("*")
                .allowedHeaders(allowHeaders)
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
                .listen(config().getInteger("http.port", PORT),
                        System.getProperty("http.address", HOST), result -> {
                    if (result.succeeded())
                        future.complete();
                    else
                        future.fail(result.cause());
                });
    }

    private void handleCreateTodo(RoutingContext context) {
        final Todo todo = wrapObject(getTodoFromJson
                (context.getBodyAsString()), context);
        final String encoded = Json.encode(todo);
        redis.hset(REDIS_TODO_KEY, String.valueOf(todo.getId()),
                encoded, res -> {
                    if (res.succeeded())
                        context.response()
                                .setStatusCode(201)
                                .putHeader("content-type", "application/json; charset=utf-8")
                                .end(encoded);
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
            List<Object> list = new ArrayList<>();
            if (res.succeeded()) {
                res.result().forEach(x ->
                        list.add(getTodoFromJson((String)x.getValue())));
                String encoded = new JsonArray(list).encode();
                context.response()
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .end(encoded);
            }
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
                String response = Json.encode(oldTodo.merge(newTodo));
                redis.hset(REDIS_TODO_KEY, todoID, response, res -> {
                    if (res.succeeded()) {
                        context.response()
                                .putHeader("content-type", "application/json; charset=utf-8")
                                .end(response);
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
                sendError(400, context.response());
        });
    }

    private void sendError(int statusCode, HttpServerResponse response) {
        response.setStatusCode(statusCode).end();
    }

    /**
     * Decode Todo entity from JSON str
     * @param jsonStr JSON str
     * @return Todo entity
     */
    private Todo getTodoFromJson(String jsonStr) {
        return Json.decodeValue(jsonStr, Todo.class);
    }

    /**
     * Wrap the Todo entity with appropriate id and url
     * @param todo a todo entity
     * @param context RoutingContext
     * @return the wrapped todo entity
     */
    private Todo wrapObject(Todo todo, RoutingContext context) {
        if(todo.getId() == 0)
            todo.setId(Math.abs(new Random().nextInt()));
        todo.setUrl(context.request().absoluteURI() +  "/" + todo.getId());
        return todo;
    }

}
