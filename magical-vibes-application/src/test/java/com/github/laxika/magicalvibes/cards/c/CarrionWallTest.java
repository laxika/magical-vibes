package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CarrionWallTest extends BaseCardTest {

    @Test
    @DisplayName("Activating regeneration targets self and goes on the stack")
    void activatingAbilityPutsOnStack() {
        Permanent wallPerm = addCarrionWallReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(wallPerm.getId());
    }

    @Test
    @DisplayName("Resolving regeneration ability grants a regeneration shield")
    void resolvingAbilityGrantsRegenerationShield() {
        addCarrionWallReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);

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
        addCarrionWallReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Regeneration shield saves Carrion Wall from lethal combat damage")
    void regenerationSavesFromLethalCombatDamage() {
        // Carrion Wall (3/2) with regen shield blocks Grizzly Bears (2/2) — 2 damage = lethal
        Permanent wallPerm = addCarrionWallReady(player1);
        wallPerm.setRegenerationShield(1);
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
        Permanent wall = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Carrion Wall"))
                .findFirst().orElseThrow();
        assertThat(wall.isTapped()).isTrue();
        assertThat(wall.getRegenerationShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("Carrion Wall dies without a regeneration shield from lethal combat damage")
    void diesWithoutRegenerationShieldFromLethalDamage() {
        // Carrion Wall (3/2) blocks Grizzly Bears (2/2) — 2 damage >= 2 toughness, dies
        Permanent wallPerm = addCarrionWallReady(player1);
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
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Carrion Wall"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Carrion Wall"));
    }

    @Test
    @DisplayName("Carrion Wall survives sub-lethal combat damage without regeneration")
    void survivesSublethalDamageWithoutRegeneration() {
        // Carrion Wall (3/2) blocks Fugitive Wizard (1/1) — 1 damage < 2 toughness, survives
        Permanent wallPerm = addCarrionWallReady(player1);
        wallPerm.setBlocking(true);
        wallPerm.addBlockingTarget(0);

        FugitiveWizard wizard = new FugitiveWizard();
        Permanent attacker = new Permanent(wizard);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Carrion Wall"));
    }

    private Permanent addCarrionWallReady(Player player) {
        CarrionWall card = new CarrionWall();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
