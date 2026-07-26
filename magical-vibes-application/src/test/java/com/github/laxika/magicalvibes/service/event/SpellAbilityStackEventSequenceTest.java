package com.github.laxika.magicalvibes.service.event;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.event.GameEventBatch;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.model.event.GameEventKind;
import com.github.laxika.magicalvibes.service.effect.EffectResolutionService;
import com.github.laxika.magicalvibes.service.effect.normalfx.CopySupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandler;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.state.StateBasedActionService;
import com.github.laxika.magicalvibes.service.turn.TurnProgressionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

class SpellAbilityStackEventSequenceTest {

    private UUID playerId;
    private GameData gameData;
    private RecordingSubscriber events;
    private GameMutationCoordinator coordinator;
    private InteractionHandlerRegistry interactions;

    @BeforeEach
    void setUp() {
        playerId = UUID.randomUUID();
        gameData = new GameData(UUID.randomUUID(), "workflow-events", playerId, "Player");
        gameData.status = GameStatus.RUNNING;
        gameData.playerIds.add(playerId);
        gameData.orderedPlayerIds.add(playerId);
        gameData.playerIdToName.put(playerId, "Player");
        gameData.playerManaPools.put(playerId, new ManaPool());

        events = new RecordingSubscriber();
        coordinator = new GameMutationCoordinator(new GameEventDispatcher(List.of(events)));
        interactions = new InteractionHandlerRegistry(() -> coordinator);
        interactions.register(new NoopInteractionHandler<>(
                PendingInteraction.PermanentChoice.class));
        interactions.register(new NoopInteractionHandler<>(
                PendingInteraction.DiscardCostChoice.class));
        interactions.register(new NoopInteractionHandler<>(
                PendingInteraction.MayAbilityChoice.class));
    }

    @Test
    void castTargetOrCostDecisionRemainsBetweenAnnouncementAndStackState() {
        coordinator.mutate(gameData, () -> {
            coordinator.invalidateAllPlayerViews(gameData);
            interactions.begin(gameData, new PendingInteraction.PermanentChoice(
                    playerId, List.of(), List.of(), null, "Choose a target or cost permanent."));
            gameData.stack.add(spellEntry("Cast spell"));
            coordinator.invalidateAllPlayerViews(gameData);
        });

        assertThat(lastKinds()).containsExactly(
                GameEventKind.STATE_INVALIDATED,
                GameEventKind.DECISION_REQUESTED,
                GameEventKind.STATE_INVALIDATED);
        assertThat(gameData.stack).singleElement();
    }

    @Test
    void activatedAbilityCostDecisionPrecedesPaidAbilityStackState() {
        coordinator.mutate(gameData, () -> {
            interactions.begin(gameData, new PendingInteraction.DiscardCostChoice(
                    playerId, List.of(0), "Discard a card as an activation cost."));
            gameData.stack.add(new StackEntry(
                    StackEntryType.ACTIVATED_ABILITY,
                    namedCard("Ability source"),
                    playerId,
                    "Ability",
                    List.of()));
            coordinator.invalidateAllPlayerViews(gameData);
        });

        assertThat(lastKinds()).containsExactly(
                GameEventKind.DECISION_REQUESTED,
                GameEventKind.STATE_INVALIDATED);
    }

