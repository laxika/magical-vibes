package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SentryBot.class, GrizzlyBears.class})
class SentryBotTest extends BaseCardTest {

    @Test
    void costsOneLessForEachCreatureAttackingYou() {
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());

        harness.setHand(player1, List.of(new SentryBot()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void entersWithEnergyForEachCreatureAttackingYou() {
        for (int i = 0; i < 2; i++) {
            Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
            attacker.setAttacking(true);
            attacker.setAttackTarget(player1.getId());
        }

        harness.setHand(player1, List.of(new SentryBot()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
    }

    @Test
    void payingThreeEnergyAtBeginningOfCombatPutsCountersOnEachCreatureYouControl() {
        Permanent sentryBot = addCreatureReady(player1, new SentryBot());
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());
        gd.playerEnergyCounters.put(player1.getId(), 3);

        advanceToBeginningOfCombat(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(sentryBot.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(otherCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void beginningOfCombatAbilityDoesNothingWhenPaymentIsDeclined() {
        Permanent sentryBot = addCreatureReady(player1, new SentryBot());
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());
        gd.playerEnergyCounters.put(player1.getId(), 3);

        advanceToBeginningOfCombat(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(3);
        assertThat(sentryBot.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(otherCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void insufficientEnergyCannotPayForCombatCounters() {
        Permanent sentryBot = addCreatureReady(player1, new SentryBot());
        gd.playerEnergyCounters.put(player1.getId(), 2);

        advanceToBeginningOfCombat(player1);
        resolveAllTriggers();
        if (gd.interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player1, true);
            resolveAllTriggers();
        }

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        assertThat(sentryBot.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotTriggerAtBeginningOfOpponentsCombat() {
        Permanent sentryBot = addCreatureReady(player1, new SentryBot());
        gd.playerEnergyCounters.put(player1.getId(), 3);

        advanceToBeginningOfCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(3);
        assertThat(sentryBot.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void costReductionCannotReduceWhiteManaRequirement() {
        for (int i = 0; i < 5; i++) {
            Permanent attacker = addCreatureReady(player2, new SentryBot());
            attacker.setAttacking(true);
            attacker.setAttackTarget(player1.getId());
        }
        harness.setHand(player1, List.of(new SentryBot()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    void entryEnergyCountsAttackersWhenTriggerResolves() {
        Permanent attacker = addCreatureReady(player2, new SentryBot());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        harness.setHand(player1, List.of(new SentryBot()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        attacker.setAttacking(false);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    void payingEnergyDoesNotPutCountersOnOpponentsCreatures() {
        Permanent sentryBot = addCreatureReady(player1, new SentryBot());
        Permanent opponentCreature = addCreatureReady(player2, new SentryBot());
        gd.playerEnergyCounters.put(player1.getId(), 5);

        advanceToBeginningOfCombat(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        assertThat(sentryBot.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void advanceToBeginningOfCombat(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }
}
