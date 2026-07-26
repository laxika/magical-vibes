package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.event.GameEventBatch;
import com.github.laxika.magicalvibes.model.event.GameEventEnvelope;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.model.event.GameEventKind;
import com.github.laxika.magicalvibes.service.event.GameEventDispatcher;
import com.github.laxika.magicalvibes.service.event.GameMutationCoordinator;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GameLogServiceTest {

    private static final UUID PLAYER_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000011");

    @Test
    void multipleLogsAppendExactlyOnceAndRecordOrderedFactsInOneActionBatch() {
        GameData gameData = gameData();
        List<GameEventBatch> received = new ArrayList<>();
        GameMutationCoordinator coordinator = coordinator(received);
        GameLogService service = new GameLogService(coordinator);
        GameLogEntry first = GameLog.text("First.");
        GameLogEntry second = GameLog.text("Second.");

        coordinator.mutate(gameData, () -> {
            service.append(gameData, first);
            service.append(gameData, second);
        });

        assertThat(gameData.gameLog).containsExactly(first, second);
        assertThat(received).singleElement().satisfies(batch -> {
            assertThat(batch.events()).extracting(GameEventEnvelope::kind).containsExactly(
                    GameEventKind.GAME_LOG_APPENDED,
                    GameEventKind.STATE_INVALIDATED,
                    GameEventKind.GAME_LOG_APPENDED);
            assertThat(batch.events()).extracting(GameEventEnvelope::fact)
                    .filteredOn(GameEventFact.GameLogAppended.class::isInstance)
                    .containsExactly(
                            new GameEventFact.GameLogAppended(0),
                            new GameEventFact.GameLogAppended(1));
            assertThat(batch.events())
                    .filteredOn(event -> event.fact() instanceof GameEventFact.StateInvalidated)
                    .singleElement()
                    .satisfies(event -> assertThat(
                            ((GameEventFact.StateInvalidated) event.fact()).sections())
                            .containsExactly(GameEventFact.StateSection.GAME_LOG));
        });
    }

    @Test
    void failedNontransactionalActionRetainsPaidCostLogButDiscardsItsIncompleteFactBatch() {
        GameData gameData = gameData();
        List<GameEventBatch> received = new ArrayList<>();
        GameMutationCoordinator coordinator = coordinator(received);
        GameLogService service = new GameLogService(coordinator);
        GameLogEntry paidCost = GameLog.text("Player pays a cost.");

        assertThatThrownBy(() -> coordinator.mutate(gameData, () -> {
            service.append(gameData, paidCost);
            throw new IllegalStateException("later cost validation failed");
        })).isInstanceOf(IllegalStateException.class);

        assertThat(gameData.gameLog).containsExactly(paidCost);
        assertThat(received).isEmpty();
        assertThat(gameData.domainActionSequence()).isZero();
        assertThat(gameData.domainStateVersion()).isZero();
        assertThat(gameData.domainEventSequence()).isZero();
    }

    @Test
    void simulationAppendsLocallyAndAllocatesFactsWithoutDispatchingThem() {
        GameData gameData = gameData();
        gameData.simulation = true;
        List<GameEventBatch> received = new ArrayList<>();
        GameMutationCoordinator coordinator = coordinator(received);
        GameLogService service = new GameLogService(coordinator);
        GameLogEntry entry = GameLog.text("Simulated action.");

        coordinator.mutate(gameData, () -> service.append(gameData, entry));

        assertThat(gameData.gameLog).containsExactly(entry);
        assertThat(gameData.domainActionSequence()).isEqualTo(1);
        assertThat(gameData.domainStateVersion()).isEqualTo(1);
        assertThat(gameData.domainEventSequence()).isEqualTo(2);
        assertThat(received).isEmpty();
    }

    @Test
    void appendOutsideMutationScopeFailsBeforeMutatingAuthoritativeLog() {
        GameData gameData = gameData();
        GameLogService service = new GameLogService(coordinator(new ArrayList<>()));

        assertThatThrownBy(() -> service.append(gameData, GameLog.text("Invalid.")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mutation scope");
        assertThat(gameData.gameLog).isEmpty();
    }

    @Test
    void diagnosticFactDoesNotCopyStructuredCardIdentityOutOfAuthoritativeLog() {
        GameData gameData = gameData();
        List<GameEventBatch> received = new ArrayList<>();
        GameMutationCoordinator coordinator = coordinator(received);
        GameLogService service = new GameLogService(coordinator);
        Card hiddenCard = new Card();
        hiddenCard.setName("Diagnostic Secret");
        GameLogEntry entry = GameLog.builder()
                .text("Player reveals ")
                .card(hiddenCard)
                .text(".")
                .build();

        coordinator.mutate(gameData, () -> service.append(gameData, entry));

        assertThat(received.getFirst().toString())
                .contains("GameLogAppended[logIndex=0]")
                .doesNotContain("Diagnostic Secret", hiddenCard.getId().toString());
        assertThat(gameData.gameLog).containsExactly(entry);
    }

    private static GameData gameData() {
        return new GameData(UUID.randomUUID(), "game-log-test", PLAYER_ID, "Player");
    }

    private static GameMutationCoordinator coordinator(List<GameEventBatch> received) {
        return new GameMutationCoordinator(new GameEventDispatcher(List.of(received::add)));
    }
}
