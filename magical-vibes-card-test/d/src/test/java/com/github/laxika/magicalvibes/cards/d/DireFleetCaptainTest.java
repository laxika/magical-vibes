package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FathomFleetFirebrand;
import com.github.laxika.magicalvibes.cards.r.RaptorHatchling;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DireFleetCaptain.class, FathomFleetFirebrand.class, RaptorHatchling.class})
class DireFleetCaptainTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking puts ON_ATTACK trigger on the stack")
    void attackPutsTriggerOnStack() {
        addCreatureReady(player1, new DireFleetCaptain());

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).anyMatch(e ->
                e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && e.getCard().getName().equals("Dire Fleet Captain"));
    }

    @Test
    @DisplayName("Gets +0/+0 when attacking alone (no other Pirates)")
    void noBoostWhenAttackingAlone() {
        Permanent captain = addCreatureReady(player1, new DireFleetCaptain());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(captain.getPowerModifier()).isEqualTo(0);
        assertThat(captain.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Gets +1/+1 when attacking with one other Pirate")
    void boostWithOneOtherPirate() {
        Permanent captain = addCreatureReady(player1, new DireFleetCaptain());
        addCreatureReady(player1, new FathomFleetFirebrand());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(captain.getPowerModifier()).isEqualTo(1);
        assertThat(captain.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Gets +2/+2 when attacking with two other Pirates")
    void boostWithTwoOtherPirates() {
        Permanent captain = addCreatureReady(player1, new DireFleetCaptain());
        addCreatureReady(player1, new FathomFleetFirebrand());
        addCreatureReady(player1, new FathomFleetFirebrand());

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(captain.getPowerModifier()).isEqualTo(2);
        assertThat(captain.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Non-Pirate attackers do not count")
    void nonPirateAttackersDoNotCount() {
        Permanent captain = addCreatureReady(player1, new DireFleetCaptain());
        addCreatureReady(player1, new RaptorHatchling());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(captain.getPowerModifier()).isEqualTo(0);
        assertThat(captain.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Only counts attacking Pirates, not non-attacking ones")
    void onlyCountsAttackingPirates() {
        Permanent captain = addCreatureReady(player1, new DireFleetCaptain());
        addCreatureReady(player1, new FathomFleetFirebrand());
        addCreatureReady(player1, new FathomFleetFirebrand());

        // Only captain (index 0) and the first pirate (index 1) attack; second pirate (index 2) stays back
        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(captain.getPowerModifier()).isEqualTo(1);
        assertThat(captain.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Modifier resets at end of turn cleanup")
    void modifierResetsAtEndOfTurn() {
        Permanent captain = addCreatureReady(player1, new DireFleetCaptain());
        addCreatureReady(player1, new FathomFleetFirebrand());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(captain.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(captain.getPowerModifier()).isEqualTo(0);
        assertThat(captain.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Counts Pirates still attacking when the trigger resolves")
    void pirateLeavingBeforeResolutionDoesNotCount() {
        Permanent captain = addCreatureReady(player1, new DireFleetCaptain());
        Permanent pirate = addCreatureReady(player1, new FathomFleetFirebrand());
        addCreatureReady(player1, new FathomFleetFirebrand());

        declareAttackers(player1, List.of(0, 1, 2));
        harness.getPermanentRemovalService().removePermanentToHand(gd, pirate);
        resolveAllTriggers();

        assertThat(captain.getPowerModifier()).isEqualTo(1);
        assertThat(captain.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("The resolved bonus stays fixed if another attacking Pirate leaves")
    void resolvedBonusDoesNotShrink() {
        Permanent captain = addCreatureReady(player1, new DireFleetCaptain());
        Permanent pirate = addCreatureReady(player1, new FathomFleetFirebrand());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();
        harness.getPermanentRemovalService().removePermanentToHand(gd, pirate);

        assertThat(captain.getPowerModifier()).isEqualTo(1);
        assertThat(captain.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Two attacking Captains each count the other Pirate")
    void eachCaptainGetsItsOwnBonus() {
        Permanent first = addCreatureReady(player1, new DireFleetCaptain());
        Permanent second = addCreatureReady(player1, new DireFleetCaptain());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(first.getPowerModifier()).isEqualTo(1);
        assertThat(first.getToughnessModifier()).isEqualTo(1);
        assertThat(second.getPowerModifier()).isEqualTo(1);
        assertThat(second.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("A Captain returning after its attack trigger does not get the old bonus")
    void returnedCaptainIsANewObject() {
        Permanent original = addCreatureReady(player1, new DireFleetCaptain());
        addCreatureReady(player1, new FathomFleetFirebrand());

        declareAttackers(player1, List.of(0, 1));
        harness.getPermanentRemovalService().removePermanentToHand(gd, original);
        gd.playerHands.get(player1.getId()).remove(original.getCard());
        Permanent returned = addCreatureReady(player1, original.getCard());
        resolveAllTriggers();

        assertThat(returned.getPowerModifier()).isZero();
        assertThat(returned.getToughnessModifier()).isZero();
    }
}
