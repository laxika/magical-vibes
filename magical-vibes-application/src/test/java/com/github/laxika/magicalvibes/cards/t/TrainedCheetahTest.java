package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.s.ShuFootSoldiers;
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

@CardUsed({TrainedCheetah.class, ShuFootSoldiers.class})
class TrainedCheetahTest extends BaseCardTest {

    @Test
    @DisplayName("Becoming blocked creates a becomes-blocked trigger")
    void becomingBlockedCreatesTrigger() {
        Permanent cheetah = addCreatureReady(player1, new TrainedCheetah());
        cheetah.setAttacking(true);
        addCreatureReady(player2, new ShuFootSoldiers());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getSourcePermanentId()).isEqualTo(cheetah.getId());
    }

    @Test
    @DisplayName("When blocked Trained Cheetah gets +1/+1 until end of turn")
    void blockedGivesPlusOnePlusOne() {
        Permanent cheetah = addCreatureReady(player1, new TrainedCheetah());
        cheetah.setAttacking(true);
        addCreatureReady(player2, new ShuFootSoldiers());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(cheetah.getPowerModifier()).isEqualTo(1);
        assertThat(cheetah.getToughnessModifier()).isEqualTo(1);
        assertThat(cheetah.getEffectivePower()).isEqualTo(3);
        assertThat(cheetah.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Blocked by two creatures still only gets +1/+1")
    void multipleBlockersStillOneBoost() {
        Permanent cheetah = addCreatureReady(player1, new TrainedCheetah());
        cheetah.setAttacking(true);
        addCreatureReady(player2, new ShuFootSoldiers());
        addCreatureReady(player2, new ShuFootSoldiers());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));
        resolveAllTriggers();

        assertThat(cheetah.getPowerModifier()).isEqualTo(1);
        assertThat(cheetah.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("If unblocked no becomes-blocked trigger is created")
    void unblockedCreatesNoTrigger() {
        Permanent cheetah = addCreatureReady(player1, new TrainedCheetah());
        cheetah.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(cheetah.getPowerModifier()).isZero();
        assertThat(cheetah.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The +1/+1 bonus wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent cheetah = addCreatureReady(player1, new TrainedCheetah());
        cheetah.setAttacking(true);
        addCreatureReady(player2, new ShuFootSoldiers());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(cheetah.getPowerModifier()).isEqualTo(1);
        assertThat(cheetah.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(cheetah.getPowerModifier()).isZero();
        assertThat(cheetah.getToughnessModifier()).isZero();
    }
}
