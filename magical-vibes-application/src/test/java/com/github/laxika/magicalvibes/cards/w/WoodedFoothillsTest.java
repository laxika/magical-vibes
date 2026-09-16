package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
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

@CardUsed({WoodedFoothills.class, Forest.class, Mountain.class, Plains.class})
class WoodedFoothillsTest extends BaseCardTest {

    @Test
    @DisplayName("Activating Wooded Foothills pays 1 life, sacrifices it, and searches for a Mountain or Forest")
    void activationPaysLifeSacrificesAndSearchesForMatchingLand() {
        activateSearch();

        harness.assertLife(player1, 19);
        harness.assertNotOnBattlefield(player1, "Wooded Foothills");
        harness.assertInGraveyard(player1, "Wooded Foothills");

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .allMatch(card -> card.getName().equals("Mountain") || card.getName().equals("Forest"))
                .noneMatch(card -> card.getName().equals("Plains") || card.getName().equals("Wooded Foothills"));
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD);
    }

    @Test
    @DisplayName("Search ability cannot be activated while Wooded Foothills is tapped")
    void searchRequiresUntappedSource() {
        Permanent foothills = harness.addToBattlefieldAndReturn(player1, new WoodedFoothills());
        foothills.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(foothills);
    }

    @Test
    @DisplayName("Search resolves without a choice when the library has no Mountain or Forest")
    void searchWithNoMatchingCards() {
        activateSearchWithLibrary(List.of(new Plains(), new WoodedFoothills()));

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Wooded Foothills");
        harness.assertInGraveyard(player1, "Wooded Foothills");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Chosen Mountain or Forest enters the battlefield untapped")
    void chosenLandEntersUntapped() {
        activateSearch();
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> (permanent.getCard().getName().equals("Mountain")
                        || permanent.getCard().getName().equals("Forest")) && !permanent.isTapped());
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

    private void activateSearch() {
        activateSearchWithLibrary(List.of(new Mountain(), new Forest(), new Plains(), new WoodedFoothills()));
    }

    private void activateSearchWithLibrary(List<Card> library) {
        harness.addToBattlefield(player1, new WoodedFoothills());
        harness.setLibrary(player1, library);

        harness.activateAbility(player1, 0, null, null);
    }
}
