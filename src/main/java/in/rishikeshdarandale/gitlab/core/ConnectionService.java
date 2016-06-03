/*
 *  The MIT License (MIT)
 *  Copyright (c) 2016 Rishikesh Darandale
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 *  documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 *  the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 *  and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all copies or substantial portions
 *  of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 *  TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 *  THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 *  OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 *  DEALINGS IN THE SOFTWARE.
 */
package in.rishikeshdarandale.gitlab.core;

import com.google.common.base.Strings;

import in.rishikeshdarandale.gitlab.model.Session;
import org.glassfish.jersey.jackson.JacksonFeature;

import javax.ws.rs.client.*;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author Rishikesh Darandale
 */
public class ConnectionService {
    private static ConnectionService service;
    private Client client;
    private String privateToken;

    private ConnectionService(Client client) {
        this.client = client;
    }

    public static synchronized ConnectionService getInstance() {
        if(service == null) {
            service = new ConnectionService(ClientBuilder.newClient().register(JacksonFeature.class));
        }
        return service;
    }

    /**
     * Login to Gitlab using username and password to get the private token
     *
     * @param apiUrl
     * @param username
     * @param password
     * @return
     * @throws AuthenticationException
     */
    public Session createSession(String apiUrl, String username, String password) throws AuthenticationException {
        Session session = null;
        if(Strings.isNullOrEmpty(apiUrl) || Strings.isNullOrEmpty(username)
            || Strings.isNullOrEmpty(password) ) {
            throw new IllegalArgumentException("One of the connection parameter is not provided");
        }
        WebTarget webTarget = client.target(apiUrl).path(Constants.SESSION_API_PATH);

        Form form = new Form();
        form.param("login", username);
        form.param("password", password);

        Invocation.Builder invocationBuilder =  webTarget.request().accept(MediaType.APPLICATION_JSON);
        Response response
                = invocationBuilder.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED));
        int statusCode = response.getStatus();
        System.out.print("Response: " + statusCode);
        if(statusCode == Response.Status.CREATED.getStatusCode()) {
            session = (Session) response.readEntity(Session.class);
            privateToken = session.getPrivateToken();
        } else {
            throw new AuthenticationException("Invalid username or password provided.");
        }
        return session;
    }

    /**
     * Login to Gitlab using username and password to get the private token
     *
     * This uses the default gitlab api url
     *
     * @param username
     * @param password
     * @return
     * @throws AuthenticationException
     */
    public Session createSession(String username, String password) throws AuthenticationException {
        return this.createSession(Constants.GITLAB_API_URL, username, password);
    }

    /**
     *
     * @param apiUrl
     * @param <T>
     *
     * @return a
     */
    public <T> T get(String apiUrl, Class<T> zClass) {
        if(!Strings.isNullOrEmpty(privateToken)) {
            WebTarget webTarget = client.target(apiUrl).path(Constants.SESSION_API_PATH);
            Invocation.Builder invocationBuilder =  webTarget.request()
                    .accept(MediaType.APPLICATION_JSON)
                    .header(Constants.PRIVATE_TOKEN_HEADER, privateToken);
            Response response = invocationBuilder.get();
            System.out.print("Response: " + response.getStatus());
            if(response.getStatus() == Response.Status.OK.getStatusCode()) {
                return response.readEntity(zClass);
            }
        }
        return null;
    }
}