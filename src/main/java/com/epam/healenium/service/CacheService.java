package com.epam.healenium.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;

import java.time.Duration;
import java.util.Objects;

@Slf4j(topic = "healenium")
public class CacheService {
    /**
     * hashed(URL, By) -> Healed "By"-locator
     * Make cache available for only 30 seconds to allow locator adjustments in case the locator was wrong.
     */
    private static Cache<Integer, By> cached;
    private static int lifespan;

    public static void enable(int lifespan) {
        if (cached != null) {
            log.info("Caching was already enabled by another web driver." +
                    " Cache will be shared." +
                    " Lifespan={}s", CacheService.lifespan);
            return;
        }

        if (lifespan <= 0) {
            log.info("Caching is disabled.");
            return;
        }

        log.info("Caching is enabled. Lifespan={}s", lifespan);
        if (lifespan > 300) {
            log.warn("Lifespan is longer than 5 minutes. Use with caution!");
        }

        CacheService.lifespan = lifespan;
        cached = CacheBuilder.newBuilder()
                .expireAfterWrite(Duration.ofSeconds(lifespan)).build();
    }

    private static int key(String url, By locator) {
        return Objects.hash(url, locator);
    }

    public static By get(String url, By originalLocator) {
        if (cached == null) return null;
        return cached.getIfPresent(key(url, originalLocator));
    }

    public static void add(String url, By originalLocator, By corrected) {
        if (cached == null) return;
        cached.put(key(url, originalLocator), corrected);
    }

    public static void remove(String url, By locator) {
        if (cached == null) return;
        cached.invalidate(CacheService.key(url, locator));
    }
}
