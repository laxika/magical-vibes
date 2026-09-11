package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GiftOfEstates.class, Forest.class, Plains.class, GrizzlyBears.class})
class GiftOfEstatesTest extends BaseCardTest {

    @Test
    @DisplayName("When an opponent controls more lands, resolving presents only Plains cards")
    void opponentControlsMoreLandsPresentsPlains() {
        setupAndCast();
        harness.addToBattlefield(player2, new Forest());
        harness.setLibrary(player1, List.of(new Plains(), new Plains(), new Plains(), new GrizzlyBears()));

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> c.getName().equals("Plains"));
    }

    @Test
    @DisplayName("Chosen Plains cards go to hand")
    void chosenPlainsGoToHand() {
        setupAndCast();
        harness.addToBattlefield(player2, new Forest());
        harness.setLibrary(player1, List.of(new Plains(), new Plains(), new Plains(), new GrizzlyBears()));

        harness.passBothPriorities();

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        harness.assertInHand(player1, "Plains");
    }

    @Test
    @DisplayName("The search may find no Plains cards when the player chooses none")
    void mayChooseNoPlains() {
        setupAndCast();
        harness.addToBattlefield(player2, new Forest());
        harness.setLibrary(player1, List.of(new Plains(), new GrizzlyBears()));

        harness.passBothPriorities();

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        harness.assertNotInHand(player1, "Plains");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("No library search interaction is offered when no Plains card is available")
    void noSearchWhenLibraryHasNoPlains() {
        setupAndCast();
        harness.addToBattlefield(player2, new Forest());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        harness.assertNotInHand(player1, "Plains");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The search may put up to three Plains cards into hand")
    void mayChooseUpToThreePlains() {
        setupAndCast();
        harness.addToBattlefield(player2, new Forest());
        harness.setLibrary(player1, List.of(
                new Plains(), new Plains(), new Plains(), new Plains(), new GrizzlyBears()));

        harness.passBothPriorities();

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("When you control at least as many lands, no search happens")
    void noSearchWhenNotFewerLands() {
        setupAndCast();
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.setLibrary(player1, List.of(new Plains(), new Plains(), new Plains(), new GrizzlyBears()));

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        harness.assertNotInHand(player1, "Plains");
    }

    private void setupAndCast() {
        harness.castFromHand(player1, new GiftOfEstates(), "{1}{W}");
    }
}
