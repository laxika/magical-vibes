package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
}
