package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantDustwasp;
import com.github.laxika.magicalvibes.cards.p.PoulticeSliver;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
}
