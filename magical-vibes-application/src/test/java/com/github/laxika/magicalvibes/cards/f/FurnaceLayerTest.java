package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
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

@CardUsed({FurnaceLayer.class, GrizzlyBears.class, Forest.class})
class FurnaceLayerTest extends BaseCardTest {

    private PlanechaseService planar;
    private PlanarObject source;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        source = new PlanarObject(new FurnaceLayer(), gd.nextTimestamp());
        gd.planechase.faceUp.add(source);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
    }

    @Test
    void planeswalkToFurnaceLayerMakesARandomPlayerDiscardAndLoseLifeForALand() {
        harness.setHand(player1, List.of(new Forest()));
        harness.setHand(player2, List.of(new Forest()));

        resolveRandomDiscard(EffectSlot.PLANESWALK_TO_TRIGGERED);

        assertThat(gd.getLife(player1.getId()) + gd.getLife(player2.getId())).isEqualTo(37);
    }

    @Test
    void upkeepDiscardDoesNotCauseLifeLossForANonland() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of(new GrizzlyBears()));

        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(StepTriggerService.class)
                .handleUpkeepTriggers(gd));
        harness.passBothPriorities();
        resolveRandomDiscardChoice();

        assertThat(gd.getLife(player1.getId()) + gd.getLife(player2.getId())).isEqualTo(40);
    }

    @Test
    void chaosMayDestroyTargetNonlandPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(TriggerCollectionService.class)
                .processNextSpellTargetTrigger(gd));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    private void resolveRandomDiscard(EffectSlot slot) {
        harness.inMutationScope(() -> planar.trigger(gd, source, slot, player1.getId()));
        harness.passBothPriorities();
        resolveRandomDiscardChoice();
    }

    private void resolveRandomDiscardChoice() {
        PendingInteraction.DiscardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        Player selectedPlayer = choice.playerId().equals(player1.getId()) ? player1 : player2;
        harness.handleCardChosen(selectedPlayer, 0);
        harness.passBothPriorities();
    }
}
