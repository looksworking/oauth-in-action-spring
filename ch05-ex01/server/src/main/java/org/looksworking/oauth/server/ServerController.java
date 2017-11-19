package org.looksworking.oauth.server;

import org.looksworking.oauth.server.org.looksworking.oauth.server.model.ClientEntity;
import org.looksworking.oauth.server.org.looksworking.oauth.server.model.ClientRepository;
import org.looksworking.oauth.server.org.looksworking.oauth.server.model.ClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
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
    public String getClients(Model model, HttpServletRequest request, HttpServletResponse response){

        List<ClientEntity> clients = clientRepository.findAll();
        model.addAttribute("clients", clients);
        return "clients";
    }

    @RequestMapping(path = "/authorize", method = RequestMethod.GET)
    public String authorize(@RequestParam String client_id, HttpServletRequest request, HttpServletResponse response){

        logger.info(client_id);

        return "ok";
    }
}
