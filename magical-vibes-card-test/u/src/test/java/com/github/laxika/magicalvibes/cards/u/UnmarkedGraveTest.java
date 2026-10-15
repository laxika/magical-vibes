package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.a.Asmoranomardicadaistinaculdacar;
import com.github.laxika.magicalvibes.cards.d.DuskImp;
import com.github.laxika.magicalvibes.cards.k.Karakas;
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

@CardUsed({UnmarkedGrave.class, Asmoranomardicadaistinaculdacar.class,
        DuskImp.class, Karakas.class, Plains.class, Swamp.class})
class UnmarkedGraveTest extends BaseCardTest {

    @Test
    @DisplayName("Offers only nonlegendary cards and puts the chosen card into the graveyard")
    void searchesForNonlegendaryCardIntoGraveyard() {
        harness.castFromHand(player1, new UnmarkedGrave(), "{1}{B}");
        harness.setLibrary(player1, List.of(new Karakas(), new DuskImp(), new Plains(), new Swamp()));

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        var search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).extracting(Card::getName)
                .containsExactly("Dusk Imp", "Plains", "Swamp");
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.GRAVEYARD);
        assertThat(search.params().reveals()).isFalse();
        assertThat(search.params().canFailToFind()).isTrue();

        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Dusk Imp");
        harness.assertInGraveyard(player1, "Unmarked Grave");
        harness.assertNotInGraveyard(player1, "Karakas");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .contains("Karakas", "Plains", "Swamp");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Can fail to find even when a nonlegendary card is available")
    void canDeclineToFindMatchingCard() {
        harness.castFromHand(player1, new UnmarkedGrave(), "{1}{B}");
        Swamp swamp = new Swamp();
        harness.setLibrary(player1, List.of(swamp));
        harness.passBothPriorities();

        harness.handleCardChosen(player1, -1);

        harness.assertInGraveyard(player1, "Unmarked Grave");
        harness.assertNotInGraveyard(player1, "Swamp");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(swamp);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Resolves without finding a card when the library contains only legendary cards")
    void resolvesWithOnlyLegendaryCards() {
        harness.castFromHand(player1, new UnmarkedGrave(), "{1}{B}");
        Asmoranomardicadaistinaculdacar legendary = new Asmoranomardicadaistinaculdacar();
        harness.setLibrary(player1, List.of(legendary));

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Unmarked Grave");
        harness.assertNotInGraveyard(player1, "Asmoranomardicadaistinaculdacar");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(legendary);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Resolves normally with an empty library")
    void resolvesWithEmptyLibrary() {
        harness.castFromHand(player1, new UnmarkedGrave(), "{1}{B}");
        harness.setLibrary(player1, List.of());

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Unmarked Grave");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can put a noncreature land into the graveyard and searches only its controller's library")
    void canFindLandInControllersLibrary() {
        harness.castFromHand(player1, new UnmarkedGrave(), "{1}{B}");
        Swamp swamp = new Swamp();
        Plains opponentsPlains = new Plains();
        harness.setLibrary(player1, List.of(swamp));
        harness.setLibrary(player2, List.of(opponentsPlains));
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Swamp");
        harness.assertInGraveyard(player1, "Unmarked Grave");
        harness.assertNotInHand(player1, "Swamp");
        harness.assertNotOnBattlefield(player1, "Swamp");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentsPlains);
        harness.assertNotInGraveyard(player2, "Plains");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
