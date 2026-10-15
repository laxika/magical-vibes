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

    @Test
    @DisplayName("Kavu Scout updates its bonus as lands enter and leave, regardless of whether they are tapped")
    void updatesBonusWhenLandsEnterAndLeave() {
        Permanent scout = addCreatureReady(player1, new KavuScout());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        forest.tap();

        assertThat(gqs.getEffectivePower(gd, scout)).isEqualTo(1);

        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        assertThat(gqs.getEffectivePower(gd, scout)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(forest);
        assertThat(gqs.getEffectivePower(gd, scout)).isEqualTo(1);

        gd.playerBattlefields.get(player1.getId()).remove(island);
        assertThat(gqs.getEffectivePower(gd, scout)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, scout)).isEqualTo(2);
    }

    @Test
    @DisplayName("Each Kavu Scout uses its own controller's lands and boosts only itself")
    void eachScoutUsesItsOwnControllersLands() {
        Permanent firstScout = addCreatureReady(player1, new KavuScout());
        Permanent secondScout = addCreatureReady(player1, new KavuScout());
        Permanent opposingScout = addCreatureReady(player2, new KavuScout());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Swamp());

        assertThat(gqs.getEffectivePower(gd, firstScout)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, secondScout)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opposingScout)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, firstScout)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, secondScout)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingScout)).isEqualTo(2);
    }
}
