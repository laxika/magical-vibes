package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed(AgentOfMasks.class)
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
}
