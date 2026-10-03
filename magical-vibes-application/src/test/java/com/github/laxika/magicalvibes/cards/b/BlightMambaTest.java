package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CarapaceForger;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlightMamba.class, CarapaceForger.class})
class BlightMambaTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Blight Mamba puts it on the stack")
    void castingPutsItOnStack() {
        harness.castFromHand(player1, new BlightMamba(), "{1}{G}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Resolving Blight Mamba puts it on the battlefield")
    void resolvingPutsItOnBattlefield() {
        harness.castFromHand(player1, new BlightMamba(), "{1}{G}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Blight Mamba");
    }

    @Test
    @DisplayName("Activating regeneration ability puts it on the stack")
    void activatingRegenPutsOnStack() {
        addBlightMambaReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Resolving regeneration ability grants a regeneration shield")
    void resolvingRegenGrantsShield() {
        addBlightMambaReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent mamba = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(mamba.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate regeneration without enough mana")
    void cannotActivateWithoutMana() {
        addBlightMambaReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Regeneration shield saves Blight Mamba from lethal combat damage")
    void regenerationSavesFromLethalCombatDamage() {
        Permanent mamba = addBlightMambaReady(player1);
        mamba.setRegenerationShield(1);
        mamba.setBlocking(true);
        mamba.addBlockingTarget(0);

        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new CarapaceForger());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Blight Mamba should survive via regeneration
        harness.assertOnBattlefield(player1, "Blight Mamba");
        Permanent survivedMamba = findPermanent(player1, "Blight Mamba");
        assertThat(survivedMamba.isTapped()).isTrue();
        assertThat(survivedMamba.getRegenerationShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("Blight Mamba dies without regeneration shield")
    void diesWithoutRegenerationShield() {
        Permanent mamba = addBlightMambaReady(player1);
        mamba.setBlocking(true);
        mamba.addBlockingTarget(0);

        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new CarapaceForger());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Blight Mamba");
        harness.assertInGraveyard(player1, "Blight Mamba");
    }

    @Test
    @DisplayName("Unblocked Blight Mamba deals poison counters to defending player")
    void unblockedDealsPoisonCounters() {
        Permanent mamba = addBlightMambaReady(player1);
        mamba.setAttacking(true);

        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Player should get 1 poison counter (1 power)
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(1);
        // Life should NOT change from infect damage
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Blight Mamba deals -1/-1 counters to blocking creature")
    void dealsMinusCountersToBlocker() {
        Permanent mamba = addBlightMambaReady(player1);
        mamba.setAttacking(true);

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new CarapaceForger());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Carapace Forger (2/2) gets 1 -1/-1 counter from 1-power Blight Mamba
        // Should survive as a 1/1
        harness.assertOnBattlefield(player2, "Carapace Forger");
        Permanent bears = findPermanent(player2, "Carapace Forger");
        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Blight Mamba with regen survives combat and still deals -1/-1 counters")
    void regenSurvivesCombatAndStillDealsInfect() {
        Permanent mamba = addBlightMambaReady(player1);
        mamba.setRegenerationShield(1);
        mamba.setAttacking(true);

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new CarapaceForger());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Blight Mamba should survive via regeneration
        harness.assertOnBattlefield(player1, "Blight Mamba");
        // Carapace Forger should have -1/-1 counters from infect
        Permanent bears = findPermanent(player2, "Carapace Forger");
        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    void regenerationCanBeActivatedWhileTappedAndSummoningSick() {
        Permanent mamba = harness.addToBattlefieldAndReturn(player1, new BlightMamba());
        mamba.tap();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(mamba.getRegenerationShield()).isEqualTo(1);
        assertThat(mamba.isTapped()).isTrue();
    }

    @Test
    void regenerationRequiresGreenMana() {
        addBlightMambaReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void regenerationClearsDamageAndRemovesCreatureFromCombat() {
        Permanent mamba = addBlightMambaReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(mamba.isTapped()).isFalse();
        mamba.setAttacking(true);
        mamba.setMarkedDamage(1);

        harness.runStateBasedActions();

        harness.assertOnBattlefield(player1, "Blight Mamba");
        assertThat(mamba.isTapped()).isTrue();
        assertThat(mamba.isAttacking()).isFalse();
        assertThat(mamba.getMarkedDamage()).isZero();
        assertThat(mamba.getRegenerationShield()).isZero();
    }

    @Test
    void regenerationCannotSaveCreatureFromInfectReducingToughnessToZero() {
        Permanent attacker = addBlightMambaReady(player1);
        attacker.setAttacking(true);
        Permanent blocker = addBlightMambaReady(player2);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Blight Mamba");
        harness.assertInGraveyard(player2, "Blight Mamba");
        harness.assertNotOnBattlefield(player2, "Blight Mamba");
    }

    private Permanent addBlightMambaReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new BlightMamba());
        perm.setSummoningSick(false);
        return perm;
    }
}
