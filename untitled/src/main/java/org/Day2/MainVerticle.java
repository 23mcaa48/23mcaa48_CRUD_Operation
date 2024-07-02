package org.Day2;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class MainVerticle extends AbstractVerticle {

    private static final String DB_NAME = "inventory";
    private static final String COLLECTION_NAME = "items";

    private MongoClient mongo;

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new MainVerticle());
    }

    @Override
    public void start(Promise<Void> startPromise) {
        JsonObject config = new JsonObject()
                .put("connection_string", "mongodb://localhost:27017")
                .put("db_name", DB_NAME);

        mongo = MongoClient.createShared(vertx, config);

        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        // API routes
        InventoryController inventoryController = new InventoryController(mongo);
        router.post("/items").handler(inventoryController::addItem);
        router.get("/items").handler(inventoryController::getAllItems);
        router.get("/items/:itemId").handler(inventoryController::getItem);
        router.post("/items/:itemId").handler(inventoryController::updateItem);
        router.post("/items/:itemId/delete").handler(inventoryController::deleteItem);

        vertx.createHttpServer()
                .requestHandler(router)
                .listen(8080, ar -> {
                    if (ar.succeeded()) {
                        System.out.println("Server started on port 8080");
                        startPromise.complete();
                    } else {
                        System.out.println("Failed to start server");
                        startPromise.fail(ar.cause());
                    }
                });
    }

    @Override
    public void stop() {
        mongo.close();
    }
}
