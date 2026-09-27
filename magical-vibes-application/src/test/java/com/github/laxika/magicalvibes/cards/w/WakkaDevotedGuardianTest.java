package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.CreditVoucher;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WakkaDevotedGuardian.class, CreditVoucher.class, GrizzlyBears.class})
class WakkaDevotedGuardianTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage destroys an artifact and puts a +1/+1 counter on Wakka")
    void combatDamageDestroysArtifactAndAddsCounter() {
        Permanent wakka = addCreatureReady(player1, new WakkaDevotedGuardian());
        wakka.setAttacking(true);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new CreditVoucher());

        resolveCombat();
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(wakka.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player2, "Credit Voucher");
    }

    @Test
    @DisplayName("The end-step trigger puts counters on each other creature after Wakka gets a counter")
    void endStepCountersOtherCreatures() {
        Permanent wakka = addCreatureReady(player1, new WakkaDevotedGuardian());
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());
        wakka.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();
        assertThat(wakka.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        advanceToEndStep();

        assertThat(otherCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(wakka.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The end-step trigger does not fire when no counter was put on Wakka")
    void endStepDoesNothingWithoutCounterPlacement() {
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new WakkaDevotedGuardian());

        advanceToEndStep();

        assertThat(otherCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
