package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CarnivorousPlant;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DairyCow.class, Forest.class, CarnivorousPlant.class})
class DairyCowTest extends BaseCardTest {

    @Test
    void entersWithFiveMilkCountersForEachForestAndPlantYouControl() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new CarnivorousPlant());
        harness.addToBattlefield(player2, new Forest());
        harness.castFromHand(player1, new DairyCow(), "{G}");
        harness.passBothPriorities();

        Permanent cow = findPermanent(player1, "Dairy Cow");
        assertThat(cow.getCounterCount(CounterType.MILK)).isEqualTo(10);
    }

    @Test
    void entersWithoutMilkCountersWhenOnlyOpponentControlsForestsAndPlants() {
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new CarnivorousPlant());

        harness.castFromHand(player1, new DairyCow(), "{G}");
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Dairy Cow").getCounterCount(CounterType.MILK)).isZero();
    }

    @Test
    void countsMultipleForestsWithoutAnyPlants() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());

        harness.castFromHand(player1, new DairyCow(), "{G}");
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Dairy Cow").getCounterCount(CounterType.MILK)).isEqualTo(10);
    }

    @Test
    void countsPlantsWithoutAnyForestsWhenEnteringWithoutBeingCast() {
        harness.addToBattlefield(player1, new CarnivorousPlant());
        harness.addToBattlefield(player1, new CarnivorousPlant());

        Permanent cow = harness.enterBattlefieldAndReturn(player1, new DairyCow());

        assertThat(cow.getCounterCount(CounterType.MILK)).isEqualTo(10);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void countsForestsAtEntryAndDoesNotRecalculateAfterward() {
        harness.castFromHand(player1, new DairyCow(), "{G}");
        harness.addToBattlefield(player1, new Forest());
        harness.passBothPriorities();

        Permanent cow = findPermanent(player1, "Dairy Cow");
        assertThat(cow.getCounterCount(CounterType.MILK)).isEqualTo(5);

        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new CarnivorousPlant());
        harness.runStateBasedActions();

        assertThat(cow.getCounterCount(CounterType.MILK)).isEqualTo(5);
    }
}
