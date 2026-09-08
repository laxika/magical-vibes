package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({InsatiableSkittermaw.class, Forest.class, GrizzlyBears.class, StarfieldShepherd.class})
class InsatiableSkittermawTest extends BaseCardTest {

    @Test
    void putsCounterAtEndStepAfterNonlandPermanentLeaves() {
        Permanent skittermaw = addReadySkittermaw();
        Permanent departed = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
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

    private Permanent addReadySkittermaw() {
        Permanent skittermaw = harness.addToBattlefieldAndReturn(player1, new InsatiableSkittermaw());
        skittermaw.setSummoningSick(false);
        return skittermaw;
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
