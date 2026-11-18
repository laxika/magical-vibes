package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WarriorsCharge.class, GrizzlyBears.class, Plains.class})
class WarriorsChargeTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving boosts all own creatures +1/+1")
    void resolvingBoostsAllOwnCreatures() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.castFromHand(player1, new WarriorsCharge(), "{2}{W}");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, firstCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, firstCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, secondCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, secondCreature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not boost opponent's creatures")
    void doesNotBoostOpponentCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.castFromHand(player1, new WarriorsCharge(), "{2}{W}");
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, ownCreature)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, ownCreature)).isEqualTo(1);
        assertThat(PermanentGrants.powerBoostOf(gd, opponentCreature)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, opponentCreature)).isZero();
    }

    @Test
    @DisplayName("Does not boost noncreatures")
    void doesNotBoostNoncreatures() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());

        harness.castFromHand(player1, new WarriorsCharge(), "{2}{W}");
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, land)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, land)).isZero();
    }

    @Test
    @DisplayName("Does not boost creatures entering after resolution")
    void doesNotBoostCreaturesEnteringAfterResolution() {
        Permanent existingCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.castFromHand(player1, new WarriorsCharge(), "{2}{W}");
        harness.passBothPriorities();

        Permanent laterCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, existingCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, existingCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, laterCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, laterCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost wears off at cleanup step")
    void boostResetsAtCleanup() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.castFromHand(player1, new WarriorsCharge(), "{2}{W}");
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Includes creatures entering before resolution")
    void boostsCreaturesEnteringBeforeResolution() {
        harness.castFromHand(player1, new WarriorsCharge(), "{2}{W}");
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Multiple resolutions give cumulative boosts")
    void multipleResolutionsGiveCumulativeBoosts() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.castFromHand(player1, new WarriorsCharge(), "{2}{W}");
        harness.passBothPriorities();
        harness.castFromHand(player1, new WarriorsCharge(), "{2}{W}");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Resolves with no creatures and does not boost a later creature")
    void resolvesWithNoCreatures() {
        harness.castFromHand(player1, new WarriorsCharge(), "{2}{W}");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Warrior's Charge");
        assertThat(gd.stack).isEmpty();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }
}
