package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.StarfieldShepherd;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KavaronSkywarden.class, Forest.class, GrizzlyBears.class, StarfieldShepherd.class})
class KavaronSkywardenTest extends BaseCardTest {

    @Test
    void putsCounterOnItAfterNonlandPermanentLeaves() {
        Permanent skywarden = addReadySkywarden();
        Permanent departed = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, departed));

        advanceToEndStep();

        assertThat(skywarden.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void putsCounterOnItAfterSpellIsWarped() {
        Permanent skywarden = addReadySkywarden();
        harness.setHand(player1, List.of(new StarfieldShepherd()));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        advanceToEndStep();

        assertThat(skywarden.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotPutCounterWithoutVoidEvent() {
        Permanent skywarden = addReadySkywarden();

        advanceToEndStep();

        assertThat(skywarden.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotPutCounterAfterOnlyLandLeaves() {
        Permanent skywarden = addReadySkywarden();
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, land));

        advanceToEndStep();

        assertThat(skywarden.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void putsOnlyOneCounterAfterMultipleNonlandPermanentsLeave() {
        Permanent skywarden = addReadySkywarden();
        Permanent first = harness.addToBattlefieldAndReturn(player1, new KavaronSkywarden());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new KavaronSkywarden());
        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, first);
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, second);
        });

        advanceToEndStep();

        assertThat(skywarden.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void putsCounterAfterNonlandPermanentReturnsToHandBeforeSkywardenEnters() {
        Permanent departed = harness.addToBattlefieldAndReturn(player2, new KavaronSkywarden());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, departed));
        Permanent skywarden = addReadySkywarden();

        advanceToEndStep();

        assertThat(skywarden.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerDuringOpponentsEndStep() {
        Permanent skywarden = addReadySkywarden();
        Permanent departed = harness.addToBattlefieldAndReturn(player2, new KavaronSkywarden());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, departed));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(skywarden.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void departureAfterEndStepBeginsDoesNotTriggerRetroactively() {
        Permanent skywarden = addReadySkywarden();
        Permanent departed = harness.addToBattlefieldAndReturn(player2, new KavaronSkywarden());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.stack).isEmpty();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, departed));

        assertThat(gd.stack).isEmpty();
        assertThat(skywarden.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void pendingTriggerDoesNotPutCounterOnSourceThatLeftAndReturned() {
        Permanent skywarden = addReadySkywarden();
        Permanent departed = harness.addToBattlefieldAndReturn(player2, new KavaronSkywarden());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, departed));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, skywarden));
        harness.setHand(player1, List.of());
        Permanent returned = harness.enterBattlefieldAndReturn(player1, skywarden.getCard());
        harness.passBothPriorities();

        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent addReadySkywarden() {
        harness.setHand(player1, List.<Card>of());
        return addCreatureReady(player1, new KavaronSkywarden());
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();
    }
}
