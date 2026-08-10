package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SkyshroudEliteTest extends BaseCardTest {

    @Test
    void hasBaseStatsWithoutAnOpponentNonbasicLand() {
        harness.addToBattlefield(player1, new SkyshroudElite());

        assertStats(1, 1);
    }

    @Test
    void getsPlusOnePlusTwoWhenOpponentControlsNonbasicLand() {
        harness.addToBattlefield(player1, new SkyshroudElite());
        harness.addToBattlefield(player2, new ShivanReef());

        assertStats(2, 3);
    }

    @Test
    void basicLandDoesNotGrantBonus() {
        harness.addToBattlefield(player1, new SkyshroudElite());
        harness.addToBattlefield(player2, new Forest());

        assertStats(1, 1);
    }

    @Test
    void ownNonbasicLandDoesNotGrantBonus() {
        harness.addToBattlefield(player1, new SkyshroudElite());
        harness.addToBattlefield(player1, new ShivanReef());

        assertStats(1, 1);
    }

    @Test
    void losesBonusWhenOpponentNonbasicLandLeaves() {
        harness.addToBattlefield(player1, new SkyshroudElite());
        harness.addToBattlefield(player2, new ShivanReef());

        assertStats(2, 3);

        gd.playerBattlefields.get(player2.getId()).removeIf(permanent -> permanent.getCard() instanceof ShivanReef);

        assertStats(1, 1);
    }

    private void assertStats(int power, int toughness) {
        Permanent elite = findPermanent(player1, "Skyshroud Elite");
        assertThat(gqs.getEffectivePower(gd, elite)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, elite)).isEqualTo(toughness);
    }
}
