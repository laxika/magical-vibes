package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.d.DevilthornFox;
import com.github.laxika.magicalvibes.cards.p.PastInFlames;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BeholdTheBeyond.class, Forest.class, Plains.class, Swamp.class, DevilthornFox.class, PastInFlames.class})
class BeholdTheBeyondTest extends BaseCardTest {

    @Test
    @DisplayName("Discards your hand and searches three cards into your hand")
    void discardsHandAndSearchesThreeCards() {
        Card discardedOne = new Forest();
        Card discardedTwo = new Plains();
        Card searchedOne = new Swamp();
        Card searchedTwo = new DevilthornFox();
        Card searchedThree = new Forest();
        Card libraryRemainder = new Plains();

        harness.setHand(player1, List.of(new BeholdTheBeyond(), discardedOne, discardedTwo));
        harness.setLibrary(player1, List.of(searchedOne, searchedTwo, searchedThree, libraryRemainder));
        harness.addMana(player1, ManaColor.BLACK, 7);
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discardedOne, discardedTwo);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().remainingCount()).isEqualTo(3);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(searchedOne, searchedTwo, searchedThree);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryRemainder);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Behold the Beyond");
    }

    @Test
    @DisplayName("An empty hand still searches, and an unrestricted search cannot stop early")
    void emptyHandStillRequiresThreeCards() {
        Card first = new Forest();
        Card second = new Forest();
        Card third = new Swamp();
        Card remainder = new Plains();
        harness.setHand(player1, List.of(new BeholdTheBeyond()));
        harness.setLibrary(player1, List.of(first, second, third, remainder));
        harness.addMana(player1, ManaColor.BLACK, 7);
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThatThrownBy(() -> harness.handleCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class);
        harness.handleCardChosen(player1, 0);
        assertThatThrownBy(() -> harness.handleCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(first, second, third);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainder);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A library with fewer than three cards yields every available card")
    void searchesAllCardsInShortLibrary() {
        Card first = new Forest();
        Card second = new Plains();
        harness.setHand(player1, List.of(new BeholdTheBeyond()));
        harness.setLibrary(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.BLACK, 7);
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Behold the Beyond");
    }

    @Test
    @DisplayName("An empty library still discards the controller's hand and leaves the opponent's hand alone")
    void emptyLibraryStillDiscardsHand() {
        Card discarded = new Forest();
        Card opponentCard = new Plains();
        harness.setHand(player1, List.of(new BeholdTheBeyond(), discarded));
        harness.setHand(player2, List.of(opponentCard));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.BLACK, 7);
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Behold the Beyond");
    }

    @Test
    @DisplayName("Granted flashback still searches for three cards")
    void grantedFlashbackSearchesThreeCards() {
        Card behold = new BeholdTheBeyond();
        Card discarded = new Swamp();
        Card first = new Forest();
        Card second = new Plains();
        Card third = new DevilthornFox();
        Card remainder = new Swamp();
        harness.setGraveyard(player1, List.of(behold));
        harness.setHand(player1, List.of(new PastInFlames(), discarded));
        harness.setLibrary(player1, List.of(first, second, third, remainder));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.addMana(player1, ManaColor.BLACK, 7);
        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().remainingCount()).isEqualTo(3);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(first, second, third);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainder);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
