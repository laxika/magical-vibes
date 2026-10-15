package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.e.EvolvingWilds;
import com.github.laxika.magicalvibes.cards.f.FoeLiage;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IrmaPartTimeMutant.class, GrizzlyBears.class, FoeLiage.class, EvolvingWilds.class})
class IrmaPartTimeMutantTest extends BaseCardTest {

    @Test
    @DisplayName("At beginning of combat, Irma offers another creature you control")
    void targetsAnotherCreatureYouControl() {
        Permanent irma = addCreatureReady(player1, new IrmaPartTimeMutant());
        Permanent ownBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingBear = addCreatureReady(player2, new GrizzlyBears());

        advanceToBeginningOfCombat(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(ownBear.getId())
                .doesNotContain(irma.getId(), opposingBear.getId());
    }

    @Test
    @DisplayName("Irma becomes the chosen creature, keeps her name, and gets a counter")
    void copiesChosenCreatureAndGetsCounter() {
        Permanent irma = addCreatureReady(player1, new IrmaPartTimeMutant());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        advanceToBeginningOfCombat(player1);
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();

        assertThat(irma.getCard().getName()).isEqualTo("Irma, Part-Time Mutant");
        assertThat(gqs.getEffectivePower(gd, irma)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, irma)).isEqualTo(3);
        assertThat(irma.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Irma's copy ability remains available after she copies a creature")
    void copyAbilityIsRetained() {
        Permanent irma = addCreatureReady(player1, new IrmaPartTimeMutant());
        Permanent firstBear = addCreatureReady(player1, new GrizzlyBears());

        advanceToBeginningOfCombat(player1);
        harness.handlePermanentChosen(player1, firstBear.getId());
        harness.passBothPriorities();

        Permanent secondBear = addCreatureReady(player1, new GrizzlyBears());
        advanceToBeginningOfCombat(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(firstBear.getId(), secondBear.getId())
                .doesNotContain(irma.getId());

        harness.handlePermanentChosen(player1, secondBear.getId());
        harness.passBothPriorities();

        assertThat(irma.getCard().getName()).isEqualTo("Irma, Part-Time Mutant");
        assertThat(gqs.getEffectivePower(gd, irma)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, irma)).isEqualTo(4);
        assertThat(irma.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Irma gets a counter when no copy target is chosen")
    void noTargetStillAddsCounter() {
        Permanent irma = addCreatureReady(player1, new IrmaPartTimeMutant());

        advanceToBeginningOfCombat(player1);
        harness.passBothPriorities();

        assertThat(irma.getCard().getName()).isEqualTo("Irma, Part-Time Mutant");
        assertThat(gqs.getEffectivePower(gd, irma)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, irma)).isEqualTo(2);
        assertThat(irma.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void decliningAvailableTargetStillAddsCounter() {
        Permanent irma = addCreatureReady(player1, new IrmaPartTimeMutant());
        addCreatureReady(player1, new GrizzlyBears());

        advanceToBeginningOfCombat(player1);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, irma)).isEqualTo(2);
        assertThat(irma.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerDuringOpponentsCombat() {
        Permanent irma = addCreatureReady(player1, new IrmaPartTimeMutant());
        addCreatureReady(player1, new GrizzlyBears());

        advanceToBeginningOfCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(irma.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void losingControlOfTargetPreventsCopyAndCounter() {
        Permanent irma = addCreatureReady(player1, new IrmaPartTimeMutant());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        advanceToBeginningOfCombat(player1);
        harness.handlePermanentChosen(player1, bear.getId());
        gd.playerBattlefields.get(player1.getId()).remove(bear);
        gd.playerBattlefields.get(player2.getId()).add(bear);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, irma)).isEqualTo(1);
        assertThat(irma.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void copiesAbilityButNotTargetsCounters() {
        Permanent irma = addCreatureReady(player1, new IrmaPartTimeMutant());
        Permanent plant = addCreatureReady(player1, new FoeLiage());
        plant.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        advanceToBeginningOfCombat(player1);
        harness.handlePermanentChosen(player1, plant.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, irma)).isEqualTo(4);
        assertThat(irma.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.enterBattlefieldAndReturn(player1, new EvolvingWilds());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(irma.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(plant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, irma)).isEqualTo(5);
    }

    private void advanceToBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }
}
