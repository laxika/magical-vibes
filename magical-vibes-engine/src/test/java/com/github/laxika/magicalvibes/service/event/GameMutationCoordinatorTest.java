package com.github.laxika.magicalvibes.service.event;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.event.GameEventAudience;
import com.github.laxika.magicalvibes.model.event.GameEventBatch;
import com.github.laxika.magicalvibes.model.event.GameEventEnvelope;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.model.event.GameEventKind;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GameMutationCoordinatorTest {

    private static final UUID GAME_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID PLAYER_1 = UUID.fromString("00000000-0000-0000-0000-000000000011");
    private static final UUID PLAYER_2 = UUID.fromString("00000000-0000-0000-0000-000000000012");
    private static final long ACTION_1 = 1L;
    private static final long ACTION_2 = 2L;

    @Test
    void outermostScopeOwnsOneOrderedBatchAndNestedScopesDoNotDoubleFlush() {
        GameData gameData = gameData();
        List<GameEventBatch> received = new ArrayList<>();
        GameMutationCoordinator coordinator = coordinator(received::add);

        GameEventFact.DecisionRequested decision = new GameEventFact.DecisionRequested(
                UUID.randomUUID(), PLAYER_1, GameEventFact.DecisionKind.INTERACTION);
        GameEventFact.PrivateReveal firstReveal = privateReveal("First");
        GameEventFact.PrivateReveal secondReveal = privateReveal("Second");

        coordinator.mutate(gameData, () -> {
            coordinator.emit(gameData,
                    new GameEventFact.StateInvalidated(GameEventFact.StateSection.BATTLEFIELD),
                    GameEventAudience.allPlayers());

            coordinator.mutate(gameData, () -> {
                coordinator.emit(gameData, decision, GameEventAudience.player(PLAYER_1));
                coordinator.emit(gameData,
                        new GameEventFact.StateInvalidated(GameEventFact.StateSection.STACK),
                        GameEventAudience.allPlayers());
                coordinator.emit(gameData, firstReveal, GameEventAudience.player(PLAYER_1));
            });

            coordinator.emit(gameData, secondReveal, GameEventAudience.player(PLAYER_1));
            coordinator.emit(gameData,
                    new GameEventFact.GameEnded(GameEventFact.GameResult.WIN, PLAYER_1),
                    GameEventAudience.allPlayers());
        });

        assertThat(received).hasSize(1);
        GameEventBatch batch = received.getFirst();
        assertThat(batch.causalActionId()).isEqualTo(ACTION_1);
        assertThat(batch.stateVersion()).isEqualTo(1);
        assertThat(batch.dispatchMode()).isEqualTo(GameEventBatch.DispatchMode.LIVE);
        assertThat(batch.events()).extracting(GameEventEnvelope::kind).containsExactly(
                GameEventKind.STATE_INVALIDATED,
                GameEventKind.DECISION_REQUESTED,
                GameEventKind.STATE_INVALIDATED,
                GameEventKind.PRIVATE_REVEAL,
                GameEventKind.PRIVATE_REVEAL,
                GameEventKind.GAME_ENDED);
        assertThat(batch.events()).extracting(GameEventEnvelope::sequence)
                .containsExactly(1L, 2L, 3L, 4L, 5L, 6L);
        assertThat(batch.events()).allSatisfy(event -> {
            assertThat(event.gameId()).isEqualTo(GAME_ID);
            assertThat(event.causalActionId()).isEqualTo(ACTION_1);
            assertThat(event.stateVersion()).isEqualTo(1);
        });

        GameEventFact.StateInvalidated firstInvalidation =
                (GameEventFact.StateInvalidated) batch.events().getFirst().fact();
        assertThat(firstInvalidation.sections()).containsExactly(
                GameEventFact.StateSection.BATTLEFIELD);
        assertThat(batch.events().get(1).fact()).isSameAs(decision);
        GameEventFact.StateInvalidated secondInvalidation =
                (GameEventFact.StateInvalidated) batch.events().get(2).fact();
        assertThat(secondInvalidation.sections()).containsExactly(
                GameEventFact.StateSection.STACK);
        assertThat(batch.events().get(3).fact()).isSameAs(firstReveal);
        assertThat(batch.events().get(4).fact()).isSameAs(secondReveal);

        coordinator.mutate(gameData, () ->
                coordinator.emit(gameData, new GameEventFact.DecisionRequested(
                        UUID.randomUUID(), PLAYER_2, GameEventFact.DecisionKind.BLOCKER_DECLARATION),
                        GameEventAudience.player(PLAYER_2)));

        assertThat(received).hasSize(2);
        assertThat(received.get(1).events()).singleElement()
                .extracting(GameEventEnvelope::sequence).isEqualTo(7L);
        assertThat(received.get(1).stateVersion()).isEqualTo(2);
    }

    @Test
    void subscribersRunAfterTheGameMonitorIsReleasedAndFailuresAreIsolated() {
        GameData gameData = gameData();
        AtomicBoolean mutationCompleted = new AtomicBoolean();
        AtomicBoolean healthySubscriberRan = new AtomicBoolean();
        AtomicInteger invocationCount = new AtomicInteger();

        GameEventSubscriber failing = batch -> {
            invocationCount.incrementAndGet();
            assertThat(Thread.holdsLock(gameData)).isFalse();
            assertThat(mutationCompleted).isTrue();
            throw new IllegalStateException("observer failed");
        };
        GameEventSubscriber healthy = batch -> {
            invocationCount.incrementAndGet();
            assertThat(Thread.holdsLock(gameData)).isFalse();
            assertThat(mutationCompleted).isTrue();
            healthySubscriberRan.set(true);
        };
        GameMutationCoordinator coordinator =
                new GameMutationCoordinator(new GameEventDispatcher(List.of(failing, healthy)));

        coordinator.mutate(gameData, () -> {
            gameData.turnNumber = 7;
            coordinator.emit(gameData,
                    new GameEventFact.StateInvalidated(GameEventFact.StateSection.TURN_AND_PRIORITY),
                    GameEventAudience.allPlayers());
            mutationCompleted.set(true);
        });

        assertThat(gameData.turnNumber).isEqualTo(7);
        assertThat(invocationCount).hasValue(2);
        assertThat(healthySubscriberRan).isTrue();
        assertThat(gameData.domainStateVersion()).isEqualTo(1);
        assertThat(gameData.domainEventSequence()).isEqualTo(1);
    }

    @Test
    void omittedAudienceIsInternalAndPrivateRevealCannotBecomePublic() {
        GameData gameData = gameData();
        List<GameEventBatch> received = new ArrayList<>();
        GameMutationCoordinator coordinator = coordinator(received::add);

        coordinator.mutate(gameData, () ->
                coordinator.emit(gameData,
                        new GameEventFact.StateInvalidated(GameEventFact.StateSection.GAME_LOG)));

        GameEventAudience audience = received.getFirst().events().getFirst().audience();
        assertThat(audience).isEqualTo(GameEventAudience.internalOnly());
        assertThat(audience.isVisibleTo(PLAYER_1)).isFalse();

        assertThatThrownBy(() -> coordinator.mutate(gameData, () ->
                coordinator.emit(gameData, privateReveal("Secret"), GameEventAudience.allPlayers())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("private audience");

        assertThatThrownBy(() -> coordinator.mutate(gameData, () ->
                coordinator.emit(gameData, new GameEventFact.DecisionRequested(
                                UUID.randomUUID(), PLAYER_1, GameEventFact.DecisionKind.INTERACTION),
                        GameEventAudience.allPlayers())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("private audience");
    }

    @Test
    void eventCollectionsAreDefensiveImmutableSnapshots() {
        List<GameEventFact.CardSnapshot> mutableCards = new ArrayList<>();
        mutableCards.add(cardSnapshot("Known"));
        GameEventFact.PrivateReveal reveal = new GameEventFact.PrivateReveal(
                UUID.randomUUID(), PLAYER_2, GameEventFact.RevealZone.HAND, mutableCards);
        mutableCards.add(cardSnapshot("Added later"));

        Set<UUID> mutableAudience = new java.util.HashSet<>();
        mutableAudience.add(PLAYER_1);
        GameEventAudience audience =
                new GameEventAudience(GameEventAudience.Visibility.PRIVATE, mutableAudience);
        mutableAudience.add(PLAYER_2);

        assertThat(reveal.cards()).extracting(GameEventFact.CardSnapshot::name).containsExactly("Known");
        assertThat(audience.playerIds()).containsExactly(PLAYER_1);
        assertThatThrownBy(() -> reveal.cards().add(cardSnapshot("Mutation")))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> audience.playerIds().add(PLAYER_2))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void privateRevealDiagnosticRenderingRedactsEveryCardIdentityField() {
        UUID hiddenCardId = UUID.fromString("00000000-0000-0000-0000-000000000099");
        GameEventFact.PrivateReveal reveal = new GameEventFact.PrivateReveal(
                UUID.fromString("00000000-0000-0000-0000-000000000088"),
                PLAYER_2,
                GameEventFact.RevealZone.HAND,
                List.of(new GameEventFact.CardSnapshot(
                        hiddenCardId, "Diagnostic Secret", "SEC", "999")));
        List<GameEventBatch> received = new ArrayList<>();
        GameMutationCoordinator coordinator = coordinator(received::add);

        GameData gameData = gameData();
        coordinator.mutate(gameData, () ->
                coordinator.emit(gameData, reveal, GameEventAudience.player(PLAYER_1)));

        String diagnostic = received.getLast().toString();
        assertThat(diagnostic)
                .contains("cardCount=1", "cards=<redacted>")
                .doesNotContain(
                        "Diagnostic Secret",
                        "SEC",
                        "999",
                        hiddenCardId.toString());
        assertThat(reveal.cards().getFirst().toString()).isEqualTo("CardSnapshot[<redacted>]");
    }

    @Test
    void simulationCommitsLocalOrderingButPerformsNoExternalDispatch() {
        GameData gameData = gameData();
        gameData.simulation = true;
        AtomicInteger subscriberInvocations = new AtomicInteger();
        GameMutationCoordinator coordinator = coordinator(batch -> subscriberInvocations.incrementAndGet());

        coordinator.mutate(gameData, () ->
                coordinator.emit(gameData, new GameEventFact.DecisionRequested(
                                UUID.randomUUID(), PLAYER_1, GameEventFact.DecisionKind.ATTACKER_DECLARATION),
                        GameEventAudience.player(PLAYER_1)));

        assertThat(subscriberInvocations).hasValue(0);
        assertThat(gameData.domainActionSequence()).isEqualTo(1);
        assertThat(gameData.domainStateVersion()).isEqualTo(1);
        assertThat(gameData.domainEventSequence()).isEqualTo(1);
    }

    @Test
    void outermostScopeMustBeginBeforeCallerAcquiresTheGameMonitor() {
        GameData gameData = gameData();
        GameMutationCoordinator coordinator = coordinator(batch -> {
        });

        synchronized (gameData) {
            assertThatThrownBy(() -> coordinator.mutate(gameData, () -> {
            }))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("before acquiring the GameData monitor");
        }
    }

    @Test
    void eventEmissionOutsideMutationScopeIsRejected() {
        GameData gameData = gameData();
        GameMutationCoordinator coordinator = coordinator(batch -> {
        });

        assertThatThrownBy(() -> coordinator.emit(
                gameData,
                new GameEventFact.StateInvalidated(GameEventFact.StateSection.BATTLEFIELD),
                GameEventAudience.allPlayers()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mutation scope");

        assertThat(gameData.domainActionSequence()).isZero();
        assertThat(gameData.domainStateVersion()).isZero();
        assertThat(gameData.domainEventSequence()).isZero();
    }

    @Test
    void failedMutationDoesNotDispatchOrAllocateCompletedActionMetadata() {
        GameData gameData = gameData();
        List<GameEventBatch> received = new ArrayList<>();
        AtomicInteger subscriberInvocations = new AtomicInteger();
        GameMutationCoordinator coordinator = coordinator(batch -> {
            subscriberInvocations.incrementAndGet();
            received.add(batch);
        });

        assertThatThrownBy(() -> coordinator.mutate(gameData, () -> {
            gameData.turnNumber = 9;
            coordinator.emit(gameData,
                    new GameEventFact.StateInvalidated(GameEventFact.StateSection.BATTLEFIELD),
                    GameEventAudience.allPlayers());
            throw new IllegalStateException("mutation failed");
        })).isInstanceOf(IllegalStateException.class);

        // The mutable engine is deliberately nontransactional: applied state remains, but facts
        // from the incomplete action are discarded because there is no stable observation point.
        assertThat(gameData.turnNumber).isEqualTo(9);
        assertThat(subscriberInvocations).hasValue(0);
        assertThat(gameData.domainActionSequence()).isZero();
        assertThat(gameData.domainStateVersion()).isZero();
        assertThat(gameData.domainEventSequence()).isZero();

        coordinator.mutate(gameData, () ->
                coordinator.emit(gameData,
                        new GameEventFact.StateInvalidated(GameEventFact.StateSection.GAME_LOG),
                        GameEventAudience.allPlayers()));

        assertThat(coordinator.isInAction(gameData)).isFalse();
        assertThat(received).singleElement().satisfies(batch -> {
            assertThat(batch.causalActionId()).isEqualTo(ACTION_1);
            assertThat(batch.stateVersion()).isEqualTo(1);
            assertThat(batch.events()).singleElement()
                    .extracting(GameEventEnvelope::sequence).isEqualTo(1L);
        });
    }

    @Test
    void differentGamesCanMutateConcurrentlyWithoutSharingScopeState() throws Exception {
        GameData firstGame = gameData();
        GameData secondGame = new GameData(
                UUID.fromString("00000000-0000-0000-0000-000000000002"),
                "second-event-test", PLAYER_2, "Player 2");
        GameMutationCoordinator coordinator = coordinator(batch -> {
        });
        CountDownLatch bothEntered = new CountDownLatch(2);
        CountDownLatch release = new CountDownLatch(1);

        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            Future<?> first = executor.submit(() -> coordinator.mutate(firstGame, () -> {
                bothEntered.countDown();
                await(release);
            }));
            Future<?> second = executor.submit(() -> coordinator.mutate(secondGame, () -> {
                bothEntered.countDown();
                await(release);
            }));

            assertThat(bothEntered.await(2, TimeUnit.SECONDS)).isTrue();
            release.countDown();
            first.get(2, TimeUnit.SECONDS);
            second.get(2, TimeUnit.SECONDS);
        }

        assertThat(firstGame.domainStateVersion()).isEqualTo(1);
        assertThat(secondGame.domainStateVersion()).isEqualTo(1);
        assertThat(firstGame.domainActionSequence()).isEqualTo(1);
        assertThat(secondGame.domainActionSequence()).isEqualTo(1);
    }

    @Test
    void sameGameActionsSerializeThroughDispatchAndKeepSequencesMonotonic() throws Exception {
        GameData gameData = gameData();
        List<GameEventBatch> received = java.util.Collections.synchronizedList(new ArrayList<>());
        GameMutationCoordinator coordinator = coordinator(received::add);
        CountDownLatch firstEntered = new CountDownLatch(1);
        CountDownLatch releaseFirst = new CountDownLatch(1);
        CountDownLatch secondAttempted = new CountDownLatch(1);
        CountDownLatch secondEntered = new CountDownLatch(1);

        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            Future<?> first = executor.submit(() -> coordinator.mutate(gameData, () -> {
                firstEntered.countDown();
                await(releaseFirst);
                coordinator.emit(gameData,
                        new GameEventFact.StateInvalidated(GameEventFact.StateSection.BATTLEFIELD),
                        GameEventAudience.allPlayers());
            }));
            assertThat(firstEntered.await(2, TimeUnit.SECONDS)).isTrue();

            Future<?> second = executor.submit(() -> {
                secondAttempted.countDown();
                coordinator.mutate(gameData, () -> {
                    secondEntered.countDown();
                    coordinator.emit(gameData,
                            new GameEventFact.StateInvalidated(GameEventFact.StateSection.STACK),
                            GameEventAudience.allPlayers());
                });
            });
            assertThat(secondAttempted.await(2, TimeUnit.SECONDS)).isTrue();
            assertThat(secondEntered.await(200, TimeUnit.MILLISECONDS)).isFalse();

            releaseFirst.countDown();
            first.get(2, TimeUnit.SECONDS);
            second.get(2, TimeUnit.SECONDS);
        }

        assertThat(received).extracting(GameEventBatch::causalActionId)
                .containsExactly(ACTION_1, ACTION_2);
        assertThat(received).extracting(GameEventBatch::stateVersion)
                .containsExactly(1L, 2L);
        assertThat(received).flatExtracting(GameEventBatch::events)
                .extracting(GameEventEnvelope::sequence)
                .containsExactly(1L, 2L);
    }

    @Test
    void oneActionCannotSpanTwoGames() {
        GameData firstGame = gameData();
        GameData secondGame = new GameData(
                UUID.fromString("00000000-0000-0000-0000-000000000002"),
                "second-event-test", PLAYER_2, "Player 2");
        GameMutationCoordinator coordinator = coordinator(batch -> {
        });

        assertThatThrownBy(() -> coordinator.mutate(firstGame,
                () -> coordinator.mutate(secondGame, () -> {
                })))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("multiple GameData");
    }

    private static GameMutationCoordinator coordinator(GameEventSubscriber subscriber) {
        return new GameMutationCoordinator(new GameEventDispatcher(List.of(subscriber)));
    }

    private static GameData gameData() {
        return new GameData(GAME_ID, "event-test", PLAYER_1, "Player 1");
    }

    private static GameEventFact.PrivateReveal privateReveal(String cardName) {
        return new GameEventFact.PrivateReveal(
                UUID.randomUUID(), PLAYER_2, GameEventFact.RevealZone.HAND, List.of(cardSnapshot(cardName)));
    }

    private static GameEventFact.CardSnapshot cardSnapshot(String name) {
        return new GameEventFact.CardSnapshot(UUID.randomUUID(), name, "TST", "1");
    }

    private static void await(CountDownLatch latch) {
        try {
            if (!latch.await(2, TimeUnit.SECONDS)) {
                throw new AssertionError("Timed out waiting for concurrent test");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError(e);
        }
    }
}
