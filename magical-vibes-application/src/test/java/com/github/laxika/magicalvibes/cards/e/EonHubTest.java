package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.ArmageddonClock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EonHub.class, ArmageddonClock.class})
class EonHubTest extends BaseCardTest {

    @Test
    @DisplayName("Players skip their upkeep steps")
    void playersSkipTheirUpkeepSteps() {
        harness.addToBattlefield(player1, new EonHub());
        Permanent clock = harness.addToBattlefieldAndReturn(player2, new ArmageddonClock());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.currentStep).isEqualTo(TurnStep.PRECOMBAT_MAIN);
        assertThat(clock.getCounterCount(CounterType.DOOM)).isZero();
    }
}
