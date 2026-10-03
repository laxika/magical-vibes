package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(DuergarCaveGuard.class)
class DuergarCaveGuardTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving ability gives +1/+0, payable with red mana")
    void resolvingBoostsWithRed() {
        Permanent guard = addCreatureReady(player1, new DuergarCaveGuard());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(guard.getEffectivePower()).isEqualTo(2);
        assertThat(guard.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Ability is also payable with white mana (hybrid cost)")
    void payableWithWhite() {
        Permanent guard = addCreatureReady(player1, new DuergarCaveGuard());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(guard.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("Can activate multiple times, stacking the boost")
    void stacksMultipleActivations() {
        Permanent guard = addCreatureReady(player1, new DuergarCaveGuard());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(guard.getEffectivePower()).isEqualTo(3);
        assertThat(guard.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost wears off at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        Permanent guard = addCreatureReady(player1, new DuergarCaveGuard());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(guard.getEffectivePower()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(guard.getEffectivePower()).isEqualTo(1);
        assertThat(guard.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Wither deals combat damage to creatures as -1/-1 counters")
    void witherPutsMinusOneMinusOneCountersOnCombatCreatures() {
        Permanent attacker = addCreatureReady(player1, new DuergarCaveGuard());
        Permanent blocker = addCreatureReady(player2, new DuergarCaveGuard());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(attacker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(blocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(blocker.getMarkedDamage()).isZero();
        assertThat(gqs.getEffectivePower(gd, attacker)).isZero();
        assertThat(gqs.getEffectivePower(gd, blocker)).isZero();
    }

    @Test
    @DisplayName("Wither deals normal combat damage to players")
    void witherDealsNormalDamageToPlayers() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new DuergarCaveGuard());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Cannot activate ability without mana")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new DuergarCaveGuard());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Can activate while tapped and summoning sick")
    void activatesWhileTappedAndSummoningSick() {
        Permanent guard = addCreatureReady(player1, new DuergarCaveGuard());
        guard.setSummoningSick(true);
        guard.setTapped(true);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, guard)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, guard)).isEqualTo(3);
        assertThat(guard.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Hybrid cost cannot be paid with blue mana")
    void cannotPayWithBlueMana() {
        Permanent guard = addCreatureReady(player1, new DuergarCaveGuard());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(gqs.getEffectivePower(gd, guard)).isEqualTo(1);
    }

    @Test
    @DisplayName("Boosted combat damage puts additional counters on the blocker")
    void boostedWitherDamagePlacesTwoCounters() {
        Permanent attacker = addCreatureReady(player1, new DuergarCaveGuard());
        Permanent blocker = addCreatureReady(player2, new DuergarCaveGuard());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(blocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(attacker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(blocker.getMarkedDamage()).isZero();
        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(gqs.getEffectiveToughness(gd, blocker)).isEqualTo(1);
    }
}
