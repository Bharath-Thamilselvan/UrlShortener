package com.example.UrlShortner.domain.models;

public record CreateUserCmd(
        String email,
        String password,
        String name,
        Role role
) {
}
