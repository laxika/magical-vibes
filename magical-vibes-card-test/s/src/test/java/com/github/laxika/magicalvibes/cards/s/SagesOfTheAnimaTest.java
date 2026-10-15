package com.github.laxika.magicalvibes.cards.s;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.u.UbaMask;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({SagesOfTheAnima.class, GrizzlyBears.class, Plains.class, Forest.class, Island.class,
        Snakeform.class, UbaMask.class})
class SagesOfTheAnimaTest extends BaseCardTest {

    private void drawWithSages() {
        harness.addToBattlefield(player1, new SagesOfTheAnima());
        harness.setHand(player1, new ArrayList<>());
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
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

    @Test
    @CardUsed({SagesOfTheAnima.class, Snakeform.class, GrizzlyBears.class})
    void losingAbilitiesStopsReplacingDrawsImmediately() {
        var sages = harness.addToBattlefieldAndReturn(player1, new SagesOfTheAnima());
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card third = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(new Snakeform()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveInstant(player1, 0, sages.getId());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, third);
    }

    @Test
    @CardUsed({SagesOfTheAnima.class, UbaMask.class, GrizzlyBears.class})
    void drawingPlayerChoosesBetweenCompetingMandatoryReplacements() {
        harness.addToBattlefield(player1, new SagesOfTheAnima());
        harness.addToBattlefield(player2, new UbaMask());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.interaction.isAwaitingInput() || !gd.pendingMayAbilities.isEmpty()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
    }

    @Test
    @CardUsed({SagesOfTheAnima.class, GrizzlyBears.class})
    void opponentsDrawIsNotReplaced() {
        harness.addToBattlefield(player1, new SagesOfTheAnima());
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(first, second));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player2.getId()));

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(first);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(second);
    }

    @Test
    @CardUsed({SagesOfTheAnima.class, GrizzlyBears.class})
    void multipleCopiesReplaceTheDrawOnlyOnce() {
        harness.addToBattlefield(player1, new SagesOfTheAnima());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears()));

        drawWithSages();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({SagesOfTheAnima.class, GrizzlyBears.class, Plains.class, Forest.class, Island.class})
    void consecutiveDrawsResumeAfterBottomOrdering() {
        harness.addToBattlefield(player1, new SagesOfTheAnima());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Plains(), new Forest(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new Island()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCards(gd, player1.getId(), 2));
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Island", "Forest", "Plains");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
