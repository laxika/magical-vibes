package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CarnivorousPlant;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DairyCow.class, Forest.class, CarnivorousPlant.class})
class DairyCowTest extends BaseCardTest {

    @Test
    void entersWithFiveMilkCountersForEachForestAndPlantYouControl() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new CarnivorousPlant());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new DairyCow()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent cow = findPermanent(player1, "Dairy Cow");
        assertThat(cow.getCounterCount(CounterType.MILK)).isEqualTo(10);
    }
}
