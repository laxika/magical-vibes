package com.github.laxika.magicalvibes.carddata.scryfall;

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

import static org.assertj.core.api.Assertions.assertThat;

class ScryfallOracleLoaderTest {

    private static final ObjectMapper MAPPER = JsonMapper.builder().build();

    /**
     * SOS prepare-spell cards use Scryfall's "prepare" layout: like transform DFCs, the front
     * face's oracle text, power and toughness live in card_faces[0] and there is no top-level
     * oracle_text. Before "prepare" was recognised as a face-bearing layout the front face parsed
     * with a null cardText, so prepared creatures rendered with an empty text box.
     */
    @Test
    void parsesFrontFaceOfPrepareLayoutCard() {
        OracleData data = ScryfallOracleLoader.parseOracleData(preparedCardNode());

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

    /** The prepare spell itself is the back face, and keeps its own cost and text. */
    @Test
    void parsesBackFaceOfPrepareLayoutCard() {
        OracleData data = ScryfallOracleLoader.parseBackFaceOracleData(preparedCardNode());

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
        OracleData data = ScryfallOracleLoader.parseOracleData(MAPPER.readTree("""
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
    }

    /** A keyword mentioned inside rules text is not part of a keyword list and stays lowercase. */
    @Test
    void leavesKeywordsMentionedInRulesTextAlone() {
        OracleData data = ScryfallOracleLoader.parseOracleData(MAPPER.readTree("""
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
        OracleData data = ScryfallOracleLoader.parseOracleData(MAPPER.readTree("""
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
        OracleData data = ScryfallOracleLoader.parseBackFaceOracleData(MAPPER.readTree("""
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
        OracleData data = ScryfallOracleLoader.parseBackFaceOracleData(preparedCardNode());

        assertThat(data.keywords()).isEmpty();
    }

    /**
     * A back face's color identity comes off the top-level card, since Scryfall carries
     * color_identity there only. Reading it off the face node instead left every transformed land
     * with an empty identity, and so with an untinted frame.
     */
    @Test
    void backFaceLandTakesItsColorIdentityFromTheWholeCard() {
        OracleData data = ScryfallOracleLoader.parseBackFaceOracleData(MAPPER.readTree("""
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
}
