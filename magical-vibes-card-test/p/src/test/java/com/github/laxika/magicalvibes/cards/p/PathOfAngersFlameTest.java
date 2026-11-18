package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.h.HandOfHonor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PathOfAngersFlame.class, HandOfHonor.class})
class PathOfAngersFlameTest extends BaseCardTest {

    @Test
    void boostsYourCreaturesAndNotOpponentsCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HandOfHonor());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new HandOfHonor());
        harness.castFromHand(player1, new PathOfAngersFlame(), "{2}{R}");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(2);
    }

    @Test
    void boostEndsAtCleanup() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HandOfHonor());
        harness.castFromHand(player1, new PathOfAngersFlame(), "{2}{R}");
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
    }

    @Test
    void creaturesEnteringLaterAreNotBoosted() {
        Permanent existingCreature = harness.addToBattlefieldAndReturn(player1, new HandOfHonor());

        harness.castFromHand(player1, new PathOfAngersFlame(), "{2}{R}");
        harness.passBothPriorities();

        Permanent laterCreature = harness.addToBattlefieldAndReturn(player1, new HandOfHonor());

        assertThat(gqs.getEffectivePower(gd, existingCreature)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, laterCreature)).isEqualTo(2);
    }

    @Test
    void creaturesEnteringBeforeResolutionAreBoosted() {
        harness.castFromHand(player1, new PathOfAngersFlame(), "{2}{R}");

        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HandOfHonor());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    void multipleCopiesGiveCumulativeBoosts() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HandOfHonor());

        harness.castFromHand(player1, new PathOfAngersFlame(), "{2}{R}");
        harness.passBothPriorities();
        harness.castFromHand(player1, new PathOfAngersFlame(), "{2}{R}");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }
}
