package org.jretty.util.httpclientasync.example;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.util.concurrent.Future;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.nio.conn.ssl.SSLIOSessionStrategy;
import org.apache.http.ssl.SSLContexts;

/**
 * This example demonstrates how to create secure connections with a custom SSL
 * context.
 */
public class AsyncClientCustomSSL {

    public final static void main(String[] args) throws Exception {
        KeyStore trustStore  = KeyStore.getInstance(KeyStore.getDefaultType());
        FileInputStream instream = new FileInputStream(new File("my.keystore"));
        try {
            trustStore.load(instream, "nopassword".toCharArray());
        } finally {
            instream.close();
        }
        // Trust own CA and all self-signed certs
        SSLContext sslcontext = SSLContexts.custom()
                .loadTrustMaterial(trustStore, new TrustSelfSignedStrategy())
                .build();
        // Allow TLSv1 protocol only
        SSLIOSessionStrategy sslSessionStrategy = new SSLIOSessionStrategy(
                sslcontext,
                new String[] { "TLSv1" },
                null,
                SSLIOSessionStrategy.getDefaultHostnameVerifier());
        CloseableHttpAsyncClient httpclient = HttpAsyncClients.custom()
                .setSSLStrategy(sslSessionStrategy)
                .build();
        try {
            httpclient.start();
            HttpGet request = new HttpGet("https://issues.apache.org/");
            Future<HttpResponse> future = httpclient.execute(request, null);
            HttpResponse response = future.get();
            System.out.println("Response: " + response.getStatusLine());
            System.out.println("Shutting down");
        } finally {
            httpclient.close();
        }
        System.out.println("Done");
    }

}