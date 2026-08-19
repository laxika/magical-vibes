package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.BaronyVampire;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LegionLieutenantTest extends BaseCardTest {

    @Test
    void buffsOtherVampiresYouControl() {
        harness.addToBattlefield(player1, new LegionLieutenant());
        harness.addToBattlefield(player1, new BaronyVampire());

        Permanent vampire = findPermanent(player1, "Barony Vampire");

        assertThat(gqs.getEffectivePower(gd, vampire)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, vampire)).isEqualTo(3);
    }

    @Test
    void doesNotBuffItself() {
        harness.addToBattlefield(player1, new LegionLieutenant());

        Permanent lieutenant = findPermanent(player1, "Legion Lieutenant");

        assertThat(gqs.getEffectivePower(gd, lieutenant)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lieutenant)).isEqualTo(2);
    }

    @Test
    void doesNotBuffNonVampiresOrOpponentsVampires() {
        harness.addToBattlefield(player1, new LegionLieutenant());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new BaronyVampire());

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        Permanent opponentVampire = findPermanent(player2, "Barony Vampire");

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
        harness.addToBattlefield(player1, new BaronyVampire());

        Permanent vampire = findPermanent(player1, "Barony Vampire");
        assertThat(gqs.getEffectivePower(gd, vampire)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(permanent -> permanent.getCard().getName().equals("Legion Lieutenant"));

        assertThat(gqs.getEffectivePower(gd, vampire)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, vampire)).isEqualTo(2);
    }
}
