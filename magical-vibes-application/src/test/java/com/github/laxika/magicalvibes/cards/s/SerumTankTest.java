package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AuriokSteelshaper;
import com.github.laxika.magicalvibes.cards.m.MycosynthLattice;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SerumTank.class, Ornithopter.class, AuriokSteelshaper.class})
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
    void doesNotPutChargeCounterOnItselfForNonArtifactEntering() {
        Permanent tank = harness.addToBattlefieldAndReturn(player1, new SerumTank());

        harness.enterBattlefieldAndReturn(player2, new AuriokSteelshaper());
        resolveAllTriggers();

        assertThat(tank.getCounterCount(CounterType.CHARGE)).isZero();
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

        assertThat(tank.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(tank.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    void cannotActivateWithoutAChargeCounter() {
        Permanent tank = harness.addToBattlefieldAndReturn(player1, new SerumTank());
        tank.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(tank.isTapped()).isFalse();
        assertThat(tank.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent tank = harness.addToBattlefieldAndReturn(player1, new SerumTank());
        tank.setCounterCount(CounterType.CHARGE, 1);
        tank.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(tank.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canDrawDuringOpponentsTurnAndRemovesOnlyOneCounter() {
        Permanent tank = harness.addToBattlefieldAndReturn(player1, new SerumTank());
        tank.setCounterCount(CounterType.CHARGE, 2);
        Ornithopter drawnCard = new Ornithopter();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(tank.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @CardUsed(MycosynthLattice.class)
    void countsPermanentsMadeArtifactsByMycosynthLattice() {
        Permanent tank = harness.addToBattlefieldAndReturn(player1, new SerumTank());
        harness.addToBattlefield(player1, new MycosynthLattice());

        harness.enterBattlefieldAndReturn(player2, new AuriokSteelshaper());
        resolveAllTriggers();

        assertThat(tank.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }
}
