package com.github.laxika.magicalvibes.service.turn;
import com.github.laxika.magicalvibes.model.action.AddManaAtNextMainPhase;
import com.github.laxika.magicalvibes.model.action.DelayedGraveyardToBattlefieldSelfReturn;
import com.github.laxika.magicalvibes.model.action.DelayedGraveyardToBattlefieldTransformedReturn;
import com.github.laxika.magicalvibes.model.action.DelayedGraveyardToBattlefieldUnderControl;
import com.github.laxika.magicalvibes.model.action.DelayedGraveyardCardsToBattlefieldUnderControl;
import com.github.laxika.magicalvibes.model.action.DelayedGraveyardToHandReturn;
import com.github.laxika.magicalvibes.model.action.DelayedReturnAuraAttachedToPermanent;
import com.github.laxika.magicalvibes.model.action.DelayedEndOfCombatTrigger;
import com.github.laxika.magicalvibes.model.action.DelayedBeginningOfCombatTrigger;
import com.github.laxika.magicalvibes.model.action.DelayedCreateToken;
import com.github.laxika.magicalvibes.model.action.DelayedCreateTokenAtNextUpkeep;
import com.github.laxika.magicalvibes.model.action.DelayedCreateTokenCopy;
import com.github.laxika.magicalvibes.model.action.DelayedExileCreatedPermanentsAtEndStep;
import com.github.laxika.magicalvibes.model.action.DelayedChooseOpponentGainsControlOfSource;
import com.github.laxika.magicalvibes.model.action.DiscardCardsAtNextEndStep;
import com.github.laxika.magicalvibes.model.action.ExileCardsFromOwnGraveyardAtNextEndStep;
import com.github.laxika.magicalvibes.model.action.DelayedDestroyAllPermanents;
import com.github.laxika.magicalvibes.model.action.DelayedLoseLifeAndReturnFromGraveyard;
import com.github.laxika.magicalvibes.model.action.DelayedSacrificeTargetPermanentAtEndStep;
import com.github.laxika.magicalvibes.model.action.DelayedSacrificeTargetPermanentAtEndStepIfManaValueAtMost;
import com.github.laxika.magicalvibes.model.action.DelayedCoinFlipSacrificeTargetPermanentAtEndStep;
import com.github.laxika.magicalvibes.model.action.DelayedUntapPermanents;
import com.github.laxika.magicalvibes.model.action.DamageAtNextUpkeepUnlessPays;
import com.github.laxika.magicalvibes.model.action.DamageForCardsStillExiledAtNextEndStep;
import com.github.laxika.magicalvibes.model.action.PoisonAtNextUpkeepUnlessPays;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextUpkeep;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextEndStep;
import com.github.laxika.magicalvibes.model.action.PutCounterOnPermanentAtNextEndStep;
import com.github.laxika.magicalvibes.model.action.UnattachEquipmentAtNextEndStep;
import com.github.laxika.magicalvibes.model.action.SacrificeSelfAtNextEndStepTrigger;
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
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveTimeCounterFromExiledCardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveScreamCounterFromExiledCardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseModeNotYetChosenEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOpponentGainsControlOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.effect.TargetSpec;
import com.github.laxika.magicalvibes.model.effect.TransformToBackFaceEffect;
import com.github.laxika.magicalvibes.model.effect.GrantChosenLandwalkEffect;
import com.github.laxika.magicalvibes.model.effect.UnattachEquipmentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.action.DelayedPlusOneCounters;
import com.github.laxika.magicalvibes.model.action.DelayedPlusZeroPlusOneCounters;
import com.github.laxika.magicalvibes.model.action.RemoveCounterFromPermanentAtNextEndStep;
import com.github.laxika.magicalvibes.model.action.RemoveCountersFromPermanentAtNextCleanup;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.model.action.DestroyNonAttackersAtEndStep;
import com.github.laxika.magicalvibes.model.action.DestroyPermanentIfDidNotAttackAtEndStep;
import com.github.laxika.magicalvibes.model.action.ExilePermanentAtControllerEndStep;
import com.github.laxika.magicalvibes.model.action.LoseGameAtEndStep;
import com.github.laxika.magicalvibes.model.action.ReturnExiledCardToHandAtEndStep;
import com.github.laxika.magicalvibes.model.action.ReturnExiledCardToHandAtNextEndStep;
import com.github.laxika.magicalvibes.model.action.EachPlayerHandExileReturnAtNextEndStep;
import com.github.laxika.magicalvibes.model.action.TargetPlayerHandExileReturnAtNextTurnEndStep;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.effect.EmblemStepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.EmblemTriggerStep;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.OpeningHandRevealTrigger;
import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.paradigm.ParadigmService;
import com.github.laxika.magicalvibes.service.epic.EpicService;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.trigger.TriggerTargetCollector;
import com.github.laxika.magicalvibes.service.target.ValidTargetService;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyOneOfTargetsAtRandomEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.condition.APlayerControlsMoreCreaturesThanEachOtherPlayer;
import com.github.laxika.magicalvibes.model.condition.APlayerHasMoreCardsInHandThanEachOtherPlayer;
import com.github.laxika.magicalvibes.model.condition.ActivePlayerHandAtLeast;
import com.github.laxika.magicalvibes.model.condition.ActivePlayerHandAtMost;
import com.github.laxika.magicalvibes.model.condition.ActivePlayerHandEmpty;
import com.github.laxika.magicalvibes.model.condition.AnyPlayerControlsPermanentCount;
import com.github.laxika.magicalvibes.model.condition.AnyPlayerControlsPermanentCountAtMost;
import com.github.laxika.magicalvibes.model.condition.CardsInHandAtLeast;
import com.github.laxika.magicalvibes.model.condition.CardsInLibraryAtLeast;
import com.github.laxika.magicalvibes.model.condition.ControllerLifeAtLeast;
import com.github.laxika.magicalvibes.model.condition.ControllerLifeAtMost;
import com.github.laxika.magicalvibes.model.condition.ControllerCastTwoOrMoreSpellsThisTurn;
import com.github.laxika.magicalvibes.model.condition.ControllerDrewAtLeastCardsThisTurn;
import com.github.laxika.magicalvibes.model.condition.Coven;
import com.github.laxika.magicalvibes.model.condition.EachPlayerLifeAtMost;
import com.github.laxika.magicalvibes.model.condition.ControlsEachCreatureWithGreatestPower;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCountAtMost;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentsWithDifferentNames;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.condition.CardsLeftGraveyardThisTurn;
import com.github.laxika.magicalvibes.model.condition.CreatureDiedUnderYourControlThisTurn;
import com.github.laxika.magicalvibes.model.condition.CreatureDiedUnderOpponentControlThisTurn;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.AnotherPermanentEnteredLastTurn;
import com.github.laxika.magicalvibes.model.condition.DidntActivateLoyaltyAbilityThisTurn;
import com.github.laxika.magicalvibes.model.condition.DidntAttack;
import com.github.laxika.magicalvibes.model.condition.GainedLifeThisTurn;
import com.github.laxika.magicalvibes.model.condition.AnOpponentHandEmpty;
import com.github.laxika.magicalvibes.model.condition.CardDirectlyAboveSelfInGraveyard;
import com.github.laxika.magicalvibes.model.condition.CardsAboveSelfInGraveyard;
import com.github.laxika.magicalvibes.model.condition.Metalcraft;
import com.github.laxika.magicalvibes.model.condition.MaxSpeed;
import com.github.laxika.magicalvibes.model.condition.Morbid;
import com.github.laxika.magicalvibes.model.condition.NoOtherPermanent;
import com.github.laxika.magicalvibes.model.condition.SourceRegeneratedThisTurn;
import com.github.laxika.magicalvibes.model.condition.NotControllerTurn;
import com.github.laxika.magicalvibes.model.condition.NotKicked;
import com.github.laxika.magicalvibes.model.condition.Raid;
import com.github.laxika.magicalvibes.model.condition.SelfDealtDamageToOpponentThisTurn;
import com.github.laxika.magicalvibes.model.condition.SelfWasDealtDamageThisTurn;
import com.github.laxika.magicalvibes.model.condition.SourceDamagedCreatureDiedThisTurn;
import com.github.laxika.magicalvibes.model.effect.AllPermanentsUpkeepSacrificeUnlessPayEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachTargetPlayerDrawsCardsEqualToAttachedCountEffect;
import com.github.laxika.magicalvibes.model.effect.MaySkipDrawReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinWinEffect;
import com.github.laxika.magicalvibes.model.effect.DamageDealingEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.UpkeepPlayerDependentEffect;
import com.github.laxika.magicalvibes.model.effect.DamageTargetPlayerOrPlaneswalkerUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.GivePoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PoisonRecipient;
import com.github.laxika.magicalvibes.model.effect.PayEchoCost;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.effect.ControllerLosesGameEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ReplaceSingleDrawEffect;
import com.github.laxika.magicalvibes.model.effect.SkipDrawStepEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersSkipUpkeepStepEffect;
import com.github.laxika.magicalvibes.model.effect.SkipStepOrPhaseKind;
import com.github.laxika.magicalvibes.model.DrawReplacementKind;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.GrantedTriggeredAbilitySupport;
import com.github.laxika.magicalvibes.service.effect.GrantedUpkeepEffectSupport;
import com.github.laxika.magicalvibes.model.effect.DealDamageIfDidntCastSpellThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEndStepPlayerIfLifeAtMostEffect;
import com.github.laxika.magicalvibes.model.effect.EndStepPlayerTargetedEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageIfFewCardsInHandEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyRandomOpponentPermanentWithCounterEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlIfSubtypesDealtCombatDamageEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.DrawUpToNCardsEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsControlOfEnchantedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentDrawStepOnlyEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureControllerLosesLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ExchangeControlOfTargetPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.effect.PregameBattlefieldChoiceEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.effect.ReboundCastFromExileEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayRevealSubtypeFromHandEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnUntapLockedPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCreatedPermanentsAtEndStepUnlessConditionEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveDelayCounterFromExiledSpellEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveSuspendCounterFromExiledSpellEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveEggCounterFromExileAndReturnEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveRefineCounterFromExiledCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfAndReturnCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEachPlayerHandAndReturnExiledCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnExiledCardsToTargetPlayerHandEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesGameEffect;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GraveyardTransformedReturnService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.ETBTokenTargetService;
import com.github.laxika.magicalvibes.service.battlefield.GraveyardTargetingService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Collects and processes triggered abilities that fire at the beginning of
 * specific turn steps: upkeep, draw, precombat main, and end step.
 *
 * <p>Extracted from {@code TurnProgressionService} to isolate the trigger-
 * scanning logic.  For each step the service iterates the relevant
 * {@link EffectSlot}s on permanents (and graveyards, for upkeep triggers),
 * pushes {@link StackEntry}s onto the stack, and queues
 * {@link MayEffect}/{@link MayPayManaEffect} choices as needed.
 *
 * <p>Also handles the Chancellor cycle's opening-hand reveal triggers on
 * the first upkeep, and upkeep copy-trigger target selection (CR 603.3d).
 */
@Slf4j
@Service
public class StepTriggerService {

    private static final PermanentIsLandPredicate LAND_PREDICATE = new PermanentIsLandPredicate();

    private final DrawService drawService;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final ConditionEvaluationService conditionEvaluationService;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final PermanentRemovalService permanentRemovalService;
    private final LifeSupport lifeSupport;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GraveyardTransformedReturnService graveyardTransformedReturnService;
    private final GraveyardTargetingService graveyardTargetingService;
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

    public StepTriggerService(DrawService drawService,
                              GameQueryService gameQueryService,
                              PredicateEvaluationService predicateEvaluationService,
                              ConditionEvaluationService conditionEvaluationService,
                              GameLogService gameLogService,
                              PlayerInputService playerInputService,
                              PermanentRemovalService permanentRemovalService,
                              LifeSupport lifeSupport,
                              BattlefieldEntryService battlefieldEntryService,
                              GraveyardTransformedReturnService graveyardTransformedReturnService,
                              GraveyardTargetingService graveyardTargetingService,
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
                              AmountEvaluationService amountEvaluationService) {
        this.drawService = drawService;
        this.gameQueryService = gameQueryService;
        this.predicateEvaluationService = predicateEvaluationService;
        this.conditionEvaluationService = conditionEvaluationService;
        this.gameLogService = gameLogService;
        this.playerInputService = playerInputService;
        this.permanentRemovalService = permanentRemovalService;
        this.lifeSupport = lifeSupport;
        this.battlefieldEntryService = battlefieldEntryService;
        this.graveyardTransformedReturnService = graveyardTransformedReturnService;
        this.graveyardTargetingService = graveyardTargetingService;
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
    }

    private record GrantedUpkeepSacrifice(AllPermanentsUpkeepSacrificeUnlessPayEffect effect,
                                          UUID sourcePermanentId) {}

