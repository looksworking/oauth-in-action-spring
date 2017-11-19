package org.looksworking.oauth.server.org.looksworking.oauth.server.model;

import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.UUID;

@Entity
@Transactional
public class CodeEntity {

    @Id
    private UUID id;
    private String state;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
