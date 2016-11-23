package org.folio.rest.impl;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.folio.rest.RestVerticle;
import org.folio.rest.persist.PostgresClient;
import org.folio.rest.tools.utils.NetworkUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.config.EncoderConfig;
import com.jayway.restassured.response.Response;

@RunWith(VertxUnitRunner.class)
public class FundsAPITest {
  private static Vertx vertx;

  /** funds path */
  private static String funds = "/funds";
  /** invoices path */
  private static String polines = "/po_lines";

  private static void setupPostgres() throws Exception {
    PostgresClient.setIsEmbedded(true);
    PostgresClient.getInstance(vertx).startEmbeddedPostgres();
  }

  private static void deployRestVerticle(TestContext context) {
    DeploymentOptions deploymentOptions = new DeploymentOptions().setConfig(
        new JsonObject().put("http.port", RestAssured.port));
    vertx.deployVerticle(RestVerticle.class.getName(), deploymentOptions,
      context.asyncAssertSuccess());

      Async async = context.async(2);
      PostgresClient.getInstance(vertx).mutate(
        "CREATE SCHEMA test; create table test.funds (_id SERIAL PRIMARY KEY,jsonb JSONB NOT NULL)",
        res -> {
          if(res.succeeded()){
            async.countDown();
            System.out.println("funds table created");
            PostgresClient.getInstance(vertx).mutate(
              "create table test.po_line (_id SERIAL PRIMARY KEY,jsonb JSONB NOT NULL)",
              res2 -> {
                if(res2.succeeded()){
                  File resourcesDirectory = new File("src/test/resources");
                  PostgresClient.getInstance(vertx).
                    importFile(resourcesDirectory.getAbsolutePath()+"/import.txt", "test.po_line");
                  async.countDown();
                  System.out.println("invoices table created");
                }
                else{
                  System.out.println("invoices table NOT created");
                  Assert.fail("invoices table NOT created " + res2.cause().getMessage());
                  async.complete();
                }
              });
          }
          else{
            System.out.println("funds table NOT created");
            Assert.fail("funds table NOT created " + res.cause().getMessage());
            async.complete();
          }
        });
  }

  @Before
  public void before(TestContext context) throws Exception {
    vertx = Vertx.vertx();
    RestAssured.port = NetworkUtils.nextFreePort();
    RestAssured.baseURI = "http://localhost";
    RestAssured.config = RestAssured.config().encoderConfig(EncoderConfig.encoderConfig()
        .appendDefaultContentCharsetToContentTypeIfUndefined(false));
    RestAssured.requestSpecification = new RequestSpecBuilder()
      //.addHeader("Authorization", "authtoken")
      .build();

    try {
      setupPostgres();
    } catch (Exception e) {
      context.fail(e);
    }
    deployRestVerticle(context);

  }

  private String getFile(String filename) throws IOException {
    return IOUtils.toString(getClass().getClassLoader().getResourceAsStream(filename), "UTF-8");
  }

  @Test
  public void test() throws IOException {
    given().accept("application/json").
    when().get(funds).
    then().
      body("total_records", equalTo(0)).
      body("funds", empty());

    Response response =
    given().
      body(getFile("fund1.json")).
      contentType("application/json").
      accept("text/plain").
    when().
      post(funds).
    then().
      statusCode(201).
    extract().
      response();
    System.out.println(response.asString());

    response =
    given().accept("application/json").
    when().get(funds).
    then().
      body("total_records", equalTo(1)).
      body("funds[0].code", equalTo("MEDGRANT")).
    extract().response();
    System.out.println(response.asString());

    given().accept("application/json").
    when().get(polines).
    then().
      body("total_records", equalTo(22));

  }


  @After
  public void tearDown(TestContext context) {
    vertx.close(context.asyncAssertSuccess());
  }
}
