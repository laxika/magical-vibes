package com.github.laxika.magicalvibes.carddata.store;

import com.github.laxika.magicalvibes.carddata.CardDataSupport;
import com.github.laxika.magicalvibes.carddata.OracleDataProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * The only reader and writer of the per-set files in {@code card-data.cache-dir}.
 *
 * <p>A set is read from its {@code set-{code}.json} if that file exists and has the current
 * {@link StoredSet#FORMAT_VERSION}. Otherwise it is downloaded into a fresh temp directory from the
 * preferred provider ({@code oracle.data-provider}, Scryfall when absent) — or from the other one
 * if the preferred provider still fails after retries — converted, written to the cache directory,
 * and the temp directory with the raw download is deleted. Only the converted file is ever kept.
 *
 * <p>The fallback cannot make oracle text differ from one run to the next: it only ever applies to
 * the first fetch of a set, and the file it produces pins the data from then on, with
 * {@link StoredSet#source} recording where it came from. Stored sets never expire; deleting a
 * file is how a set is refreshed.
 *
 * <p>Thread-safe: concurrent requests for one set download it once. Separate JVMs sharing the
 * directory (parallel test forks) may both download a missing set, but the atomic write in
 * {@link CardDataSupport#writeCacheFile} means neither ever reads a partial file.
 */
@Service
public class SetDataStore {

    private static final Logger LOG = Logger.getLogger(SetDataStore.class.getName());
    private static final ObjectMapper MAPPER = JsonMapper.builder().build();
    private static final int DEFAULT_FETCH_ATTEMPTS = 3;
    private static final Duration DEFAULT_RETRY_DELAY = Duration.ofSeconds(1);

    private final Path cacheDir;
    private final List<SetSource> sourcesInPreferenceOrder;
    private final int fetchAttempts;
    private final Duration retryDelay;
    private final Map<String, Object> setLocks = new ConcurrentHashMap<>();

    /**
     * @param preferredProvider an {@link OracleDataProvider} name, matched case-insensitively. Any
     *                          other value fails construction, and with it application startup.
     */
    @Autowired
    public SetDataStore(@Value("${card-data.cache-dir:./card-data-cache}") String cacheDir,
                        @Value("${oracle.data-provider:SCRYFALL}") String preferredProvider,
                        List<SetSource> sources) {
        this(Path.of(cacheDir), parseProvider(preferredProvider), sources,
                DEFAULT_FETCH_ATTEMPTS, DEFAULT_RETRY_DELAY);
    }

    SetDataStore(Path cacheDir, OracleDataProvider preferredProvider, List<SetSource> sources,
                 int fetchAttempts, Duration retryDelay) {
        if (fetchAttempts < 1) {
            throw new IllegalArgumentException("fetchAttempts must be at least 1");
        }
        if (retryDelay.isNegative()) {
            throw new IllegalArgumentException("retryDelay must not be negative");
        }
        this.cacheDir = cacheDir;
        this.sourcesInPreferenceOrder = sources.stream()
                .sorted(Comparator.comparing(source -> source.provider() != preferredProvider))
                .toList();
        this.fetchAttempts = fetchAttempts;
        this.retryDelay = Objects.requireNonNull(retryDelay);
    }

    static OracleDataProvider parseProvider(String value) {
        try {
            return OracleDataProvider.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown oracle.data-provider '" + value
                    + "'; expected one of " + List.of(OracleDataProvider.values()), e);
        }
    }

    /**
     * The stored set, downloading and storing it first if there is no usable file.
     *
     * @throws IOException if there is no usable file and no provider could supply the set; nothing
     *                     is written in that case, so the next call tries again
     */
    public StoredSet get(String setCode) throws IOException, InterruptedException {
        String key = setCode.toLowerCase(Locale.ROOT);
        synchronized (setLocks.computeIfAbsent(key, ignored -> new Object())) {
            Path file = fileFor(setCode);
            StoredSet stored = read(file);
            if (stored != null) {
                return stored;
            }
            StoredSet downloaded = download(setCode);
            Files.createDirectories(cacheDir);
            CardDataSupport.writeCacheFile(file, MAPPER.writeValueAsString(downloaded));
            LOG.info("Stored " + setCode + " from " + downloaded.source() + " to: " + file);
            return downloaded;
        }
    }

    /** Where this set is stored, whether or not it has been written yet. */
    public Path fileFor(String setCode) {
        return cacheDir.resolve("set-" + setCode.toLowerCase(Locale.ROOT) + ".json");
    }

    private static StoredSet read(Path file) throws IOException {
        if (!Files.exists(file)) {
            return null;
        }
        try {
            StoredSet stored = MAPPER.readValue(file.toFile(), StoredSet.class);
            if (stored.formatVersion() == StoredSet.FORMAT_VERSION) {
                return stored;
            }
            LOG.info(file + " has format version " + stored.formatVersion() + ", expected "
                    + StoredSet.FORMAT_VERSION + "; downloading it again");
        } catch (JacksonException e) {
            LOG.warning(file + " is unreadable (" + e.getOriginalMessage() + "); downloading it again");
        }
        return null;
    }

    private StoredSet download(String setCode) throws IOException, InterruptedException {
        List<String> failures = new ArrayList<>();
        IOException lastFailure = null;
        for (SetSource source : sourcesInPreferenceOrder) {
            try {
                StoredSet downloaded = downloadWithRetry(source, setCode);
                if (!failures.isEmpty()) {
                    LOG.warning("Stored " + setCode + " from fallback provider " + source.provider()
                            + " after: " + String.join("; ", failures));
                }
                return downloaded;
            } catch (IOException e) {
                failures.add(source.provider() + ": " + e.getMessage());
                lastFailure = e;
            }
        }
        throw new IOException("Could not download set " + setCode + " from any provider: "
                + String.join("; ", failures), lastFailure);
    }

    private StoredSet downloadWithRetry(SetSource source, String setCode)
            throws IOException, InterruptedException {
        for (int attempt = 1; ; attempt++) {
            Path tempDir = Files.createTempDirectory("magical-vibes-set-");
            try {
                LOG.info("Downloading " + setCode + " from " + source.provider() + "...");
                StoredSet downloaded;
                try {
                    downloaded = source.download(setCode, tempDir);
                } catch (JacksonException e) {
                    throw new IOException(source.provider() + " returned malformed data for set "
                            + setCode + ": " + e.getOriginalMessage(), e);
                }
                if (downloaded.printings().isEmpty()) {
                    throw new IOException(source.provider() + " returned no printings for set " + setCode);
                }
                return downloaded;
            } catch (IOException e) {
                if (attempt == fetchAttempts) {
                    throw e;
                }
                LOG.warning("Failed to download " + setCode + " from " + source.provider()
                        + " (attempt " + attempt + " of " + fetchAttempts + "): " + e.getMessage());
            } finally {
                deleteRecursively(tempDir);
            }
            Thread.sleep(retryDelay.multipliedBy(attempt));
        }
    }

    private static void deleteRecursively(Path dir) throws IOException {
        try (Stream<Path> paths = Files.walk(dir)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(path);
            }
        }
    }
}
