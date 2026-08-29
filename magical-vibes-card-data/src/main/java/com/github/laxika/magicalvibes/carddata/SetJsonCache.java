package com.github.laxika.magicalvibes.carddata;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Locale;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * A set's raw JSON, read from disk if it has been fetched before and fetched once if not.
 *
 * <p>Startup would otherwise make one network round trip per set on every run. The cache is
 * permanent rather than expiring: oracle text for a released set does not change, and a build that
 * silently picked up different card text between runs is exactly what this module avoids elsewhere
 * too. Delete the cache directory to re-fetch.
 *
 * <p>Parsing is the caller's job — this deals in strings, because the two providers disagree about
 * what the document even is (an array of cards versus an object with a {@code data} node).
 */
public final class SetJsonCache {

    private static final Logger LOG = Logger.getLogger(SetJsonCache.class.getName());
    private static final int DEFAULT_FETCH_ATTEMPTS = 3;
    private static final Duration DEFAULT_RETRY_DELAY = Duration.ofSeconds(1);

    /** Retrieves a set's JSON from the upstream service. Owns its own rate limiting, if any. */
    @FunctionalInterface
    public interface Fetcher {
        String fetch(String setCode) throws IOException, InterruptedException;
    }

    private final Path cacheDir;
    private final String filePrefix;
    private final String sourceName;
    private final Fetcher fetcher;
    private final int fetchAttempts;
    private final Duration retryDelay;

    /**
     * @param filePrefix distinguishes one provider's cache files from another's in the shared cache
     *                   directory. Part of the on-disk layout, so changing it orphans existing
     *                   caches rather than corrupting them.
     * @param sourceName the provider's name, for log messages only
     */
    public SetJsonCache(String cacheDir, String filePrefix, String sourceName, Fetcher fetcher) {
        this(cacheDir, filePrefix, sourceName, fetcher,
                DEFAULT_FETCH_ATTEMPTS, DEFAULT_RETRY_DELAY);
    }

    SetJsonCache(String cacheDir, String filePrefix, String sourceName, Fetcher fetcher,
                 int fetchAttempts, Duration retryDelay) {
        if (fetchAttempts < 1) {
            throw new IllegalArgumentException("fetchAttempts must be at least 1");
        }
        this.cacheDir = Path.of(cacheDir);
        this.filePrefix = filePrefix;
        this.sourceName = sourceName;
        this.fetcher = fetcher;
        this.fetchAttempts = fetchAttempts;
        this.retryDelay = Objects.requireNonNull(retryDelay);
        if (retryDelay.isNegative()) {
            throw new IllegalArgumentException("retryDelay must not be negative");
        }
    }

    public String get(String setCode) throws IOException, InterruptedException {
        Files.createDirectories(cacheDir);
        Path cacheFile = fileFor(setCode);

        if (Files.exists(cacheFile)) {
            LOG.info("Loading " + setCode + " from " + sourceName + " cache: " + cacheFile);
            return Files.readString(cacheFile);
        }

        LOG.info("Fetching " + setCode + " from " + sourceName + "...");
        String json = fetch(setCode);
        CardDataSupport.writeCacheFile(cacheFile, json);
        LOG.info("Cached " + setCode + " to: " + cacheFile);
        return json;
    }

    private String fetch(String setCode) throws IOException, InterruptedException {
        for (int attempt = 1; attempt <= fetchAttempts; attempt++) {
            try {
                return fetcher.fetch(setCode);
            } catch (IOException e) {
                if (attempt == fetchAttempts) {
                    throw e;
                }
                LOG.warning("Failed to fetch " + setCode + " from " + sourceName
                        + " (attempt " + attempt + " of " + fetchAttempts + "): " + e.getMessage());
                Thread.sleep(retryDelay.multipliedBy(attempt));
            }
        }
        throw new IllegalStateException("Fetch attempts exhausted without a result or failure");
    }

    /** Where this set's JSON is cached, whether or not it has been written yet. */
    public Path fileFor(String setCode) {
        return cacheDir.resolve(filePrefix + setCode.toLowerCase(Locale.ROOT) + ".json");
    }
}
