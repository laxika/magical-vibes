package com.github.laxika.magicalvibes.carddata.scryfall;

import com.github.laxika.magicalvibes.carddata.OracleDataProvider;
import com.github.laxika.magicalvibes.carddata.store.PrintVariant;
import com.github.laxika.magicalvibes.carddata.store.StoredPrinting;
import com.github.laxika.magicalvibes.carddata.store.StoredSet;
import com.github.laxika.magicalvibes.carddata.store.StoredSetOracleLoader;
import com.github.laxika.magicalvibes.carddata.store.StoredToken;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.OracleData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ScryfallSetSourceTest {

    private static final ObjectMapper MAPPER = JsonMapper.builder().build();

    @TempDir
    Path tempDir;

    @Test
    void downloadsTheSetAndItsTokenSet() throws Exception {
        List<String> fetchedSets = new ArrayList<>();
        ScryfallSetSource source = new ScryfallSetSource((setCode, dir) -> {
            fetchedSets.add(setCode);
            assertThat(dir).isEqualTo(tempDir);
            return setCode.startsWith("t")
                    ? List.of(MAPPER.readTree("""
                    { "name": "Soldier", "collector_number": "2", "type_line": "Token Creature \u2014 Soldier",
                      "power": "1", "toughness": "1", "colors": ["W"] }
                    """))
                    : List.of(MAPPER.readTree("""
                    { "name": "Grizzly Bears", "collector_number": "1", "set_name": "Conflux" }
                    """));
        });

        StoredSet set = source.download("CON", tempDir);

        assertThat(fetchedSets).containsExactly("CON", "tcon");
        assertThat(set.source()).isEqualTo(OracleDataProvider.SCRYFALL);
        assertThat(set.formatVersion()).isEqualTo(StoredSet.FORMAT_VERSION);
        assertThat(set.setCode()).isEqualTo("CON");
        assertThat(set.setName()).isEqualTo("Conflux");
        assertThat(set.tokenSetCode()).isEqualTo("tcon");
        assertThat(set.printings()).extracting(StoredPrinting::collectorNumber).containsExactly("1");
        assertThat(set.tokens()).extracting(StoredToken::collectorNumber).containsExactly("2");
        assertThat(set.tokens().getFirst().face().name()).isEqualTo("Soldier");
    }

    /** MB1's cards are served from CMB1, but the set is still stored as the MB1 the game asked for. */
    @Test
    void downloadsMb1FromCmb1() throws Exception {
        List<String> fetchedSets = new ArrayList<>();
        ScryfallSetSource source = new ScryfallSetSource((setCode, dir) -> {
            fetchedSets.add(setCode);
            return List.of();
        });

        StoredSet set = source.download("MB1", tempDir);

        assertThat(fetchedSets).containsExactly("CMB1", "tcmb1");
        assertThat(set.setCode()).isEqualTo("MB1");
        assertThat(set.tokenSetCode()).isEqualTo("tcmb1");
    }

    /** The card browser keeps every printing, so none may be dropped on the way to the store. */
    @Test
    void keepsEveryPrintingWithItsRarityVariantAndLegalities() {
        StoredSet set = ScryfallSetSource.convert("TST", "TST", List.of(
                MAPPER.readTree("""
                        { "name": "Grizzly Bears", "collector_number": "1", "rarity": "common",
                          "border_color": "black", "legalities": { "Modern": "Legal", "vintage": "legal" } }
                        """),
                MAPPER.readTree("""
                        { "name": "Grizzly Bears", "collector_number": "300", "rarity": "rare",
                          "border_color": "borderless", "full_art": true,
                          "promo_types": ["boosterfun"], "frame_effects": ["showcase"] }
                        """)), List.of(), Instant.EPOCH);

        assertThat(set.printings()).extracting(StoredPrinting::collectorNumber).containsExactly("1", "300");
        StoredPrinting base = set.printings().get(0);
        assertThat(base.rarity()).isEqualTo("common");
        assertThat(base.variant()).isEqualTo(new PrintVariant(List.of(), List.of(), "black", false));
        assertThat(base.legalities()).containsOnly(Map.entry("modern", "legal"), Map.entry("vintage", "legal"));
        assertThat(set.printings().get(1).variant()).isEqualTo(
                new PrintVariant(List.of("boosterfun"), List.of("showcase"), "borderless", true));
    }

    @Test
    void parsesEveryFaceNameOfAThreeFaceSplitCard() {
        JsonNode card = MAPPER.readTree("""
                {
                  "name": "Smelt // Herd // Saw",
                  "collector_number": "1",
                  "card_faces": [
                    { "name": "Smelt" },
                    { "name": "Herd" },
                    { "name": "Saw" }
                  ]
                }
                """);

        assertThat(ScryfallSetSource.toPrinting(card).faceNames())
                .containsExactly("Smelt", "Herd", "Saw");
    }

    /**
     * SOS prepare-spell cards use Scryfall's "prepare" layout: like transform DFCs, the front
     * face's oracle text, power and toughness live in card_faces[0] and there is no top-level
     * oracle_text. Before "prepare" was recognised as a face-bearing layout the front face parsed
     * with a null cardText, so prepared creatures rendered with an empty text box.
     */
    @Test
    void parsesFrontFaceOfPrepareLayoutCard() {
        OracleData data = parseOracleData(preparedCardNode());

        assertThat(data.cardText()).isEqualTo(
                "Flying\nWhenever you cast a creature spell, Abigale becomes prepared.");
        assertThat(data.name()).isEqualTo("Abigale, Poet Laureate");
        assertThat(data.manaCost()).isEqualTo("{1}{W}{B}");
        assertThat(data.type()).isEqualTo(CardType.CREATURE);
        assertThat(data.supertypes()).containsExactly(CardSupertype.LEGENDARY);
        assertThat(data.subtypes()).contains(CardSubtype.BIRD);
        assertThat(data.power()).isEqualTo(2);
        assertThat(data.toughness()).isEqualTo(3);
        assertThat(data.keywords()).contains(Keyword.FLYING);
    }

    @Test
    void parsesFrontFaceOfReversibleCard() {
        OracleData data = parseOracleData(MAPPER.readTree("""
                {
                  "name": "Okaun, Eye of Chaos // Okaun, Eye of Chaos",
                  "layout": "reversible_card",
                  "color_identity": ["R"],
                  "keywords": ["Partner with", "Partner", "Double"],
                  "card_faces": [
                    {
                      "name": "Okaun, Eye of Chaos",
                      "mana_cost": "{4}{R}",
                      "type_line": "Legendary Creature \\u2014 Cyclops Berserker",
                      "oracle_text": "Partner with Zndrsplt, Eye of Wisdom (When this creature enters, target player may put Zndrsplt into their hand from their library, then shuffle.)\\nAt the beginning of combat on your turn, flip a coin until you lose a flip.\\nWhenever a player wins a coin flip, double Okaun's power and toughness until end of turn.",
                      "colors": ["R"],
                      "power": "3",
                      "toughness": "3"
                    },
                    {
                      "name": "Okaun, Eye of Chaos",
                      "mana_cost": "{4}{R}",
                      "type_line": "Legendary Creature \\u2014 Cyclops Berserker",
                      "oracle_text": "Partner with Zndrsplt, Eye of Wisdom (When this creature enters, target player may put Zndrsplt into their hand from their library, then shuffle.)\\nAt the beginning of combat on your turn, flip a coin until you lose a flip.\\nWhenever a player wins a coin flip, double Okaun's power and toughness until end of turn.",
                      "colors": ["R"],
                      "power": "3",
                      "toughness": "3"
                    }
                  ]
                }
                """));

        assertThat(data.name()).isEqualTo("Okaun, Eye of Chaos");
        assertThat(data.manaCost()).isEqualTo("{4}{R}");
        assertThat(data.type()).isEqualTo(CardType.CREATURE);
        assertThat(data.cardText()).contains("Whenever a player wins a coin flip");
        assertThat(data.power()).isEqualTo(3);
        assertThat(data.toughness()).isEqualTo(3);
    }

    /** The prepare spell itself is the back face, and keeps its own cost and text. */
    @Test
    void parsesBackFaceOfPrepareLayoutCard() {
        OracleData data = parseBackFaceOracleData(preparedCardNode());

        assertThat(data.name()).isEqualTo("Heroic Stanza");
        assertThat(data.manaCost()).isEqualTo("{1}{W/B}");
        assertThat(data.type()).isEqualTo(CardType.SORCERY);
        assertThat(data.cardText()).isEqualTo("Put a +1/+1 counter on target creature.");
    }

    private static JsonNode preparedCardNode() {
        return MAPPER.readTree("""
                {
                  "name": "Abigale, Poet Laureate // Heroic Stanza",
                  "layout": "prepare",
                  "mana_cost": "{1}{W}{B} // {1}{W/B}",
                  "type_line": "Legendary Creature \\u2014 Bird Bard // Sorcery",
                  "colors": ["B", "W"],
                  "color_identity": ["B", "W"],
                  "keywords": ["Flying", "Prepared"],
                  "card_faces": [
                    {
                      "name": "Abigale, Poet Laureate",
                      "mana_cost": "{1}{W}{B}",
                      "type_line": "Legendary Creature \\u2014 Bird Bard",
                      "oracle_text": "Flying\\nWhenever you cast a creature spell, Abigale becomes prepared. (While it's prepared, you may cast a copy of its spell. Doing so unprepares it.)",
                      "power": "2",
                      "toughness": "3"
                    },
                    {
                      "name": "Heroic Stanza",
                      "mana_cost": "{1}{W/B}",
                      "type_line": "Sorcery",
                      "oracle_text": "Put a +1/+1 counter on target creature."
                    }
                  ]
                }
                """);
    }

    /**
     * Scryfall capitalizes only the first keyword of a keyword list ("Trample, reach"); we
     * capitalize all of them so the card face matches the granted-keyword line and the tooltip.
     */
    @Test
    void capitalizesEveryKeywordInAKeywordList() {
        OracleData data = parseOracleData(MAPPER.readTree("""
                {
                  "name": "Rancorous Archaic",
                  "layout": "normal",
                  "mana_cost": "{5}",
                  "type_line": "Creature \\u2014 Avatar",
                  "colors": [],
                  "keywords": ["Trample", "Reach", "Converge"],
                  "oracle_text": "Trample, reach\\nConverge \\u2014 This creature enters with a +1/+1 counter on it for each color of mana spent to cast it.",
                  "power": "2",
                  "toughness": "2"
                }
                """));

        assertThat(data.cardText()).isEqualTo(
                "Trample, Reach\nConverge — This creature enters with a +1/+1 counter on it for each color of mana spent to cast it.");
        assertThat(data.keywords()).contains(Keyword.CONVERGE);
    }

    @Test
    void readsEachSplitHalfFromItsCardFace() {
        OracleData data = parseOracleData(MAPPER.readTree("""
                {
                  "name": "Flotsam // Jetsam",
                  "layout": "split",
                  "mana_cost": "{1}{G/U} // {4}{U/B}{U/B}",
                  "cmc": 8.0,
                  "type_line": "Instant // Sorcery",
                  "colors": ["B", "G", "U"],
                  "keywords": ["Investigate", "Mill"],
                  "card_faces": [
                    { "name": "Flotsam", "mana_cost": "{1}{G/U}", "type_line": "Instant",
                      "oracle_text": "Mill three cards. Investigate." },
                    { "name": "Jetsam", "mana_cost": "{4}{U/B}{U/B}", "type_line": "Sorcery",
                      "oracle_text": "Each opponent mills three cards." }
                  ]
                }
                """));

        assertThat(data.splitHalves()).extracting(OracleData::name).containsExactly("Flotsam", "Jetsam");
        assertThat(data.splitHalves()).extracting(OracleData::manaCost)
                .containsExactly("{1}{G/U}", "{4}{U/B}{U/B}");
        assertThat(data.splitHalves()).extracting(OracleData::type)
                .containsExactly(CardType.INSTANT, CardType.SORCERY);
    }

    /** A keyword mentioned inside rules text is not part of a keyword list and stays lowercase. */
    @Test
    void leavesKeywordsMentionedInRulesTextAlone() {
        OracleData data = parseOracleData(MAPPER.readTree("""
                {
                  "name": "Trample Granter",
                  "layout": "normal",
                  "mana_cost": "{1}{G}",
                  "type_line": "Enchantment \\u2014 Aura",
                  "colors": ["G"],
                  "keywords": ["Enchant", "Trample", "Flying"],
                  "oracle_text": "Enchant creature\\nFlying\\nEnchanted creature gets +2/+2 and has trample.\\nWhen this Aura is put into a graveyard, draw a card, then discard a card."
                }
                """));

        assertThat(data.cardText()).isEqualTo("""
                Enchant creature
                Flying
                Enchanted creature gets +2/+2 and has trample.
                When this Aura is put into a graveyard, draw a card, then discard a card.""");
    }

    /** Keywords that carry a parameter are matched on their leading word. */
    @Test
    void capitalizesParameterizedKeywords() {
        OracleData data = parseOracleData(MAPPER.readTree("""
                {
                  "name": "Warded Sentry",
                  "layout": "normal",
                  "mana_cost": "{2}{W}",
                  "type_line": "Creature \\u2014 Soldier",
                  "colors": ["W"],
                  "keywords": ["Flying", "Ward", "Protection"],
                  "oracle_text": "Flying, ward {2}, protection from red",
                  "power": "2",
                  "toughness": "3"
                }
                """));

        assertThat(data.cardText()).isEqualTo("Flying, Ward {2}, Protection from red");
    }

    /**
     * Scryfall's keywords array is the union of both faces, so a back face must keep only what its
     * own text states. Avacyn, the Purifier has flying but neither the front face's flash nor its
     * vigilance; inheriting the whole list handed the back face keywords it does not have.
     */
    @Test
    void backFaceKeepsOnlyTheKeywordsItsOwnTextStates() {
        OracleData data = parseBackFaceOracleData(MAPPER.readTree("""
                {
                  "name": "Archangel Avacyn // Avacyn, the Purifier",
                  "layout": "transform",
                  "type_line": "Legendary Creature \\u2014 Angel // Legendary Creature \\u2014 Angel",
                  "colors": ["W"],
                  "color_identity": ["R", "W"],
                  "keywords": ["Flying", "Vigilance", "Transform", "Flash"],
                  "card_faces": [
                    {
                      "name": "Archangel Avacyn",
                      "mana_cost": "{3}{W}{W}",
                      "type_line": "Legendary Creature \\u2014 Angel",
                      "colors": ["W"],
                      "oracle_text": "Flash\\nFlying, vigilance\\nWhen Archangel Avacyn enters, creatures you control gain indestructible until end of turn.",
                      "power": "4",
                      "toughness": "4"
                    },
                    {
                      "name": "Avacyn, the Purifier",
                      "mana_cost": "",
                      "type_line": "Legendary Creature \\u2014 Angel",
                      "colors": ["R"],
                      "oracle_text": "Flying\\nWhen this creature transforms into Avacyn, the Purifier, it deals 3 damage to each other creature and each opponent.",
                      "power": "6",
                      "toughness": "5"
                    }
                  ]
                }
                """));

        assertThat(data.keywords()).containsExactly(Keyword.FLYING);
    }

    /** A prepare spell is a Sorcery — none of the creature front face's keywords belong to it. */
    @Test
    void backFaceOfPrepareCardInheritsNoKeywords() {
        OracleData data = parseBackFaceOracleData(preparedCardNode());

        assertThat(data.keywords()).isEmpty();
    }

    /**
     * A back face's color identity comes off the top-level card, since Scryfall carries
     * color_identity there only. Reading it off the face node instead left every transformed land
     * with an empty identity, and so with an untinted frame.
     */
    @Test
    void backFaceLandTakesItsColorIdentityFromTheWholeCard() {
        OracleData data = parseBackFaceOracleData(MAPPER.readTree("""
                {
                  "name": "Search for Azcanta // Azcanta, the Sunken Ruin",
                  "layout": "transform",
                  "type_line": "Legendary Enchantment // Legendary Land",
                  "colors": ["U"],
                  "color_identity": ["U"],
                  "keywords": ["Surveil", "Transform"],
                  "card_faces": [
                    {
                      "name": "Search for Azcanta",
                      "mana_cost": "{1}{U}",
                      "type_line": "Legendary Enchantment",
                      "colors": ["U"],
                      "oracle_text": "At the beginning of your upkeep, surveil 1."
                    },
                    {
                      "name": "Azcanta, the Sunken Ruin",
                      "mana_cost": "",
                      "type_line": "Legendary Land",
                      "colors": [],
                      "oracle_text": "{T}: Add {U}."
                    }
                  ]
                }
                """));

        assertThat(data.color()).isNull();
        assertThat(data.colors()).isEmpty();
        assertThat(data.colorIdentity()).containsExactly(CardColor.BLUE);
    }

    private static OracleData parseOracleData(JsonNode card) {
        return StoredSetOracleLoader.frontFace(ScryfallSetSource.toPrinting(card));
    }

    private static OracleData parseBackFaceOracleData(JsonNode card) {
        return StoredSetOracleLoader.backFace(ScryfallSetSource.toPrinting(card));
    }
}
