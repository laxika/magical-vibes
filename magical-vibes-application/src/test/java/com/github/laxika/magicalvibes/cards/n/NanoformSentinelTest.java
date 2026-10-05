package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.i.Island;
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

@CardUsed({NanoformSentinel.class, Island.class})
class NanoformSentinelTest extends BaseCardTest {

    @Test
    @DisplayName("When it becomes tapped, untaps another target permanent")
    void untapsAnotherTargetPermanent() {
        Permanent sentinel = addCreatureReady(player1, new NanoformSentinel());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Island());
        target.tap();

        tapAndQueueTrigger(sentinel);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.EntersTriggerTarget.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(target.getId())
                .doesNotContain(sentinel.getId());

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(sentinel.isTapped()).isTrue();
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Triggers only once each turn")
    void triggersOnlyOnceEachTurn() {
        Permanent sentinel = addCreatureReady(player1, new NanoformSentinel());
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new Island());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player2, new Island());
        firstTarget.tap();
        secondTarget.tap();

        tapAndQueueTrigger(sentinel);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, firstTarget.getId());
        harness.passBothPriorities();

        sentinel.untap();
        tapAndQueueTrigger(sentinel);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(secondTarget.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping another permanent does not trigger it")
    void tappingAnotherPermanentDoesNotTrigger() {
        addCreatureReady(player1, new NanoformSentinel());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new Island());

        tapAndQueueTrigger(other);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void attackingTriggersTheUntapAbility() {
        Permanent sentinel = addCreatureReady(player1, new NanoformSentinel());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Island());
        target.tap();

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(sentinel.isTapped()).isTrue();
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void eachCopyCanTriggerInTheSameTurn() {
        Permanent first = addCreatureReady(player1, new NanoformSentinel());
        Permanent second = addCreatureReady(player1, new NanoformSentinel());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Island());
        target.tap();

        tapAndQueueTrigger(first);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        tapAndQueueTrigger(second);
        harness.handlePermanentChosen(player1, first.getId());
        harness.passBothPriorities();

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isTrue();
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void triggerResolvesAfterSourceLeavesBattlefield() {
        Permanent sentinel = addCreatureReady(player1, new NanoformSentinel());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Island());
        target.tap();

        tapAndQueueTrigger(sentinel);
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(sentinel);
        gd.playerGraveyards.get(player1.getId()).add(sentinel.getCard());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void canTargetAnUntappedPermanent() {
        Permanent sentinel = addCreatureReady(player1, new NanoformSentinel());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Island());

        tapAndQueueTrigger(sentinel);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(sentinel.isTapped()).isTrue();
    }

    @Test
    void triggersAgainOnTheOpponentsTurn() {
        Permanent sentinel = addCreatureReady(player1, new NanoformSentinel());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Island());
        target.tap();
        tapAndQueueTrigger(sentinel);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        sentinel.untap();
        target.tap();
        tapAndQueueTrigger(sentinel);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(sentinel.isTapped()).isTrue();
    }

    @Test
    void cannotTargetItselfWhenThereAreNoOtherPermanents() {
        Permanent sentinel = addCreatureReady(player1, new NanoformSentinel());

        tapAndQueueTrigger(sentinel);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(sentinel.isTapped()).isTrue();
    }

    @Test
    void doesNotRetargetWhenTheChosenPermanentLeaves() {
        Permanent sentinel = addCreatureReady(player1, new NanoformSentinel());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new Island());
        target.tap();
        other.tap();

        tapAndQueueTrigger(sentinel);
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerGraveyards.get(player1.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(other.isTapped()).isTrue();
        assertThat(sentinel.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    private void tapAndQueueTrigger(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(() -> {
            harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent);
            harness.getTriggerCollectionService().processNextEntersTriggerTarget(gd);
        });
    }
}
