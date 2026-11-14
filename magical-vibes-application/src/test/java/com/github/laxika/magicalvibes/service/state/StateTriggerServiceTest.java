package com.github.laxika.magicalvibes.service.state;

import com.github.laxika.magicalvibes.testutil.TestStackDoor;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.Card;
import static org.mockito.Mockito.mock;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.StateTriggerKey;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.StateTriggerEffect;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.StateTrigger;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityRequest;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService;
import static org.mockito.Mockito.lenient;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class StateTriggerServiceTest {

    @Mock
    private GameLogService gameLogService;

    @Mock
    private PredicateEvaluationService predicateEvaluationService;

    @Mock
    private TriggeredAbilityStackService triggeredAbilityStackService;

    private StateTriggerService sut;

    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;

    @BeforeEach
    void setUp() {
        player1Id = UUID.randomUUID();
        player2Id = UUID.randomUUID();
        gd = new GameData(UUID.randomUUID(), "test", player1Id, "Player1");
        gd.playerIds.add(player1Id);
        gd.playerIds.add(player2Id);
        gd.orderedPlayerIds.add(player1Id);
        gd.orderedPlayerIds.add(player2Id);
        gd.playerIdToName.put(player1Id, "Player1");
        gd.playerIdToName.put(player2Id, "Player2");
        gd.status = GameStatus.RUNNING;
        gd.activePlayerId = player1Id;
        gd.playerBattlefields.put(player1Id, new ArrayList<>());
        gd.playerBattlefields.put(player2Id, new ArrayList<>());
        gd.playerManaPools.put(player1Id, new ManaPool());
        gd.playerManaPools.put(player2Id, new ManaPool());
        sut = new StateTriggerService(gameLogService, predicateEvaluationService, null,
                triggeredAbilityStackService, null);
        lenient().when(triggeredAbilityStackService.put(eq(gd), any()))
                .thenAnswer(invocation -> putOnStack(invocation.getArgument(1)));
    }

    /**
     * The stack door as these tests need it: the ability's entry of its effects as written, its source and controller,
     * stamped with the request's record, on the stack.
     */
    private boolean putOnStack(TriggeredAbilityRequest request) {
        StackEntry entry = TestStackDoor.triggered(request.sourceCard(),
                request.controllerId(), request.description(), new ArrayList<>(request.ability().effects().stream()
                .map(EffectRegistration::effect).toList()), null, request.sourcePermanentId());
        request.record().stamp(entry);
        gd.stack.add(entry);
        return true;
    }

    private static Card createCardWithStateTrigger(String name, StateTrigger trigger, CardEffect... effects) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ENCHANTMENT);
        card.addTrigger(trigger, effects);
        return card;
    }

    @Nested
    @DisplayName("checkStateTriggers")
    class CheckStateTriggers {

        @Test
        @DisplayName("Fires a state-triggered ability a static ability grants the permanent")
        void firesGrantedStateTriggeredAbility() {
            GameQueryService gameQueryService = mock(GameQueryService.class);
            lenient().when(gameQueryService.withQueryScope(org.mockito.ArgumentMatchers.any(),
                            org.mockito.ArgumentMatchers.any()))
                    .thenAnswer(invocation -> invocation.<java.util.function.Supplier<?>>getArgument(1).get());
            StateTriggerService service = new StateTriggerService(gameLogService, predicateEvaluationService,
                    gameQueryService, triggeredAbilityStackService, null);
            Card card = new Card();
            card.setName("Returned Creature");
            card.setType(CardType.CREATURE);
            Permanent perm = new Permanent(card);
            gd.playerBattlefields.get(player1Id).add(perm);
            TriggeredAbility granted = new TriggeredAbility(Triggers.state((gameData, p, controllerId) -> true),
                    new GainLifeEffect(1));
            when(gameQueryService.getGrantedEffects(gd, perm))
                    .thenReturn(List.of(new GrantTriggeredAbilityEffect(granted, GrantScope.TARGET)));

            service.checkStateTriggers(gd);

            assertThat(gd.stack).singleElement().satisfies(entry -> {
                assertThat(entry.getEffectsToResolve()).containsExactly(new GainLifeEffect(1));
                assertThat(entry.getSourcePermanentId()).isEqualTo(perm.getId());
                assertThat(entry.getDescription()).isEqualTo("Returned Creature's state-triggered ability");
            });
        }

        @Test
        @DisplayName("Fires trigger when predicate is satisfied and not already on stack")
        void firesWhenPredicateSatisfied() {
            List<CardEffect> effects = List.of(new GainLifeEffect(3));
            StateTrigger trigger = Triggers.state((gameData, perm, controllerId) -> true);
            Card card = createCardWithStateTrigger("Trigger Card", trigger, effects.toArray(CardEffect[]::new));
            Permanent perm = new Permanent(card);
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.checkStateTriggers(gd);

            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.getFirst();
            assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(entry.getCard()).isEqualTo(card);
            assertThat(entry.getControllerId()).isEqualTo(player1Id);
            assertThat(entry.getDescription()).isEqualTo("Trigger Card's state-triggered ability");
            assertThat(entry.getEffectsToResolve()).isEqualTo(effects);
            assertThat(entry.getSourcePermanentId()).isEqualTo(perm.getId());
            assertThat(entry.getStateTriggerEffectIndex()).isZero();
            assertThat(gd.stateTriggerOnStack).contains(new StateTriggerKey(perm.getId(), 0));
            verify(gameLogService).append(eq(gd), argThat((GameLogEntry e) -> e.plainText().equals("Trigger Card's state-triggered ability triggers.")));
        }

        @Test
        @DisplayName("Stores a selected non-targeting permanent on the triggered stack entry")
        void storesSelectedPermanentReference() {
            Card selectedCard = new Card();
            selectedCard.setType(CardType.CREATURE);
            Permanent selected = new Permanent(selectedCard);
            gd.playerBattlefields.get(player2Id).add(selected);
            StateTrigger trigger = Triggers.state((gameData, perm, controllerId) -> true)
                    .referencing(new PermanentIsCreaturePredicate());
            Permanent source = new Permanent(createCardWithStateTrigger("Trigger Card", trigger, new GainLifeEffect(1)));
            when(predicateEvaluationService.matchesPermanentPredicate(eq(source),
                    eq(trigger.referencedPermanentPredicate()), any())).thenReturn(false);
            when(predicateEvaluationService.matchesPermanentPredicate(eq(selected),
                    eq(trigger.referencedPermanentPredicate()), any())).thenReturn(true);
            gd.playerBattlefields.get(player1Id).add(source);

            sut.checkStateTriggers(gd);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getTriggeringPermanentId()).isEqualTo(selected.getId());
        }

        @Test
        @DisplayName("Source predicate is evaluated through the layer-aware predicate evaluator")
        void firesFromSourcePredicate() {
            StateTrigger trigger = Triggers.sourceState(new PermanentHasKeywordPredicate(Keyword.FLYING));
            Card card = createCardWithStateTrigger("Source Predicate Card", trigger, new GainLifeEffect(3));
            Permanent perm = new Permanent(card);
            gd.playerBattlefields.get(player1Id).add(perm);
            when(predicateEvaluationService.matchesPermanentPredicate(eq(perm),
                    eq(trigger.sourcePredicate()), any())).thenReturn(true);

            sut.checkStateTriggers(gd);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getSourcePermanentId()).isEqualTo(perm.getId());
        }

        @Test
        @DisplayName("Does not fire when the source predicate does not match")
        void doesNotFireWhenSourcePredicateUnmatched() {
            StateTrigger trigger = Triggers.sourceState(new PermanentHasKeywordPredicate(Keyword.FLYING));
            Permanent perm = new Permanent(createCardWithStateTrigger("Source Predicate Card", trigger, new GainLifeEffect(3)));
            gd.playerBattlefields.get(player1Id).add(perm);
            when(predicateEvaluationService.matchesPermanentPredicate(eq(perm),
                    eq(trigger.sourcePredicate()), any())).thenReturn(false);

            sut.checkStateTriggers(gd);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Does not fire when predicate is not satisfied")
        void doesNotFireWhenPredicateNotSatisfied() {
            StateTrigger trigger = Triggers.state((gameData, perm, controllerId) -> false);
            Card card = createCardWithStateTrigger("No Trigger Card", trigger, new GainLifeEffect(3));
            Permanent perm = new Permanent(card);
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.checkStateTriggers(gd);

            assertThat(gd.stack).isEmpty();
            assertThat(gd.stateTriggerOnStack).doesNotContain(new StateTriggerKey(perm.getId(), 0));
            verifyNoInteractions(gameLogService);
        }

        @Test
        @DisplayName("Does not retrigger while already on stack — CR 603.8")
        void doesNotRetriggerWhileOnStack() {
            StateTrigger trigger = Triggers.state((gameData, perm, controllerId) -> true);
            Card card = createCardWithStateTrigger("Already On Stack", trigger, new GainLifeEffect(3));
            Permanent perm = new Permanent(card);
            gd.playerBattlefields.get(player1Id).add(perm);
            gd.stateTriggerOnStack.add(new StateTriggerKey(perm.getId(), 0));

            sut.checkStateTriggers(gd);

            assertThat(gd.stack).isEmpty();
            verifyNoInteractions(gameLogService);
        }

        @Test
        @DisplayName("Checks both players in APNAP order")
        void checksPlayersInApnapOrder() {
            List<CardEffect> effects1 = List.of(new DealDamageToAnyTargetEffect(1));
            StateTrigger trigger1 = Triggers.state((gameData, perm, controllerId) -> true);
            Card card1 = createCardWithStateTrigger("P1 Card", trigger1, effects1.toArray(CardEffect[]::new));
            Permanent perm1 = new Permanent(card1);
            gd.playerBattlefields.get(player1Id).add(perm1);

            List<CardEffect> effects2 = List.of(new GainLifeEffect(2));
            StateTrigger trigger2 = Triggers.state((gameData, perm, controllerId) -> true);
            Card card2 = createCardWithStateTrigger("P2 Card", trigger2, effects2.toArray(CardEffect[]::new));
            Permanent perm2 = new Permanent(card2);
            gd.playerBattlefields.get(player2Id).add(perm2);

            sut.checkStateTriggers(gd);

            assertThat(gd.stack).hasSize(2);
            assertThat(gd.stack.get(0).getControllerId()).isEqualTo(player1Id);
            assertThat(gd.stack.get(1).getControllerId()).isEqualTo(player2Id);
        }

        @Test
        @DisplayName("Handles multiple permanents with triggers for same player")
        void handlesMultiplePermanentsForSamePlayer() {
            StateTrigger trigger1 = Triggers.state((gameData, perm, controllerId) -> true);
            StateTrigger trigger2 = Triggers.state((gameData, perm, controllerId) -> true);
            Card card1 = createCardWithStateTrigger("Card A", trigger1, new GainLifeEffect(1));
            Card card2 = createCardWithStateTrigger("Card B", trigger2, new GainLifeEffect(2));
            Permanent perm1 = new Permanent(card1);
            Permanent perm2 = new Permanent(card2);
            gd.playerBattlefields.get(player1Id).add(perm1);
            gd.playerBattlefields.get(player1Id).add(perm2);

            sut.checkStateTriggers(gd);

            assertThat(gd.stack).hasSize(2);
            assertThat(gd.stateTriggerOnStack).containsExactlyInAnyOrder(
                    new StateTriggerKey(perm1.getId(), 0),
                    new StateTriggerKey(perm2.getId(), 0));
        }

        @Test
        @DisplayName("Skips permanents without state-triggered abilities")
        void skipsPermanentsWithoutStateTriggers() {
            Card card = new Card();
            card.setName("Vanilla Creature");
            card.setType(CardType.CREATURE);
            Permanent perm = new Permanent(card);
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.checkStateTriggers(gd);

            assertThat(gd.stack).isEmpty();
            assertThat(gd.stateTriggerOnStack).isEmpty();
            verifyNoInteractions(gameLogService);
        }

        @Test
        @DisplayName("Rejects a state trigger without a game state, or with a StateTriggerEffect")
        void rejectsStateTriggerWithoutGameState() {
            Card card = new Card();
            card.setName("Odd Card");
            card.setType(CardType.ENCHANTMENT);

            assertThatThrownBy(() -> card.addTrigger(Triggers.state(), new GainLifeEffect(1)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("A state trigger needs its game state");
            assertThatThrownBy(() -> card.addTrigger(Triggers.state((gameData, perm, controllerId) -> true),
                    new StateTriggerEffect((gameData, perm, controllerId) -> true, List.of(new GainLifeEffect(1)),
                            "Old form")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("not a StateTriggerEffect");
        }

        @Test
        @DisplayName("Hands the ability whole to the stack door and tracks it only once it triggered")
        void tracksOnlyAnAbilityTheDoorPut() {
            StateTrigger trigger = Triggers.state((gameData, perm, controllerId) -> true);
            Card card = new Card();
            card.setName("Intervening Card");
            card.setType(CardType.ENCHANTMENT);
            card.addTrigger(trigger, TriggerOptions.DEFAULT.withInterveningIf(new ControllerTurn()),
                    new GainLifeEffect(1));
            Permanent perm = new Permanent(card);
            gd.playerBattlefields.get(player1Id).add(perm);
            // The door judges the intervening "if" (CR 603.4): here it doesn't hold.
            when(triggeredAbilityStackService.put(eq(gd), any())).thenReturn(false);

            sut.checkStateTriggers(gd);

            assertThat(gd.stateTriggerOnStack).isEmpty();
            ArgumentCaptor<TriggeredAbilityRequest> request = ArgumentCaptor.forClass(TriggeredAbilityRequest.class);
            verify(triggeredAbilityStackService).put(eq(gd), request.capture());
            assertThat(request.getValue().ability()).isEqualTo(card.getTriggeredAbilities().getFirst());
            assertThat(request.getValue().sourcePermanent()).isSameAs(perm);
            assertThat(request.getValue().controllerId()).isEqualTo(player1Id);

            when(triggeredAbilityStackService.put(eq(gd), any()))
                    .thenAnswer(invocation -> putOnStack(invocation.getArgument(1)));

            sut.checkStateTriggers(gd);

            assertThat(gd.stack).singleElement().satisfies(entry ->
                    assertThat(entry.getEffectsToResolve()).containsExactly(new GainLifeEffect(1)));
            assertThat(gd.stateTriggerOnStack).contains(new StateTriggerKey(perm.getId(), 0));
        }

        @Test
        @DisplayName("Fires all state triggers on same permanent independently")
        void firesAllTriggersOnSamePermanent() {
            StateTrigger trigger1 = Triggers.state((gameData, perm, controllerId) -> true);
            StateTrigger trigger2 = Triggers.state((gameData, perm, controllerId) -> true);
            Card card = new Card();
            card.setName("Multi Trigger Card");
            card.setType(CardType.ENCHANTMENT);
            card.addTrigger(trigger1, new GainLifeEffect(1));
            card.addTrigger(trigger2, new GainLifeEffect(2));
            Permanent perm = new Permanent(card);
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.checkStateTriggers(gd);

            assertThat(gd.stack).hasSize(2);
            assertThat(gd.stack.get(0).getEffectsToResolve()).containsExactly(new GainLifeEffect(1));
            assertThat(gd.stack.get(0).getStateTriggerEffectIndex()).isZero();
            assertThat(gd.stack.get(1).getEffectsToResolve()).containsExactly(new GainLifeEffect(2));
            assertThat(gd.stack.get(1).getStateTriggerEffectIndex()).isEqualTo(1);
            assertThat(gd.stateTriggerOnStack).containsExactlyInAnyOrder(
                    new StateTriggerKey(perm.getId(), 0),
                    new StateTriggerKey(perm.getId(), 1));
        }

        @Test
        @DisplayName("Does not retrigger specific effect while on stack — other effects still fire")
        void doesNotRetriggerSpecificEffectWhileOnStack() {
            StateTrigger trigger1 = Triggers.state((gameData, perm, controllerId) -> true);
            StateTrigger trigger2 = Triggers.state((gameData, perm, controllerId) -> true);
            Card card = new Card();
            card.setName("Multi Trigger Card");
            card.setType(CardType.ENCHANTMENT);
            card.addTrigger(trigger1, new GainLifeEffect(1));
            card.addTrigger(trigger2, new GainLifeEffect(2));
            Permanent perm = new Permanent(card);
            gd.playerBattlefields.get(player1Id).add(perm);

            // Mark only the first trigger as already on stack
            gd.stateTriggerOnStack.add(new StateTriggerKey(perm.getId(), 0));

            sut.checkStateTriggers(gd);

            // Only the second trigger fires
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEffectsToResolve()).containsExactly(new GainLifeEffect(2));
        }

        @Test
        @DisplayName("Handles null battlefield gracefully")
        void handlesNullBattlefield() {
            gd.playerBattlefields.remove(player2Id);

            sut.checkStateTriggers(gd);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Predicate receives correct gameData, permanent, and controllerId")
        void predicateReceivesCorrectArguments() {
            UUID[] capturedControllerId = new UUID[1];
            Permanent[] capturedPerm = new Permanent[1];
            GameData[] capturedGd = new GameData[1];

            StateTrigger trigger = Triggers.state((gameData, perm, controllerId) -> {
                        capturedGd[0] = gameData;
                        capturedPerm[0] = perm;
                        capturedControllerId[0] = controllerId;
                        return false;
                    });
            Card card = createCardWithStateTrigger("Arg Card", trigger, new GainLifeEffect(1));
            Permanent perm = new Permanent(card);
            gd.playerBattlefields.get(player2Id).add(perm);

            sut.checkStateTriggers(gd);

            assertThat(capturedGd[0]).isSameAs(gd);
            assertThat(capturedPerm[0]).isSameAs(perm);
            assertThat(capturedControllerId[0]).isEqualTo(player2Id);
        }
    }

    @Nested
    @DisplayName("cleanupResolvedStateTrigger")
    class CleanupResolvedStateTrigger {

        @Test
        @DisplayName("Removes state trigger key from stateTriggerOnStack for triggered ability")
        void removesTrackingForTriggeredAbility() {
            UUID sourcePermanentId = UUID.randomUUID();
            gd.stateTriggerOnStack.add(new StateTriggerKey(sourcePermanentId, 0));

            Card card = new Card();
            card.setName("Trigger Card");
            StackEntry entry = TestStackDoor.triggered(card,
                    player1Id,
                    "Test trigger",
                    List.of(),
                    null,
                    sourcePermanentId
            );
            entry.setStateTriggerEffectIndex(0);

            sut.cleanupResolvedStateTrigger(gd, entry);

            assertThat(gd.stateTriggerOnStack).doesNotContain(new StateTriggerKey(sourcePermanentId, 0));
        }

        @Test
        @DisplayName("Does not remove tracking when sourcePermanentId is null")
        void doesNotRemoveWhenSourcePermanentIdIsNull() {
            UUID otherId = UUID.randomUUID();
            StateTriggerKey otherKey = new StateTriggerKey(otherId, 0);
            gd.stateTriggerOnStack.add(otherKey);

            Card card = new Card();
            card.setName("No Source Card");
            StackEntry entry = TestStackDoor.triggered(card,
                    player1Id,
                    "No source trigger",
                    List.of()
            );

            sut.cleanupResolvedStateTrigger(gd, entry);

            assertThat(gd.stateTriggerOnStack).contains(otherKey);
        }

        @Test
        @DisplayName("Does not remove tracking for non-triggered-ability entry types")
        void doesNotRemoveForNonTriggeredAbility() {
            UUID sourcePermanentId = UUID.randomUUID();
            StateTriggerKey key = new StateTriggerKey(sourcePermanentId, 0);
            gd.stateTriggerOnStack.add(key);

            Card card = new Card();
            card.setName("Creature Card");
            StackEntry entry = new StackEntry(card, player1Id);

            sut.cleanupResolvedStateTrigger(gd, entry);

            assertThat(gd.stateTriggerOnStack).contains(key);
        }

        @Test
        @DisplayName("Does not remove tracking for triggered ability without stateTriggerEffectIndex")
        void doesNotRemoveForNonStateTrigger() {
            UUID sourcePermanentId = UUID.randomUUID();
            StateTriggerKey key = new StateTriggerKey(sourcePermanentId, 0);
            gd.stateTriggerOnStack.add(key);

            Card card = new Card();
            card.setName("ETB Card");
            StackEntry entry = TestStackDoor.triggered(card,
                    player1Id,
                    "ETB trigger",
                    List.of(),
                    null,
                    sourcePermanentId
            );
            // stateTriggerEffectIndex defaults to -1 (not a state trigger)

            sut.cleanupResolvedStateTrigger(gd, entry);

            assertThat(gd.stateTriggerOnStack).contains(key);
        }

        @Test
        @DisplayName("Allows trigger to fire again after cleanup")
        void allowsRetriggerAfterCleanup() {
            List<CardEffect> effects = List.of(new GainLifeEffect(5));
            StateTrigger trigger = Triggers.state((gameData, perm, controllerId) -> true);
            Card card = createCardWithStateTrigger("Repeater", trigger, effects.toArray(CardEffect[]::new));
            Permanent perm = new Permanent(card);
            gd.playerBattlefields.get(player1Id).add(perm);

            // First fire
            sut.checkStateTriggers(gd);
            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.getFirst();

            // While on stack, should not retrigger
            sut.checkStateTriggers(gd);
            assertThat(gd.stack).hasSize(1);

            // Cleanup (simulating resolution)
            sut.cleanupResolvedStateTrigger(gd, entry);
            gd.stack.clear();

            // Should fire again
            sut.checkStateTriggers(gd);
            assertThat(gd.stack).hasSize(1);
        }
    }
}
