package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Architects of Will")
@CardUsed({ArchitectsOfWill.class})
class ArchitectsOfWillTest extends BaseCardTest {

    @Test
    @DisplayName("Entering starts a reorder of the top 3 cards of the target's library")
    void etbEntersReorderOfTargetsLibrary() {
        harness.setHand(player1, List.of(new ArchitectsOfWill()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);

        List<Card> targetDeck = gd.playerDecks.get(player2.getId());
        Card top0 = targetDeck.get(0);
        Card top1 = targetDeck.get(1);
        Card top2 = targetDeck.get(2);

        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities(); // resolve creature spell → ETB trigger on stack
        harness.passBothPriorities(); // resolve ETB trigger → library reorder

        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder).isNotNull();
        assertThat(reorder.cards()).containsExactly(top0, top1, top2);
        assertThat(reorder.deckOwnerId()).isEqualTo(player2.getId());
        assertThat(reorder.playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Reordering places the chosen card on top of the target's library")
    void reorderingChangesTargetsTopCard() {
        harness.setHand(player1, List.of(new ArchitectsOfWill()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);

        List<Card> targetDeck = gd.playerDecks.get(player2.getId());
        Card originallyThird = targetDeck.get(2);

        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        // Controller decides: put the original third card on top.
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(2, 0, 1)));

        assertThat(gd.playerDecks.get(player2.getId()).get(0)).isSameAs(originallyThird);
    }

    @Test
    @DisplayName("The controller may target their own library")
    void canTargetOwnLibrary() {
        harness.setHand(player1, List.of(new ArchitectsOfWill()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder).isNotNull();
        assertThat(reorder.deckOwnerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Cycling discards the card and draws one")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new ArchitectsOfWill()));
        Card drawnCard = new ArchitectsOfWill();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Architects of Will");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("Cycling accepts blue mana and discards before drawing")
    void cyclingWithBlueManaPaysDiscardImmediately() {
        Card cycledCard = new ArchitectsOfWill();
        Card drawnCard = new ArchitectsOfWill();
        harness.setHand(player1, List.of(cycledCard));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(cycledCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawnCard);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertNotOnBattlefield(player1, "Architects of Will");
    }

    @Test
    @DisplayName("A two-card library is reordered in full")
    void reordersAllCardsInShortLibrary() {
        Card first = new ArchitectsOfWill();
        Card second = new ArchitectsOfWill();
        harness.setLibrary(player2, List.of(first, second));
        harness.setHand(player1, List.of(new ArchitectsOfWill()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder).isNotNull();
        assertThat(reorder.cards()).containsExactly(first, second);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(second, first);
    }

    @Test
    @DisplayName("An empty target library resolves without a reorder or a draw")
    void emptyLibraryDoesNothing() {
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new ArchitectsOfWill()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        List<Card> originalHand = List.copyOf(gd.playerHands.get(player2.getId()));

        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).containsExactlyElementsOf(originalHand);
        harness.assertOnBattlefield(player1, "Architects of Will");
    }

    @Test
    @DisplayName("The controller can look at the sole card of the target's library")
    void showsSoleLibraryCardToController() {
        Card onlyCard = new ArchitectsOfWill();
        harness.setLibrary(player2, List.of(onlyCard));
        harness.setHand(player1, List.of(new ArchitectsOfWill()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder).isNotNull();
        assertThat(reorder.playerId()).isEqualTo(player1.getId());
        assertThat(reorder.cards()).containsExactly(onlyCard);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0)));

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(onlyCard);
    }
}
