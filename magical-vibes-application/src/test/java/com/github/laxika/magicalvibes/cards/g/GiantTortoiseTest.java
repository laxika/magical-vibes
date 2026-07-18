package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GiantTortoiseTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +0/+3 while untapped")
    void untappedGetsBoost() {
        harness.addToBattlefield(player1, new GiantTortoise());

        Permanent tortoise = findPermanent(player1, "Giant Tortoise");

        assertThat(tortoise.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, tortoise)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, tortoise)).isEqualTo(4);
    }

    @Test
    @DisplayName("Loses the boost while tapped")
    void tappedNoBoost() {
        harness.addToBattlefield(player1, new GiantTortoise());

        Permanent tortoise = findPermanent(player1, "Giant Tortoise");
        tortoise.tap();

        assertThat(gqs.getEffectivePower(gd, tortoise)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, tortoise)).isEqualTo(1);
    }

    @Test
    @DisplayName("Boost is removed when it taps and restored when it untaps")
    void boostFollowsTapState() {
        harness.addToBattlefield(player1, new GiantTortoise());

        Permanent tortoise = findPermanent(player1, "Giant Tortoise");
        assertThat(gqs.getEffectiveToughness(gd, tortoise)).isEqualTo(4);

        tortoise.tap();
        assertThat(gqs.getEffectiveToughness(gd, tortoise)).isEqualTo(1);

        tortoise.untap();
        assertThat(gqs.getEffectiveToughness(gd, tortoise)).isEqualTo(4);
    }
}
