package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DarkOfferingTest extends BaseCardTest {

    private static Card creature(String name, CardColor color, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(color);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }

    @Test
    @DisplayName("Destroys a nonblack creature and gains 3 life")
    void destroysNonblackCreatureAndGainsLife() {
        harness.addToBattlefield(player2, creature("Grizzly Bears", CardColor.GREEN, 2, 2));
        harness.setHand(player1, List.of(new DarkOffering()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        harness.addToBattlefield(player2, creature("Grizzly Bears", CardColor.GREEN, 2, 2)); // legal target so the spell is playable
        harness.addToBattlefield(player2, creature("Bog Imp", CardColor.BLACK, 1, 1));
        harness.setHand(player1, List.of(new DarkOffering()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        UUID targetId = harness.getPermanentId(player2, "Bog Imp");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonblack creature");
    }

    @Test
    @DisplayName("A creature with a regeneration shield is regenerated instead of destroyed")
    void regenerationShieldSavesCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, creature("Grizzly Bears", CardColor.GREEN, 2, 2));
        bears.setRegenerationShield(1);
        harness.setHand(player1, List.of(new DarkOffering()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        UUID targetId = bears.getId();
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player1, 23);
    }
}
