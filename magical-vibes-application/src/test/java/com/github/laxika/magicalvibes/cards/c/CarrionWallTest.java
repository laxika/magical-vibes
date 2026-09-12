package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CarrionWall.class, GrizzlyBears.class, FugitiveWizard.class})
class CarrionWallTest extends BaseCardTest {

    @Test
    @DisplayName("Activating regeneration puts the self-regeneration ability on the stack")
    void activatingAbilityPutsOnStack() {
        Permanent wallPerm = addCreatureReady(player1, new CarrionWall());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(wallPerm.getId());
    }

    @Test
    @DisplayName("Resolving regeneration ability grants a regeneration shield")
    void resolvingAbilityGrantsRegenerationShield() {
        addCreatureReady(player1, new CarrionWall());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        Permanent wall = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(wall.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Multiple activations create multiple regeneration shields")
    void multipleActivationsCreateMultipleShields() {
        Permanent wall = addCreatureReady(player1, new CarrionWall());
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(wall.getRegenerationShield()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate regeneration ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addCreatureReady(player1, new CarrionWall());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Regeneration shield saves Carrion Wall from lethal combat damage")
    void regenerationSavesFromLethalCombatDamage() {
        // Carrion Wall (3/2) with regen shield blocks Grizzly Bears (2/2) — 2 damage = lethal
        Permanent wallPerm = addCreatureReady(player1, new CarrionWall());
        wallPerm.setRegenerationShield(1);

        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        wallPerm.setBlocking(true);
        wallPerm.addBlockingTargetId(attacker.getId());

        resolveCombat(player2);

        Permanent wall = findPermanent(player1, "Carrion Wall");
        assertThat(wall.isTapped()).isTrue();
        assertThat(wall.getRegenerationShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("Carrion Wall dies without a regeneration shield from lethal combat damage")
    void diesWithoutRegenerationShieldFromLethalDamage() {
        // Carrion Wall (3/2) blocks Grizzly Bears (2/2) — 2 damage >= 2 toughness, dies
        Permanent wallPerm = addCreatureReady(player1, new CarrionWall());

        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        wallPerm.setBlocking(true);
        wallPerm.addBlockingTargetId(attacker.getId());

        resolveCombat(player2);

        harness.assertNotOnBattlefield(player1, "Carrion Wall");
        harness.assertInGraveyard(player1, "Carrion Wall");
    }

    @Test
    @DisplayName("Carrion Wall survives sub-lethal combat damage without regeneration")
    void survivesSublethalDamageWithoutRegeneration() {
        // Carrion Wall (3/2) blocks Fugitive Wizard (1/1) — 1 damage < 2 toughness, survives
        Permanent wallPerm = addCreatureReady(player1, new CarrionWall());

        Permanent attacker = addCreatureReady(player2, new FugitiveWizard());
        attacker.setAttacking(true);
        wallPerm.setBlocking(true);
        wallPerm.addBlockingTargetId(attacker.getId());

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Carrion Wall");
    }
}
