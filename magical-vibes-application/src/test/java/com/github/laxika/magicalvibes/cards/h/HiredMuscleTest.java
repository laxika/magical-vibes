package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GoblinCohort;
import com.github.laxika.magicalvibes.cards.k.KamiOfFalseHope;
import com.github.laxika.magicalvibes.cards.s.Scarmaker;
import com.github.laxika.magicalvibes.cards.s.Shuko;
import com.github.laxika.magicalvibes.cards.v.VitalSurge;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HiredMuscle.class, Scarmaker.class, KamiOfFalseHope.class, VitalSurge.class,
        GoblinCohort.class, Shuko.class})
class HiredMuscleTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a Spirit spell offers a ki counter and accepting places it")
    void spiritSpellPlacesKiCounter() {
        Permanent muscle = addMuscle();
        prepareMainPhase();
        harness.setHand(player1, List.of(new KamiOfFalseHope()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(muscle.getCounterCount(CounterType.KI)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting an Arcane spell offers a ki counter")
    void arcaneSpellPlacesKiCounter() {
        Permanent muscle = addMuscle();
        prepareMainPhase();
        harness.setHand(player1, List.of(new VitalSurge()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(muscle.getCounterCount(CounterType.KI)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the trigger places no ki counter")
    void decliningPlacesNoCounter() {
        Permanent muscle = addMuscle();
        prepareMainPhase();
        harness.setHand(player1, List.of(new VitalSurge()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(muscle.getCounterCount(CounterType.KI)).isZero();
    }

    @Test
    @DisplayName("Casting a spell that is neither Spirit nor Arcane does not trigger")
    void unrelatedSpellDoesNotTrigger() {
        Permanent muscle = addMuscle();
        prepareMainPhase();
        harness.setHand(player1, List.of(new GoblinCohort()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Hired Muscle"));
        assertThat(muscle.getCounterCount(CounterType.KI)).isZero();
    }

    @Test
    @DisplayName("An opponent casting a Spirit spell does not trigger Hired Muscle")
    void opponentCastingSpiritDoesNotTrigger() {
        Permanent muscle = addMuscle();
        prepareMainPhase(player2);
        harness.setHand(player2, List.of(new KamiOfFalseHope()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(muscle.getCounterCount(CounterType.KI)).isZero();
    }

    @Test
    @DisplayName("Flips at the end step with two ki counters when the controller accepts")
    void flipsAtEndStepWithTwoCounters() {
        Permanent muscle = addMuscle();
        muscle.setCounterCount(CounterType.KI, 2);

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(muscle.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Flips at an opponent's end step with two ki counters when the controller accepts")
    void flipsAtOpponentsEndStepWithTwoCounters() {
        Permanent muscle = addMuscle();
        muscle.setCounterCount(CounterType.KI, 2);

        advanceToEndStep(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(muscle.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Declining the end step trigger leaves it unflipped")
    void decliningLeavesUnflipped() {
        Permanent muscle = addMuscle();
        muscle.setCounterCount(CounterType.KI, 2);

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(muscle.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Does not flip if the ki counters fall below two before the trigger resolves")
    void doesNotFlipIfCountersFallBelowTwoBeforeResolution() {
        Permanent muscle = addMuscle();
        muscle.setCounterCount(CounterType.KI, 2);

        advanceToEndStep(player1);
        muscle.setCounterCount(CounterType.KI, 1);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(muscle.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Flipping to Scarmaker preserves a ki counter for its activated ability")
    void flippingPreservesKiCounterForScarmaker() {
        Permanent muscle = addMuscle();
        muscle.setCounterCount(CounterType.KI, 2);
        Permanent goblin = addCreatureReady(player1, new GoblinCohort());

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.activateAbility(player1, 0, null, goblin.getId());
        harness.passBothPriorities();

        assertThat(muscle.isTransformed()).isTrue();
        assertThat(muscle.getCounterCount(CounterType.KI)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, goblin, Keyword.FEAR)).isTrue();
    }

    @Test
    @DisplayName("Does not flip at the end step with only one ki counter")
    void doesNotFlipBelowTwoCounters() {
        Permanent muscle = addMuscle();
        muscle.setCounterCount(CounterType.KI, 1);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(muscle.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Scarmaker removes a ki counter to give target creature fear")
    void scarmakerGrantsFear() {
        Permanent muscle = addFlippedMuscle();
        Permanent goblin = addCreatureReady(player1, new GoblinCohort());

        prepareMainPhase();
        harness.activateAbility(player1, 0, null, goblin.getId());
        harness.passBothPriorities();

        assertThat(muscle.getCounterCount(CounterType.KI)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, goblin, Keyword.FEAR)).isTrue();
    }

    @Test
    @DisplayName("The granted fear wears off at end of turn")
    void fearWearsOff() {
        addFlippedMuscle();
        Permanent goblin = addCreatureReady(player1, new GoblinCohort());

        prepareMainPhase();
        harness.activateAbility(player1, 0, null, goblin.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, goblin, Keyword.FEAR)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.CLEANUP);

        assertThat(gqs.hasKeyword(gd, goblin, Keyword.FEAR)).isFalse();
    }

    @Test
    @DisplayName("Scarmaker cannot activate without a ki counter")
    void scarmakerRequiresKiCounter() {
        Permanent muscle = addFlippedMuscle();
        muscle.setCounterCount(CounterType.KI, 0);
        Permanent goblin = addCreatureReady(player1, new GoblinCohort());
        prepareMainPhase();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, goblin.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("counter");
    }

    @Test
    @DisplayName("Scarmaker's ability only targets creatures")
    void scarmakerOnlyTargetsCreatures() {
        addFlippedMuscle();
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new Shuko());
        prepareMainPhase();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, equipment.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addMuscle() {
        return addCreatureReady(player1, new HiredMuscle());
    }

    private Permanent addFlippedMuscle() {
        HiredMuscle card = new HiredMuscle();
        Permanent muscle = addCreatureReady(player1, card);
        muscle.setCard(card.getBackFaceCard());
        muscle.setTransformed(true);
        muscle.setCounterCount(CounterType.KI, 2);
        return muscle;
    }

    private void prepareMainPhase() {
        prepareMainPhase(player1);
    }

    private void prepareMainPhase(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }
}
