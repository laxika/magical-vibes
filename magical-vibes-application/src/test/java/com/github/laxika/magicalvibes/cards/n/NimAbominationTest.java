package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NimAbominationTest extends BaseCardTest {

    private void resolveEndStepTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Controller loses 3 life when Nim Abomination is untapped at their end step")
    void losesLifeWhenUntappedAtEndStep() {
        harness.addToBattlefield(player1, new NimAbomination());
        harness.setLife(player1, 20);

        resolveEndStepTrigger();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Does not trigger when Nim Abomination is tapped at the beginning of the end step")
    void doesNotTriggerWhenTappedAtEndStep() {
        Permanent nimAbomination = harness.addToBattlefieldAndReturn(player1, new NimAbomination());
        nimAbomination.tap();
        harness.setLife(player1, 20);

        resolveEndStepTrigger();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }
}
