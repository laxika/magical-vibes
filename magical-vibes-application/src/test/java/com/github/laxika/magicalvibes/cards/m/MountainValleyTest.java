package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
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

@CardUsed({Forest.class, Island.class, Mountain.class, MountainValley.class, Swamp.class})
class MountainValleyTest extends BaseCardTest {

    @Test
    @DisplayName("Mountain Valley enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new MountainValley()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent valley = findPermanent(player1, "Mountain Valley");
        assertThat(valley.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Search ability cannot be activated while Mountain Valley is tapped")
    void searchRequiresUntappedSource() {
        Permanent valley = harness.addToBattlefieldAndReturn(player1, new MountainValley());
        valley.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(valley);
    }

    @Test
    @DisplayName("Search ability sacrifices Mountain Valley and presents only Mountain or Forest cards")
    void searchPresentsOnlyMountainOrForest() {
        activateSearch();

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Mountain Valley");
        harness.assertInGraveyard(player1, "Mountain Valley");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .hasSize(2)
                .allMatch(c -> c.getName().equals("Mountain") || c.getName().equals("Forest"))
                .anyMatch(c -> c.getName().equals("Mountain"))
                .anyMatch(c -> c.getName().equals("Forest"));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().destination())
                .isEqualTo(LibrarySearchDestination.BATTLEFIELD);
    }

    @Test
    @DisplayName("Search resolves without a choice when the library has no Mountain or Forest")
    void searchWithNoMatchingCards() {
        harness.addToBattlefield(player1, new MountainValley());
        harness.setLibrary(player1, List.of(new Island(), new Swamp()));
        harness.activateAbility(player1, 0, null, null);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Mountain Valley");
        harness.assertInGraveyard(player1, "Mountain Valley");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Chosen land enters the battlefield untapped")
    void chosenLandEntersUntapped() {
        activateSearch();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Mountain") && !p.isTapped());
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
        harness.addToBattlefield(player1, new MountainValley());
        setupLibrary();
        harness.activateAbility(player1, 0, null, null);
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Mountain(), new Forest(), new Island(), new Swamp()));
    }
}
