package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DrudgeSkeletons;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StrongholdTaskmasterTest extends BaseCardTest {

    @Test
    @DisplayName("Other black creatures get -1/-1")
    void debuffsOtherBlackCreatures() {
        harness.addToBattlefield(player1, new StrongholdTaskmaster());
        harness.addToBattlefield(player1, new DrudgeSkeletons());
        harness.addToBattlefield(player2, new DrudgeSkeletons());

        Permanent ownSkeletons = findPermanent(player1, "Drudge Skeletons");
        Permanent opponentSkeletons = findPermanent(player2, "Drudge Skeletons");

        assertThat(gqs.getEffectivePower(gd, ownSkeletons)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, ownSkeletons)).isEqualTo(0);
        assertThat(gqs.getEffectivePower(gd, opponentSkeletons)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, opponentSkeletons)).isEqualTo(0);
    }

    @Test
    @DisplayName("Does not affect itself or nonblack creatures")
    void doesNotAffectItselfOrNonblackCreatures() {
        harness.addToBattlefield(player1, new StrongholdTaskmaster());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent taskmaster = findPermanent(player1, "Stronghold Taskmaster");
        Permanent bears = findPermanent(player1, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, taskmaster)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, taskmaster)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("The debuff is removed when Stronghold Taskmaster leaves")
    void debuffRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new StrongholdTaskmaster());
        harness.addToBattlefield(player1, new DrudgeSkeletons());

        Permanent skeletons = findPermanent(player1, "Drudge Skeletons");

        assertThat(gqs.getEffectivePower(gd, skeletons)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, skeletons)).isEqualTo(0);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Stronghold Taskmaster"));

        assertThat(gqs.getEffectivePower(gd, skeletons)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, skeletons)).isEqualTo(1);
    }
}
