package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.cards.t.TranquilThicket;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KrosanTusker.class, Forest.class, GlorySeeker.class, TranquilThicket.class})
class KrosanTuskerTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling may reveal a basic land into hand before drawing")
    void cyclingSearchesBasicLandThenDraws() {
        harness.setHand(player1, List.of(new KrosanTusker()));
        harness.setLibrary(player1, List.of(new Forest(), new GlorySeeker()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Krosan Tusker");
        harness.assertInHand(player1, "Forest");
        harness.assertNotInHand(player1, "Glory Seeker");
        harness.passBothPriorities();
        harness.assertInHand(player1, "Glory Seeker");
    }

    @Test
    @DisplayName("Cycling search offers only basic lands to hand")
    void searchFiltersToBasicLands() {
        Forest forest = new Forest();
        harness.setHand(player1, List.of(new KrosanTusker()));
        harness.setLibrary(player1, List.of(forest, new GlorySeeker()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(forest);
        assertThat(search.params().reveals()).isTrue();
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.HAND);
    }

    @Test
    @DisplayName("Declining the search still draws a card")
    void decliningSearchStillDraws() {
        harness.setHand(player1, List.of(new KrosanTusker()));
        harness.setLibrary(player1, List.of(new GlorySeeker()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Krosan Tusker");
        harness.assertNotInHand(player1, "Glory Seeker");
        harness.passBothPriorities();
        harness.assertInHand(player1, "Glory Seeker");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Accepting the search with no basic land still draws")
    void acceptingSearchWithNoBasicLandStillDraws() {
        harness.setHand(player1, List.of(new KrosanTusker()));
        harness.setLibrary(player1, List.of(new GlorySeeker()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Krosan Tusker");
        harness.assertNotInHand(player1, "Glory Seeker");
        harness.passBothPriorities();
        harness.assertInHand(player1, "Glory Seeker");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Cycling puts the search trigger above the separate draw ability")
    void cyclingCreatesSeparateSearchTrigger() {
        harness.setHand(player1, List.of(new KrosanTusker()));
        harness.setLibrary(player1, List.of(new Forest(), new GlorySeeker()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Krosan Tusker");
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getFirst().getEntryType())
                .isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getLast().getEntryType())
                .isEqualTo(StackEntryType.TRIGGERED_ABILITY);
    }

    @Test
    @DisplayName("Failing to find a basic land still leaves the cycling draw to resolve")
    void mayFailToFindEvenWhenBasicLandExists() {
        harness.setHand(player1, List.of(new KrosanTusker()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        harness.assertNotInHand(player1, "Forest");
        harness.passBothPriorities();
        harness.assertInHand(player1, "Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The search cannot find a nonbasic land")
    void searchExcludesNonbasicLands() {
        Forest forest = new Forest();
        harness.setHand(player1, List.of(new KrosanTusker()));
        harness.setLibrary(player1, List.of(new TranquilThicket(), forest));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(forest);
        harness.handleCardChosen(player1, 0);
        harness.assertInHand(player1, "Forest");
    }
}
