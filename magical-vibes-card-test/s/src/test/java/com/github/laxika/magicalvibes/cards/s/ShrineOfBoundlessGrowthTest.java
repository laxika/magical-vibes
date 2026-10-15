package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GlistenerElf;
import com.github.laxika.magicalvibes.cards.g.GutShot;
import com.github.laxika.magicalvibes.cards.m.MutagenicGrowth;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShrineOfBoundlessGrowth.class, MutagenicGrowth.class, GlistenerElf.class, GutShot.class})
class ShrineOfBoundlessGrowthTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger adds a charge counter (mandatory)")
    void upkeepTriggerAddsChargeCounter() {
        Permanent shrine = addReadyShrine(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // move to upkeep, trigger fires
        harness.passBothPriorities(); // resolve PutCountersOnSelfEffect

        assertThat(shrine.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Multiple upkeeps accumulate charge counters")
    void multipleUpkeepsAccumulateCounters() {
        Permanent shrine = addReadyShrine(player1);

        // First upkeep
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(shrine.getCounterCount(CounterType.CHARGE)).isEqualTo(1);

        // Second upkeep
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(shrine.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's upkeep does not add a charge counter")
    void opponentUpkeepDoesNotAddCounter() {
        Permanent shrine = addReadyShrine(player1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(shrine.getCounterCount(CounterType.CHARGE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Casting a green spell adds a charge counter")
    void castingGreenSpellAddsChargeCounter() {
        Permanent shrine = addReadyShrine(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        MutagenicGrowth mutagenicGrowth = new MutagenicGrowth();
        harness.setHand(player1, List.of(mutagenicGrowth));
        // Mutagenic Growth targets a creature, so we need a creature
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GlistenerElf());
        harness.castInstant(player1, 0, creature.getId());

        // Spell cast trigger should put charge counter on shrine
        harness.passBothPriorities(); // resolve charge counter trigger
        assertThat(shrine.getCounterCount(CounterType.CHARGE)).isEqualTo(1);

        // Resolve the Mutagenic Growth itself
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Casting a non-green spell does not add a charge counter")
    void castingNonGreenSpellDoesNotAddCounter() {
        Permanent shrine = addReadyShrine(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        GutShot gutShot = new GutShot();
        harness.setHand(player1, List.of(gutShot));
        // Gut Shot targets any target — target opponent
        harness.castInstant(player1, 0, player2.getId());

        // No charge counter trigger should fire — resolve Gut Shot
        harness.passBothPriorities();

        assertThat(shrine.getCounterCount(CounterType.CHARGE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Sacrificing with charge counters adds colorless mana")
    void sacrificeAddsColorlessManaPerChargeCounter() {
        Permanent shrine = addReadyShrine(player1);
        shrine.setCounterCount(CounterType.CHARGE, 4);

        int colorlessBefore = gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS);

        harness.activateAbility(player1, 0, null, null);

        // Mana ability resolves immediately (no stack)
        int colorlessAfter = gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS);
        assertThat(colorlessAfter - colorlessBefore).isEqualTo(4);

        // Shrine should be in graveyard
        harness.assertNotOnBattlefield(player1, "Shrine of Boundless Growth");
        harness.assertInGraveyard(player1, "Shrine of Boundless Growth");
    }

    @Test
    @DisplayName("Sacrificing with zero counters adds no mana")
    void sacrificeWithZeroCountersAddsNoMana() {
        addReadyShrine(player1);
        // No charge counters

        int colorlessBefore = gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS);

        harness.activateAbility(player1, 0, null, null);

        int colorlessAfter = gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS);
        assertThat(colorlessAfter - colorlessBefore).isEqualTo(0);

        // Shrine should still be sacrificed
        harness.assertNotOnBattlefield(player1, "Shrine of Boundless Growth");
    }

    @Test
    @DisplayName("Activated ability requires tap — tapped shrine cannot activate")
    void activatedAbilityRequiresTap() {
        Permanent shrine = addReadyShrine(player1);
        shrine.tap();

        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> harness.activateAbility(player1, 0, null, null)
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Mana ability resolves immediately without using the stack")
    void manaAbilityResolvesImmediately() {
        Permanent shrine = addReadyShrine(player1);
        shrine.setCounterCount(CounterType.CHARGE, 3);

        int stackSizeBefore = gd.stack.size();

        harness.activateAbility(player1, 0, null, null);

        // Should not add anything to the stack (mana ability)
        assertThat(gd.stack.size()).isEqualTo(stackSizeBefore);

        // Mana should already be in pool
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isGreaterThanOrEqualTo(3);
    }

    @Test
    @DisplayName("Activating Shrine mana ability does not clear priority")
    void manaAbilityDoesNotClearPriority() {
        Permanent shrine = addReadyShrine(player1);
        shrine.setCounterCount(CounterType.CHARGE, 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        // Simulate: active player (player2) passed, now player1 has priority
        gd.priorityPassedBy.clear();
        gd.priorityPassedBy.add(player2.getId());

        harness.activateAbility(player1, 0, null, null);

        // Player1 should still have priority — active player's pass should be preserved
        assertThat(gd.priorityPassedBy).containsExactly(player2.getId());
        // Mana should be in pool
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("Non-active player can cast after Shrine mana ability")
    void nonActivePlayerCanCastAfterShrineManaAbility() {
        Permanent shrine = addReadyShrine(player2);
        shrine.setCounterCount(CounterType.CHARGE, 4);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        // Active player (player1) passed priority → non-active player2 has priority
        gd.priorityPassedBy.clear();
        gd.priorityPassedBy.add(player1.getId());

        // Player2 activates Shrine for 4 colorless mana
        harness.ensurePriority(player2);
        harness.activateAbility(player2, 0, null, null);

        // Player2 should still have priority — the Gut Shot cast below should succeed
        harness.addMana(player2, ManaColor.RED, 1);
        GutShot gutShot = new GutShot();
        harness.setHand(player2, List.of(gutShot));

        // This would throw "You do not have priority" before the fix
        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.stack).isNotEmpty();
        assertThat(gd.stack.getLast().getCard().getName()).isEqualTo("Gut Shot");
    }

    @Test
    @DisplayName("Multiple mana abilities in sequence don't clear priority")
    void multipleManaAbilitiesPreservePriority() {
        // Add two shrines
        Permanent shrine1 = addReadyShrine(player1);
        shrine1.setCounterCount(CounterType.CHARGE, 2);
        Permanent shrine2 = addReadyShrine(player1);
        shrine2.setCounterCount(CounterType.CHARGE, 3);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gd.priorityPassedBy.clear();
        gd.priorityPassedBy.add(player2.getId());

        // Activate first shrine (index 0)
        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.priorityPassedBy).containsExactly(player2.getId());

        // After sacrifice, shrine2 is now at index 0
        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.priorityPassedBy).containsExactly(player2.getId());

        // Total mana: 2 + 3 = 5
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isGreaterThanOrEqualTo(5);
    }

    @Test
    @DisplayName("Opponent casting a green spell does not charge your shrine")
    void opponentGreenSpellDoesNotAddCounter() {
        Permanent shrine = addReadyShrine(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.setHand(player2, List.of(new GlistenerElf()));

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(shrine.getCounterCount(CounterType.CHARGE)).isZero();
        harness.assertOnBattlefield(player2, "Glistener Elf");
    }

    @Test
    @DisplayName("A green creature charges the shrine before the creature resolves")
    void greenCreatureSpellChargesBeforeResolving() {
        Permanent shrine = addReadyShrine(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new GlistenerElf()));

        harness.castCreature(player1, 0);

        assertThat(shrine.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(shrine.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Glistener Elf");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Glistener Elf");
    }

    @Test
    @DisplayName("Sacrificing in response to a charge trigger uses only existing counters")
    void sacrificeBeforeChargeTriggerResolves() {
        Permanent shrine = addReadyShrine(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        shrine.setCounterCount(CounterType.CHARGE, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new GlistenerElf()));
        harness.castCreature(player1, 0);
        int manaBefore = gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(manaBefore + 2);
        assertThat(gd.stack).hasSize(2);
        harness.assertInGraveyard(player1, "Shrine of Boundless Growth");
        harness.passBothPriorities();
        assertThat(shrine.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Glistener Elf");
    }

    @Test
    @DisplayName("A shrine can activate on the turn it enters the battlefield")
    void newlyEnteredShrineCanActivate() {
        Permanent shrine = harness.addToBattlefieldAndReturn(player1, new ShrineOfBoundlessGrowth());
        shrine.setCounterCount(CounterType.CHARGE, 1);
        int manaBefore = gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(manaBefore + 1);
        harness.assertInGraveyard(player1, "Shrine of Boundless Growth");
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyShrine(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new ShrineOfBoundlessGrowth());
        perm.setSummoningSick(false);
        return perm;
    }
}
