package com.github.laxika.magicalvibes.ai;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.spi.FilterReply;
import com.github.laxika.magicalvibes.ai.simulation.GameSimulator;
import com.github.laxika.magicalvibes.ai.simulation.MCTSEngine;
import com.github.laxika.magicalvibes.ai.simulation.SimulationAction;
import com.github.laxika.magicalvibes.ai.simulation.SimulationLogSuppressor;
import com.github.laxika.magicalvibes.model.GameData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SimulationLogSuppressorTest {

    private final SimulationLogSuppressor filter = new SimulationLogSuppressor();
    private final Logger engineLogger =
            (Logger) LoggerFactory.getLogger("com.github.laxika.magicalvibes.service.GameService");
    private final Logger aiLogger =
            (Logger) LoggerFactory.getLogger("com.github.laxika.magicalvibes.ai.simulation.MCTSEngine");

    @AfterEach
    void clearMdc() {
        MDC.remove(SimulationLogSuppressor.MDC_KEY);
    }

    @Test
    @DisplayName("Engine WARN/INFO/DEBUG is denied while the simulation flag is set")
    void engineSubErrorDeniedDuringSimulation() {
        SimulationLogSuppressor.enterSimulation();

        assertThat(filter.decide(null, engineLogger, Level.WARN, "msg", null, null))
                .isEqualTo(FilterReply.DENY);
        assertThat(filter.decide(null, engineLogger, Level.INFO, "msg", null, null))
                .isEqualTo(FilterReply.DENY);
        assertThat(filter.decide(null, engineLogger, Level.DEBUG, "msg", null, null))
                .isEqualTo(FilterReply.DENY);
    }

    @Test
    @DisplayName("Engine ERROR passes through even during simulation")
    void errorPassesThroughDuringSimulation() {
        SimulationLogSuppressor.enterSimulation();

        assertThat(filter.decide(null, engineLogger, Level.ERROR, "msg", null, null))
                .isEqualTo(FilterReply.NEUTRAL);
    }

    @Test
    @DisplayName("Engine INFO passes through on threads not running a simulation")
    void engineInfoPassesThroughOutsideSimulation() {
        assertThat(filter.decide(null, engineLogger, Level.INFO, "msg", null, null))
                .isEqualTo(FilterReply.NEUTRAL);
    }

    @Test
    @DisplayName("Loggers outside the engine service package are unaffected during simulation")
    void nonEngineLoggersUnaffectedDuringSimulation() {
        SimulationLogSuppressor.enterSimulation();

        assertThat(filter.decide(null, aiLogger, Level.DEBUG, "msg", null, null))
                .isEqualTo(FilterReply.NEUTRAL);
    }

    @Test
    @DisplayName("install() registers the filter exactly once")
    void installIsIdempotent() {
        SimulationLogSuppressor.install();
        SimulationLogSuppressor.install();

        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        long installed = context.getTurboFilterList().stream()
                .filter(f -> f instanceof SimulationLogSuppressor)
                .count();
        assertThat(installed).isEqualTo(1);
    }

    @Test
    @DisplayName("search() sets the simulation flag for simulator calls and clears it afterwards")
    void searchScopesSimulationFlagToTheSearch() {
        GameSimulator simulator = mock(GameSimulator.class);
        List<String> observedFlags = new ArrayList<>();
        // Two rollout-scorable actions so search() proceeds past the single-action shortcut
        List<SimulationAction> actions = List.of(
                new SimulationAction.PassPriority(),
                new SimulationAction.MayAbilityChoice(true));
        when(simulator.getLegalActions(any(), any())).thenAnswer(inv -> {
            observedFlags.add(MDC.get(SimulationLogSuppressor.MDC_KEY));
            return new ArrayList<>(actions);
        });
        when(simulator.isTerminal(any())).thenReturn(false);
        when(simulator.evaluate(any(), any())).thenAnswer(inv -> {
            observedFlags.add(MDC.get(SimulationLogSuppressor.MDC_KEY));
            return 0.5;
        });

        MCTSEngine engine = new MCTSEngine(simulator, 42L, 5);
        GameData gd = new GameData(UUID.randomUUID(), "sim-log-test", UUID.randomUUID(), "tester");
        SimulationAction action = engine.search(gd, UUID.randomUUID(), 5);

        assertThat(action).isNotNull();
        assertThat(observedFlags).isNotEmpty();
        assertThat(observedFlags).allSatisfy(flag -> assertThat(flag).isEqualTo("1"));
        assertThat(MDC.get(SimulationLogSuppressor.MDC_KEY)).isNull();
    }
}
