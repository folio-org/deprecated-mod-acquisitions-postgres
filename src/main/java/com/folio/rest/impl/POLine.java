package com.folio.rest.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Handler;

import java.util.List;

import javax.ws.rs.core.Response;

import com.sling.rest.annotations.Validate;
import com.folio.rest.jaxrs.model.FundDistribution;
import com.folio.rest.jaxrs.model.PoLine;
import com.folio.rest.jaxrs.model.PoLines;
import com.folio.rest.jaxrs.resource.POLinesResource;
import com.sling.rest.persist.PostgresClient;
import com.sling.rest.persist.Criteria.Criteria;
import com.sling.rest.persist.Criteria.Criterion;
import com.sling.rest.persist.Criteria.Limit;
import com.sling.rest.persist.Criteria.Offset;
import com.sling.rest.persist.Criteria.Order.ORDER;
import com.sling.rest.resource.utils.OutStream;
import com.sling.rest.tools.Messages;

/**
 * @author shale
 *
 */
public class POLine implements POLinesResource {

  public final static String TABLE_NAME_POLINE = "test.po_line";
  public final static String TABLE_NAME_FUNDS  = "test.funds";

  public final static String JSONB_FIELD       = "jsonb";

  private final Messages     messages          = Messages.getInstance();

  @Validate
  @Override
  public void getPoLines(String authorization, String query, String orderBy, Order order, int offset, int limit, String lang,
      Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) throws Exception {

    /**
     * query can be a json array containing one or more {field, value, op}
     * triplet if more then one triple is used an op should be added between the
     * two
     */
    Criterion criterion = Criterion.json2Criterion(query);

    try {
      System.out.println("sending... getPoLines");
      vertxContext.runOnContext(v -> {
        try {
          criterion.setLimit(new Limit(limit)).setOffset(new Offset(offset));
          com.sling.rest.persist.Criteria.Order or = getOrder(order, orderBy);
          if (or != null) {
            criterion.setOrder(or);
          }
          PostgresClient.getInstance(vertxContext.owner()).get(
              TABLE_NAME_POLINE, PoLine.class, criterion, true,
              reply -> {
                try {
                  if (reply.succeeded()) {
                    System.out.println("sending... getPoLines");
                    List<PoLine> polines = (List<PoLine>) reply.result()[0];
                    PoLines p = new PoLines();
                    p.setPoLines(polines);
                    p.setTotalRecords((int) reply.result()[1]);
                    OutStream stream = new OutStream();
                    stream.setData(p);
                    asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(GetPoLinesResponse.withJsonOK(p)));
                  } else {
                    asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(GetPoLinesResponse.withPlainInternalServerError(reply
                        .cause().getMessage())));
                  }
                } catch (Exception e) {
                  e.printStackTrace();
                  asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(GetPoLinesResponse.withPlainInternalServerError(messages
                      .getMessage(lang, "10001"))));
                }
              });
        } catch (Exception e) {
          e.printStackTrace();
          asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(GetPoLinesResponse.withPlainInternalServerError(messages
              .getMessage(lang, "10001"))));
        }
      });
    } catch (Exception e) {
      e.printStackTrace();
      asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(GetPoLinesResponse.withPlainInternalServerError(messages.getMessage(
          lang, "10001"))));
    }
  }

  private void saveLoop(PostgresClient client, Object beginTx, List<?> loop, String tableName,
      Handler<AsyncResult<String>> asyncResultHandler) {
    try {
      if (loop.size() > 0) {
        client.save(beginTx, tableName, loop.get(0), reply2 -> {
          if (reply2.succeeded()) {
            loop.remove(0);
            saveLoop(client, beginTx, loop, tableName, asyncResultHandler);
            asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(reply2.result()));
          }
        });
      }
    } catch (Exception e) {
      e.printStackTrace();
      asyncResultHandler.handle(io.vertx.core.Future.failedFuture(e.getMessage()));
    }
  }

  @Override
  @Validate
  public void postPoLines(String authorization, String lang, PoLine entity, Handler<AsyncResult<Response>> asyncResultHandler,
      Context vertxContext) throws Exception {

    try {
      System.out.println("sending... postPoLines");
      PostgresClient postgresClient = PostgresClient.getInstance(vertxContext.owner());

      vertxContext.runOnContext(v -> {
        try {
          postgresClient.startTx(beginTx -> {
            try {
              postgresClient.save(beginTx, TABLE_NAME_POLINE, entity, reply -> {
                try {
                  if (reply.succeeded()) {
                    System.out.println("sending... postPoLines");
                    final PoLine p = entity;
                    // p.setId(reply.result());
                  final List<FundDistribution> funds = p.getFundDistributions();
                  if (funds.size() > 0) {
                    try {
                      postgresClient.save(beginTx, TABLE_NAME_FUNDS, funds.get(0), reply2 -> {
                        if (reply2.failed()) {
                          postgresClient.rollbackTx(beginTx, done -> {
                            postgresClient.endTx(beginTx, done2 -> {
                              asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(PostPoLinesResponse
                                  .withPlainInternalServerError(reply2.cause().getMessage())));
                            });
                          });
                        } else {
                          System.out.println("sending... postPoLines");
                          OutStream stream = new OutStream();
                          stream.setData(p);
                          postgresClient.endTx(beginTx, done -> {
                            try {
                              postgresClient.get(TABLE_NAME_POLINE, entity, true, reply3 -> {
                                List<PoLine> polines = (List<PoLine>) reply3.result()[0];
                                System.out.println("size " + polines.size());
                                asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(PostPoLinesResponse.withJsonCreated(
                                    reply.result(), stream)));
                              });
                            } catch (Exception e) {
                              e.printStackTrace();
                            }

                          });
                        }
                      });
                    } catch (Exception e) {
                      e.printStackTrace();
                    }
                  } else {
                    asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(PostPoLinesResponse
                        .withPlainInternalServerError("Error....No funds found in po line")));
                  }
                } else {
                  asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(PostPoLinesResponse.withPlainInternalServerError(reply
                      .cause().getMessage())));
                }
              } catch (Exception e) {
                e.printStackTrace();
                asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(PostPoLinesResponse.withPlainInternalServerError(messages
                    .getMessage(lang, "10001"))));
              }
            } );
            } catch (Exception e) {
              e.printStackTrace();
              asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(PostPoLinesResponse.withPlainInternalServerError(messages
                  .getMessage(lang, "10001"))));
            }

          });

        } catch (Exception e) {
          e.printStackTrace();
          asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(PostPoLinesResponse.withPlainInternalServerError(messages
              .getMessage(lang, "10001"))));
        }
      });
    } catch (Exception e) {
      e.printStackTrace();
      asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(PostPoLinesResponse.withPlainInternalServerError(messages.getMessage(
          lang, "10001"))));
    }

  }
  
  @Validate
  @Override
  public void getPoLinesByPoLineId(String poLineId, String authorization, String lang, Handler<AsyncResult<Response>> asyncResultHandler,
      Context vertxContext) throws Exception {

    try {
      System.out.println("sending... getPoLinesByPoLineId");
      vertxContext.runOnContext(v -> {
        try {
          Criteria c = new Criteria();
          c.addField("_id");
          c.setOperation("=");
          c.setValue(poLineId);
          c.setJSONB(false);
          PostgresClient.getInstance(vertxContext.owner()).get( TABLE_NAME_POLINE,
              PoLine.class, new Criterion(c), true, reply -> {
                try {
                  if (reply.succeeded()) {
                    List<PoLine> polines = (List<PoLine>) reply.result()[0];
                    PoLines p = new PoLines();
                    p.setPoLines(polines);
                    p.setTotalRecords((int) reply.result()[1]);
                    OutStream stream = new OutStream();
                    stream.setData(p);
                    asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(GetPoLinesResponse.withJsonOK(p)));
                  } else {
                    asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(GetPoLinesResponse.withPlainInternalServerError(reply
                        .cause().getMessage())));
                  }
                } catch (Exception e) {
                  e.printStackTrace();
                  asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(GetPoLinesResponse.withPlainInternalServerError(messages
                      .getMessage(lang, "10001"))));
                }
              });
        } catch (Exception e) {
          e.printStackTrace();
          asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(GetPoLinesResponse.withPlainInternalServerError(messages
              .getMessage(lang, "10001"))));
        }
      });
    } catch (Exception e) {
      e.printStackTrace();
      asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(GetPoLinesResponse.withPlainInternalServerError(messages.getMessage(
          lang, "10001"))));
    }

  }

  @Validate
  @Override
  public void deletePoLinesByPoLineId(String poLineId, String authorization, String lang,
      Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) throws Exception {
    try {
      System.out.println("sending... getPoLinesByPoLineId");
      vertxContext.runOnContext(v -> {
        try {
          Criteria c = new Criteria();
          c.addField("_id");
          c.setOperation("=");
          c.setValue(poLineId);

          PostgresClient.getInstance(vertxContext.owner()).delete(
              TABLE_NAME_POLINE, new Criterion(c), reply -> {
                try {
                  if (reply.succeeded()) {
                    asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(DeletePoLinesByPoLineIdResponse.withNoContent()));
                  } else {
                    asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(DeletePoLinesByPoLineIdResponse
                        .withPlainInternalServerError(reply.cause().getMessage())));
                  }
                } catch (Exception e) {
                  e.printStackTrace();
                  asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(DeletePoLinesByPoLineIdResponse
                      .withPlainInternalServerError(messages.getMessage(lang, "10001"))));
                }
              });
        } catch (Exception e) {
          e.printStackTrace();
          asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(DeletePoLinesByPoLineIdResponse
              .withPlainInternalServerError(messages.getMessage(lang, "10001"))));
        }
      });
    } catch (Exception e) {
      e.printStackTrace();
      asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(DeletePoLinesByPoLineIdResponse.withPlainInternalServerError(messages
          .getMessage(lang, "10001"))));
    }

  }
  
  @Validate
  @Override
  public void putPoLinesByPoLineId(String poLineId, String authorization, String lang, PoLine entity,
      Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) throws Exception {
    try {
      System.out.println("sending... putPoLinesByPoLineId");
      vertxContext.runOnContext(v -> {
        try {
          Criteria c = new Criteria();
          c.addField("_id");
          c.setOperation("=");
          c.setValue(poLineId);
          c.setJSONB(false);

          // update option 1: (pass entity and criteria)

          PostgresClient.getInstance(vertxContext.owner()).update(
              TABLE_NAME_POLINE,
              entity,
              new Criterion(c),
              true,
              reply -> {
                try {
                  if (reply.succeeded()) {
                    asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(PutPoLinesByPoLineIdResponse.withNoContent()));
                  } else {
                    asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(PutPoLinesByPoLineIdResponse
                        .withPlainInternalServerError(reply.cause().getMessage())));
                  }
                } catch (Exception e) {
                  e.printStackTrace();
                  asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(PutPoLinesByPoLineIdResponse
                      .withPlainInternalServerError(messages.getMessage(lang, "10001"))));
                }
              });

          /*
           * UpdateSection us = new UpdateSection();
           * us.addField("price").addField("po_currency").addField("value");
           * us.setValue(entity.getPrice().getPoCurrency().getValue()); //see
           * the @JsonProperty(value="YYY") annotation in the javadocs (do
           * mouseover on the getter)
           * 
           * PostgresClient.getInstance(vertxContext.owner()).update(
           * TABLE_NAME_POLINE, us, new Criterion( c ), true, reply -> { try {
           * if (reply.succeeded()) {
           * System.out.println("number of records updated" + reply.result());
           * asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(
           * PutPoLinesByPoLineIdResponse.withNoContent())); } else {
           * asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(
           * PutPoLinesByPoLineIdResponse
           * .withPlainInternalServerError(reply.cause().getMessage()))); } }
           * catch (Exception e) { e.printStackTrace();
           * asyncResultHandler.handle
           * (io.vertx.core.Future.succeededFuture(PutPoLinesByPoLineIdResponse
           * .withPlainInternalServerError(messages.getMessage(lang,
           * "10001")))); } });
           */

        } catch (Exception e) {
          e.printStackTrace();
          asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(PutPoLinesByPoLineIdResponse.withPlainInternalServerError(messages
              .getMessage(lang, "10001"))));
        }
      });
    } catch (Exception e) {
      e.printStackTrace();
      asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(PutPoLinesByPoLineIdResponse.withPlainInternalServerError(messages
          .getMessage(lang, "10001"))));
    }
  }

  private com.sling.rest.persist.Criteria.Order getOrder(Order order, String field) {

    if (field == null) {
      return null;
    }

    String sortOrder = com.sling.rest.persist.Criteria.Order.ASC;
    if (order.name().equals("asc")) {
      sortOrder = com.sling.rest.persist.Criteria.Order.DESC;
    }

    return new com.sling.rest.persist.Criteria.Order(field, ORDER.valueOf(sortOrder.toUpperCase()));
  }
}
