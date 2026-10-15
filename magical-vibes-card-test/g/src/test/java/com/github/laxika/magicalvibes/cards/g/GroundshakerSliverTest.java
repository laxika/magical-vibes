package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BonescytheSliver;
import com.github.laxika.magicalvibes.cards.d.Disperse;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GroundshakerSliver.class, BonescytheSliver.class, GrizzlyBears.class, Disperse.class})
class GroundshakerSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Groundshaker Sliver grants itself trample (it is a Sliver)")
    void grantsSelfTrample() {
        Permanent sliver = addCreatureReady(player1, new GroundshakerSliver());

        assertThat(gqs.hasKeyword(gd, sliver, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Grants trample to another Sliver you control")
    void grantsTrampleToOtherSliver() {
        addCreatureReady(player1, new GroundshakerSliver());
        Permanent otherSliver = addCreatureReady(player1, new BonescytheSliver());

        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Does not grant trample to a non-Sliver creature")
    void doesNotGrantToNonSliver() {
        addCreatureReady(player1, new GroundshakerSliver());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Does not grant trample to an opponent's Sliver")
    void doesNotGrantToOpponentSliver() {
        addCreatureReady(player1, new GroundshakerSliver());
        Permanent opponentSliver = addCreatureReady(player2, new BonescytheSliver());

        assertThat(gqs.hasKeyword(gd, opponentSliver, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Slivers already on the battlefield gain trample when Groundshaker enters")
    void grantsTrampleToExistingSliversOnEntering() {
        Permanent sliver = addCreatureReady(player1, new BonescytheSliver());
        assertThat(gqs.hasKeyword(gd, sliver, Keyword.TRAMPLE)).isFalse();

        harness.enterBattlefieldAndReturn(player1, new GroundshakerSliver());

        assertThat(gqs.hasKeyword(gd, sliver, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Slivers lose granted trample when Groundshaker leaves the battlefield")
    void removesTrampleWhenSourceLeaves() {
        Permanent source = addCreatureReady(player1, new GroundshakerSliver());
        Permanent sliver = addCreatureReady(player1, new BonescytheSliver());
        assertThat(gqs.hasKeyword(gd, sliver, Keyword.TRAMPLE)).isTrue();
        harness.setHand(player1, List.of(new Disperse()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, source.getId());

        harness.assertInHand(player1, "Groundshaker Sliver");
        harness.assertNotOnBattlefield(player1, "Groundshaker Sliver");
        assertThat(gqs.hasKeyword(gd, sliver, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, sliver, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Trample remains while another Groundshaker is still on the battlefield")
    void retainsTrampleUntilLastSourceLeaves() {
        Permanent firstSource = addCreatureReady(player1, new GroundshakerSliver());
        Permanent secondSource = addCreatureReady(player1, new GroundshakerSliver());
        Permanent sliver = addCreatureReady(player1, new BonescytheSliver());
        harness.setHand(player1, List.of(new Disperse(), new Disperse()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveInstant(player1, 0, firstSource.getId());

        assertThat(gqs.hasKeyword(gd, sliver, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, secondSource, Keyword.TRAMPLE)).isTrue();

        harness.castAndResolveInstant(player1, 0, secondSource.getId());

        assertThat(gqs.hasKeyword(gd, sliver, Keyword.TRAMPLE)).isFalse();
        assertThat(countPermanents(player1, "Groundshaker Sliver")).isZero();
    }
}
