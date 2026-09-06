package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NanoformSentinel.class, GrizzlyBears.class})
class NanoformSentinelTest extends BaseCardTest {

    @Test
    @DisplayName("When it becomes tapped, untaps another target permanent")
    void untapsAnotherTargetPermanent() {
        Permanent sentinel = addCreatureReady(player1, new NanoformSentinel());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
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
        Permanent firstTarget = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondTarget = addCreatureReady(player2, new GrizzlyBears());
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
        Permanent other = addCreatureReady(player1, new GrizzlyBears());

        tapAndQueueTrigger(other);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void tapAndQueueTrigger(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(() -> {
            harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent);
            harness.getTriggerCollectionService().processNextEntersTriggerTarget(gd);
        });
    }
}
