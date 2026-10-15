package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BonescytheSliver;
import com.github.laxika.magicalvibes.cards.d.Disperse;
import com.github.laxika.magicalvibes.cards.k.KalonianTusker;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GaleriderSliver.class, BonescytheSliver.class, KalonianTusker.class, Disperse.class})
class GaleriderSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Galerider Sliver grants itself flying (it is a Sliver)")
    void grantsSelfFlying() {
        Permanent sliver = addCreatureReady(player1, new GaleriderSliver());

        assertThat(gqs.hasKeyword(gd, sliver, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Grants flying to another Sliver you control")
    void grantsFlyingToOtherSliver() {
        addCreatureReady(player1, new GaleriderSliver());
        Permanent otherSliver = addCreatureReady(player1, new BonescytheSliver());

        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Does not grant flying to a non-Sliver creature")
    void doesNotGrantToNonSliver() {
        addCreatureReady(player1, new GaleriderSliver());
        Permanent bears = addCreatureReady(player1, new KalonianTusker());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Does not grant flying to an opponent's Sliver")
    void doesNotGrantToOpponentSliver() {
        addCreatureReady(player1, new GaleriderSliver());
        Permanent opponentSliver = addCreatureReady(player2, new BonescytheSliver());

        assertThat(gqs.hasKeyword(gd, opponentSliver, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("A Sliver entering later immediately gains flying")
    void grantsFlyingToNewlyEnteringSliver() {
        addCreatureReady(player1, new GaleriderSliver());

        harness.castFromHand(player1, new BonescytheSliver(), "{3}{W}");
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Bonescythe Sliver"), Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Flying ends when the only Galerider Sliver leaves the battlefield")
    void flyingEndsWhenSourceLeaves() {
        Permanent source = addCreatureReady(player1, new GaleriderSliver());
        Permanent otherSliver = addCreatureReady(player1, new BonescytheSliver());
        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.FLYING)).isTrue();

        harness.setHand(player1, java.util.List.of(new Disperse()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, source.getId());

        harness.assertInHand(player1, "Galerider Sliver");
        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Another Galerider Sliver keeps granting flying when one leaves")
    void secondSourceKeepsFlyingActive() {
        Permanent source = addCreatureReady(player1, new GaleriderSliver());
        Permanent remainingSource = addCreatureReady(player1, new GaleriderSliver());
        Permanent otherSliver = addCreatureReady(player1, new BonescytheSliver());

        harness.setHand(player1, java.util.List.of(new Disperse()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, source.getId());

        harness.assertInHand(player1, "Galerider Sliver");
        assertThat(gqs.hasKeyword(gd, remainingSource, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.FLYING)).isTrue();
    }
}
