package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TalonOfPainTest extends BaseCardTest {

    @Test
    @DisplayName("Damage from another source puts a charge counter on Talon of Pain")
    void anotherSourceDamageAddsChargeCounter() {
        Permanent talon = harness.addToBattlefieldAndReturn(player1, new TalonOfPain());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(talon.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Talon of Pain's own damage does not trigger its charge-counter ability")
    void ownDamageDoesNotAddChargeCounter() {
        Permanent talon = harness.addToBattlefieldAndReturn(player1, new TalonOfPain());
        talon.setCounterCount(CounterType.CHARGE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, player2.getId());
        harness.passBothPriorities();

        assertThat(talon.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Removing X charge counters deals X damage to any target")
    void removesXChargeCountersAndDealsDamage() {
        Permanent talon = harness.addToBattlefieldAndReturn(player1, new TalonOfPain());
        talon.setCounterCount(CounterType.CHARGE, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 3, player2.getId());
        harness.passBothPriorities();

        assertThat(talon.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }
}
