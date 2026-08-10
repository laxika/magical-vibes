package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SerumTankTest extends BaseCardTest {

    @Test
    void putsChargeCountersOnItselfAndForAnyArtifactEntering() {
        SerumTank tank = new SerumTank();
        harness.setHand(player1, List.of(tank));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        Permanent tankPermanent = findPermanent(player1, "Serum Tank");
        assertThat(tankPermanent.getCounterCount(CounterType.CHARGE)).isEqualTo(1);

        harness.setHand(player2, List.of(new Ornithopter()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castArtifact(player2, 0);
        resolveAllTriggers();

        assertThat(tankPermanent.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    void removesChargeCounterAndDrawsWithActivatedAbility() {
        Permanent tank = harness.addToBattlefieldAndReturn(player1, new SerumTank());
        tank.setCounterCount(CounterType.CHARGE, 1);
        Ornithopter drawnCard = new Ornithopter();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(tank.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(tank.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }
}
