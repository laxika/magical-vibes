package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SelflessPoliceCaptain.class})
class SelflessPoliceCaptainTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a +1/+1 counter")
    void entersWithCounter() {
        Permanent captain = castCaptain();

        assertThat(captain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("When it leaves, puts its +1/+1 counters on a creature you control")
    void leavingTransfersPlusOneCountersToControlledCreature() {
        Permanent recipient = addCreatureReady(player1, new SelflessPoliceCaptain());
        Permanent captain = addCreatureReady(player1, new SelflessPoliceCaptain());
        captain.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        captain.setCounterCount(CounterType.CHARGE, 3);

        removeCaptain(captain);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(recipient.getId());

        harness.handlePermanentChosen(player1, recipient.getId());
        harness.passBothPriorities();

        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(recipient.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    @DisplayName("The leaves trigger still targets a creature when it has no +1/+1 counters")
    void leavesTriggerWithNoCounters() {
        Permanent recipient = addCreatureReady(player1, new SelflessPoliceCaptain());
        Permanent captain = addCreatureReady(player1, new SelflessPoliceCaptain());
        captain.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        removeCaptain(captain);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, recipient.getId());
        harness.passBothPriorities();

        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @ParameterizedTest
    @ValueSource(strings = {"hand", "exile"})
    void transfersCountersWhenLeavingWithoutDying(String destination) {
        Permanent recipient = addCreatureReady(player1, new SelflessPoliceCaptain());
        recipient.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent captain = castCaptain();
        captain.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        harness.inMutationScope(() -> {
            if (destination.equals("hand")) {
                harness.getPermanentRemovalService().removePermanentToHand(gd, captain);
            } else {
                harness.getPermanentRemovalService().removePermanentToExile(gd, captain);
            }
        });
        prepareLeavesTrigger();
        harness.handlePermanentChosen(player1, recipient.getId());
        harness.passBothPriorities();

        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(captain);
    }

    @Test
    void excludesOpponentsCreaturesFromTargets() {
        Permanent recipient = addCreatureReady(player1, new SelflessPoliceCaptain());
        Permanent opponent = addCreatureReady(player2, new SelflessPoliceCaptain());
        Permanent captain = castCaptain();

        removeCaptain(captain);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(recipient.getId());
        harness.handlePermanentChosen(player1, recipient.getId());
        harness.passBothPriorities();

        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void usesCounterCountFromDepartureEvenAfterSourceSnapshotChanges() {
        Permanent recipient = addCreatureReady(player1, new SelflessPoliceCaptain());
        Permanent captain = castCaptain();
        captain.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        removeCaptain(captain);
        harness.handlePermanentChosen(player1, recipient.getId());
        captain.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.passBothPriorities();

        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void noControlledCreatureMeansNoLegalTarget() {
        Permanent opponent = addCreatureReady(player2, new SelflessPoliceCaptain());
        Permanent captain = castCaptain();

        removeCaptain(captain);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void removeCaptain(Permanent captain) {
        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, captain));
        prepareLeavesTrigger();
    }

    private void prepareLeavesTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private Permanent castCaptain() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new SelflessPoliceCaptain(), "{1}{W}");
        harness.passBothPriorities();
        return findPermanents(player1, "Selfless Police Captain").getLast();
    }
}
