package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.y.YotianSoldier;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HarbinVanguardAviatorTest extends BaseCardTest {

    @Test
    @DisplayName("Buffs and gives flying to creatures you control after attacking with five Soldiers")
    void buffsCreaturesAfterAttackingWithFiveSoldiers() {
        Permanent harbin = addCreatureReady(player1, new HarbinVanguardAviator());
        Permanent soldier1 = addCreatureReady(player1, new YotianSoldier());
        Permanent soldier2 = addCreatureReady(player1, new YotianSoldier());
        Permanent soldier3 = addCreatureReady(player1, new YotianSoldier());
        Permanent soldier4 = addCreatureReady(player1, new YotianSoldier());
        Permanent nonattackingCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        int harbinPower = gqs.getEffectivePower(gd, harbin);
        int harbinToughness = gqs.getEffectiveToughness(gd, harbin);
        int nonattackingCreaturePower = gqs.getEffectivePower(gd, nonattackingCreature);

        declareAttackers(List.of(0, 1, 2, 3, 4));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, harbin)).isEqualTo(harbinPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, harbin)).isEqualTo(harbinToughness + 1);
        assertThat(harbin.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(soldier1.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(soldier2.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(soldier3.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(soldier4.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, nonattackingCreature)).isEqualTo(nonattackingCreaturePower + 1);
        assertThat(nonattackingCreature.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(opponentCreature.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Does not trigger when five creatures attack but fewer than five are Soldiers")
    void doesNotTriggerWithFewerThanFiveSoldiers() {
        Permanent harbin = addCreatureReady(player1, new HarbinVanguardAviator());
        Permanent soldier = addCreatureReady(player1, new YotianSoldier());
        addCreatureReady(player1, new YotianSoldier());
        addCreatureReady(player1, new YotianSoldier());
        addCreatureReady(player1, new GrizzlyBears());

        int harbinPower = gqs.getEffectivePower(gd, harbin);
        int harbinToughness = gqs.getEffectiveToughness(gd, harbin);

        declareAttackers(List.of(0, 1, 2, 3, 4));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, harbin)).isEqualTo(harbinPower);
        assertThat(gqs.getEffectiveToughness(gd, harbin)).isEqualTo(harbinToughness);
        assertThat(soldier.hasKeyword(Keyword.FLYING)).isFalse();
    }
}
