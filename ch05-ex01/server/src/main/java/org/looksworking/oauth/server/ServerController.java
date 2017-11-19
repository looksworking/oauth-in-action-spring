package org.looksworking.oauth.server;

import org.looksworking.oauth.server.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Base64;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;

@Controller
public class ServerController {

    private final Logger logger = LoggerFactory.getLogger(ServerController.class);

    @Autowired
    ClientRepository clientRepository;

    @Autowired
    ClientService clientService;

    @Autowired
    RequestRepository requestRepository;

    @Autowired
    AccessTokenRepository accessTokenRepository;

    @Autowired
    CodeRepository codeRepository;

    @PostConstruct
    public void init() {
        clientRepository.save(clientService.getClientList());
    }

    @RequestMapping(path = "/clients", method = RequestMethod.GET)
    public String getClients(Model model, HttpServletRequest request, HttpServletResponse response) {

        List<ClientEntity> clients = clientRepository.findAll();
        model.addAttribute("clients", clients);
        return "clients";
    }

    @RequestMapping(path = "/authorize", method = RequestMethod.GET)
    public String authorize(@RequestParam String client_id, HttpServletRequest request, HttpServletResponse response,
                            Model model, @RequestParam String redirect_uri, @RequestParam String response_type,
                            @RequestParam String scope, @RequestParam String state) {


        if (client_id == null) {
            return forbid(response);
        }
        ClientEntity clientEntity = clientRepository.findClientEntityByClientId(client_id);
        if (clientEntity == null) {
            return forbid(response);
        }
        if (!clientEntity.getRedirectUris().contains(redirect_uri)) {
            return forbid(response);
        }

        if (!response_type.equals("code")) {
            forbid(response);
        }

        RequestEntity requestEntity = new RequestEntity();
        requestEntity.setId(UUID.randomUUID());
        requestEntity.setClientId(client_id);
        requestEntity.setRedirectUri(redirect_uri);
        requestEntity.setResponseType(response_type);
        requestEntity.setScope(scope);
        requestEntity.setState(state);
        requestRepository.save(requestEntity);

        model.addAttribute("request", requestEntity);

        return "approve";
    }

    @RequestMapping(path = "/approve", method = RequestMethod.POST, params = "approve")
    public String approve(HttpServletRequest request, HttpServletResponse response, Model model,
                          @ModelAttribute("approval") ApprovalFrom approvalFrom) {

        if (approvalFrom.getId() == null) {
            return forbid(response);
        }

        RequestEntity requestEntity = requestRepository.findOne(UUID.fromString(approvalFrom.getId()));

        if (requestEntity == null) {
            return forbid(response);
        }

        CodeEntity codeEntity = new CodeEntity();
        codeEntity.setId(UUID.randomUUID());
        codeEntity.setState(requestEntity.getState());
        codeRepository.save(codeEntity);

        logger.info("reqid: {}", approvalFrom.getId());

        return "redirect:" + requestEntity.getRedirectUri() + "?state=" + requestEntity.getState()
                + "&code=" + codeEntity.getId().toString();
    }

    @RequestMapping(path = "/approve", params = "deny", method = RequestMethod.POST)
    public String deny(@ModelAttribute("approval") ApprovalFrom approvalFrom, HttpServletResponse response) {
        RequestEntity requestEntity = requestRepository.findOne(UUID.fromString(approvalFrom.getId()));

        return "redirect:" + requestEntity.getRedirectUri() + "?error=access_denied";
    }

    @RequestMapping(path = "/token", method = RequestMethod.POST)
    public ResponseEntity<Object> token(HttpServletRequest request, HttpServletResponse response) {

        String encodedCredentials = request.getHeader("Authorization").substring(6);
        if (encodedCredentials == null) {
//            return forbid(response);
        }
        String decodedCredentials = new String(Base64.getDecoder().decode(encodedCredentials));
        String clientId = decodedCredentials.substring(0, decodedCredentials.indexOf(":"));
        String clientSecret = decodedCredentials.substring(decodedCredentials.indexOf(":") + 1);
        if (decodedCredentials == null || clientId == null || clientSecret == null) {
//            return forbid(response);
        }
        logger.info("log: {} pass: {}", clientId, clientSecret);

        ClientEntity clientEntity = clientRepository.findClientEntityByClientId(clientId);
        if (clientEntity == null || !clientEntity.getClientSecret().equals(clientSecret)) {
//            forbid(response);
        }

        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            logger.info(parameterNames.nextElement());
        }

        String grant_type = request.getParameter("grant_type");
        String code = request.getParameter("code");
        if (grant_type == null || grant_type.equals("authorization_code") || code == null) {
            forbid(response);
        }

        CodeEntity codeEntity = codeRepository.findOne(UUID.fromString(code));

        if (codeEntity != null) {
            codeRepository.delete(codeEntity);
        } else {
            forbid(response);
        }
        logger.info("clientId: {}", clientId);
        AccessTokenEntity accessTokenEntity = new AccessTokenEntity(clientId);
        accessTokenRepository.save(accessTokenEntity);
        logger.info("Returning access token: {}", accessTokenEntity.toString());
        return new ResponseEntity<>(accessTokenEntity, HttpStatus.OK);
    }

    private String forbid(HttpServletResponse response) {
        response.setStatus(400);
        return "error";
    }

    protected ResponseEntity<Object> forbid() {
        return new ResponseEntity<Object>("FORBIDDEN", HttpStatus.FORBIDDEN);
    }
}
