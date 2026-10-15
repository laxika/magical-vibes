package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.cards.a.AlloyMyr;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import static org.assertj.core.api.Assertions.assertThat;
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({HexParasite.class, AlloyMyr.class})
class HexParasiteTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability puts it on the stack with correct X value")
    void activatingPutsOnStack() {
        addHexParasiteReady(player1);
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
        assertThat(targetPerm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
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
        assertThat(targetPerm.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
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
        assertThat(targetPerm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
        // Hex Parasite only gets +2/+0 (not +5)
        assertThat(hexPerm.getPowerModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Controller chooses which counter types to remove at resolution")
    void removesMultipleCounterTypes() {
        Permanent hexPerm = addHexParasiteReady(player1);
        Permanent targetPerm = harness.addToBattlefieldAndReturn(player2, new AlloyMyr());
        targetPerm.setSummoningSick(false);
        targetPerm.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        targetPerm.setCounterCount(CounterType.CHARGE, 3);
        harness.addMana(player1, ManaColor.BLACK, 5); // X=4, {B/P}=1

        harness.activateAbility(player1, 0, 4, targetPerm.getId());
        harness.passBothPriorities();

        // Both kinds must remain available until the controller makes a choice.
        assertThat(gd.interaction.activeInteraction()).isNotNull();
        assertThat(targetPerm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(targetPerm.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
        assertThat(hexPerm.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("No boost when target has no counters")
    void noBoostWhenNoCounters() {
        Permanent hexPerm = addHexParasiteReady(player1);
        Permanent targetPerm = harness.addToBattlefieldAndReturn(player2, new AlloyMyr());
        targetPerm.setSummoningSick(false);
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
        assertThat(targetPerm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
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
        assertThat(targetPerm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
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

        assertThat(targetPerm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(hexPerm.getPowerModifier()).isEqualTo(1);

        // Second activation: X=1
        harness.activateAbility(player1, 0, 1, targetPerm.getId());
        harness.passBothPriorities();

        assertThat(targetPerm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
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
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        // No boost since it fizzled
        assertThat(hexPerm.getPowerModifier()).isEqualTo(0);
    }

    @Test
    void canChooseToRemoveZeroCountersWithPositiveX() {
        Permanent source = addHexParasiteReady(player1);
        Permanent target = addCreatureWithPlusCounters(player2, 3);
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, 2, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNotNull();
        harness.handleXValueChosen(player1, 0);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(source.getPowerModifier()).isZero();
    }

    @ParameterizedTest
    @EnumSource(value = CounterType.class, names = {"MAGNET", "LEVEL", "LORE", "OIL", "STUN", "FLYING"})
    void removesCountersBeyondTheHardcodedTypes(CounterType counterType) {
        Permanent source = addHexParasiteReady(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlloyMyr());
        target.setCounterCount(counterType, 2);
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, 2, target.getId());
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.XValueChoice) {
            harness.handleXValueChosen(player1, 2);
        }

        assertThat(target.getCounterCount(counterType)).isZero();
        assertThat(source.getPowerModifier()).isEqualTo(2);
    }

    @Test
    void removesCountersEvenWhenSourceLeavesBeforeResolution() {
        Permanent source = addHexParasiteReady(player1);
        Permanent target = addCreatureWithPlusCounters(player2, 3);
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, 2, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(source);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void boostExpiresAtEndOfTurn() {
        Permanent source = addHexParasiteReady(player1);
        Permanent target = addCreatureWithPlusCounters(player2, 3);
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.activateAbility(player1, 0, 2, target.getId());
        harness.passBothPriorities();
        assertThat(source.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(source.getPowerModifier()).isZero();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void canActivateWhileSummoningSickAndTargetItself() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HexParasite());
        source.setSummoningSick(true);
        source.setCounterCount(CounterType.CHARGE, 2);
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, 2, source.getId());
        harness.passBothPriorities();

        assertThat(source.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(source.getPowerModifier()).isEqualTo(2);
        assertThat(source.isTapped()).isFalse();
    }

    private Permanent addHexParasiteReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new HexParasite());
        perm.setSummoningSick(false);
        return perm;
    }

    private Permanent addCreatureWithPlusCounters(Player player, int counters) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new AlloyMyr());
        perm.setSummoningSick(false);
        perm.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, counters);
        return perm;
    }

    private Permanent addPermanentWithChargeCounters(Player player, int counters) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new AlloyMyr());
        perm.setSummoningSick(false);
        perm.setCounterCount(CounterType.CHARGE, counters);
        return perm;
    }
}
