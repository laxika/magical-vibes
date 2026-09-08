package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BladeSliver.class, BonescytheSliver.class, GrizzlyBears.class})
class BladeSliverTest extends BaseCardTest {

    @Test
    void boostsAllSliversIncludingItself() {
        Permanent ownSliver = addCreatureReady(player1, new BladeSliver());
        Permanent opponentSliver = addCreatureReady(player2, new BonescytheSliver());

        assertThat(gqs.getEffectivePower(gd, ownSliver)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownSliver)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentSliver)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opponentSliver)).isEqualTo(2);
    }

    @Test
    void doesNotBoostNonSliverCreatures() {
        addCreatureReady(player1, new BladeSliver());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }
}
