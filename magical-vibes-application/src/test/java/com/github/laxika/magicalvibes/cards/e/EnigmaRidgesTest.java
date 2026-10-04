package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EnigmaRidges.class, Forest.class, GrizzlyBears.class, Island.class})
class EnigmaRidgesTest extends BaseCardTest {

    private PlanechaseService planar;
    private PlanarObject source;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        source = new PlanarObject(new EnigmaRidges(), gd.nextTimestamp());
        gd.planechase.faceUp.add(source);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
    }

    @Test
    void planeswalkToSearchesOnlyPlayersBelowTheGreatestLandCount() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());

        Card firstLand = new Forest();
        Card secondLand = new Island();
        Card nonland = new GrizzlyBears();
        harness.setLibrary(player1, List.of(firstLand, secondLand, nonland));
        harness.setLibrary(player2, List.of(new Forest()));

        triggerPlaneswalkTo();

        PendingInteraction.LibrarySearch search = activeSearch();
        assertThat(search.params().playerId()).isEqualTo(player1.getId());
        assertThat(search.params().remainingCount()).isEqualTo(2);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.HAND);
        assertThat(search.params().reveals()).isTrue();
        assertThat(search.params().cards()).containsExactly(firstLand, secondLand);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(activeSearch()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstLand, secondLand);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonland);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    void chaosDrawsThenMayPutALandFromHandOntoTheBattlefield() {
        Card drawn = new Island();
        Card land = new Forest();
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(creature, land));

        triggerChaos();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature, drawn);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .map(permanent -> permanent.getCard())).contains(land);
    }

    @Test
    void decliningChaosStillKeepsTheDrawnCardAndLeavesTheLandInHand() {
        Card drawn = new Island();
        Card land = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(land));

        triggerChaos();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land, drawn);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .map(permanent -> permanent.getCard())).doesNotContain(land);
    }

    @Test
    void tiedLandCountsDoNotSearch() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Island());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Island()));

        triggerPlaneswalkTo();

        assertThat(activeSearch()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    void playerMayStopSearchingBeforeReachingTheLandDifference() {
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());
        Card selected = new Forest();
        Card remaining = new Island();
        harness.setLibrary(player1, List.of(selected, remaining));

        triggerPlaneswalkTo();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(activeSearch()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(selected);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    void opponentWithFewerLandsSearchesTheirOwnLibrary() {
        harness.addToBattlefield(player1, new Forest());
        Card land = new Island();
        harness.setLibrary(player2, List.of(land));

        triggerPlaneswalkTo();

        assertThat(activeSearch().params().playerId()).isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);

        assertThat(activeSearch()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(land);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    void chaosCanPutTheLandItJustDrewOntoTheBattlefieldUntapped() {
        Card drawn = new Island();
        harness.setLibrary(player1, List.of(drawn));

        triggerChaos();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement().satisfies(permanent -> {
            assertThat(permanent.getCard()).isSameAs(drawn);
            assertThat(permanent.isTapped()).isFalse();
        });
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    private void triggerPlaneswalkTo() {
        harness.inMutationScope(() -> planar.trigger(
                gd, source, com.github.laxika.magicalvibes.model.EffectSlot.PLANESWALK_TO_TRIGGERED,
                player1.getId()));
        harness.passBothPriorities();
    }

    private void triggerChaos() {
        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();
    }

    private PendingInteraction.LibrarySearch activeSearch() {
        return gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
    }
}