    /**
     * Scans battlefields, graveyards, and (on turn 1) hands for upkeep-triggered
     * abilities and pushes them onto the stack or queues may-ability prompts.
     *
     * <p>Handles slots: {@code UPKEEP_TRIGGERED}, {@code GRAVEYARD_UPKEEP_TRIGGERED},
     * {@code EACH_UPKEEP_TRIGGERED}, {@code OPPONENT_UPKEEP_TRIGGERED},
     * {@code ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED}, and
     * {@code ON_OPENING_HAND_REVEAL} (Chancellor cycle, turn 1 only).
     *
     * @param gameData the current game state to modify
     */
    /**
     * Puts step-triggered emblem abilities onto the stack. Emblems are never removed from the game;
     * the step determines whether the active player must be the emblem's controller or an opponent.
     */
    private void collectEmblemStepTriggers(GameData gameData, EmblemTriggerStep step) {
        for (Emblem emblem : gameData.emblems) {
            boolean isControllerStep = gameData.activePlayerId.equals(emblem.controllerId());
            boolean opponentStep = step == EmblemTriggerStep.OPPONENT_UPKEEP
                    || step == EmblemTriggerStep.OPPONENT_DRAW_STEP;
            if ((opponentStep && isControllerStep) || (!opponentStep && !isControllerStep)) {
                continue;
            }
            for (CardEffect effect : emblem.staticEffects()) {
                if (!(effect instanceof EmblemStepTriggerEffect upkeepTrigger) || upkeepTrigger.step() != step) {
                    continue;
                }
                Card source = emblem.sourceCard();
                String description = (source != null ? source.getName() : "Emblem") + "'s emblem";
                boolean needsGraveyardTarget = upkeepTrigger.effects().stream()
                        .anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD));
                if (needsGraveyardTarget) {
                    gameData.queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                            source, emblem.controllerId(), new ArrayList<>(upkeepTrigger.effects())));
                    gameLogService.append(gameData, GameLog.text(
                            description + " triggers: \"" + upkeepTrigger.reminderText() + "\""));
                    log.info("Game {} - {} {} emblem trigger queued for graveyard target selection",
                            gameData.id, description, step);
                    continue;
                }
                boolean needsPermanentTarget = upkeepTrigger.effects().stream()
                        .anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.PERMANENT));
                if (needsPermanentTarget) {
                    gameData.queueInteraction(new PermanentChoiceContext.EmblemTriggerTarget(
                            description, emblem.controllerId(), new ArrayList<>(upkeepTrigger.effects()), source));
                    gameLogService.append(gameData, GameLog.text(
                            description + " triggers: \"" + upkeepTrigger.reminderText() + "\""));
                    log.info("Game {} - {} {} emblem trigger queued for permanent target selection",
                            gameData.id, description, step);
                    continue;
                }
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY, source, emblem.controllerId(), description,
                        new ArrayList<>(upkeepTrigger.effects()),
                        opponentStep ? gameData.activePlayerId : null,
                        (UUID) null);
                entry.setNonTargeting(true);
                gameData.stack.add(entry);
                gameLogService.append(gameData, GameLog.text(
                        description + " triggers: \"" + upkeepTrigger.reminderText() + "\""));
                log.info("Game {} - {} {} emblem trigger pushed onto stack", gameData.id, description, step);
            }
        }
    }

    public void handleUpkeepTriggers(GameData gameData) {
        // "… until your next upkeep" (Cycle of Life): the floating layer-7b effect ends as the
        // upkeep begins, before the delayed trigger below puts its counter on the creature.
        gameData.expireFloatingEffectsAtUpkeep(gameData.activePlayerId);
        gameData.forEachPermanent((playerId, permanent) ->
                permanent.clearUntilNextUpkeepTriggeredEffects(gameData.activePlayerId));
        gameData.phasedOutPermanents.values().forEach(permanents -> permanents.forEach(permanent ->
                permanent.clearUntilNextUpkeepTriggeredEffects(gameData.activePlayerId)));

        // Spatial Binding: "Until your next upkeep, target permanent can't phase out." Phasing is a
        // turn-based action of the untap step (CR 502.1), which has already passed, so clearing here
        // still protected the permanent through the marking player's own untap step.
        expireCantPhaseOut(gameData);

        collectEmblemStepTriggers(gameData, EmblemTriggerStep.UPKEEP);
        collectEmblemStepTriggers(gameData, EmblemTriggerStep.OPPONENT_UPKEEP);
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
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY, action.sourceCard(), action.controllerId(),
                        action.sourceCard().getName() + "'s delayed ability",
                        new ArrayList<>(List.of(new PutCounterOnTargetPermanentEffect(
                                action.counterType(), action.amount()))),
                        action.permanentId(), (UUID) null);
                entry.setNonTargeting(true);
                gameData.stack.add(entry);
                gameLogService.append(gameData, GameLog.cardThen(action.sourceCard(),
                        "'s delayed ability triggers."));
                log.info("Game {} - {} delayed upkeep counter trigger pushed onto stack",
                        gameData.id, action.sourceCard().getName());
            }
        }

        if (gameData.hasDelayedAction(ExilePermanentAtNextUpkeep.class)) {
            List<ExilePermanentAtNextUpkeep> pendingExiles = gameData.drainDelayedActions(
                    ExilePermanentAtNextUpkeep.class, a -> a.controllerId().equals(gameData.activePlayerId));
            for (ExilePermanentAtNextUpkeep action : pendingExiles) {
                if (gameQueryService.findPermanentById(gameData, action.permanentId()) == null) {
                    continue;
                }
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY, action.sourceCard(), action.controllerId(),
                        action.sourceCard().getName() + "'s delayed ability",
                        new ArrayList<>(List.of(new ExileTargetPermanentEffect())),
                        action.permanentId(), (UUID) null);
                entry.setNonTargeting(true);
                gameData.stack.add(entry);
                gameLogService.append(gameData, GameLog.cardThen(action.sourceCard(),
                        "'s delayed ability triggers."));
                log.info("Game {} - {} delayed upkeep exile trigger pushed onto stack",
                        gameData.id, action.sourceCard().getName());
            }
        }

        if (gameData.hasDelayedAction(DelayedCreateTokenAtNextUpkeep.class)) {
            List<DelayedCreateTokenAtNextUpkeep> pendingTokens = gameData.drainDelayedActions(
                    DelayedCreateTokenAtNextUpkeep.class,
                    action -> action.controllerId().equals(gameData.activePlayerId));
            for (DelayedCreateTokenAtNextUpkeep action : pendingTokens) {
                int amount = amountEvaluationService.evaluate(gameData, action.tokenEffect().amount(),
                        new AmountContext(action.controllerId(), null, null, 0, 0));
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY, action.sourceCard(), action.controllerId(),
                        action.sourceCard().getName() + "'s delayed ability",
                        new ArrayList<>(List.of(action.tokenEffect().withAmount(amount))));
                entry.setNonTargeting(true);
                gameData.stack.add(entry);
                gameLogService.append(gameData, GameLog.cardThen(action.sourceCard(),
                        "'s delayed ability triggers."));
                log.info("Game {} - {} delayed upkeep token creation trigger pushed onto stack",
                        gameData.id, action.sourceCard().getName());
            }
        }

        // Phytotitan: "return it to the battlefield tapped under its owner's control at the beginning
        // of their next upkeep" — only at the owner's own upkeep.
        resolveDelayedSelfReturns(gameData,
                pending -> pending.atNextUpkeep() && pending.ownerId().equals(gameData.activePlayerId));
        resolveDelayedGraveyardCardsUnderControlAtUpkeep(gameData);

        // Delayed "draw N cards at the beginning of the next turn's upkeep" (e.g. Library of Lat-Nam).
        // Drained regardless of who the active player is — the scheduling player draws.
        if (gameData.hasDelayedAction(DrawCardsAtNextUpkeep.class)) {
            List<DrawCardsAtNextUpkeep> pendingDraws = gameData.drainDelayedActions(DrawCardsAtNextUpkeep.class);
            for (DrawCardsAtNextUpkeep pending : pendingDraws) {
                String playerName = gameData.playerIdToName.get(pending.controllerId());
                if (pending.upTo()) {
                    // Arcane Denial: "may draw up to two cards" — a delayed triggered ability that uses
                    // the stack, controlled by the drawing player so they make the choice.
                    StackEntry entry = new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY, pending.sourceCard(), pending.controllerId(),
                            pending.sourceCard().getName() + "'s delayed ability",
                            new ArrayList<>(List.of(new DrawUpToNCardsEffect(pending.count()))),
                            (UUID) null, (UUID) null);
                    entry.setNonTargeting(true);
                    gameData.stack.add(entry);
                    gameLogService.append(gameData, GameLog.cardThen(pending.sourceCard(),
                            "'s delayed ability triggers."));
                    log.info("Game {} - {} may draw up to {} from delayed upkeep trigger ({})",
                            gameData.id, playerName, pending.count(), pending.sourceCard().getName());
                    continue;
                }
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY, pending.sourceCard(), pending.controllerId(),
                        pending.sourceCard().getName() + "'s delayed ability",
                        new ArrayList<>(List.of(new DrawCardEffect(pending.count()))),
                        (UUID) null, (UUID) null);
                entry.setNonTargeting(true);
                gameData.stack.add(entry);
                gameLogService.append(gameData, GameLog.cardThen(pending.sourceCard(),
                        "'s delayed ability triggers."));
                log.info("Game {} - {} delayed upkeep draw trigger pushed onto stack for {} card(s) ({})",
                        gameData.id, playerName, pending.count(), pending.sourceCard().getName());
            }
        }

        if (gameData.hasDelayedAction(EchoAtNextUpkeep.class)) {
            List<EchoAtNextUpkeep> pendingEchoes = gameData.drainDelayedActions(EchoAtNextUpkeep.class);
            for (EchoAtNextUpkeep action : pendingEchoes) {
                UUID controllerId = gameQueryService.findPermanentController(gameData, action.permanentId());
                if (controllerId == null) {
                    continue;
                }
                if (!controllerId.equals(gameData.activePlayerId)) {
                    gameData.queueDelayedAction(action);
                    continue;
                }

                String echoCost = action.manaCost();
                if (action.dynamicManaCost() != null) {
                    Permanent source = gameQueryService.findPermanentById(gameData, action.permanentId());
                    int amount = amountEvaluationService.evaluate(gameData, action.dynamicManaCost(),
                            new AmountContext(controllerId, source, null, 0, 0));
                    echoCost = "{" + Math.max(0, amount) + "}";
                }
                ForcedCostOrElseEffect payEchoOrSacrifice = new ForcedCostOrElseEffect(
                        new PayEchoCost(echoCost, action.handCardCost(), action.cost()),
                        new ArrayList<>(List.of(new SacrificeSelfEffect())),
                        true, action.paidEffects());
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        action.sourceCard(),
                        controllerId,
                        action.sourceCard().getName() + "'s echo ability",
                        new ArrayList<>(List.of(payEchoOrSacrifice)),
                        (UUID) null,
                        action.permanentId());
                entry.setNonTargeting(true);
                gameData.stack.add(entry);
                gameLogService.append(gameData, GameLog.cardThen(action.sourceCard(), "'s echo ability triggers."));
                log.info("Game {} - {} echo trigger pushed onto stack", gameData.id, action.sourceCard().getName());
            }
        }

        // Quenchable Fire: "It deals an additional N damage to that player or planeswalker at the
        // beginning of your next upkeep step unless that player or that planeswalker's controller pays
        // {cost} before that step." Fires only at the spell controller's own upkeep; the paying party
        // is the targeted player, or the targeted planeswalker's controller (skip if the target is
        // gone). Pushed onto the stack as a "you may pay; if you don't, take damage" trigger.
        if (gameData.hasDelayedAction(DamageAtNextUpkeepUnlessPays.class)) {
            List<DamageAtNextUpkeepUnlessPays> pending = gameData.drainDelayedActions(
                    DamageAtNextUpkeepUnlessPays.class, a -> a.spellControllerId().equals(gameData.activePlayerId));
            for (DamageAtNextUpkeepUnlessPays action : pending) {
                UUID payerId = gameData.playerIds.contains(action.targetId())
                        ? action.targetId()
                        : gameQueryService.findPermanentController(gameData, action.targetId());
                if (payerId == null) continue; // targeted planeswalker (or player) is gone — trigger fizzles

                DamageTargetPlayerOrPlaneswalkerUnlessPaysEffect effect =
                        new DamageTargetPlayerOrPlaneswalkerUnlessPaysEffect(action.damage(), action.manaCost());
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY, action.sourceCard(), payerId,
                        action.sourceCard().getName() + "'s delayed ability",
                        new ArrayList<>(List.of(effect)), action.targetId(), (UUID) null));

                gameLogService.append(gameData, GameLog.text(action.sourceCard().getName()
                        + "'s delayed ability triggers — pay " + action.manaCost() + " or take "
                        + action.damage() + " damage."));
                log.info("Game {} - {} delayed upkeep pay-or-take-damage trigger pushed for {}",
                        gameData.id, action.sourceCard().getName(), gameData.playerIdToName.get(payerId));
            }
        }

        // Sabertooth Cobra: "The player gets another poison counter at the beginning of their next
        // upkeep unless they pay {2} before that step." Delayed trigger keyed to the damaged player's
        // own upkeep — fired here as a "you may pay {2}; if you don't, get a poison counter" prompt
        // controlled by that player (paying avoids the counter, declining incurs it).
        if (gameData.hasDelayedAction(PoisonAtNextUpkeepUnlessPays.class)) {
            List<PoisonAtNextUpkeepUnlessPays> pendingPoison = gameData.drainDelayedActions(
                    PoisonAtNextUpkeepUnlessPays.class, a -> a.playerId().equals(gameData.activePlayerId));
            for (PoisonAtNextUpkeepUnlessPays action : pendingPoison) {
                ForcedCostOrElseEffect payOrGetPoison = new ForcedCostOrElseEffect(
                        new PayManaCost(action.manaCost()),
                        new ArrayList<>(List.of(new GivePoisonCountersEffect(action.amount(), PoisonRecipient.CONTROLLER))),
                        true);
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        action.sourceCard(),
                        action.playerId(),
                        action.sourceCard().getName() + "'s delayed ability",
                        new ArrayList<>(List.of(payOrGetPoison))));

                gameLogService.append(gameData, GameLog.cardThen(action.sourceCard(),
                        "'s delayed ability triggers — " + gameData.playerIdToName.get(action.playerId())
                        + " gets a poison counter unless they pay " + action.manaCost() + "."));
                log.info("Game {} - {} delayed upkeep pay-or-poison trigger pushed for {}",
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
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
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
            for (ExileToOwnerGraveyardAtNextUpkeep action : pending) {
                gameData.exilePlayPermissions.remove(action.cardId());
                gameData.exilePlayCostModifiers.remove(action.cardId());
                var exiled = gameData.findExiledCard(action.cardId());
                if (exiled == null) {
                    // The card was played (or otherwise left exile) — nothing to clean up.
                    continue;
                }
                gameData.removeFromExile(action.cardId());
                graveyardService.addCardToGraveyard(gameData, action.ownerId(), exiled.card());
                String sourceName = action.sourceCard() != null ? action.sourceCard().getName() : "an effect";
                gameLogService.append(gameData, GameLog.text("The card exiled with " + sourceName + " is put into its owner's graveyard."));
                log.info("Game {} - unplayed card exiled with {} put into owner's graveyard",
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
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
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
                if (gameQueryService.findPermanentById(gameData, action.permanentId()) == null) {
                    continue;
                }
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
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
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        action.card(),
                        action.controllerId(),
                        action.card().getName() + "'s rebound ability",
                        new ArrayList<>(List.of(new ReboundCastFromExileEffect())));
                gameData.stack.add(entry);
                gameLogService.append(gameData, GameLog.cardThen(action.card(), "'s rebound ability triggers."));
                log.info("Game {} - {} rebound trigger pushed onto stack", gameData.id, action.card().getName());
            }
        }

        // Chancellor cycle: at the beginning of the first upkeep, check all players' hands
        // for cards with ON_OPENING_HAND_REVEAL effects (revealed from opening hand)
        if (gameData.turnNumber == 1) {
            handleOpeningHandTriggers(gameData);
        }

        UUID activePlayerId = gameData.activePlayerId;
        // Snapshot untapped lands the active player controls now (post-untap, pre-priority) — the
        // "number of untapped lands they controlled at the beginning of this turn" for Power Surge.
        // Locked here so tapping lands in response to the upkeep trigger cannot reduce the value.
        gameData.untappedLandsAtTurnStart.put(activePlayerId, countUntappedLands(gameData, activePlayerId));

        List<Permanent> battlefield = gameData.playerBattlefields.get(activePlayerId);
        if (battlefield == null) return;

        for (Permanent perm : battlefield) {
            int previousCopies = gameData.beginTriggeredAbilityCopies(1
                    + gameQueryService.countAdditionalTriggeredAbilityTriggers(
                    gameData, activePlayerId, perm));
            try {
            List<CardEffect> upkeepEffects = new ArrayList<>(perm.getCard().getEffects(EffectSlot.UPKEEP_TRIGGERED));
            upkeepEffects.addAll(perm.getTemporaryTriggeredEffects(EffectSlot.UPKEEP_TRIGGERED));
            upkeepEffects.addAll(perm.getPersistentTriggeredEffects(EffectSlot.UPKEEP_TRIGGERED));
            // Continuous grants (Breath of Dreams: green creatures have Cumulative upkeep {1})
            grantedUpkeepEffectSupport.appendGrantedUpkeepEffects(gameData, perm, upkeepEffects);
            if (upkeepEffects.isEmpty()) continue;

            // An intervening-if condition is checked at trigger time, so a failed check must not
            // put the ability on the stack or ask for any targets.
            upkeepEffects.removeIf(e -> e instanceof ConditionalEffect ce
                    && ce.interveningIf()
                    && !conditionEvaluationService.isMet(gameData, ce.condition(),
                            ConditionContext.forPermanent(perm, activePlayerId)));
            if (upkeepEffects.isEmpty()) continue;

            // "Choose one that hasn't been chosen —" (Demonic Pact): the mode is picked as the
            // ability goes on the stack, and only then does the chosen mode's own targeting run.
            CardEffect modal = upkeepEffects.stream()
                    .filter(ChooseModeNotYetChosenEffect.class::isInstance)
                    .findFirst().orElse(null);
            if (modal != null) {
                gameData.queueInteraction(new PermanentChoiceContext.UpkeepModalTrigger(
                        perm.getCard(), activePlayerId, (ChooseModeNotYetChosenEffect) modal, perm.getId()));
                upkeepEffects.remove(modal);
                if (upkeepEffects.isEmpty()) continue;
            }

            CardEffect targetedModal = upkeepEffects.stream()
                    .filter(ChooseOneEffect.class::isInstance)
                    .filter(effect -> ChooseOneEffect.class.cast(effect).options().stream()
                            .flatMap(option -> option.effects().stream())
                            .anyMatch(optionEffect -> optionEffect.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                                    || optionEffect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)))
                    .findFirst().orElse(null);
            if (targetedModal != null) {
                gameData.queueInteraction(new PermanentChoiceContext.UpkeepModalTrigger(
                        perm.getCard(), activePlayerId, ChooseOneEffect.class.cast(targetedModal), perm.getId()));
                upkeepEffects.remove(targetedModal);
                if (upkeepEffects.isEmpty()) continue;
            }

            // If any effect can target both a player and a permanent (i.e. "any target" —
            // creature/planeswalker/player, e.g. Form of the Dragon's "deals 5 damage to any target"),
            // route through the any-target pipeline so the controller may pick a permanent as well as a
            // player. Creature-only targeted upkeep effects (e.g. become-a-copy) keep their own pipelines.
            boolean hasAnyTarget = upkeepEffects.stream()
                    .anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.PLAYER) && e.targetSpec().admits(TargetPredicate.Kind.PERMANENT));
            if (hasAnyTarget) {
                gameData.queueInteraction(new PermanentChoiceContext.UpkeepAnyTargetTrigger(
                        perm.getCard(), activePlayerId, new ArrayList<>(upkeepEffects), perm.getId()));
                continue;
            }

            // If any effect targets a player (and is not also an any-target), queue only the
            // player-targeting abilities as one UpkeepPlayerTargetTrigger. Non-targeting upkeep
            // abilities on the same permanent (e.g. Cumulative upkeep alongside Corrosion's rust
            // trigger) stay separate triggered abilities and fall through to the per-effect loop.
            List<CardEffect> playerTargetEffects = upkeepEffects.stream()
                    .filter(e -> e.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                            && !e.targetSpec().admits(TargetPredicate.Kind.PERMANENT))
                    .toList();
            if (!playerTargetEffects.isEmpty()) {
                int maxPlayerTargets = playerTargetEffects.stream()
                        .mapToInt(e -> e.targetSpec().playerTargetCount())
                        .max().orElse(1);
                if (maxPlayerTargets >= 2) {
                    gameData.queueInteraction(new PermanentChoiceContext.UpkeepMultiPlayerTargetTrigger(
                            perm.getCard(), activePlayerId, new ArrayList<>(playerTargetEffects), perm.getId()));
                } else {
                    gameData.queueInteraction(new PermanentChoiceContext.UpkeepPlayerTargetTrigger(
                            perm.getCard(), activePlayerId, new ArrayList<>(playerTargetEffects), perm.getId()));
                }
                upkeepEffects.removeIf(e -> e.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                        && !e.targetSpec().admits(TargetPredicate.Kind.PERMANENT));
                if (upkeepEffects.isEmpty()) continue;
            }

            // Intervening-if on a targeted upkeep ability must be checked before its target is
            // chosen as the trigger is put on the stack.
            upkeepEffects.removeIf(e -> e instanceof ConditionalEffect conditional
                    && conditional.interveningIf()
                    && conditional.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)
                    && !conditionEvaluationService.isMet(gameData, conditional.condition(),
                            ConditionContext.forPermanent(perm, activePlayerId)));
            if (upkeepEffects.isEmpty()) continue;

            // Puca's Mischief: two interdependent nonland-permanent targets chosen at trigger time
            // (one you control, one an opponent controls with equal or lesser mana value). The
            // MayEffect wrapper is carried through so the "you may" is honoured at resolution.
            // Source-mode exchanges (Conjured Currency) declare a single ordinary permanent target
            // and are handled by the generic may/targeted-trigger paths below instead.
            boolean isExchangeControl = upkeepEffects.stream()
                    .map(e -> e instanceof MayEffect m ? m.wrapped() : e)
                    .anyMatch(e -> e instanceof ExchangeControlOfTargetPermanentsEffect x && !x.sourceIsFirstTarget());
            if (isExchangeControl) {
                gameData.queueInteraction(new PermanentChoiceContext.PucasMischiefOwnTarget(
                        perm.getCard(), activePlayerId, new ArrayList<>(upkeepEffects), perm.getId()));
                continue;
            }

            // "At the beginning of your upkeep, you may return target enchantment card from your
            // graveyard to the battlefield" (Starfield of Nyx): the graveyard target is chosen as
            // the trigger goes on the stack (CR 603.3d); with no legal target it is never put on
            // the stack at all (CR 603.3c). The "you may" is the target choice itself — the pick
            // is an up-to-one selection the controller can leave empty.
            List<CardEffect> graveyardTargetEffects = upkeepEffects.stream()
                    .filter(e -> e.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD))
                    .toList();
            if (!graveyardTargetEffects.isEmpty()) {
                gameData.queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                        perm.getCard(), activePlayerId, new ArrayList<>(graveyardTargetEffects)));
                gameLogService.append(gameData, GameLog.cardThen(perm.getCard(), "'s upkeep ability triggers."));
                log.info("Game {} - {} upkeep graveyard-target trigger queued", gameData.id, perm.getCard().getName());
                upkeepEffects.removeAll(graveyardTargetEffects);
                if (upkeepEffects.isEmpty()) continue;
            }

            for (CardEffect effect : upkeepEffects) {
                if (effect instanceof ConditionalEffect conditional
                        && conditional.interveningIf()
                        && conditional.condition() instanceof AnotherPermanentEnteredLastTurn
                        && !conditionEvaluationService.isMet(gameData, conditional.condition(),
                        ConditionContext.forPermanent(perm, activePlayerId))) {
                    continue;
                } else if (effect instanceof MayEffect may
                        && may.targetSpec().admits(TargetPredicate.Kind.PERMANENT)) {
                    // A targeted "you may" trigger still chooses its target as the trigger is put
                    // on the stack; only the may decision waits for resolution.
                    gameData.queueInteraction(new PermanentChoiceContext.UpkeepPermanentTargetTrigger(
                            perm.getCard(), activePlayerId, new ArrayList<>(List.of(effect)), perm.getId()));
                } else if (effect instanceof MayEffect may) {
                    gameData.queueMayAbility(perm.getCard(), activePlayerId, may, null, perm.getId());
                } else if (effect instanceof MayRevealSubtypeFromHandEffect mayReveal) {
                    List<Card> hand = gameData.playerHands.get(activePlayerId);
                    boolean hasSubtype = hand != null && hand.stream()
                            .anyMatch(c -> c.getSubtypes().contains(mayReveal.subtype()));
                    if (hasSubtype) {
                        MayEffect may = new MayEffect(mayReveal.thenEffect(), mayReveal.prompt());
                        gameData.queueMayAbility(perm.getCard(), activePlayerId, may, null, perm.getId());
                    }
                } else if (effect instanceof BecomeCopyOfTargetCreatureEffect) {
                    // Targeted upkeep trigger: target is chosen at trigger time (CR 603.3d).
                    // Collect valid creature targets excluding self ("another creature").
                    boolean hasValidTargets = false;
                    for (UUID pid : gameData.orderedPlayerIds) {
                        List<Permanent> bf = gameData.playerBattlefields.get(pid);
                        if (bf == null) continue;
                        for (Permanent p : bf) {
                            if (p.getId().equals(perm.getId())) continue;
                            if (gameQueryService.isCreature(gameData, p)) {
                                hasValidTargets = true;
                                break;
                            }
                        }
                        if (hasValidTargets) break;
                    }
                    if (hasValidTargets) {
                        gameData.queueInteraction(new PermanentChoiceContext.UpkeepCopyTriggerTarget(
                                perm.getCard(), activePlayerId, perm.getId(),
                                (BecomeCopyOfTargetCreatureEffect) effect));
                    }
                } else if (effect instanceof DestroyOneOfTargetsAtRandomEffect) {
                    // Targeted upkeep trigger: targets chosen at trigger time (CR 603.3d).
                    // The Efreet itself is a valid "nonland permanent you control" target,
                    // so this always triggers as long as it's on the battlefield.
                    gameData.queueInteraction(new PermanentChoiceContext.CapriciousEfreetOwnTarget(
                            perm.getCard(), activePlayerId, perm.getId()));
                } else if (effect instanceof ConditionalEffect conditional
                        && conditional.condition() instanceof NoOtherPermanent) {
                    // Intervening-if: only trigger if controller has no other matching permanents
                    boolean conditionMet = conditionEvaluationService.isMet(gameData, conditional.condition(),
                            ConditionContext.forPermanent(perm, activePlayerId));
                    if (conditionMet) {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                activePlayerId,
                                perm.getCard().getName() + "'s upkeep ability",
                                new ArrayList<>(List.of(effect)),
                                (UUID) null,
                                perm.getId()
                        ));

                        gameLogService.append(gameData, GameLog.cardThen(perm.getCard(), "'s upkeep ability triggers."));
                        log.info("Game {} - {} upkeep trigger pushed onto stack (intervening-if met: no other matching permanents)",
                                gameData.id, perm.getCard().getName());
                    }
                } else if (effect instanceof ConditionalEffect conditional
                        && conditional.condition() instanceof APlayerControlsMoreCreaturesThanEachOtherPlayer) {
                    if (conditionEvaluationService.isMet(gameData, conditional.condition(),
                            ConditionContext.forPermanent(perm, activePlayerId))) {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                activePlayerId,
                                perm.getCard().getName() + "'s upkeep ability",
                                new ArrayList<>(List.of(effect)),
                                (UUID) null,
                                perm.getId()
                        ));

                        gameLogService.append(gameData, GameLog.cardThen(perm.getCard(), "'s upkeep ability triggers."));
                        log.info("Game {} - {} upkeep trigger pushed onto stack (intervening-if met: unique player with most creatures)",
                                gameData.id, perm.getCard().getName());
                    }
                } else if (effect instanceof ConditionalEffect conditional
                        && conditional.condition() instanceof APlayerHasMoreCardsInHandThanEachOtherPlayer) {
                    if (conditionEvaluationService.isMet(gameData, conditional.condition(),
                            ConditionContext.forPermanent(perm, activePlayerId))) {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                activePlayerId,
                                perm.getCard().getName() + "'s upkeep ability",
                                new ArrayList<>(List.of(effect)),
                                (UUID) null,
                                perm.getId()
                        ));

                        gameLogService.append(gameData, GameLog.cardThen(perm.getCard(), "'s upkeep ability triggers."));
                        log.info("Game {} - {} upkeep trigger pushed onto stack (intervening-if met: unique player with most cards in hand)",
                                gameData.id, perm.getCard().getName());
                    }
                } else if (effect instanceof ConditionalEffect conditional
                        && conditional.condition() instanceof ControllerLifeAtMost lifeCheck) {
                    // Intervening-if: only trigger if controller's life total <= threshold
                    int lifeTotal = gameData.playerLifeTotals.getOrDefault(activePlayerId, 20);
                    if (conditionEvaluationService.isMet(gameData, lifeCheck,
                            ConditionContext.forPermanent(perm, activePlayerId))) {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                activePlayerId,
                                perm.getCard().getName() + "'s upkeep ability",
                                new ArrayList<>(List.of(effect)),
                                (UUID) null,
                                perm.getId()
                        ));

                        gameLogService.append(gameData, GameLog.cardThen(perm.getCard(), "'s upkeep ability triggers."));
                        log.info("Game {} - {} upkeep trigger pushed onto stack (intervening-if met: life {} <= {})",
                                gameData.id, perm.getCard().getName(), lifeTotal, lifeCheck.threshold());
                    }
                } else if (effect instanceof ConditionalEffect conditional
                        && conditional.interveningIf()
                        && conditional.condition() instanceof AllOf) {
                    if (conditionEvaluationService.isMet(gameData, conditional.condition(),
                            ConditionContext.forPermanent(perm, activePlayerId))) {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                activePlayerId,
                                perm.getCard().getName() + "'s upkeep ability",
                                new ArrayList<>(List.of(effect)),
                                (UUID) null,
                                perm.getId()
                        ));

                        gameLogService.append(gameData, GameLog.cardThen(perm.getCard(), "'s upkeep ability triggers."));
                        log.info("Game {} - {} upkeep trigger pushed onto stack (intervening-if met: composed condition)",
                                gameData.id, perm.getCard().getName());
                    }
                } else if (effect instanceof ConditionalEffect conditional
                        && conditional.condition() instanceof ControllerLifeAtLeast lifeCheck) {
                    if (conditionEvaluationService.isMet(gameData, lifeCheck,
                            ConditionContext.forPermanent(perm, activePlayerId))) {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                activePlayerId,
                                perm.getCard().getName() + "'s upkeep ability",
                                new ArrayList<>(List.of(effect)),
                                (UUID) null,
                                perm.getId()
                        ));

                        gameLogService.append(gameData, GameLog.cardThen(perm.getCard(), "'s upkeep ability triggers."));
                        log.info("Game {} - {} upkeep trigger pushed onto stack (intervening-if met: life >= {})",
                                gameData.id, perm.getCard().getName(), lifeCheck.threshold());
                    }
                } else if (effect instanceof ConditionalEffect conditional
                        && conditional.condition() instanceof EachPlayerLifeAtMost eachLifeCheck) {
                    // Intervening-if: only trigger if every player's life total <= threshold
                    // (Cryptolith Fragment — "if each player has 10 or less life")
                    if (conditionEvaluationService.isMet(gameData, eachLifeCheck,
                            ConditionContext.forPermanent(perm, activePlayerId))) {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                activePlayerId,
                                perm.getCard().getName() + "'s upkeep ability",
                                new ArrayList<>(List.of(effect)),
                                (UUID) null,
                                perm.getId()
                        ));

                        gameLogService.append(gameData, GameLog.cardThen(perm.getCard(), "'s upkeep ability triggers."));
                        log.info("Game {} - {} upkeep trigger pushed onto stack (intervening-if met: each player at or below {} life)",
                                gameData.id, perm.getCard().getName(), eachLifeCheck.threshold());
                    }
                } else if (effect instanceof ConditionalEffect conditional
                        && conditional.condition() instanceof ControlsPermanentCount countCheck) {
                    // Intervening-if: only trigger if controller has enough matching permanents
                    List<Permanent> controllerBf = gameData.playerBattlefields.get(activePlayerId);
                    long matchCount = controllerBf == null ? 0 : controllerBf.stream()
                            .filter(p -> predicateEvaluationService.matchesPermanentPredicate(gameData, p, countCheck.filter()))
                            .count();
                    if (conditionEvaluationService.isMet(gameData, countCheck,
                            ConditionContext.forPermanent(perm, activePlayerId))) {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                activePlayerId,
                                perm.getCard().getName() + "'s upkeep ability",
                                new ArrayList<>(List.of(effect)),
                                (UUID) null,
                                perm.getId()
                        ));

                        gameLogService.append(gameData, GameLog.cardThen(perm.getCard(), "'s upkeep ability triggers."));
                        log.info("Game {} - {} upkeep trigger pushed onto stack (intervening-if met: {} matching permanents >= {})",
                                gameData.id, perm.getCard().getName(), matchCount, countCheck.minCount());
                    }
                } else if (effect instanceof ConditionalEffect conditional
                        && conditional.condition() instanceof AnyPlayerControlsPermanentCount anyCountCheck) {
                    // Intervening-if: only trigger if enough matching permanents exist across all battlefields
                    if (conditionEvaluationService.isMet(gameData, anyCountCheck,
                            ConditionContext.forPermanent(perm, activePlayerId))) {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                activePlayerId,
                                perm.getCard().getName() + "'s upkeep ability",
                                new ArrayList<>(List.of(effect)),
                                (UUID) null,
                                perm.getId()
                        ));

                        gameLogService.append(gameData, GameLog.cardThen(perm.getCard(), "'s upkeep ability triggers."));
                        log.info("Game {} - {} upkeep trigger pushed onto stack (intervening-if met: {} or more matching permanents on the battlefield)",
                                gameData.id, perm.getCard().getName(), anyCountCheck.minCount());
                    }
                } else if (effect instanceof ConditionalEffect conditional
                        && conditional.condition() instanceof ControlsPermanentsWithDifferentNames namesCheck) {
                    // Intervening-if: only trigger if the controller has enough DIFFERENTLY NAMED
                    // matching permanents (Liliana's Contract — "four or more Demons with different
                    // names"). The wrapper is pushed intact so resolution re-checks it.
                    if (conditionEvaluationService.isMet(gameData, namesCheck,
                            ConditionContext.forPermanent(perm, activePlayerId))) {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                activePlayerId,
                                perm.getCard().getName() + "'s upkeep ability",
                                new ArrayList<>(List.of(effect)),
                                (UUID) null,
                                perm.getId()
                        ));

                        gameLogService.append(gameData, GameLog.cardThen(perm.getCard(), "'s upkeep ability triggers."));
                        log.info("Game {} - {} upkeep trigger pushed onto stack (intervening-if met: {} or more differently named matching permanents)",
                                gameData.id, perm.getCard().getName(), namesCheck.minCount());
                    }
                } else if (effect instanceof ConditionalEffect conditional
                        && conditional.condition() instanceof ControlsPermanentCountAtMost atMostCheck) {
                    // Intervening-if: only trigger if controller has few enough matching permanents
                    // (Sheltered Valley "three or fewer lands"; Kookus "don't control a Keeper of Kookus")
                    if (conditionEvaluationService.isMet(gameData, atMostCheck,
                            ConditionContext.forPermanent(perm, activePlayerId))) {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                activePlayerId,
                                perm.getCard().getName() + "'s upkeep ability",
                                new ArrayList<>(List.of(effect)),
                                (UUID) null,
                                perm.getId()
                        ));

                        gameLogService.append(gameData, GameLog.cardThen(perm.getCard(), "'s upkeep ability triggers."));
                        log.info("Game {} - {} upkeep trigger pushed onto stack (intervening-if met: {} or fewer matching permanents)",
                                gameData.id, perm.getCard().getName(), atMostCheck.maxCount());
                    }
                } else if (effect instanceof ConditionalEffect conditional
                        && conditional.condition() instanceof AnyPlayerControlsPermanentCountAtMost anyAtMostCheck) {
                    // Intervening-if: only trigger if at most N matching permanents exist across all
                    // battlefields (Spirit Mirror — "if there are no Reflection tokens on the battlefield")
                    if (conditionEvaluationService.isMet(gameData, anyAtMostCheck,
                            ConditionContext.forPermanent(perm, activePlayerId))) {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                activePlayerId,
                                perm.getCard().getName() + "'s upkeep ability",
                                new ArrayList<>(List.of(effect)),
                                (UUID) null,
                                perm.getId()
                        ));

                        gameLogService.append(gameData, GameLog.cardThen(perm.getCard(), "'s upkeep ability triggers."));
                        log.info("Game {} - {} upkeep trigger pushed onto stack (intervening-if met: {} or fewer matching permanents on the battlefield)",
                                gameData.id, perm.getCard().getName(), anyAtMostCheck.maxCount());
                    }
                } else if (effect instanceof ConditionalEffect conditional
                        && conditional.condition() instanceof CardsInHandAtLeast handCheck) {
                    // Intervening-if: only trigger if controller has enough cards in hand (Imaginary Pet)
                    if (conditionEvaluationService.isMet(gameData, handCheck,
                            ConditionContext.forPermanent(perm, activePlayerId))) {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                activePlayerId,
                                perm.getCard().getName() + "'s upkeep ability",
                                new ArrayList<>(List.of(effect)),
                                (UUID) null,
                                perm.getId()
                        ));

                        gameLogService.append(gameData, GameLog.cardThen(perm.getCard(), "'s upkeep ability triggers."));
                        log.info("Game {} - {} upkeep trigger pushed onto stack (intervening-if met: hand >= {})",
                                gameData.id, perm.getCard().getName(), handCheck.threshold());
                    }
                } else if (effect instanceof ConditionalEffect conditional
                        && conditional.condition() instanceof ActivePlayerHandEmpty handEmptyCheck) {
                    // Intervening-if: only trigger if the active player (controller, on their own
                    // upkeep) has no cards in hand (Hollowborn Barghest)
                    if (conditionEvaluationService.isMet(gameData, handEmptyCheck,
                            ConditionContext.forPermanent(perm, activePlayerId))) {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                activePlayerId,
                                perm.getCard().getName() + "'s upkeep ability",
                                new ArrayList<>(List.of(effect)),
                                (UUID) null,
                                perm.getId()
                        ));

                        gameLogService.append(gameData, GameLog.cardThen(perm.getCard(), "'s upkeep ability triggers."));
                        log.info("Game {} - {} upkeep trigger pushed onto stack (intervening-if met: active player has no cards in hand)",
                                gameData.id, perm.getCard().getName());
                    }
                } else if (effect instanceof ConditionalEffect conditional
                        && conditional.condition() instanceof CardsInLibraryAtLeast libraryCheck) {
                    // Intervening-if: only trigger if controller has enough cards in library (Battle of Wits)
                    if (conditionEvaluationService.isMet(gameData, libraryCheck,
                            ConditionContext.forPermanent(perm, activePlayerId))) {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                activePlayerId,
                                perm.getCard().getName() + "'s upkeep ability",
                                new ArrayList<>(List.of(effect)),
                                (UUID) null,
                                perm.getId()
                        ));

                        gameLogService.append(gameData, GameLog.cardThen(perm.getCard(), "'s upkeep ability triggers."));
                        log.info("Game {} - {} upkeep trigger pushed onto stack (intervening-if met: library >= {})",
                                gameData.id, perm.getCard().getName(), libraryCheck.threshold());
                    }
                } else if (effect instanceof ConditionalEffect conditional
                        && conditional.condition() instanceof SourceCounterThreshold counterCheck) {
                    // Intervening-if: only trigger if the source permanent has enough counters of the
                    // given type (Helix Pinnacle — 100+ tower counters)
                    if (conditionEvaluationService.isMet(gameData, counterCheck,
                            ConditionContext.forPermanent(perm, activePlayerId))) {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                activePlayerId,
                                perm.getCard().getName() + "'s upkeep ability",
                                new ArrayList<>(List.of(effect)),
                                (UUID) null,
                                perm.getId()
                        ));

                        gameLogService.append(gameData, GameLog.cardThen(perm.getCard(), "'s upkeep ability triggers."));
                        log.info("Game {} - {} upkeep trigger pushed onto stack (intervening-if met: {}+ {} counters)",
                                gameData.id, perm.getCard().getName(), counterCheck.threshold(), counterCheck.counterType());
                    }
                } else if (effect instanceof SurveilEffect) {
                    // Surveil is part of a compound triggered ability (e.g. "surveil 1, then if...").
                    // Group ALL upkeep effects into a single stack entry so they resolve sequentially.
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            perm.getCard(),
                            activePlayerId,
                            perm.getCard().getName() + "'s upkeep ability",
                            new ArrayList<>(upkeepEffects),
                            (UUID) null,
                            perm.getId()
                    ));

                    gameLogService.append(gameData, GameLog.cardThen(perm.getCard(), "'s upkeep ability triggers."));
                    log.info("Game {} - {} upkeep trigger pushed onto stack (surveil compound)",
                            gameData.id, perm.getCard().getName());
                    break; // All effects grouped into one entry
                } else if (effect instanceof ConditionalEffect conditional
                        && conditional.condition() instanceof GraveyardCardThreshold graveyardCheck) {
                    // Intervening-if: only trigger if the controller's graveyard holds enough
                    // matching cards (Mortal Combat — twenty creature cards)
                    if (conditionEvaluationService.isMet(gameData, graveyardCheck,
                            ConditionContext.forPermanent(perm, activePlayerId))) {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                activePlayerId,
                                perm.getCard().getName() + "'s upkeep ability",
                                new ArrayList<>(List.of(effect)),
                                (UUID) null,
                                perm.getId()
                        ));

                        gameLogService.append(gameData, GameLog.cardThen(perm.getCard(), "'s upkeep ability triggers."));
                        log.info("Game {} - {} upkeep trigger pushed onto stack (intervening-if met: {}+ matching cards in graveyard)",
                                gameData.id, perm.getCard().getName(), graveyardCheck.threshold());
                    }
                } else if (effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)) {
                    // Generic targeted-permanent upkeep trigger (e.g. Weed-Pruner Poplar's
                    // "target creature other than this creature gets -1/-1"). Target is chosen
                    // at trigger time (CR 603.3d) via a permanent choice.
                    gameData.queueInteraction(new PermanentChoiceContext.UpkeepPermanentTargetTrigger(
                            perm.getCard(), activePlayerId, new ArrayList<>(List.of(effect)), perm.getId()));
                } else {
                    StackEntry entry = new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            perm.getCard(),
                            activePlayerId,
                            perm.getCard().getName() + "'s upkeep ability",
                            new ArrayList<>(List.of(effect)),
                            (UUID) null,
                            perm.getId()
                    );
                    entry.setSourcePermanentSnapshot(new Permanent(perm));
                    if (perm.isAttached()) {
                        Permanent attached = gameQueryService.findPermanentById(gameData, perm.getAttachedTo());
                        if (attached != null) {
                            entry.setAttachedPermanentSnapshot(new Permanent(attached));
                        }
                    }
                    gameData.stack.add(entry);

                    gameLogService.append(gameData, GameLog.cardThen(perm.getCard(), "'s upkeep ability triggers."));
                    log.info("Game {} - {} upkeep trigger pushed onto stack", gameData.id, perm.getCard().getName());
                }
            }
            } finally {
                gameData.restoreTriggeredAbilityCopies(previousCopies);
            }
        }

        // Energy Flux: "All artifacts have 'At the beginning of your upkeep, sacrifice this artifact
        // unless you pay {N}.'" The grant is global (any controller's Energy Flux), and the granted
        // ability fires on each artifact's controller's own upkeep — so during the active player's
        // upkeep, push a pay-or-sacrifice trigger sourced at each artifact they control.
        handleGrantedArtifactSacrificeTriggers(gameData, activePlayerId, battlefield);

        List<Card> graveyard = gameData.playerGraveyards.get(activePlayerId);
        if (graveyard != null) {
            for (Card card : new ArrayList<>(graveyard)) {
                List<CardEffect> upkeepEffects = gameQueryService.getEffectiveGraveyardEffects(
                        gameData, card, EffectSlot.GRAVEYARD_UPKEEP_TRIGGERED);
                if (upkeepEffects == null || upkeepEffects.isEmpty()) continue;

                for (CardEffect effect : upkeepEffects) {
                    // Check the intervening-if gate at trigger time while keeping the wrapper on
                    // the stack so the same condition is checked again during resolution.
                    if (effect instanceof ConditionalEffect conditional && conditional.interveningIf()) {
                        if (!conditionEvaluationService.isMet(gameData, conditional.condition(),
                                ConditionContext.forCard(card, activePlayerId))) {
                            log.info("Game {} - {} graveyard upkeep ability skipped ({})",
                                    gameData.id, card.getName(), conditional.condition().conditionNotMetReason());
                            continue;
                        }
                    }

                    if (effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
                        gameData.queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                                card, activePlayerId, new ArrayList<>(List.of(effect))));
                        gameLogService.append(gameData,
                                GameLog.cardThen(card, "'s upkeep ability triggers."));
                        log.info("Game {} - {} graveyard upkeep trigger queued for graveyard target selection",
                                gameData.id, card.getName());
                    } else if (effect instanceof MayPayManaEffect mayPay) {
                        gameData.queueMayAbility(card, activePlayerId, mayPay, null);
                    } else if (effect instanceof MayEffect may) {
                        gameData.queueMayAbility(card, activePlayerId, may);
                    } else {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                card,
                                activePlayerId,
                                card.getName() + "'s upkeep ability",
                                new ArrayList<>(List.of(effect))
                        ));

                        gameLogService.append(gameData, GameLog.cardThen(card, "'s upkeep ability triggers."));
                        log.info("Game {} - {} graveyard upkeep trigger pushed onto stack", gameData.id, card.getName());
                    }
                }
            }
        }

        // Check all battlefields for EACH_UPKEEP_TRIGGERED effects
        gameData.forEachPermanent((playerId, perm) -> {
            List<CardEffect> eachUpkeepEffects = new ArrayList<>(
                    perm.getCard().getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED));
            eachUpkeepEffects.addAll(perm.getTemporaryTriggeredEffects(EffectSlot.EACH_UPKEEP_TRIGGERED));
            eachUpkeepEffects.addAll(perm.getPersistentTriggeredEffects(EffectSlot.EACH_UPKEEP_TRIGGERED));
            if (eachUpkeepEffects == null || eachUpkeepEffects.isEmpty()) return;

            for (CardEffect effect : eachUpkeepEffects) {
                if (effect instanceof UpkeepPlayerDependentEffect playerDependent
                        && !playerDependent.triggersFor(activePlayerId)) {
                    continue;
                }
                if (effect instanceof ConditionalEffect conditional
                        && conditional.interveningIf()
                        && !conditionEvaluationService.isMet(gameData, conditional.condition(),
                        ConditionContext.forPermanent(perm, playerId))) {
                    continue;
                }

                if (effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                        && !effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)) {
                    gameData.queueInteraction(new PermanentChoiceContext.UpkeepPlayerTargetTrigger(
                            perm.getCard(), playerId, new ArrayList<>(List.of(effect)), perm.getId(), null,
                            activePlayerId));
                } else if (effect instanceof MayEffect may) {
                    if (may.choicePlayer() == MayChoicePlayer.ACTIVE_PLAYER) {
                        gameData.queueMayAbility(
                                perm.getCard(), playerId, may, null, perm.getId(), activePlayerId,
                                new Permanent(perm));
                    } else {
                        gameData.queueMayAbility(perm.getCard(), playerId, may, null, perm.getId());
                    }
                } else if (effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)) {
                    TargetFilter targetFilter = perm.getCard().getTargetFilter();
                    UUID choosingPlayerId = effect.targetChosenByActivePlayer()
                            || targetFilter != null && targetFilter.activePlayerChoosesTarget()
                            ? activePlayerId : null;
                    gameData.queueInteraction(new PermanentChoiceContext.UpkeepPermanentTargetTrigger(
                            perm.getCard(), playerId, new ArrayList<>(List.of(effect)), perm.getId(), null,
                            choosingPlayerId));
                } else {
                    StackEntry entry = new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            perm.getCard(),
                            playerId,
                            perm.getCard().getName() + "'s upkeep ability",
                            new ArrayList<>(List.of(effect)),
                            activePlayerId,
                            perm.getId()
                    );
                    entry.setSourcePermanentSnapshot(new Permanent(perm));
                    gameData.stack.add(entry);
                }

                gameLogService.append(gameData, GameLog.cardThen(perm.getCard(), "'s upkeep ability triggers."));
                log.info("Game {} - {} each-upkeep trigger pushed onto stack", gameData.id, perm.getCard().getName());
            }
        });

        // Check all battlefields for OPPONENT_UPKEEP_TRIGGERED effects (only opponents of the active player)
        gameData.forEachBattlefield((playerId, playerBattlefield) -> {
            if (playerId.equals(activePlayerId)) return; // Skip the active player's own permanents

            for (Permanent perm : playerBattlefield) {
                List<CardEffect> opponentUpkeepEffects = perm.getCard().getEffects(EffectSlot.OPPONENT_UPKEEP_TRIGGERED);
                if (opponentUpkeepEffects == null || opponentUpkeepEffects.isEmpty()) continue;

                for (CardEffect effect : opponentUpkeepEffects) {
                    // Intervening-if: check condition at trigger time
                    if (effect instanceof DealDamageIfFewCardsInHandEffect fewCardsEffect) {
                        List<Card> hand = gameData.playerHands.get(activePlayerId);
                        int handSize = hand != null ? hand.size() : 0;
                        if (handSize > fewCardsEffect.maxCards()) {
                            continue; // Condition not met, don't trigger
                        }
                    }
                    if (effect instanceof ConditionalEffect conditional
                            && conditional.interveningIf()
                            && conditional.targetSpec() != TargetSpec.NONE
                            && !conditionEvaluationService.isMet(gameData, conditional.condition(),
                            ConditionContext.forPermanent(perm, playerId))) {
                        continue;
                    }
                    // Intervening-if on the active opponent's hand size — Hollowborn Barghest's
                    // "if that player has no cards in hand" and Misers' Cage's "if that player has
                    // five or more cards in hand"
                    if (effect instanceof ConditionalEffect conditional
                            && (conditional.condition() instanceof ActivePlayerHandEmpty
                                    || conditional.condition() instanceof ActivePlayerHandAtLeast
                                    || conditional.condition() instanceof ActivePlayerHandAtMost)
                            && !conditionEvaluationService.isMet(gameData, conditional.condition(),
                                    ConditionContext.forPermanent(perm, playerId))) {
                        continue;
                    }

                    if (effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)) {
                        TargetFilter targetFilter = perm.getCard().getTargetFilter();
                        UUID choosingPlayerId = targetFilter != null && targetFilter.activePlayerChoosesTarget()
                                ? activePlayerId : null;
                        gameData.queueInteraction(new PermanentChoiceContext.UpkeepPermanentTargetTrigger(
                                perm.getCard(), playerId, new ArrayList<>(List.of(effect)), perm.getId(), null,
                                choosingPlayerId));
                    } else {
                        StackEntry entry = new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                playerId,
                                perm.getCard().getName() + "'s upkeep ability",
                                new ArrayList<>(List.of(effect)),
                                activePlayerId,
                                perm.getId()
                        );
                        entry.setSourcePermanentSnapshot(new Permanent(perm));
                        gameData.stack.add(entry);
                    }

                    gameLogService.append(gameData, GameLog.cardThen(perm.getCard(), "'s upkeep ability triggers."));
                    log.info("Game {} - {} opponent-upkeep trigger pushed onto stack", gameData.id, perm.getCard().getName());
                }
            }
        });

        // Check all battlefields for auras with ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED effects
        // These fire during the enchanted permanent's controller's upkeep (e.g. Numbing Dose)
        gameData.forEachPermanent((auraOwnerId, perm) -> {
            List<CardEffect> enchantedControllerUpkeepEffects = perm.getCard().getEffects(EffectSlot.ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED);
            if (enchantedControllerUpkeepEffects == null || enchantedControllerUpkeepEffects.isEmpty()) return;
            if (!perm.isAttached()) return;

            UUID enchantedPermanentControllerId = gameQueryService.findPermanentController(gameData, perm.getAttachedTo());
            if (enchantedPermanentControllerId == null) return;
            if (!enchantedPermanentControllerId.equals(activePlayerId)) return;
            Permanent enchantedPermanent = gameQueryService.findPermanentById(gameData, perm.getAttachedTo());
            int enchantedPermanentPowerAtTrigger = enchantedPermanent == null
                    ? 0 : gameQueryService.getEffectivePower(gameData, enchantedPermanent);

            for (CardEffect effect : enchantedControllerUpkeepEffects) {
                if (effect instanceof ForcedCostOrElseEffect forcedCost
                        && (forcedCost.elseEffects().stream().anyMatch(
                                TargetPlayerGainsControlOfEnchantedPermanentEffect.class::isInstance)
                        || forcedCost.paidEffects().stream().anyMatch(
                                TargetPlayerGainsControlOfEnchantedPermanentEffect.class::isInstance))) {
                    gameData.queueInteraction(new PermanentChoiceContext.UpkeepPlayerTargetTrigger(
                            perm.getCard(), enchantedPermanentControllerId, new ArrayList<>(List.of(effect)),
                            perm.getId(), new PlayerPredicateTargetFilter(
                                    new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                                    "Target must be an opponent"), enchantedPermanentControllerId));
                    gameLogService.append(gameData, GameLog.cardThen(perm.getCard(), "'s upkeep ability triggers."));
                    log.info("Game {} - {} enchanted-permanent-controller upkeep trigger awaiting opponent target",
                            gameData.id, perm.getCard().getName());
                    continue;
                }

                // Bake the enchanted permanent's controller into effects that need it
                CardEffect effectForStack = effect;
                if (effect instanceof EnchantedCreatureControllerLosesLifeEffect e) {
                    effectForStack = new EnchantedCreatureControllerLosesLifeEffect(e.amount(), enchantedPermanentControllerId);
                }
                // DealDamageToPlayersEffect(ENCHANTED_PERMANENT_CONTROLLER) reads that player from the
                // stack entry's targetId (set below); the sacrifice/life-loss effects find their host
                // via the source aura, so baking the controller as targetId is safe for them.

                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        auraOwnerId,
                        perm.getCard().getName() + "'s upkeep ability",
                        new ArrayList<>(List.of(effectForStack)),
                        enchantedPermanentControllerId,
                        perm.getId()
                );
                entry.setActivePlayerId(activePlayerId);
                entry.setTriggeringPermanentPowerAtTrigger(enchantedPermanentPowerAtTrigger);
                entry.setSourcePermanentSnapshot(new Permanent(perm));
                gameData.stack.add(entry);

                gameLogService.append(gameData, GameLog.cardThen(perm.getCard(), "'s upkeep ability triggers."));
                log.info("Game {} - {} enchanted-permanent-controller upkeep trigger pushed onto stack", gameData.id, perm.getCard().getName());
            }
        });

        // Check all battlefields for curses with ENCHANTED_PLAYER_UPKEEP_TRIGGERED effects
        // These fire during the enchanted player's upkeep (e.g. Curse of Oblivion, Curse of the Bloody Tome)
        gameData.forEachPermanent((auraOwnerId, perm) -> {
            List<CardEffect> enchantedPlayerUpkeepEffects = perm.getCard().getEffects(EffectSlot.ENCHANTED_PLAYER_UPKEEP_TRIGGERED);
            if (enchantedPlayerUpkeepEffects == null || enchantedPlayerUpkeepEffects.isEmpty()) return;
            if (!perm.isAttached()) return;

            // For curses, attachedTo is the enchanted player's UUID
            UUID enchantedPlayerId = perm.getAttachedTo();
            if (!enchantedPlayerId.equals(activePlayerId)) return;

            for (CardEffect effect : enchantedPlayerUpkeepEffects) {
                // Bake the enchanted player ID into effects that need it
                CardEffect effectForStack = effect;
                if (effect instanceof ExileGraveyardCardsEffect e && e.scope() == GraveyardExileScope.OWN) {
                    effectForStack = new ExileGraveyardCardsEffect(e.count(), GraveyardExileScope.OWN, null, enchantedPlayerId);
                } else if (effect instanceof EachTargetPlayerDrawsCardsEqualToAttachedCountEffect e) {
                    effectForStack = new EachTargetPlayerDrawsCardsEqualToAttachedCountEffect(e.filter(), enchantedPlayerId);
                }
                // DealDamageToPlayersEffect(ENCHANTED_PLAYER) reads the enchanted player from the
                // stack entry's targetId (set below), so no per-effect baking is needed here.

                if (effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                        && !effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)) {
                    gameData.queueInteraction(new PermanentChoiceContext.UpkeepPlayerTargetTrigger(
                            perm.getCard(), auraOwnerId, new ArrayList<>(List.of(effectForStack)), perm.getId(),
                            null, auraOwnerId, true, enchantedPlayerId));
                    gameLogService.append(gameData, GameLog.cardThen(perm.getCard(), "'s upkeep ability triggers."));
                    log.info("Game {} - {} enchanted-player upkeep trigger awaiting any-number player targets",
                            gameData.id, perm.getCard().getName());
                    continue;
                }

                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        auraOwnerId,
                        perm.getCard().getName() + "'s upkeep ability",
                        new ArrayList<>(List.of(effectForStack)),
                        enchantedPlayerId,
                        perm.getId()
                ));

                gameLogService.append(gameData, GameLog.cardThen(perm.getCard(), "'s upkeep ability triggers."));
                log.info("Game {} - {} enchanted-player upkeep trigger pushed onto stack", gameData.id, perm.getCard().getName());
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
                    if (eggCounters != null && eggCounters > 0) {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                card,
                                activePlayerId,
                                card.getName() + "'s egg counter ability",
                                new ArrayList<>(List.of(new RemoveEggCounterFromExileAndReturnEffect(card.getId())))
                        ));

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
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            card,
                            activePlayerId,
                            card.getName() + "'s refine counter ability",
                            new ArrayList<>(List.of(new RemoveRefineCounterFromExiledCardEffect(card.getId())))
                    ));
                    gameLogService.append(gameData,
                            GameLog.cardThen(card, "'s refine counter ability triggers."));
                }
            }
        }

        // Suspend: "At the beginning of your upkeep, remove a time counter from this card."
        if (!gameData.exiledCardTimeCounters.isEmpty()) {
            List<Card> exiledCards = gameData.getPlayerExiledCards(activePlayerId);
            for (Card card : new ArrayList<>(exiledCards)) {
                Integer timeCounters = gameData.exiledCardTimeCounters.get(card.getId());
                if (timeCounters != null && timeCounters > 0) {
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            card,
                            activePlayerId,
                            card.getName() + "'s suspend ability",
                            new ArrayList<>(List.of(new RemoveTimeCounterFromExiledCardEffect(card.getId())))
                    ));

                    gameLogService.append(gameData,
                            GameLog.cardThen(card, "'s suspend ability triggers."));
                    log.info("Game {} - {} suspend upkeep trigger pushed onto stack", gameData.id, card.getName());
                }
            }
        }

        // Exiled cards with scream counters trigger during their owners' upkeeps.
        for (var exiledEntry : new ArrayList<>(gameData.exiledCards)) {
            if (!activePlayerId.equals(exiledEntry.ownerId())) {
                continue;
            }
            Card card = exiledEntry.card();
            Integer screamCounters = gameData.exiledCardScreamCounters.get(card.getId());
            if (screamCounters == null || screamCounters <= 0) {
                continue;
            }

            for (CardEffect effect : card.getEffects(EffectSlot.EXILED_SCREAM_COUNTER_UPKEEP_TRIGGERED)) {
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        card,
                        exiledEntry.ownerId(),
                        card.getName() + "'s scream counter ability",
                        new ArrayList<>(List.of(effect))
                );
                gameData.stack.add(entry);

                gameLogService.append(gameData,
                        GameLog.cardThen(card, "'s scream counter upkeep ability triggers."));
                log.info("Game {} - {} scream counter upkeep trigger pushed onto stack", gameData.id, card.getName());
            }
        }

        // Suspended cards may also trigger during every player's upkeep.
        for (var exiledEntry : new ArrayList<>(gameData.exiledCards)) {
            Card card = exiledEntry.card();
            Integer timeCounters = gameData.exiledCardTimeCounters.get(card.getId());
            if (timeCounters == null || timeCounters <= 0) {
                continue;
            }

            for (CardEffect effect : card.getEffects(EffectSlot.SUSPENDED_EACH_UPKEEP_TRIGGERED)) {
                if (effect instanceof MayEffect may) {
                    gameData.queueMayAbility(card, exiledEntry.ownerId(), may, activePlayerId, null,
                            activePlayerId, null);
                } else {
                    StackEntry entry = new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            card,
                            exiledEntry.ownerId(),
                            card.getName() + "'s suspended ability",
                            new ArrayList<>(List.of(effect)),
                            activePlayerId,
                            (UUID) null
                    );
                    entry.setActivePlayerId(activePlayerId);
                    gameData.stack.add(entry);
                }

                gameLogService.append(gameData,
                        GameLog.cardThen(card, "'s suspended upkeep ability triggers."));
                log.info("Game {} - {} suspended upkeep trigger pushed onto stack", gameData.id, card.getName());
            }
        }

        // Ertai's Meddling: "At the beginning of each of that player's upkeeps, if that card is
        // exiled, remove a delay counter from it." The trigger belongs to the exiled spell's
        // controller, so it only fires on their own upkeeps.
        for (GameData.DelayedSpellExile pending : new ArrayList<>(gameData.delayedSpellExiles)) {
            if (!pending.controllerId().equals(activePlayerId)) {
                continue;
            }
            Card delayedCard = pending.originalEntry().getCard();
            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    delayedCard,
                    activePlayerId,
                    delayedCard.getName() + "'s delay counter ability",
                    new ArrayList<>(List.of(new RemoveDelayCounterFromExiledSpellEffect(pending.cardId())))
            ));

            gameLogService.append(gameData,
                    GameLog.cardThen(delayedCard, "'s delay counter ability triggers."));
            log.info("Game {} - {} delay counter upkeep trigger pushed onto stack", gameData.id, delayedCard.getName());
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
            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    suspendedCard,
                    activePlayerId,
                    suspendedCard.getName() + "'s suspend ability",
                    new ArrayList<>(List.of(new RemoveSuspendCounterFromExiledSpellEffect(pending.cardId())))
            ));

            gameLogService.append(gameData, GameLog.cardThen(suspendedCard, "'s suspend ability triggers."));
            log.info("Game {} - {} suspend upkeep trigger pushed onto stack", gameData.id, suspendedCard.getName());
        }

        // Phase-in targeted triggers were queued during the untap-step phasing action; choose targets
        // now as they go on the stack (before upkeep's own targeted triggers).
        if (gameData.hasPendingInteraction(PermanentChoiceContext.PhasesInTriggerTarget.class)) {
            processNextPhasesInTriggerTarget(gameData);
            return;
        }

        // Modal upkeep triggers pick their mode before any targeting runs (Demonic Pact) — the mode
        // decides which targets, if any, the ability needs.
        if (gameData.hasPendingInteraction(PermanentChoiceContext.UpkeepModalTrigger.class)) {
            processNextUpkeepModalTrigger(gameData);
            return;
        }

        // Process upkeep any-target triggers first (e.g. Form of the Dragon, CR 603.3d)
        if (gameData.hasPendingInteraction(PermanentChoiceContext.UpkeepAnyTargetTrigger.class)) {
            processNextUpkeepAnyTargetTrigger(gameData);
            return;
        }

        // Process upkeep multi-player-targeted triggers first (e.g. Axis of Mortality, CR 603.3d)
        if (gameData.hasPendingInteraction(PermanentChoiceContext.UpkeepMultiPlayerTargetTrigger.class)) {
            processNextUpkeepMultiPlayerTarget(gameData);
            return;
        }

        // Process upkeep player-targeted triggers (mandatory targeting at trigger time, CR 603.3d)
        if (gameData.hasPendingInteraction(PermanentChoiceContext.UpkeepPlayerTargetTrigger.class)) {
            processNextUpkeepPlayerTarget(gameData);
            return;
        }

        // Process upkeep copy trigger target selection (mandatory targeting at trigger time)
        if (gameData.hasPendingInteraction(PermanentChoiceContext.UpkeepCopyTriggerTarget.class)) {
            processNextUpkeepCopyTarget(gameData);
            return;
        }

        if (gameData.hasPendingInteraction(PermanentChoiceContext.CapriciousEfreetOwnTarget.class)) {
            processNextCapriciousEfreetTarget(gameData);
            return;
        }

        if (gameData.hasPendingInteraction(PermanentChoiceContext.PucasMischiefOwnTarget.class)) {
            processNextPucasMischiefTarget(gameData);
            return;
        }

        if (gameData.hasPendingInteraction(PermanentChoiceContext.UpkeepPermanentTargetTrigger.class)) {
            processNextUpkeepPermanentTarget(gameData);
            return;
        }

        // Process upkeep graveyard-target triggers (e.g. Starfield of Nyx, CR 603.3d)
        if (gameData.hasPendingInteraction(PermanentChoiceContext.SpellGraveyardTargetTrigger.class)) {
            triggerCollectionService.processNextSpellGraveyardTargetTrigger(gameData);
            return;
        }

        playerInputService.processNextMayAbility(gameData);
    }

    /**
     * Ends every "until your next upkeep, target permanent can't phase out" restriction (Spatial
     * Binding) created by the active player. Phased-out permanents are scanned too so a restriction
     * never outlives its window on an object that is currently off the battlefield.
     */
    private void expireCantPhaseOut(GameData gameData) {
        gameData.forEachBattlefield((controllerId, permanents) -> permanents.forEach(permanent -> {
            if (gameData.activePlayerId.equals(permanent.getCantPhaseOutUntilUpkeepOf())) {
                permanent.setCantPhaseOutUntilUpkeepOf(null);
            }
        }));
        gameData.phasedOutPermanents.forEach((controllerId, permanents) -> permanents.forEach(permanent -> {
            if (gameData.activePlayerId.equals(permanent.getCantPhaseOutUntilUpkeepOf())) {
                permanent.setCantPhaseOutUntilUpkeepOf(null);
            }
        }));
    }

    /** Counts the untapped lands the given player currently controls (layer-aware land check). */
    private int countUntappedLands(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return 0;
        int count = 0;
        for (Permanent perm : battlefield) {
            if (!perm.isTapped()
                    && predicateEvaluationService.matchesPermanentPredicate(gameData, perm, LAND_PREDICATE)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Global upkeep grants give a class of permanents "At the beginning of your upkeep, sacrifice
     * or destroy this permanent unless you pay {N}." For every
     * {@link AllPermanentsUpkeepSacrificeUnlessPayEffect} on the battlefield (regardless of who
     * controls it), this pushes one {@link ForcedCostOrElseEffect} pay-or-sacrifice trigger per
     * matching permanent the active player controls, sourced at that permanent so the "pay {N}"
     * prompt and the penalty both act on the individual permanent.
     * Grants stack: a permanent matching two grants gets a trigger from each.
     *
     * @param gameData       the current game state to modify
     * @param activePlayerId the player whose upkeep is being processed
     * @param battlefield    the active player's battlefield
     */
    private void handleGrantedArtifactSacrificeTriggers(GameData gameData, UUID activePlayerId,
                                                        List<Permanent> battlefield) {
        List<GrantedUpkeepSacrifice> grants = new ArrayList<>();
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf == null) continue;
            for (Permanent perm : bf) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof AllPermanentsUpkeepSacrificeUnlessPayEffect grant) {
                        grants.add(new GrantedUpkeepSacrifice(grant, perm.getId()));
                    }
                }
            }
        }
        if (grants.isEmpty()) return;

        for (Permanent perm : new ArrayList<>(battlefield)) {
            for (GrantedUpkeepSacrifice granted : grants) {
                AllPermanentsUpkeepSacrificeUnlessPayEffect grant = granted.effect();
                if (grant.excludeSource() && granted.sourcePermanentId().equals(perm.getId())) {
                    continue;
                }
                if (!predicateEvaluationService.matchesPermanentPredicate(gameData, perm, grant.filter())) {
                    continue;
                }

                CardEffect unpaidPenalty = grant.sacrifice()
                        ? new SacrificeSelfEffect()
                        : new DestroyReferencedPermanentEffect(PermanentReference.SOURCE);
                ForcedCostOrElseEffect payOrSacrifice = new ForcedCostOrElseEffect(
                        new PayManaCost(grant.manaCost(), null, null, false, grant.lifeAmount(), false),
                        new ArrayList<>(List.of(unpaidPenalty)),
                        true);
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        activePlayerId,
                        perm.getCard().getName() + "'s upkeep ability",
                        new ArrayList<>(List.of(payOrSacrifice)),
                        (UUID) null,
                        perm.getId()));

                gameLogService.append(gameData, GameLog.cardThen(perm.getCard(), "'s upkeep ability triggers."));
                log.info("Game {} - granted upkeep payment trigger pushed for {}",
                        gameData.id, perm.getCard().getName());
            }
        }
    }

    /**
     * Processes the next pending upkeep player-targeted trigger (e.g. Bloodgift Demon).
     * Presents the controller with a player choice; when selected, the trigger is
     * pushed onto the stack with all its effects sharing the chosen target.
     *
     * @param gameData the current game state to modify
     */
    /**
     * Processes the next pending upkeep any-target trigger (e.g. Form of the Dragon's
     * "deals 5 damage to any target"). Presents the controller with a choice among all valid
     * players and permanents; when selected, the trigger is pushed onto the stack with the
     * chosen target. Mandatory targeting at trigger time (CR 603.3d).
     *
     * @param gameData the current game state to modify
     */
    public void processNextUpkeepModalTrigger(GameData gameData) {
        if (!gameData.hasPendingInteraction(PermanentChoiceContext.UpkeepModalTrigger.class)) {
            processNextUpkeepAnyTargetTrigger(gameData);
            return;
        }

        PermanentChoiceContext.UpkeepModalTrigger trigger =
                gameData.pollPendingInteraction(PermanentChoiceContext.UpkeepModalTrigger.class);

        Permanent source = gameQueryService.findPermanentById(gameData, trigger.sourcePermanentId());
        Set<String> alreadyChosen = source == null ? Set.of() : source.getChosenModeLabels();
        List<ChooseOneEffect.ChooseOneOption> remaining = trigger.consumeModes()
                ? trigger.effect().options().stream()
                        .filter(option -> !alreadyChosen.contains(option.label()))
                        .toList()
                : trigger.effect().options();

        if (remaining.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.cardThen(trigger.sourceCard(), "'s upkeep trigger has no modes left to choose."));
            processNextUpkeepModalTrigger(gameData);
            return;
        }

        gameLogService.append(gameData, GameLog.cardThen(trigger.sourceCard(), "'s upkeep ability triggers."));
        playerInputService.beginChooseModeChoice(gameData, trigger.controllerId(), trigger.sourceCard(),
                new ChooseOneEffect(remaining), true, trigger.sourcePermanentId(), trigger.consumeModes());
    }

    /**
     * Puts a modal upkeep trigger's chosen mode on the stack, routing it through the same targeting
     * pipelines as an ordinary upkeep trigger: an "any target" mode queues an any-target choice, a
     * player-targeting mode a player choice, and a non-targeting mode goes straight onto the stack.
     * The mode's own {@code targetFilter} (e.g. "target opponent") overrides the card's.
     *
     * @param gameData    the current game state to modify
     * @param sourceCard  the triggering permanent's card
     * @param controllerId the ability's controller, who chooses the targets
     * @param permanentId the triggering permanent
     * @param chosen      the chosen mode
     */
    public void queueChosenModeUpkeepTrigger(GameData gameData, Card sourceCard, UUID controllerId,
            UUID permanentId, ChooseOneEffect.ChooseOneOption chosen) {
        List<CardEffect> effects = new ArrayList<>(chosen.effects());
        boolean anyTarget = effects.stream().anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                && e.targetSpec().admits(TargetPredicate.Kind.PERMANENT));
        boolean playerTarget = effects.stream().anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                && !e.targetSpec().admits(TargetPredicate.Kind.PERMANENT));
        boolean permanentTarget = effects.stream().anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                && !e.targetSpec().admits(TargetPredicate.Kind.PLAYER));

        if (anyTarget) {
            gameData.queueInteraction(new PermanentChoiceContext.UpkeepAnyTargetTrigger(
                    sourceCard, controllerId, effects, permanentId, chosen.targetFilter()));
        } else if (playerTarget) {
            gameData.queueInteraction(new PermanentChoiceContext.UpkeepPlayerTargetTrigger(
                    sourceCard, controllerId, effects, permanentId, chosen.targetFilter()));
        } else if (permanentTarget) {
            gameData.queueInteraction(new PermanentChoiceContext.UpkeepPermanentTargetTrigger(
                    sourceCard, controllerId, effects, permanentId, chosen.targetFilter()));
        } else {
            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sourceCard,
                    controllerId,
                    sourceCard.getName() + "'s upkeep ability",
                    effects,
                    null,
                    permanentId));
        }
    }

    public void processNextUpkeepAnyTargetTrigger(GameData gameData) {
        if (!gameData.hasPendingInteraction(PermanentChoiceContext.UpkeepAnyTargetTrigger.class)) {
            processNextUpkeepMultiPlayerTarget(gameData);
            return;
        }

        PermanentChoiceContext.UpkeepAnyTargetTrigger trigger =
                gameData.pollPendingInteraction(PermanentChoiceContext.UpkeepAnyTargetTrigger.class);

        TargetFilter targetFilter = trigger.targetFilter() != null
                ? trigger.targetFilter() : trigger.sourceCard().getTargetFilter();
        TriggerTargetCollector.Result result = triggerTargetCollector.collect(
                gameData,
                trigger.effects(),
                targetFilter,
                trigger.controllerId(),
                trigger.sourceCard(),
                TriggerTargetCollector.Options.UPKEEP);
        List<UUID> validTargets = result.validTargets();

        if (validTargets.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.cardThen(trigger.sourceCard(), "'s upkeep trigger has no valid targets."));
            log.info("Game {} - {} upkeep any-target trigger skipped (no valid targets)",
                    gameData.id, trigger.sourceCard().getName());
            processNextUpkeepAnyTargetTrigger(gameData);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(trigger);

        String targetDescription;
        if (targetFilter instanceof PermanentPredicateTargetFilter ppf) {
            targetDescription = ppf.errorMessage().replace("Target must be ", "").replace("an ", "").replace("a ", "");
        } else if (result.canTargetPlayers() && result.canTargetPermanents()) {
            targetDescription = "any target";
        } else if (result.canTargetPlayers()) {
            targetDescription = "target player";
        } else {
            targetDescription = "target permanent";
        }

        playerInputService.beginPermanentChoice(gameData, trigger.controllerId(), validTargets,
                trigger.sourceCard().getName() + "'s ability — Choose " + targetDescription + ".");

        gameLogService.append(gameData,
                GameLog.cardThen(trigger.sourceCard(), "'s upkeep trigger — choose " + targetDescription + "."));
        log.info("Game {} - {} upkeep any-target trigger awaiting target selection",
                gameData.id, trigger.sourceCard().getName());
    }

    /**
     * Processes the next pending {@code ON_SELF_PHASES_IN} permanent-target trigger (e.g. Shimmering
     * Efreet's "target creature phases out"). Queued during the untap-step phasing action; drained
     * here at upkeep start so the target is chosen as the ability is put on the stack.
     *
     * @param gameData the current game state to modify
     */
    public void processNextPhasesInTriggerTarget(GameData gameData) {
        if (!gameData.hasPendingInteraction(PermanentChoiceContext.PhasesInTriggerTarget.class)) {
            if (gameData.hasPendingInteraction(PermanentChoiceContext.UpkeepAnyTargetTrigger.class)) {
                processNextUpkeepAnyTargetTrigger(gameData);
            } else if (gameData.hasPendingInteraction(PermanentChoiceContext.UpkeepMultiPlayerTargetTrigger.class)) {
                processNextUpkeepMultiPlayerTarget(gameData);
            } else if (gameData.hasPendingInteraction(PermanentChoiceContext.UpkeepPlayerTargetTrigger.class)) {
                processNextUpkeepPlayerTarget(gameData);
            } else if (gameData.hasPendingInteraction(PermanentChoiceContext.UpkeepCopyTriggerTarget.class)) {
                processNextUpkeepCopyTarget(gameData);
            } else if (gameData.hasPendingInteraction(PermanentChoiceContext.CapriciousEfreetOwnTarget.class)) {
                processNextCapriciousEfreetTarget(gameData);
            } else if (gameData.hasPendingInteraction(PermanentChoiceContext.PucasMischiefOwnTarget.class)) {
                processNextPucasMischiefTarget(gameData);
            } else if (gameData.hasPendingInteraction(PermanentChoiceContext.UpkeepPermanentTargetTrigger.class)) {
                processNextUpkeepPermanentTarget(gameData);
            } else {
                playerInputService.processNextMayAbility(gameData);
            }
            return;
        }

        PermanentChoiceContext.PhasesInTriggerTarget trigger =
                gameData.pollPendingInteraction(PermanentChoiceContext.PhasesInTriggerTarget.class);

        TargetFilter targetFilter = trigger.sourceCard().getTargetFilter();
        TriggerTargetCollector.Result result = triggerTargetCollector.collect(
                gameData,
                trigger.effects(),
                targetFilter,
                trigger.controllerId(),
                trigger.sourceCard(),
                TriggerTargetCollector.Options.END_STEP);
        List<UUID> validTargets = result.validTargets();

        if (validTargets.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.cardThen(trigger.sourceCard(), "'s phase-in trigger has no valid targets."));
            log.info("Game {} - {} phase-in trigger skipped (no valid targets)",
                    gameData.id, trigger.sourceCard().getName());
            processNextPhasesInTriggerTarget(gameData);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(trigger);

        String targetDescription;
        if (targetFilter instanceof PermanentPredicateTargetFilter ppf) {
            targetDescription = ppf.errorMessage().replace("Target must be ", "").replace("an ", "").replace("a ", "");
        } else {
            targetDescription = "target permanent";
        }

        playerInputService.beginPermanentChoice(gameData, trigger.controllerId(), validTargets,
                trigger.sourceCard().getName() + "'s ability — Choose " + targetDescription + ".");

        gameLogService.append(gameData,
                GameLog.cardThen(trigger.sourceCard(), "'s phase-in trigger — choose " + targetDescription + "."));
        log.info("Game {} - {} phase-in trigger awaiting target selection",
                gameData.id, trigger.sourceCard().getName());
    }

    /**
     * Processes the next pending upkeep permanent-target trigger (e.g. Weed-Pruner Poplar's
     * "target creature other than this creature gets -1/-1"). Presents the controller with a
     * permanent choice honouring the source card's target filter; when selected, the trigger is
     * pushed onto the stack with the chosen target. Mandatory targeting at trigger time (CR 603.3d).
     *
     * @param gameData the current game state to modify
     */
    public void processNextUpkeepPermanentTarget(GameData gameData) {
        if (!gameData.hasPendingInteraction(PermanentChoiceContext.UpkeepPermanentTargetTrigger.class)) {
            playerInputService.processNextMayAbility(gameData);
            return;
        }

        PermanentChoiceContext.UpkeepPermanentTargetTrigger trigger =
                gameData.pollPendingInteraction(PermanentChoiceContext.UpkeepPermanentTargetTrigger.class);

        TargetFilter targetFilter = trigger.targetFilter() != null
                ? trigger.targetFilter() : trigger.sourceCard().getTargetFilter();
        TriggerTargetCollector.Result result = triggerTargetCollector.collect(
                gameData,
                trigger.effects(),
                targetFilter,
                trigger.controllerId(),
                trigger.sourceCard(),
                TriggerTargetCollector.Options.UPKEEP);
        List<UUID> validTargets = result.validTargets();
        boolean optionalTarget = trigger.sourceCard().getMinTargets() == 0
                && trigger.sourceCard().getMaxTargets() == 1;

        if (validTargets.isEmpty()) {
            if (optionalTarget) {
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        trigger.sourceCard(),
                        trigger.controllerId(),
                        trigger.sourceCard().getName() + "'s upkeep ability",
                        new ArrayList<>(trigger.effects()),
                        (UUID) null,
                        trigger.sourcePermanentId()));
                gameLogService.append(gameData,
                        GameLog.cardThen(trigger.sourceCard(), "'s upkeep ability triggers with no target."));
                processNextUpkeepPermanentTarget(gameData);
                return;
            }
            gameLogService.append(gameData,
                    GameLog.cardThen(trigger.sourceCard(), "'s upkeep trigger has no valid targets."));
            log.info("Game {} - {} upkeep permanent-target trigger skipped (no valid targets)",
                    gameData.id, trigger.sourceCard().getName());
            processNextUpkeepPermanentTarget(gameData);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(trigger);

        String targetDescription;
        if (targetFilter instanceof PermanentPredicateTargetFilter ppf) {
            targetDescription = ppf.errorMessage().replace("Target must be ", "").replace("an ", "").replace("a ", "");
        } else {
            targetDescription = "target permanent";
        }

        UUID choosingPlayerId = trigger.choosingPlayerId() != null
                ? trigger.choosingPlayerId() : trigger.controllerId();
        if (optionalTarget) {
            playerInputService.beginMultiPermanentChoice(
                    gameData,
                    choosingPlayerId,
                    validTargets,
                    1,
                    new MultiPermanentChoiceContext.UpkeepOptionalPermanentTarget(
                            trigger.sourceCard(), trigger.controllerId(), trigger.effects(), trigger.sourcePermanentId()),
                    trigger.sourceCard().getName() + "'s ability — Choose up to one " + targetDescription + ".");
        } else {
            playerInputService.beginPermanentChoice(gameData, choosingPlayerId, validTargets,
                    trigger.sourceCard().getName() + "'s ability — Choose " + targetDescription + ".");
        }

        gameLogService.append(gameData,
                GameLog.cardThen(trigger.sourceCard(), "'s upkeep trigger — choose " + targetDescription + "."));
        log.info("Game {} - {} upkeep permanent-target trigger awaiting target selection",
                gameData.id, trigger.sourceCard().getName());
    }

    public void processNextUpkeepPlayerTarget(GameData gameData) {
        if (!gameData.hasPendingInteraction(PermanentChoiceContext.UpkeepPlayerTargetTrigger.class)) {
            processNextUpkeepCopyTarget(gameData);
            return;
        }

        PermanentChoiceContext.UpkeepPlayerTargetTrigger trigger = gameData.pollPendingInteraction(PermanentChoiceContext.UpkeepPlayerTargetTrigger.class);

        // Honour the trigger's target filter (e.g. "target opponent") so the choosing player is not
        // offered as a valid target. A null filter (e.g. "target player") leaves all players eligible.
        TargetFilter playerTargetFilter = trigger.targetFilter() != null
                ? trigger.targetFilter() : trigger.sourceCard().getTargetFilter();
        List<UUID> validPlayerTargets = validTargetService.filterValidPlayerTargets(
                gameData, playerTargetFilter,
                new ArrayList<>(gameData.orderedPlayerIds), trigger.choosingPlayerId(),
                trigger.sourcePermanentId());

        if (trigger.anyNumberTargets()) {
            if (trigger.excludedPlayerId() != null) {
                validPlayerTargets.remove(trigger.excludedPlayerId());
            }

            if (validPlayerTargets.isEmpty()) {
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        trigger.sourceCard(),
                        trigger.controllerId(),
                        trigger.sourceCard().getName() + "'s upkeep ability",
                        new ArrayList<>(trigger.effects()),
                        trigger.sourcePermanentId(),
                        List.of()));
                processNextUpkeepPlayerTarget(gameData);
                return;
            }

            gameData.interaction.setPermanentChoiceContext(trigger);
            playerInputService.beginMultiPermanentOrPlayerChoice(
                    gameData,
                    trigger.choosingPlayerId(),
                    List.of(),
                    validPlayerTargets,
                    validPlayerTargets.size(),
                    new MultiPermanentChoiceContext.UpkeepAnyNumberPlayerTargets(
                            trigger.sourceCard(), trigger.controllerId(), trigger.effects(), trigger.sourcePermanentId()),
                    trigger.sourceCard().getName() + "'s ability — Choose any number of target players other than the enchanted player.");
            return;
        }

        if (validPlayerTargets.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.cardThen(trigger.sourceCard(), "'s upkeep trigger has no valid targets."));
            log.info("Game {} - {} upkeep player-target trigger skipped (no valid targets)",
                    gameData.id, trigger.sourceCard().getName());
            processNextUpkeepPlayerTarget(gameData);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(trigger);
        playerInputService.beginAnyTargetChoice(gameData, trigger.choosingPlayerId(),
                List.of(), validPlayerTargets,
                trigger.sourceCard().getName() + "'s ability — Choose target player.");

        gameLogService.append(gameData, GameLog.cardThen(trigger.sourceCard(), "'s upkeep ability triggers."));
        log.info("Game {} - {} upkeep trigger awaiting player target selection", gameData.id, trigger.sourceCard().getName());
    }

    /**
     * Processes the next pending upkeep multi-player-targeted trigger (e.g. Axis of Mortality).
     * Presents the controller with a player choice for the first target; when selected,
     * a second target selection is initiated via {@code UpkeepSecondPlayerTargetTrigger}.
     */
    public void processNextUpkeepMultiPlayerTarget(GameData gameData) {
        if (!gameData.hasPendingInteraction(PermanentChoiceContext.UpkeepMultiPlayerTargetTrigger.class)) {
            processNextUpkeepPlayerTarget(gameData);
            return;
        }

        PermanentChoiceContext.UpkeepMultiPlayerTargetTrigger trigger = gameData.pollPendingInteraction(PermanentChoiceContext.UpkeepMultiPlayerTargetTrigger.class);

        List<UUID> validPlayerTargets = new ArrayList<>(gameData.orderedPlayerIds);

        gameData.interaction.setPermanentChoiceContext(trigger);
        playerInputService.beginAnyTargetChoice(gameData, trigger.controllerId(),
                List.of(), validPlayerTargets,
                trigger.sourceCard().getName() + "'s ability — Choose first target player.");

        gameLogService.append(gameData, GameLog.cardThen(trigger.sourceCard(), "'s upkeep ability triggers."));
        log.info("Game {} - {} upkeep trigger awaiting first player target selection", gameData.id, trigger.sourceCard().getName());
    }

    /**
     * Processes the second player target for an upkeep multi-player-targeted trigger.
     * After the second target is selected, the trigger is pushed onto the stack with both targets.
     */
    public void processUpkeepSecondPlayerTarget(GameData gameData, PermanentChoiceContext.UpkeepSecondPlayerTargetTrigger trigger) {
        List<UUID> validPlayerTargets = new ArrayList<>(gameData.orderedPlayerIds);
        // Cannot target the same player twice
        validPlayerTargets.remove(trigger.firstTargetPlayerId());

        gameData.interaction.setPermanentChoiceContext(trigger);
        playerInputService.beginAnyTargetChoice(gameData, trigger.controllerId(),
                List.of(), validPlayerTargets,
                trigger.sourceCard().getName() + "'s ability — Choose second target player.");

        log.info("Game {} - {} upkeep trigger awaiting second player target selection", gameData.id, trigger.sourceCard().getName());
    }

    /**
     * Processes the next pending upkeep copy-trigger target selection
     * (e.g. Clone Shell).  If no targets remain, continues to may-ability
     * processing.
     *
     * @param gameData the current game state to modify
     */
    public void processNextUpkeepCopyTarget(GameData gameData) {
        if (!gameData.hasPendingInteraction(PermanentChoiceContext.UpkeepCopyTriggerTarget.class)) {
            // All copy triggers targeted, continue with Capricious Efreet targets then may abilities
            processNextCapriciousEfreetTarget(gameData);
            return;
        }

        PermanentChoiceContext.UpkeepCopyTriggerTarget trigger = gameData.peekPendingInteraction(PermanentChoiceContext.UpkeepCopyTriggerTarget.class);

        // Collect valid creature targets (excluding source permanent)
        List<UUID> validTargets = new ArrayList<>();
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf == null) continue;
            for (Permanent p : bf) {
                if (p.getId().equals(trigger.sourcePermanentId())) continue;
                if (gameQueryService.isCreature(gameData, p)) {
                    validTargets.add(p.getId());
                }
            }
        }

        if (validTargets.isEmpty()) {
            // No valid targets remaining — skip
            gameData.pollPendingInteraction(PermanentChoiceContext.UpkeepCopyTriggerTarget.class);
            processNextUpkeepCopyTarget(gameData);
            return;
        }

        gameData.pollPendingInteraction(PermanentChoiceContext.UpkeepCopyTriggerTarget.class);
        gameData.interaction.setPermanentChoiceContext(trigger);
        playerInputService.beginPermanentChoice(gameData, trigger.controllerId(), validTargets,
                trigger.sourceCard().getName() + " — Choose a creature to target.");

        gameLogService.append(gameData, GameLog.cardThen(trigger.sourceCard(), "'s upkeep ability triggers."));
        log.info("Game {} - {} upkeep copy trigger awaiting target selection", gameData.id, trigger.sourceCard().getName());
    }

    /**
     * Processes the next pending Capricious Efreet upkeep trigger target selection.
     * Step 1: controller chooses one nonland permanent they control.
     */
    public void processNextCapriciousEfreetTarget(GameData gameData) {
        if (!gameData.hasPendingInteraction(PermanentChoiceContext.CapriciousEfreetOwnTarget.class)) {
            processNextPucasMischiefTarget(gameData);
            return;
        }

        PermanentChoiceContext.CapriciousEfreetOwnTarget trigger = gameData.pollPendingInteraction(PermanentChoiceContext.CapriciousEfreetOwnTarget.class);

        // Collect valid own nonland permanents (Efreet itself is a valid target)
        List<UUID> validOwnTargets = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(trigger.controllerId());
        if (battlefield != null) {
            for (Permanent p : battlefield) {
                if (!p.getCard().hasType(CardType.LAND)) {
                    validOwnTargets.add(p.getId());
                }
            }
        }

        if (validOwnTargets.isEmpty()) {
            // No valid own targets — skip this trigger
            processNextCapriciousEfreetTarget(gameData);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(trigger);
        playerInputService.beginPermanentChoice(gameData, trigger.controllerId(), validOwnTargets,
                trigger.sourceCard().getName() + " — Choose a nonland permanent you control.");

        gameLogService.append(gameData, GameLog.cardThen(trigger.sourceCard(), "'s upkeep ability triggers."));
        log.info("Game {} - {} upkeep trigger awaiting own target selection", gameData.id, trigger.sourceCard().getName());
    }

    /**
     * Processes the next pending Puca's Mischief upkeep trigger target selection.
     * Step 1: controller chooses one nonland permanent they control that has at least one legal
     * opponent pairing (an opponent nonland permanent with equal or lesser mana value).
     */
    public void processNextPucasMischiefTarget(GameData gameData) {
        if (!gameData.hasPendingInteraction(PermanentChoiceContext.PucasMischiefOwnTarget.class)) {
            processNextUpkeepPermanentTarget(gameData);
            return;
        }

        PermanentChoiceContext.PucasMischiefOwnTarget trigger =
                gameData.pollPendingInteraction(PermanentChoiceContext.PucasMischiefOwnTarget.class);

        ExchangeControlOfTargetPermanentsEffect exchange = interdependentExchange(trigger.effects());
        if (exchange != null && exchange.requireOpponentPowerNotGreater()) {
            List<UUID> validOwnTargets = new ArrayList<>();
            List<Permanent> battlefield = gameData.playerBattlefields.get(trigger.controllerId());
            if (battlefield != null) {
                for (Permanent own : battlefield) {
                    if (!gameQueryService.isCreature(gameData, own)) continue;
                    int ownPower = gameQueryService.getEffectivePower(gameData, own);
                    if (hasOpponentCreatureAtMostPower(gameData, trigger.controllerId(), ownPower)) {
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
                    trigger.sourceCard().getName() + " — Choose a creature you control.");
            gameLogService.append(gameData, GameLog.cardThen(trigger.sourceCard(), "'s enter-the-battlefield ability triggers."));
            return;
        }

        // A nonland permanent you control is a legal first target only if some opponent nonland
        // permanent has mana value <= its own — i.e. own MV >= the smallest opponent MV.
        int minOpponentManaValue = Integer.MAX_VALUE;
        for (UUID pid : gameData.orderedPlayerIds) {
            if (pid.equals(trigger.controllerId())) continue;
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf == null) continue;
            for (Permanent p : bf) {
                if (!p.getCard().hasType(CardType.LAND)) {
                    minOpponentManaValue = Math.min(minOpponentManaValue, p.getCard().getManaValue());
                }
            }
        }

        List<UUID> validOwnTargets = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(trigger.controllerId());
        if (battlefield != null) {
            for (Permanent p : battlefield) {
                if (!p.getCard().hasType(CardType.LAND) && p.getCard().getManaValue() >= minOpponentManaValue) {
                    validOwnTargets.add(p.getId());
                }
            }
        }

        if (validOwnTargets.isEmpty()) {
            // No legal pair of targets — the trigger does nothing.
            log.info("Game {} - {} upkeep trigger skipped (no legal target pair)",
                    gameData.id, trigger.sourceCard().getName());
            processNextPucasMischiefTarget(gameData);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(trigger);
        playerInputService.beginPermanentChoice(gameData, trigger.controllerId(), validOwnTargets,
                trigger.sourceCard().getName() + " — Choose a nonland permanent you control.");

        gameLogService.append(gameData, GameLog.cardThen(trigger.sourceCard(), "'s upkeep ability triggers."));
        log.info("Game {} - {} upkeep trigger awaiting own target selection (Puca's Mischief)",
                gameData.id, trigger.sourceCard().getName());
    }

    private ExchangeControlOfTargetPermanentsEffect interdependentExchange(List<CardEffect> effects) {
        return effects.stream()
                .map(effect -> effect instanceof MayEffect may ? may.wrapped() : effect)
                .filter(ExchangeControlOfTargetPermanentsEffect.class::isInstance)
                .map(ExchangeControlOfTargetPermanentsEffect.class::cast)
                .findFirst().orElse(null);
    }

    private boolean hasOpponentCreatureAtMostPower(GameData gameData, UUID controllerId, int power) {
        for (UUID pid : gameData.orderedPlayerIds) {
            if (pid.equals(controllerId)) continue;
            List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
            if (battlefield == null) continue;
            for (Permanent permanent : battlefield) {
                if (gameQueryService.isCreature(gameData, permanent)
                        && gameQueryService.getEffectivePower(gameData, permanent) <= power) {
                    return true;
                }
            }
        }
        return false;
    }

    private void handleOpeningHandTriggers(GameData gameData) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> hand = gameData.playerHands.get(playerId);
            if (hand == null) continue;

            for (Card card : hand) {
                List<CardEffect> openingHandEffects = card.getEffects(EffectSlot.ON_OPENING_HAND_REVEAL);
                if (openingHandEffects == null || openingHandEffects.isEmpty()) continue;

                for (CardEffect effect : openingHandEffects) {
                    // Leyline effects are handled during the pregame procedure
                    // (MulliganService.startGame), not during the first upkeep.
                    if (effect instanceof MayEffect may
                            && may.wrapped() instanceof PregameBattlefieldChoiceEffect) {
                        continue;
                    }
                    if (effect instanceof MayEffect may) {
                        gameData.queueMayAbility(card, playerId, may);
                    } else {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                card,
                                playerId,
                                card.getName() + "'s opening hand ability",
                                new ArrayList<>(List.of(effect))
                        ));

                        String playerName = gameData.playerIdToName.get(playerId);
                        gameLogService.append(gameData,
                                GameLog.textCardText(playerName + " reveals ", card, " from their opening hand."));
                        log.info("Game {} - {} reveals {} from opening hand, trigger pushed onto stack",
                                gameData.id, playerName, card.getName());
                    }
                }
            }
        }
    }

    /**
     * Executes the draw step: the active player draws a card (rule 504.1),
     * unless it is turn 1 for the starting player (rule 103.7a).
     * Then scans for {@code DRAW_TRIGGERED} and {@code EACH_DRAW_TRIGGERED}
     * abilities.
     *
     * @param gameData the current game state to modify
     */
    public void handleDrawStep(GameData gameData) {
        UUID activePlayerId = gameData.activePlayerId;

        // The starting player skips their entire draw step on turn 1 (rule 103.7a)
        if (gameData.turnNumber == 1 && activePlayerId.equals(gameData.startingPlayerId)) {
            String logEntry = gameData.playerIdToName.get(activePlayerId) + " skips the draw (first turn).";
            gameLogService.append(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} skips draw on turn 1", gameData.id, gameData.playerIdToName.get(activePlayerId));
            return;
        }

        if (gameData.skippedStepOrPhasesThisTurn
                .getOrDefault(activePlayerId, Set.of()).contains(SkipStepOrPhaseKind.DRAW_STEP)) {
            String logEntry = gameData.playerIdToName.get(activePlayerId) + " skips their draw step.";
            gameLogService.append(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} skips draw step (chosen step or phase)", gameData.id,
                    gameData.playerIdToName.get(activePlayerId));
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
            return;
        }

        if (currentTurnSkip != null && currentTurnSkip == gameData.turnNumber) {
            String logEntry = gameData.playerIdToName.get(activePlayerId) + " skips their draw step.";
            gameLogService.append(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} skips draw step (current-turn effect)", gameData.id, gameData.playerIdToName.get(activePlayerId));
            return;
        }

        // A permanent may instruct its controller to skip their draw step (e.g. Colfenor's Plans).
        if (controlsSkipDrawStep(gameData, activePlayerId)) {
            String logEntry = gameData.playerIdToName.get(activePlayerId) + " skips their draw step.";
            gameLogService.append(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} skips draw step (SkipDrawStepEffect)", gameData.id, gameData.playerIdToName.get(activePlayerId));
            return;
        }

        DrawStepReplacementSource replacement = findMaySkipDrawStepDrawSource(gameData, activePlayerId);
        if (replacement != null) {
            gameData.pendingMayAbilities.add(new PendingMayAbility(
                    replacement.card(),
                    activePlayerId,
                    List.of(new ReplaceSingleDrawEffect(activePlayerId, replacement.kind())),
                    replacement.kind() == DrawReplacementKind.FASTING
                            ? "Skip your draw step and gain 2 life?"
                            : "Skip your draw? Until your next turn you can only be attacked by creatures with flying and/or islandwalk."
            ));

            if (replacement.kind() == DrawReplacementKind.ISLAND_SANCTUARY) {
                // Draw step triggered abilities (e.g. Howling Mine) still trigger at the beginning of the step.
                handleDrawStepTriggers(gameData);
            }

            if (!gameData.pendingMayAbilities.isEmpty() && !gameData.interaction.isAwaitingInput()) {
                playerInputService.processNextMayAbility(gameData);
            }
            return;
        }

        // Normal draw (turn-based action, rule 504.1)
        drawService.resolveDrawCard(gameData, activePlayerId);

        // Check for draw step triggered abilities (e.g. Howling Mine)
        handleDrawStepTriggers(gameData);

        if (!gameData.pendingMayAbilities.isEmpty() && !gameData.interaction.isAwaitingInput()) {
            playerInputService.processNextMayAbility(gameData);
        }
    }

    private DrawStepReplacementSource findMaySkipDrawStepDrawSource(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return null;
        for (Permanent perm : battlefield) {
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.MAY_SKIP_DRAW_STEP_DRAW)) {
                if (effect instanceof MaySkipDrawReplacementEffect replacement) {
                    return new DrawStepReplacementSource(perm.getCard(), replacement.replacementKind());
                }
            }
        }
        return null;
    }

    private record DrawStepReplacementSource(Card card, DrawReplacementKind kind) {
    }

    private boolean controlsSkipDrawStep(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return false;
        for (Permanent perm : battlefield) {
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof SkipDrawStepEffect) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean playersSkipUpkeepStepApplies(GameData gameData) {
        boolean globalSkip = gameData.anyPermanentMatches(permanent ->
                permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(PlayersSkipUpkeepStepEffect::isGlobal));
        if (globalSkip) {
            return true;
        }

        if (!gameData.playerHands.getOrDefault(gameData.activePlayerId, List.of()).isEmpty()) {
            return false;
        }
        return gameData.playerBattlefields.getOrDefault(gameData.activePlayerId, List.of()).stream()
                .flatMap(permanent -> permanent.getCard().getEffects(EffectSlot.STATIC).stream())
                .anyMatch(PlayersSkipUpkeepStepEffect::isControllerScoped);
    }

    public void handleDrawStepTriggers(GameData gameData) {
        UUID activePlayerId = gameData.activePlayerId;
        collectEmblemStepTriggers(gameData, EmblemTriggerStep.OPPONENT_DRAW_STEP);
        collectEmblemStepTriggers(gameData, EmblemTriggerStep.DRAW_STEP);

        // Nafs Asp: "that player loses N life at the beginning of their next draw step unless they
        // pay {M} before that draw step." Delayed trigger keyed to the damaged player's own draw
        // step — fired here as a "you may pay {M}; if you don't, lose N life" prompt controlled by
        // that player (paying avoids the loss, declining incurs it).
        if (gameData.hasDelayedAction(LoseLifeAtNextDrawStepUnlessPays.class)) {
            List<LoseLifeAtNextDrawStepUnlessPays> pending = gameData.drainDelayedActions(
                    LoseLifeAtNextDrawStepUnlessPays.class, a -> a.playerId().equals(activePlayerId));
            for (LoseLifeAtNextDrawStepUnlessPays action : pending) {
                ForcedCostOrElseEffect payOrLoseLife = new ForcedCostOrElseEffect(
                        new PayManaCost("{" + action.payAmount() + "}"),
                        new ArrayList<>(List.of(new LoseLifeEffect(action.lifeLoss()))),
                        true);
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        action.sourceCard(),
                        activePlayerId,
                        action.sourceCard().getName() + "'s delayed ability",
                        new ArrayList<>(List.of(payOrLoseLife))));

                gameLogService.append(gameData, GameLog.cardThen(action.sourceCard(),
                        "'s delayed ability triggers — " + gameData.playerIdToName.get(activePlayerId)
                        + " loses " + action.lifeLoss() + " life unless they pay {" + action.payAmount() + "}."));
                log.info("Game {} - {} delayed draw-step pay-or-lose-life trigger pushed for {}",
                        gameData.id, action.sourceCard().getName(), gameData.playerIdToName.get(activePlayerId));
            }
        }

        // Check active player's battlefield for DRAW_TRIGGERED effects (controller's own draw step only)
        List<Permanent> activeBattlefield = gameData.playerBattlefields.get(activePlayerId);
        if (activeBattlefield != null) {
            for (Permanent perm : activeBattlefield) {
                List<CardEffect> drawEffects = perm.getCard().getEffects(EffectSlot.DRAW_TRIGGERED);
                if (drawEffects == null || drawEffects.isEmpty()) continue;

                for (CardEffect effect : drawEffects) {
                    if (effect instanceof MayEffect may) {
                        gameData.queueMayAbility(perm.getCard(), activePlayerId, may);
                    } else {
                        StackEntry triggerEntry = new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                activePlayerId,
                                perm.getCard().getName() + "'s draw step ability",
                                new ArrayList<>(List.of(effect)),
                                activePlayerId,
                                perm.getId()
                        );
                        if (effect instanceof PutCountersOnUntapLockedPermanentsEffect) {
                            List<UUID> lockedPermanentIds = new ArrayList<>();
                            gameData.forEachPermanent((playerId, permanent) -> {
                                if (permanent.getUntapPreventedByPermanentIds().contains(perm.getId())) {
                                    lockedPermanentIds.add(permanent.getId());
                                }
                            });
                            triggerEntry.setEventCardIds(lockedPermanentIds);
                        }
                        gameData.stack.add(triggerEntry);

                        gameLogService.append(gameData, GameLog.cardThen(perm.getCard(), "'s draw step ability triggers."));
                        log.info("Game {} - {} draw-step trigger pushed onto stack", gameData.id, perm.getCard().getName());
                    }
                }
            }
        }

        // Auras: ENCHANTED_PERMANENT_CONTROLLER_DRAW_TRIGGERED fires on the enchanted permanent's
        // controller's draw step (e.g. Righteous Authority), baking that player as targetId.
        gameData.forEachPermanent((auraOwnerId, perm) -> {
            List<CardEffect> enchantedControllerDrawEffects =
                    perm.getCard().getEffects(EffectSlot.ENCHANTED_PERMANENT_CONTROLLER_DRAW_TRIGGERED);
            if (enchantedControllerDrawEffects == null || enchantedControllerDrawEffects.isEmpty()) return;
            if (!perm.isAttached()) return;

            UUID enchantedPermanentControllerId =
                    gameQueryService.findPermanentController(gameData, perm.getAttachedTo());
            if (enchantedPermanentControllerId == null) return;
            if (!enchantedPermanentControllerId.equals(activePlayerId)) return;

            for (CardEffect effect : enchantedControllerDrawEffects) {
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        auraOwnerId,
                        perm.getCard().getName() + "'s draw step ability",
                        new ArrayList<>(List.of(effect)),
                        enchantedPermanentControllerId,
                        perm.getId()
                ));

                gameLogService.append(gameData, GameLog.cardThen(perm.getCard(), "'s draw step ability triggers."));
                log.info("Game {} - {} enchanted-permanent-controller draw trigger pushed onto stack",
                        gameData.id, perm.getCard().getName());
            }
        });

        // Check all battlefields for EACH_DRAW_TRIGGERED effects (all players' draw steps)
        gameData.forEachPermanent((playerId, perm) -> {
            List<CardEffect> drawEffects = perm.getCard().getEffects(EffectSlot.EACH_DRAW_TRIGGERED);
            if (drawEffects == null || drawEffects.isEmpty()) return;

            for (CardEffect effect : drawEffects) {
                // Intervening-if: skip trigger if the effect requires an untapped source and it's tapped
                if (effect instanceof DrawCardForTargetPlayerEffect dcEffect
                        && dcEffect.requireSourceUntapped() && perm.isTapped()) {
                    continue;
                }

                // "At the beginning of each opponent's draw step" — never triggers on the controller's own draw step.
                if (effect instanceof OpponentDrawStepOnlyEffect && playerId.equals(activePlayerId)) {
                    continue;
                }

                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        playerId,
                        perm.getCard().getName() + "'s draw step ability",
                        new ArrayList<>(List.of(effect)),
                        activePlayerId,
                        perm.getId()
                ));

                gameLogService.append(gameData, GameLog.cardThen(perm.getCard(), "'s draw step ability triggers."));
                log.info("Game {} - {} draw-step trigger pushed onto stack", gameData.id, perm.getCard().getName());
            }
        });
    }

    /**
     * Fires Chancellor-style delayed mana triggers at the beginning of the
     * revealing player's first precombat main phase.
     *
     * @param gameData the current game state to modify
     */
    public void handlePrecombatMainTriggers(GameData gameData) {
        // Saga lore counters: add a lore counter to each Saga the active player controls (MTG Rule 714.3b)
        handleSagaLoreCounters(gameData);

        handlePrecombatMainBattlefieldTriggers(gameData);

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
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            trigger.sourceCard(),
                            trigger.revealingPlayerId(),
                            trigger.sourceCard().getName() + "'s ability",
                            new ArrayList<>(List.of(trigger.effect()))
                    ));

                    gameLogService.append(gameData,
                            GameLog.cardThen(trigger.sourceCard(), "'s delayed trigger fires — adds mana."));
                    log.info("Game {} - {}'s opening hand mana trigger fires for {}",
                            gameData.id, trigger.sourceCard().getName(),
                            gameData.playerIdToName.get(activePlayerId));
                }
            }
        }

        drainAddManaAtNextMainPhase(gameData, true);

        if (gameData.hasPendingInteraction(PermanentChoiceContext.TriggeredModalTrigger.class)) {
            triggerCollectionService.processNextTriggeredModalTrigger(gameData);
        } else if (gameData.hasPendingInteraction(PermanentChoiceContext.MainPhasePlayerTargetTrigger.class)) {
            processNextMainPhasePlayerTarget(gameData);
        }
        if (gameData.hasPendingInteraction(PermanentChoiceContext.SpellGraveyardTargetTrigger.class)
                && !gameData.interaction.isAwaitingInput()) {
            triggerCollectionService.processNextSpellGraveyardTargetTrigger(gameData);
        }
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
            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
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

    /**
     * Fires triggered abilities on permanents the active player controls at the
     * beginning of the precombat main phase (e.g. Abstract Paintmage).
     */
    private void handlePrecombatMainBattlefieldTriggers(GameData gameData) {
        UUID activePlayerId = gameData.activePlayerId;
        List<Permanent> battlefield = gameData.playerBattlefields.get(activePlayerId);
        if (battlefield == null) {
            return;
        }

        for (Permanent perm : battlefield) {
            int previousCopies = gameData.beginTriggeredAbilityCopies(1
                    + gameQueryService.countAdditionalTriggeredAbilityTriggers(
                    gameData, activePlayerId, perm));
            try {
            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.PRECOMBAT_MAIN_TRIGGERED);
            if (effects == null || effects.isEmpty()) {
                continue;
            }

            // Intervening-if: a counter-threshold gate ("if this artifact has a charge counter on
            // it" — Ventifact Bottle) is checked at trigger time as well as on resolution, so the
            // ability does not even go on the stack when the source has no counters.
            List<CardEffect> triggering = effects.stream()
                    .filter(effect -> conditionEvaluationService.isInterveningIfMet(
                            gameData, effect, perm, activePlayerId))
                    .toList();
            if (triggering.isEmpty()) {
                continue;
            }

            List<CardEffect> modalEffects = triggering.stream()
                    .filter(effect -> effect instanceof ChooseModeNotYetChosenEffect)
                    .toList();
            for (CardEffect modalEffect : modalEffects) {
                ChooseModeNotYetChosenEffect modal = (ChooseModeNotYetChosenEffect) modalEffect;
                gameData.queueInteraction(new PermanentChoiceContext.TriggeredModalTrigger(
                        perm.getCard(), activePlayerId, new ChooseOneEffect(modal.options()), perm.getId(), false, true));
            }

            List<CardEffect> playerTargetEffects = triggering.stream()
                    .filter(effect -> !modalEffects.contains(effect))
                    .filter(effect -> effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                            && !effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT))
                    .toList();
            if (!playerTargetEffects.isEmpty()) {
                gameData.queueInteraction(new PermanentChoiceContext.MainPhasePlayerTargetTrigger(
                        perm.getCard(), activePlayerId, new ArrayList<>(playerTargetEffects), perm.getId(),
                        perm.getCard().getTargetFilter()));
            }

            List<CardEffect> graveyardTargetEffects = triggering.stream()
                    .filter(effect -> !modalEffects.contains(effect))
                    .filter(effect -> !playerTargetEffects.contains(effect))
                    .filter(effect -> effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD))
                    .toList();
            if (!graveyardTargetEffects.isEmpty()) {
                gameData.queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                        perm.getCard(), activePlayerId, new ArrayList<>(graveyardTargetEffects)));
            }

            List<CardEffect> nonTargetEffects = triggering.stream()
                    .filter(effect -> !modalEffects.contains(effect)
                            && !playerTargetEffects.contains(effect)
                            && !graveyardTargetEffects.contains(effect))
                    .toList();
            if (!nonTargetEffects.isEmpty()) {
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        activePlayerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(nonTargetEffects),
                        null,
                        perm.getId()
                );
                entry.setSourcePermanentSnapshot(new Permanent(perm));
                if (perm.isAttached()) {
                    Permanent attached = gameQueryService.findPermanentById(gameData, perm.getAttachedTo());
                    if (attached != null) {
                        entry.setAttachedPermanentSnapshot(new Permanent(attached));
                    }
                }
                gameData.stack.add(entry);
            }

            gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
            log.info("Game {} - {} precombat main trigger pushed onto stack",
                    gameData.id, perm.getCard().getName());
            } finally {
                gameData.restoreTriggeredAbilityCopies(previousCopies);
            }
        }
    }

    /**
     * Fires triggered abilities that watch every player's first main phase, regardless of who
     * controls the source (Eladamri's Vineyard). The ability is controlled by the source's
     * controller; the active player is carried as the entry's target so effects that act on
     * "that player" know who the phase belongs to.
     */
    private void handleEachPrecombatMainTriggers(GameData gameData) {
        UUID activePlayerId = gameData.activePlayerId;
        gameData.forEachPermanent((playerId, perm) -> {
            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.EACH_PRECOMBAT_MAIN_TRIGGERED);
            if (effects == null || effects.isEmpty()) {
                return;
            }

            List<CardEffect> triggering = effects.stream()
                    .filter(effect -> conditionEvaluationService.isInterveningIfMet(
                            gameData, effect, perm, playerId))
                    .toList();
            if (triggering.isEmpty()) {
                return;
            }

            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    perm.getCard(),
                    playerId,
                    perm.getCard().getName() + "'s ability",
                    new ArrayList<>(triggering),
                    activePlayerId,
                    perm.getId()
            );
            entry.setActivePlayerId(activePlayerId);
            gameData.stack.add(entry);

            gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
            log.info("Game {} - {} each-precombat-main trigger pushed onto stack",
                    gameData.id, perm.getCard().getName());
        });
    }

    /**
     * Fires triggered abilities on permanents the active player controls at the
     * beginning of each postcombat main phase (e.g. Neheb, the Eternal).
     *
     * @param gameData the current game state to modify
     */
    public void handlePostcombatMainTriggers(GameData gameData) {
        UUID activePlayerId = gameData.activePlayerId;
        List<Permanent> battlefield = gameData.playerBattlefields.get(activePlayerId);
        if (battlefield == null) {
            return;
        }

        for (Permanent perm : battlefield) {
            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.POSTCOMBAT_MAIN_TRIGGERED);
            if (effects == null || effects.isEmpty()) {
                continue;
            }

            List<CardEffect> triggering = effects.stream()
                    .filter(effect -> conditionEvaluationService.isInterveningIfMet(
                            gameData, effect, perm, activePlayerId))
                    .toList();
            if (triggering.isEmpty()) {
                continue;
            }

            List<CardEffect> playerTargetEffects = triggering.stream()
                    .filter(effect -> effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                            && !effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT))
                    .toList();
            if (!playerTargetEffects.isEmpty()) {
                gameData.queueInteraction(new PermanentChoiceContext.MainPhasePlayerTargetTrigger(
                        perm.getCard(), activePlayerId, new ArrayList<>(playerTargetEffects), perm.getId(),
                        perm.getCard().getTargetFilter()));
            }

            List<CardEffect> nonPlayerTargetEffects = triggering.stream()
                    .filter(effect -> !playerTargetEffects.contains(effect))
                    .toList();
            if (!nonPlayerTargetEffects.isEmpty()) {
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        activePlayerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(nonPlayerTargetEffects),
                        null,
                        perm.getId()
                ));
            }

            gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
            log.info("Game {} - {} postcombat main trigger pushed onto stack",
                    gameData.id, perm.getCard().getName());
        }

        if (gameData.hasPendingInteraction(PermanentChoiceContext.MainPhasePlayerTargetTrigger.class)) {
            processNextMainPhasePlayerTarget(gameData);
        }
    }

    /** Presents mandatory player targets for a main-phase trigger as it is put on the stack. */
    public void processNextMainPhasePlayerTarget(GameData gameData) {
        if (!gameData.hasPendingInteraction(PermanentChoiceContext.MainPhasePlayerTargetTrigger.class)) {
            return;
        }

        PermanentChoiceContext.MainPhasePlayerTargetTrigger trigger =
                gameData.pollPendingInteraction(PermanentChoiceContext.MainPhasePlayerTargetTrigger.class);
        TargetFilter targetFilter = trigger.targetFilter() != null
                ? trigger.targetFilter() : trigger.sourceCard().getTargetFilter();
        List<UUID> validPlayerTargets = validTargetService.filterValidPlayerTargets(
                gameData, targetFilter, new ArrayList<>(gameData.orderedPlayerIds), trigger.controllerId());

        if (validPlayerTargets.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.cardThen(trigger.sourceCard(), "'s main-phase trigger has no valid targets."));
            processNextMainPhasePlayerTarget(gameData);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(trigger);
        playerInputService.beginAnyTargetChoice(gameData, trigger.controllerId(), List.of(), validPlayerTargets,
                trigger.sourceCard().getName() + "'s ability — Choose target opponent.");
        gameLogService.append(gameData, GameLog.cardThen(trigger.sourceCard(), "'s main-phase ability triggers."));
        log.info("Game {} - {} main-phase trigger awaiting player target selection",
                gameData.id, trigger.sourceCard().getName());
    }

    /**
     * Scans every battlefield for {@code END_OF_COMBAT_TRIGGERED} abilities and pushes them onto
     * the stack as the end of combat step begins (CR 511.2). Unlike the postcombat-main scan this
     * runs on all players' permanents, because "at end of combat" is not restricted to the
     * controller's own turn. The triggers are non-targeting.
     *
     * @param gameData the current game state to modify
     */
    public void handleEndOfCombatTriggers(GameData gameData) {
        List<DelayedEndOfCombatTrigger> delayedTriggers =
                gameData.drainDelayedActions(DelayedEndOfCombatTrigger.class);
        for (DelayedEndOfCombatTrigger delayedTrigger : delayedTriggers) {
            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    delayedTrigger.sourceCard(),
                    delayedTrigger.controllerId(),
                    delayedTrigger.sourceCard().getName() + "'s end-of-combat ability",
                    new ArrayList<>(List.of(delayedTrigger.effect())),
                    (UUID) null,
                    delayedTrigger.sourcePermanentId()
            ));

            gameLogService.append(gameData, GameLog.cardThen(
                    delayedTrigger.sourceCard(), "'s end-of-combat ability triggers."));
            log.info("Game {} - {} delayed end-of-combat trigger pushed onto stack",
                    gameData.id, delayedTrigger.sourceCard().getName());
        }

        gameData.forEachPermanent((playerId, perm) -> {
            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.END_OF_COMBAT_TRIGGERED);
            if (effects == null || effects.isEmpty()) {
                return;
            }
            List<CardEffect> triggeredEffects = effects.stream()
                    .filter(effect -> !(effect instanceof ConditionalEffect conditional)
                            || !conditional.interveningIf()
                            || conditionEvaluationService.isMet(gameData, conditional.condition(),
                                    ConditionContext.forPermanent(perm, playerId)))
                    .toList();
            if (triggeredEffects.isEmpty()) {
                return;
            }

            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    perm.getCard(),
                    playerId,
                    perm.getCard().getName() + "'s ability",
                    new ArrayList<>(triggeredEffects),
                    (UUID) null,
                    perm.getId()
            ));

            gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
            log.info("Game {} - {} end-of-combat trigger pushed onto stack",
                    gameData.id, perm.getCard().getName());
        });
    }

    /**
     * Adds a lore counter to each Saga the active player controls and triggers
     * the appropriate chapter ability (MTG Rule 714.3b).
     * Called at the beginning of the active player's precombat main phase.
     */
    private void handleSagaLoreCounters(GameData gameData) {
        UUID activePlayerId = gameData.activePlayerId;
        List<Permanent> battlefield = gameData.playerBattlefields.get(activePlayerId);
        if (battlefield == null) return;

        // Collect Sagas first to avoid ConcurrentModificationException
        List<Permanent> sagas = battlefield.stream()
                .filter(p -> p.getCard().isSaga())
                .toList();

        for (Permanent saga : sagas) {
            Card card = saga.getCard();
            int newLoreCount = saga.getCounterCount(CounterType.LORE) + 1;
            saga.setCounterCount(CounterType.LORE, newLoreCount);

            gameLogService.append(gameData,
                    GameLog.cardThen(card, " gets a lore counter (" + newLoreCount + ")."));
            log.info("Game {} - {} gets lore counter {}", gameData.id, card.getName(), newLoreCount);

            // Trigger the appropriate chapter ability
            EffectSlot chapterSlot = switch (newLoreCount) {
                case 1 -> EffectSlot.SAGA_CHAPTER_I;
                case 2 -> EffectSlot.SAGA_CHAPTER_II;
                case 3 -> EffectSlot.SAGA_CHAPTER_III;
                case 4 -> EffectSlot.SAGA_CHAPTER_IV;
                case 5 -> EffectSlot.SAGA_CHAPTER_V;
                default -> null;
            };
            if (chapterSlot == null) continue;

            List<CardEffect> chapterEffects = card.getEffects(chapterSlot);
            if (chapterEffects.isEmpty()) continue;

            String chapterName = switch (newLoreCount) {
                case 1 -> "I";
                case 2 -> "II";
                case 3 -> "III";
                case 4 -> "IV";
                case 5 -> "V";
                default -> String.valueOf(newLoreCount);
            };

            boolean needsPlayerTarget = chapterEffects.stream()
                    .anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.PLAYER))
                    || card.getSagaChapterTargetFilters(chapterSlot).stream()
                    .anyMatch(com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter.class::isInstance);
            boolean hasSagaTargetGroups = !card.getSagaChapterTargetGroups(chapterSlot).isEmpty();
            boolean needsPermanentTarget = chapterEffects.stream()
                    .anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.PERMANENT))
                    || hasSagaTargetGroups;
            boolean needsGraveyardTarget = chapterEffects.stream().anyMatch(e ->
                    e.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)
                            || e instanceof ReturnTargetCardsFromGraveyardToHandEffect);
            if (hasSagaTargetGroups) {
                gameData.queueInteraction(
                        new PermanentChoiceContext.SagaChapterTarget(card, activePlayerId,
                                new ArrayList<>(chapterEffects), saga.getId(), chapterName,
                                card.getSagaChapterTargetFilters(chapterSlot),
                                card.getSagaChapterTargetGroups(chapterSlot), List.of(), 0));
                gameLogService.append(gameData,
                        GameLog.cardThen(card, "'s chapter " + chapterName + " ability triggers."));
                log.info("Game {} - {} chapter {} triggers (awaiting grouped target selection)",
                        gameData.id, card.getName(), chapterName);
            } else if (needsPlayerTarget && needsPermanentTarget) {
                gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                        card, activePlayerId, new ArrayList<>(chapterEffects), false,
                        sagaChapterAnyTargetFilter(chapterEffects), 0, saga.getId()));
                gameLogService.append(gameData,
                        GameLog.cardThen(card, "'s chapter " + chapterName + " ability triggers."));
                log.info("Game {} - {} chapter {} triggers (awaiting any target selection)",
                        gameData.id, card.getName(), chapterName);
            } else if (needsPlayerTarget) {
                gameData.queueInteraction(
                        new PermanentChoiceContext.SagaChapterPlayerTarget(card, activePlayerId,
                                new ArrayList<>(chapterEffects), saga.getId(), chapterName,
                                card.getSagaChapterTargetFilters(chapterSlot)));
                gameLogService.append(gameData,
                        GameLog.cardThen(card, "'s chapter " + chapterName + " ability triggers."));
                log.info("Game {} - {} chapter {} triggers (awaiting player target selection)",
                        gameData.id, card.getName(), chapterName);
            } else if (needsPermanentTarget) {
                gameData.queueInteraction(
                        new PermanentChoiceContext.SagaChapterTarget(card, activePlayerId,
                                new ArrayList<>(chapterEffects), saga.getId(), chapterName,
                                card.getSagaChapterTargetFilters(chapterSlot),
                                card.getSagaChapterTargetGroups(chapterSlot), List.of(), 0));
                gameLogService.append(gameData,
                        GameLog.cardThen(card, "'s chapter " + chapterName + " ability triggers."));
                log.info("Game {} - {} chapter {} triggers (awaiting target selection)", gameData.id, card.getName(), chapterName);
            } else if (needsGraveyardTarget) {
                gameData.queueInteraction(
                        new PermanentChoiceContext.SagaChapterGraveyardTarget(card, activePlayerId,
                                new ArrayList<>(chapterEffects), saga.getId(), chapterName));
                gameLogService.append(gameData,
                        GameLog.cardThen(card, "'s chapter " + chapterName + " ability triggers."));
                log.info("Game {} - {} chapter {} triggers (awaiting graveyard target selection)", gameData.id, card.getName(), chapterName);
            } else {
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        card,
                        activePlayerId,
                        card.getName() + "'s chapter " + chapterName + " ability",
                        new ArrayList<>(chapterEffects),
                        null,
                        saga.getId()
                ));

                gameLogService.append(gameData,
                        GameLog.cardThen(card, "'s chapter " + chapterName + " ability triggers."));
                log.info("Game {} - {} chapter {} triggers", gameData.id, card.getName(), chapterName);
            }
        }

        // Process any queued saga chapter target selections
        if (gameData.hasPendingInteraction(PermanentChoiceContext.SagaChapterTarget.class)) {
            triggerCollectionService.processNextSagaChapterTarget(gameData);
        }
        if (gameData.hasPendingInteraction(PermanentChoiceContext.SagaChapterPlayerTarget.class)) {
            triggerCollectionService.processNextSagaChapterPlayerTarget(gameData);
        }
        if (gameData.hasPendingInteraction(PermanentChoiceContext.SpellTargetTriggerAnyTarget.class)) {
            triggerCollectionService.processNextSpellTargetTrigger(gameData);
        }
        if (gameData.hasPendingInteraction(PermanentChoiceContext.SagaChapterGraveyardTarget.class)) {
            triggerCollectionService.processNextSagaChapterGraveyardTarget(gameData);
        }
    }

    private TargetFilter sagaChapterAnyTargetFilter(List<CardEffect> chapterEffects) {
        CardEffect permanentTargetEffect = chapterEffects.stream()
                .filter(effect -> effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT))
                .findFirst()
                .orElseThrow();
        var permanentPredicate = permanentTargetEffect.targetSpec().targetPredicate()
                .permanentRestriction().orElse(new PermanentTruePredicate());
        PlayerRelation relation = chapterEffects.stream()
                .filter(effect -> effect.targetSpec().admits(TargetPredicate.Kind.PLAYER))
                .map(CardEffect::targetPlayerRelation)
                .findFirst()
                .orElse(PlayerRelation.ANY);
        return new AnyTargetPredicateTargetFilter(permanentPredicate,
                new PlayerRelationPredicate(relation), "target opponent or planeswalker");
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
                        && (!p.onlyOnControllersTurn()
                        || (p.timingControllerId() != null
                        ? p.timingControllerId() : p.controllerId()).equals(gameData.activePlayerId)));
        if (matching.isEmpty()) {
            return;
        }

        processPendingExileReturns(gameData, matching);
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

    private void returnPendingExileCard(GameData gameData, PendingExileReturn pending, UUID attackTargetId) {
        UUID controllerId = pending.controllerId();

        List<Card> cards = new ArrayList<>();
        cards.add(pending.card());
        cards.addAll(pending.additionalCards());

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
            for (Card additional : pending.additionalCards()) {
                if (gameData.removeFromExile(additional.getId())) {
                    returned.add(additional);
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
            if (pending.returnTapped()) {
                perm.tap();
            }
            if (pending.returnAttacking() && attackTargetId != null) {
                perm.setAttacking(true);
                perm.setAttackTarget(attackTargetId);
            }
            boolean isCreature = card.hasType(CardType.CREATURE);
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
                int loyalty = gameQueryService.replaceCounters(gameData, controllerId, CounterType.LOYALTY,
                        card.getLoyalty() + pending.loyaltyCountersOnPlaneswalkers(), isCreature);
                perm.setCounterCount(CounterType.LOYALTY, loyalty);
            }
            if (pending.counterTypeOnReturn() != null && pending.counterAmountOnReturn() > 0) {
                int counters = gameQueryService.replaceCounters(gameData, perm, controllerId,
                        pending.counterTypeOnReturn(), pending.counterAmountOnReturn(), controllerId);
                if (counters > 0) {
                    perm.setCounterCount(pending.counterTypeOnReturn(), counters);
                }
            }
            perm.setEnteredFromExile(true);
            if (pending.grantHaste()) {
                perm.getPersistentGrantedKeywords().add(Keyword.HASTE);
            }
            if (card.getId().equals(pending.card().getId())) {
                primaryPermanent = perm;
            }
            boolean attachToPrimary = primaryPermanent != null
                    && pending.cardsToAttachToPrimary().contains(card.getId());
            UUID cardControllerId = attachToPrimary && card.getOwnerId() != null
                    ? card.getOwnerId() : controllerId;
            if (attachToPrimary) {
                perm.setAttachedTo(primaryPermanent.getId());
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
                        StackEntry discardEntry = new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                pending.followUpSourceCard(),
                                pending.timingControllerId(),
                                pending.followUpSourceCard().getName() + "'s delayed trigger — discard "
                                        + discardCount + " cards",
                                new ArrayList<>(List.of(new DiscardEffect(
                                        discardCount, DiscardRecipient.CONTROLLER))));
                        discardEntry.setNonTargeting(true);
                        gameData.stack.add(discardEntry);
                        gameLogService.append(gameData,
                                GameLog.cardThen(pending.followUpSourceCard(), "'s delayed trigger discards cards."));
                    }
                }
            }
        }
    }

    /**
     * Processes end-step triggers: exiles pending tokens (e.g. Mimic Vat),
     * then scans battlefields for
     * {@code END_STEP_TRIGGERED} and {@code CONTROLLER_END_STEP_TRIGGERED}
     * abilities.
     *
     * @param gameData the current game state to modify
     */
    /**
     * Resolves the queued {@link DelayedGraveyardToBattlefieldSelfReturn} actions matching
     * {@code filter}: each card still in its owner's graveyard returns to the battlefield under that
     * owner's control, tapped and/or with counters as the action requests. Shared by the end-step
     * timing (Sand Golem, Ivory Gargoyle) and the owner's-next-upkeep timing (Phytotitan).
     */
    private void resolveDelayedSelfReturns(GameData gameData,
                                           Predicate<DelayedGraveyardToBattlefieldSelfReturn> filter) {
        if (!gameData.hasDelayedAction(DelayedGraveyardToBattlefieldSelfReturn.class, filter)) {
            return;
        }
        List<DelayedGraveyardToBattlefieldSelfReturn> pendingReturns =
                gameData.drainDelayedActions(DelayedGraveyardToBattlefieldSelfReturn.class, filter);
        for (DelayedGraveyardToBattlefieldSelfReturn pending : pendingReturns) {
            List<Card> graveyard = gameData.playerGraveyards.get(pending.ownerId());
            if (graveyard == null) continue;
            Card cardToReturn = null;
            for (Card card : graveyard) {
                if (card.getId().equals(pending.cardId())) {
                    cardToReturn = card;
                    break;
                }
            }
            if (cardToReturn == null) {
                log.info("Game {} - Delayed graveyard return for card {} skipped (no longer in graveyard)",
                        gameData.id, pending.cardId());
                continue;
            }
            if (gameQueryService.isCardBlockedFromEnteringFromZone(gameData, cardToReturn, com.github.laxika.magicalvibes.model.Zone.GRAVEYARD)) {
                gameLogService.append(gameData,
                        GameLog.cardThen(cardToReturn, " can't return from the graveyard; it stays in the graveyard."));
                continue;
            }

            permanentRemovalService.removeCardFromGraveyardById(gameData, cardToReturn.getId());
            Permanent permanent = new Permanent(cardToReturn);
            permanent.setEnteredFromGraveyardOwnerId(pending.ownerId());
            if (pending.counterType() != null && pending.counterAmount() > 0) {
                permanent.setCounterCount(pending.counterType(), pending.counterAmount());
            }
            if (pending.tapped()) {
                permanent.tap();
            }
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, pending.ownerId(), permanent);

            gameLogService.append(gameData, GameLog.cardThen(cardToReturn,
                    " returns to the battlefield (delayed trigger)."));
            log.info("Game {} - {} returns to the battlefield from the graveyard (delayed trigger)",
                    gameData.id, cardToReturn.getName());
            battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, pending.ownerId(), cardToReturn, null, false);
        }
    }

    /** Resolves delayed simultaneous graveyard returns scheduled for a player's next upkeep. */
    private void resolveDelayedGraveyardCardsUnderControlAtUpkeep(GameData gameData) {
        if (!gameData.hasDelayedAction(DelayedGraveyardCardsToBattlefieldUnderControl.class,
                action -> action.controllerId().equals(gameData.activePlayerId))) {
            return;
        }

        List<DelayedGraveyardCardsToBattlefieldUnderControl> pendingReturns =
                gameData.drainDelayedActions(DelayedGraveyardCardsToBattlefieldUnderControl.class,
                        action -> action.controllerId().equals(gameData.activePlayerId));
        for (DelayedGraveyardCardsToBattlefieldUnderControl pending : pendingReturns) {
            List<DelayedReturningGraveyardCard> returningCards = new ArrayList<>();
            for (UUID cardId : pending.cardIds()) {
                Card card = gameQueryService.findCardInGraveyardById(gameData, cardId);
                UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, cardId);
                if (card == null || ownerId == null) {
                    log.info("Game {} - Delayed simultaneous return for card {} skipped (no longer in a graveyard)",
                            gameData.id, cardId);
                    continue;
                }
                if (gameQueryService.isCardBlockedFromEnteringFromZone(gameData, card, com.github.laxika.magicalvibes.model.Zone.GRAVEYARD)) {
                    gameLogService.append(gameData,
                            GameLog.cardThen(card, " can't return from the graveyard; it stays in the graveyard."));
                    continue;
                }
                returningCards.add(new DelayedReturningGraveyardCard(card, ownerId));
            }
            if (returningCards.isEmpty()) {
                continue;
            }

            for (DelayedReturningGraveyardCard returningCard : returningCards) {
                permanentRemovalService.removeCardFromGraveyardById(gameData, returningCard.card().getId());
            }

            Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
            List<Permanent> simultaneouslyEntered = new ArrayList<>();
            for (DelayedReturningGraveyardCard returningCard : returningCards) {
                Card card = returningCard.card();
                Permanent permanent = new Permanent(card);
                permanent.setEnteredFromGraveyardOwnerId(returningCard.ownerId());
                battlefieldEntryService.putPermanentOntoBattlefield(
                        gameData, pending.controllerId(), permanent, enterTappedTypes, simultaneouslyEntered);
                simultaneouslyEntered.add(permanent);

                if (!pending.controllerId().equals(returningCard.ownerId())) {
                    gameData.stolenCreatures.put(permanent.getId(), returningCard.ownerId());
                    creatureControlService.applyControlEffect(gameData, pending.controllerId(), permanent,
                            new GainControlOfTargetEffect(ControlDuration.PERMANENT),
                            ControlDuration.PERMANENT.toEffectDuration(), null,
                            "Rescue from the Underworld");
                }

                String playerName = gameData.playerIdToName.get(pending.controllerId());
                gameLogService.append(gameData, GameLog.cardThen(card,
                        " returns to the battlefield under " + playerName + "'s control (Rescue from the Underworld)."));
                log.info("Game {} - {} returns under {}'s control (Rescue from the Underworld)",
                        gameData.id, card.getName(), playerName);
            }
            for (DelayedReturningGraveyardCard returningCard : returningCards) {
                battlefieldEntryService.handleCreatureEnteredBattlefield(
                        gameData, pending.controllerId(), returningCard.card(), null, false);
            }
        }
    }

    private record DelayedReturningGraveyardCard(Card card, UUID ownerId) {}

    public void handleEndStepTriggers(GameData gameData) {
        collectEmblemStepTriggers(gameData, EmblemTriggerStep.END_STEP);

        if (gameData.hasDelayedAction(PutCounterOnPermanentAtNextEndStep.class)) {
            List<PutCounterOnPermanentAtNextEndStep> pendingCounters =
                    gameData.drainDelayedActions(PutCounterOnPermanentAtNextEndStep.class);
            for (PutCounterOnPermanentAtNextEndStep action : pendingCounters) {
                if (gameQueryService.findPermanentById(gameData, action.permanentId()) == null) {
                    continue;
                }
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY, action.sourceCard(), action.controllerId(),
                        action.sourceCard().getName() + "'s delayed ability",
                        new ArrayList<>(List.of(new PutCounterOnTargetPermanentEffect(
                                action.counterType(), action.amount()))),
                        action.permanentId(), (UUID) null);
                entry.setNonTargeting(true);
                gameData.stack.add(entry);
                gameLogService.append(gameData, GameLog.cardThen(action.sourceCard(),
                        "'s delayed ability triggers."));
                log.info("Game {} - {} delayed end-step counter trigger pushed onto stack",
                        gameData.id, action.sourceCard().getName());
            }
        }

        if (gameData.hasDelayedAction(DrawCardsAtNextEndStep.class)) {
            List<DrawCardsAtNextEndStep> pendingDraws =
                    gameData.drainDelayedActions(DrawCardsAtNextEndStep.class);
            for (DrawCardsAtNextEndStep pending : pendingDraws) {
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        pending.sourceCard(),
                        pending.controllerId(),
                        pending.sourceCard().getName() + "'s delayed ability",
                        new ArrayList<>(List.of(new DrawCardEffect(pending.count())))
                ));
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
                long stillExiled = action.cardIds().stream()
                        .filter(cardId -> gameData.findExiledCard(cardId) != null)
                        .count();
                int damage = Math.toIntExact(stillExiled * action.damagePerCard());
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        action.sourceCard(),
                        action.controllerId(),
                        action.sourceCard().getName() + "'s delayed trigger",
                        new ArrayList<>(List.of(new DealDamageToPlayersEffect(
                                damage, DamageRecipient.EACH_OPPONENT))),
                        null,
                        action.sourcePermanentId());
                entry.setNonTargeting(true);
                gameData.stack.add(entry);
                gameLogService.append(gameData, GameLog.cardThen(action.sourceCard(),
                        "'s delayed trigger deals " + damage + " damage to each opponent."));
                log.info("Game {} - {} delayed trigger deals {} damage to each opponent",
                        gameData.id, action.sourceCard().getName(), damage);
            }
        }

        if (gameData.hasDelayedAction(UnattachEquipmentAtNextEndStep.class)) {
            List<UnattachEquipmentAtNextEndStep> pendingUnattaches =
                    gameData.drainDelayedActions(UnattachEquipmentAtNextEndStep.class);
            for (UnattachEquipmentAtNextEndStep pending : pendingUnattaches) {
                Permanent equipment = gameQueryService.findPermanentById(gameData, pending.equipmentId());
                if (equipment == null || equipment.getAttachedTo() == null) {
                    continue;
                }
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        pending.sourceCard(),
                        pending.controllerId(),
                        pending.sourceCard().getName() + "'s delayed ability",
                        new ArrayList<>(List.of(new UnattachEquipmentEffect(pending.equipmentId()))));
                entry.setNonTargeting(true);
                gameData.stack.add(entry);
                gameLogService.append(gameData,
                        GameLog.cardThen(pending.sourceCard(), "'s delayed ability triggers to unattach the Equipment."));
            }
        }

        if (gameData.hasDelayedAction(SacrificeSelfAtNextEndStepTrigger.class,
                action -> action.registeredTurnNumber() == null
                        || (action.registeredTurnNumber() < gameData.turnNumber
                        && action.controllerId() != null
                        && action.controllerId().equals(gameData.activePlayerId)))) {
            List<SacrificeSelfAtNextEndStepTrigger> pendingSacrifices =
                    gameData.drainDelayedActions(SacrificeSelfAtNextEndStepTrigger.class,
                            action -> action.registeredTurnNumber() == null
                                    || (action.registeredTurnNumber() < gameData.turnNumber
                                    && action.controllerId() != null
                                    && action.controllerId().equals(gameData.activePlayerId)));
            for (SacrificeSelfAtNextEndStepTrigger pending : pendingSacrifices) {
                if (gameQueryService.findPermanentById(gameData, pending.permanentId()) == null) {
                    continue;
                }
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        pending.sourceCard(),
                        pending.controllerId(),
                        pending.sourceCard().getName() + "'s delayed ability",
                        new ArrayList<>(List.of(new SacrificeSelfEffect())),
                        null,
                        pending.permanentId());
                entry.setNonTargeting(true);
                gameData.stack.add(entry);
                gameLogService.append(gameData, GameLog.cardThen(
                        pending.sourceCard(), "'s delayed sacrifice ability triggers."));
            }
        }

        // Memory Jar: each player discards their hand and returns the cards exiled by its ability.
        if (gameData.hasDelayedAction(EachPlayerHandExileReturnAtNextEndStep.class)) {
            List<EachPlayerHandExileReturnAtNextEndStep> pending = gameData.drainDelayedActions(
                    EachPlayerHandExileReturnAtNextEndStep.class);
            for (EachPlayerHandExileReturnAtNextEndStep action : pending) {
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        action.sourceCard(),
                        action.controllerId(),
                        action.sourceCard().getName() + "'s delayed trigger",
                        new ArrayList<>(List.of(new DiscardEachPlayerHandAndReturnExiledCardsEffect(
                                action.players()))));
                entry.setNonTargeting(true);
                gameData.stack.add(entry);
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
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        action.sourceCard(),
                        action.controllerId(),
                        action.sourceCard().getName() + "'s delayed trigger",
                        new ArrayList<>(List.of(new ReturnExiledCardsToTargetPlayerHandEffect(
                                action.playerId(), action.cardIds()))));
                entry.setNonTargeting(true);
                gameData.stack.add(entry);
                gameLogService.append(gameData, GameLog.cardThen(action.sourceCard(),
                        "'s delayed trigger returns the exiled hand at the target player's next end step."));
            }
        }

        // Elkin Lair: "At the beginning of the next end step, if the player hasn't played the card,
        // they put it into their graveyard." Chronological next end step — no active-player filter.
        if (gameData.hasDelayedAction(ExileToOwnerGraveyardAtNextEndStep.class)) {
            List<ExileToOwnerGraveyardAtNextEndStep> pending =
                    gameData.drainDelayedActions(ExileToOwnerGraveyardAtNextEndStep.class);
            for (ExileToOwnerGraveyardAtNextEndStep action : pending) {
                gameData.exilePlayPermissions.remove(action.cardId());
                gameData.exilePlayCostModifiers.remove(action.cardId());
                gameData.exilePlayPermissionsExpireEndOfTurn.remove(action.cardId());
                var exiled = gameData.findExiledCard(action.cardId());
                if (exiled == null) {
                    continue;
                }
                gameData.removeFromExile(action.cardId());
                graveyardService.addCardToGraveyard(gameData, action.ownerId(), exiled.card());
                String sourceName = action.sourceCard() != null ? action.sourceCard().getName() : "an effect";
                gameLogService.append(gameData, GameLog.text(
                        "The card exiled with " + sourceName + " is put into its owner's graveyard."));
                log.info("Game {} - unplayed card exiled with {} put into owner's graveyard at end step",
                        gameData.id, sourceName);
            }
        }

        if (gameData.hasDelayedAction(ReturnExiledCardToHandAtNextEndStep.class)) {
            List<ReturnExiledCardToHandAtNextEndStep> pending =
                    gameData.drainDelayedActions(ReturnExiledCardToHandAtNextEndStep.class);
            for (ReturnExiledCardToHandAtNextEndStep action : pending) {
                var exiled = gameData.findExiledCard(action.cardId());
                if (exiled == null) {
                    log.info("Game {} - Delayed next-end-step exile-to-hand return for card {} skipped (no longer in exile)",
                            gameData.id, action.cardId());
                    continue;
                }
                gameData.removeFromExile(action.cardId());
                gameData.addCardToHand(action.ownerId(), exiled.card());
                if (action.sourceCard() != null) {
                    String sourceName = action.sourceCard().getName();
                    gameLogService.append(gameData, GameLog.text(
                            "The card exiled with " + sourceName + " returns to its owner's hand."));
                    log.info("Game {} - uncast card exiled with {} returns to owner's hand at end step",
                            gameData.id, sourceName);
                } else {
                    String playerName = gameData.playerIdToName.get(action.ownerId());
                    gameLogService.append(gameData,
                            GameLog.cardThen(exiled.card(), " returns to " + playerName + "'s hand (delayed trigger)."));
                    log.info("Game {} - {} returns to {}'s hand from exile (delayed next-end-step trigger)",
                            gameData.id, exiled.card().getName(), playerName);
                }
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

                int toughness = Math.max(0, gameQueryService.getEffectiveToughness(gameData, permanent));
                UUID sacrificingPlayerId = gameQueryService.findPermanentController(gameData, permanent.getId());
                if (!permanentRemovalService.removePermanentToGraveyard(gameData, permanent)) {
                    continue;
                }
                triggerCollectionService.checkAllyPermanentSacrificedTriggers(
                        gameData, sacrificingPlayerId, permanent.getCard());
                gameLogService.append(gameData, GameLog.isSacrificed(permanent.getCard()));
                lifeSupport.applyGainLife(gameData, action.controllerId(), toughness,
                        action.sourceCard().getName(), action.sourceCard(), StackEntryType.TRIGGERED_ABILITY,
                        action.controllerId());
                permanentRemovalService.removeOrphanedAuras(gameData);
            }
        }

        if (gameData.hasDelayedAction(DelayedCoinFlipSacrificeTargetPermanentAtEndStep.class)) {
            List<DelayedCoinFlipSacrificeTargetPermanentAtEndStep> pending = gameData.drainDelayedActions(
                    DelayedCoinFlipSacrificeTargetPermanentAtEndStep.class);
            for (DelayedCoinFlipSacrificeTargetPermanentAtEndStep action : pending) {
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        action.sourceCard(),
                        action.controllerId(),
                        action.sourceCard().getName() + "'s delayed trigger",
                        new ArrayList<>(List.of(new FlipCoinWinEffect(null, new SacrificeSelfEffect()))),
                        0,
                        action.permanentId());
                entry.setNonTargeting(true);
                gameData.stack.add(entry);
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
                if (permanent == null
                        || !action.controllerId().equals(
                                gameQueryService.findPermanentController(gameData, permanent.getId()))
                        || permanent.getCard().getManaValue() > action.maxManaValue()) {
                    continue;
                }

                UUID sacrificingPlayerId = gameQueryService.findPermanentController(gameData, permanent.getId());
                if (!permanentRemovalService.removePermanentToGraveyard(gameData, permanent)) {
                    continue;
                }
                triggerCollectionService.checkAllyPermanentSacrificedTriggers(
                        gameData, sacrificingPlayerId, permanent.getCard());
                gameLogService.append(gameData, GameLog.isSacrificed(permanent.getCard()));
                log.info("Game {} - {} sacrificed by a mana-value-conditional delayed trigger",
                        gameData.id, permanent.getCard().getName());
                permanentRemovalService.removeOrphanedAuras(gameData);
            }
        }

        // Perform the scheduled end-step zone changes: token exiles (e.g. Mimic Vat), nontoken
        // exiles (e.g. Dark Maze), sacrifices (e.g. Choreographed Sparks' creature-copy token) and
        // destructions (e.g. Stone Giant).
        permanentRemovalService.processDelayedPermanentActions(gameData,
                DelayedPermanentActionKind.EXILE_TOKEN_AT_END_STEP);
        permanentRemovalService.processDelayedPermanentActions(gameData,
                DelayedPermanentActionKind.EXILE_AT_END_STEP);
        permanentRemovalService.processDelayedPermanentActions(gameData,
                DelayedPermanentActionKind.EXILE_WARPED_AT_END_STEP);
        permanentRemovalService.processDelayedPermanentActions(gameData,
                DelayedPermanentActionKind.SACRIFICE_AT_END_STEP);
        permanentRemovalService.processDelayedPermanentActions(gameData,
                DelayedPermanentActionKind.DESTROY_AT_END_STEP);

        // Stone Idol Trap: exile permanents created by the spell at the beginning of its
        // controller's next end step, not at the next end step of any player.
        if (gameData.hasDelayedAction(ExilePermanentAtControllerEndStep.class,
                action -> action.controllerId().equals(gameData.activePlayerId))) {
            List<ExilePermanentAtControllerEndStep> pending =
                    gameData.drainDelayedActions(ExilePermanentAtControllerEndStep.class,
                            action -> action.controllerId().equals(gameData.activePlayerId));
            for (ExilePermanentAtControllerEndStep action : pending) {
                Permanent permanent = gameQueryService.findPermanentById(gameData, action.permanentId());
                if (permanent == null || !permanentRemovalService.removePermanentToExile(gameData, permanent)) {
                    continue;
                }
                gameLogService.append(gameData,
                        GameLog.builder().card(permanent.getCard()).text(" token is exiled.").build());
                log.info("Game {} - {} token is exiled at its controller's end step",
                        gameData.id, permanent.getCard().getName());
                permanentRemovalService.removeOrphanedAuras(gameData);
            }
        }

        // Process Siren's Call: destroy all non-Wall creatures the player controls that didn't attack
        // this turn, ignoring creatures they didn't control continuously since the beginning of the
        // turn (summoning sick).
        if (gameData.hasDelayedAction(DestroyNonAttackersAtEndStep.class)) {
            List<DestroyNonAttackersAtEndStep> pending =
                    gameData.drainDelayedActions(DestroyNonAttackersAtEndStep.class);
            for (DestroyNonAttackersAtEndStep action : pending) {
                List<Permanent> battlefield = gameData.playerBattlefields.get(action.playerId());
                if (battlefield == null) continue;
                // Snapshot first: tryDestroyPermanent mutates the battlefield list.
                List<Permanent> toDestroy = new ArrayList<>();
                for (Permanent perm : battlefield) {
                    if (gameQueryService.isCreature(gameData, perm)
                            && !GameQueryService.permanentHasSubtype(perm, CardSubtype.WALL)
                            && !perm.isAttackedThisTurn()
                            && !perm.isSummoningSick()) {
                        toDestroy.add(perm);
                    }
                }
                for (Permanent perm : toDestroy) {
                    if (permanentRemovalService.tryDestroyPermanent(gameData, perm)) {
                        gameLogService.append(gameData,
                                GameLog.cardThen(perm.getCard(), " is destroyed for not attacking."));
                        log.info("Game {} - {} destroyed by Siren's Call for not attacking",
                                gameData.id, perm.getCard().getName());
                    }
                }
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
                if (permanentRemovalService.tryDestroyPermanent(gameData, perm)) {
                    gameLogService.append(gameData,
                            GameLog.cardThen(perm.getCard(), " is destroyed for not attacking."));
                    log.info("Game {} - {} destroyed for not attacking",
                            gameData.id, perm.getCard().getName());
                }
            }
        }

        // Perform the scheduled end-step returns to hand (e.g. Dragon Mask)
        permanentRemovalService.processDelayedPermanentActions(gameData,
                DelayedPermanentActionKind.RETURN_TO_HAND_AT_END_STEP);

        // Process delayed "lose the game" triggers (e.g. Last Chance, Glorious End). These fire at the
        // beginning of the scheduling player's *own* next end step ("your next end step"): only entries
        // scheduled on an earlier turn (so the scheduling turn's own end step is skipped) AND only while
        // that player is the active player (so opponents' end steps are skipped). For the extra-turn
        // cards the very next turn is the controller's, so the loss lands on it; for Glorious End cast
        // on your own turn it skips the intervening opponent turn and lands on your next turn's end step.
        if (gameData.hasDelayedAction(LoseGameAtEndStep.class)) {
            List<LoseGameAtEndStep> toLose = gameData.drainDelayedActions(
                    LoseGameAtEndStep.class,
                    a -> gameData.turnNumber > a.registeredTurnNumber()
                            && a.playerId().equals(gameData.activePlayerId));
            for (LoseGameAtEndStep action : toLose) {
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
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
                UUID controllerId = gameQueryService.findPermanentController(gameData, permanentId);
                if (controllerId == null) continue;

                int triggerCount = totalCountersToAdd / 2; // each trigger adds 2 counters
                for (int i = 0; i < triggerCount; i++) {
                    PutCountersOnSourceEffect effect = new PutCountersOnSourceEffect(1, 1, 2);
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
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
                    gameData.drainDelayedActions(DelayedPlusZeroPlusOneCounters.class);
            for (DelayedPlusZeroPlusOneCounters counterEntry : pendingCounters) {
                int totalCountersToAdd = counterEntry.totalCounters();
                Permanent perm = gameQueryService.findPermanentById(gameData, counterEntry.permanentId());
                if (perm == null || totalCountersToAdd <= 0) continue;
                UUID controllerId = gameQueryService.findPermanentController(gameData, perm.getId());
                if (controllerId == null) continue;

                PutCountersOnSourceEffect effect = new PutCountersOnSourceEffect(0, 1, totalCountersToAdd);
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        controllerId,
                        perm.getCard().getName() + "'s delayed +0/+1 counter trigger",
                        new ArrayList<>(List.of(effect)),
                        null,
                        perm.getId()
                ));
                log.info("Game {} - {} delayed +0/+1 counter trigger: {} counter(s) pushed onto stack", gameData.id, perm.getCard().getName(), totalCountersToAdd);
            }
        }

        if (gameData.hasDelayedAction(RemoveCounterFromPermanentAtNextEndStep.class)) {
            List<RemoveCounterFromPermanentAtNextEndStep> pendingRemovals =
                    gameData.drainDelayedActions(RemoveCounterFromPermanentAtNextEndStep.class);
            for (RemoveCounterFromPermanentAtNextEndStep action : pendingRemovals) {
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        action.sourceCard(),
                        action.controllerId(),
                        action.sourceCard().getName() + "'s delayed ability",
                        new ArrayList<>(List.of(new RemoveCounterFromTargetPermanentEffect(
                                action.counterType(), null, 1))),
                        action.permanentId(),
                        (UUID) null);
                entry.setNonTargeting(true);
                gameData.stack.add(entry);
                gameLogService.append(gameData, GameLog.cardThen(action.sourceCard(),
                        "'s delayed ability triggers to remove a counter."));
            }
        }

        // Process delayed untap permanents triggers (e.g. Teferi, Hero of Dominaria +1)
        if (gameData.hasDelayedAction(DelayedUntapPermanents.class)) {
            List<DelayedUntapPermanents> pendingUntaps =
                    gameData.drainDelayedActions(DelayedUntapPermanents.class);
            for (DelayedUntapPermanents pending : pendingUntaps) {
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        pending.sourceCard(),
                        pending.controllerId(),
                        pending.sourceCard().getName() + "'s delayed trigger — untap up to " + pending.count() + " permanent(s)",
                        new ArrayList<>(List.of(new UntapPermanentsEffect(
                                TapUntapScope.CONTROLLED, pending.filter(), pending.count())))
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
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
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
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
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

        if (gameData.hasDelayedAction(DelayedCreateTokenCopy.class)) {
            List<DelayedCreateTokenCopy> pendingCopies =
                    gameData.drainDelayedActions(DelayedCreateTokenCopy.class);
            for (DelayedCreateTokenCopy pending : pendingCopies) {
                String sourceName = pending.sourceCard() != null
                        ? pending.sourceCard().getName() : pending.copiedCard().getName();
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        pending.copiedCard(),
                        pending.controllerId(),
                        sourceName + "'s delayed trigger — create token copy",
                        new ArrayList<>(List.of(new CreateTokenCopyOfSourceEffect()))
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
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        pending.sourceCard(),
                        pending.controllerId(),
                        pending.sourceCard().getName() + "'s delayed trigger — choose an opponent",
                        new ArrayList<>(List.of(new ChooseOpponentGainsControlOfSourceEffect())),
                        null,
                        pending.sourcePermanentId());
                entry.setNonTargeting(true);
                gameData.stack.add(entry);
                gameLogService.append(gameData,
                        GameLog.cardThen(pending.sourceCard(), "'s delayed trigger — choose an opponent."));
            }
        }

        if (gameData.hasDelayedAction(DiscardCardsAtNextEndStep.class)) {
            List<DiscardCardsAtNextEndStep> pendingDiscards =
                    gameData.drainDelayedActions(DiscardCardsAtNextEndStep.class);
            for (DiscardCardsAtNextEndStep pending : pendingDiscards) {
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        pending.sourceCard(),
                        pending.controllerId(),
                        pending.sourceCard().getName() + "'s delayed trigger — discard " + pending.count() + " cards",
                        new ArrayList<>(List.of(new DiscardEffect(pending.count(), DiscardRecipient.CONTROLLER)))
                );
                entry.setNonTargeting(true);
                gameData.stack.add(entry);
                gameLogService.append(gameData,
                        GameLog.cardThen(pending.sourceCard(), "'s delayed trigger discards cards."));
                log.info("Game {} - {} delayed discard trigger pushed onto stack",
                        gameData.id, pending.sourceCard().getName());
            }
        }

        if (gameData.hasDelayedAction(ExileCardsFromOwnGraveyardAtNextEndStep.class)) {
            List<ExileCardsFromOwnGraveyardAtNextEndStep> pendingExiles =
                    gameData.drainDelayedActions(ExileCardsFromOwnGraveyardAtNextEndStep.class);
            for (ExileCardsFromOwnGraveyardAtNextEndStep pending : pendingExiles) {
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        pending.sourceCard(),
                        pending.controllerId(),
                        pending.sourceCard().getName() + "'s delayed trigger — exile cards from graveyard",
                        new ArrayList<>(List.of(new ExileGraveyardCardsEffect(
                                pending.count(), GraveyardExileScope.OWN)))
                );
                entry.setNonTargeting(true);
                gameData.stack.add(entry);
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
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
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
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        pending.sourceCard(),
                        pending.controllerId(),
                        pending.sourceCard().getName()
                                + "'s delayed trigger — lose " + pending.lifeLoss()
                                + " life and return to hand",
                        new ArrayList<>(List.of(
                                new LoseLifeEffect(pending.lifeLoss()),
                                ReturnCardFromGraveyardEffect.builder()
                                        .destination(GraveyardChoiceDestination.HAND)
                                        .filter(new CardIsSelfPredicate())
                                        .returnAll(true)
                                        .build()
                        ))
                ));
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
                Card cardToReturn = exiledEntry.card();
                gameData.removeFromExile(pending.cardId());
                gameData.addCardToHand(pending.ownerId(), cardToReturn);
                String playerName = gameData.playerIdToName.get(pending.ownerId());
                gameLogService.append(gameData,
                        GameLog.cardThen(cardToReturn, " returns to " + playerName + "'s hand (delayed trigger)."));
                log.info("Game {} - {} returns to {}'s hand from exile (delayed end-step trigger)",
                        gameData.id, cardToReturn.getName(), playerName);
            }
        }

        // Process delayed graveyard-to-battlefield transformed returns (e.g. Loyal Cathar)
        if (gameData.hasDelayedAction(DelayedGraveyardToBattlefieldTransformedReturn.class)) {
            List<DelayedGraveyardToBattlefieldTransformedReturn> pendingReturns =
                    gameData.drainDelayedActions(DelayedGraveyardToBattlefieldTransformedReturn.class);
            for (DelayedGraveyardToBattlefieldTransformedReturn pending : pendingReturns) {
                graveyardTransformedReturnService.returnTransformed(
                        gameData, pending.cardId(), pending.ownerId(), pending.controllerId());
            }
        }

        // Process delayed graveyard-to-battlefield self returns (Sand Golem); the upkeep-scheduled
        // ones (Phytotitan) are left in the queue for handleUpkeepTriggers.
        resolveDelayedSelfReturns(gameData, pending -> !pending.atNextUpkeep());

        if (gameData.hasDelayedAction(DelayedReturnAuraAttachedToPermanent.class)) {
            List<DelayedReturnAuraAttachedToPermanent> pendingReturns =
                    gameData.drainDelayedActions(DelayedReturnAuraAttachedToPermanent.class);
            for (DelayedReturnAuraAttachedToPermanent pending : pendingReturns) {
                Permanent enchantedPermanent = gameQueryService.findPermanentById(
                        gameData, pending.enchantedPermanentId());
                if (enchantedPermanent == null || !gameQueryService.isCreature(gameData, enchantedPermanent)) {
                    continue;
                }

                Card auraCard = gameQueryService.findCardInGraveyardById(gameData, pending.auraCardId());
                if (auraCard == null) {
                    continue;
                }

                permanentRemovalService.removeCardFromGraveyardById(gameData, pending.auraCardId());
                Permanent auraPermanent = new Permanent(auraCard);
                auraPermanent.setAttachedTo(enchantedPermanent.getId());
                battlefieldEntryService.putPermanentOntoBattlefield(
                        gameData, pending.auraOwnerId(), auraPermanent);
                gameLogService.append(gameData, GameLog.builder()
                        .card(auraCard)
                        .text(" returns to the battlefield attached to ")
                        .card(enchantedPermanent.getCard())
                        .text(" (delayed trigger).")
                        .build());
            }
        }

        // Process delayed graveyard-to-battlefield-under-control returns (Seraph, Grave Betrayal, Lifeline)
        if (gameData.hasDelayedAction(DelayedGraveyardToBattlefieldUnderControl.class)) {
            List<DelayedGraveyardToBattlefieldUnderControl> pendingReturns =
                    gameData.drainDelayedActions(DelayedGraveyardToBattlefieldUnderControl.class);
            for (DelayedGraveyardToBattlefieldUnderControl pending : pendingReturns) {
                UUID ownerId = null;
                Card cardToReturn = null;
                for (UUID pid : gameData.orderedPlayerIds) {
                    List<Card> graveyard = gameData.playerGraveyards.get(pid);
                    if (graveyard == null) continue;
                    for (Card card : graveyard) {
                        if (card.getId().equals(pending.cardId())) {
                            cardToReturn = card;
                            ownerId = pid;
                            break;
                        }
                    }
                    if (cardToReturn != null) break;
                }
                if (cardToReturn == null) {
                    // No longer in a graveyard (moved/exiled/reanimated already, or it was a token) —
                    // you don't get it back.
                    log.info("Game {} - Delayed return under control for card {} skipped (no longer in a graveyard)",
                            gameData.id, pending.cardId());
                    continue;
                }
                if (gameQueryService.isCardBlockedFromEnteringFromZone(gameData, cardToReturn, com.github.laxika.magicalvibes.model.Zone.GRAVEYARD)) {
                    gameLogService.append(gameData,
                            GameLog.cardThen(cardToReturn, " can't return from the graveyard; it stays in the graveyard."));
                    continue;
                }

                Permanent sourcePermanent = pending.sourcePermanentId() == null
                        ? null : gameQueryService.findPermanentById(gameData, pending.sourcePermanentId());
                String sourceName = sourcePermanent != null ? sourcePermanent.getCard().getName() : "Delayed return";

                // Shirei: "if Shirei is still on the battlefield". The permanent id is matched, so a
                // Shirei that left and returned is a new object and the return does not happen.
                if (pending.requireSourceOnBattlefield() && sourcePermanent == null) {
                    log.info("Game {} - Delayed return under control for card {} skipped (source left the battlefield)",
                            gameData.id, pending.cardId());
                    continue;
                }

                permanentRemovalService.removeCardFromGraveyardById(gameData, cardToReturn.getId());
                Permanent permanent = new Permanent(cardToReturn);
                permanent.setEnteredFromGraveyardOwnerId(ownerId);
                // "with an additional +1/+1 counter on it" / "is a black Zombie in addition to its
                // other colors and types" (Grave Betrayal) — applied before the permanent enters.
                if (pending.counterType() != null && pending.counterAmount() > 0) {
                    permanent.setCounterCount(pending.counterType(), pending.counterAmount());
                }
                if (pending.grantColor() != null) {
                    permanent.getGrantedColors().add(pending.grantColor());
                }
                if (pending.grantSubtype() != null && !permanent.getGrantedSubtypes().contains(pending.grantSubtype())) {
                    permanent.getGrantedSubtypes().add(pending.grantSubtype());
                }
                UUID returnControllerId = pending.returnUnderOwnersControl() ? ownerId : pending.controllerId();
                battlefieldEntryService.putPermanentOntoBattlefield(gameData, returnControllerId, permanent);

                // When the returned card belongs to another player, the controller keeps it via a
                // permanent control effect (CR 613 layer 2) — without one it would revert to its owner
                // — and its ownership is recorded so it dies to its owner's graveyard.
                if (!pending.returnUnderOwnersControl() && !returnControllerId.equals(ownerId)) {
                    gameData.stolenCreatures.put(permanent.getId(), ownerId);
                    creatureControlService.applyControlEffect(gameData, returnControllerId, permanent,
                            new GainControlOfTargetEffect(ControlDuration.PERMANENT),
                            ControlDuration.PERMANENT.toEffectDuration(), null, sourceName);
                }

                String playerName = gameData.playerIdToName.get(returnControllerId);
                gameLogService.append(gameData, GameLog.cardThen(cardToReturn,
                        " returns to the battlefield under " + playerName + "'s control (" + sourceName + ")."));
                log.info("Game {} - {} returns under {}'s control ({})", gameData.id, cardToReturn.getName(), playerName, sourceName);
                battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, returnControllerId, cardToReturn, null, false);

                // Link to the Seraph for the control-loss sacrifice, but only if it is still on the
                // battlefield — if it already left, you never have to sacrifice the returned creature.
                if (!pending.returnUnderOwnersControl() && pending.sacrificeOnSourceControlLoss()
                        && sourcePermanent != null) {
                    gameData.seraphReturnedCreatures
                            .computeIfAbsent(pending.sourcePermanentId(), k -> java.util.concurrent.ConcurrentHashMap.newKeySet())
                            .add(permanent.getId());
                    gameData.seraphControlWatch.putIfAbsent(pending.sourcePermanentId(), returnControllerId);
                }
            }
        }

        UUID activePlayerId = gameData.activePlayerId;
        List<UUID> triggerOrder = new ArrayList<>();
        triggerOrder.add(activePlayerId);
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(activePlayerId)) {
                triggerOrder.add(playerId);
            }
        }

        for (UUID playerId : triggerOrder) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;

            for (Permanent perm : battlefield) {
                List<CardEffect> endStepEffects = perm.getCard().getEffects(EffectSlot.END_STEP_TRIGGERED);
                if (endStepEffects == null || endStepEffects.isEmpty()) continue;

                for (CardEffect effect : endStepEffects) {
                    if (effect instanceof ConditionalEffect conditional
                            && conditional.interveningIf()
                            && !conditionEvaluationService.isMet(gameData, conditional.condition(),
                            ConditionContext.forPermanent(perm, playerId))) {
                        log.info("Game {} - {} end-step trigger skipped ({})",
                                gameData.id, perm.getCard().getName(), conditional.conditionNotMetReason());
                        continue;
                    }
                    if (effect instanceof MayEffect may) {
                        gameData.queueMayAbility(perm.getCard(), playerId, may);
                    } else if (effect instanceof DealDamageIfDidntCastSpellThisTurnEffect) {
                        // Intervening-if (CR 603.4): only trigger if the end-step player (the active
                        // player) didn't cast a spell this turn. Bake that player into targetId so the
                        // damage is dealt to them; re-checked at resolution. Impatience.
                        if (gameData.getSpellsCastThisTurnCount(activePlayerId) > 0) {
                            log.info("Game {} - {} end-step trigger skipped (active player cast a spell this turn)",
                                    gameData.id, perm.getCard().getName());
                            continue;
                        }
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                playerId,
                                perm.getCard().getName() + "'s end step ability",
                                new ArrayList<>(List.of(effect)),
                                activePlayerId,
                                perm.getId()
                        ));
                        gameLogService.append(gameData,
                                GameLog.cardThen(perm.getCard(), "'s end step ability triggers."));
                        log.info("Game {} - {} end-step didn't-cast-spell trigger pushed onto stack", gameData.id, perm.getCard().getName());
                    } else if (effect instanceof DealDamageToEndStepPlayerIfLifeAtMostEffect lifeDamage) {
                        // Intervening-if (CR 603.4): only trigger if the end-step player (the active
                        // player) has N or less life. Bake that player into targetId so the damage is
                        // dealt to them; re-checked at resolution. Razor Pendulum.
                        if (gameData.playerLifeTotals.getOrDefault(activePlayerId, 20) > lifeDamage.lifeThreshold()) {
                            log.info("Game {} - {} end-step trigger skipped (active player above {} life)",
                                    gameData.id, perm.getCard().getName(), lifeDamage.lifeThreshold());
                            continue;
                        }
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                playerId,
                                perm.getCard().getName() + "'s end step ability",
                                new ArrayList<>(List.of(effect)),
                                activePlayerId,
                                perm.getId()
                        ));
                        gameLogService.append(gameData,
                                GameLog.cardThen(perm.getCard(), "'s end step ability triggers."));
                        log.info("Game {} - {} end-step low-life trigger pushed onto stack", gameData.id, perm.getCard().getName());
                    } else if (effect instanceof SacrificeSelfAndReturnCardsExiledWithSourceEffect sacReturn) {
                        // Intervening-if: only trigger if N or more cards have been exiled with the
                        // source permanent (CR 603.4). Re-checked again at resolution.
                        if (gameData.getCardsExiledByPermanent(perm.getId()).size() < sacReturn.minCount()) {
                            log.info("Game {} - {} end-step trigger skipped (fewer than {} cards exiled)",
                                    gameData.id, perm.getCard().getName(), sacReturn.minCount());
                            continue;
                        }
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                playerId,
                                perm.getCard().getName() + "'s end step ability",
                                new ArrayList<>(List.of(effect)),
                                null,
                                perm.getId()
                        ));
                        gameLogService.append(gameData,
                                GameLog.cardThen(perm.getCard(), "'s end step ability triggers."));
                        log.info("Game {} - {} end-step sacrifice-and-return trigger pushed onto stack", gameData.id, perm.getCard().getName());
                    } else if (effect instanceof ConditionalEffect conditional
                            && conditional.condition() instanceof NotKicked) {
                        // Intervening-if: only trigger if the permanent was NOT kicked (CR 603.4)
                        if (!conditionEvaluationService.isMet(gameData, conditional.condition(),
                                ConditionContext.forPermanent(perm, playerId))) {
                            log.info("Game {} - {} end-step trigger skipped (was kicked)",
                                    gameData.id, perm.getCard().getName());
                            continue;
                        }
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                playerId,
                                perm.getCard().getName() + "'s end step ability",
                                new ArrayList<>(List.of(effect)),
                                null,
                                perm.getId()
                        ));
                        gameLogService.append(gameData,
                                GameLog.cardThen(perm.getCard(), "'s end step ability triggers."));
                        log.info("Game {} - {} end-step not-kicked trigger pushed onto stack", gameData.id, perm.getCard().getName());
                    } else if (effect instanceof ConditionalEffect conditional
                            && conditional.condition() instanceof SourceRegeneratedThisTurn) {
                        // Intervening-if (CR 603.4): only trigger if the source regenerated this turn —
                        // Spiny Starfish. Re-checked at resolution by the conditional effect handler.
                        if (!conditionEvaluationService.isMet(gameData, conditional.condition(),
                                ConditionContext.forPermanent(perm, playerId))) {
                            log.info("Game {} - {} end-step trigger skipped (did not regenerate this turn)",
                                    gameData.id, perm.getCard().getName());
                            continue;
                        }
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                playerId,
                                perm.getCard().getName() + "'s end step ability",
                                new ArrayList<>(List.of(effect)),
                                null,
                                perm.getId()
                        ));
                        gameLogService.append(gameData,
                                GameLog.cardThen(perm.getCard(), "'s end step ability triggers."));
                        log.info("Game {} - {} end-step regenerated-this-turn trigger pushed onto stack",
                                gameData.id, perm.getCard().getName());
                    } else if (effect instanceof ConditionalEffect conditional
                            && conditional.condition() instanceof NoOtherPermanent) {
                        // Intervening-if: only trigger if controller has no other matching permanents (CR 603.4)
                        if (!conditionEvaluationService.isMet(gameData, conditional.condition(),
                                ConditionContext.forPermanent(perm, playerId))) {
                            log.info("Game {} - {} end-step trigger skipped (intervening-if failed: matching permanent present)",
                                    gameData.id, perm.getCard().getName());
                            continue;
                        }
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                playerId,
                                perm.getCard().getName() + "'s end step ability",
                                new ArrayList<>(List.of(effect)),
                                null,
                                perm.getId()
                        ));
                        gameLogService.append(gameData,
                                GameLog.cardThen(perm.getCard(), "'s end step ability triggers."));
                        log.info("Game {} - {} end-step no-other-permanent trigger pushed onto stack", gameData.id, perm.getCard().getName());
                    } else if (effect instanceof ConditionalEffect conditional
                            && conditional.condition() instanceof AnyPlayerControlsPermanentCountAtMost) {
                        // Intervening-if: only trigger if at most N matching permanents exist across all
                        // battlefields (CR 603.4) — e.g. Pestilence sacrifices itself when no creatures remain.
                        if (!conditionEvaluationService.isMet(gameData, conditional.condition(),
                                ConditionContext.forPermanent(perm, playerId))) {
                            log.info("Game {} - {} end-step trigger skipped (intervening-if failed: matching permanents present)",
                                    gameData.id, perm.getCard().getName());
                            continue;
                        }
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                playerId,
                                perm.getCard().getName() + "'s end step ability",
                                new ArrayList<>(List.of(effect)),
                                null,
                                perm.getId()
                        ));
                        gameLogService.append(gameData,
                                GameLog.cardThen(perm.getCard(), "'s end step ability triggers."));
                        log.info("Game {} - {} end-step count-at-most trigger pushed onto stack", gameData.id, perm.getCard().getName());
                    } else if (effect instanceof ConditionalEffect conditional
                            && conditional.condition() instanceof SelfDealtDamageToOpponentThisTurn) {
                        // Intervening-if: only trigger if this permanent dealt damage to an opponent
                        // this turn (CR 603.4) — Whirling Dervish. Re-checked at resolution.
                        if (!conditionEvaluationService.isMet(gameData, conditional.condition(),
                                ConditionContext.forPermanent(perm, playerId))) {
                            log.info("Game {} - {} end-step trigger skipped (dealt no damage to an opponent this turn)",
                                    gameData.id, perm.getCard().getName());
                            continue;
                        }
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                playerId,
                                perm.getCard().getName() + "'s end step ability",
                                new ArrayList<>(List.of(effect)),
                                null,
                                perm.getId()
                        ));
                        gameLogService.append(gameData,
                                GameLog.cardThen(perm.getCard(), "'s end step ability triggers."));
                        log.info("Game {} - {} end-step dealt-damage-to-opponent trigger pushed onto stack", gameData.id, perm.getCard().getName());
                    } else if (effect instanceof ConditionalEffect conditional
                            && conditional.condition() instanceof NotControllerTurn) {
                        // Intervening-if: only trigger if it's not the controller's turn (CR 603.4) —
                        // Discordant Spirit. Re-checked at resolution.
                        if (!conditionEvaluationService.isMet(gameData, conditional.condition(),
                                ConditionContext.forPermanent(perm, playerId))) {
                            log.info("Game {} - {} end-step trigger skipped (it's the controller's turn)",
                                    gameData.id, perm.getCard().getName());
                            continue;
                        }
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                playerId,
                                perm.getCard().getName() + "'s end step ability",
                                new ArrayList<>(List.of(effect)),
                                null,
                                perm.getId()
                        ));
                        gameLogService.append(gameData,
                                GameLog.cardThen(perm.getCard(), "'s end step ability triggers."));
                        log.info("Game {} - {} end-step opponent-turn trigger pushed onto stack",
                                gameData.id, perm.getCard().getName());
                    } else if (effect instanceof ConditionalEffect conditional
                            && conditional.condition() instanceof SelfWasDealtDamageThisTurn) {
                        // Intervening-if: only trigger if this permanent was dealt damage this turn
                        // (CR 603.4) — Wall of Resistance. Re-checked at resolution.
                        if (!conditionEvaluationService.isMet(gameData, conditional.condition(),
                                ConditionContext.forPermanent(perm, playerId))) {
                            log.info("Game {} - {} end-step trigger skipped (wasn't dealt damage this turn)",
                                    gameData.id, perm.getCard().getName());
                            continue;
                        }
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                playerId,
                                perm.getCard().getName() + "'s end step ability",
                                new ArrayList<>(List.of(effect)),
                                null,
                                perm.getId()
                        ));
                        gameLogService.append(gameData,
                                GameLog.cardThen(perm.getCard(), "'s end step ability triggers."));
                        log.info("Game {} - {} end-step was-dealt-damage trigger pushed onto stack", gameData.id, perm.getCard().getName());
                    } else if (effect instanceof ConditionalEffect conditional
                            && conditional.condition() instanceof SourceDamagedCreatureDiedThisTurn) {
                        // Intervening-if: a creature this permanent damaged this turn died (CR 603.4) —
                        // Krovikan Vampire. Re-checked at resolution; returns still-in-graveyard cards.
                        if (!conditionEvaluationService.isMet(gameData, conditional.condition(),
                                ConditionContext.forPermanent(perm, playerId))) {
                            log.info("Game {} - {} end-step trigger skipped (no damaged creature died this turn)",
                                    gameData.id, perm.getCard().getName());
                            continue;
                        }
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                playerId,
                                perm.getCard().getName() + "'s end step ability",
                                new ArrayList<>(List.of(effect)),
                                null,
                                perm.getId()
                        ));
                        gameLogService.append(gameData,
                                GameLog.cardThen(perm.getCard(), "'s end step ability triggers."));
                        log.info("Game {} - {} end-step damaged-creature-died trigger pushed onto stack",
                                gameData.id, perm.getCard().getName());
                    } else if (effect instanceof ConditionalEffect morbid
                            && morbid.condition() instanceof Morbid) {
                        // Intervening-if: only trigger if morbid condition is met (CR 603.4)
                        if (!conditionEvaluationService.isMet(gameData, morbid.condition(),
                                ConditionContext.forPermanent(perm, playerId))) {
                            log.info("Game {} - {} end-step morbid trigger skipped (no creature died this turn)",
                                    gameData.id, perm.getCard().getName());
                            continue;
                        }
                        CardEffect wrapped = morbid.wrapped();
                        if (wrapped.targetSpec().admits(TargetPredicate.Kind.PERMANENT) || wrapped.targetSpec().admits(TargetPredicate.Kind.PLAYER)) {
                            // Targeting triggered ability — queue for target selection
                            gameData.queueInteraction(new PermanentChoiceContext.EndStepTriggerTarget(
                                    perm.getCard(), playerId, new ArrayList<>(List.of(effect)), perm.getId()));
                        } else {
                            gameData.stack.add(new StackEntry(
                                    StackEntryType.TRIGGERED_ABILITY,
                                    perm.getCard(),
                                    playerId,
                                    perm.getCard().getName() + "'s end step ability",
                                    new ArrayList<>(List.of(effect)),
                                    null,
                                    perm.getId()
                            ));
                            gameLogService.append(gameData,
                                    GameLog.cardThen(perm.getCard(), "'s end step ability triggers."));
                            log.info("Game {} - {} end-step morbid trigger pushed onto stack", gameData.id, perm.getCard().getName());
                        }
                    } else if (effect instanceof ConditionalEffect conditional
                            && conditional.wrapped() instanceof MayEffect) {
                        // Intervening-if "you may" (CR 603.4), e.g. Sygg, River Cutthroat: only
                        // trigger if the condition holds at the beginning of the end step. The
                        // ConditionalEffect wrapper is pushed intact so resolution re-checks the
                        // condition (CR 603.4) and then prompts the optional "you may".
                        if (!conditionEvaluationService.isMet(gameData, conditional.condition(),
                                ConditionContext.forPermanent(perm, playerId))) {
                            log.info("Game {} - {} end-step trigger skipped ({})",
                                    gameData.id, perm.getCard().getName(), conditional.conditionNotMetReason());
                            continue;
                        }
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                playerId,
                                perm.getCard().getName() + "'s end step ability",
                                new ArrayList<>(List.of(effect)),
                                null,
                                perm.getId()
                        ));
                        gameLogService.append(gameData,
                                GameLog.cardThen(perm.getCard(), "'s end step ability triggers."));
                        log.info("Game {} - {} end-step conditional-may trigger pushed onto stack", gameData.id, perm.getCard().getName());
                    } else if (effect instanceof ConditionalEffect conditional
                            && conditional.condition() instanceof GainedLifeThisTurn gainedLife) {
                        // Intervening-if (CR 603.4): "at the beginning of each end step, if you gained
                        // [N or more] life this turn" — the controller of the permanent is "you", not the
                        // active player. The wrapper is pushed intact so resolution re-checks it.
                        if (gameData.getLifeGainedThisTurn(playerId) < gainedLife.minimumAmount()) {
                            log.info("Game {} - {} end-step trigger skipped ({})",
                                    gameData.id, perm.getCard().getName(), conditional.conditionNotMetReason());
                            continue;
                        }
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                playerId,
                                perm.getCard().getName() + "'s end step ability",
                                new ArrayList<>(List.of(effect)),
                                null,
                                perm.getId()
                        ));
                        gameLogService.append(gameData,
                                GameLog.cardThen(perm.getCard(), "'s end step ability triggers."));
                        log.info("Game {} - {} end-step life-gain trigger pushed onto stack", gameData.id, perm.getCard().getName());
                    } else if (effect instanceof ConditionalEffect conditional
                            && conditional.condition() instanceof ControlsPermanentCount) {
                        // Intervening-if (CR 603.4): "at the beginning of the end step, if you control
                        // N or more […]" — Biovisionary. "You" is the permanent's controller, not the
                        // active player. The wrapper is pushed intact so resolution re-checks it.
                        if (!conditionEvaluationService.isMet(gameData, conditional.condition(),
                                ConditionContext.forPermanent(perm, playerId))) {
                            log.info("Game {} - {} end-step trigger skipped ({})",
                                    gameData.id, perm.getCard().getName(), conditional.conditionNotMetReason());
                            continue;
                        }
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                playerId,
                                perm.getCard().getName() + "'s end step ability",
                                new ArrayList<>(List.of(effect)),
                                null,
                                perm.getId()
                        ));
                        gameLogService.append(gameData,
                                GameLog.cardThen(perm.getCard(), "'s end step ability triggers."));
                        log.info("Game {} - {} end-step permanent-count trigger pushed onto stack",
                                gameData.id, perm.getCard().getName());
                    } else {
                        // EndStepPlayerTargetedEffect ("... that player ...") reads the end-step
                        // player off targetId; every other end-step effect gets a null target id.
                        boolean targetsEndStepPlayer = effect instanceof EndStepPlayerTargetedEffect
                                || effect instanceof ConditionalEffect conditional
                                && conditional.wrapped() instanceof EndStepPlayerTargetedEffect;
                        UUID endStepTargetId = targetsEndStepPlayer ? activePlayerId : null;
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                playerId,
                                perm.getCard().getName() + "'s end step ability",
                                new ArrayList<>(List.of(effect)),
                                endStepTargetId,
                                perm.getId()
                        ));

                        gameLogService.append(gameData,
                                GameLog.cardThen(perm.getCard(), "'s end step ability triggers."));
                        log.info("Game {} - {} end-step trigger pushed onto stack", gameData.id, perm.getCard().getName());
                    }
                }
            }
        }

        // GRAVEYARD_END_STEP_TRIGGERED: "At the beginning of the end step, if this card is in your
        // graveyard …" — fires at every end step, from any player's graveyard, in APNAP order.
        for (UUID playerId : triggerOrder) {
            List<Card> playerGraveyard = gameData.playerGraveyards.get(playerId);
            if (playerGraveyard == null) continue;

            for (Card card : new ArrayList<>(playerGraveyard)) {
                List<CardEffect> graveyardEndStepEffects = new ArrayList<>(
                        gameQueryService.getEffectiveGraveyardEffects(
                                gameData, card, EffectSlot.GRAVEYARD_END_STEP_TRIGGERED));
                if (playerId.equals(activePlayerId)) {
                    graveyardEndStepEffects.addAll(
                            gameQueryService.getEffectiveGraveyardEffects(
                                    gameData, card, EffectSlot.GRAVEYARD_CONTROLLER_END_STEP_TRIGGERED));
                }
                if (graveyardEndStepEffects == null || graveyardEndStepEffects.isEmpty()) continue;

                for (CardEffect effect : graveyardEndStepEffects) {
                    CardEffect innerEffect = effect;

                    // Intervening-if (CR 603.4): the graveyard-position gate is checked at trigger
                    // time before the ability is offered (Krovikan Horror's "with a creature card
                    // directly above it").
                    if (innerEffect instanceof ConditionalEffect conditional) {
                        if (!conditionEvaluationService.isMet(gameData, conditional.condition(),
                                ConditionContext.forCard(card, playerId))) {
                            log.info("Game {} - {} graveyard end-step ability skipped ({})",
                                    gameData.id, card.getName(), conditional.condition().conditionNotMetReason());
                            continue;
                        }
                    }

                    if (innerEffect instanceof MayEffect may) {
                        gameData.queueMayAbility(card, playerId, may);
                    } else {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                card,
                                playerId,
                                card.getName() + "'s end step ability",
                                new ArrayList<>(List.of(innerEffect))
                        ));

                        gameLogService.append(gameData, GameLog.cardThen(card, "'s end step ability triggers."));
                        log.info("Game {} - {} graveyard end-step trigger pushed onto stack", gameData.id, card.getName());
                    }
                }
            }
        }

        // CONTROLLER_END_STEP_TRIGGERED: only fires for the active player's permanents
        List<Permanent> activeBattlefield = gameData.playerBattlefields.get(activePlayerId);
        if (activeBattlefield != null) {
            for (Permanent perm : activeBattlefield) {
                List<CardEffect> controllerEndStepEffects = new ArrayList<>(
                        perm.getCard().getEffects(EffectSlot.CONTROLLER_END_STEP_TRIGGERED));
                controllerEndStepEffects.addAll(grantedTriggeredAbilitySupport.grantedTriggeredEffects(
                        gameData, perm, EffectSlot.CONTROLLER_END_STEP_TRIGGERED));
                if (controllerEndStepEffects == null || controllerEndStepEffects.isEmpty()) continue;

                for (CardEffect effect : controllerEndStepEffects) {
                    // CR 603.4: an intervening-"if" ability does not trigger at all when its
                    // condition is false as the trigger event occurs. Checked once here for every
                    // condition, so the branches below only have to decide how to put the trigger
                    // on the stack. "Unless" clauses opt out and always trigger.
                    if (effect instanceof ConditionalEffect conditional
                            && conditional.interveningIf()
                            && !conditionEvaluationService.isMet(gameData, conditional.condition(),
                            ConditionContext.forPermanent(perm, activePlayerId))) {
                        log.info("Game {} - {} controller end-step trigger skipped ({})",
                                gameData.id, perm.getCard().getName(), conditional.conditionNotMetReason());
                        continue;
                    }
                    if (effect instanceof ChooseOneEffect chooseOneEffect) {
                        gameData.queueInteraction(new PermanentChoiceContext.TriggeredModalTrigger(
                                perm.getCard(), activePlayerId, chooseOneEffect, perm.getId()));
                        gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                        log.info("Game {} - {} controller end-step trigger queued for mode selection",
                                gameData.id, perm.getCard().getName());
                    } else if (effect instanceof ConditionalEffect raidEffect
                            && raidEffect.condition() instanceof Raid) {
                        CardEffect wrapped = raidEffect.wrapped();
                        if (wrapped instanceof MayEffect may) {
                            gameData.queueMayAbility(perm.getCard(), activePlayerId, may);
                        } else if (wrapped.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
                            gameData.queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                                    perm.getCard(), activePlayerId, new ArrayList<>(List.of(wrapped))));
                            gameLogService.append(gameData,
                                    GameLog.cardThen(perm.getCard(), "'s end step ability triggers."));
                            log.info("Game {} - {} controller end-step graveyard-target raid trigger queued",
                                    gameData.id, perm.getCard().getName());
                        } else if (wrapped.targetSpec().admits(TargetPredicate.Kind.PERMANENT) || wrapped.targetSpec().admits(TargetPredicate.Kind.PLAYER)) {
                            // Raid condition met, targeting required — queue for target selection
                            gameData.queueInteraction(new PermanentChoiceContext.EndStepTriggerTarget(
                                    perm.getCard(), activePlayerId, new ArrayList<>(List.of(wrapped)), perm.getId()));
                        } else {
                            gameData.stack.add(new StackEntry(
                                    StackEntryType.TRIGGERED_ABILITY,
                                    perm.getCard(),
                                    activePlayerId,
                                    perm.getCard().getName() + "'s end step ability",
                                    new ArrayList<>(List.of(wrapped)),
                                    null,
                                    perm.getId()
                            ));

                            gameLogService.append(gameData,
                                    GameLog.cardThen(perm.getCard(), "'s end step ability triggers."));
                            log.info("Game {} - {} controller end-step raid trigger pushed onto stack", gameData.id, perm.getCard().getName());
                        }
                    } else if (effect instanceof MayEffect may) {
                        if (may.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                                || may.targetSpec().admits(TargetPredicate.Kind.PLAYER)) {
                            // Targeted "you may" end-step trigger (Goblin Razerunners, Wall of Reverence,
                            // Conjurer's Closet). Targets are chosen as the trigger is put onto the stack and
                            // the trigger is skipped entirely when no legal target exists (CR 603.3d); the
                            // "you may" is honoured later, at resolution. Candidates come from the card-level
                            // TargetFilter when present, otherwise from the wrapped effect's targetSpec.
                            gameData.queueInteraction(new PermanentChoiceContext.EndStepTriggerTarget(
                                    perm.getCard(), activePlayerId, new ArrayList<>(List.of(may)), perm.getId()));
                            String logEntry = perm.getCard().getName() + "'s end step ability triggers.";
                            gameLogService.append(gameData, GameLog.text(logEntry));
                            log.info("Game {} - {} controller end-step targeting may-trigger queued", gameData.id, perm.getCard().getName());
                        } else {
                            // Source permanent context is required by self-affecting may-effects
                            // (Obzedat, Ghost Council's "you may exile Obzedat").
                            gameData.queueMayAbility(perm.getCard(), activePlayerId, may, null, perm.getId());
                        }
                    } else if (effect instanceof DestroyRandomOpponentPermanentWithCounterEffect destroyRandom) {
                        // Intervening-if: only trigger if enough opponent permanents have the counter
                        int count = 0;
                        for (UUID pid : gameData.orderedPlayerIds) {
                            if (pid.equals(activePlayerId)) continue;
                            List<Permanent> opponentBf = gameData.playerBattlefields.get(pid);
                            if (opponentBf == null) continue;
                            for (Permanent p : opponentBf) {
                                int counterCount = switch (destroyRandom.counterType()) {
                                    case AIM -> p.getCounterCount(CounterType.AIM);
                                    case CHARGE -> p.getCounterCount(CounterType.CHARGE);
                                    default -> 0;
                                };
                                if (counterCount > 0) count++;
                            }
                        }
                        if (count < destroyRandom.minRequired()) {
                            log.info("Game {} - {} end-step trigger skipped (only {} permanents with {} counters, need {})",
                                    gameData.id, perm.getCard().getName(), count,
                                    destroyRandom.counterType().name().toLowerCase(), destroyRandom.minRequired());
                            continue;
                        }
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                activePlayerId,
                                perm.getCard().getName() + "'s end step ability",
                                new ArrayList<>(List.of(effect)),
                                null,
                                perm.getId()
                        ));

                        gameLogService.append(gameData,
                                GameLog.cardThen(perm.getCard(), "'s end step ability triggers."));
                        log.info("Game {} - {} controller end-step trigger pushed onto stack", gameData.id, perm.getCard().getName());
                    } else if (effect instanceof ConditionalEffect conditional
                            && conditional.condition() instanceof ControlsPermanentCount) {
                        CardEffect countWrapped = conditional.wrapped();
                        if (countWrapped.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
                            // A conditional end-step ability can target a graveyard card. Keep the
                            // conditional wrapper so it is checked again when the ability resolves.
                            gameData.queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                                    perm.getCard(), activePlayerId, new ArrayList<>(List.of(effect))));
                            gameLogService.append(gameData,
                                    GameLog.cardThen(perm.getCard(), "'s end step ability triggers."));
                            log.info("Game {} - {} controller end-step graveyard-target count trigger queued",
                                    gameData.id, perm.getCard().getName());
                        } else if (countWrapped.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                                || countWrapped.targetSpec().admits(TargetPredicate.Kind.PLAYER)) {
                            // Condition met and the inner effect targets (e.g. Exuberant Firestoker's
                            // "deal 2 damage to target player or planeswalker") — queue for target selection.
                            gameData.queueInteraction(new PermanentChoiceContext.EndStepTriggerTarget(
                                    perm.getCard(), activePlayerId, new ArrayList<>(List.of(countWrapped)), perm.getId()));
                            gameLogService.append(gameData,
                                    GameLog.cardThen(perm.getCard(), "'s end step ability triggers."));
                            log.info("Game {} - {} controller end-step targeting trigger queued", gameData.id, perm.getCard().getName());
                        } else {
                            gameData.stack.add(new StackEntry(
                                    StackEntryType.TRIGGERED_ABILITY,
                                    perm.getCard(),
                                    activePlayerId,
                                    perm.getCard().getName() + "'s end step ability",
                                    new ArrayList<>(List.of(effect)),
                                    null,
                                    perm.getId()
                            ));

                            gameLogService.append(gameData,
                                    GameLog.cardThen(perm.getCard(), "'s end step ability triggers."));
                            log.info("Game {} - {} controller end-step trigger pushed onto stack", gameData.id, perm.getCard().getName());
                        }
                    } else if (effect instanceof ConditionalEffect conditional
                            && conditional.condition() instanceof DidntActivateLoyaltyAbilityThisTurn) {
                        // The Chain Veil. The condition is re-checked at resolution, so the whole
                        // ConditionalEffect goes on the stack.
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                activePlayerId,
                                perm.getCard().getName() + "'s end step ability",
                                new ArrayList<>(List.of(effect)),
                                null,
                                perm.getId()
                        ));

                        gameLogService.append(gameData,
                                GameLog.cardThen(perm.getCard(), "'s end step ability triggers."));
                        log.info("Game {} - {} controller end-step trigger pushed onto stack", gameData.id, perm.getCard().getName());
                    } else if (effect instanceof ConditionalEffect conditional
                            && conditional.condition() instanceof DidntAttack) {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                activePlayerId,
                                perm.getCard().getName() + "'s end step ability",
                                new ArrayList<>(List.of(effect)),
                                null,
                                perm.getId()
                        ));

                        gameLogService.append(gameData,
                                GameLog.cardThen(perm.getCard(), "'s end step ability triggers."));
                        log.info("Game {} - {} controller end-step trigger pushed onto stack", gameData.id, perm.getCard().getName());
                    } else if (effect instanceof ConditionalEffect conditional
                            && conditional.condition() instanceof AllOf) {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                activePlayerId,
                                perm.getCard().getName() + "'s end step ability",
                                new ArrayList<>(List.of(effect)),
                                null,
                                perm.getId()
                        ));

                        gameLogService.append(gameData,
                                GameLog.cardThen(perm.getCard(), "'s end step ability triggers."));
                        log.info("Game {} - {} controller end-step trigger pushed onto stack", gameData.id, perm.getCard().getName());
                    } else if (effect instanceof GainControlIfSubtypesDealtCombatDamageEffect subtypeEffect) {
                        // Intervening-if: check if any opponent was dealt combat damage by enough
                        // creatures of the required subtype this turn
                        boolean conditionMet = false;
                        for (UUID opponentId : gameData.orderedPlayerIds) {
                            if (opponentId.equals(activePlayerId)) continue;
                            int count = 0;
                            for (var dmgEntry : gameData.combatDamageToPlayersThisTurn.entrySet()) {
                                UUID permId = dmgEntry.getKey();
                                if (!dmgEntry.getValue().contains(opponentId)) continue;
                                Set<CardSubtype> subtypes = gameData.combatDamageSourceSubtypesThisTurn
                                        .getOrDefault(permId, Set.of());
                                if (subtypes.contains(subtypeEffect.subtype())
                                        || gameData.combatDamageSourcesWithChangelingThisTurn.contains(permId)) {
                                    count++;
                                }
                            }
                            if (count >= subtypeEffect.threshold()) {
                                conditionMet = true;
                                break;
                            }
                        }
                        if (!conditionMet) {
                            log.info("Game {} - {} end-step trigger skipped (no opponent dealt combat damage by {} or more {}s)",
                                    gameData.id, perm.getCard().getName(), subtypeEffect.threshold(),
                                    subtypeEffect.subtype().getDisplayName());
                            continue;
                        }
                        // Condition met — queue for targeting with GainControlOfTargetEffect.
                        // The card's targetFilter restricts to nonland opponent permanents.
                        gameData.queueInteraction(new PermanentChoiceContext.EndStepTriggerTarget(
                                perm.getCard(), activePlayerId,
                                new ArrayList<>(List.of(new GainControlOfTargetEffect(ControlDuration.PERMANENT))),
                                perm.getId()));
                    } else if (effect instanceof ConditionalEffect conditional
                            && conditional.condition() instanceof GainedLifeThisTurn) {
                        CardEffect wrapped = conditional.wrapped();
                        if (wrapped.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
                            // Graveyard-targeting trigger (e.g. Moseo) — queue for graveyard target selection
                            gameData.queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                                    perm.getCard(), activePlayerId, new ArrayList<>(List.of(wrapped))));
                            gameLogService.append(gameData,
                                    GameLog.cardThen(perm.getCard(), "'s end step ability triggers."));
                            log.info("Game {} - {} controller end-step graveyard-target trigger queued", gameData.id, perm.getCard().getName());
                        } else if (wrapped.targetSpec().admits(TargetPredicate.Kind.PERMANENT) || wrapped.targetSpec().admits(TargetPredicate.Kind.PLAYER)) {
                            gameData.queueInteraction(new PermanentChoiceContext.EndStepTriggerTarget(
                                    perm.getCard(), activePlayerId, new ArrayList<>(List.of(wrapped)), perm.getId()));
                        } else {
                            gameData.stack.add(new StackEntry(
                                    StackEntryType.TRIGGERED_ABILITY,
                                    perm.getCard(),
                                    activePlayerId,
                                    perm.getCard().getName() + "'s end step ability",
                                    new ArrayList<>(List.of(wrapped)),
                                    null,
                                    perm.getId()
                            ));
                            gameLogService.append(gameData,
                                    GameLog.cardThen(perm.getCard(), "'s end step ability triggers."));
                            log.info("Game {} - {} controller end-step trigger pushed onto stack", gameData.id, perm.getCard().getName());
                        }
                    } else if (effect instanceof ConditionalEffect conditional
                            && (conditional.condition() instanceof CreatureDiedUnderOpponentControlThisTurn
                                || conditional.condition() instanceof CreatureDiedUnderYourControlThisTurn
                                || conditional.condition() instanceof CardsLeftGraveyardThisTurn)) {
                        CardEffect wrapped = conditional.wrapped();
                        if (wrapped.targetSpec().admits(TargetPredicate.Kind.PERMANENT) || wrapped.targetSpec().admits(TargetPredicate.Kind.PLAYER)) {
                            gameData.queueInteraction(new PermanentChoiceContext.EndStepTriggerTarget(
                                    perm.getCard(), activePlayerId, new ArrayList<>(List.of(wrapped)), perm.getId()));
                        } else {
                            gameData.stack.add(new StackEntry(
                                    StackEntryType.TRIGGERED_ABILITY,
                                    perm.getCard(),
                                    activePlayerId,
                                    perm.getCard().getName() + "'s end step ability",
                                    new ArrayList<>(List.of(wrapped)),
                                    null,
                                    perm.getId()
                            ));
                            gameLogService.append(gameData,
                                    GameLog.cardThen(perm.getCard(), "'s end step ability triggers."));
                            log.info("Game {} - {} controller end-step trigger pushed onto stack", gameData.id, perm.getCard().getName());
                        }
                    } else if (effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
                        gameData.queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                                perm.getCard(), activePlayerId, new ArrayList<>(List.of(effect))));
                        gameLogService.append(gameData,
                                GameLog.cardThen(perm.getCard(), "'s end step ability triggers."));
                        log.info("Game {} - {} controller end-step graveyard-target trigger queued",
                                gameData.id, perm.getCard().getName());
                    } else if (effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT) || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)) {
                        if (perm.getCard().getSpellTargets().size() > 1
                                || etbTokenTargetService.needsSlotBySlotTargetSelection(perm.getCard())) {
                            gameData.queueInteraction(new PermanentChoiceContext.ETBTokenMultiTargetTrigger(
                                    perm.getCard(), activePlayerId, new ArrayList<>(List.of(effect)), perm.getId(),
                                    List.of(), 0, 0));
                        } else {
                            // Targeting triggered ability — queue for target selection
                            gameData.queueInteraction(new PermanentChoiceContext.EndStepTriggerTarget(
                                    perm.getCard(), activePlayerId, new ArrayList<>(List.of(effect)), perm.getId()));
                        }
                    } else {
                        StackEntry entry = new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                activePlayerId,
                                perm.getCard().getName() + "'s end step ability",
                                new ArrayList<>(List.of(effect)),
                                null,
                                perm.getId()
                        );
                        if (effect instanceof DamageDealingEffect) {
                            entry.setSourcePermanentSnapshot(new Permanent(perm));
                            if (perm.isAttached()) {
                                UUID enchantedPermanentControllerId =
                                        gameQueryService.findPermanentController(gameData, perm.getAttachedTo());
                                entry.setTargetId(enchantedPermanentControllerId);
                                entry.setNonTargeting(true);
                                Permanent attached = gameQueryService.findPermanentById(gameData, perm.getAttachedTo());
                                if (attached != null) {
                                    entry.setAttachedPermanentSnapshot(new Permanent(attached));
                                }
                            }
                        }
                        gameData.stack.add(entry);

                        gameLogService.append(gameData,
                                GameLog.cardThen(perm.getCard(), "'s end step ability triggers."));
                        log.info("Game {} - {} controller end-step trigger pushed onto stack", gameData.id, perm.getCard().getName());
                    }
                }
            }
        }

        // OPPONENT_END_STEP_TRIGGERED: "At the beginning of each opponent's end step, ..." Fires only
        // during the end step of a player who is an opponent of the permanent's controller (never the
        // controller's own end step). The end-step player is baked into the stack entry's targetId so
        // an intervening-if ConditionalEffect can gate on "that player" — checked here (CR 603.4) and
        // re-checked at resolution. Predatory Advantage.
        gameData.forEachBattlefield((playerId, playerBattlefield) -> {
            if (playerId.equals(activePlayerId)) return; // the controller's own end step is not an opponent's

            for (Permanent perm : playerBattlefield) {
                List<CardEffect> opponentEndStepEffects = perm.getCard().getEffects(EffectSlot.OPPONENT_END_STEP_TRIGGERED);
                if (opponentEndStepEffects == null || opponentEndStepEffects.isEmpty()) continue;

                for (CardEffect effect : opponentEndStepEffects) {
                    if (effect instanceof ConditionalEffect conditional
                            && !conditionEvaluationService.isMet(gameData, conditional.condition(),
                                    ConditionContext.forPermanent(perm, playerId).withTargetId(activePlayerId))) {
                        log.info("Game {} - {} opponent end-step trigger skipped ({})",
                                gameData.id, perm.getCard().getName(), conditional.conditionNotMetReason());
                        continue;
                    }

                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            perm.getCard(),
                            playerId,
                            perm.getCard().getName() + "'s end step ability",
                            new ArrayList<>(List.of(effect)),
                            activePlayerId,
                            perm.getId()
                    ));

                    gameLogService.append(gameData,
                            GameLog.cardThen(perm.getCard(), "'s end step ability triggers."));
                    log.info("Game {} - {} opponent end-step trigger pushed onto stack", gameData.id, perm.getCard().getName());
                }
            }
        });

        // Check all battlefields for auras with ENCHANTED_PERMANENT_CONTROLLER_END_STEP_TRIGGERED
        // effects. These fire during the enchanted permanent's controller's end step (e.g. Nettlevine
        // Blight). The ability is controlled by the enchanted permanent's controller, so the stack
        // entry's controller is that player even though the Aura keeps its own controller.
        gameData.forEachPermanent((auraOwnerId, perm) -> {
            List<CardEffect> enchantedControllerEndStepEffects =
                    perm.getCard().getEffects(EffectSlot.ENCHANTED_PERMANENT_CONTROLLER_END_STEP_TRIGGERED);
            if (enchantedControllerEndStepEffects == null || enchantedControllerEndStepEffects.isEmpty()) return;
            if (!perm.isAttached()) return;

            UUID enchantedPermanentControllerId = gameQueryService.findPermanentController(gameData, perm.getAttachedTo());
            if (enchantedPermanentControllerId == null) return;
            if (!enchantedPermanentControllerId.equals(activePlayerId)) return;

            for (CardEffect effect : enchantedControllerEndStepEffects) {
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        enchantedPermanentControllerId,
                        perm.getCard().getName() + "'s end step ability",
                        new ArrayList<>(List.of(effect)),
                        perm.getAttachedTo(),
                        perm.getId()
                );
                entry.setNonTargeting(true);
                entry.setTriggeringPermanentId(perm.getAttachedTo());
                entry.setSourcePermanentSnapshot(new Permanent(perm));
                gameData.stack.add(entry);

                gameLogService.append(gameData,
                        GameLog.cardThen(perm.getCard(), "'s end step ability triggers."));
                log.info("Game {} - {} enchanted-permanent-controller end-step trigger pushed onto stack",
                        gameData.id, perm.getCard().getName());
            }
        });

        // Check all battlefields for curses with ENCHANTED_PLAYER_END_STEP_TRIGGERED effects. Unlike
        // the upkeep variant, these fire at EACH end step (any player's turn) and act on the enchanted
        // player, whose id is baked as the (non-targeting) targetId (e.g. Fraying Sanity's mill).
        gameData.forEachPermanent((auraOwnerId, perm) -> {
            List<CardEffect> enchantedPlayerEndStepEffects =
                    perm.getCard().getEffects(EffectSlot.ENCHANTED_PLAYER_END_STEP_TRIGGERED);
            if (enchantedPlayerEndStepEffects == null || enchantedPlayerEndStepEffects.isEmpty()) return;
            if (!perm.isAttached()) return;

            // For curses, attachedTo is the enchanted player's UUID.
            UUID enchantedPlayerId = perm.getAttachedTo();

            for (CardEffect effect : enchantedPlayerEndStepEffects) {
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        auraOwnerId,
                        perm.getCard().getName() + "'s end step ability",
                        new ArrayList<>(List.of(effect)),
                        enchantedPlayerId,
                        perm.getId()
                ));

                gameLogService.append(gameData,
                        GameLog.cardThen(perm.getCard(), "'s end step ability triggers."));
                log.info("Game {} - {} enchanted-player end-step trigger pushed onto stack",
                        gameData.id, perm.getCard().getName());
            }
        });

        if (gameData.hasPendingInteraction(PermanentChoiceContext.ETBTokenMultiTargetTrigger.class)) {
            etbTokenTargetService.processNextETBTokenMultiTargetTrigger(gameData);
            return;
        }

        // Process pending end-step targeted triggers (e.g. Reaper from the Abyss morbid, Voltaic Servant)
        if (gameData.hasPendingInteraction(PermanentChoiceContext.EndStepTriggerTarget.class)) {
            processNextEndStepTriggerTarget(gameData);
            return;
        }

        // Process pending end-step graveyard-target triggers (e.g. Moseo, Vein's New Dean)
        if (gameData.hasPendingInteraction(PermanentChoiceContext.SpellGraveyardTargetTrigger.class)) {
            triggerCollectionService.processNextSpellGraveyardTargetTrigger(gameData);
            return;
        }

        playerInputService.processNextMayAbility(gameData);
    }

    public void handleCleanupTriggers(GameData gameData) {
        List<RemoveCountersFromPermanentAtNextCleanup> pendingRemovals =
                gameData.drainDelayedActions(RemoveCountersFromPermanentAtNextCleanup.class);
        for (RemoveCountersFromPermanentAtNextCleanup action : pendingRemovals) {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    action.sourceCard(),
                    action.controllerId(),
                    action.sourceCard().getName() + "'s delayed ability",
                    new ArrayList<>(List.of(new RemoveCounterFromTargetPermanentEffect(
                            action.counterType(), null, action.amount()))),
                    action.permanentId(),
                    (UUID) null);
            entry.setNonTargeting(true);
            gameData.stack.add(entry);
            gameLogService.append(gameData, GameLog.cardThen(action.sourceCard(),
                    "'s delayed ability triggers to remove counters."));
        }
    }

    /**
     * Processes the next pending end-step targeted trigger.
     * Presents the controller with a permanent choice; when selected, the trigger is
     * pushed onto the stack with the chosen target.
     *
     * @param gameData the current game state to modify
     */
    public void processNextEndStepTriggerTarget(GameData gameData) {
        if (!gameData.hasPendingInteraction(PermanentChoiceContext.EndStepTriggerTarget.class)) {
            playerInputService.processNextMayAbility(gameData);
            return;
        }

        PermanentChoiceContext.EndStepTriggerTarget trigger = gameData.pollPendingInteraction(PermanentChoiceContext.EndStepTriggerTarget.class);

        TargetFilter targetFilter = trigger.sourceCard().getTargetFilter();
        TriggerTargetCollector.Result result = triggerTargetCollector.collect(
                gameData,
                trigger.effects(),
                targetFilter,
                trigger.controllerId(),
                trigger.sourceCard(),
                TriggerTargetCollector.Options.END_STEP);
        List<UUID> validTargets = result.validTargets();
        boolean canTargetPlayers = result.canTargetPlayers();
        boolean canTargetPermanents = result.canTargetPermanents();

        if (validTargets.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.cardThen(trigger.sourceCard(), "'s end step trigger has no valid targets."));
            log.info("Game {} - {} end-step trigger skipped (no valid targets)",
                    gameData.id, trigger.sourceCard().getName());
            // Try next pending trigger
            processNextEndStepTriggerTarget(gameData);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(trigger);

        String targetDescription;
        if (targetFilter instanceof PermanentPredicateTargetFilter ppf) {
            targetDescription = ppf.errorMessage().replace("Target must be ", "").replace("an ", "").replace("a ", "");
        } else if (canTargetPlayers && canTargetPermanents) {
            targetDescription = "any target";
        } else if (canTargetPlayers) {
            targetDescription = "target player";
        } else {
            targetDescription = "target permanent";
        }

        playerInputService.beginPermanentChoice(gameData, trigger.controllerId(), validTargets,
                trigger.sourceCard().getName() + "'s ability — Choose " + targetDescription + ".");

        gameLogService.append(gameData,
                GameLog.cardThen(trigger.sourceCard(), "'s end step trigger — choose " + targetDescription + "."));
        log.info("Game {} - {} end-step trigger awaiting target selection", gameData.id, trigger.sourceCard().getName());
    }

    /**
     * Scans battlefields and the active player's graveyard for beginning-of-combat triggered
     * abilities and pushes them onto the stack.
     * {@code BEGINNING_OF_COMBAT_TRIGGERED} fires only for the active player's permanents
     * (CR 507.1: "At the beginning of combat on your turn").
     * {@code EACH_BEGINNING_OF_COMBAT_TRIGGERED} fires for every permanent on every
     * battlefield (e.g. Majestic Myriarch / Odric, Lunarch Marshal).
     * {@code OPPONENT_BEGINNING_OF_COMBAT_TRIGGERED} fires only for permanents controlled by a
     * player other than the active player (Sentinel of the Eternal Watch).
     *
     * @param gameData the current game state to modify
     */
    public void handleBeginningOfCombatTriggers(GameData gameData) {
        queueDelayedBeginningOfCombatTriggers(gameData);
        collectEmblemStepTriggers(gameData, EmblemTriggerStep.BEGINNING_OF_COMBAT);

        UUID activePlayerId = gameData.activePlayerId;
        List<Permanent> battlefield = gameData.playerBattlefields.get(activePlayerId);
        if (battlefield != null) {
            for (Permanent perm : battlefield) {
                queueBeginningOfCombatTriggers(gameData, activePlayerId, perm,
                        perm.getCard().getEffects(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED));
            }
        }

        List<Card> graveyard = gameData.playerGraveyards.get(activePlayerId);
        if (graveyard != null) {
            for (Card card : new ArrayList<>(graveyard)) {
                queueGraveyardBeginningOfCombatTriggers(gameData, activePlayerId, card,
                        gameQueryService.getEffectiveGraveyardEffects(
                                gameData, card, EffectSlot.GRAVEYARD_BEGINNING_OF_COMBAT_TRIGGERED));
            }
        }

        gameData.forEachPermanent((playerId, perm) ->
                queueBeginningOfCombatTriggers(gameData, playerId, perm,
                        perm.getCard().getEffects(EffectSlot.EACH_BEGINNING_OF_COMBAT_TRIGGERED)));

        gameData.forEachPermanent((playerId, perm) -> {
            if (!playerId.equals(activePlayerId)) {
                queueBeginningOfCombatTriggers(gameData, playerId, perm,
                        perm.getCard().getEffects(EffectSlot.OPPONENT_BEGINNING_OF_COMBAT_TRIGGERED));
            }
        });

        triggerCollectionService.checkUntapAttackedCreaturesEachCombatThisTurnTriggers(gameData);

        if (gameData.hasPendingInteraction(PermanentChoiceContext.ETBTokenMultiTargetTrigger.class)) {
            etbTokenTargetService.processNextETBTokenMultiTargetTrigger(gameData);
        }

        if (gameData.interaction.isAwaitingInput()) {
            return;
        }

        if (gameData.hasPendingInteraction(PermanentChoiceContext.TriggeredModalTrigger.class)) {
            triggerCollectionService.processNextTriggeredModalTrigger(gameData);
            return;
        }

        if (gameData.hasPendingInteraction(PermanentChoiceContext.SpellGraveyardTargetTrigger.class)) {
            triggerCollectionService.processNextSpellGraveyardTargetTrigger(gameData);
            return;
        }

        if (gameData.hasPendingInteraction(PermanentChoiceContext.EmblemTriggerTarget.class)) {
            triggerCollectionService.processNextEmblemTriggerTarget(gameData);
            return;
        }

        if (gameData.hasPendingInteraction(PermanentChoiceContext.BeginningOfCombatTriggerTarget.class)) {
            processNextBeginningOfCombatTriggerTarget(gameData);
            return;
        }

        if (gameData.interaction.isAwaitingInput()) {
            return;
        }

        playerInputService.processNextMayAbility(gameData);
    }

    private void queueDelayedBeginningOfCombatTriggers(GameData gameData) {
        List<DelayedBeginningOfCombatTrigger> delayedTriggers =
                gameData.drainDelayedActions(DelayedBeginningOfCombatTrigger.class);
        for (DelayedBeginningOfCombatTrigger trigger : delayedTriggers) {
            gameData.queueInteraction(new PermanentChoiceContext.ETBTokenMultiTargetTrigger(
                    trigger.sourceCard(), trigger.controllerId(), new ArrayList<>(List.of(trigger.effect())),
                    null, List.of(), 0, 0));
            gameLogService.append(gameData,
                    GameLog.cardThen(trigger.sourceCard(), "'s delayed beginning of combat trigger triggers."));
        }
    }

    private void queueGraveyardBeginningOfCombatTriggers(GameData gameData, UUID controllerId,
                                                          Card card, List<CardEffect> combatEffects) {
        if (combatEffects == null || combatEffects.isEmpty()) {
            return;
        }

        for (CardEffect effect : combatEffects) {
            if (effect instanceof ConditionalEffect conditional
                    && conditional.interveningIf()) {
                if (!conditionEvaluationService.isMet(gameData, conditional.condition(),
                        ConditionContext.forCard(card, controllerId))) {
                    log.info("Game {} - {} graveyard beginning-of-combat ability skipped ({})",
                            gameData.id, card.getName(), conditional.conditionNotMetReason());
                    continue;
                }
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        card,
                        controllerId,
                        card.getName() + "'s combat ability",
                        new ArrayList<>(List.of(effect))
                ));
                gameLogService.append(gameData, GameLog.cardThen(card, "'s combat ability triggers."));
                log.info("Game {} - {} graveyard beginning-of-combat trigger pushed onto stack",
                        gameData.id, card.getName());
                continue;
            }

            if (effect instanceof MayPayManaEffect mayPay) {
                gameData.queueMayAbility(card, controllerId, mayPay, null);
            } else if (effect instanceof MayEffect may) {
                gameData.queueMayAbility(card, controllerId, may);
            } else {
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        card,
                        controllerId,
                        card.getName() + "'s combat ability",
                        new ArrayList<>(List.of(effect))
                ));

                gameLogService.append(gameData, GameLog.cardThen(card, "'s combat ability triggers."));
                log.info("Game {} - {} graveyard beginning-of-combat trigger pushed onto stack",
                        gameData.id, card.getName());
            }
        }
    }

    private void queueBeginningOfCombatTriggers(GameData gameData, UUID controllerId, Permanent perm,
                                                List<CardEffect> combatEffects) {
        if (combatEffects == null || combatEffects.isEmpty()) {
            return;
        }

        // For equipment triggers, only fire if the equipment is attached to a creature
        if (perm.isAttached()) {
            Permanent equippedCreature = gameQueryService.findPermanentById(gameData, perm.getAttachedTo());
            if (equippedCreature == null || !gameQueryService.isCreature(gameData, equippedCreature)) {
                return;
            }
        }

        int previousCopies = gameData.beginTriggeredAbilityCopies(1
                + gameQueryService.countAdditionalTriggeredAbilityTriggers(
                gameData, controllerId, perm));
        try {
        List<CardEffect> mayEffects = combatEffects.stream()
                .filter(e -> e instanceof MayEffect && e.targetSpec() == TargetSpec.NONE)
                .toList();
        List<ChooseOneEffect> modalEffects = new ArrayList<>();
        List<CardEffect> mandatoryEffects = new ArrayList<>();
        for (CardEffect effect : combatEffects) {
            if (effect instanceof ChooseOneEffect chooseOne) {
                modalEffects.add(chooseOne);
                continue;
            }
            // Targets are chosen when a triggered ability is put on the stack, before the
            // resolution-time may choice. Keep targeted MayEffects in the targeting flow.
            if (effect instanceof MayEffect && effect.targetSpec() == TargetSpec.NONE) {
                continue;
            }
            // Intervening-if conditions gate at trigger time. Max speed is an ability-granting
            // condition, so the wrapped ability does not exist unless the controller is at speed 4.
            if (effect instanceof ConditionalEffect conditional && conditional.interveningIf()) {
                if (!conditionEvaluationService.isMet(gameData, conditional.condition(),
                        ConditionContext.forPermanent(perm, controllerId))) {
                    log.info("Game {} - {} beginning-of-combat trigger skipped ({} not met)",
                            gameData.id, perm.getCard().getName(), conditional.condition().conditionName());
                    continue;
                }
            }
            mandatoryEffects.add(effect);
        }

        for (CardEffect effect : mayEffects) {
            MayEffect may = (MayEffect) effect;
            if (may.choicePlayer() == MayChoicePlayer.ACTIVE_PLAYER) {
                gameData.queueMayAbility(perm.getCard(), controllerId, may, null, perm.getId(),
                        gameData.activePlayerId, new Permanent(perm));
            } else {
                gameData.queueMayAbility(perm.getCard(), controllerId, may, null, perm.getId());
            }
        }

        for (ChooseOneEffect effect : modalEffects) {
            gameData.queueInteraction(new PermanentChoiceContext.TriggeredModalTrigger(
                    perm.getCard(), controllerId, effect, perm.getId()));
            gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
            log.info("Game {} - {} beginning-of-combat trigger queued for mode selection",
                    gameData.id, perm.getCard().getName());
        }

        if (mandatoryEffects.isEmpty()) {
            return;
        }

        ChooseOneEffect targetedModal = mandatoryEffects.stream()
                .filter(ChooseOneEffect.class::isInstance)
                .map(ChooseOneEffect.class::cast)
                .filter(modal -> modal.options().stream()
                        .flatMap(option -> option.effects().stream())
                        .anyMatch(effect -> effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                                || effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)))
                .findFirst()
                .orElse(null);
        if (targetedModal != null) {
            gameData.queueInteraction(new PermanentChoiceContext.TriggeredModalTrigger(
                    perm.getCard(), controllerId, targetedModal, perm.getId()));
            mandatoryEffects.remove(targetedModal);
            gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
            if (mandatoryEffects.isEmpty()) {
                return;
            }
        }

        boolean needsPermanentTarget = mandatoryEffects.stream()
                .filter(effect -> !(effect instanceof MayPayManaEffect mayPay && mayPay.targetAfterPayment()))
                .anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.PERMANENT) || e.targetSpec().admits(TargetPredicate.Kind.PLAYER));
        boolean needsGraveyardTarget = mandatoryEffects.stream()
                .filter(effect -> !(effect instanceof MayPayManaEffect mayPay && mayPay.targetAfterPayment()))
                .anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD));
        if (needsGraveyardTarget) {
            ExileGraveyardCardsEffect exileEffect = mandatoryEffects.stream()
                    .filter(e -> e instanceof ExileGraveyardCardsEffect ge
                            && ge.scope() == GraveyardExileScope.TARGET_CARDS_ANY_GRAVEYARD)
                    .map(e -> (ExileGraveyardCardsEffect) e)
                    .findFirst()
                    .orElse(null);
            if (exileEffect != null) {
                graveyardTargetingService.handleBeginningOfCombatGraveyardTargeting(
                        gameData, controllerId, perm.getCard(), mandatoryEffects, perm.getId(), exileEffect);
            } else {
                gameData.queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                        perm.getCard(), controllerId, new ArrayList<>(mandatoryEffects)));
                gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                log.info("Game {} - {} beginning-of-combat graveyard-target trigger queued",
                        gameData.id, perm.getCard().getName());
            }
        } else if (needsPermanentTarget
                && (etbTokenTargetService.hasGroupWithMaxTargetsGreaterThanOne(perm.getCard())
                    || etbTokenTargetService.hasMultipleTargetGroups(perm.getCard()))) {
            // "up to two target creatures", or two independently filtered target groups (Boros
            // Battleshaper) — reuse the slot-by-slot multi-target walker (shared with ETB /
            // self-cast / attack triggers) so each target is chosen separately.
            gameData.queueInteraction(
                    new PermanentChoiceContext.ETBTokenMultiTargetTrigger(
                            perm.getCard(), controllerId,
                            new ArrayList<>(mandatoryEffects), perm.getId(),
                            new ArrayList<>(), 0, 0));
            gameLogService.append(gameData,
                    GameLog.cardThen(perm.getCard(), "'s beginning of combat ability triggers."));
            log.info("Game {} - {} beginning-of-combat trigger queued for multi-target selection",
                    gameData.id, perm.getCard().getName());
        } else if (needsPermanentTarget) {
            gameData.queueInteraction(
                    new PermanentChoiceContext.BeginningOfCombatTriggerTarget(
                            perm.getCard(), controllerId,
                            new ArrayList<>(mandatoryEffects), perm.getId()));
            gameLogService.append(gameData,
                    GameLog.cardThen(perm.getCard(), "'s beginning of combat ability triggers."));
            log.info("Game {} - {} beginning-of-combat trigger queued for targeting",
                    gameData.id, perm.getCard().getName());
        } else {
            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    perm.getCard(),
                    controllerId,
                    perm.getCard().getName() + "'s combat ability",
                    new ArrayList<>(mandatoryEffects),
                    (UUID) null,
                    perm.getId()
            ));

            gameLogService.append(gameData,
                    GameLog.cardThen(perm.getCard(), "'s beginning of combat ability triggers."));
            log.info("Game {} - {} beginning-of-combat trigger pushed onto stack",
                    gameData.id, perm.getCard().getName());
        }
        } finally {
            gameData.restoreTriggeredAbilityCopies(previousCopies);
        }
    }

    /**
     * Processes the next pending beginning-of-combat targeted trigger.
     * Presents the controller with a permanent choice; when selected, the trigger is
     * pushed onto the stack with the chosen target.
     */
    public void processNextBeginningOfCombatTriggerTarget(GameData gameData) {
        if (!gameData.hasPendingInteraction(PermanentChoiceContext.BeginningOfCombatTriggerTarget.class)) {
            playerInputService.processNextMayAbility(gameData);
            return;
        }

        PermanentChoiceContext.BeginningOfCombatTriggerTarget trigger =
                gameData.pollPendingInteraction(PermanentChoiceContext.BeginningOfCombatTriggerTarget.class);

        TargetFilter targetFilter = trigger.sourceCard().getTargetFilter();
        TriggerTargetCollector.Result result = triggerTargetCollector.collect(
                gameData,
                trigger.effects(),
                targetFilter,
                trigger.controllerId(),
                trigger.sourceCard(),
                TriggerTargetCollector.Options.END_STEP,
                gameQueryService.findPermanentById(gameData, trigger.sourcePermanentId()));
        List<UUID> validTargets = result.validTargets();
        boolean optionalTarget = trigger.sourceCard().getMinTargets() == 0;

        if (validTargets.isEmpty()) {
            if (optionalTarget) {
                // "up to one" with no legal targets — ability still goes on the stack with no target
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        trigger.sourceCard(),
                        trigger.controllerId(),
                        trigger.sourceCard().getName() + "'s combat ability",
                        new ArrayList<>(trigger.effects()),
                        (UUID) null,
                        trigger.sourcePermanentId()
                ));
                gameLogService.append(gameData,
                        GameLog.cardThen(trigger.sourceCard(), "'s beginning of combat trigger targets nothing."));
                log.info("Game {} - {} beginning-of-combat trigger stacked with no target (up to one)",
                        gameData.id, trigger.sourceCard().getName());
                processNextBeginningOfCombatTriggerTarget(gameData);
                return;
            }
            gameLogService.append(gameData,
                    GameLog.cardThen(trigger.sourceCard(), "'s beginning of combat trigger has no valid targets."));
            log.info("Game {} - {} beginning-of-combat trigger skipped (no valid targets)",
                    gameData.id, trigger.sourceCard().getName());
            processNextBeginningOfCombatTriggerTarget(gameData);
            return;
        }

        if (optionalTarget) {
            // Choose yourself to decline (Saga chapter "up to one" pattern)
            validTargets = new ArrayList<>(validTargets);
            validTargets.add(trigger.controllerId());
        }

        gameData.interaction.setPermanentChoiceContext(trigger);

        String targetDescription;
        if (targetFilter instanceof PermanentPredicateTargetFilter ppf) {
            targetDescription = ppf.errorMessage().replace("Target must be ", "").replace("an ", "").replace("a ", "");
        } else if (result.canTargetPlayers() && result.canTargetPermanents()) {
            targetDescription = "any target";
        } else if (result.canTargetPlayers()) {
            targetDescription = "target player";
        } else {
            targetDescription = "target permanent";
        }

        String prompt = optionalTarget
                ? trigger.sourceCard().getName() + "'s ability — Choose up to one " + targetDescription
                        + " (choose yourself to decline)."
                : trigger.sourceCard().getName() + "'s ability — Choose " + targetDescription + ".";

        playerInputService.beginPermanentChoice(gameData, trigger.controllerId(), validTargets, prompt);

        gameLogService.append(gameData, GameLog.cardThen(trigger.sourceCard(),
                "'s beginning of combat trigger — choose " + targetDescription + "."));
        log.info("Game {} - {} beginning-of-combat trigger awaiting target selection",
                gameData.id, trigger.sourceCard().getName());
    }
}
