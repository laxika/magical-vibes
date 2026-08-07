package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BonescytheSliver;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MuscleSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Muscle Sliver boosts itself (it is a Sliver)")
    void boostsSelf() {
        Permanent sliver = addCreatureReady(player1, new MuscleSliver());

        assertThat(gqs.getEffectivePower(gd, sliver)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sliver)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boosts another Sliver you control")
    void boostsOtherSliver() {
        Permanent otherSliver = addCreatureReady(player1, new BonescytheSliver());
        int basePower = gqs.getEffectivePower(gd, otherSliver);
        int baseToughness = gqs.getEffectiveToughness(gd, otherSliver);

        addCreatureReady(player1, new MuscleSliver());

        assertThat(gqs.getEffectivePower(gd, otherSliver)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, otherSliver)).isEqualTo(baseToughness + 1);
    }

    @Test
    @DisplayName("Boosts an opponent's Sliver too (all Slivers)")
    void boostsOpponentSliver() {
        Permanent opponentSliver = addCreatureReady(player2, new BonescytheSliver());
        int basePower = gqs.getEffectivePower(gd, opponentSliver);
        int baseToughness = gqs.getEffectiveToughness(gd, opponentSliver);

        addCreatureReady(player1, new MuscleSliver());

        assertThat(gqs.getEffectivePower(gd, opponentSliver)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, opponentSliver)).isEqualTo(baseToughness + 1);
    }

    @Test
    @DisplayName("Does not boost a non-Sliver creature")
    void doesNotBoostNonSliver() {
        addCreatureReady(player1, new MuscleSliver());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }
}
