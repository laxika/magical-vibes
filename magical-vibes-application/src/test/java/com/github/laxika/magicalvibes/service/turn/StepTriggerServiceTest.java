package com.github.laxika.magicalvibes.service.turn;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.model.action.DelayedDestroyAllPermanents;
import com.github.laxika.magicalvibes.model.action.DelayedSacrificeTargetPermanentAtEndStep;
import com.github.laxika.magicalvibes.model.action.DelayedSacrificeTargetPermanentAtEndStepIfManaValueAtMost;
import com.github.laxika.magicalvibes.model.action.ExilePermanentAtControllerEndStep;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.action.DelayedPlusOneCounters;
import com.github.laxika.magicalvibes.model.action.EchoAtNextUpkeep;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.OpeningHandRevealTrigger;
import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.model.action.EachPlayerHandExileReturnAtNextEndStep;
import com.github.laxika.magicalvibes.model.action.DiscardCardsAtNextEndStep;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.SagaChapterTargetGroup;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.AllPermanentsUpkeepSacrificeUnlessPayEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageIfFewCardsInHandEffect;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyOneOfTargetsAtRandomEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.condition.DidntAttack;
import com.github.laxika.magicalvibes.model.condition.AnyPlayerControlsPermanentCount;
import com.github.laxika.magicalvibes.model.condition.ActivePlayerHandEmpty;
import com.github.laxika.magicalvibes.model.condition.APlayerControlsMoreCreaturesThanEachOtherPlayer;
import com.github.laxika.magicalvibes.model.condition.OpponentLostLifeThisTurn;
import com.github.laxika.magicalvibes.model.condition.OpponentLostLifeLastTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.EmblemStepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.EmblemTriggerStep;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureControllerLosesLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LeylineStartOnBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayRevealSubtypeFromHandEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerWithMostCreaturesGainsControlOfSourceCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.PayEchoCost;
import com.github.laxika.magicalvibes.model.amount.ControllerLifeTotal;
import com.github.laxika.magicalvibes.model.condition.Metalcraft;
import com.github.laxika.magicalvibes.model.condition.MaxSpeed;
import com.github.laxika.magicalvibes.model.condition.NoOtherPermanent;
import com.github.laxika.magicalvibes.model.condition.NoSpellsCastLastTurn;
import com.github.laxika.magicalvibes.model.condition.NotControllerTurn;
import com.github.laxika.magicalvibes.model.condition.NotKicked;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.condition.Raid;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEachPlayerHandAndReturnExiledCardsEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.TapPlayersPermanentsAndDamageEqualToCountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.condition.TwoOrMoreSpellsCastLastTurn;
import com.github.laxika.magicalvibes.model.condition.EachPlayerLifeAtMost;
import com.github.laxika.magicalvibes.model.condition.ControllerLifeAtLeast;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.effect.WinGameEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.ETBTokenTargetService;
import com.github.laxika.magicalvibes.service.battlefield.GraveyardTargetingService;
import com.github.laxika.magicalvibes.service.battlefield.GraveyardTransformedReturnService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.GrantedTriggeredAbilitySupport;
import com.github.laxika.magicalvibes.service.effect.GrantedUpkeepEffectSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import com.github.laxika.magicalvibes.service.epic.EpicService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.paradigm.ParadigmService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.trigger.TriggerTargetCollector;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import com.github.laxika.magicalvibes.service.target.ValidTargetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;

@ExtendWith(MockitoExtension.class)
class StepTriggerServiceTest {

    @Mock
    private DrawService drawService;

    @Mock
    private GameQueryService gameQueryService;
    @Mock
    private PredicateEvaluationService predicateEvaluationService;

    @Mock
    private TargetLegalityService targetLegalityService;

    @Mock
    private GameLogService gameLogService;

    @Mock
    private PlayerInputService playerInputService;

    @Mock
    private PermanentRemovalService permanentRemovalService;

    @Mock
    private LifeSupport lifeSupport;

    @Mock
    private BattlefieldEntryService battlefieldEntryService;

    @Mock
    private GraveyardTransformedReturnService graveyardTransformedReturnService;

    @Mock
    private GraveyardTargetingService graveyardTargetingService;

    @Mock
    private GraveyardService graveyardService;

    @Mock
    private TriggerCollectionService triggerCollectionService;

    @Mock
    private ParadigmService paradigmService;

    @Mock
    private EpicService epicService;

    @Mock
    private CreatureControlService creatureControlService;

    @Mock
    private GrantedTriggeredAbilitySupport grantedTriggeredAbilitySupport;

    @Mock
    private GrantedUpkeepEffectSupport grantedUpkeepEffectSupport;

    @Mock
    private ETBTokenTargetService etbTokenTargetService;

    @Mock
    private AmountEvaluationService amountEvaluationService;

    private StepTriggerService sut;

    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;

    @BeforeEach
    void setUp() {
        // Build the SUT manually so we can pass a REAL TriggerTargetCollector. The collector's
        // opponent-filter / valid-target logic is exercised by several tests in this class, so a
        // mock would silently return nulls and break them.
        TriggerTargetCollector triggerTargetCollector = new TriggerTargetCollector(
                gameQueryService, predicateEvaluationService, targetLegalityService);
        ValidTargetService validTargetService = new ValidTargetService(gameQueryService, predicateEvaluationService);
        sut = new StepTriggerService(
                drawService,
                gameQueryService,
                predicateEvaluationService,
                new ConditionEvaluationService(gameQueryService, predicateEvaluationService),
                gameLogService,
                playerInputService,
                permanentRemovalService,
                lifeSupport,
                battlefieldEntryService,
                graveyardTransformedReturnService,
                graveyardTargetingService,
                graveyardService,
                triggerCollectionService,
                triggerTargetCollector,
                paradigmService,
                epicService,
                validTargetService,
                creatureControlService,
                grantedTriggeredAbilitySupport,
                grantedUpkeepEffectSupport,
                etbTokenTargetService,
                amountEvaluationService);

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
        gd.playerHands.put(player1Id, new ArrayList<>());
        gd.playerHands.put(player2Id, new ArrayList<>());
        gd.playerGraveyards.put(player1Id, new ArrayList<>());
        gd.playerGraveyards.put(player2Id, new ArrayList<>());
    }

    @Nested
    @DisplayName("handleDrawStep")
    class HandleDrawStep {

        @Test
        @DisplayName("Starting player skips draw on turn 1")
        void startingPlayerSkipsDrawOnTurn1() {
            gd.turnNumber = 1;
            gd.startingPlayerId = player1Id;

            sut.handleDrawStep(gd);

            verify(drawService, never()).resolveDrawCard(gd, player1Id);
            verify(gameLogService).append(gd, GameLog.text("Player1 skips the draw (first turn)."));
        }

        @Test
        @DisplayName("Draw-step emblem triggers after the turn-based draw")
        void drawStepEmblemTriggersAfterTurnBasedDraw() {
            gd.turnNumber = 2;
            Card source = createCardWithName("Sarkhan, the Dragonspeaker");
            gd.emblems.add(new Emblem(player1Id, List.of(new EmblemStepTriggerEffect(
                    EmblemTriggerStep.DRAW_STEP, List.of(new GainLifeEffect(1)), "Draw-step effect")), source));

            sut.handleDrawStep(gd);

            verify(drawService).resolveDrawCard(gd, player1Id);
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getDescription()).contains("Sarkhan, the Dragonspeaker");
        }

        @Test
        @DisplayName("Active player draws a card on turn 2+")
        void activePlayerDrawsCardOnTurn2() {
            gd.turnNumber = 2;

            sut.handleDrawStep(gd);

            verify(drawService).resolveDrawCard(gd, player1Id);
        }

        @Test
        @DisplayName("Non-starting player draws on turn 1")
        void nonStartingPlayerDrawsOnTurn1() {
            gd.activePlayerId = player2Id;
            gd.turnNumber = 1;
            gd.startingPlayerId = player1Id;

            sut.handleDrawStep(gd);

            verify(drawService).resolveDrawCard(gd, player2Id);
        }

