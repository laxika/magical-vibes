package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.StarfieldShepherd;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InsatiableSkittermaw.class, Forest.class, StarfieldShepherd.class})
class InsatiableSkittermawTest extends BaseCardTest {

    @Test
    void putsCounterAtEndStepAfterNonlandPermanentLeaves() {
        Permanent skittermaw = addReadySkittermaw();
        Permanent departed = harness.addToBattlefieldAndReturn(player2, new InsatiableSkittermaw());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, departed));

        advanceToEndStep();

        assertThat(skittermaw.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotPutCounterWithoutVoidEvent() {
        Permanent skittermaw = addReadySkittermaw();

        advanceToEndStep();

        assertThat(skittermaw.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotPutCounterAfterOnlyLandLeaves() {
        Permanent skittermaw = addReadySkittermaw();
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, land));

        advanceToEndStep();

        assertThat(skittermaw.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void putsCounterAtEndStepAfterWarpedSpell() {
        Permanent skittermaw = addReadySkittermaw();
        StarfieldShepherd shepherd = new StarfieldShepherd();
        harness.setHand(player1, List.of(shepherd));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();
        advanceToEndStep();

        assertThat(skittermaw.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void multipleDeparturesStillGiveOnlyOneCounter() {
        Permanent skittermaw = addReadySkittermaw();
        Permanent first = harness.addToBattlefieldAndReturn(player1, new InsatiableSkittermaw());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new InsatiableSkittermaw());
        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, first);
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, second);
        });

        advanceToEndStep();

        assertThat(skittermaw.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerDuringOpponentsEndStep() {
        Permanent skittermaw = addReadySkittermaw();
        Permanent departed = harness.addToBattlefieldAndReturn(player2, new InsatiableSkittermaw());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, departed));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(skittermaw.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void departureAfterEndStepBeginsDoesNotTriggerRetroactively() {
        Permanent skittermaw = addReadySkittermaw();
        Permanent departed = harness.addToBattlefieldAndReturn(player2, new InsatiableSkittermaw());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.stack).isEmpty();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, departed));

        assertThat(gd.stack).isEmpty();
        assertThat(skittermaw.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void departureBeforeSkittermawEntersStillEnablesVoid() {
        Permanent departed = harness.addToBattlefieldAndReturn(player2, new InsatiableSkittermaw());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, departed));
        Permanent skittermaw = addReadySkittermaw();

        advanceToEndStep();

        assertThat(skittermaw.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent addReadySkittermaw() {
        return harness.addToBattlefieldAndReturn(player1, new InsatiableSkittermaw());
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();
    }
}
