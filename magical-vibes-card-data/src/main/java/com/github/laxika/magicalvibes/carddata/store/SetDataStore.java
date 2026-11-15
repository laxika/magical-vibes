package com.github.laxika.magicalvibes.carddata.store;

import com.github.laxika.magicalvibes.carddata.CardDataSupport;
import com.github.laxika.magicalvibes.carddata.OracleDataProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.cbor.CBORMapper;
import tools.jackson.databind.module.SimpleModule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.ToIntFunction;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * The only reader and writer of the per-set files in {@code card-data.cache-dir}.
 *
 * <p>A set is two CBOR files: {@code set-{code}.cbor}, the {@link StoredSet} that loading the set
 * reads, and {@code set-{code}.details.cbor}, the {@link StoredSetDetails} only the card browser
 * and deck validation read. Either is read if it exists and has the current
 * {@link StoredSet#CONVERSION_VERSION}. Otherwise the set is downloaded into a fresh temp directory
 * from the preferred provider ({@code oracle.data-provider}, Scryfall when absent) — or from the
 * other one if the preferred provider still fails after retries — converted, both files are
 * written to the cache directory, and the temp directory with the raw download is deleted. Only
 * the converted files are ever kept.
 *
 * <p>The fallback cannot make oracle text differ from one run to the next: it only ever applies to
 * the first fetch of a set, and the file it produces pins the data from then on, with
 * {@link StoredSet#source} recording where it came from. Stored sets never expire; a set is
 * refreshed by a new {@link StoredSet#CONVERSION_VERSION} or by deleting its file.
 *
 * <p>Thread-safe: concurrent requests for one set download it once. Separate JVMs sharing the
 * directory (parallel test forks) may both download a missing set, but the atomic write in
 * {@link CardDataSupport#writeCacheFile} means neither ever reads a partial file.
 */
@Service
public class SetDataStore {

    private static final Logger LOG = Logger.getLogger(SetDataStore.class.getName());
    /**
     * Sets are read back in the order they were written. The mapped faces hold enum sets, whose
     * written order is the enums' own; a hash set would hand them back in an order that changes
     * from one JVM to the next.
     */
    private static final ObjectMapper MAPPER = CBORMapper.builder()
            .addModule(new SimpleModule().addAbstractTypeMapping(Set.class, LinkedHashSet.class))
            .build();
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
     * What loading the set reads, downloading and storing the set first if there is no usable file.
     *
     * @throws IOException if there is no usable file and no provider could supply the set; nothing
     *                     is written in that case, so the next call tries again
     */
    public StoredSet get(String setCode) throws IOException, InterruptedException {
        synchronized (lockFor(setCode)) {
            StoredSet stored = read(fileFor(setCode), StoredSet.class, StoredSet::conversionVersion);
            return stored != null ? stored : downloadAndStore(setCode).set();
        }
    }

    /**
     * What only the card browser and deck validation read of the set, downloading and storing the
     * set first if there is no usable file. A download replaces the {@link StoredSet} too, so the
     * two never come from different downloads.
     *
     * @throws IOException as {@link #get}
     */
    public StoredSetDetails details(String setCode) throws IOException, InterruptedException {
        synchronized (lockFor(setCode)) {
            StoredSetDetails stored = read(detailsFileFor(setCode), StoredSetDetails.class,
                    StoredSetDetails::conversionVersion);
            return stored != null ? stored : downloadAndStore(setCode).details();
        }
    }

    /** Where this set's {@link StoredSet} is stored, whether or not it has been written yet. */
    public Path fileFor(String setCode) {
        return cacheDir.resolve("set-" + setCode.toLowerCase(Locale.ROOT) + ".cbor");
    }

    /** Where this set's {@link StoredSetDetails} are stored, whether or not they have been written yet. */
    public Path detailsFileFor(String setCode) {
        return cacheDir.resolve("set-" + setCode.toLowerCase(Locale.ROOT) + ".details.cbor");
    }

    private Object lockFor(String setCode) {
        return setLocks.computeIfAbsent(setCode.toLowerCase(Locale.ROOT), ignored -> new Object());
    }

    /**
     * The details are written first: the {@link StoredSet} file is what says a set is stored, so
     * it must not exist before the details it was downloaded with.
     */
    private ConvertedSet downloadAndStore(String setCode) throws IOException, InterruptedException {
        ConvertedSet downloaded = download(setCode);
        Files.createDirectories(cacheDir);
        CardDataSupport.writeCacheFile(detailsFileFor(setCode), MAPPER.writeValueAsBytes(downloaded.details()));
        CardDataSupport.writeCacheFile(fileFor(setCode), MAPPER.writeValueAsBytes(downloaded.set()));
        LOG.info("Stored " + setCode + " from " + downloaded.set().source() + " to: " + fileFor(setCode));
        return downloaded;
    }

    private static <T> T read(Path file, Class<T> type, ToIntFunction<T> conversionVersion) {
        if (!Files.exists(file)) {
            return null;
        }
        try {
            T stored = MAPPER.readValue(file.toFile(), type);
            if (conversionVersion.applyAsInt(stored) == StoredSet.CONVERSION_VERSION) {
                return stored;
            }
            LOG.info(file + " has conversion version " + conversionVersion.applyAsInt(stored) + ", expected "
                    + StoredSet.CONVERSION_VERSION + "; downloading the set again");
        } catch (JacksonException e) {
            LOG.warning(file + " is unreadable (" + e.getOriginalMessage() + "); downloading the set again");
        }
        return null;
    }

    private ConvertedSet download(String setCode) throws IOException, InterruptedException {
        List<String> failures = new ArrayList<>();
        IOException lastFailure = null;
        for (SetSource source : sourcesInPreferenceOrder) {
            try {
                ConvertedSet downloaded = downloadWithRetry(source, setCode);
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

    private ConvertedSet downloadWithRetry(SetSource source, String setCode)
            throws IOException, InterruptedException {
        for (int attempt = 1; ; attempt++) {
            Path tempDir = Files.createTempDirectory("magical-vibes-set-");
            try {
                LOG.info("Downloading " + setCode + " from " + source.provider() + "...");
                ConvertedSet downloaded;
                try {
                    downloaded = source.download(setCode, tempDir);
                } catch (JacksonException e) {
                    throw new IOException(source.provider() + " returned malformed data for set "
                            + setCode + ": " + e.getOriginalMessage(), e);
                }
                if (downloaded.set().printings().isEmpty()) {
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
