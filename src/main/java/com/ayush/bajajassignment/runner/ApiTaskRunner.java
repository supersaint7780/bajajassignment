package com.ayush.bajajassignment.runner;

import com.ayush.bajajassignment.dto.FinalRequest;
import com.ayush.bajajassignment.dto.InitialRequest;
import com.ayush.bajajassignment.dto.InitialResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ApiTaskRunner implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(ApiTaskRunner.class);

    private final RestTemplate restTemplate;

    public ApiTaskRunner(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public void run(String... args) {
        logger.info("Application started, running API tasks...");

        // Prepare the initial request body with your details
        InitialRequest userDetails = new InitialRequest();
        userDetails.setName("Ayush Maurya");
        userDetails.setRegNo("2240401124");
        userDetails.setEmail("2240401124@stu.manit.ac.in");

        // Step 1: Send the initial POST request to generate the webhook
        InitialResponse initialResponse = generateWebhook(userDetails);
        if (initialResponse == null || initialResponse.getWebhookUrl() == null) {
            logger.error("Failed to get webhook URL. Halting process.");
            return;
        }

        logger.info("Successfully received webhook URL: {}", initialResponse.getWebhookUrl());
        logger.info("Access Token received.");

        // Step 2: Send the solution to the received webhook URL
        submitSolution(initialResponse, userDetails.getRegNo());
    }

    private InitialResponse generateWebhook(InitialRequest requestBody) {
        String url = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

        logger.info("Sending initial request to: {}", url);
        try {
            return restTemplate.postForObject(url, requestBody, InitialResponse.class);
        } catch (Exception e) {
            logger.error("Error during webhook generation: {}", e.getMessage());
            return null;
        }
    }

    private void submitSolution(InitialResponse response, String regNo) {
        String submissionUrl = response.getWebhookUrl();
        String accessToken = response.getAccessToken();
        String finalQuery = getFinalQueryByRegNo(regNo);

        if (finalQuery.isEmpty() || finalQuery.contains("YOUR_ODD_NUMBER_SQL_QUERY_HERE")) {
            logger.error("Could not determine a valid SQL query for regNo: {}. Halting.", regNo);
            return;
        }

        FinalRequest finalRequestBody = new FinalRequest();
        finalRequestBody.setFinalQuery(finalQuery);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // This line sets the header with only the token value, as requested.
        headers.set("Authorization", accessToken);

        HttpEntity<FinalRequest> requestEntity = new HttpEntity<>(finalRequestBody, headers);

        logger.info("Submitting final query to: {}", submissionUrl);
        try {
            String result = restTemplate.postForObject(submissionUrl, requestEntity, String.class);
            logger.info("Submission successful! Response: {}", result);
        } catch (Exception e) {
            logger.error("Error during solution submission: {}", e.getMessage());
        }
    }

    private String getFinalQueryByRegNo(String regNo) {
        try {
            int lastTwoDigits = Integer.parseInt(regNo.substring(regNo.length() - 2));

            if (lastTwoDigits % 2 == 0) {
                logger.info("Registration number ends in an even number ({}). Using Question 2.", lastTwoDigits);
                return "SELECT e1.EMP_ID, e1.FIRST_NAME, e1.LAST_NAME, d.DEPARTMENT_NAME, COUNT(e2.EMP_ID) AS YOUNGER_EMPLOYEES_COUNT FROM EMPLOYEE e1 JOIN DEPARTMENT d ON e1.DEPARTMENT = d.DEPARTMENT_ID LEFT JOIN EMPLOYEE e2 ON e1.DEPARTMENT = e2.DEPARTMENT AND e2.DOB > e1.DOB GROUP BY e1.EMP_ID, e1.FIRST_NAME, e1.LAST_NAME, d.DEPARTMENT_NAME ORDER BY e1.EMP_ID DESC;";
            } else {
                logger.info("Registration number ends in an odd number ({}). Using Question 1.", lastTwoDigits);
                // Make sure to replace this if you get an odd registration number.
                return "YOUR_ODD_NUMBER_SQL_QUERY_HERE";
            }
        } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
            logger.error("Could not parse registration number: {}", regNo, e);
            return "";
        }
    }
}