package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.h.HomaridWarrior;
import com.github.laxika.magicalvibes.cards.i.IcatianInfantry;
import com.github.laxika.magicalvibes.cards.i.IcatianPhalanx;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BrassclawOrcs.class, IcatianInfantry.class, IcatianPhalanx.class, HomaridWarrior.class})
class BrassclawOrcsTest extends BaseCardTest {

    private Permanent orcs() {
        return addCreatureReady(player1, new BrassclawOrcs());
    }

    @Test
    @DisplayName("Can block an attacker with power 1")
    void canBlockPowerOne() {
        Permanent orcs = orcs();
        Permanent infantry = addCreatureReady(player2, new IcatianInfantry());

        assertThat(bls.canBlockAttacker(gd, orcs, infantry,
                gd.playerBattlefields.get(player1.getId()))).isTrue();
    }

    @Test
    @DisplayName("Can't block an attacker with power 2")
    void cantBlockPowerTwo() {
        Permanent orcs = orcs();
        Permanent phalanx = addCreatureReady(player2, new IcatianPhalanx());

        assertThat(bls.canBlockAttacker(gd, orcs, phalanx,
                gd.playerBattlefields.get(player1.getId()))).isFalse();
    }

    @Test
    @DisplayName("Can't block an attacker with power greater than 2")
    void cantBlockHigherPower() {
        Permanent orcs = orcs();
        Permanent homaridWarrior = addCreatureReady(player2, new HomaridWarrior());

        assertThat(bls.canBlockAttacker(gd, orcs, homaridWarrior,
                gd.playerBattlefields.get(player1.getId()))).isFalse();
    }

    @Test
    @DisplayName("Can't block a power 1 attacker after it reaches effective power 2")
    void cantBlockEffectivePowerTwo() {
        Permanent orcs = orcs();
        Permanent infantry = addCreatureReady(player2, new IcatianInfantry());
        infantry.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(bls.canBlockAttacker(gd, orcs, infantry,
                gd.playerBattlefields.get(player1.getId()))).isFalse();
    }
}
