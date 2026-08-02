package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IntuitionTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving prompts the controller to pick three library cards")
    void resolvingPromptsSearchForThree() {
        setupAndCast();
        setLibrary(5);

        harness.passBothPriorities();

        PendingInteraction.IntuitionSearchChoice search =
                gd.interaction.activeInteraction(PendingInteraction.IntuitionSearchChoice.class);
        assertThat(search).isNotNull();
        assertThat(search.playerId()).isEqualTo(player1.getId());
        assertThat(search.count()).isEqualTo(3);
        assertThat(search.pool()).hasSize(5);
    }

    @Test
    @DisplayName("Opponent's pick goes to hand, the other two to the graveyard, library shuffled")
    void opponentPickGoesToHandRestToGraveyard() {
        setupAndCast();
        List<Card> library = setLibrary(5);

        harness.passBothPriorities();

        List<UUID> revealed = List.of(library.get(0).getId(), library.get(1).getId(), library.get(2).getId());
        harness.handleMultipleCardsChosen(player1, revealed);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleMultipleCardsChosen(player2, List.of(revealed.get(1)));

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .containsExactly(revealed.get(1));
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .contains(revealed.get(0), revealed.get(2));
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Controller must reveal exactly three cards")
    void controllerMustRevealExactlyThree() {
        setupAndCast();
        List<Card> library = setLibrary(5);

        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(library.get(0).getId(), library.get(1).getId())))
                .hasMessageContaining("exactly 3");
    }

    @Test
    @DisplayName("A library of two cards is searched for both, and the opponent still chooses")
    void smallLibrarySearchesForWhatIsThere() {
        setupAndCast();
        List<Card> library = setLibrary(2);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.IntuitionSearchChoice.class).count())
                .isEqualTo(2);

        harness.handleMultipleCardsChosen(player1, List.of(library.get(0).getId(), library.get(1).getId()));
        harness.handleMultipleCardsChosen(player2, List.of(library.get(0).getId()));

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .containsExactly(library.get(0).getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .contains(library.get(1).getId());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A single-card library needs no opponent choice — that card goes to hand")
    void singleCardLibraryNeedsNoChoice() {
        setupAndCast();
        Card only = new RagingGoblin();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.add(only);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId).containsExactly(only.getId());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new Intuition()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castInstant(player1, 0, player2.getId());
    }

    private List<Card> setLibrary(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new GrizzlyBears());
        }
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(cards);
        return cards;
    }
}
