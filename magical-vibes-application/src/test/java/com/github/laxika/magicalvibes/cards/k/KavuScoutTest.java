package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KavuScout.class, Forest.class, Island.class, Mountain.class, Plains.class, Swamp.class})
class KavuScoutTest extends BaseCardTest {

    @Test
    @DisplayName("Kavu Scout has no domain bonus without basic land types")
    void hasNoBonusWithoutBasicLandTypes() {
        Permanent scout = addCreatureReady(player1, new KavuScout());

        assertThat(gqs.getEffectivePower(gd, scout)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, scout)).isEqualTo(2);
    }

    @Test
    @DisplayName("Kavu Scout gets +1/+0 for each distinct basic land type its controller controls")
    void boostsPowerByDomainCount() {
        Permanent scout = addCreatureReady(player1, new KavuScout());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Plains());

        assertThat(gqs.getEffectivePower(gd, scout)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, scout)).isEqualTo(2);
    }

    @Test
    @DisplayName("Kavu Scout counts all five basic land types")
    void countsAllFiveBasicLandTypes() {
        Permanent scout = addCreatureReady(player1, new KavuScout());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Forest());

        assertThat(gqs.getEffectivePower(gd, scout)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, scout)).isEqualTo(2);
    }

    @Test
    @DisplayName("Kavu Scout counts distinct controller types only")
    void countsDistinctControllerTypesOnly() {
        Permanent scout = addCreatureReady(player1, new KavuScout());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Swamp());

        assertThat(gqs.getEffectivePower(gd, scout)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, scout)).isEqualTo(2);
    }
}
