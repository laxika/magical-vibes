package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShorelineRanger.class, Island.class, Forest.class})
class ShorelineRangerTest extends BaseCardTest {

    @Test
    @DisplayName("Islandcycling discards Shoreline Ranger and searches for an Island")
    void islandcyclingSearchesForIsland() {
        ShorelineRanger ranger = new ShorelineRanger();
        Card island = new Island();
        Card forest = new Forest();
        harness.setHand(player1, List.of(ranger));
        harness.setLibrary(player1, List.of(island, forest));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(island);
        assertThat(search.params().reveals()).isTrue();
        harness.assertInGraveyard(player1, "Shoreline Ranger");

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Island");
    }

    @Test
    @DisplayName("Islandcycling completes without a choice when no Island is in the library")
    void islandcyclingCanFailToFind() {
        ShorelineRanger ranger = new ShorelineRanger();
        Card forest = new Forest();
        harness.setHand(player1, List.of(ranger));
        harness.setLibrary(player1, List.of(forest));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Shoreline Ranger");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
    }
}
