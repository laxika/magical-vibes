package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ScuzzbackScroungerTest extends BaseCardTest {

    @Test
    void acceptingFirstMainPhaseTriggerBlightsAndCreatesTreasure() {
        Permanent scrounger = harness.addToBattlefieldAndReturn(player1, new ScuzzbackScrounger());

        advanceToPrecombatMain(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(scrounger.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
    }

    @Test
    void decliningFirstMainPhaseTriggerDoesNothing() {
        Permanent scrounger = harness.addToBattlefieldAndReturn(player1, new ScuzzbackScrounger());

        advanceToPrecombatMain(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(scrounger.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(countPermanents(player1, "Treasure")).isZero();
    }

    private void advanceToPrecombatMain(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
