package com.scmtp.routeservice.repository;

import com.scmtp.routeservice.domain.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface ScheduleRepository extends JpaRepository<Schedule, UUID> {

    @Query("select s from Schedule s " +
            "where s.route.id in :routeIds " +
            "and :dayOfWeek member of s.daysOfWeek " +
            "and s.departureTime >= :fromTime " +
            "order by s.departureTime asc")
    List<Schedule> findUpcomingByRoutesAndDay(
            @Param("routeIds") List<UUID> routeIds,
            @Param("dayOfWeek") String dayOfWeek,
            @Param("fromTime") OffsetDateTime fromTime
    );
}