        @Test
        @DisplayName("DRAW_TRIGGERED effect on active player's permanent pushes trigger onto stack")
        void drawTriggeredEffectPushesToStack() {
            gd.turnNumber = 2;
            Card card = createCardWithName("Temple Bell");
            card.addEffect(EffectSlot.DRAW_TRIGGERED, new GainLifeEffect(1));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));

            sut.handleDrawStep(gd);

            assertThat(gd.stack).isNotEmpty();
            assertThat(gd.stack.getFirst().getDescription()).contains("Temple Bell");
        }

        @Test
        @DisplayName("DRAW_TRIGGERED MayEffect is queued onto the stack via queueMayAbility")
        void drawTriggeredMayEffectQueuedOntoStack() {
            gd.turnNumber = 2;
            Card card = createCardWithName("Mystic Card");
            card.addEffect(EffectSlot.DRAW_TRIGGERED, new MayEffect(new GainLifeEffect(1), "Gain 1 life?"));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));

            sut.handleDrawStep(gd);

            // queueMayAbility adds a StackEntry containing the MayEffect
            assertThat(gd.stack).isNotEmpty();
            assertThat(gd.stack.getFirst().getDescription()).contains("Mystic Card");
        }

        @Test
        @DisplayName("EACH_DRAW_TRIGGERED fires for all players' draw steps")
        void eachDrawTriggeredFiresForAllPlayers() {
            gd.turnNumber = 2;
            Card card = createCardWithName("Howling Mine");
            card.addEffect(EffectSlot.EACH_DRAW_TRIGGERED, new DrawCardForTargetPlayerEffect(1));
            gd.playerBattlefields.get(player2Id).add(new Permanent(card));

            sut.handleDrawStep(gd);

            assertThat(gd.stack).isNotEmpty();
            assertThat(gd.stack.getFirst().getDescription()).contains("Howling Mine");
        }

        @Test
        @DisplayName("EACH_DRAW_TRIGGERED skips when source requires untapped and is tapped")
        void eachDrawTriggeredSkipsWhenSourceTapped() {
            gd.turnNumber = 2;
            Card card = createCardWithName("Howling Mine");
            card.addEffect(EffectSlot.EACH_DRAW_TRIGGERED, new DrawCardForTargetPlayerEffect(1, true));
            Permanent perm = new Permanent(card);
            perm.tap();
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.handleDrawStep(gd);

            assertThat(gd.stack).isEmpty();
        }
    }

    @Nested
    @DisplayName("handleUpkeepTriggers")
    class HandleUpkeepTriggers {

        @Test
        @DisplayName("Opponent-upkeep emblem triggers for the active opponent")
        void opponentUpkeepEmblemTriggersForActiveOpponent() {
            Card source = createCardWithName("Sorin, Solemn Visitor");
            gd.emblems.add(new Emblem(player1Id, List.of(new EmblemStepTriggerEffect(
                    EmblemTriggerStep.OPPONENT_UPKEEP, List.of(new GainLifeEffect(1)), "Opponent-upkeep effect")), source));
            gd.activePlayerId = player2Id;

            sut.handleUpkeepTriggers(gd);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1Id);
            assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(player2Id);
            assertThat(gd.stack.getFirst().isNonTargeting()).isTrue();
        }

        @Test
        @DisplayName("Opponent-upkeep emblem does not trigger during its controller's upkeep")
        void opponentUpkeepEmblemDoesNotTriggerDuringControllerUpkeep() {
            Card source = createCardWithName("Sorin, Solemn Visitor");
            gd.emblems.add(new Emblem(player1Id, List.of(new EmblemStepTriggerEffect(
                    EmblemTriggerStep.OPPONENT_UPKEEP, List.of(new GainLifeEffect(1)), "Opponent-upkeep effect")), source));

            sut.handleUpkeepTriggers(gd);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Permanent with upkeep effect pushes trigger onto stack")
        void permanentWithUpkeepEffectPushesTrigger() {
            gd.turnNumber = 2;
            Card card = createCardWithName("Venser's Journal");
            card.addEffect(EffectSlot.UPKEEP_TRIGGERED, new GainLifeEffect(1));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));

            sut.handleUpkeepTriggers(gd);

            assertThat(gd.stack).isNotEmpty();
            assertThat(gd.stack.getFirst().getDescription()).contains("Venser's Journal");
        }

        @Test
        @DisplayName("Dynamic echo cost is evaluated when the upkeep trigger is created")
        void dynamicEchoCostIsEvaluatedAtUpkeep() {
            Card card = createCardWithName("Dynamic Echo");
            Permanent permanent = new Permanent(card);
            gd.playerBattlefields.get(player1Id).add(permanent);
            gd.queueDelayedAction(new EchoAtNextUpkeep(permanent.getId(), new ControllerLifeTotal(), card));
            when(gameQueryService.findPermanentController(gd, permanent.getId())).thenReturn(player1Id);
            when(gameQueryService.findPermanentById(gd, permanent.getId())).thenReturn(permanent);
            when(amountEvaluationService.evaluate(eq(gd), any(ControllerLifeTotal.class), any(AmountContext.class)))
                    .thenReturn(13);

            sut.handleUpkeepTriggers(gd);

            ForcedCostOrElseEffect echo = (ForcedCostOrElseEffect)
                    gd.stack.getFirst().getEffectsToResolve().getFirst();
            assertThat(((PayEchoCost) echo.forcedCost()).echoCost()).isEqualTo("{13}");
        }

        @Test
        @DisplayName("Unique creature-count leader lets an intervening-if upkeep ability trigger")
        void uniqueCreatureCountLeaderTriggers() {
            Card card = createCardWithName("Wild Mammoth");
            card.addEffect(EffectSlot.UPKEEP_TRIGGERED, new ConditionalEffect(
                    new APlayerControlsMoreCreaturesThanEachOtherPlayer(),
                    new PlayerWithMostCreaturesGainsControlOfSourceCreatureEffect()));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));
            gd.playerBattlefields.get(player2Id).add(new Permanent(createCardWithName("Bear 1")));
            gd.playerBattlefields.get(player2Id).add(new Permanent(createCardWithName("Bear 2")));
            when(gameQueryService.isCreature(eq(gd), any(Permanent.class))).thenReturn(true);

            sut.handleUpkeepTriggers(gd);

            assertThat(gd.stack).hasSize(1);
        }

        @Test
        @DisplayName("Global permanent-count intervening-if upkeep ability triggers at its threshold")
        void globalPermanentCountTriggersAtThreshold() {
            Card card = createCardWithName("Planar Collapse");
            card.addEffect(EffectSlot.UPKEEP_TRIGGERED, new ConditionalEffect(
                    new AnyPlayerControlsPermanentCount(4, new PermanentIsCreaturePredicate()),
                    new GainLifeEffect(1)));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));

            for (int i = 0; i < 4; i++) {
                Card creatureCard = creatureCard();
                gd.playerBattlefields.get(i % 2 == 0 ? player1Id : player2Id).add(new Permanent(creatureCard));
            }

            lenient().when(predicateEvaluationService.matchesPermanentPredicate(any(Permanent.class), any(), any()))
                    .thenAnswer(invocation -> invocation.getArgument(1) instanceof PermanentIsCreaturePredicate
                            && ((Permanent) invocation.getArgument(0)).getCard().hasType(CardType.CREATURE));

            sut.handleUpkeepTriggers(gd);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getDescription()).contains("Planar Collapse");
        }

        @Test
        @DisplayName("Tied creature counts suppress an intervening-if upkeep ability")
        void tiedCreatureCountsDoNotTrigger() {
            Card card = createCardWithName("Wild Mammoth");
            card.addEffect(EffectSlot.UPKEEP_TRIGGERED, new ConditionalEffect(
                    new APlayerControlsMoreCreaturesThanEachOtherPlayer(),
                    new PlayerWithMostCreaturesGainsControlOfSourceCreatureEffect()));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));
            gd.playerBattlefields.get(player2Id).add(new Permanent(createCardWithName("Bear")));
            when(gameQueryService.isCreature(eq(gd), any(Permanent.class))).thenReturn(true);

            sut.handleUpkeepTriggers(gd);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Global upkeep grant can require a life payment")
        void globalUpkeepGrantCanRequireLifePayment() {
            gd.turnNumber = 2;
            Card grantCard = createCardWithName("Vile Consumption");
            grantCard.addEffect(EffectSlot.STATIC,
                    new AllPermanentsUpkeepSacrificeUnlessPayEffect(new PermanentIsCreaturePredicate(), 1));
            gd.playerBattlefields.get(player2Id).add(new Permanent(grantCard));

            Card creatureCard = createCardWithName("Creature");
            creatureCard.setType(CardType.CREATURE);
            Permanent creature = new Permanent(creatureCard);
            gd.playerBattlefields.get(player1Id).add(creature);
            lenient().when(predicateEvaluationService.matchesPermanentPredicate(
                    eq(gd), eq(creature), any(PermanentIsCreaturePredicate.class))).thenReturn(true);

            sut.handleUpkeepTriggers(gd);

            assertThat(gd.stack).hasSize(1);
            ForcedCostOrElseEffect payOrSacrifice = (ForcedCostOrElseEffect)
                    gd.stack.getFirst().getEffectsToResolve().getFirst();
            PayManaCost payment = (PayManaCost) payOrSacrifice.forcedCost();
            assertThat(payment.manaCost()).isEmpty();
            assertThat(payment.lifeAmount()).isEqualTo(1);
        }

        @Test
        @DisplayName("No triggers pushed when no permanents with upkeep effects")
        void noTriggersWhenNoPermanentsWithUpkeepEffects() {
            gd.turnNumber = 2;
            Card card = createCardWithName("Grizzly Bears");
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));

            sut.handleUpkeepTriggers(gd);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Opponent's upkeep-triggered permanent does not trigger during active player's upkeep")
        void opponentUpkeepTriggeredDoesNotFireForActivePlayer() {
            gd.turnNumber = 2;
            Card card = createCardWithName("Venser's Journal");
            card.addEffect(EffectSlot.UPKEEP_TRIGGERED, new GainLifeEffect(1));
            gd.playerBattlefields.get(player2Id).add(new Permanent(card));

            sut.handleUpkeepTriggers(gd);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("MayEffect queues may ability instead of pushing to stack")
        void mayEffectQueuesMayAbility() {
            gd.turnNumber = 2;
            Card card = createCardWithName("Optional Card");
            card.addEffect(EffectSlot.UPKEEP_TRIGGERED, new MayEffect(new GainLifeEffect(1), "Gain 1 life?"));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));

            sut.handleUpkeepTriggers(gd);

            // MayEffect goes through queueMayAbility which adds to stack
            assertThat(gd.stack).isNotEmpty();
            verify(playerInputService).processNextMayAbility(gd);
        }

        @Test
        @DisplayName("Player-targeting upkeep effect triggers target selection")
        void playerTargetingEffectTriggersTargetSelection() {
            gd.turnNumber = 2;
            Card card = createCardWithName("Bloodgift Demon");
            card.addEffect(EffectSlot.UPKEEP_TRIGGERED, new DrawCardForTargetPlayerEffect(1, false, true));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));

            sut.handleUpkeepTriggers(gd);

            // processNextUpkeepPlayerTarget consumes the pending trigger and asks for target selection
            verify(playerInputService).beginAnyTargetChoice(eq(gd), eq(player1Id), any(), any(), any());
        }

        @Test
        @DisplayName("Player-targeting intervening-if upkeep effect skips target selection when its condition fails")
        void playerTargetingInterveningIfSkipsWhenConditionFails() {
            gd.turnNumber = 2;
            Card card = createCardWithName("Brink of Madness");
            card.addEffect(EffectSlot.UPKEEP_TRIGGERED, new ConditionalEffect(
                    new ActivePlayerHandEmpty(), new DrawCardForTargetPlayerEffect(1, false, true)));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));
            gd.playerHands.get(player1Id).add(createCardWithName("Card in hand"));

            sut.handleUpkeepTriggers(gd);

            verify(playerInputService, never()).beginAnyTargetChoice(any(), any(), any(), any(), any());
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("BecomeCopyOfTargetCreatureEffect triggers copy target selection when valid targets exist")
        void copyEffectTriggersCopyTargetSelection() {
            gd.turnNumber = 2;
            Card sourceCard = createCardWithName("Clone Shell");
            sourceCard.addEffect(EffectSlot.UPKEEP_TRIGGERED, new BecomeCopyOfTargetCreatureEffect());
            Permanent sourcePerm = new Permanent(sourceCard);
            gd.playerBattlefields.get(player1Id).add(sourcePerm);

            Card targetCard = createCardWithName("Target Creature");
            Permanent targetPerm = new Permanent(targetCard);
            gd.playerBattlefields.get(player2Id).add(targetPerm);

            when(gameQueryService.isCreature(eq(gd), any(Permanent.class))).thenAnswer(inv -> {
                Permanent p = inv.getArgument(1);
                return p.getId().equals(targetPerm.getId());
            });

            sut.handleUpkeepTriggers(gd);

            // processNextUpkeepCopyTarget consumes the trigger and asks for target selection
            verify(playerInputService).beginPermanentChoice(eq(gd), eq(player1Id), any(), any());
        }

        @Test
        @DisplayName("BecomeCopyOfTargetCreatureEffect skipped when no valid creature targets")
        void copyEffectSkippedWhenNoTargets() {
            gd.turnNumber = 2;
            Card sourceCard = createCardWithName("Clone Shell");
            sourceCard.addEffect(EffectSlot.UPKEEP_TRIGGERED, new BecomeCopyOfTargetCreatureEffect());
            gd.playerBattlefields.get(player1Id).add(new Permanent(sourceCard));
            // No other permanents on any battlefield — source is skipped, so isCreature is never called

            sut.handleUpkeepTriggers(gd);

            assertThat(gd.hasPendingInteraction(PermanentChoiceContext.UpkeepCopyTriggerTarget.class)).isFalse();
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("DestroyOneOfTargetsAtRandomEffect triggers own-target selection")
        void destroyAtRandomTriggersOwnTargetSelection() {
            gd.turnNumber = 2;
            Card card = createCardWithName("Capricious Efreet");
            card.addEffect(EffectSlot.UPKEEP_TRIGGERED, new DestroyOneOfTargetsAtRandomEffect());
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));

            sut.handleUpkeepTriggers(gd);

            // processNextCapriciousEfreetTarget consumes the trigger and asks for permanent choice
            verify(playerInputService).beginPermanentChoice(eq(gd), eq(player1Id), any(), any());
        }

        @Test
        @DisplayName("ConditionalEffect triggers when no other permanents match")
        void noOtherSubtypeTriggersWhenConditionMet() {
            gd.turnNumber = 2;
            Card card = createCardWithName("Tribal Card");
            card.addEffect(EffectSlot.UPKEEP_TRIGGERED, new ConditionalEffect(new NoOtherPermanent(new PermanentHasSubtypePredicate(CardSubtype.HUMAN)), new GainLifeEffect(1)));
            Permanent perm = new Permanent(card);
            gd.playerBattlefields.get(player1Id).add(perm);
            // Only one permanent — no other permanents to match, so condition is met

            sut.handleUpkeepTriggers(gd);

            assertThat(gd.stack).isNotEmpty();
            assertThat(gd.stack.getFirst().getDescription()).contains("Tribal Card");
        }

        @Test
        @DisplayName("ConditionalEffect does not trigger when another permanent matches")
        void noOtherSubtypeDoesNotTriggerWhenOtherExists() {
            gd.turnNumber = 2;
            Card card = createCardWithName("Tribal Card");
            card.addEffect(EffectSlot.UPKEEP_TRIGGERED, new ConditionalEffect(new NoOtherPermanent(new PermanentHasSubtypePredicate(CardSubtype.HUMAN)), new GainLifeEffect(1)));
            Permanent perm = new Permanent(card);
            gd.playerBattlefields.get(player1Id).add(perm);

            Card otherCard = createCardWithName("Other Human");
            Permanent otherPerm = new Permanent(otherCard);
            gd.playerBattlefields.get(player1Id).add(otherPerm);

            // lenient(): the Power Surge upkeep snapshot (countUntappedLands) also probes
            // matchesPermanentPredicate with a land predicate for every untapped permanent the active
            // player controls, so the source permanent is queried too — that unrelated call must not
            // trip strict-stubbing. Conditions evaluate through the FilterContext-aware overload
            // (permanent, filter, filterContext), so match the context with any().
            lenient().when(predicateEvaluationService.matchesPermanentPredicate(eq(otherPerm), any(), any())).thenReturn(true);

            sut.handleUpkeepTriggers(gd);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Graveyard-threshold intervening-if triggers when threshold met")
        void winGameTriggersWhenThresholdMet() {
            gd.turnNumber = 2;
            Card card = createCardWithName("Mortal Combat");
            card.addEffect(EffectSlot.UPKEEP_TRIGGERED, mortalCombatEffect());
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));

            // Add 20 creature cards to graveyard
            for (int i = 0; i < 20; i++) {
                gd.playerGraveyards.get(player1Id).add(creatureCard());
            }

            sut.handleUpkeepTriggers(gd);

            assertThat(gd.stack).isNotEmpty();
            assertThat(gd.stack.getFirst().getDescription()).contains("Mortal Combat");
        }

        @Test
        @DisplayName("Graveyard-threshold intervening-if does not trigger when threshold not met")
        void winGameDoesNotTriggerWhenThresholdNotMet() {
            gd.turnNumber = 2;
            Card card = createCardWithName("Mortal Combat");
            card.addEffect(EffectSlot.UPKEEP_TRIGGERED, mortalCombatEffect());
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));

            // Add only 5 creatures
            for (int i = 0; i < 5; i++) {
                gd.playerGraveyards.get(player1Id).add(creatureCard());
            }

            sut.handleUpkeepTriggers(gd);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Each-player life intervening-if triggers when every player is at or below the threshold")
        void eachPlayerLifeTriggersWhenAllAtOrBelow() {
            gd.turnNumber = 2;
            Card card = createCardWithName("Cryptolith Fragment");
            card.addEffect(EffectSlot.UPKEEP_TRIGGERED,
                    new ConditionalEffect(new EachPlayerLifeAtMost(10), new TransformSelfEffect()));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));
            gd.playerLifeTotals.put(player1Id, 10);
            gd.playerLifeTotals.put(player2Id, 3);

            sut.handleUpkeepTriggers(gd);

            assertThat(gd.stack).isNotEmpty();
            assertThat(gd.stack.getFirst().getDescription()).contains("Cryptolith Fragment");
        }

        @Test
        @DisplayName("Each-player life intervening-if does not trigger when one player is above the threshold")
        void eachPlayerLifeDoesNotTriggerWhenOneAbove() {
            gd.turnNumber = 2;
            Card card = createCardWithName("Cryptolith Fragment");
            card.addEffect(EffectSlot.UPKEEP_TRIGGERED,
                    new ConditionalEffect(new EachPlayerLifeAtMost(10), new TransformSelfEffect()));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));
            gd.playerLifeTotals.put(player1Id, 10);
            gd.playerLifeTotals.put(player2Id, 11);

            sut.handleUpkeepTriggers(gd);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Controller-life-at-least intervening-if triggers at the threshold")
        void controllerLifeAtLeastTriggersAtThreshold() {
            gd.turnNumber = 2;
            Card card = createCardWithName("Felidar Sovereign");
            card.addEffect(EffectSlot.UPKEEP_TRIGGERED,
                    new ConditionalEffect(new ControllerLifeAtLeast(40), new WinGameEffect()));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));
            gd.playerLifeTotals.put(player1Id, 40);

            sut.handleUpkeepTriggers(gd);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getDescription()).contains("Felidar Sovereign");
        }

        @Test
        @DisplayName("Controller-life-at-least intervening-if suppresses the trigger below the threshold")
        void controllerLifeAtLeastSuppressesBelowThreshold() {
            gd.turnNumber = 2;
            Card card = createCardWithName("Felidar Sovereign");
            card.addEffect(EffectSlot.UPKEEP_TRIGGERED,
                    new ConditionalEffect(new ControllerLifeAtLeast(40), new WinGameEffect()));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));
            gd.playerLifeTotals.put(player1Id, 39);

            sut.handleUpkeepTriggers(gd);

            assertThat(gd.stack).isEmpty();
        }

        private ConditionalEffect mortalCombatEffect() {
            // Mortal Combat: "if twenty or more creature cards are in your graveyard, you win the game".
            lenient().when(predicateEvaluationService.matchesCardPredicate(any(), any(), any(), any(), any()))
                    .thenAnswer(invocation -> ((Card) invocation.getArgument(0)).hasType(CardType.CREATURE));
            return new ConditionalEffect(
                    new GraveyardCardThreshold(20, new CardTypePredicate(CardType.CREATURE)),
                    new WinGameEffect());
        }

        private Card creatureCard() {
            Card creature = new Card();
            creature.setType(CardType.CREATURE);
            return creature;
        }

        @Test
        @DisplayName("GRAVEYARD_UPKEEP_TRIGGERED pushes trigger from graveyard card")
        void graveyardUpkeepTriggeredPushesToStack() {
            gd.turnNumber = 2;
            Card card = createCardWithName("Graveyard Card");
            card.addEffect(EffectSlot.GRAVEYARD_UPKEEP_TRIGGERED, new GainLifeEffect(1));
            gd.playerGraveyards.get(player1Id).add(card);

            sut.handleUpkeepTriggers(gd);

            assertThat(gd.stack).isNotEmpty();
            assertThat(gd.stack.getFirst().getDescription()).contains("Graveyard Card");
        }

        @Test
        @DisplayName("GRAVEYARD_UPKEEP_TRIGGERED with MayPayManaEffect queues onto stack")
        void graveyardUpkeepMayPayQueuesOntoStack() {
            gd.turnNumber = 2;
            Card card = createCardWithName("Graveyard May Card");
            card.addEffect(EffectSlot.GRAVEYARD_UPKEEP_TRIGGERED,
                    new MayPayManaEffect("{2}", new GainLifeEffect(1), "Pay {2} to gain life?"));
            gd.playerGraveyards.get(player1Id).add(card);

            sut.handleUpkeepTriggers(gd);

            // MayPayManaEffect goes through queueMayAbility, which adds to stack
            assertThat(gd.stack).isNotEmpty();
            assertThat(gd.stack.getFirst().getDescription()).contains("Graveyard May Card");
        }

        @Test
        @DisplayName("GRAVEYARD_UPKEEP_TRIGGERED with MayEffect queues may ability")
        void graveyardUpkeepMayEffectQueuesAbility() {
            gd.turnNumber = 2;
            Card card = createCardWithName("Graveyard May Card");
            card.addEffect(EffectSlot.GRAVEYARD_UPKEEP_TRIGGERED,
                    new MayEffect(new GainLifeEffect(1), "Gain life?"));
            gd.playerGraveyards.get(player1Id).add(card);

            sut.handleUpkeepTriggers(gd);

            assertThat(gd.stack).isNotEmpty();
        }

        @Test
        @DisplayName("GRAVEYARD_UPKEEP_TRIGGERED with ConditionalEffect skips when metalcraft not met")
        void graveyardMetalcraftSkipsWhenNotMet() {
            gd.turnNumber = 2;
            Card card = createCardWithName("Metalcraft Card");
            card.addEffect(EffectSlot.GRAVEYARD_UPKEEP_TRIGGERED,
                    new ConditionalEffect(new Metalcraft(), new MayPayManaEffect("{2}", new GainLifeEffect(1), "Pay?")));
            gd.playerGraveyards.get(player1Id).add(card);

            when(gameQueryService.isMetalcraftMet(gd, player1Id)).thenReturn(false);

            sut.handleUpkeepTriggers(gd);

            assertThat(gd.pendingMayAbilities).isEmpty();
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("GRAVEYARD_UPKEEP_TRIGGERED with ConditionalEffect triggers when metalcraft met")
        void graveyardMetalcraftTriggersWhenMet() {
            gd.turnNumber = 2;
            Card card = createCardWithName("Metalcraft Card");
            ConditionalEffect effect = new ConditionalEffect(
                    new Metalcraft(), new MayPayManaEffect("{2}", new GainLifeEffect(1), "Pay?"));
            card.addEffect(EffectSlot.GRAVEYARD_UPKEEP_TRIGGERED, effect);
            gd.playerGraveyards.get(player1Id).add(card);

            when(gameQueryService.isMetalcraftMet(gd, player1Id)).thenReturn(true);

            sut.handleUpkeepTriggers(gd);

            assertThat(gd.stack).singleElement()
                    .satisfies(entry -> assertThat(entry.getEffectsToResolve()).containsExactly(effect));
            assertThat(gd.stack.getFirst().getDescription()).contains("Metalcraft Card");
        }

        @Test
        @DisplayName("EACH_UPKEEP_TRIGGERED fires for all players' permanents")
        void eachUpkeepTriggeredFiresForAllPlayers() {
            gd.turnNumber = 2;
            Card card = createCardWithName("Each Upkeep Card");
            card.addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED, new GainLifeEffect(1));
            gd.playerBattlefields.get(player2Id).add(new Permanent(card));

            sut.handleUpkeepTriggers(gd);

            assertThat(gd.stack).isNotEmpty();
            assertThat(gd.stack.getFirst().getDescription()).contains("Each Upkeep Card");
        }

        @Test
        @DisplayName("EACH_UPKEEP_TRIGGERED with ConditionalEffect triggers when no spells cast")
        void noSpellsCastTriggersWhenConditionMet() {
            gd.turnNumber = 2;
            Card card = createCardWithName("Werewolf");
            card.addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED,
                    new ConditionalEffect(new NoSpellsCastLastTurn(), new GainLifeEffect(1)));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));

            // No spells cast last turn (empty map)

            sut.handleUpkeepTriggers(gd);

            assertThat(gd.stack).isNotEmpty();
        }

        @Test
        @DisplayName("EACH_UPKEEP_TRIGGERED with ConditionalEffect skips when spells were cast")
        void noSpellsCastSkipsWhenSpellsWereCast() {
            gd.turnNumber = 2;
            Card card = createCardWithName("Werewolf");
            card.addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED,
                    new ConditionalEffect(new NoSpellsCastLastTurn(), new GainLifeEffect(1)));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));

            gd.spellsCastLastTurn.put(player1Id, 1);

            sut.handleUpkeepTriggers(gd);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("EACH_UPKEEP_TRIGGERED with ConditionalEffect triggers when condition met")
        void twoOrMoreSpellsTriggersWhenConditionMet() {
            gd.turnNumber = 2;
            Card card = createCardWithName("Werewolf Reverse");
            card.addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED,
                    new ConditionalEffect(new TwoOrMoreSpellsCastLastTurn(), new GainLifeEffect(1)));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));

            gd.spellsCastLastTurn.put(player1Id, 2);

            sut.handleUpkeepTriggers(gd);

            assertThat(gd.stack).isNotEmpty();
        }

        @Test
        @DisplayName("EACH_UPKEEP_TRIGGERED with ConditionalEffect skips when no one cast two")
        void twoOrMoreSpellsSkipsWhenNotMet() {
            gd.turnNumber = 2;
            Card card = createCardWithName("Werewolf Reverse");
            card.addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED,
                    new ConditionalEffect(new TwoOrMoreSpellsCastLastTurn(), new GainLifeEffect(1)));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));

            gd.spellsCastLastTurn.put(player1Id, 1);
            gd.spellsCastLastTurn.put(player2Id, 0);

            sut.handleUpkeepTriggers(gd);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("EACH_UPKEEP_TRIGGERED with OpponentLostLifeLastTurn only counts opponents")
        void opponentLostLifeLastTurnOnlyCountsOpponents() {
            gd.turnNumber = 2;
            Card card = createCardWithName("Feast on the Fallen");
            card.addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED,
                    new ConditionalEffect(new OpponentLostLifeLastTurn(), new GainLifeEffect(1)));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));

            gd.lifeLostLastTurn.put(player1Id, 2);
            sut.handleUpkeepTriggers(gd);
            assertThat(gd.stack).isEmpty();

            gd.lifeLostLastTurn.put(player2Id, 2);
            sut.handleUpkeepTriggers(gd);
            assertThat(gd.stack).hasSize(1);
        }

        @Test
        @DisplayName("OPPONENT_UPKEEP_TRIGGERED fires for opponent's permanents during active player's upkeep")
        void opponentUpkeepTriggeredFiresDuringActivePlayersUpkeep() {
            gd.turnNumber = 2;
            Card card = createCardWithName("Opponent Trigger Card");
            card.addEffect(EffectSlot.OPPONENT_UPKEEP_TRIGGERED, new GainLifeEffect(1));
            gd.playerBattlefields.get(player2Id).add(new Permanent(card));

            sut.handleUpkeepTriggers(gd);

            assertThat(gd.stack).isNotEmpty();
            assertThat(gd.stack.getFirst().getDescription()).contains("Opponent Trigger Card");
        }

        @Test
        @DisplayName("OPPONENT_UPKEEP_TRIGGERED does not fire for active player's own permanents")
        void opponentUpkeepTriggeredDoesNotFireForOwnPermanents() {
            gd.turnNumber = 2;
            Card card = createCardWithName("Opponent Trigger Card");
            card.addEffect(EffectSlot.OPPONENT_UPKEEP_TRIGGERED, new GainLifeEffect(1));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));

            sut.handleUpkeepTriggers(gd);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("OPPONENT_UPKEEP_TRIGGERED with DealDamageIfFewCardsInHandEffect triggers when hand size meets condition")
        void opponentUpkeepFewCardsTriggersWhenConditionMet() {
            gd.turnNumber = 2;
            Card card = createCardWithName("Punisher Card");
            card.addEffect(EffectSlot.OPPONENT_UPKEEP_TRIGGERED, new DealDamageIfFewCardsInHandEffect(3, 2));
            gd.playerBattlefields.get(player2Id).add(new Permanent(card));

            // Active player has 2 cards in hand (≤ 3)
            gd.playerHands.get(player1Id).add(new Card());
            gd.playerHands.get(player1Id).add(new Card());

            sut.handleUpkeepTriggers(gd);

            assertThat(gd.stack).isNotEmpty();
        }

        @Test
        @DisplayName("OPPONENT_UPKEEP_TRIGGERED with DealDamageIfFewCardsInHandEffect skips when hand too large")
        void opponentUpkeepFewCardsSkipsWhenHandTooLarge() {
            gd.turnNumber = 2;
            Card card = createCardWithName("Punisher Card");
            card.addEffect(EffectSlot.OPPONENT_UPKEEP_TRIGGERED, new DealDamageIfFewCardsInHandEffect(3, 2));
            gd.playerBattlefields.get(player2Id).add(new Permanent(card));

            // Active player has 5 cards in hand (> 3)
            for (int i = 0; i < 5; i++) {
                gd.playerHands.get(player1Id).add(new Card());
            }

            sut.handleUpkeepTriggers(gd);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED fires when enchanted permanent's controller is active")
        void enchantedPermanentControllerUpkeepFires() {
            gd.turnNumber = 2;
            // Create the enchanted permanent (controlled by active player)
            Card targetCard = createCardWithName("Enchanted Creature");
            Permanent targetPerm = new Permanent(targetCard);
            gd.playerBattlefields.get(player1Id).add(targetPerm);

            // Create the aura with the trigger (owned by player2)
            Card auraCard = createCardWithName("Numbing Dose");
            auraCard.addEffect(EffectSlot.ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED,
                    new EnchantedCreatureControllerLosesLifeEffect(1, null));
            Permanent auraPerm = new Permanent(auraCard);
            auraPerm.setAttachedTo(targetPerm.getId());
            gd.playerBattlefields.get(player2Id).add(auraPerm);

            when(gameQueryService.findPermanentController(gd, targetPerm.getId())).thenReturn(player1Id);

            sut.handleUpkeepTriggers(gd);

            assertThat(gd.stack).isNotEmpty();
            assertThat(gd.stack.getFirst().getDescription()).contains("Numbing Dose");
        }

        @Test
        @DisplayName("ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED bakes the enchanted permanent's controller as the stack target")
        void enchantedPermanentControllerUpkeepBakesControllerAsTarget() {
            gd.turnNumber = 2;
            Card targetCard = createCardWithName("Enchanted Enchantment");
            Permanent targetPerm = new Permanent(targetCard);
            gd.playerBattlefields.get(player1Id).add(targetPerm);

            Card auraCard = createCardWithName("Feedback");
            auraCard.addEffect(EffectSlot.ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED,
                    new DealDamageToPlayersEffect(1, DamageRecipient.ENCHANTED_PERMANENT_CONTROLLER));
            Permanent auraPerm = new Permanent(auraCard);
            auraPerm.setAttachedTo(targetPerm.getId());
            gd.playerBattlefields.get(player2Id).add(auraPerm);

            when(gameQueryService.findPermanentController(gd, targetPerm.getId())).thenReturn(player1Id);

            sut.handleUpkeepTriggers(gd);

            assertThat(gd.stack).isNotEmpty();
            assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(player1Id);
        }

        @Test
        @DisplayName("ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED does not fire when enchanted permanent's controller is not active")
        void enchantedPermanentControllerUpkeepDoesNotFireWhenNotActive() {
            gd.turnNumber = 2;
            // Enchanted permanent controlled by player2 (not active)
            Card targetCard = createCardWithName("Enchanted Creature");
            Permanent targetPerm = new Permanent(targetCard);
            gd.playerBattlefields.get(player2Id).add(targetPerm);

            Card auraCard = createCardWithName("Numbing Dose");
            auraCard.addEffect(EffectSlot.ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED,
                    new EnchantedCreatureControllerLosesLifeEffect(1, null));
            Permanent auraPerm = new Permanent(auraCard);
            auraPerm.setAttachedTo(targetPerm.getId());
            gd.playerBattlefields.get(player1Id).add(auraPerm);

            when(gameQueryService.findPermanentController(gd, targetPerm.getId())).thenReturn(player2Id);

            sut.handleUpkeepTriggers(gd);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("ENCHANTED_PLAYER_UPKEEP_TRIGGERED fires when enchanted player is active")
        void enchantedPlayerUpkeepFires() {
            gd.turnNumber = 2;
            Card curseCard = createCardWithName("Curse of Oblivion");
            curseCard.addEffect(EffectSlot.ENCHANTED_PLAYER_UPKEEP_TRIGGERED,
                    new ExileGraveyardCardsEffect(2, GraveyardExileScope.OWN, null, null));
            Permanent cursePerm = new Permanent(curseCard);
            cursePerm.setAttachedTo(player1Id); // Attached to active player
            gd.playerBattlefields.get(player2Id).add(cursePerm);

            sut.handleUpkeepTriggers(gd);

            assertThat(gd.stack).isNotEmpty();
            assertThat(gd.stack.getFirst().getDescription()).contains("Curse of Oblivion");
        }

        @Test
        @DisplayName("ENCHANTED_PLAYER_UPKEEP_TRIGGERED does not fire when enchanted player is not active")
        void enchantedPlayerUpkeepDoesNotFireWhenNotActive() {
            gd.turnNumber = 2;
            Card curseCard = createCardWithName("Curse of Oblivion");
            curseCard.addEffect(EffectSlot.ENCHANTED_PLAYER_UPKEEP_TRIGGERED,
                    new ExileGraveyardCardsEffect(2, GraveyardExileScope.OWN, null, null));
            Permanent cursePerm = new Permanent(curseCard);
            cursePerm.setAttachedTo(player2Id); // Attached to non-active player
            gd.playerBattlefields.get(player1Id).add(cursePerm);

            sut.handleUpkeepTriggers(gd);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("ENCHANTED_PLAYER_UPKEEP_TRIGGERED pushes damage effect with the enchanted player as targetId")
        void enchantedPlayerUpkeepPushesDamageEffect() {
            gd.turnNumber = 2;
            Card curseCard = createCardWithName("Curse of the Bloody Tome");
            curseCard.addEffect(EffectSlot.ENCHANTED_PLAYER_UPKEEP_TRIGGERED,
                    new DealDamageToPlayersEffect(2, DamageRecipient.ENCHANTED_PLAYER));
            Permanent cursePerm = new Permanent(curseCard);
            cursePerm.setAttachedTo(player1Id);
            gd.playerBattlefields.get(player2Id).add(cursePerm);

            sut.handleUpkeepTriggers(gd);

            assertThat(gd.stack).isNotEmpty();
            assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(player1Id);
            DealDamageToPlayersEffect effect =
                    (DealDamageToPlayersEffect) gd.stack.getFirst().getEffectsToResolve().getFirst();
            assertThat(effect.recipient()).isEqualTo(DamageRecipient.ENCHANTED_PLAYER);
        }

        @Test
        @DisplayName("ENCHANTED_PLAYER_UPKEEP_TRIGGERED preserves the attached-count predicate")
        void enchantedPlayerUpkeepPreservesAttachedCountPredicate() {
            gd.turnNumber = 2;
            PermanentPredicate predicate = new PermanentHasSubtypePredicate(CardSubtype.CURSE);
            Card curseCard = createCardWithName("Curse of Thirst");
            curseCard.addEffect(EffectSlot.ENCHANTED_PLAYER_UPKEEP_TRIGGERED,
                    DealDamageToPlayersEffect.enchantedAttachedCount(predicate));
            Permanent cursePerm = new Permanent(curseCard);
            cursePerm.setAttachedTo(player1Id);
            gd.playerBattlefields.get(player2Id).add(cursePerm);

            sut.handleUpkeepTriggers(gd);

            assertThat(gd.stack).isNotEmpty();
            assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(player1Id);
            DealDamageToPlayersEffect effect =
                    (DealDamageToPlayersEffect) gd.stack.getFirst().getEffectsToResolve().getFirst();
            assertThat(effect.recipient()).isEqualTo(DamageRecipient.ENCHANTED_PLAYER);
            assertThat(effect.attachedCountFilter()).isEqualTo(predicate);
        }

        @Test
        @DisplayName("Opening hand triggers fire on turn 1 and push to stack")
        void openingHandTriggersFireOnTurn1() {
            gd.turnNumber = 1;
            Card card = createCardWithName("Chancellor");
            card.addEffect(EffectSlot.ON_OPENING_HAND_REVEAL, new GainLifeEffect(7));
            gd.playerHands.get(player1Id).add(card);

            sut.handleUpkeepTriggers(gd);

            assertThat(gd.stack).isNotEmpty();
            assertThat(gd.stack.getFirst().getDescription()).contains("Chancellor");
        }

        @Test
        @DisplayName("Opening hand MayEffect queues may ability on turn 1")
        void openingHandMayEffectQueuesMayAbility() {
            gd.turnNumber = 1;
            Card card = createCardWithName("Chancellor May");
            card.addEffect(EffectSlot.ON_OPENING_HAND_REVEAL,
                    new MayEffect(new GainLifeEffect(1), "Do something?"));
            gd.playerHands.get(player1Id).add(card);

            sut.handleUpkeepTriggers(gd);

            // queueMayAbility adds to stack
            assertThat(gd.stack).isNotEmpty();
        }

        @Test
        @DisplayName("Opening hand Leyline MayEffect is skipped (handled during pregame)")
        void openingHandLeylineSkipped() {
            gd.turnNumber = 1;
            Card card = createCardWithName("Leyline");
            card.addEffect(EffectSlot.ON_OPENING_HAND_REVEAL,
                    new MayEffect(new LeylineStartOnBattlefieldEffect(), "Put onto battlefield?"));
            gd.playerHands.get(player1Id).add(card);

            sut.handleUpkeepTriggers(gd);

            // Leyline effects are skipped, stack should be empty
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Opening hand triggers do not fire on turn 2+")
        void openingHandTriggersDoNotFireOnTurn2() {
            gd.turnNumber = 2;
            Card card = createCardWithName("Chancellor");
            card.addEffect(EffectSlot.ON_OPENING_HAND_REVEAL, new GainLifeEffect(7));
            gd.playerHands.get(player1Id).add(card);

            sut.handleUpkeepTriggers(gd);

            // Turn 2 — handleOpeningHandTriggers is not called
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("TransformSourceAtNextUpkeep pushes delayed transform trigger onto stack")
        void transformSourceAtNextUpkeepPushesTrigger() {
            gd.turnNumber = 2;
            Card card = createCardWithName("Archangel Avacyn");
            Permanent perm = new Permanent(card);
            gd.playerBattlefields.get(player1Id).add(perm);
            gd.queueDelayedAction(new com.github.laxika.magicalvibes.model.action.TransformSourceAtNextUpkeep(
                    perm.getId(), player1Id, card));

            sut.handleUpkeepTriggers(gd);

            assertThat(gd.stack).isNotEmpty();
            assertThat(gd.stack.getFirst().getDescription()).contains("transform");
            assertThat(gd.stack.getFirst().getSourcePermanentId()).isEqualTo(perm.getId());
            assertThat(gd.stack.getFirst().getEffectsToResolve().getFirst())
                    .isInstanceOf(com.github.laxika.magicalvibes.model.effect.TransformToBackFaceEffect.class);
        }

        @Test
        @DisplayName("MayRevealSubtypeFromHandEffect queues may ability when hand contains matching subtype")
        void mayRevealSubtypeQueuesWhenSubtypeInHand() {
            gd.turnNumber = 2;
            Card priestCard = createCardWithName("Priest of the Wakening Sun");
            priestCard.addEffect(EffectSlot.UPKEEP_TRIGGERED,
                    new MayRevealSubtypeFromHandEffect(CardSubtype.DINOSAUR, new GainLifeEffect(2),
                            "Reveal a Dinosaur card from your hand to gain 2 life?"));
            gd.playerBattlefields.get(player1Id).add(new Permanent(priestCard));

            // Add a Dinosaur card to the player's hand
            Card dinosaur = createCardWithName("Frenzied Raptor");
            dinosaur.setSubtypes(List.of(CardSubtype.DINOSAUR));
            gd.playerHands.get(player1Id).add(dinosaur);

            sut.handleUpkeepTriggers(gd);

            assertThat(gd.stack).isNotEmpty();
            assertThat(gd.stack.getFirst().getEffectsToResolve().getFirst()).isInstanceOf(MayEffect.class);
        }

        @Test
        @DisplayName("MayRevealSubtypeFromHandEffect does not queue when hand lacks matching subtype")
        void mayRevealSubtypeSkipsWhenNoSubtypeInHand() {
            gd.turnNumber = 2;
            Card priestCard = createCardWithName("Priest of the Wakening Sun");
            priestCard.addEffect(EffectSlot.UPKEEP_TRIGGERED,
                    new MayRevealSubtypeFromHandEffect(CardSubtype.DINOSAUR, new GainLifeEffect(2),
                            "Reveal a Dinosaur card from your hand to gain 2 life?"));
            gd.playerBattlefields.get(player1Id).add(new Permanent(priestCard));

            // Add a non-Dinosaur card to the player's hand
            Card bear = createCardWithName("Grizzly Bears");
            bear.setSubtypes(List.of(CardSubtype.BEAR));
            gd.playerHands.get(player1Id).add(bear);

            sut.handleUpkeepTriggers(gd);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("MayRevealSubtypeFromHandEffect does not queue when hand is empty")
        void mayRevealSubtypeSkipsWhenHandEmpty() {
            gd.turnNumber = 2;
            Card priestCard = createCardWithName("Priest of the Wakening Sun");
            priestCard.addEffect(EffectSlot.UPKEEP_TRIGGERED,
                    new MayRevealSubtypeFromHandEffect(CardSubtype.DINOSAUR, new GainLifeEffect(2),
                            "Reveal a Dinosaur card from your hand to gain 2 life?"));
            gd.playerBattlefields.get(player1Id).add(new Permanent(priestCard));

            sut.handleUpkeepTriggers(gd);

            assertThat(gd.stack).isEmpty();
        }
    }

    @Nested
    @DisplayName("handleBeginningOfCombatTriggers")
    class HandleBeginningOfCombatTriggers {

        @Test
        @DisplayName("BEGINNING_OF_COMBAT_TRIGGERED fires only for active player's permanents")
        void beginningOfCombatFiresForActivePlayerOnly() {
            Card activeCard = createCardWithName("Active Combat Card");
            activeCard.addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, new GainLifeEffect(1));
            gd.playerBattlefields.get(player1Id).add(new Permanent(activeCard));

            Card opponentCard = createCardWithName("Opponent Combat Card");
            opponentCard.addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, new GainLifeEffect(1));
            gd.playerBattlefields.get(player2Id).add(new Permanent(opponentCard));

            sut.handleBeginningOfCombatTriggers(gd);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getDescription()).contains("Active Combat Card");
        }

        @Test
        @DisplayName("BEGINNING_OF_COMBAT_TRIGGERED does not fire for non-active player's permanents")
        void beginningOfCombatSkipsNonActivePlayer() {
            Card card = createCardWithName("Combat Trigger Card");
            card.addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, new GainLifeEffect(1));
            gd.playerBattlefields.get(player2Id).add(new Permanent(card));

            sut.handleBeginningOfCombatTriggers(gd);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("EACH_BEGINNING_OF_COMBAT_TRIGGERED fires for non-active player's permanents")
        void eachBeginningOfCombatFiresForAllPlayers() {
            Card card = createCardWithName("Each Combat Card");
            card.addEffect(EffectSlot.EACH_BEGINNING_OF_COMBAT_TRIGGERED, new GainLifeEffect(1));
            gd.playerBattlefields.get(player2Id).add(new Permanent(card));

            sut.handleBeginningOfCombatTriggers(gd);

            assertThat(gd.stack).isNotEmpty();
            assertThat(gd.stack.getFirst().getDescription()).contains("Each Combat Card");
            assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player2Id);
        }

        @Test
        @DisplayName("Targeted beginning-of-combat MayEffect queues target selection before resolution")
        void targetedBeginningOfCombatMayEffectQueuesTargetSelection() {
            Card sourceCard = createCardWithName("Battle-Rattle Shaman");
            sourceCard.target(new PermanentPredicateTargetFilter(
                            new PermanentIsCreaturePredicate(), "Target creature"))
                    .addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                            new MayEffect(new BoostTargetCreatureEffect(2, 0), "Boost target creature?"));
            gd.playerBattlefields.get(player1Id).add(new Permanent(sourceCard));

            Permanent target = new Permanent(createCardWithName("Target Creature"));
            gd.playerBattlefields.get(player2Id).add(target);
            lenient().when(predicateEvaluationService.matchesPermanentPredicate(
                    eq(target), any(PermanentPredicate.class), any())).thenReturn(true);

            sut.handleBeginningOfCombatTriggers(gd);

            verify(playerInputService).beginPermanentChoice(eq(gd), eq(player1Id), any(), any());
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Targeted modal beginning-of-combat trigger queues mode selection")
        void targetedModalBeginningOfCombatQueuesModeSelection() {
            Card sourceCard = createCardWithName("Ferocification");
            sourceCard.addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, new ChooseOneEffect(List.of(
                    new ChooseOneEffect.ChooseOneOption(
                            "Target creature gets +2/+0",
                            new BoostTargetCreatureEffect(2, 0),
                            new PermanentPredicateTargetFilter(
                                    new PermanentIsCreaturePredicate(), "Target creature"))
            )));
            gd.playerBattlefields.get(player1Id).add(new Permanent(sourceCard));

            sut.handleBeginningOfCombatTriggers(gd);

            verify(triggerCollectionService).processNextTriggeredModalTrigger(gd);
            assertThat(gd.stack).isEmpty();
            assertThat(gd.hasPendingInteraction(PermanentChoiceContext.TriggeredModalTrigger.class)).isTrue();
        }

        @Test
        @DisplayName("OPPONENT_BEGINNING_OF_COMBAT_TRIGGERED fires for the non-active player's permanents")
        void opponentBeginningOfCombatFiresForNonActivePlayer() {
            Card card = createCardWithName("Opponent Combat Card");
            card.addEffect(EffectSlot.OPPONENT_BEGINNING_OF_COMBAT_TRIGGERED, new GainLifeEffect(1));
            gd.playerBattlefields.get(player2Id).add(new Permanent(card));

            sut.handleBeginningOfCombatTriggers(gd);

            assertThat(gd.stack).isNotEmpty();
            assertThat(gd.stack.getFirst().getDescription()).contains("Opponent Combat Card");
            assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player2Id);
        }

        @Test
        @DisplayName("OPPONENT_BEGINNING_OF_COMBAT_TRIGGERED does not fire on the controller's own turn")
        void opponentBeginningOfCombatSkipsActivePlayer() {
            Card card = createCardWithName("Opponent Combat Card");
            card.addEffect(EffectSlot.OPPONENT_BEGINNING_OF_COMBAT_TRIGGERED, new GainLifeEffect(1));
            gd.playerBattlefields.get(gd.activePlayerId).add(new Permanent(card));

            sut.handleBeginningOfCombatTriggers(gd);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("AllOf intervening-if skips beginning-of-combat trigger when unmet")
        void allOfInterveningIfSkipsWhenUnmet() {
            Card card = createCardWithName("Graf Rats");
            card.addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, new ConditionalEffect(
                    new com.github.laxika.magicalvibes.model.condition.AllOf(List.of(
                            new com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount(
                                    1, new PermanentHasSubtypePredicate(CardSubtype.RAT)))),
                    new GainLifeEffect(1)));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));

            sut.handleBeginningOfCombatTriggers(gd);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("GRAVEYARD_BEGINNING_OF_COMBAT_TRIGGERED fires when its condition is met")
        void graveyardBeginningOfCombatFiresWhenConditionIsMet() {
            Card card = createCardWithName("Graveyard Combat Card");
            card.addEffect(EffectSlot.GRAVEYARD_BEGINNING_OF_COMBAT_TRIGGERED,
                    new ConditionalEffect(
                            new ControlsPermanentCount(1, new PermanentPowerAtLeastPredicate(4)),
                            new GainLifeEffect(1)));
            gd.playerGraveyards.get(player1Id).add(card);
            gd.playerBattlefields.get(player1Id).add(new Permanent(createCardWithName("Large Creature")));
            when(predicateEvaluationService.matchesPermanentPredicate(
                    any(Permanent.class), any(PermanentPowerAtLeastPredicate.class), any()))
                    .thenReturn(true);

            sut.handleBeginningOfCombatTriggers(gd);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getDescription()).contains("Graveyard Combat Card");
        }

        @Test
        @DisplayName("GRAVEYARD_BEGINNING_OF_COMBAT_TRIGGERED skips when its condition is unmet")
        void graveyardBeginningOfCombatSkipsWhenConditionIsUnmet() {
            Card card = createCardWithName("Graveyard Combat Card");
            card.addEffect(EffectSlot.GRAVEYARD_BEGINNING_OF_COMBAT_TRIGGERED,
                    new ConditionalEffect(
                            new ControlsPermanentCount(1, new PermanentPowerAtLeastPredicate(4)),
                            new GainLifeEffect(1)));
            gd.playerGraveyards.get(player1Id).add(card);

            sut.handleBeginningOfCombatTriggers(gd);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Max speed intervening-if skips beginning-of-combat trigger when unmet")
        void maxSpeedInterveningIfSkipsWhenUnmet() {
            Card card = createCardWithName("Max Speed Combat Card");
            card.addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, new ConditionalEffect(
                    new MaxSpeed(), new GainLifeEffect(1)));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));

            sut.handleBeginningOfCombatTriggers(gd);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Max speed intervening-if allows beginning-of-combat trigger at speed 4")
        void maxSpeedInterveningIfAllowsAtMaxSpeed() {
            Card card = createCardWithName("Max Speed Combat Card");
            card.addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, new ConditionalEffect(
                    new MaxSpeed(), new GainLifeEffect(1)));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));
            gd.playerSpeeds.put(player1Id, 4);

            sut.handleBeginningOfCombatTriggers(gd);

            assertThat(gd.stack).hasSize(1);
        }
    }

    @Nested
    @DisplayName("handleEndStepTriggers")
    class HandleEndStepTriggers {

        @Test
        @DisplayName("Permanent with controller end-step effect pushes trigger onto stack")
        void controllerEndStepEffectPushesTrigger() {
            Card card = createCardWithName("Jin-Gitaxias, Core Augur");
            card.addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new GainLifeEffect(1));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));

            sut.handleEndStepTriggers(gd);

            assertThat(gd.stack).isNotEmpty();
            assertThat(gd.stack.getFirst().getDescription()).contains("Jin-Gitaxias, Core Augur");
        }

        @Test
        @DisplayName("Controller end-step modal effect queues mode selection")
        void controllerEndStepModalEffectQueuesModeSelection() {
            Card card = createCardWithName("Sylvan Scavenging");
            ChooseOneEffect chooseOne = new ChooseOneEffect(List.of(
                    new ChooseOneEffect.ChooseOneOption("Gain life", new GainLifeEffect(1)),
                    new ChooseOneEffect.ChooseOneOption("Gain more life", new GainLifeEffect(2))));
            card.addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, chooseOne);
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));

            sut.handleEndStepTriggers(gd);

            assertThat(gd.stack).isEmpty();
            assertThat(gd.hasPendingInteraction(PermanentChoiceContext.TriggeredModalTrigger.class)).isTrue();
        }

        @Test
        @DisplayName("Granted controller end-step effect pushes trigger onto stack")
        void grantedControllerEndStepEffectPushesTrigger() {
            Card card = createCardWithName("Granted End Step Card");
            Permanent permanent = new Permanent(card);
            gd.playerBattlefields.get(player1Id).add(permanent);
            when(grantedTriggeredAbilitySupport.grantedTriggeredEffects(
                    gd, permanent, EffectSlot.CONTROLLER_END_STEP_TRIGGERED))
                    .thenReturn(List.of(new GainLifeEffect(1)));

            sut.handleEndStepTriggers(gd);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getDescription()).contains("Granted End Step Card");
        }

        @Test
        @DisplayName("Controller end-step permanent does not trigger during opponent's end step")
        void controllerEndStepDoesNotTriggerDuringOpponentEndStep() {
            gd.activePlayerId = player2Id;
            Card card = createCardWithName("Jin-Gitaxias, Core Augur");
            card.addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new GainLifeEffect(1));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));

            sut.handleEndStepTriggers(gd);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Controller end-step multi-target effect queues slot-by-slot target selection")
        void controllerEndStepMultiTargetEffectQueuesMultiTargetSelection() {
            Card card = createCardWithName("Magmatic Core");
            DealDividedDamageEffect effect = DealDividedDamageEffect.xAmongTargetCreaturesAtResolution();
            card.target(new PermanentPredicateTargetFilter(new PermanentIsCreaturePredicate(),
                    "Target must be a creature."), 0, 99)
                    .addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, effect);
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));
            when(etbTokenTargetService.needsSlotBySlotTargetSelection(card)).thenReturn(true);

            sut.handleEndStepTriggers(gd);

            assertThat(gd.stack).isEmpty();
            assertThat(gd.hasPendingInteraction(PermanentChoiceContext.ETBTokenMultiTargetTrigger.class)).isTrue();
        }

        @Test
        @DisplayName("No triggers pushed when no permanents with end-step effects")
        void noTriggersWhenNoPermanentsWithEndStepEffects() {
            Card card = createCardWithName("Grizzly Bears");
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));

            sut.handleEndStepTriggers(gd);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("END_STEP_TRIGGERED fires for any player's permanents")
        void endStepTriggeredFiresForAnyPlayer() {
            Card card = createCardWithName("End Step Card");
            card.addEffect(EffectSlot.END_STEP_TRIGGERED, new GainLifeEffect(1));
            gd.playerBattlefields.get(player2Id).add(new Permanent(card));

            sut.handleEndStepTriggers(gd);

            assertThat(gd.stack).isNotEmpty();
            assertThat(gd.stack.getFirst().getDescription()).contains("End Step Card");
        }

        @Test
        @DisplayName("END_STEP_TRIGGERED EndStepPlayerTargetedEffect bakes the end-step player into targetId")
        void endStepPlayerTargetedEffectBakesActivePlayer() {
            Card card = createCardWithName("Monsoon-like Card");
            card.addEffect(EffectSlot.END_STEP_TRIGGERED,
                    new TapPlayersPermanentsAndDamageEqualToCountEffect(new PermanentHasSubtypePredicate(CardSubtype.ISLAND)));
            gd.playerBattlefields.get(player2Id).add(new Permanent(card));

            sut.handleEndStepTriggers(gd);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(gd.activePlayerId);
        }

        @Test
        @DisplayName("END_STEP_TRIGGERED with a plain effect gets a null targetId")
        void endStepPlainEffectHasNoTargetId() {
            Card card = createCardWithName("Plain End Step Card");
            card.addEffect(EffectSlot.END_STEP_TRIGGERED, new GainLifeEffect(1));
            gd.playerBattlefields.get(player2Id).add(new Permanent(card));

            sut.handleEndStepTriggers(gd);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getTargetId()).isNull();
        }

        @Test
        @DisplayName("END_STEP_TRIGGERED NotControllerTurn fires on an opponent's end step")
        void endStepNotControllerTurnFiresOnOpponentTurn() {
            Card card = createCardWithName("Opponent Turn Card");
            card.addEffect(EffectSlot.END_STEP_TRIGGERED,
                    new ConditionalEffect(new NotControllerTurn(), new GainLifeEffect(1)));
            gd.playerBattlefields.get(player2Id).add(new Permanent(card));

            sut.handleEndStepTriggers(gd);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getDescription()).contains("Opponent Turn Card");
        }

        @Test
        @DisplayName("END_STEP_TRIGGERED NotControllerTurn is skipped on the controller's own end step")
        void endStepNotControllerTurnSkippedOnOwnTurn() {
            Card card = createCardWithName("Own Turn Card");
            card.addEffect(EffectSlot.END_STEP_TRIGGERED,
                    new ConditionalEffect(new NotControllerTurn(), new GainLifeEffect(1)));
            gd.playerBattlefields.get(gd.activePlayerId).add(new Permanent(card));

            sut.handleEndStepTriggers(gd);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("END_STEP_TRIGGERED with MayEffect queues may ability")
        void endStepMayEffectQueuesMayAbility() {
            Card card = createCardWithName("Optional End Step Card");
            card.addEffect(EffectSlot.END_STEP_TRIGGERED, new MayEffect(new GainLifeEffect(1), "Gain life?"));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));

            sut.handleEndStepTriggers(gd);

            // MayEffect goes through queueMayAbility which adds to stack
            assertThat(gd.stack).isNotEmpty();
        }

        @Test
        @DisplayName("GRAVEYARD_CONTROLLER_END_STEP_TRIGGERED fires during the card owner's end step")
        void graveyardControllerEndStepTriggeredFiresOnOwnersEndStep() {
            Card card = createCardWithName("Silversmote Ghoul");
            card.addEffect(EffectSlot.GRAVEYARD_CONTROLLER_END_STEP_TRIGGERED, new GainLifeEffect(1));
            gd.playerGraveyards.get(player1Id).add(card);

            sut.handleEndStepTriggers(gd);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getDescription()).contains("Silversmote Ghoul");
        }

        @Test
        @DisplayName("GRAVEYARD_CONTROLLER_END_STEP_TRIGGERED skips an opponent's end step")
        void graveyardControllerEndStepTriggeredSkipsOpponentsEndStep() {
            gd.activePlayerId = player2Id;
            Card card = createCardWithName("Silversmote Ghoul");
            card.addEffect(EffectSlot.GRAVEYARD_CONTROLLER_END_STEP_TRIGGERED, new GainLifeEffect(1));
            gd.playerGraveyards.get(player1Id).add(card);

            sut.handleEndStepTriggers(gd);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("END_STEP_TRIGGERED ConditionalEffect wrapping MayEffect triggers when an opponent lost enough life")
        void endStepConditionalMayTriggersWhenOpponentLostLife() {
            Card card = createCardWithName("River Cutthroat");
            card.addEffect(EffectSlot.END_STEP_TRIGGERED, new ConditionalEffect(
                    new OpponentLostLifeThisTurn(3), new MayEffect(new GainLifeEffect(1), "Draw?")));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));
            gd.lifeLostThisTurn.put(player2Id, 3);

            sut.handleEndStepTriggers(gd);

            assertThat(gd.stack).isNotEmpty();
            assertThat(gd.stack.getFirst().getDescription()).contains("River Cutthroat");
        }

        @Test
        @DisplayName("END_STEP_TRIGGERED ConditionalEffect wrapping MayEffect skips when no opponent lost enough life")
        void endStepConditionalMaySkipsWhenThresholdNotMet() {
            Card card = createCardWithName("River Cutthroat");
            card.addEffect(EffectSlot.END_STEP_TRIGGERED, new ConditionalEffect(
                    new OpponentLostLifeThisTurn(3), new MayEffect(new GainLifeEffect(1), "Draw?")));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));
            gd.lifeLostThisTurn.put(player2Id, 2);

            sut.handleEndStepTriggers(gd);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("CONTROLLER_END_STEP_TRIGGERED with MayEffect queues may ability")
        void controllerEndStepMayEffectQueuesMayAbility() {
            Card card = createCardWithName("Controller May Card");
            card.addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                    new MayEffect(new GainLifeEffect(1), "Gain life?"));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));

            sut.handleEndStepTriggers(gd);

            assertThat(gd.stack).isNotEmpty();
        }

        @Test
        @DisplayName("ConditionalEffect triggers when creature did not attack this turn")
        void didntAttackTriggersWhenNotAttacked() {
            Card card = createCardWithName("Vigilant Creature");
            card.addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                    new ConditionalEffect(new DidntAttack(), new GainLifeEffect(1)));
            Permanent perm = new Permanent(card);
            // Did not attack this turn (default)
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.handleEndStepTriggers(gd);

            assertThat(gd.stack).isNotEmpty();
            assertThat(gd.stack.getFirst().getDescription()).contains("Vigilant Creature");
        }

        @Test
        @DisplayName("ConditionalEffect skips when creature attacked this turn")
        void didntAttackSkipsWhenAttacked() {
            Card card = createCardWithName("Vigilant Creature");
            card.addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                    new ConditionalEffect(new DidntAttack(), new GainLifeEffect(1)));
            Permanent perm = new Permanent(card);
            perm.setAttackedThisTurn(true);
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.handleEndStepTriggers(gd);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("CONTROLLER_END_STEP_TRIGGERED with ConditionalEffect wrapping MayEffect queues may ability when raid met")
        void raidConditionalEndStepMayEffectQueuedWhenRaidMet() {
            Card card = createCardWithName("Raiding Looter");
            card.addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                    new ConditionalEffect(new Raid(), new MayEffect(new GainLifeEffect(1), "Gain life?")));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));
            gd.playersDeclaredAttackersThisTurn.add(player1Id);

            sut.handleEndStepTriggers(gd);

            assertThat(gd.stack).isNotEmpty();
        }

        @Test
        @DisplayName("CONTROLLER_END_STEP_TRIGGERED with ConditionalEffect skips when raid not met")
        void raidConditionalEndStepSkipsWhenRaidNotMet() {
            Card card = createCardWithName("Raiding Looter");
            card.addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                    new ConditionalEffect(new Raid(), new MayEffect(new GainLifeEffect(1), "Gain life?")));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));
            // Do NOT add player1Id to playersDeclaredAttackersThisTurn

            sut.handleEndStepTriggers(gd);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("CONTROLLER_END_STEP_TRIGGERED with ConditionalEffect wrapping non-MayEffect pushes to stack when raid met")
        void raidConditionalEndStepNonMayPushesToStackWhenRaidMet() {
            Card card = createCardWithName("Raiding Creature");
            card.addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                    new ConditionalEffect(new Raid(), new GainLifeEffect(1)));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));
            gd.playersDeclaredAttackersThisTurn.add(player1Id);

            sut.handleEndStepTriggers(gd);

            assertThat(gd.stack).isNotEmpty();
            assertThat(gd.stack.getFirst().getDescription()).contains("Raiding Creature");
        }

        @Test
        @DisplayName("CONTROLLER_END_STEP_TRIGGERED with ConditionalEffect wrapping targeting effect queues for target selection when raid met")
        void raidConditionalEndStepTargetingEffectQueuesWhenRaidMet() {
            Card card = createCardWithName("Navigator's Ruin");
            card.addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                    new ConditionalEffect(new Raid(), new MillEffect(4, MillRecipient.TARGET_PLAYER)));
            card.setCastTimeTargetFilter(new PlayerPredicateTargetFilter(
                    new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                    "Target must be an opponent"));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));
            gd.playersDeclaredAttackersThisTurn.add(player1Id);

            sut.handleEndStepTriggers(gd);

            // Should not push directly to stack — should queue for target selection
            assertThat(gd.stack).isEmpty();
            assertThat(gd.hasPendingInteraction(PermanentChoiceContext.EndStepTriggerTarget.class)).isFalse(); // processed immediately
            // processNextEndStepTriggerTarget fires and presents choice
            verify(playerInputService).beginPermanentChoice(eq(gd), eq(player1Id), any(), any());
        }

        @Test
        @DisplayName("CONTROLLER_END_STEP_TRIGGERED with ConditionalEffect wrapping targeting effect skips when raid not met")
        void raidConditionalEndStepTargetingEffectSkipsWhenRaidNotMet() {
            Card card = createCardWithName("Navigator's Ruin");
            card.addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                    new ConditionalEffect(new Raid(), new MillEffect(4, MillRecipient.TARGET_PLAYER)));
            card.setCastTimeTargetFilter(new PlayerPredicateTargetFilter(
                    new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                    "Target must be an opponent"));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));
            // Do NOT add player1Id to playersDeclaredAttackersThisTurn

            sut.handleEndStepTriggers(gd);

            assertThat(gd.stack).isEmpty();
            assertThat(gd.hasPendingInteraction(PermanentChoiceContext.EndStepTriggerTarget.class)).isFalse();
            verify(playerInputService, never()).beginPermanentChoice(any(), any(), any(), any());
        }

        @Test
        @DisplayName("processNextEndStepTriggerTarget with PlayerPredicateTargetFilter OPPONENT excludes controller from valid targets")
        void endStepTriggerOpponentFilterExcludesController() {
            Card card = createCardWithName("Navigator's Ruin");
            card.setCastTimeTargetFilter(new PlayerPredicateTargetFilter(
                    new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                    "Target must be an opponent"));
            gd.queueInteraction(new PermanentChoiceContext.EndStepTriggerTarget(
                    card, player1Id, new ArrayList<>(List.of(new MillEffect(4, MillRecipient.TARGET_PLAYER))),
                    UUID.randomUUID()));

            sut.processNextEndStepTriggerTarget(gd);

            // Should present choice with only opponent (player2), not controller (player1)
            verify(playerInputService).beginPermanentChoice(eq(gd), eq(player1Id),
                    eq(List.of(player2Id)), any());
        }

        @Test
        @DisplayName("ConditionalEffect triggers when permanent was not kicked")
        void notKickedTriggersWhenNotKicked() {
            Card card = createCardWithName("Unkicked Elemental");
            card.addEffect(EffectSlot.END_STEP_TRIGGERED,
                    new ConditionalEffect(new NotKicked(), new SacrificeSelfEffect()));
            Permanent perm = new Permanent(card);
            // Not kicked (default)
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.handleEndStepTriggers(gd);

            assertThat(gd.stack).isNotEmpty();
            assertThat(gd.stack.getFirst().getDescription()).contains("Unkicked Elemental");
        }

        @Test
        @DisplayName("ConditionalEffect skips when permanent was kicked")
        void notKickedSkipsWhenKicked() {
            Card card = createCardWithName("Kicked Elemental");
            card.addEffect(EffectSlot.END_STEP_TRIGGERED,
                    new ConditionalEffect(new NotKicked(), new SacrificeSelfEffect()));
            Permanent perm = new Permanent(card);
            perm.setKicked(true);
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.handleEndStepTriggers(gd);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Delegates scheduled end-step zone changes to PermanentRemovalService")
        void delegatesEndStepDelayedPermanentActions() {
            sut.handleEndStepTriggers(gd);

            verify(permanentRemovalService).processDelayedPermanentActions(gd,
                    DelayedPermanentActionKind.EXILE_TOKEN_AT_END_STEP);
            verify(permanentRemovalService).processDelayedPermanentActions(gd,
                    DelayedPermanentActionKind.EXILE_AT_END_STEP);
            verify(permanentRemovalService).processDelayedPermanentActions(gd,
                    DelayedPermanentActionKind.SACRIFICE_AT_END_STEP);
            verify(permanentRemovalService).processDelayedPermanentActions(gd,
                    DelayedPermanentActionKind.DESTROY_AT_END_STEP);
            verify(permanentRemovalService).processDelayedPermanentActions(gd,
                    DelayedPermanentActionKind.RETURN_TO_HAND_AT_END_STEP);
        }

        @Test
        @DisplayName("Exiles controller-scoped delayed permanents only on that player's end step")
        void controllerScopedDelayedExileWaitsForControllerEndStep() {
            Card card = createCardWithName("Delayed token");
            Permanent permanent = new Permanent(card);
            gd.playerBattlefields.get(player1Id).add(permanent);
            ExilePermanentAtControllerEndStep action =
                    new ExilePermanentAtControllerEndStep(permanent.getId(), player1Id);
            gd.queueDelayedAction(action);

            gd.activePlayerId = player2Id;
            sut.handleEndStepTriggers(gd);

            verify(permanentRemovalService, never()).removePermanentToExile(gd, permanent);
            assertThat(gd.getDelayedActions(ExilePermanentAtControllerEndStep.class)).containsExactly(action);

            gd.activePlayerId = player1Id;
            when(gameQueryService.findPermanentById(gd, permanent.getId())).thenReturn(permanent);
            when(permanentRemovalService.removePermanentToExile(gd, permanent)).thenReturn(true);

            sut.handleEndStepTriggers(gd);

            verify(permanentRemovalService).removePermanentToExile(gd, permanent);
            assertThat(gd.getDelayedActions(ExilePermanentAtControllerEndStep.class)).isEmpty();
        }

        @Test
        @DisplayName("Sacrifices the delayed target and gains life equal to its toughness")
        void sacrificesDelayedTargetAndGainsLife() {
            Card sourceCard = createCardWithName("Spinal Embrace");
            Card targetCard = createCardWithName("Target Creature");
            Permanent target = new Permanent(targetCard);
            gd.playerBattlefields.get(player1Id).add(target);
            gd.queueDelayedAction(new DelayedSacrificeTargetPermanentAtEndStep(
                    target.getId(), player1Id, sourceCard));

            when(gameQueryService.findPermanentById(gd, target.getId())).thenReturn(target);
            when(gameQueryService.findPermanentController(gd, target.getId())).thenReturn(player1Id);
            when(gameQueryService.getEffectiveToughness(gd, target)).thenReturn(4);
            when(permanentRemovalService.removePermanentToGraveyard(gd, target)).thenReturn(true);

            sut.handleEndStepTriggers(gd);

            verify(permanentRemovalService).removePermanentToGraveyard(gd, target);
            verify(lifeSupport).applyGainLife(
                    gd, player1Id, 4, "Spinal Embrace", sourceCard, StackEntryType.TRIGGERED_ABILITY);
            assertThat(gd.getDelayedActions(DelayedSacrificeTargetPermanentAtEndStep.class)).isEmpty();
        }

        @Test
        @DisplayName("Sacrifices a delayed target when its mana value is within the limit")
        void sacrificesDelayedTargetWithinManaValueLimit() {
            Card targetCard = createCardWithName("Target Creature");
            Permanent target = new Permanent(targetCard);
            gd.playerBattlefields.get(player1Id).add(target);
            gd.queueDelayedAction(new DelayedSacrificeTargetPermanentAtEndStepIfManaValueAtMost(
                    target.getId(), player1Id, 3));

            when(gameQueryService.findPermanentById(gd, target.getId())).thenReturn(target);
            when(gameQueryService.findPermanentController(gd, target.getId())).thenReturn(player1Id);
            when(permanentRemovalService.removePermanentToGraveyard(gd, target)).thenReturn(true);

            sut.handleEndStepTriggers(gd);

            verify(permanentRemovalService).removePermanentToGraveyard(gd, target);
            assertThat(gd.getDelayedActions(DelayedSacrificeTargetPermanentAtEndStepIfManaValueAtMost.class))
                    .isEmpty();
        }

        @Test
        @DisplayName("Pending exile returns return cards from exile to battlefield")
        void pendingExileReturnsProcessed() {
            Card card = createCardWithName("Exiled Card");
            gd.addToExile(player1Id, card);
            gd.queueDelayedAction(new PendingExileReturn(card, player1Id, false));

            sut.processPendingExileReturns(gd, TurnStep.END_STEP);

            verify(battlefieldEntryService).putPermanentOntoBattlefield(
                    eq(gd), eq(player1Id), any(Permanent.class), any(), any());
            verify(battlefieldEntryService).handleCreatureEnteredBattlefield(eq(gd), eq(player1Id), eq(card), any(), eq(false));
            assertThat(gd.getPlayerExiledCards(player1Id)).doesNotContain(card);
            assertThat(gd.getDelayedActions(PendingExileReturn.class)).isEmpty();
        }

        @Test
        @DisplayName("Pending exile returns with returnTapped taps the permanent")
        void pendingExileReturnsTapped() {
            Card card = createCardWithName("Exiled Card");
            gd.addToExile(player1Id, card);
            gd.queueDelayedAction(new PendingExileReturn(card, player1Id, true));

            sut.processPendingExileReturns(gd, TurnStep.END_STEP);

            verify(battlefieldEntryService).putPermanentOntoBattlefield(
                    eq(gd), eq(player1Id), argThat(Permanent::isTapped), any(), any());
        }

        @Test
        @DisplayName("Pending exile returns can put a permanent onto the battlefield attacking")
        void pendingExileReturnsAttacking() {
            Card card = createCardWithName("Exiled Card");
            gd.addToExile(player1Id, card);
            gd.queueDelayedAction(new PendingExileReturn(card, player1Id, true, false,
                    TurnStep.DECLARE_ATTACKERS, 0, List.of(), true, false, true));

            sut.processPendingExileReturns(gd, TurnStep.DECLARE_ATTACKERS);

            verify(battlefieldEntryService).putPermanentOntoBattlefield(
                    eq(gd), eq(player1Id), argThat(permanent -> permanent.isTapped()
                            && permanent.isAttacking()
                            && player2Id.equals(permanent.getAttackTarget())), any(), any());
        }

        @Test
        @DisplayName("Memory Jar delayed hand exchange is pushed onto the stack at the next end step")
        void memoryJarDelayedHandExchangeIsPushed() {
            Card source = createCardWithName("Memory Jar");
            gd.queueDelayedAction(new EachPlayerHandExileReturnAtNextEndStep(
                    source, player1Id, List.of(
                            new EachPlayerHandExileReturnAtNextEndStep.PlayerCards(player1Id, List.of()),
                            new EachPlayerHandExileReturnAtNextEndStep.PlayerCards(player2Id, List.of()))));

            sut.handleEndStepTriggers(gd);

            assertThat(gd.getDelayedActions(EachPlayerHandExileReturnAtNextEndStep.class)).isEmpty();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEffectsToResolve().getFirst())
                    .isInstanceOf(DiscardEachPlayerHandAndReturnExiledCardsEffect.class);
            assertThat(gd.stack.getFirst().isNonTargeting()).isTrue();
        }

        @Test
        @DisplayName("Delayed +1/+1 counter triggers push onto stack")
        void delayedPlusOnePlusOneCountersPushed() {
            Card card = createCardWithName("Protean Hydra");
            Permanent perm = new Permanent(card);
            gd.playerBattlefields.get(player1Id).add(perm);
            // 4 total counters to add = 2 triggers (each adds 2)
            gd.addDelayedPlusOneCounters(perm.getId(), 4);

            when(gameQueryService.findPermanentById(gd, perm.getId())).thenReturn(perm);
            when(gameQueryService.findPermanentController(gd, perm.getId())).thenReturn(player1Id);

            sut.handleEndStepTriggers(gd);

            assertThat(gd.stack).hasSize(2);
            assertThat(gd.stack.getFirst().getDescription()).contains("Protean Hydra");
            assertThat(gd.getDelayedActions(DelayedPlusOneCounters.class)).isEmpty();
        }

        @Test
        @DisplayName("Delayed destroy-all-permanents trigger pushes a global wipe onto the stack")
        void delayedDestroyAllPermanentsPushed() {
            Card sourceCard = createCardWithName("Bearer of the Heavens");
            gd.queueDelayedAction(new DelayedDestroyAllPermanents(player1Id, sourceCard));

            sut.handleEndStepTriggers(gd);

            assertThat(gd.getDelayedActions(DelayedDestroyAllPermanents.class)).isEmpty();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1Id);
            assertThat(gd.stack.getFirst().getEffectsToResolve())
                    .containsExactly(new DestroyAllPermanentsEffect(new PermanentTruePredicate()));
        }

        @Test
        @DisplayName("Delayed discard trigger pushes a controller discard onto the stack")
        void delayedDiscardPushed() {
            Card sourceCard = createCardWithName("Ideas Unbound");
            gd.queueDelayedAction(new DiscardCardsAtNextEndStep(player1Id, 3, sourceCard));

            sut.handleEndStepTriggers(gd);

            assertThat(gd.getDelayedActions(DiscardCardsAtNextEndStep.class)).isEmpty();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1Id);
            assertThat(gd.stack.getFirst().getEffectsToResolve())
                    .containsExactly(new DiscardEffect(3, DiscardRecipient.CONTROLLER));
            assertThat(gd.stack.getFirst().isNonTargeting()).isTrue();
        }
    }

    @Nested
    @DisplayName("handlePrecombatMainTriggers")
    class HandlePrecombatMainTriggers {

        @Test
        @DisplayName("Does nothing when there are no opening hand mana triggers")
        void doesNothingWithNoOpeningHandTriggers() {
            sut.handlePrecombatMainTriggers(gd);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Fires mana trigger for active player")
        void firesTriggerForActivePlayer() {
            Card card = createCardWithName("Chancellor of the Tangle");
            GainLifeEffect effect = new GainLifeEffect(1);
            gd.openingHandManaTriggers.add(new OpeningHandRevealTrigger(player1Id, card, effect));

            sut.handlePrecombatMainTriggers(gd);

            assertThat(gd.stack).isNotEmpty();
            assertThat(gd.stack.getFirst().getDescription()).contains("Chancellor of the Tangle");
            assertThat(gd.openingHandManaTriggers).isEmpty();
        }

        @Test
        @DisplayName("Does not fire trigger for non-active player")
        void doesNotFireTriggerForNonActivePlayer() {
            Card card = createCardWithName("Chancellor of the Tangle");
            GainLifeEffect effect = new GainLifeEffect(1);
            gd.openingHandManaTriggers.add(new OpeningHandRevealTrigger(player2Id, card, effect));

            sut.handlePrecombatMainTriggers(gd);

            assertThat(gd.stack).isEmpty();
            assertThat(gd.openingHandManaTriggers).hasSize(1); // Not removed
        }

        @Test
        @DisplayName("Saga on active player's battlefield gets a lore counter at precombat main")
        void sagaGetsLoreCounterAtPrecombatMain() {
            Card saga = createSaga("Test Saga");
            Permanent perm = new Permanent(saga);
            perm.setCounterCount(CounterType.LORE, 1); // already has 1 from ETB
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.handlePrecombatMainTriggers(gd);

            assertThat(perm.getCounterCount(CounterType.LORE)).isEqualTo(2);
        }

        @Test
        @DisplayName("Saga triggers chapter ability matching new lore counter value")
        void sagaTriggersCorrectChapter() {
            Card saga = createSaga("Test Saga");
            saga.addEffect(EffectSlot.SAGA_CHAPTER_II, new GainLifeEffect(3));
            Permanent perm = new Permanent(saga);
            perm.setCounterCount(CounterType.LORE, 1); // chapter II will trigger when counter goes to 2
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.handlePrecombatMainTriggers(gd);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(gd.stack.getFirst().getDescription()).contains("chapter II");
            assertThat(gd.stack.getFirst().getEffectsToResolve()).hasSize(1);
        }

        @Test
        @DisplayName("Saga target groups trigger target selection for optional multi-target chapters")
        void sagaTargetGroupsTriggerSelectionForOptionalChapter() {
            Card saga = createSaga("Targeted Saga");
            saga.addEffect(EffectSlot.SAGA_CHAPTER_I,
                    new MayEffect(new GainLifeEffect(1), "Gain life?"));
            saga.setSagaChapterTargetGroups(EffectSlot.SAGA_CHAPTER_I, List.of(
                    new SagaChapterTargetGroup(new PermanentPredicateTargetFilter(
                            new PermanentIsCreaturePredicate(), "Target must be a creature"), 1, 1)));
            Permanent sagaPermanent = new Permanent(saga);
            sagaPermanent.setCounterCount(CounterType.LORE, 0);
            gd.playerBattlefields.get(player1Id).add(sagaPermanent);

            sut.handlePrecombatMainTriggers(gd);

            assertThat(gd.stack).isEmpty();
            assertThat(gd.hasPendingInteraction(PermanentChoiceContext.SagaChapterTarget.class)).isTrue();
            verify(triggerCollectionService).processNextSagaChapterTarget(gd);
        }

        @Test
        @DisplayName("Saga chapter ability has correct source permanent ID")
        void sagaChapterHasSourcePermanentId() {
            Card saga = createSaga("Test Saga");
            saga.addEffect(EffectSlot.SAGA_CHAPTER_II, new GainLifeEffect(3));
            Permanent perm = new Permanent(saga);
            perm.setCounterCount(CounterType.LORE, 1);
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.handlePrecombatMainTriggers(gd);

            assertThat(gd.stack.getFirst().getSourcePermanentId()).isEqualTo(perm.getId());
        }

        @Test
        @DisplayName("Non-active player's Saga does not get a lore counter")
        void opponentSagaNotAffected() {
            Card saga = createSaga("Opponent Saga");
            saga.addEffect(EffectSlot.SAGA_CHAPTER_II, new GainLifeEffect(3));
            Permanent perm = new Permanent(saga);
            perm.setCounterCount(CounterType.LORE, 1);
            gd.playerBattlefields.get(player2Id).add(perm);

            sut.handlePrecombatMainTriggers(gd);

            assertThat(perm.getCounterCount(CounterType.LORE)).isEqualTo(1); // unchanged
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Non-saga permanents are not affected by saga lore counter logic")
        void nonSagaPermanentNotAffected() {
            Card creature = createCardWithName("Regular Creature");
            creature.setType(CardType.CREATURE);
            Permanent perm = new Permanent(creature);
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.handlePrecombatMainTriggers(gd);

            assertThat(perm.getCounterCount(CounterType.LORE)).isZero();
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Multiple Sagas each get a lore counter")
        void multipleSagasEachGetLoreCounter() {
            Card saga1 = createSaga("Saga A");
            saga1.addEffect(EffectSlot.SAGA_CHAPTER_II, new GainLifeEffect(1));
            Permanent perm1 = new Permanent(saga1);
            perm1.setCounterCount(CounterType.LORE, 1);

            Card saga2 = createSaga("Saga B");
            saga2.addEffect(EffectSlot.SAGA_CHAPTER_III, new GainLifeEffect(2));
            Permanent perm2 = new Permanent(saga2);
            perm2.setCounterCount(CounterType.LORE, 2);

            gd.playerBattlefields.get(player1Id).add(perm1);
            gd.playerBattlefields.get(player1Id).add(perm2);

            sut.handlePrecombatMainTriggers(gd);

            assertThat(perm1.getCounterCount(CounterType.LORE)).isEqualTo(2);
            assertThat(perm2.getCounterCount(CounterType.LORE)).isEqualTo(3);
            assertThat(gd.stack).hasSize(2); // both chapters triggered
        }

        @Test
        @DisplayName("Saga with no effects for the reached chapter doesn't push to stack")
        void sagaNoEffectsForChapterDoesNotTrigger() {
            Card saga = createSaga("Sparse Saga");
            saga.addEffect(EffectSlot.SAGA_CHAPTER_I, new GainLifeEffect(1));
            // no chapter II effects
            Permanent perm = new Permanent(saga);
            perm.setCounterCount(CounterType.LORE, 1); // will go to 2, but chapter II has no effects
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.handlePrecombatMainTriggers(gd);

            assertThat(perm.getCounterCount(CounterType.LORE)).isEqualTo(2); // counter still incremented
            assertThat(gd.stack).isEmpty(); // but no ability triggered
        }

        @Test
        @DisplayName("Saga lore counter logs are broadcast")
        void sagaLoreCounterLogsBroadcast() {
            Card saga = createSaga("Chainer's Torment");
            saga.addEffect(EffectSlot.SAGA_CHAPTER_II, new GainLifeEffect(2));
            Permanent perm = new Permanent(saga);
            perm.setCounterCount(CounterType.LORE, 1);
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.handlePrecombatMainTriggers(gd);

            verify(gameLogService).append(eq(gd), argThat((GameLogEntry e) -> e.plainText().equals("Chainer's Torment gets a lore counter (2).")));
            verify(gameLogService).append(eq(gd), argThat((GameLogEntry e) -> e.plainText().equals("Chainer's Torment's chapter II ability triggers.")));
        }
    }

    @Nested
    @DisplayName("handlePostcombatMainTriggers")
    class HandlePostcombatMainTriggers {

        @Test
        @DisplayName("Pushes POSTCOMBAT_MAIN_TRIGGERED effects for the active player")
        void firesTriggerForActivePlayer() {
            Card card = createCardWithName("Neheb, the Eternal");
            card.addEffect(EffectSlot.POSTCOMBAT_MAIN_TRIGGERED, new GainLifeEffect(1));
            Permanent perm = new Permanent(card);
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.handlePostcombatMainTriggers(gd);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(gd.stack.getFirst().getSourcePermanentId()).isEqualTo(perm.getId());
        }

        @Test
        @DisplayName("Does not fire for permanents the non-active player controls")
        void doesNotFireForNonActivePlayer() {
            Card card = createCardWithName("Neheb, the Eternal");
            card.addEffect(EffectSlot.POSTCOMBAT_MAIN_TRIGGERED, new GainLifeEffect(1));
            gd.playerBattlefields.get(player2Id).add(new Permanent(card));

            sut.handlePostcombatMainTriggers(gd);

            assertThat(gd.stack).isEmpty();
        }
    }

    // ---- Test helpers ----

    private static Card createCardWithName(String name) {
        Card card = new Card();
        card.setName(name);
        return card;
    }

    private static Card createSaga(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ENCHANTMENT);
        card.setSubtypes(List.of(CardSubtype.SAGA));
        return card;
    }
}
