package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HarbingerOfNightTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger puts a -1/-1 counter on every creature, including itself and opponents'")
    void upkeepPutsCounterOnEachCreature() {
        Permanent harbinger = harness.addToBattlefieldAndReturn(player1, new HarbingerOfNight());
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent enemyBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(harbinger.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(ownBears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(enemyBears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, enemyBears)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger on the opponent's upkeep")
    void doesNotTriggerOnOpponentUpkeep() {
        harness.addToBattlefieldAndReturn(player1, new HarbingerOfNight());
        Permanent enemyBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(enemyBears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }
}
