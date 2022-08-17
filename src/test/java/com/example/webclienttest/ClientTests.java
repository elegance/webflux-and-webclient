package com.example.webclienttest;

import lombok.extern.slf4j.Slf4j;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.example.webclienttest.BuildRestTemplate.simpleClientHttpRequestFactory;

@SpringBootTest
@Slf4j
public class ClientTests {

    @Test
    public void contextLoads() {
    }

    private static WebClient client;

    private static RestTemplate restTemplate;

    private static final String DELAY_URI = "/delay/0.5";

    // online
    // docker
    // binary
    //  httpbin -host :9090

    @BeforeClass
    public static void init() {
        client = WebClient.create("http://127.0.0.1:9090");
        restTemplate = new RestTemplate(simpleClientHttpRequestFactory());

    }

    public Mono<String> fluxQuery() {
        return client
                .method(HttpMethod.GET)
                .uri(DELAY_URI)
                .body(Mono.just(""), String.class)
                .retrieve()
                .bodyToMono(String.class);
    }

    @Test
    public void bd() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Mono<String> stringMono = client.method(HttpMethod.GET)
                .uri("https://www.baidu.com").retrieve()
                .bodyToMono(String.class);
        stringMono.subscribe(s-> {
            System.out.println(s);
            latch.countDown();
        });
        latch.await();
    }

    @Test
    public void testWebClient() throws InterruptedException {
        int count = 1000;
        CountDownLatch latch = new CountDownLatch(count);
        long start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            int finalI = i;
            fluxQuery().subscribe(rs -> {
                // log.info("orderNo: {}, 支付查询结果： {}", finalI, rs);
                log.info("done : {},", finalI);
                latch.countDown();
            });
        }
        log.info("wait io response....");
        latch.await();
        log.info("work was done! cost: {}ms", (System.currentTimeMillis() - start));
    }

    /**
     * 35486 ms,
     */
    @Test
    public void threadHttpClient() throws InterruptedException {
        int count = 1000;
        // 20 + 1
        ThreadPoolExecutor executor = new ThreadPoolExecutor(20, 20, 60, TimeUnit.SECONDS, new SynchronousQueue<>(), new ThreadPoolExecutor.CallerRunsPolicy());
        // 理论值 500ms * 1000  / 20t = 25 000ms
        CountDownLatch latch = new CountDownLatch(count);
        long start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            int finalI = i;
            executor.execute(() -> {
                restTemplate.getForObject("http://127.0.0.1:9090/delay/0.5", String.class);
                log.info("done : {},", finalI);
                latch.countDown();
            });
        }
        log.info("wait io response....");
        latch.await();
        log.info("work was done! cost: {}ms", (System.currentTimeMillis() - start));
        // 出现问题：积压数据，时效性变低
        // 资源用尽了么，压力在谁哪，谁最忙？              都是 httpbin server 在干具体的活，而本身被拖慢了
        // 加机器 ？ (我们的抢票扩容 50个实例)
        // 加线程？加多少， coreSize * 2 ? 加太大也不合适
    }
}
