package com.spdringwebflux.controller;

import com.spdringwebflux.service.NasaPictureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


@RestController
@RequiredArgsConstructor
public class PictureController {

    private final NasaPictureService nasaPictureService;

    @GetMapping(value = "/pictures/largest", produces = MediaType.IMAGE_PNG_VALUE)
    public Mono<byte[]> getLargestPicture() {
        return nasaPictureService.getLargestPictureUrl()
                .flatMap(url -> WebClient.create(url)
                        .mutate().codecs(config -> config.defaultCodecs().maxInMemorySize(10_000_000)).build()
                        .get()
                        .exchangeToMono(resp -> resp.bodyToMono(byte[].class))
                );
    }
}
