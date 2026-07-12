package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GodheadOfAweTest extends BaseCardTest {

    @Test
    @DisplayName("Other creatures on both battlefields have base power and toughness 1/1")
    void otherCreaturesBecome1() {
        Permanent godhead = new Permanent(new GodheadOfAwe());
        gd.playerBattlefields.get(player1.getId()).add(godhead);

        // A 4/4 flyer under Godhead's controller and a 2/2 under the opponent.
        Permanent airElemental = new Permanent(new AirElemental());
        gd.playerBattlefields.get(player1.getId()).add(airElemental);
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(bears);

        assertThat(gqs.getEffectivePower(gd, airElemental)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, airElemental)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(1);
    }

    @Test
    @DisplayName("Godhead of Awe does not shrink itself")
    void godheadKeepsOwnStats() {
        Permanent godhead = new Permanent(new GodheadOfAwe());
        gd.playerBattlefields.get(player1.getId()).add(godhead);

        assertThat(gqs.getEffectivePower(gd, godhead)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, godhead)).isEqualTo(4);
    }

    @Test
    @DisplayName("Counters apply on top of the 1/1 base")
    void countersApplyOnTop() {
        Permanent godhead = new Permanent(new GodheadOfAwe());
        gd.playerBattlefields.get(player1.getId()).add(godhead);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        // Base 1/1 + 2 counters = 3/3
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Removing Godhead of Awe restores other creatures' original P/T")
    void removalRestoresStats() {
        Permanent godhead = new Permanent(new GodheadOfAwe());
        gd.playerBattlefields.get(player1.getId()).add(godhead);

        Permanent airElemental = new Permanent(new AirElemental());
        gd.playerBattlefields.get(player2.getId()).add(airElemental);

        assertThat(gqs.getEffectivePower(gd, airElemental)).isEqualTo(1);

        gd.playerBattlefields.get(player1.getId()).remove(godhead);

        assertThat(gqs.getEffectivePower(gd, airElemental)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, airElemental)).isEqualTo(4);
    }
}
