package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.b.BriarberryCohort;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GodheadOfAwe.class, AirElemental.class, GrizzlyBears.class, BriarberryCohort.class})
class GodheadOfAweTest extends BaseCardTest {

    @Test
    @DisplayName("Two Godheads of Awe shrink each other, and the survivor recovers when one leaves")
    void twoGodheadsShrinkEachOther() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GodheadOfAwe());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GodheadOfAwe());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(1);

        gd.playerBattlefields.get(player2.getId()).remove(second);

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(4);
    }

    @Test
    @DisplayName("Static power and toughness bonuses apply after Godhead sets the base")
    void staticBonusAppliesAfterBaseSetter() {
        harness.addToBattlefield(player1, new GodheadOfAwe());
        Permanent cohort = harness.addToBattlefieldAndReturn(player1, new BriarberryCohort());

        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, cohort)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creatures entering after Godhead are also affected")
    void laterCreaturesBecomeOneOne() {
        harness.enterBattlefieldAndReturn(player1, new GodheadOfAwe());
        Permanent creature = harness.enterBattlefieldAndReturn(player2, new AirElemental());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }

    @Test
    @DisplayName("Other creatures on both battlefields have base power and toughness 1/1")
    void otherCreaturesBecome1() {
        harness.addToBattlefield(player1, new GodheadOfAwe());

        // A 4/4 flyer under Godhead's controller and a 2/2 under the opponent.
        Permanent airElemental = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, airElemental)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, airElemental)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(1);
    }

    @Test
    @DisplayName("Godhead of Awe does not shrink itself")
    void godheadKeepsOwnStats() {
        Permanent godhead = harness.addToBattlefieldAndReturn(player1, new GodheadOfAwe());

        assertThat(gqs.getEffectivePower(gd, godhead)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, godhead)).isEqualTo(4);
    }

    @Test
    @DisplayName("Counters apply on top of the 1/1 base")
    void countersApplyOnTop() {
        harness.addToBattlefield(player1, new GodheadOfAwe());

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        // Base 1/1 + 2 counters = 3/3
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Removing Godhead of Awe restores other creatures' original P/T")
    void removalRestoresStats() {
        Permanent godhead = harness.addToBattlefieldAndReturn(player1, new GodheadOfAwe());

        Permanent airElemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        assertThat(gqs.getEffectivePower(gd, airElemental)).isEqualTo(1);

        gd.playerBattlefields.get(player1.getId()).remove(godhead);

        assertThat(gqs.getEffectivePower(gd, airElemental)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, airElemental)).isEqualTo(4);
    }
}
