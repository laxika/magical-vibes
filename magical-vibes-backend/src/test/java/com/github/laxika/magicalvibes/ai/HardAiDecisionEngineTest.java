package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.ai.simulation.GameSimulator;
import com.github.laxika.magicalvibes.ai.simulation.MCTSEngine;
import com.github.laxika.magicalvibes.ai.simulation.SimulationAction;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
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

class HardAiDecisionEngineTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gd = harness.getGameData();
        harness.skipMulligan();
    }

    @Test
    @DisplayName("MCTS search completes within time budget for spell casting")
    void mctsSearchCompletesInTimeBudget() {
        harness.setHand(player1, List.of(new GrizzlyBears(), new SerraAngel()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        gd.stack.clear();

        GameSimulator simulator = new GameSimulator(harness.getGameQueryService());
        MCTSEngine engine = new MCTSEngine(simulator);

        long start = System.currentTimeMillis();
        SimulationAction action = engine.search(gd, player1.getId(), 500);
        long elapsed = System.currentTimeMillis() - start;

        assertThat(action).isNotNull();
        assertThat(elapsed).isLessThan(3000); // Must complete within reasonable time
    }

    @Test
    @DisplayName("MCTS search completes within time budget for attacker declaration")
    void mctsSearchCompletesForAttackers() {
        // Add some creatures to battlefield
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new SerraAngel());
        // Make them not summoning sick
        gd.playerBattlefields.get(player1.getId()).forEach(p -> p.setSummoningSick(false));

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.forceActivePlayer(player1);
        gd.interaction.beginAttackerDeclaration(player1.getId());

        GameSimulator simulator = new GameSimulator(harness.getGameQueryService());
        MCTSEngine engine = new MCTSEngine(simulator);

        long start = System.currentTimeMillis();
        SimulationAction action = engine.search(gd, player1.getId(), 200);
        long elapsed = System.currentTimeMillis() - start;

        assertThat(action).isNotNull();
        assertThat(action).isInstanceOf(SimulationAction.DeclareAttackers.class);
        assertThat(elapsed).isLessThan(3000);
    }

    @Test
    @DisplayName("HardAiDecisionEngine constructor initializes without errors")
    void hardEngineConstructorWorks() {
        HardAiDecisionEngine engine = new HardAiDecisionEngine(
                gd.id, player1, harness.getGameRegistry(),
                harness.getMessageHandler(), harness.getGameQueryService());
        assertThat(engine).isNotNull();
    }
}
