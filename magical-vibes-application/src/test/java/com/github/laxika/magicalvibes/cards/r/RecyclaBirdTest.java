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

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, recyclaBird));
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
