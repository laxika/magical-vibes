package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RenegadeWheelsmithTest extends BaseCardTest {

    @Test
    @DisplayName("When Renegade Wheelsmith becomes tapped, target creature can't block this turn")
    void tappedWheelsmithMakesTargetCreatureUnableToBlock() {
        Permanent wheelsmith = addCreatureReady(player1, new RenegadeWheelsmith());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        tapAndQueueTrigger(wheelsmith);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("The tapped trigger can target only creatures")
    void triggerRestrictsTargetsToCreatures() {
        Permanent wheelsmith = addCreatureReady(player1, new RenegadeWheelsmith());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());

        tapAndQueueTrigger(wheelsmith);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.EntersTriggerTarget.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(wheelsmith.getId(), creature.getId())
                .doesNotContain(land.getId());
    }

    @Test
    @DisplayName("The blocking restriction wears off at end of turn")
    void blockingRestrictionWearsOffAtEndOfTurn() {
        Permanent wheelsmith = addCreatureReady(player1, new RenegadeWheelsmith());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        tapAndQueueTrigger(wheelsmith);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Tapping another creature does not trigger Renegade Wheelsmith")
    void tappingAnotherCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new RenegadeWheelsmith());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());

        tapAndQueueTrigger(other);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void tapAndQueueTrigger(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent));
        harness.inMutationScope(() -> harness.getTriggerCollectionService().processNextEntersTriggerTarget(gd));
    }
}
