package de.fh.albsig.app;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.time.LocalDateTime;

/**
 * Represents weather data for a specific location.
 *
 * <p>This entity is used for persisting weather information and supports XML
 * marshalling/unmarshalling for API communication.
 *
 * <ul>
 *   <li>Location: The geographical location of the weather data.
 *   <li>Temperature: The recorded temperature in Celsius.
 *   <li>Humidity: The recorded humidity as a percentage.
 *   <li>Timestamp: The time at which the weather data was recorded.
 * </ul>
 */
@XmlRootElement
@Entity
@Table(name = "weather_data")
public class WeatherData {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "location")
  private String location;

  @Column(name = "temperature")
  private double temperature;

  @Column(name = "humidity")
  private int humidity;

  @Column(name = "timestamp")
  private LocalDateTime timestamp;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public double getTemperature() {
    return temperature;
  }

  public void setTemperature(double temperature) {
    this.temperature = temperature;
  }

  public int getHumidity() {
    return humidity;
  }

  public void setHumidity(int humidity) {
    this.humidity = humidity;
  }

  public LocalDateTime getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(LocalDateTime timestamp) {
    this.timestamp = timestamp;
  }
}
