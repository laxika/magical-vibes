package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.b.BonescytheSliver;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BonesplitterSliver.class, BonescytheSliver.class, GrizzlyBears.class})
class BonesplitterSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Bonesplitter Sliver boosts itself")
    void boostsSelf() {
        Permanent sliver = addCreatureReady(player1, new BonesplitterSliver());

        assertThat(gqs.getEffectivePower(gd, sliver)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, sliver)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boosts Slivers controlled by either player")
    void boostsSliversControlledByEitherPlayer() {
        Permanent ownSliver = addCreatureReady(player1, new BonescytheSliver());
        Permanent opponentSliver = addCreatureReady(player2, new BonescytheSliver());

        int ownBasePower = gqs.getEffectivePower(gd, ownSliver);
        int opponentBasePower = gqs.getEffectivePower(gd, opponentSliver);
        addCreatureReady(player1, new BonesplitterSliver());

        assertThat(gqs.getEffectivePower(gd, ownSliver)).isEqualTo(ownBasePower + 2);
        assertThat(gqs.getEffectivePower(gd, opponentSliver)).isEqualTo(opponentBasePower + 2);
    }

    @Test
    @DisplayName("Does not boost a non-Sliver creature")
    void doesNotBoostNonSliver() {
        addCreatureReady(player1, new BonesplitterSliver());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }
}
