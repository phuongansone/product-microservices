package study.microservices.composite.product.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import study.api.core.product.Product;
import study.api.core.product.ProductService;
import study.api.core.recommendation.Recommendation;
import study.api.core.recommendation.RecommendationService;
import study.api.core.review.Review;
import study.api.core.review.ReviewService;
import study.api.exceptions.InvalidInputException;
import study.api.exceptions.NotFoundException;
import study.util.http.HttpErrorInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.springframework.http.HttpMethod.GET;

@Component
public class ProductCompositeIntegration implements ProductService, RecommendationService,
    ReviewService {

  private static final Logger LOG = LoggerFactory.getLogger(ProductCompositeIntegration.class);
  private final RestTemplate restTemplate;
  private final ObjectMapper mapper;

  private final String productServiceUrl;
  private final String recommendationServiceUrl;
  private final String reviewServiceUrl;

  public ProductCompositeIntegration(
      RestTemplate restTemplate,
      ObjectMapper mapper,
      @Value("${app.product-service.host}") String productServiceHost,
      @Value("${app.product-service.port}") int productServicePort,
      @Value("${app.recommendation-service.host}") String recommendationServiceHost,
      @Value("${app.recommendation-service.port}") int recommendationServicePort,
      @Value("${app.review-service.host}") String reviewServiceHost,
      @Value("${app.review-service.port}") int reviewServicePort) {
    this.restTemplate = restTemplate;
    this.mapper = mapper;

    productServiceUrl = "http://" + productServiceHost + ":" + productServicePort + "/product";
    recommendationServiceUrl =
        "http://" + recommendationServiceHost + ":" + recommendationServicePort + "/recommendation";
    reviewServiceUrl = "http://" + reviewServiceHost + ":" + reviewServicePort + "/review";
  }

  @Override
  public Product getProduct(int productId) {
    try {
      String url = productServiceUrl + "/" + productId;
      LOG.debug("Will call the getProduct API on URL: {}", url);

      Product product = restTemplate.getForObject(url, Product.class);
      LOG.debug("Found a product with id: {}", product.getProductId());

      return product;

    } catch (HttpClientErrorException ex) {
      throw handleHttpClientException(ex);
    }
  }

  @Override
  public Product createProduct(Product body) {
    try {
      LOG.debug("Will post a new product to URL: {}, Product: {}", productServiceUrl, body);

      Product product = restTemplate.postForObject(productServiceUrl, body, Product.class);
      LOG.debug("Created a product with id: {}", product.getProductId());

      return product;

    } catch (HttpClientErrorException ex) {
      throw handleHttpClientException(ex);
    }
  }

  @Override
  public void deleteProduct(int productId) {
    try {
      String url = productServiceUrl + "/" + productId;
      LOG.debug("Will call the deleteProduct API on URL: {}", url);

      restTemplate.delete(url);

    } catch (HttpClientErrorException ex) {
      throw handleHttpClientException(ex);
    }
  }

  private String getErrorMessage(HttpClientErrorException ex) {
    try {
      return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
    } catch (IOException e) {
      return ex.getMessage();
    }
  }

  @Override
  public List<Recommendation> getRecommendations(int productId) {

    try {
      String url = recommendationServiceUrl + "?productId=" + productId;

      LOG.debug("Will call the getRecommendations API on URL: {}", url);
      List<Recommendation> recommendations = restTemplate
          .exchange(url, GET, null, new ParameterizedTypeReference<List<Recommendation>>() {
          })
          .getBody();

      LOG.debug("Found {} recommendations for a product with id: {}", recommendations.size(),
          productId);
      return recommendations;

    } catch (Exception ex) {
      LOG.warn("Got an exception while requesting recommendations, return zero recommendations: {}",
          ex.getMessage());
      return new ArrayList<>();
    }
  }

  @Override
  public Recommendation createRecommendation(Recommendation body) {
    try {
      LOG.debug("Will post a new recommendation to URL: {}", recommendationServiceUrl);

      Recommendation recommendation = restTemplate.postForObject(recommendationServiceUrl, body,
          Recommendation.class);
      LOG.debug("Created a recommendation with id: {}", recommendation.getProductId());

      return recommendation;
    } catch (HttpClientErrorException ex) {
      throw handleHttpClientException(ex);
    }
  }

  @Override
  public void deleteRecommendations(int productId) {
    try {
      String url = recommendationServiceUrl + "?productId=" + productId;
      LOG.debug("Will call the deleteRecommendations API on URL: {}", url);

      restTemplate.delete(url);

    } catch (HttpClientErrorException ex) {
      throw handleHttpClientException(ex);
    }
  }

  @Override
  public List<Review> getReviews(int productId) {
    try {
      String url = reviewServiceUrl + "?productId=" + productId;

      LOG.debug("Will call the getReviews API on URL: {}", url);
      List<Review> reviews = restTemplate
          .exchange(url, GET, null, new ParameterizedTypeReference<List<Review>>() {
          })
          .getBody();

      LOG.debug("Found {} reviews for a product with id: {}", reviews.size(), productId);
      return reviews;

    } catch (Exception ex) {
      LOG.warn("Got an exception while requesting reviews, return zero reviews: {}",
          ex.getMessage());
      return new ArrayList<>();
    }
  }

  @Override
  public Review createReview(Review body) {
    try {
      LOG.debug("Will post a new review to URL: {}", reviewServiceUrl);

      Review review = restTemplate.postForObject(reviewServiceUrl, body, Review.class);
      LOG.debug("Created a review with id: {}", review.getProductId());

      return review;

    } catch (HttpClientErrorException ex) {
      throw handleHttpClientException(ex);
    }
  }

  @Override
  public void deleteReviews(int productId) {
    try {
      String url = reviewServiceUrl + "?productId=" + productId;
      LOG.debug("Will call the deleteReviews API on URL: {}", url);

      restTemplate.delete(url);

    } catch (HttpClientErrorException ex) {
      throw handleHttpClientException(ex);
    }
  }

  private RuntimeException handleHttpClientException(HttpClientErrorException ex) {
    switch (Objects.requireNonNull(HttpStatus.resolve(ex.getStatusCode().value()))) {

      case NOT_FOUND:
        return new NotFoundException(getErrorMessage(ex));

      case UNPROCESSABLE_ENTITY:
        return new InvalidInputException(getErrorMessage(ex));

      default:
        LOG.warn("Got an unexpected HTTP error: {}, will rethrow it", ex.getStatusCode());
        LOG.warn("Error body: {}", ex.getResponseBodyAsString());
        return ex;
    }
  }
}
