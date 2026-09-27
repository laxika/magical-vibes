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

        assertThat(ownCreature.getEffectivePower()).isEqualTo(4);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(2);
        assertThat(opposingCreature.getEffectivePower()).isEqualTo(2);
        assertThat(opposingCreature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void boostEndsAtCleanup() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HandOfHonor());
        harness.castFromHand(player1, new PathOfAngersFlame(), "{2}{R}");
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ownCreature.getEffectivePower()).isEqualTo(2);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void creaturesEnteringLaterAreNotBoosted() {
        Permanent existingCreature = harness.addToBattlefieldAndReturn(player1, new HandOfHonor());

        harness.castFromHand(player1, new PathOfAngersFlame(), "{2}{R}");
        harness.passBothPriorities();

        Permanent laterCreature = harness.addToBattlefieldAndReturn(player1, new HandOfHonor());

        assertThat(existingCreature.getEffectivePower()).isEqualTo(4);
        assertThat(laterCreature.getEffectivePower()).isEqualTo(2);
    }
}
