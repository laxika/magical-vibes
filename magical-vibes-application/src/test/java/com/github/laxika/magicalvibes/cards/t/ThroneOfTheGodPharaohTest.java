package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ThroneOfTheGodPharaohTest extends BaseCardTest {

    @Test
    @DisplayName("Each opponent loses life equal to tapped creatures you control at your end step")
    void opponentLosesLifePerTappedCreature() {
        harness.addToBattlefield(player1, new ThroneOfTheGodPharaoh());

        Permanent tapped1 = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent tapped2 = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        tapped1.tap();
        tapped2.tap();
        // Untapped creature is not counted.
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        // Advance to end step (fires the trigger), then resolve it.
        gs.advanceStep(gd);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("No life loss when you control no tapped creatures")
    void noLifeLossWithoutTappedCreatures() {
        harness.addToBattlefield(player1, new ThroneOfTheGodPharaoh());
        // Untapped creature only.
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        gs.advanceStep(gd);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Does not trigger on opponent's end step")
    void doesNotTriggerOnOpponentEndStep() {
        harness.addToBattlefield(player1, new ThroneOfTheGodPharaoh());
        Permanent tapped = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        tapped.tap();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        gs.advanceStep(gd);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }
}
