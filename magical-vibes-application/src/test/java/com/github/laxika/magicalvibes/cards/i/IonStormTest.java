package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IonStormTest extends BaseCardTest {

    @Test
    @DisplayName("Removes a +1/+1 counter and deals 2 damage to a player")
    void removesPlusOneCounterAndDealsDamage() {
        Permanent ionStorm = addIonStorm();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(ionStorm).isIn(gd.playerBattlefields.get(player1.getId()));
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Removes a charge counter and deals 2 damage to a creature")
    void removesChargeCounterAndDealsDamage() {
        addIonStorm();
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        artifact.setCounterCount(CounterType.CHARGE, 1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(artifact.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot use an unrelated counter to pay Ion Storm's ability")
    void rejectsUnrelatedCounterType() {
        addIonStorm();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.QUEST, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("counter");
        assertThat(bears.getCounterCount(CounterType.QUEST)).isEqualTo(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    private Permanent addIonStorm() {
        return harness.addToBattlefieldAndReturn(player1, new IonStorm());
    }
}
