package com.scmtp.routeservice.repository;

import com.scmtp.routeservice.domain.RouteStop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RouteStopRepository extends JpaRepository<RouteStop, UUID> {

    List<RouteStop> findByStopIdOrderByOrderIndexAsc(UUID stopId);
}



