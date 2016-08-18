package com.sling.rest.impl;

import java.util.List;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import javax.ws.rs.core.Response;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sling.rest.annotations.Validate;
import com.sling.rest.jaxrs.model.Fund;
import com.sling.rest.jaxrs.model.Funds;
import com.sling.rest.jaxrs.resource.FundsResource;
import com.sling.rest.persist.MongoCRUD;
import com.sling.rest.resource.utils.OutStream;
import com.sling.rest.resource.utils.RestUtils;
import com.sling.rest.tools.Messages;
import com.sling.rest.utils.Consts;

public class FundsAPI implements FundsResource {

  private final Messages            messages = Messages.getInstance();
  private static final ObjectMapper mapper   = new ObjectMapper();

  @Validate
  public void getFunds(String authorization, String query, String orderBy, Order order, int offset, int limit, String lang,
      Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) throws Exception {

    JsonObject q = new JsonObject();
    q.put("query", query);
    System.out.println("sending... getFunds");
    JsonObject jObj = RestUtils.createMongoObject(Consts.FUNDS_COLLECTION, Consts.METHOD_GET, authorization, q, orderBy, order, offset,
        limit, null, null);
    vertxContext.runOnContext(v -> {
      try {
        MongoCRUD.getInstance(vertxContext.owner()).get(
            jObj,
            reply -> {
              try {
                Funds funds = new Funds();
                // this is wasteful!!!
                List<Fund> fundObj = mapper.readValue(reply.result().toString(),
                    mapper.getTypeFactory().constructCollectionType(List.class, Fund.class));
                funds.setFunds(fundObj);
                funds.setTotalRecords(fundObj.size());
                asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(GetFundsResponse.withJsonOK(funds)));
              } catch (Exception e) {
                e.printStackTrace();
                asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(GetFundsResponse.withPlainInternalServerError(messages
                    .getMessage(lang, "10001"))));
              }
            });
      } catch (Exception e) {
        e.printStackTrace();
        asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(GetFundsResponse.withPlainInternalServerError(messages.getMessage(
            lang, "10001"))));
      }
    });
    
  }
  
  @Validate
  public void postFunds(String authorization, String lang, Fund entity, Handler<AsyncResult<Response>> asyncResultHandler,
      Context vertxContext) throws Exception {

    try {
      System.out.println("sending... postFunds");
      JsonObject jObj = RestUtils.createMongoObject(Consts.FUNDS_COLLECTION, Consts.METHOD_POST, authorization, null, null, null, 0, 0,
          entity, null);

      vertxContext.runOnContext(v -> {

        try {
          MongoCRUD.getInstance(vertxContext.owner())
              .save(
                  jObj,
                  reply -> {
                    try {
                      Fund p = new Fund();
                      p = entity;
                      //p.setPatronId(reply.result());
                      OutStream stream = new OutStream();
                      stream.setData(p);
                      asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(PostFundsResponse.withJsonCreated(reply.result(),
                          stream)));
                    } catch (Exception e) {
                      e.printStackTrace();
                      asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(PostFundsResponse
                          .withPlainInternalServerError(messages.getMessage(lang, "10001"))));
                    }
                  });
        } catch (Exception e) {
          e.printStackTrace();
          asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(PostFundsResponse.withPlainInternalServerError(messages
              .getMessage(lang, "10001"))));
        }
      });
    } catch (Exception e) {
      e.printStackTrace();
      asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(PostFundsResponse.withPlainInternalServerError(messages.getMessage(
          lang, "10001"))));
    }
  }
  
  @Validate
  public void getFundsByFundId(String fundId, String authorization, String lang, Handler<AsyncResult<Response>> asyncResultHandler,
      Context vertxContext) throws Exception {

    try {
      JsonObject q = new JsonObject();
      q.put("_id", fundId);
      JsonObject jObj = RestUtils.createMongoObject(Consts.FUNDS_COLLECTION, Consts.METHOD_GET, authorization, q, null, null, 0, 0, null,
          fundId);
      System.out.println("sending... getFundsByFundId");
      vertxContext.runOnContext(v -> {
        MongoCRUD.getInstance(vertxContext.owner()).get(
            jObj,
            reply -> {
              try {
                List<Fund> funds = mapper.readValue(reply.result().toString(),
                    mapper.getTypeFactory().constructCollectionType(List.class, Fund.class));
                if (funds.size() == 0) {
                  asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(GetFundsByFundIdResponse.withPlainNotFound("Patron"
                      + messages.getMessage(lang, "10008"))));
                } else {
                  asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(GetFundsByFundIdResponse.withJsonOK(funds.get(0))));
                }
              } catch (Exception e) {
                e.printStackTrace();
                asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(GetFundsByFundIdResponse
                    .withPlainInternalServerError(messages.getMessage(lang, "10001"))));
              }
            });
      });
    } catch (Exception e) {
      e.printStackTrace();
      asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(GetFundsByFundIdResponse.withPlainInternalServerError(messages
          .getMessage(lang, "10001"))));
    }
  }
  
  @Validate
  public void deleteFundsByFundId(String fundId, String authorization, String lang, Handler<AsyncResult<Response>> asyncResultHandler,
      Context vertxContext) throws Exception {

    try {
      JsonObject q = new JsonObject();
      q.put("_id", fundId);
      JsonObject jObj = RestUtils.createMongoObject(Consts.FUNDS_COLLECTION, Consts.METHOD_DELETE, authorization, q, null, null, 0, 0,
          null, null);
      System.out.println("sending... deleteFundsByFundId");
      vertxContext.runOnContext(v -> {
        MongoCRUD.getInstance(vertxContext.owner()).delete(
            jObj,
            reply -> {
              try {
                asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(DeleteFundsByFundIdResponse.withNoContent()));
              } catch (Exception e) {
                e.printStackTrace();
                asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(DeleteFundsByFundIdResponse
                    .withPlainInternalServerError(messages.getMessage(lang, "10001"))));
              }
            });
      });
    } catch (Exception e) {
      e.printStackTrace();
      asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(DeleteFundsByFundIdResponse.withPlainInternalServerError(messages
          .getMessage(lang, "10001"))));
    }
  }

  @Validate
  public void putFundsByFundId(String fundId, String authorization, String lang, Fund entity,
      Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) throws Exception {

    try {
      JsonObject q = new JsonObject();
      q.put("_id", fundId);
      JsonObject jObj = RestUtils.createMongoObject(Consts.FUNDS_COLLECTION, Consts.METHOD_PUT, authorization, q, null, null, 0, 0,
          entity, null);
      System.out.println("sending... putPatronsByPatronId");
      vertxContext.runOnContext(v -> {
        MongoCRUD.getInstance(vertxContext.owner()).update(
            jObj,
            reply -> {
              try {
                asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(PutFundsByFundIdResponse.withNoContent()));
              } catch (Exception e) {
                e.printStackTrace();
                asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(PutFundsByFundIdResponse
                    .withPlainInternalServerError(messages.getMessage(lang, "10001"))));
              }
            });
      });
    } catch (Exception e) {
      e.printStackTrace();
      asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(PutFundsByFundIdResponse.withPlainInternalServerError(messages
          .getMessage(lang, "10001"))));
    }
    
  }

}
