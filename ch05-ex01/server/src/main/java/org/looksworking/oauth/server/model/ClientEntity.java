package org.looksworking.oauth.server.model;

import javax.persistence.*;
import java.util.List;

@Entity
public class ClientEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String clientId;

    private String clientSecret;

    @ElementCollection
    @CollectionTable(name="redirectUris", joinColumns=@JoinColumn(name="id"))
    @Column(name="redirectUri")
    private List<String> redirectUris;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public List<String> getRedirectUris() {
        return redirectUris;
    }

    public void setRedirectUris(List<String> redirectUris) {
        this.redirectUris = redirectUris;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientEntity that = (ClientEntity) o;

        return clientId != null ? clientId.equals(that.clientId) : that.clientId == null;
    }

    @Override
    public int hashCode() {
        return clientId != null ? clientId.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "ClientEntity{" +
                "id=" + id +
                ", clientId='" + clientId + '\'' +
                ", clientSecret='" + clientSecret + '\'' +
                ", redirectUris=" + redirectUris +
                '}';
    }
}
