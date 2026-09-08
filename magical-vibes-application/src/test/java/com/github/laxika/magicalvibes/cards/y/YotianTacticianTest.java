package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.a.AvenCloudchaser;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class YotianTacticianTest extends BaseCardTest {

    @Test
    @DisplayName("Other Soldiers you control get +1/+1")
    void buffsOtherSoldiersYouControl() {
        harness.addToBattlefield(player1, new AvenCloudchaser());

        Permanent soldier = findPermanent(player1, "Aven Cloudchaser");
        int basePower = gqs.getEffectivePower(gd, soldier);
        int baseToughness = gqs.getEffectiveToughness(gd, soldier);

        harness.addToBattlefield(player1, new YotianTactician());

        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(baseToughness + 1);
    }

    @Test
    @DisplayName("Yotian Tactician does not buff itself")
    void doesNotBuffItself() {
        YotianTactician card = new YotianTactician();
        card.setPower(10);
        card.setToughness(10);
        harness.addToBattlefield(player1, card);

        Permanent tactician = findPermanent(player1, "Yotian Tactician");

        assertThat(gqs.getEffectivePower(gd, tactician)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, tactician)).isEqualTo(10);
    }

    @Test
    @DisplayName("Does not buff non-Soldier creatures")
    void doesNotBuffNonSoldiers() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        int basePower = gqs.getEffectivePower(gd, bears);
        int baseToughness = gqs.getEffectiveToughness(gd, bears);

        harness.addToBattlefield(player1, new YotianTactician());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("Does not buff an opponent's Soldiers")
    void doesNotBuffOpponentSoldiers() {
        harness.addToBattlefield(player2, new AvenCloudchaser());

        Permanent opponentSoldier = findPermanent(player2, "Aven Cloudchaser");
        int basePower = gqs.getEffectivePower(gd, opponentSoldier);
        int baseToughness = gqs.getEffectiveToughness(gd, opponentSoldier);

        harness.addToBattlefield(player1, new YotianTactician());

        assertThat(gqs.getEffectivePower(gd, opponentSoldier)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, opponentSoldier)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("Two Yotian Tacticians stack their bonuses")
    void bonusesStack() {
        harness.addToBattlefield(player1, new AvenCloudchaser());

        Permanent soldier = findPermanent(player1, "Aven Cloudchaser");
        int basePower = gqs.getEffectivePower(gd, soldier);
        int baseToughness = gqs.getEffectiveToughness(gd, soldier);

        harness.addToBattlefield(player1, new YotianTactician());
        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(baseToughness + 1);

        harness.addToBattlefield(player1, new YotianTactician());

        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(baseToughness + 2);
    }
}
