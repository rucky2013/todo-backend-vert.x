# todo-backend-vert.x
[![Travis Build Status](https://travis-ci.org/sczyh30/todo-backend-vert.x.svg?branch=master)](https://travis-ci.org/sczyh30/todo-backend-vert.x)

A simple Todo-Backend implementation using Vert.x, with redis backend(Vert.x-Redis support).

## Build with Docker

Build command:
```bash
gradle build
docker build -t sczyh30/vert-todo-backend .
```

And then launch:

`docker run -p 8082:8082 -i -t sczyh30/vert-todo-backend`

Then the service will run on `http://localhost:8082`.