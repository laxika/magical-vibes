package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.DuskImp;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Entomb.class, DuskImp.class, Plains.class, Swamp.class})
class EntombTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving offers any library card for the graveyard")
    void offersAnyCardForGraveyard() {
        castEntomb();
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        var search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).extracting(Card::getName)
                .containsExactly("Dusk Imp", "Plains", "Swamp");
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.GRAVEYARD);
        assertThat(search.params().reveals()).isFalse();
        assertThat(search.params().canFailToFind()).isFalse();
    }

    @Test
    @DisplayName("Choosing a card puts it into the graveyard and shuffles the library")
    void choosingCardPutsItIntoGraveyard() {
        castEntomb();
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        int librarySizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .contains("Dusk Imp", "Entomb");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(librarySizeBefore - 1);
        assertThat(gd.playerDecks.get(player1.getId()))
                .noneMatch(card -> card.getName().equals("Dusk Imp"));
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    @DisplayName("An empty library completes the search without prompting")
    void emptyLibraryCompletesSearch() {
        castEntomb();
        harness.setLibrary(player1, List.of());

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Entomb");
        assertThat(gameLogContains("it is empty")).isTrue();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    @DisplayName("An unrestricted search cannot fail to find a card")
    void cannotFailToFind() {
        castEntomb();
        setupLibrary();

        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot fail to find");
    }

    private void castEntomb() {
        harness.castFromHand(player1, new Entomb(), "{B}");
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new DuskImp(), new Plains(), new Swamp()));
    }
}
