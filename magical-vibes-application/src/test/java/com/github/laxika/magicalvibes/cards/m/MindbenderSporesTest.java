package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MindbenderSporesTest extends BaseCardTest {

    @Test
    @DisplayName("Blocking a creature puts four fungus counters on it")
    void blockingPutsFourFungusCounters() {
        Permanent attacker = blockWithSpores();

        assertThat(attacker.getCounterCount(CounterType.FUNGUS)).isEqualTo(4);
    }

    @Test
    @DisplayName("The blocked creature stays tapped through its untap step and loses one fungus counter at upkeep")
    void blockedCreatureStaysTappedAndLosesOneCounterPerUpkeep() {
        Permanent attacker = blockWithSpores();
        attacker.tap();

        advanceToNextTurn(player1);
        advanceToNextTurn(player2);

        assertThat(attacker.isTapped()).isTrue();
        assertThat(attacker.getCounterCount(CounterType.FUNGUS)).isEqualTo(3);
    }

    @Test
    @DisplayName("Once the last fungus counter is gone the creature untaps again")
    void untapsAfterLastFungusCounterRemoved() {
        Permanent attacker = blockWithSpores();
        attacker.tap();
        attacker.setCounterCount(CounterType.FUNGUS, 1);

        advanceToNextTurn(player1);
        advanceToNextTurn(player2);

        assertThat(attacker.getCounterCount(CounterType.FUNGUS)).isZero();
        assertThat(attacker.isTapped()).isTrue();

        advanceToNextTurn(player1);
        advanceToNextTurn(player2);

        assertThat(attacker.isTapped()).isFalse();
    }

    @Test
    @DisplayName("No fungus counters are placed when Mindbender Spores never blocks")
    void noCountersWithoutBlocking() {
        Permanent attacker = addCreatureReady(player1, new GiantSpider());
        attacker.setAttacking(true);
        addCreatureReady(player2, new MindbenderSpores());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(attacker.getCounterCount(CounterType.FUNGUS)).isZero();
    }

    /** Player 1's Giant Spider attacks and is blocked by player 2's Mindbender Spores. */
    private Permanent blockWithSpores() {
        Permanent attacker = addCreatureReady(player1, new GiantSpider());
        attacker.setAttacking(true);
        addCreatureReady(player2, new MindbenderSpores());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        return attacker;
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
