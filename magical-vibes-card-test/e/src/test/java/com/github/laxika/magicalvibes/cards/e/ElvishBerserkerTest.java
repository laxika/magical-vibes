package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HornedTurtle;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ElvishBerserker.class, GrizzlyBears.class, HornedTurtle.class, Boomerang.class})
class ElvishBerserkerTest extends BaseCardTest {

    @Test
    @DisplayName("Becoming blocked creates one becomes-blocked trigger")
    void becomingBlockedCreatesTrigger() {
        Permanent berserker = addCreatureReady(player1, new ElvishBerserker());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getSourcePermanentId()).isEqualTo(berserker.getId());
    }

    @Test
    @DisplayName("With one blocker Elvish Berserker gets +1/+1 until end of turn")
    void oneBlockerGivesPlusOnePlusOne() {
        Permanent berserker = addCreatureReady(player1, new ElvishBerserker());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, berserker)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, berserker)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, berserker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, berserker)).isEqualTo(2);
    }

    @Test
    @DisplayName("With two blockers Elvish Berserker gets +2/+2 until end of turn")
    void twoBlockersGivesPlusTwoPlusTwo() {
        Permanent berserker = addCreatureReady(player1, new ElvishBerserker());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, berserker)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, berserker)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, berserker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, berserker)).isEqualTo(3);
    }

    @Test
    @DisplayName("If unblocked no becomes-blocked trigger is created")
    void unblockedCreatesNoTrigger() {
        Permanent berserker = addCreatureReady(player1, new ElvishBerserker());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(PermanentGrants.powerBoostOf(gd, berserker)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, berserker)).isZero();
    }

    @Test
    @DisplayName("The blocker-based boost expires at end of turn")
    void boostExpiresAtEndOfTurn() {
        Permanent berserker = addCreatureReady(player1, new ElvishBerserker());
        addCreatureReady(player2, new HornedTurtle());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, berserker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, berserker)).isEqualTo(2);

        harness.passUntil(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, berserker)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, berserker)).isZero();
        assertThat(gqs.getEffectivePower(gd, berserker)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, berserker)).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    @DisplayName("The boost counts only blockers still present when the trigger resolves")
    void blockerLeavingBeforeResolutionReducesBoost(int blockerCount) {
        Permanent berserker = addCreatureReady(player1, new ElvishBerserker());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        if (blockerCount == 2) {
            addCreatureReady(player2, new GrizzlyBears());
        }
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, blockerCount == 1
                ? List.of(new BlockerAssignment(0, 0))
                : List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(gd.stack).hasSize(1);
        assertThat(PermanentGrants.powerBoostOf(gd, berserker)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, berserker)).isZero();

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            harness.castAndResolveInstant(player1, 0, blocker.getId());
            harness.assertInHand(player2, "Grizzly Bears");
            assertThat(gd.stack).hasSize(1);
            harness.passBothPriorities();
        });

        assertThat(PermanentGrants.powerBoostOf(gd, berserker)).isEqualTo(blockerCount - 1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, berserker)).isEqualTo(blockerCount - 1);
    }
}
