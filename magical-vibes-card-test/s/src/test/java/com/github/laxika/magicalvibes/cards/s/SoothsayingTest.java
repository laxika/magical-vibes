package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(Soothsaying.class)
class SoothsayingTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability shuffles the controller's library")
    void shufflesLibrary() {
        List<Card> controllerLibrary = List.of(new Soothsaying(), new Soothsaying(), new Soothsaying(), new Soothsaying());
        List<Card> opponentLibrary = List.of(new Soothsaying(), new Soothsaying());
        harness.setLibrary(player1, controllerLibrary);
        harness.setLibrary(player2, opponentLibrary);
        harness.addToBattlefield(player1, new Soothsaying());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        List<Card> opponentDeck = gd.playerDecks.get(player2.getId());
        List<Card> before = List.copyOf(deck);
        List<Card> opponentBefore = List.copyOf(opponentDeck);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(deck).containsExactlyInAnyOrderElementsOf(before);
        assertThat(opponentDeck).containsExactlyElementsOf(opponentBefore);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("shuffles their library"));
    }

    @Test
    @DisplayName("The X ability reorders the top X cards")
    void reordersTopXCards() {
        List<Card> library = List.of(new Soothsaying(), new Soothsaying(), new Soothsaying(), new Soothsaying());
        harness.setLibrary(player1, library);
        harness.addToBattlefield(player1, new Soothsaying());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card originalTop = deck.get(0);
        Card originalSecond = deck.get(1);
        Card originalThird = deck.get(2);
        Card originalFourth = deck.get(3);

        harness.activateAbility(player1, 0, 1, 3, null);
        harness.passBothPriorities();

        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder).isNotNull();
        assertThat(reorder.cards()).containsExactly(originalTop, originalSecond, originalThird);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(2, 0, 1)));

        assertThat(deck.subList(0, 3)).containsExactly(originalThird, originalTop, originalSecond);
        assertThat(deck).containsExactly(originalThird, originalTop, originalSecond, originalFourth);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The X ability reorders all cards when X exceeds the library size")
    void reordersAllCardsInShortLibrary() {
        Card first = new Soothsaying();
        Card second = new Soothsaying();
        harness.setLibrary(player1, List.of(first, second));
        harness.addToBattlefield(player1, new Soothsaying());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, 3, null);
        harness.passBothPriorities();

        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder).isNotNull();
        assertThat(reorder.cards()).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The X ability with X=0 leaves the library unchanged")
    void xZeroLeavesLibraryUnchanged() {
        List<Card> library = List.of(new Soothsaying(), new Soothsaying());
        harness.setLibrary(player1, library);
        harness.addToBattlefield(player1, new Soothsaying());

        harness.activateAbility(player1, 0, 1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(library);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A positive X with an empty library resolves without a choice")
    void positiveXWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        harness.addToBattlefield(player1, new Soothsaying());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, 2, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The activated ability retains its controller and X after its source leaves")
    void reordersControllersLibraryAfterSourceLeaves() {
        Card first = new Soothsaying();
        Card second = new Soothsaying();
        Card third = new Soothsaying();
        Card opponentCard = new Soothsaying();
        harness.setLibrary(player2, List.of(first, second, third));
        harness.setLibrary(player1, List.of(opponentCard));
        harness.addToBattlefield(player2, new Soothsaying());
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.activateAbility(player2, 0, 1, 2, null);
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder).isNotNull();
        assertThat(reorder.playerId()).isEqualTo(player2.getId());
        assertThat(reorder.cards()).containsExactly(first, second);

        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(second, first, third);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(opponentCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
