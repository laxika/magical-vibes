package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GiantDustwasp;
import com.github.laxika.magicalvibes.cards.s.SynchronousSliver;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ReflexSliver.class, SynchronousSliver.class, GiantDustwasp.class})
class ReflexSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Newly entered Slivers can attack, but non-Slivers cannot")
    void newlyEnteredSliversCanAttack() {
        Permanent source = harness.enterBattlefieldAndReturn(player1, new ReflexSliver());
        Permanent sliver = harness.enterBattlefieldAndReturn(player1, new SynchronousSliver());
        Permanent nonSliver = harness.enterBattlefieldAndReturn(player1, new GiantDustwasp());

        assertThat(source.isSummoningSick()).isTrue();
        assertThat(sliver.isSummoningSick()).isTrue();
        assertThat(nonSliver.isSummoningSick()).isTrue();
        assertThat(als.canAttack(gd, source, player1.getId())).isTrue();
        assertThat(als.canAttack(gd, sliver, player1.getId())).isTrue();
        assertThat(als.canAttack(gd, nonSliver, player1.getId())).isFalse();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, source));

        assertThat(als.canAttack(gd, sliver, player1.getId())).isFalse();
    }

    @Test
    @DisplayName("Haste remains while either player's Reflex Sliver remains")
    void overlappingSourcesKeepGrantingHaste() {
        Permanent firstSource = addCreatureReady(player1, new ReflexSliver());
        Permanent secondSource = addCreatureReady(player2, new ReflexSliver());
        Permanent sliver = addCreatureReady(player1, new SynchronousSliver());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, firstSource));

        assertThat(gqs.hasKeyword(gd, sliver, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, secondSource, Keyword.HASTE)).isTrue();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, secondSource));

        assertThat(gqs.hasKeyword(gd, sliver, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Reflex Sliver grants itself haste")
    void grantsSelfHaste() {
        Permanent sliver = addCreatureReady(player1, new ReflexSliver());

        assertThat(gqs.hasKeyword(gd, sliver, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Grants haste to another Sliver you control")
    void grantsHasteToAnotherSliver() {
        addCreatureReady(player1, new ReflexSliver());
        Permanent otherSliver = addCreatureReady(player1, new SynchronousSliver());

        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Grants haste to an opponent's Sliver")
    void grantsHasteToOpponentSliver() {
        addCreatureReady(player1, new ReflexSliver());
        Permanent opponentSliver = addCreatureReady(player2, new SynchronousSliver());

        assertThat(gqs.hasKeyword(gd, opponentSliver, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Does not grant haste to a non-Sliver creature")
    void doesNotGrantToNonSliver() {
        addCreatureReady(player1, new ReflexSliver());
        Permanent nonSliver = addCreatureReady(player1, new GiantDustwasp());

        assertThat(gqs.hasKeyword(gd, nonSliver, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Stops granting haste when it leaves the battlefield")
    void stopsGrantingHasteWhenSourceLeavesBattlefield() {
        Permanent source = addCreatureReady(player1, new ReflexSliver());
        Permanent otherSliver = addCreatureReady(player1, new SynchronousSliver());

        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.HASTE)).isTrue();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, source));

        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.HASTE)).isFalse();
    }
}
