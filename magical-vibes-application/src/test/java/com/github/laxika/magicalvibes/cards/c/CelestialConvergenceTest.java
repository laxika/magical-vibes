package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CelestialConvergenceTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with seven omen counters")
    void entersWithSevenOmenCounters() {
        harness.setHand(player1, List.of(new CelestialConvergence()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        Permanent convergence = findPermanent(player1, "Celestial Convergence");
        assertThat(convergence.getCounterCount(CounterType.OMEN)).isEqualTo(7);
    }

    @Test
    @DisplayName("Removes one omen counter during its controller's upkeep")
    void removesOneOmenCounterDuringControllersUpkeep() {
        Permanent convergence = addWithOmenCounters(7);

        advanceToUpkeep(player2);
        assertThat(convergence.getCounterCount(CounterType.OMEN)).isEqualTo(7);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(convergence.getCounterCount(CounterType.OMEN)).isEqualTo(6);
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("The player with the highest life total wins after the last omen counter is removed")
    void highestLifePlayerWinsAfterLastCounter() {
        harness.setLife(player1, 30);
        harness.setLife(player2, 20);
        addWithOmenCounters(1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.gameResult).isEqualTo(GameEventFact.GameResult.WIN);
        assertThat(gameLogContains("Alice wins the game")).isTrue();
    }

    @Test
    @DisplayName("The opponent with the highest life total wins")
    void opponentWithHighestLifeWins() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 30);
        addWithOmenCounters(1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.gameResult).isEqualTo(GameEventFact.GameResult.WIN);
        assertThat(gameLogContains("Bob wins the game")).isTrue();
    }

    @Test
    @DisplayName("A tied highest life total draws the game")
    void tiedHighestLifeTotalsDraw() {
        addWithOmenCounters(1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.gameResult).isEqualTo(GameEventFact.GameResult.DRAW);
    }

    private Permanent addWithOmenCounters(int count) {
        Permanent convergence = harness.addToBattlefieldAndReturn(player1, new CelestialConvergence());
        convergence.setCounterCount(CounterType.OMEN, count);
        return convergence;
    }
}
