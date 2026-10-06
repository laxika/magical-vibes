package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RumblingSlum.class})
class RumblingSlumTest extends BaseCardTest {

    @Test
    @DisplayName("Two copies each trigger and damage waits for resolution")
    void multipleCopiesTriggerIndependently() {
        harness.addToBattlefield(player1, new RumblingSlum());
        harness.addToBattlefield(player1, new RumblingSlum());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);

        assertThat(gd.stack).hasSize(2);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);

        resolveAllTriggers();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Only the active player's Slum triggers when both players control one")
    void onlySecondPlayersCopyTriggersDuringSecondPlayersUpkeep() {
        harness.addToBattlefield(player1, new RumblingSlum());
        harness.addToBattlefield(player2, new RumblingSlum());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToUpkeep(player2);

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("At the beginning of its controller's upkeep, deals 1 damage to each player")
    void dealsDamageToEachPlayerOnControllerUpkeep() {
        harness.addToBattlefield(player1, new RumblingSlum());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new RumblingSlum());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToUpkeep(player2);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Triggered damage resolves even if it leaves the battlefield before resolution")
    void triggerStillDealsDamageIfSourceLeavesBeforeResolution() {
        var slum = harness.addToBattlefieldAndReturn(player1, new RumblingSlum());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, slum));
        resolveAllTriggers();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
    }
}
