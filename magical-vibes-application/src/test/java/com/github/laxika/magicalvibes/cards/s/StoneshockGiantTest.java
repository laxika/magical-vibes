package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StoneshockGiantTest extends BaseCardTest {

    @Test
    @DisplayName("Monstrosity puts three +1/+1 counters on Stoneshock Giant")
    void monstrosityAddsCountersAndMarksItMonstrous() {
        Permanent giant = addReadyStoneshockGiant(player1);
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(giant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(giant.isMonstrous()).isTrue();
        assertThat(giant.getEffectivePower()).isEqualTo(8);
        assertThat(giant.getEffectiveToughness()).isEqualTo(7);
    }

    @Test
    @DisplayName("Becoming monstrous stops opponents' non-flying creatures from blocking this turn")
    void becomingMonstrousRestrictsOpponentsNonFliers() {
        Permanent giant = addReadyStoneshockGiant(player1);
        Permanent ownBears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());
        Permanent opponentAirElemental = addCreatureReady(player2, new AirElemental());
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(giant.isMonstrous()).isTrue();
        assertThat(ownBears.isCantBlockThisTurn()).isFalse();
        assertThat(opponentBears.isCantBlockThisTurn()).isTrue();
        assertThat(opponentAirElemental.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Monstrosity cannot be activated again after it resolves")
    void monstrosityOnlyResolvesOnce() {
        addReadyStoneshockGiant(player1);
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        addMonstrosityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already monstrous");
    }

    private Permanent addReadyStoneshockGiant(Player player) {
        Permanent giant = harness.addToBattlefieldAndReturn(player, new StoneshockGiant());
        giant.setSummoningSick(false);
        return giant;
    }

    private void addMonstrosityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.RED, 2);
    }
}
