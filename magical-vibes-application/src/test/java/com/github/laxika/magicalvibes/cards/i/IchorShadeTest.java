package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IchorShade.class, GrizzlyBears.class, PropheticPrism.class, Forest.class})
class IchorShadeTest extends BaseCardTest {

    @Test
    @DisplayName("Gets a +1/+1 counter at your end step after an artifact is put into a graveyard")
    void getsCounterAfterArtifactIsPutIntoGraveyard() {
        Permanent shade = harness.addToBattlefieldAndReturn(player1, new IchorShade());
        Permanent prism = harness.addToBattlefieldAndReturn(player2, new PropheticPrism());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, prism));

        resolveEndStepTrigger();

        assertThat(shade.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gets a +1/+1 counter at your end step after a creature is put into a graveyard")
    void getsCounterAfterCreatureIsPutIntoGraveyard() {
        Permanent shade = harness.addToBattlefieldAndReturn(player1, new IchorShade());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bears));

        resolveEndStepTrigger();

        assertThat(shade.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not get a counter when only a land is put into a graveyard")
    void doesNotGetCounterAfterLandIsPutIntoGraveyard() {
        Permanent shade = harness.addToBattlefieldAndReturn(player1, new IchorShade());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, forest));

        resolveEndStepTrigger();

        assertThat(shade.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void resolveEndStepTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
