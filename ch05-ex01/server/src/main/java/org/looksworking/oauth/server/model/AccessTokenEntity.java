package org.looksworking.oauth.server.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.UUID;

@Entity
public class AccessTokenEntity {

    private final String token_type = "Bearer";

    @Id
    private UUID access_token;

    private String clientId;

    public AccessTokenEntity() {
        this.access_token = UUID.randomUUID();
    }

    public AccessTokenEntity(String clientId) {
        this();
        this.clientId = clientId;
    }

    public UUID getAccess_token() {
        return access_token;
    }

    public void setAccess_token(UUID access_token) {
        this.access_token = access_token;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getToken_type() {
        return token_type;
    }

    @Override
    public String toString() {
        return "AccessTokenEntity{" +
                "access_token=" + access_token +
                ", clientId='" + clientId + '\'' +
                '}';
    }
}
