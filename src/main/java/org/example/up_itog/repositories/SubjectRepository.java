package org.example.up_itog.repositories;

import org.example.up_itog.models.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {
    boolean existsByNameIgnoreCase(String name);
}

