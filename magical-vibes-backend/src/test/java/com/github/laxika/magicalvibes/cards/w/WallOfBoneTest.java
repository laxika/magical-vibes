package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.e.EarthElemental;
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

class WallOfBoneTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Wall of Bone has regeneration activated ability costing {B}")
    void hasCorrectProperties() {
        WallOfBone card = new WallOfBone();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isInstanceOf(RegenerateEffect.class);
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{B}");
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Wall of Bone puts it on the stack")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new WallOfBone()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Wall of Bone");
    }

    @Test
    @DisplayName("Resolving Wall of Bone puts it on the battlefield")
    void resolvingPutsItOnBattlefield() {
        harness.setHand(player1, List.of(new WallOfBone()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Wall of Bone"));
    }

    // ===== Activate regeneration ability =====

    @Test
    @DisplayName("Activating regeneration ability puts it on the stack with self as target")
    void activatingAbilityPutsOnStack() {
        Permanent wallPerm = addWallOfBoneReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Wall of Bone");
        assertThat(entry.getTargetPermanentId()).isEqualTo(wallPerm.getId());
    }

    @Test
    @DisplayName("Resolving regeneration ability grants a regeneration shield")
    void resolvingAbilityGrantsRegenerationShield() {
        addWallOfBoneReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        Permanent wall = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(wall.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate regeneration ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addWallOfBoneReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    // ===== Regeneration saves from combat damage =====

    @Test
    @DisplayName("Regeneration shield saves Wall of Bone from lethal combat damage")
    void regenerationSavesFromLethalCombatDamage() {
        // Wall of Bone (1/4) with regen shield blocks Earth Elemental (4/5) — 4 damage = lethal
        Permanent wallPerm = addWallOfBoneReady(player1);
        wallPerm.setRegenerationShield(1);
        wallPerm.setBlocking(true);
        wallPerm.addBlockingTarget(0);

        EarthElemental elemental = new EarthElemental();
        Permanent attacker = new Permanent(elemental);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Wall of Bone should survive via regeneration
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Wall of Bone"));
        Permanent wall = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Wall of Bone"))
                .findFirst().orElseThrow();
        assertThat(wall.isTapped()).isTrue();
        assertThat(wall.getRegenerationShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("Wall of Bone survives sub-lethal combat damage without regeneration")
    void survivesSublethalDamageWithoutRegeneration() {
        // Wall of Bone (1/4) blocks Grizzly Bears (2/2) — takes 2 damage, survives naturally
        Permanent wallPerm = addWallOfBoneReady(player1);
        wallPerm.setBlocking(true);
        wallPerm.addBlockingTarget(0);

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
        // Wall of Bone survives — 2 damage < 4 toughness
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Wall of Bone"));
    }

    @Test
    @DisplayName("Wall of Bone dies without regeneration shield from lethal combat damage")
    void diesWithoutRegenerationShieldFromLethalDamage() {
        // Wall of Bone (1/4) blocks Earth Elemental (4/5) — 4 damage >= 4 toughness, dies
        Permanent wallPerm = addWallOfBoneReady(player1);
        wallPerm.setBlocking(true);
        wallPerm.addBlockingTarget(0);

        EarthElemental elemental = new EarthElemental();
        Permanent attacker = new Permanent(elemental);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Wall of Bone"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Wall of Bone"));
    }

    // ===== Helper methods =====

    private Permanent addWallOfBoneReady(Player player) {
        WallOfBone card = new WallOfBone();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
