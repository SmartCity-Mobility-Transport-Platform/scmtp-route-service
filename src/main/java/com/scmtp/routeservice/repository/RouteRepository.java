package com.scmtp.routeservice.repository;

import com.scmtp.routeservice.domain.Route;
import com.scmtp.routeservice.domain.RouteProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface RouteRepository extends JpaRepository<Route, UUID> {
    
    // Use native query to fetch only route fields without collections
    @Query(value = "SELECT id, name FROM routes", nativeQuery = true)
    List<Object[]> findAllRoutesBasic();
    
    // Find route by ID without fetching collections - use native query to avoid Hibernate issues
    @Query(value = "SELECT id, name FROM routes WHERE id = :id", nativeQuery = true)
    Object[] findByIdBasic(@Param("id") UUID id);
}



