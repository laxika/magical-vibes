package com.github.laxika.magicalvibes.service.turn;
import com.github.laxika.magicalvibes.model.action.DelayedDrawStepTriggerWhileSourceTapped;
import com.github.laxika.magicalvibes.model.action.AddManaAtNextMainPhase;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextMainPhase;
import com.github.laxika.magicalvibes.model.action.DelayedGraveyardToBattlefieldSelfReturn;
import com.github.laxika.magicalvibes.model.action.DelayedGraveyardToBattlefieldTransformedReturn;
import com.github.laxika.magicalvibes.model.action.DelayedGraveyardToBattlefieldUnderControl;
import com.github.laxika.magicalvibes.model.action.DelayedGraveyardCardsToBattlefieldUnderControl;
import com.github.laxika.magicalvibes.model.action.DelayedGraveyardToHandReturn;
import com.github.laxika.magicalvibes.model.action.DelayedReturnAuraAttachedToPermanent;
import com.github.laxika.magicalvibes.model.action.DelayedReturnCurseAttachedToPlayer;
import com.github.laxika.magicalvibes.model.action.DelayedReturnSourceAuraToCreature;
import com.github.laxika.magicalvibes.model.action.DelayedEndOfCombatTrigger;
import com.github.laxika.magicalvibes.model.action.DelayedBeginningOfCombatTrigger;
import com.github.laxika.magicalvibes.model.action.DelayedCreateToken;
import com.github.laxika.magicalvibes.model.action.DelayedCreateTokenAtNextUpkeep;
import com.github.laxika.magicalvibes.model.action.CyclopeanTombUpkeepCleanup;
import com.github.laxika.magicalvibes.model.action.DelayedCreateTokenCopy;
import com.github.laxika.magicalvibes.model.action.DelayedRevealCreatureCardsToBattlefield;
import com.github.laxika.magicalvibes.model.action.DelayedExileCreatedPermanentsAtEndStep;
import com.github.laxika.magicalvibes.model.action.DelayedChooseOpponentGainsControlOfSource;
import com.github.laxika.magicalvibes.model.action.DiscardCardsAtNextEndStep;
import com.github.laxika.magicalvibes.model.action.DiscardCardsAtNextTurnEndStep;
import com.github.laxika.magicalvibes.model.action.DiscardSpecificCardAtNextEndStep;
import com.github.laxika.magicalvibes.model.action.ExileCardsFromOwnGraveyardAtNextEndStep;
import com.github.laxika.magicalvibes.model.action.DelayedDestroyAllPermanents;
import com.github.laxika.magicalvibes.model.action.DelayedLoseLifeAndReturnFromGraveyard;
import com.github.laxika.magicalvibes.model.action.DelayedSacrificeTargetPermanentAtEndStep;
import com.github.laxika.magicalvibes.model.action.DelayedSacrificeTargetPermanentAtEndStepIfManaValueAtMost;
import com.github.laxika.magicalvibes.model.action.DelayedSacrificeTargetPermanentAtEndStepIfManaValueAtLeast;
import com.github.laxika.magicalvibes.model.action.DelayedCoinFlipSacrificeTargetPermanentAtEndStep;
import com.github.laxika.magicalvibes.model.action.DelayedUntapPermanents;
import com.github.laxika.magicalvibes.model.action.DamageAtNextUpkeepUnlessPays;
import com.github.laxika.magicalvibes.model.action.DamageForCardsStillExiledAtNextEndStep;
import com.github.laxika.magicalvibes.model.action.DelayedEndStepIfStillInHandTrigger;
import com.github.laxika.magicalvibes.model.action.DelayedStillExiledCardsEndStepTrigger;
import com.github.laxika.magicalvibes.model.action.PoisonAtNextUpkeepUnlessPays;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextUpkeep;
import com.github.laxika.magicalvibes.model.action.RandomDiscardCardsAtNextUpkeep;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextEndStep;
import com.github.laxika.magicalvibes.model.action.UnattachEquipmentAtNextEndStep;
import com.github.laxika.magicalvibes.model.action.SacrificeSelfAtNextEndStepTrigger;
import com.github.laxika.magicalvibes.model.action.SacrificePermanentAtControllerEndStepUnlessPays;
import com.github.laxika.magicalvibes.model.action.EchoAtNextUpkeep;
import com.github.laxika.magicalvibes.model.action.LoseLifeAtNextDrawStepUnlessPays;
import com.github.laxika.magicalvibes.model.action.PayManaOrLoseGameAtNextUpkeep;
import com.github.laxika.magicalvibes.model.action.ExileToOwnerGraveyardAtNextEndStep;
import com.github.laxika.magicalvibes.model.action.ExileToOwnerGraveyardAtNextUpkeep;
import com.github.laxika.magicalvibes.model.action.ExilePermanentAtNextUpkeep;
import com.github.laxika.magicalvibes.model.action.PutCounterOnPermanentAtNextUpkeep;
import com.github.laxika.magicalvibes.model.action.RevokeExilePlayPermissionAtNextUpkeep;
import com.github.laxika.magicalvibes.model.action.GrantExilePlayPermissionAtNextTurn;
import com.github.laxika.magicalvibes.model.action.TransformSourceAtNextUpkeep;
import com.github.laxika.magicalvibes.model.action.GrantChosenLandwalkAtNextUpkeep;
import com.github.laxika.magicalvibes.model.action.ReboundAtNextUpkeep;
import com.github.laxika.magicalvibes.model.action.DimensionalBreachUpkeepReturn;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllMireCountersFromChosenLandEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveTimeCounterFromExiledCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnAuraFromGraveyardAttachedToPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtRandomEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtTriggerTimeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOpponentGainsControlOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCreatureCardsToBattlefieldAndShuffleRestEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.effect.TargetSpec;
import com.github.laxika.magicalvibes.model.effect.TransformToBackFaceEffect;
import com.github.laxika.magicalvibes.model.effect.GrantChosenLandwalkEffect;
import com.github.laxika.magicalvibes.model.effect.UnattachEquipmentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.action.DelayedPlusOneCounters;
import com.github.laxika.magicalvibes.model.action.DelayedPlusZeroPlusOneCounters;
import com.github.laxika.magicalvibes.model.effect.PutCountersForPreventedDamageEffect;
import com.github.laxika.magicalvibes.model.action.PutCounterOnPermanentAtNextEndStep;
import com.github.laxika.magicalvibes.model.action.RemoveCounterFromPermanentAtNextEndStep;
import com.github.laxika.magicalvibes.model.action.RemoveCountersFromPermanentAtNextCleanup;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DestroyChosenCreaturesIfDidNotAttackAtEndStep;
import com.github.laxika.magicalvibes.model.action.DestroyNonAttackersAtEndStep;
import com.github.laxika.magicalvibes.model.action.DestroyPermanentIfDidNotAttackAtEndStep;
import com.github.laxika.magicalvibes.model.action.ExilePermanentAtControllerEndStep;
import com.github.laxika.magicalvibes.model.action.LoseGameAtEndStep;
import com.github.laxika.magicalvibes.model.action.ReturnExiledCardToHandAtEndStep;
import com.github.laxika.magicalvibes.model.action.ReturnExiledCardToHandAtNextEndStep;
import com.github.laxika.magicalvibes.model.effect.ReturnExiledCardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardExiledWithSourceToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.PutExiledCardsIntoOwnersGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetCardFromExileIntoOwnersGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.PutUnplayedExiledCardsIntoOwnersGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTriggeringCardFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceAuraToCreatureOnDeathEffect;
import com.github.laxika.magicalvibes.model.action.EachPlayerHandExileReturnAtNextEndStep;
import com.github.laxika.magicalvibes.model.action.TargetPlayerHandExileReturnAtNextTurnEndStep;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityRequest;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityRequest.TriggerRecord;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService;
import com.github.laxika.magicalvibes.model.Boon;
import com.github.laxika.magicalvibes.model.BoonTrigger;
import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.BattlefieldEntryCard;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.Dungeon;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.OpeningHandRevealTrigger;
import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.model.action.ReturnExiledCardAtNextEndStepUnlessPays;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TargetGroup;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.SagaChapterTargetGroup;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ChapterTrigger;
import com.github.laxika.magicalvibes.model.trigger.StepTrigger;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.paradigm.ParadigmService;
import com.github.laxika.magicalvibes.service.epic.EpicService;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.trigger.TriggerTargetCollector;
import com.github.laxika.magicalvibes.service.target.ValidTargetService;
import com.github.laxika.magicalvibes.model.effect.VentureIntoDungeonEffect;
import com.github.laxika.magicalvibes.model.effect.TemporaryCopyEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.MaySkipDrawReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinWinEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.ResolveRadCountersEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.GivePoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PoisonRecipient;
import com.github.laxika.magicalvibes.model.effect.PayEchoCost;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.ControllerLosesGameEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSpecificPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.effect.ReplaceSingleDrawEffect;
import com.github.laxika.magicalvibes.model.effect.SkipDrawStepEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersSkipUpkeepStepEffect;
import com.github.laxika.magicalvibes.model.effect.SkipStepOrPhaseKind;
import com.github.laxika.magicalvibes.model.DrawReplacementKind;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.GrantedTriggeredAbilitySupport;
import com.github.laxika.magicalvibes.service.effect.GrantedUpkeepEffectSupport;
import com.github.laxika.magicalvibes.model.effect.ReturnDelayedCurseAttachedToPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnDelayedGraveyardCardUnderControlEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnGraveyardCardsToBattlefieldUnderControlEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.DrawUpToNCardsEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawDuringEachOpponentsDrawStepEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.service.effect.EffectResolutionService;
import com.github.laxika.magicalvibes.model.effect.DiscardSpecificCardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReboundCastFromExileEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCreatedPermanentsAtEndStepUnlessConditionEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveDelayCounterFromExiledSpellEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveSuspendCounterFromExiledSpellEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveEggCounterFromExileAndReturnEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveRefineCounterFromExiledCardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEachPlayerHandAndReturnExiledCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnExiledCardsToTargetPlayerHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnExiledCardToBattlefieldUnderOwnerControlEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesGameEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryBatchSupport;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.battlefield.GraveyardTransformedReturnService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.ETBTokenTargetService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.battlefield.SagaChapterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Collects the triggered abilities that trigger at the beginning of a step or phase (CR 603.2b): upkeep, draw step,
 * the main phases, beginning and end of combat, end step and cleanup. For each step it reads the abilities whose
 * {@link StepTrigger} is that step's beginning on permanents and in the other zones they function from, judges their
 * event conditions ({@link StepAbilityReader}) and hands each one whole to the stack door
 * ({@code TriggeredAbilityStackService.put}) with the step's record ("that player", the source as it was, an attached
 * permanent, a graveyard stay), then begins the modes and targets the door queued
 * ({@link #beginNextStepTriggerChoice}). It also puts the step's delayed and engine-made abilities on the stack.
 */
@Slf4j
@Service
public class StepTriggerService {
    @org.springframework.beans.factory.annotation.Autowired
    @org.springframework.context.annotation.Lazy
    private com.github.laxika.magicalvibes.service.planar.PlanechaseService planechaseService;
    @Autowired
    @Lazy
    private TriggeredAbilityStackService triggeredAbilityStackService;
    @Autowired
    @Lazy
    private com.github.laxika.magicalvibes.service.trigger.StackedModalTriggerService stackedModalTriggerService;
    @Autowired
    @Lazy
    private com.github.laxika.magicalvibes.service.trigger.StackedDividedDamageTriggerService
            stackedDividedDamageTriggerService;

    private static final StepTrigger YOUR_UPKEEP = Triggers.upkeep(ActivePlayer.YOU);
    private static final StepTrigger EACH_UPKEEP = Triggers.upkeep(ActivePlayer.ANY);
    private static final StepTrigger OPPONENT_UPKEEP = Triggers.upkeep(ActivePlayer.OPPONENT);
    private static final StepTrigger CHOSEN_PLAYER_UPKEEP = Triggers.upkeep(ActivePlayer.CHOSEN_PLAYER);
    private static final StepTrigger ENCHANTED_PLAYER_UPKEEP = Triggers.upkeep(ActivePlayer.ENCHANTED_PLAYER);
    private static final StepTrigger ENCHANTED_CONTROLLER_UPKEEP =
            Triggers.upkeep(ActivePlayer.ENCHANTED_PERMANENT_CONTROLLER);
    private static final StepTrigger YOUR_DRAW_STEP = Triggers.drawStep(ActivePlayer.YOU);
    private static final StepTrigger EACH_DRAW_STEP = Triggers.drawStep(ActivePlayer.ANY);
    private static final StepTrigger OPPONENT_DRAW_STEP = Triggers.drawStep(ActivePlayer.OPPONENT);
    private static final StepTrigger ENCHANTED_PLAYER_DRAW_STEP = Triggers.drawStep(ActivePlayer.ENCHANTED_PLAYER);
    private static final StepTrigger ENCHANTED_CONTROLLER_DRAW_STEP =
            Triggers.drawStep(ActivePlayer.ENCHANTED_PERMANENT_CONTROLLER);
    private static final StepTrigger YOUR_PRECOMBAT_MAIN_PHASE = Triggers.precombatMainPhase(ActivePlayer.YOU);
    private static final StepTrigger EACH_PRECOMBAT_MAIN_PHASE = Triggers.precombatMainPhase(ActivePlayer.ANY);
    private static final StepTrigger YOUR_BEGINNING_OF_COMBAT = Triggers.beginningOfCombat(ActivePlayer.YOU);
    private static final StepTrigger EACH_BEGINNING_OF_COMBAT = Triggers.beginningOfCombat(ActivePlayer.ANY);
    private static final StepTrigger OPPONENT_BEGINNING_OF_COMBAT = Triggers.beginningOfCombat(ActivePlayer.OPPONENT);
    private static final StepTrigger END_OF_COMBAT = Triggers.endOfCombat();
    private static final StepTrigger YOUR_POSTCOMBAT_MAIN_PHASE = Triggers.postcombatMainPhase(ActivePlayer.YOU);
    private static final StepTrigger YOUR_SECOND_MAIN_PHASE = Triggers.secondMainPhase(ActivePlayer.YOU);
    private static final StepTrigger EACH_END_STEP = Triggers.endStep(ActivePlayer.ANY);
    private static final StepTrigger YOUR_END_STEP = Triggers.endStep(ActivePlayer.YOU);
    private static final StepTrigger OPPONENT_END_STEP = Triggers.endStep(ActivePlayer.OPPONENT);
    private static final StepTrigger ENCHANTED_PLAYER_END_STEP = Triggers.endStep(ActivePlayer.ENCHANTED_PLAYER);
    private static final StepTrigger ENCHANTED_CONTROLLER_END_STEP =
            Triggers.endStep(ActivePlayer.ENCHANTED_PERMANENT_CONTROLLER);

    private final DrawService drawService;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final ConditionEvaluationService conditionEvaluationService;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final PermanentRemovalService permanentRemovalService;
    private final AuraAttachmentService auraAttachmentService;
    @Autowired
    @Lazy
    private BattlefieldEntryBatchSupport battlefieldEntryBatchSupport;
    private final LifeSupport lifeSupport;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GraveyardTransformedReturnService graveyardTransformedReturnService;
    private final GraveyardService graveyardService;
    private final TriggerCollectionService triggerCollectionService;
    private final TriggerTargetCollector triggerTargetCollector;
    private final ParadigmService paradigmService;
    private final EpicService epicService;
    private final ValidTargetService validTargetService;
    private final CreatureControlService creatureControlService;
    private final GrantedTriggeredAbilitySupport grantedTriggeredAbilitySupport;
    private final GrantedUpkeepEffectSupport grantedUpkeepEffectSupport;
    private final ETBTokenTargetService etbTokenTargetService;
    private final AmountEvaluationService amountEvaluationService;
    private final SagaChapterService sagaChapterService;
    private final EffectResolutionService effectResolutionService;
    private final StepAbilityReader stepAbilityReader;

    public StepTriggerService(DrawService drawService,
                              GameQueryService gameQueryService,
                              PredicateEvaluationService predicateEvaluationService,
                              ConditionEvaluationService conditionEvaluationService,
                              GameLogService gameLogService,
                              PlayerInputService playerInputService,
                              PermanentRemovalService permanentRemovalService,
                              AuraAttachmentService auraAttachmentService,
                              LifeSupport lifeSupport,
                              BattlefieldEntryService battlefieldEntryService,
                              GraveyardTransformedReturnService graveyardTransformedReturnService,
                              GraveyardService graveyardService,
                              TriggerCollectionService triggerCollectionService,
                              TriggerTargetCollector triggerTargetCollector,
                              @Lazy ParadigmService paradigmService,
                              EpicService epicService,
                              ValidTargetService validTargetService,
                              CreatureControlService creatureControlService,
                              GrantedTriggeredAbilitySupport grantedTriggeredAbilitySupport,
                              GrantedUpkeepEffectSupport grantedUpkeepEffectSupport,
                              @Lazy ETBTokenTargetService etbTokenTargetService,
                              AmountEvaluationService amountEvaluationService,
                              @Lazy SagaChapterService sagaChapterService,
                              @Lazy EffectResolutionService effectResolutionService) {
        this.drawService = drawService;
        this.gameQueryService = gameQueryService;
        this.predicateEvaluationService = predicateEvaluationService;
        this.conditionEvaluationService = conditionEvaluationService;
        this.gameLogService = gameLogService;
        this.playerInputService = playerInputService;
        this.permanentRemovalService = permanentRemovalService;
        this.auraAttachmentService = auraAttachmentService;
        this.lifeSupport = lifeSupport;
        this.battlefieldEntryService = battlefieldEntryService;
        this.graveyardTransformedReturnService = graveyardTransformedReturnService;
        this.graveyardService = graveyardService;
        this.triggerCollectionService = triggerCollectionService;
        this.triggerTargetCollector = triggerTargetCollector;
        this.paradigmService = paradigmService;
        this.epicService = epicService;
        this.validTargetService = validTargetService;
        this.creatureControlService = creatureControlService;
        this.grantedTriggeredAbilitySupport = grantedTriggeredAbilitySupport;
        this.grantedUpkeepEffectSupport = grantedUpkeepEffectSupport;
        this.etbTokenTargetService = etbTokenTargetService;
        this.amountEvaluationService = amountEvaluationService;
        this.sagaChapterService = sagaChapterService;
        this.effectResolutionService = effectResolutionService;
        this.stepAbilityReader = new StepAbilityReader(conditionEvaluationService);
    }

    /**
     * {@code card}'s printed abilities with {@code trigger} functioning in {@code zone} whose trigger event this step's
     * beginning is, their event conditions judged in {@code context} ({@link StepAbilityReader}).
     */
    private List<TriggeredAbility> printedAbilities(GameData gameData, Card card, StepTrigger trigger, Zone zone,
                                                    ConditionContext context) {
        return stepAbilityReader.triggering(gameData, card.getTriggeredAbilities(trigger, zone), context);
    }

    /**
     * The abilities with {@code trigger} granted to {@code perm} for a duration that trigger now: until end of turn,
     * end of combat or next upkeep, then until it leaves, their event conditions judged in {@code context}.
     */
    private List<TriggeredAbility> durationGrantedAbilities(GameData gameData, Permanent perm, StepTrigger trigger,
                                                            ConditionContext context) {
        return stepAbilityReader.triggering(gameData, StepAbilityReader.granted(perm, trigger), context);
    }

    /**
     * The abilities with {@code trigger} that other permanents' static abilities grant {@code perm} that trigger now,
     * their event conditions judged in {@code context}.
     */
    private List<TriggeredAbility> staticGrantedAbilities(GameData gameData, Permanent perm, StepTrigger trigger,
                                                          ConditionContext context) {
        return stepAbilityReader.triggering(gameData, grantedTriggeredAbilitySupport.grantedTriggeredAbilities(
                gameData, perm, null, ability -> ability.hasTrigger(trigger)), context);
    }

    /** The abilities with {@code trigger} functioning in {@code zone} perpetually granted to {@code card}. */
    private static List<TriggeredAbility> perpetualAbilities(GameData gameData, Card card, StepTrigger trigger,
                                                            Zone zone) {
        List<TriggeredAbility> abilities = new ArrayList<>();
        for (com.github.laxika.magicalvibes.model.Ability ability : gameData.perpetualAbilities(card.getId())) {
            if (ability instanceof TriggeredAbility triggered && triggered.hasTrigger(trigger, zone)) {
                abilities.add(triggered);
            }
        }
        return abilities;
    }

    /**
     * The abilities with {@code trigger} of {@code card} in a graveyard, its own and its perpetual grants that function
     * there, that trigger now, their event conditions judged in {@code context}.
     */
    private List<TriggeredAbility> graveyardAbilities(GameData gameData, Card card, StepTrigger trigger,
                                                      ConditionContext context) {
        List<TriggeredAbility> abilities = gameQueryService.getEffectiveGraveyardTriggeredAbilities(gameData, card)
                .stream().filter(ability -> ability.hasTrigger(trigger, Zone.GRAVEYARD)).toList();
        return stepAbilityReader.triggering(gameData, abilities, context);
    }

    /**
     * Hands {@code ability}, an ability of {@code card} whose trigger event this step or phase's beginning is, to the
     * stack door under {@code controllerId}, its source the permanent {@code source} ({@code null} for a card in another
     * zone): the door judges its intervening "if" in {@code context} (CR 603.4) and its limit, puts it on the stack as
     * one object of its effects as written (CR 603.3) with {@code record}, and chooses its mode and targets as it goes
     * there (CR 603.3c, 603.3d). Logs that it triggered, as the ability called {@code kind}.
     *
     * @return whether it triggered
     */
    private boolean putStepAbility(GameData gameData, TriggeredAbility ability, Card card, Permanent source,
                                   UUID controllerId, String kind, ConditionContext context, TriggerRecord record) {
        boolean triggered = triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(ability, card,
                source, controllerId, card.getName() + "'s " + kind, context, record));
        if (triggered) {
            gameLogService.append(gameData, GameLog.cardThen(card, "'s " + kind + " triggers."));
        }
        return triggered;
    }

    /**
     * The record of an ability that names the player whose step it is: "that player", the entry's active and
     * triggering player and its non-targeting target (CR 115.10a), which the effects read as the ability resolves,
     * whatever their class; with {@code source}, a permanent, as it was.
     */
    private static TriggerRecord stepPlayerRecord(UUID activePlayerId, Permanent source) {
        return entry -> {
            recordStepPlayer(entry, activePlayerId);
            if (source != null) {
                entry.setSourcePermanentSnapshot(new Permanent(source));
            }
        };
    }

    /**
     * The record of an ability of a card in a graveyard: this graveyard stay of its source (CR 400.7), which the
     * source-in-graveyard check reads, so the ability can't act after the card has left and come back.
     */
    private static TriggerRecord graveyardStayRecord(GameData gameData, Card card) {
        long version = gameData.graveyardEntryVersion(card.getId());
        return entry -> entry.setSourceGraveyardEntryVersion(version);
    }

    /**
     * Scans battlefields and graveyards for upkeep-triggered
     * abilities and pushes them onto the stack or queues may-ability prompts.
     *
     * <p>Handles every {@code Triggers.upkeep(…)} ability: "your upkeep" from the battlefield, the command zone,
     * the graveyard and exile, "each upkeep" from the battlefield and exile (suspended cards), "each opponent's
     * upkeep", and the enchanted player's and the enchanted permanent's controller's upkeep.
     *
     * @param gameData the current game state to modify
     */
    /**
     * The emblems' "at the beginning of [step or phase]" abilities for {@code moment} whose trigger names
     * {@code player} ("your upkeep", "each opponent's upkeep", "each end step"; Chandra, Roaring Flame's, Sarkhan, the
     * Dragonspeaker's, Domri, Chaos Bringer's emblems), and "at end of combat on your turn, if it's the first combat
     * phase" (Zariel, Archduke of Avernus's). Abilities of emblems function in the command zone (CR 114.4), so the
     * battlefield sweeps never see them, and an emblem is never removed. {@code player} is judged against the emblem's
     * controller; the emblem's source card is the ability's source as its event condition is judged and as the stack
     * door puts it on the stack, recording the player whose step it is. A step trigger naming another player relation
     * fails loudly instead of triggering wrongly.
     */
    private void collectEmblemStepTriggers(GameData gameData, StepTrigger.Moment moment, ActivePlayer player) {
        for (Emblem emblem : List.copyOf(gameData.emblems)) {
            boolean related = switch (player) {
                case YOU -> gameData.activePlayerId.equals(emblem.controllerId());
                case OPPONENT -> !gameData.activePlayerId.equals(emblem.controllerId());
                case ANY -> true;
                default -> throw new IllegalArgumentException("No emblem step reader reads " + player);
            };
            if (!related) {
                continue;
            }
            List<TriggeredAbility> abilities = emblem.triggeredAbilities().stream()
                    .filter(ability -> emblemStepAbility(emblem, ability, moment, player)).toList();
            Card source = emblem.sourceCard();
            ConditionContext context = ConditionContext.forCard(source, emblem.controllerId());
            for (TriggeredAbility ability : stepAbilityReader.triggering(gameData, abilities, context)) {
                boolean triggered = triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(ability,
                        source, null, emblem.controllerId(), source.getName() + "'s emblem", context,
                        stepPlayerRecord(gameData.activePlayerId, null)));
                if (triggered) {
                    gameLogService.append(gameData, GameLog.text(source.getName() + "'s emblem triggers."));
                }
            }
        }
    }

    /**
     * Whether {@code ability} of {@code emblem} is an "at the beginning of {@code moment}" ability for {@code player}.
     * An emblem's step ability that names a player relation no emblem step reader reads fails loudly.
     */
    private static boolean emblemStepAbility(Emblem emblem, TriggeredAbility ability, StepTrigger.Moment moment,
                                             ActivePlayer player) {
        boolean matches = false;
        for (TriggerCondition condition : ability.conditions()) {
            if (!(condition.trigger() instanceof StepTrigger step) || step.moment() != moment) {
                continue;
            }
            if (step.player() != ActivePlayer.YOU && step.player() != ActivePlayer.OPPONENT
                    && step.player() != ActivePlayer.ANY) {
                throw new IllegalStateException(emblem.sourceCard().getName() + "'s emblem has a step ability its "
                        + "reader doesn't judge: " + ability);
            }
            matches |= step.player() == player;
        }
        return matches;
    }

    /**
     * Records the step's event on {@code entry}, an ability that chooses no target: the player whose step it is, as
     * the entry's active and triggering player and as its non-targeting target, "that player" (CR 115.10a). The
     * effects read it as the ability resolves, whatever their class.
     */
    private static StackEntry recordStepPlayer(StackEntry entry, UUID activePlayerId) {
        entry.setActivePlayerId(activePlayerId);
        entry.setTriggeringPlayerId(activePlayerId);
        entry.setTargetId(activePlayerId);
        entry.setNonTargeting(true);
        return entry;
    }

    /**
     * Records on {@code entry} the permanent {@code source}, an Aura or Equipment, is attached to, whatever the entry's
     * effects: "enchanted creature" and "enchanted artifact's controller" are read as the ability resolves, and the
     * permanent's last-known information once it has left (CR 608.2h).
     */
    private void recordAttachedPermanent(GameData gameData, Permanent source, StackEntry entry) {
        Permanent attached = source.isAttached()
                ? gameQueryService.findPermanentById(gameData, source.getAttachedTo()) : null;
        if (attached != null) {
            entry.setAttachedPermanentSnapshot(new Permanent(attached));
        }
    }

    /**
     * Whether {@code ability}, an "at the beginning of your upkeep" ability of {@code perm}, acts on the controller of
     * the permanent {@code perm} enchants: "At the beginning of your upkeep, enchanted permanent's controller
     * sacrifices …" (Strangling Grasp). The Aura binds the effect to its enchant target, and the player the effect
     * names is that permanent's controller rather than a target.
     */
    private boolean affectsEnchantedPermanentController(GameData gameData, Permanent perm, TriggeredAbility ability) {
        return perm.isAttached() && !gameData.playerIds.contains(perm.getAttachedTo())
                && perm.getCard().isAura()
                && ability.effects().stream().map(EffectRegistration::effect)
                .anyMatch(effect -> perm.getCard().isEffectBoundToTargetGroup(effect, 0)
                        && effect.targetSpec().admits(TargetPredicate.Kind.PLAYER));
    }

    /** Whether {@code perm} is a Curse, an Aura attached to a player. */
    private static boolean enchantsPlayer(GameData gameData, Permanent perm) {
        return perm.isAttached() && gameData.playerIds.contains(perm.getAttachedTo());
    }

    public void handleUpkeepTriggers(GameData gameData) {
        if (gameData.planechase != null) planechaseService.step(gameData,
                YOUR_UPKEEP, EACH_UPKEEP);
        // "… until your next upkeep" (Cycle of Life): the floating layer-7b effect ends as the
        // upkeep begins, before the delayed trigger below puts its counter on the creature.
        gameData.expireFloatingEffectsAtUpkeep(gameData.activePlayerId);
        gameData.forEachPermanent((playerId, permanent) -> permanent.markUpkeepStarted(gameData.activePlayerId));
        gameData.forEachPermanent((playerId, permanent) ->
                permanent.clearUntilNextUpkeepTriggeredEffects(gameData.activePlayerId));
        gameData.phasedOutPermanents.values().forEach(permanents -> permanents.forEach(permanent ->
                permanent.clearUntilNextUpkeepTriggeredEffects(gameData.activePlayerId)));

        if (gameData.activePlayerId != null
                && gameData.activePlayerId.equals(gameData.initiativePlayerId)) {
            // "At the beginning of the upkeep of the player who has the initiative, that player ventures into
            // Undercity" (CR 727.2).
            triggeredAbilityStackService.putInherent(gameData, gameData.activePlayerId, "The initiative's ability",
                    List.of(new VentureIntoDungeonEffect(Dungeon.UNDERCITY)), TriggerRecord.NONE);
        }

        // Spatial Binding: "Until your next upkeep, target permanent can't phase out." Phasing is a
        // turn-based action of the untap step (CR 502.1), which has already passed, so clearing here
        // still protected the permanent through the marking player's own untap step.
        expireCantPhaseOut(gameData);

        collectEmblemStepTriggers(gameData, StepTrigger.Moment.UPKEEP, ActivePlayer.YOU);
        collectEmblemStepTriggers(gameData, StepTrigger.Moment.UPKEEP, ActivePlayer.OPPONENT);
        epicService.fireUpkeepTriggers(gameData);
        permanentRemovalService.processDelayedPermanentActions(gameData,
                DelayedPermanentActionKind.SACRIFICE_AT_NEXT_UPKEEP);

        if (gameData.hasDelayedAction(GrantExilePlayPermissionAtNextTurn.class)) {
            List<GrantExilePlayPermissionAtNextTurn> pendingPermissions = gameData.drainDelayedActions(
                    GrantExilePlayPermissionAtNextTurn.class,
                    action -> action.exiledTurnNumber() < gameData.turnNumber);
            for (GrantExilePlayPermissionAtNextTurn action : pendingPermissions) {
                if (gameData.findExiledCard(action.cardId()) != null) {
                    gameData.exilePlayPermissions.put(action.cardId(), action.ownerId());
                }
            }
        }

        // Cycle of Life: "At the beginning of your next upkeep, put a +1/+1 counter on that
        // creature." A delayed triggered ability — it uses the stack but doesn't target, so the
        // remembered permanent is carried as a non-targeting pointer.
        if (gameData.hasDelayedAction(PutCounterOnPermanentAtNextUpkeep.class)) {
            List<PutCounterOnPermanentAtNextUpkeep> pendingCounters = gameData.drainDelayedActions(
                    PutCounterOnPermanentAtNextUpkeep.class, a -> a.controllerId().equals(gameData.activePlayerId));
            for (PutCounterOnPermanentAtNextUpkeep action : pendingCounters) {
                if (gameQueryService.findPermanentById(gameData, action.permanentId()) == null) {
                    continue; // the creature is gone — nothing to put counters on
                }
                StackEntry entry = TriggeredAbilityStackService.madeAbility(action.sourceCard(), action.controllerId(),
                        action.sourceCard().getName() + "'s delayed ability",
                        new ArrayList<>(List.of(new PutCounterOnTargetPermanentEffect(
                                action.counterType(), action.amount()))),
                        action.permanentId(), (UUID) null);
                entry.setNonTargeting(true);
                triggeredAbilityStackService.putDelayed(gameData, entry);
                gameLogService.append(gameData, GameLog.cardThen(action.sourceCard(),
                        "'s delayed ability triggers."));
                log.info("Game {} - {} delayed upkeep counter trigger pushed onto stack",
                        gameData.id, action.sourceCard().getName());
            }
        }

        if (gameData.hasDelayedAction(CyclopeanTombUpkeepCleanup.class)) {
            List<CyclopeanTombUpkeepCleanup> pendingCleanups = gameData.drainDelayedActions(
                    CyclopeanTombUpkeepCleanup.class,
                    action -> action.controllerId().equals(gameData.activePlayerId));
            for (CyclopeanTombUpkeepCleanup action : pendingCleanups) {
                Set<UUID> remainingLandIds = new java.util.HashSet<>(action.trackedLandIds());
                remainingLandIds.removeAll(action.removedLandIds());
                if (remainingLandIds.isEmpty()) {
                    continue;
                }

                PermanentPredicate trackedLandFilter = new PermanentAllOfPredicate(List.of(
                        new PermanentIsLandPredicate(),
                        new PermanentAnyOfPredicate(remainingLandIds.stream()
                                .<PermanentPredicate>map(PermanentIsSpecificPermanentPredicate::new)
                                .toList())));
                StackEntry entry = TriggeredAbilityStackService.madeAbility(
                        action.sourceCard(),
                        action.controllerId(),
                        action.sourceCard().getName() + "'s delayed ability",
                        new ArrayList<>(List.of(new RemoveAllMireCountersFromChosenLandEffect(
                                action.actionId(), trackedLandFilter))));
                entry.setNonTargeting(true);
                triggeredAbilityStackService.putDelayed(gameData, entry);
                gameData.queueDelayedAction(action);
                gameLogService.append(gameData, GameLog.cardThen(action.sourceCard(),
                        "'s delayed ability triggers."));
                log.info("Game {} - {} delayed mire-counter cleanup trigger pushed onto stack",
                        gameData.id, action.sourceCard().getName());
            }
        }

        if (gameData.hasDelayedAction(ExilePermanentAtNextUpkeep.class)) {
            List<ExilePermanentAtNextUpkeep> pendingExiles = gameData.drainDelayedActions(
                    ExilePermanentAtNextUpkeep.class, a -> a.controllerId().equals(gameData.activePlayerId));
            // Creatures scheduled by one resolution share a single delayed trigger that exiles them all
            // ("exile those creatures at the beginning of the next upkeep"); another resolution of the same
            // source created another (CR 603.7a).
            Map<List<Object>, List<ExilePermanentAtNextUpkeep>> batches = new LinkedHashMap<>();
            for (ExilePermanentAtNextUpkeep action : pendingExiles) {
                if (gameQueryService.findPermanentById(gameData, action.permanentId()) == null) {
                    continue;
                }
                batches.computeIfAbsent(List.of(action.sourceCard().getId(), action.controllerId(),
                                action.schedulingResolution()),
                        ignored -> new ArrayList<>()).add(action);
            }
            for (List<ExilePermanentAtNextUpkeep> batch : batches.values()) {
                ExilePermanentAtNextUpkeep first = batch.getFirst();
                List<UUID> permanentIds = batch.stream().map(ExilePermanentAtNextUpkeep::permanentId).toList();
                StackEntry entry = TriggeredAbilityStackService.madeAbility(first.sourceCard(), first.controllerId(),
                        first.sourceCard().getName() + "'s delayed ability",
                        new ArrayList<>(List.of(new ExileTargetPermanentEffect())),
                        (UUID) null, permanentIds);
                entry.setNonTargeting(true);
                triggeredAbilityStackService.putDelayed(gameData, entry);
                gameLogService.append(gameData, GameLog.cardThen(first.sourceCard(),
                        "'s delayed ability triggers."));
                log.info("Game {} - {} delayed upkeep exile trigger pushed onto stack",
                        gameData.id, first.sourceCard().getName());
            }
        }

        if (gameData.hasDelayedAction(DelayedCreateTokenAtNextUpkeep.class)) {
            List<DelayedCreateTokenAtNextUpkeep> pendingTokens = gameData.drainDelayedActions(
                    DelayedCreateTokenAtNextUpkeep.class,
                    action -> action.anyPlayerNextUpkeep()
                            || action.controllerId().equals(gameData.activePlayerId));
            for (DelayedCreateTokenAtNextUpkeep action : pendingTokens) {
                StackEntry entry = TriggeredAbilityStackService.madeAbility(action.sourceCard(), action.controllerId(),
                        action.sourceCard().getName() + "'s delayed ability",
                        new ArrayList<>(List.of(action.tokenEffect())));
                entry.setNonTargeting(true);
                entry.setEventValue(action.eventValue());
                triggeredAbilityStackService.putDelayed(gameData, entry);
                gameLogService.append(gameData, GameLog.cardThen(action.sourceCard(),
                        "'s delayed ability triggers."));
                log.info("Game {} - {} delayed upkeep token creation trigger pushed onto stack",
                        gameData.id, action.sourceCard().getName());
            }
        }

        // Phytotitan: "return it to the battlefield tapped under its owner's control at the beginning
        // of their next upkeep" — only at the owner's own upkeep.
        resolveDelayedSelfReturns(gameData,
                pending -> pending.atNextUpkeep() && pending.upkeepPlayerId().equals(gameData.activePlayerId));
        resolveDelayedGraveyardCardsUnderControlAtUpkeep(gameData);

        if (gameData.hasDelayedAction(DimensionalBreachUpkeepReturn.class)) {
            List<DimensionalBreachUpkeepReturn> pendingReturns = gameData.drainDelayedActions(
                    DimensionalBreachUpkeepReturn.class);
            for (DimensionalBreachUpkeepReturn action : pendingReturns) {
                UUID sourceCardId = action.sourceCard().getId();
                boolean hasCards = gameData.getCardsExiledByPermanent(sourceCardId).stream()
                        .anyMatch(card -> !card.isToken());
                if (!hasCards) {
                    continue;
                }

                StackEntry entry = TriggeredAbilityStackService.madeAbility(action.sourceCard(), action.controllerId(),
                        action.sourceCard().getName() + "'s delayed ability",
                        new ArrayList<>(List.of(ReturnCardExiledWithSourceToBattlefieldEffect.ownedByActivePlayer())),
                        0, sourceCardId);
                entry.setNonTargeting(true);
                triggeredAbilityStackService.putDelayed(gameData, entry);
                gameData.queueDelayedAction(action);
                gameLogService.append(gameData, GameLog.cardThen(action.sourceCard(),
                        "'s delayed ability triggers."));
                log.info("Game {} - {} delayed upkeep return trigger pushed onto the stack",
                        gameData.id, action.sourceCard().getName());
            }
        }

        // Delayed "draw N cards at the beginning of the next turn's upkeep" (e.g. Library of Lat-Nam).
        // Drained regardless of who the active player is — the scheduling player draws.
        if (gameData.hasDelayedAction(DrawCardsAtNextUpkeep.class)) {
            List<DrawCardsAtNextUpkeep> pendingDraws = gameData.drainDelayedActions(DrawCardsAtNextUpkeep.class);
            List<DrawCardsAtNextUpkeep> notYetDue = pendingDraws.stream()
                    .filter(pending -> !pending.isDue(gameData))
                    .toList();
            notYetDue.forEach(gameData::queueDelayedAction);
            pendingDraws = pendingDraws.stream().filter(pending -> pending.isDue(gameData)).toList();
            for (DrawCardsAtNextUpkeep pending : pendingDraws) {
                String playerName = gameData.playerIdToName.get(pending.controllerId());
                if (pending.upTo()) {
                    // Arcane Denial: "may draw up to two cards" — a delayed triggered ability that uses
                    // the stack, with its original controller and a separate drawing recipient.
                    StackEntry entry = TriggeredAbilityStackService.madeAbility(pending.sourceCard(), pending.triggerControllerId(),
                            pending.sourceCard().getName() + "'s delayed ability",
                            new ArrayList<>(List.of(new DrawUpToNCardsEffect(pending.count(),
                                    com.github.laxika.magicalvibes.model.effect.DrawUpToRecipient.TARGET_PLAYER))),
                            pending.controllerId(), (UUID) null);
                    entry.setNonTargeting(true);
                    triggeredAbilityStackService.putDelayed(gameData, entry);
                    gameLogService.append(gameData, GameLog.cardThen(pending.sourceCard(),
                            "'s delayed ability triggers."));
                    log.info("Game {} - {} may draw up to {} from delayed upkeep trigger ({})",
                            gameData.id, playerName, pending.count(), pending.sourceCard().getName());
                    continue;
                }
                boolean drawerIsNotTriggerController = !pending.controllerId().equals(pending.triggerControllerId());
                StackEntry entry = TriggeredAbilityStackService.madeAbility(pending.sourceCard(), pending.triggerControllerId(),
                        pending.sourceCard().getName() + "'s delayed ability",
                        new ArrayList<>(List.of(drawerIsNotTriggerController
                                ? new DrawCardForTargetPlayerEffect(pending.count())
                                : new DrawCardEffect(pending.count()))),
                        drawerIsNotTriggerController ? pending.controllerId() : (UUID) null, (UUID) null);
                entry.setNonTargeting(true);
                triggeredAbilityStackService.putDelayed(gameData, entry);
                gameLogService.append(gameData, GameLog.cardThen(pending.sourceCard(),
                        "'s delayed ability triggers."));
                log.info("Game {} - {} delayed upkeep draw trigger pushed onto stack for {} card(s) ({})",
                        gameData.id, playerName, pending.count(), pending.sourceCard().getName());
            }
        }

        if (gameData.hasDelayedAction(RandomDiscardCardsAtNextUpkeep.class)) {
            List<RandomDiscardCardsAtNextUpkeep> pendingDiscards = gameData.drainDelayedActions(
                    RandomDiscardCardsAtNextUpkeep.class,
                    action -> action.controllerId().equals(gameData.activePlayerId));
            for (RandomDiscardCardsAtNextUpkeep pending : pendingDiscards) {
                StackEntry entry = TriggeredAbilityStackService.madeAbility(
                        pending.sourceCard(),
                        pending.controllerId(),
                        pending.sourceCard().getName() + "'s delayed trigger — discard " + pending.count() + " cards",
                        new ArrayList<>(List.of(new DiscardEffect(
                                pending.count(), DiscardRecipient.CONTROLLER, true))));
                entry.setNonTargeting(true);
                triggeredAbilityStackService.putDelayed(gameData, entry);
                gameLogService.append(gameData,
                        GameLog.cardThen(pending.sourceCard(), "'s delayed trigger discards cards at random."));
                log.info("Game {} - {} delayed random discard trigger pushed onto stack",
                        gameData.id, pending.sourceCard().getName());
            }
        }

        if (gameData.hasDelayedAction(EchoAtNextUpkeep.class)) {
            List<EchoAtNextUpkeep> pendingEchoes = gameData.drainDelayedActions(EchoAtNextUpkeep.class);
            for (EchoAtNextUpkeep action : pendingEchoes) {
                Permanent echoSource = gameQueryService.findPermanentById(gameData, action.permanentId());
                if (echoSource == null || gameQueryService.hasLostAllAbilities(gameData, echoSource)) continue;
                UUID controllerId = gameQueryService.findPermanentController(gameData, action.permanentId());
                if (controllerId == null) {
                    continue;
                }
                if (!controllerId.equals(gameData.activePlayerId)) {
                    gameData.queueDelayedAction(action);
                    continue;
                }

                ForcedCostOrElseEffect payEchoOrSacrifice = new ForcedCostOrElseEffect(
                        new PayEchoCost(action.manaCost(), action.handCardCost(), action.cost(),
                                action.dynamicManaCost()),
                        new ArrayList<>(List.of(new SacrificeSelfEffect())),
                        true, action.paidEffects());
                // Echo is the permanent's own triggered ability (CR 702.30a): the effects that make its abilities
                // trigger an additional time count it (CR 603.3).
                triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(
                        new TriggeredAbility(YOUR_UPKEEP, payEchoOrSacrifice), action.sourceCard(), echoSource,
                        controllerId, action.sourceCard().getName() + "'s echo ability",
                        ConditionContext.forPermanent(echoSource, controllerId), TriggerRecord.NONE));
                gameLogService.append(gameData, GameLog.cardThen(action.sourceCard(), "'s echo ability triggers."));
                log.info("Game {} - {} echo trigger pushed onto stack", gameData.id, action.sourceCard().getName());
            }
        }

        // Quenchable Fire: "It deals an additional N damage to that player or planeswalker at the
        // beginning of your next upkeep step unless that player or that planeswalker's controller pays
        // {cost} before that step." Paying is a special action (CR 116.2c,
        // AbilityActivationService.payDelayedTriggerCost) that removes the obligation; one still
        // queued here was not paid, and once upkeep has begun it can't be. The delayed trigger is
        // controlled by the spell's controller and fizzles if the target is gone.
        if (gameData.hasDelayedAction(DamageAtNextUpkeepUnlessPays.class)) {
            List<DamageAtNextUpkeepUnlessPays> pending = gameData.drainDelayedActions(
                    DamageAtNextUpkeepUnlessPays.class, a -> a.spellControllerId().equals(gameData.activePlayerId));
            for (DamageAtNextUpkeepUnlessPays action : pending) {
                if (!gameData.playerIds.contains(action.targetId())
                        && gameQueryService.findPermanentById(gameData, action.targetId()) == null) {
                    continue; // targeted planeswalker is gone — trigger fizzles
                }

                triggeredAbilityStackService.putDelayed(gameData, TriggeredAbilityStackService.madeAbility(action.sourceCard(), action.spellControllerId(),
                        action.sourceCard().getName() + "'s delayed ability",
                        new ArrayList<>(List.of(new DealDamageToTargetPlayerOrPlaneswalkerEffect(action.damage()))),
                        action.targetId(), (UUID) null));

                gameLogService.append(gameData, GameLog.text(action.sourceCard().getName()
                        + "'s delayed ability triggers — " + action.damage() + " damage."));
                log.info("Game {} - {} delayed upkeep damage trigger pushed for {}",
                        gameData.id, action.sourceCard().getName(),
                        gameData.playerIdToName.get(action.spellControllerId()));
            }
        }

        // Sabertooth Cobra: "The player gets another poison counter at the beginning of their next
        // upkeep unless they pay {2} before that step." Delayed trigger keyed to the damaged player's
        // own upkeep. Paying is a special action (CR 116.2c, AbilityActivationService
        // .payDelayedTriggerCost) that removes the obligation, so one still queued here was not paid.
        if (gameData.hasDelayedAction(PoisonAtNextUpkeepUnlessPays.class)) {
            List<PoisonAtNextUpkeepUnlessPays> pendingPoison = gameData.drainDelayedActions(
                    PoisonAtNextUpkeepUnlessPays.class, a -> a.playerId().equals(gameData.activePlayerId));
            for (PoisonAtNextUpkeepUnlessPays action : pendingPoison) {
                triggeredAbilityStackService.putDelayed(gameData, TriggeredAbilityStackService.madeAbility(
                        action.sourceCard(),
                        action.controllerId(),
                        action.sourceCard().getName() + "'s delayed ability",
                        new ArrayList<>(List.of(new GivePoisonCountersEffect(
                                action.amount(), PoisonRecipient.TARGET_PLAYER))),
                        action.playerId(),
                        (UUID) null));

                gameLogService.append(gameData, GameLog.cardThen(action.sourceCard(),
                        "'s delayed ability triggers — " + gameData.playerIdToName.get(action.playerId())
                        + " gets a poison counter."));
                log.info("Game {} - {} delayed upkeep poison trigger pushed for {}",
                        gameData.id, action.sourceCard().getName(), gameData.playerIdToName.get(action.playerId()));
            }
        }

        if (gameData.hasDelayedAction(PayManaOrLoseGameAtNextUpkeep.class)) {
            List<PayManaOrLoseGameAtNextUpkeep> pending = gameData.drainDelayedActions(
                    PayManaOrLoseGameAtNextUpkeep.class, a -> a.playerId().equals(gameData.activePlayerId));
            for (PayManaOrLoseGameAtNextUpkeep action : pending) {
                ForcedCostOrElseEffect payOrLoseGame = new ForcedCostOrElseEffect(
                        new PayManaCost(action.manaCost()),
                        new ArrayList<>(List.of(new ControllerLosesGameEffect())),
                        true);
                triggeredAbilityStackService.putDelayed(gameData, TriggeredAbilityStackService.madeAbility(
                        action.sourceCard(),
                        action.playerId(),
                        action.sourceCard().getName() + "'s delayed ability",
                        new ArrayList<>(List.of(payOrLoseGame))));

                gameLogService.append(gameData, GameLog.cardThen(action.sourceCard(),
                        "'s delayed ability triggers — pay " + action.manaCost() + " or lose the game."));
                log.info("Game {} - {} delayed upkeep pay-or-lose-game trigger pushed for {}",
                        gameData.id, action.sourceCard().getName(), gameData.playerIdToName.get(action.playerId()));
            }
        }

        // Grinning Totem: "At the beginning of your next upkeep, if you haven't played it, put it into
        // its owner's graveyard." Only the scheduling player's own upkeep resolves the cleanup.
        if (gameData.hasDelayedAction(ExileToOwnerGraveyardAtNextUpkeep.class)) {
            List<ExileToOwnerGraveyardAtNextUpkeep> pending = gameData.drainDelayedActions(
                    ExileToOwnerGraveyardAtNextUpkeep.class, a -> a.controllerId().equals(gameData.activePlayerId));
            // Cards exiled by one resolution share a single delayed trigger ("put any of those cards you
            // didn't play into your graveyard"); another resolution of the same source created another
            // (CR 603.7a).
            Map<List<Object>, List<UUID>> unplayedBySource = new LinkedHashMap<>();
            Map<List<Object>, ExileToOwnerGraveyardAtNextUpkeep> firstBySource = new LinkedHashMap<>();
            for (ExileToOwnerGraveyardAtNextUpkeep action : pending) {
                gameData.exilePlayPermissions.remove(action.cardId());
                gameData.exilePlayCostModifiers.remove(action.cardId());
                if (gameData.findExiledCard(action.cardId()) == null) {
                    // The card was played (or otherwise left exile) — nothing to clean up.
                    continue;
                }
                List<Object> key = Arrays.asList(
                        action.sourceCard() != null ? action.sourceCard().getId() : null, action.controllerId(),
                        action.schedulingResolution());
                unplayedBySource.computeIfAbsent(key, ignored -> new ArrayList<>()).add(action.cardId());
                firstBySource.putIfAbsent(key, action);
            }
            for (Map.Entry<List<Object>, List<UUID>> batch : unplayedBySource.entrySet()) {
                ExileToOwnerGraveyardAtNextUpkeep first = firstBySource.get(batch.getKey());
                String sourceName = first.sourceCard() != null ? first.sourceCard().getName() : "an effect";
                StackEntry cleanup = TriggeredAbilityStackService.madeAbility(
                        first.sourceCard() != null ? first.sourceCard()
                                : gameData.findExiledCard(batch.getValue().getFirst()).card(),
                        first.controllerId(),
                        sourceName + "'s delayed ability — put the unplayed cards into their owners' graveyards",
                        new ArrayList<>(List.of(new PutUnplayedExiledCardsIntoOwnersGraveyardEffect())),
                        new ArrayList<>(batch.getValue()));
                cleanup.setNonTargeting(true);
                triggeredAbilityStackService.putDelayed(gameData, cleanup);
                log.info("Game {} - delayed cleanup for unplayed cards exiled with {} pushed onto stack",
                        gameData.id, sourceName);
            }
        }

        // Elkin Bottle: "Until the beginning of your next upkeep, you may play that card." At the
        // scheduling player's next upkeep the permission is revoked; an unplayed card stays in exile.
        if (gameData.hasDelayedAction(RevokeExilePlayPermissionAtNextUpkeep.class)) {
            List<RevokeExilePlayPermissionAtNextUpkeep> pending = gameData.drainDelayedActions(
                    RevokeExilePlayPermissionAtNextUpkeep.class, a -> a.controllerId().equals(gameData.activePlayerId));
            for (RevokeExilePlayPermissionAtNextUpkeep action : pending) {
                if (gameData.exilePlayPermissions.remove(action.cardId()) != null
                        && gameData.findExiledCard(action.cardId()) != null) {
                    gameData.exilePlayCostModifiers.remove(action.cardId());
                    String sourceName = action.sourceCard() != null ? action.sourceCard().getName() : "an effect";
                    gameLogService.append(gameData, GameLog.text(
                            "The card exiled with " + sourceName + " can no longer be played."));
                }
            }
        }

        // Archangel Avacyn: "transform ~ at the beginning of the next upkeep." Fires at the next
        // upkeep regardless of active player; TransformToBackFaceEffect no-ops if already flipped.
        if (gameData.hasDelayedAction(TransformSourceAtNextUpkeep.class)) {
            List<TransformSourceAtNextUpkeep> pending =
                    gameData.drainDelayedActions(TransformSourceAtNextUpkeep.class);
            for (TransformSourceAtNextUpkeep action : pending) {
                triggeredAbilityStackService.putDelayed(gameData, TriggeredAbilityStackService.madeAbility(
                        action.sourceCard(),
                        action.controllerId(),
                        action.sourceCard().getName() + "'s delayed ability — transform",
                        new ArrayList<>(List.of(new TransformToBackFaceEffect())),
                        null,
                        action.permanentId()));
                gameLogService.append(gameData, GameLog.cardThen(action.sourceCard(),
                        "'s delayed ability triggers — transform."));
                log.info("Game {} - {} delayed transform-at-next-upkeep trigger pushed onto stack",
                        gameData.id, action.sourceCard().getName());
            }
        }

        if (gameData.hasDelayedAction(GrantChosenLandwalkAtNextUpkeep.class)) {
            List<GrantChosenLandwalkAtNextUpkeep> pending = gameData.drainDelayedActions(
                    GrantChosenLandwalkAtNextUpkeep.class,
                    action -> action.controllerId().equals(gameData.activePlayerId));
            for (GrantChosenLandwalkAtNextUpkeep action : pending) {
                triggeredAbilityStackService.putDelayed(gameData, TriggeredAbilityStackService.madeAbility(
                        action.sourceCard(),
                        action.controllerId(),
                        action.sourceCard().getName() + "'s delayed ability",
                        new ArrayList<>(List.of(new GrantChosenLandwalkEffect(GrantScope.SELF))),
                        null,
                        action.permanentId()));
                gameData.stack.getLast().setNonTargeting(true);
                gameLogService.append(gameData, GameLog.cardThen(action.sourceCard(),
                        "'s delayed ability triggers."));
                log.info("Game {} - {} delayed landwalk trigger pushed onto stack",
                        gameData.id, action.sourceCard().getName());
            }
        }

        if (gameData.hasDelayedAction(ReboundAtNextUpkeep.class)) {
            List<ReboundAtNextUpkeep> pending = gameData.drainDelayedActions(
                    ReboundAtNextUpkeep.class,
                    action -> action.controllerId().equals(gameData.activePlayerId));
            for (ReboundAtNextUpkeep action : pending) {
                if (gameData.findExiledCard(action.card().getId()) == null) {
                    continue;
                }
                if (action.exileInsteadOfGraveyard()) {
                    gameData.exileInsteadOfGraveyard.add(action.card().getId());
                }
                // Rebound's delayed triggered ability, made by the spell as it resolved (CR 702.88a, 603.7d).
                StackEntry entry = TriggeredAbilityStackService.madeAbility(action.card(), action.controllerId(),
                        action.card().getName() + "'s rebound ability", List.of(new ReboundCastFromExileEffect()));
                entry.setNonTargeting(true);
                triggeredAbilityStackService.putDelayed(gameData, entry);
                gameLogService.append(gameData, GameLog.cardThen(action.card(), "'s rebound ability triggers."));
                log.info("Game {} - {} rebound trigger pushed onto stack", gameData.id, action.card().getName());
            }
        }

        handleOpeningHandUpkeepTriggers(gameData);

        UUID activePlayerId = gameData.activePlayerId;
        gameData.untappedLandsAtTurnStart.computeIfAbsent(activePlayerId, ignored -> {
            List<Permanent> battlefield = gameData.playerBattlefields.get(activePlayerId);
            if (battlefield == null) {
                return 0;
            }
            return (int) battlefield.stream()
                    .filter(permanent -> !permanent.isTapped())
                    .filter(permanent -> gameQueryService.isLand(gameData, permanent))
                    .count();
        });

        // A Soul Echo replacement lasts "until your next upkeep", whether or not Soul Echo is still around, and
        // whether or not its upkeep ability triggers now: the duration ends as the upkeep begins.
        gameData.soulEchoReplacementPlayers.remove(activePlayerId);

        List<Permanent> battlefield = gameData.playerBattlefields.get(activePlayerId);
        if (battlefield == null) return;
        battlefield.forEach(permanent -> permanent.setEchoDamageRedirectionActive(false));

        for (Permanent perm : battlefield) {
            try {
                ConditionContext upkeepContext = ConditionContext.forPermanent(perm, activePlayerId);
                List<TriggeredAbility> upkeepAbilities = new ArrayList<>();
                if (!gameQueryService.hasLostPrintedAbilities(gameData, perm)) {
                    if (!perm.isFaceDown()) {
                        printedAbilities(gameData, perm.getCard(), YOUR_UPKEEP, Zone.BATTLEFIELD, upkeepContext)
                                .stream()
                                .filter(ability -> !affectsEnchantedPermanentController(gameData, perm, ability))
                                .forEach(upkeepAbilities::add);
                    }
                    upkeepAbilities.addAll(durationGrantedAbilities(gameData, perm, YOUR_UPKEEP, upkeepContext));
                }
                // Continuous grants (Breath of Dreams: green creatures have Cumulative upkeep {1}; Necrotic Plague's
                // "enchanted creature has '…'"): the creature is the ability's source and its controller controls it
                // (CR 113.8); an Aura's grant names the Aura and its own target filter.
                upkeepAbilities.addAll(stepAbilityReader.triggering(gameData,
                        grantedUpkeepEffectSupport.grantedUpkeepAbilities(gameData, perm), upkeepContext));
                for (TriggeredAbility ability : upkeepAbilities) {
                    putYourUpkeepAbility(gameData, perm, activePlayerId, ability, upkeepContext);
                }
            } finally {
                // "Since your last upkeep" (Wiitigo) ends as the upkeep begins, not when an ability
                // reading it resolves or only when one triggers: the window closes here for every permanent, after
                // the entries above took their snapshots.
                perm.setBlockedOrWasBlockedSinceLastUpkeep(false);
            }
        }

        List<Card> commandZone = gameData.playerCommandZones.get(activePlayerId);
        if (commandZone != null) {
            for (Card card : new ArrayList<>(commandZone)) {
                ConditionContext commandZoneContext = ConditionContext.forCard(card, activePlayerId);
                List<TriggeredAbility> commandZoneAbilities = new ArrayList<>(printedAbilities(gameData, card,
                        YOUR_UPKEEP, Zone.COMMAND, commandZoneContext));
                // CR 314.4: a face-up scheme's "at the beginning of your upkeep" abilities trigger.
                if (gameData.isFaceUpScheme(card)) {
                    commandZoneAbilities.addAll(printedAbilities(gameData, card, YOUR_UPKEEP, Zone.BATTLEFIELD,
                            commandZoneContext));
                }
                for (TriggeredAbility ability : commandZoneAbilities) {
                    putStepAbility(gameData, ability, card, null, activePlayerId, "command zone upkeep ability",
                            commandZoneContext, entry -> entry.setActivePlayerId(activePlayerId));
                }
            }
        }

        List<Card> graveyard = gameData.playerGraveyards.get(activePlayerId);
        if (graveyard != null) {
            for (Card card : new ArrayList<>(graveyard)) {
                ConditionContext graveyardContext = ConditionContext.forCard(card, activePlayerId);
                for (TriggeredAbility ability : graveyardAbilities(gameData, card, YOUR_UPKEEP, graveyardContext)) {
                    putStepAbility(gameData, ability, card, null, activePlayerId, "upkeep ability", graveyardContext,
                            graveyardStayRecord(gameData, card));
                }
            }
        }

        // Triggers.upkeep(ActivePlayer.ANY) of every battlefield and face-up scheme (CR 314.4). "That player" is the
        // player whose upkeep it is, which the record names: a player the card declares no target for is that player
        // (Pillar Tombs of Aku's "that player may sacrifice a creature"), and an event condition reads it ("at the
        // beginning of that player's upkeep", the ability Takklemaggot gains).
        BiConsumer<UUID, Permanent> eachUpkeep = (playerId, perm) -> {
            if (gameQueryService.hasLostAllAbilities(gameData, perm)) return;
            ConditionContext eachUpkeepContext = ConditionContext.forPermanent(perm, playerId)
                    .withTargetId(activePlayerId);
            List<TriggeredAbility> eachUpkeepAbilities = new ArrayList<>(printedAbilities(gameData, perm.getCard(),
                    EACH_UPKEEP, Zone.BATTLEFIELD, eachUpkeepContext));
            eachUpkeepAbilities.addAll(durationGrantedAbilities(gameData, perm, EACH_UPKEEP, eachUpkeepContext));
            eachUpkeepAbilities.addAll(stepAbilityReader.triggering(gameData,
                    grantedUpkeepEffectSupport.grantedEachUpkeepAbilities(gameData, perm), eachUpkeepContext));
            for (TriggeredAbility ability : eachUpkeepAbilities) {
                putStepAbility(gameData, ability, perm.getCard(), perm, playerId, "upkeep ability", eachUpkeepContext,
                        entry -> {
                            entry.setTargetId(activePlayerId);
                            entry.setNonTargeting(true);
                            entry.setActivePlayerId(activePlayerId);
                            entry.setSourcePermanentSnapshot(new Permanent(perm));
                        });
            }
        };
        gameData.forEachPermanent(eachUpkeep);
        gameData.forEachFaceUpSchemeSource(eachUpkeep);

        // Triggers.upkeep(ActivePlayer.OPPONENT): "at the beginning of each opponent's upkeep", only for an opponent of
        // the permanent's controller; Triggers.upkeep(ActivePlayer.CHOSEN_PLAYER): "at the beginning of the chosen
        // player's upkeep", only for the player chosen as the permanent entered, whoever controls it now (The Rack's
        // ruling). Either way "that player" is the player whose upkeep it is.
        gameData.forEachBattlefield((playerId, playerBattlefield) -> {
            for (Permanent perm : gameData.permanentsAndFaceUpSchemes(playerId)) {
                if (perm.isFaceDown() || gameQueryService.hasLostPrintedAbilities(gameData, perm)) continue;
                ConditionContext upkeepContext = ConditionContext.forPermanent(perm, playerId)
                        .withTargetId(activePlayerId);
                List<TriggeredAbility> opponentUpkeepAbilities = new ArrayList<>();
                if (!playerId.equals(activePlayerId)) {
                    opponentUpkeepAbilities.addAll(printedAbilities(gameData, perm.getCard(), OPPONENT_UPKEEP,
                            Zone.BATTLEFIELD, upkeepContext));
                }
                UUID chosenPlayerId = perm.getChosenPlayerIds().isEmpty()
                        ? perm.getRememberedTargetPlayerId() : perm.getChosenPlayerIds().getFirst();
                if (activePlayerId.equals(chosenPlayerId)) {
                    opponentUpkeepAbilities.addAll(printedAbilities(gameData, perm.getCard(), CHOSEN_PLAYER_UPKEEP,
                            Zone.BATTLEFIELD, upkeepContext));
                }
                for (TriggeredAbility ability : opponentUpkeepAbilities) {
                    putStepAbility(gameData, ability, perm.getCard(), perm, playerId, "upkeep ability", upkeepContext,
                            stepPlayerRecord(activePlayerId, perm));
                }
            }
        });

        // Auras with "at the beginning of the upkeep of enchanted permanent's controller" abilities (Numbing Dose)
        // and, for "At the beginning of your upkeep, enchanted permanent's controller ..." (Strangling Grasp), "your
        // upkeep" abilities that act on that player: the record names the enchanted permanent's controller as "that
        // player", the non-targeting target (CR 115.10a). An ability the Aura grants the creature ("enchanted creature
        // has '…'", Necrotic Plague) is the creature's own, read by the your-upkeep reader with the creature as its
        // source (CR 113.8).
        gameData.forEachPermanent((auraOwnerId, perm) -> {
            ConditionContext auraContext = ConditionContext.forPermanent(perm, auraOwnerId);
            List<TriggeredAbility> affectingUpkeepAbilities = printedAbilities(gameData, perm.getCard(), YOUR_UPKEEP,
                    Zone.BATTLEFIELD, auraContext)
                    .stream()
                    .filter(ability -> affectsEnchantedPermanentController(gameData, perm, ability))
                    .toList();
            boolean auraControllersUpkeep = !affectingUpkeepAbilities.isEmpty();
            List<TriggeredAbility> enchantedControllerUpkeepAbilities = auraControllersUpkeep
                    ? affectingUpkeepAbilities
                    : printedAbilities(gameData, perm.getCard(), ENCHANTED_CONTROLLER_UPKEEP, Zone.BATTLEFIELD,
                            auraContext);
            if (enchantedControllerUpkeepAbilities.isEmpty()) return;
            if (!perm.isAttached()) return;

            UUID enchantedPermanentControllerId = gameQueryService.findPermanentController(gameData, perm.getAttachedTo());
            if (enchantedPermanentControllerId == null) return;
            UUID upkeepOwnerId = auraControllersUpkeep ? auraOwnerId : enchantedPermanentControllerId;
            if (!upkeepOwnerId.equals(activePlayerId)) return;
            Permanent enchantedPermanent = gameQueryService.findPermanentById(gameData, perm.getAttachedTo());
            int enchantedPermanentPowerAtTrigger = enchantedPermanent == null
                    ? 0 : gameQueryService.getEffectivePower(gameData, enchantedPermanent);

            for (TriggeredAbility ability : enchantedControllerUpkeepAbilities) {
                putStepAbility(gameData, ability, perm.getCard(), perm, auraOwnerId, "upkeep ability", auraContext,
                        entry -> {
                            entry.setTargetId(enchantedPermanentControllerId);
                            entry.setNonTargeting(true);
                            entry.setActivePlayerId(activePlayerId);
                            entry.setTriggeringPermanentPowerAtTrigger(enchantedPermanentPowerAtTrigger);
                            entry.setSourcePermanentSnapshot(new Permanent(perm));
                            if (enchantedPermanent != null) {
                                entry.setAttachedPermanentSnapshot(new Permanent(enchantedPermanent));
                            }
                            entry.setTriggeringPermanentControllerId(enchantedPermanentControllerId);
                        });
            }
        });

        // Curses with "at the beginning of enchanted player's upkeep" abilities (Curse of Oblivion, Curse of the
        // Bloody Tome): "that player" is the enchanted player, the non-targeting target and triggering player of the
        // record (CR 115.10a), whom "any number of target players other than that player" excludes.
        gameData.forEachPermanent((auraOwnerId, perm) -> {
            ConditionContext curseContext = ConditionContext.forPermanent(perm, auraOwnerId)
                    .withTargetId(perm.getAttachedTo());
            List<TriggeredAbility> enchantedPlayerUpkeepAbilities = printedAbilities(gameData, perm.getCard(),
                    ENCHANTED_PLAYER_UPKEEP, Zone.BATTLEFIELD, curseContext);
            if (enchantedPlayerUpkeepAbilities.isEmpty()) return;
            if (!perm.isAttached()) return;

            // For curses, attachedTo is the enchanted player's UUID
            UUID enchantedPlayerId = perm.getAttachedTo();
            if (!enchantedPlayerId.equals(activePlayerId)) return;

            for (TriggeredAbility ability : enchantedPlayerUpkeepAbilities) {
                putStepAbility(gameData, ability, perm.getCard(), perm, auraOwnerId, "upkeep ability", curseContext,
                        entry -> {
                            entry.setTargetId(enchantedPlayerId);
                            entry.setNonTargeting(true);
                            entry.setActivePlayerId(activePlayerId);
                            entry.setTriggeringPlayerId(enchantedPlayerId);
                        });
            }
        });

        // Check exiled cards with egg counters owned by the active player (e.g. Darigaaz Reincarnated).
        // "At the beginning of your upkeep, if this card is exiled with an egg counter on it,
        //  remove an egg counter from it. Then if it has no egg counters, return it to the battlefield."
        if (!gameData.exiledCardEggCounters.isEmpty()) {
            List<Card> exiledCards = gameData.getPlayerExiledCards(activePlayerId);
            if (!exiledCards.isEmpty()) {
                for (Card card : new ArrayList<>(exiledCards)) {
                    Integer eggCounters = gameData.exiledCardEggCounters.get(card.getId());
                    if (eggCounters != null && eggCounters > 0
                            && card.getTriggerEffects(YOUR_UPKEEP, Zone.EXILE).stream()
                            .noneMatch(RemoveEggCounterFromExileAndReturnEffect.class::isInstance)) {
                        // The card's own "at the beginning of your upkeep, if this card is exiled with an egg counter
                        // on it" ability, functioning from exile (Darigaaz Reincarnated).
                        triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(
                                new TriggeredAbility(new TriggerCondition(YOUR_UPKEEP, AbilityZones.EXILE),
                                        new RemoveEggCounterFromExileAndReturnEffect(card.getId())),
                                card, null, activePlayerId, card.getName() + "'s egg counter ability",
                                ConditionContext.forCard(card, activePlayerId), TriggerRecord.NONE));

                        gameLogService.append(gameData,
                                GameLog.cardThen(card, "'s upkeep ability triggers (exiled with egg counters)."));
                        log.info("Game {} - {} egg counter upkeep trigger pushed onto stack", gameData.id, card.getName());
                    }
                }
            }
        }

        // Cards exiled with refine counters trigger for their owner at upkeep.
        if (!gameData.exiledCardRefineCounters.isEmpty()) {
            List<Card> exiledCards = gameData.getPlayerExiledCards(activePlayerId);
            for (Card card : new ArrayList<>(exiledCards)) {
                Integer refineCounters = gameData.exiledCardRefineCounters.get(card.getId());
                if (refineCounters != null && refineCounters > 0) {
                    // "At the beginning of your upkeep, if this card is exiled, remove a refine counter from it", an
                    // ability the card gained as it was exiled (Uvilda, Dean of Perfection).
                    triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(
                            new TriggeredAbility(new TriggerCondition(YOUR_UPKEEP, AbilityZones.EXILE),
                                    new RemoveRefineCounterFromExiledCardEffect(card.getId())),
                            card, null, activePlayerId, card.getName() + "'s refine counter ability",
                            ConditionContext.forCard(card, activePlayerId), TriggerRecord.NONE));
                    gameLogService.append(gameData,
                            GameLog.cardThen(card, "'s refine counter ability triggers."));
                }
            }
        }

        // Suspend: "At the beginning of your upkeep, remove a time counter from this card."
        if (!gameData.exiledCardTimeCounters.isEmpty()) {
            List<Card> exiledCards = gameData.getPlayerExiledCards(activePlayerId);
            for (Card card : new ArrayList<>(exiledCards)) {
                if (gameData.exiledCardsWithNonSuspendTimeCounters.contains(card.getId())
                        && card.getActivatedAbilities(Zone.HAND).stream()
                        .noneMatch(com.github.laxika.magicalvibes.model.ActivatedAbility::isSuspendsSourceFromHand)) {
                    continue;
                }
                Integer timeCounters = gameData.exiledCardTimeCounters.get(card.getId());
                if (timeCounters != null && timeCounters > 0) {
                    // Suspend's "at the beginning of your upkeep, if this card is suspended, remove a time counter
                    // from it" (CR 702.62a).
                    triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(
                            new TriggeredAbility(new TriggerCondition(YOUR_UPKEEP, AbilityZones.EXILE),
                                    new RemoveTimeCounterFromExiledCardEffect(card.getId())),
                            card, null, activePlayerId, card.getName() + "'s suspend ability",
                            ConditionContext.forCard(card, activePlayerId), TriggerRecord.NONE));

                    gameLogService.append(gameData,
                            GameLog.cardThen(card, "'s suspend ability triggers."));
                    log.info("Game {} - {} suspend upkeep trigger pushed onto stack", gameData.id, card.getName());
                }
            }
        }

        for (var exiledEntry : new ArrayList<>(gameData.exiledCards)) {
            if (!activePlayerId.equals(exiledEntry.ownerId())) {
                continue;
            }
            Card card = exiledEntry.card();
            ConditionContext exileContext = ConditionContext.forCard(card, exiledEntry.ownerId());
            List<TriggeredAbility> upkeepAbilities = new ArrayList<>(
                    printedAbilities(gameData, card, YOUR_UPKEEP, Zone.EXILE, exileContext));
            upkeepAbilities.addAll(stepAbilityReader.triggering(gameData,
                    perpetualAbilities(gameData, card, YOUR_UPKEEP, Zone.EXILE), exileContext));
            // Its intervening "if" ("if this card is exiled with a scream counter on it", All Hallow's Eve) is judged
            // as it triggers and again as it resolves (CR 603.4).
            for (TriggeredAbility ability : upkeepAbilities) {
                putStepAbility(gameData, ability, card, null, exiledEntry.ownerId(), "exile upkeep ability",
                        exileContext, entry -> entry.setActivePlayerId(activePlayerId));
            }
        }

        // Suspended cards may also trigger during every player's upkeep; "that player" is the player whose upkeep it is.
        for (var exiledEntry : new ArrayList<>(gameData.exiledCards)) {
            Card card = exiledEntry.card();
            if (gameData.exiledCardsWithNonSuspendTimeCounters.contains(card.getId())
                    && card.getActivatedAbilities(Zone.HAND).stream()
                    .noneMatch(com.github.laxika.magicalvibes.model.ActivatedAbility::isSuspendsSourceFromHand)) {
                continue;
            }
            Integer timeCounters = gameData.exiledCardTimeCounters.get(card.getId());
            if (timeCounters == null || timeCounters <= 0) {
                continue;
            }
            ConditionContext exileContext = ConditionContext.forCard(card, exiledEntry.ownerId());
            for (TriggeredAbility ability : printedAbilities(gameData, card, EACH_UPKEEP, Zone.EXILE, exileContext)) {
                putStepAbility(gameData, ability, card, null, exiledEntry.ownerId(), "suspended ability",
                        exileContext, stepPlayerRecord(activePlayerId, null));
            }
        }

        // Ertai's Meddling: "At the beginning of each of that player's upkeeps, if that card is
        // exiled, remove a delay counter from it." The trigger fires only on the exiled spell's
        // controller's upkeeps, but it is controlled by the player who resolved Ertai's Meddling
        // and its source is that Meddling card.
        for (GameData.DelayedSpellExile pending : new ArrayList<>(gameData.delayedSpellExiles)) {
            if (!pending.controllerId().equals(activePlayerId)) {
                continue;
            }
            Card meddlingCard = pending.meddlingCard();
            // A delayed triggered ability Ertai's Meddling made as it resolved: its source is the Meddling and its
            // controller the player who controlled it (CR 603.7d).
            StackEntry delayCounter = TriggeredAbilityStackService.madeAbility(meddlingCard, pending.meddlerId(),
                    meddlingCard.getName() + " delay counter ability",
                    List.of(new RemoveDelayCounterFromExiledSpellEffect(pending.cardId())));
            delayCounter.setNonTargeting(true);
            triggeredAbilityStackService.putDelayed(gameData, delayCounter);

            gameLogService.append(gameData,
                    GameLog.cardThen(meddlingCard, "'s delay counter ability triggers."));
            log.info("Game {} - {} delay counter upkeep trigger pushed onto stack", gameData.id, meddlingCard.getName());
        }

        // Suspend: remove a time counter from every suspended spell owned by the active player.
        for (GameData.SuspendedSpellExile pending : new ArrayList<>(gameData.suspendedSpellExiles)) {
            if (!pending.ownerId().equals(activePlayerId)) {
                continue;
            }
            var suspendedEntry = gameData.findExiledCard(pending.cardId());
            if (suspendedEntry == null) {
                continue;
            }
            Card suspendedCard = suspendedEntry.card();
            // Suspend's "remove a time counter" upkeep ability of a spell exiled with time counters that gained suspend
            // (CR 702.62a).
            triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(
                    new TriggeredAbility(new TriggerCondition(YOUR_UPKEEP, AbilityZones.EXILE),
                            new RemoveSuspendCounterFromExiledSpellEffect(pending.cardId())),
                    suspendedCard, null, activePlayerId, suspendedCard.getName() + "'s suspend ability",
                    ConditionContext.forCard(suspendedCard, activePlayerId), TriggerRecord.NONE));

            gameLogService.append(gameData, GameLog.cardThen(suspendedCard, "'s suspend ability triggers."));
            log.info("Game {} - {} suspend upkeep trigger pushed onto stack", gameData.id, suspendedCard.getName());
        }

        // Phase-in targeted triggers were queued during the untap-step phasing action; choose targets
        // now as they go on the stack (before upkeep's own targeted triggers).

        processNextPucasMischiefTarget(gameData);
    }

    /**
     * Begins the next mode or target choice of a triggered ability the stack door put on the stack or queued as it went
     * there (CR 603.3c, 603.3d), before anyone gets priority: the mode of a modal ability already on the stack and its
     * division of damage, then the queued mode and target choices (its targets in one choice or group by group, its
     * graveyard cards); the answer's handler begins the one after it. With none left, the may abilities are asked.
     */
    public void beginNextStepTriggerChoice(GameData gameData) {
        if (gameData.interaction.isAwaitingInput()) {
            return;
        }
        if (stackedModalTriggerService.announceNextMode(gameData)
                || stackedDividedDamageTriggerService.announceNextDivision(gameData)) {
            return;
        }
        if (gameData.hasPendingInteraction(PermanentChoiceContext.TriggeredModalTrigger.class)) {
            triggerCollectionService.processNextTriggeredModalTrigger(gameData);
            if (gameData.interaction.isAwaitingInput()) {
                return;
            }
        }
        if (gameData.hasPendingInteraction(PermanentChoiceContext.AttackTriggerTarget.class)) {
            triggerCollectionService.processNextAttackTriggerTarget(gameData);
            if (gameData.interaction.isAwaitingInput()) {
                return;
            }
        }
        if (gameData.hasPendingInteraction(PermanentChoiceContext.ETBTokenMultiTargetTrigger.class)) {
            etbTokenTargetService.processNextETBTokenMultiTargetTrigger(gameData);
            if (gameData.interaction.isAwaitingInput()) {
                return;
            }
        }
        if (gameData.hasPendingInteraction(PermanentChoiceContext.SpellGraveyardTargetTrigger.class)) {
            triggerCollectionService.processNextSpellGraveyardTargetTrigger(gameData);
            if (gameData.interaction.isAwaitingInput()) {
                return;
            }
        }
        triggerCollectionService.processNextDayNightTriggerTarget(gameData);
        if (gameData.interaction.isAwaitingInput()) {
            return;
        }
        playerInputService.processNextMayAbility(gameData);
    }

    /**
     * Hands {@code ability}, an "at the beginning of your upkeep" ability of {@code perm}, to the stack door, recording
     * the active player, the source as it was, the permanent an Aura or Equipment is attached to ("shares a creature
     * type with enchanted creature", Call to the Kindred, read as it resolves and from the creature's last-known
     * information once it has left, CR 608.2h). The door chooses a mode as it goes on the stack ("choose one that
     * hasn't been chosen", Demonic Pact) and its targets with all its effects (CR 603.3c, 603.3d), by its own target
     * filter (a granted ability's "target opponent", Custody Battle) over the card's.
     */
    private void putYourUpkeepAbility(GameData gameData, Permanent perm, UUID activePlayerId,
                                      TriggeredAbility ability, ConditionContext context) {
        putStepAbility(gameData, ability, perm.getCard(), perm, activePlayerId, "upkeep ability", context, entry -> {
            entry.setActivePlayerId(activePlayerId);
            entry.setSourcePermanentSnapshot(new Permanent(perm));
            recordAttachedPermanent(gameData, perm, entry);
        });
    }

    /**
     * Ends every "until your next upkeep, target permanent can't phase out" restriction (Spatial
     * Binding) created by the active player. Phased-out permanents are scanned too so a restriction
     * never outlives its window on an object that is currently off the battlefield.
     */
    private void expireCantPhaseOut(GameData gameData) {
        gameData.forEachBattlefield((controllerId, permanents) -> permanents.forEach(permanent -> {
            permanent.expirePhaseOutPreventionAtUpkeepOf(gameData.activePlayerId);
        }));
        gameData.phasedOutPermanents.forEach((controllerId, permanents) -> permanents.forEach(permanent -> {
            permanent.expirePhaseOutPreventionAtUpkeepOf(gameData.activePlayerId);
        }));
    }

    /**
     * Begins the first target choice of the next queued exchange whose second target depends on the first: "target
     * creature you control and target creature an opponent controls with power less than or equal to that creature's
     * power" (Spawnbroker), or, for a card with {@link MultiTargetConstraint#MANA_VALUE_AT_MOST_FIRST_TARGET}, "target
     * nonland permanent you control and target nonland permanent an opponent controls with an equal or lesser mana
     * value" (Puca's Mischief). A permanent you control is offered only with a legal partner (CR 601.2c via 603.3d).
     */
    public void processNextPucasMischiefTarget(GameData gameData) {
        if (!gameData.hasPendingInteraction(PermanentChoiceContext.PucasMischiefOwnTarget.class)) {
            beginNextStepTriggerChoice(gameData);
            return;
        }

        PermanentChoiceContext.PucasMischiefOwnTarget trigger =
                gameData.pollPendingInteraction(PermanentChoiceContext.PucasMischiefOwnTarget.class);
        boolean byManaValue = trigger.sourceCard().getMultiTargetConstraint()
                == MultiTargetConstraint.MANA_VALUE_AT_MOST_FIRST_TARGET;

        List<UUID> validOwnTargets = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(trigger.controllerId());
        if (battlefield != null) {
            for (Permanent own : battlefield) {
                if (validTargetService.isBlockedByHexproofOrGrantedEffect(gameData, own, trigger.controllerId())) continue;
                boolean hasPartner = byManaValue
                        ? !own.getCard().hasType(CardType.LAND)
                        && hasOpponentNonlandAtMostManaValue(gameData, trigger.controllerId(),
                        own.getCard().getManaValue())
                        : gameQueryService.isCreature(gameData, own)
                        && hasOpponentCreatureAtMostPower(gameData, trigger.controllerId(),
                        gameQueryService.getEffectivePower(gameData, own));
                if (hasPartner) {
                    validOwnTargets.add(own.getId());
                }
            }
        }
        if (validOwnTargets.isEmpty()) {
            log.info("Game {} - {} exchange skipped (no legal target pair)",
                    gameData.id, trigger.sourceCard().getName());
            processNextPucasMischiefTarget(gameData);
            return;
        }
        gameData.interaction.setPermanentChoiceContext(trigger);
        playerInputService.beginPermanentChoice(gameData, trigger.controllerId(), validOwnTargets,
                trigger.sourceCard().getName() + (byManaValue
                        ? " — Choose a nonland permanent you control." : " — Choose a creature you control."));
        gameLogService.append(gameData, GameLog.cardThen(trigger.sourceCard(), byManaValue
                ? "'s triggered ability triggers." : "'s enter-the-battlefield ability triggers."));
    }

    private boolean hasOpponentNonlandAtMostManaValue(GameData gameData, UUID controllerId, int manaValue) {
        for (UUID pid : gameData.orderedPlayerIds) {
            if (pid.equals(controllerId)) continue;
            for (Permanent permanent : gameData.playerBattlefields.getOrDefault(pid, List.of())) {
                if (!permanent.getCard().hasType(CardType.LAND)
                        && permanent.getCard().getManaValue() <= manaValue
                        && !validTargetService.isBlockedByHexproofOrGrantedEffect(gameData, permanent, controllerId)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasOpponentCreatureAtMostPower(GameData gameData, UUID controllerId, int power) {
        for (UUID pid : gameData.orderedPlayerIds) {
            if (pid.equals(controllerId)) continue;
            List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
            if (battlefield == null) continue;
            for (Permanent permanent : battlefield) {
                if (gameQueryService.isCreature(gameData, permanent)
                        && gameQueryService.getEffectivePower(gameData, permanent) <= power
                        && !validTargetService.isBlockedByHexproofOrGrantedEffect(gameData, permanent, controllerId)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Puts pending opening-hand reveal triggers onto the stack. "At the beginning of the first upkeep"
     * (the Chancellors) fires at the first upkeep of the game whichever player revealed the card, so the
     * first upkeep drains all of those; "at the beginning of your first upkeep" (Sphinx of Foresight) waits
     * for the revealing player's own first upkeep. The active player's triggers go on the stack first (APNAP).
     */
    private void handleOpeningHandUpkeepTriggers(GameData gameData) {
        UUID activePlayerId = gameData.activePlayerId;
        List<OpeningHandRevealTrigger> due = new ArrayList<>(gameData.openingHandUpkeepTriggers.stream()
                .filter(trigger -> !trigger.revealersFirstUpkeep()
                        || trigger.revealingPlayerId().equals(activePlayerId))
                .toList());
        due.sort(java.util.Comparator.comparing(trigger -> !trigger.revealingPlayerId().equals(activePlayerId)));
        for (OpeningHandRevealTrigger trigger : due) {
            gameData.openingHandUpkeepTriggers.remove(trigger);
            // A delayed triggered ability revealing the card made, with the card as its source (CR 603.7).
            StackEntry entry = TriggeredAbilityStackService.madeAbility(trigger.sourceCard(),
                    trigger.revealingPlayerId(), trigger.sourceCard().getName() + "'s opening hand ability",
                    List.of(trigger.effect()));
            triggeredAbilityStackService.putDelayed(gameData, entry);
        }
    }

    /**
     * Executes the draw step: the active player draws a card (rule 504.1),
     * unless it is turn 1 for the starting player (rule 103.7a).
     * Then scans for {@code Triggers.drawStep(ActivePlayer.YOU)} and {@code Triggers.drawStep(ActivePlayer.ANY)}
     * abilities.
     *
     * @param gameData the current game state to modify
     */
    public void handleDrawStep(GameData gameData) {
        UUID activePlayerId = gameData.activePlayerId;
        gameData.drawStepSkippedThisStep = false;

        // The starting player skips their entire draw step on turn 1 (rule 103.7a)
        if (gameData.turnNumber == 1 && activePlayerId.equals(gameData.startingPlayerId)
                && gameData.additionalBeginningPhaseReturnStep == null) {
            String logEntry = gameData.playerIdToName.get(activePlayerId) + " skips the draw (first turn).";
            gameLogService.append(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} skips draw on turn 1", gameData.id, gameData.playerIdToName.get(activePlayerId));
            gameData.drawStepSkippedThisStep = true;
            return;
        }

        if (gameData.skippedStepOrPhasesThisTurn
                .getOrDefault(activePlayerId, Set.of()).contains(SkipStepOrPhaseKind.DRAW_STEP)) {
            String logEntry = gameData.playerIdToName.get(activePlayerId) + " skips their draw step.";
            gameLogService.append(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} skips draw step (chosen step or phase)", gameData.id,
                    gameData.playerIdToName.get(activePlayerId));
            gameData.drawStepSkippedThisStep = true;
            return;
        }
        Integer currentTurnSkip = gameData.skipDrawStepThisTurn.remove(activePlayerId);

        // A one-shot skip queued earlier (e.g. Ivory Gargoyle's death trigger) consumes one draw step.
        int queuedSkips = gameData.skipNextDrawStepCount.getOrDefault(activePlayerId, 0);
        if (queuedSkips > 0) {
            if (queuedSkips - 1 > 0) {
                gameData.skipNextDrawStepCount.put(activePlayerId, queuedSkips - 1);
            } else {
                gameData.skipNextDrawStepCount.remove(activePlayerId);
            }
            String logEntry = gameData.playerIdToName.get(activePlayerId) + " skips their draw step.";
            gameLogService.append(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} skips draw step (queued skip)", gameData.id, gameData.playerIdToName.get(activePlayerId));
            gameData.drawStepSkippedThisStep = true;
            return;
        }

        if (currentTurnSkip != null && currentTurnSkip == gameData.turnNumber) {
            String logEntry = gameData.playerIdToName.get(activePlayerId) + " skips their draw step.";
            gameLogService.append(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} skips draw step (current-turn effect)", gameData.id, gameData.playerIdToName.get(activePlayerId));
            gameData.drawStepSkippedThisStep = true;
            return;
        }

        // A permanent may instruct its controller to skip their draw step (e.g. Colfenor's Plans).
        if (controlsSkipDrawStep(gameData, activePlayerId)) {
            String logEntry = gameData.playerIdToName.get(activePlayerId) + " skips their draw step.";
            gameLogService.append(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} skips draw step (SkipDrawStepEffect)", gameData.id, gameData.playerIdToName.get(activePlayerId));
            gameData.drawStepSkippedThisStep = true;
            return;
        }

        List<DrawStepReplacementSource> replacements = findMaySkipDrawStepDrawSources(gameData, activePlayerId);
        if (!replacements.isEmpty()) {
            for (DrawStepReplacementSource replacement : replacements) {
                gameData.pendingMayAbilities.add(new PendingMayAbility(
                        replacement.card(),
                        activePlayerId,
                        List.of(new ReplaceSingleDrawEffect(activePlayerId, replacement.kind())),
                        replacement.kind() == DrawReplacementKind.FASTING
                                ? "Skip your draw step and gain 2 life?"
                                : "Skip your draw? Until your next turn you can only be attacked by creatures with flying and/or islandwalk."
                ));
            }

            if (replacements.getFirst().kind() == DrawReplacementKind.ISLAND_SANCTUARY) {
                // Draw step triggered abilities (e.g. Howling Mine) still trigger at the beginning of the step.
                drawDuringOpponentsDrawStep(gameData);
                handleDrawStepTriggers(gameData);
            }

            if (!gameData.pendingMayAbilities.isEmpty() && !gameData.interaction.isAwaitingInput()) {
                playerInputService.processNextMayAbility(gameData);
            }
            return;
        }

        // Normal draw (turn-based action, rule 504.1)
        drawService.resolveDrawCard(gameData, activePlayerId);
        drawDuringOpponentsDrawStep(gameData);

        // Check for draw step triggered abilities (e.g. Howling Mine)
        handleDrawStepTriggers(gameData);

        if (!gameData.pendingMayAbilities.isEmpty() && !gameData.interaction.isAwaitingInput()) {
            playerInputService.processNextMayAbility(gameData);
        }
    }

    /**
     * "You draw a card during each opponent's draw step" (Teferi, Who Slows the Sunset's emblem,
     * {@link DrawDuringEachOpponentsDrawStepEffect}): each emblem controller other than the active player draws right
     * after the active player's draw-step draw (CR 504.1), with no player getting priority in between (the card's
     * ruling), in APNAP order. Abilities of emblems function in the command zone (CR 114.4).
     */
    private void drawDuringOpponentsDrawStep(GameData gameData) {
        UUID activePlayerId = gameData.activePlayerId;
        List<UUID> players = gameData.orderedPlayerIds;
        int activeIndex = players.indexOf(activePlayerId);
        for (int offset = 1; offset < players.size(); offset++) {
            UUID playerId = players.get((activeIndex + offset) % players.size());
            for (Emblem emblem : List.copyOf(gameData.emblems)) {
                if (!playerId.equals(emblem.controllerId())) {
                    continue;
                }
                for (CardEffect effect : emblem.staticEffects()) {
                    if (effect instanceof DrawDuringEachOpponentsDrawStepEffect draw) {
                        for (int i = 0; i < draw.amount(); i++) {
                            drawService.resolveDrawCard(gameData, playerId);
                        }
                    }
                }
            }
        }
    }

    /**
     * Every permanent offering the same kind of draw-step skip, in battlefield order. Each one is a separate
     * replacement effect the player may decline in turn (CR 616.1), so each gets its own offer.
     */
    private List<DrawStepReplacementSource> findMaySkipDrawStepDrawSources(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return List.of();
        List<DrawStepReplacementSource> sources = new ArrayList<>();
        for (Permanent perm : battlefield) {
            for (CardEffect effect : perm.getCard().getStaticEffects()) {
                if (effect instanceof MaySkipDrawReplacementEffect replacement && replacement.drawStepOnly()) {
                    sources.add(new DrawStepReplacementSource(perm.getCard(), replacement.replacementKind()));
                }
            }
        }
        if (sources.isEmpty()) return List.of();
        DrawReplacementKind kind = sources.getFirst().kind();
        return sources.stream().filter(source -> source.kind() == kind).toList();
    }

    private record DrawStepReplacementSource(Card card, DrawReplacementKind kind) {
    }

    public boolean controlsSkipDrawStep(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return false;
        for (Permanent perm : battlefield) {
            if (gameQueryService.hasLostAllAbilities(gameData, perm)) continue;
            for (CardEffect effect : perm.getCard().getStaticEffects()) {
                if (effect instanceof SkipDrawStepEffect) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Puts on the stack the delayed "for as long as [source] remains tapped, at the beginning of each of your draw
     * steps" abilities of the active player (Giant Oyster's, its activator's: CR 603.7e), each aimed at its permanent
     * without targeting it. One whose source has untapped or left the battlefield, or whose permanent has left it,
     * has ended and is removed.
     */
    private void collectDelayedDrawStepTriggersWhileSourceTapped(GameData gameData, UUID activePlayerId) {
        for (DelayedDrawStepTriggerWhileSourceTapped delayed
                : gameData.getDelayedActions(DelayedDrawStepTriggerWhileSourceTapped.class)) {
            Permanent source = gameQueryService.findPermanentById(gameData, delayed.sourcePermanentId());
            Permanent affected = gameQueryService.findPermanentById(gameData, delayed.affectedPermanentId());
            if (source == null || affected == null || source.getUntapSequence() != delayed.sourceUntapSequence()) {
                gameData.clearDelayedActions(DelayedDrawStepTriggerWhileSourceTapped.class, delayed::equals);
                continue;
            }
            if (!activePlayerId.equals(delayed.controllerId())) continue;
            StackEntry triggerEntry = TriggeredAbilityStackService.madeAbility(delayed.sourceCard(),
                    delayed.controllerId(), delayed.sourceCard().getName() + "'s delayed ability",
                    new ArrayList<>(List.of(delayed.effect())), affected.getId(), source.getId());
            triggerEntry.setNonTargeting(true);
            triggerEntry.setSourcePermanentSnapshot(new Permanent(source));
            triggeredAbilityStackService.putDelayed(gameData, triggerEntry);
            gameLogService.append(gameData, GameLog.cardThen(delayed.sourceCard(), "'s delayed ability triggers."));
        }
    }

    public boolean playersSkipUpkeepStepApplies(GameData gameData) {
        boolean globalSkip = gameData.anyPermanentMatches(permanent ->
                gameQueryService.getActiveStaticEffects(gameData, permanent).stream()
                        .anyMatch(PlayersSkipUpkeepStepEffect::isGlobal));
        if (globalSkip) {
            return true;
        }

        if (!gameData.playerHands.getOrDefault(gameData.activePlayerId, List.of()).isEmpty()) {
            return false;
        }
        return gameData.playerBattlefields.getOrDefault(gameData.activePlayerId, List.of()).stream()
                .flatMap(permanent -> gameQueryService.getActiveStaticEffects(gameData, permanent).stream())
                .anyMatch(PlayersSkipUpkeepStepEffect::isControllerScoped);
    }

    public void handleDrawStepTriggers(GameData gameData) {
        UUID activePlayerId = gameData.activePlayerId;
        collectEmblemStepTriggers(gameData, StepTrigger.Moment.DRAW_STEP, ActivePlayer.OPPONENT);
        collectEmblemStepTriggers(gameData, StepTrigger.Moment.DRAW_STEP, ActivePlayer.YOU);

        // Nafs Asp / Glass Asp: paying before this draw step is a special action (CR 116.2c,
        // AbilityActivationService.payDelayedTriggerCost) that removes the obligation; the payment
        // window closes as this step begins, so every obligation still queued here was not paid.
        if (gameData.hasDelayedAction(LoseLifeAtNextDrawStepUnlessPays.class)) {
            List<LoseLifeAtNextDrawStepUnlessPays> pending = gameData.drainDelayedActions(
                    LoseLifeAtNextDrawStepUnlessPays.class, a -> a.playerId().equals(activePlayerId));
            for (LoseLifeAtNextDrawStepUnlessPays action : pending) {
                // The delayed ability belongs to whoever controlled the ability that created it
                // (CR 603.7d); the drawing player is the one who loses life.
                StackEntry delayed = TriggeredAbilityStackService.madeAbility(
                        action.sourceCard(),
                        action.controllerId(),
                        action.sourceCard().getName() + "'s delayed ability",
                        new ArrayList<>(List.of(new LoseLifeEffect(action.lifeLoss(),
                                LoseLifeRecipient.TRIGGERING_PLAYER))),
                        activePlayerId,
                        (UUID) null);
                delayed.setNonTargeting(true);
                triggeredAbilityStackService.putDelayed(gameData, delayed);

                gameLogService.append(gameData, GameLog.cardThen(action.sourceCard(),
                        "'s delayed ability triggers — " + gameData.playerIdToName.get(activePlayerId)
                        + " loses " + action.lifeLoss() + " life."));
                log.info("Game {} - {} delayed draw-step life-loss trigger pushed for {}",
                        gameData.id, action.sourceCard().getName(), gameData.playerIdToName.get(activePlayerId));
            }
        }

        if (gameData.planechase != null) planechaseService.drawStep(gameData);

        // Triggers.drawStep(ActivePlayer.YOU) of the active player's permanents: "that player" is the active player,
        // and the source is recorded as it was, whatever the effects are.
        List<Permanent> activeBattlefield = gameData.playerBattlefields.get(activePlayerId);
        if (activeBattlefield != null) {
            for (Permanent perm : activeBattlefield) {
                if (perm.isFaceDown() || gameQueryService.hasLostPrintedAbilities(gameData, perm)) {
                    continue;
                }
                ConditionContext drawContext = ConditionContext.forPermanent(perm, activePlayerId);
                for (TriggeredAbility ability : printedAbilities(gameData, perm.getCard(), YOUR_DRAW_STEP,
                        Zone.BATTLEFIELD, drawContext)) {
                    putStepAbility(gameData, ability, perm.getCard(), perm, activePlayerId, "draw step ability",
                            drawContext, entry -> {
                                entry.setTargetId(activePlayerId);
                                entry.setNonTargeting(true);
                                entry.setSourcePermanentSnapshot(new Permanent(perm));
                            });
                }
            }
        }

        collectDelayedDrawStepTriggersWhileSourceTapped(gameData, activePlayerId);

        // Auras: "at the beginning of the draw step of enchanted permanent's controller" fires on the enchanted
        // permanent's controller's draw step (Righteous Authority); that player is the record's non-targeting target.
        gameData.forEachPermanent((auraOwnerId, perm) -> {
            ConditionContext auraContext = ConditionContext.forPermanent(perm, auraOwnerId);
            List<TriggeredAbility> enchantedControllerDrawAbilities = printedAbilities(gameData, perm.getCard(),
                    ENCHANTED_CONTROLLER_DRAW_STEP, Zone.BATTLEFIELD, auraContext);
            if (enchantedControllerDrawAbilities.isEmpty()) return;
            if (!perm.isAttached()) return;

            UUID enchantedPermanentControllerId =
                    gameQueryService.findPermanentController(gameData, perm.getAttachedTo());
            if (enchantedPermanentControllerId == null) return;
            if (!enchantedPermanentControllerId.equals(activePlayerId)) return;

            for (TriggeredAbility ability : enchantedControllerDrawAbilities) {
                putStepAbility(gameData, ability, perm.getCard(), perm, auraOwnerId, "draw step ability",
                        auraContext, entry -> {
                            entry.setTargetId(enchantedPermanentControllerId);
                            entry.setNonTargeting(true);
                        });
            }
        });

        // Player-enchanting Curses with Triggers.drawStep(ActivePlayer.ENCHANTED_PLAYER): they fire during the
        // enchanted player's draw step and act on that player, the record's non-targeting target.
        gameData.forEachPermanent((auraOwnerId, perm) -> {
            ConditionContext curseContext = ConditionContext.forPermanent(perm, auraOwnerId)
                    .withTargetId(perm.getAttachedTo());
            List<TriggeredAbility> enchantedPlayerDrawAbilities = printedAbilities(gameData, perm.getCard(),
                    ENCHANTED_PLAYER_DRAW_STEP, Zone.BATTLEFIELD, curseContext);
            if (enchantedPlayerDrawAbilities.isEmpty()) return;
            if (!perm.isAttached()) return;

            UUID enchantedPlayerId = perm.getAttachedTo();
            if (!enchantedPlayerId.equals(activePlayerId)) return;

            for (TriggeredAbility ability : enchantedPlayerDrawAbilities) {
                putStepAbility(gameData, ability, perm.getCard(), perm, auraOwnerId, "draw step ability",
                        curseContext, entry -> {
                            entry.setTargetId(enchantedPlayerId);
                            entry.setNonTargeting(true);
                        });
            }
        });

        // "At the beginning of each draw step" abilities (all players' draw steps) and "at the beginning of each
        // opponent's draw step" ones (an opponent of the permanent's controller's draw step): "that player" is the
        // player whose draw step it is (Howling Mine, Teferi's Puzzle Box).
        gameData.forEachPermanent((playerId, perm) -> {
            ConditionContext drawStepContext = ConditionContext.forPermanent(perm, playerId).withTargetId(activePlayerId);
            List<TriggeredAbility> drawAbilities = new ArrayList<>(printedAbilities(gameData, perm.getCard(),
                    EACH_DRAW_STEP, Zone.BATTLEFIELD, drawStepContext));
            if (!playerId.equals(activePlayerId)) {
                drawAbilities.addAll(printedAbilities(gameData, perm.getCard(), OPPONENT_DRAW_STEP, Zone.BATTLEFIELD,
                        drawStepContext));
            }
            if (drawAbilities.isEmpty()) return;
            if (gameQueryService.hasLostPrintedAbilities(gameData, perm)) return;

            for (TriggeredAbility ability : drawAbilities) {
                putStepAbility(gameData, ability, perm.getCard(), perm, playerId, "draw step ability",
                        drawStepContext, stepPlayerRecord(activePlayerId, perm));
            }
        });
        beginNextStepTriggerChoice(gameData);
    }

    /**
     * Fires Chancellor-style delayed mana triggers at the beginning of the
     * revealing player's first precombat main phase.
     *
     * @param gameData the current game state to modify
     */
    public void handlePrecombatMainTriggers(GameData gameData) {
        gameData.mainPhasesBegunThisTurn++;
        if (gameData.mainPhasesBegunThisTurn == 2) {
            handleMainPhaseTriggers(gameData, YOUR_SECOND_MAIN_PHASE);
        }
        if (gameData.planechase != null) planechaseService.step(gameData,
                YOUR_PRECOMBAT_MAIN_PHASE);

        collectEmblemStepTriggers(gameData, StepTrigger.Moment.PRECOMBAT_MAIN_PHASE, ActivePlayer.YOU);
        handleRadCounterTrigger(gameData);

        // Saga lore counters: add a lore counter to each Saga the active player controls (CR 715.3c)
        handleSagaLoreCounters(gameData);

        handlePrecombatMainBattlefieldTriggers(gameData);
        handlePrecombatMainGraveyardTriggers(gameData);

        handleEachPrecombatMainTriggers(gameData);

        paradigmService.firePrecombatMainTriggers(gameData);

        // Chancellor-style delayed mana triggers: fire at the beginning of the revealing player's first main phase
        if (!gameData.openingHandManaTriggers.isEmpty()) {
            UUID activePlayerId = gameData.activePlayerId;
            List<OpeningHandRevealTrigger> toFire = gameData.openingHandManaTriggers.stream()
                    .filter(t -> t.revealingPlayerId().equals(activePlayerId))
                    .toList();

            if (!toFire.isEmpty()) {
                gameData.openingHandManaTriggers.removeAll(toFire);
                for (OpeningHandRevealTrigger trigger : toFire) {
                    // A delayed triggered ability revealing the card made (CR 603.7); it adds mana as it resolves,
                    // on the stack (it doesn't trigger from a mana ability, CR 605.1b).
                    triggeredAbilityStackService.putDelayed(gameData, TriggeredAbilityStackService.madeAbility(
                            trigger.sourceCard(), trigger.revealingPlayerId(),
                            trigger.sourceCard().getName() + "'s ability", List.of(trigger.effect())));

                    gameLogService.append(gameData,
                            GameLog.cardThen(trigger.sourceCard(), "'s delayed trigger fires — adds mana."));
                    log.info("Game {} - {}'s opening hand mana trigger fires for {}",
                            gameData.id, trigger.sourceCard().getName(),
                            gameData.playerIdToName.get(activePlayerId));
                }
            }
        }

        drainAddManaAtNextMainPhase(gameData, true);
        drainDrawCardsAtNextMainPhase(gameData);

        beginNextStepTriggerChoice(gameData);
    }

    private void handleRadCounterTrigger(GameData gameData) {
        UUID activePlayerId = gameData.activePlayerId;
        int radCounters = gameData.playerRadCounters.getOrDefault(activePlayerId, 0);
        if (radCounters <= 0) {
            return;
        }

        // Rad counters' inherent ability, controlled by the active player (CR 729.1); its intervening "if" is judged
        // again as it resolves.
        triggeredAbilityStackService.putInherent(gameData, activePlayerId,
                gameData.playerIdToName.get(activePlayerId) + "'s rad ability",
                List.of(new ResolveRadCountersEffect()), TriggerRecord.NONE);
        gameLogService.append(gameData, GameLog.text(
                gameData.playerIdToName.get(activePlayerId) + "'s rad ability triggers."));
        log.info("Game {} - {}'s rad ability triggers", gameData.id,
                gameData.playerIdToName.get(activePlayerId));
    }

    /**
     * Fires delayed "add mana at the beginning of your next main phase" abilities for the active
     * player. Called on entry to both precombat and postcombat main (Conduit of Storms schedules
     * during combat for the postcombat main; Scattering Stroke may schedule for either).
     *
     * @param precombatMain whether this is the precombat ("first") main phase — entries scheduled by
     *                      a "next first main phase" wording (Plasm Capture) stay queued otherwise
     */
    public void drainAddManaAtNextMainPhase(GameData gameData, boolean precombatMain) {
        UUID mainPhasePlayerId = gameData.activePlayerId;
        List<AddManaAtNextMainPhase> manaRewards = gameData.drainDelayedActions(
                AddManaAtNextMainPhase.class,
                a -> a.controllerId().equals(mainPhasePlayerId) && (precombatMain || !a.firstMainOnly()));
        for (AddManaAtNextMainPhase reward : manaRewards) {
            CardEffect manaEffect = reward.anyColorCombination()
                    ? new AwardManaOfColorsEffect(ManaColor.COLORS, reward.amount())
                    : new AwardManaEffect(reward.color(), reward.amount());
            if (reward.optional()) {
                manaEffect = new MayEffect(
                        manaEffect,
                        "Add " + reward.amount() + " " + reward.color().getCode() + "?");
            }
            triggeredAbilityStackService.putDelayed(gameData, TriggeredAbilityStackService.madeAbility(
                    reward.sourceCard(),
                    reward.controllerId(),
                    reward.sourceCard().getName() + "'s delayed ability",
                    new ArrayList<>(List.of(manaEffect))
            ));

            gameLogService.append(gameData, GameLog.cardThen(reward.sourceCard(), "'s delayed ability triggers."));
            log.info("Game {} - {}'s delayed mana reward fires for {}",
                    gameData.id, reward.sourceCard().getName(),
                    gameData.playerIdToName.get(mainPhasePlayerId));
        }
    }

    /** Fires delayed draws that are due at the active player's next main phase. */
    public void drainDrawCardsAtNextMainPhase(GameData gameData) {
        UUID mainPhasePlayerId = gameData.activePlayerId;
        List<DrawCardsAtNextMainPhase> pendingDraws = gameData.drainDelayedActions(
                DrawCardsAtNextMainPhase.class,
                action -> action.controllerId().equals(mainPhasePlayerId));
        int damagedPlayers = (int) gameData.combatDamageDealtToPlayersThisTurn.keySet().stream()
                .filter(gameData.playerIds::contains)
                .count();
        for (DrawCardsAtNextMainPhase pending : pendingDraws) {
            StackEntry entry = TriggeredAbilityStackService.madeAbility(
                    pending.sourceCard(),
                    pending.controllerId(),
                    pending.sourceCard().getName() + "'s delayed ability",
                    new ArrayList<>(List.of(new DrawCardEffect(damagedPlayers)))
            );
            entry.setNonTargeting(true);
            triggeredAbilityStackService.putDelayed(gameData, entry);
            gameLogService.append(gameData, GameLog.cardThen(pending.sourceCard(),
                    "'s delayed ability triggers — draw " + damagedPlayers + " card(s)."));
            log.info("Game {} - {} delayed main-phase draw trigger pushed onto the stack for {} card(s)",
                    gameData.id, pending.sourceCard().getName(), damagedPlayers);
        }
    }

    /**
     * Hands the "at the beginning of your precombat main phase" abilities of the active player's permanents (Abstract
     * Paintmage) to the stack door, each one whole with the source as it was and the permanent an Aura or Equipment is
     * attached to, the last one first; the door chooses a mode or targets as it goes on the stack.
     */
    private void handlePrecombatMainBattlefieldTriggers(GameData gameData) {
        UUID activePlayerId = gameData.activePlayerId;
        List<Permanent> battlefield = gameData.playerBattlefields.get(activePlayerId);
        if (battlefield == null) {
            return;
        }
        for (Permanent perm : battlefield) {
            if (gameQueryService.hasLostPrintedAbilities(gameData, perm)) {
                continue;
            }
            ConditionContext context = ConditionContext.forPermanent(perm, activePlayerId);
            // Its controller orders them (CR 603.3b); they are put there with the first ability on top.
            for (TriggeredAbility ability : printedAbilities(gameData, perm.getCard(), YOUR_PRECOMBAT_MAIN_PHASE,
                    Zone.BATTLEFIELD, context).reversed()) {
                putStepAbility(gameData, ability, perm.getCard(), perm, activePlayerId, "ability", context, entry -> {
                    entry.setSourcePermanentSnapshot(new Permanent(perm));
                    recordAttachedPermanent(gameData, perm, entry);
                });
            }
        }
    }

    /** Hands the precombat-main abilities of cards in the active player's graveyard to the stack door. */
    private void handlePrecombatMainGraveyardTriggers(GameData gameData) {
        UUID activePlayerId = gameData.activePlayerId;
        List<Card> graveyard = gameData.playerGraveyards.get(activePlayerId);
        if (graveyard == null) {
            return;
        }
        for (Card card : new ArrayList<>(graveyard)) {
            ConditionContext context = ConditionContext.forCard(card, activePlayerId);
            for (TriggeredAbility ability : graveyardAbilities(gameData, card, YOUR_PRECOMBAT_MAIN_PHASE, context)) {
                putStepAbility(gameData, ability, card, null, activePlayerId, "precombat main ability", context,
                        graveyardStayRecord(gameData, card));
            }
        }
    }

    /**
     * Hands the abilities that watch every player's first main phase (Eladamri's Vineyard) to the stack door, controlled
     * by their source's controller. The active player is recorded as the entry's active player, not as a target: these
     * abilities don't target, so a player's hexproof or shroud must not counter them, and effects that act on "that
     * player" read the active player.
     */
    private void handleEachPrecombatMainTriggers(GameData gameData) {
        UUID activePlayerId = gameData.activePlayerId;
        gameData.forEachPermanent((playerId, perm) -> {
            ConditionContext context = ConditionContext.forPermanent(perm, playerId);
            for (TriggeredAbility ability : printedAbilities(gameData, perm.getCard(), EACH_PRECOMBAT_MAIN_PHASE,
                    Zone.BATTLEFIELD, context)) {
                putStepAbility(gameData, ability, perm.getCard(), perm, playerId, "ability", context,
                        entry -> entry.setActivePlayerId(activePlayerId));
            }
        });
    }

    /**
     * Fires triggered abilities on permanents the active player controls at the
     * beginning of each postcombat main phase (e.g. Neheb, the Eternal).
     *
     * @param gameData the current game state to modify
     */
    public void handlePostcombatMainTriggers(GameData gameData) {
        gameData.mainPhasesBegunThisTurn++;
        gameData.postcombatMainPhasesBegunThisTurn++;
        handleMainPhaseTriggers(gameData, YOUR_POSTCOMBAT_MAIN_PHASE);
        if (gameData.mainPhasesBegunThisTurn == 2) {
            handleMainPhaseTriggers(gameData, YOUR_SECOND_MAIN_PHASE);
        }
        if (gameData.planechase != null) {
            planechaseService.step(gameData, YOUR_POSTCOMBAT_MAIN_PHASE);
        }
        beginNextStepTriggerChoice(gameData);
    }

    private void handleMainPhaseTriggers(GameData gameData, StepTrigger trigger) {
        UUID activePlayerId = gameData.activePlayerId;
        List<Permanent> battlefield = gameData.playerBattlefields.get(activePlayerId);
        if (battlefield == null) {
            return;
        }
        for (Permanent perm : battlefield) {
            if (gameQueryService.hasLostAllAbilities(gameData, perm)) {
                continue;
            }
            ConditionContext context = ConditionContext.forPermanent(perm, activePlayerId);
            for (TriggeredAbility ability : printedAbilities(gameData, perm.getCard(), trigger, Zone.BATTLEFIELD,
                    context)) {
                putStepAbility(gameData, ability, perm.getCard(), perm, activePlayerId, "ability", context,
                        entry -> entry.setSourcePermanentSnapshot(new Permanent(perm)));
            }
        }
    }

    /**
     * Scans every battlefield for {@code Triggers.endOfCombat()} abilities and pushes them onto
     * the stack as the end of combat step begins (CR 511.2). Unlike the postcombat-main scan this
     * runs on all players' permanents, because "at end of combat" is not restricted to the
     * controller's own turn. Also collects emblems that trigger at the end of their controller's
     * first combat phase.
     *
     * @param gameData the current game state to modify
     */
    public void handleEndOfCombatTriggers(GameData gameData) {
        collectEmblemStepTriggers(gameData, StepTrigger.Moment.END_OF_COMBAT, ActivePlayer.ANY);
        if (gameData.planechase != null) {
            planechaseService.step(gameData, END_OF_COMBAT);
        }

        List<DelayedEndOfCombatTrigger> delayedTriggers =
                gameData.drainDelayedActions(DelayedEndOfCombatTrigger.class);
        for (DelayedEndOfCombatTrigger delayedTrigger : delayedTriggers) {
            StackEntry delayedEntry = TriggeredAbilityStackService.madeAbility(
                    delayedTrigger.sourceCard(),
                    delayedTrigger.controllerId(),
                    delayedTrigger.sourceCard().getName() + "'s end-of-combat ability",
                    new ArrayList<>(List.of(delayedTrigger.effect())),
                    delayedTrigger.affectedPermanentId(),
                    delayedTrigger.sourcePermanentId()
            );
            delayedEntry.setNonTargeting(delayedTrigger.affectedPermanentId() != null);
            delayedEntry.setTriggeringPermanentId(delayedTrigger.triggeringPermanentId());
            delayedEntry.rememberLastKnownPermanentAttachments(delayedTrigger.affectedPermanentId(),
                    delayedTrigger.affectedPermanentAttachments());
            triggeredAbilityStackService.putDelayed(gameData, delayedEntry);

            gameLogService.append(gameData, GameLog.cardThen(
                    delayedTrigger.sourceCard(), "'s end-of-combat ability triggers."));
            log.info("Game {} - {} delayed end-of-combat trigger pushed onto stack",
                    gameData.id, delayedTrigger.sourceCard().getName());
        }

        gameData.forEachPermanent((playerId, perm) -> {
            if (perm.isFaceDown() || gameQueryService.hasLostPrintedAbilities(gameData, perm)) {
                return;
            }
            ConditionContext context = ConditionContext.forPermanent(perm, playerId);
            for (TriggeredAbility ability : printedAbilities(gameData, perm.getCard(), END_OF_COMBAT,
                    Zone.BATTLEFIELD, context)) {
                putStepAbility(gameData, ability, perm.getCard(), perm, playerId, "ability", context, entry -> {
                    entry.setSourcePermanentSnapshot(new Permanent(perm));
                    entry.setNonTargeting(true);
                });
            }
        });
        beginNextStepTriggerChoice(gameData);
    }

    /**
     * Adds a lore counter to each Saga the active player controls and triggers
     * the appropriate chapter ability (CR 715.3c, 715.2b).
     * Called at the beginning of the active player's precombat main phase.
     */
    private void handleSagaLoreCounters(GameData gameData) {
        UUID activePlayerId = gameData.activePlayerId;
        List<Permanent> battlefield = gameData.playerBattlefields.get(activePlayerId);
        if (battlefield == null) return;

        // Collect Sagas first to avoid ConcurrentModificationException
        List<Permanent> sagas = battlefield.stream()
                .filter(p -> p.getCard().isSaga() && !p.getCard().isBedtimeStory())
                .toList();

        // Each lore counter is placed through the replacement pipeline (Vorinclex doubles or halves it),
        // and every chapter reached by a placed counter triggers in order.
        List<Map.Entry<Permanent, Integer>> loreSteps = new ArrayList<>();
        for (Permanent saga : sagas) {
            int previousLoreCount = saga.getCounterCount(CounterType.LORE);
            int placed = gameQueryService.replaceCounters(gameData, saga, CounterType.LORE, 1, activePlayerId);
            for (int step = 1; step <= placed; step++) {
                loreSteps.add(Map.entry(saga, previousLoreCount + step));
            }
            saga.setCounterCount(CounterType.LORE, previousLoreCount + placed);
        }

        for (Map.Entry<Permanent, Integer> loreStep : loreSteps) {
            Permanent saga = loreStep.getKey();
            Card card = saga.getCard();
            int newLoreCount = loreStep.getValue();
            sagaChapterService.renewLoreCounterPlayPermissions(gameData, saga, activePlayerId);

            gameLogService.append(gameData,
                    GameLog.cardThen(card, " gets a lore counter (" + newLoreCount + ")."));
            log.info("Game {} - {} gets lore counter {}", gameData.id, card.getName(), newLoreCount);
            triggerCollectionService.checkYouPutLoreCounterOnSagaTriggers(gameData, saga, activePlayerId);

            // Each chapter a placed counter reached triggers (CR 715.2b); the stack door chooses its targets.
            sagaChapterService.putChapterAbility(gameData, saga, card, activePlayerId, newLoreCount, false);
        }
        sagaChapterService.beginChapterChoices(gameData);
    }

    /**
     * Returns exiled cards scheduled for the given step from exile to the battlefield
     * under their scheduled controller's control. A return flagged {@code onlyOnControllersTurn}
     * waits for a step of this kind on that controller's own turn ("at the beginning of
     * <em>your</em> next upkeep").
     */
    public void processPendingExileReturns(GameData gameData, TurnStep step) {
        List<PendingExileReturn> matching = gameData.drainDelayedActions(PendingExileReturn.class,
                p -> p.returnStep() == step
                        && gameData.pendingExileReturnEarliestTurns.getOrDefault(p, 0) <= gameData.turnNumber
                        && (!p.onlyOnControllersTurn()
                        || (p.timingControllerId() != null
                        ? p.timingControllerId() : p.controllerId()).equals(gameData.activePlayerId)));
        if (matching.isEmpty()) {
            return;
        }

        for (PendingExileReturn pending : matching) {
            gameData.pendingExileReturnEarliestTurns.remove(pending);
            Card triggerSource = pending.triggerSourceCard() == null ? pending.card() : pending.triggerSourceCard();
            UUID triggerController = pending.triggerControllerId() == null
                    ? pending.controllerId() : pending.triggerControllerId();
            StackEntry entry = TriggeredAbilityStackService.madeAbility(triggerSource,
                    triggerController, triggerSource.getName() + "'s delayed return ability",
                    new ArrayList<>(List.of(new com.github.laxika.magicalvibes.model.effect.ResolvePendingExileReturnEffect(pending))));
            entry.setNonTargeting(true);
            entry.setTriggeringCardId(pending.card().getId());
            Long exileVersionWhenQueued = gameData.pendingExileReturnExileVersions.remove(pending);
            entry.setTriggeringCardExileEntryVersion(exileVersionWhenQueued != null
                    ? exileVersionWhenQueued
                    : gameData.exileEntryVersions.getOrDefault(pending.card().getId(), -1L));
            triggeredAbilityStackService.putDelayed(gameData, entry);
        }
    }

    /** Applies the scheduled return and obtains an attack choice when necessary. */
    public void resolvePendingExileReturn(GameData gameData, PendingExileReturn pending) {
        processPendingExileReturns(gameData, List.of(pending));
    }

    public void resolvePendingExileReturnAttackTarget(GameData gameData, UUID attackTargetId,
                                                       PermanentChoiceContext.ExileReturnAttackTarget context) {
        Set<UUID> validTargets = validExileReturnAttackTargets(gameData, context.pending().controllerId());
        UUID resolvedTarget = validTargets.contains(attackTargetId)
                ? attackTargetId
                : validTargets.stream().findFirst().orElse(null);
        returnPendingExileCard(gameData, context.pending(), resolvedTarget);
        processPendingExileReturns(gameData, context.remaining());
    }

    private void processPendingExileReturns(GameData gameData, List<PendingExileReturn> pendingReturns) {
        for (int i = 0; i < pendingReturns.size(); i++) {
            PendingExileReturn pending = pendingReturns.get(i);
            if (pending.returnAttacking()) {
                Set<UUID> validTargets = validExileReturnAttackTargets(gameData, pending.controllerId());
                if (validTargets.size() > 1) {
                    List<PendingExileReturn> remaining = pendingReturns.subList(i + 1, pendingReturns.size());
                    gameData.interaction.setPermanentChoiceContext(
                            new PermanentChoiceContext.ExileReturnAttackTarget(pending, remaining));
                    playerInputService.beginAnyTargetChoice(
                            gameData,
                            pending.controllerId(),
                            validTargets.stream().filter(id -> !gameData.playerIds.contains(id)).toList(),
                            validTargets.stream().filter(gameData.playerIds::contains).toList(),
                            "Choose an opponent or opposing planeswalker for " + pending.card().getName()
                                    + " to attack.");
                    return;
                }
                returnPendingExileCard(gameData, pending, validTargets.stream().findFirst().orElse(null));
            } else {
                returnPendingExileCard(gameData, pending, null);
            }
        }
    }

    private Set<UUID> validExileReturnAttackTargets(GameData gameData, UUID controllerId) {
        Set<UUID> validTargets = new LinkedHashSet<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (controllerId.equals(playerId)) {
                continue;
            }
            validTargets.add(playerId);
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) {
                continue;
            }
            for (Permanent permanent : battlefield) {
                if (permanent.getCard().hasType(CardType.PLANESWALKER)) {
                    validTargets.add(permanent.getId());
                }
            }
        }
        return validTargets;
    }

    /**
     * An Aura returning from exile without an attachment chosen by its effect is attached as its owner
     * chooses (CR 303.4f). With no legal object or player to enchant, it remains in exile (CR 303.4g).
     */
    private void beginAuraReturnChoice(GameData gameData, Card auraCard, UUID ownerId) {
        List<UUID> permanentTargets = new ArrayList<>();
        gameData.forEachPermanent((ignored, candidate) -> {
            if (auraAttachmentService.canEnchant(gameData, auraCard, ownerId, candidate)) {
                permanentTargets.add(candidate.getId());
            }
        });
        List<UUID> playerTargets = auraCard.isEnchantPlayer()
                ? gameData.orderedPlayerIds.stream()
                .filter(playerId -> auraAttachmentService.canEnchantPlayer(gameData, auraCard, ownerId, playerId))
                .toList()
                : List.of();
        if (permanentTargets.isEmpty() && playerTargets.isEmpty()) {
            log.info("Game {} - {} has no legal object to enchant and stays in exile", gameData.id,
                    auraCard.getName());
            return;
        }
        if (!gameData.removeFromExile(auraCard.getId())) {
            return;
        }
        gameData.interaction.setPendingAuraCard(auraCard);
        if (playerTargets.isEmpty()) {
            playerInputService.beginPermanentChoice(gameData, ownerId, permanentTargets,
                    "Choose a permanent for " + auraCard.getName() + " to enchant.");
        } else {
            playerInputService.beginAnyTargetChoice(gameData, ownerId, permanentTargets, playerTargets,
                    "Choose a permanent or player for " + auraCard.getName() + " to enchant.");
        }
    }

    private void returnPendingExileCard(GameData gameData, PendingExileReturn pending, UUID attackTargetId) {
        UUID controllerId = pending.controllerId();
        if (pending.card().isAura() && !pending.card().hasType(CardType.CREATURE)
                && pending.cardsToAttachToPrimary().isEmpty() && pending.additionalCards().isEmpty()) {
            beginAuraReturnChoice(gameData, pending.card(), controllerId);
            return;
        }

        List<Card> cards = new ArrayList<>();
        cards.add(pending.card());
        cards.addAll(pending.additionalCards());

        if (pending.cardsToAttachToPrimary().isEmpty()) {
            // CR 303.4f/303.4g: an Aura returning without an attachment chosen by the effect picks a host
            // among the permanents already on the battlefield (not those returning with it) and stays
            // in exile when none can be enchanted.
            List<BattlefieldEntryCard> returningAuras = new ArrayList<>();
            for (Card card : cards) {
                ExiledCardEntry exiled = gameData.findExiledCard(card.getId());
                if (exiled != null && card.isAura() && !card.isEnchantZone() && !card.hasType(CardType.CREATURE)) {
                    returningAuras.add(new BattlefieldEntryCard(
                            pending.returnControllerByCardId().getOrDefault(card.getId(), controllerId),
                            exiled.ownerId(), card, Zone.EXILE, null).withTapped(pending.returnTapped()));
                }
            }
            if (!returningAuras.isEmpty()) {
                List<UUID> auraIds = returningAuras.stream().map(entry -> entry.card().getId()).toList();
                cards.removeIf(card -> auraIds.contains(card.getId()));
                battlefieldEntryBatchSupport.begin(gameData, returningAuras);
            }
        }

        List<Card> returningCards;
        if (pending.cardsToAttachToPrimary().isEmpty()) {
            returningCards = cards.stream()
                    .filter(card -> gameData.removeFromExile(card.getId()))
                    .toList();
        } else {
            if (!gameData.removeFromExile(pending.card().getId())) {
                log.info("Game {} - delayed attached return skipped because its primary card is no longer in exile",
                        gameData.id);
                return;
            }
            List<Card> returned = new ArrayList<>();
            returned.add(pending.card());
            // Auras attached to the primary card can only enter attached to a creature; when the
            // primary card returns as a noncreature they stay in exile.
            if (pending.card().hasType(CardType.CREATURE)) {
                for (Card additional : pending.additionalCards()) {
                    if (gameData.removeFromExile(additional.getId())) {
                        returned.add(additional);
                    }
                }
            }
            returningCards = returned;
        }
        if (returningCards.isEmpty()) {
            log.info("Game {} - delayed return skipped because its cards are no longer in exile", gameData.id);
            return;
        }

        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        List<Permanent> simultaneouslyEntered = new ArrayList<>();
        List<Permanent> returnedPermanents = new ArrayList<>();
        List<UUID> returnedControllerIds = new ArrayList<>();
        Permanent primaryPermanent = null;
        for (Card card : returningCards) {
            Permanent perm = new Permanent(card);
            boolean returningAttachment = pending.cardsToAttachToPrimary().contains(card.getId());
            if ((pending.returnTapped() && !returningAttachment)
                    || (pending.returnLandsTapped() && card.hasType(CardType.LAND))) {
                perm.tap();
            }
            if (pending.returnAttacking() && attackTargetId != null && !returningAttachment) {
                perm.setAttacking(true);
                perm.setAttackTarget(attackTargetId);
            }
            if (pending.returnCantBeBlockedUntilEndOfCombat()
                    && card.getId().equals(pending.card().getId())) {
                gameData.addFloatingEffect(new FloatingContinuousEffect(
                        UUID.randomUUID(), card.getName(), null, controllerId,
                        new GrantEffectEffect(new CantBeBlockedEffect(), GrantScope.TARGET),
                        perm.getId(), null, null, EffectDuration.UNTIL_END_OF_COMBAT, 0));
            }
            boolean isCreature = card.hasType(CardType.CREATURE)
                    || (pending.plusOnePlusOneCountersOnlyOnCreatures() && gameQueryService.isCreature(
                    gameData, perm, pending.returnControllerByCardId().getOrDefault(card.getId(), controllerId)));
            if (pending.plusOnePlusOneCounters() > 0
                    && (!pending.plusOnePlusOneCountersOnlyOnCreatures() || isCreature)
                    && !gameQueryService.cantHavePlusOnePlusOneCounters(gameData, perm, controllerId)) {
                int counters = gameQueryService.doublePlusOnePlusOneCounters(
                        gameData, perm, controllerId, pending.plusOnePlusOneCounters());
                if (counters > 0) {
                    perm.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, counters);
                }
            }
            if (card.hasType(CardType.PLANESWALKER) && card.getLoyalty() != null) {
                int loyalty = gameQueryService.replaceCounters(gameData, perm, controllerId, CounterType.LOYALTY,
                        card.getLoyalty() + pending.loyaltyCountersOnPlaneswalkers());
                perm.setCounterCount(CounterType.LOYALTY, loyalty);
            }
            if (pending.counterTypeOnReturn() != null && pending.counterAmountOnReturn() > 0) {
                int counters = gameQueryService.replaceCounters(gameData, perm, controllerId,
                        pending.counterTypeOnReturn(), pending.counterAmountOnReturn(), controllerId);
                if (counters > 0) {
                    perm.setCounterCount(pending.counterTypeOnReturn(), counters);
                }
            }
            for (Map.Entry<CounterType, Integer> counter : pending.countersOnReturn().entrySet()) {
                if (counter.getValue() <= 0) {
                    continue;
                }
                int counters = gameQueryService.replaceCounters(
                        gameData, perm, controllerId, counter.getKey(), counter.getValue(), controllerId);
                if (counters > 0) {
                    perm.setCounterCount(counter.getKey(), counters);
                }
            }
            perm.setEnteredFromExile(true);
            if (pending.grantHaste()) {
                perm.getGrantedKeywords().add(Keyword.HASTE);
            }
            if (card.getId().equals(pending.card().getId())) {
                primaryPermanent = perm;
            }
            boolean attachToPrimary = primaryPermanent != null
                    && pending.cardsToAttachToPrimary().contains(card.getId());
            UUID cardControllerId = attachToPrimary && card.getOwnerId() != null
                    ? card.getOwnerId()
                    : pending.returnControllerByCardId().getOrDefault(card.getId(), controllerId);
            if (attachToPrimary) {
                perm.setAttachedTo(primaryPermanent.getId());
            }
            var cardTypeChoice = returningCards.size() == 1
                    ? card.getStaticEffects().stream()
                    .filter(com.github.laxika.magicalvibes.model.effect.ChooseCardTypeOnEnterEffect.class::isInstance)
                    .map(com.github.laxika.magicalvibes.model.effect.ChooseCardTypeOnEnterEffect.class::cast)
                    .filter(choice -> !choice.lookAtOpponentHand())
                    .findFirst().orElse(null)
                    : null;
            if (cardTypeChoice != null) {
                // "As this enters, choose a card type" is made before the permanent enters; the choice
                // handler puts the prepared permanent onto the battlefield (Stenn, Paranoid Partisan).
                playerInputService.beginCardTypeOnEnterChoice(
                        gameData, cardControllerId, card, cardTypeChoice.excludedTypes(), perm);
                return;
            }
            battlefieldEntryService.putPermanentOntoBattlefield(
                    gameData, cardControllerId, perm, enterTappedTypes, simultaneouslyEntered);
            simultaneouslyEntered.add(perm);
            returnedPermanents.add(perm);
            returnedControllerIds.add(cardControllerId);
            String playerName = gameData.playerIdToName.get(cardControllerId);
            String attackText = pending.returnAttacking() && attackTargetId != null
                    ? " tapped and attacking" : "";
            gameLogService.append(gameData,
                    GameLog.cardThen(card, " returns to the battlefield" + attackText + " under "
                            + playerName + "'s control."));
            log.info("Game {} - {} returns from exile for {}", gameData.id, card.getName(), playerName);
        }
        for (int i = 0; i < returningCards.size(); i++) {
            Card card = returningCards.get(i);
            UUID cardControllerId = returnedControllerIds.get(i);
            battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, cardControllerId, card, null, false);
        }
        if (primaryPermanent != null && !pending.cardsToAttachToPrimary().isEmpty()) {
            creatureControlService.recomputeControl(gameData, primaryPermanent);
        }

        if (pending.discardControllerCardsEqualToReturnedToughness()
                && pending.followUpSourceCard() != null
                && pending.timingControllerId() != null) {
            int returnedIndex = -1;
            for (int i = 0; i < returningCards.size(); i++) {
                if (returningCards.get(i).getId().equals(pending.card().getId())) {
                    returnedIndex = i;
                    break;
                }
            }
            if (returnedIndex >= 0) {
                Permanent returned = returnedPermanents.get(returnedIndex);
                if (gameQueryService.isCreature(gameData, returned)) {
                    int discardCount = Math.max(0, gameQueryService.getEffectiveToughness(gameData, returned));
                    if (discardCount > 0) {
                        StackEntry discardEntry = StackEntry.carrier(
                                pending.followUpSourceCard(),
                                pending.timingControllerId(),
                                pending.followUpSourceCard().getName() + "'s delayed trigger — discard "
                                        + discardCount + " cards",
                                new ArrayList<>(List.of(new DiscardEffect(
                                        discardCount, DiscardRecipient.CONTROLLER))));
                        discardEntry.setNonTargeting(true);
                        gameLogService.append(gameData,
                                GameLog.cardThen(pending.followUpSourceCard(), "'s delayed trigger discards cards."));
                        effectResolutionService.resolveEffects(gameData, discardEntry);
                    }
                }
            }
        }
    }

    /**
     * Processes end-step triggers: exiles pending tokens (e.g. Mimic Vat),
     * then scans battlefields for
     * {@code Triggers.endStep(ActivePlayer.ANY)} and {@code Triggers.endStep(ActivePlayer.YOU)}
     * abilities.
     *
     * @param gameData the current game state to modify
     */
    /** Queues the delayed self-return abilities matching {@code filter}. */
    private void resolveDelayedSelfReturns(GameData gameData,
                                           Predicate<DelayedGraveyardToBattlefieldSelfReturn> filter) {
        if (!gameData.hasDelayedAction(DelayedGraveyardToBattlefieldSelfReturn.class, filter)) {
            return;
        }
        List<DelayedGraveyardToBattlefieldSelfReturn> pendingReturns =
                gameData.drainDelayedActions(DelayedGraveyardToBattlefieldSelfReturn.class, filter);
        for (DelayedGraveyardToBattlefieldSelfReturn pending : pendingReturns) {
            List<Card> graveyard = gameData.playerGraveyards.getOrDefault(pending.ownerId(), List.of());
            Card cardToReturn = null;
            for (Card card : graveyard) {
                if (card.getId().equals(pending.cardId())) {
                    cardToReturn = card;
                    break;
                }
            }
            if (cardToReturn == null) {
                // The delayed trigger exists independently of the card (CR 603.7); a card that left
                // the graveyard is a new object, so the trigger resolves without returning anything.
                var exiled = gameData.findExiledCard(pending.cardId());
                if (exiled == null) {
                    log.info("Game {} - Delayed graveyard return for card {} skipped (no longer in graveyard)",
                            gameData.id, pending.cardId());
                    continue;
                }
                StackEntry lostEntry = TriggeredAbilityStackService.madeAbility(
                        exiled.card(),
                        pending.upkeepPlayerId(),
                        exiled.card().getName() + "'s delayed return ability",
                        new ArrayList<>(List.of(new ReturnTriggeringCardFromGraveyardToBattlefieldEffect(
                                pending.tapped(), false, pending.counterType(), pending.counterAmount()))));
                lostEntry.setTriggeringCardId(exiled.card().getId());
                lostEntry.setTriggeringCardGraveyardEntryVersion(-1);
                lostEntry.setNonTargeting(true);
                triggeredAbilityStackService.putDelayed(gameData, lostEntry);
                gameLogService.append(gameData,
                        GameLog.cardThen(exiled.card(), "'s delayed return ability triggers."));
                continue;
            }
            if (pending.graveyardEntryVersion() >= 0
                    && gameData.graveyardEntryVersion(pending.cardId()) != pending.graveyardEntryVersion()) {
                log.info("Game {} - Delayed graveyard return for card {} skipped (no longer in graveyard)",
                        gameData.id, pending.cardId());
                continue;
            }
            StackEntry entry = TriggeredAbilityStackService.madeAbility(
                    cardToReturn,
                    pending.upkeepPlayerId(),
                    cardToReturn.getName() + "'s delayed return ability",
                    new ArrayList<>(List.of(new ReturnTriggeringCardFromGraveyardToBattlefieldEffect(
                            pending.tapped(), false, pending.counterType(), pending.counterAmount()))));
            entry.setTriggeringCardId(cardToReturn.getId());
            entry.setTriggeringCardGraveyardEntryVersion(
                    pending.graveyardEntryVersion() >= 0 ? pending.graveyardEntryVersion()
                            : gameData.graveyardEntryVersion(cardToReturn.getId()));
            entry.setNonTargeting(true);
            triggeredAbilityStackService.putDelayed(gameData, entry);
            gameLogService.append(gameData,
                    GameLog.cardThen(cardToReturn, "'s delayed return ability triggers."));
        }
    }

    /** Puts the delayed simultaneous graveyard returns scheduled for a player's next upkeep on the stack. */
    private void resolveDelayedGraveyardCardsUnderControlAtUpkeep(GameData gameData) {
        if (!gameData.hasDelayedAction(DelayedGraveyardCardsToBattlefieldUnderControl.class,
                action -> action.controllerId().equals(gameData.activePlayerId))) {
            return;
        }

        List<DelayedGraveyardCardsToBattlefieldUnderControl> pendingReturns =
                gameData.drainDelayedActions(DelayedGraveyardCardsToBattlefieldUnderControl.class,
                        action -> action.controllerId().equals(gameData.activePlayerId));
        for (DelayedGraveyardCardsToBattlefieldUnderControl pending : pendingReturns) {
            StackEntry entry = TriggeredAbilityStackService.madeAbility(pending.sourceCard(),
                    pending.controllerId(), pending.sourceCard().getName() + "'s delayed ability",
                    new ArrayList<>(List.of(new ReturnGraveyardCardsToBattlefieldUnderControlEffect(
                            pending.cardIds(), pending.graveyardEntryVersions()))));
            entry.setNonTargeting(true);
            triggeredAbilityStackService.putDelayed(gameData, entry);
            gameLogService.append(gameData,
                    GameLog.cardThen(pending.sourceCard(), "'s delayed return ability triggers."));
        }
    }

    public void handleEndStepTriggers(GameData gameData) {
        for (UUID cardId : Set.copyOf(gameData.exilePlayPermissionsExpireAtEndStep)) {
            Integer expiryTurn = gameData.exilePlayPermissionsExpireAtTurnEnd.get(cardId);
            if (expiryTurn != null && expiryTurn <= gameData.turnNumber
                    && !gameData.exilePlayPermissionsAwaitNextTurnOfPlayer.containsKey(cardId)) {
                gameData.exilePlayPermissions.remove(cardId);
                gameData.exilePlayPermissionsExpireAtTurnEnd.remove(cardId);
                gameData.exilePlayPermissionsExpireAtEndStep.remove(cardId);
                gameData.exilePlayWithoutPayingManaCost.remove(cardId);
                gameData.exilePlayAnyManaType.remove(cardId);
                gameData.exilePlayPermissionSpellFilters.remove(cardId);
            }
        }
        if (gameData.activePlayerId.equals(gameData.monarchPlayerId)) {
            // "At the beginning of the monarch's end step, that player draws a card" (CR 726.2).
            UUID monarchId = gameData.monarchPlayerId;
            triggeredAbilityStackService.putInherent(gameData, monarchId, "The monarch draws a card",
                    List.of(new com.github.laxika.magicalvibes.model.effect.DrawCardForMonarchEndStepEffect()),
                    entry -> {
                        entry.setTargetId(monarchId);
                        entry.setNonTargeting(true);
                    });
        }
        if (gameData.planechase != null) planechaseService.step(gameData,
                EACH_END_STEP, YOUR_END_STEP);
        expireNextEndStepTemporaryCopies(gameData);
        handleBedtimeStoryLoreCounters(gameData);
        for (var delayed : gameData.drainDelayedActions(
                com.github.laxika.magicalvibes.model.action.DelayedEndStepTrigger.class)) {
            StackEntry entry = TriggeredAbilityStackService.madeAbility(delayed.sourceCard(),
                    delayed.controllerId(), delayed.sourceCard().getName() + "'s delayed ability",
                    new ArrayList<>(List.of(delayed.effect())), delayed.affectedPermanentId(),
                    delayed.sourcePermanentId());
            entry.setNonTargeting(true);
            // "That creature" of the delayed ability is its event's object on every route: an ability that also
            // chooses a target (Swooping Pteranodon's "target land deals 3 damage to that creature") reads it there.
            entry.setTriggeringPermanentId(delayed.affectedPermanentId());
            if (delayed.targetGroups().isEmpty()) {
                entry.setDeclaredTargetIds(delayed.affectedPermanentIds());
                triggeredAbilityStackService.putDelayed(gameData, entry);
            } else {
                // Its targets, the groups its runtime card declares, are chosen as it goes on the stack (CR 603.3d).
                triggeredAbilityStackService.putMade(gameData, entry);
            }
        }
        for (var delayed : gameData.drainDelayedActions(DelayedEndStepIfStillInHandTrigger.class)) {
            List<Card> hand = gameData.playerHands.getOrDefault(delayed.controllerId(), List.of());
            boolean stillSameHandEntry = delayed.handEntryVersion()
                    == gameData.handEntryVersions.getOrDefault(delayed.cardId(), 0L)
                    && hand.stream().anyMatch(card -> card.getId().equals(delayed.cardId()));
            if (!stillSameHandEntry) {
                continue;
            }
            StackEntry entry = TriggeredAbilityStackService.madeAbility(
                    delayed.sourceCard(),
                    delayed.controllerId(),
                    delayed.sourceCard().getName() + "'s delayed ability",
                    new ArrayList<>(List.of(delayed.effect())),
                    null,
                    delayed.sourcePermanentId());
            entry.setNonTargeting(true);
            triggeredAbilityStackService.putDelayed(gameData, entry);
        }
        for (var delayed : gameData.drainDelayedActions(DelayedStillExiledCardsEndStepTrigger.class)) {
            boolean anyStillExiled = delayed.cardIds().stream()
                    .anyMatch(cardId -> gameData.findExiledCard(cardId) != null);
            if (!anyStillExiled) {
                continue;
            }
            StackEntry entry = TriggeredAbilityStackService.madeAbility(
                    delayed.sourceCard(),
                    delayed.controllerId(),
                    delayed.sourceCard().getName() + "'s delayed ability",
                    new ArrayList<>(List.of(delayed.effect())),
                    null,
                    delayed.sourcePermanentId());
            entry.setNonTargeting(true);
            triggeredAbilityStackService.putDelayed(gameData, entry);
        }
        // "At the beginning of your next end step" (Foggy Swamp Visions): only the controller's end step fires.
        for (var delayed : gameData.drainDelayedActions(
                com.github.laxika.magicalvibes.model.action.DelayedControllerEndStepTrigger.class,
                pending -> pending.controllerId().equals(gameData.activePlayerId))) {
            StackEntry entry = TriggeredAbilityStackService.madeAbility(delayed.sourceCard(),
                    delayed.controllerId(), delayed.sourceCard().getName() + "'s delayed ability",
                    new ArrayList<>(List.of(delayed.effect())), null, delayed.sourcePermanentId());
            entry.setNonTargeting(true);
            triggeredAbilityStackService.putDelayed(gameData, entry);
        }
        // "At the beginning of your next end step" (Desert Warfare): only the scheduling player's end
        // step fires it, and the return goes on the stack like any other triggered ability.
        for (var delayed : gameData.drainDelayedActions(
                com.github.laxika.magicalvibes.model.action.DelayedControllerEndStepGraveyardReturn.class,
                pending -> pending.controllerId().equals(gameData.activePlayerId))) {
            Card cardToReturn = gameQueryService.findCardInGraveyardById(gameData, delayed.cardId());
            if (cardToReturn == null
                    || gameData.graveyardEntryVersion(delayed.cardId()) != delayed.graveyardEntryVersion()) {
                log.info("Game {} - Delayed end-step return for card {} skipped (no longer in the scheduled graveyard entry)",
                        gameData.id, delayed.cardId());
                continue;
            }
            StackEntry entry = TriggeredAbilityStackService.madeAbility(delayed.sourceCard(),
                    delayed.controllerId(), delayed.sourceCard().getName() + "'s delayed ability",
                    new ArrayList<>(List.of(new com.github.laxika.magicalvibes.model.effect.ReturnTriggeringCardFromGraveyardToBattlefieldEffect(
                            false, true))));
            entry.setTriggeringCardId(cardToReturn.getId());
            entry.setTriggeringCardGraveyardEntryVersion(delayed.graveyardEntryVersion());
            entry.setNonTargeting(true);
            triggeredAbilityStackService.putDelayed(gameData, entry);
            gameLogService.append(gameData, GameLog.cardThen(cardToReturn, "'s delayed return ability triggers."));
        }
        collectEmblemStepTriggers(gameData, StepTrigger.Moment.END_STEP, ActivePlayer.YOU);
        collectEmblemStepTriggers(gameData, StepTrigger.Moment.END_STEP, ActivePlayer.ANY);

        if (gameData.hasDelayedAction(ReturnExiledCardAtNextEndStepUnlessPays.class)) {
            List<ReturnExiledCardAtNextEndStepUnlessPays> pendingReturns = gameData.drainDelayedActions(
                    ReturnExiledCardAtNextEndStepUnlessPays.class);
            for (ReturnExiledCardAtNextEndStepUnlessPays action : pendingReturns) {
                ForcedCostOrElseEffect payOrReturn = new ForcedCostOrElseEffect(
                        new PayManaCost("{3}{B}"),
                        new ArrayList<>(List.of(new ReturnExiledCardToBattlefieldUnderOwnerControlEffect(
                                action.cardId()))),
                        true);
                StackEntry entry = TriggeredAbilityStackService.madeAbility(
                        action.sourceCard(),
                        action.controllerId(),
                        action.sourceCard().getName() + "'s delayed ability",
                        new ArrayList<>(List.of(payOrReturn)),
                        null,
                        action.sourcePermanentId());
                entry.setNonTargeting(true);
                triggeredAbilityStackService.putDelayed(gameData, entry);
                gameLogService.append(gameData, GameLog.cardThen(action.sourceCard(),
                        "'s delayed ability triggers — return the exiled card unless its controller pays {3}{B}."));
                log.info("Game {} - {} delayed return trigger pushed onto stack",
                        gameData.id, action.sourceCard().getName());
            }
        }

        if (gameData.hasDelayedAction(PutCounterOnPermanentAtNextEndStep.class)) {
            List<PutCounterOnPermanentAtNextEndStep> pendingCounters =
                    gameData.drainDelayedActions(PutCounterOnPermanentAtNextEndStep.class);
            for (PutCounterOnPermanentAtNextEndStep action : pendingCounters) {
                if (gameQueryService.findPermanentById(gameData, action.permanentId()) == null) {
                    continue;
                }
                StackEntry entry = TriggeredAbilityStackService.madeAbility(action.sourceCard(), action.controllerId(),
                        action.sourceCard().getName() + "'s delayed ability",
                        new ArrayList<>(List.of(action.effect())),
                        action.permanentId(), (UUID) null);
                entry.setNonTargeting(true);
                triggeredAbilityStackService.putDelayed(gameData, entry);
                gameLogService.append(gameData, GameLog.cardThen(action.sourceCard(),
                        "'s delayed ability triggers."));
                log.info("Game {} - {} delayed end-step counter trigger pushed onto stack",
                        gameData.id, action.sourceCard().getName());
            }
        }

        if (gameData.hasDelayedAction(DrawCardsAtNextEndStep.class)) {
            // A draw registered by the same spell as a pending exile-return ("return those cards to
            // your hand, then draw a card", Ignorant Bliss) is part of that single delayed trigger and
            // is added to it below.
            List<DrawCardsAtNextEndStep> pendingDraws = gameData.drainDelayedActions(
                    DrawCardsAtNextEndStep.class,
                    draw -> gameData.getDelayedActions(ReturnExiledCardToHandAtNextEndStep.class).stream()
                            .noneMatch(ret -> ret.sourceCard() == draw.sourceCard()
                                    && ret.controllerId().equals(draw.controllerId())
                                    && draw.drawingPlayerId().equals(draw.controllerId())));
            for (DrawCardsAtNextEndStep pending : pendingDraws) {
                boolean drawerIsController = pending.drawingPlayerId().equals(pending.controllerId());
                StackEntry drawEntry = TriggeredAbilityStackService.madeAbility(
                        pending.sourceCard(),
                        pending.controllerId(),
                        pending.sourceCard().getName() + "'s delayed ability",
                        new ArrayList<>(List.of(drawerIsController
                                ? new DrawCardEffect(pending.count())
                                : new com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect(
                                        pending.count())))
                );
                if (!drawerIsController) {
                    drawEntry.setTargetId(pending.drawingPlayerId());
                    drawEntry.setNonTargeting(true);
                }
                triggeredAbilityStackService.putDelayed(gameData, drawEntry);
                gameLogService.append(gameData, GameLog.cardThen(pending.sourceCard(),
                        "'s delayed ability triggers — draw " + pending.count() + " card(s)."));
                log.info("Game {} - {} delayed draw trigger pushed onto stack for {} card(s)",
                        gameData.id, pending.sourceCard().getName(), pending.count());
            }
        }

        if (gameData.hasDelayedAction(DamageForCardsStillExiledAtNextEndStep.class)) {
            List<DamageForCardsStillExiledAtNextEndStep> pendingDamage = gameData.drainDelayedActions(
                    DamageForCardsStillExiledAtNextEndStep.class,
                    action -> action.controllerId().equals(gameData.activePlayerId));
            for (DamageForCardsStillExiledAtNextEndStep action : pendingDamage) {
                var damage = new com.github.laxika.magicalvibes.model.amount.Scaled(
                        new com.github.laxika.magicalvibes.model.amount.CardsInExile(
                                new com.github.laxika.magicalvibes.model.filter.CardIdSetPredicate(
                                        java.util.Set.copyOf(action.cardIds())),
                                com.github.laxika.magicalvibes.model.amount.CountScope.ANY_PLAYER),
                        action.damagePerCard());
                StackEntry entry = TriggeredAbilityStackService.madeAbility(
                        action.sourceCard(),
                        action.controllerId(),
                        action.sourceCard().getName() + "'s delayed trigger",
                        new ArrayList<>(List.of(new DealDamageToPlayersEffect(
                                damage, DamageRecipient.EACH_OPPONENT))),
                        null,
                        action.sourcePermanentId());
                entry.setNonTargeting(true);
                triggeredAbilityStackService.putDelayed(gameData, entry);
                gameLogService.append(gameData, GameLog.cardThen(action.sourceCard(),
                        "'s delayed damage trigger is put onto the stack."));
                log.info("Game {} - {} delayed damage trigger pushed onto stack",
                        gameData.id, action.sourceCard().getName());
            }
        }

        if (gameData.hasDelayedAction(UnattachEquipmentAtNextEndStep.class)) {
            List<UnattachEquipmentAtNextEndStep> pendingUnattaches =
                    gameData.drainDelayedActions(UnattachEquipmentAtNextEndStep.class);
            for (UnattachEquipmentAtNextEndStep pending : pendingUnattaches) {
                StackEntry entry = TriggeredAbilityStackService.madeAbility(
                        pending.sourceCard(),
                        pending.controllerId(),
                        pending.sourceCard().getName() + "'s delayed ability",
                        new ArrayList<>(List.of(new UnattachEquipmentEffect(pending.equipmentId()))));
                entry.setNonTargeting(true);
                triggeredAbilityStackService.putDelayed(gameData, entry);
                gameLogService.append(gameData,
                        GameLog.cardThen(pending.sourceCard(), "'s delayed ability triggers to unattach the Equipment."));
            }
        }

        if (gameData.hasDelayedAction(SacrificeSelfAtNextEndStepTrigger.class,
                action -> isSacrificeAtNextEndStepDue(gameData, action))) {
            List<SacrificeSelfAtNextEndStepTrigger> pendingSacrifices =
                    gameData.drainDelayedActions(SacrificeSelfAtNextEndStepTrigger.class,
                            action -> isSacrificeAtNextEndStepDue(gameData, action));
            for (SacrificeSelfAtNextEndStepTrigger pending : pendingSacrifices) {
                if (gameQueryService.findPermanentById(gameData, pending.permanentId()) == null) {
                    continue;
                }
                UUID triggerControllerId = pending.currentControllersEndStepOnly()
                        ? gameQueryService.findPermanentController(gameData, pending.permanentId())
                        : pending.controllerId();
                StackEntry entry = TriggeredAbilityStackService.madeAbility(
                        pending.sourceCard(),
                        triggerControllerId,
                        pending.sourceCard().getName() + "'s delayed ability",
                        new ArrayList<>(List.of(new SacrificeSelfEffect(
                                pending.currentControllerSacrifices()))),
                        null,
                        pending.permanentId());
                entry.setNonTargeting(true);
                triggeredAbilityStackService.putDelayed(gameData, entry);
                gameLogService.append(gameData, GameLog.cardThen(
                        pending.sourceCard(), "'s delayed sacrifice ability triggers."));
            }
        }

        Predicate<SacrificePermanentAtControllerEndStepUnlessPays> sacrificeFiresAtThisEndStep =
                action -> !action.controllerEndStepOnly() || action.controllerId().equals(gameData.activePlayerId);
        if (gameData.hasDelayedAction(SacrificePermanentAtControllerEndStepUnlessPays.class,
                sacrificeFiresAtThisEndStep)) {
            List<SacrificePermanentAtControllerEndStepUnlessPays> pendingSacrifices =
                    gameData.drainDelayedActions(SacrificePermanentAtControllerEndStepUnlessPays.class,
                            sacrificeFiresAtThisEndStep);
            for (SacrificePermanentAtControllerEndStepUnlessPays action : pendingSacrifices) {
                if (gameQueryService.findPermanentById(gameData, action.permanentId()) == null) {
                    continue;
                }
                ForcedCostOrElseEffect payOrSacrifice = new ForcedCostOrElseEffect(
                        action.cost(),
                        new ArrayList<>(List.of(new SacrificeSelfEffect())),
                        true);
                StackEntry entry = TriggeredAbilityStackService.madeAbility(
                        action.sourceCard(),
                        action.controllerId(),
                        action.sourceCard().getName() + "'s delayed ability",
                        new ArrayList<>(List.of(payOrSacrifice)),
                        null,
                        action.permanentId());
                entry.setNonTargeting(true);
                triggeredAbilityStackService.putDelayed(gameData, entry);
                gameLogService.append(gameData, GameLog.cardThen(action.sourceCard(),
                        "'s delayed ability triggers â€” sacrifice the token unless you pay " + action.manaCost() + "."));
            }
        }

        // Memory Jar: each player discards their hand and returns the cards exiled by its ability.
        if (gameData.hasDelayedAction(EachPlayerHandExileReturnAtNextEndStep.class)) {
            List<EachPlayerHandExileReturnAtNextEndStep> pending = gameData.drainDelayedActions(
                    EachPlayerHandExileReturnAtNextEndStep.class);
            for (EachPlayerHandExileReturnAtNextEndStep action : pending) {
                StackEntry entry = TriggeredAbilityStackService.madeAbility(
                        action.sourceCard(),
                        action.controllerId(),
                        action.sourceCard().getName() + "'s delayed trigger",
                        new ArrayList<>(List.of(new DiscardEachPlayerHandAndReturnExiledCardsEffect(
                                action.players()))));
                entry.setNonTargeting(true);
                triggeredAbilityStackService.putDelayed(gameData, entry);
                gameLogService.append(gameData, GameLog.cardThen(action.sourceCard(),
                        "'s delayed trigger returns the exiled hands at the next end step."));
            }
        }

        if (gameData.hasDelayedAction(TargetPlayerHandExileReturnAtNextTurnEndStep.class,
                action -> gameData.turnNumber > action.registeredTurnNumber()
                        && action.playerId().equals(gameData.activePlayerId))) {
            List<TargetPlayerHandExileReturnAtNextTurnEndStep> pending = gameData.drainDelayedActions(
                    TargetPlayerHandExileReturnAtNextTurnEndStep.class,
                    action -> gameData.turnNumber > action.registeredTurnNumber()
                            && action.playerId().equals(gameData.activePlayerId));
            for (TargetPlayerHandExileReturnAtNextTurnEndStep action : pending) {
                StackEntry entry = TriggeredAbilityStackService.madeAbility(
                        action.sourceCard(),
                        action.controllerId(),
                        action.sourceCard().getName() + "'s delayed trigger",
                        new ArrayList<>(List.of(new ReturnExiledCardsToTargetPlayerHandEffect(
                                action.playerId(), action.cardIds()))));
                entry.setNonTargeting(true);
                triggeredAbilityStackService.putDelayed(gameData, entry);
                gameLogService.append(gameData, GameLog.cardThen(action.sourceCard(),
                        "'s delayed trigger returns the exiled hand at the target player's next end step."));
            }
        }

        if (gameData.hasDelayedAction(DiscardCardsAtNextTurnEndStep.class,
                action -> gameData.turnNumber > action.registeredTurnNumber()
                        && action.playerId().equals(gameData.activePlayerId))) {
            List<DiscardCardsAtNextTurnEndStep> pending = gameData.drainDelayedActions(
                    DiscardCardsAtNextTurnEndStep.class,
                    action -> gameData.turnNumber > action.registeredTurnNumber()
                            && action.playerId().equals(gameData.activePlayerId));
            for (DiscardCardsAtNextTurnEndStep action : pending) {
                List<CardEffect> effects = action.cardIds().stream()
                        .<CardEffect>map(com.github.laxika.magicalvibes.model.effect.DiscardSpecificCardEffect::new)
                        .toList();
                if (effects.isEmpty()) {
                    continue;
                }
                StackEntry entry = TriggeredAbilityStackService.madeAbility(
                        action.sourceCard(),
                        action.controllerId(),
                        action.sourceCard().getName() + "'s delayed trigger — discard those cards",
                        new ArrayList<>(effects));
                entry.setNonTargeting(true);
                triggeredAbilityStackService.putDelayed(gameData, entry);
                gameLogService.append(gameData, GameLog.cardThen(action.sourceCard(),
                        "'s delayed trigger discards the returned cards."));
            }
        }

        // Elkin Lair: "At the beginning of the next end step, if the player hasn't played the card,
        // they put it into their graveyard." Chronological next end step — no active-player filter.
        if (gameData.hasDelayedAction(ExileToOwnerGraveyardAtNextEndStep.class)) {
            List<ExileToOwnerGraveyardAtNextEndStep> pending =
                    gameData.drainDelayedActions(ExileToOwnerGraveyardAtNextEndStep.class);
            Map<UUID, List<ExileToOwnerGraveyardAtNextEndStep>> groups = new LinkedHashMap<>();
            for (ExileToOwnerGraveyardAtNextEndStep action : pending) {
                groups.computeIfAbsent(action.groupId() == null ? action.cardId() : action.groupId(),
                        ignored -> new ArrayList<>()).add(action);
            }
            for (List<ExileToOwnerGraveyardAtNextEndStep> group : groups.values()) {
                ExileToOwnerGraveyardAtNextEndStep action = group.getFirst();
                List<ExileToOwnerGraveyardAtNextEndStep> remaining = group.stream()
                        .filter(member -> gameData.findExiledCard(member.cardId()) != null)
                        .toList();
                if (remaining.isEmpty()) {
                    continue;
                }
                Card sourceCard = action.sourceCard() != null ? action.sourceCard()
                        : gameData.findExiledCard(remaining.getFirst().cardId()).card();
                StackEntry entry;
                if (action.groupId() == null) {
                    entry = TriggeredAbilityStackService.madeAbility(sourceCard, action.controllerId(),
                            sourceCard.getName() + "'s delayed ability",
                            new ArrayList<>(List.of(new PutTargetCardFromExileIntoOwnersGraveyardEffect())),
                            action.cardId(), Zone.EXILE, null);
                } else {
                    entry = TriggeredAbilityStackService.madeAbility(sourceCard, action.controllerId(),
                            sourceCard.getName() + "'s delayed ability",
                            new ArrayList<>(List.of(new PutExiledCardsIntoOwnersGraveyardEffect(
                                    remaining.stream().map(ExileToOwnerGraveyardAtNextEndStep::cardId).toList()))));
                }
                entry.setNonTargeting(true);
                triggeredAbilityStackService.putDelayed(gameData, entry);
                gameLogService.append(gameData, GameLog.cardThen(sourceCard,
                        "'s delayed ability triggers."));
                log.info("Game {} - {} delayed exile-to-graveyard ability pushed onto stack",
                        gameData.id, sourceCard.getName());
            }
        }

        if (gameData.hasDelayedAction(ReturnExiledCardToHandAtNextEndStep.class)) {
            List<ReturnExiledCardToHandAtNextEndStep> pending =
                    gameData.drainDelayedActions(ReturnExiledCardToHandAtNextEndStep.class);
            Map<UUID, List<ReturnExiledCardToHandAtNextEndStep>> groups = new LinkedHashMap<>();
            for (ReturnExiledCardToHandAtNextEndStep action : pending) {
                groups.computeIfAbsent(action.groupId() == null ? action.cardId() : action.groupId(),
                        ignored -> new ArrayList<>()).add(action);
            }
            for (List<ReturnExiledCardToHandAtNextEndStep> group : groups.values()) {
                ReturnExiledCardToHandAtNextEndStep action = group.getFirst();
                List<CardEffect> returns = new ArrayList<>(group.stream()
                        .filter(member -> gameData.findExiledCard(member.cardId()) != null)
                        .map(member -> (CardEffect) new ReturnExiledCardToHandEffect(member.cardId(), member.ownerId()))
                        .toList());
                var exiled = group.stream().map(member -> gameData.findExiledCard(member.cardId()))
                        .filter(java.util.Objects::nonNull).findFirst().orElse(null);
                List<DrawCardsAtNextEndStep> thenDraws = action.sourceCard() == null ? List.of()
                        : gameData.drainDelayedActions(DrawCardsAtNextEndStep.class,
                        draw -> draw.sourceCard() == action.sourceCard()
                                && draw.controllerId().equals(action.controllerId())
                                && draw.drawingPlayerId().equals(draw.controllerId()));
                int thenDrawCount = thenDraws.stream().mapToInt(DrawCardsAtNextEndStep::count).sum();
                if (thenDrawCount > 0) {
                    returns.add(new DrawCardEffect(thenDrawCount));
                }
                if (exiled == null && thenDrawCount == 0) {
                    log.info("Game {} - Delayed next-end-step exile-to-hand return for card {} skipped (no longer in exile)",
                            gameData.id, action.cardId());
                    continue;
                }
                Card sourceCard = action.sourceCard() != null ? action.sourceCard() : exiled.card();
                triggeredAbilityStackService.putDelayed(gameData, TriggeredAbilityStackService.madeAbility(sourceCard, action.controllerId(),
                        sourceCard.getName() + "'s delayed ability — return exiled card to hand",
                        new ArrayList<>(returns)));
                gameLogService.append(gameData, GameLog.cardThen(sourceCard, "'s delayed ability triggers."));
            }
        }

        if (gameData.hasDelayedAction(DelayedSacrificeTargetPermanentAtEndStep.class)) {
            List<DelayedSacrificeTargetPermanentAtEndStep> pending =
                    gameData.drainDelayedActions(DelayedSacrificeTargetPermanentAtEndStep.class);
            for (DelayedSacrificeTargetPermanentAtEndStep action : pending) {
                Permanent permanent = gameQueryService.findPermanentById(gameData, action.permanentId());
                if (permanent == null
                        || !action.controllerId().equals(
                                gameQueryService.findPermanentController(gameData, permanent.getId()))) {
                    continue;
                }

                // A delayed triggered ability: it goes on the stack so players can respond to it.
                StackEntry delayedTrigger = TriggeredAbilityStackService.madeAbility(
                        action.sourceCard(), action.controllerId(),
                        action.sourceCard().getName() + "'s delayed ability",
                        new ArrayList<>(List.of(
                                new com.github.laxika.magicalvibes.model.effect.SacrificeTargetPermanentAtEndStepAndGainLifeEqualToToughnessEffect())),
                        action.permanentId(), (UUID) null);
                delayedTrigger.setNonTargeting(true);
                triggeredAbilityStackService.putDelayed(gameData, delayedTrigger);
                gameLogService.append(gameData, GameLog.cardThen(action.sourceCard(),
                        "'s delayed sacrifice triggers."));
            }
        }

        if (gameData.hasDelayedAction(DelayedCoinFlipSacrificeTargetPermanentAtEndStep.class)) {
            List<DelayedCoinFlipSacrificeTargetPermanentAtEndStep> pending = gameData.drainDelayedActions(
                    DelayedCoinFlipSacrificeTargetPermanentAtEndStep.class);
            for (DelayedCoinFlipSacrificeTargetPermanentAtEndStep action : pending) {
                StackEntry entry = TriggeredAbilityStackService.madeAbility(
                        action.sourceCard(),
                        action.controllerId(),
                        action.sourceCard().getName() + "'s delayed trigger",
                        new ArrayList<>(List.of(new FlipCoinWinEffect(null, new SacrificeSelfEffect()))),
                        0,
                        action.permanentId());
                entry.setNonTargeting(true);
                triggeredAbilityStackService.putDelayed(gameData, entry);
                gameLogService.append(gameData,
                        GameLog.cardThen(action.sourceCard(), "'s delayed trigger flips a coin."));
                log.info("Game {} - {} delayed coin-flip trigger pushed onto stack",
                        gameData.id, action.sourceCard().getName());
            }
        }

        if (gameData.hasDelayedAction(DelayedSacrificeTargetPermanentAtEndStepIfManaValueAtMost.class)) {
            List<DelayedSacrificeTargetPermanentAtEndStepIfManaValueAtMost> pending =
                    gameData.drainDelayedActions(DelayedSacrificeTargetPermanentAtEndStepIfManaValueAtMost.class);
            for (DelayedSacrificeTargetPermanentAtEndStepIfManaValueAtMost action : pending) {
                Permanent permanent = gameQueryService.findPermanentById(gameData, action.permanentId());
                if (permanent == null) {
                    continue;
                }
                CardEffect sacrifice = new SacrificePermanentsEffect(1,
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsSpecificPermanentPredicate(action.permanentId()),
                                new com.github.laxika.magicalvibes.model.filter.PermanentMaxManaValuePredicate(
                                        action.maxManaValue()))),
                        SacrificeRecipient.CONTROLLER);
                StackEntry delayedTrigger = TriggeredAbilityStackService.madeAbility(
                        action.sourceCard() != null ? action.sourceCard() : permanent.getCard(),
                        action.controllerId(), "Delayed conditional sacrifice",
                        List.of(sacrifice), 0, (UUID) null);
                delayedTrigger.setNonTargeting(true);
                triggeredAbilityStackService.putDelayed(gameData, delayedTrigger);
                gameLogService.append(gameData, GameLog.cardThen(permanent.getCard(),
                        "'s delayed sacrifice triggers."));
            }
        }

        if (gameData.hasDelayedAction(DelayedSacrificeTargetPermanentAtEndStepIfManaValueAtLeast.class)) {
            List<DelayedSacrificeTargetPermanentAtEndStepIfManaValueAtLeast> pending =
                    gameData.drainDelayedActions(DelayedSacrificeTargetPermanentAtEndStepIfManaValueAtLeast.class);
            for (DelayedSacrificeTargetPermanentAtEndStepIfManaValueAtLeast action : pending) {
                Permanent permanent = gameQueryService.findPermanentById(gameData, action.permanentId());
                if (permanent == null
                        || !action.controllerId().equals(
                                gameQueryService.findPermanentController(gameData, permanent.getId()))
                        || permanent.getCard().getManaValue() < action.minManaValue()
                        || gameQueryService.cantBeSacrificed(gameData, permanent)) {
                    continue;
                }

                CardEffect sacrifice = new SacrificePermanentsEffect(1,
                        new PermanentIsSpecificPermanentPredicate(action.permanentId()),
                        SacrificeRecipient.CONTROLLER);
                StackEntry delayedTrigger = TriggeredAbilityStackService.madeAbility(
                        permanent.getCard(), action.controllerId(), "Delayed conditional sacrifice",
                        List.of(sacrifice), 0, (UUID) null);
                delayedTrigger.setNonTargeting(true);
                triggeredAbilityStackService.putDelayed(gameData, delayedTrigger);
                gameLogService.append(gameData, GameLog.cardThen(permanent.getCard(),
                        "'s delayed sacrifice triggers."));
            }
        }

        // Perform the scheduled end-step zone changes: token exiles (e.g. Mimic Vat), nontoken
        // exiles (e.g. Dark Maze), sacrifices (e.g. Choreographed Sparks' creature-copy token) and
        // destructions (e.g. Stone Giant).
        permanentRemovalService.processDelayedPermanentActions(gameData,
                DelayedPermanentActionKind.EXILE_TOKEN_AT_END_STEP);
        queueDelayedPermanentExiles(gameData, DelayedPermanentActionKind.EXILE_AT_END_STEP);
        permanentRemovalService.processDelayedPermanentActions(gameData,
                DelayedPermanentActionKind.EXILE_WARPED_AT_END_STEP);
        permanentRemovalService.processDelayedPermanentActions(gameData,
                DelayedPermanentActionKind.SACRIFICE_AT_END_STEP);
        permanentRemovalService.processDelayedPermanentActions(gameData,
                DelayedPermanentActionKind.DESTROY_AT_END_STEP);

        // Stone Idol Trap: exile permanents created by the spell at the beginning of its
        // controller's next end step, not at the next end step of any player. The exile is a
        // triggered ability that goes on the stack.
        if (gameData.hasDelayedAction(ExilePermanentAtControllerEndStep.class,
                action -> action.controllerId().equals(gameData.activePlayerId))) {
            List<ExilePermanentAtControllerEndStep> pending =
                    gameData.drainDelayedActions(ExilePermanentAtControllerEndStep.class,
                            action -> action.controllerId().equals(gameData.activePlayerId));
            for (ExilePermanentAtControllerEndStep action : pending) {
                Permanent permanent = gameQueryService.findPermanentById(gameData, action.permanentId());
                if (permanent == null) {
                    continue;
                }
                StackEntry entry = TriggeredAbilityStackService.madeAbility(
                        permanent.getCard(),
                        action.controllerId(),
                        permanent.getCard().getName() + "'s delayed ability",
                        new ArrayList<>(List.of(new ExileTargetPermanentEffect())),
                        permanent.getId(),
                        permanent.getId());
                entry.setNonTargeting(true);
                triggeredAbilityStackService.putDelayed(gameData, entry);
                gameLogService.append(gameData,
                        GameLog.cardThen(permanent.getCard(), "'s delayed exile ability triggers."));
                log.info("Game {} - {} delayed exile ability triggers at its controller's end step",
                        gameData.id, permanent.getCard().getName());
            }
        }

        // Process Siren's Call: destroy all non-Wall creatures the player controls that didn't attack
        // this turn, ignoring creatures they didn't control continuously since the beginning of the
        // turn (summoning sick).
        if (gameData.hasDelayedAction(DestroyNonAttackersAtEndStep.class)) {
            List<DestroyNonAttackersAtEndStep> pending =
                    gameData.drainDelayedActions(DestroyNonAttackersAtEndStep.class);
            for (DestroyNonAttackersAtEndStep action : pending) {
                var filter = new com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate(List.of(
                        new com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate(),
                        new com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate(
                                new com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate(CardSubtype.WALL)),
                        new com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate(
                                new com.github.laxika.magicalvibes.model.filter.PermanentAttackedThisTurnPredicate()),
                        action.excludeSummoningSickFromDestruction()
                                ? new com.github.laxika.magicalvibes.model.filter.PermanentControlledContinuouslySinceBeginningOfTurnPredicate()
                                : new com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate()));
                StackEntry delayed = TriggeredAbilityStackService.madeAbility(action.sourceCard(), action.controllerId(),
                        action.sourceCard().getName() + "'s delayed ability",
                        new ArrayList<>(List.of(new com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect(
                                filter, com.github.laxika.magicalvibes.model.effect.EachPermanentScope.TARGET_PLAYER, null))));
                delayed.setTargetId(action.playerId());
                delayed.setNonTargeting(true);
                triggeredAbilityStackService.putDelayed(gameData, delayed);
            }
        }

        // Norritt: destroy the specific permanent if it didn't attack this turn.
        if (gameData.hasDelayedAction(DestroyPermanentIfDidNotAttackAtEndStep.class)) {
            List<DestroyPermanentIfDidNotAttackAtEndStep> pending =
                    gameData.drainDelayedActions(DestroyPermanentIfDidNotAttackAtEndStep.class);
            for (DestroyPermanentIfDidNotAttackAtEndStep action : pending) {
                Permanent perm = gameQueryService.findPermanentById(gameData, action.permanentId());
                if (perm == null || perm.isAttackedThisTurn()) {
                    continue;
                }
                StackEntry trigger = TriggeredAbilityStackService.madeAbility(
                        action.sourceCard() == null ? perm.getCard() : action.sourceCard(),
                        action.controllerId() == null ? gameQueryService.findPermanentController(gameData, perm.getId()) : action.controllerId(),
                        "Destroy creature that didn't attack",
                        new ArrayList<>(List.of(new com.github.laxika.magicalvibes.model.effect.DestroyReferencedPermanentEffect(
                                com.github.laxika.magicalvibes.model.effect.PermanentReference.TRIGGERING))),
                        null, (UUID) null);
                trigger.setTriggeringPermanentId(perm.getId());
                trigger.setNonTargeting(true);
                triggeredAbilityStackService.putDelayed(gameData, trigger);
            }
        }

        // Oracle en-Vec: one trigger per resolved ability destroys each chosen creature that didn't attack.
        if (gameData.hasDelayedAction(DestroyChosenCreaturesIfDidNotAttackAtEndStep.class)) {
            List<DestroyChosenCreaturesIfDidNotAttackAtEndStep> pending =
                    gameData.drainDelayedActions(DestroyChosenCreaturesIfDidNotAttackAtEndStep.class);
            for (DestroyChosenCreaturesIfDidNotAttackAtEndStep action : pending) {
                List<UUID> stillDestructible = action.permanentIds().stream()
                        .map(permanentId -> gameQueryService.findPermanentById(gameData, permanentId))
                        .filter(perm -> perm != null && !perm.isAttackedThisTurn())
                        .map(Permanent::getId)
                        .toList();
                if (stillDestructible.isEmpty()) {
                    continue;
                }
                Permanent first = gameQueryService.findPermanentById(gameData, stillDestructible.getFirst());
                StackEntry trigger = TriggeredAbilityStackService.madeAbility(
                        action.sourceCard() == null ? first.getCard() : action.sourceCard(),
                        action.controllerId() == null ? gameQueryService.findPermanentController(gameData, first.getId()) : action.controllerId(),
                        "Destroy chosen creatures that didn't attack",
                        new ArrayList<>(List.of(new com.github.laxika.magicalvibes.model.effect.DestroyEachTargetPermanentEffect())),
                        null, stillDestructible);
                trigger.setNonTargeting(true);
                triggeredAbilityStackService.putDelayed(gameData, trigger);
            }
        }

        // Perform the scheduled end-step returns to hand (e.g. Dragon Mask)
        permanentRemovalService.processDelayedPermanentActions(gameData,
                DelayedPermanentActionKind.RETURN_TO_HAND_AT_END_STEP);
        permanentRemovalService.processDelayedPermanentActions(gameData,
                DelayedPermanentActionKind.RETURN_TO_COMMAND_ZONE_AT_END_STEP);

        // Process delayed "lose the game" triggers (e.g. Last Chance, Glorious End). These fire at the
        // beginning of the scheduling player's *own* next end step ("your next end step"): only entries
        // scheduled on an earlier turn (so the scheduling turn's own end step is skipped) AND only while
        // that player is the active player (so opponents' end steps are skipped). For the extra-turn
        // cards the very next turn is the controller's, so the loss lands on it; for Glorious End cast
        // on your own turn it skips the intervening opponent turn and lands on your next turn's end step.
        if (gameData.hasDelayedAction(LoseGameAtEndStep.class)) {
            List<LoseGameAtEndStep> toLose = gameData.drainDelayedActions(
                    LoseGameAtEndStep.class,
                    action -> action.extraTurnSequence() != null
                            ? action.extraTurnSequence().equals(gameData.currentExtraTurnSequence)
                            : gameData.turnNumber > action.registeredTurnNumber()
                            && action.playerId().equals(gameData.activePlayerId));
            for (LoseGameAtEndStep action : toLose) {
                triggeredAbilityStackService.putDelayed(gameData, TriggeredAbilityStackService.madeAbility(
                        action.sourceCard(),
                        action.playerId(),
                        action.sourceCard().getName() + "'s delayed trigger — you lose the game",
                        new ArrayList<>(List.of(new TargetPlayerLosesGameEffect(action.playerId())))
                ));
                gameLogService.append(gameData, GameLog.cardThen(action.sourceCard(),
                        "'s delayed trigger — " + gameData.playerIdToName.get(action.playerId()) + " loses the game."));
                log.info("Game {} - {} delayed lose-game trigger pushed onto stack",
                        gameData.id, action.sourceCard().getName());
            }
        }

        // Process delayed +1/+1 counter regrowth triggers (e.g. Protean Hydra)
        // Ruling: "If multiple +1/+1 counters are removed at once, its last ability will trigger that many times."
        // Each removed counter creates a separate delayed trigger that adds 2 +1/+1 counters.
        // The pending map stores countersRemoved * 2 (total counters to add), so we divide by 2
        // to get the number of individual triggers, each adding 2 counters.
        if (gameData.hasDelayedAction(DelayedPlusOneCounters.class)) {
            List<DelayedPlusOneCounters> pendingCounters =
                    gameData.drainDelayedActions(DelayedPlusOneCounters.class);
            for (DelayedPlusOneCounters counterEntry : pendingCounters) {
                UUID permanentId = counterEntry.permanentId();
                int totalCountersToAdd = counterEntry.totalCounters();
                Permanent perm = gameQueryService.findPermanentById(gameData, permanentId);
                if (perm == null) continue;
                UUID controllerId = counterEntry.controllerId() != null
                        ? counterEntry.controllerId()
                        : gameQueryService.findPermanentController(gameData, permanentId);
                if (controllerId == null) continue;

                int triggerCount = totalCountersToAdd / 2; // each trigger adds 2 counters
                for (int i = 0; i < triggerCount; i++) {
                    PutCountersOnSourceEffect effect = new PutCountersOnSourceEffect(1, 1, 2);
                    triggeredAbilityStackService.putDelayed(gameData, TriggeredAbilityStackService.madeAbility(
                            perm.getCard(),
                            controllerId,
                            perm.getCard().getName() + "'s delayed +1/+1 counter trigger",
                            new ArrayList<>(List.of(effect)),
                            null,
                            perm.getId()
                    ));
                }

                gameLogService.append(gameData, GameLog.cardThen(perm.getCard(),
                        "'s delayed trigger — " + triggerCount + " trigger(s), adding " + totalCountersToAdd + " +1/+1 counter(s)."));
                log.info("Game {} - {} delayed +1/+1 counter regrowth: {} trigger(s) pushed onto stack", gameData.id, perm.getCard().getName(), triggerCount);
            }
        }

        // Process delayed +0/+1 counter triggers (e.g. Sacred Boon: one +0/+1 counter per 1 damage prevented).
        if (gameData.hasDelayedAction(DelayedPlusZeroPlusOneCounters.class)) {
            List<DelayedPlusZeroPlusOneCounters> pendingCounters =
                    gameData.drainDelayedActions(DelayedPlusZeroPlusOneCounters.class, boon -> !boon.triggered());
            for (DelayedPlusZeroPlusOneCounters counterEntry : pendingCounters) {
                Permanent perm = gameQueryService.findPermanentById(gameData, counterEntry.permanentId());
                // "If it's a creature": a planeswalker that was the shield's target gets no counters.
                if (perm == null || !gameQueryService.isCreature(gameData, perm)) continue;
                UUID controllerId = counterEntry.controllerId() != null
                        ? counterEntry.controllerId()
                        : gameQueryService.findPermanentController(gameData, perm.getId());
                if (controllerId == null) continue;

                gameData.requeueSacredBoonEntry(new DelayedPlusZeroPlusOneCounters(
                        counterEntry.permanentId(), counterEntry.totalCounters(), counterEntry.controllerId(),
                        counterEntry.sourceCard(), counterEntry.shieldId(), counterEntry.remainingShield(), true));
                var boonSource = counterEntry.sourceCard() != null ? counterEntry.sourceCard() : perm.getCard();
                triggeredAbilityStackService.putDelayed(gameData, TriggeredAbilityStackService.madeAbility(
                        boonSource,
                        controllerId,
                        boonSource.getName() + "'s delayed +0/+1 counter trigger",
                        new ArrayList<>(List.of(new PutCountersForPreventedDamageEffect(counterEntry.shieldId()))),
                        null,
                        perm.getId()
                ));
                log.info("Game {} - {} delayed +0/+1 counter trigger pushed onto stack", gameData.id, perm.getCard().getName());
            }
        }

        if (gameData.hasDelayedAction(RemoveCounterFromPermanentAtNextEndStep.class)) {
            List<RemoveCounterFromPermanentAtNextEndStep> pendingRemovals =
                    gameData.drainDelayedActions(RemoveCounterFromPermanentAtNextEndStep.class);
            for (RemoveCounterFromPermanentAtNextEndStep action : pendingRemovals) {
                StackEntry entry = TriggeredAbilityStackService.madeAbility(
                        action.sourceCard(),
                        action.controllerId(),
                        action.sourceCard().getName() + "'s delayed ability",
                        new ArrayList<>(List.of(new RemoveCounterFromTargetPermanentEffect(
                                action.counterType(), null, action.count()))),
                        action.permanentId(),
                        (UUID) null);
                entry.setNonTargeting(true);
                triggeredAbilityStackService.putDelayed(gameData, entry);
                gameLogService.append(gameData, GameLog.cardThen(action.sourceCard(),
                        "'s delayed ability triggers to remove " + action.count() + " counter(s)."));
            }
        }

        // Process delayed untap permanents triggers (e.g. Teferi, Hero of Dominaria +1)
        if (gameData.hasDelayedAction(DelayedUntapPermanents.class)) {
            List<DelayedUntapPermanents> pendingUntaps =
                    gameData.drainDelayedActions(DelayedUntapPermanents.class);
            for (DelayedUntapPermanents pending : pendingUntaps) {
                triggeredAbilityStackService.putDelayed(gameData, TriggeredAbilityStackService.madeAbility(
                        pending.sourceCard(),
                        pending.controllerId(),
                        pending.sourceCard().getName() + "'s delayed trigger — untap up to " + pending.count() + " permanent(s)",
                        new ArrayList<>(List.of(new UntapPermanentsEffect(
                                TapUntapScope.ALL_PERMANENTS, pending.filter(), pending.count())))
                ));
                gameLogService.append(gameData, GameLog.cardThen(pending.sourceCard(),
                        "'s delayed trigger — untap up to " + pending.count() + " permanent(s)."));
                log.info("Game {} - {} delayed untap {} permanent(s) trigger pushed onto stack",
                        gameData.id, pending.sourceCard().getName(), pending.count());
            }
        }

        if (gameData.hasDelayedAction(DelayedExileCreatedPermanentsAtEndStep.class)) {
            List<DelayedExileCreatedPermanentsAtEndStep> pendingExiles =
                    gameData.drainDelayedActions(DelayedExileCreatedPermanentsAtEndStep.class);
            for (DelayedExileCreatedPermanentsAtEndStep pending : pendingExiles) {
                triggeredAbilityStackService.putDelayed(gameData, TriggeredAbilityStackService.madeAbility(
                        pending.sourceCard(),
                        pending.controllerId(),
                        pending.sourceCard().getName() + "'s delayed trigger — exile created permanents",
                        new ArrayList<>(List.of(new ExileCreatedPermanentsAtEndStepUnlessConditionEffect(
                                pending.permanentIds(), pending.condition())))
                ));
                gameLogService.append(gameData,
                        GameLog.cardThen(pending.sourceCard(), "'s delayed trigger — exile created permanents."));
                log.info("Game {} - {} delayed exile trigger pushed onto stack",
                        gameData.id, pending.sourceCard().getName());
            }
        }

        // Process delayed token creations (e.g. Rukh Egg)
        if (gameData.hasDelayedAction(DelayedCreateToken.class)) {
            List<DelayedCreateToken> pendingTokens =
                    gameData.drainDelayedActions(DelayedCreateToken.class);
            for (DelayedCreateToken pending : pendingTokens) {
                triggeredAbilityStackService.putDelayed(gameData, TriggeredAbilityStackService.madeAbility(
                        pending.sourceCard(),
                        pending.controllerId(),
                        pending.sourceCard().getName() + "'s delayed trigger — create token",
                        new ArrayList<>(List.of(pending.tokenEffect()))
                ));
                gameLogService.append(gameData,
                        GameLog.cardThen(pending.sourceCard(), "'s delayed trigger — create token."));
                log.info("Game {} - {} delayed token creation trigger pushed onto stack",
                        gameData.id, pending.sourceCard().getName());
            }
        }

        if (gameData.hasDelayedAction(DelayedRevealCreatureCardsToBattlefield.class)) {
            List<DelayedRevealCreatureCardsToBattlefield> pendingReveals =
                    gameData.drainDelayedActions(DelayedRevealCreatureCardsToBattlefield.class);
            for (DelayedRevealCreatureCardsToBattlefield pending : pendingReveals) {
                triggeredAbilityStackService.putDelayed(gameData, TriggeredAbilityStackService.madeAbility(
                        pending.sourceCard(),
                        pending.controllerId(),
                        pending.sourceCard().getName() + "'s delayed trigger — reveal creature cards",
                        new ArrayList<>(List.of(new RevealCreatureCardsToBattlefieldAndShuffleRestEffect(
                                pending.creatureCount())))
                ));
                gameLogService.append(gameData,
                        GameLog.cardThen(pending.sourceCard(),
                                "'s delayed trigger reveals creature cards."));
                log.info("Game {} - {} delayed creature reveal trigger pushed onto stack",
                        gameData.id, pending.sourceCard().getName());
            }
        }

        if (gameData.hasDelayedAction(DelayedCreateTokenCopy.class)) {
            List<DelayedCreateTokenCopy> pendingCopies =
                    gameData.drainDelayedActions(DelayedCreateTokenCopy.class);
            for (DelayedCreateTokenCopy pending : pendingCopies) {
                String sourceName = pending.sourceCard() != null
                        ? pending.sourceCard().getName() : pending.copiedCard().getName();
                triggeredAbilityStackService.putDelayed(gameData, TriggeredAbilityStackService.madeAbility(
                        pending.copiedCard(),
                        pending.controllerId(),
                        sourceName + "'s delayed trigger — create token copy",
                        new ArrayList<>(List.of(pending.copyEffect() != null ? pending.copyEffect() : new CreateTokenCopyOfSourceEffect()))
                ));
                gameLogService.append(gameData,
                        GameLog.cardThen(pending.sourceCard() != null ? pending.sourceCard() : pending.copiedCard(),
                                "'s delayed trigger — create token copy."));
                log.info("Game {} - {} delayed token copy trigger pushed onto stack",
                        gameData.id, sourceName);
            }
        }

        if (gameData.hasDelayedAction(DelayedChooseOpponentGainsControlOfSource.class)) {
            List<DelayedChooseOpponentGainsControlOfSource> pendingControlChanges =
                    gameData.drainDelayedActions(DelayedChooseOpponentGainsControlOfSource.class);
            for (DelayedChooseOpponentGainsControlOfSource pending : pendingControlChanges) {
                if (gameQueryService.findPermanentById(gameData, pending.sourcePermanentId()) == null) {
                    continue;
                }
                StackEntry entry = TriggeredAbilityStackService.madeAbility(
                        pending.sourceCard(),
                        pending.controllerId(),
                        pending.sourceCard().getName() + "'s delayed trigger — choose an opponent",
                        new ArrayList<>(List.of(new ChooseOpponentGainsControlOfSourceEffect())),
                        null,
                        pending.sourcePermanentId());
                entry.setNonTargeting(true);
                triggeredAbilityStackService.putDelayed(gameData, entry);
                gameLogService.append(gameData,
                        GameLog.cardThen(pending.sourceCard(), "'s delayed trigger — choose an opponent."));
            }
        }

        if (gameData.hasDelayedAction(DiscardCardsAtNextEndStep.class)) {
            List<DiscardCardsAtNextEndStep> pendingDiscards =
                    gameData.drainDelayedActions(DiscardCardsAtNextEndStep.class);
            for (DiscardCardsAtNextEndStep pending : pendingDiscards) {
                StackEntry entry = TriggeredAbilityStackService.madeAbility(
                        pending.sourceCard(),
                        pending.controllerId(),
                        pending.sourceCard().getName() + "'s delayed trigger — discard " + pending.count() + " cards",
                        new ArrayList<>(List.of(new DiscardEffect(pending.count(), DiscardRecipient.CONTROLLER)))
                );
                entry.setNonTargeting(true);
                triggeredAbilityStackService.putDelayed(gameData, entry);
                gameLogService.append(gameData,
                        GameLog.cardThen(pending.sourceCard(), "'s delayed trigger discards cards."));
                log.info("Game {} - {} delayed discard trigger pushed onto stack",
                        gameData.id, pending.sourceCard().getName());
            }
        }

        if (gameData.hasDelayedAction(DiscardSpecificCardAtNextEndStep.class)) {
            List<DiscardSpecificCardAtNextEndStep> pendingDiscards = gameData.drainDelayedActions(
                    DiscardSpecificCardAtNextEndStep.class,
                    pending -> pending.controllerId().equals(gameData.activePlayerId));
            for (DiscardSpecificCardAtNextEndStep pending : pendingDiscards) {
                List<CardEffect> discards = pending.cardIds().stream()
                        .<CardEffect>map(DiscardSpecificCardEffect::new)
                        .toList();
                StackEntry entry = TriggeredAbilityStackService.madeAbility(
                        pending.sourceCard(),
                        pending.controllerId(),
                        pending.sourceCard().getName() + "'s delayed trigger — discard those cards",
                        new ArrayList<>(discards));
                entry.setNonTargeting(true);
                triggeredAbilityStackService.putDelayed(gameData, entry);
                gameLogService.append(gameData,
                        GameLog.cardThen(pending.sourceCard(), "'s delayed trigger discards those cards."));
                log.info("Game {} - {} delayed specific discard trigger pushed onto stack",
                        gameData.id, pending.sourceCard().getName());
            }
        }

        if (gameData.hasDelayedAction(ExileCardsFromOwnGraveyardAtNextEndStep.class)) {
            List<ExileCardsFromOwnGraveyardAtNextEndStep> pendingExiles =
                    gameData.drainDelayedActions(ExileCardsFromOwnGraveyardAtNextEndStep.class);
            for (ExileCardsFromOwnGraveyardAtNextEndStep pending : pendingExiles) {
                StackEntry entry = TriggeredAbilityStackService.madeAbility(
                        pending.sourceCard(),
                        pending.controllerId(),
                        pending.sourceCard().getName() + "'s delayed trigger — exile cards from graveyard",
                        new ArrayList<>(List.of(new ExileGraveyardCardsEffect(
                                pending.count(), GraveyardExileScope.OWN)))
                );
                entry.setNonTargeting(true);
                triggeredAbilityStackService.putDelayed(gameData, entry);
                gameLogService.append(gameData,
                        GameLog.cardThen(pending.sourceCard(), "'s delayed trigger exiles cards from graveyard."));
                log.info("Game {} - {} delayed graveyard exile trigger pushed onto stack",
                        gameData.id, pending.sourceCard().getName());
            }
        }

        if (gameData.hasDelayedAction(DelayedDestroyAllPermanents.class)) {
            List<DelayedDestroyAllPermanents> pendingWipes =
                    gameData.drainDelayedActions(DelayedDestroyAllPermanents.class);
            for (DelayedDestroyAllPermanents pending : pendingWipes) {
                triggeredAbilityStackService.putDelayed(gameData, TriggeredAbilityStackService.madeAbility(
                        pending.sourceCard(),
                        pending.controllerId(),
                        pending.sourceCard().getName() + "'s delayed trigger — destroy all permanents",
                        new ArrayList<>(List.of(new DestroyAllPermanentsEffect(new PermanentTruePredicate())))
                ));
                gameLogService.append(gameData,
                        GameLog.cardThen(pending.sourceCard(), "'s delayed trigger — destroy all permanents."));
                log.info("Game {} - {} delayed destroy-all-permanents trigger pushed onto stack",
                        gameData.id, pending.sourceCard().getName());
            }
        }

        // Process delayed lose-life-and-return (e.g. Brood of Cockroaches). Both instructions are
        // one triggered ability — life loss still happens if the card left the graveyard.
        if (gameData.hasDelayedAction(DelayedLoseLifeAndReturnFromGraveyard.class)) {
            List<DelayedLoseLifeAndReturnFromGraveyard> pendingReturns =
                    gameData.drainDelayedActions(DelayedLoseLifeAndReturnFromGraveyard.class);
            for (DelayedLoseLifeAndReturnFromGraveyard pending : pendingReturns) {
                StackEntry delayedEntry = TriggeredAbilityStackService.madeAbility(
                        pending.sourceCard(),
                        pending.controllerId(),
                        pending.sourceCard().getName()
                                + "'s delayed trigger — lose " + pending.lifeLoss()
                                + " life and return to hand",
                        new ArrayList<>(List.of(
                                new LoseLifeEffect(pending.lifeLoss()),
                                new com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToOwnerHandEffect()
                        ))
                );
                delayedEntry.setSourceGraveyardEntryVersion(pending.graveyardEntryVersion());
                triggeredAbilityStackService.putDelayed(gameData, delayedEntry);
                gameLogService.append(gameData, GameLog.cardThen(pending.sourceCard(),
                        "'s delayed trigger — lose " + pending.lifeLoss()
                                + " life and return to hand."));
                log.info("Game {} - {} delayed lose-life-and-return trigger pushed onto stack",
                        gameData.id, pending.sourceCard().getName());
            }
        }

        // Process delayed graveyard-to-hand returns (e.g. Tiana, Ship's Caretaker)
        if (gameData.hasDelayedAction(DelayedGraveyardToHandReturn.class)) {
            List<DelayedGraveyardToHandReturn> pendingReturns =
                    gameData.drainDelayedActions(DelayedGraveyardToHandReturn.class);
            for (DelayedGraveyardToHandReturn pending : pendingReturns) {
                List<Card> graveyard = gameData.playerGraveyards.get(pending.ownerId());
                if (graveyard == null) continue;
                Card cardToReturn = null;
                for (Card card : graveyard) {
                    if (card.getId().equals(pending.cardId())) {
                        cardToReturn = card;
                        break;
                    }
                }
                if (cardToReturn != null) {
                    permanentRemovalService.removeCardFromGraveyardById(gameData, cardToReturn.getId());
                    permanentRemovalService.addCardToHandFromGraveyard(
                            gameData, pending.ownerId(), pending.ownerId(), cardToReturn);
                    String playerName = gameData.playerIdToName.get(pending.ownerId());
                    gameLogService.append(gameData, GameLog.cardThen(cardToReturn,
                            " returns to " + playerName + "'s hand (delayed trigger)."));
                    log.info("Game {} - {} returns to {}'s hand from graveyard (delayed end-step trigger)",
                            gameData.id, cardToReturn.getName(), playerName);
                } else {
                    log.info("Game {} - Delayed graveyard-to-hand return for card {} skipped (no longer in graveyard)",
                            gameData.id, pending.cardId());
                }
            }
        }

        // Process delayed exile-to-hand returns (e.g. Necropotence). Only the controller's own end
        // step returns their set-aside cards ("your next end step"), so filter by the active player.
        if (gameData.hasDelayedAction(ReturnExiledCardToHandAtEndStep.class)) {
            List<ReturnExiledCardToHandAtEndStep> pendingReturns = gameData.drainDelayedActions(
                    ReturnExiledCardToHandAtEndStep.class,
                    a -> gameData.activePlayerId != null && gameData.activePlayerId.equals(a.ownerId()));
            for (ReturnExiledCardToHandAtEndStep pending : pendingReturns) {
                var exiledEntry = gameData.findExiledCard(pending.cardId());
                if (exiledEntry == null) {
                    log.info("Game {} - Delayed exile-to-hand return for card {} skipped (no longer in exile)",
                            gameData.id, pending.cardId());
                    continue;
                }
                Card sourceCard = pending.sourceCard() != null
                        ? pending.sourceCard() : exiledEntry.card();
                StackEntry entry = TriggeredAbilityStackService.madeAbility(
                        sourceCard,
                        pending.ownerId(),
                        sourceCard.getName() + "'s delayed ability",
                        new ArrayList<>(List.of(new ReturnExiledCardToHandEffect(
                                pending.cardId(), pending.ownerId()))));
                entry.setNonTargeting(true);
                triggeredAbilityStackService.putDelayed(gameData, entry);
                gameLogService.append(gameData, GameLog.cardThen(
                        sourceCard, "'s delayed return ability triggers."));
            }
        }

        // Process delayed graveyard-to-battlefield transformed returns (e.g. Loyal Cathar)
        if (gameData.hasDelayedAction(DelayedGraveyardToBattlefieldTransformedReturn.class)) {
            List<DelayedGraveyardToBattlefieldTransformedReturn> pendingReturns =
                    gameData.drainDelayedActions(DelayedGraveyardToBattlefieldTransformedReturn.class);
            for (DelayedGraveyardToBattlefieldTransformedReturn pending : pendingReturns) {
                Card returningCard = gameData.playerGraveyards.getOrDefault(pending.ownerId(), List.of()).stream()
                        .filter(card -> card.getId().equals(pending.cardId())).findFirst().orElse(null);
                if (returningCard == null
                        || gameData.graveyardEntryVersion(returningCard.getId()) != pending.graveyardEntryVersion()) {
                    continue;
                }
                StackEntry trigger = TriggeredAbilityStackService.madeAbility(
                        returningCard, pending.controllerId(), returningCard.getName() + "'s delayed return ability",
                        List.of(new com.github.laxika.magicalvibes.model.effect.ReturnSourceTransformedFromGraveyardEffect()));
                trigger.setTriggeringCardId(returningCard.getId());
                trigger.setTriggeringCardGraveyardEntryVersion(gameData.graveyardEntryVersion(returningCard.getId()));
                trigger.setNonTargeting(true);
                triggeredAbilityStackService.putDelayed(gameData, trigger);
                gameLogService.append(gameData, GameLog.cardThen(returningCard, "'s delayed return ability triggers."));
            }
        }

        // Process delayed graveyard-to-battlefield self returns (Sand Golem); the upkeep-scheduled
        // ones (Phytotitan) are left in the queue for handleUpkeepTriggers.
        resolveDelayedSelfReturns(gameData, pending -> !pending.atNextUpkeep());

        if (gameData.hasDelayedAction(DelayedReturnAuraAttachedToPermanent.class)) {
            List<DelayedReturnAuraAttachedToPermanent> pendingReturns =
                    gameData.drainDelayedActions(DelayedReturnAuraAttachedToPermanent.class);
            for (DelayedReturnAuraAttachedToPermanent pending : pendingReturns) {
                Card auraCard = gameQueryService.findCardInGraveyardById(gameData, pending.auraCardId());
                if (auraCard == null) {
                    continue;
                }

                StackEntry trigger = TriggeredAbilityStackService.madeAbility(
                        auraCard, pending.auraOwnerId(), auraCard.getName() + "'s delayed return ability",
                        new ArrayList<>(List.of(new ReturnAuraFromGraveyardAttachedToPermanentEffect(
                                pending.auraCardId(), pending.auraOwnerId(), pending.enchantedPermanentId()))));
                trigger.setNonTargeting(true);
                triggeredAbilityStackService.putDelayed(gameData, trigger);
                gameLogService.append(gameData, GameLog.cardThen(auraCard, "'s delayed return ability triggers."));
            }
        }

        if (gameData.hasDelayedAction(DelayedReturnCurseAttachedToPlayer.class)) {
            List<DelayedReturnCurseAttachedToPlayer> pendingReturns =
                    gameData.drainDelayedActions(DelayedReturnCurseAttachedToPlayer.class);
            for (DelayedReturnCurseAttachedToPlayer pending : pendingReturns) {
                Card curseCard = gameQueryService.findCardInGraveyardById(gameData, pending.cardId());
                if (curseCard == null) {
                    continue;
                }
                StackEntry entry = TriggeredAbilityStackService.madeAbility(
                        curseCard, pending.attachedPlayerId(), curseCard.getName() + "'s delayed return ability",
                        new ArrayList<>(List.of(new ReturnDelayedCurseAttachedToPlayerEffect(pending))));
                entry.setNonTargeting(true);
                entry.setTriggeringCardId(pending.cardId());
                triggeredAbilityStackService.putDelayed(gameData, entry);
                gameLogService.append(gameData, GameLog.cardThen(curseCard, "'s delayed return ability triggers."));
            }
        }

        if (gameData.hasDelayedAction(DelayedReturnSourceAuraToCreature.class)) {
            List<DelayedReturnSourceAuraToCreature> pendingReturns =
                    gameData.drainDelayedActions(DelayedReturnSourceAuraToCreature.class);
            for (DelayedReturnSourceAuraToCreature pending : pendingReturns) {
                Card auraCard = gameQueryService.findCardInGraveyardById(gameData, pending.auraCardId());
                if (auraCard == null
                        || gameData.graveyardEntryVersion(auraCard.getId()) != pending.graveyardEntryVersion()) {
                    continue;
                }

                StackEntry entry = TriggeredAbilityStackService.madeAbility(
                        auraCard,
                        pending.controllerId(),
                        auraCard.getName() + "'s delayed return ability",
                        new ArrayList<>(List.of(new ReturnSourceAuraToCreatureOnDeathEffect())));
                entry.setNonTargeting(true);
                entry.setTriggeringCardId(auraCard.getId());
                entry.setTriggeringCardGraveyardEntryVersion(
                        gameData.graveyardEntryVersion(auraCard.getId()));
                triggeredAbilityStackService.putDelayed(gameData, entry);
                gameLogService.append(gameData,
                        GameLog.cardThen(auraCard, "'s delayed return ability triggers."));
            }
        }

        // Delayed graveyard-to-battlefield-under-control returns (Seraph, Grave Betrayal, Endless Whispers)
        // are delayed triggered abilities: they go on the stack as the end step begins.
        if (gameData.hasDelayedAction(DelayedGraveyardToBattlefieldUnderControl.class)) {
            List<DelayedGraveyardToBattlefieldUnderControl> pendingReturns =
                    gameData.drainDelayedActions(DelayedGraveyardToBattlefieldUnderControl.class);
            for (DelayedGraveyardToBattlefieldUnderControl pending : pendingReturns) {
                Card returningCard = gameQueryService.findCardInGraveyardById(gameData, pending.cardId());
                if (returningCard == null) {
                    log.info("Game {} - Delayed return under control for card {} skipped (no longer in a graveyard)",
                            gameData.id, pending.cardId());
                    continue;
                }
                Permanent sourcePermanent = pending.sourcePermanentId() == null
                        ? null : gameQueryService.findPermanentById(gameData, pending.sourcePermanentId());
                Card sourceCard = sourcePermanent != null ? sourcePermanent.getCard() : returningCard;
                StackEntry entry = TriggeredAbilityStackService.madeAbility(
                        sourceCard,
                        pending.triggerControllerId(),
                        sourceCard.getName() + "'s delayed return ability",
                        new ArrayList<>(List.of(pending.optionalReturn()
                                ? new MayEffect(new ReturnDelayedGraveyardCardUnderControlEffect(pending),
                                        "Return " + returningCard.getName() + " to the battlefield?")
                                : new ReturnDelayedGraveyardCardUnderControlEffect(pending))));
                entry.setNonTargeting(true);
                entry.setTriggeringCardId(pending.cardId());
                triggeredAbilityStackService.putDelayed(gameData, entry);
                gameLogService.append(gameData,
                        GameLog.cardThen(sourceCard, "'s delayed return ability triggers."));
            }
        }

        UUID activePlayerId = gameData.activePlayerId;

        // "At the beginning of your end step" from a card that remains in its owner's command zone (secret missions
        // and similar cards).
        List<Card> commandZone = gameData.playerCommandZones.get(activePlayerId);
        if (commandZone != null) {
            for (Card card : new ArrayList<>(commandZone)) {
                if (gameData.faceDownCommandZoneCards.contains(card.getId())) {
                    continue;
                }
                ConditionContext context = ConditionContext.forCard(card, activePlayerId);
                for (TriggeredAbility ability : printedAbilities(gameData, card, YOUR_END_STEP, Zone.COMMAND,
                        context)) {
                    putStepAbility(gameData, ability, card, null, activePlayerId, "end step ability", context,
                            entry -> { });
                }
            }
        }

        List<UUID> triggerOrder = new ArrayList<>();
        triggerOrder.add(activePlayerId);
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(activePlayerId)) {
                triggerOrder.add(playerId);
            }
        }

        // "At the beginning of each end step" of every permanent and face-up scheme: "that player" is the player whose
        // end step it is; an intervening "if" (Sygg, River Cutthroat's, Colfenor's Urn's) is judged as the ability
        // triggers and again as it resolves (CR 603.4).
        for (UUID playerId : triggerOrder) {
            for (Permanent perm : gameData.permanentsAndFaceUpSchemes(playerId)) {
                if (perm.isFaceDown() || gameQueryService.hasLostPrintedAbilities(gameData, perm)) continue;
                ConditionContext endStepContext = ConditionContext.forPermanent(perm, playerId)
                        .withTargetId(activePlayerId);
                List<TriggeredAbility> endStepAbilities = new ArrayList<>();
                // A Curse's "each end step" ability acts on the enchanted player; the curse loop below collects it.
                if (!enchantsPlayer(gameData, perm)) {
                    endStepAbilities.addAll(printedAbilities(gameData, perm.getCard(), EACH_END_STEP,
                            Zone.BATTLEFIELD, endStepContext));
                }
                endStepAbilities.addAll(stepAbilityReader.triggering(gameData,
                        StepAbilityReader.persistent(perm, EACH_END_STEP), endStepContext));
                for (TriggeredAbility ability : endStepAbilities) {
                    putStepAbility(gameData, ability, perm.getCard(), perm, playerId, "end step ability",
                            endStepContext, stepPlayerRecord(activePlayerId, perm));
                }
            }
        }

        // Triggers.endStep(ActivePlayer.ANY) from the command zone: "At the beginning of each end step" from a face-up
        // card in any player's command zone — fires at every end step, in APNAP order.
        for (UUID playerId : triggerOrder) {
            List<Card> playerCommandZone = gameData.playerCommandZones.get(playerId);
            if (playerCommandZone == null) continue;

            for (Card card : new ArrayList<>(playerCommandZone)) {
                if (gameData.faceDownCommandZoneCards.contains(card.getId())) {
                    continue;
                }
                ConditionContext context = ConditionContext.forCard(card, playerId);
                for (TriggeredAbility ability : printedAbilities(gameData, card, EACH_END_STEP, Zone.COMMAND,
                        context)) {
                    putStepAbility(gameData, ability, card, null, playerId, "end step ability", context,
                            entry -> { });
                }
            }
        }

        // Triggers.endStep(ActivePlayer.ANY) from the graveyard: "At the beginning of the end step, if this card is in
        // your graveyard …" — fires at every end step, from any player's graveyard, in APNAP order. The record keeps
        // this graveyard stay (CR 400.7): a card that has left and re-entered the graveyard is a new object.
        for (UUID playerId : triggerOrder) {
            List<Card> playerGraveyard = gameData.playerGraveyards.get(playerId);
            if (playerGraveyard == null) continue;

            for (Card card : new ArrayList<>(playerGraveyard)) {
                ConditionContext graveyardContext = ConditionContext.forCard(card, playerId);
                List<TriggeredAbility> graveyardEndStepAbilities = new ArrayList<>(
                        graveyardAbilities(gameData, card, EACH_END_STEP, graveyardContext));
                if (playerId.equals(activePlayerId)) {
                    graveyardEndStepAbilities.addAll(graveyardAbilities(gameData, card, YOUR_END_STEP,
                            graveyardContext));
                }
                long graveyardVersion = gameData.graveyardEntryVersion(card.getId());
                for (TriggeredAbility ability : graveyardEndStepAbilities) {
                    putStepAbility(gameData, ability, card, null, playerId, "end step ability", graveyardContext,
                            entry -> {
                                entry.setTriggeringCardId(card.getId());
                                entry.setTriggeringCardGraveyardEntryVersion(graveyardVersion);
                            });
                }
            }
        }

        // End-step boons trigger before the active player's permanent abilities. This preserves
        // the normal stack ordering for a boon and a permanent ability that both trigger here:
        // the permanent ability resolves first while the boon still exists.
        for (Boon boon : List.copyOf(gameData.boons)) {
            if (boon.trigger() != BoonTrigger.CONTROLLER_END_STEP
                    || !boon.controllerId().equals(activePlayerId)) {
                continue;
            }
            if (boon.effect() instanceof ConditionalEffect conditional
                    && !conditionEvaluationService.isMet(gameData, conditional.condition(),
                    ConditionContext.forCard(boon.sourceCard(), activePlayerId))) {
                continue;
            }
            StackEntry trigger = TriggeredAbilityStackService.madeAbility(
                    boon.sourceCard(),
                    boon.controllerId(),
                    boon.sourceCard().getName() + "'s boon",
                    new ArrayList<>(List.of(boon.effect())));
            trigger.setNonTargeting(true);
            triggeredAbilityStackService.putDelayed(gameData, trigger);
            gameLogService.append(gameData, GameLog.abilityTriggers(boon.sourceCard()));
        }

        // "At the beginning of your end step": only fires for the active player's permanents and face-up schemes. Each
        // goes on the stack with the source as it was (Obzedat, Ghost Council's "you may exile Obzedat"), the
        // permanent an Aura or Equipment is attached to ("enchanted artifact's controller", Gremlin Infestation, read
        // as it resolves and from that permanent's last-known information once it has left, CR 608.2h) and the
        // permanent that granted it (Nettlevine Blight's "attach Nettlevine Blight").
        List<Permanent> activeBattlefield = gameData.permanentsAndFaceUpSchemes(activePlayerId);
        if (activeBattlefield != null) {
            for (Permanent perm : activeBattlefield) {
                ConditionContext endStepContext = ConditionContext.forPermanent(perm, activePlayerId);
                List<TriggeredAbility> controllerEndStepAbilities = new ArrayList<>();
                if (!gameQueryService.hasLostAllAbilities(gameData, perm)) {
                    if (!perm.isFaceDown()) {
                        controllerEndStepAbilities.addAll(printedAbilities(gameData, perm.getCard(), YOUR_END_STEP,
                                Zone.BATTLEFIELD, endStepContext));
                    }
                    controllerEndStepAbilities.addAll(durationGrantedAbilities(gameData, perm, YOUR_END_STEP,
                            endStepContext));
                }
                controllerEndStepAbilities.addAll(staticGrantedAbilities(gameData, perm, YOUR_END_STEP,
                        endStepContext));
                for (TriggeredAbility ability : controllerEndStepAbilities) {
                    putStepAbility(gameData, ability, perm.getCard(), perm, activePlayerId, "end step ability",
                            endStepContext, entry -> {
                                entry.setActivePlayerId(activePlayerId);
                                entry.setSourcePermanentSnapshot(new Permanent(perm));
                                recordAttachedPermanent(gameData, perm, entry);
                            });
                }
            }
        }

        // Triggers.endStep(ActivePlayer.OPPONENT): "At the beginning of each opponent's end step, ..." Fires only during
        // the end step of a player who is an opponent of the permanent's controller (never the controller's own end
        // step); "that player" is the record's non-targeting target, which an intervening "if" reads (Predatory
        // Advantage).
        gameData.forEachBattlefield((playerId, playerBattlefield) -> {
            if (playerId.equals(activePlayerId)) return;

            for (Permanent perm : gameData.permanentsAndFaceUpSchemes(playerId)) {
                ConditionContext context = ConditionContext.forPermanent(perm, playerId).withTargetId(activePlayerId);
                for (TriggeredAbility ability : printedAbilities(gameData, perm.getCard(), OPPONENT_END_STEP,
                        Zone.BATTLEFIELD, context)) {
                    putStepAbility(gameData, ability, perm.getCard(), perm, playerId, "end step ability", context,
                            entry -> {
                                entry.setTargetId(activePlayerId);
                                entry.setNonTargeting(true);
                            });
                }
            }
        });

        // Auras with Triggers.endStep(ActivePlayer.ENCHANTED_PERMANENT_CONTROLLER): they fire during the enchanted
        // permanent's controller's end step and are controlled by the Aura's controller (Aggression, Lingering Death);
        // an ability an Aura grants is the enchanted permanent's own, read by the your-end-step reader (Nettlevine
        // Blight).
        gameData.forEachPermanent((auraOwnerId, perm) -> {
            ConditionContext auraContext = ConditionContext.forPermanent(perm, auraOwnerId);
            List<TriggeredAbility> enchantedControllerEndStepAbilities = printedAbilities(gameData, perm.getCard(),
                    ENCHANTED_CONTROLLER_END_STEP, Zone.BATTLEFIELD, auraContext);
            if (enchantedControllerEndStepAbilities.isEmpty()) return;
            if (!perm.isAttached()) return;

            UUID enchantedPermanentControllerId = gameQueryService.findPermanentController(gameData, perm.getAttachedTo());
            if (enchantedPermanentControllerId == null) return;
            if (!enchantedPermanentControllerId.equals(activePlayerId)) return;

            for (TriggeredAbility ability : enchantedControllerEndStepAbilities) {
                putStepAbility(gameData, ability, perm.getCard(), perm, auraOwnerId, "end step ability", auraContext,
                        entry -> {
                            entry.setTargetId(perm.getAttachedTo());
                            entry.setNonTargeting(true);
                            entry.setTriggeringPermanentId(perm.getAttachedTo());
                            entry.setTriggeringPermanentControllerId(enchantedPermanentControllerId);
                            entry.setSourcePermanentSnapshot(new Permanent(perm));
                        });
            }
        });

        // Curses with "at the beginning of each end step" abilities (Fraying Sanity's mill) and "at the beginning of
        // enchanted player's end step" ones: they act on the enchanted player, the record's non-targeting target.
        gameData.forEachPermanent((auraOwnerId, perm) -> {
            if (!enchantsPlayer(gameData, perm)) return;

            UUID enchantedPlayerId = perm.getAttachedTo();
            ConditionContext curseContext = ConditionContext.forPermanent(perm, auraOwnerId)
                    .withTargetId(enchantedPlayerId);
            List<TriggeredAbility> enchantedPlayerEndStepAbilities = new ArrayList<>(
                    printedAbilities(gameData, perm.getCard(), EACH_END_STEP, Zone.BATTLEFIELD, curseContext));
            if (enchantedPlayerId.equals(activePlayerId)) {
                enchantedPlayerEndStepAbilities.addAll(printedAbilities(gameData, perm.getCard(),
                        ENCHANTED_PLAYER_END_STEP, Zone.BATTLEFIELD, curseContext));
            }
            for (TriggeredAbility ability : enchantedPlayerEndStepAbilities) {
                putStepAbility(gameData, ability, perm.getCard(), perm, auraOwnerId, "end step ability", curseContext,
                        entry -> {
                            entry.setTargetId(enchantedPlayerId);
                            entry.setNonTargeting(true);
                        });
            }
        });

        beginNextStepTriggerChoice(gameData);
    }

    private void handleBedtimeStoryLoreCounters(GameData gameData) {
        UUID activePlayerId = gameData.activePlayerId;
        List<Permanent> battlefield = gameData.playerBattlefields.get(activePlayerId);
        if (battlefield == null) return;

        battlefield.stream()
                .filter(permanent -> permanent.getCard().isSaga()
                        && permanent.getCard().isBedtimeStory())
                .toList()
                .forEach(saga -> sagaChapterService.addLoreCounterAndTriggerChapter(
                        gameData, saga, saga.getCard(), activePlayerId));
    }

    private void expireNextEndStepTemporaryCopies(GameData gameData) {
        for (FloatingContinuousEffect expired : gameData.expireFloatingEffectsAtNextEndStep()) {
            if (expired.effect() instanceof TemporaryCopyEffect
                    && expired.affectedPermanentId() != null) {
                Permanent copy = gameQueryService.findPermanentById(gameData, expired.affectedPermanentId());
                if (copy != null) {
                    copy.revertEndOfTurnCopy();
                }
            }
        }
    }

    public void handleCleanupTriggers(GameData gameData) {
        List<RemoveCountersFromPermanentAtNextCleanup> pendingRemovals =
                gameData.drainDelayedActions(RemoveCountersFromPermanentAtNextCleanup.class);
        for (RemoveCountersFromPermanentAtNextCleanup action : pendingRemovals) {
            StackEntry entry = TriggeredAbilityStackService.madeAbility(
                    action.sourceCard(),
                    action.controllerId(),
                    action.sourceCard().getName() + "'s delayed ability",
                    new ArrayList<>(List.of(new RemoveCounterFromTargetPermanentEffect(
                            action.counterType(), null, action.amount()))),
                    action.permanentId(),
                    (UUID) null);
            entry.setNonTargeting(true);
            triggeredAbilityStackService.putDelayed(gameData, entry);
            gameLogService.append(gameData, GameLog.cardThen(action.sourceCard(),
                    "'s delayed ability triggers to remove counters."));
        }
    }

    /**
     * Scans the active player's battlefield, command zone, and graveyard for beginning-of-combat triggered
     * abilities and pushes them onto the stack.
     * {@code Triggers.beginningOfCombat(ActivePlayer.YOU)} fires only for the active player's permanents
     * (CR 507.1: "At the beginning of combat on your turn").
     * {@code Triggers.beginningOfCombat(ActivePlayer.ANY)} fires for every permanent on every
     * battlefield (e.g. Majestic Myriarch / Odric, Lunarch Marshal).
     * {@code Triggers.beginningOfCombat(ActivePlayer.OPPONENT)} fires only for permanents controlled by a
     * player other than the active player (Sentinel of the Eternal Watch).
     *
     * @param gameData the current game state to modify
     */
    public void handleBeginningOfCombatTriggers(GameData gameData) {
        if (gameData.planechase != null) {
            planechaseService.step(gameData, YOUR_BEGINNING_OF_COMBAT);
        }
        queueDelayedBeginningOfCombatTriggers(gameData);
        collectEmblemStepTriggers(gameData, StepTrigger.Moment.BEGINNING_OF_COMBAT, ActivePlayer.YOU);

        UUID activePlayerId = gameData.activePlayerId;
        List<Permanent> battlefield = gameData.permanentsAndFaceUpSchemes(activePlayerId);
        if (battlefield != null) {
            for (Permanent perm : battlefield) {
                ConditionContext combatContext = beginningOfCombatContext(gameData, perm, activePlayerId);
                List<TriggeredAbility> combatAbilities = new ArrayList<>(
                        perm.isFaceDown() || gameQueryService.hasLostPrintedAbilities(gameData, perm)
                                ? List.of()
                                : printedAbilities(gameData, perm.getCard(), YOUR_BEGINNING_OF_COMBAT,
                                        Zone.BATTLEFIELD, combatContext));
                combatAbilities.addAll(staticGrantedAbilities(gameData, perm, YOUR_BEGINNING_OF_COMBAT,
                        combatContext));
                putBeginningOfCombatAbilities(gameData, activePlayerId, perm.getCard(), perm, combatContext,
                        combatAbilities);
            }
        }

        List<Card> commandZone = gameData.playerCommandZones.get(activePlayerId);
        if (commandZone != null) {
            for (Card card : new ArrayList<>(commandZone)) {
                if (card.hasType(CardType.SCHEME) || gameData.faceDownCommandZoneCards.contains(card.getId())) {
                    continue;
                }
                ConditionContext context = ConditionContext.forCard(card, activePlayerId);
                putBeginningOfCombatAbilities(gameData, activePlayerId, card, null, context,
                        printedAbilities(gameData, card, YOUR_BEGINNING_OF_COMBAT, Zone.BATTLEFIELD, context));
            }
        }

        // Abilities of cards in the graveyard keep this graveyard stay (CR 400.7).
        List<Card> graveyard = gameData.playerGraveyards.get(activePlayerId);
        if (graveyard != null) {
            for (Card card : new ArrayList<>(graveyard)) {
                ConditionContext context = ConditionContext.forCard(card, activePlayerId);
                for (TriggeredAbility ability : graveyardAbilities(gameData, card, YOUR_BEGINNING_OF_COMBAT,
                        context)) {
                    putStepAbility(gameData, ability, card, null, activePlayerId, "combat ability", context,
                            graveyardStayRecord(gameData, card));
                }
            }
        }

        BiConsumer<UUID, Permanent> eachCombat = (playerId, perm) -> {
            ConditionContext combatContext = beginningOfCombatContext(gameData, perm, playerId);
            List<TriggeredAbility> combatAbilities = new ArrayList<>(printedAbilities(gameData, perm.getCard(),
                    EACH_BEGINNING_OF_COMBAT, Zone.BATTLEFIELD, combatContext));
            combatAbilities.addAll(staticGrantedAbilities(gameData, perm, EACH_BEGINNING_OF_COMBAT, combatContext));
            putBeginningOfCombatAbilities(gameData, playerId, perm.getCard(), perm, combatContext, combatAbilities);
        };
        gameData.forEachPermanent(eachCombat);
        gameData.forEachFaceUpSchemeSource(eachCombat);

        BiConsumer<UUID, Permanent> opponentCombat = (playerId, perm) -> {
            if (!playerId.equals(activePlayerId)) {
                ConditionContext combatContext = beginningOfCombatContext(gameData, perm, playerId);
                List<TriggeredAbility> combatAbilities = new ArrayList<>(printedAbilities(gameData, perm.getCard(),
                        OPPONENT_BEGINNING_OF_COMBAT, Zone.BATTLEFIELD, combatContext));
                combatAbilities.addAll(staticGrantedAbilities(gameData, perm, OPPONENT_BEGINNING_OF_COMBAT,
                        combatContext));
                putBeginningOfCombatAbilities(gameData, playerId, perm.getCard(), perm, combatContext,
                        combatAbilities);
            }
        };
        gameData.forEachPermanent(opponentCombat);
        gameData.forEachFaceUpSchemeSource(opponentCombat);

        triggerCollectionService.checkUntapAttackedCreaturesEachCombatThisTurnTriggers(gameData);

        beginNextStepTriggerChoice(gameData);
    }

    private void queueDelayedBeginningOfCombatTriggers(GameData gameData) {
        List<DelayedBeginningOfCombatTrigger> delayedTriggers =
                gameData.drainDelayedActions(DelayedBeginningOfCombatTrigger.class);
        for (DelayedBeginningOfCombatTrigger trigger : delayedTriggers) {
            // A delayed ability records nothing of an event; its targets, the groups its runtime card declares, are
            // chosen as it goes on the stack (CR 603.3d).
            triggeredAbilityStackService.putDelayed(gameData, TriggeredAbilityStackService.madeAbility(
                    trigger.sourceCard(), trigger.controllerId(), trigger.sourceCard().getName() + "'s delayed ability",
                    List.of(trigger.effect())));
            gameLogService.append(gameData,
                    GameLog.cardThen(trigger.sourceCard(), "'s delayed beginning of combat trigger triggers."));
        }
    }

    /**
     * The context a beginning-of-combat ability of {@code perm} is judged in: the permanent's, or its card's for a
     * face-up scheme, which isn't on the battlefield.
     */
    private ConditionContext beginningOfCombatContext(GameData gameData, Permanent perm, UUID controllerId) {
        return gameQueryService.findPermanentById(gameData, perm.getId()) != null
                ? ConditionContext.forPermanent(perm, controllerId)
                : ConditionContext.forCard(perm.getCard(), controllerId);
    }

    /**
     * Hands each of {@code abilities}, the beginning-of-combat abilities of {@code card} (of the permanent
     * {@code perm}, or of a card in the command zone for {@code null}) that trigger now, to the stack door, recording the
     * active player and the source as it was: the door chooses a mode ("choose one; if [condition], choose both
     * instead", judged as it triggers) or a mode at random ("choose one at random", Umaro, Raging Yeti), and targets,
     * every target group walked in turn when there are several or one takes more than one (Boros Battleshaper). An
     * Equipment's ability triggers whether or not it is attached ("if there is no equipped creature, no token is
     * created", Helm of the Host's ruling): what it does with the equipped creature is read as it resolves. A face-up
     * scheme, which isn't on the battlefield, is no source permanent.
     */
    private void putBeginningOfCombatAbilities(GameData gameData, UUID controllerId, Card card, Permanent perm,
                                               ConditionContext context, List<TriggeredAbility> abilities) {
        Permanent source = perm != null && gameQueryService.findPermanentById(gameData, perm.getId()) != null
                ? perm : null;
        for (TriggeredAbility ability : abilities) {
            putStepAbility(gameData, ability, card, source, controllerId, "combat ability", context, entry -> {
                entry.setActivePlayerId(gameData.activePlayerId);
                if (source != null) {
                    entry.setSourcePermanentSnapshot(new Permanent(source));
                }
            });
        }
    }

    private void queueDelayedPermanentExiles(GameData gameData, DelayedPermanentActionKind kind) {
        List<DelayedPermanentAction> actions = new ArrayList<>(gameData.drainDelayedActions(
                DelayedPermanentAction.class,
                action -> action.kind() == kind
                        && (action.controllerId() == null
                        || action.controllerId().equals(gameData.activePlayerId))));
        Map<List<Object>, List<DelayedPermanentAction>> sharedTriggers = new LinkedHashMap<>();
        for (DelayedPermanentAction action : actions) {
            if (action.sourceCard() != null) {
                sharedTriggers.computeIfAbsent(
                        List.of(action.sourceCard().getId(), Objects.requireNonNullElse(action.controllerId(), ""),
                                action.schedulingResolution()),
                        ignored -> new ArrayList<>()).add(action);
            }
        }
        for (List<DelayedPermanentAction> shared : sharedTriggers.values()) {
            if (shared.size() < 2) {
                continue;
            }
            // One delayed trigger of one resolution exiles everything it scheduled together (Storm Herald); two
            // resolutions of one source (Corpse Dance cast twice with buyback) are two delayed triggers (CR 603.7a).
            List<DelayedPermanentAction> present = shared.stream()
                    .filter(action -> gameQueryService.findPermanentById(gameData, action.permanentId()) != null)
                    .toList();
            actions.removeAll(shared);
            if (present.isEmpty()) {
                continue;
            }
            Permanent first = gameQueryService.findPermanentById(gameData, present.getFirst().permanentId());
            UUID triggerControllerId = present.getFirst().controllerId() != null
                    ? present.getFirst().controllerId()
                    : gameQueryService.findPermanentController(gameData, first.getId());
            StackEntry entry = TriggeredAbilityStackService.madeAbility(
                    present.getFirst().sourceCard(),
                    triggerControllerId,
                    present.getFirst().sourceCard().getName() + "'s delayed ability",
                    new ArrayList<>(present.stream()
                            .<CardEffect>map(com.github.laxika.magicalvibes.model.effect.ResolveDelayedPermanentActionEffect::new)
                            .toList()),
                    null,
                    first.getId());
            entry.setSourcePermanentSnapshot(new Permanent(first));
            entry.setNonTargeting(true);
            triggeredAbilityStackService.putDelayed(gameData, entry);
            gameLogService.append(gameData, GameLog.cardThen(present.getFirst().sourceCard(),
                    "'s delayed exile ability triggers."));
        }
        for (DelayedPermanentAction action : actions) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, action.permanentId());
            if (permanent == null) {
                continue;
            }
            // CR 603.7e: an ability that created the delayed trigger keeps controlling it (recorded as the
            // action's player), even if the permanent has since changed controllers.
            UUID controllerId = action.triggerControllerId() != null
                    ? action.triggerControllerId()
                    : action.sacrificingPlayerId() != null
                    ? action.sacrificingPlayerId()
                    : action.controllerId() != null
                    ? action.controllerId()
                    : gameQueryService.findPermanentController(gameData, permanent.getId());
            Card triggerCard = action.sourceCard() != null ? action.sourceCard() : permanent.getCard();
            StackEntry entry = TriggeredAbilityStackService.madeAbility(
                    triggerCard,
                    controllerId,
                    triggerCard.getName() + "'s delayed ability",
                    new ArrayList<>(List.of(new ExileTargetPermanentEffect())),
                    permanent.getId(),
                    action.triggerSourcePermanentId() != null ? action.triggerSourcePermanentId() : permanent.getId());
            entry.setNonTargeting(true);
            triggeredAbilityStackService.putDelayed(gameData, entry);
            gameLogService.append(gameData,
                    GameLog.cardThen(permanent.getCard(), "'s delayed exile ability triggers."));
        }
    }

    /**
     * A delayed sacrifice fires at the end step of the turn after it was registered (or any end step when
     * registered without a turn number). A "your end step" sacrifice is only due during an end step of the
     * permanent's current controller.
     */
    private boolean isSacrificeAtNextEndStepDue(GameData gameData, SacrificeSelfAtNextEndStepTrigger action) {
        UUID dueControllerId;
        if (action.currentControllersEndStepOnly()) {
            if (gameQueryService.findPermanentById(gameData, action.permanentId()) == null) {
                return true;
            }
            dueControllerId = gameQueryService.findPermanentController(gameData, action.permanentId());
            if (dueControllerId == null || !dueControllerId.equals(gameData.activePlayerId)) {
                return false;
            }
            return action.registeredTurnNumber() == null || action.registeredTurnNumber() < gameData.turnNumber;
        }
        return action.registeredTurnNumber() == null
                || (action.registeredTurnNumber() < gameData.turnNumber
                && action.controllerId() != null
                && action.controllerId().equals(gameData.activePlayerId));
    }

}
