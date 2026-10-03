package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BristlingHydra.class})
class BristlingHydraTest extends BaseCardTest {

    @Test
    void entersWithThreeEnergyCounters() {
        harness.castFromHand(player1, new BristlingHydra(), "{2}{G}{G}");
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(3);
    }

    @Test
    void paysEnergyToPutCounterOnHydraAndGrantHexproof() {
        Permanent hydra = addReadyHydra();
        gd.playerEnergyCounters.put(player1.getId(), 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, hydra, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    void hexproofWearsOffAtEndOfTurn() {
        Permanent hydra = addReadyHydra();
        gd.playerEnergyCounters.put(player1.getId(), 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, hydra, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    void cannotActivateWithoutEnoughEnergy() {
        addReadyHydra();
        gd.playerEnergyCounters.put(player1.getId(), 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("three energy counters");
    }

    private Permanent addReadyHydra() {
        return addCreatureReady(player1, new BristlingHydra());
    }

    @Test
    void energyIsPaidImmediatelyButCounterAndHexproofWaitForResolution() {
        Permanent hydra = addReadyHydra();
        gd.playerEnergyCounters.put(player1.getId(), 3);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, hydra, Keyword.HEXPROOF)).isFalse();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("three energy counters");

        harness.passBothPriorities();

        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, hydra, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    void canActivateRepeatedlyWhileTappedAndSummoningSick() {
        Permanent hydra = addReadyHydra();
        hydra.setSummoningSick(true);
        hydra.setTapped(true);
        gd.playerEnergyCounters.put(player1.getId(), 6);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        resolveAllTriggers();

        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, hydra, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    void enteringAddsEnergyToItsControllerWithoutChangingOpponentsEnergy() {
        gd.playerEnergyCounters.put(player1.getId(), 2);
        gd.playerEnergyCounters.put(player2.getId(), 4);
        harness.castFromHand(player1, new BristlingHydra(), "{2}{G}{G}");
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(5);
        assertThat(gd.playerEnergyCounters.get(player2.getId())).isEqualTo(4);
    }
}
