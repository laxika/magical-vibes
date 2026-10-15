package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.i.IrontreadCrusher;
import com.github.laxika.magicalvibes.cards.w.WelderAutomaton;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RenegadeWheelsmith.class, WelderAutomaton.class, IrontreadCrusher.class})
class RenegadeWheelsmithTest extends BaseCardTest {

    @Test
    @DisplayName("When Renegade Wheelsmith becomes tapped, target creature can't block this turn")
    void tappedWheelsmithMakesTargetCreatureUnableToBlock() {
        Permanent wheelsmith = addCreatureReady(player1, new RenegadeWheelsmith());
        Permanent target = addCreatureReady(player2, new WelderAutomaton());

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
        Permanent creature = addCreatureReady(player2, new WelderAutomaton());
        Permanent vehicle = harness.addToBattlefieldAndReturn(player2, new IrontreadCrusher());

        tapAndQueueTrigger(wheelsmith);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.EntersTriggerTarget.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(wheelsmith.getId(), creature.getId())
                .doesNotContain(vehicle.getId());
    }

    @Test
    @DisplayName("The blocking restriction wears off at end of turn")
    void blockingRestrictionWearsOffAtEndOfTurn() {
        Permanent wheelsmith = addCreatureReady(player1, new RenegadeWheelsmith());
        Permanent target = addCreatureReady(player2, new WelderAutomaton());

        tapAndQueueTrigger(wheelsmith);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.UPKEEP);

        assertThat(target.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Tapping another creature does not trigger Renegade Wheelsmith")
    void tappingAnotherCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new RenegadeWheelsmith());
        Permanent other = addCreatureReady(player1, new WelderAutomaton());

        tapAndQueueTrigger(other);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void attackingTriggersTheBlockingRestriction() {
        addCreatureReady(player1, new RenegadeWheelsmith());
        Permanent target = addCreatureReady(player2, new WelderAutomaton());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isCantBlockThisTurn()).isTrue();
    }

    @Test
    void crewingWhileSummoningSickTriggersTheBlockingRestriction() {
        harness.addToBattlefield(player1, new IrontreadCrusher());
        Permanent wheelsmith = harness.addToBattlefieldAndReturn(player1, new RenegadeWheelsmith());
        Permanent target = addCreatureReady(player2, new WelderAutomaton());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(wheelsmith.isTapped()).isTrue();
        assertThat(target.isCantBlockThisTurn()).isTrue();
    }

    @Test
    void eachNewTapCanAffectADifferentCreature() {
        Permanent wheelsmith = addCreatureReady(player1, new RenegadeWheelsmith());
        Permanent first = addCreatureReady(player2, new WelderAutomaton());
        Permanent second = addCreatureReady(player2, new WelderAutomaton());

        tapAndQueueTrigger(wheelsmith);
        harness.handlePermanentChosen(player1, first.getId());
        harness.passBothPriorities();
        wheelsmith.untap();
        tapAndQueueTrigger(wheelsmith);
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();

        assertThat(first.isCantBlockThisTurn()).isTrue();
        assertThat(second.isCantBlockThisTurn()).isTrue();
    }

    @Test
    void tappingOneWheelsmithDoesNotTriggerAnotherCopy() {
        Permanent first = addCreatureReady(player1, new RenegadeWheelsmith());
        addCreatureReady(player1, new RenegadeWheelsmith());
        Permanent target = addCreatureReady(player2, new WelderAutomaton());

        tapAndQueueTrigger(first);
        harness.handlePermanentChosen(player1, target.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.pendingInteractions).isEmpty();
    }

    @Test
    void abilityResolvesAfterWheelsmithLeavesTheBattlefield() {
        Permanent wheelsmith = addCreatureReady(player1, new RenegadeWheelsmith());
        Permanent target = addCreatureReady(player2, new WelderAutomaton());

        tapAndQueueTrigger(wheelsmith);
        harness.handlePermanentChosen(player1, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, wheelsmith));
        harness.passBothPriorities();

        assertThat(target.isCantBlockThisTurn()).isTrue();
    }

    @Test
    void abilityCanTargetWheelsmithItself() {
        Permanent wheelsmith = addCreatureReady(player1, new RenegadeWheelsmith());

        tapAndQueueTrigger(wheelsmith);
        harness.handlePermanentChosen(player1, wheelsmith.getId());
        harness.passBothPriorities();

        assertThat(wheelsmith.isCantBlockThisTurn()).isTrue();
    }

    private void tapAndQueueTrigger(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent));
        harness.inMutationScope(() -> harness.getTriggerCollectionService().processNextEntersTriggerTarget(gd));
    }
}
