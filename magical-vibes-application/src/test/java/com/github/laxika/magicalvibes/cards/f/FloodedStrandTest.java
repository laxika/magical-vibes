package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.w.WirewoodElf;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FloodedStrand.class, Island.class, Mountain.class, Plains.class, WirewoodElf.class})
class FloodedStrandTest extends BaseCardTest {

    @Test
    @DisplayName("Activating Flooded Strand pays 1 life, sacrifices it, and searches for a Plains or Island")
    void activationPaysLifeSacrificesAndSearchesForMatchingLand() {
        activateSearch();

        harness.assertLife(player1, 19);
        harness.assertNotOnBattlefield(player1, "Flooded Strand");
        harness.assertInGraveyard(player1, "Flooded Strand");

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .allMatch(card -> card.getName().equals("Plains") || card.getName().equals("Island"))
                .anyMatch(card -> card.getName().equals("Plains"))
                .anyMatch(card -> card.getName().equals("Island"))
                .noneMatch(card -> card.getName().equals("Mountain") || card.getName().equals("Wirewood Elf"));
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD);
    }

    @Test
    @DisplayName("Chosen Plains or Island enters the battlefield untapped")
    void chosenLandEntersUntapped() {
        activateSearch();
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> (permanent.getCard().getName().equals("Plains")
                        || permanent.getCard().getName().equals("Island")) && !permanent.isTapped());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Player may fail to find")
    void canFailToFind() {
        activateSearch();
        harness.passBothPriorities();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().hasType(CardType.LAND));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Search ability cannot be activated while Flooded Strand is tapped")
    void searchRequiresUntappedSource() {
        Permanent strand = harness.addToBattlefieldAndReturn(player1, new FloodedStrand());
        strand.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(strand);
    }

    @Test
    @DisplayName("Search resolves without a choice when the library has no Plains or Island")
    void searchWithNoMatchingCards() {
        activateSearchWithLibrary(List.of(new Mountain(), new WirewoodElf()));

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Flooded Strand");
        harness.assertInGraveyard(player1, "Flooded Strand");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void activateSearch() {
        activateSearchWithLibrary(List.of(new Plains(), new Island(), new Mountain(), new WirewoodElf()));
    }

    private void activateSearchWithLibrary(List<Card> library) {
        harness.addToBattlefield(player1, new FloodedStrand());
        harness.setLibrary(player1, library);

        harness.activateAbility(player1, 0, null, null);
    }
}
