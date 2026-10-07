package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BonescytheSliver;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpinneretSliver.class, BonescytheSliver.class, GrizzlyBears.class, SidewinderSliver.class})
class SpinneretSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Spinneret Sliver grants reach to itself")
    void grantsReachToSelf() {
        Permanent spinneretSliver = addCreatureReady(player1, new SpinneretSliver());

        assertThat(gqs.hasKeyword(gd, spinneretSliver, Keyword.REACH)).isTrue();
    }

    @Test
    @DisplayName("Spinneret Sliver grants reach to all Slivers")
    void grantsReachToAllSlivers() {
        Permanent ownSliver = addCreatureReady(player1, new BonescytheSliver());
        Permanent opponentSliver = addCreatureReady(player2, new BonescytheSliver());
        addCreatureReady(player1, new SpinneretSliver());

        assertThat(gqs.hasKeyword(gd, ownSliver, Keyword.REACH)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentSliver, Keyword.REACH)).isTrue();
    }

    @Test
    @DisplayName("Spinneret Sliver does not grant reach to non-Slivers")
    void doesNotGrantReachToNonSlivers() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new SpinneretSliver());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.REACH)).isFalse();
    }

    @Test
    @DisplayName("Slivers entering after Spinneret Sliver also gain reach")
    void grantsReachToSliversEnteringLater() {
        addCreatureReady(player1, new SpinneretSliver());
        Permanent ownSliver = addCreatureReady(player1, new SidewinderSliver());
        Permanent opponentSliver = addCreatureReady(player2, new SidewinderSliver());

        assertThat(gqs.hasKeyword(gd, ownSliver, Keyword.REACH)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentSliver, Keyword.REACH)).isTrue();
    }

    @Test
    @DisplayName("Slivers lose granted reach when Spinneret Sliver leaves the battlefield")
    void reachEndsWhenSourceLeaves() {
        Permanent ownSliver = addCreatureReady(player1, new SidewinderSliver());
        Permanent opponentSliver = addCreatureReady(player2, new SidewinderSliver());
        Permanent source = addCreatureReady(player2, new SpinneretSliver());

        assertThat(gqs.hasKeyword(gd, ownSliver, Keyword.REACH)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentSliver, Keyword.REACH)).isTrue();

        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, source);

        harness.assertInGraveyard(player2, "Spinneret Sliver");
        assertThat(gqs.hasKeyword(gd, ownSliver, Keyword.REACH)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentSliver, Keyword.REACH)).isFalse();
    }

    @Test
    @DisplayName("Reach remains until the last Spinneret Sliver leaves")
    void reachRemainsWhileAnotherSourceExists() {
        Permanent sliver = addCreatureReady(player1, new SidewinderSliver());
        Permanent firstSource = addCreatureReady(player1, new SpinneretSliver());
        Permanent secondSource = addCreatureReady(player2, new SpinneretSliver());

        assertThat(gqs.hasKeyword(gd, sliver, Keyword.REACH)).isTrue();
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, firstSource);
        assertThat(gqs.hasKeyword(gd, sliver, Keyword.REACH)).isTrue();
        assertThat(gqs.hasKeyword(gd, secondSource, Keyword.REACH)).isTrue();

        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, secondSource);
        assertThat(gqs.hasKeyword(gd, sliver, Keyword.REACH)).isFalse();
    }
}
