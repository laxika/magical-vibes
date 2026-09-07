package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.i.Impulse;
import com.github.laxika.magicalvibes.cards.j.JamuraanLion;
import com.github.laxika.magicalvibes.cards.j.JujuBubble;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VampiricTutor.class, Impulse.class, JujuBubble.class, JamuraanLion.class})
class VampiricTutorTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving offers any card from the library")
    void offersAnyCard() {
        setupLibrary();
        cast();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).hasSize(3);
        assertThat(search.params().cards().stream().map(Card::getId).toList())
                .containsExactlyElementsOf(gd.playerDecks.get(player1.getId()).stream().map(Card::getId).toList());
        assertThat(search.params().reveals()).isFalse();
        assertThat(search.params().canFailToFind()).isFalse();
    }

    @Test
    @DisplayName("Choosing a card puts it on top of the library and loses 2 life")
    void choosingCardPutsOnTopAndLosesLife() {
        harness.setLife(player1, 20);
        setupLibrary();
        cast();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        UUID chosenId = offered.get(1).getId();

        harness.handleCardChosen(player1, 1);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck.getFirst().getId()).isEqualTo(chosenId);
        harness.assertLife(player1, 18);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Life loss waits until the mandatory search completes")
    void lifeLossWaitsUntilSearchCompletes() {
        harness.setLife(player1, 20);
        setupLibrary();
        cast();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.handleCardChosen(player1, 0);
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Choosing a card does not reveal it")
    void choosingCardDoesNotRevealIt() {
        setupLibrary();
        cast();
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .noneMatch(entry -> entry.contains("reveals"));
    }

    @Test
    @DisplayName("An empty library still causes the life loss")
    void emptyLibraryStillLosesLife() {
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of());
        cast();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Search is mandatory: cannot fail to find")
    void cannotFailToFind() {
        setupLibrary();
        cast();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast() {
        harness.castFromHand(player1, new VampiricTutor(), "{B}");
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Impulse(), new JujuBubble(), new JamuraanLion()));
    }
}
