package com.sczyh30.todolist;

import com.sczyh30.todolist.entity.Todo;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Test case for Todo API
 * @author sczyh30
 */
@RunWith(VertxUnitRunner.class)
public class TodoApiTest {

    final static int port = 8082;

    Vertx vertx;

    Todo todoEx = new Todo(164, "Test case...", false, 22, "http://127.0.0.1:8082/todos/164");
    Todo todoUp = new Todo(164, "Test case...Update!", false, 26, "http://127.0.0.1:8082/todos/164");

    @Before
    public void before(TestContext context) {
        vertx = Vertx.vertx();
        vertx.deployVerticle(TodoVerticle.class.getName(), context.asyncAssertSuccess());
    }

    @Test(timeout = 3000L)
    public void testAdd(TestContext context) throws Exception {
        Async async = context.async();
        HttpClient client = vertx.createHttpClient();
        Todo todo = new Todo(164, "Test case...", false, 22, "/164");
        client.post(port, "localhost", "/todos", response -> {
            context.assertEquals(201, response.statusCode());
            client.close();
            async.complete();
        }).putHeader("content-type", "application/json").end(Json.encodePrettily(todo));
    }

    @Test(timeout = 3000L)
    public void testGet(TestContext context) throws Exception {
        Async async = context.async();
        HttpClient client = vertx.createHttpClient();
        client.getNow(port, "localhost", "/todos/164", response -> response.bodyHandler(body -> {
            context.assertEquals(getTodoFromJson(body.toString()), todoEx);
            client.close();
            async.complete();
        }));
    }

    @Test(timeout = 3000L)
    public void testUpdate(TestContext context) throws Exception {
        Async async = context.async();
        HttpClient client = vertx.createHttpClient();
        Todo todo = new Todo(164, "Test case...Update!", false, 26, "/164h");
        client.request(HttpMethod.PATCH, port, "localhost", "/todos/164", response -> response.bodyHandler(body -> {
            context.assertEquals(getTodoFromJson(body.toString()), todoUp);
            client.close();
            async.complete();
        })).putHeader("content-type", "application/json").end(Json.encodePrettily(todo));
    }

    @Test(timeout = 3000L)
    public void testDelete(TestContext context) throws Exception {
        /*Async async = context.async();
        HttpClient client = vertx.createHttpClient();
        client.delete(port, "localhost", "/todos/164", response -> {
            context.assertEquals(204, response.statusCode());
            client.close();
            async.complete();
        });*/
    }

    private Todo getTodoFromJson(String jsonStr) {
        return Json.decodeValue(jsonStr, Todo.class);
    }

    @After
    public void after(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }
}