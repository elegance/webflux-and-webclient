package com.example.webclienttest.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
public class NormalController {

    @GetMapping("/rest/delay/{seconds}")
    public String delay(@PathVariable double seconds) throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep((long) (seconds * 1000));
        return "done";
    }

}
