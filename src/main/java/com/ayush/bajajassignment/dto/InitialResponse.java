package com.ayush.bajajassignment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class InitialResponse {
    @JsonProperty("webhook URL to submit your answer")
    private String webhookUrl;
    private String accessToken;
}