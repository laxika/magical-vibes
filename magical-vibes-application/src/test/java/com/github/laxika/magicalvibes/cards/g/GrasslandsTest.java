package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Grasslands.class, Forest.class, Island.class, Mountain.class, Plains.class, Swamp.class})
class GrasslandsTest extends BaseCardTest {

    @Test
    @DisplayName("Grasslands enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new Grasslands()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent land = findPermanent(player1, "Grasslands");
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Search ability cannot be activated while Grasslands is tapped")
    void searchRequiresUntappedSource() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Grasslands());
        land.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(land);
    }

    @Test
    @DisplayName("Search ability sacrifices Grasslands and presents only Forest or Plains cards")
    void searchPresentsOnlyForestOrPlains() {
        activateSearch();

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grasslands");
        harness.assertInGraveyard(player1, "Grasslands");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> c.getName().equals("Forest") || c.getName().equals("Plains"))
                .anyMatch(c -> c.getName().equals("Forest"))
                .anyMatch(c -> c.getName().equals("Plains"))
                .noneMatch(c -> c.getName().equals("Island")
                        || c.getName().equals("Mountain")
                        || c.getName().equals("Swamp"));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().destination())
                .isEqualTo(LibrarySearchDestination.BATTLEFIELD);
    }

    @Test
    @DisplayName("Search resolves without a choice when the library has no Forest or Plains")
    void searchWithNoMatchingCards() {
        harness.addToBattlefield(player1, new Grasslands());
        harness.setLibrary(player1, List.of(new Island(), new Mountain(), new Swamp()));
        harness.activateAbility(player1, 0, null, null);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grasslands");
        harness.assertInGraveyard(player1, "Grasslands");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Chosen land enters the battlefield untapped")
    void chosenLandEntersUntapped() {
        activateSearch();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Forest") && !p.isTapped());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Player may fail to find")
    void canFailToFind() {
        activateSearch();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().hasType(CardType.LAND));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void activateSearch() {
        harness.addToBattlefield(player1, new Grasslands());
        setupLibrary();
        harness.activateAbility(player1, 0, null, null);
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Forest(), new Plains(), new Island(), new Mountain(), new Swamp()));
    }
}
