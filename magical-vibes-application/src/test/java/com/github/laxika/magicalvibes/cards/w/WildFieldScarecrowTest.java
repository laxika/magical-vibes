package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.d.DauntlessCathar;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WildFieldScarecrow.class, Forest.class, Plains.class, DauntlessCathar.class})
class WildFieldScarecrowTest extends BaseCardTest {

    @Test
    @DisplayName("Activating sacrifices Wild-Field Scarecrow and offers only basic lands for the search")
    void activatingSacrificesAndOffersBasicLands() {
        activateSearch();

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Wild-Field Scarecrow");
        harness.assertInGraveyard(player1, "Wild-Field Scarecrow");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> c.getName().equals("Forest") || c.getName().equals("Plains"))
                .noneMatch(c -> c.getName().equals("Dauntless Cathar"));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().destination())
                .isEqualTo(LibrarySearchDestination.HAND);
    }

    @Test
    @DisplayName("Both chosen basic lands go to hand")
    void bothChosenLandsGoToHand() {
        activateSearch();

        harness.passBothPriorities();

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleCardChosen(player1, 0);

        // A second pick is offered (up to two).
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
        assertThat(gd.playerDecks.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Forest") || c.getName().equals("Plains"));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Player may fail to find")
    void canFailToFind() {
        activateSearch();

        harness.passBothPriorities();
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The ability has no tap cost, so a tapped Wild-Field Scarecrow can still activate it")
    void tappedScarecrowCanStillActivate() {
        harness.addToBattlefield(player1, new WildFieldScarecrow());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        setupLibrary();
        gd.playerBattlefields.get(player1.getId()).get(0).tap();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Wild-Field Scarecrow");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
    }

    @Test
    @DisplayName("Player may choose only one basic land")
    void canStopAfterOneLand() {
        activateSearch();
        harness.passBothPriorities();
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(c -> c.getName())
                .containsExactlyInAnyOrder("Plains", "Dauntless Cathar");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Search resolves when the library contains no basic lands")
    void noBasicLandsInLibrary() {
        activateSearch();
        harness.setLibrary(player1, List.of(new DauntlessCathar()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerDecks.get(player1.getId())).extracting(c -> c.getName())
                .containsExactly("Dauntless Cathar");
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Wild-Field Scarecrow");
    }

    @Test
    @DisplayName("Search resolves with an empty library")
    void emptyLibrary() {
        activateSearch();
        harness.setLibrary(player1, List.of());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Wild-Field Scarecrow");
    }

    @Test
    @DisplayName("Sacrifice is paid immediately before the search resolves")
    void sacrificeIsPaidAsActivationCost() {
        activateSearch();

        harness.assertNotOnBattlefield(player1, "Wild-Field Scarecrow");
        harness.assertInGraveyard(player1, "Wild-Field Scarecrow");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);
    }

    private void activateSearch() {
        harness.addToBattlefield(player1, new WildFieldScarecrow());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        setupLibrary();
        harness.activateAbility(player1, 0, null, null);
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Forest(), new Plains(), new DauntlessCathar()));
    }
}
