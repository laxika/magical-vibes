package com.github.laxika.magicalvibes.carddata.mtgjson;

import com.github.laxika.magicalvibes.carddata.FaceOracleMapper;
import com.github.laxika.magicalvibes.carddata.OracleDataProvider;
import com.github.laxika.magicalvibes.carddata.store.PrintVariant;
import com.github.laxika.magicalvibes.carddata.store.StoredPrinting;
import com.github.laxika.magicalvibes.carddata.store.StoredSet;
import com.github.laxika.magicalvibes.carddata.store.StoredToken;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.OracleData;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.net.http.HttpClient;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class MtgjsonSetSourceTest {

    private static final ObjectMapper MAPPER = JsonMapper.builder().build();

    @Test
    void boundsConnectionAndRequestDuration() {
        try (HttpClient client = MtgjsonSetSource.createHttpClient()) {
            assertThat(client.connectTimeout()).contains(Duration.ofSeconds(20));
        }

        assertThat(MtgjsonSetSource.createRequest("grn").timeout())
                .contains(Duration.ofMinutes(2));
    }

    @Test
    void sendsGenericBrowserUserAgent() {
        assertThat(MtgjsonSetSource.createRequest("grn").headers().firstValue("User-Agent"))
                .hasValueSatisfying(userAgent -> assertThat(userAgent).startsWith("Mozilla/5.0").contains("Chrome/"));
    }

    @Test
    void parsesFrontFaceOfTransformCard() {
        JsonNode face = MAPPER.readTree("""
                {
                  "name": "Huntmaster of the Fells // Ravager of the Fells",
                  "faceName": "Huntmaster of the Fells",
                  "side": "a",
                  "layout": "transform",
                  "number": "140",
                  "manaCost": "{2}{R}{G}",
                  "type": "Creature \\u2014 Human Werewolf",
                  "colors": ["G", "R"],
                  "colorIdentity": ["G", "R"],
                  "power": "2",
                  "toughness": "2",
                  "keywords": ["Trample", "Transform"],
                  "rarity": "mythic",
                  "text": "Whenever this creature enters, create a 2/2 green Wolf creature token. (Reminder text.)"
                }
                """);

        OracleData data = parseOracleData(face, false);

        assertThat(data.name()).isEqualTo("Huntmaster of the Fells");
        assertThat(data.manaCost()).isEqualTo("{2}{R}{G}");
        assertThat(data.type()).isEqualTo(CardType.CREATURE);
        assertThat(data.subtypes()).containsExactly(CardSubtype.HUMAN, CardSubtype.WEREWOLF);
        assertThat(data.color()).isEqualTo(CardColor.GREEN);
        assertThat(data.colors()).containsExactly(CardColor.GREEN, CardColor.RED);
        assertThat(data.power()).isEqualTo(2);
        assertThat(data.toughness()).isEqualTo(2);
        assertThat(data.keywords()).isEmpty();
        assertThat(data.cardText()).isEqualTo("Whenever this creature enters, create a 2/2 green Wolf creature token.");
    }

    @Test
    void parsesBackFaceUsingColorIndicatorAndDroppingTransformKeyword() {
        JsonNode face = MAPPER.readTree("""
                {
                  "name": "Huntmaster of the Fells // Ravager of the Fells",
                  "faceName": "Ravager of the Fells",
                  "side": "b",
                  "layout": "transform",
                  "number": "140",
                  "manaCost": "",
                  "type": "Creature \\u2014 Werewolf",
                  "colors": ["G", "R"],
                  "colorIndicator": ["G", "R"],
                  "power": "4",
                  "toughness": "4",
                  "keywords": ["Trample", "Transform"],
                  "watermark": "somewatermark",
                  "text": "Trample"
                }
                """);

        OracleData data = parseOracleData(face, true);

        assertThat(data.name()).isEqualTo("Ravager of the Fells");
        assertThat(data.manaCost()).isNull();
        assertThat(data.subtypes()).containsExactly(CardSubtype.WEREWOLF);
        assertThat(data.colors()).containsExactly(CardColor.GREEN, CardColor.RED);
        assertThat(data.power()).isEqualTo(4);
        assertThat(data.toughness()).isEqualTo(4);
        assertThat(data.keywords()).containsExactly(Keyword.TRAMPLE);
        assertThat(data.watermark()).isNull();
    }

    @Test
    void loadsALandAsColorlessAlongsideItsColorIdentity() {
        JsonNode face = MAPPER.readTree("""
                {
                  "name": "Forest",
                  "number": "301",
                  "type": "Basic Land \\u2014 Forest",
                  "colors": [],
                  "colorIdentity": ["G"],
                  "supertypes": ["Basic"]
                }
                """);

        OracleData data = parseOracleData(face, false);

        assertThat(data.type()).isEqualTo(CardType.LAND);
        assertThat(data.supertypes()).containsExactly(CardSupertype.BASIC);
        assertThat(data.color()).isNull();
        assertThat(data.colors()).isEmpty();
        assertThat(data.colorIdentity()).containsExactly(CardColor.GREEN);
    }

    @Test
    void colorlessNonLandStaysColorlessDespiteColorIdentity() {
        JsonNode face = MAPPER.readTree("""
                {
                  "name": "Legacy Weapon",
                  "number": "1",
                  "type": "Legendary Artifact",
                  "colors": [],
                  "colorIdentity": ["W", "U", "B", "R", "G"]
                }
                """);

        OracleData data = parseOracleData(face, false);

        assertThat(data.type()).isEqualTo(CardType.ARTIFACT);
        assertThat(data.color()).isNull();
        assertThat(data.colors()).isEmpty();
    }

    @Test
    void normalizesLoyaltyAbilityBracketsAndParsesLoyalty() {
        JsonNode face = MAPPER.readTree("""
                {
                  "name": "Jace Beleren",
                  "number": "58",
                  "manaCost": "{1}{U}{U}",
                  "type": "Legendary Planeswalker \\u2014 Jace",
                  "colors": ["U"],
                  "loyalty": "3",
                  "text": "[+2]: Each player draws a card.\\n[\\u22121]: Target player draws a card.\\n[\\u221210]: Target player mills twenty cards."
                }
                """);

        OracleData data = parseOracleData(face, false);

        assertThat(data.type()).isEqualTo(CardType.PLANESWALKER);
        assertThat(data.loyalty()).isEqualTo(3);
        assertThat(data.cardText()).isEqualTo(
                "+2: Each player draws a card.\n−1: Target player draws a card.\n−10: Target player mills twenty cards.");
    }

    @Test
    void promotesMeldResultToStandalonePrintingButKeepsTransformBacksAsBackFaces() {
        JsonNode cards = MAPPER.readTree("""
                [
                  { "faceName": "Bruna, the Fading Light", "number": "14", "side": "a", "layout": "meld" },
                  { "faceName": "Brisela, Voice of Nightmares", "number": "14b", "side": "b", "layout": "meld" },
                  { "faceName": "Gisela, the Broken Blade", "number": "24", "side": "a", "layout": "meld" },
                  { "faceName": "Huntmaster of the Fells", "number": "140", "side": "a", "layout": "transform" },
                  { "faceName": "Ravager of the Fells", "number": "140", "side": "b", "layout": "transform" },
                  { "faceName": "Grizzly Bears", "number": "200" }
                ]
                """);

        MtgjsonSetSource.FaceIndex faces = MtgjsonSetSource.indexFacesByCollectorNumber(cards);

        // Meld result: side "b" with its own collector number → a standalone printing
        assertThat(faces.frontFaces().get("14b").get("faceName").asText())
                .isEqualTo("Brisela, Voice of Nightmares");
        // Transform back: shares its front face's number → stays a back face only
        assertThat(faces.frontFaces().get("140").get("faceName").asText())
                .isEqualTo("Huntmaster of the Fells");
        assertThat(faces.backFaces().get("140").get("faceName").asText())
                .isEqualTo("Ravager of the Fells");
        assertThat(faces.frontFaces()).containsOnlyKeys("14", "14b", "24", "140", "200");
    }

    @Test
    void keepsTheFrontOfAThreeFaceSplitCardAndRecordsAllFaceNames() {
        JsonNode cards = MAPPER.readTree("""
                [
                  { "faceName": "Smelt", "number": "100", "side": "a" },
                  { "faceName": "Herd", "number": "100", "side": "b" },
                  { "faceName": "Saw", "number": "100", "side": "c" }
                ]
                """);

        MtgjsonSetSource.FaceIndex faces = MtgjsonSetSource.indexFacesByCollectorNumber(cards);

        assertThat(faces.frontFaces().get("100").get("faceName").asText()).isEqualTo("Smelt");
        assertThat(faces.backFaces().get("100").get("faceName").asText()).isEqualTo("Herd");
        assertThat(faces.faceNamesByCollectorNumber().get("100"))
                .containsExactly("Smelt", "Herd", "Saw");
    }

    @Test
    void collectsEverySplitHalfInSideOrder() {
        JsonNode cards = MAPPER.readTree("""
                [
                  { "faceName": "Herd", "number": "100", "side": "b", "layout": "split" },
                  { "faceName": "Saw", "number": "100", "side": "c", "layout": "split" },
                  { "faceName": "Smelt", "number": "100", "side": "a", "layout": "split" },
                  { "faceName": "Huntmaster of the Fells", "number": "140", "side": "a", "layout": "transform" },
                  { "faceName": "Ravager of the Fells", "number": "140", "side": "b", "layout": "transform" }
                ]
                """);

        MtgjsonSetSource.FaceIndex faces = MtgjsonSetSource.indexFacesByCollectorNumber(cards);

        assertThat(faces.splitHalves().get("100")).extracting(half -> half.get("faceName").asText())
                .containsExactly("Smelt", "Herd", "Saw");
        assertThat(faces.splitHalves()).containsOnlyKeys("100");
    }

    @Test
    void storesEveryPrintingWithItsSourceDateRarityVariantAndLegalities() throws Exception {
        StoredSet set = MtgjsonSetSource.convert("ZZZ", "ZZZ", MAPPER.readTree("""
                {
                  "meta": { "date": "2026-10-01" },
                  "data": {
                    "name": "Fake Set",
                    "cards": [
                      { "name": "Grizzly Bears", "number": "1", "rarity": "common", "borderColor": "black",
                        "legalities": { "modern": "Legal" } },
                      { "name": "Grizzly Bears", "number": "300", "rarity": "rare", "borderColor": "borderless",
                        "isFullArt": true, "promoTypes": ["boosterfun"], "frameEffects": ["showcase"] }
                    ]
                  }
                }
                """), Instant.EPOCH);

        assertThat(set.source()).isEqualTo(OracleDataProvider.MTGJSON);
        assertThat(set.sourceDate()).isEqualTo("2026-10-01");
        assertThat(set.setName()).isEqualTo("Fake Set");
        assertThat(set.printings()).extracting(StoredPrinting::collectorNumber).containsExactly("1", "300");
        StoredPrinting base = set.printings().get(0);
        assertThat(base.rarity()).isEqualTo("common");
        assertThat(base.variant()).isEqualTo(new PrintVariant(List.of(), List.of(), "black", false));
        assertThat(base.legalities()).containsOnly(Map.entry("modern", "legal"));
        assertThat(set.printings().get(1).variant()).isEqualTo(
                new PrintVariant(List.of("boosterfun"), List.of("showcase"), "borderless", true));
    }

    @Test
    void storesBothFacesOfATransformCardButNoBackFaceForAMeldResult() throws Exception {
        StoredSet set = MtgjsonSetSource.convert("ZZZ", "ZZZ", MAPPER.readTree("""
                { "data": { "cards": [
                  { "faceName": "Bruna, the Fading Light", "number": "14", "side": "a", "layout": "meld" },
                  { "faceName": "Brisela, Voice of Nightmares", "number": "14b", "side": "b", "layout": "meld" },
                  { "faceName": "Huntmaster of the Fells", "number": "140", "side": "a", "layout": "transform" },
                  { "faceName": "Ravager of the Fells", "number": "140", "side": "b", "layout": "transform" }
                ] } }
                """), Instant.EPOCH);

        Map<String, StoredPrinting> printings = set.printings().stream()
                .collect(Collectors.toMap(StoredPrinting::collectorNumber, printing -> printing));
        assertThat(printings.get("140").back().name()).isEqualTo("Ravager of the Fells");
        assertThat(printings.get("140").faceNames())
                .containsExactly("Huntmaster of the Fells", "Ravager of the Fells");
        assertThat(printings.get("14b").front().name()).isEqualTo("Brisela, Voice of Nightmares");
        assertThat(printings.get("14b").back()).isNull();
    }

    @Test
    void storesEveryTokenEntryWithItsSide() throws Exception {
        StoredSet set = MtgjsonSetSource.convert("ZZZ", "ZZZ", MAPPER.readTree("""
                { "data": { "cards": [], "tokens": [
                  { "name": "Wolf", "number": "5", "type": "Token Creature \u2014 Wolf",
                    "power": "2", "toughness": "2", "colors": ["G"] },
                  { "name": "Sorin Emblem", "number": "6", "type": "Emblem \u2014 Sorin" },
                  { "name": "Incubator // Phyrexian", "faceName": "Phyrexian", "number": "7", "side": "b",
                    "type": "Token Artifact Creature \u2014 Phyrexian" }
                ] } }
                """), Instant.EPOCH);

        assertThat(set.tokenSetCode()).isEqualTo("tzzz");
        assertThat(set.tokens()).extracting(StoredToken::collectorNumber).containsExactly("5", "6", "7");
        assertThat(set.tokens()).extracting(StoredToken::side).containsExactly(null, null, "b");
        assertThat(set.tokens()).extracting(token -> token.face().name())
                .containsExactly("Wolf", "Sorin Emblem", "Phyrexian");
    }

    /**
     * MTGJSON never tags a planeswalker-deck printing; it lists it as found only in decks. Without
     * the tag the card browser would drop those printings as unknown promos.
     */
    @Test
    void tagsADeckOnlyPrintingWithoutPromoTypesAsAPlaneswalkerDeckPrinting() {
        assertThat(MtgjsonSetSource.promoTypes(MAPPER.readTree("""
                { "number": "281", "boosterTypes": ["deck"] }
                """))).containsExactly("planeswalkerdeck");
        assertThat(MtgjsonSetSource.promoTypes(MAPPER.readTree("""
                { "number": "310", "boosterTypes": ["deck"], "promoTypes": ["starterdeck"] }
                """))).containsExactly("starterdeck");
        assertThat(MtgjsonSetSource.promoTypes(MAPPER.readTree("""
                { "number": "100", "boosterTypes": ["default"] }
                """))).isEmpty();
        assertThat(MtgjsonSetSource.promoTypes(MAPPER.readTree("""
                { "number": "1" }
                """))).isEmpty();
    }

    private static OracleData parseOracleData(JsonNode face, boolean isBackFace) {
        return FaceOracleMapper.toOracleData(MtgjsonSetSource.toRawFace(face), isBackFace);
    }
}
