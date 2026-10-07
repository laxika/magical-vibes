package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.o.OgreResister;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TangleHulk.class, OgreResister.class})
class TangleHulkTest extends BaseCardTest {

    @Test
    @DisplayName("Regeneration can be activated while tapped and summoning sick")
    void activatesWhileTappedAndSummoningSick() {
        Permanent hulk = harness.addToBattlefieldAndReturn(player1, new TangleHulk());
        hulk.setSummoningSick(true);
        hulk.tap();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(hulk.getRegenerationShield()).isEqualTo(1);
        assertThat(hulk.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Casting Tangle Hulk puts it on the stack as an artifact spell")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new TangleHulk()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castArtifact(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Tangle Hulk");
    }

    @Test
    @DisplayName("Resolving Tangle Hulk puts it on the battlefield")
    void resolvingPutsItOnBattlefield() {
        harness.setHand(player1, List.of(new TangleHulk()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Tangle Hulk");
    }

    @Test
    @DisplayName("Activating regeneration ability puts it on the stack for its source")
    void activatingAbilityPutsOnStack() {
        Permanent hulkPerm = addTangleHulkReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Tangle Hulk");
        assertThat(entry.getTargetId()).isEqualTo(hulkPerm.getId());
    }

    @Test
    @DisplayName("Resolving regeneration ability grants a regeneration shield")
    void resolvingAbilityGrantsRegenerationShield() {
        addTangleHulkReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        Permanent hulk = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(hulk.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Activating regeneration ability does NOT tap the permanent")
    void activatingAbilityDoesNotTap() {
        addTangleHulkReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        Permanent hulk = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(hulk.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate regeneration ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addTangleHulkReady(player1);
        // Only 2 colorless, need {2}{G}
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Mana is consumed when activating regeneration ability")
    void manaIsConsumedWhenActivating() {
        addTangleHulkReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration shield saves blocking Tangle Hulk from lethal combat damage")
    void regenerationSavesFromLethalCombatDamage() {
        // Tangle Hulk (5/3) with regen shield blocks Ogre Resister (4/3)
        // Ogre Resister deals 4 damage >= 3 toughness - lethal, but regen saves
        Permanent hulkPerm = addTangleHulkReady(player1);
        hulkPerm.setRegenerationShield(1);
        hulkPerm.setBlocking(true);
        hulkPerm.addBlockingTarget(0);

        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new OgreResister());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Tangle Hulk survives via regeneration
        harness.assertOnBattlefield(player1, "Tangle Hulk");
        Permanent hulk = findPermanent(player1, "Tangle Hulk");
        assertThat(hulk.isTapped()).isTrue();
        assertThat(hulk.getRegenerationShield()).isEqualTo(0);
        // Ogre Resister should also die (5 damage from Tangle Hulk >= 3 toughness)
        harness.assertNotOnBattlefield(player2, "Ogre Resister");
    }

    @Test
    @DisplayName("Tangle Hulk dies to lethal combat damage without regeneration shield")
    void diesWithoutRegenerationShieldInCombat() {
        // Tangle Hulk (5/3) without regen blocks Ogre Resister (4/3)
        // 4 damage >= 3 toughness - lethal, no regen to save it
        Permanent hulkPerm = addTangleHulkReady(player1);
        hulkPerm.setBlocking(true);
        hulkPerm.addBlockingTarget(0);

        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new OgreResister());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Tangle Hulk");
        harness.assertInGraveyard(player1, "Tangle Hulk");
    }

    @Test
    @DisplayName("Regeneration shield saves attacking Tangle Hulk from lethal blocker damage")
    void regenerationSavesAttackingCreature() {
        // Tangle Hulk (5/3) with regen attacks, blocked by Ogre Resister (4/3)
        Permanent hulkPerm = addTangleHulkReady(player1);
        hulkPerm.setRegenerationShield(1);
        hulkPerm.setAttacking(true);

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new OgreResister());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Tangle Hulk survives via regeneration
        harness.assertOnBattlefield(player1, "Tangle Hulk");
        Permanent hulk = findPermanent(player1, "Tangle Hulk");
        assertThat(hulk.isTapped()).isTrue();
        assertThat(hulk.isAttacking()).isFalse();
        assertThat(hulk.getRegenerationShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("A resolved shield replaces lethal damage and removes all marked damage")
    void resolvedShieldReplacesLethalDamage() {
        Permanent hulk = addTangleHulkReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        hulk.setMarkedDamage(7);
        harness.runStateBasedActions();

        harness.assertOnBattlefield(player1, "Tangle Hulk");
        assertThat(hulk.getMarkedDamage()).isZero();
        assertThat(hulk.getRegenerationShield()).isZero();
        assertThat(hulk.isTapped()).isTrue();

        hulk.setMarkedDamage(3);
        harness.runStateBasedActions();

        harness.assertInGraveyard(player1, "Tangle Hulk");
        harness.assertNotOnBattlefield(player1, "Tangle Hulk");
    }

    @Test
    @DisplayName("Repeated activations create independently consumable shields")
    void repeatedActivationsCreateSeparateShields() {
        Permanent hulk = addTangleHulkReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(hulk.getRegenerationShield()).isEqualTo(2);
        assertThat(hulk.isTapped()).isFalse();

        hulk.setMarkedDamage(3);
        harness.runStateBasedActions();
        assertThat(hulk.getRegenerationShield()).isEqualTo(1);

        hulk.setMarkedDamage(3);
        harness.runStateBasedActions();
        harness.assertOnBattlefield(player1, "Tangle Hulk");
        assertThat(hulk.getRegenerationShield()).isZero();
        assertThat(hulk.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Regeneration does not save a creature with zero toughness")
    void regenerationDoesNotReplaceZeroToughness() {
        Permanent hulk = addTangleHulkReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        hulk.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 3);
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Tangle Hulk");
        harness.assertInGraveyard(player1, "Tangle Hulk");
    }

    @Test
    @DisplayName("Three generic mana cannot pay the green component")
    void cannotActivateWithoutGreenMana() {
        addTangleHulkReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private Permanent addTangleHulkReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new TangleHulk());
        perm.setSummoningSick(false);
        return perm;
    }
}
