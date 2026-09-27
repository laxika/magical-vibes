package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Panopticon;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InysHaen.class, Forest.class, GrizzlyBears.class, Shock.class, Panopticon.class})
class InysHaenTest extends BaseCardTest {

    private PlanechaseService planar;
    private PlanarObject source;
    private TriggerCollectionService triggers;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        triggers = GameTestEngineContext.get().getBean(TriggerCollectionService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.deck.add(new Panopticon());
        source = new PlanarObject(new InysHaen(), gd.nextTimestamp());
        gd.planechase.faceUp.add(source);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
    }

    @Test
    void planeswalkToAndUpkeepEachMillThreeCards() {
        harness.setLibrary(player1, List.of(
                new Shock(), new Shock(), new Shock(), new Shock(), new Shock(), new Shock()));

        harness.inMutationScope(() -> planar.trigger(
                gd, source, com.github.laxika.magicalvibes.model.EffectSlot.PLANESWALK_TO_TRIGGERED,
                player1.getId()));
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);

        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(StepTriggerService.class)
                .handleUpkeepTriggers(gd));
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(6);
    }

    @Test
    void planeswalkingAwayReturnsAllLandsFromEachGraveyardTapped() {
        Card ownLand = new Forest();
        Card ownNonland = new GrizzlyBears();
        Card opposingLand = new Forest();
        Card opposingNonland = new Shock();
        harness.setGraveyard(player1, List.of(ownLand, ownNonland));
        harness.setGraveyard(player2, List.of(opposingLand, opposingNonland));

        harness.inMutationScope(() -> planar.planeswalk(gd));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == ownLand && permanent.isTapped());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard() == opposingLand && permanent.isTapped());
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(ownNonland);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposingNonland);
    }

    @Test
    void chaosReturnsTargetNonlandCardFromYourGraveyardToYourHand() {
        Card land = new Forest();
        Card nonland = new GrizzlyBears();
        Card opposingCard = new Shock();
        harness.setGraveyard(player1, List.of(land, nonland));
        harness.setGraveyard(player2, List.of(opposingCard));

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.inMutationScope(() -> triggers.processNextSpellGraveyardTargetTrigger(gd));

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(nonland.getId());

        harness.handleMultipleCardsChosen(player1, List.of(nonland.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(nonland);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposingCard);
    }
}
