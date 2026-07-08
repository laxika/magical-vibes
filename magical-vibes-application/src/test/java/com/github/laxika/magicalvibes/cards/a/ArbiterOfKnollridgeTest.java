package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ArbiterOfKnollridgeTest extends BaseCardTest {

    @Test
    @DisplayName("ETB sets each player's life total to the highest among all players")
    void etbSetsLifeToHighest() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 30);
        harness.setHand(player1, List.of(new ArbiterOfKnollridge()));
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell (puts ETB on stack)
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(30);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(30);
    }

    @Test
    @DisplayName("ETB leaves life unchanged when all players already share the highest total")
    void etbNoChangeWhenAlreadyEqual() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new ArbiterOfKnollridge()));
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("ETB raises the caster when an opponent has more life")
    void etbRaisesCasterWhenOpponentAhead() {
        harness.setLife(player1, 5);
        harness.setLife(player2, 18);
        harness.setHand(player1, List.of(new ArbiterOfKnollridge()));
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }
}
