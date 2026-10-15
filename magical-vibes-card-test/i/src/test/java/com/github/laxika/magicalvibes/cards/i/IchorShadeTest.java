package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.cards.w.WrennAndRealmbreaker;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IchorShade.class, GrizzlyBears.class, PropheticPrism.class, Forest.class,
        WrennAndRealmbreaker.class})
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

    @Test
    void doesNotTriggerWithoutAQualifyingDeath() {
        Permanent shade = harness.addToBattlefieldAndReturn(player1, new IchorShade());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(shade.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void multipleDeathsGiveOnlyOneCounter() {
        Permanent shade = harness.addToBattlefieldAndReturn(player1, new IchorShade());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new IchorShade());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new IchorShade());
        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, first);
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, second);
        });

        resolveEndStepTrigger();

        assertThat(shade.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void countsDeathBeforeShadeEnteredTheBattlefield() {
        Permanent dying = harness.addToBattlefieldAndReturn(player2, new IchorShade());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, dying));
        Permanent shade = harness.addToBattlefieldAndReturn(player1, new IchorShade());

        resolveEndStepTrigger();

        assertThat(shade.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerAtOpponentsEndStep() {
        Permanent shade = harness.addToBattlefieldAndReturn(player1, new IchorShade());
        Permanent dying = harness.addToBattlefieldAndReturn(player2, new IchorShade());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, dying));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(shade.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void countsAnimatedLandPutIntoGraveyardAsACreature() {
        harness.addToBattlefield(player1, new WrennAndRealmbreaker());
        Permanent shade = harness.addToBattlefieldAndReturn(player1, new IchorShade());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, 0, forest.getId(), null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, forest)).isTrue();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, forest));

        resolveEndStepTrigger();

        assertThat(shade.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void resolveEndStepTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();
    }
}
