package com.scmtp.routeservice.graphql;

import com.scmtp.routeservice.domain.Route;
import com.scmtp.routeservice.domain.RouteStop;
import com.scmtp.routeservice.domain.Schedule;
import com.scmtp.routeservice.domain.Stop;
import com.scmtp.routeservice.dto.RouteDTO;
import com.scmtp.routeservice.repository.RouteStopRepository;
import com.scmtp.routeservice.repository.ScheduleRepository;
import com.scmtp.routeservice.service.RouteQueryService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
public class RouteGraphQLController {

    private final RouteQueryService routeQueryService;
    private final RouteStopRepository routeStopRepository;
    private final ScheduleRepository scheduleRepository;

    public RouteGraphQLController(RouteQueryService routeQueryService,
                                  RouteStopRepository routeStopRepository,
                                  ScheduleRepository scheduleRepository) {
        this.routeQueryService = routeQueryService;
        this.routeStopRepository = routeStopRepository;
        this.scheduleRepository = scheduleRepository;
    }

    @QueryMapping
    public List<RouteGraphQL> routes() {
        // Use non-entity class to avoid Hibernate issues
        List<RouteDTO> dtos = routeQueryService.getAllRoutes();
        return dtos.stream()
                .map(dto -> new RouteGraphQL(dto.getId(), dto.getName()))
                .collect(Collectors.toList());
    }

    @QueryMapping
    public RouteGraphQL route(@Argument("id") UUID id) {
        // Return RouteGraphQL to match the routes() query return type and resolver expectations
        return routeQueryService.getRouteById(id)
                .map(route -> new RouteGraphQL(route.getId(), route.getName()))
                .orElse(null);
    }

    @QueryMapping
    public List<Stop> stopsByLocation(@Argument("lat") double lat,
                                      @Argument("lng") double lng,
                                      @Argument("radiusKm") double radiusKm) {
        return routeQueryService.findStopsByLocation(lat, lng, radiusKm);
    }

    @QueryMapping
    public List<Schedule> nextDepartures(@Argument("stopId") UUID stopId,
                                         @Argument("fromTime") String fromTime) {
        return routeQueryService.getNextDepartures(stopId, fromTime);
    }

    // Custom resolver for Route.stops - fetches separately to avoid multiple bag fetch
    @SchemaMapping(typeName = "Route", field = "stops")
    public List<Stop> getStops(RouteGraphQL route) {
        if (route == null || route.getId() == null) {
            return List.of();
        }
        try {
            List<RouteStop> routeStops = routeStopRepository.findByRouteIdOrderByOrderIndexAsc(route.getId());
            return routeStops.stream()
                    .map(RouteStop::getStop)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error fetching stops for route " + route.getId() + ": " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    // Custom resolver for Route.schedules - fetches separately to avoid multiple bag fetch
    @SchemaMapping(typeName = "Route", field = "schedules")
    public List<Schedule> getSchedules(RouteGraphQL route) {
        if (route == null || route.getId() == null) {
            return List.of();
        }
        try {
            UUID routeId = route.getId();
            List<Schedule> schedules = scheduleRepository.findByRouteId(routeId);
            // Ensure route relationship is initialized for each schedule
            schedules.forEach(schedule -> {
                Route scheduleRoute = schedule.getRoute();
                if (scheduleRoute == null) {
                    scheduleRoute = new Route();
                    scheduleRoute.setId(routeId);
                    schedule.setRoute(scheduleRoute);
                } else if (scheduleRoute.getId() == null) {
                    scheduleRoute.setId(routeId);
                }
            });
            return schedules;
        } catch (Exception e) {
            System.err.println("Error fetching schedules for route " + route.getId() + ": " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    // Custom resolver for Schedule.routeId - extracts route ID from the route relationship
    @SchemaMapping(typeName = "Schedule", field = "routeId")
    public UUID getRouteId(Schedule schedule) {
        // Try to get from the schedule's route relationship
        if (schedule.getRoute() != null && schedule.getRoute().getId() != null) {
            return schedule.getRoute().getId();
        }
        // This should not happen if getSchedules properly initializes the route
        // But if it does, we need to throw an error or return a default
        throw new IllegalStateException("Schedule route is not initialized. Schedule ID: " + schedule.getId());
    }
}



