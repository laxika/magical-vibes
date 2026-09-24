package com.github.laxika.magicalvibes.cards.u;

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

@CardUsed({UnmarkedGrave.class, DuskImp.class, Karakas.class, Plains.class, Swamp.class})
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

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .contains("Dusk Imp", "Unmarked Grave")
                .doesNotContain("Karakas");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .contains("Karakas", "Plains", "Swamp");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
