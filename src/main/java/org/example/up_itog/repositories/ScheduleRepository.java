package org.example.up_itog.repositories;

import org.example.up_itog.models.Client;
import org.example.up_itog.models.Course;
import org.example.up_itog.models.Room;
import org.example.up_itog.models.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByCourse(Course course);

    List<Schedule> findByCourseIn(List<Course> courses);

    boolean existsByDateTimeAndCourseIdAndRoomId(LocalDateTime dateTime, Long courseId, Long roomId);

    boolean existsByDateTimeAndCourseAndRoom(LocalDateTime dateTime, Course course, Room room);

    List<Schedule> findByRoomId(Long roomId);
}
