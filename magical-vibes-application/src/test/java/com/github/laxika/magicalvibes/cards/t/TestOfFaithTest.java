package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TestOfFaithTest extends BaseCardTest {

    @Test
    void preventsThreeDamageAndAddsThreePlusOneCounters() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TestOfFaith()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(bears.getMarkedDamage()).isZero();
    }

    @Test
    void preventsOnlyTheNextThreeDamage() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TestOfFaith()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new LightningBolt(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(bears.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void cannotTargetAPlayer() {
        harness.setHand(player1, List.of(new TestOfFaith()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
