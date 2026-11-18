package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WarriorsHonor.class, GrizzlyBears.class, FountainOfYouth.class})
class WarriorsHonorTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts it on the stack as INSTANT_SPELL")
    void castingPutsOnStack() {
        WarriorsHonor honor = castWarriorsHonor();

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard()).isSameAs(honor);
    }

    @Test
    @DisplayName("Resolving boosts all own creatures +1/+1")
    void resolvingBoostsAllOwnCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castWarriorsHonor();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, first)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, first)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);
        assertThat(PermanentGrants.powerBoostOf(gd, second)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, second)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not boost opponent's creatures")
    void doesNotBoostOpponentCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castWarriorsHonor();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, ownCreature)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, ownCreature)).isEqualTo(1);
        assertThat(PermanentGrants.powerBoostOf(gd, opponentCreature)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, opponentCreature)).isZero();
    }

    @Test
    @DisplayName("Boost resets at cleanup step")
    void boostResetsAtCleanup() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castWarriorsHonor();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, creature)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, creature)).isZero();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Affects only creatures present under its controller at resolution")
    void affectsOnlyCreaturesPresentAtResolution() {
        castWarriorsHonor();
        Permanent presentCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent noncreature = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());

        harness.passBothPriorities();

        Permanent laterCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, presentCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, presentCreature)).isEqualTo(3);
        assertThat(PermanentGrants.powerBoostOf(gd, noncreature)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, noncreature)).isZero();
        assertThat(gqs.getEffectivePower(gd, laterCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, laterCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Works with empty battlefield (no crash)")
    void worksWithEmptyBattlefield() {
        castWarriorsHonor();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Warrior's Honor goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        WarriorsHonor honor = castWarriorsHonor();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(honor);
    }

    @Test
    @DisplayName("Two resolving copies give cumulative boosts until end of turn")
    void multipleCopiesGiveCumulativeBoosts() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castWarriorsHonor();
        harness.passBothPriorities();
        castWarriorsHonor();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    private WarriorsHonor castWarriorsHonor() {
        WarriorsHonor honor = new WarriorsHonor();
        harness.castFromHand(player1, honor, "{2}{W}");
        return honor;
    }
}

