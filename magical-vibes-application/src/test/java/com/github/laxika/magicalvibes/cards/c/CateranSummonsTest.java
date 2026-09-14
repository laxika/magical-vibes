package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CateranSummons.class, CateranPersuader.class, Counterspell.class})
class CateranSummonsTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving offers only Mercenary cards and reveals the chosen card")
    void offersOnlyMercenariesAndRevealsChosenCard() {
        harness.setLibrary(player1, List.of(new CateranPersuader(), new Counterspell()));
        cast();

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(Card::getName).containsExactly("Cateran Persuader");
        assertThat(search.params().reveals()).isTrue();
        assertThat(search.params().canFailToFind()).isTrue();
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.HAND);

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Cateran Persuader");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.gameLog).anyMatch(entry -> entry.plainText().contains("reveals"));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Resolving with no Mercenary cards completes without opening a choice")
    void noMercenaryCardsCompleteWithoutOpeningChoice() {
        harness.setLibrary(player1, List.of(new Counterspell()));
        cast();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.gameLog).anyMatch(entry -> entry.plainText().contains("finds no")
                && entry.plainText().contains("Library is shuffled"));
    }

    @Test
    @DisplayName("Allows failing to find when a Mercenary card is available")
    void allowsFailingToFindWithMatchingCard() {
        harness.setLibrary(player1, List.of(new CateranPersuader(), new Counterspell()));
        cast();

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotInHand(player1, "Cateran Persuader");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.gameLog).anyMatch(entry -> entry.plainText().contains("chooses not to take a card")
                && entry.plainText().contains("Library is shuffled"));
    }

    private void cast() {
        harness.castFromHand(player1, new CateranSummons(), "{B}");
    }
}
