package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(PonderingMage.class)
class PonderingMageTest extends BaseCardTest {

    @Test
    void enteringLetsItsControllerReorderTheTopThreeCards() {
        harness.setHand(player1, List.of(new PonderingMage()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card top0 = deck.get(0);
        Card top1 = deck.get(1);
        Card top2 = deck.get(2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder).isNotNull();
        assertThat(reorder.cards()).containsExactly(top0, top1, top2);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(2, 1, 0)));

        assertThat(deck).containsSubsequence(top2, top1, top0);
    }

    @Test
    void decliningShuffleDrawsTheReorderedTopCard() {
        harness.setHand(player1, List.of(new PonderingMage()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card top2 = deck.get(2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(2, 0, 1)));
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).contains(top2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void acceptingShuffleStillDrawsOneCard() {
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.setHand(player1, List.of(new PonderingMage()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1, 2)));
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void twoCardLibraryCanBeReorderedBeforeDrawing() {
        Card first = new PonderingMage();
        Card second = new PonderingMage();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new PonderingMage()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder).isNotNull();
        assertThat(reorder.cards()).containsExactly(first, second);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void singleCardLibraryCanBeShuffledBeforeDrawingItsOnlyCard() {
        Card onlyCard = new PonderingMage();
        harness.setLibrary(player1, List.of(onlyCard));
        harness.setHand(player1, List.of(new PonderingMage()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0)));
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(onlyCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void opponentsMageReordersAndDrawsFromItsControllersLibrary() {
        List<Card> ourLibrary = List.copyOf(gd.playerDecks.get(player1.getId()));
        List<Card> ourHand = List.copyOf(gd.playerHands.get(player1.getId()));
        Card first = new PonderingMage();
        Card second = new PonderingMage();
        harness.setLibrary(player2, List.of(first, second));
        harness.setHand(player2, List.of(new PonderingMage()));
        harness.forceActivePlayer(player2);
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.castCreature(player2, 0);
        resolveAllTriggers();
        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.CardOrder(List.of(1, 0)));
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(second);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(first);
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(ourHand);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(ourLibrary);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
