package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.w.WildElephant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Brushwagg.class, WildElephant.class})
class BrushwaggTest extends BaseCardTest {

    @Test
    @DisplayName("When Brushwagg becomes blocked, it gets -2/+2 until end of turn")
    void becomesBlockedGetsBoost() {
        Permanent brushwagg = addCreatureReady(player1, new Brushwagg());
        addCreatureReady(player2, new WildElephant());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, brushwagg)).isEqualTo(-2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, brushwagg)).isEqualTo(2);
    }

    @Test
    @DisplayName("When Brushwagg blocks, it gets -2/+2 until end of turn")
    void blocksGetsBoost() {
        addCreatureReady(player1, new WildElephant());
        Permanent brushwagg = addCreatureReady(player2, new Brushwagg());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, brushwagg)).isEqualTo(-2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, brushwagg)).isEqualTo(2);
    }

    @Test
    @DisplayName("When Brushwagg is unblocked, it gets no boost")
    void unblockedNoBoost() {
        Permanent brushwagg = addCreatureReady(player1, new Brushwagg());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(PermanentGrants.powerBoostOf(gd, brushwagg)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, brushwagg)).isZero();
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOff() {
        Permanent brushwagg = addCreatureReady(player1, new Brushwagg());
        addCreatureReady(player2, new WildElephant());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, brushwagg)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, brushwagg)).isZero();
    }

    @Test
    @DisplayName("When Brushwagg becomes blocked by multiple creatures, it gets only one boost")
    void becomesBlockedByMultipleCreaturesGetsOneBoost() {
        Permanent brushwagg = addCreatureReady(player1, new Brushwagg());
        addCreatureReady(player2, new WildElephant());
        addCreatureReady(player2, new WildElephant());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, brushwagg)).isEqualTo(-2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, brushwagg)).isEqualTo(2);
    }

    @Test
    @DisplayName("Both Brushwaggs get their own boost only after their combat triggers resolve")
    void opposingBrushwaggsResolveTheirOwnTriggers() {
        Permanent attacker = addCreatureReady(player1, new Brushwagg());
        Permanent blocker = addCreatureReady(player2, new Brushwagg());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(2);
        assertThat(PermanentGrants.powerBoostOf(gd, attacker)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, attacker)).isZero();
        assertThat(PermanentGrants.powerBoostOf(gd, blocker)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, blocker)).isZero();

        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, attacker)).isEqualTo(-2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, attacker)).isEqualTo(2);
        assertThat(PermanentGrants.powerBoostOf(gd, blocker)).isEqualTo(-2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, blocker)).isEqualTo(2);
    }
}
