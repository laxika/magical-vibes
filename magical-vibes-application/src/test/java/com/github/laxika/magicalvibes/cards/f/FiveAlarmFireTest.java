package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GruulKeyrune;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FiveAlarmFire.class, GrizzlyBears.class, SerraAngel.class, GruulKeyrune.class})
class FiveAlarmFireTest extends BaseCardTest {

    private Permanent addFire() {
        return harness.addToBattlefieldAndReturn(player1, new FiveAlarmFire());
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Card card, com.github.laxika.magicalvibes.model.Player owner) {
        Permanent perm = harness.addToBattlefieldAndReturn(owner, card);
        perm.setSummoningSick(false);
        return perm;
    }

    @Test
    @DisplayName("Blaze counter is added when a creature you control deals combat damage to a player")
    void blazeCounterOnDamageToPlayer() {
        Permanent fire = addFire();
        addReadyCreature(new GrizzlyBears(), player1).setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // combat damage
        harness.passBothPriorities(); // Five-Alarm Fire trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(fire.getCounterCount(CounterType.BLAZE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Blaze counter is added when the damage is dealt only to a blocking creature")
    void blazeCounterOnDamageToCreature() {
        Permanent fire = addFire();
        addReadyCreature(new GrizzlyBears(), player1).setAttacking(true);

        Permanent blocker = addReadyCreature(new SerraAngel(), player2);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // combat damage
        harness.passBothPriorities(); // Five-Alarm Fire trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(fire.getCounterCount(CounterType.BLAZE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A creature an opponent controls dealing combat damage does not add a blaze counter")
    void opponentCreatureDoesNotTrigger() {
        Permanent fire = addFire();
        addReadyCreature(new GrizzlyBears(), player2).setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // combat damage

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(fire.getCounterCount(CounterType.BLAZE)).isZero();
    }

    @Test
    @DisplayName("Removing five blaze counters deals 5 damage to any target")
    void removeFiveCountersDealsFiveDamage() {
        Permanent fire = addFire();
        fire.setCounterCount(CounterType.BLAZE, 5);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
        assertThat(fire.getCounterCount(CounterType.BLAZE)).isZero();
    }

    @Test
    @DisplayName("The ability can't be activated with fewer than five blaze counters")
    void cannotActivateWithoutFiveCounters() {
        Permanent fire = addFire();
        fire.setCounterCount(CounterType.BLAZE, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(fire.getCounterCount(CounterType.BLAZE)).isEqualTo(4);
    }

    @Test
    void animatedKeyruneDealingCombatDamageAddsBlazeCounter() {
        Permanent fire = addFire();
        Permanent keyrune = addReadyCreature(new GruulKeyrune(), player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();
        keyrune.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        assertThat(fire.getCounterCount(CounterType.BLAZE)).isEqualTo(1);
    }

    @Test
    void eachCreatureDealingCombatDamageAddsOneCounter() {
        Permanent fire = addFire();
        addReadyCreature(new GrizzlyBears(), player1).setAttacking(true);
        addReadyCreature(new GrizzlyBears(), player1).setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        assertThat(fire.getCounterCount(CounterType.BLAZE)).isEqualTo(2);
    }

    @Test
    void defendingCreatureDealingCombatDamageAddsCounterEvenWhenItDies() {
        Permanent fire = addFire();
        addReadyCreature(new SerraAngel(), player2).setAttacking(true);
        Permanent blocker = addReadyCreature(new SerraAngel(), player1);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Serra Angel");
        harness.assertInGraveyard(player2, "Serra Angel");
        assertThat(fire.getCounterCount(CounterType.BLAZE)).isEqualTo(1);
    }

    @Test
    void countersArePaidImmediatelyAndAbilityCanBeActivatedTwice() {
        Permanent fire = addFire();
        fire.setCounterCount(CounterType.BLAZE, 11);
        harness.activateAbility(player1, 0, null, player2.getId());
        assertThat(fire.getCounterCount(CounterType.BLAZE)).isEqualTo(6);
        harness.assertLife(player2, 20);
        harness.activateAbility(player1, 0, null, player2.getId());
        assertThat(fire.getCounterCount(CounterType.BLAZE)).isEqualTo(1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 10);
        assertThat(fire.getCounterCount(CounterType.BLAZE)).isEqualTo(1);
    }

    @Test
    void abilityCanDamageCreatureWithoutAddingBlazeCounter() {
        Permanent fire = addFire();
        fire.setCounterCount(CounterType.BLAZE, 5);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SerraAngel());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Serra Angel");
        assertThat(fire.getCounterCount(CounterType.BLAZE)).isZero();
    }
}
