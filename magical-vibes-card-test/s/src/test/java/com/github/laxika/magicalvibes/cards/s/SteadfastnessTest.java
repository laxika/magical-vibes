package com.github.laxika.magicalvibes.cards.s;

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

@CardUsed({Steadfastness.class, GrizzlyBears.class, Plains.class})
class SteadfastnessTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving boosts all own creatures +0/+3")
    void resolvingBoostsAllOwnCreatures() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.castFromHand(player1, new Steadfastness(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, firstCreature)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, firstCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, firstCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, firstCreature)).isEqualTo(5);
        assertThat(PermanentGrants.powerBoostOf(gd, secondCreature)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, secondCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, secondCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, secondCreature)).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not boost opponent's creatures")
    void doesNotBoostOpponentCreatures() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.castFromHand(player1, new Steadfastness(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, opponentCreature)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, opponentCreature)).isZero();
    }

    @Test
    @DisplayName("Boost resets at cleanup step")
    void boostResetsAtCleanup() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.castFromHand(player1, new Steadfastness(), "{1}{W}");
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, creature)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, creature)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Works with empty battlefield (no crash)")
    void worksWithEmptyBattlefield() {
        harness.castFromHand(player1, new Steadfastness(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not boost a noncreature permanent")
    void doesNotBoostNoncreaturePermanent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());

        harness.castFromHand(player1, new Steadfastness(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(PermanentGrants.toughnessBoostOf(gd, creature)).isEqualTo(3);
        assertThat(PermanentGrants.powerBoostOf(gd, land)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, land)).isZero();
    }

    @Test
    @DisplayName("Does not boost creatures entering after resolution")
    void doesNotBoostCreaturesEnteringAfterResolution() {
        Permanent existingCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.castFromHand(player1, new Steadfastness(), "{1}{W}");
        harness.passBothPriorities();

        Permanent laterCreature = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(PermanentGrants.toughnessBoostOf(gd, existingCreature)).isEqualTo(3);
        assertThat(PermanentGrants.powerBoostOf(gd, laterCreature)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, laterCreature)).isZero();
    }

    @Test
    @DisplayName("Boosts creatures entering before resolution")
    void boostsCreaturesEnteringBeforeResolution() {
        harness.castFromHand(player1, new Steadfastness(), "{1}{W}");

        Permanent creature = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @Test
    @DisplayName("Multiple copies add their boosts until cleanup")
    void multipleCopiesAddTheirBoostsUntilCleanup() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.castFromHand(player1, new Steadfastness(), "{1}{W}");
        harness.passBothPriorities();
        harness.castFromHand(player1, new Steadfastness(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(8);

        harness.forceStep(TurnStep.END_STEP);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(8);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }
}
