package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SapphireDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("Own creature with a +1/+1 counter gains flying")
    void counteredOwnCreatureGainsFlying() {
        harness.addToBattlefieldAndReturn(player1, new SapphireDrake());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Own creature without a +1/+1 counter does not gain flying")
    void uncounteredOwnCreatureDoesNotGainFlying() {
        harness.addToBattlefieldAndReturn(player1, new SapphireDrake());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Opponent's creature with a +1/+1 counter does not gain flying")
    void counteredOpponentCreatureDoesNotGainFlying() {
        harness.addToBattlefieldAndReturn(player1, new SapphireDrake());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
    }
}
