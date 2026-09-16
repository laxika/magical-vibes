package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SavageFirecat.class, Forest.class})
class SavageFirecatTest extends BaseCardTest {

    @Test
    void entersWithSevenPlusOneCounters() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new SavageFirecat(), "{3}{R}{R}");

        harness.passBothPriorities();

        Permanent firecat = findPermanent(player1, "Savage Firecat");
        assertThat(firecat.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(7);
    }

    @Test
    void controllerTappingALandRemovesOneCounter() {
        Permanent firecat = harness.addToBattlefieldAndReturn(player1, new SavageFirecat());
        firecat.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 7);
        harness.addToBattlefield(player1, new Forest());

        harness.tapPermanent(player1, 1);
        resolveAllTriggers();

        assertThat(firecat.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
    }

    @Test
    void eachControllerLandTapRemovesOneCounter() {
        Permanent firecat = harness.addToBattlefieldAndReturn(player1, new SavageFirecat());
        firecat.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 7);
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());

        harness.tapPermanent(player1, 1);
        resolveAllTriggers();
        harness.tapPermanent(player1, 2);
        resolveAllTriggers();

        assertThat(firecat.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    void opponentTappingALandDoesNotRemoveCounter() {
        Permanent firecat = harness.addToBattlefieldAndReturn(player1, new SavageFirecat());
        firecat.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 7);
        harness.addToBattlefield(player2, new Forest());

        harness.tapPermanent(player2, 0);
        resolveAllTriggers();

        assertThat(firecat.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(7);
    }
}
