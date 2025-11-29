package com.scmtp.routeservice.graphql;

import java.util.UUID;

/**
 * GraphQL representation of Route - not a JPA entity
 * This avoids Hibernate multiple bag fetch issues
 */
public class RouteGraphQL {
    private UUID id;
    private String name;

    public RouteGraphQL() {
    }

    public RouteGraphQL(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

