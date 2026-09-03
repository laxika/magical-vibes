package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SealedFate.class, Island.class, Forest.class, Mountain.class})
class SealedFateTest extends BaseCardTest {

    private void giveMana(int generic) {
        harness.addMana(player1, ManaColor.BLUE, 1 + generic);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }

    @Test
    @DisplayName("Exiles the chosen card and puts the rest back on top in the chosen order")
    void exilesChosenCardRestOnTop() {
        harness.setHand(player1, List.of(new SealedFate()));
        giveMana(3);

        Card c0 = new Island();
        Card c1 = new Forest();
        Card c2 = new Mountain();
        Card c3 = new SealedFate();
        harness.setLibrary(player2, List.of(c0, c1, c2, c3));

        harness.castSorcery(player1, 0, 3, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);

        // Exile the second of the three looked-at cards.
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(1));

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getId().equals(c1.getId()));

        // The two remaining looked-at cards are reordered onto the top of the library.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        List<Card> deckAfter = gd.playerDecks.get(player2.getId());
        assertThat(deckAfter).hasSize(3);
        assertThat(deckAfter.get(0).getId()).isEqualTo(c2.getId());
        assertThat(deckAfter.get(1).getId()).isEqualTo(c0.getId());
        assertThat(deckAfter.get(2).getId()).isEqualTo(c3.getId());
    }

    @Test
    @DisplayName("The exile is mandatory — the pick cannot be declined")
    void exileIsMandatory() {
        harness.setHand(player1, List.of(new SealedFate()));
        giveMana(2);

        harness.setLibrary(player2, List.of(new Island(), new Forest()));

        harness.castSorcery(player1, 0, 2, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().canFailToFind()).isFalse();
    }

    @Test
    @DisplayName("Looks at only as many cards as the library holds")
    void looksAtOnlyAvailableCards() {
        harness.setHand(player1, List.of(new SealedFate()));
        giveMana(4);

        Card only = new Island();
        harness.setLibrary(player2, List.of(only));

        harness.castSorcery(player1, 0, 4, player2.getId());
        harness.passBothPriorities();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getId().equals(only.getId()));
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("X=0 looks at nothing and exiles nothing")
    void xZeroDoesNothing() {
        harness.setHand(player1, List.of(new SealedFate()));
        giveMana(0);

        harness.castAndResolveSorcery(player1, 0, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Sealed Fate");
    }

    @Test
    @DisplayName("Resolving against an empty library does nothing")
    void emptyLibraryDoesNothing() {
        harness.setHand(player1, List.of(new SealedFate()));
        giveMana(2);

        harness.setLibrary(player2, List.of());

        harness.castAndResolveSorcery(player1, 0, 2, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot target self — must target an opponent")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new SealedFate()));
        giveMana(2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
