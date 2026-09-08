package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AdarkarWastes;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WildernessElementalTest extends BaseCardTest {

    @Test
    @DisplayName("Power equals the number of nonbasic lands opponents control and toughness remains 3")
    void powerCountsOpponentsNonbasicLands() {
        Permanent elemental = addElemental(player1);
        harness.addToBattlefield(player2, new AdarkarWastes());
        harness.addToBattlefield(player2, new AdarkarWastes());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player1, new AdarkarWastes());

        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(3);
    }

    @Test
    @DisplayName("Power updates as opponents' nonbasic lands enter and leave")
    void powerUpdatesDynamically() {
        Permanent elemental = addElemental(player1);
        Permanent firstLand = harness.addToBattlefieldAndReturn(player2, new AdarkarWastes());

        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(1);

        harness.addToBattlefield(player2, new AdarkarWastes());
        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(2);

        gd.playerBattlefields.get(player2.getId()).remove(firstLand);
        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(1);
    }

    @Test
    @DisplayName("Power is zero when opponents control no nonbasic lands")
    void powerIsZeroWithOnlyBasicLands() {
        Permanent elemental = addElemental(player1);
        harness.addToBattlefield(player2, new Forest());

        assertThat(gqs.getEffectivePower(gd, elemental)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(3);
    }

    private Permanent addElemental(Player player) {
        Permanent permanent = new Permanent(new WildernessElemental());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
