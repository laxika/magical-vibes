package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.CabalRitual;
import com.github.laxika.magicalvibes.cards.k.KamahlsSledge;
import com.github.laxika.magicalvibes.cards.p.PsychogenicProbe;
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

@CardUsed({InsidiousDreams.class, CabalRitual.class, KamahlsSledge.class})
class InsidiousDreamsTest extends BaseCardTest {

    @Test
    @DisplayName("Discards X cards and puts X searched cards on top in the chosen order")
    void discardsAndSearchesForXCards() {
        Card topCandidate = new CabalRitual();
        Card secondCandidate = new KamahlsSledge();
        Card remainingCandidate = new CabalRitual();
        harness.setLibrary(player1, List.of(topCandidate, secondCandidate, remainingCandidate));
        harness.setHand(player1, List.of(new InsidiousDreams(), new CabalRitual(),
                new KamahlsSledge(), new CabalRitual()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castInstantForXWithDiscards(player1, 0, 2, List.of(), List.of(1, 2));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.SearchLibraryToTopChoice.class))
                .isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(topCandidate.getId(), secondCandidate.getId()));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(secondCandidate, topCandidate, remainingCandidate);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .contains("Insidious Dreams", "Cabal Ritual", "Kamahl's Sledge");
    }

    @Test
    @DisplayName("The search requires exactly X cards when the library contains enough cards")
    void searchRequiresExactlyXCards() {
        Card firstCandidate = new CabalRitual();
        Card secondCandidate = new KamahlsSledge();
        harness.setLibrary(player1, List.of(firstCandidate, secondCandidate, new CabalRitual()));
        harness.setHand(player1, List.of(new InsidiousDreams(), new CabalRitual(), new KamahlsSledge()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castInstantForXWithDiscards(player1, 0, 2, List.of(), List.of(1, 2));
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(firstCandidate.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must choose 2 cards");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.SearchLibraryToTopChoice.class))
                .isNotNull();

        harness.handleMultipleCardsChosen(player1, List.of(firstCandidate.getId(), secondCandidate.getId()));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1)));

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("X=0 discards no cards and does not prompt for a search")
    void xZeroDoesNotDiscardOrPrompt() {
        Card libraryCard = new CabalRitual();
        Card otherLibraryCard = new KamahlsSledge();
        harness.setLibrary(player1, List.of(libraryCard, otherLibraryCard));
        harness.setHand(player1, List.of(new InsidiousDreams(), new CabalRitual()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castInstantForXWithDiscards(player1, 0, 0, List.of(), List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(libraryCard, otherLibraryCard);
    }

    @Test
    @DisplayName("X=1 puts the single chosen card on top without an order prompt")
    void xOnePutsSingleChosenCardOnTop() {
        Card chosenCard = new CabalRitual();
        Card remainingCard = new KamahlsSledge();
        harness.setLibrary(player1, List.of(chosenCard, remainingCard));
        harness.setHand(player1, List.of(new InsidiousDreams(), new KamahlsSledge()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castInstantForXWithDiscards(player1, 0, 1, List.of(), List.of(1));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.SearchLibraryToTopChoice.class))
                .isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(chosenCard.getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(chosenCard, remainingCard);
    }

    @Test
    @DisplayName("Cannot cast when X exceeds the number of cards available to discard")
    void cannotCastWithFewerCardsThanX() {
        InsidiousDreams spell = new InsidiousDreams();
        Card discard = new CabalRitual();
        harness.setLibrary(player1, List.of(new KamahlsSledge()));
        harness.setHand(player1, List.of(spell, discard));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castInstantForXWithDiscards(
                player1, 0, 2, List.of(), List.of(1, 2)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must discard");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(spell, discard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @CardUsed(PsychogenicProbe.class)
    @DisplayName("An empty library still causes shuffle-triggered abilities to trigger")
    void emptyLibraryStillTriggersShuffleAbilities() {
        harness.addToBattlefield(player2, new PsychogenicProbe());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new InsidiousDreams()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castInstantForXWithDiscards(player1, 0, 0, List.of(), List.of());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertLife(player1, 18);
    }
}
