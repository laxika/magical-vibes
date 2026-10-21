package com.github.laxika.magicalvibes.carddata.store;

import com.github.laxika.magicalvibes.carddata.OracleDataProvider;
import com.github.laxika.magicalvibes.carddata.RawFace;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The read-or-download-and-store behaviour, with stub sources instead of the network.
 *
 * <p>Worth its own test because the real loaders only ever exercise the cache-hit path once a
 * developer machine has warmed its cache, so a regression in the download path would stay
 * invisible until someone cloned the repo fresh.
 */
class SetDataStoreTest {

    @TempDir
    Path cacheDir;

    private final List<String> downloads = new ArrayList<>();
    private final List<Path> tempDirs = new ArrayList<>();

    @Test
    void aMissingSetIsDownloadedStoredAndItsTempDirectoryDeleted() throws Exception {
        SetDataStore store = store(OracleDataProvider.SCRYFALL,
                source(OracleDataProvider.SCRYFALL, 0), source(OracleDataProvider.MTGJSON, 0));

        StoredSet set = store.get("ISD");

        assertThat(set.source()).isEqualTo(OracleDataProvider.SCRYFALL);
        assertThat(downloads).containsExactly("SCRYFALL:ISD");
        assertThat(store.fileFor("ISD")).isEqualTo(cacheDir.resolve("set-isd.json")).exists();
        try (var files = Files.list(cacheDir)) {
            assertThat(files.map(path -> path.getFileName().toString())).containsExactly("set-isd.json");
        }
        assertThat(tempDirs).isNotEmpty().allSatisfy(dir -> assertThat(dir).doesNotExist());
    }

    @Test
    void aStoredSetIsReadBackWithoutDownloadingAgain() throws Exception {
        store(OracleDataProvider.SCRYFALL, source(OracleDataProvider.SCRYFALL, 0)).get("ISD");
        downloads.clear();

        StoredSet set = store(OracleDataProvider.SCRYFALL, source(OracleDataProvider.SCRYFALL, 0)).get("ISD");

        assertThat(downloads).isEmpty();
        assertThat(set).isEqualTo(storedSet("ISD", OracleDataProvider.SCRYFALL));
    }

    @Test
    void thePreferredProviderIsTriedFirst() throws Exception {
        StoredSet set = store(OracleDataProvider.MTGJSON,
                source(OracleDataProvider.SCRYFALL, 0), source(OracleDataProvider.MTGJSON, 0)).get("ISD");

        assertThat(set.source()).isEqualTo(OracleDataProvider.MTGJSON);
        assertThat(downloads).containsExactly("MTGJSON:ISD");
    }

    @Test
    void aTransientFailureIsRetried() throws Exception {
        StoredSet set = store(OracleDataProvider.SCRYFALL,
                source(OracleDataProvider.SCRYFALL, 2), source(OracleDataProvider.MTGJSON, 0)).get("ISD");

        assertThat(set.source()).isEqualTo(OracleDataProvider.SCRYFALL);
        assertThat(downloads).containsExactly("SCRYFALL:ISD", "SCRYFALL:ISD", "SCRYFALL:ISD");
    }

    @Test
    void theOtherProviderIsUsedWhenThePreferredOneKeepsFailing() throws Exception {
        SetDataStore store = store(OracleDataProvider.SCRYFALL,
                source(OracleDataProvider.SCRYFALL, Integer.MAX_VALUE), source(OracleDataProvider.MTGJSON, 0));

        StoredSet set = store.get("ISD");

        assertThat(set.source()).isEqualTo(OracleDataProvider.MTGJSON);
        assertThat(downloads).containsExactly("SCRYFALL:ISD", "SCRYFALL:ISD", "SCRYFALL:ISD", "MTGJSON:ISD");
        assertThat(store.fileFor("ISD")).exists();
        assertThat(tempDirs).allSatisfy(dir -> assertThat(dir).doesNotExist());
    }

    /** A failure must not leave a file behind, or it would be cached forever. */
    @Test
    void whenEveryProviderFailsNothingIsStored() {
        SetDataStore store = store(OracleDataProvider.SCRYFALL,
                source(OracleDataProvider.SCRYFALL, Integer.MAX_VALUE),
                source(OracleDataProvider.MTGJSON, Integer.MAX_VALUE));

        assertThatThrownBy(() -> store.get("ISD"))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("SCRYFALL: SCRYFALL is down")
                .hasMessageContaining("MTGJSON: MTGJSON is down");
        assertThat(store.fileFor("ISD")).doesNotExist();
        assertThat(tempDirs).allSatisfy(dir -> assertThat(dir).doesNotExist());
    }

