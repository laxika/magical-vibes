package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BallistaSquad.class, GrizzlyBears.class})
class BallistaSquadTest extends BaseCardTest {

    // ===== Casting =====

    @Test
    @DisplayName("Casting Ballista Squad puts it on the stack")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new BallistaSquad()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Resolving Ballista Squad puts it on the battlefield")
    void resolvingPutsItOnBattlefield() {
        harness.setHand(player1, List.of(new BallistaSquad()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ballista Squad");
    }

    // ===== Activate ability =====

    @Test
    @DisplayName("Can activate ability targeting attacking creature")
    void canActivateAbilityOnAttackingCreature() {
        addBallistaReadyToCombat(player1);
        Permanent targetPerm = addAttackingCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 2, targetPerm.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getXValue()).isEqualTo(2);
        assertThat(entry.getTargetId()).isEqualTo(targetPerm.getId());
    }

    @Test
    @DisplayName("Activating ability taps the permanent")
    void activatingAbilityTapsPermanent() {
        Permanent ballistaPerm = addBallistaReadyToCombat(player1);
        Permanent targetPerm = addAttackingCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.activateAbility(player1, 0, 0, targetPerm.getId());

        assertThat(ballistaPerm.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Resolving ability deals X damage and destroys creature when X >= toughness")
    void resolvingAbilityDealsXDamageAndDestroysCreature() {
        addBallistaReadyToCombat(player1);
        Permanent targetPerm = addAttackingCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 3); // X=2, W=1 → 3 white mana needed
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.activateAbility(player1, 0, 2, targetPerm.getId());

        // Resolve the ability
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        // GrizzlyBears has 2 toughness, X=2 so it should be destroyed
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Resolving ability with X < toughness deals damage but does not destroy")
    void resolvingAbilityWithLowXDoesNotDestroy() {
        addBallistaReadyToCombat(player1);
        Permanent targetPerm = addAttackingCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 2); // X=1, W=1
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.activateAbility(player1, 0, 1, targetPerm.getId());

        // Resolve the ability
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        // GrizzlyBears has 2 toughness, X=1 so it should survive
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("deals 1 damage"));
    }

    @Test
    @DisplayName("Can activate ability targeting blocking creature")
    void canActivateAbilityOnBlockingCreature() {
        addBallistaReadyToCombat(player1);
        Permanent blockerPerm = addCreatureReady(player2, new GrizzlyBears());
        blockerPerm.setBlocking(true);
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        harness.activateAbility(player1, 0, 2, blockerPerm.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(blockerPerm.getId());
    }

    @Test
    @DisplayName("Ability fizzles if target is removed before resolution")
    void abilityFizzlesIfTargetRemoved() {
        addBallistaReadyToCombat(player1);
        Permanent targetPerm = addAttackingCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.activateAbility(player1, 0, 2, targetPerm.getId());

        // Remove target before resolution
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Ability fizzles if target stops attacking before resolution")
    void abilityFizzlesIfTargetStopsAttackingBeforeResolution() {
        addBallistaReadyToCombat(player1);
        Permanent targetPerm = addAttackingCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.activateAbility(player1, 0, 1, targetPerm.getId());
        targetPerm.setAttacking(false);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(targetPerm);
        assertThat(targetPerm.getMarkedDamage()).isZero();
    }

    // ===== Validation errors =====

    @Test
    @DisplayName("Cannot activate ability targeting non-combat creature")
    void cannotActivateAbilityOnNonCombatCreature() {
        addBallistaReadyToCombat(player1);
        Permanent nonCombatPerm = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, nonCombatPerm.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking or blocking");
    }

    @Test
    @DisplayName("Cannot activate ability without a target")
    void cannotActivateAbilityWithoutTarget() {
        addBallistaReadyToCombat(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("requires a target");
    }

    @Test
    @DisplayName("Cannot activate ability when already tapped")
    void cannotActivateWhenTapped() {
        Permanent ballistaPerm = addBallistaReadyToCombat(player1);
        ballistaPerm.tap();
        Permanent targetPerm = addAttackingCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, targetPerm.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Cannot activate ability with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new BallistaSquad());
        Permanent targetPerm = addAttackingCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, targetPerm.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addBallistaReadyToCombat(player1);
        Permanent targetPerm = addAttackingCreature(player2);
        // No mana added
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, targetPerm.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Mana is consumed when activating ability")
    void manaIsConsumedWhenActivating() {
        addBallistaReadyToCombat(player1);
        Permanent targetPerm = addAttackingCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.activateAbility(player1, 0, 3, targetPerm.getId());

        // X=3 + {W} = 4 mana used, should have 0 left
        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Activating ability with X=0 still costs {W}")
    void activatingWithXZeroStillCostsW() {
        addBallistaReadyToCombat(player1);
        Permanent targetPerm = addAttackingCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.activateAbility(player1, 0, 0, targetPerm.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    // ===== Helper methods =====

    private Permanent addBallistaReadyToCombat(Player player) {
        return addCreatureReady(player, new BallistaSquad());
    }

    private Permanent addAttackingCreature(Player player) {
        Permanent perm = addCreatureReady(player, new GrizzlyBears());
        perm.setAttacking(true);
        return perm;
    }
}

