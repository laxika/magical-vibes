package com.github.laxika.magicalvibes.cards.t;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.laxika.magicalvibes.cards.b.BileUrchin;
import com.github.laxika.magicalvibes.cards.f.Frostling;
import com.github.laxika.magicalvibes.cards.f.FrostOgre;
import com.github.laxika.magicalvibes.cards.g.GnarledMass;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({TomorrowAzamisFamiliar.class, BileUrchin.class, Frostling.class, FrostOgre.class, GnarledMass.class})
class TomorrowAzamisFamiliarTest extends BaseCardTest {

    private void addTomorrow() {
        harness.addToBattlefield(player1, new TomorrowAzamisFamiliar());
        harness.setHand(player1, List.of());
    }

    private void drawWithTomorrow() {
        addTomorrow();
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
    }

    private void answerReorderInOrder() {
        List<Card> cards = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardOrder(IntStream.range(0, cards.size()).boxed().toList()));
    }

    @Test
    @DisplayName("A replaced draw looks at the top three cards; the chosen one goes to hand")
    void chosenCardGoesToHand() {
        Card first = new GnarledMass();
        Card chosen = new BileUrchin();
        Card third = new Frostling();
        Card untouched = new FrostOgre();
        harness.setLibrary(player1, List.of(first, chosen, third, untouched));

        drawWithTomorrow();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class).allCards()).hasSize(3);

        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosen);

        // The two unchosen cards await a bottom-ordering choice.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(2);
        answerReorderInOrder();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        // The untouched card was never looked at; the other two were put on the bottom under it.
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched, first, third);
    }

    @Test
    @DisplayName("The chosen card is put into hand, not drawn — the library only loses the looked-at cards")
    void replacementIsNotADraw() {
        harness.setLibrary(player1, List.of(
                new GnarledMass(), new BileUrchin(), new Frostling(), new FrostOgre()));

        drawWithTomorrow();
        Card chosen = gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class).allCards().get(1);
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));
        answerReorderInOrder();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.cardsDrawnThisTurn).doesNotContainKey(player1.getId());
    }

    @Test
    @DisplayName("With a single card left in the library, that card is put into hand with no choice")
    void singleCardLibraryNeedsNoChoice() {
        Card card = new GnarledMass();
        harness.setLibrary(player1, List.of(card));

        drawWithTomorrow();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("With two cards left, one is chosen and the other goes on the bottom without a reorder")
    void twoCardLibraryBottomsTheLeftover() {
        Card chosen = new BileUrchin();
        Card leftover = new Frostling();
        harness.setLibrary(player1, List.of(chosen, leftover));

        drawWithTomorrow();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosen);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(leftover);
    }

    @Test
    @DisplayName("The replacement requires choosing exactly one card")
    void replacementRequiresChoosingOneCard() {
        harness.setLibrary(player1, List.of(new GnarledMass(), new BileUrchin(), new Frostling()));

        drawWithTomorrow();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Invalid number of cards selected");
    }

    @Test
    @DisplayName("Each card in a multiple-card draw gets its own replacement")
    void replacesEachCardInMultipleCardDraw() {
        Card first = new GnarledMass();
        Card second = new BileUrchin();
        Card third = new Frostling();
        Card fourth = new FrostOgre();
        Card fifth = new GnarledMass();
        Card sixth = new BileUrchin();
        harness.setLibrary(player1, List.of(first, second, third, fourth, fifth, sixth));
        addTomorrow();

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCards(gd, player1.getId(), 2));

        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));
        answerReorderInOrder();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(fourth.getId()));
        answerReorderInOrder();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, fourth);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, third, fifth, sixth);
        assertThat(gd.cardsDrawnThisTurn).doesNotContainKey(player1.getId());
    }

    @Test
    @DisplayName("A replaced draw from an empty library looks at nothing and does not lose the game")
    void emptyLibraryDoesNotLose() {
        harness.setLibrary(player1, List.of());

        drawWithTomorrow();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        assertThat(gameLogContains("looks at no cards")).isTrue();
    }

    @Test
    @DisplayName("An opponent's draw is unaffected — the replacement only applies to the controller")
    void opponentDrawsNormally() {
        harness.addToBattlefield(player1, new TomorrowAzamisFamiliar());
        harness.setHand(player2, List.of());
        Card top = new BileUrchin();
        harness.setLibrary(player2, List.of(top, new Frostling(), new GnarledMass()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player2.getId()));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(top);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
    }
}