    /** An empty set is never real; the provider not knowing the set must not be stored as one. */
    @Test
    void aDownloadWithNoPrintingsCountsAsAFailure() throws Exception {
        SetSource empty = new SetSource() {
            @Override
            public OracleDataProvider provider() {
                return OracleDataProvider.SCRYFALL;
            }

            @Override
            public StoredSet download(String setCode, Path tempDir) {
                downloads.add("SCRYFALL:" + setCode);
                return new StoredSet(StoredSet.FORMAT_VERSION, setCode, null, OracleDataProvider.SCRYFALL,
                        null, Instant.EPOCH, "t" + setCode, List.of(), List.of());
            }
        };

        StoredSet set = store(OracleDataProvider.SCRYFALL, empty, source(OracleDataProvider.MTGJSON, 0)).get("ISD");

        assertThat(set.source()).isEqualTo(OracleDataProvider.MTGJSON);
    }

    @Test
    void aFileWithAnotherFormatVersionIsDownloadedAgain() throws Exception {
        Files.writeString(cacheDir.resolve("set-isd.json"), "{\"formatVersion\": 0}");

        StoredSet set = store(OracleDataProvider.SCRYFALL, source(OracleDataProvider.SCRYFALL, 0)).get("ISD");

        assertThat(downloads).containsExactly("SCRYFALL:ISD");
        assertThat(set.formatVersion()).isEqualTo(StoredSet.FORMAT_VERSION);
        assertThat(store(OracleDataProvider.SCRYFALL).get("ISD")).isEqualTo(set);
    }

    @Test
    void anUnreadableFileIsDownloadedAgain() throws Exception {
        Files.writeString(cacheDir.resolve("set-isd.json"), "{ not json");

        store(OracleDataProvider.SCRYFALL, source(OracleDataProvider.SCRYFALL, 0)).get("ISD");

        assertThat(downloads).containsExactly("SCRYFALL:ISD");
    }

    @Test
    void theProviderPropertyIsMatchedCaseInsensitivelyAndATypoIsRejected() {
        assertThat(SetDataStore.parseProvider(" mtgjson ")).isEqualTo(OracleDataProvider.MTGJSON);
        assertThatThrownBy(() -> SetDataStore.parseProvider("SCYRFALL"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SCYRFALL");
    }

    private SetDataStore store(OracleDataProvider preferred, SetSource... sources) {
        return new SetDataStore(cacheDir, preferred, List.of(sources), 3, Duration.ZERO);
    }

    /** A source that fails its first {@code failures} downloads, then serves a one-printing set. */
    private SetSource source(OracleDataProvider provider, int failures) {
        return new SetSource() {
            private int attempts;

            @Override
            public OracleDataProvider provider() {
                return provider;
            }

            @Override
            public StoredSet download(String setCode, Path tempDir) throws IOException {
                downloads.add(provider + ":" + setCode);
                tempDirs.add(tempDir);
                Files.writeString(tempDir.resolve("raw.json"), "raw provider data");
                if (attempts++ < failures) {
                    throw new IOException(provider + " is down");
                }
                return storedSet(setCode, provider);
            }
        };
    }

    private static StoredSet storedSet(String setCode, OracleDataProvider provider) {
        RawFace face = new RawFace("Grizzly Bears", "{1}{G}", "Creature — Bear", null,
                List.of("G"), List.of(), List.of("G"), "2", "2", null, null, List.of(), null);
        StoredPrinting printing = new StoredPrinting("1", "common", List.of("Grizzly Bears"), face, null,
                List.of(), new PrintVariant(List.of(), List.of(), "black", false), Map.of("modern", "legal"));
        return new StoredSet(StoredSet.FORMAT_VERSION, setCode, "Innistrad", provider, null,
                Instant.parse("2026-10-21T10:15:30Z"), "t" + setCode.toLowerCase(), List.of(printing),
                List.of(new StoredToken("2", null, face)));
    }
}
