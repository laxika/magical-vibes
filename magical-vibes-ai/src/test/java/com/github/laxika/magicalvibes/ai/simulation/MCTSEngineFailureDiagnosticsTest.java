package com.github.laxika.magicalvibes.ai.simulation;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Covers the failed-iteration diagnostics of {@link MCTSEngine}: when the simulator
 * throws on every {@code applyAction}, the search must count the failures and record
 * their causes so the end-of-search summary can surface a silently broken search.
 */
@Tag("scryfall")
class MCTSEngineFailureDiagnosticsTest {

    private GameData gd;
    private UUID playerId;

    @BeforeEach
    void setUp() {
        GameTestHarness harness = new GameTestHarness();
        Player player1 = harness.getPlayer1();
        harness.skipMulligan();
        gd = harness.getGameData();
        playerId = player1.getId();
    }

    /**
     * Simulator whose {@code applyAction} always throws, so every MCTS iteration fails
     * during expansion. Two legal root actions keep the search from early-exiting.
     * All methods the engine touches are overridden, so the null dependencies passed
     * to the superclass are never used.
     */
    private static class ThrowingSimulator extends GameSimulator {
        ThrowingSimulator() {
            super(null, null, null, null, null, null);
        }

        @Override
        public List<SimulationAction> getLegalActions(GameData gd, UUID playerId) {
            return List.of(new SimulationAction.PassPriority(),
                    new SimulationAction.DeclareAttackers(List.of(0)));
        }

        @Override
        public void applyAction(GameData gd, UUID playerId, SimulationAction action) {
            throw new IllegalStateException("boom");
        }

        @Override
        public boolean isTerminal(GameData gd) {
            return false;
        }

        @Override
        public double evaluate(GameData gd, UUID aiPlayerId) {
            return 0.5;
        }
    }

    @Test
    @DisplayName("Every-iteration-failing search counts failures and records the cause")
    void failingSearchRecordsFailuresAndCauses() {
        MCTSEngine engine = new MCTSEngine(new ThrowingSimulator(), 42L, 50);

        engine.search(gd, playerId, 50);

        assertThat(engine.getLastSearchIterations()).isEqualTo(50);
        assertThat(engine.getLastSearchFailures()).isEqualTo(50);
        assertThat(engine.getLastSearchFailureCauses())
                .containsEntry("IllegalStateException: boom", 50)
                .hasSize(1);
    }

    @Test
    @DisplayName("Failure causes are reset per search, not accumulated across calls")
    void failureCausesResetPerSearch() {
        MCTSEngine engine = new MCTSEngine(new ThrowingSimulator(), 42L, 50);

        engine.search(gd, playerId, 50);
        engine.search(gd, playerId, 20);

        assertThat(engine.getLastSearchFailures()).isEqualTo(20);
        assertThat(engine.getLastSearchFailureCauses())
                .containsEntry("IllegalStateException: boom", 20);
    }

    @Test
    @DisplayName("Distinct failure causes are capped at 10 keys")
    void failureCausesCappedAtTenDistinctKeys() {
        MCTSEngine engine = new MCTSEngine(new ThrowingSimulator() {
            private int calls;

            @Override
            public void applyAction(GameData gd, UUID playerId, SimulationAction action) {
                throw new IllegalStateException("boom-" + calls++);
            }
        }, 42L, 25);

        engine.search(gd, playerId, 25);

        assertThat(engine.getLastSearchFailures()).isEqualTo(25);
        assertThat(engine.getLastSearchFailureCauses()).hasSize(10);
    }
}
