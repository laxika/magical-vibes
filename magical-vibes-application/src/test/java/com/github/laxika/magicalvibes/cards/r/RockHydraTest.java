package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.Whippoorwill;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RockHydra.class, GiantGrowth.class, Shock.class, Whippoorwill.class, ProdigalSorcerer.class})
class RockHydraTest extends BaseCardTest {

    @Test
    @DisplayName("Casting with X=3 enters with three +1/+1 counters")
    void entersWithXCounters() {
        harness.setHand(player1, List.of(new RockHydra()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gs.playCard(gd, player1, 0, 3, null, null);
        harness.passBothPriorities();

        Permanent hydra = findPermanent(player1, "Rock Hydra");
        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, hydra)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, hydra)).isEqualTo(3);
    }

    @Test
    @DisplayName("Each available counter prevents one damage")
    void preventsOnlyDamageCoveredByCounters() {
        Permanent hydra = addCreatureReady(player2, new RockHydra());
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player2, 0, hydra.getId());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, hydra.getId());

        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(hydra.getMarkedDamage()).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, hydra)).isEqualTo(3);
    }

    @Test
    @DisplayName("With no counters, damage is not prevented")
    void doesNotPreventDamageWithoutCounters() {
        Permanent hydra = addCreatureReady(player2, new RockHydra());
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player2, 0, hydra.getId());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, hydra.getId());

        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.castAndResolveInstant(player2, 0, hydra.getId());

        harness.setHand(player1, List.of(new Shock()));
        harness.castAndResolveInstant(player1, 0, hydra.getId());

        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(hydra.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("The red ability prevents the next damage to Rock Hydra")
    void redAbilityPreventsNextDamage() {
        Permanent hydra = addCreatureReady(player1, new RockHydra());
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, hydra.getId());

        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, hydra.getId());

        assertThat(hydra.getMarkedDamage()).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, hydra)).isEqualTo(3);
    }

    @Test
    @DisplayName("Rock Hydra's counter prevention combines with its red prevention")
    void counterPreventionCombinesWithRedPrevention() {
        Permanent hydra = addCreatureReady(player1, new RockHydra());
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, hydra.getId());

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, hydra.getId());

        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(hydra.getMarkedDamage()).isZero();
        assertThat(gqs.getEffectiveToughness(gd, hydra)).isEqualTo(3);
    }

    @Test
    @DisplayName("Unpreventable damage still removes a +1/+1 counter")
    void unpreventableDamageStillRemovesCounter() {
        addCreatureReady(player1, new Whippoorwill());
        addCreatureReady(player1, new ProdigalSorcerer());
        Permanent hydra = addCreatureReady(player2, new RockHydra());
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, null, hydra.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, hydra.getId());

        harness.activateAbility(player1, 1, null, hydra.getId());
        harness.passBothPriorities();

        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(hydra.getMarkedDamage()).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, hydra)).isEqualTo(3);
    }

    @Test
    @DisplayName("The upkeep ability adds a +1/+1 counter")
    void upkeepAbilityAddsCounter() {
        Permanent hydra = addCreatureReady(player1, new RockHydra());
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("The upkeep ability cannot be activated outside your upkeep")
    void upkeepAbilityCannotBeActivatedOutsideUpkeep() {
        Permanent hydra = addCreatureReady(player1, new RockHydra());
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("upkeep");
    }

    @Test
    @DisplayName("Casting with X=0 puts Rock Hydra into the graveyard")
    void zeroXCountersCannotKeepHydraAlive() {
        harness.setHand(player1, List.of(new RockHydra()));
        harness.addMana(player1, ManaColor.RED, 2);

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Rock Hydra");
        harness.assertInGraveyard(player1, "Rock Hydra");
    }

    @Test
    @DisplayName("Removing the last counter kills an unboosted Rock Hydra")
    void removingLastCounterLeavesZeroToughness() {
        Permanent hydra = addCreatureReady(player1, new RockHydra());
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addCreatureReady(player2, new ProdigalSorcerer());

        harness.activateAbility(player2, 0, null, hydra.getId());
        harness.passBothPriorities();

        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(hydra.getMarkedDamage()).isZero();
        harness.assertNotOnBattlefield(player1, "Rock Hydra");
        harness.assertInGraveyard(player1, "Rock Hydra");
    }

    @Test
    @DisplayName("The controller chooses between red prevention and removing the last counter")
    void controllerCanChooseWhichPreventionApplies() {
        Permanent hydra = addCreatureReady(player1, new RockHydra());
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addCreatureReady(player2, new ProdigalSorcerer());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.activateAbility(player2, 0, null, hydra.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput())
                .as("The controller must choose a prevention effect before damage is processed")
                .isTrue();
        harness.assertOnBattlefield(player1, "Rock Hydra");
    }

    @Test
    @DisplayName("The red shield is consumed by the first point of damage")
    void redShieldDoesNotPreventLaterDamage() {
        Permanent hydra = addCreatureReady(player1, new RockHydra());
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addCreatureReady(player2, new ProdigalSorcerer());
        addCreatureReady(player2, new ProdigalSorcerer());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, hydra.getId());
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.activateAbility(player2, 0, null, hydra.getId());
        harness.passBothPriorities();
        assertThat(hydra.getMarkedDamage()).isZero();

        harness.activateAbility(player2, 1, null, hydra.getId());
        harness.passBothPriorities();
        assertThat(hydra.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Rock Hydra");
    }

    @Test
    @DisplayName("The upkeep ability cannot be activated during an opponent's upkeep")
    void upkeepAbilityCannotBeActivatedDuringOpponentsUpkeep() {
        Permanent hydra = addCreatureReady(player1, new RockHydra());
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("upkeep");
        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The upkeep ability can add counters more than once in the same upkeep")
    void upkeepAbilityCanBeActivatedRepeatedly() {
        Permanent hydra = addCreatureReady(player1, new RockHydra());
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 6);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        harness.assertOnBattlefield(player1, "Rock Hydra");
    }

    @Test
    @DisplayName("Combat damage removes counters and is prevented")
    void combatDamageRemovesCounters() {
        Permanent hydra = addCreatureReady(player2, new RockHydra());
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        addCreatureReady(player1, new ProdigalSorcerer());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(hydra.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Rock Hydra");
        harness.assertInGraveyard(player1, "Prodigal Sorcerer");
    }

    @Test
    @DisplayName("Unpreventable combat damage still removes a counter")
    void unpreventableCombatDamageStillRemovesCounter() {
        Permanent hydra = addCreatureReady(player2, new RockHydra());
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addCreatureReady(player1, new ProdigalSorcerer());
        addCreatureReady(player1, new Whippoorwill());
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player2, 0, hydra.getId());

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 1, null, hydra.getId());
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(hydra.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Rock Hydra");
    }
}
