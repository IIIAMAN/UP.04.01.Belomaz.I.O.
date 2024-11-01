package org.example.up_itog.repositories;

import org.example.up_itog.models.Client;
import org.example.up_itog.models.UserTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    Client findByUserTable(UserTable userTable);
}

