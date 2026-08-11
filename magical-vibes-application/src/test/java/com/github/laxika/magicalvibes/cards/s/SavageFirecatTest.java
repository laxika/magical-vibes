package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SavageFirecatTest extends BaseCardTest {

    @Test
    void entersWithSevenPlusOneCounters() {
        harness.setHand(player1, List.of(new SavageFirecat()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);
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
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(firecat.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
    }

    @Test
    void opponentTappingALandDoesNotRemoveCounter() {
        Permanent firecat = harness.addToBattlefieldAndReturn(player1, new SavageFirecat());
        firecat.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 7);
        harness.addToBattlefield(player2, new Forest());

        harness.tapPermanent(player2, 0);
        harness.passBothPriorities();

        assertThat(firecat.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(7);
    }
}
