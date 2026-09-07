package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.y.YouthfulKnight;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VenerableKnight.class, YouthfulKnight.class, GrizzlyBears.class})
class VenerableKnightTest extends BaseCardTest {

    @Test
    @DisplayName("Death trigger puts a +1/+1 counter on a Knight you control")
    void deathTriggerPutsCounterOnKnightYouControl() {
        Permanent venerableKnight = harness.addToBattlefieldAndReturn(player1, new VenerableKnight());
        Permanent targetKnight = harness.addToBattlefieldAndReturn(player1, new YouthfulKnight());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, venerableKnight));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetKnight.getId());
        harness.passBothPriorities();

        assertThat(targetKnight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Death trigger cannot target a non-Knight creature")
    void deathTriggerCannotTargetNonKnight() {
        Permanent venerableKnight = harness.addToBattlefieldAndReturn(player1, new VenerableKnight());
        Permanent targetKnight = harness.addToBattlefieldAndReturn(player1, new YouthfulKnight());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, venerableKnight));
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(targetKnight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Death trigger cannot target an opponent's Knight")
    void deathTriggerCannotTargetOpponentsKnight() {
        Permanent venerableKnight = harness.addToBattlefieldAndReturn(player1, new VenerableKnight());
        Permanent targetKnight = harness.addToBattlefieldAndReturn(player1, new YouthfulKnight());
        Permanent opponentsKnight = harness.addToBattlefieldAndReturn(player2, new YouthfulKnight());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, venerableKnight));
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentsKnight.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(targetKnight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
