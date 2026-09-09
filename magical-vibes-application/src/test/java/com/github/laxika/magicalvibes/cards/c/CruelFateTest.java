package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CruelFate.class, Forest.class, GrizzlyBears.class, Island.class, Mountain.class, Plains.class})
class CruelFateTest extends BaseCardTest {

    @Test
    @DisplayName("Puts the chosen card into the opponent's graveyard and the rest back on top")
    void putsChosenCardIntoGraveyardRestOnTop() {
        harness.setHand(player1, List.of(new CruelFate()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        Card c0 = new Island();
        Card c1 = new Forest();
        Card c2 = new GrizzlyBears();
        Card c3 = new Mountain();
        Card c4 = new Plains();
        harness.setLibrary(player2, List.of(c0, c1, c2, c3, c4));

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);

        // Put the top card (Island) into the graveyard.
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getId().equals(c0.getId()));

        // The remaining four cards must be reordered onto the top of the library.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1, 2, 3)));

        List<Card> deckAfter = gd.playerDecks.get(player2.getId());
        assertThat(deckAfter.get(0).getId()).isEqualTo(c1.getId());
        assertThat(deckAfter.get(1).getId()).isEqualTo(c2.getId());
        assertThat(deckAfter.get(2).getId()).isEqualTo(c3.getId());
        assertThat(deckAfter.get(3).getId()).isEqualTo(c4.getId());
    }

    @Test
    @DisplayName("Allows the remaining cards to be reordered arbitrarily")
    void reordersRemainingCardsInAnyOrder() {
        harness.setHand(player1, List.of(new CruelFate()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        Card c0 = new Island();
        Card c1 = new Forest();
        Card c2 = new GrizzlyBears();
        Card c3 = new Mountain();
        Card c4 = new Plains();
        harness.setLibrary(player2, List.of(c0, c1, c2, c3, c4));

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 2);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(3, 2, 1, 0)));

        assertThat(gd.playerDecks.get(player2.getId()))
                .extracting(Card::getId)
                .containsExactly(c4.getId(), c3.getId(), c1.getId(), c0.getId());
    }

    @Test
    @DisplayName("Looks at only as many cards as the library holds")
    void looksAtOnlyAvailableCards() {
        harness.setHand(player1, List.of(new CruelFate()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        Card top = new Island();
        Card bottom = new Forest();
        harness.setLibrary(player2, List.of(top, bottom));

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);

        // Put the second card into the graveyard, leaving one to go back on top.
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getId().equals(bottom.getId()));

        List<Card> deckAfter = gd.playerDecks.get(player2.getId());
        assertThat(deckAfter).hasSize(1);
        assertThat(deckAfter.get(0).getId()).isEqualTo(top.getId());
    }

    @Test
    @DisplayName("Resolving against an empty library does nothing")
    void emptyLibraryDoesNothing() {
        harness.setHand(player1, List.of(new CruelFate()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.setLibrary(player2, List.of());

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot target self — must target an opponent")
    void cannotTargetSelf() {
        harness.setHand(player1, new ArrayList<>(List.of(new CruelFate(), new GrizzlyBears())));
        harness.addMana(player1, ManaColor.BLUE, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
