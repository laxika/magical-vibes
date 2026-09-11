package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MurasaBrute;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KarganWarleader.class, MurasaBrute.class, GrizzlyBears.class})
class KarganWarleaderTest extends BaseCardTest {

    @Test
    void buffsOtherWarriorsYouControl() {
        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new MurasaBrute());
        int basePower = gqs.getEffectivePower(gd, warrior);
        int baseToughness = gqs.getEffectiveToughness(gd, warrior);

        harness.addToBattlefield(player1, new KarganWarleader());

        assertThat(gqs.getEffectivePower(gd, warrior)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, warrior)).isEqualTo(baseToughness + 1);
    }

    @Test
    void doesNotBuffItselfNonWarriorsOrOpposingWarriors() {
        Permanent nonWarrior = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        int nonWarriorPower = gqs.getEffectivePower(gd, nonWarrior);
        int nonWarriorToughness = gqs.getEffectiveToughness(gd, nonWarrior);

        Permanent opposingWarrior = harness.addToBattlefieldAndReturn(player2, new MurasaBrute());
        int opposingWarriorPower = gqs.getEffectivePower(gd, opposingWarrior);
        int opposingWarriorToughness = gqs.getEffectiveToughness(gd, opposingWarrior);

        KarganWarleader card = new KarganWarleader();
        card.setPower(10);
        card.setToughness(10);
        Permanent warleader = harness.addToBattlefieldAndReturn(player1, card);

        assertThat(gqs.getEffectivePower(gd, warleader)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, warleader)).isEqualTo(10);
        assertThat(gqs.getEffectivePower(gd, nonWarrior)).isEqualTo(nonWarriorPower);
        assertThat(gqs.getEffectiveToughness(gd, nonWarrior)).isEqualTo(nonWarriorToughness);
        assertThat(gqs.getEffectivePower(gd, opposingWarrior)).isEqualTo(opposingWarriorPower);
        assertThat(gqs.getEffectiveToughness(gd, opposingWarrior)).isEqualTo(opposingWarriorToughness);
    }

    @Test
    void multipleWarleadersStackTheirBonuses() {
        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new MurasaBrute());
        int basePower = gqs.getEffectivePower(gd, warrior);
        int baseToughness = gqs.getEffectiveToughness(gd, warrior);

        harness.addToBattlefield(player1, new KarganWarleader());
        harness.addToBattlefield(player1, new KarganWarleader());

        assertThat(gqs.getEffectivePower(gd, warrior)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, warrior)).isEqualTo(baseToughness + 2);
    }
}
