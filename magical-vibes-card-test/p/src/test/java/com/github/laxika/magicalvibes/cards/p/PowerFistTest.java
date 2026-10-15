package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PowerFist.class, GrizzlyBears.class})
class PowerFistTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature has trample")
    void equippedCreatureHasTrample() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent powerFist = harness.addToBattlefieldAndReturn(player1, new PowerFist());
        powerFist.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Equipped creature gets combat-damage-scaled +1/+1 counters")
    void combatDamageAddsCountersToEquippedCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent powerFist = harness.addToBattlefieldAndReturn(player1, new PowerFist());
        powerFist.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equip ability attaches Power Fist to a creature you control")
    void equipAttachesPowerFist() {
        Permanent powerFist = harness.addToBattlefieldAndReturn(player1, new PowerFist());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(powerFist.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void trampleCountsOnlyDamageDealtToPlayer() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent powerFist = harness.addToBattlefieldAndReturn(player1, new PowerFist());
        powerFist.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 18);
        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    @Test
    void damageOnlyToBlockerDoesNotAddCounters() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent powerFist = harness.addToBattlefieldAndReturn(player1, new PowerFist());
        powerFist.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        assertThat(gd.stack).isEmpty();
        resolveAllTriggers();

        harness.assertLife(player2, 20);
        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void movingEquipmentBeforeResolutionStillCountersOriginalCreature() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());
        Permanent powerFist = harness.addToBattlefieldAndReturn(player1, new PowerFist());
        powerFist.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);

        resolveCombat();
        assertThat(gd.stack).hasSize(1);
        powerFist.setAttachedTo(other.getId());
        resolveAllTriggers();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, other, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void detachingEquipmentBeforeResolutionDoesNotStopCounters() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent powerFist = harness.addToBattlefieldAndReturn(player1, new PowerFist());
        powerFist.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);

        resolveCombat();
        assertThat(gd.stack).hasSize(1);
        powerFist.setAttachedTo(null);
        resolveAllTriggers();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void creatureControllerControlsGrantedCombatDamageTrigger() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent powerFist = harness.addToBattlefieldAndReturn(player2, new PowerFist());
        powerFist.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);

        resolveCombat();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
        assertThat(gd.stack.getFirst().getSourcePermanentId()).isEqualTo(attacker.getId());
        resolveAllTriggers();
        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }
}
