package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HighGround;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChubToad.class, GrizzlyBears.class, HighGround.class})
class ChubToadTest extends BaseCardTest {

    @Test
    @DisplayName("Both Chub Toads trigger when one blocks the other, and boost only on resolution")
    void bothToadsReceiveTheirOwnBoostsOnResolution() {
        Permanent attackingToad = addCreatureReady(player1, new ChubToad());
        attackingToad.setAttacking(true);
        Permanent blockingToad = addCreatureReady(player2, new ChubToad());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(2);
        assertThat(PermanentGrants.powerBoostOf(gd, attackingToad)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, attackingToad)).isZero();
        assertThat(PermanentGrants.powerBoostOf(gd, blockingToad)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, blockingToad)).isZero();

        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, attackingToad)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, attackingToad)).isEqualTo(2);
        assertThat(PermanentGrants.powerBoostOf(gd, blockingToad)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, blockingToad)).isEqualTo(2);
    }

    @Test
    @DisplayName("When Chub Toad becomes blocked, it gets +2/+2 until end of turn")
    void becomesBlockedGetsBoost() {
        Permanent toad = addCreatureReady(player1, new ChubToad());
        toad.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, toad)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, toad)).isEqualTo(2);
    }

    @Test
    @DisplayName("When Chub Toad blocks, it gets +2/+2 until end of turn")
    void blocksGetsBoost() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent toad = addCreatureReady(player2, new ChubToad());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, toad)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, toad)).isEqualTo(2);
    }

    @Test
    @DisplayName("When Chub Toad becomes blocked by multiple creatures, it gets only one boost")
    void becomesBlockedByMultipleCreaturesGetsOneBoost() {
        Permanent toad = addCreatureReady(player1, new ChubToad());
        toad.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, toad)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, toad)).isEqualTo(2);
    }

    @Test
    @DisplayName("When Chub Toad blocks multiple creatures, it gets only one boost")
    void blocksMultipleCreaturesGetsOneBoost() {
        harness.addToBattlefield(player2, new HighGround());
        Permanent toad = addCreatureReady(player2, new ChubToad());
        Permanent attacker1 = addCreatureReady(player1, new GrizzlyBears());
        attacker1.setAttacking(true);
        Permanent attacker2 = addCreatureReady(player1, new GrizzlyBears());
        attacker2.setAttacking(true);

        prepareDeclareBlockers();
        int toadIndex = gd.playerBattlefields.get(player2.getId()).indexOf(toad);
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(toadIndex, 0),
                new BlockerAssignment(toadIndex, 1)));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, toad)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, toad)).isEqualTo(2);
    }

    @Test
    @DisplayName("When Chub Toad is unblocked, it gets no boost")
    void unblockedNoBoost() {
        Permanent toad = addCreatureReady(player1, new ChubToad());
        toad.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(PermanentGrants.powerBoostOf(gd, toad)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, toad)).isZero();
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOff() {
        Permanent toad = addCreatureReady(player1, new ChubToad());
        toad.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, toad)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, toad)).isZero();
    }
}
