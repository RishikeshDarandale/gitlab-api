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

import in.rishikeshdarandale.gitlab.model.PaginatedList;
import in.rishikeshdarandale.gitlab.model.Session;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.*;
import javax.ws.rs.core.*;
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
            Client client = ClientBuilder.newClient().register(JacksonFeature.class);
            client.property(ClientProperties.CONNECT_TIMEOUT, 5000);
            client.property(ClientProperties.READ_TIMEOUT, 5000);
            service = new ConnectionService(client);
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

        Form form = new Form();
        form.param("login", username);
        form.param("password", password);

        Response response = doPostRequest(apiUrlPrefix, Constants.SESSION_API_PATH,
                form, MediaType.APPLICATION_FORM_URLENCODED, null, MediaType.APPLICATION_JSON);
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

    public <T> T getObject(String sudoUserName, String apiUrlPrefix,
                           String apiPath, Class<T> zClass,
                           MultivaluedMap<String, Object> queryParams) throws AuthenticationException{
        if(!Strings.isNullOrEmpty(privateToken)) {
            MultivaluedMap headers = new MultivaluedHashMap<>();
            headers.add(Constants.PRIVATE_TOKEN_HEADER, privateToken);
            if (!Strings.isNullOrEmpty(sudoUserName)) {
                headers.add(Constants.SUDO_HEADER, sudoUserName);
            }
            Response response = doGetRequest(apiUrlPrefix, apiPath, queryParams, headers, MediaType.APPLICATION_JSON);
            System.out.print("Response: " + response.getStatus());
            if(response.getStatus() == Response.Status.OK.getStatusCode()) {
                return response.readEntity(zClass);
            } else if(response.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
                throw new NotFoundException();
            } else if(response.getStatus() == Response.Status.UNAUTHORIZED.getStatusCode()) {
                throw new AuthenticationException("Please use valid token");
            }
        }
        return null;
    }

    public <T> T getObject(String sudoUserName, String apiPath,
                           Class<T> zClass, MultivaluedMap<String, Object> queryParams) throws AuthenticationException {
        return this.getObject(sudoUserName, Constants.GITLAB_API_URL, apiPath, zClass, queryParams);
    }

    /**
     *
     * @param apiUrlPrefix
     * @param <T>
     *
     * @return a
     */
    public <T> PaginatedList<T> getList(String sudoUserName, String apiUrlPrefix,
                                        String apiPath, Class<T> zClass,
                                        MultivaluedMap<String, Object> queryParams) throws AuthenticationException{
        if(!Strings.isNullOrEmpty(this.getPrivateToken())) {
            MultivaluedMap headers = new MultivaluedHashMap<>();
            headers.add(Constants.PRIVATE_TOKEN_HEADER, privateToken);
            if (!Strings.isNullOrEmpty(sudoUserName)) {
                headers.add(Constants.SUDO_HEADER, sudoUserName);
            }
            Response response = doGetRequest(apiUrlPrefix, apiPath, queryParams, headers, MediaType.APPLICATION_JSON);
            System.out.print("Response: " + response.getStatus());
            if(response.getStatus() == Response.Status.OK.getStatusCode()) {
                List<T> tList = response.readEntity(getType(zClass));
                return getPaginatedList(tList, response);
            } else if(response.getStatus() == Response.Status.UNAUTHORIZED.getStatusCode()) {
                throw new AuthenticationException("Please use valid token");
            }
        }
        return null;
    }

    public <T> PaginatedList<T> getList(String sudoUserName, String apiPath, Class<T> zClass,
                                        MultivaluedMap<String, Object> queryParams) throws AuthenticationException {
        return this.getList(sudoUserName, Constants.GITLAB_API_URL, apiPath, zClass, queryParams);
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public String getPrivateToken() {
        return privateToken;
    }

    public void setPrivateToken(String privateToken) {
        this.privateToken = privateToken;
    }

    private <T> Response doPostRequest(String apiUrlPrefix, String apiPath,
                                       T entity, String postEntityMediaType,
                                       MultivaluedMap<String, Object> headers, String ... acceptMediaTypes) {
        WebTarget webTarget = this.getClient().target(apiUrlPrefix).path(apiPath);
        Invocation.Builder invocationBuilder =  webTarget.request().accept(acceptMediaTypes).headers(headers);
        return invocationBuilder.post(Entity.entity(entity, postEntityMediaType));
    }

    private <T> PaginatedList<T> getPaginatedList(List<T> t, Response response) {
        return new PaginatedList<T>(t, convertToZeroIfNullOrEmpty(response.getHeaderString(Constants.X_TOTAL)),
                convertToZeroIfNullOrEmpty(response.getHeaderString(Constants.X_TOTAL_PAGES)),
                convertToZeroIfNullOrEmpty(response.getHeaderString(Constants.X_PER_PAGE)),
                convertToZeroIfNullOrEmpty(response.getHeaderString(Constants.X_PAGE)),
                convertToZeroIfNullOrEmpty(response.getHeaderString(Constants.X_PREVIOUS_PAGE)),
                convertToZeroIfNullOrEmpty(response.getHeaderString(Constants.X_NEXT_PAGE)));
    }

    private Integer convertToZeroIfNullOrEmpty(String value) {
        return Integer.parseInt(
                Strings.padStart(Strings.nullToEmpty(value), 1, '0'));
    }

    private Response doGetRequest(String apiUrlPrefix, String apiPath,
                                  MultivaluedMap<String, Object> queryParams,
                                  MultivaluedMap<String, Object> headers, String ... mediaTypes) {
        WebTarget webTarget = this.getClient().target(apiUrlPrefix).path(apiPath);
        if (queryParams!= null && queryParams.keySet().size() > 0 ) {
            for(String key : queryParams.keySet()) {
                webTarget = webTarget.queryParam(key, queryParams.get(key));
            }
        }
        Invocation.Builder invocationBuilder =  webTarget.request()
                .accept(mediaTypes)
                .headers(headers);
        return invocationBuilder.get();
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