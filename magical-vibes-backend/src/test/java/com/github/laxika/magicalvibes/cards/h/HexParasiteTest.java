package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.RemoveCountersFromTargetAndBoostSelfEffect;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class HexParasiteTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Hex Parasite has correct activated ability")
    void hasCorrectActivatedAbility() {
        HexParasite card = new HexParasite();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{X}{B/P}");
        assertThat(card.getActivatedAbilities().get(0).isRequiresTap()).isFalse();
        assertThat(card.getActivatedAbilities().get(0).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isInstanceOf(RemoveCountersFromTargetAndBoostSelfEffect.class);
    }

    // ===== Activate ability =====

    @Test
    @DisplayName("Activating ability puts it on the stack with correct X value")
    void activatingPutsOnStack() {
        Permanent hexPerm = addHexParasiteReady(player1);
        Permanent targetPerm = addCreatureWithPlusCounters(player2, 3);
        harness.addMana(player1, ManaColor.BLACK, 3); // X=2, {B/P}=1

        harness.activateAbility(player1, 0, 2, targetPerm.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getXValue()).isEqualTo(2);
        assertThat(entry.getTargetId()).isEqualTo(targetPerm.getId());
    }

    @Test
    @DisplayName("Removes +1/+1 counters from target and boosts self")
    void removesPlusOneCountersAndBoostsSelf() {
        Permanent hexPerm = addHexParasiteReady(player1);
        Permanent targetPerm = addCreatureWithPlusCounters(player2, 3);
        harness.addMana(player1, ManaColor.BLACK, 3); // X=2, {B/P}=1

        harness.activateAbility(player1, 0, 2, targetPerm.getId());
        harness.passBothPriorities();

        // Target should have 1 +1/+1 counter left (3 - 2)
        assertThat(targetPerm.getPlusOnePlusOneCounters()).isEqualTo(1);
        // Hex Parasite gets +2/+0 (power modifier)
        assertThat(hexPerm.getPowerModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Removes charge counters from target and boosts self")
    void removesChargeCountersAndBoostsSelf() {
        Permanent hexPerm = addHexParasiteReady(player1);
        Permanent targetPerm = addPermanentWithChargeCounters(player2, 4);
        harness.addMana(player1, ManaColor.BLACK, 4); // X=3, {B/P}=1

        harness.activateAbility(player1, 0, 3, targetPerm.getId());
        harness.passBothPriorities();

        // Target should have 1 charge counter left (4 - 3)
        assertThat(targetPerm.getChargeCounters()).isEqualTo(1);
        // Hex Parasite gets +3/+0
        assertThat(hexPerm.getPowerModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Removes only up to available counters when X exceeds counter count")
    void removesOnlyAvailableCounters() {
        Permanent hexPerm = addHexParasiteReady(player1);
        Permanent targetPerm = addCreatureWithPlusCounters(player2, 2);
        harness.addMana(player1, ManaColor.BLACK, 6); // X=5, {B/P}=1

        harness.activateAbility(player1, 0, 5, targetPerm.getId());
        harness.passBothPriorities();

        // Target had only 2 +1/+1 counters, all removed
        assertThat(targetPerm.getPlusOnePlusOneCounters()).isEqualTo(0);
        // Hex Parasite only gets +2/+0 (not +5)
        assertThat(hexPerm.getPowerModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Removes counters of multiple types")
    void removesMultipleCounterTypes() {
        Permanent hexPerm = addHexParasiteReady(player1);
        GrizzlyBears bear = new GrizzlyBears();
        Permanent targetPerm = new Permanent(bear);
        targetPerm.setSummoningSick(false);
        targetPerm.setPlusOnePlusOneCounters(2);
        targetPerm.setChargeCounters(3);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(targetPerm);
        harness.addMana(player1, ManaColor.BLACK, 5); // X=4, {B/P}=1

        harness.activateAbility(player1, 0, 4, targetPerm.getId());
        harness.passBothPriorities();

        // Should remove 2 +1/+1 counters first, then 2 charge counters
        assertThat(targetPerm.getPlusOnePlusOneCounters()).isEqualTo(0);
        assertThat(targetPerm.getChargeCounters()).isEqualTo(1);
        // Hex Parasite gets +4/+0
        assertThat(hexPerm.getPowerModifier()).isEqualTo(4);
    }

    @Test
    @DisplayName("No boost when target has no counters")
    void noBoostWhenNoCounters() {
        Permanent hexPerm = addHexParasiteReady(player1);
        GrizzlyBears bear = new GrizzlyBears();
        Permanent targetPerm = new Permanent(bear);
        targetPerm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(targetPerm);
        harness.addMana(player1, ManaColor.BLACK, 3); // X=2, {B/P}=1

        harness.activateAbility(player1, 0, 2, targetPerm.getId());
        harness.passBothPriorities();

        // No counters to remove, no boost
        assertThat(hexPerm.getPowerModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Can activate with X=0 paying only Phyrexian mana")
    void canActivateWithXZero() {
        Permanent hexPerm = addHexParasiteReady(player1);
        Permanent targetPerm = addCreatureWithPlusCounters(player2, 2);
        harness.addMana(player1, ManaColor.BLACK, 1); // X=0, {B/P}=1

        harness.activateAbility(player1, 0, 0, targetPerm.getId());
        harness.passBothPriorities();

        // No counters removed with X=0
        assertThat(targetPerm.getPlusOnePlusOneCounters()).isEqualTo(2);
        assertThat(hexPerm.getPowerModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Can pay Phyrexian mana with life instead of black mana")
    void canPayPhyrexianWithLife() {
        Permanent hexPerm = addHexParasiteReady(player1);
        Permanent targetPerm = addCreatureWithPlusCounters(player2, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2); // X=2, pay {B/P} with 2 life
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 2, targetPerm.getId());
        harness.passBothPriorities();

        // Paid 2 life for Phyrexian mana
        assertThat(harness.getGameData().playerLifeTotals.get(player1.getId())).isEqualTo(18);
        // Counters removed and boost applied
        assertThat(targetPerm.getPlusOnePlusOneCounters()).isEqualTo(1);
        assertThat(hexPerm.getPowerModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Can activate ability multiple times per turn (no tap cost)")
    void canActivateMultipleTimes() {
        Permanent hexPerm = addHexParasiteReady(player1);
        Permanent targetPerm = addCreatureWithPlusCounters(player2, 5);
        harness.addMana(player1, ManaColor.BLACK, 4); // enough for two activations

        // First activation: X=1
        harness.activateAbility(player1, 0, 1, targetPerm.getId());
        harness.passBothPriorities();

        assertThat(targetPerm.getPlusOnePlusOneCounters()).isEqualTo(4);
        assertThat(hexPerm.getPowerModifier()).isEqualTo(1);

        // Second activation: X=1
        harness.activateAbility(player1, 0, 1, targetPerm.getId());
        harness.passBothPriorities();

        assertThat(targetPerm.getPlusOnePlusOneCounters()).isEqualTo(3);
        assertThat(hexPerm.getPowerModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Ability fizzles if target is removed before resolution")
    void abilityFizzlesIfTargetRemoved() {
        Permanent hexPerm = addHexParasiteReady(player1);
        Permanent targetPerm = addCreatureWithPlusCounters(player2, 3);
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, 2, targetPerm.getId());

        // Remove target before resolution
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        // No boost since it fizzled
        assertThat(hexPerm.getPowerModifier()).isEqualTo(0);
    }

    // ===== Helper methods =====

    private Permanent addHexParasiteReady(com.github.laxika.magicalvibes.model.Player player) {
        HexParasite card = new HexParasite();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addCreatureWithPlusCounters(com.github.laxika.magicalvibes.model.Player player, int counters) {
        GrizzlyBears bear = new GrizzlyBears();
        Permanent perm = new Permanent(bear);
        perm.setSummoningSick(false);
        perm.setPlusOnePlusOneCounters(counters);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addPermanentWithChargeCounters(com.github.laxika.magicalvibes.model.Player player, int counters) {
        GrizzlyBears bear = new GrizzlyBears();
        Permanent perm = new Permanent(bear);
        perm.setSummoningSick(false);
        perm.setChargeCounters(counters);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
