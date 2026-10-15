package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.k.KorDuelist;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MarshFlats.class, Plains.class, Swamp.class, Forest.class, Island.class, Mountain.class, KorDuelist.class, MistveilPlains.class})
class MarshFlatsTest extends BaseCardTest {

    @Test
    @DisplayName("Search ability pays 1 life, sacrifices Marsh Flats, and presents only Plains or Swamp cards")
    void searchPresentsOnlyPlainsOrSwamp() {
        int lifeBefore = gd.getLife(player1.getId());
        activateSearch();

        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
        harness.assertNotOnBattlefield(player1, "Marsh Flats");
        harness.assertInGraveyard(player1, "Marsh Flats");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> c.getName().equals("Plains") || c.getName().equals("Swamp"))
                .anyMatch(c -> c.getName().equals("Plains"))
                .anyMatch(c -> c.getName().equals("Swamp"))
                .noneMatch(c -> c.getName().equals("Forest") || c.getName().equals("Island") || c.getName().equals("Mountain"));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().destination())
                .isEqualTo(LibrarySearchDestination.BATTLEFIELD);
    }

    @Test
    @DisplayName("Chosen Plains or Swamp enters the battlefield untapped")
    void chosenLandEntersUntapped() {
        activateSearch();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> (p.getCard().getName().equals("Plains") || p.getCard().getName().equals("Swamp"))
                        && !p.isTapped());
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
    void costsArePaidBeforeSearchResolves() {
        activateSearch();

        harness.assertLife(player1, 19);
        harness.assertInGraveyard(player1, "Marsh Flats");
        harness.assertNotOnBattlefield(player1, "Marsh Flats");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(6);
    }

    @Test
    void tappedSourceCannotActivate() {
        var source = harness.addToBattlefieldAndReturn(player1, new MarshFlats());
        source.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertLife(player1, 20);
        harness.assertOnBattlefield(player1, "Marsh Flats");
        harness.assertNotInGraveyard(player1, "Marsh Flats");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void chosenSwampLeavesOnlyControllersLibrary() {
        activateSearch();
        var swamp = gd.playerDecks.get(player1.getId()).get(1);
        var opponentsLand = new Plains();
        harness.setLibrary(player2, List.of(opponentsLand));

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(swamp.getId()) && !p.isTapped());
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5).doesNotContain(swamp);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentsLand);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void emptyLibraryResolves() {
        harness.addToBattlefield(player1, new MarshFlats());
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertInGraveyard(player1, "Marsh Flats");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void noMatchingCardsStayInLibrary() {
        harness.addToBattlefield(player1, new MarshFlats());
        var forest = new Forest();
        var creature = new KorDuelist();
        harness.setLibrary(player1, List.of(forest, creature));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(forest, creature);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed({MistveilPlains.class})
    void nonbasicPlainsCanBeFoundAndEntersTapped() {
        harness.addToBattlefield(player1, new MarshFlats());
        var land = new MistveilPlains();
        harness.setLibrary(player1, List.of(land));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(land.getId()) && p.isTapped());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void activateSearch() {
        harness.addToBattlefield(player1, new MarshFlats());
        setupLibrary();
        harness.activateAbility(player1, 0, null, null);
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Plains(), new Swamp(), new Forest(), new Island(), new Mountain(), new KorDuelist()));
    }
}
