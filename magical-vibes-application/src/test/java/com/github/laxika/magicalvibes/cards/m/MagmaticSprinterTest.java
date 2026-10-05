package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MagmaticSprinter.class, PropheticPrism.class})
class MagmaticSprinterTest extends BaseCardTest {

    @Test
    @DisplayName("Puts two oil counters on a target artifact you control when it enters")
    void putsOilCountersOnTargetArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new PropheticPrism());
        castSprinter();

        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        assertThat(artifact.getCounterCount(CounterType.OIL)).isEqualTo(2);
    }

    @Test
    @DisplayName("Removing two oil counters at the end step keeps it on the battlefield")
    void removesOilCountersToStayOnBattlefield() {
        Permanent sprinter = addSprinterWithOilCounters(2);

        beginEndStep();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(sprinter.getCounterCount(CounterType.OIL)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sprinter);
    }

    @Test
    @DisplayName("Declining to remove oil counters returns it to its owner's hand")
    void decliningOilPaymentReturnsItToHand() {
        addSprinterWithOilCounters(2);

        beginEndStep();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Magmatic Sprinter");
        harness.assertInHand(player1, "Magmatic Sprinter");
    }

    @Test
    @DisplayName("Returns to hand automatically when it has fewer than two oil counters")
    void insufficientOilCountersReturnItToHand() {
        addSprinterWithOilCounters(1);

        beginEndStep();

        harness.assertNotOnBattlefield(player1, "Magmatic Sprinter");
        harness.assertInHand(player1, "Magmatic Sprinter");
    }

    @Test
    @DisplayName("Can put its entrance counters on itself and spend them to stay")
    void targetsItselfAndPaysAtEndStep() {
        castSprinter();
        Permanent sprinter = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.handlePermanentChosen(player1, sprinter.getId());
        harness.passBothPriorities();

        assertThat(sprinter.getCounterCount(CounterType.OIL)).isEqualTo(2);
        beginEndStep();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(sprinter.getCounterCount(CounterType.OIL)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sprinter);
    }

    @Test
    @DisplayName("Can put oil counters on another creature you control")
    void targetsAnotherCreature() {
        Permanent other = harness.addToBattlefieldAndReturn(player1, new MagmaticSprinter());
        other.setCounterCount(CounterType.OIL, 1);
        castSprinter();
        Permanent sprinter = gd.playerBattlefields.get(player1.getId()).getLast();

        harness.handlePermanentChosen(player1, other.getId());
        harness.passBothPriorities();

        assertThat(other.getCounterCount(CounterType.OIL)).isEqualTo(3);
        assertThat(sprinter.getCounterCount(CounterType.OIL)).isZero();
    }

    @Test
    @DisplayName("Oil counters on another permanent cannot pay its own end-step cost")
    void countersOnAnotherPermanentCannotPayCost() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new PropheticPrism());
        artifact.setCounterCount(CounterType.OIL, 4);
        addSprinterWithOilCounters(0);

        beginEndStep();

        harness.assertNotOnBattlefield(player1, "Magmatic Sprinter");
        harness.assertInHand(player1, "Magmatic Sprinter");
        assertThat(artifact.getCounterCount(CounterType.OIL)).isEqualTo(4);
    }

    @Test
    @DisplayName("Removes exactly two oil counters when it has more than two")
    void removesOnlyTwoOilCounters() {
        Permanent sprinter = addSprinterWithOilCounters(4);

        beginEndStep();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(sprinter.getCounterCount(CounterType.OIL)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sprinter);
    }

    @Test
    @DisplayName("Does not return during the opponent's end step")
    void doesNotTriggerDuringOpponentsEndStep() {
        Permanent sprinter = addSprinterWithOilCounters(0);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sprinter);
        harness.assertNotInHand(player1, "Magmatic Sprinter");
    }

    @Test
    @DisplayName("Cannot target an opponent's artifact or creature")
    void rejectsOpponentsPermanents() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new PropheticPrism());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new MagmaticSprinter());
        castSprinter();
        Permanent sprinter = gd.playerBattlefields.get(player1.getId()).getFirst();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, sprinter.getId());
        harness.passBothPriorities();

        assertThat(artifact.getCounterCount(CounterType.OIL)).isZero();
        assertThat(creature.getCounterCount(CounterType.OIL)).isZero();
        assertThat(sprinter.getCounterCount(CounterType.OIL)).isEqualTo(2);
    }

    @Test
    @DisplayName("Target must still be controlled by you when the entrance ability resolves")
    void targetChangingControllerGetsNoCounters() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new PropheticPrism());
        castSprinter();
        harness.handlePermanentChosen(player1, artifact.getId());
        gd.playerBattlefields.get(player1.getId()).remove(artifact);
        gd.playerBattlefields.get(player2.getId()).add(artifact);
        gd.stolenCreatures.put(artifact.getId(), player1.getId());

        harness.passBothPriorities();

        assertThat(artifact.getCounterCount(CounterType.OIL)).isZero();
    }

    @Test
    @DisplayName("A Sprinter controlled by another player returns to its owner's hand")
    void returnsToOwnersHandInsteadOfControllersHand() {
        MagmaticSprinter card = new MagmaticSprinter();
        card.setOwnerId(player2.getId());
        Permanent sprinter = harness.addToBattlefieldAndReturn(player1, card);
        gd.stolenCreatures.put(sprinter.getId(), player2.getId());

        beginEndStep();

        harness.assertNotOnBattlefield(player1, "Magmatic Sprinter");
        harness.assertInHand(player2, "Magmatic Sprinter");
        harness.assertNotInHand(player1, "Magmatic Sprinter");
    }

    private void castSprinter() {
        harness.setHand(player1, List.of(new MagmaticSprinter()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private Permanent addSprinterWithOilCounters(int count) {
        Permanent sprinter = harness.addToBattlefieldAndReturn(player1, new MagmaticSprinter());
        sprinter.setCounterCount(CounterType.OIL, count);
        return sprinter;
    }

    private void beginEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();
    }
}
