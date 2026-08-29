package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RavenousLeucrocotaTest extends BaseCardTest {

    @Test
    @DisplayName("Monstrosity puts three +1/+1 counters on Ravenous Leucrocota")
    void monstrosityAddsCountersAndMarksItMonstrous() {
        Permanent leucrocota = addReadyLeucrocota();
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(leucrocota.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(leucrocota.isMonstrous()).isTrue();
        assertThat(leucrocota.getEffectivePower()).isEqualTo(5);
        assertThat(leucrocota.getEffectiveToughness()).isEqualTo(7);
    }

    @Test
    @DisplayName("Ravenous Leucrocota cannot activate monstrosity after becoming monstrous")
    void monstrosityOnlyResolvesOnce() {
        addReadyLeucrocota();
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        addMonstrosityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already monstrous");
    }

    private Permanent addReadyLeucrocota() {
        Permanent leucrocota = harness.addToBattlefieldAndReturn(player1, new RavenousLeucrocota());
        leucrocota.setSummoningSick(false);
        return leucrocota;
    }

    private void addMonstrosityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }
}
