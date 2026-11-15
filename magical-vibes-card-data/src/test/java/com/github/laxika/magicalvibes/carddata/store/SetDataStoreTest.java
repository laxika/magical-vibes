package com.github.laxika.magicalvibes.carddata.store;

import com.github.laxika.magicalvibes.carddata.OracleDataProvider;
import com.github.laxika.magicalvibes.carddata.RawFace;
import com.github.laxika.magicalvibes.model.OracleData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.cbor.CBORMapper;

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

    private static final ObjectMapper CBOR = CBORMapper.builder().build();

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
        assertThat(store.fileFor("ISD")).isEqualTo(cacheDir.resolve("set-isd.cbor")).exists();
        try (var files = Files.list(cacheDir)) {
            assertThat(files.map(path -> path.getFileName().toString()))
                    .containsExactlyInAnyOrder("set-isd.cbor", "set-isd.details.cbor");
        }
        assertThat(tempDirs).isNotEmpty().allSatisfy(dir -> assertThat(dir).doesNotExist());
    }

    @Test
    void aStoredSetIsReadBackWithoutDownloadingAgain() throws Exception {
        store(OracleDataProvider.SCRYFALL, source(OracleDataProvider.SCRYFALL, 0)).get("ISD");
        downloads.clear();

        StoredSet set = store(OracleDataProvider.SCRYFALL, source(OracleDataProvider.SCRYFALL, 0)).get("ISD");

        assertThat(downloads).isEmpty();
        assertThat(set).isEqualTo(convertedSet("ISD", OracleDataProvider.SCRYFALL).set());
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
            public ConvertedSet download(String setCode, Path tempDir) {
                downloads.add("SCRYFALL:" + setCode);
                return ConvertedSet.of(setCode, null, OracleDataProvider.SCRYFALL,
                        null, Instant.EPOCH, "t" + setCode, List.of(), List.of());
            }
        };

        StoredSet set = store(OracleDataProvider.SCRYFALL, empty, source(OracleDataProvider.MTGJSON, 0)).get("ISD");

        assertThat(set.source()).isEqualTo(OracleDataProvider.MTGJSON);
    }

    @Test
    void aFileWithAnotherConversionVersionIsDownloadedAgain() throws Exception {
        StoredSet stale = convertedSet("ISD", OracleDataProvider.MTGJSON).set();
        Files.write(cacheDir.resolve("set-isd.cbor"), CBOR.writeValueAsBytes(new StoredSet(
                StoredSet.CONVERSION_VERSION - 1, stale.setCode(), stale.setName(), stale.source(),
                stale.sourceDate(), stale.tokenSetCode(), stale.printings(), stale.tokens())));

        StoredSet set = store(OracleDataProvider.SCRYFALL, source(OracleDataProvider.SCRYFALL, 0)).get("ISD");

        assertThat(downloads).containsExactly("SCRYFALL:ISD");
        assertThat(set.conversionVersion()).isEqualTo(StoredSet.CONVERSION_VERSION);
        assertThat(store(OracleDataProvider.SCRYFALL).get("ISD")).isEqualTo(set);
    }

    @Test
    void anUnreadableFileIsDownloadedAgain() throws Exception {
        Files.writeString(cacheDir.resolve("set-isd.cbor"), "{ not cbor");

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
            public ConvertedSet download(String setCode, Path tempDir) throws IOException {
                downloads.add(provider + ":" + setCode);
                tempDirs.add(tempDir);
                Files.writeString(tempDir.resolve("raw.json"), "raw provider data");
                if (attempts++ < failures) {
                    throw new IOException(provider + " is down");
                }
                return convertedSet(setCode, provider);
            }
        };
    }

    @Test
    void theDetailsAreStoredBesideTheSetAndReadBackWithoutDownloadingAgain() throws Exception {
        store(OracleDataProvider.SCRYFALL, source(OracleDataProvider.SCRYFALL, 0)).get("ISD");
        downloads.clear();

        StoredSetDetails details = store(OracleDataProvider.SCRYFALL,
                source(OracleDataProvider.SCRYFALL, 0)).details("ISD");

        assertThat(downloads).isEmpty();
        assertThat(details).isEqualTo(convertedSet("ISD", OracleDataProvider.SCRYFALL).details());
        assertThat(details.printings().getFirst().legalities()).containsEntry("modern", "legal");
    }

    /** The two files must come from one download, so a lost details file replaces the set too. */
    @Test
    void missingDetailsDownloadTheWholeSetAgain() throws Exception {
        SetDataStore first = store(OracleDataProvider.SCRYFALL, source(OracleDataProvider.SCRYFALL, 0));
        first.get("ISD");
        Files.delete(first.detailsFileFor("ISD"));
        downloads.clear();

        SetDataStore store = store(OracleDataProvider.MTGJSON, source(OracleDataProvider.MTGJSON, 0));
        StoredSetDetails details = store.details("ISD");

        assertThat(downloads).containsExactly("MTGJSON:ISD");
        assertThat(details.printings()).hasSize(1);
        assertThat(store.get("ISD").source()).isEqualTo(OracleDataProvider.MTGJSON);
    }

    @Test
    void detailsWithAnotherConversionVersionAreDownloadedAgain() throws Exception {
        StoredSetDetails stale = convertedSet("ISD", OracleDataProvider.SCRYFALL).details();
        Files.write(cacheDir.resolve("set-isd.details.cbor"), CBOR.writeValueAsBytes(new StoredSetDetails(
                StoredSet.CONVERSION_VERSION - 1, stale.setCode(), stale.fetchedAt(), stale.printings())));

        StoredSetDetails details = store(OracleDataProvider.SCRYFALL,
                source(OracleDataProvider.SCRYFALL, 0)).details("ISD");

        assertThat(downloads).containsExactly("SCRYFALL:ISD");
        assertThat(details.conversionVersion()).isEqualTo(StoredSet.CONVERSION_VERSION);
    }

    @Test
    void mappedFacesAreReadBackAsTheyWereWritten() throws Exception {
        RawFace front = new RawFace("Huntmaster of the Fells // Ravager of the Fells", "{2}{R}{G}",
                "Legendary Artifact Creature — Human Werewolf",
                "Vigilance, trample, haste\nWhen this creature enters, you gain 2 life.",
                List.of("G", "R"), List.of(), List.of("G", "R"), "2", "2", null, null,
                List.of("Haste", "Trample", "Vigilance"), "mirran", "transform", "4.0");
        RawFace back = new RawFace("Ravager of the Fells", "", "Creature — Werewolf", "Trample",
                List.of("G", "R"), List.of("G", "R"), List.of("G", "R"), "4", "4", null, null,
                List.of("Trample"), null, "transform", "4.0");
        RawFace whole = new RawFace("Flotsam // Jetsam", "{1}{G/U} // {4}{U/B}{U/B}", "Instant // Sorcery",
                "Mill three cards.", List.of("B", "G", "U"), List.of(), List.of("B", "G", "U"),
                null, null, null, null, List.of(), null, "split", "8.0");
        RawFace flotsam = new RawFace("Flotsam", "{1}{G/U}", "Instant", "Mill three cards.",
                List.of("G", "U"), List.of(), List.of("B", "G", "U"), null, null, null, null, List.of(), null);
        RawFace jetsam = new RawFace("Jetsam", "{4}{U/B}{U/B}", "Sorcery", "Each opponent mills three cards.",
                List.of("B", "U"), List.of(), List.of("B", "G", "U"), null, null, null, null, List.of(), null);
        PrintVariant variant = new PrintVariant(List.of(), List.of(), "black", false);
        ConvertedSet written = ConvertedSet.of("ISD", "Innistrad",
                OracleDataProvider.SCRYFALL, null, Instant.parse("2026-10-21T10:15:30Z"), "tisd",
                List.of(StoredSetConverter.printing("140", "mythic", List.of(), front, back, List.of(),
                                variant, Map.of()),
                        StoredSetConverter.printing("200", "uncommon", List.of("Flotsam", "Jetsam"), whole, null,
                                List.of(flotsam, jetsam), variant, Map.of())),
                List.of());
        SetSource source = new SetSource() {
            @Override
            public OracleDataProvider provider() {
                return OracleDataProvider.SCRYFALL;
            }

            @Override
            public ConvertedSet download(String setCode, Path tempDir) {
                return written;
            }
        };
        store(OracleDataProvider.SCRYFALL, source).get("ISD");

        StoredSet read = store(OracleDataProvider.SCRYFALL).get("ISD");
        StoredSetDetails readDetails = store(OracleDataProvider.SCRYFALL).details("ISD");

        assertThat(read).isEqualTo(written.set());
        assertThat(readDetails).isEqualTo(written.details());
        OracleData readFront = read.printings().get(0).front();
        OracleData writtenFront = written.set().printings().get(0).front();
        assertThat(readFront.keywords()).containsExactlyElementsOf(writtenFront.keywords());
        assertThat(readFront.keywords()).hasSize(3);
        assertThat(readFront.additionalTypes()).containsExactlyElementsOf(writtenFront.additionalTypes());
        assertThat(readFront.supertypes()).containsExactlyElementsOf(writtenFront.supertypes());
        OracleData split = read.printings().get(1).front();
        assertThat(split.splitHalves()).extracting(OracleData::name).containsExactly("Flotsam", "Jetsam");
        assertThat(split.combinedManaValue()).isEqualTo(8);
        assertThat(readDetails.printings().get(1).front().browseOracle(split).name()).isEqualTo("Flotsam");
        assertThat(readDetails.printings().get(0).front().browse()).isNull();
    }

    private static ConvertedSet convertedSet(String setCode, OracleDataProvider provider) {
        RawFace face = new RawFace("Grizzly Bears", "{1}{G}", "Creature — Bear", null,
                List.of("G"), List.of(), List.of("G"), "2", "2", null, null, List.of(), null);
        ConvertedPrinting printing = StoredSetConverter.printing("1", "common", List.of("Grizzly Bears"), face, null,
                List.of(), new PrintVariant(List.of(), List.of(), "black", false), Map.of("modern", "legal"));
        return ConvertedSet.of(setCode, "Innistrad", provider, null,
                Instant.parse("2026-10-21T10:15:30Z"), "t" + setCode.toLowerCase(), List.of(printing),
                List.of(StoredSetConverter.token("2", null, face)));
    }
}
