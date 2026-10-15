package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.i.Inspiration;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({GerrardsWisdom.class, GrizzlyBears.class, Inspiration.class})
class GerrardsWisdomTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 2 life for each other card in hand")
    void gains2LifePerCardInHand() {
        // Hand holds Gerrard's Wisdom plus 3 other cards. The spell leaves the
        // hand to the stack while resolving, so it counts the remaining 3.
        harness.setHand(player1, List.of(
                new GerrardsWisdom(),
                new GrizzlyBears(),
                new GrizzlyBears(),
                new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        // 3 cards * 2 life = 6, 20 + 6 = 26
        harness.assertLife(player1, 26);
    }

    @Test
    @DisplayName("Counts only cards in the controller's hand")
    void countsOnlyCardsInControllersHand() {
        harness.setHand(player1, List.of(
                new GerrardsWisdom(),
                new GrizzlyBears()));
        harness.setHand(player2, List.of(
                new GrizzlyBears(),
                new GrizzlyBears(),
                new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Gains no life with an otherwise empty hand")
    void gainsNoLifeWithEmptyHand() {
        harness.castFromHand(player1, new GerrardsWisdom(), "{2}{W}{W}");
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Counts cards in hand at resolution after drawing in response")
    void countsHandAtResolution() {
        harness.setHand(player1, List.of(
                new GerrardsWisdom(), new Inspiration(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0);
        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        harness.passBothPriorities();

        harness.assertLife(player1, 26);
        harness.assertLife(player2, 20);
    }
}
