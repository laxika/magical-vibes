package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.m.MuYanlingCelestialWind;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YanlingsHarbinger.class, MuYanlingCelestialWind.class})
class YanlingsHarbingerTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Yanling's Harbinger triggers a may ability")
    void resolvingTriggersMayPrompt() {
        setupAndCast();

        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting the may ability returns Mu Yanling, Celestial Wind from the graveyard")
    void acceptingMayFindsMuYanlingInGraveyard() {
        harness.setGraveyard(player1, List.of(new MuYanlingCelestialWind()));
        setupAndCast();

        resolveMay(true);

        harness.assertInHand(player1, "Mu Yanling, Celestial Wind");
        harness.assertNotInGraveyard(player1, "Mu Yanling, Celestial Wind");
    }

    @Test
    @DisplayName("Accepting the may ability searches the library when Mu Yanling is not in the graveyard")
    void acceptingMaySearchesLibrary() {
        harness.setLibrary(player1, List.of(new MuYanlingCelestialWind()));
        setupAndCast();

        resolveMay(true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).hasSize(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards().getFirst().getName()).isEqualTo("Mu Yanling, Celestial Wind");
    }

    @Test
    @DisplayName("Declining the may ability leaves Mu Yanling in the graveyard")
    void decliningMayDoesNotSearch() {
        harness.setGraveyard(player1, List.of(new MuYanlingCelestialWind()));
        setupAndCast();

        resolveMay(false);

        harness.assertInGraveyard(player1, "Mu Yanling, Celestial Wind");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Choosing a library copy puts only that card into hand")
    void choosingLibraryCopyMovesItToHand() {
        MuYanlingCelestialWind selected = new MuYanlingCelestialWind();
        MuYanlingCelestialWind other = new MuYanlingCelestialWind();
        harness.setLibrary(player1, List.of(selected, other));
        setupAndCast();

        resolveMay(true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(selected);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(other);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A named library search may fail to find an existing copy")
    void canFailToFindInLibrary() {
        MuYanlingCelestialWind muYanling = new MuYanlingCelestialWind();
        harness.setLibrary(player1, List.of(muYanling));
        setupAndCast();

        resolveMay(true);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(muYanling);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A graveyard copy does not force retrieval before choosing which zone to search")
    void offersSearchChoiceWhenBothZonesContainMuYanling() {
        MuYanlingCelestialWind graveyardCopy = new MuYanlingCelestialWind();
        harness.setGraveyard(player1, List.of(graveyardCopy));
        harness.setLibrary(player1, List.of(new MuYanlingCelestialWind()));
        setupAndCast();

        resolveMay(true);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(graveyardCopy);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNotNull();
    }

    @Test
    @DisplayName("Searching with no matching card leaves unrelated library cards in place")
    void noMatchingCardDoesNotMoveUnrelatedCards() {
        YanlingsHarbinger unrelated = new YanlingsHarbinger();
        harness.setLibrary(player1, List.of(unrelated));
        setupAndCast();

        resolveMay(true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(unrelated);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new YanlingsHarbinger()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castCreature(player1, 0);
    }

    private void resolveMay(boolean choice) {
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, choice);
    }
}
