package com.sczyh30.todolist.entity;

/**
 * Todo Entity
 */
public class Todo {

    private int id;
    private String title;
    private Boolean completed;
    private Integer order;
    private String url;

    public Todo() {}

    public Todo(String title) {
        this.title = title;
    }

    public Todo(String title, Integer order) {
        this.title = title;
        this.order = order;
    }

    public Todo(String title, Boolean completed, Integer order) {
        this.title = title;
        this.completed = completed;
        this.order = order;
    }

    public Todo(int id, String title, Boolean completed, Integer order, String url) {
        this.id = id;
        this.title = title;
        this.completed = completed;
        this.order = order;
        this.url = url;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Boolean isCompleted() {
        return getOrElse(completed, false);
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    public Integer getOrder() {
        return getOrElse(order, 0);
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    // use id as the primary key
    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Todo todo = (Todo) o;

        if (id != todo.id) return false;

        return true;
    }

    @Override
    public String toString() {
        return "Todo -> {" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", completed=" + completed +
                ", order=" + order +
                ", url='" + url + '\'' +
                '}';
    }

    private <T> T getOrElse(T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }

    public Todo merge(Todo todo) {
        return new Todo(id,
                getOrElse(todo.title, title),
                getOrElse(todo.completed, completed),
                getOrElse(todo.order, order),
                url);
    }


}
