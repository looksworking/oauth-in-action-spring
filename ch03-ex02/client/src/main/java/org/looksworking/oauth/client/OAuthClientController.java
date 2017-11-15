package org.looksworking.oauth.client;

import com.google.gson.Gson;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
public class OAuthClientController {

    private final Logger logger = LoggerFactory.getLogger(OAuthClientController.class);
    private final Environment environment;

    @Autowired
    private ModelVars modelVars;

    @Autowired
    public OAuthClientController(Environment environment) {
        this.environment = environment;
    }

    @RequestMapping(path = "/", method = RequestMethod.GET)
    public String index(HttpServletRequest request, HttpServletResponse response, Model model) {

        fillModel(model);
        return "index";
    }

    @RequestMapping(path = "/authorize", method = RequestMethod.GET)
    public String authorize(Model model) throws URISyntaxException {

        modelVars.setState(UUID.randomUUID().toString());
        logger.info("Set state to {}", modelVars.getState());
        URIBuilder uriBuilder = new URIBuilder(environment.getProperty("authServer.authorizationEndpoint"));
        String redirectUrl = uriBuilder.addParameter("response_type", "code")
                .addParameter("client_id", environment.getProperty("client.client_id"))
                .addParameter("redirect_uri", environment.getProperty("client.redirect_uris"))
                .addParameter("state", modelVars.getState()).build().toString();

        return "redirect:" + redirectUrl;
    }

    @RequestMapping(path = "/callback", method = RequestMethod.GET)
    public String callback(HttpServletRequest request, Model model, HttpServletResponse response)
            throws AuthenticationException, IOException, URISyntaxException {

        String callbackState = request.getParameter("state");
        logger.info("Received state: {}", callbackState);
        if (this.modelVars.getState() == null || !this.modelVars.getState().equals(callbackState)) {
            throw new SecurityException();
        }

        String code = request.getParameter("code");
        logger.info("Received code: {}", code);
        this.modelVars.setCode(code);

        HttpPost httpPost = getHttpPostForGrantType("authorization_code");
        CloseableHttpClient client = HttpClients.createDefault();

        logger.info("Sending form data: {}", httpPost.toString());

        CloseableHttpResponse tokenResponse = client.execute(httpPost);

        logger.info("Got response status: {}", tokenResponse.getStatusLine().getStatusCode());

        TokenResponseJson tokenResponseJson = getTokenResponseJson(tokenResponse);

        fillVars(tokenResponseJson);
        fillModel(model);
        return "index";
    }

    @RequestMapping(path = "/fetch_resource", method = RequestMethod.GET)
    public String fetch(Model model) throws URISyntaxException, IOException, AuthenticationException {

        URI resourceUrl = new URIBuilder(environment.getProperty("protectedResource")).build();
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(resourceUrl);
        httpPost.addHeader("Authorization", "Bearer " + this.modelVars.getToken());

        CloseableHttpResponse resourceResponse = client.execute(httpPost);

        int status = resourceResponse.getStatusLine().getStatusCode();

        logger.info("Resource status code: {}", status);

        String responseBody = null;
        if (status >= 200 && status < 300) {
            InputStream inputStream = resourceResponse.getEntity().getContent();


            try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
                responseBody = br.lines().collect(Collectors.joining("\n"));
            }
            this.modelVars.setResource(responseBody);

            fillModel(model);

        } else {
            this.modelVars.setToken(null);
            if (this.modelVars.getRefreshToken() != null) {
                TokenResponseJson tokenResponseJson = refreshToken();
                fillVars(tokenResponseJson);
                fillModel(model);
                return "redirect:/fetch_resource";
            }
        }

        return "index";
    }

    private void fillVars(TokenResponseJson tokenResponseJson) {
        this.modelVars.setToken(tokenResponseJson.getAccess_token());
        this.modelVars.setScope(tokenResponseJson.getScope());
        this.modelVars.setTokenType(tokenResponseJson.getToken_type());
        this.modelVars.setRefreshToken(tokenResponseJson.getRefresh_token());
    }

    private TokenResponseJson getTokenResponseJson(CloseableHttpResponse tokenResponse) throws IOException {
        InputStream inputStream = tokenResponse.getEntity().getContent();

        String responseBody;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            responseBody = br.lines().collect(Collectors.joining("\n"));
        }
        logger.info("Got response body: {}", responseBody);
        Gson gson = new Gson();
        TokenResponseJson tokenResponseJson = gson.fromJson(responseBody, TokenResponseJson.class);

        return tokenResponseJson;
    }

    private TokenResponseJson refreshToken() throws AuthenticationException, IOException, URISyntaxException {
        HttpPost httpPost = getHttpPostForGrantType("refresh_token");
        CloseableHttpClient client = HttpClients.createDefault();
        CloseableHttpResponse resourceResponse = client.execute(httpPost);
        TokenResponseJson tokenResponseJson = getTokenResponseJson(resourceResponse);
        return tokenResponseJson;
    }

    private HttpPost getHttpPostForGrantType(String grantType) throws URISyntaxException, AuthenticationException, UnsupportedEncodingException {
        URIBuilder postBuilder = new URIBuilder(environment.getProperty("authServer.tokenEndpoint"));
        HttpPost httpPost = new HttpPost(postBuilder.build());
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(
                environment.getProperty("client.client_id"),
                environment.getProperty("client.client_secret"));
        httpPost.addHeader(new BasicScheme().authenticate(credentials, httpPost, null));

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("grant_type", grantType));
        params.add(new BasicNameValuePair("code", this.modelVars.getCode()));
        params.add(new BasicNameValuePair("redirect_uri", environment.getProperty("client.redirect_uris")));
        httpPost.setEntity(new UrlEncodedFormEntity(params));
        return httpPost;
    }

    private void fillModel(Model model) {
        model.addAttribute("token", modelVars.getToken() == null ? "empty" : modelVars.getToken());
        model.addAttribute("scope", modelVars.getScope() == null ? "empty" : modelVars.getScope());
        model.addAttribute("state", modelVars.getState() == null ? "empty" : modelVars.getState());
        model.addAttribute("code", modelVars.getCode() == null ? "empty" : modelVars.getCode());
        model.addAttribute("resource", modelVars.getResource() == null ? "empty" : modelVars.getResource());
        model.addAttribute("tokenType", modelVars.getTokenType() == null ? "empty" : modelVars.getTokenType());
        model.addAttribute("refreshToken", modelVars.getRefreshToken() == null ? "empty" : modelVars.getRefreshToken());
    }

}
