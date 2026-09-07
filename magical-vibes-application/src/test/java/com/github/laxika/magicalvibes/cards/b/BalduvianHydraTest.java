package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.z.ZuranSpellcaster;
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

@CardUsed({BalduvianHydra.class, BalduvianBears.class, ZuranSpellcaster.class})
class BalduvianHydraTest extends BaseCardTest {

    @Test
    @DisplayName("Casting with X=3 enters with three +1/+0 counters, making it a 3/1")
    void entersWithXCounters() {
        harness.setHand(player1, List.of(new BalduvianHydra()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gs.playCard(gd, player1, 0, 3, null, null);
        harness.passBothPriorities();

        Permanent hydra = findPermanent(player1, "Balduvian Hydra");
        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ZERO)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, hydra)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, hydra)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting with X=0 enters with no counters and survives as a 0/1")
    void entersWithNoCountersAtXZero() {
        harness.setHand(player1, List.of(new BalduvianHydra()));
        harness.addMana(player1, ManaColor.RED, 2);

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent hydra = findPermanent(player1, "Balduvian Hydra");
        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ZERO)).isEqualTo(0);
        assertThat(gqs.getEffectivePower(gd, hydra)).isEqualTo(0);
    }

    @Test
    @DisplayName("Removing a +1/+0 counter shields it from the next 1 damage")
    void removeCounterShieldsSelf() {
        Permanent hydra = addCreatureReady(player1, new BalduvianHydra());
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ZERO, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ZERO)).isEqualTo(2);
        assertThat(hydra.getDamagePreventionShield()).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, hydra)).isEqualTo(2);
    }

    @Test
    @DisplayName("The shield prevents the next 1 noncombat damage dealt to it")
    void shieldPreventsNoncombatDamage() {
        Permanent hydra = addCreatureReady(player1, new BalduvianHydra());
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ZERO, 3);
        addCreatureReady(player1, new ZuranSpellcaster());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.activateAbility(player1, 1, null, hydra.getId());
        harness.passBothPriorities();

        assertThat(hydra.getMarkedDamage()).isEqualTo(0);
        assertThat(hydra.getDamagePreventionShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("The shield prevents the next 1 combat damage dealt to it")
    void shieldPreventsCombatDamage() {
        Permanent hydra = addCreatureReady(player1, new BalduvianHydra());
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ZERO, 3);
        Permanent attacker = addCreatureReady(player2, new BalduvianBears());
        attacker.setAttacking(true);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(hydra.getMarkedDamage()).isEqualTo(1);
        assertThat(hydra.getDamagePreventionShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot remove a +1/+0 counter when none remain")
    void cannotActivateWithoutCounters() {
        Permanent hydra = addCreatureReady(player1, new BalduvianHydra());
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ZERO, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("{R}{R}{R} during your upkeep puts a +1/+0 counter on it")
    void upkeepAbilityAddsCounter() {
        Permanent hydra = addCreatureReady(player1, new BalduvianHydra());
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ZERO, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ZERO)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, hydra)).isEqualTo(2);
    }

    @Test
    @DisplayName("The {R}{R}{R} ability cannot be activated outside your upkeep")
    void upkeepAbilityCannotBeActivatedInMainPhase() {
        addCreatureReady(player1, new BalduvianHydra());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("upkeep");
    }

    @Test
    @DisplayName("The {R}{R}{R} ability cannot be activated during an opponent's upkeep")
    void upkeepAbilityCannotBeActivatedDuringOpponentsUpkeep() {
        addCreatureReady(player1, new BalduvianHydra());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("upkeep");
    }

    @Test
    @DisplayName("The prevention shield applies only to damage dealt to that Hydra")
    void shieldAppliesOnlyToSourceHydra() {
        Permanent hydra = addCreatureReady(player1, new BalduvianHydra());
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ZERO, 1);
        Permanent bear = addCreatureReady(player1, new BalduvianBears());
        addCreatureReady(player1, new ZuranSpellcaster());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.activateAbility(player1, 2, null, bear.getId());
        harness.passBothPriorities();

        assertThat(hydra.getDamagePreventionShield()).isEqualTo(1);
        assertThat(bear.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("The prevention shield clears at end of turn")
    void shieldClearedAtEndOfTurn() {
        Permanent hydra = addCreatureReady(player1, new BalduvianHydra());
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ZERO, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(hydra.getDamagePreventionShield()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(hydra.getDamagePreventionShield()).isEqualTo(0);
    }
}
