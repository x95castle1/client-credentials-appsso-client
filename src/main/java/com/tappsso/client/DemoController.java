package com.tappsso.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

@RestController
@RequestMapping(path = "api", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = "*")
public class DemoController {

	Logger logger = LoggerFactory.getLogger(DemoController.class);

  // Inject the OAuth authorized client service and authorized client manager
	// from the OAuthClientConfiguration class
	@Autowired
	private AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientServiceAndManager;

	@GetMapping(value = "/test")
	public String testClientEndpoint() {

		// STEP 1: Retrieve the authorized JWT
		OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest.withClientRegistrationId("appsso")
			.principal("Demo Service")
			.build();

		// Perform the actual authorization request using the authorized client service and authorized client
		// manager. This is where the JWT is retrieved from the Auth Server
		OAuth2AuthorizedClient authorizedClient = this.authorizedClientServiceAndManager.authorize(authorizeRequest);

		// Get the token from the authorized client object
		OAuth2AccessToken accessToken = Objects.requireNonNull(authorizedClient).getAccessToken();

		logger.info("Issued: " + accessToken.getIssuedAt().toString() + ", Expires:" + accessToken.getExpiresAt().toString());
		logger.info("Scopes: " + accessToken.getScopes().toString());
		logger.info("Token: " + accessToken.getTokenValue());

		// STEP 2: Use the JWT and call the service
		// Add the JWT to the RestTemplate headers
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + accessToken.getTokenValue());
		var request = new HttpEntity(headers);

		// Make the actual HTTP GET request
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> response = restTemplate.exchange(
			"https://resource-server-client-credentials.dev.h2o-2-22372.h2o.vmware.com/api/private-read",
			HttpMethod.GET,
			request,
			String.class
		);

		String result = response.getBody();
		logger.info("Reply = " + result);

		return result;
	}
}
