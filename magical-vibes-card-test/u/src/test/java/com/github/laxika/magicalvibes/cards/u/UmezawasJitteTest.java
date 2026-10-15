package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GnarledMass;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UmezawasJitte.class, GnarledMass.class})
class UmezawasJitteTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature dealing combat damage puts two charge counters on Jitte")
    void combatDamageToPlayerAddsChargeCounters() {
        Permanent jitte = addJitteReady(player1);
        Permanent creature = addCreatureReady(player1, new GnarledMass());
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
        Permanent creature = addCreatureReady(player1, new GnarledMass());
        jitte.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new GnarledMass());
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
        Permanent creature = addCreatureReady(player1, new GnarledMass());
        jitte.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new GnarledMass());
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
        Permanent creature = addCreatureReady(player1, new GnarledMass());
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
        Permanent creature = addCreatureReady(player1, new GnarledMass());
        jitte.setAttachedTo(creature.getId());
        jitte.setCounterCount(CounterType.CHARGE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(jitte.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @Test
    @DisplayName("The second mode gives a target creature -1/-1")
    void shrinkMode() {
        Permanent jitte = addJitteReady(player1);
        jitte.setCounterCount(CounterType.CHARGE, 1);
        Permanent target = addCreatureReady(player2, new GnarledMass());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(jitte.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
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
    @DisplayName("The pump mode wears off at end of turn")
    void pumpModeWearsOffAtEndOfTurn() {
        Permanent jitte = addJitteReady(player1);
        Permanent creature = addCreatureReady(player1, new GnarledMass());
        jitte.setAttachedTo(creature.getId());
        jitte.setCounterCount(CounterType.CHARGE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("A mode cannot be activated without a charge counter")
    void modesRequireChargeCounter() {
        addJitteReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("counter");
    }

    @Test
    @DisplayName("The -1/-1 mode can target only a creature")
    void shrinkModeRequiresCreatureTarget() {
        Permanent jitte = addJitteReady(player1);
        Permanent target = addJitteReady(player2);
        jitte.setCounterCount(CounterType.CHARGE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Equip attaches Jitte to a creature you control")
    void equipAttaches() {
        Permanent jitte = addJitteReady(player1);
        Permanent creature = addCreatureReady(player1, new GnarledMass());
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 3, null, creature.getId());
        harness.passBothPriorities();

        assertThat(jitte.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("The pump uses the creature equipped when the ability resolves")
    void pumpUsesCurrentAttachment() {
        Permanent jitte = addJitteReady(player1);
        Permanent original = addCreatureReady(player1, new GnarledMass());
        Permanent replacement = addCreatureReady(player1, new GnarledMass());
        jitte.setAttachedTo(original.getId());
        jitte.setCounterCount(CounterType.CHARGE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        jitte.setAttachedTo(replacement.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, original)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, replacement)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, replacement)).isEqualTo(5);
    }

    @Test
    @DisplayName("An equipped blocker also earns charge counters when it deals combat damage")
    void blockingCreatureAddsChargeCounters() {
        Permanent attacker = addCreatureReady(player1, new GnarledMass());
        attacker.setAttacking(true);
        Permanent jitte = addJitteReady(player2);
        Permanent blocker = addCreatureReady(player2, new GnarledMass());
        jitte.setAttachedTo(blocker.getId());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(jitte.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(jitte.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("The pump can be activated while Jitte is unattached and still spends its counter")
    void unattachedPumpSpendsCounter() {
        Permanent jitte = addJitteReady(player1);
        Permanent creature = addCreatureReady(player1, new GnarledMass());
        jitte.setCounterCount(CounterType.CHARGE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(jitte.getCounterCount(CounterType.CHARGE)).isZero();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Repeated shrink activations spend their counters immediately and can kill a creature")
    void repeatedShrinkKillsCreature() {
        Permanent jitte = addJitteReady(player1);
        Permanent target = addCreatureReady(player2, new GnarledMass());
        jitte.setCounterCount(CounterType.CHARGE, 3);

        for (int remaining = 2; remaining >= 0; remaining--) {
            harness.activateAbility(player1, 0, 1, null, target.getId());
            assertThat(jitte.getCounterCount(CounterType.CHARGE)).isEqualTo(remaining);
            harness.passBothPriorities();
        }

        harness.assertNotOnBattlefield(player2, "Gnarled Mass");
        harness.assertInGraveyard(player2, "Gnarled Mass");
    }

    @Test
    @DisplayName("The shrink mode wears off at end of turn")
    void shrinkModeWearsOffAtEndOfTurn() {
        Permanent jitte = addJitteReady(player1);
        Permanent target = addCreatureReady(player2, new GnarledMass());
        jitte.setCounterCount(CounterType.CHARGE, 1);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    @DisplayName("Equip cannot attach Jitte to an opponent's creature")
    void equipRejectsOpponentsCreature() {
        addJitteReady(player1);
        Permanent creature = addCreatureReady(player2, new GnarledMass());
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 3, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addJitteReady(Player player) {
        Permanent jitte = harness.addToBattlefieldAndReturn(player, new UmezawasJitte());
        jitte.setSummoningSick(false);
        return jitte;
    }
}
