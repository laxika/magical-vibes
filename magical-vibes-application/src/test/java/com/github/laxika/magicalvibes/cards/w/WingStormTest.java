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

    @Test
    @DisplayName("Only the opponent takes damage when only they control flyers")
    void onlyOpponentWithFlyersTakesDamage() {
        harness.addToBattlefield(player1, new MarshBoa());
        harness.addToBattlefield(player2, new StormwatchEagle());
        harness.addToBattlefield(player2, new StormwatchEagle());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        castWingStorm();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Resolves without damage when neither player controls creatures")
    void emptyBattlefieldDealsNoDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        castWingStorm();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Wing Storm");
    }

    @Test
    @DisplayName("Counts flying creatures at resolution rather than when cast")
    void countsFlyersAtResolution() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.castFromHand(player1, new WingStorm(), "{2}{G}");

        harness.addToBattlefield(player2, new StormwatchEagle());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }
}
