package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LegacysAllureTest extends BaseCardTest {

    private Permanent addAllureWithCounters(int treasureCounters) {
        harness.addToBattlefield(player1, new LegacysAllure());
        Permanent allure = findPermanent(player1, "Legacy's Allure");
        allure.setCounterCount(CounterType.TREASURE, treasureCounters);
        return allure;
    }

    @Test
    @DisplayName("Accepting the upkeep trigger puts a treasure counter on it")
    void upkeepAcceptedAddsCounter() {
        Permanent allure = addAllureWithCounters(0);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(allure.getCounterCount(CounterType.TREASURE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the upkeep trigger leaves the counters unchanged")
    void upkeepDeclinedAddsNoCounter() {
        Permanent allure = addAllureWithCounters(0);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(allure.getCounterCount(CounterType.TREASURE)).isZero();
    }

    @Test
    @DisplayName("Sacrificing with two treasure counters steals a 2/2 permanently")
    void stealsCreatureWithinCounterCount() {
        addAllureWithCounters(2);
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent target = findPermanent(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Legacy's Allure");
    }

    @Test
    @DisplayName("Cannot target a creature with power above the treasure counter count")
    void cannotTargetTooLargeCreature() {
        addAllureWithCounters(1);
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent target = findPermanent(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature with power");
    }

    @Test
    @DisplayName("With no treasure counters even a 4/4 stays untouchable")
    void noCountersMeansNoLegalTarget() {
        addAllureWithCounters(0);
        harness.addToBattlefield(player2, new AirElemental());

        Permanent target = findPermanent(player2, "Air Elemental");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature with power");
    }
}
