package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AetherMembrane;
import com.github.laxika.magicalvibes.cards.p.PoulticeSliver;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpittingSliver.class, PoulticeSliver.class, AetherMembrane.class})
class SpittingSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Spitting Sliver grants itself first strike")
    void grantsSelfFirstStrike() {
        Permanent sliver = addCreatureReady(player1, new SpittingSliver());

        assertThat(gqs.hasKeyword(gd, sliver, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Grants first strike to Slivers controlled by either player")
    void grantsFirstStrikeToAllSlivers() {
        addCreatureReady(player1, new SpittingSliver());
        Permanent ownSliver = addCreatureReady(player1, new PoulticeSliver());
        Permanent opponentSliver = addCreatureReady(player2, new PoulticeSliver());

        assertThat(gqs.hasKeyword(gd, ownSliver, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentSliver, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Does not grant first strike to a non-Sliver creature")
    void doesNotGrantToNonSliver() {
        addCreatureReady(player1, new SpittingSliver());
        Permanent nonSliver = addCreatureReady(player1, new AetherMembrane());

        assertThat(gqs.hasKeyword(gd, nonSliver, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Stops granting first strike when it leaves the battlefield")
    void stopsGrantingFirstStrikeWhenSourceLeavesBattlefield() {
        Permanent source = addCreatureReady(player1, new SpittingSliver());
        Permanent otherSliver = addCreatureReady(player1, new PoulticeSliver());

        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.FIRST_STRIKE)).isTrue();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, source));

        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.FIRST_STRIKE)).isFalse();
    }
}
