package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LegionOfClay.class, FountainOfYouth.class, GrizzlyBears.class})
class LegionOfClayTest extends BaseCardTest {

    @Test
    void givesTrampleToCreaturesYouControl() {
        harness.addToBattlefieldAndReturn(player1, new LegionOfClay());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void perpetuallyBoostsCurrentCreaturesWhenNontokenArtifactEnters() {
        harness.addToBattlefieldAndReturn(player1, new LegionOfClay());
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.enterBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, firstCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, firstCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, secondCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, secondCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(2);
    }

    @Test
    void doesNotTriggerForANonArtifact() {
        harness.addToBattlefieldAndReturn(player1, new LegionOfClay());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }
}
