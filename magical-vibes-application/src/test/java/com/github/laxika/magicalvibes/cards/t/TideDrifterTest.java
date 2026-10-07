package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.m.MistIntruder;
import com.github.laxika.magicalvibes.cards.o.OranRiefInvoker;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TideDrifter.class, MistIntruder.class, OranRiefInvoker.class})
class TideDrifterTest extends BaseCardTest {

    @Test
    @DisplayName("Other colorless creatures you control get +0/+1")
    void buffsOtherColorlessCreaturesYouControl() {
        Permanent drifter = harness.addToBattlefieldAndReturn(player1, new TideDrifter());
        Permanent colorlessCreature = harness.addToBattlefieldAndReturn(player1, new MistIntruder());
        Permanent coloredCreature = harness.addToBattlefieldAndReturn(player1, new OranRiefInvoker());
        Permanent opponentColorlessCreature = harness.addToBattlefieldAndReturn(player2, new MistIntruder());

        assertThat(gqs.getEffectivePower(gd, drifter)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, drifter)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, colorlessCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, colorlessCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, coloredCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, coloredCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentColorlessCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentColorlessCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Multiple Tide Drifters stack their toughness bonus")
    void bonusesStack() {
        harness.addToBattlefield(player1, new TideDrifter());
        harness.addToBattlefield(player1, new TideDrifter());
        Permanent colorlessCreature = harness.addToBattlefieldAndReturn(player1, new MistIntruder());

        assertThat(gqs.getEffectivePower(gd, colorlessCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, colorlessCreature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Tide Drifters boost each other without boosting themselves")
    void driftersBoostEachOther() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new TideDrifter());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new TideDrifter());

        assertThat(gqs.getEffectivePower(gd, first)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, second)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(6);
    }

    @Test
    @DisplayName("The toughness bonus ends when Tide Drifter dies")
    void bonusEndsWhenSourceDies() {
        Permanent drifter = harness.addToBattlefieldAndReturn(player1, new TideDrifter());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new MistIntruder());
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);

        drifter.setMarkedDamage(5);
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(drifter).contains(creature);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }
}
