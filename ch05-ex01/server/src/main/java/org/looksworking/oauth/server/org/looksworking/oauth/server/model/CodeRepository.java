package org.looksworking.oauth.server.org.looksworking.oauth.server.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Transactional
public interface CodeRepository extends JpaRepository<CodeEntity, UUID>{
}
