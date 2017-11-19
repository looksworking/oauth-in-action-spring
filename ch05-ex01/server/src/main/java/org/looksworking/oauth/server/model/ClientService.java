package org.looksworking.oauth.server.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Service
@ConfigurationProperties(prefix = "clients")
public class ClientService {

    @Autowired
    ClientRepository clientRepository;

    private List<ClientEntity> clientList = new ArrayList<>();

    @PostConstruct
    public void init() {
        for(ClientEntity entity: this.getClientList()) {

        }
    }
    public List<ClientEntity> getClientList() {
        return clientList;
    }

    public void setClientList(List<ClientEntity> clientList) {
        this.clientList = clientList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientService that = (ClientService) o;

        return clientList != null ? clientList.equals(that.clientList) : that.clientList == null;
    }


    @Override
    public int hashCode() {
        return clientList != null ? clientList.hashCode() : 0;
    }
}
