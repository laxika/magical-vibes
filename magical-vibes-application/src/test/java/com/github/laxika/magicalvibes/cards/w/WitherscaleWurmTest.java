package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WitherscaleWurmTest extends BaseCardTest {

    @Test
    @DisplayName("When the Wurm blocks a creature, that attacker gains wither")
    void blocksCreatureGrantsWither() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        addCreatureReady(player2, new WitherscaleWurm());

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(attacker.getGrantedKeywords()).contains(Keyword.WITHER);
    }

    @Test
    @DisplayName("When the Wurm becomes blocked by a creature, that blocker gains wither")
    void becomesBlockedGrantsWither() {
        Permanent wurm = addCreatureReady(player1, new WitherscaleWurm());
        wurm.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(blocker.getGrantedKeywords()).contains(Keyword.WITHER);
    }

    @Test
    @DisplayName("When the Wurm deals combat damage to an opponent, all its -1/-1 counters are removed")
    void dealingDamageRemovesMinusCounters() {
        harness.setLife(player2, 20);
        Permanent wurm = addCreatureReady(player1, new WitherscaleWurm());
        wurm.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 3);
        wurm.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of()); // no blockers — 6/6 hits the player
        harness.passBothPriorities(); // advance to combat damage; damage trigger goes on the stack
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
        assertThat(wurm.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    private void setupDeclareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }

    private void resolveAllTriggers() {
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }
}
