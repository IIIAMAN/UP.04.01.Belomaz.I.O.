package org.example.up_itog.repositories;

import org.example.up_itog.models.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface ClientCourseRepository extends JpaRepository<ClientCourse, Long> {
    List<ClientCourse> findByClient(Client client);

    @Query("SELECT cc.course FROM ClientCourse cc WHERE cc.client.id = :clientId")
    List<Course> findCoursesByClientId(Long clientId);

    boolean existsByClientIdAndCourseId(Long clientId, Long courseId);

    boolean existsByClientAndCourse(Client client, Course course);
}