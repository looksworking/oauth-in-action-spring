package org.looksworking.oauth.resource;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
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
import java.util.*;

@Controller
public class ResourceController {

    private final Logger logger = LoggerFactory.getLogger(ResourceController.class);

    private final Environment environment;

    @Autowired
    public ResourceController(Environment environment) {
        this.environment = environment;
    }

    @RequestMapping(path = "/resource", method = RequestMethod.POST)
    public ResponseEntity<Object> resource(HttpServletRequest request, HttpServletResponse response) throws URISyntaxException, IOException {
        Map<String, String> resource = new HashMap<>();
        resource.put("name", "Protected Resource");
        resource.put("description", "This data is protected by OAuth2.0");

        String header = request.getHeader("Authorization");
        logger.info(header);
        int tokenIndex = header.toLowerCase().lastIndexOf("bearer ") + 7;
        String inToken = header.substring(tokenIndex);


        Path path = Paths.get(environment.getProperty("database.location"));

        Gson gson = new Gson();

        List<String> tokens = new ArrayList<>();
        List<DbRecord> database = new ArrayList<>();
        try (Scanner scanner = new Scanner(path)) {
            while (scanner.hasNext()) {
                database.add(gson.fromJson(scanner.nextLine(), DbRecord.class));
            }
        }

        for (DbRecord dbRecord : database) {
            if (inToken.equals(dbRecord.getAccess_token())){
                logger.info("Found access token: {}", dbRecord.getAccess_token());
                return new ResponseEntity<>(resource, HttpStatus.OK);
            }
        }
        logger.info("Token not found in database");
        return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
    }

}
