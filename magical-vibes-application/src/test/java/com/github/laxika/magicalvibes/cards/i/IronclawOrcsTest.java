package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.MonssGoblinRaiders;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IronclawOrcs.class, MonssGoblinRaiders.class, GrizzlyBears.class, HillGiant.class})
class IronclawOrcsTest extends BaseCardTest {

    private Permanent orcs() {
        return addCreatureReady(player1, new IronclawOrcs());
    }

    @Test
    @DisplayName("Can block an attacker with power 1")
    void canBlockPowerOne() {
        Permanent orcs = orcs();
        Permanent goblin = addCreatureReady(player2, new MonssGoblinRaiders());

        assertThat(bls.canBlockAttacker(gd, orcs, goblin,
                gd.playerBattlefields.get(player1.getId()))).isTrue();
    }

    @Test
    @DisplayName("Can't block an attacker with power 2")
    void cantBlockPowerTwo() {
        Permanent orcs = orcs();
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        assertThat(bls.canBlockAttacker(gd, orcs, bears,
                gd.playerBattlefields.get(player1.getId()))).isFalse();
    }

    @Test
    @DisplayName("Can't block an attacker with power greater than 2")
    void cantBlockHigherPower() {
        Permanent orcs = orcs();
        Permanent hillGiant = addCreatureReady(player2, new HillGiant());

        assertThat(bls.canBlockAttacker(gd, orcs, hillGiant,
                gd.playerBattlefields.get(player1.getId()))).isFalse();
    }

    @Test
    void cantBlockEffectivePowerTwo() {
        Permanent orcs = orcs();
        Permanent goblin = addCreatureReady(player2, new MonssGoblinRaiders());
        goblin.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(bls.canBlockAttacker(gd, orcs, goblin,
                gd.playerBattlefields.get(player1.getId()))).isFalse();
    }
}
