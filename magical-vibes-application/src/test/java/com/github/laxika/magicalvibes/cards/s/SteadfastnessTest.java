package com.github.laxika.magicalvibes.cards.s;

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

        assertThat(firstCreature.getPowerModifier()).isZero();
        assertThat(firstCreature.getToughnessModifier()).isEqualTo(3);
        assertThat(firstCreature.getEffectivePower()).isEqualTo(2);
        assertThat(firstCreature.getEffectiveToughness()).isEqualTo(5);
        assertThat(secondCreature.getPowerModifier()).isZero();
        assertThat(secondCreature.getToughnessModifier()).isEqualTo(3);
        assertThat(secondCreature.getEffectivePower()).isEqualTo(2);
        assertThat(secondCreature.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not boost opponent's creatures")
    void doesNotBoostOpponentCreatures() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.castFromHand(player1, new Steadfastness(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(opponentCreature.getPowerModifier()).isZero();
        assertThat(opponentCreature.getToughnessModifier()).isZero();
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

        assertThat(creature.getPowerModifier()).isZero();
        assertThat(creature.getToughnessModifier()).isZero();
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
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

        assertThat(creature.getToughnessModifier()).isEqualTo(3);
        assertThat(land.getPowerModifier()).isZero();
        assertThat(land.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Does not boost creatures entering after resolution")
    void doesNotBoostCreaturesEnteringAfterResolution() {
        Permanent existingCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.castFromHand(player1, new Steadfastness(), "{1}{W}");
        harness.passBothPriorities();

        Permanent laterCreature = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(existingCreature.getToughnessModifier()).isEqualTo(3);
        assertThat(laterCreature.getPowerModifier()).isZero();
        assertThat(laterCreature.getToughnessModifier()).isZero();
    }
}
