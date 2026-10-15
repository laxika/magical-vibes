package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.ArmoryOfIroas;
import com.github.laxika.magicalvibes.cards.u.UnderworldCoinsmith;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SupplyLineCranes.class, UnderworldCoinsmith.class, ArmoryOfIroas.class})
class SupplyLineCranesTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts a +1/+1 counter on a target creature")
    void etbPutsCounterOnTargetCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new UnderworldCoinsmith());

        harness.castFromHand(player1, new SupplyLineCranes(), "{3}{W}{W}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bears.getEffectivePower()).isEqualTo(3);
        assertThat(bears.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("ETB can target an opponent's creature")
    void etbCanTargetOpponentCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new UnderworldCoinsmith());

        harness.castFromHand(player1, new SupplyLineCranes(), "{3}{W}{W}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot choose a non-creature as the ETB target")
    void cannotTargetNonCreature() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new ArmoryOfIroas());
        Permanent cranes = harness.enterBattlefieldAndReturn(player1, new SupplyLineCranes());

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, equipment.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, cranes.getId());
        harness.passBothPriorities();

        assertThat(equipment.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(cranes.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void canBeCastOntoEmptyBattlefieldAndTargetItself() {
        harness.castFromHand(player1, new SupplyLineCranes(), "{3}{W}{W}");
        harness.passBothPriorities();

        Permanent cranes = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(cranes.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.handlePermanentChosen(player1, cranes.getId());
        harness.passBothPriorities();

        assertThat(cranes.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void counterIsMandatoryWhenCreatureEntersWithoutBeingCast() {
        Permanent cranes = harness.enterBattlefieldAndReturn(player1, new SupplyLineCranes());

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, cranes.getId());
        harness.passBothPriorities();

        assertThat(cranes.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void triggerResolvesAfterSourceLeavesBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new UnderworldCoinsmith());
        Permanent cranes = harness.enterBattlefieldAndReturn(player1, new SupplyLineCranes());
        harness.handlePermanentChosen(player1, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(cranes);
        gd.playerGraveyards.get(player1.getId()).add(cranes.getCard());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void triggerDoesNotPlaceCounterWhenTargetLeavesBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new UnderworldCoinsmith());
        Permanent cranes = harness.enterBattlefieldAndReturn(player1, new SupplyLineCranes());
        harness.handlePermanentChosen(player1, target.getId());

        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(cranes.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
