package in.rishikeshdarandale.gitlab.core;

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

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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

        Mockito.when(mockBuilder.accept(MediaType.APPLICATION_JSON)).thenReturn(mockBuilder);
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
}
