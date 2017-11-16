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
    public ResponseEntity<Object> write(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String scope = "write";

        if (!isAccessAllowed(request, scope)){
            return error(scope);
        }

        logger.info("Successful request");
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    @RequestMapping(path = "/resource", method = RequestMethod.DELETE)
    public ResponseEntity<Object> delete(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String scope = "delete";

        if (!isAccessAllowed(request, scope)){
            return error(scope);
        }

        logger.info("Successful request");
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    @RequestMapping(path = "/resource", method = RequestMethod.GET)
    public ResponseEntity<Object> read(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String scope = "read";

        if (!isAccessAllowed(request, scope)){
            return error(scope);
        }

        Map<String, String> resp = new HashMap<>();

        logger.info("Successful request");
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    private boolean isAccessAllowed(HttpServletRequest request, String scope) throws IOException {
        boolean accessAllowed = false;

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
                if (dbRecord.getScope().contains(scope)){
                    accessAllowed = true;
                } else {
                    logger.info("Requested action is out of the token scope");
                }
            }
        }
        return accessAllowed;
    }

    private ResponseEntity<Object> error(String scope){
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("WWW-Authenticate", "Bearer realm=localhost:8080, " +
                "error=insufficient_scope, scope="+ scope );
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .headers(httpHeaders).build();

    }
}
