package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Panopticon;
import com.github.laxika.magicalvibes.cards.s.SimicGrowthChamber;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.cards.s.SpatialMerging;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheFertileLandsOfSaulvinia.class, Forest.class, Panopticon.class,
        SimicGrowthChamber.class, SolRing.class, SpatialMerging.class, Card.class})
class TheFertileLandsOfSaulviniaTest extends BaseCardTest {

    private PlanechaseService planar;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.faceUp.add(new PlanarObject(
                new TheFertileLandsOfSaulvinia(), gd.nextTimestamp()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void doublesManaFromLandsTappedByEitherPlayer() {
        harness.addToBattlefield(player1, new Forest());
        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);

        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        harness.addToBattlefield(player2, new Forest());
        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    void chaosRevealsThroughPhenomenaTriggersTheFirstPlaneAndBottomsAllRevealedCards() {
        Card phenomenon = new Card();
        phenomenon.setName("Test phenomenon");
        phenomenon.setType(CardType.PHENOMENON);

        Card plane = new Panopticon();
        gd.planechase.deck.addAll(List.of(phenomenon, plane));
        harness.setLibrary(player1, List.of(new Forest()));
        int beforeHand = gd.playerHands.get(player1.getId()).size();

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards())
                .containsExactly(phenomenon, plane);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));
        harness.passBothPriorities();

        assertThat(gd.planechase.deck).containsExactly(plane, phenomenon);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(beforeHand + 1);
    }

    @Test
    void landProducingTwoTypesAddsOnlyOneExtraManaOfTheChosenType() {
        harness.addToBattlefield(player1, new SimicGrowthChamber());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tappingAnArtifactForManaDoesNotAddExtraMana() {
        harness.addToBattlefield(player1, new SolRing());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void chaosOnTheTopPlaneDoesNotPlaneswalkOrRevealLaterCards() {
        Card plane = new Panopticon();
        Card unrevealed = new SpatialMerging();
        gd.planechase.deck.addAll(List.of(plane, unrevealed));
        List<PlanarObject> faceUp = List.copyOf(gd.planechase.faceUp);
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        int beforeHand = gd.playerHands.get(player1.getId()).size();

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();

        assertThat(gd.planechase.deck).containsExactly(unrevealed, plane);
        assertThat(gd.planechase.faceUp).containsExactlyElementsOf(faceUp);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(beforeHand + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void revealingOnlyAPhenomenonDoesNotEncounterItAndReturnsItToTheDeck() {
        Card phenomenon = new SpatialMerging();
        gd.planechase.deck.add(phenomenon);
        List<PlanarObject> faceUp = List.copyOf(gd.planechase.faceUp);

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();

        assertThat(gd.planechase.deck).containsExactly(phenomenon);
        assertThat(gd.planechase.faceUp).containsExactlyElementsOf(faceUp);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
