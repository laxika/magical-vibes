package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SoulShredTest extends BaseCardTest {

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
    @DisplayName("Deals 3 damage to a nonblack creature, killing a 2/2, and gains 3 life")
    void killsNonblackCreatureAndGainsLife() {
        harness.addToBattlefield(player2, creature("Grizzly Bears", CardColor.GREEN, 2, 2));
        harness.setHand(player1, List.of(new SoulShred()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("A larger nonblack creature survives 3 damage but controller still gains 3 life")
    void largeCreatureSurvivesButLifeGainStillHappens() {
        harness.addToBattlefield(player2, creature("Wall of Stone", CardColor.RED, 0, 7));
        harness.setHand(player1, List.of(new SoulShred()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        UUID targetId = harness.getPermanentId(player2, "Wall of Stone");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Wall of Stone");
        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        harness.addToBattlefield(player2, creature("Grizzly Bears", CardColor.GREEN, 2, 2)); // legal target so the spell is playable
        harness.addToBattlefield(player2, creature("Bog Imp", CardColor.BLACK, 1, 1));
        harness.setHand(player1, List.of(new SoulShred()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        UUID targetId = harness.getPermanentId(player2, "Bog Imp");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonblack creature");
    }
}
