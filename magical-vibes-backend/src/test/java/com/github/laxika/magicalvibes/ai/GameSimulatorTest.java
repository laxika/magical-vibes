package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.ai.simulation.GameSimulator;
import com.github.laxika.magicalvibes.ai.simulation.SimulationAction;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GameSimulatorTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameData gd;
    private GameSimulator simulator;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gd = harness.getGameData();
        harness.skipMulligan();
        simulator = new GameSimulator(harness.getGameQueryService());
    }

    @Test
    @DisplayName("Legal actions in main phase include castable spells and pass")
    void legalActionsMainPhaseIncludesCastableSpells() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        gd.stack.clear();

        List<SimulationAction> actions = simulator.getLegalActions(gd, player1.getId());

        // Should have at least one PlayCard and one PassPriority
        assertThat(actions).anyMatch(a -> a instanceof SimulationAction.PlayCard);
        assertThat(actions).anyMatch(a -> a instanceof SimulationAction.PassPriority);
    }

    @Test
    @DisplayName("Legal actions in main phase with empty hand only has pass")
    void legalActionsMainPhaseEmptyHandOnlyPass() {
        harness.setHand(player1, List.of());
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        gd.stack.clear();

        List<SimulationAction> actions = simulator.getLegalActions(gd, player1.getId());

        assertThat(actions).hasSize(1);
        assertThat(actions.getFirst()).isInstanceOf(SimulationAction.PassPriority.class);
    }

    @Test
    @DisplayName("Apply action does not affect original GameData")
    void applyActionDoesNotAffectOriginal() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        gd.stack.clear();

        int origHandSize = gd.playerHands.get(player1.getId()).size();
        int origBattlefieldSize = gd.playerBattlefields.get(player1.getId()).size();

        // Apply action to a copy
        GameData copy = gd.deepCopy();
        simulator.applyAction(copy, player1.getId(), new SimulationAction.PlayCard(0, null));

        // Original unchanged
        assertThat(gd.playerHands.get(player1.getId())).hasSize(origHandSize);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(origBattlefieldSize);
    }

    @Test
    @DisplayName("Terminal detection: game over when player at 0 life")
    void terminalDetectionZeroLife() {
        gd.playerLifeTotals.put(player2.getId(), 0);
        assertThat(simulator.isTerminal(gd)).isTrue();
    }

    @Test
    @DisplayName("Terminal detection: game not over at positive life")
    void terminalDetectionPositiveLife() {
        assertThat(simulator.isTerminal(gd)).isFalse();
    }

    @Test
    @DisplayName("Evaluate returns higher score when AI is winning")
    void evaluateHigherWhenAiWinning() {
        gd.playerLifeTotals.put(player1.getId(), 20);
        gd.playerLifeTotals.put(player2.getId(), 5);

        double score = simulator.evaluate(gd, player1.getId());
        assertThat(score).isGreaterThan(0.5);
    }

    @Test
    @DisplayName("Evaluate returns lower score when AI is losing")
    void evaluateLowerWhenAiLosing() {
        gd.playerLifeTotals.put(player1.getId(), 3);
        gd.playerLifeTotals.put(player2.getId(), 20);

        double score = simulator.evaluate(gd, player1.getId());
        assertThat(score).isLessThan(0.5);
    }
}
