package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NessianAspTest extends BaseCardTest {

    @Test
    @DisplayName("Monstrosity puts four +1/+1 counters on Nessian Asp")
    void monstrosityAddsCountersAndMarksItMonstrous() {
        Permanent asp = addReadyAsp();
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(asp.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(asp.isMonstrous()).isTrue();
    }

    @Test
    @DisplayName("Nessian Asp's monstrosity ability can resolve only once")
    void monstrosityOnlyResolvesOnce() {
        addReadyAsp();
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        addMonstrosityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already monstrous");
    }

    private Permanent addReadyAsp() {
        Permanent asp = harness.addToBattlefieldAndReturn(player1, new NessianAsp());
        asp.setSummoningSick(false);
        return asp;
    }

    private void addMonstrosityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }
}
