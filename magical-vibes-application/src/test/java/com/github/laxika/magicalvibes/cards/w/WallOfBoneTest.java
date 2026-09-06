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
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WallOfBone.class, EarthElemental.class, GrizzlyBears.class})
class WallOfBoneTest extends BaseCardTest {

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

        harness.assertOnBattlefield(player1, "Wall of Bone");
    }

    @Test
    @DisplayName("Defender prevents Wall of Bone from attacking")
    void defenderPreventsAttacking() {
        addWallOfBoneReady(player1);

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

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
        assertThat(entry.getTargetId()).isEqualTo(wallPerm.getId());
    }

    @Test
    @DisplayName("Resolving regeneration ability grants a regeneration shield without tapping Wall of Bone")
    void resolvingAbilityGrantsRegenerationShield() {
        addWallOfBoneReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        Permanent wall = findPermanent(player1, "Wall of Bone");
        assertThat(wall.getRegenerationShield()).isEqualTo(1);
        assertThat(wall.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate regeneration ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addWallOfBoneReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Regeneration shield saves Wall of Bone from lethal combat damage")
    void regenerationSavesFromLethalCombatDamage() {
        // Wall of Bone (1/4) with a regeneration shield blocks Earth Elemental (4/5): 4 damage is lethal.
        Permanent wallPerm = addWallOfBoneReady(player1);
        wallPerm.setRegenerationShield(1);
        wallPerm.setBlocking(true);
        wallPerm.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new EarthElemental());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Wall of Bone");
        Permanent wall = findPermanent(player1, "Wall of Bone");
        assertThat(wall.isTapped()).isTrue();
        assertThat(wall.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("Wall of Bone survives sub-lethal combat damage without regeneration")
    void survivesSublethalDamageWithoutRegeneration() {
        // Wall of Bone (1/4) blocks Grizzly Bears (2/2): 2 damage is not lethal.
        Permanent wallPerm = addWallOfBoneReady(player1);
        wallPerm.setBlocking(true);
        wallPerm.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Wall of Bone");
    }

    @Test
    @DisplayName("A regeneration shield remains after nonlethal combat damage")
    void regenerationShieldRemainsAfterNonlethalDamage() {
        Permanent wall = addWallOfBoneReady(player1);
        wall.setRegenerationShield(1);
        wall.setBlocking(true);
        wall.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Wall of Bone");
        assertThat(wall.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Wall of Bone dies without regeneration shield from lethal combat damage")
    void diesWithoutRegenerationShieldFromLethalDamage() {
        // Wall of Bone (1/4) blocks Earth Elemental (4/5): 4 damage is lethal.
        Permanent wallPerm = addWallOfBoneReady(player1);
        wallPerm.setBlocking(true);
        wallPerm.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new EarthElemental());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertNotOnBattlefield(player1, "Wall of Bone");
        harness.assertInGraveyard(player1, "Wall of Bone");
    }

    @Test
    @DisplayName("Regeneration shields expire during cleanup")
    void regenerationShieldExpiresAtCleanup() {
        addWallOfBoneReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(findPermanent(player1, "Wall of Bone").getRegenerationShield()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(findPermanent(player1, "Wall of Bone").getRegenerationShield()).isZero();
    }

    private Permanent addWallOfBoneReady(Player player) {
        return addCreatureReady(player, new WallOfBone());
    }
}
