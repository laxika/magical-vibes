package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HydroChannelerTest extends BaseCardTest {

    private static Card createSpell(String name, CardType type, String manaCost, CardColor color) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        card.setManaCost(manaCost);
        card.setColor(color);
        return card;
    }

    @Test
    @DisplayName("Blue mana from the first ability casts an instant")
    void blueAbilityCastsInstant() {
        addCreatureReady(player1, new HydroChanneler());
        harness.setHand(player1, List.of(createSpell("Test Instant", CardType.INSTANT, "{U}", CardColor.BLUE)));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Test Instant");
    }

    @Test
    @DisplayName("The first ability's mana cannot cast a creature")
    void blueAbilityCannotCastCreature() {
        addCreatureReady(player1, new HydroChanneler());
        harness.setHand(player1, List.of(createSpell("Test Creature", CardType.CREATURE, "{U}", CardColor.BLUE)));

        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The second ability's any-color mana casts an instant after paying its generic cost")
    void anyColorAbilityCastsInstant() {
        addCreatureReady(player1, new HydroChanneler());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(createSpell("Test Instant", CardType.INSTANT, "{R}", CardColor.RED)));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "RED");
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Test Instant");
    }

    @Test
    @DisplayName("The second ability's mana cannot cast a creature")
    void anyColorAbilityCannotCastCreature() {
        addCreatureReady(player1, new HydroChanneler());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(createSpell("Test Creature", CardType.CREATURE, "{G}", CardColor.GREEN)));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
