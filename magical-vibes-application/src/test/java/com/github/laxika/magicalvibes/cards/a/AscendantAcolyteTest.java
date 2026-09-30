package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AscendantAcolyte.class, GrizzlyBears.class})
class AscendantAcolyteTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with one +1/+1 counter for each +1/+1 counter on your other creatures")
    void entersWithCountersFromOtherControlledCreatures() {
        Permanent firstBear = addCreatureReady(player1, new GrizzlyBears());
        firstBear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent secondBear = addCreatureReady(player1, new GrizzlyBears());
        secondBear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent opposingBear = addCreatureReady(player2, new GrizzlyBears());
        opposingBear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5);

        harness.setHand(player1, List.of(new AscendantAcolyte()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent acolyte = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof AscendantAcolyte)
                .findFirst()
                .orElseThrow();
        assertThat(acolyte.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Doubles only its own +1/+1 counters during its controller's upkeep")
    void doublesOwnPlusOneCountersOnUpkeep() {
        Permanent acolyte = addCreatureReady(player1, new AscendantAcolyte());
        acolyte.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        acolyte.setCounterCount(CounterType.CHARGE, 2);
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        bear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(acolyte.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(acolyte.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

}
