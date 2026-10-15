package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AetherMembrane;
import com.github.laxika.magicalvibes.cards.s.SynchronousSliver;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BatteringSliver.class, SynchronousSliver.class, AetherMembrane.class})
class BatteringSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Battering Sliver gives itself and other Slivers trample")
    void grantsTrampleToAllSliversIncludingItself() {
        Permanent batteringSliver = addCreatureReady(player1, new BatteringSliver());
        Permanent otherSliver = addCreatureReady(player1, new SynchronousSliver());

        assertThat(gqs.hasKeyword(gd, batteringSliver, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Battering Sliver gives opposing Slivers trample")
    void grantsTrampleToOpposingSlivers() {
        addCreatureReady(player1, new BatteringSliver());
        Permanent opposingSliver = addCreatureReady(player2, new SynchronousSliver());

        assertThat(gqs.hasKeyword(gd, opposingSliver, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Battering Sliver does not give trample to non-Slivers")
    void doesNotGrantTrampleToNonSlivers() {
        addCreatureReady(player1, new BatteringSliver());
        Permanent nonSliver = addCreatureReady(player1, new AetherMembrane());

        assertThat(gqs.hasKeyword(gd, nonSliver, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Trample starts when Battering Sliver resolves, not while it is on the stack")
    void grantsTrampleOnlyAfterResolving() {
        Permanent ownSliver = addCreatureReady(player1, new SynchronousSliver());
        Permanent opposingSliver = addCreatureReady(player2, new SynchronousSliver());

        harness.castFromHand(player1, new BatteringSliver(), "{5}{R}");

        assertThat(gqs.hasKeyword(gd, ownSliver, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingSliver, Keyword.TRAMPLE)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Battering Sliver"), Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownSliver, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingSliver, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Slivers lose granted trample when Battering Sliver dies")
    void removesTrampleWhenSourceDies() {
        Permanent source = addCreatureReady(player1, new BatteringSliver());
        Permanent ownSliver = addCreatureReady(player1, new SynchronousSliver());
        Permanent opposingSliver = addCreatureReady(player2, new SynchronousSliver());

        assertThat(gqs.hasKeyword(gd, ownSliver, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingSliver, Keyword.TRAMPLE)).isTrue();

        source.setMarkedDamage(4);
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(source);
        assertThat(gqs.hasKeyword(gd, ownSliver, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingSliver, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Trample remains until the last Battering Sliver leaves the battlefield")
    void retainsTrampleWhileAnotherSourceRemains() {
        Permanent firstSource = addCreatureReady(player1, new BatteringSliver());
        Permanent secondSource = addCreatureReady(player2, new BatteringSliver());
        Permanent sliver = addCreatureReady(player1, new SynchronousSliver());

        firstSource.setMarkedDamage(4);
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(firstSource);
        assertThat(gqs.hasKeyword(gd, secondSource, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, sliver, Keyword.TRAMPLE)).isTrue();

        secondSource.setMarkedDamage(4);
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(secondSource);
        assertThat(gqs.hasKeyword(gd, sliver, Keyword.TRAMPLE)).isFalse();
    }
}
