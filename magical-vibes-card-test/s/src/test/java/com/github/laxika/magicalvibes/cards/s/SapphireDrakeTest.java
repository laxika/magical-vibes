package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SapphireDrake.class, GrizzlyBears.class})
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

    @Test
    @DisplayName("Flying updates when +1/+1 counters are added and removed")
    void flyingUpdatesWithCounters() {
        harness.addToBattlefieldAndReturn(player1, new SapphireDrake());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Other counter types do not grant flying")
    void otherCountersDoNotGrantFlying() {
        harness.addToBattlefieldAndReturn(player1, new SapphireDrake());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Granted flying ends when Sapphire Drake leaves the battlefield")
    void flyingEndsWhenDrakeLeaves() {
        Permanent drake = harness.addToBattlefieldAndReturn(player1, new SapphireDrake());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();

        drake.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 4);
        harness.runStateBasedActions();

        harness.assertInGraveyard(player1, "Sapphire Drake");
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Flying follows the countered creature's current controller")
    void flyingUpdatesWhenCreatureChangesController() {
        harness.addToBattlefieldAndReturn(player1, new SapphireDrake());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(bears);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
    }
}
