package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.b.BearCub;
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

@CardUsed({NorwoodWarrior.class, BearCub.class})
class NorwoodWarriorTest extends BaseCardTest {

    @Test
    @DisplayName("Becoming blocked creates one becomes-blocked trigger")
    void becomingBlockedCreatesTrigger() {
        Permanent warrior = addCreatureReady(player1, new NorwoodWarrior());
        addCreatureReady(player2, new BearCub());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getSourcePermanentId()).isEqualTo(warrior.getId());
    }

    @Test
    @DisplayName("When blocked Norwood Warrior gets +1/+1 until end of turn")
    void blockedGivesPlusOnePlusOne() {
        Permanent warrior = addCreatureReady(player1, new NorwoodWarrior());
        addCreatureReady(player2, new BearCub());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, warrior)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, warrior)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, warrior)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, warrior)).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost is a flat +1/+1 regardless of the number of blockers")
    void twoBlockersStillGivesPlusOnePlusOne() {
        Permanent warrior = addCreatureReady(player1, new NorwoodWarrior());
        addCreatureReady(player2, new BearCub());
        addCreatureReady(player2, new BearCub());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, warrior)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, warrior)).isEqualTo(1);
    }

    @Test
    @DisplayName("If unblocked no becomes-blocked trigger is created")
    void unblockedCreatesNoTrigger() {
        Permanent warrior = addCreatureReady(player1, new NorwoodWarrior());
        warrior.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(PermanentGrants.powerBoostOf(gd, warrior)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, warrior)).isZero();
    }

    @Test
    @DisplayName("Blocking another creature does not give Norwood Warrior a boost")
    void blockingCreatesNoTrigger() {
        addCreatureReady(player1, new BearCub());
        Permanent warrior = addCreatureReady(player2, new NorwoodWarrior());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).isEmpty();
        assertThat(PermanentGrants.powerBoostOf(gd, warrior)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, warrior)).isZero();
    }

    @Test
    @DisplayName("The becomes-blocked boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent warrior = addCreatureReady(player1, new NorwoodWarrior());
        warrior.setAttacking(true);
        addCreatureReady(player2, new BearCub());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, warrior)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, warrior)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, warrior)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, warrior)).isZero();
    }
}
