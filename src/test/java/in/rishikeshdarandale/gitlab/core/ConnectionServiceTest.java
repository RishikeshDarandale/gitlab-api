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

import in.rishikeshdarandale.gitlab.model.PaginatedList;
import in.rishikeshdarandale.gitlab.model.Project;
import in.rishikeshdarandale.gitlab.model.Session;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ConnectionService test class
 *
 * @author Rishikesh Darandale
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(ClientBuilder.class)
public class ConnectionServiceTest {
    @Mock private Client mockClient;
    @Mock private Response mockResponse;
    @Mock private Invocation.Builder mockBuilder;
    @Mock private WebTarget mockWebTarget;
    private ConnectionService connectionService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(mockBuilder.accept(Matchers.<String>anyVararg())).thenReturn(mockBuilder);
        Mockito.when(mockBuilder.headers(Matchers.any())).thenReturn(mockBuilder);
        Mockito.when(mockBuilder.get()).thenReturn(this.mockResponse);
        Mockito.when(mockBuilder.post(Matchers.any())).thenReturn(this.mockResponse);
        Mockito.when(mockBuilder.put(Matchers.any())).thenReturn(this.mockResponse);
        Mockito.when(mockBuilder.delete()).thenReturn(this.mockResponse);

        Mockito.when(mockWebTarget.path(Matchers.anyString())).thenReturn(mockWebTarget);
        Mockito.when(mockWebTarget.request()).thenReturn(mockBuilder);

        Mockito.when(this.mockClient.target(Matchers.anyString())).thenReturn(mockWebTarget);
        Mockito.when(this.mockClient.register(Matchers.any())).thenReturn(this.mockClient);

        PowerMockito.mockStatic(ClientBuilder.class);
        PowerMockito.when(ClientBuilder.newClient()).thenReturn(this.mockClient);

        connectionService = ConnectionService.getInstance();
        connectionService.setClient(mockClient);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateSessionWithBlankParams() throws AuthenticationException{
        connectionService.createSession("", "");
    }

    @Test(expected = AuthenticationException.class)
    public void testCreateSessionWithInvalidCredentials() throws AuthenticationException {
        // Given
        Mockito.when(this.mockResponse.getStatus()).thenReturn(Response.Status.UNAUTHORIZED.getStatusCode());
        // When
        Session session = connectionService.createSession("InValidUser", "InValidPassword");
        // Then
        Assert.assertNull(session);
    }

    @Test
    public void testCreateSession() throws AuthenticationException {
        // Given
        Mockito.when(this.mockResponse.getStatus()).thenReturn(Response.Status.CREATED.getStatusCode());
        Session mockSession = Mockito.mock(Session.class);
        Mockito.when(this.mockResponse.readEntity(Session.class)).thenReturn(mockSession);
        Mockito.when(mockSession.getPrivateToken()).thenReturn("your-private-token");
        // When
        Session session = connectionService.createSession("ValidUser", "ValidPassword");
        // Then
        Assert.assertNotNull(session);
        Assert.assertEquals("your-private-token", session.getPrivateToken());
    }

    @Test
    public void testGetObjectAsNull() throws AuthenticationException {
        // Given
        // Session is not created
        connectionService.setPrivateToken(null);
        // When
        Project project = connectionService.getObject(null, Constants.PROJECTS_API_PATH + "/1234", Project.class, null);
        // Then
        Assert.assertNull(project);
    }

    @Test
    public void testGetObject() throws AuthenticationException {
        // Given
        connectionService.setPrivateToken("Valid-private-token");
        Mockito.when(this.mockResponse.getStatus()).thenReturn(Response.Status.OK.getStatusCode());
        Project mockProject = new Project();
        Mockito.when(this.mockResponse.readEntity(Project.class)).thenReturn(mockProject);
        // When
        Project project = connectionService.getObject(null,
                Constants.PROJECTS_API_PATH + "/1234", Project.class, null);
        // Then
        Assert.assertNotNull(project);
    }

    @Test
    public void testGetObjectAsAdmin() throws AuthenticationException {
        // Given
        connectionService.setPrivateToken("Valid-private-token");
        Mockito.when(this.mockResponse.getStatus()).thenReturn(Response.Status.OK.getStatusCode());
        Project mockProject = new Project();
        Mockito.when(this.mockResponse.readEntity(Project.class)).thenReturn(mockProject);
        // When
        Project project = connectionService.getObject("admin",
                Constants.PROJECTS_API_PATH + "/1234", Project.class, null);
        // Then
        Assert.assertNotNull(project);
    }

