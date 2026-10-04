package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AjaniInspiringLeader;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoldmaneGriffin.class, AjaniInspiringLeader.class})
class GoldmaneGriffinTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Goldmane Griffin triggers a may ability")
    void resolvingTriggersMayPrompt() {
        setupAndCast();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting the may ability returns Ajani, Inspiring Leader from the graveyard")
    void acceptingMayFindsAjaniInGraveyard() {
        harness.setGraveyard(player1, List.of(new AjaniInspiringLeader()));
        setupAndCast();

        resolveMay(true);

        harness.assertInHand(player1, "Ajani, Inspiring Leader");
        harness.assertNotInGraveyard(player1, "Ajani, Inspiring Leader");
    }

    @Test
    @DisplayName("Accepting the may ability searches the library when Ajani is not in the graveyard")
    void acceptingMaySearchesLibrary() {
        harness.setLibrary(player1, List.of(new AjaniInspiringLeader()));
        setupAndCast();

        resolveMay(true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).hasSize(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards().getFirst().getName()).isEqualTo("Ajani, Inspiring Leader");
    }

    @Test
    @DisplayName("Declining the may ability leaves Ajani in the graveyard")
    void decliningMayDoesNotSearch() {
        harness.setGraveyard(player1, List.of(new AjaniInspiringLeader()));
        setupAndCast();

        resolveMay(false);

        harness.assertInGraveyard(player1, "Ajani, Inspiring Leader");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Goldmane Griffin enters the battlefield")
    void goldmaneGriffinEntersBattlefield() {
        setupAndCast();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Goldmane Griffin");
    }

    @Test
    @DisplayName("Selecting Ajani from the library puts that copy into hand")
    void selectingLibraryCopyMovesItToHand() {
        AjaniInspiringLeader ajani = new AjaniInspiringLeader();
        harness.setLibrary(player1, List.of(new GoldmaneGriffin(), ajani));
        setupAndCast();

        resolveMay(true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(ajani);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(ajani);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A library search may fail to find Ajani even when present")
    void librarySearchMayFailToFind() {
        AjaniInspiringLeader ajani = new AjaniInspiringLeader();
        harness.setLibrary(player1, List.of(ajani));
        setupAndCast();

        resolveMay(true);
        harness.handleCardChosen(player1, -1);

        harness.assertNotInHand(player1, "Ajani, Inspiring Leader");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ajani);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Accepting with no matching card finishes without taking another card")
    void noMatchingCardFinishesWithoutTakingCard() {
        GoldmaneGriffin otherCard = new GoldmaneGriffin();
        harness.setLibrary(player1, List.of(otherCard));
        harness.setGraveyard(player1, List.of(new GoldmaneGriffin()));
        setupAndCast();

        resolveMay(true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(otherCard);
        harness.assertInGraveyard(player1, "Goldmane Griffin");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Ajani in the graveyard does not force selection before a search-zone choice")
    void matchingGraveyardCardDoesNotOverrideSearchChoice() {
        AjaniInspiringLeader graveyardCopy = new AjaniInspiringLeader();
        AjaniInspiringLeader libraryCopy = new AjaniInspiringLeader();
        harness.setGraveyard(player1, List.of(graveyardCopy));
        harness.setLibrary(player1, List.of(libraryCopy));
        setupAndCast();

        resolveMay(true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(graveyardCopy);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCopy);
        assertThat(gd.interaction.activeInteraction()).isNotNull();
    }

    private void setupAndCast() {
        harness.castFromHand(player1, new GoldmaneGriffin(), "{3}{W}{W}");
    }

    private void resolveMay(boolean choice) {
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, choice);
    }

}
