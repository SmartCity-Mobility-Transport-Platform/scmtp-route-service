package com.scmtp.routeservice.graphql;

import com.scmtp.routeservice.domain.Route;
import com.scmtp.routeservice.domain.Schedule;
import com.scmtp.routeservice.domain.Stop;
import com.scmtp.routeservice.service.RouteQueryService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

@Controller
public class RouteGraphQLController {

    private final RouteQueryService routeQueryService;

    public RouteGraphQLController(RouteQueryService routeQueryService) {
        this.routeQueryService = routeQueryService;
    }

    @QueryMapping
    public List<Route> routes() {
        return routeQueryService.getAllRoutes();
    }

    @QueryMapping
    public Route route(@Argument UUID id) {
        return routeQueryService.getRouteById(id).orElse(null);
    }

    @QueryMapping
    public List<Stop> stopsByLocation(@Argument double lat,
                                      @Argument double lng,
                                      @Argument double radiusKm) {
        return routeQueryService.findStopsByLocation(lat, lng, radiusKm);
    }

    @QueryMapping
    public List<Schedule> nextDepartures(@Argument UUID stopId,
                                         @Argument String fromTime) {
        return routeQueryService.getNextDepartures(stopId, fromTime);
    }
}



