package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.b.BonesplitterSliver;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TwoHeadedSliver.class, BonesplitterSliver.class, AshcoatBear.class})
class TwoHeadedSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Two-Headed Sliver has menace itself")
    void grantsMenaceToItself() {
        Permanent sliver = addCreatureReady(player1, new TwoHeadedSliver());

        assertThat(gqs.hasKeyword(gd, sliver, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("All Slivers have menace")
    void grantsMenaceToAllSlivers() {
        addCreatureReady(player1, new TwoHeadedSliver());
        Permanent opponentSliver = addCreatureReady(player2, new BonesplitterSliver());

        assertThat(gqs.hasKeyword(gd, opponentSliver, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Non-Sliver creatures do not have menace from Two-Headed Sliver")
    void doesNotGrantMenaceToNonSlivers() {
        addCreatureReady(player1, new TwoHeadedSliver());
        Permanent bear = addCreatureReady(player1, new AshcoatBear());

        assertThat(gqs.hasKeyword(gd, bear, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Menace prevents a single creature from blocking a Sliver")
    void menacePreventsSingleBlocker() {
        addCreatureReady(player1, new TwoHeadedSliver());
        addCreatureReady(player2, new AshcoatBear());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    @DisplayName("Menace allows two creatures to block a Sliver")
    void menaceAllowsTwoBlockers() {
        addCreatureReady(player1, new TwoHeadedSliver());
        Permanent firstBlocker = addCreatureReady(player2, new AshcoatBear());
        Permanent secondBlocker = addCreatureReady(player2, new AshcoatBear());

        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));

        assertThat(firstBlocker.isBlocking()).isTrue();
        assertThat(secondBlocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Slivers on both sides lose granted menace when the source leaves")
    void menaceEndsWhenSourceLeaves() {
        Permanent source = addCreatureReady(player1, new TwoHeadedSliver());
        Permanent ownSliver = addCreatureReady(player1, new BonesplitterSliver());
        Permanent opponentSliver = addCreatureReady(player2, new BonesplitterSliver());

        assertThat(gqs.hasKeyword(gd, ownSliver, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentSliver, Keyword.MENACE)).isTrue();

        harness.getPermanentRemovalService().destroyPermanentToGraveyard(gd, source);

        assertThat(gqs.hasKeyword(gd, ownSliver, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentSliver, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Another source keeps granting menace after one source leaves")
    void remainingSourceContinuesGrantingMenace() {
        Permanent source = addCreatureReady(player1, new TwoHeadedSliver());
        Permanent remainingSource = addCreatureReady(player2, new TwoHeadedSliver());
        Permanent sliver = addCreatureReady(player1, new BonesplitterSliver());

        harness.getPermanentRemovalService().destroyPermanentToGraveyard(gd, source);

        assertThat(gqs.hasKeyword(gd, remainingSource, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, sliver, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Multiple Two-Headed Slivers still allow exactly two blockers")
    void multipleSourcesDoNotIncreaseRequiredBlockers() {
        addCreatureReady(player1, new TwoHeadedSliver());
        addCreatureReady(player1, new TwoHeadedSliver());
        Permanent firstBlocker = addCreatureReady(player2, new AshcoatBear());
        Permanent secondBlocker = addCreatureReady(player2, new AshcoatBear());

        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));

        assertThat(firstBlocker.isBlocking()).isTrue();
        assertThat(secondBlocker.isBlocking()).isTrue();
    }
}
