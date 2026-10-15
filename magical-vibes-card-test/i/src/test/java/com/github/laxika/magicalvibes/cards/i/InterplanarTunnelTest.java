package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.p.Panopticon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InterplanarTunnel.class, Panopticon.class})
class InterplanarTunnelTest extends BaseCardTest {

    private PlanechaseService planar;
    private InterplanarTunnel tunnel;
    private PlanarObject source;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        tunnel = new InterplanarTunnel();
        source = new PlanarObject(tunnel, gd.nextTimestamp());
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.faceUp.add(source);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void choosesOneOfFivePlanesAndPlaneswalksToIt() {
        List<Card> planes = List.of(
                new Panopticon(), new Panopticon(), new Panopticon(), new Panopticon(), new Panopticon());
        InterplanarTunnel revealedPhenomenon1 = new InterplanarTunnel();
        InterplanarTunnel revealedPhenomenon2 = new InterplanarTunnel();
        gd.planechase.deck.add(revealedPhenomenon1);
        gd.planechase.deck.add(revealedPhenomenon2);
        gd.planechase.deck.addAll(planes);

        harness.inMutationScope(() -> planar.trigger(gd, source, EffectSlot.ENCOUNTER_TRIGGERED,
                player1.getId()));
        harness.passBothPriorities();

        PendingInteraction.PlanarCardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PlanarCardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.revealedCards()).hasSize(7);
        assertThat(choice.validPlaneCardIds()).containsExactlyElementsOf(
                planes.stream().map(Card::getId).toList());

        Card chosen = planes.get(2);
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.planechase.faceUp).singleElement().extracting(PlanarObject::getCard)
                .isSameAs(chosen);
        assertThat(gd.planechase.deck).containsExactlyInAnyOrder(
                tunnel, revealedPhenomenon1, revealedPhenomenon2,
                planes.get(0), planes.get(1), planes.get(3), planes.get(4));
    }

    @Test
    void chosenPlaneGoesAboveUnrevealedCards() {
        List<Card> planes = List.of(
                new Panopticon(), new Panopticon(), new Panopticon(), new Panopticon(), new Panopticon());
        Card unrevealedPlane = new Panopticon();
        gd.planechase.deck.addAll(planes);
        gd.planechase.deck.add(unrevealedPlane);

        harness.inMutationScope(() -> planar.trigger(gd, source, EffectSlot.ENCOUNTER_TRIGGERED,
                player1.getId()));
        harness.passBothPriorities();

        PendingInteraction.PlanarCardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PlanarCardChoice.class);
        assertThat(choice.revealedCards()).containsExactlyElementsOf(planes);
        assertThat(gd.planechase.deck).containsExactly(unrevealedPlane);

        Card chosen = planes.get(2);
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.planechase.faceUp).singleElement().extracting(PlanarObject::getCard)
                .isSameAs(chosen);
        assertThat(gd.planechase.deck.getFirst()).isSameAs(unrevealedPlane);
        assertThat(gd.planechase.deck).containsExactlyInAnyOrder(
                unrevealedPlane, tunnel, planes.get(0), planes.get(1), planes.get(3), planes.get(4));
    }

    @Test
    void revealedPhenomenonCannotBeChosenAsTheNextPlane() {
        Card phenomenon = new InterplanarTunnel();
        List<Card> planes = List.of(
                new Panopticon(), new Panopticon(), new Panopticon(), new Panopticon(), new Panopticon());
        gd.planechase.deck.add(phenomenon);
        gd.planechase.deck.addAll(planes);

        harness.inMutationScope(() -> planar.trigger(gd, source, EffectSlot.ENCOUNTER_TRIGGERED,
                player1.getId()));
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(phenomenon.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.interaction.isAwaitingInput()).isTrue();

        harness.handleMultipleCardsChosen(player1, List.of(planes.getFirst().getId()));
        assertThat(gd.planechase.faceUp).singleElement().extracting(PlanarObject::getCard)
                .isSameAs(planes.getFirst());
        assertThat(gd.planechase.deck).contains(phenomenon);
    }

    @Test
    void exhaustedDeckWithOnePlaneUsesThatPlaneWithoutAChoice() {
        Card phenomenon = new InterplanarTunnel();
        Card plane = new Panopticon();
        gd.planechase.deck.add(phenomenon);
        gd.planechase.deck.add(plane);

        harness.inMutationScope(() -> planar.trigger(gd, source, EffectSlot.ENCOUNTER_TRIGGERED,
                player1.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.planechase.faceUp).singleElement().extracting(PlanarObject::getCard)
                .isSameAs(plane);
        assertThat(gd.planechase.deck).containsExactly(phenomenon, tunnel);
    }
}
