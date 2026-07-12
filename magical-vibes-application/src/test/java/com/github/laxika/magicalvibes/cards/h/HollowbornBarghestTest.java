package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HollowbornBarghestTest extends BaseCardTest {

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances to UPKEEP
    }

    // ===== Your upkeep: each opponent loses 2 life if you have no cards in hand =====

    @Test
    @DisplayName("Your upkeep with empty hand drains each opponent 2 life")
    void ownUpkeepEmptyHandDrainsOpponents() {
        harness.addToBattlefield(player1, new HollowbornBarghest());
        harness.setHand(player1, List.of());
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore - 2);
    }

    @Test
    @DisplayName("Your upkeep with cards in hand does nothing")
    void ownUpkeepWithCardsDoesNothing() {
        harness.addToBattlefield(player1, new HollowbornBarghest());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore);
    }

    // ===== Each opponent's upkeep: that player loses 2 life if they have no cards in hand =====

    @Test
    @DisplayName("Opponent's upkeep with empty hand makes that opponent lose 2 life")
    void opponentUpkeepEmptyHandLosesLife() {
        harness.addToBattlefield(player1, new HollowbornBarghest());
        harness.setHand(player2, List.of());
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore - 2);
    }

    @Test
    @DisplayName("Opponent's upkeep with cards in hand does nothing")
    void opponentUpkeepWithCardsDoesNothing() {
        harness.addToBattlefield(player1, new HollowbornBarghest());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore);
    }

    // ===== Intervening-if re-checked at resolution =====

    @Test
    @DisplayName("Does nothing if the opponent draws into a card before resolution")
    void interveningIfCheckedAtResolution() {
        harness.addToBattlefield(player1, new HollowbornBarghest());
        harness.setHand(player2, List.of()); // empty, triggers
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        // Trigger is on the stack — give the opponent a card before it resolves
        gd.playerHands.get(player2.getId()).add(new GrizzlyBears());
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore);
    }
}
