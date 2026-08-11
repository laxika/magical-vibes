package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BramblebackBruteTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield with two -1/-1 counters")
    void entersWithTwoMinusOneMinusOneCounters() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new BramblebackBrute()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent brute = findPermanent(player1, "Brambleback Brute");

        assertThat(brute.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Removing a counter makes a target creature unable to block this turn")
    void removesCounterAndMakesTargetUnblockable() {
        Permanent brute = addCreatureReady(player1, new BramblebackBrute());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        brute.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int bruteIndex = gd.playerBattlefields.get(player1.getId()).indexOf(brute);
        harness.activateAbility(player1, bruteIndex, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(brute.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(attacker.isCantBeBlocked()).isTrue();

        attacker.setAttacking(true);
        prepareDeclareBlockers();
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The unblockable restriction wears off at end of turn")
    void unblockableRestrictionWearsOffAtEndOfTurn() {
        Permanent brute = addCreatureReady(player1, new BramblebackBrute());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        brute.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int bruteIndex = gd.playerBattlefields.get(player1.getId()).indexOf(brute);
        harness.activateAbility(player1, bruteIndex, null, attacker.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(attacker.isCantBeBlocked()).isFalse();
        attacker.setAttacking(true);
        prepareDeclareBlockers();
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate without a counter")
    void cannotActivateWithoutCounter() {
        Permanent brute = addCreatureReady(player1, new BramblebackBrute());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        brute.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int bruteIndex = gd.playerBattlefields.get(player1.getId()).indexOf(brute);

        assertThatThrownBy(() -> harness.activateAbility(player1, bruteIndex, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
