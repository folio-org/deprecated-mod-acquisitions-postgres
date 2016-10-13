package org.folio.rest.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Handler;

import java.util.List;

import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.folio.rest.annotations.Validate;
import org.folio.rest.jaxrs.model.Fund;
import org.folio.rest.jaxrs.model.Funds;
import org.folio.rest.jaxrs.resource.FundsResource;
import org.folio.rest.utils.Consts;
import org.folio.rest.persist.PostgresClient;
import org.folio.rest.persist.Criteria.Criterion;
import org.folio.rest.persist.Criteria.Limit;
import org.folio.rest.persist.Criteria.Offset;
import org.folio.rest.persist.Criteria.Order.ORDER;
import org.folio.rest.tools.utils.OutStream;
import org.folio.rest.tools.messages.Messages;

public class FundsAPI implements FundsResource {

  private final Messages            messages = Messages.getInstance();
  private static final ObjectMapper mapper   = new ObjectMapper();

  @Override
  @Validate
  public void getFunds(String query, String orderBy, Order order, int offset, int limit, String lang,
      Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) throws Exception {

    Criterion criterion = Criterion.json2Criterion(query);
    System.out.println("sending... getFunds");

    vertxContext.runOnContext(v -> {
      try {
        criterion.setLimit(new Limit(limit)).setOffset(new Offset(offset));
        org.folio.rest.persist.Criteria.Order or = getOrder(order, orderBy);
        if (or != null) {
          criterion.setOrder(or);
        }
        PostgresClient.getInstance(vertxContext.owner()).get(Consts.FUNDS_COLLECTION, Fund.class , criterion, true,
            reply -> {
              try {
                Funds funds = new Funds();
                List<Fund> fundObj = (List<Fund>)reply.result()[0];
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

  @Override
  @Validate
  public void postFunds(String lang, Fund entity, Handler<AsyncResult<Response>> asyncResultHandler,
      Context vertxContext) throws Exception {

    try {
      System.out.println("sending... postFunds");
      PostgresClient postgresClient = PostgresClient.getInstance(vertxContext.owner());

      vertxContext.runOnContext(v -> {

        try {
          postgresClient.save(Consts.FUNDS_COLLECTION, entity,
                  reply -> {
                    try {
                      Fund p = entity;
                      p.setId(reply.result());
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
  public void getFundsByFundId(String fundId, String lang, Handler<AsyncResult<Response>> asyncResultHandler,
      Context vertxContext) throws Exception {

/*    try {
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
    }*/
    asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(GetFundsByFundIdResponse.withPlainInternalServerError(messages
      .getMessage(lang, "10001"))));
  }

  @Validate
  public void deleteFundsByFundId(String fundId, String lang, Handler<AsyncResult<Response>> asyncResultHandler,
      Context vertxContext) throws Exception {

/*    try {
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
    }*/
    asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(DeleteFundsByFundIdResponse.withPlainInternalServerError(messages
      .getMessage(lang, "10001"))));
  }

  @Validate
  public void putFundsByFundId(String fundId, String lang, Fund entity,
      Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) throws Exception {

/*    try {
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
    }*/
    asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(PutFundsByFundIdResponse.withPlainInternalServerError(messages
      .getMessage(lang, "10001"))));
  }

  private org.folio.rest.persist.Criteria.Order getOrder(Order order, String field) {

    if (field == null) {
      return null;
    }

    String sortOrder = org.folio.rest.persist.Criteria.Order.ASC;
    if (order.name().equals("asc")) {
      sortOrder = org.folio.rest.persist.Criteria.Order.DESC;
    }

    return new org.folio.rest.persist.Criteria.Order(field, ORDER.valueOf(sortOrder.toUpperCase()));
  }

}
