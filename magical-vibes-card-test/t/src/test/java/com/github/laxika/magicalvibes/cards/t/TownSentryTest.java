package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.h.HighGround;
import com.github.laxika.magicalvibes.cards.v.VolunteerMilitia;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TownSentry.class, VolunteerMilitia.class, HighGround.class})
class TownSentryTest extends BaseCardTest {

    @Test
    @DisplayName("Blocking pushes a triggered ability onto the stack")
    void blockTriggerPushesOntoStack() {
        Permanent sentry = addCreatureReady(player2, new TownSentry());

        Permanent atkPerm = addCreatureReady(player1, new VolunteerMilitia());
        atkPerm.setAttacking(true);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(entry.getSourcePermanentId()).isEqualTo(sentry.getId());
    }

    @Test
    @DisplayName("Resolving the block trigger gives +0/+2 until end of turn")
    void blockTriggerGivesPlusZeroPlusTwo() {
        Permanent sentry = addCreatureReady(player2, new TownSentry());

        Permanent atkPerm = addCreatureReady(player1, new VolunteerMilitia());
        atkPerm.setAttacking(true);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(sentry.getPowerModifier()).isEqualTo(0);
        assertThat(sentry.getToughnessModifier()).isEqualTo(2);
        assertThat(sentry.getEffectivePower()).isEqualTo(2);
        assertThat(sentry.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("+0/+2 modifier resets at end of turn cleanup")
    void modifierResetsAtEndOfTurn() {
        Permanent sentry = addCreatureReady(player2, new TownSentry());

        Permanent atkPerm = addCreatureReady(player1, new VolunteerMilitia());
        atkPerm.setAttacking(true);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(sentry.getToughnessModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(sentry.getPowerModifier()).isEqualTo(0);
        assertThat(sentry.getToughnessModifier()).isEqualTo(0);
        assertThat(sentry.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Not blocking does not trigger the ability")
    void noTriggerWhenNotBlocking() {
        addCreatureReady(player2, new TownSentry());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Blocking multiple attackers triggers only once")
    void blockingMultipleAttackersTriggersOnce() {
        Permanent sentry = addCreatureReady(player2, new TownSentry());
        harness.addToBattlefield(player2, new HighGround());
        addCreatureReady(player1, new VolunteerMilitia()).setAttacking(true);
        addCreatureReady(player1, new VolunteerMilitia()).setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(0, 1)));

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(sentry.getPowerModifier()).isZero();
        assertThat(sentry.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost waits for resolution and affects only the blocking Sentry")
    void boostAppliesOnlyToSourceAfterResolution() {
        Permanent blockingSentry = addCreatureReady(player2, new TownSentry());
        Permanent otherSentry = addCreatureReady(player2, new TownSentry());
        Permanent attacker = addCreatureReady(player1, new VolunteerMilitia());
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blockingSentry.getToughnessModifier()).isZero();
        assertThat(otherSentry.getToughnessModifier()).isZero();
        resolveAllTriggers();

        assertThat(blockingSentry.getPowerModifier()).isZero();
        assertThat(blockingSentry.getToughnessModifier()).isEqualTo(2);
        assertThat(otherSentry.getPowerModifier()).isZero();
        assertThat(otherSentry.getToughnessModifier()).isZero();
        assertThat(attacker.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Being blocked while attacking does not trigger the ability")
    void beingBlockedDoesNotTrigger() {
        Permanent sentry = addCreatureReady(player1, new TownSentry());
        sentry.setAttacking(true);
        addCreatureReady(player2, new VolunteerMilitia());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).isEmpty();
        assertThat(sentry.getPowerModifier()).isZero();
        assertThat(sentry.getToughnessModifier()).isZero();
    }
}
