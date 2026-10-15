package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.b.BonesplitterSliver;
import com.github.laxika.magicalvibes.cards.s.Snapback;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FurySliver.class, BonesplitterSliver.class, AshcoatBear.class, Snapback.class})
class FurySliverTest extends BaseCardTest {

    @Test
    @DisplayName("Fury Sliver grants itself double strike")
    void grantsDoubleStrikeToItself() {
        Permanent furySliver = addCreatureReady(player1, new FurySliver());

        assertThat(gqs.hasKeyword(gd, furySliver, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Fury Sliver grants double strike to another Sliver")
    void grantsDoubleStrikeToAnotherSliver() {
        addCreatureReady(player1, new FurySliver());
        Permanent sliver = addCreatureReady(player1, new BonesplitterSliver());

        assertThat(gqs.hasKeyword(gd, sliver, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Fury Sliver grants double strike to an opponent's Sliver")
    void grantsDoubleStrikeToOpposingSliver() {
        addCreatureReady(player1, new FurySliver());
        Permanent sliver = addCreatureReady(player2, new BonesplitterSliver());

        assertThat(gqs.hasKeyword(gd, sliver, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Fury Sliver does not grant double strike to a non-Sliver")
    void doesNotGrantDoubleStrikeToNonSliver() {
        addCreatureReady(player1, new FurySliver());
        Permanent bear = addCreatureReady(player1, new AshcoatBear());

        assertThat(gqs.hasKeyword(gd, bear, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("An unblocked Sliver deals damage in both combat damage steps")
    void grantedDoubleStrikeDealsDamageTwice() {
        addCreatureReady(player1, new FurySliver());
        addCreatureReady(player1, new BonesplitterSliver());
        harness.setLife(player2, 20);

        declareAttackers(List.of(1));
        resolveCombat();

        harness.assertLife(player2, 12);
    }

    @Test
    @DisplayName("Slivers on both sides lose double strike when the source leaves")
    void doubleStrikeEndsWhenSourceLeaves() {
        Permanent source = addCreatureReady(player1, new FurySliver());
        Permanent ownSliver = addCreatureReady(player1, new BonesplitterSliver());
        Permanent opposingSliver = addCreatureReady(player2, new BonesplitterSliver());
        assertThat(gqs.hasKeyword(gd, ownSliver, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingSliver, Keyword.DOUBLE_STRIKE)).isTrue();
        harness.setHand(player1, List.of(new Snapback()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, source.getId());

        harness.assertInHand(player1, "Fury Sliver");
        assertThat(gqs.hasKeyword(gd, ownSliver, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingSliver, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("A second Fury Sliver keeps granting double strike after the first leaves")
    void secondSourceKeepsDoubleStrikeActive() {
        Permanent source = addCreatureReady(player1, new FurySliver());
        Permanent remainingSource = addCreatureReady(player2, new FurySliver());
        Permanent sliver = addCreatureReady(player1, new BonesplitterSliver());
        harness.setHand(player1, List.of(new Snapback()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, source.getId());

        assertThat(gqs.hasKeyword(gd, remainingSource, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, sliver, Keyword.DOUBLE_STRIKE)).isTrue();
    }
}
