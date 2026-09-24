package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.Arachnoid;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IonStorm.class, Arachnoid.class})
class IonStormTest extends BaseCardTest {

    @Test
    @DisplayName("Removes a +1/+1 counter and deals 2 damage to a player")
    void removesPlusOneCounterAndDealsDamage() {
        Permanent ionStorm = addIonStorm();
        Permanent counterHolder = harness.addToBattlefieldAndReturn(player1, new Arachnoid());
        counterHolder.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(ionStorm).isIn(gd.playerBattlefields.get(player1.getId()));
        assertThat(counterHolder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Removes a charge counter and deals 2 damage to a creature")
    void removesChargeCounterAndDealsDamage() {
        addIonStorm();
        Permanent counterHolder = harness.addToBattlefieldAndReturn(player1, new Arachnoid());
        counterHolder.setCounterCount(CounterType.CHARGE, 1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Arachnoid());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(counterHolder.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(target).isIn(gd.playerBattlefields.get(player2.getId()));
        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Can remove a charge counter from Ion Storm itself")
    void removesChargeCounterFromSourceEnchantment() {
        Permanent ionStorm = addIonStorm();
        ionStorm.setCounterCount(CounterType.CHARGE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(ionStorm.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Cannot use an unrelated counter to pay Ion Storm's ability")
    void rejectsUnrelatedCounterType() {
        addIonStorm();
        Permanent counterHolder = harness.addToBattlefieldAndReturn(player1, new Arachnoid());
        counterHolder.setCounterCount(CounterType.QUEST, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("counter");
        assertThat(counterHolder.getCounterCount(CounterType.QUEST)).isEqualTo(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    private Permanent addIonStorm() {
        return harness.addToBattlefieldAndReturn(player1, new IonStorm());
    }
}
