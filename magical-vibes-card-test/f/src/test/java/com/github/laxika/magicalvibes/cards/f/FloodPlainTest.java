package com.github.laxika.magicalvibes.cards.f;

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

@CardUsed({FloodPlain.class, FemerefScouts.class, Island.class, Mountain.class, Plains.class, Swamp.class})
class FloodPlainTest extends BaseCardTest {

    @Test
    @DisplayName("Flood Plain enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new FloodPlain()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent plain = findPermanent(player1, "Flood Plain");
        assertThat(plain.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Search ability cannot be activated while Flood Plain is tapped")
    void searchRequiresUntappedSource() {
        Permanent plain = harness.addToBattlefieldAndReturn(player1, new FloodPlain());
        plain.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(plain);
    }

    @Test
    @DisplayName("Search ability sacrifices Flood Plain and presents only Plains or Island cards")
    void searchPresentsOnlyPlainsOrIsland() {
        activateSearch();

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Flood Plain");
        harness.assertInGraveyard(player1, "Flood Plain");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> c.getName().equals("Plains") || c.getName().equals("Island"))
                .anyMatch(c -> c.getName().equals("Plains"))
                .anyMatch(c -> c.getName().equals("Island"))
                .noneMatch(c -> c.getName().equals("Swamp") || c.getName().equals("Mountain"));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().destination())
                .isEqualTo(LibrarySearchDestination.BATTLEFIELD);
    }

    @Test
    @DisplayName("Search resolves without a choice when the library has no Plains or Island")
    void searchWithNoMatchingCards() {
        harness.addToBattlefield(player1, new FloodPlain());
        harness.setLibrary(player1, List.of(new Swamp(), new Mountain(), new FemerefScouts()));
        harness.activateAbility(player1, 0, null, null);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Flood Plain");
        harness.assertInGraveyard(player1, "Flood Plain");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Chosen land enters the battlefield untapped")
    void chosenLandEntersUntapped() {
        activateSearch();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Plains") && !p.isTapped());
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

    @Test
    @DisplayName("Flood Plain is sacrificed as a cost before the search resolves")
    void sacrificedBeforeResolution() {
        activateSearch();

        harness.assertNotOnBattlefield(player1, "Flood Plain");
        harness.assertInGraveyard(player1, "Flood Plain");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Plains");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An Island can be chosen and only that card leaves the library")
    void chosenIslandEntersUntapped() {
        harness.addToBattlefield(player1, new FloodPlain());
        Island island = new Island();
        Plains plains = new Plains();
        harness.setLibrary(player1, List.of(island, plains));
        harness.activateAbility(player1, 0, null, null);

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        Permanent found = findPermanent(player1, "Island");
        assertThat(found.getCard()).isSameAs(island);
        assertThat(found.isTapped()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(plains);
        harness.assertNotOnBattlefield(player2, "Island");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Searching an empty library still sacrifices Flood Plain and finishes resolving")
    void searchEmptyLibrary() {
        harness.addToBattlefield(player1, new FloodPlain());
        harness.setLibrary(player1, List.of());
        harness.activateAbility(player1, 0, null, null);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Flood Plain");
        harness.assertInGraveyard(player1, "Flood Plain");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void activateSearch() {
        harness.addToBattlefield(player1, new FloodPlain());
        setupLibrary();
        harness.activateAbility(player1, 0, null, null);
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(
                new Plains(), new Island(), new Swamp(), new Mountain(), new FemerefScouts()));
    }
}
