package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.CallousDeceiver;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.ReachThroughMists;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HiredMuscleTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a Spirit spell offers a ki counter and accepting places it")
    void spiritSpellPlacesKiCounter() {
        Permanent muscle = addMuscle();
        prepareMainPhase();
        harness.setHand(player1, List.of(new CallousDeceiver()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

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
        harness.setHand(player1, List.of(new ReachThroughMists()));
        harness.addMana(player1, ManaColor.BLUE, 1);
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
        harness.setHand(player1, List.of(new ReachThroughMists()));
        harness.addMana(player1, ManaColor.BLUE, 1);
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
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Hired Muscle"));
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
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        prepareMainPhase();
        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(muscle.getCounterCount(CounterType.KI)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FEAR)).isTrue();
    }

    @Test
    @DisplayName("The granted fear wears off at end of turn")
    void fearWearsOff() {
        addFlippedMuscle();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        prepareMainPhase();
        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FEAR)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FEAR)).isFalse();
    }

    private Permanent addMuscle() {
        Permanent muscle = harness.addToBattlefieldAndReturn(player1, new HiredMuscle());
        muscle.setSummoningSick(false);
        return muscle;
    }

    private Permanent addFlippedMuscle() {
        HiredMuscle card = new HiredMuscle();
        Permanent muscle = new Permanent(card);
        muscle.setCard(card.getBackFaceCard());
        muscle.setTransformed(true);
        muscle.setSummoningSick(false);
        muscle.setCounterCount(CounterType.KI, 2);
        gd.playerBattlefields.get(player1.getId()).add(muscle);
        return muscle;
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
