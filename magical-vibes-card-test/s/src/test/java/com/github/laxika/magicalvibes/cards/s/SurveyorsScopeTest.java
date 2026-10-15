package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.o.OpalPalace;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SurveyorsScope.class, Forest.class, SakuraTribeElder.class, OpalPalace.class})
class SurveyorsScopeTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles itself and searches for up to one basic land when an opponent is two lands ahead")
    void searchesForBasicLandWhenOpponentIsTwoLandsAhead() {
        harness.addToBattlefield(player1, new SurveyorsScope());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.setLibrary(player1, List.of(new Forest(), new SakuraTribeElder()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard() instanceof SurveyorsScope);
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card() instanceof SurveyorsScope);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .singleElement()
                .matches(card -> card.hasType(CardType.LAND)
                        && card.getSupertypes().contains(CardSupertype.BASIC));
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard() instanceof Forest)
                .singleElement()
                .matches(permanent -> !permanent.isTapped());
    }

    @Test
    @DisplayName("Does not search when no opponent is at least two lands ahead")
    void doesNotSearchBelowTheTwoLandThreshold() {
        harness.addToBattlefield(player1, new SurveyorsScope());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard() instanceof SurveyorsScope)
                .filteredOn(p -> p.getCard() instanceof Forest)
                .hasSize(1);
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card() instanceof SurveyorsScope);
    }

    @Test
    void mayChooseNoLandEvenWhenABasicLandIsAvailable() {
        harness.addToBattlefield(player1, new SurveyorsScope());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(land));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card() instanceof SurveyorsScope);
    }

    @Test
    void landCountIsEvaluatedAtResolutionRatherThanActivation() {
        harness.addToBattlefield(player1, new SurveyorsScope());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(land));

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card() instanceof SurveyorsScope);
        harness.addToBattlefield(player1, new Forest());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    void onePlayerFarAheadStillAllowsOnlyOneLand() {
        harness.addToBattlefield(player1, new SurveyorsScope());
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player2, new Forest());
        }
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement()
                .matches(permanent -> permanent.getCard() instanceof Forest && !permanent.isTapped());
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    void noBasicLandInLibraryStillCompletesTheAbility() {
        harness.addToBattlefield(player1, new SurveyorsScope());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());
        SakuraTribeElder creature = new SakuraTribeElder();
        harness.setLibrary(player1, List.of(creature));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card() instanceof SurveyorsScope);
    }

    @Test
    void cannotActivateWhileTapped() {
        harness.addToBattlefieldAndReturn(player1, new SurveyorsScope()).tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Surveyor's Scope");
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void nonbasicLandsCountForTheThresholdButCannotBeFound() {
        harness.addToBattlefield(player1, new SurveyorsScope());
        harness.addToBattlefield(player2, new OpalPalace());
        harness.addToBattlefield(player2, new OpalPalace());
        Forest basicLand = new Forest();
        OpalPalace nonbasicLand = new OpalPalace();
        harness.setLibrary(player1, List.of(nonbasicLand, basicLand));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(basicLand);

        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonbasicLand);
    }

    @Test
    void twoQualifyingPlayersAllowTwoLandsWhichEnterTogether() {
        UUID thirdId = UUID.randomUUID();
        Player thirdPlayer = new Player(thirdId, "Charlie");
        gd.playerIds.add(thirdId);
        gd.orderedPlayerIds.add(thirdId);
        gd.playerNames.add("Charlie");
        gd.playerIdToName.put(thirdId, "Charlie");
        gd.playerDecks.put(thirdId, new ArrayList<>());
        gd.playerHands.put(thirdId, new ArrayList<>());
        gd.playerBattlefields.put(thirdId, new ArrayList<>());
        gd.playerGraveyards.put(thirdId, new ArrayList<>());
        gd.playerCommandZones.put(thirdId, new ArrayList<>());
        gd.playerManaPools.put(thirdId, new ManaPool());
        gd.playerLifeTotals.put(thirdId, 20);
        harness.getSessionManager().registerPlayer(new FakeConnection("conn-3"), thirdId, "Charlie");
        harness.addToBattlefield(player1, new SurveyorsScope());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(thirdPlayer, new Forest());
        harness.addToBattlefield(thirdPlayer, new Forest());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2)
                .allMatch(permanent -> permanent.getCard() instanceof Forest && !permanent.isTapped());
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }
}
