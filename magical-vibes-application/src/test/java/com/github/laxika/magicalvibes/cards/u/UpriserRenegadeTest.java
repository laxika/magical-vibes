package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UpriserRenegade.class, GrizzlyBears.class, Plains.class})
class UpriserRenegadeTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +2/+0 for each other modified creature you control")
    void getsPowerForEachOtherModifiedCreatureYouControl() {
        Permanent upriser = addCreatureReady(player1, new UpriserRenegade());
        Permanent modifiedBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondModifiedBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent unmodifiedBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent modifiedLand = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent opposingModifiedBear = addCreatureReady(player2, new GrizzlyBears());
        upriser.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        modifiedBear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        secondModifiedBear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        modifiedLand.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        opposingModifiedBear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.getEffectivePower(gd, upriser)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, unmodifiedBear)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not get a bonus without another modified creature you control")
    void doesNotGetBonusWithoutAnotherModifiedCreatureYouControl() {
        Permanent upriser = addCreatureReady(player1, new UpriserRenegade());
        upriser.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.getEffectivePower(gd, upriser)).isEqualTo(2);
    }
}
