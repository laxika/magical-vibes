package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.SizzlingChangeling;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BramblebackBrute.class, SizzlingChangeling.class})
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

        Permanent brute = findPermanent(player1, "Brambleback Brute");

        assertThat(brute.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Removing a counter makes a target creature unable to block this turn")
    void removesCounterAndPreventsTargetFromBlocking() {
        Permanent brute = addCreatureReady(player1, new BramblebackBrute());
        Permanent attacker = addCreatureReady(player1, new SizzlingChangeling());
        Permanent blocker = addCreatureReady(player2, new SizzlingChangeling());
        brute.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int bruteIndex = gd.playerBattlefields.get(player1.getId()).indexOf(brute);
        harness.activateAbility(player1, bruteIndex, null, blocker.getId());
        assertThat(brute.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        harness.passBothPriorities();

        assertThat(brute.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        attacker.setAttacking(true);
        prepareDeclareBlockers();
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The blocking restriction wears off at end of turn")
    void blockingRestrictionWearsOffAtEndOfTurn() {
        Permanent brute = addCreatureReady(player1, new BramblebackBrute());
        Permanent attacker = addCreatureReady(player1, new SizzlingChangeling());
        Permanent blocker = addCreatureReady(player2, new SizzlingChangeling());
        brute.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int bruteIndex = gd.playerBattlefields.get(player1.getId()).indexOf(brute);
        harness.activateAbility(player1, bruteIndex, null, blocker.getId());
        harness.passBothPriorities();

        assertThat(blocker.isCantBlockThisTurn()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blocker.isCantBlockThisTurn()).isFalse();
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
        Permanent target = addCreatureReady(player2, new SizzlingChangeling());
        brute.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int bruteIndex = gd.playerBattlefields.get(player1.getId()).indexOf(brute);

        assertThatThrownBy(() -> harness.activateAbility(player1, bruteIndex, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Enters with counters even when not cast, without a triggered ability")
    void entersWithCountersWithoutBeingCast() {
        Permanent brute = harness.enterBattlefieldAndReturn(player1, new BramblebackBrute());

        assertThat(brute.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A targeted creature can still be blocked when it attacks")
    void targetedAttackerCanStillBeBlocked() {
        Permanent brute = addCreatureReady(player1, new BramblebackBrute());
        Permanent attacker = addCreatureReady(player1, new SizzlingChangeling());
        Permanent blocker = addCreatureReady(player2, new SizzlingChangeling());
        brute.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        attacker.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Can remove a +1/+1 counter while tapped and summoning sick")
    void canPayWithPlusOneCounterWhileTappedAndSummoningSick() {
        Permanent brute = harness.addToBattlefieldAndReturn(player1, new BramblebackBrute());
        Permanent target = addCreatureReady(player2, new SizzlingChangeling());
        brute.setSummoningSick(true);
        brute.setTapped(true);
        brute.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(brute.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();
        assertThat(target.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate during combat")
    void cannotActivateDuringCombat() {
        Permanent brute = addCreatureReady(player1, new BramblebackBrute());
        Permanent target = addCreatureReady(player2, new SizzlingChangeling());
        brute.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(brute.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate during an opponent's main phase")
    void cannotActivateDuringOpponentsMainPhase() {
        Permanent brute = addCreatureReady(player1, new BramblebackBrute());
        Permanent target = addCreatureReady(player2, new SizzlingChangeling());
        brute.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(brute.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate while another ability is on the stack")
    void cannotActivateWithNonemptyStack() {
        Permanent brute = addCreatureReady(player1, new BramblebackBrute());
        Permanent target = addCreatureReady(player2, new SizzlingChangeling());
        brute.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 4);
        harness.activateAbility(player1, 0, null, target.getId());
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(brute.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate without paying the red mana requirement")
    void cannotActivateWithOnlyColorlessMana() {
        Permanent brute = addCreatureReady(player1, new BramblebackBrute());
        Permanent target = addCreatureReady(player2, new SizzlingChangeling());
        brute.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(brute.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Can remove a counter that does not modify power and toughness")
    void canPayWithChargeCounter() {
        Permanent brute = addCreatureReady(player1, new BramblebackBrute());
        Permanent target = addCreatureReady(player2, new SizzlingChangeling());
        brute.setCounterCount(CounterType.CHARGE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(brute.getCounterCount(CounterType.CHARGE)).isZero();
        harness.passBothPriorities();
        assertThat(target.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a player instead of a creature")
    void cannotTargetPlayer() {
        Permanent brute = addCreatureReady(player1, new BramblebackBrute());
        brute.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(brute.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }
}
