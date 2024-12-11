package de.fh.albsig.weatherapp;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WeatherServletTest {

  private WeatherServlet servlet;

  @Mock private WeatherRepository weatherRepository;

  @Mock private HttpServletRequest request;

  @Mock private HttpServletResponse response;

  @BeforeEach
  void setup() {
    servlet = new WeatherServlet(weatherRepository);
  }

  @Test
  void testDoGetWithValidLocation() throws Exception {
    // Arrange
    WeatherData mockData = new WeatherData();
    mockData.setLocation("Berlin");
    mockData.setTemperature(15.0);
    mockData.setHumidity(80);
    mockData.setTimestamp(LocalDateTime.now());

    when(request.getParameter("location")).thenReturn("Berlin");
    when(weatherRepository.findByLocation("Berlin")).thenReturn(mockData);

    StringWriter responseWriter = new StringWriter();
    when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));

    // Servlet aufrufen
    servlet.doGet(request, response);

    // Assertions
    verify(response).setContentType("application/xml");
    verify(response, never()).sendError(anyInt(), anyString());

    String responseContent = responseWriter.toString();

    assertTrue(responseContent.contains("<location>Berlin</location>"));
    assertTrue(responseContent.contains("<temperature>15.0</temperature>"));
    assertTrue(responseContent.contains("<humidity>80</humidity>"));
  }

  @Test
  void testDoGetWithMissingLocation() throws Exception {
    // Kein Standort angegeben
    when(request.getParameter("location")).thenReturn(null);

    // Servlet aufrufen
    servlet.doGet(request, response);

    // Assertions
    verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Location parameter is missing");
  }

  @Test
  void testDoGetWithNonexistentLocation() throws Exception {
    // Standort, der nicht existiert
    when(request.getParameter("location")).thenReturn("Nonexistent");
    when(weatherRepository.findByLocation("Nonexistent")).thenReturn(null);

    // Servlet aufrufen
    servlet.doGet(request, response);

    // Assertions
    verify(response)
        .sendError(
            HttpServletResponse.SC_NOT_FOUND, "No weather data found for the specified location");
  }

  @Test
  void testDoPostWithValidWeatherData() throws Exception {
    // Arrange: Gültige XML-Daten
    String validXml =
        """
        <weatherData>
            <location>Hamburg</location>
            <temperature>16</temperature>
            <humidity>70</humidity>
        </weatherData>
    """;

    WeatherData mockData = new WeatherData();
    mockData.setLocation("Hamburg");
    mockData.setTemperature(16);
    mockData.setHumidity(70);

    when(request.getReader()).thenReturn(new BufferedReader(new StringReader(validXml)));

    StringWriter responseWriter = new StringWriter();
    when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));

    // Act
    servlet.doPost(request, response);

    // Assert
    verify(weatherRepository)
        .save(
            argThat(
                data ->
                    data.getLocation().equals(mockData.getLocation())
                        && data.getTemperature() == mockData.getTemperature()
                        && data.getHumidity() == mockData.getHumidity()));
    verify(response).setStatus(HttpServletResponse.SC_CREATED);
    assertTrue(responseWriter.toString().contains("Weather data saved successfully."));
  }

  @Test
  void testDoPostWithInvalidData() throws Exception {
    // Arrange
    String invalidXml =
        """
            <weatherData>
                <temperature>22.5</temperature>
                <humidity>80</humidity>
            </weatherData>
        """;

    when(request.getReader()).thenReturn(new BufferedReader(new StringReader(invalidXml)));

    // Act
    servlet.doPost(request, response);

    // Assert
    verify(weatherRepository, never())
        .save(any(WeatherData.class)); // Sicherstellen, dass nichts gespeichert wurde
    verify(response)
        .sendError(
            HttpServletResponse.SC_BAD_REQUEST, "Invalid weather data format: Missing location");
  }

  @Test
  void testDoPutWithValidData() throws Exception {
    // Neue Daten für das Update
    String updateXml =
        """
            <weatherData>
                <id>1</id>
                <location>Berlin</location>
                <temperature>12.0</temperature>
                <humidity>70</humidity>
            </weatherData>
            """;

    when(request.getReader()).thenReturn(new BufferedReader(new StringReader(updateXml)));

    // Act
    servlet.doPut(request, response);

    // Assert
    verify(weatherRepository)
        .save(
            argThat(
                data ->
                    data.getId() == 1L
                        && data.getLocation().equals("Berlin")
                        && data.getTemperature() == 12.0
                        && data.getHumidity() == 70));
    verify(response).setStatus(HttpServletResponse.SC_NO_CONTENT);
  }

  @Test
  void testDoPutWithInvalidWeatherData() throws Exception {
    // Arrange: Ungültige XML-Daten
    String invalidXml =
        """
            <weatherData>
                <humidity>30</humidity>
            </weatherData>
        """;

    when(request.getReader()).thenReturn(new BufferedReader(new StringReader(invalidXml)));

    // Act
    servlet.doPut(request, response);

    // Assert
    verify(weatherRepository, never()).save(any(WeatherData.class));
    verify(response)
        .sendError(
            HttpServletResponse.SC_BAD_REQUEST, "WeatherData ID must not be null for update");
  }

  @Test
  void testDoDeleteWithValidWeatherData() throws Exception {
    // Arrange: Gültige XML-Daten
    String validXml =
        """
        <weatherData>
            <id>1</id>
            <location>Munich</location>
            <temperature>18.5</temperature>
            <humidity>30</humidity>
        </weatherData>
    """;

    WeatherData expectedData = new WeatherData();
    expectedData.setId(1L);
    expectedData.setLocation("Munich");
    expectedData.setTemperature(18.5);
    expectedData.setHumidity(30);

    when(request.getReader()).thenReturn(new BufferedReader(new StringReader(validXml)));

    // Act
    servlet.doDelete(request, response);

    // Assert
    verify(weatherRepository)
        .delete(
            argThat(
                data ->
                    data.getId().equals(expectedData.getId())
                        && data.getLocation().equals(expectedData.getLocation())
                        && data.getTemperature() == expectedData.getTemperature()
                        && data.getHumidity() == expectedData.getHumidity()));
    verify(response).setStatus(HttpServletResponse.SC_NO_CONTENT);
  }

  @Test
  void testDoDeleteWithoutID() throws Exception {
    // Arrange: Ungültige XML-Daten
    String xmlWithoutID =
        """
        <weatherData>
            <location>Munich</location>
            <temperature>18.5</temperature>
            <humidity>30</humidity>
        </weatherData>
    """;

    when(request.getReader()).thenReturn(new BufferedReader(new StringReader(xmlWithoutID)));

    // Act
    servlet.doDelete(request, response);

    // Assert
    verify(weatherRepository, never())
        .delete(any(WeatherData.class)); // Sicherstellen, dass delete nicht aufgerufen wurde
    verify(response)
        .sendError(
            HttpServletResponse.SC_BAD_REQUEST, "WeatherData ID must not be null for deletion");
  }
}
