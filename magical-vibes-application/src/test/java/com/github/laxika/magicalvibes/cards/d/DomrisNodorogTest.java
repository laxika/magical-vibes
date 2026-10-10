package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DomrisNodorog.class, DomriCitySmasher.class})
class DomrisNodorogTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates the optional search prompt")
    void enteringCreatesMayPrompt() {
        castNodorog();

        resolveEnterTheBattlefieldTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting the search returns Domri from the graveyard")
    void searchesGraveyardForDomri() {
        Card domri = new DomriCitySmasher();
        harness.setLibrary(player1, List.of());
        harness.setGraveyard(player1, List.of(domri));
        castNodorog();

        resolveSearch(true);
        harness.handleMultipleCardsChosen(player1, List.of(domri.getId()));

        harness.assertInHand(player1, "Domri, City Smasher");
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(domri);
    }

    @Test
    @DisplayName("Accepting the search offers Domri from the library and shuffles after selection")
    void searchesLibraryForDomri() {
        Card domri = new DomriCitySmasher();
        Card otherCard = new DomrisNodorog();
        harness.setLibrary(player1, List.of(domri, otherCard));
        castNodorog();

        resolveSearch(true);

        PendingInteraction.SearchLibraryAndOrGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(domri.getId());

        harness.handleMultipleCardsChosen(player1, List.of(domri.getId()));

        harness.assertInHand(player1, "Domri, City Smasher");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(otherCard);
        assertThat(gd.playersWhoSearchedLibraryThisTurn).contains(player1.getId());
    }

    @Test
    @DisplayName("Declining the search leaves Domri where it is")
    void decliningSearchDoesNothing() {
        Card domri = new DomriCitySmasher();
        harness.setGraveyard(player1, List.of(domri));
        castNodorog();

        resolveSearch(false);

        harness.assertInGraveyard(player1, "Domri, City Smasher");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void castNodorog() {
        harness.castFromHand(player1, new DomrisNodorog(), "{3}{R}{G}");
    }

    private void resolveEnterTheBattlefieldTrigger() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void resolveSearch(boolean accept) {
        resolveEnterTheBattlefieldTrigger();
        harness.handleMayAbilityChosen(player1, accept);
    }
}
