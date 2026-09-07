package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BonescytheSliver;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SinewSliver.class, BonescytheSliver.class, GrizzlyBears.class})
class SinewSliverTest extends BaseCardTest {

    @Test
    void boostsSelf() {
        Permanent sliver = addCreatureReady(player1, new SinewSliver());

        assertThat(gqs.getEffectivePower(gd, sliver)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sliver)).isEqualTo(2);
    }

    @Test
    void boostsOtherSliverYouControl() {
        Permanent otherSliver = addCreatureReady(player1, new BonescytheSliver());
        int basePower = gqs.getEffectivePower(gd, otherSliver);
        int baseToughness = gqs.getEffectiveToughness(gd, otherSliver);

        addCreatureReady(player1, new SinewSliver());

        assertThat(gqs.getEffectivePower(gd, otherSliver)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, otherSliver)).isEqualTo(baseToughness + 1);
    }

    @Test
    void boostsOpponentsSlivers() {
        Permanent opponentSliver = addCreatureReady(player2, new BonescytheSliver());
        int basePower = gqs.getEffectivePower(gd, opponentSliver);
        int baseToughness = gqs.getEffectiveToughness(gd, opponentSliver);

        addCreatureReady(player1, new SinewSliver());

        assertThat(gqs.getEffectivePower(gd, opponentSliver)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, opponentSliver)).isEqualTo(baseToughness + 1);
    }

    @Test
    void doesNotBoostNonSliverCreatures() {
        addCreatureReady(player1, new SinewSliver());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }
}
