package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.o.OldGhastbark;
import com.github.laxika.magicalvibes.cards.w.WitchbaneOrb;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HollowbornBarghest.class, OldGhastbark.class, WitchbaneOrb.class})
class HollowbornBarghestTest extends BaseCardTest {

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
        harness.setHand(player1, List.of(new OldGhastbark()));
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
        harness.setHand(player2, List.of(new OldGhastbark()));
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
        gd.playerHands.get(player2.getId()).add(new OldGhastbark());
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore);
    }

    @Test
    @DisplayName("Does nothing if you draw into a card before your upkeep trigger resolves")
    void ownInterveningIfCheckedAtResolution() {
        harness.addToBattlefield(player1, new HollowbornBarghest());
        harness.setHand(player1, List.of()); // empty, triggers
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player1);
        harness.setHand(player1, List.of(new OldGhastbark()));
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore);
    }

    @Test
    @DisplayName("Emptying your hand after upkeep begins does not create a trigger")
    void ownHandBecomingEmptyAfterUpkeepDoesNotTrigger() {
        harness.addToBattlefield(player1, new HollowbornBarghest());
        harness.setHand(player1, List.of(new OldGhastbark()));
        harness.setHand(player2, List.of());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        assertThat(gd.stack).isEmpty();
        harness.setHand(player1, List.of());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Emptying an opponent's hand after upkeep begins does not create a trigger")
    void opponentHandBecomingEmptyAfterUpkeepDoesNotTrigger() {
        harness.addToBattlefield(player1, new HollowbornBarghest());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new OldGhastbark()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToUpkeep(player2);
        assertThat(gd.stack).isEmpty();
        harness.setHand(player2, List.of());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Opponent upkeep trigger still resolves after Barghest leaves the battlefield")
    void opponentTriggerResolvesWithoutSource() {
        harness.addToBattlefield(player1, new HollowbornBarghest());
        harness.setHand(player1, List.of(new OldGhastbark()));
        harness.setHand(player2, List.of());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToUpkeep(player2);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).clear();
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Opponent's hexproof does not prevent the nontargeting upkeep life loss")
    void opponentHexproofDoesNotPreventLifeLoss() {
        harness.addToBattlefield(player1, new HollowbornBarghest());
        harness.addToBattlefield(player2, new WitchbaneOrb());
        harness.setHand(player1, List.of(new OldGhastbark()));
        harness.setHand(player2, List.of());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToUpkeep(player2);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }
}
