package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UnionOfTheThirdPath.class, Forest.class, Mountain.class, Plains.class, GrizzlyBears.class})
class UnionOfTheThirdPathTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card, then gains life equal to the resulting hand size")
    void drawsThenGainsLifeBasedOnResultingHandSize() {
        harness.setHand(player1, List.of(
                new UnionOfTheThirdPath(), new Forest(), new Mountain(), new Plains()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.setLife(player1, 10);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(14);
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Casting the last card in hand gains one life after drawing")
    void castingLastCardInHandGainsOneLife() {
        harness.setHand(player1, List.of(new UnionOfTheThirdPath()));
        harness.setLibrary(player1, List.of(new Plains()));
        harness.setHand(player2, List.of(new Forest(), new Mountain(), new Plains()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.setLife(player1, 10);
        harness.setLife(player2, 12);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Plains");
        harness.assertLife(player1, 11);
        harness.assertLife(player2, 12);
        harness.assertInGraveyard(player1, "Union of the Third Path");
    }

    @Test
    @DisplayName("Each spell counts the current hand after its draw when another Union resolves first")
    void countsCurrentHandWhenAnotherUnionResolvesFirst() {
        harness.setHand(player1, List.of(
                new UnionOfTheThirdPath(), new UnionOfTheThirdPath(), new Forest()));
        harness.setLibrary(player1, List.of(new Plains(), new Mountain()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.setLife(player1, 10);

        harness.castInstant(player1, 0);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertLife(player1, 12);
        harness.assertInHand(player1, "Plains");

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.assertLife(player1, 15);
        harness.assertInHand(player1, "Mountain");
    }
}
