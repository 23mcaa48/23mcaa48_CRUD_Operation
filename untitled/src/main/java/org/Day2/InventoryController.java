package org.Day2;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.RoutingContext;

public class InventoryController {

    private final MongoClient mongo;

    public InventoryController(MongoClient mongo) {
        this.mongo = mongo;
    }

    public void addItem(RoutingContext routingContext) {
        JsonObject newItem = routingContext.getBodyAsJson();

        mongo.insert("items", newItem, res -> {
            if (res.succeeded()) {
                String itemId = res.result();
                routingContext.response()
                        .setStatusCode(201)
                        .putHeader("content-type", "application/json")
                        .end(Json.encodePrettily(new JsonObject().put("_id", itemId)));
            } else {
                routingContext.response()
                        .setStatusCode(500)
                        .end("Internal Server Error: " + res.cause().getMessage());
            }
        });
    }

    public void getAllItems(RoutingContext routingContext) {
        mongo.find("items", new JsonObject(), res -> {
            if (res.succeeded()) {
                routingContext.response()
                        .putHeader("content-type", "application/json")
                        .end(Json.encodePrettily(res.result()));
            } else {
                routingContext.response()
                        .setStatusCode(500)
                        .end("Internal Server Error: " + res.cause().getMessage());
            }
        });
    }

    public void getItem(RoutingContext routingContext) {
        String itemId = routingContext.request().getParam("itemId");
        JsonObject query = new JsonObject().put("_id", itemId);

        mongo.findOne("items", query, null, res -> {
            if (res.succeeded()) {
                JsonObject item = res.result();
                if (item == null) {
                    routingContext.response()
                            .setStatusCode(404)
                            .end("Item not found");
                } else {
                    routingContext.response()
                            .putHeader("content-type", "application/json")
                            .end(Json.encodePrettily(item));
                }
            } else {
                routingContext.response()
                        .setStatusCode(500)
                        .end("Internal Server Error: " + res.cause().getMessage());
            }
        });
    }

    public void updateItem(RoutingContext routingContext) {
        String itemId = routingContext.request().getParam("itemId");
        JsonObject updatedItem = routingContext.getBodyAsJson();

        JsonObject query = new JsonObject().put("_id", itemId);
        JsonObject update = new JsonObject().put("$set", updatedItem);

        mongo.updateCollection("items", query, update, res -> {
            if (res.succeeded()) {
                routingContext.response().end("Item updated successfully");
            } else {
                routingContext.response()
                        .setStatusCode(500)
                        .end("Internal Server Error: " + res.cause().getMessage());
            }
        });
    }

    public void deleteItem(RoutingContext routingContext) {
        String itemId = routingContext.request().getParam("itemId");
        JsonObject query = new JsonObject().put("_id", itemId);

        mongo.removeDocument("items", query, res -> {
            if (res.succeeded()) {
                routingContext.response().end("Item deleted successfully");
            } else {
                routingContext.response()
                        .setStatusCode(500)
                        .end("Internal Server Error: " + res.cause().getMessage());
            }
        });
    }
}
