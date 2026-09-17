package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IrmaPartTimeMutant.class, GrizzlyBears.class})
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
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(StepTriggerService.class)
                .handleBeginningOfCombatTriggers(gd));

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

    private void advanceToBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(StepTriggerService.class)
                .handleBeginningOfCombatTriggers(gd));
    }
}
