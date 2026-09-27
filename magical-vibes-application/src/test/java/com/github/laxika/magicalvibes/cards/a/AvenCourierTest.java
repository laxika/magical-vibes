package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AvenCourier.class, GrizzlyBears.class})
class AvenCourierTest extends BaseCardTest {

    @Test
    void attackTriggerOnlyTargetsControlledPermanents() {
        addCreatureReady(player1, new AvenCourier());
        Permanent ownPermanent = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentPermanent = addCreatureReady(player2, new GrizzlyBears());
        ownPermanent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(ownPermanent.getId())
                .doesNotContain(opponentPermanent.getId());
    }

    @Test
    void choosesCounterOnControlledPermanentAndAddsItToTarget() {
        Permanent courier = addCreatureReady(player1, new AvenCourier());
        Permanent reference = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        reference.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "+1/+1 counters");

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(reference.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(courier.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotAddCounterIfTargetAlreadyHasChosenKind() {
        addCreatureReady(player1, new AvenCourier());
        Permanent reference = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        reference.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, reference.getId());
        harness.handleListChoice(player1, "+1/+1 counters");

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void asksWhichControlledPermanentToUseWhenSeveralHaveCounters() {
        addCreatureReady(player1, new AvenCourier());
        Permanent firstReference = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondReference = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        firstReference.setCounterCount(CounterType.CHARGE, 1);
        secondReference.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(firstReference.getId(), secondReference.getId());
        harness.handlePermanentChosen(player1, secondReference.getId());
        harness.handleListChoice(player1, "+1/+1 counters");

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.getCounterCount(CounterType.CHARGE)).isZero();
    }
}
