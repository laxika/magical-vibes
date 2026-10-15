package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AgentOfMasks.class})
class AgentOfMasksTest extends BaseCardTest {

    @Test
    @DisplayName("Each opponent loses 1 life and the controller gains that much life during their upkeep")
    void drainsOpponentDuringControllerUpkeep() {
        harness.addToBattlefield(player1, new AgentOfMasks());
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 11);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        harness.addToBattlefield(player1, new AgentOfMasks());
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Multiple Agents each drain independently during their controller's upkeep")
    void multipleAgentsDrainIndependently() {
        harness.addToBattlefield(player1, new AgentOfMasks());
        harness.addToBattlefield(player1, new AgentOfMasks());
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 12);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("The other player's Agent drains for its controller")
    void drainsForOtherController() {
        harness.addToBattlefield(player2, new AgentOfMasks());
        harness.setLife(player1, 20);
        harness.setLife(player2, 10);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 11);
    }

    @Test
    @DisplayName("The upkeep trigger resolves after Agent leaves the battlefield")
    void triggerResolvesWithoutSource() {
        harness.addToBattlefield(player1, new AgentOfMasks());
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        harness.assertLife(player1, 11);
        harness.assertLife(player2, 19);
    }
}
