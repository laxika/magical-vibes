package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KavuScoutTest extends BaseCardTest {

    @Test
    @DisplayName("Kavu Scout gets +1/+0 for each distinct basic land type its controller controls")
    void boostsPowerByDomainCount() {
        Permanent scout = addScoutReady(player1);
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Plains());

        assertThat(gqs.getEffectivePower(gd, scout)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, scout)).isEqualTo(2);
    }

    @Test
    @DisplayName("Kavu Scout counts distinct controller types only")
    void countsDistinctControllerTypesOnly() {
        Permanent scout = addScoutReady(player1);
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Swamp());

        assertThat(gqs.getEffectivePower(gd, scout)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, scout)).isEqualTo(2);
    }

    private Permanent addScoutReady(Player player) {
        Permanent permanent = new Permanent(new KavuScout());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
