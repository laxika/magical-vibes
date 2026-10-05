package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.BaronyVampire;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LegionLieutenant.class, BaronyVampire.class, GrizzlyBears.class})
class LegionLieutenantTest extends BaseCardTest {

    @Test
    void buffsOtherVampiresYouControl() {
        harness.addToBattlefield(player1, new LegionLieutenant());
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new BaronyVampire());

        assertThat(gqs.getEffectivePower(gd, vampire)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, vampire)).isEqualTo(3);
    }

    @Test
    void doesNotBuffItself() {
        Permanent lieutenant = harness.addToBattlefieldAndReturn(player1, new LegionLieutenant());

        assertThat(gqs.getEffectivePower(gd, lieutenant)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lieutenant)).isEqualTo(2);
    }

    @Test
    void doesNotBuffNonVampiresOrOpponentsVampires() {
        harness.addToBattlefield(player1, new LegionLieutenant());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentVampire = harness.addToBattlefieldAndReturn(player2, new BaronyVampire());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentVampire)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opponentVampire)).isEqualTo(2);
    }

    @Test
    void twoLieutenantsBuffEachOther() {
        harness.addToBattlefield(player1, new LegionLieutenant());
        harness.addToBattlefield(player1, new LegionLieutenant());

        for (Permanent lieutenant : findPermanents(player1, "Legion Lieutenant")) {
            assertThat(gqs.getEffectivePower(gd, lieutenant)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, lieutenant)).isEqualTo(3);
        }
    }

    @Test
    void bonusIsRemovedWhenLieutenantLeaves() {
        harness.addToBattlefield(player1, new LegionLieutenant());
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new BaronyVampire());
        assertThat(gqs.getEffectivePower(gd, vampire)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(permanent -> permanent.getCard().getName().equals("Legion Lieutenant"));

        assertThat(gqs.getEffectivePower(gd, vampire)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, vampire)).isEqualTo(2);
    }

    @Test
    void multipleBonusesUpdateAsLieutenantsEnterAndLeave() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new LegionLieutenant());
        harness.addToBattlefield(player1, new LegionLieutenant());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);

        Permanent third = harness.addToBattlefieldAndReturn(player1, new LegionLieutenant());

        for (Permanent lieutenant : findPermanents(player1, "Legion Lieutenant")) {
            assertThat(gqs.getEffectivePower(gd, lieutenant)).isEqualTo(4);
            assertThat(gqs.getEffectiveToughness(gd, lieutenant)).isEqualTo(4);
        }

        gd.playerBattlefields.get(player1.getId()).remove(third);

        for (Permanent lieutenant : findPermanents(player1, "Legion Lieutenant")) {
            assertThat(gqs.getEffectivePower(gd, lieutenant)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, lieutenant)).isEqualTo(3);
        }
    }
}
