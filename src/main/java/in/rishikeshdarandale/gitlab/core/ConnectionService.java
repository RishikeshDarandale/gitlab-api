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

import com.sun.corba.se.pept.transport.Connection;
import in.rishikeshdarandale.gitlab.model.Session;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.*;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 *
 * @author Rishikesh Darandale
 */
public class ConnectionService {
    private static final Logger LOG = LoggerFactory.getLogger(ConnectionService.class);
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
     * @param apiUrlPrefix
     * @param username
     * @param password
     * @return
     * @throws AuthenticationException
     */
    public Session createSession(String apiUrlPrefix, String username, String password) throws AuthenticationException {
        LOG.info("Creating a new session for {}", username);
        Session session = null;
        if(Strings.isNullOrEmpty(apiUrlPrefix) || Strings.isNullOrEmpty(username)
            || Strings.isNullOrEmpty(password) ) {
            LOG.error("Incorrect parameters are passed.");
            throw new IllegalArgumentException("One of the connection parameter is not provided");
        }
        WebTarget webTarget = this.getClient().target(apiUrlPrefix).path(Constants.SESSION_API_PATH);

        Form form = new Form();
        form.param("login", username);
        form.param("password", password);

        Invocation.Builder invocationBuilder =  webTarget.request().accept(MediaType.APPLICATION_JSON);
        Response response
                = invocationBuilder.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED));
        int statusCode = response.getStatus();
        LOG.debug("Response: " + statusCode);
        if(statusCode == Response.Status.CREATED.getStatusCode()) {
            LOG.info("Session successfully created for {}", username);
            session = response.readEntity(Session.class);
            privateToken = session.getPrivateToken();
        } else {
            LOG.error("Invalid username or password provided");
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
     * @param apiUrlPrefix
     * @param <T>
     *
     * @return a
     */
    public <T> List<T> get(String apiUrlPrefix, String apiPath, Class<T> zClass) {
        if(!Strings.isNullOrEmpty(privateToken)) {
            WebTarget webTarget = this.getClient().target(apiUrlPrefix).path(apiPath);
            Invocation.Builder invocationBuilder =  webTarget.request()
                    .accept(MediaType.APPLICATION_JSON)
                    .header(Constants.PRIVATE_TOKEN_HEADER, privateToken);
            Response response = invocationBuilder.get();
            System.out.print("Response: " + response.getStatus());
            if(response.getStatus() == Response.Status.OK.getStatusCode()) {
                return response.readEntity(getType(zClass));
            }
        }
        return null;
    }

    public <T> List<T> get(String apiPath, Class<T> zClass) {
        return this.get(Constants.GITLAB_API_URL, apiPath, zClass);
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    private <T> GenericType<List<T>> getType(final Class<T> clazz) {
        ParameterizedType genericType = new ParameterizedType() {
            public Type[] getActualTypeArguments() {
                return new Type[]{clazz};
            }

            public Type getRawType() {
                return List.class;
            }

            public Type getOwnerType() {
                return List.class;
            }
        };
        return new GenericType<List<T>>(genericType) {
        };
    }
}