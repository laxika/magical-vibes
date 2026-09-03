package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.IronTuskElephant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HarbingerOfNight.class, IronTuskElephant.class, Forest.class})
class HarbingerOfNightTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger puts a -1/-1 counter on every creature, including itself and opponents'")
    void upkeepPutsCounterOnEachCreature() {
        Permanent harbinger = harness.addToBattlefieldAndReturn(player1, new HarbingerOfNight());
        Permanent ownElephant = harness.addToBattlefieldAndReturn(player1, new IronTuskElephant());
        Permanent enemyElephant = harness.addToBattlefieldAndReturn(player2, new IronTuskElephant());
        Permanent ownForest = harness.addToBattlefieldAndReturn(player1, new Forest());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(harbinger.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(ownElephant.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(enemyElephant.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(ownForest.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, enemyElephant)).isEqualTo(2);
    }

    @Test
    @DisplayName("A creature entering before resolution is included, but a land is not")
    void resolvesAgainstCreaturesCurrentlyOnTheBattlefield() {
        harness.addToBattlefield(player1, new HarbingerOfNight());

        advanceToUpkeep(player1);
        Permanent lateCreature = harness.addToBattlefieldAndReturn(player2, new IronTuskElephant());
        Permanent lateForest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.passBothPriorities();

        assertThat(lateCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(lateForest.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not trigger on the opponent's upkeep")
    void doesNotTriggerOnOpponentUpkeep() {
        harness.addToBattlefieldAndReturn(player1, new HarbingerOfNight());
        Permanent enemyElephant = harness.addToBattlefieldAndReturn(player2, new IronTuskElephant());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(enemyElephant.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }
}
