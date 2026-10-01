package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.f.FurySliver;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MightSliver.class, FurySliver.class, AshcoatBear.class})
class MightSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Might Sliver boosts itself")
    void boostsSelf() {
        Permanent sliver = addCreatureReady(player1, new MightSliver());

        assertThat(gqs.getEffectivePower(gd, sliver)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, sliver)).isEqualTo(4);
    }

    @Test
    @DisplayName("Boosts another Sliver you control")
    void boostsOtherSliver() {
        Permanent otherSliver = addCreatureReady(player1, new FurySliver());
        int basePower = gqs.getEffectivePower(gd, otherSliver);
        int baseToughness = gqs.getEffectiveToughness(gd, otherSliver);

        addCreatureReady(player1, new MightSliver());

        assertThat(gqs.getEffectivePower(gd, otherSliver)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, otherSliver)).isEqualTo(baseToughness + 2);
    }

    @Test
    @DisplayName("Boosts an opponent's Sliver too")
    void boostsOpponentSliver() {
        Permanent opponentSliver = addCreatureReady(player2, new FurySliver());
        int basePower = gqs.getEffectivePower(gd, opponentSliver);
        int baseToughness = gqs.getEffectiveToughness(gd, opponentSliver);

        addCreatureReady(player1, new MightSliver());

        assertThat(gqs.getEffectivePower(gd, opponentSliver)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, opponentSliver)).isEqualTo(baseToughness + 2);
    }

    @Test
    @DisplayName("Multiple Might Slivers stack")
    void multipleCopiesStack() {
        Permanent sliver = addCreatureReady(player2, new FurySliver());

        addCreatureReady(player1, new MightSliver());
        addCreatureReady(player1, new MightSliver());

        assertThat(gqs.getEffectivePower(gd, sliver)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, sliver)).isEqualTo(7);
    }

    @Test
    @DisplayName("Does not boost a non-Sliver creature")
    void doesNotBoostNonSliver() {
        addCreatureReady(player1, new MightSliver());
        Permanent bear = addCreatureReady(player1, new AshcoatBear());

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
    }
}
