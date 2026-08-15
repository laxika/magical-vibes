package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SecondChanceTest extends BaseCardTest {

    @Test
    @DisplayName("At 5 or less life, the upkeep trigger sacrifices Second Chance and queues an extra turn")
    void lowLifeSacrificesAndQueuesExtraTurn() {
        harness.addToBattlefield(player1, new SecondChance());
        harness.setLife(player1, 5);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Second Chance");
        assertThat(gd.extraTurns).containsExactly(player1.getId());
    }

    @Test
    @DisplayName("Above 5 life, the upkeep trigger does nothing")
    void aboveThresholdDoesNothing() {
        harness.addToBattlefield(player1, new SecondChance());
        harness.setLife(player1, 6);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Second Chance");
        assertThat(gd.extraTurns).isEmpty();
    }

    @Test
    @DisplayName("Second Chance triggers only during its controller's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new SecondChance());
        harness.setLife(player1, 5);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Second Chance");
        assertThat(gd.extraTurns).isEmpty();
    }
}
