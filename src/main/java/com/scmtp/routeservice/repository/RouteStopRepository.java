package com.scmtp.routeservice.repository;

import com.scmtp.routeservice.domain.RouteStop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface RouteStopRepository extends JpaRepository<RouteStop, UUID> {

    List<RouteStop> findByStopIdOrderByOrderIndexAsc(UUID stopId);
    
    @Query("select rs from RouteStop rs join fetch rs.stop where rs.route.id = :routeId order by rs.orderIndex asc")
    List<RouteStop> findByRouteIdOrderByOrderIndexAsc(@Param("routeId") UUID routeId);
}



