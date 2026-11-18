package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
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

@CardUsed({SnortingGahr.class, FreshVolunteers.class})
class SnortingGahrTest extends BaseCardTest {

    @Test
    @DisplayName("When Snorting Gahr becomes blocked, it gets +2/+2 until end of turn")
    void becomesBlockedGetsBoost() {
        Permanent gahr = addReadyGahr(player1);
        gahr.setAttacking(true);
        addCreatureReady(player2, new FreshVolunteers());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, gahr)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, gahr)).isEqualTo(2);
    }

    @Test
    @DisplayName("Multiple blockers still cause only one +2/+2 trigger")
    void becomesBlockedByMultipleCreaturesGetsOneBoost() {
        Permanent gahr = addReadyGahr(player1);
        gahr.setAttacking(true);
        addCreatureReady(player2, new FreshVolunteers());
        addCreatureReady(player2, new FreshVolunteers());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, gahr)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, gahr)).isEqualTo(2);
    }

    @Test
    @DisplayName("Snorting Gahr gets no boost when it is unblocked")
    void unblockedGetsNoBoost() {
        Permanent gahr = addReadyGahr(player1);
        gahr.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(PermanentGrants.powerBoostOf(gd, gahr)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, gahr)).isZero();
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent gahr = addReadyGahr(player1);
        gahr.setAttacking(true);
        addCreatureReady(player2, new FreshVolunteers());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, gahr)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, gahr)).isZero();
    }

    @Test
    @DisplayName("Blocking an attacker does not boost Snorting Gahr")
    void blockingDoesNotGiveBoost() {
        Permanent attacker = addCreatureReady(player1, new FreshVolunteers());
        attacker.setAttacking(true);
        Permanent gahr = addReadyGahr(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, gahr)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, gahr)).isZero();
    }

    @Test
    @DisplayName("Only the blocked Snorting Gahr receives the boost")
    void onlyBlockedCopyGetsBoost() {
        Permanent blockedGahr = addReadyGahr(player1);
        Permanent unblockedGahr = addReadyGahr(player1);
        blockedGahr.setAttacking(true);
        unblockedGahr.setAttacking(true);
        addCreatureReady(player2, new FreshVolunteers());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, blockedGahr)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, blockedGahr)).isEqualTo(2);
        assertThat(PermanentGrants.powerBoostOf(gd, unblockedGahr)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, unblockedGahr)).isZero();
    }

    private Permanent addReadyGahr(Player player) {
        return addCreatureReady(player, new SnortingGahr());
    }
}
