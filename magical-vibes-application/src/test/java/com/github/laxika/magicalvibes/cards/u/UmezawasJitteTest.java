package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UmezawasJitteTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature dealing combat damage puts two charge counters on Jitte")
    void combatDamageToPlayerAddsChargeCounters() {
        Permanent jitte = addJitteReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        jitte.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(jitte.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Combat damage to a creature also puts two charge counters on Jitte")
    void combatDamageToCreatureAddsChargeCounters() {
        Permanent jitte = addJitteReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        jitte.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(1);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(jitte.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    @DisplayName("The trigger uses the last-known equipped creature when it dies in combat")
    void combatDamageTriggerSurvivesEquippedCreatureDeath() {
        Permanent jitte = addJitteReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        jitte.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(1);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(jitte.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(jitte.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("A Jitte controlled by another player still triggers for the equipped creature")
    void opponentControlledJitteTriggers() {
        Permanent jitte = addJitteReady(player2);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        jitte.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(jitte.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    @DisplayName("The first mode boosts the equipped creature and removes one charge counter")
    void pumpMode() {
        Permanent jitte = addJitteReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        jitte.setAttachedTo(creature.getId());
        jitte.setCounterCount(CounterType.CHARGE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(jitte.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("The second mode gives a target creature -1/-1")
    void shrinkMode() {
        Permanent jitte = addJitteReady(player1);
        jitte.setCounterCount(CounterType.CHARGE, 1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(jitte.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("The third mode gains two life")
    void lifeMode() {
        Permanent jitte = addJitteReady(player1);
        jitte.setCounterCount(CounterType.CHARGE, 1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(jitte.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("Equip attaches Jitte to a creature you control")
    void equipAttaches() {
        Permanent jitte = addJitteReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 3, null, creature.getId());
        harness.passBothPriorities();

        assertThat(jitte.getAttachedTo()).isEqualTo(creature.getId());
    }

    private Permanent addJitteReady(Player player) {
        Permanent jitte = new Permanent(new UmezawasJitte());
        jitte.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(jitte);
        return jitte;
    }
}
