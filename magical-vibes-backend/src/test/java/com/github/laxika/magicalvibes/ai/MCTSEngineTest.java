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

class MCTSEngineTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameData gd;
    private GameSimulator simulator;
    private MCTSEngine engine;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gd = harness.getGameData();
        harness.skipMulligan();
        simulator = new GameSimulator(harness.getGameQueryService());
        engine = new MCTSEngine(simulator);
    }

    @Test
    @DisplayName("Budget of 1 still returns a valid action")
    void budgetOneReturnsValidAction() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        gd.stack.clear();

        SimulationAction action = engine.search(gd, player1.getId(), 1);

        assertThat(action).isNotNull();
        assertThat(action).isInstanceOfAny(
                SimulationAction.PlayCard.class,
                SimulationAction.PassPriority.class
        );
    }

    @Test
    @DisplayName("MCTS chooses to play a spell when mana is available")
    void mctsChoosesToPlaySpell() {
        // Offer choice between Grizzly Bears (2/2) and Serra Angel (4/4 flying vigilance)
        harness.setHand(player1, List.of(new GrizzlyBears(), new SerraAngel()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        gd.stack.clear();

        SimulationAction action = engine.search(gd, player1.getId(), 100);

        // MCTS should choose to play a spell rather than pass
        assertThat(action).isNotNull();
        assertThat(action).isInstanceOf(SimulationAction.PlayCard.class);
    }

    @Test
    @DisplayName("Budget of 100 completes within reasonable time")
    void budgetCompletesInTime() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        gd.stack.clear();

        long start = System.currentTimeMillis();
        SimulationAction action = engine.search(gd, player1.getId(), 100);
        long elapsed = System.currentTimeMillis() - start;

        assertThat(action).isNotNull();
        assertThat(elapsed).isLessThan(5000); // Should complete well under 5 seconds
    }

    @Test
    @DisplayName("With only pass available, MCTS returns pass")
    void onlyPassAvailable() {
        harness.setHand(player1, List.of());
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        gd.stack.clear();

        SimulationAction action = engine.search(gd, player1.getId(), 50);

        assertThat(action).isInstanceOf(SimulationAction.PassPriority.class);
    }
}
