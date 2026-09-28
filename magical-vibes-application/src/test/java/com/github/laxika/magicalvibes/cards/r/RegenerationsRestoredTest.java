package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(RegenerationsRestored.class)
class RegenerationsRestoredTest extends BaseCardTest {

    @Test
    void entersWithTwelveTimeCounters() {
        Permanent source = harness.enterBattlefieldAndReturn(player1, new RegenerationsRestored());

        assertThat(source.getCounterCount(CounterType.TIME)).isEqualTo(12);
    }

    @Test
    void removingTimeCounterScriesAndGainsLife() {
        harness.setLibrary(player1, java.util.List.of());
        Permanent source = harness.addToBattlefieldAndReturn(player1, new RegenerationsRestored());
        source.setCounterCount(CounterType.TIME, 2);
        harness.setLife(player1, 20);

        source.setCounterCount(CounterType.TIME, 1);
        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(source.getCounterCount(CounterType.TIME)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        harness.assertOnBattlefield(player1, "Regenerations Restored");
    }

    @Test
    void removingLastTimeCounterExilesItAndQueuesAnExtraTurn() {
        harness.setLibrary(player1, java.util.List.of());
        Permanent source = harness.addToBattlefieldAndReturn(player1, new RegenerationsRestored());
        source.setCounterCount(CounterType.TIME, 1);

        source.setCounterCount(CounterType.TIME, 0);
        harness.runStateBasedActions();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Regenerations Restored");
        assertThat(gd.extraTurns).containsExactly(player1.getId());
    }
}
