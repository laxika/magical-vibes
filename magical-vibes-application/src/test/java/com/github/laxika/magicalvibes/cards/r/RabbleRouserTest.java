package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.p.Pyromatics;
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

@CardUsed({RabbleRouser.class, Pyromatics.class})
class RabbleRouserTest extends BaseCardTest {

    @Test
    @DisplayName("Bloodthirst 1 puts a +1/+1 counter on it when an opponent was dealt damage")
    void bloodthirstApplies() {
        gd.recordDamageToPlayer(player2.getId(), 1);
        castRabbleRouser();

        assertThat(findPermanent(player1, "Rabble-Rouser")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Bloodthirst 1 does not apply when no opponent was dealt damage")
    void bloodthirstDoesNotApplyWithoutOpponentDamage() {
        castRabbleRouser();

        assertThat(findPermanent(player1, "Rabble-Rouser")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Bloodthirst 1 ignores damage dealt to its controller")
    void bloodthirstIgnoresControllerDamage() {
        gd.recordDamageToPlayer(player1.getId(), 1);
        castRabbleRouser();

        assertThat(findPermanent(player1, "Rabble-Rouser")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The activated ability boosts every attacking creature by the source's power")
    void abilityUsesSourcePowerAndBoostsAllAttackers() {
        Permanent rouser = addCreatureReady(player1, new RabbleRouser());
        rouser.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        rouser.setAttacking(true);
        Permanent ownAttacker = addCreatureReady(player1, new RabbleRouser());
        ownAttacker.setAttacking(true);
        Permanent opposingAttacker = addCreatureReady(player2, new RabbleRouser());
        opposingAttacker.setAttacking(true);
        Permanent nonAttacker = addCreatureReady(player1, new RabbleRouser());

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, indexOf(player1, rouser), 0, null, null);
        harness.passBothPriorities();

        assertThat(rouser.getPowerModifier()).isEqualTo(2);
        assertThat(ownAttacker.getPowerModifier()).isEqualTo(2);
        assertThat(opposingAttacker.getPowerModifier()).isEqualTo(2);
        assertThat(nonAttacker.getPowerModifier()).isZero();
        assertThat(rouser.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The activated ability's boost wears off at end of turn")
    void abilityBoostWearsOff() {
        Permanent rouser = addCreatureReady(player1, new RabbleRouser());
        Permanent attacker = addCreatureReady(player1, new RabbleRouser());
        attacker.setAttacking(true);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, indexOf(player1, rouser), 0, null, null);
        harness.passBothPriorities();

        assertThat(attacker.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(attacker.getPowerModifier()).isZero();
    }

    private void castRabbleRouser() {
        harness.castFromHand(player1, new RabbleRouser(), "{3}{R}");
        resolveAllTriggers();
    }

    @Test
    @DisplayName("The ability uses the source's power at resolution rather than activation")
    void abilityUsesPowerAtResolution() {
        Permanent rouser = addCreatureReady(player1, new RabbleRouser());
        Permanent attacker = addCreatureReady(player1, new RabbleRouser());
        attacker.setAttacking(true);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, indexOf(player1, rouser), 0, null, null);
        rouser.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.passBothPriorities();

        assertThat(attacker.getPowerModifier()).isEqualTo(3);
        assertThat(attacker.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The ability uses last known power when its source dies in response")
    void abilityUsesLastKnownPower() {
        Permanent rouser = addCreatureReady(player1, new RabbleRouser());
        Permanent attacker = addCreatureReady(player1, new RabbleRouser());
        attacker.setAttacking(true);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, indexOf(player1, rouser), 0, null, null);
        harness.setHand(player2, List.of(new Pyromatics()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.ensurePriority(player2);
        harness.castInstant(player2, 0, rouser.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Rabble-Rouser");
        assertThat(attacker.getPowerModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Only creatures attacking at resolution receive the boost")
    void abilityLocksInAttackersAtResolution() {
        Permanent rouser = addCreatureReady(player1, new RabbleRouser());
        Permanent formerAttacker = addCreatureReady(player1, new RabbleRouser());
        formerAttacker.setAttacking(true);
        Permanent attacker = addCreatureReady(player1, new RabbleRouser());
        Permanent laterAttacker = addCreatureReady(player1, new RabbleRouser());

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, indexOf(player1, rouser), 0, null, null);
        formerAttacker.setAttacking(false);
        attacker.setAttacking(true);
        harness.passBothPriorities();
        laterAttacker.setAttacking(true);
        attacker.setAttacking(false);

        assertThat(formerAttacker.getPowerModifier()).isZero();
        assertThat(attacker.getPowerModifier()).isEqualTo(1);
        assertThat(laterAttacker.getPowerModifier()).isZero();
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }

    @Test
    @DisplayName("Noncombat damage to an opponent enables bloodthirst without using the stack for counters")
    void noncombatDamageEnablesBloodthirstAsItEnters() {
        harness.setHand(player1, List.of(new Pyromatics()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        harness.castFromHand(player1, new RabbleRouser(), "{3}{R}");
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Rabble-Rouser")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A source with negative power gives no boost")
    void negativeSourcePowerGivesNoBoost() {
        Permanent rouser = addCreatureReady(player1, new RabbleRouser());
        rouser.setPowerModifier(-2);
        Permanent attacker = addCreatureReady(player1, new RabbleRouser());
        attacker.setAttacking(true);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, indexOf(player1, rouser), 0, null, null);
        harness.passBothPriorities();

        assertThat(attacker.getPowerModifier()).isZero();
    }
}
