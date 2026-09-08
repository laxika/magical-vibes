package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PulseTrackerTest extends BaseCardTest {

    @Test
    @DisplayName("Each opponent loses 1 life when Pulse Tracker attacks")
    void eachOpponentLosesLifeWhenItAttacks() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new PulseTracker());

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }
}
