package com.github.laxika.magicalvibes.scryfall;

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

class MtgjsonOracleLoaderTest {

    private static final ObjectMapper MAPPER = JsonMapper.builder().build();

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

        OracleData data = MtgjsonOracleLoader.parseOracleData(face, false);

        assertThat(data.name()).isEqualTo("Huntmaster of the Fells");
        assertThat(data.manaCost()).isEqualTo("{2}{R}{G}");
        assertThat(data.type()).isEqualTo(CardType.CREATURE);
        assertThat(data.subtypes()).containsExactly(CardSubtype.HUMAN, CardSubtype.WEREWOLF);
        assertThat(data.color()).isEqualTo(CardColor.GREEN);
        assertThat(data.colors()).containsExactly(CardColor.GREEN, CardColor.RED);
        assertThat(data.power()).isEqualTo(2);
        assertThat(data.toughness()).isEqualTo(2);
        assertThat(data.keywords()).containsExactlyInAnyOrder(Keyword.TRAMPLE, Keyword.TRANSFORM);
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

        OracleData data = MtgjsonOracleLoader.parseOracleData(face, true);

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
    void derivesLandColorFromColorIdentity() {
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

        OracleData data = MtgjsonOracleLoader.parseOracleData(face, false);

        assertThat(data.type()).isEqualTo(CardType.LAND);
        assertThat(data.supertypes()).containsExactly(CardSupertype.BASIC);
        assertThat(data.color()).isEqualTo(CardColor.GREEN);
        assertThat(data.colors()).containsExactly(CardColor.GREEN);
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

        OracleData data = MtgjsonOracleLoader.parseOracleData(face, false);

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

        OracleData data = MtgjsonOracleLoader.parseOracleData(face, false);

        assertThat(data.type()).isEqualTo(CardType.PLANESWALKER);
        assertThat(data.loyalty()).isEqualTo(3);
        assertThat(data.cardText()).isEqualTo(
                "+2: Each player draws a card.\n−1: Target player draws a card.\n−10: Target player mills twenty cards.");
    }

    @Test
    void registersCreatureTokensUnderScryfallTokenSetCode() {
        JsonNode setData = MAPPER.readTree("""
                {
                  "name": "Fake Set",
                  "code": "ZZZ",
                  "tokens": [
                    { "name": "Wolf", "number": "5", "type": "Token Creature \\u2014 Wolf",
                      "power": "2", "toughness": "2", "colors": ["G"] },
                    { "name": "Sorin Emblem", "number": "6", "type": "Emblem \\u2014 Sorin", "colors": [] }
                  ]
                }
                """);

        MtgjsonOracleLoader.registerTokens("ZZZ", setData);

        ScryfallOracleLoader.TokenImageData wolf =
                ScryfallOracleLoader.getTokenImage("ZZZ", "Wolf", 2, 2, CardColor.GREEN);
        assertThat(wolf).isNotNull();
        assertThat(wolf.setCode()).isEqualTo("tzzz");
        assertThat(wolf.collectorNumber()).isEqualTo("5");

        // The emblem is not a creature and must not be registered
        assertThat(ScryfallOracleLoader.getTokenImage("ZZZ", "Sorin Emblem", null)).isNull();
    }
}
