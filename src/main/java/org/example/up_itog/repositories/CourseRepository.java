package org.example.up_itog.repositories;

import org.example.up_itog.models.Course;
import org.example.up_itog.models.UserTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, Long id);
}
