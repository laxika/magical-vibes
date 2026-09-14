package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TropicalIsland;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LandGrant.class, GrizzlyBears.class, Forest.class, TropicalIsland.class})
class LandGrantTest extends BaseCardTest {

    @Test
    @DisplayName("Can reveal a landless hand to search for a Forest")
    void castsForAlternateCostAndSearchesForForest() {
        harness.setHand(player1, List.of(new LandGrant(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Forest()));

        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("reveals their hand") && log.contains("Grizzly Bears"));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Forest");
        assertThat(gameLogContains("Library is shuffled")).isTrue();
    }

    @Test
    @DisplayName("Can be cast for its mana cost without revealing the hand")
    void castsForManaCostWithoutRevealingHand() {
        harness.setLibrary(player1, List.of(new Forest()));

        harness.castFromHand(player1, new LandGrant(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .noneMatch(log -> log.contains("reveals their hand"));

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Searches for a card with the Forest subtype, not only a basic Forest")
    void searchesForNonbasicForestCard() {
        harness.setHand(player1, List.of(new LandGrant(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new TropicalIsland()));

        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Tropical Island");
    }

    @Test
    @DisplayName("Resolves and shuffles when the library has no Forest card")
    void resolvesWhenNoForestIsAvailable() {
        GrizzlyBears nonForestCard = new GrizzlyBears();
        harness.setHand(player1, List.of(new LandGrant()));
        harness.setLibrary(player1, List.of(nonForestCard));

        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonForestCard);
        assertThat(gameLogContains("finds no Forest cards")).isTrue();
    }

    @Test
    @DisplayName("Cannot use the alternate cost with a land in hand")
    void alternateCostRequiresNoLandInHand() {
        harness.setHand(player1, List.of(new LandGrant(), new Forest()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("condition is not met");
    }
}
