package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(DescentIntoAvernus.class)
class DescentIntoAvernusTest extends BaseCardTest {

    @Test
    @DisplayName("At upkeep, adds two descent counters, creates matching Treasures for each player, and deals matching damage")
    void upkeepTriggerAddsCountersCreatesTreasuresAndDealsDamage() {
        Permanent descent = harness.addToBattlefieldAndReturn(player1, new DescentIntoAvernus());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(descent.getCounterCount(CounterType.DESCENT)).isEqualTo(2);
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(2);
        assertThat(countPermanents(player2, "Treasure")).isEqualTo(2);
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("The upkeep trigger uses the counters added by that trigger")
    void upkeepTriggerUsesNewCounterTotal() {
        Permanent descent = harness.addToBattlefieldAndReturn(player1, new DescentIntoAvernus());
        descent.setCounterCount(CounterType.DESCENT, 3);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(descent.getCounterCount(CounterType.DESCENT)).isEqualTo(5);
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(5);
        assertThat(countPermanents(player2, "Treasure")).isEqualTo(5);
        harness.assertLife(player1, 15);
        harness.assertLife(player2, 15);
    }
}
