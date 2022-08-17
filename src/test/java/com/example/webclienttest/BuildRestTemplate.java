package com.example.webclienttest;

import org.apache.http.client.HttpClient;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.util.Collections;

public class BuildRestTemplate {

    public static ClientHttpRequestFactory simpleClientHttpRequestFactory() {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setHttpClient(httpClient());
        //从连接管理器获取连接超时
        factory.setConnectionRequestTimeout(2000);
        //连接超时：请求建立连接 单位ms
        factory.setConnectTimeout(5000);
        //读取超时 单位ms
        factory.setReadTimeout(30000);
        return factory;
    }

    public static HttpClient httpClient() {

        HttpClientBuilder builder = HttpClients.custom();
        SSLContext sslContext = null;
        try {
            sslContext = new SSLContextBuilder().loadTrustMaterial(null, (arg0, arg1) -> true).build();
        } catch (Exception e) {
            //do nothing
        }

        HostnameVerifier hostnameVerifier = NoopHostnameVerifier.INSTANCE;

        SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext, hostnameVerifier);
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", sslSocketFactory)
                .build();

        PoolingHttpClientConnectionManager connMgr = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        // 将最大连接数增加
        connMgr.setMaxTotal(200);
        // 将每个路由基础的连接增加
        connMgr.setDefaultMaxPerRoute(100);
        connMgr.setValidateAfterInactivity(50);
        // 将目标主机的最大连接数增加
        builder.setConnectionManager(connMgr);
        BasicHeader connectionHeader = new BasicHeader("Connection", "close");
        builder.setDefaultHeaders(Collections.singletonList(connectionHeader));
        builder.setRetryHandler(new DefaultHttpRequestRetryHandler(0, false));

        return builder.build();

    }
}
