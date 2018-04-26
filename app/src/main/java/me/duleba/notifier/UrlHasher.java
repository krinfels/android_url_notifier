package me.duleba.notifier;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class UrlHasher {
    public static long hashUrl(String url) throws IOException {
        InputStream stream = new BufferedInputStream(new URL(url).openStream());
        MessageDigest hasher;
        byte[] buffer = new byte[4096];

        int len = -1;

        //Should never fail
        try {
            hasher = MessageDigest.getInstance("md5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return -1;
        }

        while((len = stream.read(buffer)) != -1)
            hasher.update(buffer);

        byte[] hash = hasher.digest();
        long result = 0;
        long exponent = 1;

        for(int i=0;i<16;i++) {
            result += hash[i]*exponent;
            exponent *= 8;
        }

        return result;
    }
}
