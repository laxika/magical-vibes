package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScrapperChampion.class})
class ScrapperChampionTest extends BaseCardTest {

    @Test
    void entersWithTwoEnergyCounters() {
        harness.castFromHand(player1, new ScrapperChampion(), "{3}{R}");
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
    }

    @Test
    void mayPayEnergyOnAttackToPutCounterOnItself() {
        Permanent champion = addCreatureReady(player1, new ScrapperChampion());
        gd.playerEnergyCounters.put(player1.getId(), 2);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(champion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void decliningEnergyPaymentDoesNotPutCounterOnItself() {
        Permanent champion = addCreatureReady(player1, new ScrapperChampion());
        gd.playerEnergyCounters.put(player1.getId(), 2);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        assertThat(champion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void cannotPayEnergyWithoutTwoEnergyCounters() {
        Permanent champion = addCreatureReady(player1, new ScrapperChampion());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(champion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void enteringAddsEnergyToItsControllerExistingEnergy() {
        harness.forceActivePlayer(player2);
        gd.playerEnergyCounters.put(player2.getId(), 3);
        harness.castFromHand(player2, new ScrapperChampion(), "{3}{R}");
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player2.getId())).isEqualTo(5);
        assertThat(gd.playerEnergyCounters.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    void oneEnergyCannotPayAndIsNotConsumed() {
        Permanent champion = addCreatureReady(player1, new ScrapperChampion());
        gd.playerEnergyCounters.put(player1.getId(), 1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
        assertThat(champion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void paysOnlyTwoEnergyAndAddsOnlyOneCounterPerAttack() {
        Permanent champion = addCreatureReady(player1, new ScrapperChampion());
        gd.playerEnergyCounters.put(player1.getId(), 6);
        champion.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(4);
        assertThat(champion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void energyGainedAfterAttackingCanPayAtResolution() {
        Permanent champion = addCreatureReady(player1, new ScrapperChampion());

        declareAttackers(List.of(0));
        gd.playerEnergyCounters.put(player1.getId(), 2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(champion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void energySpentBeforeResolutionCannotPayForCounter() {
        Permanent champion = addCreatureReady(player1, new ScrapperChampion());
        gd.playerEnergyCounters.put(player1.getId(), 2);

        declareAttackers(List.of(0));
        gd.playerEnergyCounters.put(player1.getId(), 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
        assertThat(champion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void attackCounterIncreasesBothUnblockedCombatDamageSteps() {
        addCreatureReady(player1, new ScrapperChampion());
        gd.playerEnergyCounters.put(player1.getId(), 2);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }
}
