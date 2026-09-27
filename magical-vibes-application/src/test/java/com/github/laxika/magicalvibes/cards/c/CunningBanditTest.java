package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AzamukiTreacheryIncarnate;
import com.github.laxika.magicalvibes.cards.g.GoblinCohort;
import com.github.laxika.magicalvibes.cards.k.KamiOfFalseHope;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CunningBandit.class, AzamukiTreacheryIncarnate.class, VitalSurge.class,
        KamiOfFalseHope.class, GoblinCohort.class})
class CunningBanditTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an Arcane spell offers a ki counter and accepting places it")
    void arcaneSpellPlacesKiCounter() {
        Permanent bandit = addBandit();
        prepareMainPhase();
        harness.setHand(player1, List.of(new VitalSurge()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(bandit.getCounterCount(CounterType.KI)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the trigger places no ki counter")
    void decliningPlacesNoCounter() {
        Permanent bandit = addBandit();
        prepareMainPhase();
        harness.setHand(player1, List.of(new VitalSurge()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(bandit.getCounterCount(CounterType.KI)).isZero();
    }

    @Test
    @DisplayName("Casting a Spirit spell offers a ki counter")
    void spiritSpellPlacesKiCounter() {
        Permanent bandit = addBandit();
        prepareMainPhase();
        harness.setHand(player1, List.of(new KamiOfFalseHope()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(bandit.getCounterCount(CounterType.KI)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a spell that is neither Spirit nor Arcane does not trigger")
    void unrelatedSpellDoesNotTrigger() {
        Permanent bandit = addBandit();
        prepareMainPhase();
        harness.setHand(player1, List.of(new GoblinCohort()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Cunning Bandit"));
        assertThat(bandit.getCounterCount(CounterType.KI)).isZero();
    }

    @Test
    @DisplayName("An opponent casting a Spirit spell does not trigger the bandit")
    void opponentCastingSpiritDoesNotTrigger() {
        Permanent bandit = addBandit();
        prepareMainPhase(player2);
        harness.setHand(player2, List.of(new KamiOfFalseHope()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castCreature(player2, 0);

        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Cunning Bandit"));
        assertThat(bandit.getCounterCount(CounterType.KI)).isZero();
    }

    @Test
    @DisplayName("Flips at the end step with two ki counters when the controller accepts")
    void flipsAtEndStepWithTwoCounters() {
        Permanent bandit = addBandit();
        bandit.setCounterCount(CounterType.KI, 2);

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(bandit.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Declining the end step trigger leaves the bandit unflipped")
    void decliningLeavesUnflipped() {
        Permanent bandit = addBandit();
        bandit.setCounterCount(CounterType.KI, 2);

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(bandit.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Does not flip at the end step with only one ki counter")
    void doesNotFlipBelowTwoCounters() {
        Permanent bandit = addBandit();
        bandit.setCounterCount(CounterType.KI, 1);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(bandit.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Does not flip if the ki counters fall below two before the trigger resolves")
    void doesNotFlipIfCountersFallBelowTwoBeforeResolution() {
        Permanent bandit = addBandit();
        bandit.setCounterCount(CounterType.KI, 2);

        advanceToEndStep(player1);
        bandit.setCounterCount(CounterType.KI, 1);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(bandit.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Flips during an opponent's end step when the controller accepts")
    void flipsAtOpponentsEndStepWithTwoCounters() {
        Permanent bandit = addBandit();
        bandit.setCounterCount(CounterType.KI, 2);

        advanceToEndStep(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(bandit.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Azamuki removes a ki counter to steal a creature until end of turn")
    void azamukiStealsCreatureUntilEndOfTurn() {
        Permanent bandit = addBandit();
        bandit.setCounterCount(CounterType.KI, 2);

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent creature = addCreatureReady(player2, new GoblinCohort());

        prepareMainPhase();
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(bandit.getCounterCount(CounterType.KI)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(creature.getId()));
        assertThat(gd.isStolenUntilEndOfTurn(creature.getId())).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(creature.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(creature.getId()));
        assertThat(gd.isStolenUntilEndOfTurn(creature.getId())).isFalse();
    }

    @Test
    @DisplayName("Azamuki cannot activate without a ki counter")
    void azamukiCannotActivateWithoutKiCounter() {
        Permanent bandit = addBandit();
        bandit.setTransformed(true);
        Permanent creature = addCreatureReady(player2, new GoblinCohort());

        prepareMainPhase();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(bandit.getCounterCount(CounterType.KI)).isZero();
    }

    private Permanent addBandit() {
        return addCreatureReady(player1, new CunningBandit());
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
