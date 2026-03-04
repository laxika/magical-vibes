package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
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

class TangleHulkTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Tangle Hulk has regeneration activated ability costing {2}{G}")
    void hasCorrectAbility() {
        TangleHulk card = new TangleHulk();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isInstanceOf(RegenerateEffect.class);
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{2}{G}");
    }

    // ===== Casting =====

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

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Tangle Hulk"));
    }

    // ===== Activate regeneration ability =====

    @Test
    @DisplayName("Activating regeneration ability puts it on the stack with self as target")
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
        assertThat(entry.getTargetPermanentId()).isEqualTo(hulkPerm.getId());
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

    // ===== Regeneration saves from combat damage =====

    @Test
    @DisplayName("Regeneration shield saves blocking Tangle Hulk from lethal combat damage")
    void regenerationSavesFromLethalCombatDamage() {
        // Tangle Hulk (5/3) with regen shield blocks Hill Giant (3/3)
        // Hill Giant deals 3 damage >= 3 toughness — lethal, but regen saves
        Permanent hulkPerm = addTangleHulkReady(player1);
        hulkPerm.setRegenerationShield(1);
        hulkPerm.setBlocking(true);
        hulkPerm.addBlockingTarget(0);

        HillGiant giant = new HillGiant();
        Permanent attacker = new Permanent(giant);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Tangle Hulk survives via regeneration
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Tangle Hulk"));
        Permanent hulk = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Tangle Hulk"))
                .findFirst().orElseThrow();
        assertThat(hulk.isTapped()).isTrue();
        assertThat(hulk.getRegenerationShield()).isEqualTo(0);
        // Hill Giant should also die (5 damage from Tangle Hulk >= 3 toughness)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Hill Giant"));
    }

    @Test
    @DisplayName("Tangle Hulk dies to lethal combat damage without regeneration shield")
    void diesWithoutRegenerationShieldInCombat() {
        // Tangle Hulk (5/3) without regen blocks Hill Giant (3/3)
        // 3 damage >= 3 toughness — lethal, no regen to save it
        Permanent hulkPerm = addTangleHulkReady(player1);
        hulkPerm.setBlocking(true);
        hulkPerm.addBlockingTarget(0);

        HillGiant giant = new HillGiant();
        Permanent attacker = new Permanent(giant);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Tangle Hulk"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Tangle Hulk"));
    }

    @Test
    @DisplayName("Regeneration shield saves attacking Tangle Hulk from lethal blocker damage")
    void regenerationSavesAttackingCreature() {
        // Tangle Hulk (5/3) with regen attacks, blocked by Hill Giant (3/3)
        Permanent hulkPerm = addTangleHulkReady(player1);
        hulkPerm.setRegenerationShield(1);
        hulkPerm.setAttacking(true);

        HillGiant giant = new HillGiant();
        Permanent blocker = new Permanent(giant);
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Tangle Hulk survives via regeneration
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Tangle Hulk"));
        Permanent hulk = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Tangle Hulk"))
                .findFirst().orElseThrow();
        assertThat(hulk.isTapped()).isTrue();
        assertThat(hulk.isAttacking()).isFalse();
        assertThat(hulk.getRegenerationShield()).isEqualTo(0);
    }

    // ===== Helper methods =====

    private Permanent addTangleHulkReady(Player player) {
        TangleHulk card = new TangleHulk();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