    @Test(expected = NotFoundException.class)
    public void testGetObjectWithNotFound() throws AuthenticationException{
        // Given
        connectionService.setPrivateToken("Valid-private-token");
        Mockito.when(this.mockResponse.getStatus()).thenReturn(Response.Status.NOT_FOUND.getStatusCode());
        // When
        connectionService.getObject(null, Constants.PROJECTS_API_PATH + "/1234", Project.class, null);
        // Then exception
    }

    @Test(expected =AuthenticationException.class)
    public void testGetObjectWithUnAuthorized() throws AuthenticationException{
        // Given
        connectionService.setPrivateToken("Valid-private-token");
        Mockito.when(this.mockResponse.getStatus()).thenReturn(Response.Status.UNAUTHORIZED.getStatusCode());
        // When
        connectionService.getObject(null, Constants.PROJECTS_API_PATH + "/1234", Project.class, null);
        // Then exception
    }

    @Test
    public void testGetListAsNull() throws AuthenticationException {
        // Given
        // Session is not created
        connectionService.setPrivateToken(null);
        // When
        PaginatedList<Project> projectList = connectionService.getList(null, Constants.PROJECTS_API_PATH + "/1234",
                Project.class, null);
        // Then
        Assert.assertNull(projectList);
    }

    @Test(expected =AuthenticationException.class)
    public void testGetListWithUnAuthorized() throws AuthenticationException{
        // Given
        connectionService.setPrivateToken("Valid-private-token");
        Mockito.when(this.mockResponse.getStatus()).thenReturn(Response.Status.UNAUTHORIZED.getStatusCode());
        // When
        connectionService.getList(null, Constants.PROJECTS_API_PATH + "/1234", Project.class, null);
        // Then exception
    }

    @Test
    public void testGetList() throws AuthenticationException{
        // Given
        connectionService.setPrivateToken("Valid-private-token");
        Mockito.when(this.mockResponse.getStatus()).thenReturn(Response.Status.OK.getStatusCode());
        Mockito.when(this.mockResponse.getHeaderString(Constants.X_TOTAL)).thenReturn("1");
        Project mockProject = new Project();
        List<Project> projectList = new ArrayList<>();
        projectList.add(mockProject);
        Mockito.when(this.mockResponse.readEntity(Matchers.any(GenericType.class))).thenReturn(projectList);
        // When
        PaginatedList<Project> list = connectionService.getList("", Constants.PROJECTS_API_PATH + "/1234",
                Project.class, null);
        // Then
        Assert.assertNotNull(list);
        Assert.assertEquals(new Integer(1), list.getTotalItems());
    }

    @Test
    public void testGetListWithPaginationAndAdmin() throws AuthenticationException{
        // Given
        connectionService.setPrivateToken("Valid-private-token");
        Mockito.when(this.mockWebTarget.queryParam(Mockito.anyString(), Mockito.anyObject())).thenReturn(mockWebTarget);
        Mockito.when(this.mockResponse.getStatus()).thenReturn(Response.Status.OK.getStatusCode());
        Mockito.when(this.mockResponse.getHeaderString(Constants.X_TOTAL)).thenReturn("45");
        Mockito.when(this.mockResponse.getHeaderString(Constants.X_TOTAL_PAGES)).thenReturn("3");
        Mockito.when(this.mockResponse.getHeaderString(Constants.X_PER_PAGE)).thenReturn("20");
        Mockito.when(this.mockResponse.getHeaderString(Constants.X_PAGE)).thenReturn("2");
        Mockito.when(this.mockResponse.getHeaderString(Constants.X_NEXT_PAGE)).thenReturn("3");
        Mockito.when(this.mockResponse.getHeaderString(Constants.X_PREVIOUS_PAGE)).thenReturn("1");

        MultivaluedMap<String, Object> paginationParams = new MultivaluedHashMap<>();
        paginationParams.add("page", new Integer(2));
        paginationParams.add("per_page", new Integer(20));
        Project mockProject = new Project();
        List<Project> projectList = new ArrayList<>();
        projectList.add(mockProject);
        Mockito.when(this.mockResponse.readEntity(Matchers.any(GenericType.class))).thenReturn(projectList);
        // When
        PaginatedList<Project> list = connectionService.getList("admin", Constants.PROJECTS_API_PATH + "/1234",
                Project.class, paginationParams);
        // Then
        Assert.assertNotNull(list);
        Assert.assertEquals(new Integer(45), list.getTotalItems());
    }
}
