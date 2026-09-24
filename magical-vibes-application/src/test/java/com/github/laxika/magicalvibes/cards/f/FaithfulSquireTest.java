package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GoblinCohort;
import com.github.laxika.magicalvibes.cards.k.KaisoMemoryOfLoyalty;
import com.github.laxika.magicalvibes.cards.k.KamiOfFalseHope;
import com.github.laxika.magicalvibes.cards.t.TorrentOfStone;
import com.github.laxika.magicalvibes.cards.v.VitalSurge;
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

@CardUsed({FaithfulSquire.class, KaisoMemoryOfLoyalty.class, KamiOfFalseHope.class,
        VitalSurge.class, GoblinCohort.class, TorrentOfStone.class})
class FaithfulSquireTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a Spirit spell offers a ki counter and accepting places it")
    void spiritSpellPlacesKiCounter() {
        Permanent squire = addSquire();
        prepareMainPhase();
        harness.setHand(player1, List.of(new KamiOfFalseHope()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(squire.getCounterCount(CounterType.KI)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting an Arcane spell offers a ki counter")
    void arcaneSpellPlacesKiCounter() {
        Permanent squire = addSquire();
        prepareMainPhase();
        harness.setHand(player1, List.of(new VitalSurge()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(squire.getCounterCount(CounterType.KI)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the trigger places no ki counter")
    void decliningPlacesNoCounter() {
        Permanent squire = addSquire();
        prepareMainPhase();
        harness.setHand(player1, List.of(new VitalSurge()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(squire.getCounterCount(CounterType.KI)).isZero();
    }

    @Test
    @DisplayName("Casting a spell that is neither Spirit nor Arcane does not trigger")
    void unrelatedSpellDoesNotTrigger() {
        Permanent squire = addSquire();
        prepareMainPhase();
        harness.setHand(player1, List.of(new GoblinCohort()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Faithful Squire"));
        assertThat(squire.getCounterCount(CounterType.KI)).isZero();
    }

    @Test
    @DisplayName("An opponent casting a Spirit spell does not trigger the squire")
    void opponentCastingSpiritDoesNotTrigger() {
        Permanent squire = addSquire();
        prepareMainPhase(player2);
        harness.setHand(player2, List.of(new KamiOfFalseHope()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(squire.getCounterCount(CounterType.KI)).isZero();
    }

    @Test
    @DisplayName("Flips at the end step with two ki counters when the controller accepts")
    void flipsAtEndStepWithTwoCounters() {
        Permanent squire = addSquire();
        squire.setCounterCount(CounterType.KI, 2);

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(squire.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Flips at an opponent's end step with two ki counters when the controller accepts")
    void flipsAtOpponentsEndStepWithTwoCounters() {
        Permanent squire = addSquire();
        squire.setCounterCount(CounterType.KI, 2);

        advanceToEndStep(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(squire.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Declining the end step trigger leaves the squire unflipped")
    void decliningLeavesUnflipped() {
        Permanent squire = addSquire();
        squire.setCounterCount(CounterType.KI, 2);

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(squire.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Does not flip at the end step with only one ki counter")
    void doesNotFlipBelowTwoCounters() {
        Permanent squire = addSquire();
        squire.setCounterCount(CounterType.KI, 1);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(squire.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Does not flip if the ki counters fall below two before the trigger resolves")
    void doesNotFlipIfCountersFallBelowTwoBeforeResolution() {
        Permanent squire = addSquire();
        squire.setCounterCount(CounterType.KI, 2);

        advanceToEndStep(player1);
        squire.setCounterCount(CounterType.KI, 1);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(squire.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Kaiso removes a ki counter to prevent all damage to target creature")
    void kaisoPreventsDamage() {
        Permanent squire = addSquire();
        squire.setCounterCount(CounterType.KI, 2);
        Permanent targetCreature = addCreatureReady(player1, new GoblinCohort());

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.activateAbility(player1, 0, null, targetCreature.getId());
        harness.passBothPriorities();

        assertThat(squire.getCounterCount(CounterType.KI)).isEqualTo(1);

        harness.setHand(player1, List.of(new TorrentOfStone()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0, targetCreature.getId());
        harness.passBothPriorities();

        assertThat(targetCreature.getMarkedDamage()).isZero();
    }

    private Permanent addSquire() {
        Permanent squire = harness.addToBattlefieldAndReturn(player1, new FaithfulSquire());
        squire.setSummoningSick(false);
        return squire;
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
