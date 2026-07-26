package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.event.GameEventAudience;
import com.github.laxika.magicalvibes.model.event.GameEventBatch;
import com.github.laxika.magicalvibes.model.event.GameEventEnvelope;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.networking.message.InteractionPromptMessage;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.event.GameEventDispatcher;
import com.github.laxika.magicalvibes.service.event.GameEventSubscriber;
import com.github.laxika.magicalvibes.service.event.GameMutationCoordinator;
import com.github.laxika.magicalvibes.service.event.InteractionPromptProjectionRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Direct interaction-handler test boundary: records the decision fact after mutation and exposes
 * the canonical post-mutation prompt projection without involving a session transport.
 */
final class InteractionProjectionTestSupport {

    private final List<GameEventBatch> batches = new ArrayList<>();
    private final GameMutationCoordinator coordinator;
    private final InteractionPromptProjectionRegistry projections;
    private final InteractionHandlerRegistry registry;

    InteractionProjectionTestSupport() {
        this(mock(CardViewFactory.class));
    }

    InteractionProjectionTestSupport(CardViewFactory cardViewFactory) {
        GameEventSubscriber recorder = batches::add;
        coordinator = new GameMutationCoordinator(new GameEventDispatcher(List.of(recorder)));
        projections = new InteractionPromptProjectionRegistry(cardViewFactory);
        registry = new InteractionHandlerRegistry(() -> coordinator);
    }

    InteractionHandlerRegistry registry() {
        return registry;
    }

    void register(InteractionHandler<?> handler) {
        registry.register(handler);
    }

    InteractionPromptMessage begin(GameData gameData, PendingInteraction interaction) {
        int previousBatchCount = batches.size();
        coordinator.mutate(gameData, UUID.randomUUID(), () -> registry.begin(gameData, interaction));

        assertThat(batches).hasSize(previousBatchCount + 1);
        GameEventBatch batch = batches.getLast();
        assertThat(batch.events()).singleElement().satisfies(envelope -> {
            assertDecisionInstalled(gameData, interaction, envelope);
            assertThat(envelope.audience().visibility())
                    .isEqualTo(GameEventAudience.Visibility.PRIVATE);
            assertThat(envelope.audience().playerIds())
                    .containsExactly(expectedRecipient(gameData, interaction.decidingPlayerId()));
        });
        Object projected =
                projections.project(gameData, gameData.interaction.activeInteraction()).orElse(null);
        return projected == null ? null : (InteractionPromptMessage) projected;
    }

    InteractionPromptMessage projectedPrompt(GameData gameData) {
        return (InteractionPromptMessage) projections
                .project(gameData, gameData.interaction.activeInteraction())
                .orElseThrow();
    }

    private static void assertDecisionInstalled(
            GameData gameData,
            PendingInteraction expectedInteraction,
            GameEventEnvelope envelope) {
        assertThat(envelope.fact())
                .isInstanceOfSatisfying(GameEventFact.DecisionRequested.class, decision -> {
                    assertThat(gameData.interaction.activeInteraction()).isSameAs(expectedInteraction);
                    assertThat(gameData.interaction.activeInteraction().legalOptions()).isNotNull();
                    assertThat(decision.decisionId())
                            .isEqualTo(gameData.interaction.activeDecisionId());
                    assertThat(decision.decidingPlayerId())
                            .isEqualTo(expectedInteraction.decidingPlayerId());
                    assertThat(decision.decisionKind())
                            .isEqualTo(GameEventFact.DecisionKind.INTERACTION);
                    assertThat(decision.delivery())
                            .isEqualTo(GameEventFact.DecisionDelivery.OPENED);
                });
    }

    private static UUID expectedRecipient(GameData gameData, UUID decidingPlayerId) {
        if (decidingPlayerId.equals(gameData.mindControlledPlayerId)
                && gameData.mindControllerPlayerId != null) {
            return gameData.mindControllerPlayerId;
        }
        return decidingPlayerId;
    }
}
