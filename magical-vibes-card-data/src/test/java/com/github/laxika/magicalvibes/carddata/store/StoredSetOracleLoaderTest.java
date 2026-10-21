package com.github.laxika.magicalvibes.carddata.store;

import com.github.laxika.magicalvibes.carddata.CardPrintingRegistry;
import com.github.laxika.magicalvibes.carddata.CardPrintingRegistry.TokenImageData;
import com.github.laxika.magicalvibes.carddata.LegalitySnapshot;
import com.github.laxika.magicalvibes.carddata.OracleDataProvider;
import com.github.laxika.magicalvibes.carddata.RawFace;
import com.github.laxika.magicalvibes.carddata.SetOracleData;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.OracleData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class StoredSetOracleLoaderTest {

    private static final Instant FETCHED_AT = Instant.parse("2026-10-21T10:15:30Z");

    @Test
    void mapsOnlyImplementedPrintingsButCountsAndRatesTheWholeSet() {
        SetOracleData data = StoredSetOracleLoader.toSetOracleData("TST", set(
                List.of(printing("1", "common", face("Grizzly Bears", "Creature — Bear")),
                        printing("2", "rare", face("Llanowar Elves", "Creature — Elf Druid"))),
                List.of()), Set.of("1", "99"));

        assertThat(data.setName()).isEqualTo("Test Set");
        assertThat(data.cardTotal()).isEqualTo(2);
        assertThat(data.rarityByCollectorNumber()).containsOnly(Map.entry("1", "common"), Map.entry("2", "rare"));
        assertThat(data.frontFaceByCollectorNumber()).containsOnlyKeys("1");
        assertThat(data.frontFaceByCollectorNumber().get("1").name()).isEqualTo("Grizzly Bears");
        assertThat(data.faceNamesByCollectorNumber().get("1")).containsExactly("Grizzly Bears");
        assertThat(data.backFaceByCollectorNumber()).isEmpty();
    }

    @Test
    void mapsTheBackFaceAsABackFace() {
        RawFace front = face("Huntmaster of the Fells", "Creature — Human Werewolf");
        RawFace back = new RawFace("Ravager of the Fells", "", "Creature — Werewolf", "Trample",
                List.of(), List.of("G", "R"), List.of("G", "R"), "4", "4", null, null,
                List.of("Trample", "Transform"), "watermark");
        StoredPrinting printing = new StoredPrinting("140", "mythic", List.of(), front, back, List.of(),
                variant(), Map.of());

        SetOracleData data = StoredSetOracleLoader.toSetOracleData("TST", set(List.of(printing), List.of()),
                Set.of("140"));

        OracleData backFace = data.backFaceByCollectorNumber().get("140");
        assertThat(backFace.name()).isEqualTo("Ravager of the Fells");
        assertThat(backFace.colors()).containsExactly(CardColor.GREEN, CardColor.RED);
        assertThat(backFace.watermark()).isNull();
    }

    @Test
    void mapsASplitCardAsTheWholeCardWithItsHalves() {
        RawFace whole = new RawFace("Flotsam // Jetsam", "{1}{G/U} // {4}{U/B}{U/B}", "Instant // Sorcery",
                null, List.of("B", "G", "U"), List.of(), List.of("B", "G", "U"), null, null, null, null,
                List.of(), null, "split", "8.0");
        RawFace flotsam = new RawFace("Flotsam", "{1}{G/U}", "Instant", "Mill three cards.",
                List.of(), List.of(), List.of(), null, null, null, null, List.of(), null, "split", null);
        RawFace jetsam = new RawFace("Jetsam", "{4}{U/B}{U/B}", "Sorcery", "Each opponent mills three cards.",
                List.of(), List.of(), List.of(), null, null, null, null, List.of(), null, "split", null);
        StoredPrinting printing = new StoredPrinting("1", "uncommon", List.of("Flotsam", "Jetsam"), whole, null,
                List.of(flotsam, jetsam), variant(), Map.of());

        OracleData data = StoredSetOracleLoader.toSetOracleData("TST", set(List.of(printing), List.of()),
                Set.of("1")).frontFaceByCollectorNumber().get("1");

        assertThat(data.name()).isEqualTo("Flotsam // Jetsam");
        assertThat(data.splitHalves()).extracting(OracleData::name).containsExactly("Flotsam", "Jetsam");
    }

    @Test
    void keysTokenImagesUnderTheTokenSetSkippingEmblemsAndBackFaces() {
        SetOracleData data = StoredSetOracleLoader.toSetOracleData("ZZZ", set(List.of(), List.of(
                new StoredToken("5", null, new RawFace("Wolf", null, "Token Creature — Wolf", null,
                        List.of("G"), List.of(), List.of(), "2", "2", null, null, List.of(), null)),
                new StoredToken("7", null, new RawFace("Treasure", null, "Token Artifact — Treasure", null,
                        List.of(), List.of(), List.of(), null, null, null, null, List.of(), null)),
                new StoredToken("6", null, face("Sorin Emblem", "Emblem — Sorin")),
                new StoredToken("8", "b", face("Phyrexian", "Token Artifact Creature — Phyrexian")))),
                Set.of());

        assertThat(data.tokenImages()).containsOnly(
                Map.entry(CardPrintingRegistry.buildTokenKey("Wolf", 2, 2, CardColor.GREEN),
                        new TokenImageData("tzzz", "5")),
                Map.entry(CardPrintingRegistry.buildTokenKey("Treasure", null, null, null),
                        new TokenImageData("tzzz", "7")));
    }

    @Test
    void anImplementedTokenNumberBecomesAPrintingUnlessARealPrintingHasIt() {
        StoredToken treasure = new StoredToken("153", null, new RawFace("Treasure", null,
                "Token Artifact — Treasure", "{T}, Sacrifice this token: Add one mana of any color.",
                List.of(), List.of(), List.of(), null, null, null, null, List.of(), null));
        StoredToken shadowed = new StoredToken("1", null, face("Soldier", "Token Creature — Soldier"));

        SetOracleData data = StoredSetOracleLoader.toSetOracleData("TST", set(
                List.of(printing("1", "common", face("Grizzly Bears", "Creature — Bear"))),
                List.of(treasure, shadowed, new StoredToken("154", null, face("Food", "Token Artifact — Food")))),
                Set.of("1", "153"));

        OracleData token = data.frontFaceByCollectorNumber().get("153");
        assertThat(token.name()).isEqualTo("Treasure");
        assertThat(token.type()).isEqualTo(CardType.ARTIFACT);
        assertThat(token.subtypes()).containsExactly(CardSubtype.TREASURE);
        assertThat(data.frontFaceByCollectorNumber().get("1").name()).isEqualTo("Grizzly Bears");
        assertThat(data.cardTotal()).isEqualTo(2);
    }

    @Test
    void usesMscAlternatePrintingOnlyWhileBeginnerBoxNumberIsMissing() {
        StoredPrinting alternate = printing("834", "common", face("Ant-Man, Scott Lang", "Legendary Creature — Human Hero"));

        SetOracleData aliased = StoredSetOracleLoader.toSetOracleData("MSC", set(List.of(alternate), List.of()),
                Set.of("513"));
        assertThat(aliased.frontFaceByCollectorNumber().get("513").name()).isEqualTo("Ant-Man, Scott Lang");
        assertThat(aliased.rarityByCollectorNumber()).containsEntry("513", "common");

        SetOracleData real = StoredSetOracleLoader.toSetOracleData("MSC", set(List.of(alternate,
                printing("513", "rare", face("Ant-Man", "Legendary Creature — Human Hero"))), List.of()), Set.of("513"));
        assertThat(real.frontFaceByCollectorNumber().get("513").name()).isEqualTo("Ant-Man");
        assertThat(real.rarityByCollectorNumber()).containsEntry("513", "rare");
    }

    @Test
    void usesMb2AlternatePrintingsOnlyWhenBaseNumbersAreMissing() {
        SetOracleData data = StoredSetOracleLoader.toSetOracleData("MB2", set(List.of(
                printing("549", "rare", face("Magus of the Chains", "Creature — Human Wizard")),
                printing("551", "rare", face("Oddric, Lunar Marquis", "Legendary Creature — Human Soldier")),
                printing("584", "common", face("Spuzzem Strategist", "Creature — Human Ninja")),
                printing("315", "uncommon", face("Original Oddric", "Legendary Creature — Human Soldier"))),
                List.of()), Set.of("313", "315", "346"));

        assertThat(data.frontFaceByCollectorNumber().get("313").name()).isEqualTo("Magus of the Chains");
        assertThat(data.frontFaceByCollectorNumber().get("315").name()).isEqualTo("Original Oddric");
        assertThat(data.frontFaceByCollectorNumber().get("346").name()).isEqualTo("Spuzzem Strategist");
        assertThat(data.rarityByCollectorNumber()).containsEntry("313", "rare")
                .containsEntry("315", "uncommon").containsEntry("346", "common");
    }

    @Test
    void servesMb2EmblemPrintingNeitherProviderLists() {
        SetOracleData data = StoredSetOracleLoader.toSetOracleData("MB2", set(List.of(), List.of()), Set.of("513"));

        assertThat(data.frontFaceByCollectorNumber().get("513").name()).isEqualTo("Essence of Ajani");
        assertThat(data.frontFaceByCollectorNumber().get("513").type()).isEqualTo(CardType.EMBLEM);
        assertThat(data.rarityByCollectorNumber()).containsEntry("513", "rare");
    }

    @Test
    void legalitiesComeFromTheStoredSetDatedByItsDownload(@TempDir Path cacheDir)
            throws Exception {
        StoredPrinting printing = new StoredPrinting("1", "common", List.of(), face("Grizzly Bears", "Creature — Bear"),
                null, List.of(), variant(), Map.of("modern", "legal"));
        StoredSet stored = set(List.of(printing), List.of());
        SetSource source = new SetSource() {
            @Override
            public OracleDataProvider provider() {
                return OracleDataProvider.MTGJSON;
            }

            @Override
            public StoredSet download(String setCode, Path tempDir) {
                return stored;
            }
        };
        StoredSetOracleLoader loader = new StoredSetOracleLoader(
                new SetDataStore(cacheDir, OracleDataProvider.MTGJSON, List.of(source), 1, Duration.ZERO));

        LegalitySnapshot snapshot = loader.loadLegalities("TST");

        assertThat(snapshot.updatedAt()).isEqualTo(FETCHED_AT);
        assertThat(snapshot.cards()).containsOnly(Map.entry("1", Map.of("modern", "legal")));
    }

    private static StoredSet set(List<StoredPrinting> printings, List<StoredToken> tokens) {
        return new StoredSet(StoredSet.FORMAT_VERSION, "TST", "Test Set", OracleDataProvider.MTGJSON, null,
                FETCHED_AT, "tzzz", printings, tokens);
    }

    private static StoredPrinting printing(String number, String rarity, RawFace face) {
        return new StoredPrinting(number, rarity, List.of(face.name()), face, null, List.of(), variant(), Map.of());
    }

    private static RawFace face(String name, String typeLine) {
        return new RawFace(name, null, typeLine, null, List.of(), List.of(), List.of(),
                null, null, null, null, List.of(), null);
    }

    private static PrintVariant variant() {
        return new PrintVariant(List.of(), List.of(), "black", false);
    }
}
