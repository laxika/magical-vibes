package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(AdaptiveShimmerer.class)
class AdaptiveShimmererTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with three +1/+1 counters, making it a 3/3")
    void entersWithThreeCounters() {
        harness.castFromHand(player1, new AdaptiveShimmerer(), "{5}");
        harness.passBothPriorities();

        Permanent shimmerer = findPermanent(player1, "Adaptive Shimmerer");
        assertThat(shimmerer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, shimmerer)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, shimmerer)).isEqualTo(3);
    }

    @Test
    @DisplayName("Flash allows casting during the opponent's upkeep")
    void canCastDuringOpponentsUpkeep() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);

        harness.castFromHand(player1, new AdaptiveShimmerer(), "{5}");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        Permanent shimmerer = findPermanent(player1, "Adaptive Shimmerer");
        assertThat(shimmerer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        harness.assertNotInGraveyard(player1, "Adaptive Shimmerer");
    }

    @Test
    @DisplayName("Enters with counters even when it was not cast")
    void entersWithCountersWithoutBeingCast() {
        Permanent shimmerer = harness.enterBattlefieldAndReturn(player1, new AdaptiveShimmerer());
        harness.runStateBasedActions();

        harness.assertOnBattlefield(player1, "Adaptive Shimmerer");
        assertThat(shimmerer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }
}
