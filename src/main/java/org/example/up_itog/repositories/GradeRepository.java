package org.example.up_itog.repositories;

import org.example.up_itog.models.Client;
import org.example.up_itog.models.Course;
import org.example.up_itog.models.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {
    List<Grade> findByCourse(Course course);

    List<Grade> findByStudent(Client client);

    boolean existsByStudentAndCourse(Client client, Course course);

    boolean existsByStudentAndCourseAndIdNot(Client student, Course course, Long id);

    List<Grade> findByCourseId(Long courseId);

    @Modifying
    @Query("DELETE FROM Grade g WHERE g.student.id = :studentId")
    void deleteByStudentId(@Param("studentId") Long studentId);
}

