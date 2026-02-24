package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BlightMambaTest extends BaseCardTest {


    // ===== Card properties =====

    @Test
    @DisplayName("Blight Mamba has correct activated ability")
    void hasCorrectActivatedAbility() {
        BlightMamba card = new BlightMamba();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().getManaCost()).isEqualTo("{1}{G}");
        assertThat(card.getActivatedAbilities().getFirst().getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().getEffects().getFirst())
                .isInstanceOf(RegenerateEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Blight Mamba puts it on the stack")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new BlightMamba()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Blight Mamba");
    }

    @Test
    @DisplayName("Resolving Blight Mamba puts it on the battlefield")
    void resolvingPutsItOnBattlefield() {
        harness.setHand(player1, List.of(new BlightMamba()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Blight Mamba"));
    }

    // ===== Regeneration ability =====

    @Test
    @DisplayName("Activating regeneration ability puts it on the stack")
    void activatingRegenPutsOnStack() {
        Permanent mamba = addBlightMambaReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Blight Mamba");
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

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Blight Mamba should survive via regeneration
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Blight Mamba"));
        Permanent survivedMamba = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Blight Mamba"))
                .findFirst().orElseThrow();
        assertThat(survivedMamba.isTapped()).isTrue();
        assertThat(survivedMamba.getRegenerationShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("Blight Mamba dies without regeneration shield")
    void diesWithoutRegenerationShield() {
        Permanent mamba = addBlightMambaReady(player1);
        mamba.setBlocking(true);
        mamba.addBlockingTarget(0);

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Blight Mamba"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Blight Mamba"));
    }

    // ===== Infect: combat damage to players gives poison counters =====

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
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    // ===== Infect: combat damage to creatures gives -1/-1 counters =====

    @Test
    @DisplayName("Blight Mamba deals -1/-1 counters to blocking creature")
    void dealsMinusCountersToBlocker() {
        Permanent mamba = addBlightMambaReady(player1);
        mamba.setAttacking(true);

        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Grizzly Bears (2/2) gets 1 -1/-1 counter from 1-power Blight Mamba
        // Should survive as a 1/1
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        Permanent bears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(bears.getMinusOneMinusOneCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("Blight Mamba with regen survives combat and still deals -1/-1 counters")
    void regenSurvivesCombatAndStillDealsInfect() {
        Permanent mamba = addBlightMambaReady(player1);
        mamba.setRegenerationShield(1);
        mamba.setAttacking(true);

        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Blight Mamba should survive via regeneration
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Blight Mamba"));
        // Grizzly Bears should have -1/-1 counters from infect
        Permanent bears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(bears.getMinusOneMinusOneCounters()).isEqualTo(1);
    }

    // ===== Helper methods =====

    private Permanent addBlightMambaReady(Player player) {
        BlightMamba card = new BlightMamba();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
