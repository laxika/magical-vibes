package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BonescytheSliver;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PlatedSliver.class, BonescytheSliver.class, GrizzlyBears.class})
class PlatedSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Plated Sliver boosts itself")
    void boostsSelf() {
        Permanent sliver = addCreatureReady(player1, new PlatedSliver());

        assertThat(gqs.getEffectivePower(gd, sliver)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, sliver)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boosts Slivers controlled by either player")
    void boostsSliversControlledByEitherPlayer() {
        Permanent ownSliver = addCreatureReady(player1, new BonescytheSliver());
        Permanent opponentSliver = addCreatureReady(player2, new BonescytheSliver());
        int ownBaseToughness = gqs.getEffectiveToughness(gd, ownSliver);
        int opponentBaseToughness = gqs.getEffectiveToughness(gd, opponentSliver);

        addCreatureReady(player1, new PlatedSliver());

        assertThat(gqs.getEffectiveToughness(gd, ownSliver)).isEqualTo(ownBaseToughness + 1);
        assertThat(gqs.getEffectiveToughness(gd, opponentSliver)).isEqualTo(opponentBaseToughness + 1);
    }

    @Test
    @DisplayName("Does not boost a non-Sliver creature")
    void doesNotBoostNonSliver() {
        addCreatureReady(player1, new PlatedSliver());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        int baseToughness = gqs.getEffectiveToughness(gd, bears);

        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(baseToughness);
    }
}
