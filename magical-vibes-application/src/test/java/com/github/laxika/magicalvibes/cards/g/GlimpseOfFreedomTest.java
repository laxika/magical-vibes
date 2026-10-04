package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.n.NyxbornCourser;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GlimpseOfFreedom.class, NyxbornCourser.class})
class GlimpseOfFreedomTest extends BaseCardTest {

    @Test
    void drawsACardWhenCastFromHand() {
        GlimpseOfFreedom glimpse = new GlimpseOfFreedom();
        NyxbornCourser drawnCard = new NyxbornCourser();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.castFromHand(player1, glimpse, "{1}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(glimpse);
    }

    @Test
    void escapeExilesFiveOtherCardsAndReturnsGlimpseToGraveyard() {
        GlimpseOfFreedom glimpse = new GlimpseOfFreedom();
        NyxbornCourser drawnCard = new NyxbornCourser();
        harness.setLibrary(player1, List.of(drawnCard));
        List<NyxbornCourser> otherCards = List.of(
                new NyxbornCourser(), new NyxbornCourser(), new NyxbornCourser(),
                new NyxbornCourser(), new NyxbornCourser());
        harness.setGraveyard(player1, List.of(glimpse, otherCards.get(0), otherCards.get(1),
                otherCards.get(2), otherCards.get(3), otherCards.get(4)));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castFromGraveyard(player1, 0, List.of(1, 2, 3, 4, 5));

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(otherCards);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(glimpse);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(otherCards);
    }

    @Test
    void escapeRequiresFiveOtherCardsInTheGraveyard() {
        GlimpseOfFreedom glimpse = new GlimpseOfFreedom();
        harness.setGraveyard(player1, List.of(glimpse, new NyxbornCourser(), new NyxbornCourser(),
                new NyxbornCourser(), new NyxbornCourser()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(1, 2, 3, 4)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canEscapeAgainAfterResolving() {
        GlimpseOfFreedom glimpse = new GlimpseOfFreedom();
        NyxbornCourser firstDraw = new NyxbornCourser();
        NyxbornCourser secondDraw = new NyxbornCourser();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.setGraveyard(player1, List.of(glimpse,
                new NyxbornCourser(), new NyxbornCourser(), new NyxbornCourser(),
                new NyxbornCourser(), new NyxbornCourser(), new NyxbornCourser(),
                new NyxbornCourser(), new NyxbornCourser(), new NyxbornCourser(),
                new NyxbornCourser()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castFromGraveyard(player1, 0, List.of(1, 2, 3, 4, 5));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(glimpse);
        int glimpseIndex = gd.playerGraveyards.get(player1.getId()).indexOf(glimpse);
        List<Integer> remainingCosts = java.util.stream.IntStream.range(0, 6)
                .filter(index -> index != glimpseIndex).boxed().toList();
        harness.castFromGraveyard(player1, glimpseIndex, remainingCosts);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(glimpse);
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(10).doesNotContain(glimpse);
    }

    @Test
    void cannotExileItselfToPayEscapeCost() {
        GlimpseOfFreedom glimpse = new GlimpseOfFreedom();
        harness.setGraveyard(player1, List.of(glimpse,
                new NyxbornCourser(), new NyxbornCourser(), new NyxbornCourser(),
                new NyxbornCourser(), new NyxbornCourser()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(0, 1, 2, 3, 4)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(6).contains(glimpse);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void cannotCountTheSameCardTwiceForEscapeCost() {
        GlimpseOfFreedom glimpse = new GlimpseOfFreedom();
        harness.setGraveyard(player1, List.of(glimpse,
                new NyxbornCourser(), new NyxbornCourser(), new NyxbornCourser(),
                new NyxbornCourser(), new NyxbornCourser()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(1, 2, 3, 4, 4)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(6).contains(glimpse);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }
}
