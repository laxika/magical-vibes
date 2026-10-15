package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(StingingBarrier.class)
class StingingBarrierTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to target player")
    void dealsDamageToPlayer() {
        harness.setLife(player2, 20);
        Permanent barrier = addCreatureReady(player1, new StingingBarrier());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(barrier.isTapped()).isTrue();
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Deals 1 damage to target creature")
    void dealsDamageToCreature() {
        Permanent barrier = addCreatureReady(player1, new StingingBarrier());
        Permanent target = addCreatureReady(player2, new StingingBarrier());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(barrier.isTapped()).isTrue();
        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void canTargetItself() {
        Permanent barrier = addCreatureReady(player1, new StingingBarrier());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, barrier.getId());
        harness.passBothPriorities();

        assertThat(barrier.isTapped()).isTrue();
        assertThat(barrier.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new StingingBarrier());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sick");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhenTapped() {
        Permanent barrier = addCreatureReady(player1, new StingingBarrier());
        barrier.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotPayBlueCostWithColorlessMana() {
        Permanent barrier = addCreatureReady(player1, new StingingBarrier());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(barrier.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void abilityStillResolvesAfterSourceLeavesBattlefield() {
        Permanent barrier = addCreatureReady(player1, new StingingBarrier());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, player2.getId());

        gd.playerBattlefields.get(player1.getId()).remove(barrier);
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    void abilityDoesNotResolveWhenTargetLeavesBattlefield() {
        Permanent barrier = addCreatureReady(player1, new StingingBarrier());
        Permanent target = addCreatureReady(player2, new StingingBarrier());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, target.getId());

        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(barrier.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
