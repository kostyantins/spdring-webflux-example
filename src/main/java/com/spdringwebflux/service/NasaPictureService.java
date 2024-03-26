package com.spdringwebflux.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;

@Service
public class NasaPictureService {

    public Mono<String> getLargestPictureUrl() {
        return WebClient.create("https://api.nasa.gov/mars-photos/api/v1/rovers/curiosity/photos?sol=10&api_key=DEMO_KEY")
                .get()
                .exchangeToMono(resp -> resp.bodyToMono(JsonNode.class))
                .map(json -> json.get("photos"))
                .flatMapMany(Flux::fromIterable)
                .map(photo -> photo.get("img_src"))
                .map(JsonNode::asText)
                .flatMap(pictureUrl -> WebClient.create(pictureUrl)
                        .head()
                        .exchangeToMono(ClientResponse::toBodilessEntity)
                        .map(HttpEntity::getHeaders)
                        .map(HttpHeaders::getLocation)
                        .map(URI::toString)
                        .flatMap(redirectPictureUrl -> WebClient.create(redirectPictureUrl)
                                .head()
                                .exchangeToMono(ClientResponse::toBodilessEntity)
                                .map(HttpEntity::getHeaders)
                                .map(HttpHeaders::getContentLength)
                                .map(size -> new Picture(redirectPictureUrl, size))

                        )
                )
                .reduce((p1, p2) -> (p1.size > p2.size ? p1 : p2))
                .map(Picture::url);
    }

    record Picture(String url, Long size) {
    }
}
