package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CallTheGatewatch.class, ChandraNalaar.class, GrizzlyBears.class})
class CallTheGatewatchTest extends BaseCardTest {

    @Test
    @DisplayName("Searches for a planeswalker card and puts the chosen card into hand")
    void searchesForPlaneswalkerAndPutsItIntoHand() {
        Card planeswalker = new ChandraNalaar();
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(planeswalker, creature));
        castCallTheGatewatch();

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .hasSize(1)
                .allMatch(card -> card.hasType(CardType.PLANESWALKER));

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Chandra Nalaar");
        harness.assertNotInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not offer a non-planeswalker card")
    void doesNotOfferNonPlaneswalkerCards() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        castCallTheGatewatch();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.hasType(CardType.CREATURE));
    }

    @Test
    @DisplayName("May fail to find even when a planeswalker is available")
    void mayFailToFindPlaneswalker() {
        Card planeswalker = new ChandraNalaar();
        harness.setLibrary(player1, List.of(planeswalker));
        castCallTheGatewatch();
        harness.passBothPriorities();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(planeswalker);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        harness.assertInGraveyard(player1, "Call the Gatewatch");
        assertThat(gd.gameLog).anyMatch(entry -> entry.plainText().contains("Library is shuffled"));
    }

    @Test
    @DisplayName("Resolves with an empty library")
    void resolvesWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        castCallTheGatewatch();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        harness.assertInGraveyard(player1, "Call the Gatewatch");
        assertThat(gd.gameLog).anyMatch(entry -> entry.plainText().contains("Library is shuffled"));
    }

    @Test
    @DisplayName("Reveals and moves only the selected planeswalker from its controller's library")
    void selectsOnlyOnePlaneswalkerFromOwnLibrary() {
        Card first = new ChandraNalaar();
        Card second = new ChandraNalaar();
        Card opponentCard = new ChandraNalaar();
        harness.setLibrary(player1, List.of(first, second));
        harness.setLibrary(player2, List.of(opponentCard));
        castCallTheGatewatch();
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        harness.assertNotOnBattlefield(player1, "Chandra Nalaar");
        harness.assertInGraveyard(player1, "Call the Gatewatch");
        assertThat(gd.gameLog).anyMatch(entry -> entry.plainText().contains("reveals Chandra Nalaar")
                && entry.plainText().contains("Library is shuffled"));
    }

    private void castCallTheGatewatch() {
        harness.setHand(player1, List.of(new CallTheGatewatch()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, 0);
    }
}
