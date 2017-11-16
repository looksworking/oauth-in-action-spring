package org.looksworking.oauth.resource;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.*;

@Controller
public class ResourceController {

    private final Logger logger = LoggerFactory.getLogger(ResourceController.class);

    private final Environment environment;

    @Autowired
    public ResourceController(Environment environment) {
        this.environment = environment;
    }

    @RequestMapping(path = "/resource", method = RequestMethod.GET)
    public ResponseEntity<Object> read(HttpServletRequest request, HttpServletResponse response) throws IOException {

        Map<String, List<String>> produces = new HashMap<>();
        List<String> fruits = new ArrayList<>();
        Collections.addAll(fruits, "apple", "banana", "kiwi");
        List<String> veggies = new ArrayList<>();
        Collections.addAll(veggies, "lettuce", "onion", "potato");
        List<String> meats = new ArrayList<>();
        Collections.addAll(meats, "bacon", "steak", "chicken");

        List<String> tokenScope = null;
        try {
            tokenScope = getTokenScope(request);
        } catch (GeneralSecurityException e) {
            logger.info("Forbidden");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        for (String scope : tokenScope) {
            if (scope.equals("fruits")){
                produces.put("fruits", fruits);
            }
            if (scope.equals("veggies")){
                produces.put("veggies", veggies);
            }
            if (scope.equals("meats")){
                produces.put("meats", meats);
            }
        }

        return new ResponseEntity<>(produces, HttpStatus.OK);
    }


    List<String> getTokenScope(HttpServletRequest request) throws GeneralSecurityException, IOException {

        String header = request.getHeader("Authorization");
        logger.info(header);

        int tokenIndex = header.toLowerCase().lastIndexOf("bearer ") + 7;
        String inToken = header.substring(tokenIndex);
        Path path = Paths.get(environment.getProperty("database.location"));
        Gson gson = new Gson();
        List<DbRecord> database = new ArrayList<>();

        try (Scanner scanner = new Scanner(path)) {
            while (scanner.hasNext()) {
                database.add(gson.fromJson(scanner.nextLine(), DbRecord.class));
            }
        }

        for (DbRecord dbRecord : database) {
            if (inToken.equals(dbRecord.getAccess_token())){
                logger.info("Found access token: {}", dbRecord.getAccess_token());
                return dbRecord.getScope();
            }
        }
        throw new GeneralSecurityException();
    }
}
