package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.c.Colossapede;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HarvestSeason.class, Colossapede.class, Plains.class, Forest.class, Island.class})
class HarvestSeasonTest extends BaseCardTest {

    private PendingInteraction.LibrarySearch activeSearch() {
        return gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Plains(), new Forest(), new Island(), new Colossapede()));
    }

    private void castHarvestSeason() {
        harness.setHand(player1, List.of(new HarvestSeason()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castSorcery(player1, 0, 0);
    }

    @Test
    @DisplayName("X equals the number of tapped creatures you control and only basic lands are offered, to the battlefield tapped")
    void searchCountEqualsTappedCreatures() {
        harness.addToBattlefieldAndReturn(player1, new Colossapede()).tap();
        harness.addToBattlefieldAndReturn(player1, new Colossapede()).tap();
        setupLibrary();
        castHarvestSeason();

        harness.passBothPriorities();

        assertThat(activeSearch()).isNotNull();
        assertThat(activeSearch().params().remainingCount()).isEqualTo(2);
        assertThat(activeSearch().params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
        assertThat(activeSearch().params().cards())
                .allMatch(c -> c.hasType(CardType.LAND) && c.getSupertypes().contains(CardSupertype.BASIC));
    }

    @Test
    @DisplayName("Chosen basic lands enter the battlefield tapped")
    void chosenLandsEnterTapped() {
        harness.addToBattlefieldAndReturn(player1, new Colossapede()).tap();
        harness.addToBattlefieldAndReturn(player1, new Colossapede()).tap();
        setupLibrary();
        castHarvestSeason();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(activeSearch()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().hasType(CardType.LAND))
                .hasSize(2)
                .allMatch(com.github.laxika.magicalvibes.model.Permanent::isTapped);
    }

    @Test
    @DisplayName("With no tapped creatures X is zero, so no lands enter the battlefield")
    void noTappedCreaturesFindsNoLands() {
        harness.addToBattlefield(player1, new Colossapede());
        setupLibrary();
        castHarvestSeason();

        harness.passBothPriorities();
        // X is 0 (the only creature is untapped): "up to 0" still searches/shuffles but offers no choice.
        assertThat(activeSearch()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().hasType(CardType.LAND));
        harness.assertInGraveyard(player1, "Harvest Season");
    }

    @Test
    void countsOnlyControllersTappedCreatures() {
        harness.addToBattlefieldAndReturn(player1, new Colossapede()).tap();
        harness.addToBattlefield(player1, new Colossapede());
        harness.addToBattlefieldAndReturn(player1, new Forest()).tap();
        harness.addToBattlefieldAndReturn(player2, new Colossapede()).tap();
        setupLibrary();
        castHarvestSeason();

        harness.passBothPriorities();

        assertThat(activeSearch().params().remainingCount()).isEqualTo(1);
        harness.handleCardChosen(player1, 0);
        assertThat(activeSearch()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
    }

    @Test
    void evaluatesTappedCreatureCountAtResolution() {
        var creature = harness.addToBattlefieldAndReturn(player1, new Colossapede());
        setupLibrary();
        castHarvestSeason();
        creature.tap();

        harness.passBothPriorities();

        assertThat(activeSearch().params().remainingCount()).isEqualTo(1);
        harness.handleCardChosen(player1, 0);
        assertThat(activeSearch()).isNull();
        harness.assertOnBattlefield(player1, "Plains");
    }

    @Test
    void mayChooseZeroLandsEvenWhenBasicLandsAreAvailable() {
        harness.addToBattlefieldAndReturn(player1, new Colossapede()).tap();
        setupLibrary();
        castHarvestSeason();
        harness.passBothPriorities();

        harness.handleCardChosen(player1, -1);

        assertThat(activeSearch()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().hasType(CardType.LAND));
        harness.assertInGraveyard(player1, "Harvest Season");
    }

    @Test
    void mayStopAfterFindingFewerThanXLands() {
        harness.addToBattlefieldAndReturn(player1, new Colossapede()).tap();
        harness.addToBattlefieldAndReturn(player1, new Colossapede()).tap();
        setupLibrary();
        castHarvestSeason();
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().hasType(CardType.LAND));
        harness.handleCardChosen(player1, -1);

        assertThat(activeSearch()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().hasType(CardType.LAND))
                .hasSize(1)
                .allMatch(com.github.laxika.magicalvibes.model.Permanent::isTapped);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
    }

    @Test
    void selectedLandsWaitUntilSearchCompletesToEnterTogether() {
        harness.addToBattlefieldAndReturn(player1, new Colossapede()).tap();
        harness.addToBattlefieldAndReturn(player1, new Colossapede()).tap();
        setupLibrary();
        castHarvestSeason();
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().hasType(CardType.LAND));
        harness.handleCardChosen(player1, 0);
        assertThat(activeSearch()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().hasType(CardType.LAND))
                .hasSize(2)
                .allMatch(com.github.laxika.magicalvibes.model.Permanent::isTapped);
    }

    @Test
    void finishesWhenFewerThanXBasicLandsExist() {
        harness.addToBattlefieldAndReturn(player1, new Colossapede()).tap();
        harness.addToBattlefieldAndReturn(player1, new Colossapede()).tap();
        harness.setLibrary(player1, List.of(new Forest(), new Colossapede()));
        castHarvestSeason();
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        assertThat(activeSearch()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().hasType(CardType.LAND))
                .hasSize(1)
                .allMatch(com.github.laxika.magicalvibes.model.Permanent::isTapped);
        harness.assertInGraveyard(player1, "Harvest Season");
    }
}
