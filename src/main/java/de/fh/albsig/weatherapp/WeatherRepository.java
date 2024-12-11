package de.fh.albsig.weatherapp;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;
import java.io.Serializable;
import java.util.List;

/**
 * Repository class for managing weather data in the database. Provides methods for CRUD operations
 * on weather data.
 */
public class WeatherRepository implements Serializable {

  private static final EntityManagerFactory emf =
      Persistence.createEntityManagerFactory("weatherPU");

  /**
   * Saves or updates a WeatherData entity in the database.
   *
   * <p>If the entity is new (i.e., its ID is null), it is persisted. If the entity already exists
   * (i.e., its ID is not null and matches an existing record), it is updated.
   *
   * @param weatherData the WeatherData entity to save or update
   */
  public void save(WeatherData weatherData) {
    EntityManager em = emf.createEntityManager();
    em.getTransaction().begin();
    em.merge(weatherData); // Updates the entity if it already exists
    em.getTransaction().commit();
    em.close();
  }

  /**
   * Finds a WeatherData entity by its location.
   *
   * @param location the location of the weather data
   * @return the WeatherData entity, or null if not found
   */
  public WeatherData findByLocation(String location) {
    EntityManager em = emf.createEntityManager();
    TypedQuery<WeatherData> query =
        em.createQuery(
            "SELECT w FROM WeatherData w WHERE w.location = :location", WeatherData.class);
    query.setParameter("location", location);
    List<WeatherData> results = query.getResultList();
    em.close();
    return results.isEmpty() ? null : results.get(0);
  }

  /**
   * Finds all WeatherData entities in the database.
   *
   * @return a list of WeatherData entities
   */
  public List<WeatherData> findAll() {
    EntityManager em = emf.createEntityManager();
    List<WeatherData> results =
        em.createQuery("SELECT w FROM WeatherData w", WeatherData.class).getResultList();
    em.close();
    return results;
  }

  /**
   * Deletes a specific WeatherData entity from the database.
   *
   * @param weatherData the WeatherData entity to delete
   */
  public void delete(WeatherData weatherData) {
    EntityManager em = emf.createEntityManager();
    WeatherData removeWeather = em.find(WeatherData.class, weatherData.getId());
    em.getTransaction().begin();
    em.remove(removeWeather);
    em.getTransaction().commit();
  }
}
