package com.ayush.bajajassignment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class InitialResponse {
    // Correct code based on the actual API response
    @JsonProperty("webhook")
    private String webhookUrl;
    private String accessToken;
}