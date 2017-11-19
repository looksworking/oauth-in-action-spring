package org.looksworking.oauth.server.org.looksworking.oauth.server.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepository extends JpaRepository<ClientEntity, Long> {
    ClientEntity findClientEntityByClientId(String clientId);
}
