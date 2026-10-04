package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.m.MoorlandInquisitor;
import com.github.laxika.magicalvibes.cards.s.SeraphOfDawn;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FalkenrathExterminator.class, MoorlandInquisitor.class, SeraphOfDawn.class, Forest.class})
class FalkenrathExterminatorTest extends BaseCardTest {

    @Test
    @DisplayName("Gets a +1/+1 counter when dealing combat damage to a player")
    void getsCounterOnCombatDamage() {
        Permanent exterminator = addReadyExterminator(player1);
        exterminator.setAttacking(true);
        harness.setLife(player2, 20);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);

        harness.passBothPriorities(); // resolve trigger
        assertThat(exterminator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("No counter when blocked and no damage reaches the player")
    void noCounterWhenBlocked() {
        Permanent exterminator = addReadyExterminator(player1);
        exterminator.setAttacking(true);
        harness.setLife(player2, 20);

        Permanent blocker = addCreatureReady(player2, new MoorlandInquisitor());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(exterminator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Ability deals damage equal to the number of +1/+1 counters")
    void abilityDealsDamageEqualToCounters() {
        Permanent exterminator = addReadyExterminator(player1);
        exterminator.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent target = addCreatureReady(player2, new SeraphOfDawn());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Ability deals no damage with no counters")
    void abilityDealsNoDamageWithoutCounters() {
        addReadyExterminator(player1);
        Permanent target = addCreatureReady(player2, new MoorlandInquisitor());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetLand() {
        addReadyExterminator(player1);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void usesCounterCountAtResolution() {
        Permanent exterminator = addReadyExterminator(player1);
        exterminator.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent target = addCreatureReady(player2, new SeraphOfDawn());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        exterminator.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        assertThat(exterminator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canActivateRepeatedlyWhileTappedAndSummoningSick() {
        Permanent exterminator = harness.addToBattlefieldAndReturn(player1, new FalkenrathExterminator());
        exterminator.setSummoningSick(true);
        exterminator.setTapped(true);
        exterminator.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent target = addCreatureReady(player2, new SeraphOfDawn());
        harness.addMana(player1, ManaColor.RED, 6);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.activateAbility(player1, 0, null, target.getId());
        resolveAllTriggers();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(exterminator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(exterminator.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void usesLastKnownCountersAfterSourceDies() {
        Permanent exterminator = addReadyExterminator(player1);
        exterminator.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent target = addCreatureReady(player2, new SeraphOfDawn());
        harness.addMana(player1, ManaColor.RED, 9);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.activateAbility(player1, 0, null, exterminator.getId());
        harness.activateAbility(player1, 0, null, exterminator.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(exterminator);

        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void combatDamageAddsOneCounterRegardlessOfDamageAmount() {
        Permanent exterminator = addReadyExterminator(player1);
        exterminator.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        exterminator.setAttacking(true);
        harness.setLife(player2, 20);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(exterminator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    private Permanent addReadyExterminator(Player player) {
        return addCreatureReady(player, new FalkenrathExterminator());
    }
}
