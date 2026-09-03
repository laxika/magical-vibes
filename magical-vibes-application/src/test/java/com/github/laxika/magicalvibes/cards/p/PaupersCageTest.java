package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(PaupersCage.class)
class PaupersCageTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent's upkeep with two cards in hand deals 2 damage to that opponent")
    void opponentUpkeepWithTwoCardsDealsDamage() {
        harness.addToBattlefield(player1, new PaupersCage());
        harness.setHand(player2, List.of(new PaupersCage(), new PaupersCage()));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Opponent's upkeep with an empty hand deals 2 damage to that opponent")
    void opponentUpkeepWithEmptyHandDealsDamage() {
        harness.addToBattlefield(player1, new PaupersCage());
        harness.setHand(player2, List.of());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Opponent's upkeep with three cards in hand does nothing")
    void opponentUpkeepWithThreeCardsDoesNothing() {
        harness.addToBattlefield(player1, new PaupersCage());
        harness.setHand(player2, List.of(new PaupersCage(), new PaupersCage(), new PaupersCage()));
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
        harness.setHand(player2, List.of(new PaupersCage(), new PaupersCage()));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.setHand(player2, List.of(new PaupersCage(), new PaupersCage(), new PaupersCage()));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Triggered ability still resolves if Paupers' Cage leaves before resolution")
    void triggeredAbilityResolvesAfterSourceLeavesBattlefield() {
        var cage = harness.addToBattlefieldAndReturn(player1, new PaupersCage());
        harness.setHand(player2, List.of(new PaupersCage(), new PaupersCage()));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, cage));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }
}
