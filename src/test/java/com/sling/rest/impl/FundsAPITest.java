package com.sling.rest.impl;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static com.jayway.restassured.http.ContentType.TEXT;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.config.EncoderConfig;
import com.jayway.restassured.response.Response;
import com.sling.rest.RestVerticle;
import com.sling.rest.persist.PostgresClient;
import com.sling.rest.resource.utils.NetworkUtils;

public class FundsAPITest {
  private static Vertx vertx;
  
  /** funds path */
  private static String funds    = "/apis/funds";
  /** invoices path */
  private static String polines = "/apis/po_lines";
  
  private static void setupPostgres() throws Exception {
    PostgresClient.setIsEmbedded(true);
    PostgresClient.getInstance(vertx).startEmbeddedPostgres();
  }

  private static void deployRestVerticle() {
    DeploymentOptions deploymentOptions = new DeploymentOptions().setConfig(
        new JsonObject().put("http.port", RestAssured.port));
    vertx.deployVerticle(RestVerticle.class.getName(), deploymentOptions, v -> {
      
      PostgresClient.getInstance(vertx).mutate(
        "CREATE SCHEMA test; create table test.funds (_id SERIAL PRIMARY KEY,jsonb JSONB NOT NULL)",
        res -> {
          if(res.succeeded()){
            System.out.println("funds table created");
            PostgresClient.getInstance(vertx).mutate(
              "create table test.po_line (_id SERIAL PRIMARY KEY,jsonb JSONB NOT NULL)",
              res2 -> {
                if(res2.succeeded()){
                  System.out.println("invoices table created");
                }
                else{
                  System.out.println("invoices table NOT created");
                  Assert.fail("invoices table NOT created " + res2.cause().getMessage());
                }
              });
          }
          else{
            System.out.println("funds table NOT created");
            Assert.fail("funds table NOT created " + res.cause().getMessage());
          }
        });
    });
  }
  
  @BeforeClass
  public static void beforeClass() throws Exception {
    vertx = Vertx.vertx();
    RestAssured.port = NetworkUtils.nextFreePort(); 
    RestAssured.baseURI = "http://localhost";
    RestAssured.config = RestAssured.config().encoderConfig(EncoderConfig.encoderConfig()
        .appendDefaultContentCharsetToContentTypeIfUndefined(false));
    RestAssured.requestSpecification = new RequestSpecBuilder()
      .addHeader("Authorization", "authtoken")
      .build();

    setupPostgres();
    deployRestVerticle();
  }

  private String getFile(String filename) throws IOException {
    return IOUtils.toString(getClass().getClassLoader().getResourceAsStream(filename), "UTF-8");
  }
  
  @Test
  public void getFunds() throws IOException {
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
  }

  @Test
  public void getPOLines() {
    given().accept("application/json").
    when().get(polines).
    then().
      body("total_records", equalTo(0)).
      body("po_lines", empty());
  }
  
  
  @AfterClass
  public static void shutdown(){
    PostgresClient.stopEmbeddedPostgres();
  }
}
