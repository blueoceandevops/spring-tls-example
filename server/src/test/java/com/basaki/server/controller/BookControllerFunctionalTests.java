package com.basaki.server.controller;

import com.basaki.server.ServerApplication;
import com.basaki.server.data.entity.Book;
import com.basaki.server.model.BookRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.io.IOException;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static io.restassured.RestAssured.given;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;

/**
 * {@code BookControllerFunctionalTests} represents functional tests for {@code
 * BookController}.
 * <p/>
 *
 * @author Indra Basak
 * @since 02/10/18
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ServerApplication.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class BookControllerFunctionalTests {

    @Value("${local.server.port}")
    private Integer port;

    @Autowired
    @Qualifier("customObjectMapper")
    private ObjectMapper objectMapper;

    @Value("${server.ssl.key-store}")
    private String pathToJks;

    @Value("${server.ssl.key-store-password}")
    private String password;

    @Before
    public void startUp() {
        RestAssured.useRelaxedHTTPSValidation();
        RestAssured.config().getSSLConfig()
                .with().keyStore(pathToJks, password);
    }

    @Test
    public void testCreateAndRead() throws IOException {
        BookRequest bookRequest = new BookRequest("Indra's Chronicle", "Indra");

        Response response = given()
                .auth().basic("userA", "passwordA")
                .contentType(ContentType.JSON)
                .baseUri("https://localhost")
                .port(port)
                .contentType(ContentType.JSON)
                .body(bookRequest)
                .post("/books");
        assertNotNull(response);
        assertEquals(201, response.getStatusCode());
        Book bookCreate =
                objectMapper.readValue(response.getBody().prettyPrint(),
                        Book.class);
        assertNotNull(bookCreate);
        assertNotNull(bookCreate.getId());
        assertEquals(bookRequest.getTitle(), bookCreate.getTitle());
        assertEquals(bookRequest.getAuthor(), bookCreate.getAuthor());

        response = given()
                .auth().basic("userB", "passwordB")
                .baseUri("https://localhost")
                .port(port)
                .contentType(ContentType.JSON)
                .get("/books/" + bookCreate.getId().toString());

        assertNotNull(response);
        assertEquals(200, response.getStatusCode());

        Book bookRead = objectMapper.readValue(response.getBody().prettyPrint(),
                Book.class);
        assertNotNull(bookRead);
        assertEquals(bookCreate.getId(), bookRead.getId());
        assertEquals(bookCreate.getAuthor(), bookRead.getAuthor());
        assertEquals(bookCreate.getTitle(), bookRead.getTitle());
        assertEquals(bookCreate.getAuthor(), bookRead.getAuthor());
    }

    @Test
    public void testDataNotFoundRead() {
        Response response = given()
                .auth().basic("userB", "passwordB")
                .baseUri("https://localhost")
                .port(port)
                .contentType(ContentType.JSON)
                .get("/books/" + UUID.randomUUID().toString());

        assertNotNull(response);
        assertEquals(404, response.getStatusCode());
    }
}
