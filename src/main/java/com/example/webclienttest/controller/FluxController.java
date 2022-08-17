package com.example.webclienttest.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Duration;

@RestController
public class FluxController {
    @GetMapping("/flux/delay/{seconds}")
    public Mono<String> monoDelay(@PathVariable double seconds) throws InterruptedException {
        long millis = (long) (seconds * 1000);
        System.out.println(millis);
        return Mono.just("done")
                .delayElement(Duration.ofMillis(millis));
    }
}
