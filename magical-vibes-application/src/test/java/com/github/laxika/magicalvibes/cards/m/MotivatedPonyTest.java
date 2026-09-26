package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Food;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MotivatedPony.class, GrizzlyBears.class, Food.class})
class MotivatedPonyTest extends BaseCardTest {

    @Test
    void boostsAttackingCreaturesOnly() {
        Permanent pony = addCreatureReady(player1, new MotivatedPony());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponent = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, pony)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, pony)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, nonAttacker)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(2);
    }

    @Test
    void foodEntryUntapsAndAddsAdditionalBoostToAttackers() {
        Permanent pony = addCreatureReady(player1, new MotivatedPony());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonAttacker = addCreatureReady(player1, new GrizzlyBears());
        harness.enterBattlefieldAndReturn(player1, new Food());

        declareAttackers(List.of(0, 1));
        assertThat(pony.isTapped()).isTrue();
        assertThat(attacker.isTapped()).isTrue();
        resolveAllTriggers();

        assertThat(pony.isTapped()).isFalse();
        assertThat(attacker.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, pony)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, pony)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, nonAttacker)).isEqualTo(2);
    }
}
