package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.t.TheValeyard;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PathOfTheAnimist.class, Panopticon.class, Forest.class, Island.class, GrizzlyBears.class,
        TheValeyard.class})
class PathOfTheAnimistTest extends BaseCardTest {

    private PlanarObject startingPlane;

    @BeforeEach
    void preparePlanechase() {
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        startingPlane = new PlanarObject(new Panopticon(), gd.nextTimestamp());
        gd.planechase.faceUp.add(startingPlane);
        gd.planechase.deck.add(new Panopticon());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void searchesForUpToTwoBasicLandsTappedThenChaosEnsuesOnATie() {
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new GrizzlyBears()));
        cast();
        int handBeforeChaos = gd.playerHands.get(player1.getId()).size();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).hasSize(2)
                .allMatch(card -> card.hasType(CardType.LAND)
                        && card.getSupertypes().contains(CardSupertype.BASIC));

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().hasType(CardType.LAND))
                .filteredOn(Permanent::isTapped)
                .hasSize(2);

        harness.handleListChoice(player1, ChoiceContext.WillOfThePlaneswalkersChoice.PLANESWALK);
        harness.handleListChoice(player2, ChoiceContext.WillOfThePlaneswalkersChoice.CHAOS);
        harness.passBothPriorities();

        assertThat(gd.planechase.faceUp).containsExactly(startingPlane);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBeforeChaos + 1);
    }

    @Test
    void searchesForOnlyOneAvailableBasicLandAndPlaneswalksOnAMajority() {
        harness.setLibrary(player1, List.of(new Forest()));
        cast();

        harness.handleCardChosen(player1, 0);
        harness.handleListChoice(player1, ChoiceContext.WillOfThePlaneswalkersChoice.PLANESWALK);
        harness.handleListChoice(player2, ChoiceContext.WillOfThePlaneswalkersChoice.PLANESWALK);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().hasType(CardType.LAND))
                .filteredOn(Permanent::isTapped)
                .hasSize(1);
        assertThat(gd.planechase.faceUp).noneMatch(object -> object == startingPlane).hasSize(1);
    }

    @Test
    void canChooseNoLandsAndChaosEnsuesOnAChaosMajority() {
        Forest forest = new Forest();
        Island island = new Island();
        harness.setLibrary(player1, List.of(forest, island));
        cast();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(forest, island);
        int handBeforeChaos = gd.playerHands.get(player1.getId()).size();

        harness.handleListChoice(player1, ChoiceContext.WillOfThePlaneswalkersChoice.CHAOS);
        harness.handleListChoice(player2, ChoiceContext.WillOfThePlaneswalkersChoice.CHAOS);
        harness.passBothPriorities();

        assertThat(gd.planechase.faceUp).containsExactly(startingPlane);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBeforeChaos + 1);
        harness.assertInGraveyard(player1, "Path of the Animist");
    }

    @Test
    void canStopAfterOneLandEvenWhenAnotherBasicLandIsAvailable() {
        Forest forest = new Forest();
        Island island = new Island();
        harness.setLibrary(player1, List.of(forest, island));
        cast();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement()
                .satisfies(permanent -> {
                    assertThat(permanent.getCard()).isSameAs(forest);
                    assertThat(permanent.isTapped()).isTrue();
                });
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(island);

        harness.handleListChoice(player1, ChoiceContext.WillOfThePlaneswalkersChoice.PLANESWALK);
        harness.handleListChoice(player2, ChoiceContext.WillOfThePlaneswalkersChoice.PLANESWALK);

        assertThat(gd.planechase.faceUp).noneMatch(object -> object == startingPlane).hasSize(1);
        harness.assertInGraveyard(player1, "Path of the Animist");
    }

    @Test
    void stillVotesWhenTheLibraryContainsNoBasicLands() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));
        cast();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bears);

        harness.handleListChoice(player1, ChoiceContext.WillOfThePlaneswalkersChoice.CHAOS);
        harness.handleListChoice(player2, ChoiceContext.WillOfThePlaneswalkersChoice.CHAOS);
        harness.passBothPriorities();

        assertThat(gd.planechase.faceUp).containsExactly(startingPlane);
        assertThat(gd.playerHands.get(player1.getId())).contains(bears);
        harness.assertInGraveyard(player1, "Path of the Animist");
    }

    @Test
    void offersAdditionalVoteToPlayerControllingTheValeyard() {
        harness.addToBattlefield(player1, new TheValeyard());
        harness.setLibrary(player1, List.of(new Forest(), new Island()));
        cast();
        harness.handleCardChosen(player1, -1);

        harness.handleListChoice(player1, ChoiceContext.WillOfThePlaneswalkersChoice.PLANESWALK);

        PendingInteraction.ColorChoice additionalVote =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(additionalVote).isNotNull();
        assertThat(additionalVote.playerId()).isEqualTo(player1.getId());
    }

    private void cast() {
        harness.setHand(player1, List.of(new PathOfTheAnimist()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveSorcery(player1, 0, 0);
    }
}
