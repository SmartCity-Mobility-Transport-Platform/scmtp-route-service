package com.scmtp.routeservice.service;

import com.scmtp.routeservice.domain.Route;
import com.scmtp.routeservice.domain.RouteStop;
import com.scmtp.routeservice.domain.Schedule;
import com.scmtp.routeservice.domain.Stop;
import com.scmtp.routeservice.repository.RouteRepository;
import com.scmtp.routeservice.repository.RouteStopRepository;
import com.scmtp.routeservice.repository.ScheduleRepository;
import com.scmtp.routeservice.repository.StopRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class RouteQueryService {

    private final RouteRepository routeRepository;
    private final StopRepository stopRepository;
    private final RouteStopRepository routeStopRepository;
    private final ScheduleRepository scheduleRepository;

    public RouteQueryService(RouteRepository routeRepository,
                             StopRepository stopRepository,
                             RouteStopRepository routeStopRepository,
                             ScheduleRepository scheduleRepository) {
        this.routeRepository = routeRepository;
        this.stopRepository = stopRepository;
        this.routeStopRepository = routeStopRepository;
        this.scheduleRepository = scheduleRepository;
    }

    public List<Route> getAllRoutes() {
        return routeRepository.findAll();
    }

    public Optional<Route> getRouteById(UUID id) {
        return routeRepository.findById(id);
    }

    public List<Stop> findStopsByLocation(double lat, double lng, double radiusKm) {
        double latDelta = radiusKm / 111.0;
        double lngDelta = radiusKm / (111.0 * Math.cos(Math.toRadians(lat)));

        List<Stop> candidates = stopRepository.findByLatitudeBetweenAndLongitudeBetween(
                lat - latDelta,
                lat + latDelta,
                lng - lngDelta,
                lng + lngDelta
        );

        double earthRadiusKm = 6371.0;
        List<Stop> results = new ArrayList<>();

        for (Stop stop : candidates) {
            double dLat = Math.toRadians(stop.getLatitude() - lat);
            double dLng = Math.toRadians(stop.getLongitude() - lng);
            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                    + Math.cos(Math.toRadians(lat))
                    * Math.cos(Math.toRadians(stop.getLatitude()))
                    * Math.sin(dLng / 2) * Math.sin(dLng / 2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            double distance = earthRadiusKm * c;
            if (distance <= radiusKm) {
                results.add(stop);
            }
        }

        return results;
    }

    public List<Schedule> getNextDepartures(UUID stopId, String fromTimeIso) {
        OffsetDateTime fromTime;
        try {
            fromTime = OffsetDateTime.parse(fromTimeIso);
        } catch (DateTimeParseException e) {
            // Fallback: treat as instant in UTC if missing offset
            fromTime = OffsetDateTime.parse(fromTimeIso + "Z");
        }

        String dayOfWeek = fromTime.getDayOfWeek().name().substring(0, 3).toUpperCase(Locale.ROOT);

        List<RouteStop> routeStops = routeStopRepository.findByStopIdOrderByOrderIndexAsc(stopId);
        if (routeStops.isEmpty()) {
            return List.of();
        }

        List<UUID> routeIds = routeStops.stream()
                .map(rs -> rs.getRoute().getId())
                .distinct()
                .collect(Collectors.toList());

        return scheduleRepository.findUpcomingByRoutesAndDay(routeIds, dayOfWeek, fromTime.withOffsetSameInstant(ZoneOffset.UTC));
    }
}



