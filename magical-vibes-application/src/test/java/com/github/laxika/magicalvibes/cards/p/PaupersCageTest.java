package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PaupersCageTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent's upkeep with two cards in hand deals 2 damage to that opponent")
    void opponentUpkeepWithTwoCardsDealsDamage() {
        harness.addToBattlefield(player1, new PaupersCage());
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Opponent's upkeep with three cards in hand does nothing")
    void opponentUpkeepWithThreeCardsDoesNothing() {
        harness.addToBattlefield(player1, new PaupersCage());
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Controller's own upkeep never triggers, even with an empty hand")
    void ownUpkeepDoesNothing() {
        harness.addToBattlefield(player1, new PaupersCage());
        harness.setHand(player1, List.of());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Intervening-if is rechecked at resolution — no damage if the hand grows above two")
    void interveningIfCheckedAtResolution() {
        harness.addToBattlefield(player1, new PaupersCage());
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        gd.playerHands.get(player2.getId()).add(new GrizzlyBears()); // up to three before resolution
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }
}
