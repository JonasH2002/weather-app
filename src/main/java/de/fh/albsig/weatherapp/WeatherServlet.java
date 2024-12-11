package de.fh.albsig.weatherapp;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import java.io.IOException;
import java.util.Objects;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Servlet for handling weather data operations via HTTP requests.
 *
 * <p>Supported operations:
 *
 * <ul>
 *   <li>GET: Retrieve weather data for a specific location or all weather data.
 *   <li>POST: Add new weather data to the repository.
 *   <li>PUT: Update existing weather data in the repository.
 *   <li>DELETE: Remove specific weather data from the repository.
 * </ul>
 *
 * <p>The servlet interacts with the {@link WeatherRepository} to perform database operations and
 * uses JAXB for parsing and generating XML data.
 *
 * <p>In production, the servlet initializes with a real {@link WeatherRepository}. For testing, a
 * mock repository can be injected using the parameterized constructor.
 */
@WebServlet("/weather")
public class WeatherServlet extends HttpServlet {

  private static final Logger logger = LogManager.getLogger(WeatherServlet.class);
  private final WeatherRepository weatherRepository;

  /**
   * Default constructor for the WeatherServlet. Initializes the servlet with a new instance of
   * WeatherRepository.
   *
   * <p>This constructor is primarily used in a production environment where a real repository is
   * needed to interact with the database.
   */
  public WeatherServlet() {
    this.weatherRepository = new WeatherRepository();
  }

  /**
   * Constructor for initializing the WeatherServlet with a WeatherRepository instance.
   *
   * @param weatherRepository the repository used for weather data operations
   */
  public WeatherServlet(WeatherRepository weatherRepository) {
    this.weatherRepository =
        Objects.requireNonNull(weatherRepository, "weatherRepository must not be null");
  }

  /**
   * Handles HTTP GET requests to retrieve weather data for a specific location. Expects a
   * "location" query parameter.
   *
   * @param req the HttpServletRequest object
   * @param resp the HttpServletResponse object
   * @throws IOException if an I/O error occurs during processing
   */
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    try {
      JAXBContext context = JAXBContext.newInstance(WeatherData.class);
      Marshaller marshaller = context.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

      logger.info("Received a GET request");

      String location = req.getParameter("location");

      // Prüfen, ob der Parameter "location" vorhanden ist
      if (location == null || location.isEmpty()) {
        logger.warn("No location provided in request");
        resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Location parameter is missing");
        return;
      }

      // Abfrage in der Datenbank
      logger.debug("Searching weather data for location: {}", location);
      WeatherData data = weatherRepository.findByLocation(location);

      if (data == null) {
        logger.info("No weather data found for location: {}", location);
        resp.sendError(
            HttpServletResponse.SC_NOT_FOUND, "No weather data found for the specified location");
        return;
      }

      // Wetterdaten als XML zurückgeben
      logger.debug("Weather data found: {}", data);
      // Setzen des Content-Typs auf XML
      resp.setContentType("application/xml");
      marshaller.marshal(data, resp.getWriter());
    } catch (Exception e) {
      logger.error("Error while processing the request", e);
      resp.sendError(
          HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          "An error occurred while processing the request");
    }
  }

  /**
   * Handles HTTP POST requests to save or update weather data. Expects XML representation of
   * WeatherData in the request body.
   *
   * @param req the HttpServletRequest object
   * @param resp the HttpServletResponse object
   * @throws IOException if an I/O error occurs during processing
   */
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    try {
      JAXBContext context = JAXBContext.newInstance(WeatherData.class);
      Unmarshaller unmarshaller = context.createUnmarshaller();
      WeatherData weatherData = (WeatherData) unmarshaller.unmarshal(req.getReader());

      if (weatherData == null
          || weatherData.getLocation() == null
          || weatherData.getLocation().isEmpty()) {
        resp.sendError(
            HttpServletResponse.SC_BAD_REQUEST, "Invalid weather data format: Missing location");
        return;
      }

      weatherRepository.save(weatherData);

      logger.info("Saved weather data: {}", weatherData);
      resp.setStatus(HttpServletResponse.SC_CREATED);
      resp.getWriter().write("Weather data saved successfully.");
    } catch (Exception e) {
      logger.error("Error while saving weather data", e);
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid weather data format");
    }
  }

  /**
   * Handles HTTP PUT requests to update existing weather data.
   *
   * <p>This method expects the request body to contain a valid XML representation of a WeatherData
   * object. The provided data is used to update an existing entry in the WeatherRepository. If the
   * update is successful, the response status is set to 204 (No Content). If the input data is
   * invalid, a 400 (Bad Request) error is returned.
   *
   * @param req the HttpServletRequest containing the XML weather data to update
   * @param resp the HttpServletResponse to send the status of the operation
   * @throws IOException if an input or output error occurs while processing the request
   */
  @Override
  protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    try {
      // Parse the incoming WeatherData object from the request body
      JAXBContext context = JAXBContext.newInstance(WeatherData.class);
      Unmarshaller unmarshaller = context.createUnmarshaller();
      WeatherData weatherData = (WeatherData) unmarshaller.unmarshal(req.getReader());

      // Sicherstellen, dass das WeatherData-Objekt eine ID hat
      if (weatherData.getId() == null) {
        resp.sendError(
            HttpServletResponse.SC_BAD_REQUEST, "WeatherData ID must not be null for update");
        return;
      }

      // Update the WeatherData in the repository
      weatherRepository.save(weatherData);

      logger.info("Updated weather data: {}", weatherData);
      resp.setStatus(HttpServletResponse.SC_NO_CONTENT); // Return HTTP 204 for successful update
    } catch (Exception e) {
      logger.error("Error while updating weather data", e);
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Error updating weather data");
    }
  }

  /**
   * Handles HTTP DELETE requests to delete weather data. Expects XML representation of WeatherData
   * in the request body.
   *
   * @param req the HttpServletRequest object
   * @param resp the HttpServletResponse object
   * @throws IOException if an I/O error occurs during processing
   */
  @Override
  protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    try {
      // JAXB zum Unmarshalling des Request-Bodies
      JAXBContext context = JAXBContext.newInstance(WeatherData.class);
      Unmarshaller unmarshaller = context.createUnmarshaller();
      WeatherData weatherData = (WeatherData) unmarshaller.unmarshal(req.getReader());

      // Sicherstellen, dass das WeatherData-Objekt eine ID hat
      if (weatherData.getId() == null) {
        resp.sendError(
            HttpServletResponse.SC_BAD_REQUEST, "WeatherData ID must not be null for deletion");
        return;
      }

      // Wetterdaten löschen
      weatherRepository.delete(weatherData);

      logger.info("Deleted weather data: {}", weatherData);
      resp.setStatus(HttpServletResponse.SC_NO_CONTENT); // 204 No Content bei erfolgreichem Löschen
    } catch (Exception e) {
      logger.error("Error while deleting weather data", e);
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error deleting weather data");
    }
  }
}
