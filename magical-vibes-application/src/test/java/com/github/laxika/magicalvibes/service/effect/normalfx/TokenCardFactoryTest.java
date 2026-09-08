package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.carddata.CardPrintingRegistry;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Seeds unique set codes into {@link CardPrintingRegistry} so these cases do not clear or replace
 * the real oracle-loaded token maps used by other tests in this JVM.
 */
class TokenCardFactoryTest {

    private static final String PREFERRED = "TOKPREF";
    private static final String FALLBACK = "TOKFALL";
    private static final String UNIQUE_CREATURE = "UniqueTestBeast";

    @Test
    @DisplayName("Creature token stamps art from the source card's set when that set has it")
    void creatureUsesPreferredSet() {
        CardPrintingRegistry.registerTokenImages(PREFERRED, Map.of(
                CardPrintingRegistry.buildTokenKey(UNIQUE_CREATURE, 2, 2, CardColor.GREEN),
                new CardPrintingRegistry.TokenImageData("ttokpref", "11")));
        CardPrintingRegistry.registerTokenImages(FALLBACK, Map.of(
                CardPrintingRegistry.buildTokenKey(UNIQUE_CREATURE, 2, 2, CardColor.GREEN),
                new CardPrintingRegistry.TokenImageData("ttokfall", "99")));

        CreateTokenEffect blueprint = new CreateTokenEffect(UNIQUE_CREATURE, 2, 2, CardColor.GREEN,
                List.of(CardSubtype.BEAST), Set.of(), Set.of());
        Card token = TokenCardFactory.create(blueprint, 2, 2, PREFERRED);

        assertThat(token.getSetCode()).isEqualTo("ttokpref");
        assertThat(token.getCollectorNumber()).isEqualTo("11");
        assertThat(token.isToken()).isTrue();
    }

    @Test
    @DisplayName("Creature token falls back to another set when the source set lacks it")
    void creatureFallsBackWhenPreferredMissing() {
        CardPrintingRegistry.registerTokenImages(FALLBACK, Map.of(
                CardPrintingRegistry.buildTokenKey(UNIQUE_CREATURE, 2, 2, CardColor.RED),
                new CardPrintingRegistry.TokenImageData("ttokfall", "7")));

        CreateTokenEffect blueprint = new CreateTokenEffect(UNIQUE_CREATURE, 2, 2, CardColor.RED,
                List.of(CardSubtype.BEAST), Set.of(), Set.of());
        Card token = TokenCardFactory.create(blueprint, 2, 2, PREFERRED);

        assertThat(token.getSetCode()).isEqualTo("ttokfall");
        assertThat(token.getCollectorNumber()).isEqualTo("7");
    }

    @Test
    @DisplayName("Non-creature token stamps art from the preferred set")
    void nonCreatureUsesPreferredSet() {
        CardPrintingRegistry.registerTokenImages(PREFERRED, Map.of(
                CardPrintingRegistry.buildTokenKey("Treasure", null, null, null),
                new CardPrintingRegistry.TokenImageData("ttokpref", "3")));

        Card token = TokenCardFactory.create(CreateTokenEffect.ofTreasureToken(1), 0, 0, PREFERRED);

        assertThat(token.getSetCode()).isEqualTo("ttokpref");
        assertThat(token.getCollectorNumber()).isEqualTo("3");
        assertThat(token.isToken()).isTrue();
    }

    @Test
    @DisplayName("Non-creature token with power and toughness preserves them")
    void nonCreaturePreservesPowerAndToughness() {
        CreateTokenEffect blueprint = new CreateTokenEffect(
                CardType.ARTIFACT, 1, "Vehicle", 3, 2, null, null,
                List.of(CardSubtype.VEHICLE), Set.of(), Set.of(), false, false,
                Map.of(), List.of(), false, false, false, 0, Set.of());

        Card token = TokenCardFactory.create(blueprint, 3, 2, PREFERRED);

        assertThat(token.getPower()).isEqualTo(3);
        assertThat(token.getToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Token stays artless when no registered set has a matching printing")
    void artlessWhenNoMatchAnywhere() {
        CreateTokenEffect blueprint = new CreateTokenEffect("NoSuchTokenAnywhere", 1, 1, CardColor.BLUE,
                List.of(CardSubtype.ILLUSION), Set.of(), Set.of());
        Card token = TokenCardFactory.create(blueprint, 1, 1, PREFERRED);

        assertThat(token.getSetCode()).isNull();
        assertThat(token.getCollectorNumber()).isNull();
        assertThat(token.isToken()).isTrue();
    }

    @Test
    @DisplayName("Null source set leaves the token artless even if other sets have it")
    void nullSourceStaysArtless() {
        CardPrintingRegistry.registerTokenImages(FALLBACK, Map.of(
                CardPrintingRegistry.buildTokenKey(UNIQUE_CREATURE, 1, 1, CardColor.WHITE),
                new CardPrintingRegistry.TokenImageData("ttokfall", "1")));

        CreateTokenEffect blueprint = new CreateTokenEffect(UNIQUE_CREATURE, 1, 1, CardColor.WHITE,
                List.of(CardSubtype.SOLDIER), Set.of(), Set.of());
        Card token = TokenCardFactory.create(blueprint, 1, 1, null);

        assertThat(token.getSetCode()).isNull();
        assertThat(token.getCollectorNumber()).isNull();
    }
}