    @Test
    void manaFloatedForMayCostIsObservedWithoutReplacingTheDecision() {
        coordinator.mutate(gameData, () -> interactions.begin(
                gameData, new PendingInteraction.MayAbilityChoice(
                        playerId, "You may pay {1}.", "{1}")));
        UUID decisionId = gameData.interaction.activeDecisionId();

        coordinator.mutate(gameData, () -> {
            gameData.playerManaPools.get(playerId).add(ManaColor.COLORLESS, 1);
            coordinator.invalidateAllPlayerViews(gameData);
        });

        assertThat(events.batches).hasSize(2);
        assertThat(kinds(events.batches.getFirst())).containsExactly(
                GameEventKind.DECISION_REQUESTED);
        assertThat(kinds(events.batches.getLast())).containsExactly(
                GameEventKind.STATE_INVALIDATED);
        assertThat(gameData.interaction.activeDecisionId()).isEqualTo(decisionId);
        assertThat(gameData.playerManaPools.get(playerId).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void counteredFizzledAndCopiedEntriesExposeFinalStackStates() {
        StackEntry original = spellEntry("Original");
        gameData.stack.add(original);

        coordinator.mutate(gameData, () -> {
            gameData.stack.remove(original);
            coordinator.invalidateAllPlayerViews(gameData);
        });
        assertThat(gameData.stack).isEmpty();

        StackEntry fizzled = spellEntry("Fizzled");
        gameData.stack.add(fizzled);
        coordinator.mutate(gameData, () -> {
            gameData.stack.remove(fizzled);
            coordinator.invalidateAllPlayerViews(gameData);
        });
        assertThat(gameData.stack).isEmpty();

        StackEntry source = spellEntry("Copied");
        StackEntry copy = new CopySupport().createCopyStackEntry(
                source, new CopySupport().createCopyCard(source.getCard()), playerId, null);
        coordinator.mutate(gameData, () -> {
            gameData.stack.add(source);
            gameData.stack.add(copy);
            coordinator.invalidateAllPlayerViews(gameData);
        });

        assertThat(events.batches).hasSize(3);
        assertThat(events.batches).allSatisfy(batch ->
                assertThat(kinds(batch)).containsExactly(GameEventKind.STATE_INVALIDATED));
        assertThat(gameData.stack).containsExactly(source, copy);
        assertThat(copy.isCopy()).isTrue();
    }

    @Test
    void parkedResolutionPromptsThenPublishesOnlyAfterTheLaterResumeEpilogue() {
        StackEntry parked = spellEntry("Parked");
        gameData.pendingEffectResolutionEntry = parked;
        gameData.pendingEffectResolutionIndex = 1;

        coordinator.mutate(gameData, () -> interactions.begin(
                gameData, new PendingInteraction.MayAbilityChoice(
                        playerId, "Resume?", null)));

        PlayerInputService playerInputService = mock(PlayerInputService.class);
        EffectResolutionService effectResolutionService = mock(EffectResolutionService.class);
        doAnswer(invocation -> {
            gameData.pendingEffectResolutionEntry = null;
            gameData.pendingEffectResolutionIndex = 0;
            return null;
        }).when(effectResolutionService).resolveEffectsFrom(gameData, parked, 1);
        InputCompletionService completion = new InputCompletionService(
                playerInputService,
                coordinator,
                mock(TurnProgressionService.class),
                mock(StateBasedActionService.class),
                effectResolutionService);

        coordinator.mutate(gameData, () -> {
            gameData.interaction.clearAwaitingInput();
            completion.processMayAbilitiesThenAutoPass(gameData);
        });

        assertThat(events.batches).hasSize(2);
        assertThat(kinds(events.batches.getFirst())).containsExactly(
                GameEventKind.DECISION_REQUESTED);
        assertThat(kinds(events.batches.getLast())).containsExactly(
                GameEventKind.STATE_INVALIDATED);
        assertThat(gameData.pendingEffectResolutionEntry).isNull();
    }

    private StackEntry spellEntry(String name) {
        return new StackEntry(
                StackEntryType.INSTANT_SPELL,
                namedCard(name),
                playerId,
                name,
                List.of());
    }

    private static Card namedCard(String name) {
        Card card = new Card();
        card.setName(name);
        return card;
    }

    private List<GameEventKind> lastKinds() {
        return kinds(events.batches.getLast());
    }

    private static List<GameEventKind> kinds(GameEventBatch batch) {
        return batch.events().stream()
                .map(envelope -> envelope.fact().kind())
                .toList();
    }

    private static final class RecordingSubscriber implements GameEventSubscriber {
        private final List<GameEventBatch> batches = new ArrayList<>();

        @Override
        public void onGameEvents(GameEventBatch batch) {
            batches.add(batch);
        }
    }

    private record NoopInteractionHandler<T extends PendingInteraction>(
            Class<T> handledType) implements InteractionHandler<T> {

        @Override
        public Class<? extends InteractionAnswer> answerType() {
            return InteractionAnswer.class;
        }

        @Override
        public void prompt(GameData gameData, T interaction, UUID recipientId) {
        }

        @Override
        public void handleAnswer(
                GameData gameData, Player player, T interaction, InteractionAnswer answer) {
        }
    }
}
