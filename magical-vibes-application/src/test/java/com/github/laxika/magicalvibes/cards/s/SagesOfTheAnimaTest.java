package com.github.laxika.magicalvibes.cards.s;

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

class SagesOfTheAnimaTest extends BaseCardTest {

    private void drawWithSages() {
        harness.addToBattlefield(player1, new SagesOfTheAnima());
        harness.setHand(player1, new ArrayList<>());
        harness.getDrawService().resolveDrawCard(gd, player1.getId());
    }

    private void answerReorderInOrder() {
        List<Card> cards = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardOrder(IntStream.range(0, cards.size()).boxed().toList()));
    }

    @Test
    @DisplayName("A replaced draw reveals the top three cards; revealed creatures go to hand")
    void revealedCreaturesGoToHand() {
        // Top three: creature, land, creature. The lone non-creature is bottomed without a choice.
        harness.setLibrary(player1, new ArrayList<>(List.of(
                new GrizzlyBears(), new Plains(), new GrizzlyBears(), new Forest())));

        drawWithSages();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Grizzly Bears", "Grizzly Bears");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        // Forest was never revealed; Plains was bottomed under it.
        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck).extracting(Card::getName).containsExactly("Forest", "Plains");
    }

    @Test
    @DisplayName("Two or more revealed non-creatures are ordered onto the bottom of the library")
    void nonCreaturesBottomedInChosenOrder() {
        harness.setLibrary(player1, new ArrayList<>(List.of(
                new Plains(), new Forest(), new GrizzlyBears())));

        drawWithSages();

        // The creature entered the hand; the two lands await a bottom-ordering choice.
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(2);

        answerReorderInOrder();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName).containsExactly("Plains", "Forest");
    }

    @Test
    @DisplayName("The replacement is mandatory even when no creatures are revealed")
    void mandatoryWithNoCreatures() {
        harness.setLibrary(player1, new ArrayList<>(List.of(
                new Plains(), new Forest(), new Island())));

        drawWithSages();

        // No card entered the hand; all three non-creatures are bottomed.
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(3);

        answerReorderInOrder();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("With fewer than three cards, only the available cards are revealed")
    void fewerThanThreeCards() {
        harness.setLibrary(player1, new ArrayList<>(List.of(new GrizzlyBears(), new Plains())));

        drawWithSages();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Grizzly Bears");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName).containsExactly("Plains");
    }

    @Test
    @DisplayName("A replaced draw from an empty library reveals nothing and does not lose the game")
    void emptyLibraryDoesNotLose() {
        harness.setLibrary(player1, new ArrayList<>());

        drawWithSages();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        assertThat(gameLogContains("reveals no cards")).isTrue();
    }
}
