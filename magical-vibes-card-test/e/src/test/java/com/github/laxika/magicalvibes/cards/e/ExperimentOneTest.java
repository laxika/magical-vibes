package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.Crocanura;
import com.github.laxika.magicalvibes.cards.d.DiscipleOfTheOldWays;
import com.github.laxika.magicalvibes.cards.s.Slaughterhorn;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ExperimentOne.class, DiscipleOfTheOldWays.class, Crocanura.class, Slaughterhorn.class})
class ExperimentOneTest extends BaseCardTest {

    @Test
    @DisplayName("Evolve puts a +1/+1 counter on Experiment One when a bigger creature enters")
    void evolvesWhenBiggerCreatureEnters() {
        Permanent experimentOne = harness.addToBattlefieldAndReturn(player1, new ExperimentOne());

        harness.setHand(player1, List.of(new DiscipleOfTheOldWays()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(experimentOne.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Evolve does not trigger for a creature with equal power and toughness")
    void doesNotEvolveForEqualStats() {
        Permanent experimentOne = harness.addToBattlefieldAndReturn(player1, new ExperimentOne());

        harness.setHand(player1, List.of(new ExperimentOne()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(experimentOne.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Activating the ability removes two +1/+1 counters and grants a regeneration shield")
    void abilityRemovesTwoCountersAndGrantsShield() {
        Permanent experimentOne = addCreatureReady(player1, new ExperimentOne());
        experimentOne.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(experimentOne.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(experimentOne.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate the ability with only one +1/+1 counter")
    void cannotActivateWithOneCounter() {
        Permanent experimentOne = addCreatureReady(player1, new ExperimentOne());
        experimentOne.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough counters");
    }

    @Test
    @DisplayName("Regeneration shield saves Experiment One from lethal combat damage")
    void regenerationSavesFromLethalCombatDamage() {
        Permanent experimentOne = addCreatureReady(player1, new ExperimentOne());
        experimentOne.setRegenerationShield(1);
        experimentOne.setBlocking(true);
        experimentOne.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new DiscipleOfTheOldWays());
        attacker.setAttacking(true);
        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Experiment One");
        Permanent survivor = findPermanent(player1, "Experiment One");
        assertThat(survivor.isTapped()).isTrue();
        assertThat(survivor.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("Evolve triggers when only the entering creature's toughness is greater")
    void evolvesForGreaterToughnessOnly() {
        Permanent experimentOne = harness.addToBattlefieldAndReturn(player1, new ExperimentOne());
        harness.setHand(player1, List.of(new Crocanura()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(experimentOne.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Evolve triggers when only the entering creature's power is greater")
    void evolvesForGreaterPowerOnly() {
        Permanent experimentOne = harness.addToBattlefieldAndReturn(player1, new ExperimentOne());
        experimentOne.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new Slaughterhorn()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(experimentOne.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Evolve does not trigger when an opponent's bigger creature enters")
    void doesNotEvolveForOpponentCreature() {
        Permanent experimentOne = harness.addToBattlefieldAndReturn(player1, new ExperimentOne());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new DiscipleOfTheOldWays()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(experimentOne.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Evolve checks the creatures' stats again when the trigger resolves")
    void doesNotEvolveIfSourceHasCaughtUpBeforeResolution() {
        Permanent experimentOne = harness.addToBattlefieldAndReturn(player1, new ExperimentOne());
        harness.setHand(player1, List.of(new DiscipleOfTheOldWays()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        experimentOne.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(experimentOne.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A tapped summoning-sick Experiment One pays the counter cost immediately")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent experimentOne = harness.addToBattlefieldAndReturn(player1, new ExperimentOne());
        experimentOne.setSummoningSick(true);
        experimentOne.tap();
        experimentOne.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        harness.activateAbility(player1, 0, null, null);

        assertThat(experimentOne.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(experimentOne.getRegenerationShield()).isZero();
        harness.passBothPriorities();
        assertThat(experimentOne.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("The activated regeneration shield prevents destruction and removes damage and combat status")
    void activatedShieldRegeneratesInCombat() {
        Permanent experimentOne = addCreatureReady(player1, new ExperimentOne());
        experimentOne.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        experimentOne.setBlocking(true);
        experimentOne.addBlockingTarget(0);
        Permanent attacker = addCreatureReady(player2, new DiscipleOfTheOldWays());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Experiment One");
        assertThat(experimentOne.isTapped()).isTrue();
        assertThat(experimentOne.isBlocking()).isFalse();
        assertThat(experimentOne.getBlockingTargets()).isEmpty();
        assertThat(experimentOne.getMarkedDamage()).isZero();
        assertThat(experimentOne.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("Removing counters can cause lethal damage before the regeneration ability resolves")
    void diesIfPayingCounterCostMakesMarkedDamageLethal() {
        Permanent experimentOne = harness.addToBattlefieldAndReturn(player1, new ExperimentOne());
        experimentOne.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        experimentOne.setMarkedDamage(1);

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Experiment One");
        harness.assertInGraveyard(player1, "Experiment One");
    }
}
