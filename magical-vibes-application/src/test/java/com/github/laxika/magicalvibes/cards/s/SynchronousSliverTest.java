package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantDustwasp;
import com.github.laxika.magicalvibes.cards.p.PoulticeSliver;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SynchronousSliver.class, PoulticeSliver.class, GiantDustwasp.class})
class SynchronousSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Synchronous Sliver grants itself vigilance")
    void grantsSelfVigilance() {
        Permanent sliver = addCreatureReady(player1, new SynchronousSliver());

        assertThat(gqs.hasKeyword(gd, sliver, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Grants vigilance to another Sliver you control")
    void grantsVigilanceToOtherSliver() {
        addCreatureReady(player1, new SynchronousSliver());
        Permanent otherSliver = addCreatureReady(player1, new PoulticeSliver());

        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Grants vigilance to an opponent's Sliver too")
    void grantsVigilanceToOpponentSliver() {
        addCreatureReady(player1, new SynchronousSliver());
        Permanent opponentSliver = addCreatureReady(player2, new PoulticeSliver());

        assertThat(gqs.hasKeyword(gd, opponentSliver, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Does not grant vigilance to a non-Sliver creature")
    void doesNotGrantToNonSliver() {
        addCreatureReady(player1, new SynchronousSliver());
        Permanent nonSliver = addCreatureReady(player1, new GiantDustwasp());

        assertThat(gqs.hasKeyword(gd, nonSliver, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Stops granting vigilance when it leaves the battlefield")
    void stopsGrantingVigilanceWhenSourceLeavesBattlefield() {
        Permanent source = addCreatureReady(player1, new SynchronousSliver());
        Permanent otherSliver = addCreatureReady(player1, new PoulticeSliver());

        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.VIGILANCE)).isTrue();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, source));

        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Slivers attack without tapping while non-Slivers tap normally")
    void sliversAttackWithoutTapping() {
        Permanent source = addCreatureReady(player1, new SynchronousSliver());
        Permanent otherSliver = addCreatureReady(player1, new PoulticeSliver());
        Permanent nonSliver = addCreatureReady(player1, new GiantDustwasp());

        declareAttackersAndPrepareBlockers(List.of(0, 1, 2));

        assertThat(source.isTapped()).isFalse();
        assertThat(otherSliver.isTapped()).isFalse();
        assertThat(nonSliver.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An opponent's Sliver attacks without tapping")
    void opponentSliverAttacksWithoutTapping() {
        addCreatureReady(player1, new SynchronousSliver());
        Permanent opponentSliver = addCreatureReady(player2, new PoulticeSliver());

        declareAttackersAndPrepareBlockers(player2, List.of(0));

        assertThat(opponentSliver.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Vigilance remains until the last Synchronous Sliver leaves")
    void vigilanceRemainsWhileAnotherSourceExists() {
        Permanent firstSource = addCreatureReady(player1, new SynchronousSliver());
        Permanent secondSource = addCreatureReady(player2, new SynchronousSliver());
        Permanent otherSliver = addCreatureReady(player1, new PoulticeSliver());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, firstSource));

        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, secondSource, Keyword.VIGILANCE)).isTrue();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, secondSource));

        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.VIGILANCE)).isFalse();
    }
}
