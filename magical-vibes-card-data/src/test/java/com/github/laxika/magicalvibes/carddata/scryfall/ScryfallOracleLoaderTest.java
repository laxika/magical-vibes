package com.github.laxika.magicalvibes.carddata.scryfall;

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
}
