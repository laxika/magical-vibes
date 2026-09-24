package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VileEntomber.class, GrizzlyBears.class, Island.class, Plains.class})
class VileEntomberTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers any library card for the graveyard")
    void searchesAnyLibraryCardIntoGraveyard() {
        Card creature = new GrizzlyBears();
        Card island = new Island();
        Card plains = new Plains();
        harness.setLibrary(player1, List.of(creature, island, plains));
        castVileEntomber();

        GameData gd = harness.getGameData();
        var search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(creature, island, plains);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.GRAVEYARD);
        assertThat(search.params().reveals()).isFalse();
        assertThat(search.params().canFailToFind()).isFalse();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(island, plains);
    }

    private void castVileEntomber() {
        harness.setHand(player1, List.of(new VileEntomber()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
