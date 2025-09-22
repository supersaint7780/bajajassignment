package com.example.bajaj;

import lombok.Data;

@Data // Lombok annotation for getters, setters, etc.
public class InitialRequest {
    private String name;
    private String regNo;
    private String email;
}