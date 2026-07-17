package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IronclawOrcsTest extends BaseCardTest {

    private Permanent orcs() {
        Permanent orcs = new Permanent(new IronclawOrcs());
        gd.playerBattlefields.get(player1.getId()).add(orcs);
        return orcs;
    }

    @Test
    @DisplayName("Can block an attacker with power 1")
    void canBlockPowerOne() {
        Permanent orcs = orcs();
        Permanent wizard = new Permanent(new FugitiveWizard()); // 1/1
        gd.playerBattlefields.get(player2.getId()).add(wizard);

        assertThat(gqs.canBlockAttacker(gd, orcs, wizard,
                gd.playerBattlefields.get(player1.getId()))).isTrue();
    }

    @Test
    @DisplayName("Can't block an attacker with power 2")
    void cantBlockPowerTwo() {
        Permanent orcs = orcs();
        Permanent bears = new Permanent(new GrizzlyBears()); // 2/2
        gd.playerBattlefields.get(player2.getId()).add(bears);

        assertThat(gqs.canBlockAttacker(gd, orcs, bears,
                gd.playerBattlefields.get(player1.getId()))).isFalse();
    }

    @Test
    @DisplayName("Can't block an attacker with power greater than 2")
    void cantBlockHigherPower() {
        Permanent orcs = orcs();
        Permanent hillGiant = new Permanent(new HillGiant()); // 3/3
        gd.playerBattlefields.get(player2.getId()).add(hillGiant);

        assertThat(gqs.canBlockAttacker(gd, orcs, hillGiant,
                gd.playerBattlefields.get(player1.getId()))).isFalse();
    }
}
