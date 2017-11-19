package org.looksworking.oauth.server.org.looksworking.oauth.server.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RequestRepository extends JpaRepository<RequestEntity, UUID> {
}
