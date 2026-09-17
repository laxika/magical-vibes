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

@CardUsed({WindsweptHeath.class, Forest.class, Plains.class, Mountain.class})
class WindsweptHeathTest extends BaseCardTest {

    @Test
    @DisplayName("Activating Windswept Heath pays 1 life, sacrifices it, and searches for a Forest or Plains")
    void activationPaysLifeSacrificesAndSearchesForMatchingLand() {
        activateSearch();

        harness.assertLife(player1, 19);
        harness.assertNotOnBattlefield(player1, "Windswept Heath");
        harness.assertInGraveyard(player1, "Windswept Heath");

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .allMatch(card -> card.getName().equals("Forest") || card.getName().equals("Plains"))
                .anyMatch(card -> card.getName().equals("Forest"))
                .anyMatch(card -> card.getName().equals("Plains"))
                .noneMatch(card -> card.getName().equals("Mountain"));
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD);
    }

    @Test
    @DisplayName("Chosen Forest or Plains enters the battlefield untapped")
    void chosenLandEntersUntapped() {
        activateSearch();
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> (permanent.getCard().getName().equals("Forest")
                        || permanent.getCard().getName().equals("Plains")) && !permanent.isTapped());
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
    @DisplayName("Search ability cannot be activated while Windswept Heath is tapped")
    void searchRequiresUntappedSource() {
        Permanent heath = harness.addToBattlefieldAndReturn(player1, new WindsweptHeath());
        heath.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(heath);
    }

    @Test
    @DisplayName("Search resolves without a choice when the library has no Forest or Plains")
    void searchWithNoMatchingCards() {
        activateSearchWithLibrary(List.of(new Mountain()));

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Windswept Heath");
        harness.assertInGraveyard(player1, "Windswept Heath");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void activateSearch() {
        activateSearchWithLibrary(List.of(new Forest(), new Plains(), new Mountain()));
    }

    private void activateSearchWithLibrary(List<Card> library) {
        harness.addToBattlefield(player1, new WindsweptHeath());
        harness.setLibrary(player1, library);

        harness.activateAbility(player1, 0, null, null);
    }
}
