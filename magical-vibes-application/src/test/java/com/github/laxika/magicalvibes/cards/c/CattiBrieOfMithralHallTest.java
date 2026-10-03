package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CattiBrieOfMithralHall.class, GrizzlyBears.class, LeoninScimitar.class})
class CattiBrieOfMithralHallTest extends BaseCardTest {

    @Test
    @DisplayName("Puts one +1/+1 counter on itself for each attached Equipment when it attacks")
    void gainsCountersForAttachedEquipment() {
        Permanent cattiBrie = addCreatureReady(player1, new CattiBrieOfMithralHall());
        Permanent firstEquipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        Permanent secondEquipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        firstEquipment.setAttachedTo(cattiBrie.getId());
        secondEquipment.setAttachedTo(cattiBrie.getId());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(cattiBrie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Removes all +1/+1 counters and deals that much damage to an attacking opponent creature")
    void removesCountersAndDealsDamageToAttacker() {
        Permanent cattiBrie = addCreatureReady(player1, new CattiBrieOfMithralHall());
        cattiBrie.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        declareAttackers(player2, List.of(0));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, attacker.getId());
        assertThat(cattiBrie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking or blocking")
    void cannotTargetNonCombatCreature() {
        Permanent cattiBrie = addCreatureReady(player1, new CattiBrieOfMithralHall());
        cattiBrie.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(cattiBrie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target your own attacking creature")
    void cannotTargetOwnAttackingCreature() {
        Permanent cattiBrie = addCreatureReady(player1, new CattiBrieOfMithralHall());
        cattiBrie.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(cattiBrie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void attackWithoutEquipmentAddsNoCounters() {
        Permanent cattiBrie = addCreatureReady(player1, new CattiBrieOfMithralHall());
        cattiBrie.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addToBattlefield(player1, new LeoninScimitar());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(cattiBrie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void countsEquipmentAtTriggerResolution() {
        Permanent cattiBrie = addCreatureReady(player1, new CattiBrieOfMithralHall());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        equipment.setAttachedTo(cattiBrie.getId());

        declareAttackers(List.of(0));
        equipment.setAttachedTo(null);
        resolveAllTriggers();

        assertThat(cattiBrie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void canActivateWithNoCounters() {
        addCreatureReady(player1, new CattiBrieOfMithralHall());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, attacker.getId());
        resolveAllTriggers();

        assertThat(attacker.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void damagesOpponentBlockerUsingCountersRemovedAtActivation() {
        Permanent cattiBrie = addCreatureReady(player1, new CattiBrieOfMithralHall());
        cattiBrie.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, blocker.getId());
        assertThat(cattiBrie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        cattiBrie.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        resolveAllTriggers();

        assertThat(blocker.getMarkedDamage()).isEqualTo(1);
        assertThat(cattiBrie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void targetLeavingCombatMakesAbilityFailWithoutRefundingCounters() {
        Permanent cattiBrie = addCreatureReady(player1, new CattiBrieOfMithralHall());
        cattiBrie.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, attacker.getId());
        attacker.setAttacking(false);
        resolveAllTriggers();

        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(cattiBrie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void countsAttachedEquipmentControlledByOpponent() {
        Permanent cattiBrie = addCreatureReady(player1, new CattiBrieOfMithralHall());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        equipment.setAttachedTo(cattiBrie.getId());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(cattiBrie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void destroyingBlockerLeavesAttackerBlocked() {
        Permanent cattiBrie = addCreatureReady(player1, new CattiBrieOfMithralHall());
        cattiBrie.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, blocker.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        resolveCombat();
        harness.assertLife(player2, 20);
    }
}
