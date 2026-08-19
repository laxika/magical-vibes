package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SpurGrapplerTest extends BaseCardTest {

    @Test
    void getsBoostWithNoLands() {
        Permanent spurGrappler = addSpurGrappler();

        assertStats(spurGrappler, 4, 2);
    }

    @Test
    void losesBoostWithUntappedLand() {
        Permanent spurGrappler = addSpurGrappler();
        harness.addToBattlefield(player1, new Forest());

        assertStats(spurGrappler, 2, 1);
    }

    @Test
    void keepsBoostWithOnlyTappedLands() {
        Permanent spurGrappler = addSpurGrappler();
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        forest.tap();

        assertStats(spurGrappler, 4, 2);
    }

    @Test
    void opponentLandsDoNotAffectBoost() {
        Permanent spurGrappler = addSpurGrappler();
        harness.addToBattlefield(player2, new Forest());

        assertStats(spurGrappler, 4, 2);
    }

    private Permanent addSpurGrappler() {
        return harness.addToBattlefieldAndReturn(player1, new SpurGrappler());
    }

    private void assertStats(Permanent spurGrappler, int power, int toughness) {
        assertThat(gqs.getEffectivePower(gd, spurGrappler)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, spurGrappler)).isEqualTo(toughness);
    }
}
