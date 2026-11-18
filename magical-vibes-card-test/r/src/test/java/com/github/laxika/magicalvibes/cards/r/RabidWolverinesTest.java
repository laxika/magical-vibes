package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RabidWolverines.class, RagingGoblin.class})
class RabidWolverinesTest extends BaseCardTest {

    @Test
    @DisplayName("Becoming blocked by one creature gives +1/+1 until end of turn")
    void oneBlockerGivesPlusOne() {
        Permanent wolverines = addWolverinesReady(player1);
        wolverines.setAttacking(true);
        addCreatureReady(player2, new RagingGoblin());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, wolverines)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, wolverines)).isEqualTo(1);
    }

    @Test
    @DisplayName("Becoming blocked by two creatures triggers once for each blocker")
    void twoBlockersGivePlusTwo() {
        Permanent wolverines = addWolverinesReady(player1);
        wolverines.setAttacking(true);
        addCreatureReady(player2, new RagingGoblin());
        addCreatureReady(player2, new RagingGoblin());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, wolverines)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, wolverines)).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost wears off at the end of the turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent wolverines = addWolverinesReady(player1);
        wolverines.setAttacking(true);
        addCreatureReady(player2, new RagingGoblin());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, wolverines)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, wolverines)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, wolverines)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, wolverines)).isZero();
    }

    @Test
    @DisplayName("An unblocked Rabid Wolverines creates no trigger")
    void unblockedCreatesNoTrigger() {
        Permanent wolverines = addWolverinesReady(player1);
        wolverines.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(PermanentGrants.powerBoostOf(gd, wolverines)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, wolverines)).isZero();
    }

    @Test
    @DisplayName("The boost resolves even if the blocker dies before the trigger resolves")
    void blockerDyingDoesNotPreventBoost() {
        Permanent wolverines = addWolverinesReady(player1);
        wolverines.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new RagingGoblin());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);
        assertThat(PermanentGrants.powerBoostOf(gd, wolverines)).isZero();
        blocker.setMarkedDamage(1);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player2, "Raging Goblin");
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, wolverines)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, wolverines)).isEqualTo(1);
    }

    @Test
    @DisplayName("Blocking another creature does not trigger the boost")
    void blockingDoesNotTriggerBoost() {
        Permanent attacker = addCreatureReady(player1, new RagingGoblin());
        attacker.setAttacking(true);
        Permanent wolverines = addWolverinesReady(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).isEmpty();
        assertThat(PermanentGrants.powerBoostOf(gd, wolverines)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, wolverines)).isZero();
    }

    private Permanent addWolverinesReady(Player player) {
        return addCreatureReady(player, new RabidWolverines());
    }
}
