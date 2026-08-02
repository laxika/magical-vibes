package com.github.laxika.magicalvibes.ai;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.LoggingEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the fuzz watcher's failure capture. Events are fed in directly rather than
 * logged for real: the watcher's job is to decide what a log event means, and routing through
 * a live logger would make these assertions depend on whichever level the test config enables.
 *
 * <p>The rejection-context behaviour is what turns "PlayCard failed silently" from a report
 * that a cast failed into a report of which gate refused the mana, so it is asserted on the
 * exact message shapes the swallow sites emit.
 */
class FuzzLogWatcherTest {

    private static final String AI_LOGGER = "com.github.laxika.magicalvibes.ai.AiGameActions";
    private static final String ENGINE_LOGGER =
            "com.github.laxika.magicalvibes.service.spell.SpellCastingService";
    private static final String RANDOM_AI_LOGGER =
            "com.github.laxika.magicalvibes.ai.RandomAiDecisionEngine";

    private FuzzLogWatcher watcher;

    @BeforeEach
    void setUp() {
        watcher = new FuzzLogWatcher();
        watcher.setName("fuzz-log-watcher-test");
        watcher.setContext(((Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME))
                .getLoggerContext());
        watcher.start();
    }

    private void emit(String loggerName, Level level, String message) {
        Logger logger = (Logger) LoggerFactory.getLogger(loggerName);
        watcher.doAppend(new LoggingEvent(FuzzLogWatcherTest.class.getName(), logger, level,
                message, null, null));
    }

    private void emitPlayCardFailure() {
        emit(RANDOM_AI_LOGGER, Level.WARN,
                "Random AI: PlayCard failed silently in game g. Card='Phyrexian Devourer' index=0");
    }

    @Test
    @DisplayName("a legality warning carries the engine refusals that preceded it")
    void legalityFailureCarriesRejectionContext() {
        emit(AI_LOGGER, Level.INFO,
                "AI: engine rejected tapPermanent (index=3) in game g: Creature has summoning sickness");
        emitPlayCardFailure();

        List<String> failures = watcher.drainFailures();

        assertThat(failures).hasSize(1);
        assertThat(failures.getFirst())
                .startsWith("[LEGALITY] ")
                .contains("PlayCard failed silently")
                .contains("engine refusals leading up to this")
                .contains("engine rejected tapPermanent (index=3)")
                .contains("Creature has summoning sickness");
    }

    @Test
    @DisplayName("the engine's own not-playable diagnostic is captured as context, not as a failure")
    void notPlayableDiagnosticBecomesContext() {
        emit(ENGINE_LOGGER, Level.WARN,
                "Game g - Card 'Phyrexian Devourer' at index 0 not playable. pool={C=0}, playableIndices=[]");

        assertThat(watcher.drainFailures()).isEmpty();

        emitPlayCardFailure();
        assertThat(watcher.drainFailures().getFirst())
                .contains("not playable")
                .contains("playableIndices=[]");
    }

    @Test
    @DisplayName("refusals are quoted oldest first, so they read in the order the engine issued them")
    void rejectionContextKeepsEmissionOrder() {
        emit(AI_LOGGER, Level.INFO, "AI: engine rejected tapPermanent (index=1) in game g: first");
        emit(AI_LOGGER, Level.INFO, "AI: engine rejected tapPermanent (index=2) in game g: second");
        emitPlayCardFailure();

        String failure = watcher.drainFailures().getFirst();
        assertThat(failure.indexOf("index=1")).isLessThan(failure.indexOf("index=2"));
    }

    @Test
    @DisplayName("the refusal window is bounded so a long game cannot grow the report without limit")
    void rejectionContextIsBounded() {
        for (int i = 0; i < 200; i++) {
            emit(AI_LOGGER, Level.INFO, "AI: engine rejected tapPermanent (index=" + i + ") in game g: no");
        }
        emitPlayCardFailure();

        String failure = watcher.drainFailures().getFirst();
        assertThat(failure).doesNotContain("index=0)").contains("index=199)");
        assertThat(failure.lines().filter(line -> line.contains("engine rejected")).count())
                .isEqualTo(40);
    }

    @Test
    @DisplayName("an ERROR is captured with its refusal context too")
    void errorAlsoCarriesContext() {
        emit(AI_LOGGER, Level.INFO, "AI: engine rejected activateAbility (permanentIndex=2, abilityIndex=0) in game g: nope");
        emit(AI_LOGGER, Level.ERROR, "AI: Error sending message in game g");

        assertThat(watcher.drainFailures().getFirst())
                .startsWith("[ERROR] ")
                .contains("engine rejected activateAbility");
    }

    @Test
    @DisplayName("ordinary engine chatter is neither a failure nor context")
    void ignoresUnrelatedLogging() {
        emit(ENGINE_LOGGER, Level.INFO, "Game g - Alice taps Island");
        emit("org.springframework.beans.SomeBean", Level.ERROR, "unrelated framework error");
        emitPlayCardFailure();

        String failure = watcher.drainFailures().getFirst();
        assertThat(failure).doesNotContain("taps Island").doesNotContain("engine refusals leading up to this");
    }

    @Test
    @DisplayName("a quiet run drains nothing")
    void quietRunHasNoFailures() {
        emit(AI_LOGGER, Level.INFO, "AI: engine rejected passPriority in game g: not your priority");

        assertThat(watcher.drainFailures()).isEmpty();
    }
}
