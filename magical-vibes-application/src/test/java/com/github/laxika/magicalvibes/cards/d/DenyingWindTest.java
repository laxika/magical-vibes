package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(DenyingWind.class)
class DenyingWindTest extends BaseCardTest {

    private void castDenyingWind(Player targetPlayer, List<Card> targetLibrary) {
        harness.setLibrary(targetPlayer, targetLibrary);
        harness.setHand(player1, List.of(new DenyingWind()));
        harness.addMana(player1, ManaColor.BLUE, 9);
        harness.castSorcery(player1, 0, targetPlayer.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Exiles up to seven cards from the target player's library")
    void exilesUpToSevenCards() {
        castDenyingWind(player2, List.of(
                new DenyingWind(), new DenyingWind(), new DenyingWind(), new DenyingWind()));

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("Exiles no more than seven cards")
    void exilesNoMoreThanSevenCards() {
        castDenyingWind(player2, List.of(
                new DenyingWind(), new DenyingWind(), new DenyingWind(), new DenyingWind(),
                new DenyingWind(), new DenyingWind(), new DenyingWind(), new DenyingWind()));

        for (int i = 0; i < 7; i++) {
            harness.handleCardChosen(player1, 0);
        }

        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(7);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    @DisplayName("Exiles every card when the target library has fewer than seven")
    void exilesAllCardsFromShortLibrary() {
        castDenyingWind(player2, List.of(new DenyingWind(), new DenyingWind()));

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("Can choose no cards")
    void canChooseNoCards() {
        castDenyingWind(player2, List.of(new DenyingWind(), new DenyingWind(), new DenyingWind()));

        harness.handleCardChosen(player1, -1);

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    @DisplayName("Does nothing when the target library is empty")
    void emptyTargetLibrary() {
        castDenyingWind(player2, List.of());

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gameLogContains("but it is empty. Library is shuffled.")).isTrue();
    }

    @Test
    @DisplayName("Can target yourself")
    void canTargetSelf() {
        castDenyingWind(player1, List.of(new DenyingWind(), new DenyingWind(), new DenyingWind()));

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }
}
