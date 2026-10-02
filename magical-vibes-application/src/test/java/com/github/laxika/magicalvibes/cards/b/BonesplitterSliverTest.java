package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.s.SidewinderSliver;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BonesplitterSliver.class, SidewinderSliver.class, AshcoatBear.class})
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
        Permanent ownSliver = addCreatureReady(player1, new SidewinderSliver());
        Permanent opponentSliver = addCreatureReady(player2, new SidewinderSliver());

        addCreatureReady(player1, new BonesplitterSliver());

        assertThat(gqs.getEffectivePower(gd, ownSliver)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentSliver)).isEqualTo(3);
    }

    @Test
    @DisplayName("Multiple Bonesplitter Slivers stack")
    void multipleCopiesStack() {
        Permanent sliver = addCreatureReady(player2, new SidewinderSliver());
        addCreatureReady(player1, new BonesplitterSliver());
        addCreatureReady(player1, new BonesplitterSliver());

        assertThat(gqs.getEffectivePower(gd, sliver)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, sliver)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not boost a non-Sliver creature")
    void doesNotBoostNonSliver() {
        addCreatureReady(player1, new BonesplitterSliver());
        Permanent bear = addCreatureReady(player1, new AshcoatBear());

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
    }
}
