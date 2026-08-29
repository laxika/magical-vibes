package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.ArmageddonClock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EonHubTest extends BaseCardTest {

    @Test
    @DisplayName("Players skip their upkeep steps")
    void playersSkipTheirUpkeepSteps() {
        Permanent hub = harness.addToBattlefieldAndReturn(player1, new EonHub());
        hub.tap();
        Permanent clock = harness.addToBattlefieldAndReturn(player2, new ArmageddonClock());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.currentStep).isEqualTo(TurnStep.PRECOMBAT_MAIN);
        assertThat(clock.getCounterCount(CounterType.DOOM)).isZero();
    }
}
