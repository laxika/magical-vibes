package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KarplusanMinotaurTest extends BaseCardTest {

    @Test
    @DisplayName("Cumulative upkeep adds an age counter and may be declined")
    void cumulativeUpkeepMayBeDeclined() {
        var minotaur = harness.addToBattlefieldAndReturn(player1, new KarplusanMinotaur());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(minotaur.getCounterCount(CounterType.AGE)).isEqualTo(1);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Karplusan Minotaur");
    }

    @Test
    @DisplayName("The coin-flip trigger deals damage to a target chosen by the appropriate player")
    void coinFlipTriggerUsesTheCorrectChooser() {
        harness.addToBattlefield(player1, new KarplusanMinotaur());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        boolean won = gameLogContains("wins the coin flip for Karplusan Minotaur");
        var chooser = won ? player1 : player2;
        var target = won ? player2 : player1;
        harness.handlePermanentChosen(chooser, target.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(target.getId())).isEqualTo(19);
    }
}
