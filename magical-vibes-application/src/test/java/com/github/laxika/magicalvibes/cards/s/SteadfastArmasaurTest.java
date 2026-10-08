package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.r.RaptorCompanion;
import com.github.laxika.magicalvibes.cards.l.LoomingAltisaur;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({SteadfastArmasaur.class, RaptorCompanion.class, LoomingAltisaur.class})
class SteadfastArmasaurTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to toughness to a creature blocking it")
    void dealsToughnessDamageToBlocker() {
        Permanent armasaur = addReadyArmasaur(player1);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new LoomingAltisaur());

        setupArmasaurAttackingBlockedBy(armasaur, blocker);

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        // Three damage is not lethal to Looming Altisaur.
        assertThat(blocker.getMarkedDamage()).isEqualTo(3);
        harness.assertOnBattlefield(player2, "Looming Altisaur");
    }

    @Test
    @DisplayName("Kills blocker when toughness damage is lethal")
    void killsBlockerWithLethalToughnessDamage() {
        Permanent armasaur = addReadyArmasaur(player1);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new RaptorCompanion());

        setupArmasaurAttackingBlockedBy(armasaur, blocker);

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        // Three damage is lethal to Raptor Companion.
        harness.assertInGraveyard(player2, "Raptor Companion");
    }

    @Test
    @DisplayName("Deals damage equal to toughness to a creature it is blocking")
    void dealsToughnessDamageToAttackerItBlocks() {
        Permanent armasaur = addReadyArmasaur(player1);
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new RaptorCompanion());

        setupArmasaurBlockingAttacker(armasaur, attacker);

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        // Three damage is lethal to Raptor Companion.
        harness.assertInGraveyard(player2, "Raptor Companion");
    }

    @Test
    @DisplayName("Cannot target a creature not in combat with it")
    void cannotTargetCreatureNotInCombat() {
        Permanent armasaur = addReadyArmasaur(player1);
        armasaur.setAttacking(true);

        harness.addToBattlefield(player2, new RaptorCompanion());
        UUID companionId = harness.getPermanentId(player2, "Raptor Companion");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 2);

        // Raptor Companion is not blocking the Armasaur, so it should not be a valid target
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, companionId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Damage is based on toughness — boosted toughness deals more damage")
    void damageUsesBoostedToughness() {
        Permanent armasaur = addReadyArmasaur(player1);
        armasaur.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2); // becomes 4/5

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new RaptorCompanion());
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);

        setupArmasaurAttackingBlockedBy(armasaur, blocker);

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        // Five damage is lethal to Raptor Companion with four counters.
        harness.assertInGraveyard(player2, "Raptor Companion");
    }

    @Test
    @DisplayName("Taps when activating ability")
    void tapsOnActivation() {
        Permanent armasaur = addReadyArmasaur(player1);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new RaptorCompanion());

        setupArmasaurAttackingBlockedBy(armasaur, blocker);

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, blocker.getId());

        assertThat(armasaur.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        Permanent armasaur = addReadyArmasaur(player1);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new RaptorCompanion());

        setupArmasaurAttackingBlockedBy(armasaur, blocker);

        harness.addMana(player1, ManaColor.WHITE, 1); // needs {1}{W} = 2 total

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, blocker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Activating ability puts it on the stack")
    void activatingPutsOnStack() {
        Permanent armasaur = addReadyArmasaur(player1);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new RaptorCompanion());

        setupArmasaurAttackingBlockedBy(armasaur, blocker);

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, blocker.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Steadfast Armasaur");
    }

    @Test
    @DisplayName("Uses Armasaur's last known toughness if it is removed before resolution")
    void usesLastKnownToughnessIfSourceRemoved() {
        Permanent armasaur = addReadyArmasaur(player1);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new LoomingAltisaur());

        setupArmasaurAttackingBlockedBy(armasaur, blocker);

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, blocker.getId());

        // Remove Armasaur before ability resolves
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        // The source snapshot supplies its last known toughness.
        assertThat(gd.stack).isEmpty();
        assertThat(blocker.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Ability fizzles if target creature is removed before resolution")
    void fizzlesIfTargetCreatureRemoved() {
        Permanent armasaur = addReadyArmasaur(player1);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new RaptorCompanion());

        setupArmasaurAttackingBlockedBy(armasaur, blocker);

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, blocker.getId());

        // Remove target before ability resolves
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Uses toughness at resolution rather than at activation")
    void usesCurrentToughnessOnResolution() {
        Permanent armasaur = addReadyArmasaur(player1);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new LoomingAltisaur());
        setupArmasaurAttackingBlockedBy(armasaur, blocker);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, blocker.getId());

        armasaur.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.passBothPriorities();

        assertThat(blocker.getMarkedDamage()).isEqualTo(5);
        harness.assertOnBattlefield(player2, "Looming Altisaur");
    }

    @Test
    @DisplayName("Ability fizzles when its target stops blocking")
    void fizzlesWhenTargetLeavesCombat() {
        Permanent armasaur = addReadyArmasaur(player1);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new LoomingAltisaur());
        setupArmasaurAttackingBlockedBy(armasaur, blocker);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, blocker.getId());

        blocker.clearCombatState();
        harness.passBothPriorities();

        assertThat(blocker.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        Permanent armasaur = addReadyArmasaur(player1);
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new RaptorCompanion());
        setupArmasaurBlockingAttacker(armasaur, attacker);
        armasaur.setSummoningSick(true);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhenTapped() {
        Permanent armasaur = addReadyArmasaur(player1);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new RaptorCompanion());
        setupArmasaurAttackingBlockedBy(armasaur, blocker);
        armasaur.tap();
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, blocker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyArmasaur(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new SteadfastArmasaur());
        perm.setSummoningSick(false);
        return perm;
    }

    /**
     * Sets up combat where Armasaur (player1) is attacking and the given creature (player2) blocks it.
     * Armasaur has vigilance so it remains untapped.
     */
    private void setupArmasaurAttackingBlockedBy(Permanent armasaur, Permanent blocker) {
        armasaur.setAttacking(true);
        // Vigilance: Armasaur does NOT tap when attacking

        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        int armasaurIndex = gd.playerBattlefields.get(player1.getId()).indexOf(armasaur);
        blocker.addBlockingTarget(armasaurIndex);
        blocker.addBlockingTargetId(armasaur.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
    }

    /**
     * Sets up combat where an opponent creature (player2) is attacking and Armasaur (player1) blocks it.
     */
    private void setupArmasaurBlockingAttacker(Permanent armasaur, Permanent attacker) {
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        armasaur.setBlocking(true);
        int attackerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(attacker);
        armasaur.addBlockingTarget(attackerIndex);
        armasaur.addBlockingTargetId(attacker.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
    }
}
