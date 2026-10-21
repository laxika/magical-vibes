package com.github.laxika.magicalvibes.service.event;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.event.GameEventAudience;
import com.github.laxika.magicalvibes.model.event.GameEventBatch;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.model.event.VisualCueType;
import com.github.laxika.magicalvibes.service.JacksonConfig;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import com.github.laxika.magicalvibes.testutil.TestGameEventSubscriber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@CardUsed(GrizzlyBears.class)
class VisualCueServiceTest extends BaseCardTest {

    private static final ObjectMapper JSON = new JacksonConfig().objectMapper();

    private VisualCueService visualCues;
    private GameMutationCoordinator coordinator;
    private Permanent bears;

    @BeforeEach
    void setUpCues() {
        visualCues = GameTestEngineContext.get().getBean(VisualCueService.class);
        coordinator = GameTestEngineContext.get().getBean(GameMutationCoordinator.class);
        gd.aiPlayerIds.clear();
        bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.getConn1().clearMessages();
        harness.getConn2().clearMessages();
    }

    @Test
    void cueEmittedInAnActionReachesEveryPlayersStateMessage() {
        harness.inMutationScope(() -> {
            visualCues.emit(gd, VisualCueType.DESTROYED, VisualCueService.Source.of(bears),
                    bears.getId(), null, null);
            coordinator.invalidateAllPlayerViews(gd);
        });

        for (FakeConnection connection : List.of(harness.getConn1(), harness.getConn2())) {
            List<JsonNode> cues = projectedCues(connection);
            assertThat(cues).hasSize(1);
            JsonNode cue = cues.getFirst();
            assertThat(cue.get("type").asString()).isEqualTo("DESTROYED");
            assertThat(cue.get("sourceCardId").asString()).isEqualTo(bears.getCard().getId().toString());
            assertThat(cue.get("sourceName").asString()).isEqualTo("Grizzly Bears");
            assertThat(cue.get("sourceColors").get(0).asString()).isEqualTo("GREEN");
            assertThat(cue.get("targetPermanentId").asString()).isEqualTo(bears.getId().toString());
            assertThat(cue.get("targetPlayerId").isNull()).isTrue();
            assertThat(cue.get("amount").isNull()).isTrue();
        }
    }

    @Test
    void cuesKeepTheirOrderWithinOneAction() {
        harness.inMutationScope(() -> {
            visualCues.emit(gd, VisualCueType.DAMAGE_TO_PERMANENT, VisualCueService.Source.of(bears),
                    bears.getId(), null, 2);
            visualCues.emit(gd, VisualCueType.DIED, VisualCueService.Source.none(),
                    bears.getId(), null, null);
            coordinator.invalidateAllPlayerViews(gd);
        });

        assertThat(projectedCues(harness.getConn1()))
                .extracting(cue -> cue.get("type").asString())
                .containsExactly("DAMAGE_TO_PERMANENT", "DIED");
    }

    @Test
    void cueWithAPrivateAudienceReachesOnlyThatPlayer() {
        harness.inMutationScope(() -> {
            visualCues.emit(gd, VisualCueType.COUNTERS_ADDED, VisualCueService.Source.none(),
                    bears.getId(), null, 1, GameEventAudience.player(player2.getId()));
            coordinator.invalidateAllPlayerViews(gd);
        });

        assertThat(projectedCues(harness.getConn1())).isEmpty();
        assertThat(projectedCues(harness.getConn2())).hasSize(1);
    }

    @Test
    void faceDownSourceCarriesNoNameOrColors() {
        bears.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        List<GameEventBatch> batches = new ArrayList<>();

        try (AutoCloseable ignored = subscriber().subscribe(batches::add)) {
            harness.inMutationScope(() -> visualCues.emit(gd, VisualCueType.DAMAGE_TO_PLAYER,
                    VisualCueService.Source.of(bears), null, player1.getId(), 2));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        assertThat(cueFacts(batches)).singleElement().satisfies(cue -> {
            assertThat(cue.sourceCardId()).isEqualTo(bears.getCard().getId());
            assertThat(cue.sourceName()).isNull();
            assertThat(cue.sourceColors()).isEmpty();
        });
    }

    @Test
    void simulationEmitsNoCue() {
        List<GameEventBatch> batches = new ArrayList<>();
        gd.simulation = true;
        try (AutoCloseable ignored = subscriber().subscribe(batches::add)) {
            harness.inMutationScope(() -> {
                visualCues.emit(gd, VisualCueType.DESTROYED, VisualCueService.Source.of(bears),
                        bears.getId(), null, null);
                coordinator.invalidateAllPlayerViews(gd);
            });
        } catch (Exception e) {
            throw new IllegalStateException(e);
        } finally {
            gd.simulation = false;
        }

        assertThat(cueFacts(batches)).isEmpty();
        assertThat(projectedCues(harness.getConn1())).isEmpty();
        assertThat(projectedCues(harness.getConn2())).isEmpty();
    }

    @Test
    void emittingOutsideAMutationScopeIsANoOp() {
        assertThatCode(() -> visualCues.emit(gd, VisualCueType.DESTROYED,
                VisualCueService.Source.of(bears), bears.getId(), null, null))
                .doesNotThrowAnyException();
        assertThat(harness.getConn1().getSentMessages()).isEmpty();
    }

    @Test
    void stateMessagesWithoutCuesCarryAnEmptyList() {
        harness.inMutationScope(() -> coordinator.invalidateAllPlayerViews(gd));

        List<JsonNode> states = stateMessages(harness.getConn1());
        assertThat(states).isNotEmpty();
        assertThat(states).allSatisfy(state -> assertThat(state.get("visualCues").isEmpty()).isTrue());
    }

    private static TestGameEventSubscriber subscriber() {
        return GameTestEngineContext.get().getBean(TestGameEventSubscriber.class);
    }

    private static List<GameEventFact.VisualCue> cueFacts(List<GameEventBatch> batches) {
        return batches.stream()
                .flatMap(batch -> batch.events().stream())
                .map(envelope -> envelope.fact())
                .filter(GameEventFact.VisualCue.class::isInstance)
                .map(GameEventFact.VisualCue.class::cast)
                .toList();
    }

    private static List<JsonNode> stateMessages(FakeConnection connection) {
        return connection.getSentMessages().stream()
                .map(JSON::readTree)
                .filter(message -> "GAME_STATE".equals(message.path("type").asString()))
                .toList();
    }

    private static List<JsonNode> projectedCues(FakeConnection connection) {
        List<JsonNode> cues = new ArrayList<>();
        for (JsonNode state : stateMessages(connection)) {
            state.get("visualCues").forEach(cues::add);
        }
        return cues;
    }
}
