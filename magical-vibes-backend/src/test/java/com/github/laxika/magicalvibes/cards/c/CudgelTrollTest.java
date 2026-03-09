package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CudgelTrollTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Cudgel Troll has regenerate activated ability costing {G}")
    void hasCorrectProperties() {
        CudgelTroll card = new CudgelTroll();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isInstanceOf(RegenerateEffect.class);
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{G}");
        assertThat(card.getActivatedAbilities().get(0).isRequiresTap()).isFalse();
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Cudgel Troll puts it on the stack")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new CudgelTroll()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Cudgel Troll");
    }

    @Test
    @DisplayName("Resolving Cudgel Troll puts it on the battlefield")
    void resolvingPutsItOnBattlefield() {
        harness.setHand(player1, List.of(new CudgelTroll()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Cudgel Troll"));
    }

    // ===== Activate regeneration ability =====

    @Test
    @DisplayName("Activating regeneration ability puts it on the stack")
    void activatingAbilityPutsOnStack() {
        Permanent trollPerm = addCudgelTrollReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetPermanentId()).isEqualTo(trollPerm.getId());
    }

    @Test
    @DisplayName("Resolving regeneration ability grants a regeneration shield")
    void resolvingAbilityGrantsRegenerationShield() {
        addCudgelTrollReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent troll = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(troll.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate regeneration ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addCudgelTrollReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Can stack multiple regeneration shields")
    void canStackMultipleRegenerationShields() {
        addCudgelTrollReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent troll = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(troll.getRegenerationShield()).isEqualTo(3);
    }

    // ===== Regeneration saves from lethal combat damage =====

    @Test
    @DisplayName("Regeneration shield saves Cudgel Troll from lethal combat damage when blocking")
    void regenerationSavesFromLethalCombatDamage() {
        // Cudgel Troll (4/3) with regen shield blocks two creatures dealing 4+ damage
        // Actually, let's use a scenario where troll takes lethal: it has 3 toughness
        // We need an attacker with power >= 3
        Permanent trollPerm = addCudgelTrollReady(player1);
        trollPerm.setRegenerationShield(1);
        trollPerm.setBlocking(true);
        trollPerm.addBlockingTarget(0);

        // Use a second Cudgel Troll as attacker (4 power > 3 toughness)
        CudgelTroll attackerCard = new CudgelTroll();
        Permanent attacker = new Permanent(attackerCard);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Cudgel Troll should survive via regeneration
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Cudgel Troll"));
        Permanent troll = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Cudgel Troll"))
                .findFirst().orElseThrow();
        assertThat(troll.isTapped()).isTrue();
        assertThat(troll.getRegenerationShield()).isEqualTo(0);
        assertThat(troll.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Cudgel Troll dies without regeneration shield from lethal combat damage")
    void diesWithoutRegenerationShieldInCombat() {
        Permanent trollPerm = addCudgelTrollReady(player1);
        trollPerm.setBlocking(true);
        trollPerm.addBlockingTarget(0);

        CudgelTroll attackerCard = new CudgelTroll();
        Permanent attacker = new Permanent(attackerCard);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Cudgel Troll"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Cudgel Troll"));
    }

    @Test
    @DisplayName("Cudgel Troll survives combat damage that doesn't exceed toughness")
    void survivesNonLethalCombatDamage() {
        // Cudgel Troll (4/3) blocks Grizzly Bears (2/2) — takes 2 damage, survives without regen
        Permanent trollPerm = addCudgelTrollReady(player1);
        trollPerm.setBlocking(true);
        trollPerm.addBlockingTarget(0);

        GrizzlyBears bears = new GrizzlyBears();
        Permanent attacker = new Permanent(bears);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Troll survives (2 damage < 3 toughness), Bears die (4 damage > 2 toughness)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Cudgel Troll"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    // ===== Helper methods =====

    private Permanent addCudgelTrollReady(Player player) {
        CudgelTroll card = new CudgelTroll();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
