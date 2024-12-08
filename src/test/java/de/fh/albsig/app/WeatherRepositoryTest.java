package de.fh.albsig.app;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class WeatherRepositoryTest {

  private static EntityManagerFactory emf;
  private WeatherRepository weatherRepository;

  @BeforeEach
  void setup() {
    emf = Persistence.createEntityManagerFactory("weatherPU");
    weatherRepository = new WeatherRepository();
  }

  @Test
  void testSaveAndFindByLocation() {
    // Arrange
    WeatherData data = new WeatherData();
    data.setLocation("Berlin");
    data.setTemperature(15.0);
    data.setHumidity(80);
    data.setTimestamp(LocalDateTime.now());

    weatherRepository.save(data);

    // Act
    WeatherData weatherDataBerlin = weatherRepository.findByLocation("Berlin");

    // Assert
    assertNotNull(weatherDataBerlin);
    assertEquals("Berlin", weatherDataBerlin.getLocation());
    assertEquals(15.0, weatherDataBerlin.getTemperature());
    assertEquals(80, weatherDataBerlin.getHumidity());
  }

  @Test
  void testFindAll() {
    // Arrange
    WeatherData weatherDataHamburg = new WeatherData();
    weatherDataHamburg.setLocation("Hamburg");
    weatherDataHamburg.setTemperature(12.5);
    weatherDataHamburg.setHumidity(70);
    weatherDataHamburg.setTimestamp(LocalDateTime.now());

    WeatherData weatherDataMunich = new WeatherData();
    weatherDataMunich.setLocation("Munich");
    weatherDataMunich.setTemperature(10.0);
    weatherDataMunich.setHumidity(75);
    weatherDataMunich.setTimestamp(LocalDateTime.now());

    weatherRepository.save(weatherDataHamburg);
    weatherRepository.save(weatherDataMunich);

    // Act
    List<WeatherData> allWeatherData = weatherRepository.findAll();

    // Assert
    assertNotNull(allWeatherData);
    assertEquals(2, allWeatherData.size());
  }

  @Test
  void testUpdate() {
    // Arrange: Neues WeatherData-Objekt erstellen und speichern
    WeatherData weatherDataBerlin = new WeatherData();
    weatherDataBerlin.setLocation("Berlin");
    weatherDataBerlin.setTemperature(8.5);
    weatherDataBerlin.setHumidity(65);
    weatherDataBerlin.setTimestamp(LocalDateTime.now());

    weatherRepository.save(weatherDataBerlin);

    // Abrufen, um die ID zu setzen (JPA generiert die ID beim Speichern)
    WeatherData savedData = weatherRepository.findByLocation("Berlin");
    assertNotNull(savedData);
    Long id = savedData.getId();
    assertNotNull(id);

    // Update vorbereiten: Daten ändern
    savedData.setTemperature(10.0);
    savedData.setHumidity(60);

    // Act: Update ausführen
    weatherRepository.save(savedData);

    // Daten erneut abrufen, um die Änderungen zu prüfen
    WeatherData updatedWeatherData = weatherRepository.findByLocation("Berlin");

    // Assert: Überprüfen, ob die Änderungen korrekt übernommen wurden
    assertNotNull(updatedWeatherData);
    assertEquals(id, updatedWeatherData.getId()); // Sicherstellen, dass die ID gleich bleibt
    assertEquals("Berlin", updatedWeatherData.getLocation());
    assertEquals(10.0, updatedWeatherData.getTemperature());
    assertEquals(60, updatedWeatherData.getHumidity());
  }

  @Test
  void testDelete() {
    // Arrange: Neues WeatherData-Objekt erstellen und speichern
    WeatherData weatherDataBerlin = new WeatherData();
    weatherDataBerlin.setLocation("Berlin");
    weatherDataBerlin.setTemperature(8.5);
    weatherDataBerlin.setHumidity(65);
    weatherDataBerlin.setTimestamp(LocalDateTime.now());

    weatherRepository.save(weatherDataBerlin);

    // Abrufen, um die ID zu setzen (JPA generiert die ID beim Speichern)
    WeatherData savedData = weatherRepository.findByLocation("Berlin");
    assertNotNull(savedData);
    Long id = savedData.getId();
    assertNotNull(id);

    // Act: Objekt löschen
    weatherRepository.delete(savedData);

    // Daten erneut abrufen, um sicherzustellen, dass sie gelöscht wurden
    WeatherData deletedData = weatherRepository.findByLocation("Berlin");

    // Assert: Überprüfen, dass das Objekt gelöscht wurde
    assertNull(deletedData);
  }

  @AfterAll
  static void tearDown() {
    if (emf != null) {
      emf.close();
    }
  }
}
