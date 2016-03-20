# Extend vert.x image
FROM vertx/vertx3

ENV VERTICLE_NAME com.sczyh30.todolist.TodoVerticle
ENV VERTICLE_FILE build/libs/vert-todo-backend.jar

# Set the location of the verticles
ENV VERTICLE_HOME /usr/verticles

EXPOSE 8082

COPY $VERTICLE_FILE $VERTICLE_HOME/

# Launch the verticle
WORKDIR $VERTICLE_HOME
ENTRYPOINT ["sh", "-c"]
CMD ["vertx run $VERTICLE_NAME -cp $VERTICLE_HOME/*"]