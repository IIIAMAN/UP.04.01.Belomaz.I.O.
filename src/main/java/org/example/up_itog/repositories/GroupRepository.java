package org.example.up_itog.repositories;

import org.example.up_itog.models.Groupss;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository extends JpaRepository<Groupss, Long> {
    Groupss findByName(String name);

    boolean existsByName(String name);
}
