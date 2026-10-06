package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RecyclaBird.class, GrizzlyBears.class})
class RecyclaBirdTest extends BaseCardTest {

    @Test
    @DisplayName("When Recycla-bird dies, it puts a flying counter on a creature you control")
    void deathTriggerPutsFlyingCounterOnCreatureYouControl() {
        Permanent recyclaBird = harness.addToBattlefieldAndReturn(player1, new RecyclaBird());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, recyclaBird));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        assertThat(target.hasKeyword(com.github.laxika.magicalvibes.model.Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("The death trigger cannot target an opponent's creature")
    void deathTriggerCannotTargetOpponentsCreature() {
        Permanent recyclaBird = harness.addToBattlefieldAndReturn(player1, new RecyclaBird());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent legalTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, recyclaBird));
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, legalTarget.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.FLYING)).isZero();
        assertThat(legalTarget.getCounterCount(CounterType.FLYING)).isEqualTo(1);
    }

    @Test
    @DisplayName("The death trigger uses the dying creature's controller")
    void deathTriggerUsesDyingCreaturesController() {
        Permanent recyclaBird = harness.addToBattlefieldAndReturn(player2, new RecyclaBird());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, recyclaBird));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.FLYING)).isEqualTo(1);
    }

    @Test
    @DisplayName("The death trigger does nothing when its controller has no creatures left")
    void deathTriggerHasNoLegalTarget() {
        Permanent recyclaBird = harness.addToBattlefieldAndReturn(player1, new RecyclaBird());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, recyclaBird));
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(opponentCreature.getCounterCount(CounterType.FLYING)).isZero();
        harness.assertInGraveyard(player1, "Recycla-bird");
    }

    @Test
    @DisplayName("The death trigger does not put a counter on a target that has left the battlefield")
    void deathTriggerFizzlesWhenTargetLeaves() {
        Permanent recyclaBird = harness.addToBattlefieldAndReturn(player1, new RecyclaBird());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, recyclaBird));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, target));
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.FLYING)).isZero();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }
}
