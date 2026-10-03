package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DemonicConsultation.class, Counterspell.class, DarkRitual.class})
class DemonicConsultationTest extends BaseCardTest {

    private void cast() {
        harness.castFromHand(player1, new DemonicConsultation(), "{B}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Resolving prompts the controller to name a card")
    void promptsControllerToNameCard() {
        cast();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        var choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.context()).isInstanceOf(ChoiceContext.ChooseNameExileTopRevealUntilNamedChoice.class);
    }

    @Test
    @DisplayName("Exiles top six, puts the named card into hand, and exiles other revealed cards")
    void findsNamedCardAfterTopSix() {
        UUID p1 = player1.getId();
        List<Card> deck = new ArrayList<>();
        List<Card> topSix = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            Card c = new DarkRitual();
            topSix.add(c);
            deck.add(c);
        }
        Card miss = new DarkRitual();
        Card hit = new Counterspell();
        Card secondHit = new Counterspell();
        Card leftover = new DarkRitual();
        deck.add(miss);
        deck.add(hit);
        deck.add(secondHit);
        deck.add(leftover);
        harness.setLibrary(player1, deck);

        cast();
        harness.handleListChoice(player1, "Counterspell");

        assertThat(gd.playerHands.get(p1)).anyMatch(c -> c.getId().equals(hit.getId()));
        assertThat(gd.getPlayerExiledCards(p1))
                .extracting(Card::getId)
                .containsAll(topSix.stream().map(Card::getId).toList())
                .contains(miss.getId())
                .doesNotContain(hit.getId(), secondHit.getId(), leftover.getId());
        assertThat(gd.playerDecks.get(p1)).containsExactly(secondHit, leftover);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("If the named card is never revealed, the entire remaining library is exiled")
    void missesAndExilesLibrary() {
        UUID p1 = player1.getId();
        List<Card> deck = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            deck.add(new DarkRitual());
        }
        Card onlyCopy = new Counterspell();
        // Put the only copy among the top six so the dig cannot find it.
        deck.set(2, onlyCopy);
        Card restA = new DarkRitual();
        Card restB = new DarkRitual();
        deck.add(restA);
        deck.add(restB);
        harness.setLibrary(player1, deck);

        cast();
        harness.handleListChoice(player1, "Counterspell");

        assertThat(gd.playerHands.get(p1)).noneMatch(c -> c.getId().equals(onlyCopy.getId()));
        assertThat(gd.playerDecks.get(p1)).isEmpty();
        assertThat(gd.getPlayerExiledCards(p1))
                .extracting(Card::getId)
                .contains(onlyCopy.getId(), restA.getId(), restB.getId());
    }

    @Test
    @DisplayName("Named card immediately after the top six goes to hand with no other dig exile")
    void namedCardIsNextAfterTopSix() {
        UUID p1 = player1.getId();
        List<Card> deck = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            deck.add(new DarkRitual());
        }
        Card hit = new Counterspell();
        deck.add(hit);
        harness.setLibrary(player1, deck);

        cast();
        harness.handleListChoice(player1, "Counterspell");

        assertThat(gd.playerHands.get(p1)).anyMatch(c -> c.getId().equals(hit.getId()));
        assertThat(gd.playerDecks.get(p1)).isEmpty();
        assertThat(gd.getPlayerExiledCards(p1)).hasSize(6)
                .noneMatch(c -> c.getId().equals(hit.getId()));
    }

    @Test
    @DisplayName("With an empty library, exiles no cards and completes")
    void handlesEmptyLibrary() {
        harness.setLibrary(player1, List.of());

        cast();
        harness.handleListChoice(player1, "Demonic Consultation");

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Demonic Consultation");
    }

    @Test
    @DisplayName("A library shorter than six is entirely exiled, including the named card")
    void handlesLibraryShorterThanSix() {
        Card hit = new Counterspell();
        List<Card> deck = List.of(new DarkRitual(), hit, new DarkRitual());
        harness.setLibrary(player1, deck);

        cast();
        harness.handleListChoice(player1, "Counterspell");

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyElementsOf(deck);
        harness.assertNotInHand(player1, "Counterspell");
        harness.assertInGraveyard(player1, "Demonic Consultation");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A real card name absent from the game may be chosen")
    void allowsNameAbsentFromGame() {
        List<Card> deck = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            deck.add(new DarkRitual());
        }
        harness.setLibrary(player1, deck);
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of());

        cast();
        harness.handleListChoice(player1, "Counterspell");

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyElementsOf(deck);
        harness.assertNotInHand(player1, "Counterspell");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Text that is not an Oracle card name must be rejected without changing the library")
    void rejectsNonexistentCardName() {
        Card card = new DarkRitual();
        harness.setLibrary(player1, List.of(card));
        cast();

        assertThatThrownBy(() -> harness.handleListChoice(player1,
                "This is not an actual Oracle card name"))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(card);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNotNull();
    }
}
