package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ShriekingAfflictionTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent's upkeep with an empty hand costs them 3 life")
    void opponentUpkeepWithEmptyHandLosesLife() {
        harness.addToBattlefield(player1, new ShriekingAffliction());
        harness.setHand(player2, List.of());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    @DisplayName("Opponent's upkeep with exactly one card in hand costs them 3 life")
    void opponentUpkeepWithOneCardLosesLife() {
        harness.addToBattlefield(player1, new ShriekingAffliction());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    @DisplayName("Opponent's upkeep with two cards in hand does nothing")
    void opponentUpkeepWithTwoCardsDoesNothing() {
        harness.addToBattlefield(player1, new ShriekingAffliction());
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Controller's own upkeep never triggers, even with an empty hand")
    void ownUpkeepDoesNothing() {
        harness.addToBattlefield(player1, new ShriekingAffliction());
        harness.setHand(player1, List.of());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Intervening-if is rechecked at resolution — no life loss if the hand grows past one")
    void interveningIfCheckedAtResolution() {
        harness.addToBattlefield(player1, new ShriekingAffliction());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        gd.playerHands.get(player2.getId()).add(new GrizzlyBears()); // up to two before it resolves
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }
}
