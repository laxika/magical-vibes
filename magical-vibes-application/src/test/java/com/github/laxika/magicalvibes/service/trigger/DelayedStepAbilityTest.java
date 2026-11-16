package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DelayedReferents;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.StepTrigger;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A delayed triggered ability a resolving spell or ability made that waits for the beginning of a step (CR 603.7a) is
 * listed and matched by the trigger pipeline as any step ability, triggers only the next time its step begins
 * (CR 603.7b), and triggers even if the object it is about has left (CR 603.7c).
 */
@CardUsed({GrizzlyBears.class})
class DelayedStepAbilityTest extends BaseCardTest {

    private Card maker;

    @BeforeEach
    void setUpTurn() {
        maker = new GrizzlyBears();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("\"At the beginning of the next end step\" triggers once, controlled by its maker's controller")
    void nextEndStepTriggersOnceForItsController() {
        gd.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.atNextStep(
                Triggers.endStep(ActivePlayer.ANY), player2.getId(), maker, null, null, new GainLifeEffect(3)));
        int life = gd.getLife(player2.getId());

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);

        assertThat(gd.stack).singleElement().satisfies(entry -> {
            assertThat(entry.getControllerId()).isEqualTo(player2.getId());
            assertThat(entry.getCard().getName()).isEqualTo("Grizzly Bears");
        });
        assertThat(gd.delayedStepAbilities(StepTrigger.Moment.END_STEP)).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(life + 3);
    }

    @Test
    @DisplayName("\"At the beginning of your next end step\" waits for its controller's end step")
    void yourNextEndStepSkipsAnotherPlayersEndStep() {
        gd.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.atNextStep(
                Triggers.endStep(ActivePlayer.YOU), player2.getId(), maker, null, null, new GainLifeEffect(3)));

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.delayedStepAbilities(StepTrigger.Moment.END_STEP)).hasSize(1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        gs.advanceStep(gd);

        assertThat(gd.stack).singleElement()
                .satisfies(entry -> assertThat(entry.getControllerId()).isEqualTo(player2.getId()));
    }

    @Test
    @DisplayName("A delayed ability that waits past this turn doesn't trigger at this turn's end step")
    void waitsPastItsTurn() {
        gd.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.atNextStep(player1.getId(), maker,
                new TriggeredAbility(Triggers.endStep(ActivePlayer.ANY), new GainLifeEffect(3)), null, null, null,
                DelayedReferents.NONE.afterTurn(gd.turnNumber)));

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.delayedStepAbilities(StepTrigger.Moment.END_STEP)).hasSize(1);
    }

    @Test
    @DisplayName("\"At the beginning of the next cleanup step\" triggers in the cleanup step")
    void nextCleanupStepTriggers() {
        gd.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.atNextStep(
                Triggers.cleanup(), player1.getId(), maker, null, null, new GainLifeEffect(3)));

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        gs.advanceStep(gd);

        assertThat(gd.stack).singleElement()
                .satisfies(entry -> assertThat(entry.getControllerId()).isEqualTo(player1.getId()));
        assertThat(gd.delayedStepAbilities(StepTrigger.Moment.CLEANUP)).isEmpty();
    }

    @Test
    @DisplayName("A delayed ability about a permanent that left still triggers, naming that permanent")
    void triggersAfterItsPermanentLeft() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.atNextStep(
                Triggers.endStep(ActivePlayer.ANY), player1.getId(), maker, null, bears.getId(),
                new DestroyTargetPermanentEffect()));
        gd.playerBattlefields.get(player2.getId()).remove(bears);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);

        StackEntry entry = gd.stack.getFirst();
        assertThat(gd.stack).hasSize(1);
        assertThat(entry.getTargetId()).isEqualTo(bears.getId());
        assertThat(entry.isNonTargeting()).isTrue();
    }
}
