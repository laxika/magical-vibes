package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.m.MarshBoa;
import com.github.laxika.magicalvibes.cards.s.StormwatchEagle;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WingStorm.class, MarshBoa.class, StormwatchEagle.class})
class WingStormTest extends BaseCardTest {

    private void castWingStorm() {
        harness.castFromHand(player1, new WingStorm(), "{2}{G}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Deals twice the number of flying creatures to each player")
    void dealsTwiceFlyingCreatureCountToEachPlayer() {
        harness.addToBattlefield(player1, new StormwatchEagle());
        harness.addToBattlefield(player1, new StormwatchEagle());
        harness.addToBattlefield(player1, new MarshBoa());
        harness.addToBattlefield(player2, new StormwatchEagle());
        harness.addToBattlefield(player2, new MarshBoa());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        castWingStorm();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Ignores creatures without flying")
    void ignoresCreaturesWithoutFlying() {
        harness.addToBattlefield(player1, new MarshBoa());
        harness.addToBattlefield(player2, new MarshBoa());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        castWingStorm();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
