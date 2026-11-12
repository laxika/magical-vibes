package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeylineOfSanctity;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.model.trigger.Trigger;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FurnaceLayer.class, GrizzlyBears.class, Forest.class, LeylineOfSanctity.class})
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

        resolveRandomDiscard(Triggers.planeswalkToThis());

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
        harness.beginTriggerChoices();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void randomPlayerIsTargetedBeforePlayersCanRespond() {
        harness.setHand(player1, List.of(new Forest()));
        harness.setHand(player2, List.of(new Forest()));

        harness.inMutationScope(() -> planar.trigger(gd, source,
                Triggers.planeswalkToThis(), player1.getId()));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isIn(player1.getId(), player2.getId());
    }

    @Test
    @DisplayName("\"Select target player at random\" picks only among legal targets: a hexproof player is never selected")
    void randomTargetIsALegalTarget() {
        harness.addToBattlefield(player2, new LeylineOfSanctity());

        for (int i = 0; i < 12; i++) {
            harness.inMutationScope(() -> planar.trigger(gd, source,
                    Triggers.planeswalkToThis(), player1.getId()));
        }

        // The player is a target (CR 115.1), chosen at random among the legal ones as the ability goes on the stack.
        assertThat(gd.stack).hasSize(12)
                .allSatisfy(entry -> assertThat(entry.getTargetId()).isEqualTo(player1.getId()));
    }

    @Test
    void landDiscardLosesLifeDuringTheOriginalResolution() {
        harness.setHand(player1, List.of(new Forest()));
        harness.setHand(player2, List.of(new Forest()));

        harness.inMutationScope(() -> planar.trigger(gd, source,
                Triggers.planeswalkToThis(), player1.getId()));
        harness.passBothPriorities();
        PendingInteraction.DiscardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        Player selectedPlayer = choice.playerId().equals(player1.getId()) ? player1 : player2;
        Player otherPlayer = selectedPlayer == player1 ? player2 : player1;
        harness.handleCardChosen(selectedPlayer, 0);

        harness.assertInGraveyard(selectedPlayer, "Forest");
        harness.assertLife(selectedPlayer, 17);
        harness.assertLife(otherPlayer, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void emptyHandsCauseNeitherDiscardNorLifeLoss() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        harness.inMutationScope(() -> planar.trigger(gd, source,
                Triggers.planeswalkToThis(), player1.getId()));
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void selectedPlayerChoosesWhichCardToDiscard() {
        harness.setHand(player1, List.of(new Forest(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new Forest(), new GrizzlyBears()));

        harness.inMutationScope(() -> planar.trigger(gd, source,
                Triggers.planeswalkToThis(), player1.getId()));
        harness.passBothPriorities();
        PendingInteraction.DiscardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        Player selectedPlayer = choice.playerId().equals(player1.getId()) ? player1 : player2;
        Player otherPlayer = selectedPlayer == player1 ? player2 : player1;
        harness.handleCardChosen(selectedPlayer, 1);
        harness.passBothPriorities();

        harness.assertInGraveyard(selectedPlayer, "Grizzly Bears");
        harness.assertInHand(selectedPlayer, "Forest");
        assertThat(gd.playerHands.get(selectedPlayer.getId())).hasSize(1);
        assertThat(gd.playerHands.get(otherPlayer.getId())).hasSize(2);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void chaosDestructionCanBeDeclined() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.beginTriggerChoices();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    private void resolveRandomDiscard(Trigger trigger) {
        harness.inMutationScope(() -> planar.trigger(gd, source, trigger, player1.getId()));
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
