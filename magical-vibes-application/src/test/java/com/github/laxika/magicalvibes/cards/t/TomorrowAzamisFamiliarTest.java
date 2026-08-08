package com.github.laxika.magicalvibes.cards.t;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TomorrowAzamisFamiliarTest extends BaseCardTest {

    private void drawWithTomorrow() {
        harness.addToBattlefield(player1, new TomorrowAzamisFamiliar());
        harness.setHand(player1, new ArrayList<>());
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
        harness.setLibrary(player1, new ArrayList<>(List.of(
                new Plains(), new GrizzlyBears(), new Forest(), new Island())));
        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card bears = deck.get(1);

        drawWithTomorrow();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class).allCards()).hasSize(3);

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Grizzly Bears");

        // The two unchosen cards await a bottom-ordering choice.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(2);
        answerReorderInOrder();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        // Island was never looked at; Plains then Forest were put on the bottom under it.
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName).containsExactly("Island", "Plains", "Forest");
    }

    @Test
    @DisplayName("The chosen card is put into hand, not drawn — the library only loses the looked-at cards")
    void replacementIsNotADraw() {
        harness.setLibrary(player1, new ArrayList<>(List.of(
                new Plains(), new GrizzlyBears(), new Forest(), new Island())));
        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card bears = deck.get(1);

        drawWithTomorrow();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        answerReorderInOrder();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("With a single card left in the library, that card is put into hand with no choice")
    void singleCardLibraryNeedsNoChoice() {
        harness.setLibrary(player1, new ArrayList<>(List.of(new GrizzlyBears())));

        drawWithTomorrow();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("With two cards left, one is chosen and the other goes on the bottom without a reorder")
    void twoCardLibraryBottomsTheLeftover() {
        harness.setLibrary(player1, new ArrayList<>(List.of(new GrizzlyBears(), new Plains())));
        Card bears = gd.playerDecks.get(player1.getId()).getFirst();

        drawWithTomorrow();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName).containsExactly("Plains");
    }

    @Test
    @DisplayName("A replaced draw from an empty library looks at nothing and does not lose the game")
    void emptyLibraryDoesNotLose() {
        harness.setLibrary(player1, new ArrayList<>());

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
        harness.setHand(player2, new ArrayList<>());
        harness.setLibrary(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Plains(), new Forest())));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player2.getId()));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName).containsExactly("Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
    }
}
