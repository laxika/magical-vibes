package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.c.CanopyVista;
import com.github.laxika.magicalvibes.cards.o.OranRiefInvoker;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NaturalConnection.class, Plains.class, OranRiefInvoker.class, CanopyVista.class})
class NaturalConnectionTest extends BaseCardTest {

    @Test
    @DisplayName("Searches for a basic land and offers it to enter the battlefield tapped")
    void searchesForBasicLandToBattlefieldTapped() {
        Plains firstPlains = new Plains();
        Plains plains = new Plains();
        castWithLibrary(List.of(firstPlains, new OranRiefInvoker(), plains));

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(firstPlains, plains);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
        assertThat(search.params().canFailToFind()).isTrue();
    }

    @Test
    @DisplayName("Puts the chosen basic land onto the battlefield tapped")
    void chosenBasicLandEntersTapped() {
        Plains plains = new Plains();
        OranRiefInvoker creature = new OranRiefInvoker();
        castWithLibrary(List.of(plains, creature));

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, search.params().cards().indexOf(plains));

        assertThat(findPermanent(player1, "Plains").isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Natural Connection");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Does not offer a nonbasic land with basic land types")
    void excludesNonbasicLand() {
        Plains plains = new Plains();
        castWithLibrary(List.of(new CanopyVista(), plains));

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(plains);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Plains");
        harness.assertNotOnBattlefield(player1, "Canopy Vista");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Can fail to find even when a basic land is available")
    void canFailToFind() {
        Plains plains = new Plains();
        OranRiefInvoker creature = new OranRiefInvoker();
        castWithLibrary(List.of(plains, creature));

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(plains, creature);
        harness.assertNotOnBattlefield(player1, "Plains");
        harness.assertInGraveyard(player1, "Natural Connection");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Resolves without a choice when no basic lands are present")
    void resolvesWithoutMatchingCards() {
        CanopyVista land = new CanopyVista();
        OranRiefInvoker creature = new OranRiefInvoker();
        castWithLibrary(List.of(land, creature));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(land, creature);
        harness.assertNotOnBattlefield(player1, "Canopy Vista");
        harness.assertInGraveyard(player1, "Natural Connection");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Resolves normally with an empty library")
    void resolvesWithEmptyLibrary() {
        castWithLibrary(List.of());

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Natural Connection");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void castWithLibrary(List<Card> library) {
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new NaturalConnection()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0);
    }
}
