package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.CreditVoucher;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.e.EverflowingChalice;
import com.github.laxika.magicalvibes.cards.p.PuresteelPaladin;
import com.github.laxika.magicalvibes.cards.h.HungerOfTheHowlpack;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WakkaDevotedGuardian.class, CreditVoucher.class, GrizzlyBears.class,
        EverflowingChalice.class, PuresteelPaladin.class, HungerOfTheHowlpack.class})
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
        harness.passBothPriorities();

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

    @Test
    @DisplayName("An illegal artifact target prevents the entire combat-damage ability from resolving")
    void illegalArtifactTargetPreventsCounterPlacement() {
        Permanent wakka = addCreatureReady(player1, new WakkaDevotedGuardian());
        wakka.setAttacking(true);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new CreditVoucher());
        harness.setHand(player2, List.of());

        resolveCombat();
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.activateAbility(player2, 0, null, null);
        harness.assertInGraveyard(player2, "Credit Voucher");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(wakka.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Wakka can choose no artifact target even when the damaged player controls an artifact")
    void canChooseZeroArtifactTargets() {
        Permanent wakka = addCreatureReady(player1, new WakkaDevotedGuardian());
        wakka.setAttacking(true);
        harness.addToBattlefield(player2, new EverflowingChalice());

        resolveCombat();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Everflowing Chalice");
        assertThat(wakka.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A counter placed by an opponent enables Blitzball Captain")
    void opponentPlacedCounterEnablesEndStepTrigger() {
        Permanent wakka = addCreatureReady(player1, new WakkaDevotedGuardian());
        Permanent ally = addCreatureReady(player1, new PuresteelPaladin());
        Permanent opponent = addCreatureReady(player2, new PuresteelPaladin());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new EverflowingChalice());
        harness.setHand(player2, List.of(new HungerOfTheHowlpack()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, wakka.getId());

        assertThat(wakka.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        advanceToEndStep();

        assertThat(ally.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(wakka.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(artifact.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();
    }
}
