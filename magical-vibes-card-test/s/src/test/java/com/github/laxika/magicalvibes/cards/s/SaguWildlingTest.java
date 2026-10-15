package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DispellingExhale;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.t.ThornwoodFalls;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SaguWildling.class, Forest.class, ThornwoodFalls.class, DispellingExhale.class})
class SaguWildlingTest extends BaseCardTest {

    @Test
    @DisplayName("When Sagu Wildling enters, its controller gains 3 life")
    void entersAndGainsLife() {
        harness.castFromHand(player1, new SaguWildling(), "{4}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Omen searches a basic land to hand and shuffles Sagu Wildling into its owner's library")
    void omenSearchesBasicLandAndShuffles() {
        Card saguWildling = new SaguWildling();
        Forest forest = new Forest();
        harness.setHand(player1, List.of(saguWildling));
        harness.setLibrary(player1, List.of(forest, new SaguWildling()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
        assertThat(gd.playerDecks.get(player1.getId())).contains(saguWildling);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(saguWildling);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Omen reveals only a basic land and returns exactly one physical card after searching")
    void omenReturnsExactlyOneCardAfterSuccessfulSearch() {
        SaguWildling wildling = new SaguWildling();
        Forest forest = new Forest();
        ThornwoodFalls nonbasicLand = new ThornwoodFalls();
        harness.setHand(player1, List.of(wildling));
        harness.setLibrary(player1, List.of(nonbasicLand, forest));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                (PendingInteraction.LibrarySearch) gd.interaction.activeInteraction();
        assertThat(search.params().cards()).containsExactly(forest);
        assertThat(search.params().reveals()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(wildling);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(nonbasicLand, wildling);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(wildling);
        harness.assertNotOnBattlefield(player1, "Sagu Wildling");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Omen still returns exactly once when its controller declines to find a basic land")
    void omenCanFailToFindWithBasicLandAvailable() {
        SaguWildling wildling = new SaguWildling();
        Forest forest = new Forest();
        harness.setHand(player1, List.of(wildling));
        harness.setLibrary(player1, List.of(forest));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(forest, wildling);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(wildling);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Omen returns exactly once when the library contains no basic land")
    void omenResolvesWithNoMatchingLand() {
        SaguWildling wildling = new SaguWildling();
        ThornwoodFalls nonbasicLand = new ThornwoodFalls();
        harness.setHand(player1, List.of(wildling));
        harness.setLibrary(player1, List.of(nonbasicLand));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(nonbasicLand, wildling);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(wildling);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Omen returns exactly once when searching an empty library")
    void omenResolvesWithEmptyLibrary() {
        SaguWildling wildling = new SaguWildling();
        harness.setHand(player1, List.of(wildling));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(wildling);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(wildling);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A countered Omen goes to the graveyard without searching or being shuffled back")
    void counteredOmenDoesNotSearchOrShuffleBack() {
        SaguWildling wildling = new SaguWildling();
        Forest forest = new Forest();
        harness.setHand(player1, List.of(wildling));
        harness.setLibrary(player1, List.of(forest));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player2, List.of(new DispellingExhale()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castWithAlternateCost(player1, 0, List.of());
        harness.castInstantWithBehold(player2, 0, wildling.getId(), List.of(), List.of());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(wildling);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, 20);
    }
}
