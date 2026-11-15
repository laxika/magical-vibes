package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.rules.LibraryEvent;
import com.github.laxika.magicalvibes.testutil.TestStackDoor;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;

import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.CardsInHandAtMost;
import com.github.laxika.magicalvibes.model.condition.MaxSpeed;
import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.AbundanceDrawReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleDrawReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DrawFromBottomOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.FirstNonDrawStepDrawFourReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.LivingConundrumDrawReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentDrawTwoOrMoreReplacedEffect;
import com.github.laxika.magicalvibes.model.effect.QuantumRiddlerDrawReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.effect.DredgeSupport;
import com.github.laxika.magicalvibes.service.effect.GrantedTriggeredAbilitySupport;
import com.github.laxika.magicalvibes.service.effect.mayfx.BreathstealersCryptDrawReplacementHandler;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityRequest;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class DrawServiceTest {

    @Test
    void emptyLibraryLossWaitsUntilResolutionFinishes() {
        gd.playerDecks.put(player1Id, new ArrayList<>());
        gd.deferPlayerLossCheck = true;

        sut.performDrawCard(gd, player1Id);

        assertThat(gd.playersAttemptedDrawFromEmptyLibrary).contains(player1Id);
        verify(gameOutcomeService, never()).resolveLoss(any(), any(), any());
        verify(gameOutcomeService, never()).declareWinner(any(), any());
    }

    @Test
    void drawFromBottomReplacementUsesTheControllerBottomOfLibrary() {
        Card lantern = createCard("Lantern of Undersight", CardType.ARTIFACT);
        lantern.addStaticEffect(new DrawFromBottomOfLibraryEffect());
        gd.playerBattlefields.get(player1Id).add(new Permanent(lantern));
        Card top = createCard("Top", CardType.CREATURE);
        Card bottom = createCard("Bottom", CardType.CREATURE);
        gd.playerDecks.put(player1Id, new ArrayList<>(List.of(top, bottom)));
        gd.playerHands.put(player1Id, new ArrayList<>());

        sut.resolveDrawCard(gd, player1Id);

        assertThat(gd.playerHands.get(player1Id)).containsExactly(bottom);
        assertThat(gd.playerDecks.get(player1Id)).containsExactly(top);
    }

    @Test
    void anotherPlayersDrawInstructionWaitsBehindCurrentReplacement() {
        Card abundance = createCard("Abundance", CardType.ENCHANTMENT);
        abundance.addStaticEffect(new AbundanceDrawReplacementEffect());
        gd.playerBattlefields.get(player1Id).add(new Permanent(abundance));
        Card first = createCard("First", CardType.CREATURE);
        Card second = createCard("Second", CardType.CREATURE);
        gd.playerDecks.put(player1Id, new ArrayList<>(List.of(first, second)));
        gd.playerHands.put(player1Id, new ArrayList<>());

        sut.resolveDrawCards(gd, player1Id, 2);
        sut.resolveDrawCards(gd, player2Id, 2);

        assertThat(gd.pendingMayAbilities).hasSize(1);
        assertThat(gd.pendingCardDraws).containsExactly(player1Id, player2Id, player2Id);
    }

    @Test
    void multipleDrawsWaitForEachReplacementChoice() {
        Card abundance = createCard("Abundance", CardType.ENCHANTMENT);
        abundance.addStaticEffect(new AbundanceDrawReplacementEffect());
        gd.playerBattlefields.get(player1Id).add(new Permanent(abundance));
        Card first = createCard("First", CardType.CREATURE);
        Card second = createCard("Second", CardType.CREATURE);
        gd.playerDecks.put(player1Id, new ArrayList<>(List.of(first, second)));
        gd.playerHands.put(player1Id, new ArrayList<>());

        sut.resolveDrawCards(gd, player1Id, 2);

        assertThat(gd.pendingMayAbilities).hasSize(1);
        assertThat(gd.playerHands.get(player1Id)).isEmpty();
        gd.pendingMayAbilities.clear();
        sut.resolveDrawCardWithoutStaticReplacementCheck(gd, player1Id);
        sut.resumePendingCardDraws(gd);

        assertThat(gd.playerHands.get(player1Id)).containsExactly(first);
        assertThat(gd.pendingMayAbilities).hasSize(1);
        gd.pendingMayAbilities.clear();
        sut.resolveDrawCardWithoutStaticReplacementCheck(gd, player1Id);
        sut.resumePendingCardDraws(gd);

        assertThat(gd.playerHands.get(player1Id)).containsExactly(first, second);
        assertThat(gd.pendingCardDraws).isEmpty();
    }

    @Test
    void opponentMultiCardDrawIsReplacedWithOneCardForEachPlayer() {
        when(gameQueryService.getOpponentId(gd, player2Id)).thenReturn(player1Id);
        Card almsCollector = createCard("Alms Collector", CardType.CREATURE);
        almsCollector.addStaticEffect(new OpponentDrawTwoOrMoreReplacedEffect());
        gd.playerBattlefields.get(player1Id).add(new Permanent(almsCollector));
        gd.playerHands.put(player1Id, new ArrayList<>());
        gd.playerHands.put(player2Id, new ArrayList<>());
        Card player1Card = createCard("Player 1 card", CardType.CREATURE);
        Card player2Card = createCard("Player 2 card", CardType.CREATURE);
        gd.playerDecks.put(player1Id, new ArrayList<>(List.of(player1Card)));
        gd.playerDecks.put(player2Id, new ArrayList<>(List.of(player2Card)));

        sut.resolveDrawCards(gd, player2Id, 2);

        assertThat(gd.playerHands.get(player1Id)).containsExactly(player1Card);
        assertThat(gd.playerHands.get(player2Id)).containsExactly(player2Card);
        assertThat(gd.playerDecks.get(player1Id)).isEmpty();
        assertThat(gd.playerDecks.get(player2Id)).isEmpty();
    }

    @Mock
    private GameQueryService gameQueryService;

    @Mock
    private GameLogService gameLogService;

    @Mock
    private GameOutcomeService gameOutcomeService;

    @Mock
    private TriggeredAbilityQueueService triggeredAbilityQueueService;

    @Mock
    private InteractionHandlerRegistry interactionHandlerRegistry;

    @Mock
    private BreathstealersCryptDrawReplacementHandler breathstealersCryptDrawReplacementHandler;

    @Mock
    private ConditionEvaluationService conditionEvaluationService;

    @Mock
    private DredgeSupport dredgeSupport;

    @Mock
    private GrantedTriggeredAbilitySupport grantedTriggeredAbilitySupport;

    @Mock
    private TriggerCollectionService triggerCollectionService;

    @Mock
    private PredicateEvaluationService predicateEvaluationService;

    @Mock
    private TriggeredAbilityStackService triggeredAbilityStackService;

    @InjectMocks
    private DrawService sut;

    /** The requests the draw collectors handed to the stack door, in order. */
    private final List<TriggeredAbilityRequest> doorRequests = new ArrayList<>();

    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;

    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        // A query scope runs its queries; these tests have no layered pass to share.
        lenient().when(gameQueryService.withQueryScope(any(GameData.class), any()))
                .thenAnswer(invocation -> ((java.util.function.Supplier<?>) invocation.getArgument(1)).get());
        // The draw collectors judge each ability's event condition through the service.
        lenient().when(conditionEvaluationService.triggerConditionsMet(any(), any(), any())).thenAnswer(invocation -> {
            TriggerOptions options = invocation.getArgument(1);
            return options.interveningIf() == null && options.eventCondition() == null;
        });
        lenient().when(conditionEvaluationService.eventConditionMet(any(), any(), any()))
                .thenAnswer(invocation -> ((TriggerOptions) invocation.getArgument(1)).eventCondition() == null);
        // The stack door, as far as these collectors rely on it: it judges and marks the ability's limit and puts one
        // entry of its effects as written, stamped with the collector's record, on the stack.
        lenient().when(triggeredAbilityStackService.put(any(), any())).thenAnswer(invocation -> {
            GameData game = invocation.getArgument(0);
            TriggeredAbilityRequest request = invocation.getArgument(1);
            doorRequests.add(request);
            TriggeredAbility ability = request.ability();
            UUID limitSource = request.sourcePermanent() != null ? request.sourcePermanentId()
                    : request.sourceCard().getId();
            if (TriggeredAbilityStackService.limitReached(game, ability.options().limit(), limitSource,
                    ability.limitKey())) {
                return false;
            }
            StackEntry entry = TestStackDoor.triggered(request.sourceCard(),
                    request.controllerId(), request.description(),
                    new ArrayList<>(ability.effects().stream().map(EffectRegistration::effect).toList()), null,
                    request.sourcePermanentId());
            request.record().stamp(entry);
            game.stack.add(entry);
            TriggeredAbilityStackService.markLimit(game, ability.options().limit(), limitSource, ability.limitKey());
            return true;
        });
        var doorField = DrawService.class.getDeclaredField("triggeredAbilityStackService");
        doorField.setAccessible(true);
        doorField.set(sut, triggeredAbilityStackService);
        player1Id = UUID.randomUUID();
        player2Id = UUID.randomUUID();
        gd = new GameData(UUID.randomUUID(), "test", player1Id, "Player1");
        gd.orderedPlayerIds.add(player1Id);
        gd.orderedPlayerIds.add(player2Id);
        gd.playerIdToName.put(player1Id, "Player1");
        gd.playerIdToName.put(player2Id, "Player2");
        gd.playerBattlefields.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerBattlefields.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
    }

    // ===== Helpers =====

    // Effects must be added before wrapping in a Permanent — the Permanent constructor
    // freezes the card.
    private static Card createCard(String name, CardType type) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        return card;
    }

    @Test
    @DisplayName("a card revealed by a permanent's reveal ability is logged and emitted as the reveal event")
    void aRevealedCardIsLoggedAndEmitted() {
        Card rowenCard = createCard("Rowen", CardType.ENCHANTMENT);
        Permanent rowen = new Permanent(rowenCard);
        Card forest = createCard("Forest", CardType.LAND);

        sut.revealDrawnCard(gd, rowen, player1Id, forest);

        verify(gameLogService).append(eq(gd), argThat((GameLogEntry e) ->
                e.plainText().equals("Player1 reveals Forest with Rowen.")));
        assertThat(gd.rulesEvents.pending().stream().flatMap(batch -> batch.events().stream()))
                .containsExactly(new LibraryEvent.RevealedDrawnCard(player1Id, rowen, forest));
    }

    @Test
    @DisplayName("a draw is emitted with the drawn card, whether it is the draw step's first and the turn's count")
    void aDrawIsEmitted() {
        Card top = createCard("Top", CardType.CREATURE);
        gd.playerDecks.put(player1Id, new ArrayList<>(List.of(top)));
        gd.playerHands.put(player1Id, new ArrayList<>());
        gd.cardsDrawnThisTurn.put(player1Id, 1);

        sut.resolveDrawCard(gd, player1Id);

        assertThat(gd.rulesEvents.pending().stream().flatMap(batch -> batch.events().stream()))
                .filteredOn(LibraryEvent.Drew.class::isInstance)
                .containsExactly(new LibraryEvent.Drew(player1Id, top, false, 2));
    }

    @Test
    void conditionalDoubleDrawReplacementOnlyAppliesWhenConditionIsMet() {
        Card sourceCard = createCard("Vnwxt, Verbose Host", CardType.CREATURE);
        sourceCard.addStaticEffect(new ConditionalEffect(
                new MaxSpeed(), new DoubleDrawReplacementEffect()));
        gd.playerBattlefields.get(player1Id).add(new Permanent(sourceCard));

        Card firstCard = createCard("First card", CardType.CREATURE);
        Card secondCard = createCard("Second card", CardType.CREATURE);
        gd.playerDecks.put(player1Id, new ArrayList<>(List.of(firstCard, secondCard)));
        gd.playerHands.put(player1Id, new ArrayList<>());
        when(conditionEvaluationService.isMet(eq(gd), any(), any())).thenReturn(true);

        sut.resolveDrawCard(gd, player1Id);

        assertThat(gd.playerHands.get(player1Id)).containsExactly(firstCard, secondCard);
        assertThat(gd.playerDecks.get(player1Id)).isEmpty();
    }

    @Test
    void quantumRiddlerAddsOneCardToTheWholeDrawInstruction() {
        Card sourceCard = createCard("Quantum Riddler", CardType.CREATURE);
        sourceCard.addStaticEffect(new ConditionalEffect(
                new CardsInHandAtMost(1), new QuantumRiddlerDrawReplacementEffect()));
        gd.playerBattlefields.get(player1Id).add(new Permanent(sourceCard));

        Card firstCard = createCard("First card", CardType.CREATURE);
        Card secondCard = createCard("Second card", CardType.CREATURE);
        Card thirdCard = createCard("Third card", CardType.CREATURE);
        gd.playerDecks.put(player1Id, new ArrayList<>(List.of(firstCard, secondCard, thirdCard)));
        gd.playerHands.put(player1Id, new ArrayList<>());
        when(conditionEvaluationService.isMet(eq(gd), any(), any())).thenReturn(true);

        sut.resolveDrawCards(gd, player1Id, 2);

        assertThat(gd.playerHands.get(player1Id)).containsExactly(firstCard, secondCard, thirdCard);
        assertThat(gd.playerDecks.get(player1Id)).isEmpty();
    }

    @Test
    void firstNonDrawStepReplacementAppliesOnlyOncePerTurn() {
        Card sourceCard = createCard("Reed Richards, Smartest Man", CardType.CREATURE);
        sourceCard.addStaticEffect(new FirstNonDrawStepDrawFourReplacementEffect());
        gd.playerBattlefields.get(player1Id).add(new Permanent(sourceCard));

        Card firstCard = createCard("First card", CardType.CREATURE);
        Card secondCard = createCard("Second card", CardType.CREATURE);
        Card thirdCard = createCard("Third card", CardType.CREATURE);
        Card fourthCard = createCard("Fourth card", CardType.CREATURE);
        Card fifthCard = createCard("Fifth card", CardType.CREATURE);
        gd.playerDecks.put(player1Id, new ArrayList<>(List.of(
                firstCard, secondCard, thirdCard, fourthCard, fifthCard)));
        gd.playerHands.put(player1Id, new ArrayList<>());
        gd.currentStep = TurnStep.PRECOMBAT_MAIN;

        sut.resolveDrawCard(gd, player1Id);
        sut.resolveDrawCard(gd, player1Id);

        assertThat(gd.playerHands.get(player1Id)).hasSize(5);
        assertThat(gd.playerDecks.get(player1Id)).isEmpty();
    }

    @Test
    void emptyLibraryDrawReplacementOnlySkipsControllerDraw() {
        Card sourceCard = createCard("Living Conundrum", CardType.CREATURE);
        sourceCard.addStaticEffect(new LivingConundrumDrawReplacementEffect());
        gd.playerBattlefields.get(player1Id).add(new Permanent(sourceCard));
        gd.playerDecks.put(player1Id, new ArrayList<>());
        gd.playerHands.put(player1Id, new ArrayList<>());

        Card opponentCard = createCard("Opponent card", CardType.CREATURE);
        gd.playerDecks.put(player2Id, new ArrayList<>(List.of(opponentCard)));
        gd.playerHands.put(player2Id, new ArrayList<>());

        sut.resolveDrawCard(gd, player1Id);
        sut.resolveDrawCard(gd, player2Id);

        assertThat(gd.playerHands.get(player1Id)).isEmpty();
        assertThat(gd.playersAttemptedDrawFromEmptyLibrary).doesNotContain(player1Id);
        assertThat(gd.playerHands.get(player2Id)).containsExactly(opponentCard);
    }
}
