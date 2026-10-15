package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.p.Panopticon;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
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

@CardUsed({RealityShaping.class, Forest.class, GrizzlyBears.class, LightningBolt.class, Panopticon.class, Pacifism.class})
class RealityShapingTest extends BaseCardTest {

    private PlanechaseService planar;

    @BeforeEach
    void preparePlanechase() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.deck.add(new Panopticon());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void startsWithPlanarControllerAndPutsChosenPermanentsInSequence() {
        GrizzlyBears bears = new GrizzlyBears();
        Forest forest = new Forest();
        RealityShaping realityShaping = new RealityShaping();
        harness.setHand(player1, List.of(bears));
        harness.setHand(player2, List.of(forest));
        gd.planechase.deck.addFirst(realityShaping);

        harness.inMutationScope(() -> planar.reveal(gd, true));
        harness.passBothPriorities();

        PendingInteraction.EachPlayerMayPutCardFromHandChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.EachPlayerMayPutCardFromHandChoice.class);
        assertThat(firstChoice.playerId()).isEqualTo(player1.getId());
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactly(bears.getId());

        harness.handleMultipleCardsChosen(player2, List.of(forest.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactly(bears.getId());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactly(forest.getId());
        assertThat(gd.planechase.faceUp).singleElement().extracting(PlanarObject::getCard)
                .isInstanceOf(Panopticon.class);
    }

    @Test
    void onlyPermanentCardsAreEligible() {
        LightningBolt bolt = new LightningBolt();
        Forest forest = new Forest();
        harness.setHand(player1, List.of(bolt));
        harness.setHand(player2, List.of(forest));
        gd.planechase.deck.addFirst(new RealityShaping());

        harness.inMutationScope(() -> planar.reveal(gd, true));
        harness.passBothPriorities();

        PendingInteraction.EachPlayerMayPutCardFromHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.EachPlayerMayPutCardFromHandChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validCardIds()).containsExactly(forest.getId());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bolt);
    }

    @Test
    void eachPlayerMayDeclineWithoutRepeatingTheProcess() {
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setHand(player1, List.of(first));
        harness.setHand(player2, List.of(second));
        gd.planechase.deck.addFirst(new RealityShaping());
        harness.inMutationScope(() -> planar.reveal(gd, true));
        harness.passBothPriorities();

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.handleMultipleCardsChosen(player2, List.of());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(second);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.planechase.faceUp).singleElement().extracting(PlanarObject::getCard)
                .isInstanceOf(Panopticon.class);
    }

    @Test
    void putsOnlyOneCardPerPlayerWithoutRepeating() {
        Forest chosen = new Forest();
        Forest remaining = new Forest();
        harness.setHand(player1, List.of(chosen, remaining));
        harness.setHand(player2, List.of());
        gd.planechase.deck.addFirst(new RealityShaping());
        harness.inMutationScope(() -> planar.reveal(gd, true));
        harness.passBothPriorities();

        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId()).containsExactly(chosen.getId());
        assertThat(gd.planechase.faceUp).singleElement().extracting(PlanarObject::getCard)
                .isInstanceOf(Panopticon.class);
    }

    @Test
    void planeswalksAwayWhenNeitherPlayerHasAnEligibleCard() {
        LightningBolt bolt = new LightningBolt();
        harness.setHand(player1, List.of(bolt));
        harness.setHand(player2, List.of());
        gd.planechase.deck.addFirst(new RealityShaping());
        harness.inMutationScope(() -> planar.reveal(gd, true));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bolt);
        assertThat(gd.planechase.faceUp).singleElement().extracting(PlanarObject::getCard)
                .isInstanceOf(Panopticon.class);
    }

    @Test
    void auraCanEnchantCreaturePutOntoBattlefieldByEarlierPlayer() {
        GrizzlyBears bears = new GrizzlyBears();
        Pacifism aura = new Pacifism();
        harness.setHand(player1, List.of(bears));
        harness.setHand(player2, List.of(aura));
        gd.planechase.deck.addFirst(new RealityShaping());
        harness.inMutationScope(() -> planar.reveal(gd, true));
        harness.passBothPriorities();

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.handleMultipleCardsChosen(player2, List.of(aura.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).singleElement()
                .satisfies(permanent -> {
                    assertThat(permanent.getCard().getId()).isEqualTo(aura.getId());
                    assertThat(permanent.getAttachedTo())
                            .isEqualTo(harness.getPermanentId(player1, "Grizzly Bears"));
                });
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    void auraStaysInHandWhenThereIsNothingLegalToEnchant() {
        Pacifism aura = new Pacifism();
        harness.setHand(player1, List.of(aura));
        harness.setHand(player2, List.of());
        gd.planechase.deck.addFirst(new RealityShaping());
        harness.inMutationScope(() -> planar.reveal(gd, true));
        harness.passBothPriorities();

        harness.handleMultipleCardsChosen(player1, List.of(aura.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(aura);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertNotInGraveyard(player1, "Pacifism");
        assertThat(gd.planechase.faceUp).singleElement().extracting(PlanarObject::getCard)
                .isInstanceOf(Panopticon.class);
    }
}
