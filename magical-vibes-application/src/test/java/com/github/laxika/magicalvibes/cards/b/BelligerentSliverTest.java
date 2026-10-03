package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DiffusionSliver;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.BlockerAssignment;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BelligerentSliver.class, DiffusionSliver.class, RuneclawBear.class})
class BelligerentSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Belligerent Sliver grants itself menace (it is a Sliver)")
    void grantsSelfMenace() {
        Permanent sliver = addCreatureReady(player1, new BelligerentSliver());

        assertThat(gqs.hasKeyword(gd, sliver, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Grants menace to another Sliver you control")
    void grantsMenaceToOtherSliver() {
        addCreatureReady(player1, new BelligerentSliver());
        Permanent otherSliver = addCreatureReady(player1, new DiffusionSliver());

        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Does not grant menace to a non-Sliver creature")
    void doesNotGrantToNonSliver() {
        addCreatureReady(player1, new BelligerentSliver());
        Permanent bears = addCreatureReady(player1, new RuneclawBear());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Does not grant menace to an opponent's Sliver")
    void doesNotGrantToOpponentSliver() {
        addCreatureReady(player1, new BelligerentSliver());
        Permanent opponentSliver = addCreatureReady(player2, new DiffusionSliver());

        assertThat(gqs.hasKeyword(gd, opponentSliver, Keyword.MENACE)).isFalse();
    }

    @Test
    void menaceDisappearsWhenSourceLeaves() {
        Permanent source = addCreatureReady(player1, new BelligerentSliver());
        Permanent sliver = addCreatureReady(player1, new DiffusionSliver());
        assertThat(gqs.hasKeyword(gd, sliver, Keyword.MENACE)).isTrue();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, source));

        assertThat(gqs.hasKeyword(gd, sliver, Keyword.MENACE)).isFalse();
    }

    @Test
    void anotherSourceKeepsGrantingMenace() {
        Permanent source = addCreatureReady(player1, new BelligerentSliver());
        Permanent survivor = addCreatureReady(player1, new BelligerentSliver());
        Permanent sliver = addCreatureReady(player1, new DiffusionSliver());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, source));

        assertThat(gqs.hasKeyword(gd, survivor, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, sliver, Keyword.MENACE)).isTrue();
    }

    @Test
    void sourceCannotBeBlockedByOneCreature() {
        addCreatureReady(player1, new BelligerentSliver());
        addCreatureReady(player2, new RuneclawBear());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked except by two or more creatures");
    }

    @Test
    void grantedMenaceAllowsTwoBlockers() {
        addCreatureReady(player1, new BelligerentSliver());
        addCreatureReady(player1, new DiffusionSliver());
        Permanent first = addCreatureReady(player2, new RuneclawBear());
        Permanent second = addCreatureReady(player2, new RuneclawBear());
        declareAttackersAndPrepareBlockers(List.of(1));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1), new BlockerAssignment(1, 1)));

        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isTrue();
    }

    @Test
    void grantedMenaceCannotBeBlockedByOneCreature() {
        addCreatureReady(player1, new BelligerentSliver());
        addCreatureReady(player1, new DiffusionSliver());
        addCreatureReady(player2, new RuneclawBear());
        declareAttackersAndPrepareBlockers(List.of(1));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked except by two or more creatures");
    }
}
