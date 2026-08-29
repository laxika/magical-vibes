package com.github.laxika.magicalvibes.carddata;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The fetch-once-then-read-from-disk behaviour, with a stub fetcher instead of the network.
 *
 * <p>Worth its own test because the real loaders only ever exercise the cache-hit path once a
 * developer machine has warmed its cache, so a regression in the write path would stay invisible
 * until someone cloned the repo fresh.
 */
class SetJsonCacheTest {

    @TempDir
    Path cacheDir;

    @Test
    void firstReadFetchesAndCachesIt() throws Exception {
        List<String> fetched = new ArrayList<>();
        SetJsonCache cache = cache(setCode -> {
            fetched.add(setCode);
            return "[{\"id\":\"" + setCode + "\"}]";
        });

        assertThat(cache.get("ISD")).isEqualTo("[{\"id\":\"ISD\"}]");
        assertThat(fetched).containsExactly("ISD");
        assertThat(cache.fileFor("ISD")).exists().hasContent("[{\"id\":\"ISD\"}]");
    }

    @Test
    void secondReadComesFromDiskWithoutFetching() throws Exception {
        List<String> fetched = new ArrayList<>();
        SetJsonCache cache = cache(setCode -> {
            fetched.add(setCode);
            return "[]";
        });

        cache.get("ISD");
        cache.get("ISD");

        assertThat(fetched).containsExactly("ISD");
    }

    /** A cache file written by an earlier run wins over the network, byte for byte. */
    @Test
    void anExistingCacheFileIsReadVerbatim() throws Exception {
        Files.writeString(cacheDir.resolve("test-isd.json"), "cached contents");
        SetJsonCache cache = cache(setCode -> {
            throw new AssertionError("must not fetch when the cache file exists");
        });

        assertThat(cache.get("ISD")).isEqualTo("cached contents");
    }

    /** Set codes are upper-case in code and lower-case on disk; the prefix separates providers. */
    @Test
    void cacheFileNameIsPrefixedAndLowercased() {
        assertThat(cache(setCode -> "").fileFor("ISD")).isEqualTo(cacheDir.resolve("test-isd.json"));
    }

    /** A failed fetch must not leave a file behind, or the failure would be cached forever. */
    @Test
    void aFailedFetchCachesNothing() {
        List<String> fetched = new ArrayList<>();
        SetJsonCache cache = cache(setCode -> {
            fetched.add(setCode);
            throw new java.io.IOException("upstream is down");
        });

        assertThatThrownBy(() -> cache.get("ISD")).hasMessageContaining("upstream is down");
        assertThat(fetched).containsExactly("ISD", "ISD", "ISD");
        assertThat(cache.fileFor("ISD")).doesNotExist();
    }

    @Test
    void aTransientFetchFailureIsRetriedAndCached() throws Exception {
        List<String> fetched = new ArrayList<>();
        SetJsonCache cache = cache(setCode -> {
            fetched.add(setCode);
            if (fetched.size() < 3) {
                throw new java.io.IOException("response stream ended early");
            }
            return "recovered";
        });

        assertThat(cache.get("ISD")).isEqualTo("recovered");
        assertThat(fetched).containsExactly("ISD", "ISD", "ISD");
        assertThat(cache.fileFor("ISD")).exists().hasContent("recovered");
    }

    private SetJsonCache cache(SetJsonCache.Fetcher fetcher) {
        return new SetJsonCache(
                cacheDir.toString(), "test-", "TestSource", fetcher, 3, Duration.ZERO);
    }
}
