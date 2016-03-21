package com.sczyh30.todolist;

import com.sczyh30.todolist.entity.Todo;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
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

    @Before
    public void before(TestContext context) {
        vertx = Vertx.vertx();
        vertx.deployVerticle(TodoVerticle.class.getName(), context.asyncAssertSuccess());
    }

    @After
    public void after(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void testAdd(TestContext context) throws Exception {
    }

    @Test
    public void testGet(TestContext context) throws Exception {
        Async async = context.async();
        HttpClient client = vertx.createHttpClient();
        client.getNow(port, "localhost", "/todos/98", response -> response.bodyHandler(body -> {
            Todo todo = getTodoFromJson(body.toString());
            context.assertEquals(todo, new Todo(
                    98, "Something to do...", false, 1, "todo/1"));
            client.close();
            async.complete();
        }));
    }

    @Test
    public void testUpdate(TestContext context) throws Exception {

    }

    @Test
    public void testDelete(TestContext context) throws Exception {
        /*Async async = context.async();
        HttpClient client = vertx.createHttpClient();
        client.delete(port, "localhost", "/todos/98", response -> {
            context.assertEquals(204, response.statusCode());
            client.close();
            async.complete();
        });*/
    }

    private Todo getTodoFromJson(String jsonStr) {
        return Json.decodeValue(jsonStr, Todo.class);
    }
}