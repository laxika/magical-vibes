package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AncestralVision;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({TalonGates.class, AncestralVision.class, GrizzlyBears.class})
class TalonGatesTest extends BaseCardTest {

    private PlanechaseService planar;
    private PlanarObject source;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        source = new PlanarObject(new TalonGates(), gd.nextTimestamp());
        gd.planechase.faceUp.add(source);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void exilesChosenNonlandCardWithItsManaValueAsTimeCounters() {
        Card card = new GrizzlyBears();
        harness.setHand(player1, List.of(card));

        harness.getGameService().activatePlanarAbility(
                gd, player1, source.getId(), 0, null, null, null);

        PendingInteraction.PlanarAbilityHandCardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PlanarAbilityHandCardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).containsExactly(0);

        harness.handleCardChosen(player1, 0);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(card);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());

        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 2);
    }

    @Test
    void chaosRemovesTwoTimeCountersOnlyFromCardsIOwn() {
        AncestralVision own = suspendedCard(player1, 4);
        AncestralVision opponent = suspendedCard(player2, 4);

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).containsEntry(own.getId(), 2)
                .containsEntry(opponent.getId(), 4);
    }

    private AncestralVision suspendedCard(com.github.laxika.magicalvibes.model.Player owner,
                                          int timeCounters) {
        AncestralVision card = new AncestralVision();
        harness.setExile(owner, List.of(card));
        gd.exiledCardTimeCounters.put(card.getId(), timeCounters);
        return card;
    }
}
