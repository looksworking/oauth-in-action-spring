package org.looksworking.oauth.server;

import org.looksworking.oauth.server.org.looksworking.oauth.server.model.ClientEntity;
import org.looksworking.oauth.server.org.looksworking.oauth.server.model.ClientRepository;
import org.looksworking.oauth.server.org.looksworking.oauth.server.model.ClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.UUID;

@Controller
public class ServerController {

    private final Logger logger = LoggerFactory.getLogger(ServerController.class);

    @Autowired
    ClientRepository clientRepository;

    @Autowired
    ClientService clientService;

    @PostConstruct
    public void init(){
        clientRepository.save(clientService.getClientList());
    }

    @RequestMapping(path = "/clients", method = RequestMethod.GET)
    public String getClients(){


        logger.info(String.valueOf(clientService.getClientList().size()));


        return "ok";
    }
}
