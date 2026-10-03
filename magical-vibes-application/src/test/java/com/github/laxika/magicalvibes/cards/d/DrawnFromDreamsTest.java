package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DrawnFromDreams.class, GreenwoodSentinel.class, Shock.class})
class DrawnFromDreamsTest extends BaseCardTest {

    @Test
    @DisplayName("Keeps two of the top seven and puts the rest on the bottom randomly")
    void keepsTwoOfTopSevenAndBottomsTheRestRandomly() {
        Card[] top = {
                new GreenwoodSentinel(), new Shock(), new GreenwoodSentinel(), new Shock(),
                new GreenwoodSentinel(), new Shock(), new GreenwoodSentinel(), new Shock()
        };
        harness.setLibrary(player1, List.of(top));
        List<Card> deck = gd.playerDecks.get(player1.getId());

        harness.castFromHand(player1, new DrawnFromDreams(), "{2}{U}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(top[0].getId(), top[1].getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(top[0], top[1]);
        assertThat(deck).hasSize(6);
        assertThat(deck.getFirst()).isSameAs(top[7]);
        assertThat(deck.subList(1, 6)).containsExactlyInAnyOrder(
                top[2], top[3], top[4], top[5], top[6]);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Puts all available cards into hand when the library has fewer than two")
    void putsAllAvailableCardsIntoHandWhenFewerThanTwo() {
        Card only = new GreenwoodSentinel();
        harness.setLibrary(player1, List.of(only));
        List<Card> deck = gd.playerDecks.get(player1.getId());

        harness.castFromHand(player1, new DrawnFromDreams(), "{2}{U}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(only);
        assertThat(deck).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An empty library does not cause a draw failure or leave a choice pending")
    void emptyLibraryResolvesWithoutLosing() {
        harness.setLibrary(player1, List.of());

        harness.castFromHand(player1, new DrawnFromDreams(), "{2}{U}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Drawn from Dreams");
    }

    @Test
    @DisplayName("A two-card library puts both cards into hand without a choice or draw failure")
    void twoCardLibraryPutsBothIntoHand() {
        Card first = new GreenwoodSentinel();
        Card second = new Shock();
        harness.setLibrary(player1, List.of(first, second));

        harness.castFromHand(player1, new DrawnFromDreams(), "{2}{U}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A short library still requires exactly two distinct cards and rejects invalid choices")
    void shortLibraryRequiresExactlyTwoDistinctCards() {
        Card first = new GreenwoodSentinel();
        Card second = new Shock();
        Card third = new GreenwoodSentinel();
        harness.setLibrary(player1, List.of(first, second, third));

        harness.castFromHand(player1, new DrawnFromDreams(), "{2}{U}{U}");
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(first.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(first.getId(), first.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player2,
                List.of(first.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();

        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), third.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(first, third);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Drawn from Dreams");
    }
}
