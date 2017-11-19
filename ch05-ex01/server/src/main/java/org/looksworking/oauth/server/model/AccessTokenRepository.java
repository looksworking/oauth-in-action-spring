package org.looksworking.oauth.server.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AccessTokenRepository extends JpaRepository<AccessTokenEntity, UUID>{
}
