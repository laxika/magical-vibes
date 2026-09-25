package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FirstVolley;
import com.github.laxika.magicalvibes.cards.g.GoblinCohort;
import com.github.laxika.magicalvibes.cards.j.JarakuTheInterloper;
import com.github.laxika.magicalvibes.cards.k.KamiOfFalseHope;
import com.github.laxika.magicalvibes.cards.s.StreamOfConsciousness;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({CallowJushi.class, JarakuTheInterloper.class, KamiOfFalseHope.class,
        StreamOfConsciousness.class, GoblinCohort.class, FirstVolley.class})
class CallowJushiTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a Spirit spell offers a ki counter and accepting places it")
    void spiritSpellPlacesKiCounter() {
        Permanent jushi = addJushi();
        prepareMainPhase();
        harness.setHand(player1, List.of(new KamiOfFalseHope()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(jushi.getCounterCount(CounterType.KI)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting an Arcane spell offers a ki counter")
    void arcaneSpellPlacesKiCounter() {
        Permanent jushi = addJushi();
        prepareMainPhase();
        harness.setHand(player1, List.of(new StreamOfConsciousness()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(jushi.getCounterCount(CounterType.KI)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the trigger places no ki counter")
    void decliningPlacesNoCounter() {
        Permanent jushi = addJushi();
        prepareMainPhase();
        harness.setHand(player1, List.of(new StreamOfConsciousness()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(jushi.getCounterCount(CounterType.KI)).isZero();
    }

    @Test
    @DisplayName("Casting a spell that is neither Spirit nor Arcane does not trigger")
    void unrelatedSpellDoesNotTrigger() {
        Permanent jushi = addJushi();
        prepareMainPhase();
        harness.setHand(player1, List.of(new GoblinCohort()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Callow Jushi"));
        assertThat(jushi.getCounterCount(CounterType.KI)).isZero();
    }

    @Test
    @DisplayName("A Spirit cast by an opponent does not trigger Callow Jushi")
    void opponentSpiritSpellDoesNotTrigger() {
        Permanent jushi = addJushi();
        prepareMainPhase(player2);
        harness.setHand(player2, List.of(new KamiOfFalseHope()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castCreature(player2, 0);

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(jushi.getCounterCount(CounterType.KI)).isZero();
    }

    @Test
    @DisplayName("Flips at the end step with two ki counters when the controller accepts")
    void flipsAtEndStepWithTwoCounters() {
        Permanent jushi = addJushi();
        jushi.setCounterCount(CounterType.KI, 2);

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(jushi.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Declining the end step trigger leaves the jushi unflipped")
    void decliningLeavesUnflipped() {
        Permanent jushi = addJushi();
        jushi.setCounterCount(CounterType.KI, 2);

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(jushi.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Does not flip at the end step with only one ki counter")
    void doesNotFlipBelowTwoCounters() {
        Permanent jushi = addJushi();
        jushi.setCounterCount(CounterType.KI, 1);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(jushi.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Rechecks the two-counter condition when the end step trigger resolves")
    void doesNotFlipIfCounterCountFallsBelowTwoBeforeResolution() {
        Permanent jushi = addJushi();
        jushi.setCounterCount(CounterType.KI, 2);

        advanceToEndStep(player1);
        jushi.setCounterCount(CounterType.KI, 1);
        harness.passBothPriorities();

        assertThat(jushi.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("The end step trigger also occurs during an opponent's end step")
    void triggersDuringOpponentsEndStep() {
        Permanent jushi = addJushi();
        jushi.setCounterCount(CounterType.KI, 2);

        advanceToEndStep(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(jushi.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Jaraku removes a ki counter to counter a spell whose controller cannot pay {2}")
    void jarakuCountersUnpaidSpell() {
        Permanent jushi = addJushi();
        jushi.setCounterCount(CounterType.KI, 2);

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        FirstVolley firstVolley = new FirstVolley();
        harness.setHand(player2, List.of(firstVolley));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0, jushi.getId());
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, firstVolley.getId());
        harness.passBothPriorities();

        assertThat(jushi.getCounterCount(CounterType.KI)).isEqualTo(1);
        harness.assertInGraveyard(player2, "First Volley");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Jaraku does not counter a spell whose controller pays {2}")
    void jarakuDoesNotCounterWhenPaid() {
        Permanent jushi = addJushi();
        jushi.setCounterCount(CounterType.KI, 2);

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        FirstVolley firstVolley = new FirstVolley();
        harness.setHand(player2, List.of(firstVolley));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0, jushi.getId());
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, firstVolley.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(jushi.getCounterCount(CounterType.KI)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Jaraku cannot activate without a ki counter")
    void jarakuCannotActivateWithoutKiCounter() {
        Permanent jushi = addJushi();
        jushi.setCounterCount(CounterType.KI, 0);

        FirstVolley firstVolley = new FirstVolley();
        harness.setHand(player2, List.of(firstVolley));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0, jushi.getId());
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, firstVolley.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(jushi.getCounterCount(CounterType.KI)).isZero();

        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    private Permanent addJushi() {
        return addCreatureReady(player1, new CallowJushi());
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
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }
}
