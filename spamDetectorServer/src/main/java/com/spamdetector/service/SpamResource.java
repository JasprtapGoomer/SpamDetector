package com.spamdetector.service;

import com.spamdetector.domain.TestFile;
import com.spamdetector.util.SpamDetector;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.File;
import java.net.URL;
import java.util.List;

@Path("/spam")
public class SpamResource {

    private SpamDetector detector = new SpamDetector();
    private List<TestFile> testResults;

    public SpamResource() {
        try {
            this.testResults = trainAndTest();
        } catch (Exception e) {
            // Log the error or handle it appropriately
            System.err.println("Error initializing SpamResource: " + e.getMessage());
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSpamResults() {
        if (testResults != null && !testResults.isEmpty()) {
            return Response.ok(testResults).header("Access-Control-Allow-Origin", "http://localhost:63342")
                    .header("Content-Type", "application/json").build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity("{\"error\":\"Test results not available.\"}").build();
        }
    }

    @GET
    @Path("/accuracy")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAccuracy() {
        if (testResults == null || testResults.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"No test results available to calculate accuracy.\"}").build();
        }
        double accuracy = detector.getAccuracy();
        return Response.ok("{\"accuracy\": " + accuracy + "}").header("Access-Control-Allow-Origin", "http://localhost:63342")
                .header("Content-Type", "application/json").build();
    }

    @GET
    @Path("/precision")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPrecision() {
        if (testResults == null || testResults.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"No test results available to calculate precision.\"}").build();
        }
        double precision = detector.getPrecision();
        return Response.ok("{\"precision\": " + precision + "}").header("Access-Control-Allow-Origin", "http://localhost:63342")
                .header("Content-Type", "application/json").build();
    }

    private List<TestFile> trainAndTest() {
        URL resourceUrl = getClass().getClassLoader().getResource("data");
        if (resourceUrl == null) {
            throw new IllegalStateException("data directory not found!");
        }
        File mainDirectory = new File(resourceUrl.getPath());
        return detector.trainAndTest(mainDirectory);
    }
}
