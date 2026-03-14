package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AjanisMantraTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger may gain 1 life")
    void upkeepTriggerMayGainLife() {
        harness.addToBattlefield(player1, new AjanisMantra());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep, trigger goes on stack
        harness.passBothPriorities(); // resolve triggered ability → MayEffect prompts
        harness.handleMayAbilityChosen(player1, true); // inner effect resolves inline

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Upkeep trigger can be declined")
    void upkeepTriggerCanBeDeclined() {
        harness.addToBattlefield(player1, new AjanisMantra());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep, trigger goes on stack
        harness.passBothPriorities(); // resolve triggered ability → MayEffect prompts
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not trigger on opponent's upkeep")
    void doesNotTriggerOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new AjanisMantra());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep — no trigger for player1

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Multiple upkeeps accumulate life gain")
    void multipleUpkeepsAccumulateLifeGain() {
        harness.addToBattlefield(player1, new AjanisMantra());

        // First upkeep
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);

        // Second upkeep
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }
}
