package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
                .containsExactly("Grizzly Bears", "Plains", "Swamp");
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
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .contains("Grizzly Bears", "Entomb");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(librarySizeBefore - 1);
        assertThat(gd.playerDecks.get(player1.getId()))
                .noneMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An unrestricted search cannot fail to find a card")
    void cannotFailToFind() {
        castEntomb();
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThatThrownBy(() -> harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(-1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot fail to find");
    }

    private void castEntomb() {
        harness.setHand(player1, List.of(new Entomb()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0);
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Plains(), new Swamp()));
    }
}
