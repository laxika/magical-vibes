package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MistIntruder;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TideDrifter.class, MistIntruder.class, GrizzlyBears.class})
class TideDrifterTest extends BaseCardTest {

    @Test
    @DisplayName("Other colorless creatures you control get +0/+1")
    void buffsOtherColorlessCreaturesYouControl() {
        harness.addToBattlefield(player1, new TideDrifter());
        harness.addToBattlefield(player1, new MistIntruder());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new MistIntruder());

        Permanent drifter = findPermanent(player1, "Tide Drifter");
        Permanent colorlessCreature = findPermanent(player1, "Mist Intruder");
        Permanent coloredCreature = findPermanent(player1, "Grizzly Bears");
        Permanent opponentColorlessCreature = findPermanent(player2, "Mist Intruder");

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
}
