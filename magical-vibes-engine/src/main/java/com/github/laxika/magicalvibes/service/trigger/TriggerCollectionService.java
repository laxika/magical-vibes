package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.BendingType;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DelayedSpellCastTrigger;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.CreatureDeathTriggerWatcher;
import com.github.laxika.magicalvibes.model.TargetedCreatureDeathTriggerWatcher;
import com.github.laxika.magicalvibes.model.CreatureEntersTriggerWatcher;
import com.github.laxika.magicalvibes.model.DamagedCreatureDeathTriggerWatcher;
import com.github.laxika.magicalvibes.model.Boon;
import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.DelayedEffectOnDeath;
import com.github.laxika.magicalvibes.model.DelayedReturnOnDeath;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.rules.DepartedAttachments;
import com.github.laxika.magicalvibes.model.rules.PermanentChange;
import com.github.laxika.magicalvibes.model.rules.PermanentChangeEvent;
import com.github.laxika.magicalvibes.model.rules.RulesObserver;
import com.github.laxika.magicalvibes.model.rules.ZoneChangeCause;
import com.github.laxika.magicalvibes.model.rules.RulesEvent;
import com.github.laxika.magicalvibes.model.rules.RulesEventBatch;
import com.github.laxika.magicalvibes.model.rules.ZoneChangeEvent;
import com.github.laxika.magicalvibes.model.trigger.CounterTrigger;
import com.github.laxika.magicalvibes.model.trigger.DamageTrigger;
import com.github.laxika.magicalvibes.model.trigger.CombatTrigger;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Trigger;
import com.github.laxika.magicalvibes.model.trigger.LibraryTrigger;
import com.github.laxika.magicalvibes.model.trigger.LifeTrigger;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.trigger.TapTrigger;
import com.github.laxika.magicalvibes.model.trigger.ZoneChangeTrigger;
import com.github.laxika.magicalvibes.model.trigger.TriggerMultiplicity;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.SpellTrigger;
import com.github.laxika.magicalvibes.model.trigger.LandPlayTrigger;
import com.github.laxika.magicalvibes.model.trigger.AbilityTrigger;
import com.github.laxika.magicalvibes.model.trigger.SpellFilter;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.LegacySlots;
import com.github.laxika.magicalvibes.model.LifeGainOpponentLifeLossWatcher;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaCopyRider;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.OpeningHandRevealTrigger;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingSourceDamage;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.SaturatingMath;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TextReplacement;
import com.github.laxika.magicalvibes.model.condition.TriggeringPermanentHasGreaterPowerOrToughnessThanSource;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.action.DelayedControllerSpellCastTrigger;
import com.github.laxika.magicalvibes.model.action.DelayedDestroyTargetWhenSourceLeaves;
import com.github.laxika.magicalvibes.model.action.DelayedExileReturnCounterTrigger;
import com.github.laxika.magicalvibes.model.action.DelayedSacrificeSourceWhenTargetLeaves;
import com.github.laxika.magicalvibes.model.action.DelayedSacrificeTargetWhenSourceLeaves;
import com.github.laxika.magicalvibes.model.action.DelayedWatchedCreatureDealsDamage;
import com.github.laxika.magicalvibes.model.action.DelayedWatchedCreatureDealtDamage;
import com.github.laxika.magicalvibes.model.action.DelayedWatchedCreatureDealtDamageByAttackingCreature;
import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.model.SacrificeBoonWatcher;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.event.GameEventAudience;
import com.github.laxika.magicalvibes.model.event.VisualCueType;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.AttachedCreatureBecomesCopyOfExiledCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseColorForSourceEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.AttachedCreatureBecomesCopyOfExiledCreatureEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.ChooseCardNameAtResolutionEffectHandler;
import com.github.laxika.magicalvibes.model.effect.CommanderCreaturesDealPowerDamageToPreventedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.OptionalTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeredModalEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.StackEntryControlledByWatchedPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.CardHasXInManaCostPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSpecificPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyLinkedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CopyControllerActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CopyNextSpellCastThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeForSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.DemonstrateEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.NextMatchingSpellCostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantCastTriggerToSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerGrant;
import com.github.laxika.magicalvibes.model.effect.PutVoyageCounterOnExiledCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnVoyagingCardFromExileEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.AddOneOfEachManaTypeProducedByLandEffect;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseTargetsAtRandomEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.EnterBattlefieldOnDiscardEffect;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.condition.TriggeringSpellMatches;
import com.github.laxika.magicalvibes.model.condition.TreasureManaSpentToActivate;
import com.github.laxika.magicalvibes.model.effect.CascadeEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.condition.RepeatedAdditionalCostPaid;
import com.github.laxika.magicalvibes.model.amount.RepeatedAdditionalCostCount;
import com.github.laxika.magicalvibes.model.effect.EvolveEffect;
import com.github.laxika.magicalvibes.model.effect.IncrementTriggerEffect;
import com.github.laxika.magicalvibes.service.event.VisualCueService;
import com.github.laxika.magicalvibes.service.effect.TextChangeTransformer;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggerCandidate;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityRequest;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService;
import com.github.laxika.magicalvibes.service.trigger.TargetAbilityReader.TargetEffect;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityRequest.TriggerRecord;
import com.github.laxika.magicalvibes.service.effect.LayerSystemService;
import com.github.laxika.magicalvibes.service.effect.normalfx.TargetRedirectionSupport;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTriggeringCardFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicates;
import com.github.laxika.magicalvibes.model.effect.TargetSpec;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.TriggeredAbilityQueueService;
import com.github.laxika.magicalvibes.service.battlefield.ETBTokenTargetService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.effect.GrantedTriggeredAbilitySupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.AnimationSupport;
import com.github.laxika.magicalvibes.service.effect.turnup.TurnFaceUpCopyService;
import com.github.laxika.magicalvibes.service.effect.GraveyardTargetingSupport;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.state.StateTriggerService;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import com.github.laxika.magicalvibes.service.target.ValidTargetService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Thin orchestrator that detects trigger events, iterates permanents/effect-slots,
 * and delegates per-effect handling to the {@link TriggerCollectorRegistry}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TriggerCollectionService {

    /** A becomes-the-target record that names nothing beyond the spell or ability that targeted. */
    private static final TriggerRecord NO_EXTRA_RECORD = entry -> {
    };

    /**
     * The ability a mannequin counter gives its permanent while Makeshift Mannequin's grant lasts: "When this creature
     * becomes the target of a spell or ability, sacrifice it."
     */
    private static final TriggeredAbility MANNEQUIN_ABILITY = new TriggeredAbility(
            Triggers.becomesTargetOfSpellOrAbility(), new SacrificeSelfEffect());

    private final TriggerCollectorRegistry registry;
    private final GameOutcomeService gameOutcomeService;
    private final PlayerInputService playerInputService;
    private final TriggeredAbilityQueueService triggeredAbilityQueueService;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final TargetLegalityService targetLegalityService;
    private final ValidTargetService validTargetService;
    private final ConditionEvaluationService conditionEvaluationService;
    private final AmountEvaluationService amountEvaluationService;
    private final GameLogService gameLogService;
    private final ETBTokenTargetService etbTokenTargetService;
    private final GrantedTriggeredAbilitySupport grantedTriggeredAbilitySupport;
    private final GraveyardTargetingSupport graveyardTargetingSupport;
    @Autowired
    @Lazy
    private TargetRedirectionSupport targetRedirectionSupport;
    @Autowired
    @Lazy
    private TriggeredAbilityStackService triggeredAbilityStackService;
    @Autowired
    private ObjectProvider<StateTriggerService> stateTriggerService;
    @Autowired
    private ObjectProvider<VisualCueService> visualCueService;
    @Autowired
    @Lazy
    private AttachedCreatureBecomesCopyOfExiledCreatureEffectHandler attachedCreatureCopyHandler;
    @Autowired
    @Lazy
    private ChooseCardNameAtResolutionEffectHandler chooseCardNameHandler;
    @Autowired
    @Lazy
    private DeathTriggerCollectorService deathTriggerCollectorService;
    @Autowired
    @Lazy
    private SpellCastTriggerCollectorService spellCastTriggerCollectorService;
    @Autowired
    @Lazy
    private PlayedCardNameTriggerCollectorService playedCardNameTriggerCollectorService;
    @Autowired
    @Lazy
    private DiscardTriggerCollectorService discardTriggerCollectorService;
    @Autowired
    @Lazy
    private TurnFaceUpCopyService turnFaceUpCopyService;
    @Autowired
    @Lazy
    private AnimationSupport animationSupport;

    public List<CardEffect> grantedTriggeredEffects(GameData gameData, Permanent permanent, EffectSlot slot) {
        return grantedTriggeredAbilitySupport.grantedTriggeredEffects(gameData, permanent, slot);
    }

    /** {@link GrantedTriggeredAbilitySupport#grantedOwnEntryAbilities}. */
    public List<TriggeredAbility> grantedOwnEntryAbilities(GameData gameData, Permanent permanent) {
        return grantedTriggeredAbilitySupport.grantedOwnEntryAbilities(gameData, permanent);
    }

    /**
     * The abilities that {@code watch} reads of {@code permanent} as their collector reads them
     * ({@code CombatAbilityReader}), one list of reads for each ability (one object on the stack, CR 603.3): its
     * printed ones when {@code printed}, then those other permanents' static abilities grant it.
     */
    private List<List<CombatAbilityReader.CombatEffect>> combatReads(GameData gameData, Permanent permanent,
                                                                     CombatWatch watch, boolean printed) {
        Trigger watched = watch.trigger();
        List<List<CombatAbilityReader.CombatEffect>> reads = new ArrayList<>();
        if (printed) {
            reads.addAll(CombatAbilityReader.readByAbility(WatchedAbilities.printed(permanent.getCard(), watch),
                    watched));
        }
        reads.addAll(CombatAbilityReader.readByAbility(
                grantedTriggeredAbilitySupport.grantedWatchedAbilities(gameData, permanent, watched), watched));
        return reads;
    }

    /**
     * The abilities that {@code watch} reads of {@code permanent} as their collector reads them, one list of reads for
     * each ability (one object on the stack, CR 603.3): its printed ones, those granted until end of turn, combat or
     * next upkeep, those until it leaves the battlefield, and those other permanents' static abilities grant it, in
     * that order.
     */
    private List<List<CombatAbilityReader.CombatEffect>> combatReadsWithGrants(GameData gameData, Permanent permanent,
                                                                               CombatWatch watch) {
        Trigger watched = watch.trigger();
        List<TriggeredAbility> abilities = new ArrayList<>(WatchedAbilities.printed(permanent.getCard(), watch));
        abilities.addAll(WatchedAbilities.temporary(permanent, watched));
        abilities.addAll(WatchedAbilities.persistent(permanent, watched));
        abilities.addAll(grantedTriggeredAbilitySupport.grantedWatchedAbilities(gameData, permanent, watched));
        return CombatAbilityReader.readByAbility(abilities, watched);
    }

    /** {@link GrantedTriggeredAbilitySupport#grantedWatchedAbilities(GameData, Permanent, Trigger)}. */
    public List<TriggeredAbility> grantedWatchedAbilities(GameData gameData, Permanent permanent, Trigger watched) {
        return grantedTriggeredAbilitySupport.grantedWatchedAbilities(gameData, permanent, watched);
    }

    /** {@link GrantedTriggeredAbilitySupport#grantedZoneChangeAbilities}. */
    public List<TriggeredAbility> grantedZoneChangeAbilities(GameData gameData, Permanent permanent) {
        return grantedTriggeredAbilitySupport.grantedZoneChangeAbilities(gameData, permanent);
    }

    public void checkYouPutCountersTriggers(GameData gameData, UUID placingPlayerId, int amount) {
        if (placingPlayerId == null || amount <= 0) {
            return;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(placingPlayerId);
        if (battlefield == null) {
            return;
        }

        TriggerContext context = new TriggerContext.CountersPlaced(placingPlayerId, amount);
        for (Permanent permanent : new ArrayList<>(battlefield)) {
            // "Whenever you put one or more counters on a permanent or player, … deals that much damage to target …"
            // (All Will Be One): the placing player and the number of counters put ("that much").
            putCounterAbilities(gameData, permanent, placingPlayerId,
                    CounterWatch.ON_YOU_PUT_COUNTERS_ON_PERMANENT_OR_PLAYER, context,
                    ConditionContext.forPermanent(permanent, placingPlayerId).withEventValue(amount),
                    eventRecord(permanent, null, placingPlayerId, amount, null, null));
        }
    }

    /** Fires triggers for one time-counter placement event on a permanent the player controls. */
    public void checkYouPutTimeCountersTriggers(GameData gameData, Permanent target,
                                                UUID placingPlayerId, int amount) {
        if (target == null || placingPlayerId == null || amount <= 0
                || !placingPlayerId.equals(gameQueryService.findPermanentController(gameData, target.getId()))) {
            return;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(placingPlayerId);
        if (battlefield == null) {
            return;
        }

        TriggerContext context = new TriggerContext.TimeCountersPlaced(target, placingPlayerId, amount);
        for (Permanent permanent : new ArrayList<>(battlefield)) {
            putCounterAbilities(gameData, permanent, placingPlayerId,
                    CounterWatch.ON_YOU_PUT_TIME_COUNTERS_ON_CONTROLLED_PERMANENT, context,
                    ConditionContext.forPermanent(permanent, placingPlayerId).withEventValue(amount),
                    eventRecord(permanent, null, placingPlayerId, amount, target, placingPlayerId));
        }
    }

    /** Fires triggers for each lore counter placed on a Saga the player controls. */
    public void checkYouPutLoreCounterOnSagaTriggers(GameData gameData, Permanent saga,
                                                     UUID placingPlayerId) {
        if (saga == null || placingPlayerId == null || !saga.getCard().isSaga()
                || !placingPlayerId.equals(gameQueryService.findPermanentController(gameData, saga.getId()))) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(placingPlayerId);
        if (battlefield == null) {
            return;
        }

        TriggerContext context = new TriggerContext.LoreCounterPlaced(saga, placingPlayerId);
        for (Permanent permanent : new ArrayList<>(battlefield)) {
            // "Whenever you put a lore counter on a Saga you control, put a +1/+1 counter on up to one other target
            // creature" (Sigurd, Jarl of Ravensthorpe): the Saga is the triggering permanent.
            putCounterAbilities(gameData, permanent, placingPlayerId,
                    CounterWatch.ON_YOU_PUT_LORE_COUNTERS_ON_SAGA, context,
                    ConditionContext.forPermanent(permanent, placingPlayerId),
                    eventRecord(permanent, null, placingPlayerId, null, saga, placingPlayerId));
        }
    }

    /** Fires +1/+1 counter triggers on cards in the controlled creature's controller's graveyard. */
    public void checkGraveyardAllyPlusOnePlusOneCountersPutOnCreatureTriggers(
            GameData gameData, Permanent creature, UUID creatureControllerId, int amount) {
        if (creature == null || creatureControllerId == null || amount <= 0
                || !gameQueryService.isCreature(gameData, creature)) {
            return;
        }

        List<Card> graveyard = gameData.playerGraveyards.get(creatureControllerId);
        if (graveyard == null) {
            return;
        }

        TriggerContext event = new TriggerContext.CountersPlaced(creatureControllerId, amount);
        for (Card card : new ArrayList<>(graveyard)) {
            putGraveyardCounterAbilities(gameData, card, creatureControllerId,
                    CounterWatch.GRAVEYARD_ON_ALLY_PLUS_ONE_PLUS_ONE_COUNTERS_PUT_ON_CREATURE, event);
        }
    }

    public void checkLoyaltyCounterRemovalTriggers(GameData gameData) {
        gameData.forEachPermanent((controllerId, permanent) -> {
            int removedCounters = permanent.drainCountersRemovedSinceTriggerCheck();
            if (removedCounters > 0) {
                TriggerContext context = new TriggerContext.CountersRemovedFromPermanent(
                        permanent, removedCounters);
                for (Permanent watcher : new ArrayList<>(
                        gameData.playerBattlefields.getOrDefault(controllerId, List.of()))) {
                    // "Whenever one or more counters are removed from a permanent you control" (Cloudsculpt
                    // Armorer's seek): the permanent and the number removed ("that much").
                    putCounterAbilities(gameData, watcher, controllerId,
                            CounterWatch.ON_ALLY_COUNTERS_REMOVED_FROM_PERMANENT, context,
                            ConditionContext.forPermanent(watcher, controllerId).withEventValue(removedCounters),
                            eventRecord(watcher, null, controllerId, removedCounters, permanent, controllerId));
                }
            }

            int timeCountersRemoved = permanent.drainTimeCountersRemovedSinceTriggerCheck();
            if (timeCountersRemoved > 0) {
                putCounterAbilities(gameData, permanent, controllerId, CounterWatch.ON_SELF_TIME_COUNTERS_REMOVED,
                        new TriggerContext.TimeCountersRemoved(permanent, timeCountersRemoved),
                        ConditionContext.forPermanent(permanent, controllerId).withEventValue(timeCountersRemoved),
                        eventRecord(permanent, null, controllerId, timeCountersRemoved, null, null));
            }

            int plusOneRemoved = permanent.drainPlusOnePlusOneCountersRemovedSinceTriggerCheck();
            // "Whenever a +1/+1 counter is removed from this": once for each counter.
            for (int i = 0; i < plusOneRemoved; i++) {
                putCounterAbilities(gameData, permanent, controllerId,
                        CounterWatch.ON_SELF_PLUS_ONE_PLUS_ONE_COUNTER_REMOVED,
                        new TriggerContext.PlusOnePlusOneCountersRemoved(permanent, plusOneRemoved),
                        ConditionContext.forPermanent(permanent, controllerId),
                        eventRecord(permanent, null, controllerId, null, null, null));
            }

            int amount = permanent.drainLoyaltyCountersRemovedSinceTriggerCheck();
            if (amount <= 0) return;

            // "Whenever one or more loyalty counters are removed from this, it deals that much damage to target
            // opponent or planeswalker" (Chandra, Fire Artisan).
            putCounterAbilities(gameData, permanent, controllerId, CounterWatch.ON_SELF_LOYALTY_COUNTERS_REMOVED,
                    new TriggerContext.LoyaltyCountersRemoved(permanent, amount),
                    ConditionContext.forPermanent(permanent, controllerId).withEventValue(amount),
                    eventRecord(permanent, null, controllerId, amount, permanent, controllerId));
        });
    }

    /**
     * Fires control-change triggers before the changed permanent moves to its new battlefield: "whenever an opponent
     * gains control of a permanent from you", where every other player is an opponent (CR 806.1).
     */
    public void checkOpponentGainsControlTriggers(GameData gameData, Permanent changedPermanent,
                                                   UUID previousControllerId, UUID newControllerId) {
        if (changedPermanent == null || previousControllerId == null || newControllerId == null
                || previousControllerId.equals(newControllerId)) {
            return;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(previousControllerId);
        if (battlefield == null) {
            return;
        }

        TriggerContext context = new TriggerContext.PermanentControlChanged(
                changedPermanent, previousControllerId, newControllerId);
        for (Permanent permanent : new ArrayList<>(battlefield)) {
            dispatchWatch(gameData, permanent, previousControllerId,
                    MiscWatch.ON_OPPONENT_GAINS_CONTROL_OF_YOUR_PERMANENT, context);
        }
    }

    public void checkTimeCounterRemovedFromExiledCardTriggers(GameData gameData, Card card,
            UUID ownerId, int remainingCounters) {
        checkTimeCounterRemovedFromExiledCardTriggers(gameData, card, ownerId, remainingCounters, null);
    }

    /** As above, naming the player who removed the counter ({@code null} for the card's owner). */
    public void checkTimeCounterRemovedFromExiledCardTriggers(GameData gameData, Card card,
            UUID ownerId, int remainingCounters, UUID removerId) {
        if (card == null || ownerId == null || remainingCounters < 0) {
            return;
        }

        CounterWatch watch = CounterWatch.ON_SELF_TIME_COUNTER_REMOVED_FROM_EXILE;
        TriggerContext event = new TriggerContext.TimeCounterRemovedFromExile(remainingCounters, removerId);
        for (TriggeredAbility ability : CounterAbilityReader.printed(card, watch)) {
            CounterTrigger trigger = CounterAbilityReader.trigger(ability, watch.trigger());
            if (trigger.qualifier() != null || ability.options().limit() != TriggerLimit.NONE) {
                throw new IllegalStateException("The collector of " + watch + " judges no filter or limit: "
                        + ability);
            }
            // "Whenever you remove a time counter from this card while it's exiled" (Watcher of Hours).
            if (trigger.event() == CounterTrigger.CounterEvent.YOU_REMOVE_TIME_COUNTER_IN_EXILE
                    && removerId != null && !removerId.equals(ownerId)) {
                continue;
            }
            // Its event condition (CR 603.2: "when the last time counter is removed") and intervening "if" (CR 603.4)
            // are read from the exiled card.
            putEventAbility(gameData, ability, card, null, ownerId, ConditionContext.forCard(card, ownerId), event,
                    TriggerRecord.NONE);
        }
    }

    public void checkSpellCastTriggers(GameData gameData, Card spellCard, UUID castingPlayerId) {
        checkSpellCastTriggers(gameData, spellCard, castingPlayerId, true);
    }

    public void checkControllerGivesGiftTriggers(GameData gameData, UUID giverId) {
        TriggerContext context = new TriggerContext.GiftGiven(giverId);
        gameData.forEachPermanent((controllerId, permanent) -> {
            if (controllerId.equals(giverId)) {
                dispatchWatch(gameData, permanent, controllerId, ActionWatch.ON_CONTROLLER_GIVES_GIFT, context);
            }
        });
    }

    public void checkSpellCopyTriggers(GameData gameData, StackEntry copiedSpell) {
        if (copiedSpell.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                || copiedSpell.getEntryType() == StackEntryType.ACTIVATED_ABILITY) {
            return;
        }

        UUID copyingPlayerId = copiedSpell.getControllerId();
        TriggerContext.SpellCopy context = new TriggerContext.SpellCopy(copiedSpell, copyingPlayerId);
        gameData.forEachPermanent((playerId, permanent) -> {
            if (playerId.equals(copyingPlayerId)) {
                dispatchWatch(gameData, permanent, playerId, SpellAbilityWatch.ON_CONTROLLER_COPIES_SPELL, context);
            } else {
                dispatchWatch(gameData, permanent, playerId, SpellAbilityWatch.ON_OPPONENT_COPIES_SPELL, context);
            }
        });
    }

    /**
     * Fires delayed "until end of turn, whenever you cast a [filter] spell, …" triggers registered
     * this turn (Mountain Titan). One trigger per registration. Registrations that require their
     * source permanent to remain on the battlefield are skipped once it has left.
     */
    private void processDelayedControllerSpellCastTriggers(GameData gameData, Card spellCard, UUID castingPlayerId) {
        if (!gameData.hasDelayedAction(DelayedControllerSpellCastTrigger.class)) {
            return;
        }
        for (DelayedControllerSpellCastTrigger delayed
                : gameData.getDelayedActions(DelayedControllerSpellCastTrigger.class)) {
            if (!delayed.controllerId().equals(castingPlayerId)) continue;
            Permanent source = delayed.sourcePermanentId() == null
                    ? null : gameQueryService.findPermanentById(gameData, delayed.sourcePermanentId());
            if (delayed.sourceMustRemainOnBattlefield() && source == null) {
                continue;
            }
            StackEntry spellEntry = gameData.stack.stream()
                    .filter(candidate -> spellCard.getId().equals(candidate.getCard().getId()))
                    .findFirst()
                    .orElse(null);
            // A spell with {X} on the stack is evaluated with its chosen X (CR 202.3e).
            Integer spellXValue = spellEntry != null && spellCard.getParsedManaCost() != null
                    && spellCard.getParsedManaCost().hasX() ? spellEntry.getXValue() : null;
            if (delayed.spellFilter() != null
                    && !predicateEvaluationService.matchesCardPredicate(
                            spellCard, delayed.spellFilter(), null, gameData, castingPlayerId,
                            null, null, spellXValue)) {
                continue;
            }
            if (delayed.stackEntryFilter() != null
                    && (spellEntry == null
                    || !targetLegalityService.matchesStackEntryPredicate(
                    gameData, spellEntry, delayed.stackEntryFilter(), castingPlayerId,
                    source != null ? source : delayed.sourcePermanentSnapshot(), null,
                    source == null ? delayed.sourcePowerAtLastKnown() : null))) {
                continue;
            }

            if (delayed.oneShot()) {
                gameData.delayedActions.remove(delayed);
            }

            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    delayed.sourceCard(),
                    delayed.controllerId(),
                    delayed.sourceCard().getName() + "'s delayed trigger",
                    new ArrayList<>(delayed.resolvedEffects()),
                    null,
                    delayed.sourcePermanentId());
            entry.setTriggeringCardId(spellCard.getId());
            entry.setTriggeringSpell(spellEntry);
            if (spellEntry != null) {
                entry.setXValue(spellEntry.getXValue());
            }
            entry.setEventValue(spellManaValue(gameData, spellCard));
            entry.setSourcePermanentSnapshot(delayed.sourcePermanentSnapshot());
            entry.setNonTargeting(true);
            gameData.handOffTriggerRecord(entry);

            if (delayed.targetFilter() != null) {
                boolean optionalTarget = delayed.resolvedEffects().stream()
                        .anyMatch(effect -> effect instanceof OptionalTargetEffect || effect.hasOptionalTarget());
                gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                        delayed.sourceCard(),
                        delayed.controllerId(),
                        new ArrayList<>(delayed.resolvedEffects()),
                        delayed.targetFilter() instanceof PlayerPredicateTargetFilter,
                        delayed.targetFilter(),
                        0,
                        delayed.sourcePermanentId(),
                        delayed.sourcePermanentSnapshot(),
                        optionalTarget,
                        null,
                        null,
                        delayed.controllerId(),
                        spellManaValue(gameData, spellCard),
                        null,
                        false));
                gameLogService.append(gameData, GameLog.cardTextCard(
                        delayed.sourceCard(), "'s delayed trigger fires for ", spellCard, "."));
                log.info("Game {} - {} delayed spell-cast trigger awaits a target for {}",
                        gameData.id, delayed.sourceCard().getName(), spellCard.getName());
                continue;
            }

            gameData.stack.add(entry);
            gameLogService.append(gameData, GameLog.cardTextCard(
                    delayed.sourceCard(), "'s delayed trigger fires for ", spellCard, "."));
            log.info("Game {} - {} delayed spell-cast trigger fires for {}",
                    gameData.id, delayed.sourceCard().getName(), spellCard.getName());
        }
    }

    public void checkSpellCastTriggers(GameData gameData, Card spellCard, UUID castingPlayerId, boolean castFromHand) {
        checkSpellCastTriggers(gameData, spellCard, castingPlayerId,
                castFromHand ? Zone.HAND : Zone.GRAVEYARD);
    }

    /**
     * Fires triggers caused by a scheme being set in motion. The scheme ability must already be
     * on the stack so effects that repeat it can snapshot the exact set-in-motion ability.
     */
    public void checkSchemeSetInMotionTriggers(GameData gameData, StackEntry schemeEntry) {
        if (schemeEntry == null || schemeEntry.getCard() == null
                || schemeEntry.getControllerId() == null
                || !schemeEntry.getCard().hasType(CardType.SCHEME)) {
            return;
        }

        UUID settingPlayerId = schemeEntry.getControllerId();
        int schemesSet = 1;
        Integer pendingReplacements = gameData.pendingNextSchemeSetInMotionReplacements.get(settingPlayerId);
        if (pendingReplacements != null) {
            if (pendingReplacements <= 1) {
                gameData.pendingNextSchemeSetInMotionReplacements.remove(settingPlayerId);
            } else {
                gameData.pendingNextSchemeSetInMotionReplacements.put(settingPlayerId,
                        pendingReplacements - 1);
            }
            schemesSet = 3;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.getOrDefault(settingPlayerId, List.of());

        for (int schemeIndex = 0; schemeIndex < schemesSet; schemeIndex++) {
            TriggerContext context = new TriggerContext.SchemeSetInMotion(schemeEntry, settingPlayerId);
            for (Permanent permanent : List.copyOf(battlefield)) {
                dispatchWatch(gameData, permanent, settingPlayerId,
                        ActionWatch.ON_CONTROLLER_SETS_SCHEME_IN_MOTION, context);
            }
            dispatchFaceUpSchemes(gameData, ActionWatch.ON_CONTROLLER_SETS_SCHEME_IN_MOTION, context,
                    settingPlayerId::equals);
        }
    }

    public void checkCrimeTriggers(GameData gameData, StackEntry stackEntry) {
        if (!gameData.isCommittedCrime(stackEntry)) return;

        UUID committingPlayerId = stackEntry.getControllerId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(committingPlayerId);
        TriggerContext ctx = new TriggerContext.Crime(committingPlayerId);
        if (battlefield != null) {
            for (Permanent permanent : List.copyOf(battlefield)) {
                dispatchWatch(gameData, permanent, committingPlayerId,
                        ActionWatch.ON_CONTROLLER_COMMITS_CRIME, ctx);
            }
        }

        gameData.forEachBattlefield((controllerId, opponentsBattlefield) -> {
            if (controllerId.equals(committingPlayerId)) return;
            for (Permanent permanent : List.copyOf(opponentsBattlefield)) {
                dispatchWatch(gameData, permanent, controllerId,
                        ActionWatch.ON_OPPONENT_COMMITS_CRIME, ctx);
            }
        });

        List<Card> graveyard = gameData.playerGraveyards.get(committingPlayerId);
        if (graveyard != null) {
            for (Card card : List.copyOf(graveyard)) {
                for (List<CardEffect> effects : graveyardActionAbilities(gameData, card,
                        ActionWatch.GRAVEYARD_ON_CONTROLLER_COMMITS_CRIME)) {
                    queueGraveyardCrimeTrigger(gameData, card, committingPlayerId, effects);
                }
            }
        }

    }

    /**
     * Queues the "whenever you fully unlock a Room" abilities of {@code card} in its owner's graveyard (Fear of
     * Infinity), each ability as one object of its effects (CR 603.3); their collector judges no door, first time or
     * limit.
     */
    private void queueGraveyardEerieTriggers(GameData gameData, UUID controllerId, Card card) {
        // Each ability whole (CR 603.3), a "you may" like any other: it is asked as the ability resolves (CR 603.5).
        for (List<CardEffect> effects : MiscAbilityReader.printedEffectsByAbility(card,
                MiscWatch.GRAVEYARD_ON_ALLY_ROOM_FULLY_UNLOCKED)) {
            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    card,
                    controllerId,
                    card.getName() + "'s ability",
                    new ArrayList<>(effects)
            ));
            gameLogService.append(gameData, GameLog.abilityTriggers(card));
            log.info("Game {} - {} graveyard eerie trigger queued", gameData.id, card.getName());
        }
    }

    private void queueGraveyardCrimeTrigger(GameData gameData, Card card, UUID controllerId,
                                             List<CardEffect> effects) {
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                card,
                controllerId,
                card.getName() + "'s ability",
                new ArrayList<>(effects));
        entry.setSourceGraveyardEntryVersion(gameData.graveyardEntryVersion(card.getId()));
        gameData.enqueueTrigger(entry);
        gameLogService.append(gameData, GameLog.abilityTriggers(card));
    }

    /**
     * Zone-carrying form: {@code castZone} is the zone the spell was cast from, so triggers that
     * care about a specific origin (cast from a graveyard, cast from the top of a library) can tell
     * them apart instead of sharing one "not from hand" flag.
     */
    public void checkSpellCastTriggers(GameData gameData, Card spellCard, UUID castingPlayerId, Zone castZone) {
        checkSpellCastTriggers(gameData, spellCard, castingPlayerId, castZone, null);
    }

    public void checkSpellCastTriggers(GameData gameData, Card spellCard, UUID castingPlayerId,
                                       Zone castZone, UUID exiledSourcePermanentId) {
        checkSpellCastTriggers(gameData, spellCard, castingPlayerId, castZone, exiledSourcePermanentId, false);
    }

    /**
     * Emits the cosmetic {@link VisualCueType#SPELL_CAST} cue for a spell that was just put on the
     * stack. Targets are public once chosen, so the cue names the first player, the first permanent
     * and the first spell the spell targets; a face-down spell keeps its name and colors hidden.
     */
    private void emitSpellCastCue(GameData gameData, Card spellCard) {
        VisualCueService cues = visualCueService == null ? null : visualCueService.getIfAvailable();
        if (cues == null) {
            return;
        }
        StackEntry spell = null;
        for (StackEntry entry : gameData.stack) {
            if (entry.getCard() != null && entry.getCard().getId().equals(spellCard.getId()) && !entry.isCopy()) {
                spell = entry;
            }
        }
        if (spell == null) {
            return;
        }
        List<UUID> targetIds = new ArrayList<>();
        if (spell.getTargetId() != null) {
            targetIds.add(spell.getTargetId());
        }
        if (spell.getTargetIds() != null) {
            targetIds.addAll(spell.getTargetIds());
        }
        UUID targetPlayerId = null;
        UUID targetPermanentId = null;
        UUID targetCardId = null;
        for (UUID targetId : targetIds) {
            if (gameData.playerIds.contains(targetId)) {
                targetPlayerId = targetPlayerId != null ? targetPlayerId : targetId;
            } else if (gameQueryService.findPermanentById(gameData, targetId) != null) {
                targetPermanentId = targetPermanentId != null ? targetPermanentId : targetId;
            } else if (targetCardId == null) {
                targetCardId = targetedSpellCardId(gameData, spell, targetId);
            }
        }
        cues.emit(gameData, VisualCueType.SPELL_CAST, VisualCueService.Source.of(spell),
                targetPermanentId, targetPlayerId, targetCardId, null, GameEventAudience.allPlayers());
    }

    private static UUID targetedSpellCardId(GameData gameData, StackEntry spell, UUID targetId) {
        for (StackEntry entry : gameData.stack) {
            if (entry != spell && entry.getCard() != null && entry.getTargetableId().equals(targetId)
                    && entry.getEntryType() != StackEntryType.ACTIVATED_ABILITY
                    && entry.getEntryType() != StackEntryType.TRIGGERED_ABILITY) {
                return entry.getCard().getId();
            }
        }
        return null;
    }

    public void checkSpellCastTriggers(GameData gameData, Card spellCard, UUID castingPlayerId,
                                       Zone castZone, UUID exiledSourcePermanentId,
                                       boolean castFaceDown) {
        emitSpellCastCue(gameData, spellCard);
        gameData.initializeCardIntensity(spellCard);
        for (StackEntry spell : gameData.stack) {
            if (spell.getCard() != null && spell.getCard().getId().equals(spellCard.getId()) && !spell.isCopy()) {
                spell.setAdditionalEnterCounters(gameData.spellAdditionalEnterCounters
                        .getOrDefault(spellCard.getId(), 0));
                // The mana spent to cast it, and the part of it creatures produced, as the spell becomes cast (CR
                // 601.2i): the game's records are cleared once its cast triggers are collected, so the cast triggers
                // read them from the spell's entry (or their snapshot of it) as they resolve.
                spell.setManaSpentToCast(gameData.getSpellCastManaSpent(spellCard.getId()));
                spell.setCreatureManaSpentToCast(gameData.getSpellCastCreatureManaSpent(spellCard.getId()));
                spell.setManaSpentByColorToCast(gameData.spellCastManaSpentByColor.get(spellCard.getId()));
            }
        }
        gameData.recordSpellCastFromZone(castingPlayerId, castZone, spellCard);
        if (castZone == Zone.EXILE && !spellCard.isToken()) {
            gameData.recordCardPlayedFromExile(castingPlayerId);
        }
        if (castZone == Zone.HAND) {
            gameData.recordSpellCastFromHand(spellCard);
        }
        var ctx = new TriggerContext.SpellCast(spellCard, castingPlayerId, castZone,
                exiledSourcePermanentId);
        collectCreatureSpellBoonTriggers(gameData, spellCard, castingPlayerId, castFaceDown);
        if (!castFaceDown && !spellCard.hasType(CardType.CREATURE)) {
            for (Permanent source : gameData.playerBattlefields.getOrDefault(castingPlayerId, List.of())) {
                int prowessInstances = gameQueryService.triggerKeywordInstances(gameData, source, Keyword.PROWESS);
                if (prowessInstances == 0) continue;
                int triggerCount = prowessInstances * (1 + gameQueryService.countAdditionalTriggeredAbilityTriggers(
                        gameData, castingPlayerId, source));
                for (int triggerIndex = 0; triggerIndex < triggerCount; triggerIndex++) {
                    StackEntry prowess = new StackEntry(StackEntryType.TRIGGERED_ABILITY, source.getCard(),
                        castingPlayerId, source.getCard().getName() + "'s prowess ability",
                        List.of(new com.github.laxika.magicalvibes.model.effect.BoostSelfEffect(1, 1)),
                        null, source.getId());
                    prowess.setSourcePermanentSnapshot(new Permanent(source));
                    gameData.enqueueTrigger(prowess);
                }
            }
        }
        if (castZone != Zone.HAND) {
            gameData.recordSpellCastFromOutsideHand(castingPlayerId);
            gameData.playersWhoPlayedOrCastFromOutsideHandThisTurn.add(castingPlayerId);
        }

        // "The first spell you cast from anywhere other than your hand each turn has demonstrate."
        // The trigger entry must retain the just-cast spell ID because DemonstrateEffect copies
        // that spell when the optional ability resolves.
        if (castZone != Zone.HAND
                && gameData.getSpellsCastFromOutsideHandThisTurnCount(castingPlayerId) == 1) {
            List<Permanent> casterBattlefield = gameData.playerBattlefields.get(castingPlayerId);
            if (casterBattlefield != null) {
                for (Permanent perm : new ArrayList<>(casterBattlefield)) {
                    if (perm.isLosesAllAbilitiesUntilEndOfTurn()) continue;
                    List<CardEffect> grantEffects = castTriggerGrants(perm, SpellCastTriggerGrant.DEMONSTRATE_TO_FIRST_SPELL_FROM_OUTSIDE_HAND);
                    if (grantEffects.isEmpty()) continue;

                    // Each granted instance is a triggered ability of its own, one stack object each (CR 603.3).
                    for (CardEffect grantEffect : grantEffects) {
                        StackEntry entry = new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                castingPlayerId,
                                perm.getCard().getName() + "'s ability",
                                new ArrayList<>(List.of(grantEffect)),
                                null,
                                perm.getId());
                        entry.setTriggeringCardId(spellCard.getId());
                        entry.setNonTargeting(true);
                        gameData.stack.add(entry);
                    }
                    gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                }
            }
        }

        if (castZone == Zone.EXILE) {
            gameData.expireFloatingEffects(fe ->
                    fe.duration() == EffectDuration.UNTIL_SOURCE_CARD_CAST_FROM_EXILE
                            && fe.effect() instanceof GrantActivatedAbilityEffect grant
                            && spellCard.getId().equals(grant.expirationCardId()));
        }

        gameData.expireFloatingEffects(fe ->
                (fe.duration() == EffectDuration.UNTIL_MATCHING_SPELL_CAST
                        || fe.duration() == EffectDuration.UNTIL_TARGET_PLAYER_MATCHING_SPELL_CAST)
                        && fe.effect() instanceof NextMatchingSpellCostEffect next
                        && next.appliesToPlayer(castingPlayerId, fe.controllerId())
                        && next.appliesToFaceDownCast(castFaceDown)
                        && predicateEvaluationService.matchesCardPredicate(
                                spellCard, next.predicate(), null, gameData, castingPlayerId));

        if (spellCard.hasType(CardType.CREATURE)) {
            gameData.expireFloatingEffectsOnCreatureSpellCast();
        }

        // Opening hand reveal delayed triggers (Chancellor cycle)
        if (!gameData.openingHandRevealTriggers.isEmpty()
                && !gameData.playersWhoCastFirstSpellInGame.contains(castingPlayerId)) {
            gameData.playersWhoCastFirstSpellInGame.add(castingPlayerId);
            StackEntry firstSpell = findStackEntryByCardId(gameData, spellCard.getId());
            for (OpeningHandRevealTrigger trigger : gameData.openingHandRevealTriggers) {
                if (trigger.revealingPlayerId().equals(castingPlayerId)) continue;
                // "Counter that spell unless that player pays": the spell is the entry's triggering spell, which the
                // ability acts on without targeting it.
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        trigger.sourceCard(),
                        trigger.revealingPlayerId(),
                        trigger.sourceCard().getName() + "'s ability",
                        new ArrayList<>(List.of(trigger.effect())),
                        spellCard.getId(),
                        Zone.STACK
                );
                entry.setTriggeringCardId(spellCard.getId());
                entry.setTriggeringSpell(firstSpell);
                entry.setTriggeringPlayerId(castingPlayerId);
                entry.setNonTargeting(true);
                gameData.stack.add(entry);
            }
        }

        // ON_ANY_PLAYER_CASTS_SPELL
        gameData.forEachPermanent((playerId, perm) -> {
            dispatchWatch(gameData, perm, playerId, SpellAbilityWatch.ON_ANY_PLAYER_CASTS_SPELL, ctx);
        });
        dispatchFaceUpSchemes(gameData, SpellAbilityWatch.ON_ANY_PLAYER_CASTS_SPELL, ctx, ownerId -> true);

        collectTemporaryGlobalSpellCastTriggers(gameData, spellCard, castingPlayerId);

        if (gameData.planechase != null && gameData.planechase.controllerId != null) {
            for (PlanarObject object : List.copyOf(gameData.planechase.faceUp)) {
                dispatchCardSpellAbilities(gameData, object.getCard(), gameData.planechase.controllerId,
                        SpellAbilityWatch.ON_ANY_PLAYER_CASTS_SPELL, ctx, object);
            }
        }

        // ON_CONTROLLER_CASTS_SPELL (only controller's own spells)
        gameData.forEachPermanent((playerId, perm) -> {
            if (!playerId.equals(castingPlayerId)) return;
            dispatchWatch(gameData, perm, playerId, SpellAbilityWatch.ON_CONTROLLER_CASTS_SPELL, ctx);
        });
        dispatchDepartedManaSourceSpellCastTriggers(gameData, spellCard, castingPlayerId, ctx);
        dispatchFaceUpSchemes(gameData, SpellAbilityWatch.ON_CONTROLLER_CASTS_SPELL, ctx, castingPlayerId::equals);

        if (gameData.planechase != null && castingPlayerId.equals(gameData.planechase.controllerId)) {
            for (PlanarObject object : List.copyOf(gameData.planechase.faceUp)) {
                dispatchCardSpellAbilities(gameData, object.getCard(), castingPlayerId,
                        SpellAbilityWatch.ON_CONTROLLER_CASTS_SPELL, ctx, object);
            }
        }

        collectTemporaryControllerSpellCastTriggers(gameData, spellCard, castingPlayerId,
                castZone, exiledSourcePermanentId);

        processDelayedControllerSpellCastTriggers(gameData, spellCard, castingPlayerId);

        // ON_OPPONENT_CASTS_SPELL (only opponents' permanents)
        gameData.forEachPermanent((playerId, perm) -> {
            if (playerId.equals(castingPlayerId)) return;
            dispatchWatch(gameData, perm, playerId, SpellAbilityWatch.ON_OPPONENT_CASTS_SPELL, ctx);
        });
        dispatchFaceUpSchemes(gameData, SpellAbilityWatch.ON_OPPONENT_CASTS_SPELL, ctx,
                ownerId -> !ownerId.equals(castingPlayerId));

        dispatchSuspendedExiledCardSpellCastTriggers(gameData, castingPlayerId, ctx);

        // Increment keyword (CR keyword): "Whenever you cast a spell, if the amount of mana you spent
        // is greater than this creature's power or toughness, put a +1/+1 counter on it." Driven by the
        // Scryfall-loaded keyword (like Undying) rather than a per-card effect. Read before mana-spent
        // is cleared below.
        collectIncrementTriggers(gameData, spellCard, castingPlayerId);

        // Emblem spell cast triggers: the emblems' cast abilities (Ajani, Jace, Telepath Unbound, Ral, Jace, Unraveler
        // of Secrets, Garruk, Caller of Beasts, Venser, ...). Mana-spent readers must run before clearSpellCastManaSpent.
        for (Emblem emblem : gameData.emblems) {
            collectEmblemCastAbilities(gameData, emblem, ctx);
        }

        int pathOfAncestryScries = gameData.getSpellCastPathOfAncestryManaSpent(spellCard.getId());
        gameData.clearSpellCastPathOfAncestryManaSpent(spellCard.getId());
        for (int i = 0; i < pathOfAncestryScries; i++) {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    spellCard,
                    castingPlayerId,
                    "Path of Ancestry's ability",
                    new ArrayList<>(List.of(new ScryEffect(1)))
            );
            entry.setTriggeringCardId(spellCard.getId());
            entry.setNonTargeting(true);
            gameData.enqueueTrigger(entry);
        }
        if (pathOfAncestryScries > 0) {
            gameLogService.append(gameData, GameLog.text("Path of Ancestry triggers."));
        }

        if (gameData.hasPendingInteraction(PermanentChoiceContext.EmblemTriggerTarget.class)) {
            triggeredAbilityQueueService.processNextEmblemTriggerTarget(gameData);
            if (gameData.interaction.isAwaitingInput()) {
                return;
            }
        }

        // The spell as it is on the stack, its mana spent recorded (above): the graveyard and hand abilities below judge
        // their intervening "if" on it and keep it as their triggering spell for the check as they resolve.
        StackEntry castSpellEntry = gameData.stack.stream()
                .filter(entry -> entry.getTargetableId().equals(spellCard.getId()))
                .findFirst()
                .orElse(null);
        boolean castUsingTreasureMana = gameData.spellCastUsedTreasureMana(spellCard.getId());
        gameData.clearSpellCastManaSpent(spellCard.getId());
        gameData.clearSpellCastManaSources(spellCard.getId());
        gameData.clearSpellCastTreasureMana(spellCard.getId());
        gameData.clearSpellCastCreatureManaSpent(spellCard.getId());

        // "Whenever you cast a spell" abilities of cards in the caster's graveyard (Lingering Phantom: "Whenever you
        // cast a historic spell, you may pay {B}. If you do, return this card from your graveyard to your hand.").
        List<Card> castingPlayerGraveyard = gameData.playerGraveyards.get(castingPlayerId);
        if (castingPlayerGraveyard != null) {
            for (Card card : new ArrayList<>(castingPlayerGraveyard)) {
                for (TriggeredAbility ability : gameQueryService.getEffectiveGraveyardTriggeredAbilities(gameData, card)) {
                    SpellFilter filter = watchedSpellFilter(ability,
                            SpellAbilityWatch.GRAVEYARD_ON_CONTROLLER_CASTS_SPELL, false);
                    if (filter == null) continue;
                    requireOnlyInterveningIf(ability, card);
                    if (!predicateEvaluationService.matchesCardPredicate(spellCard, filter.spell(), null)) continue;
                    if (filter.stackEntry() != null) {
                        StackEntry spellEntry = gameData.stack.stream()
                                .filter(entry -> entry.getTargetableId().equals(spellCard.getId()))
                                .findFirst()
                                .orElse(null);
                        if (spellEntry == null || !targetLegalityService.matchesStackEntryPredicate(
                                gameData, spellEntry, filter.stackEntry(), castingPlayerId)) {
                            continue;
                        }
                    }
                    if (filter.nthThisTurn() > 0 && !isNthMatchingSpell(gameData, filter, castingPlayerId)) {
                        continue;
                    }
                    // The intervening "if" (CR 603.4: "if five or more mana was spent to cast it", Covercast) is judged
                    // by the stack door on the spell, which the entry keeps as its triggering spell for the check as it
                    // resolves. "If this card is in your graveyard" reads this graveyard incarnation from the entry
                    // (CR 400.7), so the ability does nothing if the card has left and returned to the graveyard.
                    long sourceEntryVersion = gameData.graveyardEntryVersion(card.getId());
                    triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(ability, card, null,
                            castingPlayerId, card.getName() + "'s ability",
                            ConditionContext.forCard(card, castingPlayerId).withTriggeringSpell(castSpellEntry),
                            entry -> {
                                entry.setTriggeringCardId(spellCard.getId());
                                entry.setSourceGraveyardEntryVersion(sourceEntryVersion);
                                entry.setTriggeringSpell(castSpellEntry);
                            }));
                }
            }
        }

        // HAND_ON_CONTROLLER_CASTS_SPELL — hand-resident spell-cast triggers (Covercast: "Whenever you cast
        // another instant or sorcery spell, if five or more mana was spent to cast it, this card intensifies.").
        List<Card> castingPlayerHand = gameData.playerHands.get(castingPlayerId);
        if (castingPlayerHand != null) {
            for (Card card : new ArrayList<>(castingPlayerHand)) {
                if (card.getId().equals(spellCard.getId())) continue;
                for (TriggeredAbility ability : card.getTriggeredAbilities()) {
                    SpellFilter filter = watchedSpellFilter(ability, SpellAbilityWatch.HAND_ON_CONTROLLER_CASTS_SPELL,
                            false);
                    if (filter == null) continue;
                    requireOnlyInterveningIf(ability, card);
                    if (!predicateEvaluationService.matchesCardPredicate(spellCard, filter.spell(), null)) {
                        continue;
                    }
                    // The intervening "if" (Covercast's mana spent) is judged by the stack door now and as it
                    // resolves, on the entry's triggering spell.
                    triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(ability, card, null,
                            castingPlayerId, card.getName() + "'s ability",
                            ConditionContext.forCard(card, castingPlayerId).withTriggeringSpell(castSpellEntry),
                            entry -> {
                                entry.setTriggeringCardId(spellCard.getId());
                                entry.setTriggeringSpell(castSpellEntry);
                            }));
                }
            }
        }

        // EXILE_ON_CONTROLLER_CASTS_SPELL — face-up exile-resident spell-cast triggers.
        for (ExiledCardEntry exiledEntry : new ArrayList<>(gameData.exiledCards)) {
            if (exiledEntry.faceDown() || !castingPlayerId.equals(exiledEntry.ownerId())) {
                continue;
            }
            dispatchCardSpellAbilities(gameData, exiledEntry.card(), castingPlayerId,
                    SpellAbilityWatch.EXILE_ON_CONTROLLER_CASTS_SPELL, ctx, null);
        }

        // Abaddon the Despoiler: during your turn, hand-cast spells with mana value at most the
        // total life lost by opponents this turn have cascade. The spell's effective mana value is
        // snapshotted so X spells use their announced value when the cascade trigger resolves.
        int opponentsLifeLostThisTurn = gameData.orderedPlayerIds.stream()
                .filter(playerId -> !playerId.equals(castingPlayerId))
                .mapToInt(playerId -> gameData.lifeLostThisTurn.getOrDefault(playerId, 0))
                .sum();
        if (castZone == Zone.HAND
                && castingPlayerId.equals(gameData.activePlayerId)
                && spellManaValue(gameData, spellCard) <= opponentsLifeLostThisTurn) {
            List<Permanent> casterBattlefield = gameData.playerBattlefields.get(castingPlayerId);
            if (casterBattlefield != null) {
                for (Permanent perm : new ArrayList<>(casterBattlefield)) {
                    if (perm.isLosesAllAbilitiesUntilEndOfTurn()) continue;
                    GameQueryService.StaticBonus staticBonus = gameQueryService.computeStaticBonus(gameData, perm);
                    if (staticBonus.losesAllAbilities() || staticBonus.losesAllNonManaAbilities()
                            || gameQueryService.hasLostPrintedAbilities(gameData, perm)) continue;
                    List<CardEffect> grantEffects = castTriggerGrants(perm, SpellCastTriggerGrant.CASCADE_TO_HAND_SPELLS_WITH_MANA_VALUE_AT_MOST_OPPONENTS_LIFE_LOST);
                    if (grantEffects.isEmpty()) continue;

                    // Each granted instance triggers separately (CR 702.85c).
                    for (CardEffect grantEffect : grantEffects) {
                        StackEntry cascadeTrigger = new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                spellCard,
                                castingPlayerId,
                                spellCard.getName() + "'s ability",
                                new ArrayList<>(List.of(grantEffect)));
                        cascadeTrigger.setTriggeringCardId(spellCard.getId());
                        cascadeTrigger.setEventValue(spellManaValue(gameData, spellCard));
                        gameData.stack.add(cascadeTrigger);
                    }
                    gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                    log.info("Game {} - {} grants cascade to {} for {} (opponents lost {} life)",
                            gameData.id, perm.getCard().getName(), spellCard.getName(), castingPlayerId,
                            opponentsLifeLostThisTurn);
                }
            }
        }

        // Sloppity Bilepiper: the next creature spell this turn has cascade. Each granted instance of
        // cascade is its own triggered ability (CR 702.85c), so each goes on the stack separately. The
        // spell's effective mana value is snapshotted so the existing CascadeEffect handler uses it.
        int pendingCreatureSpellCascades = gameData.consumeNextCreatureSpellCascade(
                castingPlayerId, spellCard);
        for (int i = 0; i < pendingCreatureSpellCascades; i++) {
            StackEntry cascadeTrigger = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    spellCard,
                    castingPlayerId,
                    spellCard.getName() + "'s ability",
                    new ArrayList<>(List.of(new CascadeEffect())));
            cascadeTrigger.setTriggeringCardId(spellCard.getId());
            cascadeTrigger.setEventValue(spellManaValue(gameData, spellCard));
            gameData.stack.add(cascadeTrigger);
            gameLogService.append(gameData, GameLog.cardThen(
                    spellCard, " has cascade from Sloppity Bilepiper."));
        }
        if (pendingCreatureSpellCascades > 0) {
            log.info("Game {} - queued {} delayed cascade grant(s) for {}",
                    gameData.id, pendingCreatureSpellCascades, spellCard.getName());
        }

        // "When you next cast a creature spell this turn, ..." delayed triggers (Yuna, Grand Summoner):
        // each goes on the stack above the spell as its own triggered ability.
        for (DelayedSpellCastTrigger delayed : gameData.consumeNextCreatureSpellDelayedTriggers(
                castingPlayerId, spellCard)) {
            StackEntry delayedTrigger = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    delayed.source(),
                    castingPlayerId,
                    delayed.source().getName() + "'s ability",
                    new ArrayList<>(List.of(delayed.effect())));
            delayedTrigger.setTriggeringCardId(spellCard.getId());
            gameData.stack.add(delayedTrigger);
            gameLogService.append(gameData, GameLog.abilityTriggers(delayed.source()));
            log.info("Game {} - delayed cast trigger of {} queued for {}",
                    gameData.id, delayed.source().getName(), spellCard.getName());
        }

        // COMMAND_ZONE_ON_CONTROLLER_CASTS_SPELL — Eminence and similar command-zone spell-cast triggers
        List<Card> castingPlayerCommandZone = gameData.playerCommandZones.get(castingPlayerId);
        if (castingPlayerCommandZone != null) {
            for (Card card : new ArrayList<>(castingPlayerCommandZone)) {
                for (TriggeredAbility ability : card.getTriggeredAbilities()) {
                    SpellFilter filter = watchedSpellFilter(ability,
                            SpellAbilityWatch.COMMAND_ZONE_ON_CONTROLLER_CASTS_SPELL, false);
                    if (filter == null) continue;
                    requireNoOptions(ability, card);
                    if (!predicateEvaluationService.matchesCardPredicate(spellCard, filter.spell(), null)) {
                        continue;
                    }
                    triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(ability, card, null,
                            castingPlayerId, card.getName() + "'s ability",
                            ConditionContext.forCard(card, castingPlayerId), TriggerRecord.NONE));
                }
            }
        }

        ManaPool spellPaymentPool = gameData.playerManaPools.get(castingPlayerId);
        if (spellPaymentPool != null) {
            boolean instantOrSorcery = spellCard.hasType(CardType.INSTANT) || spellCard.hasType(CardType.SORCERY);
            boolean red = spellCard.getColors() != null && spellCard.getColors().contains(CardColor.RED);
            int wellspringCopies = spellPaymentPool.takeSpentCopyGrantingMana(ManaCopyRider.INSTANT_OR_SORCERY_SPELL);
            int gogglesCopies = spellPaymentPool.takeSpentCopyGrantingMana(ManaCopyRider.RED_INSTANT_OR_SORCERY_SPELL);
            if (instantOrSorcery) {
                queueManaLinkedSpellCopyTriggers(gameData, spellCard, castingPlayerId, wellspringCopies);
            }
            if (instantOrSorcery && red) {
                queueManaLinkedSpellCopyTriggers(gameData, spellCard, castingPlayerId, gogglesCopies);
            }
        }

        // "When you next cast an instant or sorcery spell this turn, copy that spell"
        // (e.g. Chandra, the Firebrand −2). Same one-shot shape as Primal Wellspring's trigger, but
        // tracked in the turn-scoped counter so it survives mana drain.
        Integer pendingTurnCopies = gameData.pendingNextInstantSorceryCopyThisTurnCount.get(castingPlayerId);
        List<Integer> pendingMaxManaValues =
                gameData.pendingNextInstantSorceryCopyThisTurnMaxManaValues.get(castingPlayerId);
        List<DynamicAmount> pendingDynamicCounts =
                gameData.pendingNextInstantSorceryCopyThisTurnDynamicCounts.get(castingPlayerId);
        if ((pendingTurnCopies != null && pendingTurnCopies > 0
                || pendingMaxManaValues != null && !pendingMaxManaValues.isEmpty()
                || pendingDynamicCounts != null && !pendingDynamicCounts.isEmpty())
                && (spellCard.hasType(CardType.INSTANT) || spellCard.hasType(CardType.SORCERY))) {
            StackEntry spellEntry = null;
            for (StackEntry se : gameData.stack) {
                if (se.getTargetableId().equals(spellCard.getId())) {
                    spellEntry = se;
                    break;
                }
            }
            if (spellEntry != null) {
                int limitedCopies = 0;
                if (pendingMaxManaValues != null) {
                    int manaValue = spellCard.getManaValue();
                    if (spellCard.getParsedManaCost() != null) {
                        manaValue += spellEntry.getXValue() * spellCard.getParsedManaCost().getXSymbolCount();
                    }
                    for (int i = pendingMaxManaValues.size() - 1; i >= 0; i--) {
                        if (manaValue <= pendingMaxManaValues.get(i)) {
                            pendingMaxManaValues.remove(i);
                            limitedCopies++;
                        }
                    }
                    if (pendingMaxManaValues.isEmpty()) {
                        gameData.pendingNextInstantSorceryCopyThisTurnMaxManaValues.remove(castingPlayerId);
                    }
                }
                int unrestrictedCopies = pendingTurnCopies == null ? 0 : pendingTurnCopies;
                if (pendingDynamicCounts != null) {
                    List<Card> dynamicSources = gameData.pendingNextInstantSorceryCopyThisTurnDynamicSources
                            .getOrDefault(castingPlayerId, List.of());
                    for (int dynamicIndex = 0; dynamicIndex < pendingDynamicCounts.size(); dynamicIndex++) {
                        DynamicAmount dynamicCount = pendingDynamicCounts.get(dynamicIndex);
                        Card triggerSource = dynamicIndex < dynamicSources.size()
                                && dynamicSources.get(dynamicIndex) != null
                                ? dynamicSources.get(dynamicIndex) : spellCard;
                        // The delayed trigger always triggers; how many copies it makes is counted as it resolves.
                        gameData.stack.add(new StackEntry(StackEntryType.TRIGGERED_ABILITY,
                                triggerSource, castingPlayerId, "Copy " + spellCard.getName(),
                                List.of(new CopyControllerCastSpellEffect(
                                        new StackEntry(spellEntry), castingPlayerId, dynamicCount))));
                    }
                    gameData.pendingNextInstantSorceryCopyThisTurnDynamicCounts.remove(castingPlayerId);
                    gameData.pendingNextInstantSorceryCopyThisTurnDynamicSources.remove(castingPlayerId);
                }
                int totalCopies = unrestrictedCopies + limitedCopies;
                if (totalCopies > 0) {
                    StackEntry snapshot = new StackEntry(spellEntry);
                    Card countSource = unrestrictedCopies > 0
                            ? gameData.pendingNextInstantSorceryCopyThisTurnCountSource
                            .getOrDefault(castingPlayerId, spellCard) : spellCard;
                    // Each one-copy delayed trigger (Teach by Example) triggers separately (CR 603.7); the
                    // remaining unrestricted copies belong to a single multi-copy trigger ("copy it X times").
                    int separateTriggers = Math.min(unrestrictedCopies, Math.max(0,
                            gameData.pendingNextInstantSorceryCopyThisTurnSingleTriggerCount
                                    .getOrDefault(castingPlayerId, 0)));
                    int multiCopies = unrestrictedCopies - separateTriggers;
                    if (multiCopies > 0) {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY, countSource, castingPlayerId,
                                "Copy " + spellCard.getName(),
                                List.of(new CopyControllerCastSpellEffect(snapshot, castingPlayerId, multiCopies))));
                    }
                    for (int i = 0; i < separateTriggers; i++) {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY, countSource, castingPlayerId,
                                "Copy " + spellCard.getName(),
                                List.of(new CopyControllerCastSpellEffect(snapshot, castingPlayerId, 1))));
                    }
                    // The mana-value-limited copies the spell matches are one entry: a kicked Sea Gate
                    // Stormcaller's "copy that spell twice instead" is two limits of one delayed ability, and the
                    // list keeps no resolution to tell them from the limits of two Stormcallers.
                    if (limitedCopies > 0) {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY, spellCard, castingPlayerId,
                                "Copy " + spellCard.getName(),
                                List.of(new CopyControllerCastSpellEffect(snapshot, castingPlayerId, limitedCopies))));
                    }
                    if (unrestrictedCopies > 0) {
                        gameData.pendingNextInstantSorceryCopyThisTurnCount.remove(castingPlayerId);
                        gameData.pendingNextInstantSorceryCopyThisTurnSingleTriggerCount.remove(castingPlayerId);
                        gameData.pendingNextInstantSorceryCopyThisTurnCountSource.remove(castingPlayerId);
                        if (gameData.pendingNextSpellOrLoyaltyCopyLinkedThisTurn.remove(castingPlayerId)) {
                            gameData.pendingNextLoyaltyAbilityCopyThisTurnCount.remove(castingPlayerId);
                            gameData.pendingNextLoyaltyAbilityCopySourcesThisTurn.remove(castingPlayerId);
                        }
                    }
                    gameLogService.append(gameData, GameLog.cardThen(spellCard, " is copied."));
                    log.info("Game {} - {} delayed spell-copy trigger(s) queued for {}",
                            gameData.id, totalCopies, spellCard.getName());
                }
            }
        }

        // "The next instant or sorcery spell you cast this turn has Storm" (Storm, Force of Nature).
        Integer pendingStorms = gameData.pendingNextInstantSorceryStormThisTurnCount.get(castingPlayerId);
        if (pendingStorms != null && pendingStorms > 0
                && (spellCard.hasType(CardType.INSTANT) || spellCard.hasType(CardType.SORCERY))) {
            StackEntry spellEntry = null;
            for (StackEntry se : gameData.stack) {
                if (se.getTargetableId().equals(spellCard.getId())) {
                    spellEntry = se;
                    break;
                }
            }
            if (spellEntry != null) {
                for (int i = 0; i < pendingStorms; i++) {
                    queueGrantedStorm(gameData, spellCard, castingPlayerId, spellCard.getName() + "'s ability",
                            spellEntry, null, CopyTriggeringSpellEffect.storm());
                }
                gameData.pendingNextInstantSorceryStormThisTurnCount.remove(castingPlayerId);
                log.info("Game {} - {} delayed Storm grant(s) applied to {}",
                        gameData.id, pendingStorms, spellCard.getName());
            }
        }

        Integer pendingSpellCopies = gameData.pendingNextSpellCopyThisTurnCount.get(castingPlayerId);
        if (pendingSpellCopies != null && pendingSpellCopies > 0) {
            StackEntry spellEntry = null;
            for (StackEntry se : gameData.stack) {
                if (se.getTargetableId().equals(spellCard.getId())) {
                    spellEntry = se;
                    break;
                }
            }
            if (spellEntry != null) {
                boolean permanentSpell = switch (spellEntry.getEntryType()) {
                    case CREATURE_SPELL, ENCHANTMENT_SPELL, ARTIFACT_SPELL, PLANESWALKER_SPELL, BATTLE_SPELL -> true;
                    default -> false;
                };
                StackEntry snapshot = new StackEntry(spellEntry);
                // Each "copy the next spell you cast this turn" is a delayed triggered ability of the resolution
                // that created it, so each is a stack object of its own (CR 603.7a).
                for (int i = 0; i < pendingSpellCopies; i++) {
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            spellCard,
                            castingPlayerId,
                            "Copy " + spellCard.getName(),
                            new ArrayList<>(List.of(new CopyControllerCastSpellEffect(
                                    snapshot, castingPlayerId, Set.of(), Set.of(), permanentSpell, true)))
                    ));
                }
                gameData.pendingNextSpellCopyThisTurnCount.remove(castingPlayerId);
                gameLogService.append(gameData, GameLog.cardThen(spellCard, " is copied."));
                log.info("Game {} - {} all-spell copy trigger(s) queued for {}",
                        gameData.id, pendingSpellCopies, spellCard.getName());
            }
        }

        List<CopyNextSpellCastThisTurnEffect> pendingFilteredCopies =
                gameData.pendingNextFilteredSpellCopiesThisTurn.get(castingPlayerId);
        if (pendingFilteredCopies != null && !pendingFilteredCopies.isEmpty()) {
            StackEntry spellEntry = null;
            for (StackEntry se : gameData.stack) {
                if (se.getTargetableId().equals(spellCard.getId())) {
                    spellEntry = se;
                    break;
                }
            }
            if (spellEntry != null) {
                Card spellOnStack = SpellCastTriggerCollectorService.spellOnStackWithChosenX(
                        spellCard, spellEntry.getXValue());
                List<CopyNextSpellCastThisTurnEffect> matchingCopies = new ArrayList<>();
                List<CopyNextSpellCastThisTurnEffect> remainingCopies = new ArrayList<>();
                for (CopyNextSpellCastThisTurnEffect copyEffect : pendingFilteredCopies) {
                    // "{X} in its mana cost" is read off the printed cost, which the chosen X is substituted into
                    // on the stack.
                    Card filterSubject = copyEffect.spellFilter() instanceof CardHasXInManaCostPredicate
                            ? spellCard : spellOnStack;
                    if (copyEffect.spellFilter() == null
                            || predicateEvaluationService.matchesCardPredicate(
                            filterSubject, copyEffect.spellFilter(), null, gameData, castingPlayerId)) {
                        matchingCopies.add(copyEffect);
                    } else {
                        remainingCopies.add(copyEffect);
                    }
                }
                if (!matchingCopies.isEmpty()) {
                    boolean permanentSpell = switch (spellEntry.getEntryType()) {
                        case CREATURE_SPELL, ENCHANTMENT_SPELL, ARTIFACT_SPELL, PLANESWALKER_SPELL, BATTLE_SPELL -> true;
                        default -> false;
                    };
                    StackEntry snapshot = new StackEntry(spellEntry);
                    // Each "when you next cast a ... spell this turn, copy it" is a delayed triggered ability of
                    // the resolution that created it, so each is a stack object of its own (CR 603.7a).
                    for (CopyNextSpellCastThisTurnEffect copyEffect : matchingCopies) {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                spellCard,
                                castingPlayerId,
                                "Copy " + spellCard.getName(),
                                new ArrayList<>(List.of(new CopyControllerCastSpellEffect(
                                        snapshot, castingPlayerId, Set.of(), Set.of(),
                                        copyEffect.removedSupertypes(), permanentSpell, true)))
                        ));
                    }
                    int linkedXCopies = (int) matchingCopies.stream()
                            .filter(copyEffect -> copyEffect.spellFilter() instanceof CardHasXInManaCostPredicate)
                            .count();
                    if (linkedXCopies > 0) {
                        consumeLinkedXAbilityCopies(gameData, castingPlayerId, linkedXCopies);
                    }
                    if (remainingCopies.isEmpty()) {
                        gameData.pendingNextFilteredSpellCopiesThisTurn.remove(castingPlayerId);
                    } else {
                        gameData.pendingNextFilteredSpellCopiesThisTurn.put(castingPlayerId, remainingCopies);
                    }
                    gameLogService.append(gameData, GameLog.cardThen(spellCard, " is copied."));
                    log.info("Game {} - {} filtered spell-copy trigger(s) queued for {}",
                            gameData.id, matchingCopies.size(), spellCard.getName());
                }
            }
        }

        // "Until end of turn, whenever you cast an instant or sorcery spell, copy it"
        // (e.g. The Mirari Conjecture chapter III). Each granting ability triggers on its own.
        int spellCopyGrants = gameData.spellCopyGrantsUntilEndOfTurn.getOrDefault(castingPlayerId, 0);
        if (spellCopyGrants > 0
                && (spellCard.hasType(CardType.INSTANT) || spellCard.hasType(CardType.SORCERY))) {
            // Find the spell on the stack to create a snapshot
            StackEntry spellEntry = null;
            for (StackEntry se : gameData.stack) {
                if (se.getTargetableId().equals(spellCard.getId())) {
                    spellEntry = se;
                    break;
                }
            }
            if (spellEntry != null) {
                for (int grant = 0; grant < spellCopyGrants; grant++) {
                    StackEntry snapshot = new StackEntry(spellEntry);
                    CopyControllerCastSpellEffect copyEffect =
                            new CopyControllerCastSpellEffect(snapshot, castingPlayerId);
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            spellCard,
                            castingPlayerId,
                            "Copy " + spellCard.getName(),
                            new ArrayList<>(List.of(copyEffect))
                    ));
                    gameLogService.append(gameData, GameLog.cardThen(spellCard, " is copied (The Mirari Conjecture)."));
                    log.info("Game {} - {} spell-copy trigger queued for {}",
                            gameData.id, spellCard.getName(), castingPlayerId);
                }
            }
        }

        // Conspire (CR 702.78): "When you [tap the two creatures], copy it and you may choose a new
        // target for the copy." Each conspire instance paid during casting queues its own copy
        // trigger; the count is recorded in gameData.conspiredSpellCopyCounts.
        Integer conspireCopies = gameData.conspiredSpellCopyCounts.remove(spellCard.getId());
        if (conspireCopies != null && conspireCopies > 0) {
            StackEntry spellEntry = null;
            for (StackEntry se : gameData.stack) {
                if (se.getTargetableId().equals(spellCard.getId())) {
                    spellEntry = se;
                    break;
                }
            }
            if (spellEntry != null) {
                for (int i = 0; i < conspireCopies; i++) {
                    StackEntry snapshot = new StackEntry(spellEntry);
                    CopyControllerCastSpellEffect copyEffect =
                            new CopyControllerCastSpellEffect(snapshot, castingPlayerId);
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            spellCard,
                            castingPlayerId,
                            "Copy " + spellCard.getName() + " (Conspire)",
                            new ArrayList<>(List.of(copyEffect))
                    ));
                    gameLogService.append(gameData, GameLog.cardThen(spellCard, " is copied (Conspire)."));
                    log.info("Game {} - {} conspire copy trigger queued for {}",
                            gameData.id, spellCard.getName(), castingPlayerId);
                }
            }
        }

        Integer casualtyCopies = gameData.casualtySpellCopyCounts.remove(spellCard.getId());
        if (casualtyCopies != null && casualtyCopies > 0) {
            StackEntry spellEntry = null;
            for (StackEntry se : gameData.stack) {
                if (se.getTargetableId().equals(spellCard.getId())) {
                    spellEntry = se;
                    break;
                }
            }
            if (spellEntry != null) {
                for (int copyNumber = 0; copyNumber < casualtyCopies; copyNumber++) {
                    CopyControllerCastSpellEffect copyEffect =
                            new CopyControllerCastSpellEffect(new StackEntry(spellEntry), castingPlayerId);
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            spellCard,
                            castingPlayerId,
                            "Copy " + spellCard.getName() + " (Casualty)",
                            new ArrayList<>(List.of(copyEffect))
                    ));
                }
                gameLogService.append(gameData, GameLog.cardThen(spellCard, " is copied (Casualty)."));
                log.info("Game {} - {} casualty copy trigger(s) queued for {}",
                        gameData.id, casualtyCopies, spellCard.getName());
            }
        }

        // "When you cast this spell, ..." abilities of the just-cast card itself (it's a spell on the stack, not a
        // permanent), each one object on the stack (collectSelfCastAbilities): replicate, demonstrate, the card's own
        // abilities (their options judged as they trigger) and the ones perpetually granted to it.
        List<List<CardEffect>> selfCastAbilities = new ArrayList<>();
        if (spellCard.getManaCost() != null) {
            // A granted replicate (CR 702.56a): "When you cast this spell, if a replicate cost was paid for it, copy it
            // for each time its replicate cost was paid", its intervening "if" judged here and again as it resolves.
            // The spell's own replicate with the same cost counts the same payments, so it triggers alone.
            StackEntry replicatedSpell = findStackEntryByCardId(gameData, spellCard.getId());
            gameQueryService.getSpellCastingAbilityGrantValues(
                            gameData, castingPlayerId, spellCard, Keyword.REPLICATE, castZone).stream()
                    .map(value -> value > 0 ? "{" + value + "}" : spellCard.getManaCost())
                    .distinct()
                    .filter(replicateCost -> spellCard.getTriggerEffects(SpellAbilityWatch.ON_SELF_CAST.trigger(),
                                    SpellAbilityWatch.ON_SELF_CAST.zone()).stream()
                            .noneMatch(effect -> countsReplicatePayments(effect, replicateCost)))
                    .filter(replicateCost -> replicatedSpell != null
                            && replicatedSpell.getRepeatedAdditionalCosts().contains(replicateCost))
                    .map(replicateCost -> new ConditionalEffect(new RepeatedAdditionalCostPaid(replicateCost),
                            new CopyTriggeringSpellEffect().withCopies(new RepeatedAdditionalCostCount(replicateCost))))
                    .forEach(replicate -> selfCastAbilities.add(List.of(replicate)));
        }

        ManaPool castingPlayerPool = gameData.playerManaPools.get(castingPlayerId);
        int pendingSpellOrAbilityCopies = castingPlayerPool == null ? 0
                : castingPlayerPool.takeSpentCopyGrantingMana(ManaCopyRider.SPELL_OR_ABILITY);
        if (pendingSpellOrAbilityCopies > 0) {
            StackEntry spellEntry = null;
            for (StackEntry se : gameData.stack) {
                if (se.getTargetableId().equals(spellCard.getId())) {
                    spellEntry = se;
                    break;
                }
            }
            if (spellEntry != null) {
                StackEntry snapshot = new StackEntry(spellEntry);
                boolean permanentSpell = switch (spellEntry.getEntryType()) {
                    case CREATURE_SPELL, ARTIFACT_SPELL, ENCHANTMENT_SPELL, PLANESWALKER_SPELL,
                            BATTLE_SPELL -> true;
                    default -> false;
                };
                // Each mana with "when you spend this mana to cast a spell, copy that spell" is a delayed triggered
                // ability of the mana ability that added it, so each is a stack object of its own (CR 603.7a).
                for (int i = 0; i < pendingSpellOrAbilityCopies; i++) {
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            spellCard,
                            castingPlayerId,
                            "Copy " + spellCard.getName(),
                            new ArrayList<>(List.of(new CopyControllerCastSpellEffect(
                                    snapshot, castingPlayerId, Set.of(), Set.of(), permanentSpell, true)))
                    ));
                }
                gameLogService.append(gameData, GameLog.cardThen(spellCard, " is copied."));
                log.info("Game {} - {} mana-linked spell-or-ability copy trigger(s) queued for {}",
                        gameData.id, pendingSpellOrAbilityCopies, spellCard.getName());
            }
        }
        // Each granted instance of demonstrate triggers separately (e.g. two Silverquill Lecturers).
        gameQueryService.getSpellCastingAbilityGrantValues(
                        gameData, castingPlayerId, spellCard, Keyword.DEMONSTRATE, castZone)
                .forEach(grant -> selfCastAbilities.add(List.of(new MayEffect(
                        new DemonstrateEffect(), "Copy " + spellCard.getName() + "?"))));
        StackEntry selfCastSpellEntry = gameData.stack.stream()
                .filter(entry -> entry.getTargetableId().equals(spellCard.getId()))
                .findFirst()
                .orElse(null);
        ConditionContext selfCastContext = selfCastSpellEntry != null
                ? ConditionContext.forStackEntry(selfCastSpellEntry)
                : ConditionContext.forCard(spellCard, castingPlayerId);
        collectSelfCastAbilities(gameData, spellCard, castingPlayerId, castZone, selfCastSpellEntry, selfCastAbilities);
        List<TriggeredAbility> ownCastAbilities = new ArrayList<>();
        for (TriggeredAbility ability : spellCard.getTriggeredAbilities()) {
            if (ability.hasWatchedTrigger(SpellAbilityWatch.ON_SELF_CAST.trigger(),
                    SpellAbilityWatch.ON_SELF_CAST.zone())) {
                requireNoLimit(ability, spellCard);
                ownCastAbilities.add(ability);
            }
        }
        for (Ability granted : gameData.perpetualAbilities(spellCard.getId())) {
            if (granted instanceof TriggeredAbility triggered && triggered.hasTrigger(
                    SpellAbilityWatch.ON_SELF_CAST.trigger(), SpellAbilityWatch.ON_SELF_CAST.zone())) {
                ownCastAbilities.add(triggered);
            }
        }
        for (TriggeredAbility ability : ownCastAbilities) {
            putSelfCastAbility(gameData, spellCard, castingPlayerId, castZone, selfCastSpellEntry, ability,
                    selfCastContext);
        }

        if (castZone == Zone.HAND && (spellCard.hasType(CardType.INSTANT) || spellCard.hasType(CardType.SORCERY))) {
            List<Permanent> casterBattlefield = gameData.playerBattlefields.get(castingPlayerId);
            if (casterBattlefield != null) {
                for (Permanent perm : new ArrayList<>(casterBattlefield)) {
                    if (perm.isLosesAllAbilitiesUntilEndOfTurn()) continue;
                    GameQueryService.StaticBonus staticBonus = gameQueryService.computeStaticBonus(gameData, perm);
                    if (staticBonus.losesAllAbilities() || staticBonus.losesAllNonManaAbilities()
                            || gameQueryService.hasLostPrintedAbilities(gameData, perm)) continue;
                    List<CardEffect> grantEffects = castTriggerGrants(perm, SpellCastTriggerGrant.CASCADE_TO_INSTANT_OR_SORCERY_FROM_HAND);
                    if (grantEffects.isEmpty()) continue;

                    // Each granted instance triggers separately (CR 702.85c).
                    for (CardEffect grantEffect : grantEffects) {
                        StackEntry cascadeTrigger = new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                spellCard,
                                castingPlayerId,
                                spellCard.getName() + "'s ability",
                                new ArrayList<>(List.of(grantEffect))
                        );
                        cascadeTrigger.setTriggeringCardId(spellCard.getId());
                        cascadeTrigger.setEventValue(spellManaValue(gameData, spellCard));
                        gameData.stack.add(cascadeTrigger);
                    }
                    gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                    log.info("Game {} - {} grants cascade to {} cast from hand for {}",
                            gameData.id, perm.getCard().getName(), spellCard.getName(), castingPlayerId);
                }
            }
        }
        // COMMAND_ZONE_ON_CONTROLLER_CASTS_SPELL — Eminence and similar command-zone spell-cast triggers

        if (castZone == Zone.HAND && spellCard.hasType(CardType.ENCHANTMENT)) {
            List<Permanent> casterBattlefield = gameData.playerBattlefields.get(castingPlayerId);
            if (casterBattlefield != null) {
                for (Permanent perm : new ArrayList<>(casterBattlefield)) {
                    if (perm.isLosesAllAbilitiesUntilEndOfTurn()) continue;
                    GameQueryService.StaticBonus staticBonus = gameQueryService.computeStaticBonus(gameData, perm);
                    if (staticBonus.losesAllAbilities() || staticBonus.losesAllNonManaAbilities()
                            || gameQueryService.hasLostPrintedAbilities(gameData, perm)) continue;
                    List<CardEffect> grantEffects = castTriggerGrants(perm, SpellCastTriggerGrant.CASCADE_TO_ENCHANTMENT_FROM_HAND);
                    if (grantEffects.isEmpty()) continue;

                    // Each granted instance triggers separately (CR 702.85c).
                    for (CardEffect grantEffect : grantEffects) {
                        StackEntry cascadeTrigger = new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                spellCard,
                                castingPlayerId,
                                spellCard.getName() + "'s ability",
                                new ArrayList<>(List.of(grantEffect)));
                        cascadeTrigger.setTriggeringCardId(spellCard.getId());
                        cascadeTrigger.setEventValue(spellManaValue(gameData, spellCard));
                        gameData.stack.add(cascadeTrigger);
                    }
                    gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                    log.info("Game {} - {} grants cascade to enchantment {} cast from hand for {}",
                            gameData.id, perm.getCard().getName(), spellCard.getName(), castingPlayerId);
                }
            }
        }

        // "The first spell you cast each turn has cascade" (Maelstrom Nexus). A permanent-granted
        // keyword: a static GrantCastTriggerToSpellsEffect on the caster's battlefield. recordSpellCast runs before this method in
        // every cast path, so a count of 1 identifies the caster's first spell of the turn. The held
        // CascadeEffect is queued keyed to the just-cast spell so CascadeEffectHandler's threshold is
        // the spell's mana value (not the granting permanent's). One trigger per granting permanent.
        if (gameQueryService.cardHasSubtype(spellCard, CardSubtype.SLIVER, gameData, castingPlayerId)) {
            List<Permanent> casterBattlefield = gameData.playerBattlefields.get(castingPlayerId);
            if (casterBattlefield != null) {
                for (Permanent perm : new ArrayList<>(casterBattlefield)) {
                    if (perm.isLosesAllAbilitiesUntilEndOfTurn()) continue;
                    List<CardEffect> grantEffects = castTriggerGrants(perm, SpellCastTriggerGrant.CASCADE_TO_SLIVER_SPELL);
                    if (grantEffects.isEmpty()) continue;

                    // Each granted instance triggers separately (CR 702.85c).
                    for (CardEffect grantEffect : grantEffects) {
                        StackEntry grantedCascade = new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                spellCard,
                                castingPlayerId,
                                spellCard.getName() + "'s ability",
                                new ArrayList<>(List.of(grantEffect)));
                        StackEntry grantedCascadeSpell = findStackEntryByCardId(gameData, spellCard.getId());
                        if (grantedCascadeSpell != null) {
                            grantedCascade.setXValue(grantedCascadeSpell.getXValue());
                        }
                        gameData.stack.add(grantedCascade);
                    }
                    gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                    log.info("Game {} - {} grants cascade to Sliver spell {} for {}",
                            gameData.id, perm.getCard().getName(), spellCard.getName(), castingPlayerId);
                }
            }
        }

        if (gameData.getSpellsCastThisTurnCount(castingPlayerId) == 1) {
            List<Permanent> casterBattlefield = gameData.playerBattlefields.get(castingPlayerId);
            if (casterBattlefield != null) {
                for (Permanent perm : new ArrayList<>(casterBattlefield)) {
                    if (perm.isLosesAllAbilitiesUntilEndOfTurn()) continue;
                    List<CardEffect> grantEffects = castTriggerGrants(perm, SpellCastTriggerGrant.CASCADE_TO_FIRST_SPELL);
                    if (grantEffects.isEmpty()) continue;

                    // Each granted instance triggers separately (CR 702.85c).
                    for (CardEffect grantEffect : grantEffects) {
                        StackEntry grantedCascade = new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                spellCard,
                                castingPlayerId,
                                spellCard.getName() + "'s ability",
                                new ArrayList<>(List.of(grantEffect)));
                        StackEntry grantedCascadeSpell = findStackEntryByCardId(gameData, spellCard.getId());
                        if (grantedCascadeSpell != null) {
                            grantedCascade.setXValue(grantedCascadeSpell.getXValue());
                        }
                        gameData.stack.add(grantedCascade);
                    }
                    gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                    log.info("Game {} - {} grants cascade to first spell {} for {}",
                            gameData.id, perm.getCard().getName(), spellCard.getName(), castingPlayerId);
                }
            }
        }

        // "The first spell you cast each turn that mana from a Treasure was spent to cast has
        // cascade" (Rain of Riches). Unlike the ordinary first-spell marker above, a spell cast
        // without Treasure mana does not consume this grant.
        if (castUsingTreasureMana
                && gameData.getSpellsCastUsingTreasureManaThisTurnCount(castingPlayerId) == 1) {
            List<Permanent> casterBattlefield = gameData.playerBattlefields.get(castingPlayerId);
            if (casterBattlefield != null) {
                for (Permanent perm : new ArrayList<>(casterBattlefield)) {
                    if (gameQueryService.hasLostAllAbilities(gameData, perm)) continue;
                    List<CardEffect> grantEffects = castTriggerGrants(perm, SpellCastTriggerGrant.CASCADE_TO_FIRST_SPELL_USING_TREASURE_MANA);
                    if (grantEffects.isEmpty()) continue;

                    // Each granted instance triggers separately (CR 702.85c).
                    for (CardEffect grantEffect : grantEffects) {
                        StackEntry grantedCascade = new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                spellCard,
                                castingPlayerId,
                                spellCard.getName() + "'s ability",
                                new ArrayList<>(List.of(grantEffect)));
                        StackEntry grantedCascadeSpell = findStackEntryByCardId(gameData, spellCard.getId());
                        if (grantedCascadeSpell != null) {
                            grantedCascade.setXValue(grantedCascadeSpell.getXValue());
                        }
                        gameData.stack.add(grantedCascade);
                    }
                    gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                    log.info("Game {} - {} grants cascade to first Treasure-mana-funded spell {} for {}",
                            gameData.id, perm.getCard().getName(), spellCard.getName(), castingPlayerId);
                }
            }
        }

        if (spellCard.hasType(CardType.INSTANT) || spellCard.hasType(CardType.SORCERY)) {
            List<Permanent> casterBattlefield = gameData.playerBattlefields.get(castingPlayerId);
            if (casterBattlefield != null) {
                for (Permanent perm : new ArrayList<>(casterBattlefield)) {
                    if (perm.isLosesAllAbilitiesUntilEndOfTurn()
                            || gameQueryService.hasLostAllAbilities(gameData, perm)) continue;
                    List<CardEffect> grantEffects = castTriggerGrants(perm, SpellCastTriggerGrant.STORM_TO_INSTANT_OR_SORCERY);
                    if (grantEffects.isEmpty()) continue;

                    StackEntry spellEntry = findStackEntryByCardId(gameData, spellCard.getId());
                    if (spellEntry == null) continue;

                    for (CardEffect grantedStorm : grantEffects) {
                        queueGrantedStorm(gameData, perm.getCard(), castingPlayerId,
                                perm.getCard().getName() + "'s ability", spellEntry, perm.getId(), grantedStorm);
                    }
                    gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                    log.info("Game {} - {} grants Storm to {} for {}",
                            gameData.id, perm.getCard().getName(), spellCard.getName(), castingPlayerId);
                }
            }
        }

        playerInputService.processNextMayAbility(gameData);
    }

    /**
     * The triggered abilities a permanent's static {@link GrantCastTriggerToSpellsEffect}s of the given kind give
     * the spell being cast, in the order the permanent lists them.
     */
    private static List<CardEffect> castTriggerGrants(Permanent perm, SpellCastTriggerGrant grant) {
        return perm.getCard().getStaticEffects().stream()
                .filter(GrantCastTriggerToSpellsEffect.class::isInstance)
                .map(GrantCastTriggerToSpellsEffect.class::cast)
                .filter(granting -> granting.grant() == grant)
                .map(GrantCastTriggerToSpellsEffect::ability)
                .toList();
    }

    /**
     * Puts a storm trigger an effect gave {@code spellEntry}, the spell just cast, on the stack (CR 702.40a): the granted
     * {@code effect} as written, with that spell as its triggering spell and the spell's places in the turn's cast
     * order, from which its copy count is read as it resolves.
     */
    private void queueGrantedStorm(GameData gameData, Card sourceCard, UUID controllerId, String description,
                                   StackEntry spellEntry, UUID sourcePermanentId, CardEffect effect) {
        StackEntry stormTrigger = new StackEntry(StackEntryType.TRIGGERED_ABILITY, sourceCard, controllerId, description,
                new ArrayList<>(List.of(effect)), 0, sourcePermanentId);
        recordTriggeringSpell(gameData, stormTrigger, spellEntry);
        gameData.stack.add(stormTrigger);
        log.info("Game {} - granted Storm trigger queued for {}", gameData.id, spellEntry.getCard().getName());
    }

    /**
     * Records {@code spellEntry}, the spell just cast, as the triggering spell of {@code trigger}, one of the abilities
     * its casting triggered that refers to it ("copy it", "copy this spell"): as it is now, kept as it last existed on
     * the stack once it has left (CR 608.2h), with its places among its caster's spells and every player's spells
     * this turn, and the cast facts its copy counts and intervening "if"s read (its X, repeatable additional cost
     * payments and casualty payment).
     * The ability doesn't target the spell.
     */
    private static void recordTriggeringSpell(GameData gameData, StackEntry trigger, StackEntry spellEntry) {
        UUID spellId = spellEntry.getCard().getId();
        trigger.setTriggeringCardId(spellId);
        trigger.setTriggeringSpell(spellEntry);
        trigger.setTriggeringSpellCastIndex(gameData.getSpellCastIndexThisTurn(spellEntry.getControllerId(), spellId));
        trigger.setTriggeringSpellTurnCastIndex(gameData.getSpellCastOrdinalThisTurn(spellId) - 1);
        trigger.setXValue(spellEntry.getXValue());
        trigger.setRepeatedAdditionalCosts(List.copyOf(spellEntry.getRepeatedAdditionalCosts()));
        trigger.setCasualtyCostPaid(spellEntry.isCasualtyCostPaid());
        trigger.setNonTargeting(true);
    }

    /**
     * Queues the target {@code choice} of the ability whose entry, when it chooses nothing, is {@code record}, with
     * that record handed off ({@code GameData.handOffTriggerRecord}). The entry built from the chosen target takes the
     * record's trigger event whole, so what the choice carries of the event is written on the record first.
     */
    private void queueTargetChoice(GameData gameData, StackEntry record,
                                   PermanentChoiceContext.SpellTargetTriggerAnyTarget choice) {
        if (choice.triggeringCardId() != null) {
            record.setTriggeringCardId(choice.triggeringCardId());
            if (record.getTriggeringSpellSnapshot() == null) {
                gameData.stack.stream()
                        .filter(candidate -> candidate.getEntryType() != StackEntryType.TRIGGERED_ABILITY
                                && candidate.getEntryType() != StackEntryType.ACTIVATED_ABILITY
                                && choice.triggeringCardId().equals(candidate.getCard().getId()))
                        .findFirst()
                        .ifPresent(record::setTriggeringSpell);
            }
        }
        if (choice.onceEachTurnActionKey() != null) {
            record.setOnceEachTurnActionKey(choice.onceEachTurnActionKey());
        }
        if (choice.triggeringPermanentId() != null) {
            record.setTriggeringPermanentId(choice.triggeringPermanentId());
            Permanent triggeringPermanent = gameQueryService.findPermanentById(gameData,
                    choice.triggeringPermanentId());
            if (triggeringPermanent != null) {
                record.setTriggeringCardId(triggeringPermanent.getCard().getId());
            }
        }
        if (choice.triggeringPermanentControllerId() != null) {
            record.setTriggeringPermanentControllerId(choice.triggeringPermanentControllerId());
        }
        if (choice.triggeringPlayerId() != null) {
            record.setTriggeringPlayerId(choice.triggeringPlayerId());
        }
        if (choice.triggeringPermanentSnapshot() != null) {
            record.setAttachedPermanentSnapshot(choice.triggeringPermanentSnapshot());
        }
        if (choice.departedPermanent() != null && record.getTriggeringPermanentSnapshot() == null) {
            record.setTriggeringPermanent(choice.departedPermanent());
        }
        if (!choice.producedManaColors().isEmpty()) {
            record.setProducedManaColors(choice.producedManaColors());
        }
        if (choice.triggeringPermanentPowerAtTrigger() != null) {
            record.setTriggeringPermanentPowerAtTrigger(choice.triggeringPermanentPowerAtTrigger());
        }
        if (choice.eventValue() != null) {
            record.setEventValue(choice.eventValue());
        } else if (choice.triggeringSpellManaValue() != null) {
            record.setEventValue(choice.triggeringSpellManaValue());
        } else if (choice.spellManaSpentX() > 0) {
            record.setEventValue(choice.spellManaSpentX());
        }
        gameData.handOffTriggerRecord(record);
        gameData.queueInteraction(choice);
    }

    /**
     * {@link #queueTargetChoice} for a choice whose entry records the permanent the event was about as the target is
     * chosen: that permanent with its controller, card, power and toughness is written on the record as the ability
     * triggers.
     */
    private void queueEntersTargetChoice(GameData gameData, StackEntry record,
                                         PermanentChoiceContext.EntersTriggerTarget choice) {
        if (choice.enteringPermanentId() != null) {
            record.setTriggeringPermanentId(choice.enteringPermanentId());
            if (record.getTriggeringPermanentControllerId() == null) {
                record.setTriggeringPermanentControllerId(
                        gameQueryService.findPermanentController(gameData, choice.enteringPermanentId()));
            }
            Permanent enteringPermanent = gameQueryService.findPermanentById(gameData,
                    choice.enteringPermanentId());
            if (enteringPermanent != null) {
                record.rememberLastKnownPermanentCard(choice.enteringPermanentId(), enteringPermanent.getCard());
            }
        }
        if (choice.onceEachTurnActionKey() != null) {
            record.setOnceEachTurnActionKey(choice.onceEachTurnActionKey());
        }
        if (choice.triggeringPermanent() != null && record.getTriggeringPermanentSnapshot() == null) {
            record.setTriggeringPermanent(choice.triggeringPermanent());
        }
        gameData.handOffTriggerRecord(record);
        gameData.queueInteraction(choice);
    }

    private int spellManaValue(GameData gameData, Card spellCard) {
        StackEntry spellEntry = findStackEntryByCardId(gameData, spellCard.getId());
        if (spellEntry != null && spellEntry.isCastFaceDown()) {
            return 0;
        }
        return spellCard.getManaValue() + (spellEntry == null || spellCard.getParsedManaCost() == null
                ? 0 : spellEntry.getXValue() * spellCard.getParsedManaCost().getXSymbolCount());
    }

    /**
     * Hands {@code ability}, a "when you cast this spell" ability of {@code spellCard} (its own, or one perpetually
     * granted to it), to the stack door: its event condition is judged here (CR 603.2), the door judges its intervening
     * "if" in {@code context}, the spell on the stack, puts it on the stack as one object (CR 603.3) and chooses its
     * targets as it goes there (CR 603.3d). The record names the spell ({@code spellEntry}, its X, kick and the zone it
     * was cast from) as its triggering spell, whatever its effects are; "copy it" reads it as the ability resolves, as
     * it last existed on the stack once it has left (CR 608.2h).
     */
    private void putSelfCastAbility(GameData gameData, Card spellCard, UUID castingPlayerId, Zone castZone,
                                    StackEntry spellEntry, TriggeredAbility ability, ConditionContext context) {
        if (!conditionEvaluationService.eventConditionMet(gameData, ability.options(), context)) {
            return;
        }
        triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(ability, spellCard, null,
                castingPlayerId, spellCard.getName() + "'s ability", context, entry -> {
                    entry.setSourceZone(castZone);
                    entry.setNonTargeting(true);
                    if (spellEntry != null) {
                        recordTriggeringSpell(gameData, entry, spellEntry);
                        entry.setKicked(spellEntry.wasKicked());
                    }
                }));
    }

    /**
     * Puts {@code spellCard}'s "when you cast this spell" abilities that trigger on the stack, {@code abilities} the
     * effects of each (their options already judged, an intervening "if" wrapped around each), each ability one object
     * (CR 603.3): a keyword's (cascade, ripple, demonstrate, storm, replicate; CR 702.85c) as any other. Each entry
     * records the spell ({@code spellEntry}, its X, kick and the zone it was cast from) as its triggering spell, whatever
     * its effects are; "copy it" and cascade's "this spell's mana value" read it as the ability resolves, as it last
     * existed on the stack once it has left (CR 608.2h). A "you may" is asked as the ability resolves (CR 603.5). An
     * ability whose effects target chooses its targets as it goes on the stack (CR 603.3d).
     */
    private void collectSelfCastAbilities(GameData gameData, Card spellCard, UUID castingPlayerId, Zone castZone,
                                          StackEntry spellEntry, List<List<CardEffect>> abilities) {
        for (List<CardEffect> effects : abilities) {
            if (effects.isEmpty()) {
                continue;
            }
            StackEntry trigger = new StackEntry(StackEntryType.TRIGGERED_ABILITY, spellCard, castingPlayerId,
                    spellCard.getName() + "'s ability", new ArrayList<>(effects));
            trigger.setSourceZone(castZone);
            if (spellEntry != null) {
                recordTriggeringSpell(gameData, trigger, spellEntry);
                trigger.setKicked(spellEntry.wasKicked());
            }
            gameData.handOffTriggerRecord(trigger);
            if (!queueSelfCastTargetChoice(gameData, spellCard, castingPlayerId, effects)) {
                gameData.stack.add(trigger);
                log.info("Game {} - {} self-cast trigger queued for {}",
                        gameData.id, spellCard.getName(), castingPlayerId);
            }
        }
    }

    /**
     * Queues the target choice of a "when you cast this spell" ability whose {@code effects} target, its targets chosen
     * as it goes on the stack (CR 603.3d).
     *
     * @return whether the ability targets
     */
    private boolean queueSelfCastTargetChoice(GameData gameData, Card spellCard, UUID castingPlayerId,
                                              List<CardEffect> effects) {
        // A single target (Abundant Maw: "target opponent loses 3 life") reuses SpellTargetTriggerAnyTarget; several
        // ("up to N target permanents", Elder Deep-Fiend) the enter multi-target slot walker with no source permanent.
        boolean needsPlayerTarget = effects.stream()
                .anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.PLAYER));
        boolean needsPermanentTarget = effects.stream()
                .anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.PERMANENT));
        boolean needsGraveyardTarget = effects.stream()
                .anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD));
        if (needsGraveyardTarget && !needsPlayerTarget && !needsPermanentTarget) {
            gameData.queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                    spellCard, castingPlayerId, new ArrayList<>(effects)));
            log.info("Game {} - {} self-cast graveyard-target trigger queued for {}",
                    gameData.id, spellCard.getName(), castingPlayerId);
            return true;
        }
        if (!needsPlayerTarget && !needsPermanentTarget) {
            return false;
        }
        boolean multiTarget = spellCard.getTargetGroups().size() > 1
                || etbTokenTargetService.needsSlotBySlotTargetSelection(spellCard);
        if (multiTarget) {
            gameData.queueInteraction(new PermanentChoiceContext.ETBTokenMultiTargetTrigger(
                    spellCard, castingPlayerId, new ArrayList<>(effects),
                    null, new ArrayList<>(), 0, 0));
            log.info("Game {} - {} self-cast multi-target trigger queued for {}",
                    gameData.id, spellCard.getName(), castingPlayerId);
            return true;
        }
        boolean playerTargetOnly = needsPlayerTarget && !needsPermanentTarget;
        boolean optionalTarget = spellCard.getTargetGroups().size() == 1
                && spellCard.getTargetGroups().getFirst().getMinTargets() == 0
                && spellCard.getTargetGroups().getFirst().getMaxTargets() == 1
                && effects.stream().anyMatch(effect ->
                spellCard.getEffectTargetIndex(effect) == spellCard.getTargetGroups().getFirst().getIndex());
        gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                spellCard, castingPlayerId, new ArrayList<>(effects),
                playerTargetOnly, spellCard.getTargetFilter(), 0, null, optionalTarget));
        log.info("Game {} - {} self-cast targeting trigger queued for {}",
                gameData.id, spellCard.getName(), castingPlayerId);
        return true;
    }

    /** Whether {@code effect} is a replicate copy counting the payments of {@code replicateCost}. */
    private static boolean countsReplicatePayments(CardEffect effect, String replicateCost) {
        return switch (effect) {
            case CopyTriggeringSpellEffect copy -> copy.dynamicCopies() instanceof RepeatedAdditionalCostCount count
                    && replicateCost.equals(count.manaCost());
            case ConditionalEffect conditional -> countsReplicatePayments(conditional.wrapped(), replicateCost);
            default -> false;
        };
    }

    private void dispatchSuspendedExiledCardSpellCastTriggers(GameData gameData, UUID castingPlayerId,
            TriggerContext.SpellCast context) {
        for (ExiledCardEntry exiledEntry : new ArrayList<>(gameData.exiledCards)) {
            UUID ownerId = exiledEntry.ownerId();
            if (ownerId.equals(castingPlayerId)) {
                continue;
            }
            if (gameData.exiledCardsWithNonSuspendTimeCounters.contains(exiledEntry.card().getId())) {
                continue;
            }
            Integer timeCounters = gameData.exiledCardTimeCounters.get(exiledEntry.card().getId());
            if (timeCounters == null || timeCounters <= 0) {
                continue;
            }

            Card card = exiledEntry.card();
            for (TriggeredAbility ability : card.getTriggeredAbilities()) {
                if (ability.hasWatchedTrigger(SpellAbilityWatch.ON_OPPONENT_CASTS_SPELL.trigger(),
                        SpellAbilityWatch.ON_OPPONENT_CASTS_SPELL.zone())) {
                    requireNoLimit(ability, card);
                    collectSuspendedCastAbility(gameData, card, ownerId, context, ability);
                }
            }
        }
    }

    /**
     * Collects {@code ability}, a "whenever an opponent casts a spell" ability of the suspended card {@code card}, for
     * the cast {@code context}: its options are judged as it triggers, and its intervening "if" ("if this card is
     * suspended") stays around each effect and is checked again as it resolves (CR 603.4). The ability goes on the
     * stack as one object of its effects (CR 603.3), the spell's caster as "that player" (the entry's non-targeting
     * target, who decides Pardic Dragon's "that player may put a time counter on it") and the spell as its triggering
     * card.
     */
    private void collectSuspendedCastAbility(GameData gameData, Card card, UUID ownerId,
                                             TriggerContext.SpellCast context, TriggeredAbility ability) {
        ConditionContext conditionContext = ConditionContext.forCard(card, ownerId);
        if (!conditionEvaluationService.eventConditionMet(gameData, ability.options(), conditionContext)) {
            return;
        }
        boolean triggered = triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(ability, card, null,
                ownerId, card.getName() + "'s ability", conditionContext, entry -> {
                    entry.setTargetId(context.castingPlayerId());
                    entry.setTriggeringCardId(context.spellCard().getId());
                    entry.setNonTargeting(true);
                }));
        if (triggered) {
            gameLogService.append(gameData, GameLog.cardThen(card, "'s ability triggers."));
        }
    }

    private boolean isNthMatchingSpell(GameData gameData, SpellFilter filter, UUID playerId) {
        long matchingSpells = gameData.getSpellsCastThisTurn(playerId).stream()
                .filter(spell -> predicateEvaluationService.matchesCardPredicate(spell, filter.spell(), null))
                .count();
        return matchingSpells == filter.nthThisTurn();
    }

    /**
     * The graveyard, hand and command-zone cast readers judge no limit, and the hand and command-zone readers and the
     * graveyard exhaust reader no intervening "if" either; a card with one there fails loudly instead of triggering
     * wrongly.
     */
    private static void requireNoOptions(TriggeredAbility ability, Card card) {
        if (!ability.options().equals(TriggerOptions.DEFAULT)) {
            throw new IllegalStateException(card.getName() + "'s triggered ability has options its zone's reader "
                    + "doesn't judge: " + ability.options());
        }
    }

    /**
     * The readers of a card that isn't a permanent (a spell's "when you cast this spell" and "when this spell is
     * countered" abilities, a suspended card's) judge no limit, which is kept on a permanent; a card with one there
     * fails loudly instead of triggering wrongly.
     */
    /**
     * Collects {@code emblem}'s cast abilities ("whenever you cast a creature or planeswalker spell, …", "whenever a
     * player casts a spell, …") for the spell {@code spellCast} names. Abilities of emblems function in the command
     * zone (CR 114.4), whatever zones their trigger conditions name. The emblem has no permanent, so the generic cast
     * collector judges each ability's spell filter, intervening "if" and target filter with the emblem as its source,
     * and puts it on the stack once: nothing makes an emblem's ability trigger additional times. An ability takes no
     * limit here.
     */
    private void collectEmblemCastAbilities(GameData gameData, Emblem emblem, TriggerContext.SpellCast spellCast) {
        for (TriggeredAbility ability : emblem.triggeredAbilities()) {
            for (TriggerCondition condition : ability.conditions()) {
                if (!(condition.trigger() instanceof SpellTrigger spell)
                        || spell.event() != SpellTrigger.SpellEvent.CAST
                        || !castByRelatedPlayer(spell.player(), emblem.controllerId(), spellCast.castingPlayerId())) {
                    continue;
                }
                requireNoLimit(ability, emblem.sourceCard());
                spellCastTriggerCollectorService.collectCastAbility(
                        new TriggerMatchContext(gameData, null, emblem.controllerId(), List.of(), emblem.sourceCard(),
                                (PlanarObject) null),
                        CastAbility.of(ability, spell.filter() == null ? SpellFilter.ANY : spell.filter()),
                        spellCast, false);
                break;
            }
        }
    }

    /** Whether {@code castingPlayerId} is the player {@code relation} names, relative to {@code controllerId}. */
    private static boolean castByRelatedPlayer(com.github.laxika.magicalvibes.model.trigger.PlayerRelation relation,
                                               UUID controllerId, UUID castingPlayerId) {
        return switch (relation) {
            case ANY -> true;
            case YOU -> controllerId.equals(castingPlayerId);
            case OPPONENT -> !controllerId.equals(castingPlayerId);
        };
    }

    private static void requireNoLimit(TriggeredAbility ability, Card card) {
        if (ability.options().limit() != TriggerLimit.NONE) {
            throw new IllegalStateException(card.getName() + "'s triggered ability has a limit its reader doesn't "
                    + "judge: " + ability.options());
        }
    }

    /** As {@link #requireNoOptions}, for the graveyard and hand cast readers, which judge an intervening "if" (CR 603.4). */
    private static void requireOnlyInterveningIf(TriggeredAbility ability, Card card) {
        TriggerOptions options = ability.options();
        if (options.limit() != TriggerLimit.NONE || options.multiplicity() != TriggerMultiplicity.EACH
                || options.eventCondition() != null) {
            throw new IllegalStateException(card.getName() + "'s cast ability has options the graveyard reader "
                    + "doesn't judge: " + options);
        }
    }

    public void checkControllerCountersSpellTriggers(GameData gameData, UUID counteringPlayerId) {
        if (counteringPlayerId == null) return;

        var ctx = new TriggerContext.SpellCountered(counteringPlayerId);
        gameData.forEachPermanent((playerId, perm) -> {
            if (!playerId.equals(counteringPlayerId)) return;
            dispatchWatch(gameData, perm, playerId, SpellAbilityWatch.ON_CONTROLLER_COUNTERS_SPELL, ctx);
        });
    }

    /**
     * Collects the "whenever a spell you control causes you to gain card advantage" abilities (Toofer, Keeper of the
     * Full Grip) of the permanents {@code spellEntry}'s controller controls, once {@code spellEntry} finished resolving
     * and gave that player card advantage (the caller judges it). The abilities trigger on the spell's resolution, not
     * its cast, so a spell its controller didn't cast counts, and the source must be on the battlefield only as the
     * spell finishes resolving (CR 603.2).
     */
    public void checkSpellCardAdvantageTriggers(GameData gameData, StackEntry spellEntry) {
        UUID controllerId = spellEntry.getControllerId();
        if (controllerId == null) return;

        var ctx = new TriggerContext.SpellCardAdvantage(new StackEntry(spellEntry));
        gameData.forEachPermanent((playerId, perm) -> {
            if (!playerId.equals(controllerId)) return;
            dispatchWatch(gameData, perm, playerId, SpellAbilityWatch.ON_CONTROLLER_SPELL_CAUSES_CARD_ADVANTAGE, ctx);
        });
    }

    /** Fires effects that care when a spell cast by the given player is countered. */
    public void checkControllerSpellCounteredTriggers(GameData gameData, UUID spellControllerId) {
        if (spellControllerId == null) return;

        var ctx = new TriggerContext.SpellCastCountered(spellControllerId);
        gameData.forEachPermanent((playerId, perm) -> {
            if (!playerId.equals(spellControllerId)) return;
            dispatchWatch(gameData, perm, playerId, SpellAbilityWatch.ON_CONTROLLER_SPELL_COUNTERED, ctx);
        });
    }

    // ── Discard triggers ───────────────────────────────────────────────

    /** Fires a spell's own trigger when it is countered or fizzles. */
    public void checkSelfSpellCounteredOrFizzledTriggers(GameData gameData, StackEntry spellEntry) {
        if (spellEntry == null || spellEntry.getCard() == null || spellEntry.getControllerId() == null
                || (spellEntry.getEntryType() != StackEntryType.INSTANT_SPELL
                && spellEntry.getEntryType() != StackEntryType.SORCERY_SPELL)) return;

        TriggerContext context = new TriggerContext.SpellCounteredOrFizzled(new StackEntry(spellEntry));
        Card spellCard = spellEntry.getCard();
        SpellAbilityWatch watch = SpellAbilityWatch.ON_SELF_SPELL_COUNTERED_OR_FIZZLED;
        for (TriggeredAbility ability : spellCard.getTriggeredAbilities()) {
            if (!ability.hasWatchedTrigger(watch.trigger(), watch.zone())) {
                continue;
            }
            requireNoLimit(ability, spellCard);
            ConditionContext conditionContext = ConditionContext.forStackEntry(spellEntry);
            if (!conditionEvaluationService.eventConditionMet(gameData, ability.options(), conditionContext)) {
                continue;
            }
            // The spell's own ability, controlled by the spell's controller, with the spell as it last existed on
            // the stack as its triggering spell ("copy it" copies that, CR 608.2h).
            triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(ability, spellCard, null,
                    spellEntry.getControllerId(), spellCard.getName() + "'s ability", conditionContext,
                    spellWatchRecord(context, null, null)));
        }
    }

    public void beginDiscardEvent(GameData gameData, UUID discardingPlayerId) {
        if (gameData.discardEventPlayerId == null) {
            gameData.discardEventPlayerId = discardingPlayerId;
            gameData.discardEventCardCount = 0;
            gameData.discardEventCards.clear();
        } else if (!gameData.discardEventPlayerId.equals(discardingPlayerId)) {
            throw new IllegalStateException("A discard event is already in progress for another player");
        }
    }

    public void finishDiscardEvent(GameData gameData) {
        UUID discardingPlayerId = gameData.discardEventPlayerId;
        int discardedCount = gameData.discardEventCardCount;
        List<Card> discardedCards = List.copyOf(gameData.discardEventCards);
        gameData.discardEventPlayerId = null;
        gameData.discardEventCardCount = 0;
        gameData.discardEventCards.clear();
        stateTriggerService.getObject().checkStateTriggers(gameData);
        if (discardingPlayerId != null && discardedCount > 0) {
            boolean triggered = checkDiscardEventTriggers(gameData, discardingPlayerId, discardedCount,
                    discardedCards);
            if (triggered) {
                gameOutcomeService.checkWinCondition(gameData);
            }
        }
    }

    public void checkDiscardTriggers(GameData gameData, UUID discardingPlayerId, Card discardedCard) {
        checkDiscardTriggers(gameData, discardingPlayerId, discardedCard, false);
    }

    /**
     * Whether a discarded card sits in a library instead of a graveyard, i.e. it was put there by a
     * discard replacement (Library of Leng) without being revealed. The library-top replacement never
     * reveals the card, so its characteristics are undefined for discard triggers.
     */
    private boolean isDiscardedToLibraryUnrevealed(GameData gameData, Card discardedCard) {
        boolean inGraveyard = gameData.playerGraveyards.values().stream()
                .anyMatch(graveyard -> graveyard.stream().anyMatch(card -> card.getId().equals(discardedCard.getId())));
        if (inGraveyard) {
            return false;
        }
        return gameData.playerDecks.values().stream()
                .anyMatch(deck -> deck.stream().anyMatch(card -> card.getId().equals(discardedCard.getId())));
    }

    public void checkDiscardTriggers(GameData gameData, UUID discardingPlayerId, Card discardedCard,
                                     boolean cycled) {
        if (cycled) {
            gameData.recordCardCycled(discardingPlayerId, discardedCard);
        }
        // Central discard hook: every discard path routes through here, so count discards per player
        // for this turn (Dream Salvage's "cards target opponent discarded this turn").
        gameData.cardsDiscardedThisTurn.merge(discardingPlayerId, 1, Integer::sum);
        if (discardedCard != null && !discardedCard.hasType(CardType.LAND)) {
            gameData.nonlandCardsDiscardedThisTurn.merge(discardingPlayerId, 1, Integer::sum);
        }
        // Also remember which specific cards were discarded (cycling is a discard) so a later effect can
        // return them from the graveyard (Shadow of the Grave).
        // Remember the mana value of the card just discarded, so a later effect of the same spell can
        // scale off it (Blast of Genius's "damage equal to the discarded card's mana value").
        if (discardedCard != null) {
            gameData.lastDiscardedCardManaValue = discardedCard.getManaValue();
            gameData.greatestDiscardedCardManaValue = Math.max(
                    gameData.greatestDiscardedCardManaValue, discardedCard.getManaValue());
            EnumSet<CardType> cardTypes = EnumSet.noneOf(CardType.class);
            if (discardedCard.getType() != null) {
                cardTypes.add(discardedCard.getType());
            }
            cardTypes.addAll(discardedCard.getAdditionalTypes());
            gameData.lastDiscardedCardTypes = Set.copyOf(cardTypes);
        } else {
            gameData.lastDiscardedCardTypes = Set.of();
        }
        if (discardedCard != null && !discardedCard.isToken()) {
            gameData.cardsDiscardedOrCycledThisTurn
                    .computeIfAbsent(discardingPlayerId, ignored -> ConcurrentHashMap.newKeySet())
                    .add(discardedCard.getId());
            if (gameData.discardCausedByOpponent) {
                gameData.cardsDiscardedByOpponentThisTurn
                        .computeIfAbsent(discardingPlayerId, ignored -> ConcurrentHashMap.newKeySet())
                        .add(discardedCard.getId());
            }
        }

        if (discardedCard != null && gameData.discardEventPlayerId != null) {
            gameData.discardEventCardCount++;
            gameData.discardEventCards.add(discardedCard);
        }

        boolean[] anyTriggered = {false};
        var ctx = new TriggerContext.Discard(discardingPlayerId, discardedCard, cycled);
        // A card put into a hidden zone unrevealed (Library of Leng) has undefined characteristics
        // (CR 701.9c), so no type-gated discard trigger can match it.
        boolean characteristicsKnown = discardedCard != null
                && !isDiscardedToLibraryUnrevealed(gameData, discardedCard);

        gameData.forEachBattlefield((playerId, battlefield) -> {
            if (playerId.equals(discardingPlayerId)) return;

            for (Permanent perm : battlefield) {
                if (gameQueryService.hasLostAllAbilities(gameData, perm)) continue;
                LibraryWatch watch = LibraryWatch.ON_OPPONENT_DISCARDS;
                for (TriggeredAbility ability : LibraryAbilityReader.printed(perm.getCard(), watch)) {
                    List<LibraryAbilityReader.LibraryEffect> reads = LibraryAbilityReader.read(ability, watch.trigger());
                    LibraryAbilityReader.LibraryEffect first = reads.getFirst();
                    LibraryAbilityReader.requireWatchedEvent(first, watch.trigger());
                    LibraryAbilityReader.requireNoCount(first, watch.trigger());
                    // "Whenever an opponent discards a creature/land/noncreature-nonland card, …"
                    // (Waste Not) — the discarded card gates the trigger.
                    if (!discardedCardMatches(gameData, first, discardedCard, characteristicsKnown, playerId)) {
                        continue;
                    }
                    if (dispatchLibraryAbility(gameData, perm, playerId, watch, ctx, reads)) {
                        anyTriggered[0] = true;
                    }
                }
            }
        });

        if (collectTemporaryGlobalDiscardTriggers(gameData, discardingPlayerId, discardedCard)) {
            anyTriggered[0] = true;
        }

        // "Whenever you discard a card" — scan the discarding player's own battlefield (e.g. Necropotence).
        List<Permanent> ownBattlefield = gameData.playerBattlefields.get(discardingPlayerId);
        if (ownBattlefield != null) {
            for (Permanent perm : List.copyOf(ownBattlefield)) {
                if (collectControllerDiscardAbilities(gameData, perm, discardingPlayerId, discardedCard, cycled,
                        characteristicsKnown, ctx)) {
                    anyTriggered[0] = true;
                }
            }
        }

        if (discardedCard != null && gameData.discardEventPlayerId == null
                && checkDiscardEventTriggers(gameData, discardingPlayerId, 1, List.of(discardedCard))) {
            anyTriggered[0] = true;
        }

        if (anyTriggered[0]) {
            gameOutcomeService.checkWinCondition(gameData);
        }

        // Process any pending may abilities added by discard triggers
        if (!gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
        }

        // Check the discarded card itself for self-discard triggers
        if (discardedCard != null) {
            // "When you discard this card" — any discard (Edgar's Awakening). Non-targeting effects
            // (e.g. MayPayManaEffect) go straight onto the stack; any-target effects use the
            // DiscardTriggerAnyTarget pipeline (same as Guerrilla Tactics).
            // Each ability is one object of all its effects (CR 603.3).
            for (List<CardEffect> anyDiscardTriggers : LibraryAbilityReader.printedEffectsByAbility(discardedCard,
                    LibraryWatch.ON_SELF_DISCARDED)) {
                boolean needsAnyTarget = anyDiscardTriggers.stream()
                        .anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                                || e.targetSpec().admits(TargetPredicate.Kind.PLAYER));
                StackEntry anyDiscardEntry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        discardedCard,
                        discardingPlayerId,
                        discardedCard.getName() + "'s ability",
                        new ArrayList<>(anyDiscardTriggers)
                );
                gameData.handOffTriggerRecord(anyDiscardEntry);
                if (needsAnyTarget) {
                    gameData.queueInteraction(new PermanentChoiceContext.DiscardTriggerAnyTarget(
                            discardedCard, discardingPlayerId, new ArrayList<>(anyDiscardTriggers)
                    ));
                } else {
                    gameData.enqueueTrigger(anyDiscardEntry);
                }
                gameLogService.append(gameData, GameLog.cardThen(discardedCard,
                        " was discarded — its ability triggers!"));
                log.info("Game {} - {} ON_SELF_DISCARDED trigger queued", gameData.id, discardedCard.getName());
                anyTriggered[0] = true;
            }

            // Skip EnterBattlefieldOnDiscardEffect — it's a replacement effect handled earlier in the discard flow
            if (gameData.discardCausedByOpponent) {
                for (List<CardEffect> ability : LibraryAbilityReader.printedEffectsByAbility(discardedCard,
                        LibraryWatch.ON_SELF_DISCARDED_BY_OPPONENT, conditionEvaluationService.judge(gameData,
                                ConditionContext.forCard(discardedCard, discardingPlayerId)))) {
                    List<CardEffect> selfTriggers = ability.stream()
                            .filter(e -> !(e instanceof EnterBattlefieldOnDiscardEffect))
                            .toList();
                    if (selfTriggers.isEmpty()) {
                        continue;
                    }
                    boolean needsAnyTarget = selfTriggers.stream()
                            .anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                                    || e.targetSpec().admits(TargetPredicate.Kind.PLAYER));
                    StackEntry selfDiscardEntry = new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            discardedCard,
                            discardingPlayerId,
                            discardedCard.getName() + "'s ability",
                            new ArrayList<>(selfTriggers)
                    );
                    selfDiscardEntry.setTriggeringCard(discardedCard.getId(),
                            gameData.graveyardEntryVersion(discardedCard.getId()));
                    selfDiscardEntry.setSourceGraveyardEntryVersion(
                            gameData.graveyardEntryVersion(discardedCard.getId()));
                    gameData.handOffTriggerRecord(selfDiscardEntry);
                    if (needsAnyTarget) {
                        gameData.queueInteraction(new PermanentChoiceContext.DiscardTriggerAnyTarget(
                                discardedCard, discardingPlayerId, new ArrayList<>(selfTriggers)
                        ));
                    } else {
                        gameData.enqueueTrigger(selfDiscardEntry);
                    }
                    gameLogService.append(gameData, GameLog.cardThen(discardedCard, " was discarded by an opponent's effect — its ability triggers!"));
                    log.info("Game {} - {} self-discard trigger queued", gameData.id, discardedCard.getName());
                }
            }
        }
    }

    // ── Source deals damage to a player (noncombat) ────────────────────

    public void checkCycleTriggers(GameData gameData, UUID cyclingPlayerId, Card cycledCard) {
        checkCycleTriggers(gameData, cyclingPlayerId, cycledCard, 0);
    }

    /**
     * As {@link #checkCycleTriggers(GameData, UUID, Card)}, carrying the X paid for the cycling cost so
     * that the card's own "when you cycle" trigger can read it (Shark Typhoon).
     */
    public void checkCycleTriggers(GameData gameData, UUID cyclingPlayerId, Card cycledCard, int cyclingXValue) {
        checkCycleTriggers(gameData, cyclingPlayerId, cycledCard, cyclingXValue, null, null);
    }

    /**
     * As {@link #checkCycleTriggers(GameData, UUID, Card, int)}, for a cycling ability whose announced
     * targets belong to the card's own "when you cycle this card" trigger (Rampaging War Mammoth):
     * that trigger goes on the stack above the cycling ability with those targets.
     *
     * @param targetingCyclingAbility the cycling ability that announced the targets, or {@code null}
     * @param announcedTargetIds      the targets announced with the cycling ability
     */
    public void checkCycleTriggers(GameData gameData, UUID cyclingPlayerId, Card cycledCard, int cyclingXValue,
                                   com.github.laxika.magicalvibes.model.ActivatedAbility targetingCyclingAbility,
                                   List<UUID> announcedTargetIds) {
        // Each "when you cycle this card" ability is one object of all its effects (CR 603.3).
        for (List<CardEffect> selfEffects : LibraryAbilityReader.printedEffectsByAbility(cycledCard,
                LibraryWatch.ON_SELF_CYCLED,
                conditionEvaluationService.judge(gameData, ConditionContext.forCard(cycledCard, cyclingPlayerId)))) {
            List<UUID> announcedTargets = announcedTargetIds != null ? announcedTargetIds : List.of();
            StackEntry selfCycleRecord = new StackEntry(StackEntryType.TRIGGERED_ABILITY, cycledCard,
                    cyclingPlayerId, cycledCard.getName() + "'s ability", new ArrayList<>(selfEffects));
            selfCycleRecord.setXValue(cyclingXValue);
            boolean chooseOnTrigger = targetingCyclingAbility != null && announcedTargets.isEmpty()
                    && targetingCyclingAbility.isCyclingTriggerTargetsChosenOnTriggerIfOmitted();
            if (targetingCyclingAbility != null && !chooseOnTrigger) {
                List<UUID> targetIds = announcedTargets;
                if (!targetIds.isEmpty()) {
                    StackEntry selfCycleEntry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, cycledCard,
                            cyclingPlayerId, cycledCard.getName() + "'s ability", new ArrayList<>(selfEffects),
                            cyclingXValue, null, null, Map.of(), null, List.of(), targetIds);
                    selfCycleEntry.setTargetFilter(targetingCyclingAbility.getTargetFilter());
                    if (targetIds.size() == 1) {
                        selfCycleEntry.setTargetId(targetIds.getFirst());
                    }
                    selfCycleEntry.setMultiTargetConstraint(targetingCyclingAbility.getMultiTargetConstraint());
                    gameData.enqueueTrigger(selfCycleEntry);
                } else if (targetingCyclingAbility.isCyclingTriggerWithoutTargets()) {
                    StackEntry selfCycleEntry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, cycledCard,
                            cyclingPlayerId, cycledCard.getName() + "'s ability", new ArrayList<>(selfEffects));
                    selfCycleEntry.setXValue(cyclingXValue);
                    selfCycleEntry.setNonTargeting(true);
                    gameData.enqueueTrigger(selfCycleEntry);
                }
            } else if (selfEffects.stream().anyMatch(effect -> effect.targetSpec().admits(TargetPredicate.Kind.SPELL))) {
                com.github.laxika.magicalvibes.model.filter.StackEntryPredicate cycleSpellFilter = null;
                boolean cycleIncludesAbilities = false;
                if (cycledCard.getTargetFilter() instanceof com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter cycleFilter) {
                    cycleSpellFilter = cycleFilter.predicate();
                    cycleIncludesAbilities = predicateContainsHasTarget(cycleFilter.predicate());
                }
                gameData.handOffTriggerRecord(selfCycleRecord);
                gameData.queueInteraction(new PermanentChoiceContext.ETBSpellTargetTrigger(
                        cycledCard, cyclingPlayerId, new ArrayList<>(selfEffects), cycleSpellFilter,
                        cycleIncludesAbilities, null));
            } else if (selfEffects.stream().anyMatch(effect -> effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                    || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER))) {
                gameData.handOffTriggerRecord(selfCycleRecord);
                gameData.queueInteraction(new PermanentChoiceContext.DiscardControllerTriggerTarget(
                        cycledCard, cyclingPlayerId, new ArrayList<>(selfEffects), null));
            } else if (graveyardTargetingSupport.findTarget(selfEffects) != null) {
                // CR 603.3d: a "when you cycle this card" trigger targeting a card in a graveyard
                // chooses it as the trigger goes on the stack (Grixis Sojourners).
                gameData.handOffTriggerRecord(selfCycleRecord);
                gameData.queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                        cycledCard, cyclingPlayerId, new ArrayList<>(selfEffects)));
            } else {
                gameData.enqueueTrigger(selfCycleRecord);
            }
            gameLogService.append(gameData, GameLog.abilityTriggers(cycledCard));
        }
        TriggerContext ctx = new TriggerContext.Cycle(cyclingPlayerId, cycledCard);
        boolean[] anyTriggered = {false};
        gameData.forEachPermanent((playerId, perm) -> {
            if (dispatchWatch(gameData, perm, playerId, LibraryWatch.ON_ANY_PLAYER_CYCLES, ctx)) {
                anyTriggered[0] = true;
            }
        });
        if (anyTriggered[0]) {
            gameOutcomeService.checkWinCondition(gameData);
        }
    }

    private boolean collectTemporaryGlobalDiscardTriggers(GameData gameData, UUID discardingPlayerId,
                                                          Card discardedCard) {
        if (!gameData.discardCausedByOpponent) {
            return false;
        }

        boolean triggered = false;
        for (TemporaryGlobalTriggeredAbility watcher : List.copyOf(gameData.temporaryGlobalTriggeredAbilities)) {
            if (!watcher.ability().hasTrigger(LibraryWatch.ON_OPPONENT_DISCARDS.trigger())
                    || !watcher.controllerId().equals(discardingPlayerId)) {
                continue;
            }

            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    watcher.sourceCard(),
                    watcher.controllerId(),
                    watcher.sourceCard().getName() + "'s ability",
                    new ArrayList<>(List.of(watcher.effect())));
            entry.setTargetId(discardingPlayerId);
            entry.setNonTargeting(true);
            if (discardedCard != null) {
                entry.setEventCardIds(List.of(discardedCard.getId()));
            }
            gameData.enqueueTrigger(entry);
            gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
            log.info("Game {} - {} temporary global discard trigger fires", gameData.id,
                    watcher.sourceCard().getName());
            triggered = true;
        }
        return triggered;
    }

    /**
     * Queues {@link DamageWatch#ON_DAMAGE_TO_PLAYER} triggers when a permanent deals noncombat
     * damage to a player (e.g. Niv-Mizzet, Dracogenius's ping). Combat damage uses the richer
     * collector in {@code CombatDamageService} instead — do not call this from the combat path.
     */
    public void checkScryTriggers(GameData gameData, UUID scryingPlayerId) {
        checkScryTriggers(gameData, scryingPlayerId, 0, 0);
    }

    public void checkScryTriggers(GameData gameData, UUID scryingPlayerId, int bottomedCardCount) {
        checkScryTriggers(gameData, scryingPlayerId, bottomedCardCount, 0);
    }

    public void checkScryTriggers(GameData gameData, UUID scryingPlayerId,
                                  int bottomedCardCount, int cardsLookedAt) {
        var ctx = new TriggerContext.Scry(scryingPlayerId, bottomedCardCount, cardsLookedAt);
        List<Permanent> ownBattlefield = gameData.playerBattlefields.get(scryingPlayerId);
        if (ownBattlefield != null) {
            for (Permanent perm : List.copyOf(ownBattlefield)) {
                dispatchWatch(gameData, perm, scryingPlayerId, LibraryWatch.ON_CONTROLLER_SCRIES, ctx);
            }
        }

        gameData.forEachBattlefield((controllerId, battlefield) -> {
            if (controllerId.equals(scryingPlayerId)) {
                return;
            }
            for (Permanent perm : List.copyOf(battlefield)) {
                dispatchWatch(gameData, perm, controllerId, LibraryWatch.ON_OPPONENT_SCRIES, ctx);
            }
        });
    }

    /** Fires triggers that care about all players finishing a voting event. */
    public void checkVotingFinishedTriggers(GameData gameData,
                                             com.github.laxika.magicalvibes.model.VotingResult result) {
        TriggerContext context = new TriggerContext.VotingFinished(result);
        gameData.forEachPermanent((controllerId, permanent) ->
                dispatchWatch(gameData, permanent, controllerId,
                        ActionWatch.ON_PLAYERS_FINISH_VOTING, context));
    }

    /** Fires triggers whenever a player is tempted by the Ring. */
    public void checkRingTemptTriggers(GameData gameData, UUID temptingPlayerId, UUID ringBearerId) {
        if (temptingPlayerId == null || ringBearerId == null) {
            return;
        }
        var context = new TriggerContext.RingTempted(temptingPlayerId, ringBearerId);
        for (Permanent permanent : List.copyOf(
                gameData.playerBattlefields.getOrDefault(temptingPlayerId, List.of()))) {
            dispatchWatch(gameData, permanent, temptingPlayerId,
                    ActionWatch.ON_CONTROLLER_TEMPTS_RING, context);
        }
    }

    public void checkRingTemptsYouTriggers(GameData gameData, UUID temptedPlayerId) {
        List<Permanent> ownBattlefield = gameData.playerBattlefields.get(temptedPlayerId);
        if (ownBattlefield == null) {
            return;
        }

        TriggerContext context = new TriggerContext.RingTemptsYou(temptedPlayerId);
        for (Permanent permanent : List.copyOf(ownBattlefield)) {
            dispatchWatch(gameData, permanent, temptedPlayerId, ActionWatch.ON_RING_TEMPTS_YOU, context);
        }
    }

    public void checkInvestigateTriggers(GameData gameData, UUID investigatingPlayerId) {
        boolean firstInvestigationThisTurn = gameData.playersWhoInvestigatedThisTurn.add(investigatingPlayerId);
        var ctx = new TriggerContext.Investigate(investigatingPlayerId);
        List<Permanent> ownBattlefield = gameData.playerBattlefields.get(investigatingPlayerId);
        if (ownBattlefield == null) {
            return;
        }

        for (Permanent perm : List.copyOf(ownBattlefield)) {
            if (firstInvestigationThisTurn) {
                dispatchWatch(gameData, perm, investigatingPlayerId, ActionWatch.ON_CONTROLLER_INVESTIGATES, ctx);
            }
            dispatchWatch(gameData, perm, investigatingPlayerId,
                    ActionWatch.ON_CONTROLLER_INVESTIGATES_EACH_TIME, ctx);
        }
    }

    /** Fires triggers whenever the given player rolls one or more dice. */
    public void checkControllerRollsPlanarDieTriggers(GameData gameData, UUID rollingPlayerId) {
        checkControllerRollsPlanarDieTriggers(gameData, rollingPlayerId, 0);
    }

    /** Fires triggers whenever the given player rolls the planar die, preserving blank/nonblank. */
    public void checkControllerRollsPlanarDieTriggers(GameData gameData, UUID rollingPlayerId,
                                                       int result) {
        for (Permanent perm : List.copyOf(gameData.playerBattlefields.getOrDefault(rollingPlayerId, List.of()))) {
            dispatchWatch(gameData, perm, rollingPlayerId, ActionWatch.ON_CONTROLLER_ROLLS_ONE_OR_MORE_DICE,
                    new TriggerContext.DiceRoll(rollingPlayerId, 1, result, true));
        }
        if (gameData.planechase == null || gameData.planechase.controllerId == null
                || !gameData.planechase.controllerId.equals(rollingPlayerId)) {
            return;
        }
        UUID planarControllerId = gameData.planechase.controllerId;
        TriggerContext context = new TriggerContext.DiceRoll(rollingPlayerId, 1, result, true);
        for (PlanarObject object : List.copyOf(gameData.planechase.faceUp)) {
            TriggerJudge judge = conditionEvaluationService.judge(gameData,
                    ConditionContext.forCard(object.getCard(), planarControllerId), result);
            for (List<CardEffect> effects : byAbility(judge.filter(ActionAbilityReader.printed(object.getCard(),
                    ActionWatch.ON_CONTROLLER_ROLLS_ONE_OR_MORE_DICE)), abilities -> ActionAbilityReader.effects(
                    abilities, ActionWatch.ON_CONTROLLER_ROLLS_ONE_OR_MORE_DICE.trigger()))) {
                registry.dispatch(new TriggerMatchContext(gameData, null, planarControllerId, effects,
                        object.getCard(), object.copy()), ActionWatch.ON_CONTROLLER_ROLLS_ONE_OR_MORE_DICE, context);
            }
        }
    }

    public void checkControllerRollsOneOrMoreDiceTriggers(GameData gameData, UUID rollingPlayerId,
                                                           int diceCount) {
        checkControllerRollsOneOrMoreDiceTriggers(gameData, rollingPlayerId, diceCount, 0);
    }

    /** Fires triggers whenever the given player rolls one or more dice, preserving the result. */
    public void checkControllerRollsOneOrMoreDiceTriggers(GameData gameData, UUID rollingPlayerId,
                                                           int diceCount, int result) {
        if (rollingPlayerId == null || diceCount <= 0) return;

        List<Permanent> ownBattlefield = gameData.playerBattlefields.get(rollingPlayerId);
        if (ownBattlefield == null) return;

        var ctx = new TriggerContext.DiceRoll(rollingPlayerId, diceCount, result);
        for (Permanent perm : List.copyOf(ownBattlefield)) {
            dispatchWatch(gameData, perm, rollingPlayerId,
                    ActionWatch.ON_CONTROLLER_ROLLS_ONE_OR_MORE_DICE, ctx);
        }
    }

    /** Fires one trigger for each die whose natural result was that die's highest face. */
    public void checkControllerRollsHighestNaturalResultTriggers(GameData gameData,
                                                                  UUID rollingPlayerId,
                                                                  int sides,
                                                                  int... results) {
        if (rollingPlayerId == null || sides < 1 || results == null || results.length == 0) return;

        List<Permanent> ownBattlefield = gameData.playerBattlefields.get(rollingPlayerId);
        if (ownBattlefield == null) return;

        for (int result : results) {
            var dieContext = new TriggerContext.DiceRoll(rollingPlayerId, 1, result);
            for (Permanent perm : List.copyOf(ownBattlefield)) {
                dispatchWatch(gameData, perm, rollingPlayerId, ActionWatch.ON_CONTROLLER_ROLLS_DIE, dieContext);
            }
            if (result != sides) continue;
            var ctx = new TriggerContext.DiceRoll(rollingPlayerId, 1, result);
            for (Permanent perm : List.copyOf(ownBattlefield)) {
                dispatchWatch(gameData, perm, rollingPlayerId,
                        ActionWatch.ON_CONTROLLER_ROLLS_HIGHEST_NATURAL_RESULT, ctx);
            }
        }
    }

    /** Fires graveyard-resident triggers whenever the given player rolls a natural 20. */
    public void checkControllerRollsNaturalTwentyTriggers(GameData gameData, UUID rollingPlayerId) {
        if (rollingPlayerId == null) return;

        List<Card> graveyard = gameData.playerGraveyards.get(rollingPlayerId);
        if (graveyard == null) return;

        for (Card card : List.copyOf(graveyard)) {
            for (List<CardEffect> effects : ActionAbilityReader.printedEffectsByAbility(card,
                    ActionWatch.GRAVEYARD_ON_CONTROLLER_ROLLS_NATURAL_20)) {
                gameData.enqueueTrigger(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        card,
                        rollingPlayerId,
                        card.getName() + "'s ability",
                        new ArrayList<>(effects)
                ));
                gameLogService.append(gameData, GameLog.abilityTriggers(card));
                log.info("Game {} - {} graveyard ability triggers on a natural 20",
                        gameData.id, card.getName());
            }
        }
    }

    public void checkForageTriggers(GameData gameData, UUID foragingPlayerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(foragingPlayerId);
        if (battlefield == null) {
            return;
        }
        var context = new TriggerContext.Forage(foragingPlayerId);
        for (Permanent permanent : List.copyOf(battlefield)) {
            dispatchWatch(gameData, permanent, foragingPlayerId, ActionWatch.ON_CONTROLLER_FORAGES, context);
        }
    }

    /**
     * Fires the "whenever you seek one or more cards" abilities of {@code seekingPlayerId}'s permanents for the cards
     * they sought: none when no card was sought (a seek that found nothing is no seek of one or more cards).
     */
    public void checkSeekTriggers(GameData gameData, UUID seekingPlayerId, List<Card> soughtCards) {
        if (seekingPlayerId == null || soughtCards == null || soughtCards.isEmpty()) {
            return;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(seekingPlayerId);
        if (battlefield == null) {
            return;
        }
        TriggerContext context = new TriggerContext.Seek(seekingPlayerId, soughtCards);
        for (Permanent permanent : List.copyOf(battlefield)) {
            dispatchWatch(gameData, permanent, seekingPlayerId, ActionWatch.ON_CONTROLLER_SEEKS, context);
        }
    }

    public void checkSurveilTriggers(GameData gameData, UUID surveilingPlayerId) {
        boolean firstThisTurn = gameData.playersWhoSurveilledThisTurn.add(surveilingPlayerId);
        var ctx = new TriggerContext.Surveil(surveilingPlayerId, firstThisTurn);
        List<Permanent> ownBattlefield = gameData.playerBattlefields.get(surveilingPlayerId);
        if (ownBattlefield != null) {
            for (Permanent perm : List.copyOf(ownBattlefield)) {
                dispatchWatch(gameData, perm, surveilingPlayerId, LibraryWatch.ON_CONTROLLER_SURVEILS, ctx);
            }
        }

        gameData.forEachBattlefield((controllerId, battlefield) -> {
            if (controllerId.equals(surveilingPlayerId)) {
                return;
            }
            for (Permanent perm : List.copyOf(battlefield)) {
                dispatchWatch(gameData, perm, controllerId, LibraryWatch.ON_OPPONENT_SURVEILS, ctx);
            }
        });

        List<Card> graveyard = gameData.playerGraveyards.get(surveilingPlayerId);
        if (graveyard == null) {
            return;
        }

        LibraryWatch graveyardWatch = LibraryWatch.GRAVEYARD_ON_CONTROLLER_SURVEILS;
        for (Card card : List.copyOf(graveyard)) {
            for (List<CardEffect> effects : byAbility(WatchedAbilities.withWatchedTrigger(
                    gameQueryService.getEffectiveGraveyardTriggeredAbilities(gameData, card),
                    graveyardWatch.trigger(), graveyardWatch.zone()),
                    abilities -> LibraryAbilityReader.effects(abilities, graveyardWatch.trigger()))) {
                gameData.enqueueTrigger(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        card,
                        surveilingPlayerId,
                        card.getName() + "'s ability",
                        new ArrayList<>(effects)
                ));
                gameLogService.append(gameData, GameLog.abilityTriggers(card));
                log.info("Game {} - {} graveyard ability triggers on surveil",
                        gameData.id, card.getName());
            }
        }
    }

    public void checkManifestDreadTriggers(GameData gameData, UUID manifestingPlayerId,
                                           Card cardPutIntoGraveyard) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(manifestingPlayerId);
        if (battlefield == null) {
            return;
        }

        var context = new TriggerContext.ManifestDread(manifestingPlayerId, cardPutIntoGraveyard);
        for (Permanent permanent : List.copyOf(battlefield)) {
            dispatchWatch(gameData, permanent, manifestingPlayerId,
                    ActionWatch.ON_CONTROLLER_MANIFESTS_DREAD, context);
        }
    }

    /**
     * Completes the controller's dungeon when {@code departedEntry} is a bottom-room ability and no
     * other room ability of that dungeon is still on the stack (CR 309.6, CR 309.7). Called when the
     * ability resolves, is countered, or is removed for having no legal target, so the dungeon is
     * completed however its last room ability leaves the stack. Then fires the battlefield and
     * graveyard abilities that trigger when a player completes a dungeon.
     */
    public void completeDungeonRoomIfReady(GameData gameData, StackEntry departedEntry) {
        var completion = departedEntry.getEffectsToResolve().stream()
                .filter(com.github.laxika.magicalvibes.model.effect.CompleteDungeonEffect.class::isInstance)
                .map(com.github.laxika.magicalvibes.model.effect.CompleteDungeonEffect.class::cast)
                .findFirst().orElse(null);
        if (completion == null) return;
        var progress = gameData.playerDungeonProgress.get(departedEntry.getControllerId());
        if (progress == null || progress.dungeon() != completion.dungeon() || !progress.isBottomRoom()) return;
        boolean anotherRoomAbility = gameData.stack.stream().anyMatch(entry ->
                entry.getControllerId().equals(departedEntry.getControllerId())
                        && entry.getEffectsToResolve().stream().anyMatch(effect ->
                        effect instanceof com.github.laxika.magicalvibes.model.effect.CompleteDungeonEffect other
                                && other.dungeon() == completion.dungeon()));
        if (anotherRoomAbility) return;
        gameData.playerDungeonProgress.remove(departedEntry.getControllerId());
        gameData.recordCompletedDungeon(departedEntry.getControllerId(), completion.dungeon());
        checkDungeonCompletionTriggers(gameData, departedEntry.getControllerId());
    }

    public void checkDungeonCompletionTriggers(GameData gameData, UUID completingPlayerId) {
        if (completingPlayerId == null) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(completingPlayerId);
        if (battlefield != null) {
            TriggerContext context = new TriggerContext.DungeonCompletion(completingPlayerId);
            for (Permanent permanent : List.copyOf(battlefield)) {
                dispatchWatch(gameData, permanent, completingPlayerId,
                        ActionWatch.ON_CONTROLLER_COMPLETES_DUNGEON, context);
            }
        }

        List<Card> graveyard = gameData.playerGraveyards.get(completingPlayerId);
        if (graveyard == null) {
            return;
        }

        for (Card card : List.copyOf(graveyard)) {
            for (List<CardEffect> effects : ActionAbilityReader.printedEffectsByAbility(card,
                    ActionWatch.GRAVEYARD_ON_CONTROLLER_COMPLETES_DUNGEON)) {
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        card,
                        completingPlayerId,
                        card.getName() + "'s ability",
                        new ArrayList<>(effects)
                );
                entry.setSourceGraveyardEntryVersion(gameData.graveyardEntryVersion(card.getId()));
                gameData.enqueueTrigger(entry);
                gameLogService.append(gameData, GameLog.abilityTriggers(card));
                log.info("Game {} - {} graveyard ability triggers on dungeon completion",
                        gameData.id, card.getName());
            }
        }
    }

    public void checkDiscoverTriggers(GameData gameData, UUID discoveringPlayerId, int discoverValue) {
        var ctx = new TriggerContext.Discover(discoveringPlayerId, discoverValue);
        List<Permanent> ownBattlefield = gameData.playerBattlefields.get(discoveringPlayerId);
        if (ownBattlefield == null) {
            return;
        }
        for (Permanent perm : List.copyOf(ownBattlefield)) {
            dispatchWatch(gameData, perm, discoveringPlayerId, ActionWatch.ON_CONTROLLER_DISCOVERS, ctx);
        }
    }

    public void checkConjureTriggers(GameData gameData, UUID conjuringPlayerId, int cardCount) {
        if (conjuringPlayerId == null || cardCount <= 0) {
            return;
        }
        List<Permanent> ownBattlefield = gameData.playerBattlefields.get(conjuringPlayerId);
        if (ownBattlefield != null) {
            var context = new TriggerContext.Conjure(conjuringPlayerId, cardCount);
            for (Permanent permanent : List.copyOf(ownBattlefield)) {
                dispatchWatch(gameData, permanent, conjuringPlayerId,
                        ActionWatch.ON_CONTROLLER_CONJURES, context);
            }
        }

        List<Card> graveyard = gameData.playerGraveyards.get(conjuringPlayerId);
        if (graveyard == null) {
            return;
        }
        for (Card card : List.copyOf(graveyard)) {
            for (List<CardEffect> effects : graveyardActionAbilities(gameData, card,
                    ActionWatch.GRAVEYARD_ON_CONTROLLER_CONJURES)) {
                gameData.enqueueTrigger(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        card,
                        conjuringPlayerId,
                        card.getName() + "'s ability",
                        new ArrayList<>(effects)
                ));
                gameLogService.append(gameData, GameLog.abilityTriggers(card));
                log.info("Game {} - {} graveyard ability triggers on conjure",
                        gameData.id, card.getName());
            }
        }
    }

    /**
     * Fires turn-scoped global watchers registered for {@code ON_ANY_CREATURE_COMBAT_DAMAGE_TO_PLAYER}
     * (e.g. The Sea Devils chapter III). The watcher's trigger source is the dealing creature, so
     * effects that "it deals damage" use that creature, and the damaged player is bound as the
     * trigger's target. The trigger's filter is judged on the dealer.
     */
    private void enqueueTemporaryAnyCreatureCombatDamageToPlayerTriggers(GameData gameData, Permanent creature,
                                                                         UUID creatureControllerId,
                                                                         UUID damagedPlayerId, int damageDealt) {
        for (TemporaryGlobalTriggeredAbility watcher : List.copyOf(gameData.temporaryGlobalTriggeredAbilities)) {
            if (!watcher.ability().hasWatchedTrigger(CombatWatch.ON_ANY_CREATURE_COMBAT_DAMAGE_TO_PLAYER.trigger())
                    || watcher.watchedPermanentId() != null) {
                continue;
            }
            Trigger anyCombatDamage = CombatWatch.ON_ANY_CREATURE_COMBAT_DAMAGE_TO_PLAYER.trigger();
            CombatAbilityReader.CombatEffect read = CombatAbilityReader.read(watcher.ability(), anyCombatDamage)
                    .getFirst();
            CombatAbilityReader.requireNoLimitOrMultiplicity(read, anyCombatDamage);
            if (read.filter() != null
                    && !predicateEvaluationService.matchesPermanentPredicate(creature, read.filter(),
                    FilterContext.of(gameData)
                            .withSourceCardId(watcher.sourceCard().getId())
                            .withSourceControllerId(watcher.controllerId()))) {
                continue;
            }
            CardEffect resolvedEffect = read.effect();
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    watcher.sourceCard(),
                    watcher.controllerId(),
                    watcher.sourceCard().getName() + "'s triggered ability",
                    new ArrayList<>(List.of(resolvedEffect)),
                    damagedPlayerId,
                    creature.getId());
            entry.setTriggeringPermanentId(creature.getId());
            entry.setTriggeringPermanentControllerId(creatureControllerId);
            entry.setEventValue(damageDealt);
            entry.setNonTargeting(true);
            // "That player" is the player dealt damage, recorded whatever the ability chooses.
            entry.setTriggeringPlayerId(damagedPlayerId);
            entry.setAttackedTargetId(damagedPlayerId);
            gameData.handOffTriggerRecord(entry);
            if (resolvedEffect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)) {
                // The target is chosen as the trigger is put on the stack (CR 603.3d), by the watcher's
                // controller; "that much" is the damage dealt.
                gameData.queueInteraction(new PermanentChoiceContext.AttackTriggerTarget(
                        watcher.sourceCard(), watcher.controllerId(), List.of(resolvedEffect),
                        creature.getId(), watcher.controllerId(), damagedPlayerId, creature.getId())
                        .withEventValue(damageDealt).withTriggeringPlayer(damagedPlayerId));
                gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
                continue;
            }
            gameData.enqueueTrigger(entry);
            gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
            log.info("Game {} - {} temporary global ON_ANY_CREATURE_COMBAT_DAMAGE_TO_PLAYER trigger fires",
                    gameData.id, watcher.sourceCard().getName());
        }
    }

    public void checkAnyCreatureCombatDamageToOpponentTriggers(GameData gameData, Permanent creature,
                                                               UUID creatureControllerId,
                                                               UUID damagedPlayerId, int damageDealt,
                                                               Set<String> firedOncePerStepAbilities) {
        if (creature == null || creatureControllerId == null || damagedPlayerId == null || damageDealt <= 0) {
            return;
        }

        enqueueTemporaryAnyCreatureCombatDamageToPlayerTriggers(gameData, creature, creatureControllerId,
                damagedPlayerId, damageDealt);

        // The creature dealing combat damage to a player is the event the "triggers an additional time" effects judge.
        TriggerContext event = new TriggerContext.SourceDealsCombatDamage(creature.getCard(), creatureControllerId,
                creature.getId(), damageDealt, damageDealt);
        gameData.forEachPermanent((controllerId, source) -> {
            if (source.isLosesAllAbilitiesUntilEndOfTurn()) {
                return;
            }

            boolean printedAbilitiesLost = gameQueryService.hasLostPrintedAbilities(gameData, source);
            // Each ability, of either watch, is handed whole to the stack door (CR 603.3); its filter, conditions and
            // "one or more" are judged once, for the ability.
            List<List<CombatAbilityReader.CombatEffect>> toOpponent = controllerId.equals(damagedPlayerId)
                    ? List.of()
                    : combatReads(gameData, source, CombatWatch.ON_ANY_CREATURE_COMBAT_DAMAGE_TO_OPPONENT,
                            !printedAbilitiesLost);
            List<List<CombatAbilityReader.CombatEffect>> abilities = new ArrayList<>(toOpponent);
            abilities.addAll(combatReads(gameData, source, CombatWatch.ON_ANY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                    !printedAbilitiesLost));
            FilterContext dealerContext = FilterContext.of(gameData).withSourceControllerId(controllerId);
            for (int abilityIndex = 0; abilityIndex < abilities.size(); abilityIndex++) {
                CombatAbilityReader.CombatEffect read = abilities.get(abilityIndex).getFirst();
                // The ability's place among the source's abilities of its watch, the same whichever player was dealt
                // the damage.
                String abilityKey = source.getId() + (abilityIndex < toOpponent.size()
                        ? ":opponent:" + abilityIndex : ":player:" + (abilityIndex - toOpponent.size()));
                if (read.limit() != TriggerLimit.NONE || read.eachBlocker()) {
                    throw new IllegalStateException("The \"a creature deals combat damage to a player\" collector "
                            + "reads no " + read.options() + ": " + read.ability());
                }
                // "Whenever a [filter] creature deals combat damage to one of your opponents": the trigger's filter.
                if (read.filter() != null
                        && !predicateEvaluationService.matchesPermanentPredicate(creature, read.filter(), dealerContext)) {
                    continue;
                }
                // "Whenever your opponents are dealt combat damage, if any of that damage was dealt by a [filter]":
                // once for the combat damage step, however many creatures dealt it to however many opponents; each
                // such ability of the source on its own (CR 603.3b).
                if (read.oneOrMore() && firedOncePerStepAbilities.contains(abilityKey)) {
                    continue;
                }
                boolean triggered = putEventAbility(gameData, read.ability(), source.getCard(), source, controllerId,
                        ConditionContext.forPermanent(source, controllerId).withTargetId(damagedPlayerId), event,
                        entry -> {
                            entry.setTargetId(damagedPlayerId);
                            entry.setNonTargeting(true);
                            if (!read.oneOrMore()) {
                                entry.setTriggeringPermanentId(creature.getId());
                                entry.setTriggeringPermanentControllerId(creatureControllerId);
                            }
                        });
                if (triggered && read.oneOrMore()) {
                    firedOncePerStepAbilities.add(abilityKey);
                }
            }
        });

        if (gameData.planechase == null || gameData.planechase.controllerId == null
                || gameData.planechase.controllerId.equals(damagedPlayerId)) {
            return;
        }
        UUID planarControllerId = gameData.planechase.controllerId;
        for (PlanarObject object : List.copyOf(gameData.planechase.faceUp)) {
            putPlanarCombatDamageAbilities(gameData, object, planarControllerId,
                    CombatWatch.ON_ANY_CREATURE_COMBAT_DAMAGE_TO_OPPONENT, creature, creatureControllerId);
        }
    }

    /**
     * Hands each ability of the face-up planar card {@code object} that {@code watch} reads to the stack door under
     * {@code controllerId}, its event "a creature deals combat damage" by {@code creature}: the record names "that
     * creature's controller" as the non-targeting target (CR 115.10a), the creature as the triggering permanent, and
     * the planar object the ability is of.
     */
    private void putPlanarCombatDamageAbilities(GameData gameData, PlanarObject object, UUID controllerId,
                                                CombatWatch watch, Permanent creature, UUID creatureControllerId) {
        Card card = object.getCard();
        ConditionContext context = ConditionContext.forCard(card, controllerId)
                .withTriggeringPermanentId(creature.getId());
        for (TriggeredAbility ability : WatchedAbilities.printed(card, watch)) {
            if (CombatAbilityReader.effects(List.of(ability), watch.trigger()).isEmpty()
                    || !conditionEvaluationService.eventConditionMet(gameData, ability.options(), context)) {
                continue;
            }
            PlanarObject source = object.copy();
            boolean triggered = triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(ability, card,
                    null, controllerId, card.getName() + "'s triggered ability", context, entry -> {
                        entry.setTargetId(creatureControllerId);
                        entry.setNonTargeting(true);
                        entry.setTriggeringPermanentId(creature.getId());
                        entry.setTriggeringPermanentControllerId(creatureControllerId);
                        entry.setSourcePlanarObject(source);
                    }));
            if (triggered) {
                gameLogService.append(gameData, GameLog.abilityTriggers(card));
            }
        }
    }

    /**
     * Hands the "when you're dealt combat damage" abilities of the damaged player's permanents to the stack door,
     * once for the combat damage dealt to that player in a damage step, the damage as their event value.
     */
    public void checkControllerDealtCombatDamageTriggers(GameData gameData, UUID damagedPlayerId, int damageDealt) {
        if (damagedPlayerId == null || damageDealt <= 0) {
            return;
        }
        CombatWatch watch = CombatWatch.ON_CONTROLLER_DEALT_COMBAT_DAMAGE;
        TriggerContext event = new TriggerContext.DamageToControllerAmount(damagedPlayerId, damageDealt);
        for (Permanent source : new ArrayList<>(gameData.playerBattlefields.getOrDefault(damagedPlayerId, List.of()))) {
            if (source.isLosesAllAbilitiesUntilEndOfTurn()) {
                continue;
            }
            for (TriggeredAbility ability : printedWatchedAbilities(gameData, source, watch)) {
                requirePlainCombatAbility(ability, watch);
                putEventAbility(gameData, ability, source.getCard(), source, damagedPlayerId,
                        ConditionContext.forPermanent(source, damagedPlayerId).withEventValue(damageDealt), event,
                        entry -> {
                            entry.setNonTargeting(true);
                            entry.setEventValue(damageDealt);
                        });
            }
        }
    }

    public void checkAnyCreatureCombatDamageToOwnerTriggers(GameData gameData, Permanent creature,
                                                            UUID creatureControllerId,
                                                            UUID damagedPlayerId, int damageDealt) {
        if (creature == null || creatureControllerId == null || damagedPlayerId == null || damageDealt <= 0
                || !gameQueryService.isCreature(gameData, creature)) {
            return;
        }
        UUID creatureOwnerId = gameData.playerCommanders.entrySet().stream()
                .filter(entry -> entry.getValue().stream()
                        .anyMatch(card -> card.getId().equals(creature.getOriginalCard().getId())))
                .map(Map.Entry::getKey).findFirst().orElse(creature.getCard().getOwnerId());
        if (!damagedPlayerId.equals(creatureOwnerId)) return;

        TriggerContext event = new TriggerContext.SourceDealsCombatDamage(creature.getCard(), creatureControllerId,
                creature.getId(), damageDealt, damageDealt);
        gameData.forEachPermanent((controllerId, source) -> {
            if (source.isLosesAllAbilitiesUntilEndOfTurn()) {
                return;
            }

            FilterContext dealerContext = FilterContext.of(gameData).withSourceControllerId(controllerId);
            // Each ability is handed whole to the stack door (CR 603.3), its filter judged once.
            for (List<CombatAbilityReader.CombatEffect> reads : combatReads(gameData, source,
                    CombatWatch.ON_ANY_CREATURE_COMBAT_DAMAGE_TO_OWNER,
                    !gameQueryService.hasLostPrintedAbilities(gameData, source))) {
                CombatAbilityReader.CombatEffect read = reads.getFirst();
                CombatAbilityReader.requireNoLimitOrMultiplicity(read,
                        CombatWatch.ON_ANY_CREATURE_COMBAT_DAMAGE_TO_OWNER.trigger());
                // "Whenever a [filter] creature deals combat damage to its owner": the trigger's filter.
                if (read.filter() != null
                        && !predicateEvaluationService.matchesPermanentPredicate(creature, read.filter(), dealerContext)) {
                    continue;
                }
                putEventAbility(gameData, read.ability(), source.getCard(), source, controllerId,
                        ConditionContext.forPermanent(source, controllerId), event, entry -> {
                            entry.setTriggeringPermanentId(creature.getId());
                            entry.setTriggeringPermanentControllerId(creatureControllerId);
                            entry.setNonTargeting(true);
                        });
            }
        });
    }

    /**
     * Fires the batched trigger for one or more goaded creatures dealing combat damage to an
     * opponent. The damage dealers are supplied as one combat-damage batch so multiple goaded
     * creatures cause only one trigger.
     */
    public void checkGoadedCreatureCombatDamageToOpponentTriggers(GameData gameData,
                                                                   UUID damagedPlayerId,
                                                                   List<Permanent> damageDealers) {
        if (damagedPlayerId == null || damageDealers == null || damageDealers.isEmpty()) {
            return;
        }
        Permanent matchingDealer = damageDealers.stream()
                .filter(creature -> gameQueryService.isGoaded(gameData, creature))
                .findFirst()
                .orElse(null);
        if (matchingDealer == null) {
            return;
        }
        UUID dealerControllerId = gameQueryService.findPermanentController(gameData, matchingDealer.getId());
        TriggerContext event = new TriggerContext.SourceDealsCombatDamage(matchingDealer.getCard(), dealerControllerId,
                matchingDealer.getId(), 1, 1);

        gameData.forEachPermanent((controllerId, source) -> {
            if (controllerId.equals(damagedPlayerId)
                    || source.isLosesAllAbilitiesUntilEndOfTurn()
                    || gameQueryService.hasLostPrintedAbilities(gameData, source)) {
                return;
            }

            // "One or more goaded creatures" is the trigger's own: once for the combat damage dealt together.
            for (List<CombatAbilityReader.CombatEffect> reads : combatReadsWithGrants(gameData, source,
                    CombatWatch.ON_GOADED_CREATURES_COMBAT_DAMAGE_TO_OPPONENT)) {
                CombatAbilityReader.CombatEffect read = reads.getFirst();
                if (read.limit() != TriggerLimit.NONE || read.eachBlocker()) {
                    throw new IllegalStateException("The goaded-creatures collector reads no " + read.options() + ": "
                            + read.ability());
                }
                putEventAbility(gameData, read.ability(), source.getCard(), source, controllerId,
                        ConditionContext.forPermanent(source, controllerId), event,
                        entry -> entry.setNonTargeting(true));
            }
        });
    }

    /**
     * The emblems' "whenever a creature you control deals combat damage to a player" abilities
     * ({@code Triggers.creatureYouControlDealsCombatDamageToPlayer()}; Kaito Shizuki's emblem) for {@code creature},
     * which {@code creatureControllerId} controls, dealing {@code damageDealt} combat damage to {@code damagedPlayerId}.
     * Abilities of emblems function in the command zone (CR 114.4). Each one goes on the stack once as one object of its
     * effects (CR 603.3), the emblem's source card as its source, and records the event whatever its effects are: the
     * creature as its triggering permanent, the player dealt damage as its triggering player and the damage as its
     * event value. Nothing makes an emblem's ability trigger additional times (CR 110.1: an emblem isn't a permanent);
     * the reader judges no filter or options, and an ability with one fails loudly instead of triggering wrongly.
     */
    public void checkEmblemAllyCreatureCombatDamageToPlayerTriggers(GameData gameData, Permanent creature,
                                                                     UUID creatureControllerId, UUID damagedPlayerId,
                                                                     int damageDealt) {
        if (creature == null || creatureControllerId == null || damageDealt <= 0) {
            return;
        }
        Trigger watched = Triggers.creatureYouControlDealsCombatDamageToPlayer();
        for (Emblem emblem : List.copyOf(gameData.emblems)) {
            if (!emblem.controllerId().equals(creatureControllerId)) {
                continue;
            }
            for (TriggeredAbility ability : emblem.triggeredAbilities()) {
                if (ability.conditions().stream().noneMatch(condition -> condition.trigger().equals(watched))) {
                    continue;
                }
                if (!ability.options().equals(TriggerOptions.DEFAULT)) {
                    throw new IllegalStateException(emblemName(emblem) + " has a combat damage ability its reader "
                            + "doesn't judge: " + ability);
                }
                Card source = emblem.sourceCard() != null ? emblem.sourceCard() : creature.getCard();
                String description = emblemName(emblem);
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        source,
                        emblem.controllerId(),
                        description,
                        new ArrayList<>(ability.effects().stream().map(EffectRegistration::effect).toList()));
                entry.setTriggeringPermanentId(creature.getId());
                entry.setTriggeringPermanentControllerId(creatureControllerId);
                entry.setTriggeringPlayerId(damagedPlayerId);
                entry.setEventValue(damageDealt);
                entry.setNonTargeting(true);
                gameData.stack.add(entry);
                gameLogService.append(gameData, GameLog.text(description + " triggers."));
                log.info("Game {} - {} triggers for combat damage dealt to a player", gameData.id, description);
            }
        }
    }

    /**
     * Whether {@code subject}, the permanent a damage ability's filter is on (the damage source, the dealer, the
     * damaged permanent), matches {@code filter}, judged with {@code watcher} as the ability's source; {@code false}
     * when there is no such permanent.
     */
    private boolean damageFilterMatches(GameData gameData, Permanent watcher, UUID controllerId,
                                        PermanentPredicate filter, Permanent subject) {
        return subject != null && predicateEvaluationService.matchesPermanentPredicate(subject, filter,
                FilterContext.of(gameData)
                        .withSourceCardId(watcher.getCard().getId())
                        .withSourceControllerId(controllerId)
                        .withSourcePermanentId(watcher.getId())
                        .withSourcePermanentSnapshot(watcher));
    }

    public void checkSourceDealsDamageToPlayerTriggers(GameData gameData, Permanent source,
                                                       UUID controllerId, UUID damagedPlayerId,
                                                       int damageDealt) {
        if (source == null || controllerId == null || damagedPlayerId == null || damageDealt <= 0) {
            return;
        }

        // An effect that removes all abilities removes the source's own triggered abilities, so damage dealt after
        // that loss does not trigger them.
        boolean sourceHasAbilities = !gameQueryService.hasLostAllAbilities(gameData, source);
        DamageWatch toPlayer = DamageWatch.ON_DAMAGE_TO_PLAYER;
        DamageWatch toOpponent = DamageWatch.ON_DAMAGE_TO_OPPONENT;
        TriggerContext event = new TriggerContext.DamageToControllerAmount(damagedPlayerId, damageDealt,
                source.getId(), controllerId);
        // Each ability, printed or granted, of either watch, is one object of all its effects (CR 603.3).
        if (sourceHasAbilities) {
            List<TriggeredAbility> toPlayerAbilities = new ArrayList<>(
                    DamageAbilityReader.printed(source.getCard(), toPlayer));
            toPlayerAbilities.addAll(grantedTriggeredAbilitySupport.grantedWatchedAbilities(gameData, source,
                    toPlayer.trigger()));
            for (TriggeredAbility ability : toPlayerAbilities) {
                putDealsDamageToPlayerAbility(gameData, ability, toPlayer, source, controllerId, damagedPlayerId,
                        damageDealt, event);
            }
            if (!controllerId.equals(damagedPlayerId)) {
                List<TriggeredAbility> toOpponentAbilities = new ArrayList<>(
                        DamageAbilityReader.printed(source.getCard(), toOpponent));
                toOpponentAbilities.addAll(WatchedAbilities.temporary(source, toOpponent.trigger()));
                toOpponentAbilities.addAll(WatchedAbilities.persistent(source, toOpponent.trigger()));
                toOpponentAbilities.addAll(grantedTriggeredAbilitySupport.grantedWatchedAbilities(gameData, source,
                        toOpponent.trigger()));
                for (TriggeredAbility ability : toOpponentAbilities) {
                    putDealsDamageToPlayerAbility(gameData, ability, toOpponent, source, controllerId,
                            damagedPlayerId, damageDealt, event);
                }
            }
        }

        // Auras/Equipment attached to the source with ON_DAMAGE_TO_PLAYER (e.g. Curiosity).
        gameData.forEachPermanent((ownerId, perm) -> {
            if (!perm.isAttached() || perm.getAttachedTo() == null
                    || !perm.getAttachedTo().equals(source.getId())) {
                return;
            }
            // Attached triggers are controlled by the Aura/Equipment's controller.
            UUID attachedControllerId = gameData.findControllerOf(perm);
            if (attachedControllerId == null) return;
            for (TriggeredAbility ability : DamageAbilityReader.printed(perm.getCard(), toPlayer)) {
                putDealsDamageToPlayerAbility(gameData, ability, toPlayer, perm, attachedControllerId,
                        damagedPlayerId, damageDealt, event);
            }
            if (!ownerId.equals(damagedPlayerId)) {
                for (TriggeredAbility ability : DamageAbilityReader.printed(perm.getCard(), toOpponent)) {
                    putDealsDamageToPlayerAbility(gameData, ability, toOpponent, perm, attachedControllerId,
                            damagedPlayerId, damageDealt, event);
                }
            }
        });
    }

    /**
     * Hands a "this deals damage to a player / an opponent" ability of {@code source} to the stack door: its event
     * condition ("deals 6 or more damage", "deals damage to the player who cast Hellish Rebuke", CR 603.2) is judged
     * with the damage as the event's value and the player dealt damage as "that player", and its record names, whatever
     * its effects are, that player as its triggering player and non-targeting "that player", the damage as its event
     * value ("that much"); a target is chosen as it goes on the stack (CR 603.3d), its "that player controls" judged
     * against the player dealt damage.
     */
    private void putDealsDamageToPlayerAbility(GameData gameData, TriggeredAbility ability, DamageWatch watch,
                                               Permanent source, UUID controllerId, UUID damagedPlayerId, int damage,
                                               TriggerContext event) {
        requireNothingToJudge(ability, watch);
        Permanent snapshot = new Permanent(source);
        putEventAbility(gameData, ability, source.getCard(), source, controllerId,
                ConditionContext.forPermanent(source, controllerId).withTargetId(damagedPlayerId)
                        .withEventValue(damage), event, entry -> {
                    entry.setSourcePermanentSnapshot(snapshot);
                    entry.setNonTargeting(true);
                    entry.setTargetId(damagedPlayerId);
                    entry.setEventValue(damage);
                    entry.setTriggeringPlayerId(damagedPlayerId);
                    entry.setAttackedTargetId(damagedPlayerId);
                });
    }

    // ── Damage-dealt-to-controller triggers ────────────────────────────

    public void checkDamageDealtToControllerTriggers(GameData gameData, UUID damagedPlayerId, UUID sourcePermanentId, boolean isCombatDamage) {
        checkDamageDealtToControllerTriggers(gameData, damagedPlayerId, sourcePermanentId, isCombatDamage, null);
    }

    public void checkDamageDealtToControllerTriggers(GameData gameData, UUID damagedPlayerId,
                                                     UUID sourcePermanentId, boolean isCombatDamage,
                                                     Set<String> firedBatchedTriggerKeys) {
        if (sourcePermanentId == null) return;

        List<Permanent> damagedPlayerBattlefield = gameData.playerBattlefields.get(damagedPlayerId);
        boolean hasPlanarCombatDamageTrigger = isCombatDamage
                && hasPlanarCreatureCombatDamageToYouTrigger(gameData, damagedPlayerId);
        boolean hasPlanarDamageTrigger = hasPlanarPermanentDamageToYouTrigger(gameData, damagedPlayerId);
        if (damagedPlayerBattlefield == null && !hasPlanarCombatDamageTrigger && !hasPlanarDamageTrigger) return;

        boolean hasTrigger = false;
        if (damagedPlayerBattlefield != null) {
            for (Permanent perm : gameData.permanentsAndFaceUpSchemes(damagedPlayerId)) {
                if (!DamageAbilityReader.printed(perm.getCard(), DamageWatch.ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU).isEmpty()
                        || (isCombatDamage && !WatchedAbilities.printed(perm.getCard(),
                                CombatWatch.ON_CREATURE_DEALS_COMBAT_DAMAGE_TO_YOU).isEmpty())) {
                    hasTrigger = true;
                    break;
                }
            }
        }
        if (!hasTrigger && !hasPlanarCombatDamageTrigger && !hasPlanarDamageTrigger) return;

        Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (sourcePermanent == null) return;

        checkPlanarPermanentDealsDamageToYouTriggers(gameData, damagedPlayerId, sourcePermanent, isCombatDamage);

        if (isCombatDamage) {
            if (damagedPlayerBattlefield != null) {
                queueCreatureCombatDamageToYouTriggers(
                        gameData, damagedPlayerId, sourcePermanent, firedBatchedTriggerKeys);
            }
            queuePlanarCreatureCombatDamageToYouTriggers(gameData, damagedPlayerId, sourcePermanent);
        }

        if (damagedPlayerBattlefield == null) return;

        var ctx = new TriggerContext.DamageToController(damagedPlayerId, sourcePermanentId, isCombatDamage);
        DamageWatch watch = DamageWatch.ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU;
        Trigger watched = watch.trigger();
        UUID sourceControllerId = gameQueryService.findPermanentController(gameData, sourcePermanentId);

        for (Permanent perm : new ArrayList<>(gameData.permanentsAndFaceUpSchemes(damagedPlayerId))) {
            for (TriggeredAbility ability : printedWatchedAbilities(gameData, perm, watch)) {
                // If the source was bounced, stop processing all triggers
                Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
                if (source == null) return;
                requireEachEvent(ability, watch);
                // "Whenever a creature / a Human deals damage to you": the trigger's filter on the dealer.
                PermanentPredicate filter = DamageAbilityReader.filter(ability, watched);
                if (filter != null && !damageFilterMatches(gameData, perm, damagedPlayerId, filter, source)) continue;
                putEventAbility(gameData, ability, perm.getCard(), perm, damagedPlayerId,
                        ConditionContext.forPermanent(perm, damagedPlayerId)
                                .withTriggeringPermanentId(sourcePermanentId), ctx,
                        damageEventRecord(perm, null, source.getId(), sourceControllerId));
            }
        }
    }

    /**
     * The record of a damage event whose object is the permanent {@code triggeringPermanentId} (the source of the
     * damage, the creature that dealt it, the creature dealt it), whatever the ability's effects are: that permanent as
     * its triggering permanent with its controller as the event happened ({@code triggeringPermanentControllerId}, the
     * fallback once it has left and the controller of a spell source), the damage, when the event has one, as its event
     * value, and {@code watcher} as it was when the ability triggered. The effects read them as they resolve ("destroy
     * it", "that much damage to that creature", "that creature's or spell's controller").
     */
    private static TriggerRecord damageEventRecord(Permanent watcher, Integer damage, UUID triggeringPermanentId,
                                                   UUID triggeringPermanentControllerId) {
        Permanent snapshot = new Permanent(watcher);
        return entry -> {
            entry.setNonTargeting(true);
            if (damage != null) {
                entry.setEventValue(damage);
            }
            entry.setTriggeringPermanentId(triggeringPermanentId);
            entry.setTriggeringPermanentControllerId(triggeringPermanentControllerId);
            entry.setSourcePermanentSnapshot(snapshot);
        };
    }

    /**
     * Hands every {@link CombatWatch#ON_CREATURE_DEALS_COMBAT_DAMAGE_TO_YOU} ability on the damaged
     * player's battlefield and face-up schemes (CR 314.4) to the stack door when a creature dealt them combat damage.
     * The record names the damaging creature as the non-targeting {@code targetId}, so "destroy that creature" is
     * expressed with {@code DestroyTargetPermanentEffect} while the ability itself does not target (CR 115.10a), and
     * as its triggering permanent ("that creature's controller gains control of this land"). An intervening "if" is
     * judged by the door as the ability triggers (CR 603.4; Hixus's "if Hixus entered this turn") and again as it
     * resolves.
     */
    private void queueCreatureCombatDamageToYouTriggers(GameData gameData, UUID damagedPlayerId,
                                                        Permanent sourceCreature,
                                                        Set<String> firedBatchedTriggerKeys) {
        if (!gameQueryService.isCreature(gameData, sourceCreature)) return;

        CombatWatch watch = CombatWatch.ON_CREATURE_DEALS_COMBAT_DAMAGE_TO_YOU;
        TriggerContext event = new TriggerContext.DamageToController(damagedPlayerId, sourceCreature.getId(), true);
        UUID creatureControllerId = gameQueryService.findPermanentController(gameData, sourceCreature.getId());
        for (Permanent perm : new ArrayList<>(gameData.permanentsAndFaceUpSchemes(damagedPlayerId))) {
            List<TriggeredAbility> abilities = printedWatchedAbilities(gameData, perm, watch);
            for (int abilityIndex = 0; abilityIndex < abilities.size(); abilityIndex++) {
                TriggeredAbility ability = abilities.get(abilityIndex);
                CombatAbilityReader.CombatEffect read = CombatAbilityReader.read(ability, watch.trigger()).getFirst();
                if (read.limit() != TriggerLimit.NONE || read.eachBlocker()) {
                    throw new IllegalStateException("The \"a creature deals combat damage to you\" collector reads no "
                            + read.options() + ": " + ability);
                }
                // "Whenever a [filter] creature deals combat damage to you": the trigger's filter on the creature.
                if (read.filter() != null && !predicateEvaluationService.matchesPermanentPredicate(sourceCreature,
                        read.filter(), watcherFilterContext(gameData, perm, damagedPlayerId))) {
                    continue;
                }
                // "One or more creatures deal combat damage to you" triggers once for the damage step, each such
                // ability of the permanent on its own (CR 603.3b).
                if (read.oneOrMore() && firedBatchedTriggerKeys != null
                        && firedBatchedTriggerKeys.contains(perm.getId() + ":" + abilityIndex)) {
                    continue;
                }
                Permanent snapshot = new Permanent(perm);
                boolean triggered = putEventAbility(gameData, ability, perm.getCard(), perm, damagedPlayerId,
                        ConditionContext.forPermanent(perm, damagedPlayerId), event, entry -> {
                            entry.setTargetId(sourceCreature.getId());
                            entry.setNonTargeting(true);
                            entry.setSourcePermanentSnapshot(snapshot);
                            entry.setTriggeringPermanentId(sourceCreature.getId());
                            entry.setTriggeringPermanentControllerId(creatureControllerId);
                        });
                if (triggered && read.oneOrMore() && firedBatchedTriggerKeys != null) {
                    firedBatchedTriggerKeys.add(perm.getId() + ":" + abilityIndex);
                }
            }
        }
    }

    // ── Enchanted-creature-deals-damage-to-you reflect triggers (Backfire) ──

    /**
     * Handles {@link DamageWatch#ON_ENCHANTED_CREATURE_DEALS_DAMAGE_TO_YOU} — "Whenever enchanted creature
     * deals damage to you, this Aura deals that much damage to that creature's controller" (Backfire).
     *
     * <p>The aura is on its controller's battlefield, so scanning the damaged player's battlefield for auras
     * attached to the damage source naturally restricts the trigger to damage dealt to the aura's controller
     * ("to you"). Called for both combat and non-combat damage dealt to a player. The ability is the Aura's (CR
     * 113.7); its record names the event whatever the effects are: the enchanted creature as the triggering permanent
     * (its id, controller and a snapshot as it dealt the damage) and the damage as the event value, which "that much
     * damage to that creature's controller" reads as the ability resolves.
     */
    public void checkEnchantedCreatureDealtDamageToControllerReflectTriggers(GameData gameData, UUID damagedPlayerId,
            UUID sourcePermanentId, int amount) {
        if (sourcePermanentId == null || amount <= 0) return;

        List<Permanent> damagedPlayerBattlefield = gameData.playerBattlefields.get(damagedPlayerId);
        if (damagedPlayerBattlefield == null) return;

        Permanent enchantedCreature = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (enchantedCreature == null) return;

        DamageWatch watch = DamageWatch.ON_ENCHANTED_CREATURE_DEALS_DAMAGE_TO_YOU;
        TriggerContext event = new TriggerContext.DamageToControllerAmount(damagedPlayerId, amount, sourcePermanentId);
        for (Permanent aura : new ArrayList<>(damagedPlayerBattlefield)) {
            if (!aura.isAttached() || !sourcePermanentId.equals(aura.getAttachedTo())) continue;
            UUID creatureControllerId = gameQueryService.findPermanentController(gameData, sourcePermanentId);
            if (creatureControllerId == null) continue;

            Permanent auraSnapshot = new Permanent(aura);
            for (TriggeredAbility ability : printedWatchedAbilities(gameData, aura, watch)) {
                requireNothingToJudge(ability, watch);
                putEventAbility(gameData, ability, aura.getCard(), aura, damagedPlayerId,
                        ConditionContext.forPermanent(aura, damagedPlayerId).withEventValue(amount), event, entry -> {
                            entry.setEventValue(amount);
                            entry.setTriggeringPermanentId(enchantedCreature.getId());
                            entry.setTriggeringCardId(enchantedCreature.getCard().getId());
                            entry.setTriggeringPermanentControllerId(creatureControllerId);
                            entry.setTriggeringPermanent(enchantedCreature);
                            entry.setTriggeringPlayerId(damagedPlayerId);
                            entry.setSourcePermanentSnapshot(auraSnapshot);
                            entry.setNonTargeting(true);
                        });
            }
        }
    }

    // ── Controller-dealt-damage triggers (Living Artifact) ─────────────

    /**
     * Handles {@link DamageWatch#ON_CONTROLLER_DEALT_DAMAGE} — "Whenever you're dealt damage, ...".
     * Fires once per damage source (per the CR ruling that simultaneous sources trigger separately),
     * carrying the amount so an {@code EventValue} amount ("put that many counters") can read it.
     * Scans the damaged player's own battlefield.
     * <p>
     * Also handles {@link DamageWatch#ON_CONTROLLER_DEALT_DAMAGE_BY_OPPONENT} — "Whenever a source an
     * opponent controls deals damage to you, ..." (Retaliator Griffin) — but only when the damage
     * source is controlled by an opponent of the damaged player. {@code sourceControllerId} is the
     * controller of the damage source (the active player for combat damage to the defender, the
     * spell/ability's controller for non-combat damage); {@code null} disables the opponent-gated slot.
     */
    public void checkControllerDealtDamageTriggers(GameData gameData, UUID damagedPlayerId,
            UUID sourceControllerId, int amount) {
        checkControllerDealtDamageTriggers(gameData, damagedPlayerId, sourceControllerId, amount, true);
    }

    public void checkControllerDealtDamageTriggers(GameData gameData, UUID damagedPlayerId,
            UUID sourceControllerId, int amount, boolean includeAllSources) {
        if (amount <= 0) return;

        List<Permanent> damagedPlayerBattlefield = gameData.playerBattlefields.get(damagedPlayerId);
        if (damagedPlayerBattlefield == null) return;

        var ctx = new TriggerContext.DamageToControllerAmount(damagedPlayerId, amount, null,
                sourceControllerId);
        boolean fromOpponent = sourceControllerId != null && !sourceControllerId.equals(damagedPlayerId);
        boolean fromAllySource = sourceControllerId != null && sourceControllerId.equals(damagedPlayerId);

        for (Permanent perm : List.copyOf(damagedPlayerBattlefield)) {
            if (includeAllSources) {
                putPlayerDamageAbilities(gameData, perm, damagedPlayerId, DamageWatch.ON_CONTROLLER_DEALT_DAMAGE,
                        ctx, damagedPlayerId, damagedPlayerId, amount, null, sourceControllerId);
            }
            if (fromAllySource) {
                // "Whenever a source you control deals damage to you" (Daredevil, Fearless Fighter's "target
                // opponent"): the damage as its event value; its limit is the door's.
                DamageWatch allySource = DamageWatch.ON_CONTROLLER_DEALT_DAMAGE_BY_ALLY_SOURCE;
                for (TriggeredAbility ability : printedWatchedAbilities(gameData, perm, allySource)) {
                    requireNothingToJudge(ability, allySource);
                    putEventAbility(gameData, ability, perm.getCard(), perm, damagedPlayerId,
                            ConditionContext.forPermanent(perm, damagedPlayerId).withEventValue(amount), ctx,
                            entry -> entry.setEventValue(amount));
                }
            }
            if (fromOpponent) {
                putPlayerDamageAbilities(gameData, perm, damagedPlayerId,
                        DamageWatch.ON_CONTROLLER_DEALT_DAMAGE_BY_OPPONENT, ctx, sourceControllerId, damagedPlayerId,
                        amount, null, sourceControllerId);
            }
        }
    }

    /**
     * Hands each printed ability {@code watch} reads of {@code perm} for damage dealt to a player to the stack door
     * (see {@link #playerDamageRecord}); these watches' collectors judge no filter of their own.
     */
    private void putPlayerDamageAbilities(GameData gameData, Permanent perm, UUID controllerId, DamageWatch watch,
                                          TriggerContext event, UUID thatPlayerId, UUID damagedPlayerId, int amount,
                                          UUID sourcePermanentId, UUID sourceControllerId) {
        for (TriggeredAbility ability : printedWatchedAbilities(gameData, perm, watch)) {
            requireNothingToJudge(ability, watch);
            putEventAbility(gameData, ability, perm.getCard(), perm, controllerId,
                    ConditionContext.forPermanent(perm, controllerId).withTargetId(damagedPlayerId)
                            .withTriggeringPermanentId(sourcePermanentId).withEventValue(amount), event,
                    playerDamageRecord(perm, null, thatPlayerId, damagedPlayerId, amount, sourcePermanentId,
                            sourceControllerId));
        }
    }

    /**
     * The record of a damage-to-a-player ability, whatever its effects are: {@code thatPlayerId} as its non-targeting
     * "that player" (the player dealt damage, or the opponent who controlled the source: Michiko Konda's "that player
     * sacrifices a permanent"), the damage as its event value ("that much", "that many"), the player dealt damage as
     * its triggering player and the player a target "that player controls" is judged against, the source of the
     * damage, when it is a permanent, as its triggering permanent and the source's controller as it dealt the damage as
     * that permanent's controller ("that source's controller", the fallback once it has left and the controller of a
     * spell source), and the ability's source as it was ({@code watcher}, or the face-up plane {@code planarObject}).
     * A target the card declares is chosen as the ability goes on the stack (CR 603.3d); a player the card declares no
     * target for is "that player" (Pain Magnification's "that player discards a card").
     */
    private static TriggerRecord playerDamageRecord(Permanent watcher, PlanarObject planarObject, UUID thatPlayerId,
                                                    UUID damagedPlayerId, int amount, UUID sourcePermanentId,
                                                    UUID sourceControllerId) {
        Permanent snapshot = watcher == null ? null : new Permanent(watcher);
        PlanarObject plane = planarObject == null ? null : planarObject.copy();
        return entry -> {
            entry.setNonTargeting(true);
            entry.setTargetId(thatPlayerId);
            entry.setEventValue(amount);
            entry.setTriggeringPlayerId(damagedPlayerId);
            entry.setAttackedTargetId(damagedPlayerId);
            entry.setTriggeringPermanentId(sourcePermanentId);
            entry.setTriggeringPermanentControllerId(sourceControllerId);
            if (snapshot != null) {
                entry.setSourcePermanentSnapshot(snapshot);
            }
            if (plane != null) {
                entry.setSourcePlanarObject(plane);
            }
        };
    }

    /**
     * Handles {@link DamageWatch#ON_ALLY_SOURCE_DEALS_DAMAGE_TO_OPPONENT} — "Whenever a source you
     * control deals damage to another player, ..." (Night Dealings). The outbound mirror of
     * {@link #checkControllerDealtDamageTriggers}: it scans the damage source's controller's own
     * battlefield, and only fires when the damaged player is someone else.
     * The source permanent ID is carried for effects that are self-scoped within this slot.
     */
    public void checkAllySourceDealtDamageToOpponentTriggers(GameData gameData, UUID damagedPlayerId,
            UUID sourceControllerId, int amount) {
        checkAllySourceDealtDamageToOpponentTriggers(gameData, damagedPlayerId, sourceControllerId, null, amount);
    }

    public void checkAllySourceDealtNoncombatDamageToOpponentTriggers(GameData gameData, UUID damagedPlayerId,
            UUID sourceControllerId, int amount) {
        if (amount <= 0 || sourceControllerId == null || sourceControllerId.equals(damagedPlayerId)) return;
        List<Permanent> battlefield = gameData.playerBattlefields.get(sourceControllerId);
        if (battlefield == null) return;
        var ctx = new TriggerContext.NoncombatDamageToOpponent(damagedPlayerId, sourceControllerId, amount);
        for (Permanent permanent : List.copyOf(battlefield)) {
            putPlayerDamageAbilities(gameData, permanent, sourceControllerId,
                    DamageWatch.ON_ALLY_SOURCE_DEALS_NONCOMBAT_DAMAGE_TO_OPPONENT, ctx, damagedPlayerId,
                    damagedPlayerId, amount, null, sourceControllerId);
        }
    }

    public void checkAllySourceDealtDamageToOpponentTriggers(GameData gameData, UUID damagedPlayerId,
            UUID sourceControllerId, UUID sourcePermanentId, int amount) {
        if (amount <= 0 || sourceControllerId == null) return;

        if (!sourceControllerId.equals(damagedPlayerId)) {
            checkPlanarAllySourceDealtDamageToOpponentTriggers(
                    gameData, damagedPlayerId, sourceControllerId, sourcePermanentId, amount);
        } else {
            checkPlanarSourceDealtDamageToItsControllerTriggers(
                    gameData, damagedPlayerId, sourcePermanentId, amount);
        }

        List<Permanent> sourceControllerBattlefield = gameData.playerBattlefields.get(sourceControllerId);
        if (sourceControllerBattlefield == null) return;

        var ctx = new TriggerContext.DamageToControllerAmount(damagedPlayerId, amount, sourcePermanentId,
                sourceControllerId);
        for (Permanent perm : List.copyOf(sourceControllerBattlefield)) {
            for (DamageWatch watch : List.of(DamageWatch.ON_ALLY_SOURCE_DEALS_DAMAGE_TO_OPPONENT,
                    DamageWatch.ON_ALLY_SOURCE_DEALS_DAMAGE_TO_PLAYER)) {
                if (watch == DamageWatch.ON_ALLY_SOURCE_DEALS_DAMAGE_TO_OPPONENT
                        && sourceControllerId.equals(damagedPlayerId)) continue;
                for (TriggeredAbility ability : printedWatchedAbilities(gameData, perm, watch)) {
                    requireEachEvent(ability, watch);
                    // "Whenever a [filtered] source you control deals damage": the filter is on the source.
                    PermanentPredicate filter = DamageAbilityReader.filter(ability, watch.trigger());
                    if (filter != null && !damageFilterMatches(gameData, perm, sourceControllerId, filter,
                            sourcePermanentId == null ? null
                                    : gameQueryService.findPermanentById(gameData, sourcePermanentId))) {
                        continue;
                    }
                    // The ability's event condition and intervening "if", with the damage and its source (Dragonborn
                    // Champion's "5 or more damage", Talon of Pain's "other than this artifact", CR 603.4).
                    putEventAbility(gameData, ability, perm.getCard(), perm, sourceControllerId,
                            ConditionContext.forPermanent(perm, sourceControllerId)
                                    .withTriggeringPermanentId(sourcePermanentId).withEventValue(amount), ctx,
                            playerDamageRecord(perm, null, damagedPlayerId, damagedPlayerId, amount,
                                    sourcePermanentId, sourceControllerId));
                }
            }
        }
    }

    /**
     * Handles effects that trigger whenever an opponent is dealt damage, regardless of the damage
     * source's controller.
     */
    public void checkOpponentDealtDamageTriggers(GameData gameData, UUID damagedPlayerId, int amount) {
        checkOpponentDealtDamageTriggers(gameData, damagedPlayerId, null, amount);
    }

    public void checkOpponentDealtDamageTriggers(GameData gameData, UUID damagedPlayerId,
            UUID sourcePermanentId, int amount) {
        if (amount <= 0 || damagedPlayerId == null) return;

        var ctx = new TriggerContext.DamageToControllerAmount(damagedPlayerId, amount, sourcePermanentId);
        gameData.forEachBattlefield((watcherPlayerId, battlefield) -> {
            if (watcherPlayerId.equals(damagedPlayerId)) return;

            for (Permanent perm : List.copyOf(battlefield)) {
                putPlayerDamageAbilities(gameData, perm, watcherPlayerId, DamageWatch.ON_OPPONENT_DEALT_DAMAGE, ctx,
                        damagedPlayerId, damagedPlayerId, amount, sourcePermanentId, null);
            }
        });
    }

    /**
     * Handles {@link DamageWatch#ON_CREATURE_DEALS_DAMAGE_TO_YOU_OR_YOUR_PERMANENT} — "Whenever a
     * creature of the chosen color deals damage to you or a white creature you control, ...".
     * Scans the damaged player's battlefield for watchers and judges each ability's filter on the
     * creature and its damaged filter on the damaged permanent (damage to the player always counts).
     *
     * @param damagedPlayerId  the damaged player, or the damaged permanent's controller
     * @param damagedPermanent the damaged permanent, or {@code null} when the player was damaged
     */
    public void checkCreatureDamageToYouOrYourPermanentTriggers(GameData gameData, UUID damagedPlayerId,
            Permanent damagedPermanent, Permanent damageSource, int damage) {
        if (damage <= 0 || damagedPlayerId == null || damageSource == null) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(damagedPlayerId);
        if (battlefield == null) return;

        if (!gameQueryService.isCreature(gameData, damageSource)) return;

        var ctx = new TriggerContext.CreatureDamageToYouOrYourPermanent(
                damageSource, damagedPlayerId, damagedPermanent, damage);
        DamageWatch watch = DamageWatch.ON_CREATURE_DEALS_DAMAGE_TO_YOU_OR_YOUR_PERMANENT;
        Trigger watched = watch.trigger();
        UUID damageSourceControllerId = gameQueryService.findPermanentController(gameData, damageSource.getId());
        for (Permanent perm : List.copyOf(battlefield)) {
            for (TriggeredAbility ability : printedWatchedAbilities(gameData, perm, watch)) {
                requireEachEvent(ability, watch);
                DamageTrigger trigger = DamageAbilityReader.trigger(ability, watched);
                // "A creature of the chosen color": the trigger's filter on the creature; "to you or a white creature
                // you control": its damaged filter on the permanent, damage to you always counting.
                if (trigger.filter() != null && !damageFilterMatches(gameData, perm, damagedPlayerId,
                        trigger.filter(), damageSource)) {
                    continue;
                }
                if (damagedPermanent != null && trigger.damagedFilter() != null && !damageFilterMatches(gameData,
                        perm, damagedPlayerId, trigger.damagedFilter(), damagedPermanent)) {
                    continue;
                }
                putEventAbility(gameData, ability, perm.getCard(), perm, damagedPlayerId,
                        ConditionContext.forPermanent(perm, damagedPlayerId).withEventValue(damage), ctx,
                        damageEventRecord(perm, damage, damageSource.getId(), damageSourceControllerId));
            }
        }
    }

    /**
     * Fires triggers for each player or permanent recipient of damage during one damage event.
     * A permanent recipient is retained separately from its controller because cards may trigger
     * once for each permanent damaged, in addition to a trigger for damage dealt to the player.
     */
    public void checkOpponentSourceDamageToYouOrYourPermanentTriggers(GameData gameData, Card sourceCard,
            UUID sourceControllerId, UUID sourcePermanentId, Set<UUID> damagedPlayerIds) {
        if (sourceCard == null || sourceControllerId == null || damagedPlayerIds == null
                || damagedPlayerIds.isEmpty()) return;

        List<SourceDamageRecipient> recipients = damagedPlayerIds.stream()
                .filter(Objects::nonNull)
                .map(playerId -> new SourceDamageRecipient(playerId, null))
                .toList();
        checkOpponentSourceDamageToYouOrYourPermanentTriggers(
                gameData, sourceCard, sourceControllerId, sourcePermanentId, recipients);
    }

    public void checkOpponentSourceDamageToYouOrYourPermanentTriggers(GameData gameData, Card sourceCard,
            UUID sourceControllerId, UUID sourcePermanentId, Collection<SourceDamageRecipient> recipients) {
        if (sourceCard == null || sourceControllerId == null || recipients == null || recipients.isEmpty()) return;

        for (SourceDamageRecipient recipient : recipients) {
            // "A source an opponent controls": the damage dealt to its controller or their permanents doesn't count.
            if (recipient == null || recipient.damagedPlayerId() == null
                    || sourceControllerId.equals(recipient.damagedPlayerId())) continue;
            UUID damagedPlayerId = recipient.damagedPlayerId();
            List<Permanent> battlefield = gameData.playerBattlefields.get(damagedPlayerId);
            if (battlefield == null) continue;

            TriggerContext.SourceDamageToYouOrYourPermanent ctx =
                    new TriggerContext.SourceDamageToYouOrYourPermanent(
                            sourceCard, sourceControllerId, sourcePermanentId, damagedPlayerId,
                            recipient.damagedPermanentId());
            for (Permanent perm : List.copyOf(battlefield)) {
                // "That player" is the opponent who controlled the source as it dealt the damage (Elesh Norn's "that
                // source's controller loses 2 life unless they pay {1}").
                for (TriggeredAbility ability : printedWatchedAbilities(gameData, perm,
                        DamageWatch.ON_OPPONENT_SOURCE_DEALS_DAMAGE_TO_YOU_OR_YOUR_PERMANENT)) {
                    requireNothingToJudge(ability, DamageWatch.ON_OPPONENT_SOURCE_DEALS_DAMAGE_TO_YOU_OR_YOUR_PERMANENT);
                    putEventAbility(gameData, ability, perm.getCard(), perm, damagedPlayerId,
                            ConditionContext.forPermanent(perm, damagedPlayerId).withTargetId(damagedPlayerId)
                                    .withTriggeringPermanentId(sourcePermanentId), ctx,
                            playerDamageRecord(perm, null, sourceControllerId, damagedPlayerId, 0,
                                    sourcePermanentId, sourceControllerId));
                }
            }
        }
    }

    public record SourceDamageRecipient(UUID damagedPlayerId, UUID damagedPermanentId) {
    }

    /**
     * Collects {@code watcher}'s printed "a source [you control] deals damage" abilities of {@code watches} for one
     * source's damage in one event. Each ability triggers when the trigger's filters match the source
     * ({@link #damageSourceMatches}) and its event condition holds, and goes to the stack door whole (CR 603.3): the
     * source permanent its triggering permanent, the source's controller as it dealt the damage the fallback for "its
     * controller" (a spell's), the summed damage its event value ({@code eventValue}: the damage the source dealt in
     * all, or to you for "deals damage to you"). An ability with a target has it chosen as the ability goes on the
     * stack (CR 603.3d; Circle of Affliction's "target player").
     */
    private void collectSourceDealsDamageAbilities(GameData gameData, Permanent watcher, UUID controllerId,
            List<DamageWatch> watches, TriggerContext.SourceDealsDamage event, int eventValue) {
        for (DamageWatch watch : watches) {
            Trigger watched = watch.trigger();
            for (TriggeredAbility ability : printedWatchedAbilities(gameData, watcher, watch)) {
                requireEachEvent(ability, watch);
                if (!damageSourceMatches(gameData, watcher, controllerId,
                        DamageAbilityReader.trigger(ability, watched), event)) {
                    continue;
                }
                putEventAbility(gameData, ability, watcher.getCard(), watcher, controllerId,
                        ConditionContext.forPermanent(watcher, controllerId).withEventValue(eventValue), event,
                        damageEventRecord(watcher, eventValue, event.sourcePermanentId(), event.sourceControllerId()));
            }
        }
    }

    /**
     * Collects {@code watcher}'s printed "a source deals damage to a permanent or player" abilities for one source's
     * damage in one event, once for each permanent and player it dealt damage to (CR 603.2c; Ghyrson Starn's ruling):
     * {@link DamageWatch#ON_ANY_SOURCE_DEALS_DAMAGE_TO_PERMANENT_OR_PLAYER} always, and
     * {@link DamageWatch#ON_ANOTHER_ALLY_SOURCE_DEALS_DAMAGE_TO_PERMANENT_OR_PLAYER} when {@code controllerId} controls
     * the source and the watcher isn't it.
     */
    private void collectSourceDealsDamageToPermanentOrPlayerAbilities(GameData gameData, Permanent watcher,
            UUID controllerId, TriggerContext.SourceDealsDamage event) {
        List<DamageWatch> watches = new ArrayList<>(2);
        watches.add(DamageWatch.ON_ANY_SOURCE_DEALS_DAMAGE_TO_PERMANENT_OR_PLAYER);
        if (controllerId.equals(event.sourceControllerId()) && !watcher.getId().equals(event.sourcePermanentId())) {
            watches.add(DamageWatch.ON_ANOTHER_ALLY_SOURCE_DEALS_DAMAGE_TO_PERMANENT_OR_PLAYER);
        }
        if (watches.stream().allMatch(watch -> DamageAbilityReader.printed(watcher.getCard(), watch).isEmpty())) {
            return;
        }
        event.damageToPlayers().forEach((playerId, damage) -> {
            if (damage > 0) {
                collectDamageRecipientAbilities(gameData, watcher, controllerId, watches, event, playerId, true,
                        damage);
            }
        });
        event.damageToPermanents().forEach((permanentId, damage) -> {
            if (damage > 0) {
                collectDamageRecipientAbilities(gameData, watcher, controllerId, watches, event, permanentId, false,
                        damage);
            }
        });
    }

    /**
     * Collects {@code watcher}'s abilities of {@code watches} for the damage {@code event}'s source dealt to one
     * recipient ({@code recipientId}, a player when {@code playerRecipient}). Each ability triggers when the trigger's
     * filters match the source ({@link #damageSourceMatches}) and the recipient ({@link #damageRecipientMatches}) and
     * its event condition holds, judged with the recipient as the context's target (and triggering permanent) and the
     * damage dealt to it as the event value (CR 603.2: Ghyrson Starn's "exactly 1 damage"); the door judges its
     * intervening "if" in the same context (CR 603.4). Its record is {@link #damageRecipientRecord}'s.
     */
    private void collectDamageRecipientAbilities(GameData gameData, Permanent watcher, UUID controllerId,
            List<DamageWatch> watches, TriggerContext.SourceDealsDamage event, UUID recipientId,
            boolean playerRecipient, int damage) {
        ConditionContext context = ConditionContext.forPermanent(watcher, controllerId).withTargetId(recipientId)
                .withEventValue(damage);
        if (!playerRecipient) {
            context = context.withTriggeringPermanentId(recipientId);
        }
        for (DamageWatch watch : watches) {
            Trigger watched = watch.trigger();
            for (TriggeredAbility ability : printedWatchedAbilities(gameData, watcher, watch)) {
                requireEachEvent(ability, watch);
                DamageTrigger trigger = DamageAbilityReader.trigger(ability, watched);
                if (!damageSourceMatches(gameData, watcher, controllerId, trigger, event)
                        || !damageRecipientMatches(gameData, watcher, controllerId, trigger, event, recipientId,
                        playerRecipient)) {
                    continue;
                }
                putEventAbility(gameData, ability, watcher.getCard(), watcher, controllerId, context, event,
                        damageRecipientRecord(gameData, watcher, recipientId, playerRecipient, damage));
            }
        }
    }

    /**
     * Collects every permanent's printed "whenever damage is dealt to a player" abilities
     * ({@link DamageWatch#ON_PLAYER_DEALT_DAMAGE}) for the damage {@code damagedPlayerId} was dealt in one damage event,
     * whatever sources dealt it: the ability triggers once for the event, not once for each source (CR 603.2c; Boros
     * Reckoner's ruling). The callers sum the event's damage to the player: {@code CombatDamageService} the combat
     * damage of one combat damage step (CR 510.2), {@code DamageSupport.flushSourceDamageReflections} the noncombat
     * damage of one resolution damage event. Each ability triggers when its damaged-player filter matches the player
     * (judged with the watcher as the predicate's source) and its event condition holds, judged with the player as the
     * context's target and the damage as the event value. Its record is {@link #damageRecipientRecord}'s.
     */
    public void checkPlayerDealtDamageTriggers(GameData gameData, UUID damagedPlayerId, int damage) {
        if (damage <= 0 || damagedPlayerId == null) return;
        DamageWatch watch = DamageWatch.ON_PLAYER_DEALT_DAMAGE;
        Trigger watched = watch.trigger();
        var event = new TriggerContext.DamageToControllerAmount(damagedPlayerId, damage);
        gameData.forEachBattlefield((controllerId, battlefield) -> {
            for (Permanent watcher : List.copyOf(battlefield)) {
                for (TriggeredAbility ability : printedWatchedAbilities(gameData, watcher, watch)) {
                    requireEachEvent(ability, watch);
                    DamageTrigger trigger = DamageAbilityReader.trigger(ability, watched);
                    if (trigger.damagedPlayerFilter() != null && !targetLegalityService.matchesPlayerPredicate(
                            gameData, controllerId, damagedPlayerId, trigger.damagedPlayerFilter(), watcher.getId())) {
                        continue;
                    }
                    putEventAbility(gameData, ability, watcher.getCard(), watcher, controllerId,
                            ConditionContext.forPermanent(watcher, controllerId).withTargetId(damagedPlayerId)
                                    .withEventValue(damage), event,
                            damageRecipientRecord(gameData, watcher, damagedPlayerId, true, damage));
                }
            }
        });
    }

    /**
     * Whether the recipient of a source's damage matches the recipient filters of {@code trigger}: a player by its
     * damaged-player filter, a permanent by its damaged filter (on its snapshot as it was dealt the damage, or as it is
     * now). A trigger with neither filter matches every recipient; one with a filter only for one kind of recipient
     * matches no recipient of the other kind.
     */
    private boolean damageRecipientMatches(GameData gameData, Permanent watcher, UUID controllerId,
                                           DamageTrigger trigger, TriggerContext.SourceDealsDamage event,
                                           UUID recipientId, boolean playerRecipient) {
        if (trigger.damagedFilter() == null && trigger.damagedPlayerFilter() == null) return true;
        if (playerRecipient) {
            return trigger.damagedPlayerFilter() != null && targetLegalityService.matchesPlayerPredicate(
                    gameData, controllerId, recipientId, trigger.damagedPlayerFilter(), watcher.getId());
        }
        if (trigger.damagedFilter() == null) return false;
        Permanent damaged = event.damagedPermanentSnapshots().get(recipientId);
        if (damaged == null) {
            damaged = gameQueryService.findPermanentById(gameData, recipientId);
        }
        return damaged != null && damageFilterMatches(gameData, watcher, controllerId, trigger.damagedFilter(), damaged);
    }

    /**
     * The record of a "deals damage to a permanent or player" or "damage is dealt to a player" ability of
     * {@code watcher}, whatever its effects are: the recipient as its non-targeting {@code targetId} ("that permanent
     * or player"), a player also its triggering player and a permanent its triggering permanent with that permanent's
     * controller, the damage dealt to it as its event value, and the watcher as it was when the ability triggered.
     */
    private TriggerRecord damageRecipientRecord(GameData gameData, Permanent watcher, UUID recipientId,
                                                boolean playerRecipient, int damage) {
        UUID recipientControllerId = playerRecipient ? null
                : gameQueryService.findPermanentController(gameData, recipientId);
        Permanent snapshot = new Permanent(watcher);
        return entry -> {
            entry.setNonTargeting(true);
            entry.setTargetId(recipientId);
            entry.setEventValue(damage);
            if (playerRecipient) {
                entry.setTriggeringPlayerId(recipientId);
            } else {
                entry.setTriggeringPermanentId(recipientId);
                entry.setTriggeringPermanentControllerId(recipientControllerId);
            }
            entry.setSourcePermanentSnapshot(snapshot);
        };
    }

    /**
     * Whether the source of {@code event} matches the filters of {@code trigger}, judged with {@code watcher} as the
     * ability's source as the damage is dealt: a permanent source by the trigger's filter (on its card's
     * characteristics once it has left the battlefield), any other source (a spell, a card dealing damage from another
     * zone) by its spell filter. A trigger with neither filter matches every source; one with a filter only for one
     * kind of source matches no source of the other kind.
     */
    private boolean damageSourceMatches(GameData gameData, Permanent watcher, UUID controllerId,
                                        DamageTrigger trigger, TriggerContext.SourceDealsDamage event) {
        if (trigger.filter() == null && trigger.spellFilter() == null) return true;
        if (event.sourcePermanentId() != null) {
            if (trigger.filter() == null) return false;
            Permanent source = gameQueryService.findPermanentById(gameData, event.sourcePermanentId());
            return damageFilterMatches(gameData, watcher, controllerId, trigger.filter(),
                    source != null ? source : new Permanent(event.sourceCard()));
        }
        return trigger.spellFilter() != null && predicateEvaluationService.matchesCardPredicate(event.sourceCard(),
                trigger.spellFilter(), watcher.getCard().getId(), gameData, event.sourceControllerId());
    }

    /**
     * Collects the abilities of the instant or sorcery spell's controller's permanents that watch it deal damage, for
     * the batched damage event (already summed across simultaneous targets): "whenever an instant or sorcery spell you
     * control deals damage" ({@link DamageWatch#ON_ALLY_INSTANT_OR_SORCERY_DEALS_DAMAGE}), "... that targets only a
     * single creature deals damage to that creature" and "... deals damage to an opponent", once for each opponent.
     */
    public void checkAllyInstantOrSorcerySpellDealsDamageTriggers(GameData gameData,
                                                                   PendingSourceDamage batch) {
        Card sourceCard = batch.getSourceCard();
        UUID sourceControllerId = batch.getControllerId();
        if (!sourceCard.hasType(CardType.INSTANT) && !sourceCard.hasType(CardType.SORCERY)) {
            return;
        }
        var context = new TriggerContext.SourceDealsDamage(sourceCard, sourceControllerId,
                batch.getSourcePermanentId(), batch.getAmount(), batch.getDamageToPlayers(),
                batch.getSingleCreatureSpellTargetId(), batch.getDamageToPermanents(), false);
        List<Permanent> battlefield = gameData.playerBattlefields.get(sourceControllerId);
        if (battlefield == null) return;
        UUID onlyTargetCreatureId = batch.getSingleCreatureSpellTargetId();
        int damageToOnlyTargetCreature = onlyTargetCreatureId == null ? 0
                : batch.getDamageToPermanents().getOrDefault(onlyTargetCreatureId, 0);
        for (Permanent permanent : List.copyOf(battlefield)) {
            // Blaze Commando: "Whenever an instant or sorcery spell you control deals damage, ...", once for the
            // batched damage event: the damage dealt as its event value and the watcher as it was, which a transform
            // instruction reads to tell whether it has already transformed since (CR 701.27f).
            DamageWatch dealsDamage = DamageWatch.ON_ALLY_INSTANT_OR_SORCERY_DEALS_DAMAGE;
            for (TriggeredAbility ability : printedWatchedAbilities(gameData, permanent, dealsDamage)) {
                requireNothingToJudge(ability, dealsDamage);
                Permanent snapshot = new Permanent(permanent);
                putEventAbility(gameData, ability, permanent.getCard(), permanent, sourceControllerId,
                        ConditionContext.forPermanent(permanent, sourceControllerId)
                                .withEventValue(batch.getAmount()), context, entry -> {
                            entry.setEventValue(batch.getAmount());
                            entry.setSourcePermanentSnapshot(snapshot);
                        });
            }
            if (damageToOnlyTargetCreature > 0) {
                collectInstantOrSorceryDealsDamageToItsOnlyTargetCreatureAbilities(gameData, permanent,
                        sourceControllerId, onlyTargetCreatureId, damageToOnlyTargetCreature, context);
            }
            batch.getDamageToPlayers().forEach((damagedPlayerId, damage) -> {
                if (damage <= 0) {
                    return;
                }
                queueInstantOrSorceryDealsDamageToPlayerTriggers(gameData, permanent, sourceControllerId,
                        damagedPlayerId, damage, DamageWatch.ON_ALLY_INSTANT_OR_SORCERY_DEALS_DAMAGE_TO_PLAYER,
                        context);
                if (!damagedPlayerId.equals(sourceControllerId)) {
                    queueInstantOrSorceryDealsDamageToPlayerTriggers(gameData, permanent, sourceControllerId,
                            damagedPlayerId, damage, DamageWatch.ON_ALLY_INSTANT_OR_SORCERY_DEALS_DAMAGE_TO_OPPONENT,
                            context);
                }
            });
        }
    }

    /**
     * "Whenever an instant or sorcery spell you control that targets only a single creature deals damage to that
     * creature" ({@link DamageWatch#ON_ALLY_INSTANT_OR_SORCERY_DEALS_DAMAGE_TO_ITS_ONLY_TARGET_CREATURE}, Imodane): each
     * ability goes to the stack door whole (CR 603.3), that creature its triggering permanent and the damage dealt to
     * it ("that much") its event value.
     */
    private void collectInstantOrSorceryDealsDamageToItsOnlyTargetCreatureAbilities(GameData gameData,
            Permanent watcher, UUID controllerId, UUID creatureId, int damage, TriggerContext.SourceDealsDamage event) {
        DamageWatch watch = DamageWatch.ON_ALLY_INSTANT_OR_SORCERY_DEALS_DAMAGE_TO_ITS_ONLY_TARGET_CREATURE;
        for (TriggeredAbility ability : printedWatchedAbilities(gameData, watcher, watch)) {
            requireNothingToJudge(ability, watch);
            putEventAbility(gameData, ability, watcher.getCard(), watcher, controllerId,
                    ConditionContext.forPermanent(watcher, controllerId).withEventValue(damage), event,
                    damageEventRecord(watcher, damage, creatureId,
                            gameQueryService.findPermanentController(gameData, creatureId)));
        }
    }

    /**
     * "Whenever an instant or sorcery spell you control deals damage to an opponent" ({@link
     * DamageWatch#ON_ALLY_INSTANT_OR_SORCERY_DEALS_DAMAGE_TO_OPPONENT}) or "... to a player" ({@link
     * DamageWatch#ON_ALLY_INSTANT_OR_SORCERY_DEALS_DAMAGE_TO_PLAYER}, Syr Carah, the Bold) for one player dealt damage:
     * each player is an occurrence of its own (CR 603.2c). "That player" is the ability's triggering player and "that
     * much" its event value; a target is chosen as the ability goes on the stack (CR 603.3d), its "that player
     * controls" judged against the player dealt damage.
     */
    private void queueInstantOrSorceryDealsDamageToPlayerTriggers(GameData gameData, Permanent permanent,
                                                                  UUID controllerId, UUID damagedPlayerId,
                                                                  int damage, DamageWatch watch,
                                                                  TriggerContext event) {
        for (TriggeredAbility ability : printedWatchedAbilities(gameData, permanent, watch)) {
            requireNothingToJudge(ability, watch);
            Permanent snapshot = new Permanent(permanent);
            putEventAbility(gameData, ability, permanent.getCard(), permanent, controllerId,
                    ConditionContext.forPermanent(permanent, controllerId).withTargetId(damagedPlayerId)
                            .withEventValue(damage), event, entry -> {
                        entry.setEventValue(damage);
                        entry.setTriggeringPlayerId(damagedPlayerId);
                        entry.setSourcePermanentSnapshot(snapshot);
                        entry.setAttackedTargetId(damagedPlayerId);
                    });
        }
    }

    public void queueSourceDealsDamageReflections(GameData gameData, Card sourceCard, UUID sourceControllerId,
                                                   UUID sourcePermanentId, int totalDamage) {
        queueSourceDealsDamageReflections(gameData, sourceCard, sourceControllerId, sourcePermanentId,
                totalDamage, Map.of());
    }

    public void queueSourceDealsDamageReflections(GameData gameData, Card sourceCard, UUID sourceControllerId,
                                                   UUID sourcePermanentId, int totalDamage,
                                                   Map<UUID, Integer> damageToPlayers) {
        queueSourceDealsDamageReflections(gameData, sourceCard, sourceControllerId, sourcePermanentId,
                totalDamage, damageToPlayers, null);
    }

    public void queueSourceDealsDamageReflections(GameData gameData, Card sourceCard, UUID sourceControllerId,
                                                   UUID sourcePermanentId, int totalDamage,
                                                   Map<UUID, Integer> damageToPlayers,
                                                   List<TriggeredAbility> snapshottedSelfAbilities) {
        queueSourceDealsDamageReflections(gameData, sourceCard, sourceControllerId, sourcePermanentId, totalDamage,
                damageToPlayers, snapshottedSelfAbilities, null, Map.of());
    }

    public void queueSourceDealsDamageReflections(GameData gameData, Card sourceCard, UUID sourceControllerId,
                                                   UUID sourcePermanentId, int totalDamage,
                                                   Map<UUID, Integer> damageToPlayers,
                                                   List<TriggeredAbility> snapshottedSelfAbilities,
                                                   UUID singleCreatureSpellTargetId,
                                                   Map<UUID, Integer> damageToPermanents) {
        queueSourceDealsDamageReflections(gameData, sourceCard, sourceControllerId, sourcePermanentId, totalDamage,
                damageToPlayers, snapshottedSelfAbilities, singleCreatureSpellTargetId, damageToPermanents, false);
    }

    public void queueSourceDealsDamageReflections(GameData gameData, Card sourceCard, UUID sourceControllerId,
                                                   UUID sourcePermanentId, int totalDamage,
                                                   Map<UUID, Integer> damageToPlayers,
                                                   List<TriggeredAbility> snapshottedSelfAbilities,
                                                   UUID singleCreatureSpellTargetId,
                                                   Map<UUID, Integer> damageToPermanents,
                                                   boolean combatDamage) {
        queueSourceDealsDamageReflections(gameData, sourceCard, sourceControllerId, sourcePermanentId,
                totalDamage, damageToPlayers, snapshottedSelfAbilities, singleCreatureSpellTargetId,
                damageToPermanents, combatDamage, Map.of());
    }

    public void queueSourceDealsDamageReflections(GameData gameData, Card sourceCard, UUID sourceControllerId,
                                                   UUID sourcePermanentId, int totalDamage,
                                                   Map<UUID, Integer> damageToPlayers,
                                                   List<TriggeredAbility> snapshottedSelfAbilities,
                                                   UUID singleCreatureSpellTargetId,
                                                   Map<UUID, Integer> damageToPermanents,
                                                   boolean combatDamage,
                                                   Map<UUID, Permanent> damagedPermanentSnapshots) {
        if (sourceCard == null || sourceControllerId == null || totalDamage <= 0) return;

        var ctx = new TriggerContext.SourceDealsDamage(sourceCard, sourceControllerId, sourcePermanentId, totalDamage,
                damageToPlayers == null ? Map.of() : Map.copyOf(damageToPlayers), singleCreatureSpellTargetId,
                damageToPermanents == null ? Map.of() : Map.copyOf(damageToPermanents), combatDamage,
                damagedPermanentSnapshots);
        gameData.forEachBattlefield((watcherPlayerId, battlefield) -> {
            List<DamageWatch> watches = watcherPlayerId.equals(sourceControllerId)
                    ? List.of(DamageWatch.ON_ANY_SOURCE_DEALS_DAMAGE, DamageWatch.ON_ALLY_SOURCE_DEALS_DAMAGE)
                    : List.of(DamageWatch.ON_ANY_SOURCE_DEALS_DAMAGE);
            int damageToWatcherController = ctx.damageToPlayers().getOrDefault(watcherPlayerId, 0);
            for (Permanent perm : List.copyOf(battlefield)) {
                if (gameQueryService.hasLostPrintedAbilities(gameData, perm)) continue;
                collectSourceDealsDamageAbilities(gameData, perm, watcherPlayerId, watches, ctx, totalDamage);
                if (damageToWatcherController > 0) {
                    collectSourceDealsDamageAbilities(gameData, perm, watcherPlayerId,
                            List.of(DamageWatch.ON_ANY_SOURCE_DEALS_DAMAGE_TO_YOU), ctx, damageToWatcherController);
                }
                collectSourceDealsDamageToPermanentOrPlayerAbilities(gameData, perm, watcherPlayerId, ctx);
            }
        });

        // Self triggers (El-Hajjâj): only the damage source's own "whenever this creature deals
        // damage" abilities fire. Keyed off the source card (not a battlefield scan) so it still
        // triggers when the source died dealing that damage.
        Permanent sourcePermanent = findPermanentByCardId(gameData, sourceCard.getId());
        boolean sourceHasPrintedAbilities = sourcePermanent == null
                || !sourcePermanent.isFaceDown() && !gameQueryService.hasLostPrintedAbilities(gameData, sourcePermanent);
        Trigger selfDealsDamage = DamageWatch.ON_SELF_DEALS_DAMAGE.trigger();
        List<TriggeredAbility> selfAbilities = snapshottedSelfAbilities != null
                ? new ArrayList<>(snapshottedSelfAbilities)
                : sourceHasPrintedAbilities
                ? new ArrayList<>(DamageAbilityReader.printed(sourceCard, DamageWatch.ON_SELF_DEALS_DAMAGE))
                : new ArrayList<>();
        // Granted "whenever this creature deals damage" abilities live on the permanent, not the card
        // (the Genju cycle grants one to the animated land until end of turn). If no snapshot was
        // captured, the source is still on the battlefield and can be read here.
        if (snapshottedSelfAbilities == null && sourcePermanent != null) {
            selfAbilities.addAll(WatchedAbilities.temporary(sourcePermanent, selfDealsDamage));
            selfAbilities.addAll(WatchedAbilities.persistent(sourcePermanent, selfDealsDamage));
            selfAbilities.addAll(grantedTriggeredAbilitySupport.grantedWatchedAbilities(
                    gameData, sourcePermanent, selfDealsDamage));
        }
        // The source may have died dealing the damage: its ability keeps its last-known permanent as its source.
        Permanent abilitySource = sourcePermanent != null ? sourcePermanent
                : lastKnownSource(gameData, sourceCard, sourcePermanentId);
        for (TriggeredAbility ability : selfAbilities) {
            requireEachEvent(ability, DamageWatch.ON_SELF_DEALS_DAMAGE);
            // "Whenever this deals damage to one or more creatures": the filter is on the damaged permanents.
            PermanentPredicate filter = DamageAbilityReader.filter(ability, selfDealsDamage);
            if (filter != null && !damagedPermanentMatches(gameData, ctx, filter, sourcePermanent)) {
                continue;
            }
            // The ability's intervening "if" and event condition, with the damage dealt (CR 603.4).
            ConditionContext selfContext = sourcePermanent != null
                    ? ConditionContext.forPermanent(sourcePermanent, sourceControllerId)
                    : ConditionContext.forCard(sourceCard, sourceControllerId);
            putEventAbility(gameData, ability, sourceCard, abilitySource, sourceControllerId,
                    selfContext.withEventValue(ctx.totalDamage()), ctx,
                    entry -> entry.setEventValue(ctx.totalDamage()));
        }

        for (DelayedWatchedCreatureDealsDamage watch
                : gameData.getDelayedActions(DelayedWatchedCreatureDealsDamage.class)) {
            if (!watch.watchedPermanentId().equals(sourcePermanentId)) continue;

            StackEntry trigger = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    watch.sourceCard(),
                    watch.controllerId(),
                    watch.sourceCard().getName() + "'s delayed trigger",
                    new ArrayList<>(watch.effects()),
                    watch.watchedPermanentId(),
                    (UUID) null);
            trigger.setNonTargeting(true);
            trigger.setEventValue(totalDamage);
            gameData.stack.add(trigger);
            gameLogService.append(gameData, GameLog.abilityTriggers(watch.sourceCard()));
        }
    }

    /**
     * Fires delayed triggers watching a Wall for damage from an attacking creature. The source's
     * attacking status is checked at the damage event, so noncombat damage from a spell and damage
     * from a blocking creature do not qualify.
     */
    public void checkDelayedWatchedCreatureDealtDamageByAttackingCreatureTriggers(
            GameData gameData, Permanent damageSource, Permanent damagedCreature, int damage) {
        if (damageSource == null || damagedCreature == null || damage <= 0
                || !damageSource.isAttacking() || !gameQueryService.isCreature(gameData, damageSource)) {
            return;
        }

        for (DelayedWatchedCreatureDealtDamageByAttackingCreature watch
                : gameData.getDelayedActions(DelayedWatchedCreatureDealtDamageByAttackingCreature.class)) {
            if (!watch.watchedPermanentId().equals(damagedCreature.getId())) continue;

            StackEntry trigger = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    watch.sourceCard(),
                    watch.controllerId(),
                    watch.sourceCard().getName() + "'s delayed trigger",
                    new ArrayList<>(watch.effects()),
                    watch.watchedPermanentId(),
                    (UUID) null);
            trigger.setNonTargeting(true);
            trigger.setEventValue(damage);
            gameData.stack.add(trigger);
            gameLogService.append(gameData, GameLog.abilityTriggers(watch.sourceCard()));
        }
    }

    /**
     * Whether a permanent {@code ctx}'s source dealt damage to in this event, as it was before state-based actions,
     * matches {@code filter}: the filter of "whenever this deals damage to one or more [permanents]", judged with the
     * damage source as the ability's source.
     */
    private boolean damagedPermanentMatches(GameData gameData, TriggerContext.SourceDealsDamage ctx,
                                            PermanentPredicate filter, Permanent sourcePermanent) {
        FilterContext recipientContext = FilterContext.of(gameData)
                .withSourceControllerId(ctx.sourceControllerId())
                .withSourceCardId(ctx.sourceCard().getId())
                .withSourcePermanentId(ctx.sourcePermanentId())
                .withSourcePermanentSnapshot(sourcePermanent);
        return ctx.damageToPermanents().entrySet().stream()
                .filter(damage -> damage.getValue() > 0)
                .map(damage -> ctx.damagedPermanentSnapshots().get(damage.getKey()))
                .filter(Objects::nonNull)
                .anyMatch(recipient -> predicateEvaluationService.matchesPermanentPredicate(
                        recipient, filter, recipientContext));
    }

    /**
     * "Whenever enchanted creature deals damage, …": each ability of an Aura attached to {@code sourceCreature} goes to
     * the stack door whole (CR 603.3), the ability the Aura's (CR 113.7); its record names, whatever its effects are,
     * the Aura's controller as the non-targeting "you", the damage as its event value and the enchanted creature, with
     * its controller as it dealt the damage, as its triggering permanent ("its controller").
     */
    public void queueEnchantedCreatureDealsDamageTriggers(GameData gameData, Permanent sourceCreature,
                                                           int damageDealt) {
        if (sourceCreature == null || damageDealt <= 0) return;

        DamageWatch watch = DamageWatch.ON_ENCHANTED_CREATURE_DEALS_DAMAGE;
        UUID sourceControllerId = gameQueryService.findPermanentController(gameData, sourceCreature.getId());
        gameData.forEachPermanent((auraControllerId, aura) -> {
            if (!aura.isAttached() || !sourceCreature.getId().equals(aura.getAttachedTo())) return;

            Permanent auraSnapshot = new Permanent(aura);
            for (TriggeredAbility ability : printedWatchedAbilities(gameData, aura, watch)) {
                requireNothingToJudge(ability, watch);
                putEventAbility(gameData, ability, aura.getCard(), aura, auraControllerId,
                        ConditionContext.forPermanent(aura, auraControllerId)
                                .withTriggeringPermanentId(sourceCreature.getId()).withEventValue(damageDealt), null,
                        entry -> {
                            entry.setNonTargeting(true);
                            entry.setTargetId(auraControllerId);
                            entry.setEventValue(damageDealt);
                            entry.setTriggeringPermanentId(sourceCreature.getId());
                            entry.setTriggeringPermanentControllerId(sourceControllerId);
                            entry.setSourcePermanentSnapshot(auraSnapshot);
                        });
            }
        });
    }

    /**
     * "Whenever enchanted creature deals damage to a creature, …" for {@code sourceCreature} dealing damage to the
     * creature {@code damagedCreatureId}, judged as the damage is dealt, before state-based actions, so the ability
     * still triggers when either combat creature dies from the same damage event: the damaged creature is its
     * non-targeting "that creature".
     */
    public void queueEnchantedCreatureDealsDamageToCreatureTriggers(GameData gameData,
                                                                      Permanent sourceCreature,
                                                                      UUID damagedCreatureId,
                                                                      int damageDealt) {
        if (sourceCreature == null || damagedCreatureId == null || damageDealt <= 0
                || sourceCreature.getId().equals(damagedCreatureId)
                || !gameQueryService.isCreature(gameData, sourceCreature)) {
            return;
        }

        DamageWatch watch = DamageWatch.ON_ENCHANTED_CREATURE_DEALS_DAMAGE_TO_CREATURE;
        gameData.forEachPermanent((auraControllerId, aura) -> {
            if (!aura.isAttached() || !sourceCreature.getId().equals(aura.getAttachedTo())) return;

            for (TriggeredAbility ability : printedWatchedAbilities(gameData, aura, watch)) {
                requireNothingToJudge(ability, watch);
                putEventAbility(gameData, ability, aura.getCard(), aura, auraControllerId,
                        ConditionContext.forPermanent(aura, auraControllerId)
                                .withTriggeringPermanentId(damagedCreatureId).withEventValue(damageDealt), null,
                        entry -> {
                            entry.setNonTargeting(true);
                            entry.setTargetId(damagedCreatureId);
                        });
            }
        });
    }

    /**
     * The permanent {@code sourceCard} was as it last existed on the battlefield, when it was the permanent
     * {@code sourcePermanentId} that left it (CR 608.2h), or {@code null}.
     */
    private static Permanent lastKnownSource(GameData gameData, Card sourceCard, UUID sourcePermanentId) {
        if (sourcePermanentId == null) {
            return null;
        }
        Permanent departed = gameData.departedPermanentSnapshots.get(sourceCard.getId());
        return departed != null && sourcePermanentId.equals(departed.getId()) ? departed : null;
    }

    /** The battlefield permanent whose card has this id, or null once it has left the battlefield. */
    private Permanent findPermanentByCardId(GameData gameData, UUID cardId) {
        List<Permanent> found = new ArrayList<>(1);
        gameData.forEachPermanent((playerId, perm) -> {
            if (perm.getCard().getId().equals(cardId)) {
                found.add(perm);
            }
        });
        return found.isEmpty() ? null : found.getFirst();
    }

    // ── Land-tap triggers ──────────────────────────────────────────────

    /**
     * Dispatches combat-damage-only self triggers from the source card. The source permanent ID is
     * retained even when state-based actions removed the source before triggered abilities were put
     * on the stack, so effects that require the source to have survived can check it at resolution.
     */
    public void queueSourceDealsCombatDamageTriggers(GameData gameData, Card sourceCard,
                                                      UUID sourceControllerId, UUID sourcePermanentId,
                                                      int totalDamage, int damageToPlayers,
                                                      UUID damagedPlayerId,
                                                      List<TriggeredAbility> snapshottedSelfAbilities) {
        queueSourceDealsCombatDamageTriggers(gameData, sourceCard, sourceControllerId, sourcePermanentId,
                totalDamage, damageToPlayers, damagedPlayerId, snapshottedSelfAbilities, null);
    }

    /**
     * Captures, per controller, the permanents with "whenever a creature you control deals combat
     * damage" abilities before state-based actions remove them: the event happens while they are
     * still on the battlefield (CR 603.2), so one destroyed by that same damage still triggers. A
     * permanent that has lost its printed abilities then (Song of the Dryads) has none to trigger.
     */
    public Map<UUID, List<Permanent>> snapshotAllyCreatureDealsCombatDamageWatchers(GameData gameData) {
        Map<UUID, List<Permanent>> watchers = new LinkedHashMap<>();
        gameData.forEachPermanent((controllerId, permanent) -> {
            if (!WatchedAbilities.printed(permanent.getCard(), CombatWatch.ON_ALLY_CREATURE_DEALS_COMBAT_DAMAGE).isEmpty()
                    && !gameQueryService.hasLostPrintedAbilities(gameData, permanent)) {
                watchers.computeIfAbsent(controllerId, ignored -> new ArrayList<>()).add(permanent);
            }
        });
        return watchers;
    }

    public void queueSourceDealsCombatDamageTriggers(GameData gameData, Card sourceCard,
                                                      UUID sourceControllerId, UUID sourcePermanentId,
                                                      int totalDamage, int damageToPlayers,
                                                      UUID damagedPlayerId,
                                                      List<TriggeredAbility> snapshottedSelfAbilities,
                                                      List<Permanent> snapshottedAllyWatchers) {
        if (sourceCard == null || sourceControllerId == null || sourcePermanentId == null || totalDamage <= 0) {
            return;
        }
        var ctx = new TriggerContext.SourceDealsCombatDamage(
                sourceCard, sourceControllerId, sourcePermanentId, totalDamage, damageToPlayers, damagedPlayerId);
        dispatchSourceDealsCombatDamageTriggers(gameData, sourceCard, sourceControllerId, sourcePermanentId,
                totalDamage, damageToPlayers, snapshottedSelfAbilities, CombatWatch.ON_SELF_DEALS_COMBAT_DAMAGE);

        // "Whenever a creature you control deals combat damage" watchers (Five-Alarm Fire). Scanned on
        // the damage source's controller's battlefield only; the watcher itself needn't be a creature.
        // The damage source counts as a creature as it currently is, so an animated noncreature
        // permanent (Gruul Keyrune) qualifies, while a printed creature that stopped being one does not.
        Permanent damageSource = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        boolean damageSourceIsCreature = damageSource != null
                ? gameQueryService.isCreature(gameData, damageSource)
                : sourceCard.hasType(CardType.CREATURE);
        if (!damageSourceIsCreature) return;
        List<Permanent> battlefield = gameData.playerBattlefields.get(sourceControllerId);
        if (battlefield == null) return;
        List<Permanent> allyWatchers = snapshottedAllyWatchers != null
                ? snapshottedAllyWatchers : List.copyOf(battlefield);
        CombatWatch allyWatch = CombatWatch.ON_ALLY_CREATURE_DEALS_COMBAT_DAMAGE;
        for (Permanent watcher : allyWatchers) {
            if (snapshottedAllyWatchers == null && gameQueryService.hasLostPrintedAbilities(gameData, watcher)) {
                continue;
            }
            // The watcher, not the damage dealer, is the ability's source, so a self-referencing effect (Five-Alarm
            // Fire's blaze counter) lands on the watcher; the damage dealer is the triggering permanent, so a
            // TRIGGERING-referenced effect (Quilled Greatwurm's counters) lands on it.
            for (TriggeredAbility ability : WatchedAbilities.printed(watcher.getCard(), allyWatch)) {
                requirePlainCombatAbility(ability, allyWatch);
                putEventAbility(gameData, ability, watcher.getCard(), watcher, sourceControllerId,
                        ConditionContext.forPermanent(watcher, sourceControllerId), ctx, entry -> {
                            entry.setTriggeringPermanentId(sourcePermanentId);
                            entry.setNonTargeting(true);
                            entry.setEventValue(totalDamage);
                        });
            }
        }

        gameData.forEachPermanent((watcherControllerId, watcher) -> {
            if (!watcher.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)
                    || !isEquippedBy(gameData, watcher, sourcePermanentId)) {
                return;
            }
            // Each ability's event condition ("during your turn", The Aetherspark; CR 603.2) and intervening "if"
            // (CR 603.4), read from the Equipment and its controller as the ability triggers.
            if (damageToPlayers > 0) {
                putEquipmentCombatDamageAbilities(gameData, watcher, watcherControllerId,
                        CombatWatch.ON_EQUIPPED_CREATURE_DEALS_COMBAT_DAMAGE_TO_PLAYER, ctx, damageToPlayers);
            }
            putEquipmentCombatDamageAbilities(gameData, watcher, watcherControllerId,
                    CombatWatch.ON_EQUIPPED_CREATURE_DEALS_COMBAT_DAMAGE, ctx, totalDamage);
        });
    }

    /**
     * Hands each "whenever equipped creature deals combat damage [to a player]" ability {@code watch} reads of the
     * Equipment {@code equipment} to the stack door, the ability the Equipment's (CR 113.7): "that player" is the
     * player dealt damage, if any (the non-targeting target and triggering player), the equipped creature its
     * triggering permanent and {@code damage} its event value ("that much" counts only the damage dealt to players for
     * "deals combat damage to a player").
     */
    private void putEquipmentCombatDamageAbilities(GameData gameData, Permanent equipment, UUID controllerId,
                                                   CombatWatch watch, TriggerContext.SourceDealsCombatDamage ctx,
                                                   int damage) {
        Permanent snapshot = new Permanent(equipment);
        for (TriggeredAbility ability : WatchedAbilities.printed(equipment.getCard(), watch)) {
            requirePlainCombatAbility(ability, watch);
            putEventAbility(gameData, ability, equipment.getCard(), equipment, controllerId,
                    ConditionContext.forPermanent(equipment, controllerId)
                            .withTriggeringPermanentId(ctx.sourcePermanentId()), ctx, entry -> {
                        if (ctx.damagedPlayerId() != null) {
                            entry.setTargetId(ctx.damagedPlayerId());
                        }
                        entry.setTriggeringPlayerId(ctx.damagedPlayerId());
                        entry.setTriggeringPermanentId(ctx.sourcePermanentId());
                        entry.setNonTargeting(true);
                        entry.setEventValue(damage);
                        entry.setSourcePermanentSnapshot(snapshot);
                    });
        }
    }

    /** Throws when {@code ability} has a filter, limit or multiplicity, none of which {@code watch}'s collector reads. */
    private static void requirePlainCombatAbility(TriggeredAbility ability, CombatWatch watch) {
        CombatAbilityReader.CombatEffect read = CombatAbilityReader.read(ability, watch.trigger()).getFirst();
        CombatAbilityReader.requireNoLimitOrMultiplicity(read, watch.trigger());
        if (read.filter() != null) {
            throw new IllegalStateException("The collector of " + watch + " judges no filter: " + ability);
        }
    }

    /** Hands the player-or-battle combat-damage abilities of Equipment attached to the source to the stack door. */
    private void queueEquippedCreatureDealsCombatDamageToPlayerOrBattleTriggers(GameData gameData,
                                                                                  Card sourceCard,
                                                                                  UUID sourceControllerId,
                                                                                  UUID sourcePermanentId,
                                                                                  int totalDamage,
                                                                                  int damageToPlayers) {
        var ctx = new TriggerContext.SourceDealsCombatDamage(
                sourceCard, sourceControllerId, sourcePermanentId, totalDamage, damageToPlayers);
        gameData.forEachPermanent((watcherControllerId, watcher) -> {
            if (!watcher.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)
                    || !isEquippedBy(gameData, watcher, sourcePermanentId)) {
                return;
            }
            putEquipmentCombatDamageAbilities(gameData, watcher, watcherControllerId,
                    CombatWatch.ON_EQUIPPED_CREATURE_DEALS_COMBAT_DAMAGE_TO_PLAYER_OR_BATTLE, ctx, totalDamage);
        });
    }

    /**
     * Handles {@link TapWatch#ON_SELF_TAPPED_FOR_MANA} — "Whenever you tap this permanent for
     * mana, …" (Zhur-Taa Druid). Only the tapped permanent's own card is scanned, and only the
     * mana-ability tap path calls this, so tapping to attack or an opponent's forced tap never
     * triggers it. Each printed or granted ability goes to the stack door whole (CR 603.3), the permanent its
     * triggering permanent; the caller defers the queued trigger like every other mana-ability trigger.
     *
     * @param gameData        the current game state
     * @param tappedPermanent the permanent that was just tapped for mana
     * @param controllerId    the controller of that permanent (also the player who tapped it)
     */
    public void checkSelfTappedForManaTriggers(GameData gameData, Permanent tappedPermanent, UUID controllerId) {
        TapWatch watch = TapWatch.ON_SELF_TAPPED_FOR_MANA;
        TriggerContext event = new TriggerContext.EnchantedPermanentTap(tappedPermanent, controllerId);
        List<TriggeredAbility> abilities = new ArrayList<>(
                TapAbilityReader.printed(tappedPermanent.getCard(), watch));
        abilities.addAll(grantedTriggeredAbilitySupport.grantedWatchedAbilities(gameData, tappedPermanent,
                watch.trigger()));
        for (TriggeredAbility ability : abilities) {
            TapAbilityReader.requireNothingToJudge(TapAbilityReader.read(ability, watch.trigger()).getFirst(),
                    watch.trigger());
            putEventAbility(gameData, ability, tappedPermanent.getCard(), tappedPermanent, controllerId,
                    ConditionContext.forPermanent(tappedPermanent, controllerId), event,
                    entry -> entry.setTriggeringPermanentId(tappedPermanent.getId()));
        }
    }

    /**
     * Collects the "whenever a mana ability of this creature resolves" abilities of {@code sourcePermanent}, its own and
     * those other permanents grant it (Tyvar the Bellicose), by {@link #collectUnfilteredAbility}: each grant is an
     * instance of its own with its own "triggers only once each turn" (CR 113.2c).
     */
    public void checkManaAbilityResolutionTriggers(GameData gameData, Permanent sourcePermanent,
                                                    UUID controllerId, int manaProduced) {
        SpellAbilityWatch watch = SpellAbilityWatch.ON_SELF_MANA_ABILITY_RESOLVES;
        Card card = sourcePermanent.getCard();
        List<TriggeredAbility> abilities = new ArrayList<>(card.getTriggeredAbilities().stream()
                .filter(ability -> ability.hasWatchedTrigger(watch.trigger(), watch.zone()))
                .toList());
        abilities.addAll(grantedTriggeredAbilitySupport.grantedTriggeredAbilities(gameData, sourcePermanent, null,
                ability -> ability.hasWatchedTrigger(watch.trigger())));
        if (abilities.isEmpty()) return;

        TriggerContext context = new TriggerContext.ManaAbilityResolved(controllerId, manaProduced);
        for (TriggeredAbility ability : abilities) {
            collectUnfilteredAbility(gameData, sourcePermanent, card, controllerId, context, ability,
                    UnaryOperator.identity(), null);
        }
    }

    public void checkLandTapTriggers(GameData gameData, UUID tappingPlayerId, UUID tappedLandId) {
        checkLandTapTriggers(gameData, tappingPlayerId, tappedLandId, Set.of());
    }

    public void checkLandTapTriggers(GameData gameData, UUID tappingPlayerId, UUID tappedLandId,
                                     Set<ManaColor> producedColors) {
        // Desolation et al.: track who tapped a land for mana this turn even if no land-tap
        // trigger permanent is currently on the battlefield (2004-10-04 ruling).
        gameData.playersWhoTappedLandForManaThisTurn.add(tappingPlayerId);

        boolean[] anyTriggered = {false};
        var ctx = new TriggerContext.LandTap(tappingPlayerId, tappedLandId, producedColors);

        // Snapshot each battlefield so trigger collection remains stable if a collector mutates it.
        gameData.forEachBattlefield((playerId, battlefield) -> {
            for (Permanent perm : List.copyOf(battlefield)) {
                if (gameQueryService.hasLostAllAbilities(gameData, perm)) continue;
                // "That player" of an intervening "if" ("if it's not that player's turn": Price of Glory) is the
                // player who tapped the land, the entry's non-targeting target as it resolves.
                for (List<TapAbilityReader.TapEffect> reads : TapAbilityReader.readPrintedAsJudgedByAbility(
                        perm.getCard(), TapWatch.ON_ANY_PLAYER_TAPS_LAND, conditionEvaluationService.judge(gameData,
                                ConditionContext.forPermanent(perm, playerId).withTargetId(tappingPlayerId)))) {
                    if (!tapForManaMatches(gameData, reads.getFirst(), perm, perm.getCard(), playerId,
                            tappingPlayerId, tappedLandId)) {
                        continue;
                    }
                    var match = new TriggerMatchContext(gameData, perm, playerId, TapAbilityReader.effectsOf(reads));
                    if (dispatch(match, TapWatch.ON_ANY_PLAYER_TAPS_LAND, ctx)) {
                        anyTriggered[0] = true;
                    }
                }
            }
        });

        if (gameData.planechase != null && gameData.planechase.controllerId != null) {
            UUID planarControllerId = gameData.planechase.controllerId;
            for (PlanarObject object : List.copyOf(gameData.planechase.faceUp)) {
                for (List<TapAbilityReader.TapEffect> reads : TapAbilityReader.readPrintedAsJudgedByAbility(
                        object.getCard(), TapWatch.ON_ANY_PLAYER_TAPS_LAND, conditionEvaluationService.judge(gameData,
                                ConditionContext.forCard(object.getCard(), planarControllerId)
                                        .withTargetId(tappingPlayerId)))) {
                    if (!tapForManaMatches(gameData, reads.getFirst(), null, object.getCard(), planarControllerId,
                            tappingPlayerId, tappedLandId)) {
                        continue;
                    }
                    var match = new TriggerMatchContext(gameData, null, planarControllerId,
                            TapAbilityReader.effectsOf(reads), object.getCard(), object.copy());
                    if (registry.dispatch(match, TapWatch.ON_ANY_PLAYER_TAPS_LAND, ctx)) {
                        anyTriggered[0] = true;
                    }
                }
            }
        }

        // A player's "until end of turn, whenever you tap a land for mana, add one mana of any type that land
        // produced" (Selesnya Loft Gardens' chaos), granted as a floating effect: "you" is the affected player.
        synchronized (gameData.floatingEffects) {
            for (FloatingContinuousEffect floating : List.copyOf(gameData.floatingEffects)) {
                if (!(floating.effect() instanceof AddOneOfEachManaTypeProducedByLandEffect effect)
                        || floating.affectedPlayerId() == null
                        || !floating.affectedPlayerId().equals(tappingPlayerId)) {
                    continue;
                }
                TriggerMatchContext match = new TriggerMatchContext(
                        gameData, null, floating.affectedPlayerId(), effect);
                if (registry.dispatch(match, TapWatch.ON_ANY_PLAYER_TAPS_LAND, ctx)) {
                    anyTriggered[0] = true;
                }
            }
        }

        applyTurnScopedExtraLandTapMana(gameData, tappingPlayerId, tappedLandId);

        if (anyTriggered[0]) {
            gameOutcomeService.checkWinCondition(gameData);
        }
    }

    /**
     * Whether the tap for mana matches {@code read}'s trigger conditions the tap-for-mana collectors judge, as the
     * permanent is tapped: the tap relation ({@link TapAbilityReader.TapEffect#tapRelation()}: "you tap" and "an
     * opponent taps", the tapping player against the ability's controller;
     * "enchanted land", the permanent {@code source} is attached to; "this land", {@code source} itself) and the filter
     * on the tapped permanent ("an Island", "a nonbasic land", "an artifact"), judged on the permanent on the
     * battlefield with the ability's source as the filter's source. A permanent that left the battlefield as its mana
     * ability resolved (Gemstone Mine's last use) matches no filter: the objects that exist immediately after the event
     * are checked (CR 603.10), as the effects that carried the filter judged it.
     */
    private boolean tapForManaMatches(GameData gameData, TapAbilityReader.TapEffect read, Permanent source,
                                      Card sourceCard, UUID controllerId, UUID tappingPlayerId, UUID tappedId) {
        boolean relationMatches = switch (read.tapRelation()) {
            case YOU_CONTROL -> controllerId.equals(tappingPlayerId);
            case OPPONENT_CONTROLS -> !controllerId.equals(tappingPlayerId);
            case ENCHANTED -> source != null && source.isAttached() && tappedId.equals(source.getAttachedTo());
            case THIS -> source != null && source.getId().equals(tappedId);
            default -> true;
        };
        if (!relationMatches) {
            return false;
        }
        if (read.filter() == null) {
            return true;
        }
        Permanent tapped = gameQueryService.findPermanentById(gameData, tappedId);
        return tapped != null && predicateEvaluationService.matchesPermanentPredicate(tapped, read.filter(),
                FilterContext.of(gameData).withSourceCardId(sourceCard.getId()).withSourceControllerId(controllerId));
    }

    /**
     * Applies {@code GameData.extraManaOnLandSubtypeTapThisTurn} — a turn-scoped, symmetric
     * "whenever a player taps a land of this subtype for mana, that player adds an additional
     * {X}" granted by a resolved effect rather than a permanent's static ability (Chaos Moon's
     * odd branch). Like the static land-tap triggers above it is a triggered mana ability, so it
     * pays straight into the tapping player's pool without using the stack.
     */
    private void applyTurnScopedExtraLandTapMana(GameData gameData, UUID tappingPlayerId, UUID tappedLandId) {
        if (gameData.extraManaOnLandSubtypeTapThisTurn.isEmpty()) {
            return;
        }
        Permanent tappedLand = gameQueryService.findPermanentById(gameData, tappedLandId);
        if (tappedLand == null) {
            return;
        }
        Set<CardSubtype> types = gameQueryService.effectiveBasicLandTypes(gameData, tappedLand);
        ManaPool pool = gameData.playerManaPools.get(tappingPlayerId);
        if (pool == null) {
            return;
        }
        for (var entry : gameData.extraManaOnLandSubtypeTapThisTurn.entrySet()) {
            if (!types.contains(entry.getKey())) {
                continue;
            }
            for (var bonus : entry.getValue()) {
                ManaColor color = bonus.color() != ManaColor.COLORLESS
                        && gameData.playersWithColoredManaReplacementThisTurn.contains(bonus.controllerId())
                        ? ManaColor.WHITE : bonus.color();
                pool.add(color);
                gameLogService.append(gameData, GameLog.builder()
                        .text(gameData.playerIdToName.get(tappingPlayerId) + " adds 1 additional "
                                + color.getCode() + " mana.")
                        .build());
            }
        }
    }

    // ── Permanent-returned-to-hand triggers ────────────────────────────

    // ── Ally-permanent-sacrificed triggers ──────────────────────────────

    /**
     * Handles effects that trigger whenever one or more creatures controlled by a player deal
     * damage to a player. The dealers are supplied as one batch so the trigger fires once for the
     * event even when multiple creatures dealt damage simultaneously; the player dealt damage is the ability's
     * non-targeting "that player" (Francisco, Fowl Marauder; The Thing, Ben Grimm).
     */
    public void checkAllyCreaturesDealDamageToPlayerTriggers(GameData gameData, UUID sourceControllerId,
                                                              UUID damagedPlayerId,
                                                              List<Permanent> damageDealers) {
        if (sourceControllerId == null || damagedPlayerId == null
                || damageDealers == null || damageDealers.isEmpty()) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(sourceControllerId);
        if (battlefield == null) {
            return;
        }

        var context = new TriggerContext.AllyCreaturesDealDamageToPlayer(
                damagedPlayerId, sourceControllerId, damageDealers);
        DamageWatch toPlayer = DamageWatch.ON_ALLY_CREATURES_DEAL_DAMAGE_TO_PLAYER;
        for (Permanent permanent : List.copyOf(battlefield)) {
            for (TriggeredAbility ability : allyCreaturesDealDamageAbilities(gameData, permanent, toPlayer)) {
                requireEachEvent(ability, toPlayer);
                // "One or more [filtered] creatures you control": the filter is on each creature that dealt damage.
                if (!anyDealerMatches(gameData, permanent, sourceControllerId,
                        DamageAbilityReader.filter(ability, toPlayer.trigger()), damageDealers, true)) {
                    continue;
                }
                putAllyCreaturesDealDamageAbility(gameData, ability, permanent, sourceControllerId, damagedPlayerId,
                        context);
            }
        }
    }

    /** Hands a "whenever one or more creatures you control deal damage to a player [an opponent]" ability to the door. */
    private void putAllyCreaturesDealDamageAbility(GameData gameData, TriggeredAbility ability, Permanent watcher,
                                                   UUID controllerId, UUID damagedPlayerId, TriggerContext event) {
        putEventAbility(gameData, ability, watcher.getCard(), watcher, controllerId,
                ConditionContext.forPermanent(watcher, controllerId).withTargetId(damagedPlayerId), event, entry -> {
                    entry.setTargetId(damagedPlayerId);
                    entry.setNonTargeting(true);
                });
    }

    /**
     * Handles effects that trigger whenever one or more creatures controlled by a player deal
     * damage to an opponent. The dealers are supplied as one batch so the trigger fires once for
     * the event even when multiple creatures dealt damage simultaneously.
     */
    public void checkAllyCreaturesDealDamageToOpponentTriggers(GameData gameData, UUID sourceControllerId,
                                                                UUID damagedPlayerId,
                                                                List<Permanent> damageDealers) {
        checkAllyCreaturesDealDamageToOpponentTriggers(gameData, sourceControllerId, damagedPlayerId,
                damageDealers, snapshotAllyCreaturesDealDamageToOpponentTriggers(gameData)
                        .getOrDefault(sourceControllerId, List.of()));
    }

    /** Watcher and its abilities at the time the damage event occurred. */
    public record AllyDamageTriggerSnapshot(Permanent permanent, List<TriggeredAbility> abilities) {
        public AllyDamageTriggerSnapshot {
            permanent = new Permanent(permanent);
            abilities = List.copyOf(abilities);
        }
    }

    /**
     * The "whenever one or more creatures you control deal damage to a player [opponent]" abilities {@code watch}
     * reads of {@code permanent}: its printed ones unless it lost them, then those granted to it for a duration and by
     * other permanents' static abilities.
     */
    private List<TriggeredAbility> allyCreaturesDealDamageAbilities(GameData gameData, Permanent permanent,
                                                                    DamageWatch watch) {
        List<TriggeredAbility> abilities = new ArrayList<>();
        if (watch != DamageWatch.ON_ALLY_CREATURES_DEAL_DAMAGE_TO_OPPONENT
                || !gameQueryService.hasLostPrintedAbilities(gameData, permanent)) {
            abilities.addAll(DamageAbilityReader.printed(permanent.getCard(), watch));
        }
        abilities.addAll(WatchedAbilities.temporary(permanent, watch.trigger()));
        abilities.addAll(WatchedAbilities.persistent(permanent, watch.trigger()));
        abilities.addAll(grantedTriggeredAbilitySupport.grantedWatchedAbilities(gameData, permanent, watch.trigger()));
        return abilities;
    }

    /**
     * Whether one of {@code dealers} matches the filter of "whenever one or more [filtered] creatures you control deal
     * damage" ({@code null}: any), judged with {@code watcher} as the ability's source; with {@code creaturesOnly},
     * only creatures count.
     */
    private boolean anyDealerMatches(GameData gameData, Permanent watcher, UUID controllerId,
                                     PermanentPredicate filter, List<Permanent> dealers, boolean creaturesOnly) {
        return dealers.stream()
                .filter(dealer -> !creaturesOnly || gameQueryService.isCreature(gameData, dealer))
                .anyMatch(dealer -> filter == null
                        || damageFilterMatches(gameData, watcher, controllerId, filter, dealer));
    }

    /** Captures watchers before damage casualties remove them or the effects granting their abilities. */
    public Map<UUID, List<AllyDamageTriggerSnapshot>> snapshotAllyCreaturesDealDamageToOpponentTriggers(
            GameData gameData) {
        Map<UUID, List<AllyDamageTriggerSnapshot>> snapshots = new LinkedHashMap<>();
        gameData.forEachPermanent((controllerId, permanent) -> {
            List<TriggeredAbility> abilities = allyCreaturesDealDamageAbilities(gameData, permanent,
                    DamageWatch.ON_ALLY_CREATURES_DEAL_DAMAGE_TO_OPPONENT);
            if (!abilities.isEmpty()) {
                snapshots.computeIfAbsent(controllerId, ignored -> new ArrayList<>())
                        .add(new AllyDamageTriggerSnapshot(permanent, abilities));
            }
        });
        return snapshots;
    }

    public void checkAllyCreaturesDealDamageToOpponentTriggers(GameData gameData, UUID sourceControllerId,
                                                               UUID damagedPlayerId, List<Permanent> damageDealers,
                                                               List<AllyDamageTriggerSnapshot> watchers) {
        if (sourceControllerId == null || damagedPlayerId == null
                || sourceControllerId.equals(damagedPlayerId)
                || damageDealers == null || damageDealers.isEmpty()) {
            return;
        }

        var context = new TriggerContext.AllyCreaturesDealDamageToPlayer(
                damagedPlayerId, sourceControllerId, damageDealers);
        DamageWatch toOpponent = DamageWatch.ON_ALLY_CREATURES_DEAL_DAMAGE_TO_OPPONENT;
        for (AllyDamageTriggerSnapshot watcher : watchers) {
            for (TriggeredAbility ability : watcher.abilities()) {
                requireEachEvent(ability, toOpponent);
                PermanentPredicate filter = DamageAbilityReader.filter(ability, toOpponent.trigger());
                // "One or more Pirates you control" counts any object with the type, so a Kindred Pirate spell is a
                // dealer too; only an unfiltered trigger is limited to creatures.
                if (!anyDealerMatches(gameData, watcher.permanent(), sourceControllerId, filter, damageDealers,
                        filter == null)) {
                    continue;
                }
                putAllyCreaturesDealDamageAbility(gameData, ability, watcher.permanent(), sourceControllerId,
                        damagedPlayerId, context);
            }
        }
    }

    /**
     * Records that {@code sacrificingPlayerId} sacrificed {@code sacrificedCard}, for "this turn" counts and sacrifice
     * boons. The abilities that trigger on the sacrifice ("whenever you sacrifice a permanent") are the trigger
     * pipeline's: the departure's rules event carries the sacrifice.
     */
    public void recordSacrifice(GameData gameData, UUID sacrificingPlayerId, Card sacrificedCard) {
        gameData.playersWhoSacrificedPermanentsThisTurn.add(sacrificingPlayerId);
        gameData.recordSacrificedPermanent(sacrificingPlayerId, sacrificedCard);
        checkSacrificeBoonTriggers(gameData, sacrificingPlayerId);
        if (gameData.simultaneousDyingPermanents.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
        }
    }

    /** Fires finite digital sacrifice boons once for each successful permanent sacrifice. */
    private void checkSacrificeBoonTriggers(GameData gameData, UUID sacrificingPlayerId) {
        if (sacrificingPlayerId == null) {
            return;
        }
        if (gameData.simultaneousSacrificeDepth > 0
                && !gameData.sacrificeBoonBatchPlayerIds.add(sacrificingPlayerId)) {
            return;
        }
        List<SacrificeBoonWatcher> watchers = List.copyOf(gameData.sacrificeBoonWatchers);
        for (SacrificeBoonWatcher watcher : watchers) {
            if (!watcher.controllerId().equals(sacrificingPlayerId)
                    || watcher.remainingUses() <= 0
                    || !gameData.sacrificeBoonWatchers.remove(watcher)) {
                continue;
            }

            int remainingUses = watcher.remainingUses() - 1;
            if (remainingUses > 0) {
                gameData.sacrificeBoonWatchers.add(new SacrificeBoonWatcher(
                        watcher.controllerId(), watcher.sourceCard(), watcher.effect(), remainingUses));
            }

            gameData.enqueueTrigger(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    watcher.sourceCard(),
                    watcher.controllerId(),
                    watcher.sourceCard().getName() + "'s ability",
                    new ArrayList<>(List.of(watcher.effect()))));
            gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
            log.info("Game {} - {} sacrifice boon triggers ({} use(s) remain)", gameData.id,
                    watcher.sourceCard().getName(), remainingUses);
        }
    }

    /**
     * Puts one ability of {@code candidate} that watches another object's zone change on the stack for
     * {@code events}, binding the event data its effects need the way the old slot collector of {@code watch} did.
     * The trigger pipeline has already decided that it triggers: its relation, its filter, the zone it functions
     * from and the look-back of the event (CR 603.10a). {@code events} holds one event, or each event of the batch
     * that met a "one or more" ability's trigger condition, the first of which its effects bind.
     *
     * @return whether anything was put on the stack or queued for it
     */
    /**
     * The source permanent an old collector body puts on the stack. A permanent that left in the same event is its
     * last-known copy, whose card is a layered copy; the body names the card the candidate names instead, as the
     * pipeline's own stack entries do, so effects that act on "this card" find it.
     */
    private static Permanent namedSourcePermanent(TriggerCandidate candidate) {
        Permanent perm = candidate.sourceAsPermanent();
        if (candidate.sourcePermanent() == null || perm.getCard() == candidate.sourceCard()) {
            return perm;
        }
        Permanent named = new Permanent(perm);
        named.setCard(candidate.sourceCard());
        return named;
    }

    public boolean triggerZoneChange(GameData gameData, ZoneChangeWatch watch, TriggerCandidate candidate,
                                     List<CardEffect> effects, List<ZoneChangeEvent> events) {
        if (candidate.ability().options().limit() == TriggerLimit.FIRST_TIME_EACH_TURN
                && watch != ZoneChangeWatch.ON_ALLY_TOKEN_ENTERS_BATTLEFIELD
                && watch != ZoneChangeWatch.ON_ALLY_LAND_PUT_INTO_GRAVEYARD_FROM_ANYWHERE) {
            throw new IllegalStateException("Only the token-creation and land-to-graveyard collectors read "
                    + TriggerLimit.FIRST_TIME_EACH_TURN + ": " + candidate.ability());
        }
        if (watch.entering()) {
            return triggerEntry(gameData, watch, candidate, effects, events);
        }
        ZoneChangeEvent event = events.getFirst();
        Permanent perm = namedSourcePermanent(candidate);
        UUID controllerId = candidate.controllerId();
        Card movedCard = lastKnownCard(event);
        String markOnAcceptance = TriggeredAbilityStackService.onceEachTurnActionKey(candidate.ability());
        boolean triggered = false;
        switch (watch) {
            case ON_ALLY_PERMANENT_SACRIFICED -> triggered = triggerAllyPermanentSacrificed(gameData, perm,
                    controllerId, effects, markOnAcceptance, event);
            case ON_OPPONENT_PERMANENT_SACRIFICED -> {
                var ctx = new TriggerContext.OpponentPermanentSacrificed(event.controllerId(), movedCard);
                triggered |= dispatch(new TriggerMatchContext(gameData, perm, controllerId, effects), watch, ctx);
            }
            case ON_OPPONENT_NONTOKEN_PERMANENT_SACRIFICED -> {
                var ctx = new TriggerContext.AllySacrificed(event.controllerId(), movedCard, event.permanent());
                triggered |= dispatch(new TriggerMatchContext(gameData, perm, controllerId, effects), watch, ctx);
            }
            case ON_ANY_PERMANENT_SACRIFICED -> {
                var ctx = new TriggerContext.PermanentSacrificed(event.controllerId(), movedCard);
                triggered |= dispatch(new TriggerMatchContext(gameData, perm, controllerId, effects), watch, ctx);
            }
            case ON_ANY_CREATURE_SACRIFICED -> triggered = dispatchWatchEffects(gameData, candidate, watch, effects,
                    new TriggerContext.AllySacrificed(event.controllerId(), movedCard, event.permanent()));
            case ON_ANY_NONCREATURE_ARTIFACT_SACRIFICED_OR_DESTROYED -> triggered = dispatchWatchEffects(gameData,
                    candidate, watch, effects, anyPermanentGraveyard(event));
            case EXILE_ON_CONTROLLER_TOKEN_SACRIFICED -> {
                var ctx = new TriggerContext.AllySacrificed(event.controllerId(), movedCard, event.permanent());
                triggered |= registry.dispatch(new TriggerMatchContext(gameData, null, controllerId, effects,
                        candidate.sourceCard()), watch, ctx);
            }
            case GRAVEYARD_ON_CONTROLLER_PERMANENT_SACRIFICED -> triggered = triggerGraveyardPermanentSacrificed(
                    gameData, candidate.sourceCard(), controllerId, effects);
            case ON_CREATURE_PUT_INTO_CONTROLLER_GRAVEYARD_FROM_BATTLEFIELD,
                 ON_PERMANENT_PUT_INTO_OPPONENT_GRAVEYARD_FROM_BATTLEFIELD,
                 ON_ANY_NONTOKEN_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD -> triggered = dispatchWatchEffects(
                    gameData, candidate, watch, effects, anyPermanentGraveyard(event));
            case GRAVEYARD_ON_CREATURE_PUT_INTO_CONTROLLER_GRAVEYARD_FROM_BATTLEFIELD,
                 GRAVEYARD_ON_CREATURE_PUT_INTO_OPPONENT_GRAVEYARD_FROM_BATTLEFIELD -> {
                triggered = queueGraveyardCreaturePutTrigger(gameData, candidate.sourceCard(), controllerId, effects);
            }
            case ON_ANY_ARTIFACT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
                 ON_ARTIFACT_PUT_INTO_OPPONENT_GRAVEYARD_FROM_BATTLEFIELD,
                 ON_ARTIFACT_PUT_INTO_OPPONENT_OWNER_GRAVEYARD_FROM_BATTLEFIELD -> triggered = dispatchWatchEffects(
                    gameData, candidate, watch, effects, new TriggerContext.ArtifactGraveyard(event.ownerId(),
                            event.controllerId(), event.card(), movedCard.getManaValue(),
                            new Permanent(event.permanent()), event.cause() == ZoneChangeCause.SACRIFICE));
            case ON_ANY_ENCHANTMENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
                 ON_ALLY_ENCHANTMENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD -> triggered = dispatchWatchEffects(gameData,
                    candidate, watch, effects,
                    new TriggerContext.EnchantmentGraveyard(event.ownerId(), event.controllerId()));
            case ON_OPPONENT_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD -> triggered = dispatchWatchEffects(
                    gameData, candidate, watch, effects, anyPermanentGraveyard(event));
            case ON_ANY_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD -> triggered =
                    dispatchAnyPermanentDeathTriggersForWatcher(gameData, controllerId, perm, effects,
                            watchesPermanentsYouControl(candidate), event.card(), anyPermanentGraveyard(event));
            case ON_ALLY_NONTOKEN_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD -> {
                var ctx = anyPermanentGraveyard(event);
                triggered |= dispatch(new TriggerMatchContext(gameData, perm, controllerId, effects), watch, ctx);
            }
            case ON_ALLY_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD -> {
                var ctx = anyPermanentGraveyard(event);
                triggered |= dispatch(new TriggerMatchContext(gameData, perm, controllerId, effects), watch, ctx);
            }
            case ON_PERMANENT_PUT_INTO_YOUR_GRAVEYARD_FROM_BATTLEFIELD -> {
                var ctx = anyPermanentGraveyard(event);
                triggered |= dispatch(new TriggerMatchContext(gameData, perm, controllerId, effects), watch, ctx);
            }
            case ON_ALLY_LAND_PUT_INTO_GRAVEYARD_BY_OPPONENT, ON_ALLY_NONCREATURE_PERMANENT_DESTROYED_BY_OPPONENT,
                 ON_ANY_LAND_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD -> triggered = dispatchWatchEffects(gameData,
                    candidate, watch, effects, anyPermanentGraveyard(event));
            case ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD -> {
                CreatureDeath death = CreatureDeath.of(event);
                Permanent enchanted = event.permanent();
                var ctx = new TriggerContext.EnchantedPermanentDeath(enchanted.getId(), event.controllerId(),
                        movedCard.getId(), death.power(), death.toughness(), movedCard.getManaValue(),
                        movedCard.hasType(CardType.CREATURE),
                        enchanted.cardsLeavingBattlefield().stream().map(Card::getId).toList(), event.ownerId(),
                        movedCard, new Permanent(enchanted));
                triggered |= dispatch(new TriggerMatchContext(gameData, perm, controllerId, effects), watch, ctx);
            }
            case ON_ALLY_AURA_OR_EQUIPMENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD -> triggered = dispatchWatchEffects(
                    gameData, candidate, watch, effects,
                    new TriggerContext.AllyAuraOrEquipmentGraveyard(movedCard, event.controllerId()));
            case ON_ENCHANTED_PERMANENT_LEAVES_BATTLEFIELD -> {
                var ctx = new TriggerContext.EnchantedPermanentLeaves(new Permanent(event.permanent()),
                        event.controllerId(), event.to());
                triggered |= dispatch(new TriggerMatchContext(gameData, perm, controllerId, effects), watch, ctx);
            }
            case ON_ANOTHER_PERMANENT_LEAVES_BATTLEFIELD -> triggered = triggerAnotherPermanentLeaves(gameData, perm,
                    controllerId, effects, event);
            case ON_ANOTHER_CREATURE_LEAVES_BATTLEFIELD -> triggered = triggerAnotherCreatureLeaves(gameData, perm,
                    controllerId, effects, event);
            case ON_ANOTHER_ARTIFACT_LEAVES_BATTLEFIELD -> triggered = triggerAnotherArtifactLeaves(gameData, perm,
                    controllerId, effects, event);
            case ON_ANOTHER_NONTOKEN_ARTIFACT_PUT_INTO_GRAVEYARD_OR_EXILE_FROM_BATTLEFIELD -> triggered =
                    triggerAnotherNontokenArtifactPutIntoGraveyardOrExile(gameData, perm, controllerId, effects,
                            event);
            case ON_ALLY_CREATURE_LEAVES_BATTLEFIELD -> triggered = triggerAllyCreatureLeaves(gameData, perm,
                    controllerId, effects, event);
            case ON_ALLY_CREATURE_EXILED_FROM_BATTLEFIELD -> triggered = triggerAllyCreatureExiled(gameData, perm,
                    controllerId, effects, event);
            case ON_ANY_CREATURE_EXILED_FROM_BATTLEFIELD -> {
                var ctx = new TriggerContext.CreatureExiledFromBattlefield(new Permanent(event.permanent()),
                        event.controllerId(), Math.max(0, CreatureDeath.of(event).power()));
                triggered = dispatchWatchEffects(gameData, candidate, watch, effects, ctx);
            }
            case ON_ANY_ARTIFACT_EXILED_FROM_BATTLEFIELD -> triggered = dispatchWatchEffects(gameData, candidate,
                    watch, effects, new TriggerContext.ArtifactExiledFromBattlefield(new Permanent(event.permanent()),
                            event.controllerId()));
            case ON_ALLY_CREATURES_LEAVE_BATTLEFIELD_WITHOUT_DYING, ON_ALLY_CREATURE_LEAVES_BATTLEFIELD_WITHOUT_DYING,
                 ON_SELF_OR_ALLY_CREATURES_LEAVE_BATTLEFIELD_WITHOUT_DYING -> {
                enqueueCreatureLeavesBattlefieldWithoutDyingTriggers(gameData, perm, controllerId, effects);
                triggered = !effects.isEmpty();
            }
            case ON_ALLY_PERMANENT_LEAVES_BATTLEFIELD_DURING_CONTROLLER_TURN -> triggered =
                    triggerAllyPermanentLeavesDuringYourTurn(gameData, perm, controllerId, effects, event);
            case ON_ALLY_PERMANENT_LEAVES_BATTLEFIELD -> triggered = triggerAllyPermanentLeaves(gameData, perm,
                    controllerId, effects, event);
            case ON_ALLY_PERMANENTS_LEAVE_BATTLEFIELD -> {
                enqueueAllyPermanentsLeaveBattlefieldTriggers(gameData, perm, controllerId, effects);
                triggered = !effects.isEmpty();
            }
            case ON_CONTROLLER_SPELL_OR_ABILITY_EXILES_PERMANENT -> triggered = dispatchWatchEffects(gameData,
                    candidate, watch, effects, new TriggerContext.ControllerSpellOrAbilityExilesPermanent(
                            new Permanent(event.permanent()), event.controllerId(), event.causeControllerId()));
            case ON_ANY_PERMANENT_RETURNED_TO_HAND -> triggered = triggerPermanentReturnedToHand(gameData, perm,
                    controllerId, effects, event);
            case ON_CONTROLLER_CREATURE_RETURNED_TO_HAND -> triggered = triggerControllerCreatureReturnedToHand(
                    gameData, perm, controllerId, effects, event);
            case ON_CONTROLLER_PERMANENT_RETURNED_TO_HAND -> triggered = triggerControllerPermanentReturnedToHand(
                    gameData, perm, controllerId, effects, event);
            case ON_CONTROLLER_ANOTHER_NONLAND_PERMANENT_RETURNED_TO_HAND -> triggered =
                    triggerControllerAnotherNonlandPermanentReturnedToHand(gameData, perm, controllerId, effects,
                            event);
            case ON_ANY_CARD_EXILED -> triggered = dispatchWatchEffects(gameData, candidate, watch, effects,
                    new TriggerContext.CardExiled());
            case ON_CONTROLLER_CARDS_EXILED_DURING_TURN -> triggered = dispatchWatchEffects(gameData, candidate, watch,
                    effects, new TriggerContext.CardsExiledDuringTurn(controllerId));
            case ON_YOUR_COMMANDER_PUT_INTO_COMMAND_ZONE -> {
                if (isCommanderOf(gameData, event.card(), controllerId)) {
                    triggered = dispatchWatchEffects(gameData, candidate, watch, effects,
                            new TriggerContext.CommanderPutIntoCommandZone(event.card(), controllerId,
                                    event.permanent()));
                }
            }
            case ON_CONTROLLER_CARDS_EXILED_FROM_HAND -> triggered = dispatchWatchEffects(gameData, candidate, watch,
                    effects, new TriggerContext.ControllerCardsExiledFromHand(controllerId, events.size()));
            case ON_CONTROLLER_CARD_PUT_INTO_HAND_FROM_LIBRARY -> triggered = dispatchWatchEffects(gameData, candidate,
                    watch, effects, new TriggerContext.ControllerCardPutIntoHandFromLibrary(controllerId, event.card()));
            case ON_ALLY_LAND_PUT_INTO_GRAVEYARD_FROM_ANYWHERE -> {
                // "The first time one or more land cards are put into your graveyard each turn": only if this event's
                // lands are all the land cards that entered your graveyard this turn.
                boolean firstLandEvent = gameData.landCardGraveyardEntriesThisTurn.getOrDefault(controllerId, 0)
                        <= events.size();
                triggered = (firstLandEvent || candidate.ability().options().limit()
                        != TriggerLimit.FIRST_TIME_EACH_TURN)
                        && dispatchWatchEffects(gameData, candidate, watch, effects,
                        new TriggerContext.LandPutIntoGraveyard(event.card(), controllerId, null));
            }
            case ON_ALLY_LAND_CARD_MILLED -> triggered = dispatchWatchEffects(gameData, candidate, watch, effects,
                    new TriggerContext.LandCardMilled(event.card(), controllerId));
            case ON_ALLY_PERMANENT_CARD_PUT_INTO_GRAVEYARD_FROM_LIBRARY,
                 ON_ALLY_PERMANENT_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE -> triggered = dispatchWatchEffects(gameData,
                    candidate, watch, effects,
                    new TriggerContext.PermanentCardPutIntoGraveyard(event.card(), controllerId));
            case ON_ALLY_CREATURE_CARDS_PUT_INTO_GRAVEYARD_FROM_LIBRARY -> triggered = dispatchWatchEffects(gameData,
                    candidate, watch, effects,
                    new TriggerContext.CreatureCardsPutIntoGraveyardFromLibrary(controllerId, events.size(),
                            events.stream().map(ZoneChangeEvent::card).toList()));
            case ON_ALLY_CARDS_PUT_INTO_GRAVEYARD_FROM_LIBRARY -> triggered = dispatchWatchEffects(gameData,
                    candidate, watch, effects, new TriggerContext.CardsPutIntoGraveyardFromLibrary(controllerId,
                            events.size(), events.stream().map(ZoneChangeEvent::card).toList()));
            case ON_ANY_CREATURE_CARD_PUT_INTO_GRAVEYARD_FROM_LIBRARY -> triggered = dispatchWatchEffects(gameData,
                    candidate, watch, effects,
                    new TriggerContext.CreatureCardPutIntoGraveyardFromLibrary(event.card(), event.ownerId()));
            case ON_ANY_CARDS_PUT_INTO_LIBRARY -> triggered = dispatchWatchEffects(gameData, candidate, watch, effects,
                    new TriggerContext.CardsPutIntoLibrary(event.ownerId(), events.size()));
            case ON_ALLY_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE -> triggered = dispatchWatchEffects(gameData, candidate,
                    watch, effects, new TriggerContext.CardPutIntoGraveyard(event.card(), controllerId, event.from()));
            case ON_ALLY_ARTIFACT_CARD_PUT_INTO_GRAVEYARD_FROM_NONBATTLEFIELD, ON_NONBLACK_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE ->
                    triggered = dispatchWatchEffects(gameData, candidate, watch, effects,
                            new TriggerContext.CardPutIntoGraveyard(event.card(), event.ownerId()));
            case ON_ALLY_CREATURE_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE,
                 ON_ANY_CREATURE_CARD_PUT_INTO_GRAVEYARD_FROM_NONBATTLEFIELD,
                 ON_CREATURE_CARD_PUT_INTO_OPPONENT_GRAVEYARD_FROM_ANYWHERE -> triggered = dispatchWatchEffects(gameData,
                    candidate, watch, effects,
                    new TriggerContext.CreatureCardPutIntoGraveyard(event.card(), event.ownerId(), event.from()));
            case ON_CARD_PUT_INTO_OPPONENT_GRAVEYARD_FROM_ANYWHERE -> {
                var ctx = new TriggerContext.CardPutIntoGraveyard(event.card(), event.ownerId());
                if (candidate.zone() == Zone.EXILE) {
                    // From exile only while suspended with time counters on it (Nihilith).
                    UUID exiledId = candidate.sourceCard().getId();
                    Integer timeCounters = gameData.exiledCardTimeCounters.get(exiledId);
                    if (!gameData.exiledCardsWithNonSuspendTimeCounters.contains(exiledId)
                            && timeCounters != null && timeCounters > 0) {
                        triggered |= registry.dispatch(new TriggerMatchContext(gameData, null, controllerId, effects,
                                candidate.sourceCard()), watch, ctx);
                    }
                } else {
                    triggered = dispatchWatchEffects(gameData, candidate, watch, effects, ctx);
                }
            }
            case ON_BLACK_CARD_PUT_INTO_OPPONENT_GRAVEYARD_FROM_ANYWHERE -> triggered = dispatchWatchEffects(gameData,
                    candidate, watch, effects,
                    new TriggerContext.CardPutIntoGraveyard(event.card(), event.ownerId(), event.from()));
            case GRAVEYARD_ON_ANY_LAND_PUT_INTO_GRAVEYARD_FROM_ANYWHERE -> triggered = triggerGraveyardLandPutIntoGraveyard(
                    gameData, candidate.sourceCard(), controllerId, effects);
            case ON_OPPONENT_MILLS -> triggered = dispatchWatchEffects(gameData, candidate, watch, effects,
                    new TriggerContext.Mill(event.ownerId(), events.size()));
            case ON_NONLAND_CARDS_MILLED -> triggered = dispatchWatchEffects(gameData, candidate, watch, effects,
                    new TriggerContext.NonlandCardsMilled(event.ownerId(), events.size()));
            case GRAVEYARD_ON_OPPONENT_NONLAND_CARD_MILLED -> {
                Card card = candidate.sourceCard();
                if (!effects.isEmpty()) {
                    StackEntry milledTrigger = new StackEntry(StackEntryType.TRIGGERED_ABILITY, card, controllerId,
                            card.getName() + "'s ability", new ArrayList<>(effects));
                    milledTrigger.setSourceGraveyardEntryVersion(gameData.graveyardEntryVersion(card.getId()));
                    gameData.enqueueTrigger(milledTrigger);
                    gameLogService.append(gameData, GameLog.abilityTriggers(card));
                    triggered = true;
                }
            }
            case ON_OPPONENT_CREATURE_CARD_MILLED -> triggered = dispatchWatchEffects(gameData, candidate, watch,
                    effects, new TriggerContext.CardPutIntoGraveyard(event.card(), event.ownerId(), event.from()));
            case GRAVEYARD_ON_CREATURE_CARD_LEAVES_OPPONENT_GRAVEYARD, GRAVEYARD_ON_SELF_RETURNED_TO_HAND -> {
                Card card = candidate.sourceCard();
                if (!effects.isEmpty()) {
                    gameData.enqueueTrigger(new StackEntry(StackEntryType.TRIGGERED_ABILITY, card, controllerId,
                            card.getName() + "'s ability", new ArrayList<>(effects)));
                    gameLogService.append(gameData, GameLog.abilityTriggers(card));
                    triggered = true;
                }
            }
            case ON_CONTROLLER_CARDS_LEAVE_GRAVEYARD, ON_CONTROLLER_ARTIFACT_OR_CREATURE_CARDS_LEAVE_GRAVEYARD ->
                    triggered = dispatchWatchEffects(gameData, candidate, watch, effects,
                            new TriggerContext.ControllerCardsLeaveGraveyard(controllerId,
                                    events.stream().map(ZoneChangeEvent::card).toList()));
            case ON_CONTROLLER_CREATURE_CARDS_LEAVE_GRAVEYARD, ON_CONTROLLER_CREATURE_CARD_LEAVES_GRAVEYARD,
                 ON_CONTROLLER_ARTIFACT_CARDS_LEAVE_GRAVEYARD -> triggered = dispatchWatchEffects(gameData, candidate,
                    watch, effects, new TriggerContext.ControllerCardsLeaveGraveyard(controllerId));
            case ON_CONTROLLER_CARD_RETURNED_FROM_GRAVEYARD_TO_HAND -> triggered = dispatchWatchEffects(gameData,
                    candidate, watch, effects,
                    new TriggerContext.ControllerCardReturnedFromGraveyardToHand(controllerId, event.card()));
            case ON_CONTROLLER_INSTANT_OR_SORCERY_CARD_LEAVES_GRAVEYARD -> {
                Card card = event.card();
                if (!card.isToken() && (card.hasType(CardType.INSTANT) || card.hasType(CardType.SORCERY))) {
                    triggered = dispatchWatchEffects(gameData, candidate, watch, effects,
                            new TriggerContext.ControllerInstantOrSorceryCardLeavesGraveyard(controllerId, card));
                }
            }
            case ON_CONTROLLER_CARDS_EXILED_FROM_GRAVEYARD -> triggered = dispatchWatchEffects(gameData, candidate,
                    watch, effects, new TriggerContext.ControllerCardsExiledFromGraveyard(controllerId, events.size()));
            case ON_CONTROLLER_CARDS_EXILED_FROM_LIBRARY_OR_GRAVEYARD -> triggered = dispatchWatchEffects(gameData,
                    candidate, watch, effects,
                    new TriggerContext.ControllerCardsExiledFromLibraryOrGraveyard(controllerId, events.size()));
            case ON_CONTROLLER_CREATURES_OR_CREATURE_CARDS_EXILED -> triggered = dispatchWatchEffects(gameData,
                    candidate, watch, effects, new TriggerContext.ControllerCreaturesOrCreatureCardsExiled(controllerId,
                            events.size(), events.stream()
                            .filter(moved -> !moved.card().isToken() && moved.card().hasType(CardType.CREATURE))
                            .map(ZoneChangeEvent::card).toList()));
            case ON_OPPONENT_OWNED_CARD_EXILED -> triggered = dispatchWatchEffects(gameData, candidate, watch, effects,
                    new TriggerContext.OpponentOwnedCardExiled(event.ownerId(), event.card()));
            case ON_CARDS_EXILED_FROM_GRAVEYARDS_OR_BATTLEFIELD_DURING_YOUR_TURN -> triggered = dispatchWatchEffects(
                    gameData, candidate, watch, effects,
                    new TriggerContext.CardsExiledFromGraveyardsOrBattlefield(events.size()));
        }
        return triggered;
    }

    /** A permanent put into a graveyard from the battlefield, as the permanent-graveyard collectors read it. */
    private static TriggerContext.AnyPermanentGraveyard anyPermanentGraveyard(ZoneChangeEvent event) {
        CreatureDeath death = CreatureDeath.of(event);
        return new TriggerContext.AnyPermanentGraveyard(event.card(), event.controllerId(), event.ownerId(),
                new Permanent(event.permanent()), death.power(), death.toughness());
    }

    /** "Whenever another permanent leaves the battlefield". */
    private boolean triggerAnotherPermanentLeaves(GameData gameData, Permanent perm, UUID controllerId,
            List<CardEffect> effects, ZoneChangeEvent event) {
        Permanent leavingPermanent = event.permanent();
        UUID ownerId = controllerId;
        if (!effects.isEmpty()) {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    perm.getCard(),
                    ownerId,
                    perm.getCard().getName() + "'s ability",
                    new ArrayList<>(effects),
                    null,
                    perm.getId()
            );
            entry.setSourcePermanentSnapshot(new Permanent(perm));
            entry.setTriggeringPermanent(leavingPermanent);
            gameData.enqueueTrigger(entry);
            gameLogService.append(gameData, GameLog.text(perm.getCard().getName() + "'s ability triggers."));
            log.info("Game {} - {} triggers on another permanent leaving the battlefield ({})",
                    gameData.id, perm.getCard().getName(), leavingPermanent.getCard().getName());
        }
        return true;
    }

    /** "Whenever another creature leaves the battlefield". */
    private boolean triggerAnotherCreatureLeaves(GameData gameData, Permanent perm, UUID controllerId,
            List<CardEffect> effects, ZoneChangeEvent event) {
        Permanent leavingPermanent = event.permanent();
        UUID leavingId = leavingPermanent.getId();
        UUID ownerId = controllerId;
        UUID leavingControllerId = event.controllerId();
        if (effects.isEmpty()) {
            return true;
        }
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                perm.getCard(),
                ownerId,
                perm.getCard().getName() + "'s ability",
                new ArrayList<>(effects),
                null,
                perm.getId()
        );
        entry.setTriggeringPermanentId(leavingId);
        entry.setTriggeringPermanentControllerId(leavingControllerId);
        entry.setTriggeringPermanent(leavingPermanent);
        entry.setSourcePermanentSnapshot(new Permanent(perm));
        if (!perm.getCard().getTargetGroups().isEmpty() && declaresTarget(effects)) {
            // The card declares the trigger's target (Extractor Demon's "target player"), which is
            // chosen as the trigger is put on the stack, for the ability with all its effects.
            queueTargetChoice(gameData, entry, new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                    perm.getCard(), ownerId, new ArrayList<>(effects), declaredTargetsArePlayers(effects),
                    perm.getCard().getTargetFilter(), 0, perm.getId(), new Permanent(perm), false,
                    leavingId, null, ownerId, null).withDepartedPermanent(leavingPermanent));
        } else {
            gameData.enqueueTrigger(entry);
        }
        gameLogService.append(gameData, GameLog.text(perm.getCard().getName() + "'s ability triggers."));
        log.info("Game {} - {} triggers on another creature leaving the battlefield ({})",
                gameData.id, perm.getCard().getName(), leavingPermanent.getCard().getName());
        return true;
    }

    /** "Whenever another permanent you control leaves the battlefield". */
    private boolean triggerAllyPermanentLeaves(GameData gameData, Permanent perm, UUID controllerId,
            List<CardEffect> effects, ZoneChangeEvent event) {
        Permanent leavingPermanent = event.permanent();
        // The pipeline has judged the ability's intervening "if" against the event (CR 603.4); an "if" of an effect is
        // judged as the ability resolves, so the ability goes on the stack whole (CR 603.3).
        List<CardEffect> abilityEffects = effects;
        if (abilityEffects.isEmpty()) {
            return true;
        }
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                perm.getCard(),
                controllerId,
                perm.getCard().getName() + "'s ability",
                new ArrayList<>(abilityEffects),
                null,
                perm.getId()
        );
        entry.setTriggeringPermanent(leavingPermanent);
        if (targetsPermanentOrPlayer(abilityEffects)) {
            // The target is chosen as the ability goes on the stack (CR 603.3d), with all its effects.
            queueTargetChoice(gameData, entry, new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                    perm.getCard(), controllerId, new ArrayList<>(abilityEffects), false,
                    perm.getCard().getTargetFilter(), 0, perm.getId())
                    .withDepartedPermanent(leavingPermanent));
            gameLogService.append(gameData, GameLog.text(
                    perm.getCard().getName() + "'s ability triggers — choose a target."));
            return true;
        }
        gameData.enqueueTrigger(entry);
        gameLogService.append(gameData, GameLog.text(perm.getCard().getName() + "'s ability triggers."));
        log.info("Game {} - {} triggers on another permanent you control leaving the battlefield ({})",
                gameData.id, perm.getCard().getName(), leavingPermanent.getCard().getName());
        return true;
    }

    /** "Whenever another creature you control leaves the battlefield". */
    private boolean triggerAllyCreatureLeaves(GameData gameData, Permanent perm, UUID controllerId,
            List<CardEffect> effects, ZoneChangeEvent event) {
        Permanent leavingPermanent = event.permanent();
        // The pipeline has judged the ability's intervening "if" against the event (CR 603.4); an "if" of an effect is
        // judged as the ability resolves, so the ability goes on the stack whole (CR 603.3).
        List<CardEffect> abilityEffects = effects;
        if (abilityEffects.isEmpty()) {
            return true;
        }
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                perm.getCard(),
                controllerId,
                perm.getCard().getName() + "'s ability",
                new ArrayList<>(abilityEffects),
                null,
                perm.getId()
        );
        entry.setTriggeringPermanent(leavingPermanent);
        if (targetsPermanentOrPlayer(abilityEffects)) {
            // The target is chosen as the ability goes on the stack (CR 603.3d), with all its effects.
            queueTargetChoice(gameData, entry, new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                    perm.getCard(), controllerId, new ArrayList<>(abilityEffects), false,
                    perm.getCard().getTargetFilter(), 0, perm.getId())
                    .withDepartedPermanent(leavingPermanent));
            gameLogService.append(gameData, GameLog.text(
                    perm.getCard().getName() + "'s ability triggers — choose a target."));
            return true;
        }
        gameData.enqueueTrigger(entry);
        gameLogService.append(gameData, GameLog.text(perm.getCard().getName() + "'s ability triggers."));
        log.info("Game {} - {} triggers on another creature you control leaving the battlefield ({})",
                gameData.id, perm.getCard().getName(), leavingPermanent.getCard().getName());
        return true;
    }

    /** "Whenever another permanent you control leaves the battlefield during your turn". */
    private boolean triggerAllyPermanentLeavesDuringYourTurn(GameData gameData, Permanent perm, UUID controllerId,
            List<CardEffect> effects, ZoneChangeEvent event) {
        Permanent leavingPermanent = event.permanent();
        UUID leavingId = leavingPermanent.getId();
        if (!effects.isEmpty()) {
            gameData.enqueueTrigger(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    perm.getCard(),
                    controllerId,
                    perm.getCard().getName() + "'s ability",
                    new ArrayList<>(effects),
                    null,
                    perm.getId()
            ));
            gameLogService.append(gameData, GameLog.text(perm.getCard().getName() + "'s ability triggers."));
            log.info("Game {} - {} triggers on another permanent you control leaving the battlefield ({})",
                    gameData.id, perm.getCard().getName(), leavingPermanent.getCard().getName());
        }
        return true;
    }

    /** "Whenever another creature you control is put into exile from the battlefield". */
    private boolean triggerAllyCreatureExiled(GameData gameData, Permanent perm, UUID controllerId,
            List<CardEffect> effects, ZoneChangeEvent event) {
        Permanent exiledPermanent = event.permanent();
        if (!effects.isEmpty()) {
            gameData.enqueueTrigger(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    perm.getCard(),
                    controllerId,
                    perm.getCard().getName() + "'s ability",
                    new ArrayList<>(effects),
                    null,
                    perm.getId()
            ));
            gameLogService.append(gameData, GameLog.text(perm.getCard().getName() + "'s ability triggers."));
            log.info("Game {} - {} triggers on another creature you control being put into exile ({})",
                    gameData.id, perm.getCard().getName(), exiledPermanent.getCard().getName());
        }
        return true;
    }

    /** "Whenever another artifact you control leaves the battlefield". */
    private boolean triggerAnotherArtifactLeaves(GameData gameData, Permanent perm, UUID controllerId,
            List<CardEffect> effects, ZoneChangeEvent event) {
        Permanent leavingPermanent = event.permanent();
        if (!effects.isEmpty()) {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    perm.getCard(),
                    controllerId,
                    perm.getCard().getName() + "'s ability",
                    new ArrayList<>(effects),
                    null,
                    perm.getId()
            );
            entry.setSourcePermanentSnapshot(new Permanent(perm));
            if (declaresTarget(effects)) {
                // The target is chosen as the ability goes on the stack (CR 603.3d), with all its effects.
                queueTargetChoice(gameData, entry, new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                        perm.getCard(), controllerId, new ArrayList<>(effects), declaredTargetsArePlayers(effects),
                        perm.getCard().getTargetFilter(), 0, perm.getId(), new Permanent(perm)));
            } else {
                gameData.enqueueTrigger(entry);
            }
            gameLogService.append(gameData, GameLog.text(perm.getCard().getName() + "'s ability triggers."));
            log.info("Game {} - {} triggers on another artifact leaving the battlefield ({})",
                    gameData.id, perm.getCard().getName(), leavingPermanent.getCard().getName());
        }
        return true;
    }

    /** "Whenever another nontoken artifact you control is put into a graveyard or exiled from the battlefield". */
    private boolean triggerAnotherNontokenArtifactPutIntoGraveyardOrExile(GameData gameData, Permanent perm, UUID controllerId,
            List<CardEffect> effects, ZoneChangeEvent event) {
        Permanent leavingPermanent = event.permanent();
        Zone destination = event.to();
        if (!effects.isEmpty()) {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    perm.getCard(),
                    controllerId,
                    perm.getCard().getName() + "'s ability",
                    new ArrayList<>(effects),
                    null,
                    perm.getId()
            );
            entry.setSourcePermanentSnapshot(new Permanent(perm));
            if (declaresTarget(effects)) {
                // The target is chosen as the ability goes on the stack (CR 603.3d), with all its effects.
                queueTargetChoice(gameData, entry, new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                        perm.getCard(), controllerId, new ArrayList<>(effects), declaredTargetsArePlayers(effects),
                        perm.getCard().getTargetFilter(), 0, perm.getId(), new Permanent(perm)));
            } else {
                gameData.enqueueTrigger(entry);
            }
            gameLogService.append(gameData, GameLog.text(perm.getCard().getName() + "'s ability triggers."));
            log.info("Game {} - {} triggers on another nontoken artifact entering {} from the battlefield ({})",
                    gameData.id, perm.getCard().getName(), destination, leavingPermanent.getCard().getName());
        }
        return true;
    }

    /** "Whenever a permanent is returned to a player's hand": one stack object for its effects. */
    private boolean triggerPermanentReturnedToHand(GameData gameData, Permanent perm, UUID controllerId,
            List<CardEffect> effects, ZoneChangeEvent event) {
        Permanent returnedPermanent = event.permanent();
        UUID returnedToPlayerId = event.ownerId();
        List<CardEffect> resolvedEffects = new ArrayList<>(effects);
        if (resolvedEffects.isEmpty()) return false;

        StackEntry trigger = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                perm.getCard(),
                controllerId,
                perm.getCard().getName() + "'s ability",
                resolvedEffects,
                returnedToPlayerId,
                perm.getId());
        // "That player" is the owner the permanent returned to — determined by the event, not chosen.
        trigger.setNonTargeting(true);
        gameData.stack.add(trigger);

        gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
        log.info("Game {} - {} permanent-returned-to-hand trigger pushed onto stack",
                gameData.id, perm.getCard().getName());

        return true;
    }

    /** "Whenever a creature is returned to your hand from the battlefield". */
    private boolean triggerControllerCreatureReturnedToHand(GameData gameData, Permanent perm, UUID controllerId,
            List<CardEffect> effects, ZoneChangeEvent event) {
        Permanent returnedPermanent = event.permanent();
        UUID returnedToPlayerId = controllerId;
        if (!effects.isEmpty()) {
            gameData.enqueueTrigger(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    perm.getCard(),
                    returnedToPlayerId,
                    perm.getCard().getName() + "'s ability",
                    new ArrayList<>(effects),
                    null,
                    perm.getId()
            ));
            gameLogService.append(gameData, GameLog.text(perm.getCard().getName() + "'s ability triggers."));
            log.info("Game {} - {} triggers on a creature returned to its controller's hand ({})",
                    gameData.id, perm.getCard().getName(), returnedPermanent.getCard().getName());
        }
        return true;
    }

    /** "Whenever a permanent is returned to your hand". */
    private boolean triggerControllerPermanentReturnedToHand(GameData gameData, Permanent perm, UUID controllerId,
            List<CardEffect> effects, ZoneChangeEvent event) {
        UUID returnedToPlayerId = controllerId;
        if (!effects.isEmpty()) {
            gameData.enqueueTrigger(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    perm.getCard(),
                    returnedToPlayerId,
                    perm.getCard().getName() + "'s ability",
                    new ArrayList<>(effects),
                    null,
                    perm.getId()
            ));
            gameLogService.append(gameData, GameLog.text(perm.getCard().getName() + "'s ability triggers."));
            log.info("Game {} - {} triggers on a permanent returned to its controller's hand",
                    gameData.id, perm.getCard().getName());
        }
        return true;
    }

    /** "Whenever another nonland permanent you control is returned to its owner's hand". */
    private boolean triggerControllerAnotherNonlandPermanentReturnedToHand(GameData gameData, Permanent perm, UUID controllerId,
            List<CardEffect> effects, ZoneChangeEvent event) {
        UUID returnedPermanentControllerId = controllerId;
        if (!effects.isEmpty()) {
            gameData.enqueueTrigger(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    perm.getCard(),
                    returnedPermanentControllerId,
                    perm.getCard().getName() + "'s ability",
                    new ArrayList<>(effects),
                    null,
                    perm.getId()
            ));
            gameLogService.append(gameData, GameLog.text(perm.getCard().getName() + "'s ability triggers."));
            log.info("Game {} - {} triggers on another nonland permanent returned to its owner's hand",
                    gameData.id, perm.getCard().getName());
        }
        return true;
    }

    /** Whether {@code card} is one of {@code ownerId}'s commanders, either face of it. */
    private static boolean isCommanderOf(GameData gameData, Card card, UUID ownerId) {
        return gameData.playerCommanders.getOrDefault(ownerId, List.of()).stream()
                .anyMatch(commander -> commander.getId().equals(card.getId())
                        || commander.getBackFaceCard() != null
                        && commander.getBackFaceCard().getId().equals(card.getId()));
    }

    /** "Whenever a land card is put into a graveyard from anywhere", from a graveyard. */
    private boolean triggerGraveyardLandPutIntoGraveyard(GameData gameData, Card sourceCard, UUID sourceControllerId,
                                                         List<CardEffect> effects) {
        return enqueueGraveyardAbility(gameData, sourceCard, sourceControllerId, effects) != null;
    }

    /**
     * The permanent that entered in a zone change, as it is now (CR 603.6a: the game checks the permanents on the
     * battlefield after the event), or as it entered once it has left again.
     *
     * @param card         its card: the card it is now, or its entered copy's
     * @param permanent    the permanent on the battlefield, or {@code null} once it has left
     * @param controllerId the player it entered under the control of
     */
    private record Entering(Card card, Permanent permanent, UUID controllerId) {

        UUID permanentId() {
            return permanent == null ? null : permanent.getId();
        }
    }

    private Entering entering(GameData gameData, ZoneChangeEvent event) {
        Permanent live = gameQueryService.findPermanentById(gameData, event.permanent().getId());
        return new Entering(live != null ? live.getCard() : event.permanent().getCard(), live, event.controllerId());
    }

    /**
     * Puts {@code candidate}'s "whenever [an object] enters" ability, which the trigger pipeline matched to
     * {@code events}, on the stack the way {@code watch}'s old slot collector did. A "one or more" ability triggers
     * once, for the first of the batch's entries its effects accept, and the stack objects it queued name every
     * permanent of the batch that entered (CR 603.2c).
     *
     * @return whether it triggered
     */
    private boolean triggerEntry(GameData gameData, ZoneChangeWatch watch, TriggerCandidate candidate,
                                 List<CardEffect> effects, List<ZoneChangeEvent> events) {
        if (watch == ZoneChangeWatch.ON_ALLY_TOKEN_ENTERS_BATTLEFIELD
                || watch == ZoneChangeWatch.ON_OPPONENT_TOKEN_ENTERS_BATTLEFIELD) {
            return triggerTokensEnter(gameData, watch, candidate, effects, events);
        }
        boolean oneOrMore = candidate.ability().options().multiplicity() == TriggerMultiplicity.ONE_OR_MORE;
        if (!oneOrMore) {
            return triggerEntry(gameData, watch, candidate, effects, entering(gameData, events.getFirst()), false);
        }
        int stackSize = gameData.stack.size();
        for (ZoneChangeEvent event : events) {
            if (triggerEntry(gameData, watch, candidate, effects, entering(gameData, event), true)) {
                List<UUID> entered = events.stream().map(entry -> entry.permanent().getId()).toList();
                for (int index = stackSize; index < gameData.stack.size(); index++) {
                    StackEntry entry = gameData.stack.get(index);
                    entry.setTriggeringPermanentIds(entered);
                    if (watch == ZoneChangeWatch.ON_ALLY_LAND_ENTERS_BATTLEFIELD) {
                        for (ZoneChangeEvent other : events) {
                            entry.rememberLastKnownPermanentCard(other.permanent().getId(),
                                    other.permanent().getCard());
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }

    private boolean triggerEntry(GameData gameData, ZoneChangeWatch watch, TriggerCandidate candidate,
                                 List<CardEffect> effects, Entering entering, boolean oneOrMore) {
        Permanent perm = candidate.sourcePermanent();
        UUID controllerId = candidate.controllerId();
        Card sourceCard = candidate.sourceCard();
        TriggerLimit limit = candidate.ability().options().limit();
        String actionKey = TriggeredAbilityStackService.onceEachTurnActionKey(candidate.ability());
        return switch (watch) {
            case ON_ALLY_CREATURE_ENTERS_BATTLEFIELD, ON_ALLY_CREATURES_ENTERS_BATTLEFIELD,
                 ON_SELF_OR_ALLY_CREATURE_ENTERS_BATTLEFIELD, ON_SELF_OR_ALLY_CREATURES_ENTERS_BATTLEFIELD ->
                    triggerAllyCreatureEnters(gameData, watch, perm, controllerId, effects, entering, limit, actionKey);
            case ON_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD, ON_SELF_OR_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD ->
                    triggerAllyNontokenCreatureEnters(gameData, watch, perm, controllerId, effects, entering, limit, actionKey);
            case COMMAND_ZONE_ON_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD -> {
                // Panharmonicon-style doublers only multiply triggers of permanents (CR 603.2d), so a command-zone
                // source is triggered exactly once.
                var ctx = new TriggerContext.PermanentEnters(entering.card(), controllerId, null, 1,
                        entering.permanentId());
                yield dispatch(new TriggerMatchContext(gameData, null, controllerId, effects, sourceCard), watch, ctx);
            }
            case ON_ALLY_ARTIFACT_ENTERS_BATTLEFIELD ->
                    triggerAllyArtifactEnters(gameData, watch, perm, controllerId, effects, entering, limit, actionKey);
            case ON_SELF_OR_ALLY_ARTIFACT_ENTERS_BATTLEFIELD, ON_ALLY_NONTOKEN_ARTIFACT_ENTERS_BATTLEFIELD,
                 ON_SELF_OR_ALLY_NONTOKEN_ARTIFACT_ENTERS_BATTLEFIELD, ON_ALLY_EQUIPMENT_ENTERS_BATTLEFIELD -> {
                var ctx = new TriggerContext.PermanentEnters(entering.card(), controllerId, null,
                        1 + gameQueryService.countETBExtraTriggers(gameData, controllerId, controllerId,
                                entering.card()), entering.permanentId());
                yield dispatchEntering(gameData, perm, controllerId, watch, effects, limit, actionKey, ctx);
            }
            case ON_ALLY_ENCHANTMENT_ENTERS_BATTLEFIELD, ON_SELF_OR_ALLY_ENCHANTMENT_ENTERS_BATTLEFIELD ->
                    triggerAllyEnchantmentEnters(gameData, perm, controllerId, effects, entering, limit, actionKey);
            case ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD, ON_ANY_CREATURE_ENTERS_BATTLEFIELD ->
                    triggerAnyCreatureEnters(gameData, watch, perm, controllerId, effects, entering, limit, actionKey);
            case ON_OPPONENT_CREATURE_ENTERS_BATTLEFIELD -> {
                var ctx = new TriggerContext.PermanentEnters(entering.card(), entering.controllerId(),
                        entering.controllerId(), 1 + gameQueryService.countETBExtraTriggers(gameData, controllerId,
                        entering.controllerId(), entering.card()), null);
                yield dispatchEntering(gameData, perm, controllerId, watch, effects, limit, actionKey, ctx);
            }
            case ON_OPPONENT_LAND_ENTERS_BATTLEFIELD ->
                    triggerOpponentLandEnters(gameData, watch, perm, controllerId, effects, entering);
            case ON_ALLY_LAND_ENTERS_BATTLEFIELD ->
                    triggerAllyLandEnters(gameData, perm, controllerId, effects, entering, oneOrMore, limit, actionKey,
                            narrowedLandfall(candidate));
            case GRAVEYARD_ON_ALLY_LAND_ENTERS_BATTLEFIELD ->
                    triggerGraveyardLandEnters(gameData, sourceCard, controllerId, effects, entering);
            case ON_ANY_PERMANENT_ENTERS_BATTLEFIELD -> {
                // The player who put the permanent onto the battlefield is the caster of the spell it resolved
                // from, even when a replacement such as Gather Specimens makes it enter under another player's
                // control (Nature's Wrath ruling: it matters who puts it there, not who controls it).
                UUID puttingPlayerId = entering.permanent() != null
                        && entering.permanent().getCastControllerId() != null
                        ? entering.permanent().getCastControllerId() : entering.controllerId();
                var ctx = new TriggerContext.PermanentEnters(entering.card(), entering.controllerId(),
                        puttingPlayerId, 1 + gameQueryService.countETBExtraTriggers(gameData, controllerId,
                        entering.controllerId(), entering.card()), entering.permanentId());
                yield dispatchEntering(gameData, perm, controllerId, watch, effects, limit, actionKey, ctx);
            }
            case ON_ALLY_PERMANENT_ENTERS_TRANSFORMED -> dispatchWatchEffects(gameData, candidate, watch, effects,
                    new TriggerContext.PermanentEnters(entering.card(), controllerId, null,
                            1 + gameQueryService.countETBExtraTriggers(gameData, controllerId, controllerId,
                                    entering.card()), entering.permanentId()));
            case ON_ALLY_PERMANENT_ENTERS_BATTLEFIELD -> {
                var ctx = new TriggerContext.PermanentEnters(entering.card(), controllerId, null,
                        1 + gameQueryService.countETBExtraTriggers(gameData, controllerId, controllerId,
                                entering.card()), entering.permanentId());
                yield dispatchEntering(gameData, perm, controllerId, watch, effects, limit, actionKey, ctx);
            }
            case GRAVEYARD_ON_ALLY_PERMANENT_ENTERS_BATTLEFIELD -> entering.permanent() != null
                    && queueGraveyardPermanentEntersTriggers(gameData, sourceCard, controllerId, effects,
                    entering.permanent());
            case GRAVEYARD_ON_ALLY_CREATURE_ENTERS_BATTLEFIELD, GRAVEYARD_ON_ANY_CREATURE_ENTERS_BATTLEFIELD ->
                    triggerGraveyardCreatureEnters(gameData, sourceCard, controllerId, effects, entering);
            case GRAVEYARD_ON_ALLY_ARTIFACT_ENTERS_BATTLEFIELD, GRAVEYARD_ON_ALLY_ENCHANTMENT_ENTERS_BATTLEFIELD ->
                    triggerGraveyardPermanentTypeEnters(gameData, sourceCard, controllerId, effects, entering);
            case GRAVEYARD_ON_CREATURE_ENTERS_FROM_GRAVEYARD_OR_CAST_FROM_GRAVEYARD ->
                    triggerGraveyardCreatureEntersFromGraveyard(gameData, sourceCard, controllerId, effects, entering);
            case ON_CREATURE_ENTERS_FROM_GRAVEYARD -> {
                Permanent enteringPermanent = entering.permanent();
                if (enteringPermanent == null || !controllerId.equals(enteringPermanent.getEnteredFromGraveyardOwnerId())
                        || gameQueryService.areOpponentPermanentETBTriggersSuppressed(gameData, controllerId)) {
                    yield false;
                }
                int triggerCount = 1 + gameQueryService.countETBExtraTriggers(gameData, controllerId,
                        entering.controllerId(), entering.card());
                yield registry.dispatch(new TriggerMatchContext(gameData, perm, controllerId, effects), watch,
                        new TriggerContext.PermanentEnters(entering.card(), entering.controllerId(), null,
                                    triggerCount, null));
            }
            case ON_PERMANENT_ENTERS_FROM_GRAVEYARD, ON_PERMANENT_ENTERS_FROM_EXILE ->
                    triggerPermanentEntersFromZone(gameData, watch, perm, controllerId, effects, entering);
            case ON_SELF_ENTERS_FROM_GRAVEYARD -> triggerSelfEntersFromGraveyard(gameData, sourceCard, effects, entering);
            case ON_ENCHANTED_PLAYER_CREATURE_ENTERS_BATTLEFIELD, ON_ENCHANTED_PLAYER_LAND_ENTERS_BATTLEFIELD -> {
                if (gameQueryService.areOpponentPermanentETBTriggersSuppressed(gameData, controllerId)) {
                    yield false;
                }
                int triggerCount = 1 + gameQueryService.countETBExtraTriggers(gameData, controllerId,
                        entering.controllerId(), entering.card());
                for (int i = 0; i < triggerCount; i++) {
                    StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, perm.getCard(), controllerId,
                            perm.getCard().getName() + "'s ability", new ArrayList<>(effects),
                            entering.controllerId(), perm.getId());
                    entry.setNonTargeting(true);
                    gameData.stack.add(entry);
                    gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                }
                yield true;
            }
            default -> throw new IllegalArgumentException(watch + " is no entering watch");
        };
    }

    /**
     * "Whenever another creature you control enters", "this creature or another [restricted] creature you control",
     * "a creature you control", and their "one or more" forms. The trigger pipeline judged an intervening "if" about
     * the entering creature (Sigil Captain's "if that creature is 1/1", {@code TriggeringPermanentMatches}) as the
     * ability triggered; it stays wrapped for resolution (CR 603.4).
     */
    private boolean triggerAllyCreatureEnters(GameData gameData, ZoneChangeWatch watch, Permanent perm,
                                              UUID controllerId, List<CardEffect> effects, Entering entering,
                                              TriggerLimit limit, String actionKey) {
        Card enteringCreature = entering.card();
        var ctx = new TriggerContext.PermanentEnters(enteringCreature, controllerId, null,
                1 + gameQueryService.countETBExtraTriggers(gameData, controllerId, controllerId, enteringCreature),
                entering.permanentId());
        return dispatchEntering(gameData, perm, controllerId, watch, effects, limit, actionKey, ctx);
    }

    /** "Whenever another nontoken creature you control enters", "this or another". */
    private boolean triggerAllyNontokenCreatureEnters(GameData gameData, ZoneChangeWatch watch, Permanent perm,
                                                      UUID controllerId, List<CardEffect> effects,
                                                      Entering entering, TriggerLimit limit, String actionKey) {
        var ctx = new TriggerContext.PermanentEnters(entering.card(), controllerId, null,
                1 + gameQueryService.countETBExtraTriggers(gameData, controllerId, controllerId, entering.card()),
                entering.permanentId());
        return dispatchEntering(gameData, perm, controllerId, watch, effects, limit, actionKey, ctx);
    }

    /**
     * "Whenever another artifact you control enters". The trigger pipeline judged an intervening "if" that counts
     * permanents (Voldaren Bloodcaster) as the ability triggered; it stays wrapped for resolution (CR 603.4).
     */
    private boolean triggerAllyArtifactEnters(GameData gameData, ZoneChangeWatch watch, Permanent perm,
                                              UUID controllerId, List<CardEffect> effects, Entering entering,
                                              TriggerLimit limit, String actionKey) {
        Card enteringCard = entering.card();
        var ctx = new TriggerContext.PermanentEnters(enteringCard, controllerId, null,
                1 + gameQueryService.countETBExtraTriggers(gameData, controllerId, controllerId, enteringCard),
                entering.permanentId());
        return dispatchEntering(gameData, perm, controllerId, watch, effects, limit, actionKey, ctx);
    }

    /** "Whenever an enchantment you control enters" (constellation), "this or another". */
    private boolean triggerAllyEnchantmentEnters(GameData gameData, Permanent perm, UUID controllerId,
                                                 List<CardEffect> effects, Entering entering, TriggerLimit limit, String actionKey) {
        if (gameQueryService.areOpponentPermanentETBTriggersSuppressed(gameData, controllerId)) {
            return false;
        }
        Card enteringCard = entering.card();
        Permanent enteringPermanent = entering.permanent();
        if (effects.isEmpty()) {
            return false;
        }
        // The pipeline judged an intervening "if you cast it" (Light-Paws) as the ability triggered and left the fact
        // unwrapped (TriggeredAbilityStackService.withInterveningIf). An ability limited to once each turn is one
        // trigger per event, so an extra trigger from a doubler does not give it a second copy. The ability is one
        // object (CR 603.3): a targeted one goes to its target choice with every effect (CR 603.3d).
        int triggerCount = limit != TriggerLimit.NONE ? 1
                : 1 + gameQueryService.countETBExtraTriggers(gameData, controllerId, controllerId, enteringCard);
        boolean targets = effects.stream().anyMatch(effect -> effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER));
        for (int i = 0; i < triggerCount; i++) {
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, perm.getCard(), controllerId,
                    perm.getCard().getName() + "'s ability", new ArrayList<>(effects), null, perm.getId());
            // Ajani's Chosen needs the entering enchantment itself at resolution ("if that enchantment is an Aura, you
            // may attach it to the token").
            entry.setTriggeringCardId(enteringCard.getId());
            if (enteringPermanent != null) {
                entry.setTriggeringPermanentId(enteringPermanent.getId());
            }
            entry.setOnceEachTurnActionKey(actionKey);
            if (enteringPermanent != null) {
                entry.setAttachedPermanentSnapshot(new Permanent(enteringPermanent));
            }
            gameData.handOffTriggerRecord(entry);
            if (targets) {
                gameData.queueInteraction(new PermanentChoiceContext.EntersTriggerTarget(
                        perm.getCard(), controllerId, new ArrayList<>(effects), perm.getId(), null, perm.getId()));
            } else {
                gameData.stack.add(entry);
            }
            gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
        }
        return true;
    }

    /** "Whenever another creature enters", "whenever a creature enters", whoever controls it. */
    private boolean triggerAnyCreatureEnters(GameData gameData, ZoneChangeWatch watch, Permanent perm,
                                             UUID controllerId, List<CardEffect> effects, Entering entering,
                                             TriggerLimit limit, String actionKey) {
        Card enteringCreature = entering.card();
        UUID enteringControllerId = entering.controllerId();
        int extraTriggers = gameQueryService.countETBExtraTriggers(gameData, controllerId, enteringControllerId,
                enteringCreature);
        var ctx = new TriggerContext.PermanentEnters(enteringCreature, enteringControllerId, null, 1 + extraTriggers,
                null);
        return dispatchEntering(gameData, perm, controllerId, watch, effects, limit, actionKey, ctx);
    }

    /** "Whenever a land an opponent controls enters". */
    private boolean triggerOpponentLandEnters(GameData gameData, ZoneChangeWatch watch, Permanent perm,
                                              UUID controllerId, List<CardEffect> effects, Entering entering) {
        Card enteringLand = entering.card();
        UUID landControllerId = entering.controllerId();
        var ctx = new TriggerContext.PermanentEnters(enteringLand, landControllerId, landControllerId,
                1 + gameQueryService.countETBExtraTriggers(gameData, controllerId, landControllerId, enteringLand),
                null);
        // The trigger pipeline judged each ability's event condition (Invader Parasite's name match) and intervening
        // "if" ("if that player controls more lands than you", Archaeomancer's Map; "had another land enter", Tunnel
        // Ignus) against the entering land and its controller; the intervening "if" stays wrapped for resolution.
        return dispatch(new TriggerMatchContext(gameData, perm, controllerId, effects), watch, ctx);
    }

    /**
     * Landfall: "whenever a land you control enters", and "one or more lands". A printed land doesn't see its own
     * entry unless its trigger narrows the lands down ({@code narrowed}: Valakut, the Molten Pinnacle's "a
     * Mountain"); a nonland permanent that is a land only through a continuous effect (Scute Swarm under Ashaya)
     * does.
     */
    private boolean triggerAllyLandEnters(GameData gameData, Permanent perm, UUID landControllerId,
                                          List<CardEffect> effects, Entering entering, boolean oneOrMore,
                                          TriggerLimit limit, String actionKey, boolean narrowed) {
        Card enteringLand = entering.card();
        UUID enteringPermanentId = entering.permanentId();
        if (perm.getId().equals(enteringPermanentId) && enteringLand.hasType(CardType.LAND) && !narrowed) {
            return false;
        }
        if (gameQueryService.areOpponentPermanentETBTriggersSuppressed(gameData, landControllerId)) {
            return false;
        }
        int triggerCount = 1 + gameQueryService.countETBExtraTriggers(gameData, landControllerId, landControllerId,
                enteringLand);
        List<CardEffect> resolvedEffects = List.copyOf(effects);
        if (resolvedEffects.isEmpty()) {
            return false;
        }

        int permanentTriggerCount = triggerCount
                + gameQueryService.countAdditionalTriggeredAbilityTriggers(gameData, landControllerId, perm);
        // A modal ability's mode is chosen as it goes on the stack (CR 603.3c).
        ChooseOneEffect triggerTimeModal = resolvedEffects.size() == 1
                ? TriggeredModalEffect.choiceMadeAsTriggered(resolvedEffects.getFirst()) : null;
        if (triggerTimeModal != null) {
            for (int i = 0; i < permanentTriggerCount; i++) {
                gameData.handOffTriggerRecord(landfallEntry(perm, landControllerId, resolvedEffects, enteringLand,
                        enteringPermanentId, actionKey, oneOrMore));
                gameData.queueInteraction(new PermanentChoiceContext.TriggeredModalTrigger(
                        perm.getCard(), landControllerId, triggerTimeModal, perm.getId(),
                        false, false, null, null, enteringPermanentId));
                gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
            }
        } else if (resolvedEffects.stream()
                .anyMatch(effect -> effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD))) {
            for (int i = 0; i < permanentTriggerCount; i++) {
                gameData.handOffTriggerRecord(landfallEntry(perm, landControllerId, resolvedEffects, enteringLand,
                        enteringPermanentId, actionKey, oneOrMore));
                gameData.queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                        perm.getCard(), landControllerId, resolvedEffects, null, 0, 0, 0, null, false,
                        enteringPermanentId));
                gameLogService.append(gameData, GameLog.cardThen(perm.getCard(),
                        "'s landfall ability triggers — choose a graveyard target."));
            }
        } else if (resolvedEffects.stream()
                .anyMatch(effect -> effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                        || effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT))) {
            boolean needsPlayerTarget = resolvedEffects.stream()
                    .anyMatch(effect -> effect.targetSpec().admits(TargetPredicate.Kind.PLAYER));
            boolean needsPermanentTarget = resolvedEffects.stream()
                    .anyMatch(effect -> effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT));
            int targetGroupIndex = resolvedEffects.stream()
                    .mapToInt(perm.getCard()::getEffectTargetIndex)
                    .filter(index -> index >= 0 && index < perm.getCard().getTargetGroups().size())
                    .findFirst()
                    .orElse(-1);
            TargetFilter targetFilter = targetGroupIndex >= 0
                    ? perm.getCard().getTargetGroups().get(targetGroupIndex).getFilter()
                    : perm.getCard().getTargetFilter();
            boolean optionalTarget = targetGroupIndex >= 0
                    && perm.getCard().getTargetGroups().get(targetGroupIndex).getMinTargets() == 0
                    && perm.getCard().getTargetGroups().get(targetGroupIndex).getMaxTargets() == 1;
            for (int i = 0; i < permanentTriggerCount; i++) {
                queueTargetChoice(gameData, landfallEntry(perm, landControllerId, resolvedEffects, enteringLand,
                        enteringPermanentId, actionKey, oneOrMore),
                        new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                        perm.getCard(), landControllerId, resolvedEffects, needsPlayerTarget && !needsPermanentTarget,
                        targetFilter, 0, perm.getId(), null, optionalTarget, enteringPermanentId, null,
                        landControllerId));
                gameLogService.append(gameData, GameLog.cardThen(perm.getCard(),
                        "'s landfall ability triggers — choose a target."));
            }
        } else if (resolvedEffects.size() == 1 && resolvedEffects.getFirst() instanceof ChooseOneEffect chooseOne) {
            for (int i = 0; i < permanentTriggerCount; i++) {
                gameData.handOffTriggerRecord(landfallEntry(perm, landControllerId, resolvedEffects, enteringLand,
                        enteringPermanentId, actionKey, oneOrMore));
                gameData.queueInteraction(new PermanentChoiceContext.TriggeredModalTrigger(
                        perm.getCard(), landControllerId, chooseOne, perm.getId(),
                        false, false, null, null, enteringPermanentId));
                gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
            }
        } else {
            for (int i = 0; i < permanentTriggerCount; i++) {
                gameData.stack.add(landfallEntry(perm, landControllerId, resolvedEffects, enteringLand,
                        enteringPermanentId, actionKey, oneOrMore));
                gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
            }
        }
        return true;
    }

    /**
     * The entry of a landfall ability that chooses nothing: the land that entered as its triggering card and
     * permanent, under the player it entered under. It is also the record of a landfall ability that chooses a mode,
     * a target or a graveyard card as it goes on the stack ({@code GameData.handOffTriggerRecord}).
     */
    private static StackEntry landfallEntry(Permanent perm, UUID landControllerId, List<CardEffect> resolvedEffects,
                                            Card enteringLand, UUID enteringPermanentId, String actionKey,
                                            boolean oneOrMore) {
        StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, perm.getCard(), landControllerId,
                perm.getCard().getName() + "'s ability", new ArrayList<>(resolvedEffects), null,
                perm.getId());
        entry.setTriggeringCardId(enteringLand.getId());
        entry.setTriggeringPermanentId(enteringPermanentId);
        entry.setTriggeringPermanentControllerId(landControllerId);
        entry.setOnceEachTurnActionKey(actionKey);
        if (oneOrMore && enteringPermanentId != null) {
            entry.setTriggeringPermanentIds(List.of(enteringPermanentId));
            entry.rememberLastKnownPermanentCard(enteringPermanentId, enteringLand);
        }
        return entry;
    }

    /** Landfall from the graveyard (Reach of Branches). */
    private boolean triggerGraveyardLandEnters(GameData gameData, Card card, UUID landControllerId,
                                               List<CardEffect> effects, Entering entering) {
        return enqueueGraveyardEnterAbility(gameData, card, landControllerId, effects, entering.permanent());
    }

    /**
     * From the graveyard, "whenever a creature you control enters" (Unconventional Tactics) and "whenever a creature
     * enters".
     */
    private boolean triggerGraveyardCreatureEnters(GameData gameData, Card card, UUID controllerId,
                                                   List<CardEffect> effects, Entering entering) {
        return enqueueGraveyardEnterAbility(gameData, card, controllerId, effects, entering.permanent());
    }

    /**
     * Puts one triggered ability of {@code card}, a card in a graveyard, on the stack as one object of all its
     * {@code effects} (CR 603.3), a "you may" among them asked as it resolves (CR 603.5).
     *
     * @return the entry, or {@code null} for an ability with no effect left
     */
    private StackEntry enqueueGraveyardAbility(GameData gameData, Card card, UUID controllerId,
                                               List<CardEffect> effects) {
        if (effects.isEmpty()) {
            return null;
        }
        StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, card, controllerId,
                card.getName() + "'s ability", new ArrayList<>(effects));
        gameData.enqueueTrigger(entry);
        gameLogService.append(gameData, GameLog.abilityTriggers(card));
        return entry;
    }

    /**
     * Puts one enters ability of {@code card}, a card in a graveyard, on the stack as one object of all its
     * {@code effects} (CR 603.3), with what every such ability records, whatever its effects are: the stay in the
     * graveyard its source card is ("this card", a new object once it has left and returned, CR 400.7) and the
     * permanent that entered. "That creature" is the stack entry's triggering permanent, read as the ability resolves
     * (Sword of the Meek, Dragon Wings); an ability none of whose effects targets says so.
     *
     * @return whether it triggered
     */
    private boolean enqueueGraveyardEnterAbility(GameData gameData, Card card, UUID controllerId,
                                                 List<CardEffect> effects, Permanent enteringPermanent) {
        if (effects.isEmpty()) {
            return false;
        }
        StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, card, controllerId,
                card.getName() + "'s ability", new ArrayList<>(effects));
        entry.setSourceGraveyardEntryVersion(gameData.graveyardEntryVersion(card.getId()));
        if (enteringPermanent != null) {
            entry.setTriggeringPermanentId(enteringPermanent.getId());
            if (effects.stream().allMatch(effect -> effect.targetSpec() == TargetSpec.NONE)) {
                entry.setNonTargeting(true);
            }
        }
        gameData.enqueueTrigger(entry);
        gameLogService.append(gameData, GameLog.abilityTriggers(card));
        return true;
    }

    /** Whether one of {@code effects}, an ability's, declares a target of its own. */
    private static boolean declaresTarget(List<CardEffect> effects) {
        return effects.stream().anyMatch(effect -> effect.targetSpec().declaredTarget() != null);
    }

    /** Whether every effect of {@code effects} that declares a target admits no permanent: a player alone is offered. */
    private static boolean declaredTargetsArePlayers(List<CardEffect> effects) {
        return effects.stream().filter(effect -> effect.targetSpec().declaredTarget() != null)
                .noneMatch(effect -> effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT));
    }

    /** Whether one of {@code effects}, an ability's, targets a permanent or a player. */
    private static boolean targetsPermanentOrPlayer(List<CardEffect> effects) {
        return effects.stream().anyMatch(effect -> effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER));
    }

    /** From the graveyard, "whenever an artifact you control enters" and "an enchantment you control". */
    private boolean triggerGraveyardPermanentTypeEnters(GameData gameData, Card card, UUID controllerId,
                                                        List<CardEffect> effects, Entering entering) {
        return enqueueGraveyardEnterAbility(gameData, card, controllerId, effects, entering.permanent());
    }

    /** From the graveyard, "whenever a creature enters from your graveyard or you cast a creature from it". */
    private boolean triggerGraveyardCreatureEntersFromGraveyard(GameData gameData, Card card, UUID graveyardOwnerId,
                                                                List<CardEffect> effects, Entering entering) {
        Permanent enteringPermanent = entering.permanent();
        if (enteringPermanent == null) {
            return false;
        }
        boolean castFromGraveyard = enteringPermanent.isCast() && enteringPermanent.getCastFromZone() == Zone.GRAVEYARD;
        if (!graveyardOwnerId.equals(enteringPermanent.getEnteredFromGraveyardOwnerId())
                && !(castFromGraveyard && graveyardOwnerId.equals(entering.controllerId()))) {
            return false;
        }
        return enqueueGraveyardEnterAbility(gameData, card, graveyardOwnerId, effects, enteringPermanent);
    }

    /**
     * "Whenever this or another permanent enters from a graveyard" (River Kelpie) and "whenever a permanent you
     * control enters from exile": the ability as one stack entry of its effects, once for each time it triggers.
     */
    private boolean triggerPermanentEntersFromZone(GameData gameData, ZoneChangeWatch watch, Permanent perm,
                                                   UUID controllerId, List<CardEffect> effects, Entering entering) {
        Permanent enteringPermanent = entering.permanent();
        boolean fromExile = watch == ZoneChangeWatch.ON_PERMANENT_ENTERS_FROM_EXILE;
        if (enteringPermanent == null
                || (fromExile ? !enteringPermanent.isEnteredFromExile()
                : enteringPermanent.getEnteredFromGraveyardOwnerId() == null)
                || gameQueryService.areOpponentPermanentETBTriggersSuppressed(gameData, controllerId)) {
            return false;
        }
        int triggerCount = 1 + gameQueryService.countETBExtraTriggers(gameData, controllerId,
                entering.controllerId(), entering.card());
        for (int i = 0; i < (effects.isEmpty() ? 0 : triggerCount); i++) {
            gameData.stack.add(new StackEntry(StackEntryType.TRIGGERED_ABILITY, perm.getCard(), controllerId,
                    perm.getCard().getName() + "'s ability", new ArrayList<>(effects)));
            gameLogService.append(gameData, GameLog.cardTextCard(perm.getCard(), "'s ability triggers (",
                    entering.card(), fromExile ? " entered from exile)." : " entered from a graveyard)."));
        }
        return !effects.isEmpty();
    }

    /**
     * "When this creature enters from a graveyard" (Treacherous Pit-Dweller), one stack object of all its effects
     * (CR 603.3). An ability with a targeting effect picks its target as it goes on the stack (CR 603.3d): the
     * permanent was never cast, so no target was chosen then.
     */
    private boolean triggerSelfEntersFromGraveyard(GameData gameData, Card enteringCard, List<CardEffect> effects,
                                                   Entering entering) {
        Permanent enteringPermanent = entering.permanent();
        UUID enteringControllerId = entering.controllerId();
        if (enteringPermanent == null || enteringPermanent.getEnteredFromGraveyardOwnerId() == null
                || gameQueryService.areOpponentPermanentETBTriggersSuppressed(gameData, enteringControllerId)) {
            return false;
        }
        int triggerCount = 1 + gameQueryService.countETBExtraTriggers(gameData, enteringControllerId,
                enteringControllerId, enteringCard);
        // The pipeline has judged the ability's intervening "if" against the event (CR 603.4); an "if" of an effect is
        // judged as the ability resolves, so the ability goes on the stack whole (CR 603.3).
        List<CardEffect> abilityEffects = effects;
        boolean triggered = !abilityEffects.isEmpty();
        if (triggered) {
            boolean targets = targetsPermanentOrPlayer(abilityEffects);
            for (int i = 0; i < triggerCount; i++) {
                StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, enteringCard,
                        enteringControllerId, enteringCard.getName() + "'s ability",
                        new ArrayList<>(abilityEffects), null, enteringPermanent.getId());
                entry.setSourcePermanentSnapshot(new Permanent(enteringPermanent));
                gameData.handOffTriggerRecord(entry);
                if (targets) {
                    gameData.queueInteraction(new PermanentChoiceContext.ETBTokenTargetTrigger(
                            enteringCard, enteringControllerId, new ArrayList<>(abilityEffects),
                            enteringPermanent.getId(), enteringCard.getTargetFilter()));
                } else {
                    gameData.stack.add(entry);
                }
            }
            gameLogService.append(gameData, GameLog.cardThen(enteringCard,
                    "'s ability triggers (it entered from a graveyard)."));
        }
        if (gameData.hasPendingInteraction(PermanentChoiceContext.ETBTokenTargetTrigger.class)
                && !gameData.interaction.isAwaitingInput()) {
            etbTokenTargetService.processNextETBTokenTargetTrigger(gameData);
        }
        return triggered;
    }

    /**
     * "Whenever one or more tokens you control enter" (also "whenever you create one or more tokens") and "one or
     * more tokens an opponent controls": once for the batch's tokens, or once for each token for an ability that
     * triggers for each, or whose effect's resolution condition is judged per token.
     */
    private boolean triggerTokensEnter(GameData gameData, ZoneChangeWatch watch, TriggerCandidate candidate,
                                       List<CardEffect> effects, List<ZoneChangeEvent> events) {
        Permanent permanent = candidate.sourcePermanent();
        UUID controllerId = candidate.controllerId();
        UUID tokenControllerId = events.getFirst().controllerId();
        boolean opponent = watch == ZoneChangeWatch.ON_OPPONENT_TOKEN_ENTERS_BATTLEFIELD;
        // "Whenever you create one or more tokens" isn't caused by permanents entering, so what changes the abilities
        // permanents entering trigger (Elesh Norn, Mother of Machines) doesn't apply to it.
        boolean creation = candidate.ability().conditions().stream().anyMatch(condition ->
                condition.trigger() instanceof ZoneChangeTrigger trigger
                        && trigger.causes().contains(ZoneChangeCause.CREATE));
        if (!creation && gameQueryService.areOpponentPermanentETBTriggersSuppressed(gameData, controllerId)) {
            return false;
        }
        List<UUID> permanentIds = events.stream().map(event -> event.permanent().getId()).toList();
        int count = permanentIds.size();
        int extraTriggers = creation ? 0 : opponent
                ? gameQueryService.countETBExtraTriggersForAnyPermanent(gameData, controllerId)
                : gameQueryService.countETBExtraTriggers(gameData, controllerId, tokenControllerId,
                events.getFirst().permanent().getCard());
        boolean perToken = candidate.ability().options().multiplicity() == TriggerMultiplicity.EACH;
        TriggerLimit limit = candidate.ability().options().limit();
        boolean triggeredAny = false;
        // "Whenever you create one or more tokens for the first time each turn" (Akim, the Soaring Wind): only if this
        // event's tokens are all the tokens its controller created this turn.
        boolean firstTokenCreation = limit == TriggerLimit.FIRST_TIME_EACH_TURN;
        if (firstTokenCreation && gameData.tokensCreatedThisTurn.getOrDefault(controllerId, 0) > count) {
            return false;
        }
        if (effects.isEmpty()) {
            return false;
        }
        boolean oncePerTurn = limit == TriggerLimit.ONCE_EACH_TURN || limit == TriggerLimit.DO_ONLY_ONCE_EACH_TURN;
        String markOnAcceptance = TriggeredAbilityStackService.onceEachTurnActionKey(candidate.ability());
        boolean limitedTrigger = oncePerTurn && markOnAcceptance == null && !opponent;
        boolean perTokenOccurrences = perToken || effects.stream().anyMatch(CardEffect::hasAbilityResolutionCondition);
        int occurrences = perTokenOccurrences && !perToken ? count : 1;
        TriggerMatchContext match = new TriggerMatchContext(gameData, permanent, controllerId, effects,
                permanent.getCard(), null, markOnAcceptance);
        for (int occurrence = 0; occurrence < occurrences; occurrence++) {
            TriggerContext effectContext = perTokenOccurrences
                    ? new TriggerContext.TokensEnter(1, limitedTrigger ? 1 : 1 + extraTriggers,
                    tokenIdAt(permanentIds, occurrence))
                    : firstTokenCreation || limitedTrigger
                    ? new TriggerContext.TokensEnter(count, 1, permanentIds)
                    : new TriggerContext.TokensEnter(count, 1 + extraTriggers, permanentIds);
            // The ability, whole (CR 603.3), once for each time it triggers.
            boolean triggered = registry.dispatch(match, watch, effectContext);
            triggeredAny |= triggered;
            if (triggered && oncePerTurn && markOnAcceptance == null) {
                break;
            }
        }
        return triggeredAny;
    }

    /**
     * Puts {@code candidate}'s ability, which the trigger pipeline matched to {@code events} (permanents phasing,
     * transforming or being turned face up), on the stack the way {@code watch}'s old slot collector did, with
     * {@code effects} its effects (intervening "if"s already wrapped).
     *
     * @return whether it triggered
     */
    public boolean triggerPermanentChange(GameData gameData, ZoneChangeWatch watch, TriggerCandidate candidate,
                                          List<CardEffect> effects, List<PermanentChangeEvent> events) {
        PermanentChangeEvent event = events.getFirst();
        Permanent perm = candidate.sourceAsPermanent();
        UUID controllerId = candidate.controllerId();
        Permanent changed = event.change() == PermanentChange.PHASED_OUT ? event.permanent()
                : Objects.requireNonNullElse(gameQueryService.findPermanentById(gameData, event.permanent().getId()),
                event.permanent());
        boolean triggered = false;
        switch (watch) {
            case ON_SELF_PHASES_OUT, ON_SELF_PHASES_IN -> triggered = enqueuePhasingTriggers(gameData, changed,
                    controllerId, effects, watch == ZoneChangeWatch.ON_SELF_PHASES_IN);
            case ON_ANY_OTHER_PERMANENT_PHASES_OUT -> triggered = dispatchWatchEffects(gameData, candidate, watch,
                    effects, new TriggerContext.PermanentPhasedOut(
                            events.stream().map(PermanentChangeEvent::permanent).toList()));
            case ON_SELF_OR_ALLY_CREATURE_TURNS_FACE_UP, ON_SELF_OR_ANY_PERMANENT_TURNS_FACE_UP,
                 ON_SELF_OR_ALLY_PERMANENT_TURNS_FACE_UP -> {
                var ctx = new TriggerContext.PermanentTurnsFaceUp(changed, event.controllerId());
                triggered |= dispatch(new TriggerMatchContext(gameData, perm, controllerId, effects), watch, ctx);
            }
            case GRAVEYARD_ON_ALLY_PERMANENT_TURNS_FACE_UP -> {
                triggered = enqueueGraveyardAbility(gameData, candidate.sourceCard(), controllerId, effects) != null;
            }
            case ON_TURNED_FACE_UP -> triggered = turnFaceUpCopyService.putTurnedFaceUpAbilityOnStack(gameData,
                    changed, controllerId, effects, event.xValue());
            case ON_TRANSFORM_TO_BACK_FACE, ON_TRANSFORM_TO_FRONT_FACE -> triggered =
                    animationSupport.putTransformAbilityOnStack(gameData, changed, candidate.sourceCard(), effects);
            case ON_EQUIPPED_CREATURE_TRANSFORMS -> {
                Card equipmentCard = candidate.sourceCard();
                StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, equipmentCard, controllerId,
                        equipmentCard.getName() + "'s transform ability", new ArrayList<>(effects), null, perm.getId());
                entry.setSourcePermanentSnapshot(new Permanent(perm));
                gameData.stack.add(entry);
                gameLogService.append(gameData, GameLog.cardThen(equipmentCard, "'s transform ability triggers."));
                triggered = true;
            }
            case ON_ALLY_PERMANENT_TRANSFORMS -> triggered = dispatchWatchEffects(gameData, candidate, watch, effects,
                    new TriggerContext.PermanentTransforms(changed, changed.getCard(), event.controllerId()));
            default -> throw new IllegalArgumentException(watch + " is no permanent-change watch");
        }
        return triggered;
    }

    /**
     * The card a zone change moved, as a "whenever [an object] …" ability reads it: for a departure, the card it
     * last existed on the battlefield as (CR 603.10a), with the characteristics it left with.
     */
    private static Card lastKnownCard(ZoneChangeEvent event) {
        return event.permanent() != null ? event.permanent().getCard() : event.card();
    }

    /**
     * Dispatches a watched ability, its effects together (CR 603.3), to the watch's handler, with the trigger
     * condition read from the permanent's text-changed words for a printed ability (CR 612.1).
     */
    private boolean dispatchWatchEffects(GameData gameData, TriggerCandidate candidate, ZoneChangeWatch watch,
                                         List<CardEffect> effects, TriggerContext ctx) {
        Permanent perm = namedSourcePermanent(candidate);
        List<TextReplacement> globalWordChange = TextChangeTransformer.globalColorWordReplacements(gameData);
        String markOnAcceptance = TriggeredAbilityStackService.onceEachTurnActionKey(candidate.ability());
        List<CardEffect> changed = effects.stream().map(effect -> TextChangeTransformer.transform(effect,
                perm.getTextReplacements(), globalWordChange)).toList();
        return !changed.isEmpty()
                && dispatchSlotEffect(gameData, perm, candidate.controllerId(), watch, ctx, changed, markOnAcceptance);
    }

    /**
     * "Whenever you sacrifice a [filter] permanent": the ability through its handler, whole. {@code markOnAcceptance}
     * says whether taking the action uses up "do this only once each turn" (CR 603.2h).
     */
    private boolean triggerAllyPermanentSacrificed(GameData gameData, Permanent perm, UUID controllerId,
                                                   List<CardEffect> effects, String markOnAcceptance,
                                                   ZoneChangeEvent event) {
        var ctx = new TriggerContext.AllySacrificed(event.controllerId(), lastKnownCard(event), event.permanent());
        var match = new TriggerMatchContext(gameData, perm, controllerId, effects, perm.getCard(), null,
                markOnAcceptance);
        return dispatch(match, ZoneChangeWatch.ON_ALLY_PERMANENT_SACRIFICED, ctx);
    }

    /** "When you sacrifice a [filter] permanent", from the graveyard. */
    private boolean triggerGraveyardPermanentSacrificed(GameData gameData, Card card, UUID controllerId,
                                                        List<CardEffect> effects) {
        boolean triggered = enqueueGraveyardAbility(gameData, card, controllerId, effects) != null;
        if (triggered) {
            log.info("Game {} - {} graveyard permanent-sacrifice trigger queued", gameData.id, card.getName());
        }
        return triggered;
    }

    // ── Becomes-target-of-spell triggers ───────────────────────────────

    public void checkBecomesTargetOfSpellTriggers(GameData gameData) {
        if (gameData.stack.isEmpty()) return;
        StackEntry spellEntry = null;
        for (int i = gameData.stack.size() - 1; i >= 0; i--) {
            StackEntry candidate = gameData.stack.get(i);
            if (candidate.getEntryType() != StackEntryType.TRIGGERED_ABILITY
                    && candidate.getEntryType() != StackEntryType.ACTIVATED_ABILITY) {
                spellEntry = candidate;
                break;
            }
        }
        if (spellEntry == null) return;
        chooseTargetAtRandomIfRequired(gameData, spellEntry);
        checkBecomesTargetOfSpellTriggers(gameData, spellEntry);
        checkTargetChoiceTriggers(gameData, spellEntry);
    }

    public void checkBecomesTargetOfSpellTriggers(GameData gameData, StackEntry spellEntry) {
        gameQueryService.withQueryScope(gameData, () -> {
            collectBecomesTargetOfSpellTriggers(gameData, spellEntry);
            return null;
        });

        if (gameData.hasPendingInteraction(PermanentChoiceContext.SpellTargetTriggerAnyTarget.class) && !gameData.interaction.isAwaitingInput()) {
            processNextSpellTargetTrigger(gameData);
        }
        if (gameData.hasPendingInteraction(PermanentChoiceContext.ETBTokenMultiTargetTrigger.class)
                && !gameData.interaction.isAwaitingInput()) {
            processNextETBTokenMultiTargetTrigger(gameData);
        }
        if (gameData.hasPendingInteraction(PermanentChoiceContext.ETBTokenTargetTrigger.class)
                && !gameData.interaction.isAwaitingInput()) {
            etbTokenTargetService.processNextETBTokenTargetTrigger(gameData);
        }
        beginStackDoorChoices(gameData);
    }

    /**
     * Begins the next choice the stack door queued for an ability that triggered as a spell was cast or an ability
     * activated, or as either targeted (CR 603.3c, 603.3d): its mode, its targets in one choice or group by group, its
     * graveyard cards. The answer's handler begins the one after it.
     */
    private void beginStackDoorChoices(GameData gameData) {
        if (gameData.interaction.isAwaitingInput()) {
            return;
        }
        if (gameData.hasPendingInteraction(PermanentChoiceContext.TriggeredModalTrigger.class)) {
            processNextTriggeredModalTrigger(gameData);
            if (gameData.interaction.isAwaitingInput()) {
                return;
            }
        }
        if (gameData.hasPendingInteraction(PermanentChoiceContext.AttackTriggerTarget.class)) {
            processNextAttackTriggerTarget(gameData);
            if (gameData.interaction.isAwaitingInput()) {
                return;
            }
        }
        if (gameData.hasPendingInteraction(PermanentChoiceContext.ETBTokenMultiTargetTrigger.class)) {
            processNextETBTokenMultiTargetTrigger(gameData);
            if (gameData.interaction.isAwaitingInput()) {
                return;
            }
        }
        if (gameData.hasPendingInteraction(PermanentChoiceContext.SpellGraveyardTargetTrigger.class)) {
            processNextSpellGraveyardTargetTrigger(gameData);
        }
    }

    /**
     * Detects every becomes-the-target trigger of one spell's targeting event. All of them trigger
     * on the same event (CR 603.2) and only go on the stack afterwards (CR 603.3), so the caller runs
     * this under one query scope: the collectors' per-permanent layered queries share a single board
     * instead of each re-fingerprinting it, which would cost targets × collectors × battlefield size
     * board hashes. What the collectors write — triggered abilities on the stack, log lines, queued
     * choices and may abilities — is not read by the layered board or the static-bonus assembly.
     */
    private void collectBecomesTargetOfSpellTriggers(GameData gameData, StackEntry spellEntry) {
        if (spellEntry.getTargetZone() == Zone.STACK) {
            if (spellEntry.getTargetId() != null) {
                StackEntry targetedEntry = findStackEntryByCardId(gameData, spellEntry.getTargetId());
                collectAllyCreatureOrCreatureSpellBecomesTargetOfOpponentTriggers(gameData, targetedEntry, spellEntry);
            } else {
                for (UUID targetId : spellEntry.getTargetIds()) {
                    StackEntry targetedEntry = findStackEntryByCardId(gameData, targetId);
                    collectAllyCreatureOrCreatureSpellBecomesTargetOfOpponentTriggers(gameData, targetedEntry, spellEntry);
                }
            }
        }

        List<UUID> targetIds = new ArrayList<>();
        if (spellEntry.getTargetId() != null
                && spellEntry.getTargetZone() == null) {
            targetIds.add(spellEntry.getTargetId());
        }
        if (spellEntry.getTargetIds() != null) {
            targetIds.addAll(spellEntry.getTargetIds());
        }

        Set<UUID> targetedPlayers = new HashSet<>();
        Set<UUID> targetsWithAllyTrigger = new HashSet<>();
        Set<List<Object>> collectedAllyTargetTriggers = new HashSet<>();

        for (UUID targetId : new java.util.LinkedHashSet<>(targetIds)) {
            if (gameData.playerIds.contains(targetId)) {
                if (targetsWithAllyTrigger.add(targetId)) {
                    collectAllyPermanentOrPlayerBecomesTargetOfOpponentTriggers(
                            gameData, targetId, null, spellEntry, collectedAllyTargetTriggers);
                }
                if (targetedPlayers.add(targetId)) {
                    collectControllerBecomesTargetOfSpellTriggers(gameData, targetId, spellEntry);
                    collectControllerBecomesTargetOfOpponentTriggers(gameData, targetId, spellEntry);
                }
                continue;
            }

            Permanent targetPermanent = gameQueryService.findPermanentById(gameData, targetId);
            if (targetPermanent == null) continue;

            UUID controllerId = gameQueryService.findPermanentController(gameData, targetPermanent.getId());
            if (controllerId == null) continue;

            if (targetsWithAllyTrigger.add(targetId)) {
                collectAllyPermanentOrPlayerBecomesTargetOfOpponentTriggers(
                                gameData, controllerId, targetPermanent, spellEntry,
                                collectedAllyTargetTriggers);
            }
            collectAllyPermanentBecomesTargetOfOpponentTriggers(
                            gameData, controllerId, targetPermanent, spellEntry);
            collectAnotherAllyPermanentBecomesTargetOfOpponentTriggers(
                            gameData, targetPermanent, controllerId, spellEntry);
            collectAllyCreatureBecomesTargetOfOpponentTriggers(
                            gameData, targetPermanent, controllerId, spellEntry);
            collectAllyCreatureBecomesTargetOfOpponentSpellTriggers(
                            gameData, targetPermanent, controllerId, spellEntry);
            collectAllyCreatureOrCreatureSpellBecomesTargetOfOpponentTriggers(
                            gameData, targetPermanent, controllerId, spellEntry);
            collectAnyCreatureBecomesTargetTriggers(gameData, targetPermanent, spellEntry);
            collectOpponentCreatureBecomesTargetOfYourSpellTriggers(gameData, targetPermanent, controllerId, spellEntry);
            collectAnotherAllyCreatureBecomesTargetOfSpellOrAbilityTriggers(
                            gameData, targetPermanent, controllerId, spellEntry);
            collectAllyCreatureBecomesTargetOfSpellTriggers(
                            gameData, targetPermanent, controllerId, spellEntry);
            collectAllyCreatureBecomesTargetOfInstantOrSorceryTriggers(
                            gameData, targetPermanent, controllerId, spellEntry);
            // Check the targeted permanent itself for "when this becomes the target" triggers.
            // Attached permanents (auras/equipment) use the loop below instead — their triggers
            // monitor the enchanted/equipped creature, not themselves (Spectral Prison is not
            // sacrificed when a spell targets the Aura rather than the enchanted creature).
            if (targetPermanent.isAttached() && (targetPermanent.getCard().getKeywords().contains(Keyword.WARD)
                    || hasGrantedCounterUnlessTrigger(gameData, targetPermanent))) {
                collectBecomesTargetOfOpponentCounterTriggers(
                                gameData, targetPermanent, controllerId, spellEntry);
            }
            // A bestowed permanent (Crystalline Nautilus) is still "this permanent" while attached as an Aura, so its
            // own printed becomes-target abilities apply to it, unlike an ordinary Aura's (see the loop below).
            if (controllerId != null && (!targetPermanent.isAttached() || targetPermanent.isBestow())) {
                collectBecomesTargetOfSpellOrAbilityTriggers(
                                gameData, targetPermanent, controllerId, spellEntry);
            }
            if (!targetPermanent.isAttached()) {
                collectBecomesTargetTriggers(
                                gameData, targetPermanent, controllerId, targetPermanent, spellEntry);
                collectBecomesTargetOfAuraSpellTriggers(
                                gameData, targetPermanent, controllerId, spellEntry);
                collectBecomesTargetOfOpponentCounterTriggers(
                                gameData, targetPermanent, controllerId, spellEntry);
                collectBecomesTargetOfOpponentSpellOnlyTriggers(
                                gameData, targetPermanent, controllerId, spellEntry);
                collectBecomesTargetOfOpponentSpellOrAbilityNonCounterTriggers(
                                gameData, targetPermanent, controllerId, spellEntry);
            }

            for (UUID playerId : gameData.orderedPlayerIds) {
                List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
                if (battlefield == null) continue;
                for (Permanent attached : battlefield) {
                    if (attached.isAttached()
                            && attached.getAttachedTo().equals(targetPermanent.getId())) {
                        // CR 603.3a: the triggered ability is controlled by the controller of the
                        // permanent that has it (the aura/equipment), not the enchanted creature —
                        // the two differ when an Aura like Spectral Prison enchants an opponent's creature.
                        // A bestowed permanent's printed becomes-target abilities refer to itself, so they do not
                        // fire when the creature it enchants is targeted; only its granted ability does (on that creature).
                        if (!attached.isBestow()) {
                            collectBecomesTargetOfSpellOrAbilityTriggers(
                                            gameData, attached, playerId, spellEntry);
                        }
                        if (playerId.equals(controllerId)) {
                            collectBecomesTargetTriggers(
                                            gameData, attached, playerId, targetPermanent, spellEntry);
                            collectBecomesTargetOfAuraSpellTriggers(
                                            gameData, attached, playerId, spellEntry);
                            collectBecomesTargetOfOpponentCounterTriggers(
                                            gameData, attached, playerId, spellEntry);
                        } else {
                            collectBecomesTargetTriggers(gameData, attached, playerId, targetPermanent, spellEntry);
                            collectBecomesTargetOfAuraSpellTriggers(gameData, attached, playerId, spellEntry);
                            collectBecomesTargetOfOpponentCounterTriggers(gameData, attached, playerId, spellEntry);
                        }
                    }
                }
            }
        }
    }

    private StackEntry findStackEntryByCardId(GameData gameData, UUID cardId) {
        return gameData.stack.stream()
                .filter(entry -> entry.getCard() != null && entry.getCard().getId().equals(cardId))
                .findFirst()
                .orElse(null);
    }

    /** Drains pending multi-target trigger choices (ETB token copies and ON_SELF_CAST multi-target). */
    public void processNextETBTokenMultiTargetTrigger(GameData gameData) {
        etbTokenTargetService.processNextETBTokenMultiTargetTrigger(gameData);
    }

    public boolean needsSlotBySlotTargetSelection(Card card) {
        return etbTokenTargetService.needsSlotBySlotTargetSelection(card);
    }

    /**
     * Checks becomes-target triggers for activated/triggered abilities that target permanents.
     * Must be called after an ability is pushed onto the stack with a target permanent.
     */
    public void checkBecomesTargetOfAbilityTriggers(GameData gameData) {
        if (gameData.stack.isEmpty()) return;
        StackEntry abilityEntry = gameData.stack.getLast();
        chooseTargetAtRandomIfRequired(gameData, abilityEntry);
        checkBecomesTargetOfAbilityTriggers(gameData, abilityEntry);
        checkTargetChoiceTriggers(gameData, abilityEntry);
    }

    /**
     * Checks becomes-target triggers for an explicitly supplied activated or triggered ability.
     * This overload is used when the ability is not the current top stack entry, such as while
     * Psychic Battle is resolving.
     */
    public void checkBecomesTargetOfAbilityTriggers(GameData gameData, StackEntry abilityEntry) {
        if (abilityEntry == null) return;
        gameQueryService.withQueryScope(gameData, () -> {
            collectBecomesTargetOfAbilityTriggers(gameData, abilityEntry);
            return null;
        });

        if (gameData.hasPendingInteraction(PermanentChoiceContext.SpellTargetTriggerAnyTarget.class)
                && !gameData.interaction.isAwaitingInput()) {
            processNextSpellTargetTrigger(gameData);
        }
        beginStackDoorChoices(gameData);
    }

    /**
     * The ability counterpart of {@link #collectBecomesTargetOfSpellTriggers}, run under one query
     * scope for the same reason.
     */
    private void collectBecomesTargetOfAbilityTriggers(GameData gameData, StackEntry abilityEntry) {
        if (abilityEntry.getTargetZone() == Zone.STACK && !abilityEntry.isNonTargeting()) {
            if (abilityEntry.getTargetId() != null) {
                StackEntry targetedEntry = findStackEntryByCardId(gameData, abilityEntry.getTargetId());
                collectAllyCreatureOrCreatureSpellBecomesTargetOfOpponentTriggers(gameData, targetedEntry, abilityEntry);
            } else {
                for (UUID targetId : abilityEntry.getTargetIds()) {
                    StackEntry targetedEntry = findStackEntryByCardId(gameData, targetId);
                    collectAllyCreatureOrCreatureSpellBecomesTargetOfOpponentTriggers(gameData, targetedEntry, abilityEntry);
                }
            }
        }

        List<UUID> targetIds = new ArrayList<>();
        if (abilityEntry.getTargetId() != null
                && (abilityEntry.getTargetZone() == null || abilityEntry.getTargetZone() == Zone.BATTLEFIELD)
                && !abilityEntry.isNonTargeting()) {
            targetIds.add(abilityEntry.getTargetId());
        }
        if (abilityEntry.getTargetIds() != null) {
            targetIds.addAll(abilityEntry.getTargetIds());
        }

        if (!abilityEntry.isNonTargeting()) {
            for (UUID permanentId : abilityEntry.getMixedZoneTargetPermanentIds().values()) {
                if (!targetIds.contains(permanentId)) targetIds.add(permanentId);
            }
        }

        collectAllyCreatureBecomesTargetOfActivatedAbilityTriggers(gameData, abilityEntry, targetIds);

        Set<UUID> targetedPlayers = new HashSet<>();
        Set<UUID> targetsWithAllyTrigger = new HashSet<>();
        Set<List<Object>> collectedAllyTargetTriggers = new HashSet<>();

        for (UUID targetId : targetIds) {
            if (gameData.playerIds.contains(targetId)) {
                if (targetsWithAllyTrigger.add(targetId)) {
                    collectAllyPermanentOrPlayerBecomesTargetOfOpponentTriggers(
                            gameData, targetId, null, abilityEntry, collectedAllyTargetTriggers);
                }
                if (targetedPlayers.add(targetId)) {
                    collectControllerBecomesTargetOfOpponentTriggers(gameData, targetId, abilityEntry);
                }
                continue;
            }

            Permanent targetPermanent = gameQueryService.findPermanentById(gameData, targetId);
            if (targetPermanent == null) continue;

            UUID controllerId = gameQueryService.findPermanentController(gameData, targetPermanent.getId());

            if (controllerId != null) {
                if (targetsWithAllyTrigger.add(targetId)) {
                    collectAllyPermanentOrPlayerBecomesTargetOfOpponentTriggers(
                                    gameData, controllerId, targetPermanent, abilityEntry,
                                    collectedAllyTargetTriggers);
                }
                collectAllyPermanentBecomesTargetOfOpponentTriggers(
                                gameData, controllerId, targetPermanent, abilityEntry);
                collectAnotherAllyPermanentBecomesTargetOfOpponentTriggers(
                                gameData, targetPermanent, controllerId, abilityEntry);
            }

            // Check the targeted permanent itself for "when this becomes the target" triggers.
            // Attached permanents (auras/equipment) use the loop below instead.
            if (targetPermanent.isAttached() && controllerId != null
                    && targetPermanent.getCard().getKeywords().contains(Keyword.WARD)) {
                collectBecomesTargetOfOpponentCounterTriggers(
                                gameData, targetPermanent, controllerId, abilityEntry);
            }
            if (controllerId != null && (!targetPermanent.isAttached() || targetPermanent.isBestow())) {
                collectBecomesTargetOfSpellOrAbilityTriggers(
                                gameData, targetPermanent, controllerId, abilityEntry);
            }
            if (!targetPermanent.isAttached() && controllerId != null) {
                collectBecomesTargetOfOpponentCounterTriggers(
                                gameData, targetPermanent, controllerId, abilityEntry);
                collectBecomesTargetOfOpponentSpellOrAbilityNonCounterTriggers(
                                gameData, targetPermanent, controllerId, abilityEntry);
            }

            // Check for "whenever a creature you control becomes the target of opponent's spell or ability"
            if (controllerId != null) {
                collectAllyCreatureBecomesTargetOfOpponentTriggers(
                                gameData, targetPermanent, controllerId, abilityEntry);
                collectAllyCreatureOrCreatureSpellBecomesTargetOfOpponentTriggers(
                                gameData, targetPermanent, controllerId, abilityEntry);
                collectOpponentCreatureBecomesTargetOfYourSpellTriggers(gameData, targetPermanent, controllerId, abilityEntry);
                collectAnotherAllyCreatureBecomesTargetOfSpellOrAbilityTriggers(
                                gameData, targetPermanent, controllerId, abilityEntry);
                collectAllyCreatureBecomesTargetOfBackupAbilityTriggers(
                                gameData, targetPermanent, controllerId, abilityEntry);
            }

            collectAnyCreatureBecomesTargetTriggers(gameData, targetPermanent, abilityEntry);

            for (UUID playerId : gameData.orderedPlayerIds) {
                List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
                if (battlefield == null) continue;
                for (Permanent attached : battlefield) {
                    if (attached.isAttached()
                            && attached.getAttachedTo().equals(targetPermanent.getId())) {
                        // CR 603.3a: triggered ability controlled by the aura/equipment's controller. A bestowed
                        // permanent's printed abilities refer to itself, so they do not fire for the creature it enchants.
                        if (!attached.isBestow()) {
                            collectBecomesTargetOfSpellOrAbilityTriggers(
                                            gameData, attached, playerId, abilityEntry);
                        }
                        if (playerId.equals(controllerId)) {
                            collectBecomesTargetOfOpponentCounterTriggers(
                                            gameData, attached, playerId, abilityEntry);
                        } else {
                            collectBecomesTargetOfOpponentCounterTriggers(gameData, attached, playerId, abilityEntry);
                        }
                    }
                }
            }
        }
    }

    /**
     * Collects "whenever one or more creatures you control become the target of an activated ability" (Professor Hojo)
     * once for each ability that targets one or more of a player's creatures, however many it targets: each watching
     * ability goes to the stack door once, its "this ability triggers only once each turn" judged there.
     */
    private void collectAllyCreatureBecomesTargetOfActivatedAbilityTriggers(
            GameData gameData, StackEntry abilityEntry, List<UUID> targetIds) {
        if (abilityEntry.getEntryType() != StackEntryType.ACTIVATED_ABILITY
                || abilityEntry.isCopy()) {
            return;
        }

        Set<UUID> targetedCreatureControllers = new HashSet<>();
        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null || !gameQueryService.isCreature(gameData, target)) continue;
            UUID controllerId = gameQueryService.findPermanentController(gameData, targetId);
            if (controllerId != null) {
                targetedCreatureControllers.add(controllerId);
            }
        }

        for (UUID controllerId : targetedCreatureControllers) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
            if (battlefield == null) continue;
            for (Permanent source : List.copyOf(battlefield)) {
                if (source.isCloaked() || source.isLosesAllAbilitiesUntilEndOfTurn()) continue;
                TargetWatch watch = TargetWatch.ON_ALLY_CREATURE_BECOMES_TARGET_OF_ACTIVATED_ABILITY;
                for (TargetEffect read : targetReads(TargetAbilityReader.printed(source.getCard(), watch), watch)) {
                    TargetAbilityReader.requireNoFilter(read, watch.trigger());
                    TargetAbilityReader.requireWatchedEvent(read, watch.trigger());
                    putTargetAbility(gameData, read.ability(), source, controllerId, "'s ability", abilityEntry,
                            null, controllerId, NO_EXTRA_RECORD);
                }
            }
        }
    }

    /**
     * Checks all permanents controlled by the targeted player for abilities that watch that player or
     * one of their permanents becoming the target of an opponent's spell or ability, each handed to the stack door
     * whole with its filter judged once. "You or a permanent you control becomes the target" triggers once for each
     * such target (CR 603.2c; Rayne, Academy Chancellor's, Leovold's and Unsettled Mariner's rulings); "you and/or at
     * least one permanent you control becomes the target" ({@code TriggerMultiplicity.ONE_OR_MORE}, Leyline of
     * Combustion) once for the spell or ability, however many of them it targets: {@code collectedTriggers} holds the
     * abilities that already triggered for it. The record names the spell or ability's controller as "that player".
     */
    private void collectAllyPermanentOrPlayerBecomesTargetOfOpponentTriggers(
            GameData gameData, UUID targetControllerId, Permanent targetPermanent,
            StackEntry triggeringEntry, Set<List<Object>> collectedTriggers) {
        if (targetControllerId.equals(triggeringEntry.getControllerId())) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(targetControllerId);
        if (battlefield == null) return;

        UUID spellControllerId = triggeringEntry.getControllerId();
        for (Permanent source : List.copyOf(battlefield)) {
            if (gameQueryService.hasLostPrintedAbilities(gameData, source)) continue;
            TargetWatch watch = TargetWatch.ON_ALLY_PERMANENT_OR_PLAYER_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY;
            List<TriggeredAbility> abilities = new ArrayList<>(TargetAbilityReader.printed(source.getCard(), watch));
            abilities.addAll(grantedTargetAbilities(gameData, source, watch));
            List<TargetEffect> reads = targetReads(abilities, watch);
            for (int abilityIndex = 0; abilityIndex < reads.size(); abilityIndex++) {
                TargetEffect read = reads.get(abilityIndex);
                TargetAbilityReader.requireWatchedEvent(read, watch.trigger());
                TargetAbilityReader.requireNoLimit(read, watch.trigger());
                // The filter on the permanent (Fey Steed's "creature or planeswalker"), which a targeted player fails.
                if (!targetFilterMatches(gameData, source, targetControllerId, targetPermanent, read)) continue;
                if (read.oneOrMore() && !collectedTriggers.add(List.of(source.getId(), abilityIndex))) continue;
                putTargetAbility(gameData, read.ability(), source, targetControllerId, "'s triggered ability",
                        triggeringEntry, targetPermanent, targetControllerId, entry -> {
                            entry.setTargetId(spellControllerId);
                            entry.setSourcePermanentSnapshot(new Permanent(source));
                        });
            }
        }
    }

    /**
     * The first read of each becomes-the-target ability among {@code abilities} that {@code watch} reads, in order: the
     * ability with the trigger condition it is read by (its filter on the permanent, narrower event, source and limit),
     * which its collector judges before it hands the ability to the stack door.
     */
    private static List<TargetEffect> targetReads(List<TriggeredAbility> abilities, TargetWatch watch) {
        return TargetAbilityReader.readByAbility(abilities, watch.trigger()).stream().map(List::getFirst).toList();
    }

    /**
     * The abilities among {@code abilities} that {@code watch} reads, for a collector that judges no filter, narrower
     * event or limit: none of the abilities it reads is written with one, and this throws on one.
     */
    private static List<TriggeredAbility> unjudgedTargetAbilities(List<TriggeredAbility> abilities, TargetWatch watch) {
        List<TriggeredAbility> read = new ArrayList<>();
        for (TargetEffect first : targetReads(abilities, watch)) {
            TargetAbilityReader.requireNothingToJudge(first, watch.trigger());
            read.add(first.ability());
        }
        return read;
    }

    /**
     * The abilities {@code watch} reads of {@code source} for a collector that judges no filter, narrower event or
     * limit: its printed ones when {@code printed}, then those other permanents' static abilities grant it.
     */
    private List<TriggeredAbility> targetAbilities(GameData gameData, Permanent source, TargetWatch watch,
                                                   boolean printed) {
        List<TriggeredAbility> abilities = new ArrayList<>();
        if (printed) {
            abilities.addAll(TargetAbilityReader.printed(source.getCard(), watch));
        }
        abilities.addAll(grantedTargetAbilities(gameData, source, watch));
        return unjudgedTargetAbilities(abilities, watch);
    }

    /**
     * Hands {@code ability}, a becomes-the-target ability of {@code source} controlled by {@code controllerId}, to the
     * stack door for {@code targetingEntry}, the spell or ability that targeted {@code target} (a permanent
     * {@code targetControllerId} controls, or {@code null} for a player). Its event condition is judged now from its
     * source (CR 603.2); the door judges its intervening "if" (CR 603.4) and limit, counts the additional times it
     * triggers (Valiant Emberkin's, for a creature you control becoming the target), chooses its targets as it goes on
     * the stack (CR 603.3d) and aims an effect that acts on "that spell or ability" at it. The record names the spell or
     * ability as the triggering spell and its controller as the triggering player, whatever the effects are, and
     * {@code extra} adds what the collector's event names besides.
     *
     * @return whether it triggered
     */
    private boolean putTargetAbility(GameData gameData, TriggeredAbility ability, Permanent source, UUID controllerId,
                                     String description, StackEntry targetingEntry, Permanent target,
                                     UUID targetControllerId, TriggerRecord extra) {
        ConditionContext context = ConditionContext.forPermanent(source, controllerId)
                .withTriggeringSpell(targetingEntry);
        if (!conditionEvaluationService.eventConditionMet(gameData, ability.options(), context)) {
            return false;
        }
        TriggerRecord record = entry -> {
            entry.setNonTargeting(true);
            recordTargetingObject(entry, targetingEntry);
            extra.stamp(entry);
        };
        boolean triggered = triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(ability,
                source.getCard(), source, controllerId, source.getCard().getName() + description, context, record)
                .withEvent(new TriggerContext.BecomesTarget(target, targetControllerId)));
        if (triggered) {
            gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s triggered ability triggers."));
        }
        return triggered;
    }

    private void collectAnotherAllyPermanentBecomesTargetOfOpponentTriggers(
            GameData gameData, Permanent targetPermanent, UUID targetControllerId, StackEntry triggeringEntry) {
        if (targetControllerId.equals(triggeringEntry.getControllerId())) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(targetControllerId);
        if (battlefield == null) return;

        for (Permanent source : List.copyOf(battlefield)) {
            if (source.getId().equals(targetPermanent.getId())) continue;

            TargetWatch watch = TargetWatch.ON_ANOTHER_ALLY_PERMANENT_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY;
            for (TriggeredAbility ability : targetAbilities(gameData, source, watch, true)) {
                putTargetAbility(gameData, ability, source, targetControllerId, "'s triggered ability",
                        triggeringEntry, targetPermanent, targetControllerId, entry -> {
                            entry.setSourcePermanentSnapshot(new Permanent(source));
                            entry.setTriggeringPermanentId(targetPermanent.getId());
                            entry.setTriggeringPermanentControllerId(targetControllerId);
                        });
            }
        }
    }

    private void collectAllyPermanentBecomesTargetOfOpponentTriggers(
            GameData gameData, UUID targetControllerId, Permanent targetPermanent,
            StackEntry triggeringEntry) {
        if (targetControllerId.equals(triggeringEntry.getControllerId())) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(targetControllerId);
        if (battlefield == null) return;

        for (Permanent source : List.copyOf(battlefield)) {
            TargetWatch watch = TargetWatch.ON_ALLY_PERMANENT_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY;
            for (TriggeredAbility ability : targetAbilities(gameData, source, watch, true)) {
                putTargetAbility(gameData, ability, source, targetControllerId, "'s triggered ability",
                        triggeringEntry, targetPermanent, targetControllerId,
                        entry -> entry.setSourcePermanentSnapshot(new Permanent(source)));
            }
        }
    }

    /**
     * Records the event of a becomes-the-target ability on {@code entry}, whatever its effects are: the spell or ability
     * that targeted as its triggering spell (as it was then, and as it last existed on the stack once it has left,
     * CR 608.2h) and that object's controller as its triggering player.
     */
    private static void recordTargetingObject(StackEntry entry, StackEntry targetingEntry) {
        entry.setTriggeringCardId(targetingEntry.getTargetableId());
        entry.setTriggeringSpell(targetingEntry);
        entry.setTriggeringPlayerId(targetingEntry.getControllerId());
    }

    /**
     * Whether {@code ability} acts on "that spell or ability": one of its effects acts only on a spell or ability on the
     * stack ("counter it unless that player pays", ward). The ability goes on the stack whole either way (CR 603.3).
     */
    private static boolean actsOnASpell(TriggeredAbility ability) {
        return ability.effects().stream().map(EffectRegistration::effect)
                .anyMatch(TriggerCollectionService::actsOnASpell);
    }

    /** Whether {@code effect} acts only on a spell or ability on the stack ("counter that spell or ability"). */
    private static boolean actsOnASpell(CardEffect effect) {
        TargetSpec spec = effect.targetSpec();
        return spec.admits(TargetPredicate.Kind.SPELL) && !spec.admits(TargetPredicate.Kind.PERMANENT)
                && !spec.admits(TargetPredicate.Kind.PLAYER);
    }

    /**
     * High Troller: as a spell or ability with a single target is put on the stack, its target is
     * chosen uniformly at random from its legal targets instead of by its controller. The chosen
     * target may be the one originally announced.
     */
    private void chooseTargetAtRandomIfRequired(GameData gameData, StackEntry entry) {
        if (!entry.isSingleTarget() || entry.isNonTargeting()) {
            return;
        }
        Permanent[] randomizer = new Permanent[1];
        gameData.forEachPermanent((controllerId, permanent) -> {
            if (randomizer[0] == null
                    && !gameQueryService.hasLostAllAbilities(gameData, permanent)
                    && permanent.getCard().getStaticEffects().stream()
                    .anyMatch(ChooseTargetsAtRandomEffect.class::isInstance)) {
                randomizer[0] = permanent;
            }
        });
        if (randomizer[0] == null) {
            return;
        }
        UUID currentTarget = entry.getTargetId() != null
                ? entry.getTargetId()
                : entry.getTargetIds().isEmpty() ? null : entry.getTargetIds().getFirst();
        if (currentTarget == null) {
            return;
        }
        List<UUID> legalTargets = targetRedirectionSupport.collectValidTargetsIncludingCurrent(gameData, entry);
        if (legalTargets.isEmpty()) {
            return;
        }
        UUID selectedTarget = legalTargets.get(
                java.util.concurrent.ThreadLocalRandom.current().nextInt(legalTargets.size()));
        if (selectedTarget.equals(currentTarget)) {
            return;
        }
        entry.replaceTargetReferences(currentTarget, selectedTarget);
        gameLogService.append(gameData, GameLog.cardTextCard(
                randomizer[0].getCard(), " chooses the target of ", entry.getCard(), " at random."));
    }

    /**
     * "Whenever a player chooses one or more targets" (Grip of Chaos, Psychic Battle) and "whenever you or a permanent
     * becomes the target of an ability you control" (Loki, God of Mischief): each ability goes to the stack door whole,
     * its intervening "if" ("if it has a single target") judged there on the spell or ability whose targets were
     * chosen, its record naming that object as its triggering spell.
     */
    public void checkTargetChoiceTriggers(GameData gameData, StackEntry targetEntry) {
        if (targetEntry == null || !targetEntry.hasAnyTarget() || targetEntry.isNonTargeting()) {
            return;
        }

        TargetWatch watch = TargetWatch.ON_ANY_PLAYER_CHOOSES_TARGETS;
        gameData.forEachPermanent((controllerId, permanent) -> {
            if (gameQueryService.hasLostAllAbilities(gameData, permanent)) {
                return;
            }
            for (TargetEffect read : targetReads(TargetAbilityReader.printed(permanent.getCard(), watch), watch)) {
                TargetAbilityReader.requireNoFilter(read, watch.trigger());
                if (read.byController()) {
                    // "Whenever you or a permanent becomes the target of an ability you control" (Loki).
                    if ((targetEntry.getEntryType() != StackEntryType.ACTIVATED_ABILITY
                            && targetEntry.getEntryType() != StackEntryType.TRIGGERED_ABILITY)
                            || !controllerId.equals(targetEntry.getControllerId())
                            || !targetsPlayerOrPermanent(gameData, targetEntry)) {
                        continue;
                    }
                } else {
                    TargetAbilityReader.requireNoLimit(read, watch.trigger());
                }
                putTargetAbility(gameData, read.ability(), permanent, controllerId, "'s ability", targetEntry,
                        null, null, NO_EXTRA_RECORD);
            }
        });
    }


    private boolean targetsPlayerOrPermanent(GameData gameData, StackEntry targetEntry) {
        if (targetEntry.getTargetZone() == null
                && isPlayerOrPermanentTarget(gameData, targetEntry.getTargetId())) {
            return true;
        }
        return targetEntry.getTargetIds().stream()
                .anyMatch(targetId -> isPlayerOrPermanentTarget(gameData, targetId));
    }

    private boolean isPlayerOrPermanentTarget(GameData gameData, UUID targetId) {
        return targetId != null
                && (gameData.playerIds.contains(targetId)
                || gameQueryService.findPermanentById(gameData, targetId) != null);
    }

    private void collectBecomesTargetTriggers(GameData gameData, Permanent source, UUID controllerId,
                                              Permanent targetedCreature, StackEntry spellEntry) {
        if (source.isCloaked() || gameQueryService.hasLostPrintedAbilities(gameData, source)) return;
        TargetWatch watch = TargetWatch.ON_BECOMES_TARGET_OF_SPELL;
        List<TriggeredAbility> abilities = new ArrayList<>(TargetAbilityReader.printed(source.getCard(), watch));
        if (source == targetedCreature) {
            abilities.addAll(grantedTargetAbilities(gameData, source, watch));
        }
        UUID targetControllerId = gameQueryService.findPermanentController(gameData, targetedCreature.getId());

        // Each ability goes to the stack door whole: one with a target (Livewire Lash's "any target", granted to the
        // equipped creature, which is its source) has it chosen as it goes on the stack (CR 603.3d).
        for (TargetEffect read : targetReads(abilities, watch)) {
            TargetAbilityReader.requireNoFilter(read, watch.trigger());
            TargetAbilityReader.requireNoLimit(read, watch.trigger());
            // "This becomes the target of a spell you control" (Stormchaser Drake).
            if (read.byController() && !controllerId.equals(spellEntry.getControllerId())) continue;
            putTargetAbility(gameData, read.ability(), source, controllerId, "'s triggered ability", spellEntry,
                    targetedCreature, targetControllerId,
                    entry -> entry.setSourcePermanentSnapshot(new Permanent(source)));
        }
    }


    private void collectBecomesTargetOfSpellOrAbilityTriggers(
            GameData gameData, Permanent source, UUID controllerId, StackEntry triggeringEntry) {
        // Every targeting of a permanent counts toward "for the first time each turn", including targetings
        // that happened before a granting source entered the battlefield.
        boolean targetedEarlierThisTurn = source.isBecomeTargetCounterUsedThisTurn();
        source.setBecomeTargetCounterUsedThisTurn(true);
        // A face-down cloaked permanent has no printed abilities, but abilities granted to it still apply.
        TargetWatch watch = TargetWatch.ON_BECOMES_TARGET_OF_SPELL_OR_ABILITY;
        List<TriggeredAbility> abilities = new ArrayList<>(source.isCloaked()
                || gameQueryService.hasLostPrintedAbilities(gameData, source)
                ? List.of()
                : TargetAbilityReader.printed(source.getCard(), watch));
        abilities.addAll(WatchedAbilities.temporary(source, watch.trigger()));
        abilities.addAll(WatchedAbilities.persistent(source, watch.trigger()));
        // Dismiss into Dream continuously grants "When this creature becomes the target of a spell
        // or ability, sacrifice it" to each creature its controller's opponents control.
        abilities.addAll(grantedTargetAbilities(gameData, source, watch));
        // Makeshift Mannequin: while a permanent has a mannequin counter, it has "When this creature
        // becomes the target of a spell or ability, sacrifice it." The grant comes from the resolving
        // spell (CR 611.2b), so it is tracked on the permanent rather than read from the counter alone.
        if (source.isMannequinGrantActive() && source.getCounterCount(CounterType.MANNEQUIN) > 0
                && !gameQueryService.hasLostPrintedAbilities(gameData, source)) {
            abilities.add(MANNEQUIN_ABILITY);
        }
        // The permanent that became the target: the enchanted or equipped creature for an attachment's ability ("that
        // creature", Fractured Loyalty), else the source itself.
        UUID targetedId = source.isAttached() && !source.isBestow() ? source.getAttachedTo() : source.getId();
        Permanent targeted = targetedId.equals(source.getId()) ? source
                : gameQueryService.findPermanentById(gameData, targetedId);
        UUID targetedControllerId = targeted == null ? null
                : gameQueryService.findPermanentController(gameData, targeted.getId());

        // Each ability goes to the stack door whole, separate abilities separate objects (CR 603.3b); the door judges
        // "triggers only once each turn" and marks it, the collector "for the first time each turn".
        for (TargetEffect first : targetReads(abilities, watch)) {
            TargetAbilityReader.requireNoFilter(first, watch.trigger());
            // "Whenever this becomes the target of a spell or ability you control" (valiant).
            if (first.byController() && !controllerId.equals(triggeringEntry.getControllerId())) continue;
            // "Becomes the target of a spell or ability for the first time each turn" (Glyph Keeper): any earlier
            // targeting this turn counts, whether or not the permanent had the ability then.
            if (first.limit() == TriggerLimit.FIRST_TIME_EACH_TURN && targetedEarlierThisTurn) continue;
            putTargetAbility(gameData, first.ability(), source, controllerId, "'s triggered ability",
                    triggeringEntry, targeted, targetedControllerId, entry -> {
                        if (source.isAttached()) {
                            entry.setSourcePermanentSnapshot(new Permanent(source));
                        }
                        entry.setTriggeringPermanentId(targetedId);
                    });
        }
    }


    /**
     * "Whenever this creature becomes the target of an Aura spell, &lt;effect&gt;." Narrower than
     * {@link TargetWatch#ON_BECOMES_TARGET_OF_SPELL}: only Aura spells count, and there is no controller
     * restriction, so an opponent's Aura triggers it just as the controller's own does. Used by
     * Fugitive Druid.
     */
    private void collectBecomesTargetOfAuraSpellTriggers(GameData gameData, Permanent source, UUID controllerId, StackEntry spellEntry) {
        if (source.isCloaked()) return;
        Card spellCard = spellEntry.getTargetingCard();
        if (spellCard == null || !spellCard.isAura()) return;

        TargetWatch watch = TargetWatch.ON_BECOMES_TARGET_OF_AURA_SPELL;
        for (TriggeredAbility ability : unjudgedTargetAbilities(TargetAbilityReader.printed(source.getCard(), watch),
                watch)) {
            putTargetAbility(gameData, ability, source, controllerId, "'s triggered ability", spellEntry, source,
                    controllerId, NO_EXTRA_RECORD);
        }
    }


    private void collectBecomesTargetOfOpponentCounterTriggers(GameData gameData, Permanent source, UUID controllerId, StackEntry triggeringEntry) {
        // Only trigger if the spell/ability is controlled by an opponent
        if (controllerId.equals(triggeringEntry.getControllerId())) return;

        boolean wardSuppressed = gameQueryService.preventsOpponentCreatureWardTriggers(
                gameData, triggeringEntry.getControllerId())
                && hasWardAbility(gameData, source);
        if (source.isFaceDown()
                && (source.isCloaked() || source.isFaceDownWard())
                && gameQueryService.hasKeyword(gameData, source, Keyword.WARD)) {
            if (wardSuppressed) return;
            enqueueCloakedWardTrigger(gameData, source, controllerId, triggeringEntry);
        }

        TargetWatch watch = TargetWatch.ON_BECOMES_TARGET_OF_OPPONENT_SPELL;
        // Each ability whole (CR 603.3): the source's own (printed, and granted to it for a duration), then those
        // other permanents' static abilities grant it.
        List<TriggeredAbility> abilities = new ArrayList<>();
        boolean sourceIsTarget = source.getId().equals(triggeringEntry.getTargetId())
                || source.getCard().getId().equals(triggeringEntry.getTargetId())
                || triggeringEntry.getDeclaredTargetIds().contains(source.getId())
                || triggeringEntry.getDeclaredTargetIds().contains(source.getCard().getId())
                || triggeringEntry.getMixedZoneTargetPermanentIds().containsValue(source.getId());
        if (!source.isFaceDown() && !gameQueryService.hasLostPrintedAbilities(gameData, source)
                && (sourceIsTarget || !source.getCard().getKeywords().contains(Keyword.WARD))) {
            abilities.addAll(unjudgedTargetAbilities(TargetAbilityReader.printed(source.getCard(), watch), watch));
        }
        abilities.addAll(unjudgedTargetAbilities(WatchedAbilities.temporary(source, watch.trigger()), watch));
        abilities.addAll(unjudgedTargetAbilities(WatchedAbilities.persistent(source, watch.trigger()), watch));
        // Granted ward ("each legendary permanent you control has ward {2}") is a "this permanent becomes
        // the target" trigger, so an attached permanent only gets it when it is itself the target.
        List<TriggeredAbility> grantedAbilities = new ArrayList<>(
                unjudgedTargetAbilities(grantedTargetAbilities(gameData, source, watch), watch).stream()
                        .filter(ability -> sourceIsTarget || !actsOnASpell(ability))
                        .toList());
        boolean printedWardRemoved = source.getCard().getKeywords().contains(Keyword.WARD)
                && !gameQueryService.hasKeyword(gameData, source, Keyword.WARD);
        if (printedWardRemoved) {
            // A printed ward whose keyword was removed (e.g. "permanents your opponents control lose ward")
            // no longer has its counter-unless trigger.
            abilities.removeIf(TriggerCollectionService::actsOnASpell);
            grantedAbilities.removeIf(TriggerCollectionService::actsOnASpell);
        }
        if (wardSuppressed) {
            // Ward suppression only removes ward: a printed ward keyword's counter-unless goes, and so
            // does a granted ward, but a creature's own non-ward "counter unless" trigger stays (CR 702.21a).
            if (gameQueryService.hasKeyword(gameData, source, Keyword.WARD)) {
                abilities.removeIf(TriggerCollectionService::actsOnASpell);
            }
            grantedAbilities.removeIf(TriggerCollectionService::actsOnASpell);
        }
        abilities.addAll(grantedAbilities);

        // The abilities that act on "that spell or ability" (ward and the other "counter it unless" abilities), each
        // handed to the stack door whole, which aims it at that spell or ability; the others of the watch are
        // collectBecomesTargetOfOpponentSpellOrAbilityNonCounterTriggers's.
        Permanent targeted = sourceIsTarget || !source.isAttached() ? source
                : gameQueryService.findPermanentById(gameData, source.getAttachedTo());
        UUID targetedControllerId = targeted == null ? null
                : gameQueryService.findPermanentController(gameData, targeted.getId());
        for (TriggeredAbility ability : abilities) {
            if (!actsOnASpell(ability)) continue;
            putTargetAbility(gameData, ability, source, controllerId, "'s ability", triggeringEntry, targeted,
                    targetedControllerId, entry -> entry.setSourcePermanentSnapshot(new Permanent(source)));
        }
    }


    public void checkCollectEvidenceTriggers(GameData gameData, UUID collectingPlayerId) {
        var ctx = new TriggerContext.CollectEvidence(collectingPlayerId);
        List<Permanent> ownBattlefield = gameData.playerBattlefields.get(collectingPlayerId);
        if (ownBattlefield == null) {
            return;
        }

        for (Permanent perm : List.copyOf(ownBattlefield)) {
            dispatchWatch(gameData, perm, collectingPlayerId,
                    ActionWatch.ON_CONTROLLER_COLLECTS_EVIDENCE, ctx);
        }
    }

    /**
     * Puts a face-down cloaked permanent's ward {2} (CR 701.58a) on the stack, aimed at the spell or ability that
     * targeted it, which it counters unless that player pays.
     */
    private void enqueueCloakedWardTrigger(GameData gameData, Permanent source, UUID controllerId,
                                           StackEntry triggeringEntry) {
        Card wardSourceCard = Card.namedRuntimePlaceholder("Cloaked creature");
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                wardSourceCard,
                controllerId,
                wardSourceCard.getName() + "'s ward ability",
                new ArrayList<>(List.of(new CounterUnlessPaysEffect(2))),
                null,
                source.getId()
        );
        entry.setSourcePermanentSnapshot(new Permanent(source));
        recordTargetingObject(entry, triggeringEntry);
        entry.setTargetId(triggeringEntry.getTargetableId());
        entry.setTargetZone(Zone.STACK);
        entry.setNonTargeting(true);
        gameData.stack.add(entry);
    }


    private void collectControllerBecomesTargetOfOpponentTriggers(
            GameData gameData, UUID playerId, StackEntry triggeringEntry) {
        if (playerId.equals(triggeringEntry.getControllerId())) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return;

        TargetWatch watch = TargetWatch.ON_CONTROLLER_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY;
        for (Permanent source : new ArrayList<>(battlefield)) {
            if (source.isLosesAllAbilitiesUntilEndOfTurn()) continue;
            for (TriggeredAbility ability : unjudgedTargetAbilities(TargetAbilityReader.printed(source.getCard(),
                    watch), watch)) {
                // This triggered while the targeting ability was being put on the stack, so it goes on the
                // stack after that batch rather than being APNAP-ordered with it (CR 603.3b).
                putTargetAbility(gameData, ability, source, playerId, "'s triggered ability", triggeringEntry, null,
                        playerId, entry -> {
                            entry.setSourcePermanentSnapshot(new Permanent(source));
                            entry.setTriggerOrderChosen(true);
                        });
            }
        }
    }


    private void collectControllerBecomesTargetOfSpellTriggers(
            GameData gameData, UUID playerId, StackEntry spellEntry) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return;

        for (Permanent source : new ArrayList<>(battlefield)) {
            for (TriggeredAbility ability : targetAbilities(gameData, source,
                    TargetWatch.ON_CONTROLLER_BECOMES_TARGET_OF_SPELL,
                    !gameQueryService.hasLostPrintedAbilities(gameData, source))) {
                putTargetAbility(gameData, ability, source, playerId, "'s triggered ability", spellEntry, null,
                        playerId, NO_EXTRA_RECORD);
            }
        }
    }


    /**
     * "Whenever this creature becomes the target of a spell or ability an opponent controls, &lt;non-counter effect&gt;."
     * Handles the abilities of {@link TargetWatch#ON_BECOMES_TARGET_OF_OPPONENT_SPELL} that don't act on "that spell or
     * ability" (e.g. "you may draw a card"); the counter and ward abilities are
     * {@link #collectBecomesTargetOfOpponentCounterTriggers}'s. Called from both the spell and ability paths so the
     * "or ability" clause is honored. The record names the spell or ability's controller as the player whose
     * permanents a target "that player controls" is judged against. Used by Tenured Concocter, and by the ability
     * Lae'zel, Githyanki Warrior grants the creature it becomes.
     */
    private void collectBecomesTargetOfOpponentSpellOrAbilityNonCounterTriggers(
            GameData gameData, Permanent source, UUID controllerId, StackEntry triggeringEntry) {
        UUID spellOrAbilityControllerId = triggeringEntry.getControllerId();
        // Only trigger if the spell/ability is controlled by an opponent
        if (controllerId.equals(spellOrAbilityControllerId)) return;

        TargetWatch watch = TargetWatch.ON_BECOMES_TARGET_OF_OPPONENT_SPELL;
        List<TriggeredAbility> abilities = new ArrayList<>();
        if (!source.isCloaked() && !source.isFaceDown()
                && !gameQueryService.hasLostPrintedAbilities(gameData, source)) {
            abilities.addAll(unjudgedTargetAbilities(TargetAbilityReader.printed(source.getCard(), watch), watch));
        }
        // Abilities granted to this permanent itself still apply when its printed ones don't.
        abilities.addAll(unjudgedTargetAbilities(WatchedAbilities.temporary(source, watch.trigger()), watch));
        abilities.addAll(unjudgedTargetAbilities(WatchedAbilities.persistent(source, watch.trigger()), watch));

        for (TriggeredAbility ability : abilities) {
            if (actsOnASpell(ability)) continue;
            putTargetAbility(gameData, ability, source, controllerId, "'s triggered ability", triggeringEntry, source,
                    controllerId, entry -> {
                        entry.setAttackedTargetId(spellOrAbilityControllerId);
                        entry.setSourcePermanentSnapshot(new Permanent(source));
                    });
        }
    }


    private void collectBecomesTargetOfOpponentSpellOnlyTriggers(
            GameData gameData, Permanent source, UUID controllerId, StackEntry spellEntry) {
        if (controllerId.equals(spellEntry.getControllerId())) return;

        // "Whenever this creature becomes the target of a spell an opponent controls" (Reality Smasher) sees spells
        // only; a face-down permanent, or one whose printed abilities were removed, has none of its printed abilities.
        for (TriggeredAbility ability : targetAbilities(gameData, source,
                TargetWatch.ON_BECOMES_TARGET_OF_OPPONENT_SPELL_ONLY,
                !source.isFaceDown() && !gameQueryService.hasLostPrintedAbilities(gameData, source))) {
            putTargetAbility(gameData, ability, source, controllerId, "'s triggered ability", spellEntry, source,
                    controllerId, NO_EXTRA_RECORD);
        }
    }


    /**
     * Checks ALL permanents on the targeted creature's controller's battlefield for
     * {@link TargetWatch#ON_ALLY_CREATURE_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY}. Only fires when the
     * targeted permanent is a creature and the spell/ability is controlled by an opponent of the creature's
     * controller. The record names the targeted creature as the triggering permanent ("other than that creature",
     * Pawpatch Recruit) and the spell or ability's controller as the triggering player ("that player", Scalelord
     * Reckoner).
     */
    private void collectAllyCreatureBecomesTargetOfOpponentTriggers(
            GameData gameData, Permanent targetPermanent, UUID creatureControllerId, StackEntry triggeringEntry) {
        // Only trigger for creatures
        if (!gameQueryService.isCreature(gameData, targetPermanent)) return;
        // Only trigger if the spell/ability is controlled by an opponent
        if (creatureControllerId.equals(triggeringEntry.getControllerId())) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(creatureControllerId);
        if (battlefield == null) return;

        for (Permanent source : List.copyOf(battlefield)) {
            if (gameQueryService.hasLostPrintedAbilities(gameData, source)) continue;
            TargetWatch watch = TargetWatch.ON_ALLY_CREATURE_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY;
            List<TriggeredAbility> abilities = new ArrayList<>(TargetAbilityReader.printed(source.getCard(), watch));
            abilities.addAll(grantedTargetAbilities(gameData, source, watch));

            for (TargetEffect read : targetReads(abilities, watch)) {
                TargetAbilityReader.requireWatchedEvent(read, watch.trigger());
                TargetAbilityReader.requireNoLimit(read, watch.trigger());
                // "Whenever a Dragon you control becomes the target ..." (Thunderbreak Regent, Diffusion Sliver).
                if (!targetFilterMatches(gameData, source, creatureControllerId, targetPermanent, read)) continue;
                putTargetAbility(gameData, read.ability(), source, creatureControllerId, "'s triggered ability",
                        triggeringEntry, targetPermanent, creatureControllerId, entry -> {
                            entry.setTriggeringPermanentId(targetPermanent.getId());
                            entry.setSourcePermanentSnapshot(new Permanent(source));
                        });
            }
        }
    }


    /**
     * Checks the spell-only opponent-target trigger slot. This is separate from the
     * spell-or-ability slot because cards such as Swarm Shambler do not trigger from abilities.
     */
    private void collectAllyCreatureBecomesTargetOfOpponentSpellTriggers(
            GameData gameData, Permanent targetPermanent, UUID creatureControllerId, StackEntry spellEntry) {
        if (!targetPermanent.getCard().hasType(CardType.CREATURE)) return;
        if (creatureControllerId.equals(spellEntry.getControllerId())) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(creatureControllerId);
        if (battlefield == null) return;

        for (Permanent source : List.copyOf(battlefield)) {
            TargetWatch watch = TargetWatch.ON_ALLY_CREATURE_BECOMES_TARGET_OF_OPPONENT_SPELL;
            List<TriggeredAbility> abilities = new ArrayList<>(TargetAbilityReader.printed(source.getCard(), watch));
            abilities.addAll(grantedTargetAbilities(gameData, source, watch));

            for (TargetEffect read : targetReads(abilities, watch)) {
                TargetAbilityReader.requireWatchedEvent(read, watch.trigger());
                TargetAbilityReader.requireNoLimit(read, watch.trigger());
                // "Whenever a creature you control with a +1/+1 counter on it becomes the target" (Swarm Shambler).
                if (!targetFilterMatches(gameData, source, creatureControllerId, targetPermanent, read)) continue;
                putTargetAbility(gameData, read.ability(), source, creatureControllerId, "'s triggered ability",
                        spellEntry, targetPermanent, creatureControllerId, NO_EXTRA_RECORD);
            }
        }
    }


    /**
     * Checks ALL permanents on the targeted creature's controller's battlefield for
     * {@link TargetWatch#ON_ALLY_CREATURE_BECOMES_TARGET_OF_INSTANT_OR_SORCERY}. Only fires when the
     * targeted permanent is a creature and the targeting spell is an instant or a sorcery; there is
     * no controller restriction, so the creature's controller's own spells trigger it too. The
     * targeted creature is the record's non-targeting {@code targetId} ("that creature"). The trigger's filter is
     * judged on the targeted creature while the trigger event is collected. Used by Wild Defiance and Silverfur
     * Partisan.
     */
    private void collectAllyCreatureBecomesTargetOfInstantOrSorceryTriggers(
            GameData gameData, Permanent targetPermanent, UUID creatureControllerId, StackEntry spellEntry) {
        if (!gameQueryService.isCreature(gameData, targetPermanent)) return;
        if (!spellEntry.getCard().hasType(CardType.INSTANT) && !spellEntry.getCard().hasType(CardType.SORCERY)) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(creatureControllerId);
        if (battlefield == null) return;

        for (Permanent source : List.copyOf(battlefield)) {
            if (gameQueryService.hasLostPrintedAbilities(gameData, source)) continue;
            TargetWatch watch = TargetWatch.ON_ALLY_CREATURE_BECOMES_TARGET_OF_INSTANT_OR_SORCERY;
            for (TargetEffect read : targetReads(TargetAbilityReader.printed(source.getCard(), watch), watch)) {
                TargetAbilityReader.requireWatchedEvent(read, watch.trigger());
                TargetAbilityReader.requireNoLimit(read, watch.trigger());
                // "Whenever a Wolf or Werewolf you control becomes the target" (Silverfur Partisan).
                if (!targetFilterMatches(gameData, source, creatureControllerId, targetPermanent, read)) continue;
                putTargetAbility(gameData, read.ability(), source, creatureControllerId, "'s triggered ability",
                        spellEntry, targetPermanent, creatureControllerId, entry -> {
                            entry.setTargetId(targetPermanent.getId());
                            entry.setTriggeringPermanentId(targetPermanent.getId());
                        });
            }
        }
    }


    public void checkCreatureTapForManaTriggers(GameData gameData, UUID tappingPlayerId,
                                                UUID tappedCreatureId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(tappingPlayerId);
        if (battlefield == null) return;

        var ctx = new TriggerContext.CreatureTapForMana(tappingPlayerId, tappedCreatureId);
        for (Permanent perm : List.copyOf(battlefield)) {
            if (gameQueryService.hasLostPrintedAbilities(gameData, perm)) continue;
            for (List<CardEffect> effects : tapAbilities(perm.getCard(),
                    TapWatch.ON_CONTROLLER_TAPS_CREATURE_FOR_MANA)) {
                var match = new TriggerMatchContext(gameData, perm, tappingPlayerId, effects);
                registry.dispatch(match, TapWatch.ON_CONTROLLER_TAPS_CREATURE_FOR_MANA, ctx);
            }
        }
    }

    public void checkNonlandPermanentTapForManaTriggers(GameData gameData, UUID tappingPlayerId,
                                                        Permanent tappedPermanent,
                                                        Map<ManaColor, Integer> manaBefore,
                                                        boolean manaTypeChoicePending) {
        if (tappedPermanent.getCard().hasType(CardType.LAND)) {
            return;
        }
        UUID tappedPermanentId = tappedPermanent.getId();

        Set<ManaColor> producedManaTypes = manaTypeChoicePending
                ? null
                : newlyProducedManaTypes(manaBefore,
                        gameData.playerManaPools.get(tappingPlayerId).getAllManaTotals());
        if (producedManaTypes != null && producedManaTypes.isEmpty()) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(tappingPlayerId);
        if (battlefield == null) return;

        var ctx = new TriggerContext.NonlandPermanentTapForMana(
                tappingPlayerId, tappedPermanentId, producedManaTypes);
        List<TapWatch> watches = tappedPermanent.getCard().isToken() && tappedPermanent.getCard().hasType(CardType.ARTIFACT)
                ? List.of(TapWatch.ON_CONTROLLER_TAPS_NONLAND_PERMANENT_FOR_MANA,
                        TapWatch.ON_CONTROLLER_TAPS_ARTIFACT_TOKEN_FOR_MANA)
                : List.of(TapWatch.ON_CONTROLLER_TAPS_NONLAND_PERMANENT_FOR_MANA);
        for (TapWatch watch : watches) {
            for (Permanent perm : List.copyOf(battlefield)) {
                if (gameQueryService.hasLostPrintedAbilities(gameData, perm)) continue;
                for (List<CardEffect> effects : tapAbilities(perm.getCard(), watch)) {
                    var match = new TriggerMatchContext(gameData, perm, tappingPlayerId, effects);
                    registry.dispatch(match, watch, ctx);
                }
            }
        }
    }

    /** Fires triggers that watch any permanent being tapped for mana, including face-up planes. */
    public void checkAnyPlayerTapsPermanentForManaTriggers(GameData gameData, UUID tappingPlayerId,
                                                           UUID tappedPermanentId,
                                                           Map<ManaColor, Integer> manaBefore,
                                                           boolean manaTypeChoicePending) {
        Permanent tappedPermanent = gameQueryService.findPermanentById(gameData, tappedPermanentId);
        if (tappedPermanent == null) {
            return;
        }

        Set<ManaColor> producedManaTypes = manaTypeChoicePending
                ? null
                : newlyProducedManaTypes(manaBefore,
                        gameData.playerManaPools.get(tappingPlayerId).getAllManaTotals());

        var context = new TriggerContext.PermanentTapForMana(
                tappingPlayerId, tappedPermanentId, producedManaTypes);
        gameData.forEachPermanent((controllerId, permanent) ->
                dispatchWatch(gameData, permanent, controllerId,
                        TapWatch.ON_ANY_PLAYER_TAPS_PERMANENT_FOR_MANA, context));

        if (gameData.planechase == null || gameData.planechase.controllerId == null) {
            return;
        }
        for (PlanarObject object : List.copyOf(gameData.planechase.faceUp)) {
            for (List<CardEffect> effects : byAbility(TapAbilityReader.printed(object.getCard(),
                    TapWatch.ON_ANY_PLAYER_TAPS_PERMANENT_FOR_MANA), abilities -> TapAbilityReader.effects(abilities,
                    TapWatch.ON_ANY_PLAYER_TAPS_PERMANENT_FOR_MANA.trigger()))) {
                registry.dispatch(new TriggerMatchContext(gameData, null, gameData.planechase.controllerId, effects,
                        object.getCard(), object.copy()), TapWatch.ON_ANY_PLAYER_TAPS_PERMANENT_FOR_MANA, context);
            }
        }
    }

    private boolean hasPlanarCreatureCombatDamageToYouTrigger(GameData gameData, UUID damagedPlayerId) {
        return gameData.planechase != null
                && Objects.equals(gameData.planechase.controllerId, damagedPlayerId)
                && gameData.planechase.faceUp.stream()
                .anyMatch(object -> !WatchedAbilities.printed(object.getCard(),
                        CombatWatch.ON_CREATURE_DEALS_COMBAT_DAMAGE_TO_YOU).isEmpty());
    }

    private void queuePlanarCreatureCombatDamageToYouTriggers(GameData gameData, UUID damagedPlayerId,
                                                               Permanent sourceCreature) {
        if (!gameQueryService.isCreature(gameData, sourceCreature)
                || gameData.planechase == null
                || !Objects.equals(gameData.planechase.controllerId, damagedPlayerId)) {
            return;
        }

        UUID creatureControllerId = gameQueryService.findPermanentController(gameData, sourceCreature.getId());
        for (PlanarObject object : List.copyOf(gameData.planechase.faceUp)) {
            putPlanarCombatDamageAbilities(gameData, object, damagedPlayerId,
                    CombatWatch.ON_CREATURE_DEALS_COMBAT_DAMAGE_TO_YOU, sourceCreature, creatureControllerId);
        }
    }

    private static Set<ManaColor> newlyProducedManaTypes(Map<ManaColor, Integer> before,
                                                         Map<ManaColor, Integer> after) {
        EnumSet<ManaColor> produced = EnumSet.noneOf(ManaColor.class);
        for (ManaColor color : ManaColor.values()) {
            if (after.getOrDefault(color, 0) > before.getOrDefault(color, 0)) {
                produced.add(color);
            }
        }
        return produced;
    }

    /**
     * "Whenever a creature you control becomes the target of a spell" (Gargos, Vicious Watcher; Venerated Rotpriest):
     * the targeted creature is the record's non-targeting {@code targetId} ("that creature", "its controller").
     */
    private void collectAllyCreatureBecomesTargetOfSpellTriggers(
            GameData gameData, Permanent targetPermanent, UUID creatureControllerId, StackEntry triggeringEntry) {
        if (!gameQueryService.isCreature(gameData, targetPermanent)) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(creatureControllerId);
        if (battlefield == null) return;

        for (Permanent source : List.copyOf(battlefield)) {
            if (source.isLosesAllAbilitiesUntilEndOfTurn()) continue;
            GameQueryService.StaticBonus staticBonus = gameQueryService.computeStaticBonus(gameData, source);
            if (staticBonus.losesAllAbilities() || staticBonus.losesAllNonManaAbilities()
                    || gameQueryService.hasLostPrintedAbilities(gameData, source)) continue;
            for (TriggeredAbility ability : targetAbilities(gameData, source,
                    TargetWatch.ON_ALLY_CREATURE_BECOMES_TARGET_OF_SPELL, true)) {
                putTargetAbility(gameData, ability, source, creatureControllerId, "'s triggered ability",
                        triggeringEntry, targetPermanent, creatureControllerId, entry -> {
                            entry.setTargetId(targetPermanent.getId());
                            entry.setTriggeringPermanentId(targetPermanent.getId());
                            entry.setSourcePermanentSnapshot(new Permanent(source));
                        });
            }
        }
    }


    /**
     * Checks all other permanents on the targeted creature's controller's battlefield for
     * {@link TargetWatch#ON_ANOTHER_ALLY_CREATURE_BECOMES_TARGET_OF_SPELL_OR_ABILITY}. The
     * targeted creature is the record's non-targeting {@code targetId}.
     */
    private void collectAnotherAllyCreatureBecomesTargetOfSpellOrAbilityTriggers(
            GameData gameData, Permanent targetPermanent, UUID creatureControllerId, StackEntry triggeringEntry) {
        if (!gameQueryService.isCreature(gameData, targetPermanent)) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(creatureControllerId);
        if (battlefield == null) return;

        for (Permanent source : List.copyOf(battlefield)) {
            if (source.getId().equals(targetPermanent.getId())
                    || source.isCloaked()
                    || source.isLosesAllAbilitiesUntilEndOfTurn()) continue;

            TargetWatch watch = TargetWatch.ON_ANOTHER_ALLY_CREATURE_BECOMES_TARGET_OF_SPELL_OR_ABILITY;
            for (TriggeredAbility ability : targetAbilities(gameData, source, watch, true)) {
                putTargetAbility(gameData, ability, source, creatureControllerId, "'s triggered ability",
                        triggeringEntry, targetPermanent, creatureControllerId, entry -> {
                            entry.setTargetId(targetPermanent.getId());
                            entry.setTriggeringPermanentId(targetPermanent.getId());
                        });
            }
        }
    }


    /**
     * Whether the spell or ability that targets is one the ability's {@link TargetTrigger#source()} admits: an ability
     * of a permanent matching it, judged from the watcher's point of view ("an ability of a land you control named
     * Labyrinth of Skophos"). A spell has no such permanent.
     */
    private boolean targetingSourceMatches(GameData gameData, TargetEffect effect, StackEntry triggeringEntry,
                                           FilterContext watcherContext) {
        if (effect.source() == null) {
            return true;
        }
        if (triggeringEntry.getEntryType() != StackEntryType.ACTIVATED_ABILITY
                && triggeringEntry.getEntryType() != StackEntryType.TRIGGERED_ABILITY
                || triggeringEntry.getSourcePermanentId() == null) {
            return false;
        }
        Permanent abilitySource = gameQueryService.findPermanentById(gameData, triggeringEntry.getSourcePermanentId());
        return abilitySource != null
                && predicateEvaluationService.matchesPermanentPredicate(abilitySource, effect.source(), watcherContext);
    }

    /**
     * Checks ALL permanents across every battlefield for
     * {@link TargetWatch#ON_ANY_CREATURE_BECOMES_TARGET_OF_SPELL_OR_ABILITY}. Only fires when the
     * targeted permanent is a creature. The targeted creature is the record's non-targeting
     * {@code targetId} so the resolved effect can act on it. Used by Cowardice.
     */
    private void collectAnyCreatureBecomesTargetTriggers(
            GameData gameData, Permanent targetPermanent, StackEntry triggeringEntry) {
        if (!gameQueryService.isCreature(gameData, targetPermanent)) return;
        UUID targetControllerId = gameQueryService.findPermanentController(gameData, targetPermanent.getId());

        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;

            for (Permanent source : List.copyOf(battlefield)) {
                if (gameQueryService.hasLostPrintedAbilities(gameData, source)) continue;
                TargetWatch watch = TargetWatch.ON_ANY_CREATURE_BECOMES_TARGET_OF_SPELL_OR_ABILITY;
                FilterContext watcherContext = FilterContext.of(gameData)
                        .withSourceControllerId(playerId).withSourcePermanentId(source.getId());
                for (TargetEffect read : targetReads(TargetAbilityReader.printed(source.getCard(), watch), watch)) {
                    TargetAbilityReader.requireWatchedEvent(read, watch.trigger());
                    TargetAbilityReader.requireNoLimit(read, watch.trigger());
                    // "Whenever a Cleric creature you control becomes the target" (Daru Spiritualist).
                    if (read.filter() != null && !predicateEvaluationService.matchesPermanentPredicate(
                            targetPermanent, read.filter(), watcherContext)
                            || !targetingSourceMatches(gameData, read, triggeringEntry, watcherContext)) {
                        continue;
                    }
                    putTargetAbility(gameData, read.ability(), source, playerId, "'s triggered ability",
                            triggeringEntry, targetPermanent, targetControllerId, entry -> {
                                entry.setTargetId(targetPermanent.getId());
                                entry.setTriggeringPermanentId(targetPermanent.getId());
                            });
                }
            }
        }
    }


    /**
     * Checks the spell/ability controller's battlefield for
     * {@link TargetWatch#ON_OPPONENT_CREATURE_BECOMES_TARGET_OF_YOUR_SPELL_OR_ABILITY}. Only fires
     * when the targeted permanent is a creature controlled by an opponent of the spell/ability's
     * controller. The targeted creature is the record's non-targeting {@code targetId}. Used by Willbreaker.
     */
    private void collectOpponentCreatureBecomesTargetOfYourSpellTriggers(
            GameData gameData, Permanent targetPermanent, UUID creatureControllerId, StackEntry triggeringEntry) {
        if (!gameQueryService.isCreature(gameData, targetPermanent)) return;

        UUID triggeringControllerId = triggeringEntry.getControllerId();
        if (triggeringControllerId == null || triggeringControllerId.equals(creatureControllerId)) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(triggeringControllerId);
        if (battlefield == null) return;

        for (Permanent source : List.copyOf(battlefield)) {
            TargetWatch watch = TargetWatch.ON_OPPONENT_CREATURE_BECOMES_TARGET_OF_YOUR_SPELL_OR_ABILITY;
            if (gameQueryService.hasLostPrintedAbilities(gameData, source)) continue;
            for (TriggeredAbility ability : unjudgedTargetAbilities(TargetAbilityReader.printed(source.getCard(),
                    watch), watch)) {
                putTargetAbility(gameData, ability, source, triggeringControllerId, "'s triggered ability",
                        triggeringEntry, targetPermanent, creatureControllerId, entry -> {
                            entry.setTargetId(targetPermanent.getId());
                            entry.setTriggeringPermanentId(targetPermanent.getId());
                        });
            }
        }
    }


    public void checkDealtDamageToCreatureTriggers(GameData gameData, Permanent damagedCreature, int damageDealt,
                                                   UUID damageSourceControllerId) {
        checkDealtDamageToCreatureTriggers(gameData, damagedCreature, damageDealt, damageSourceControllerId,
                null, null);
    }

    public void checkDealtDamageToCreatureTriggers(GameData gameData, Permanent damagedCreature, int damageDealt,
                                                   UUID damageSourceControllerId, Card sourceCard,
                                                   UUID sourcePermanentId) {
        if (damageDealt > 0) {
            checkEnchantedCreatureDealtDamageTriggers(gameData, damagedCreature, damageDealt);
        }

        checkDamageToCreatureTriggers(gameData, damagedCreature, damageDealt, damageSourceControllerId,
                sourceCard, sourcePermanentId, DamageWatch.ON_DEALT_DAMAGE);
    }

    /** Fires triggers whose event specifically requires noncombat damage dealt to this permanent. */
    public void checkNoncombatDamageToSelfTriggers(GameData gameData, Permanent damagedCreature,
                                                   int damageDealt, UUID damageSourceControllerId,
                                                   Card sourceCard, UUID sourcePermanentId) {
        checkDamageToCreatureTriggers(gameData, damagedCreature, damageDealt, damageSourceControllerId,
                sourceCard, sourcePermanentId, DamageWatch.ON_NONCOMBAT_DAMAGE_TO_SELF);
    }

    private void checkDamageToCreatureTriggers(GameData gameData, Permanent damagedCreature, int damageDealt,
                                               UUID damageSourceControllerId, Card sourceCard,
                                               UUID sourcePermanentId, DamageWatch watch) {
        Trigger watched = watch.trigger();
        List<TriggeredAbility> abilities = new ArrayList<>();
        if (!damagedCreature.isFaceDown()) {
            abilities.addAll(printedWatchedAbilities(gameData, damagedCreature, watch));
        }
        abilities.addAll(WatchedAbilities.temporary(damagedCreature, watched));
        abilities.addAll(WatchedAbilities.persistent(damagedCreature, watched));
        abilities.addAll(grantedTriggeredAbilitySupport.grantedWatchedAbilities(gameData, damagedCreature, watched));
        if (abilities.isEmpty()) return;

        UUID controllerId = gameQueryService.findPermanentController(gameData, damagedCreature.getId());
        if (controllerId == null) return;

        collectDealtDamageAbilities(gameData, DamageAbilityReader.readByAbility(abilities, watched), watch,
                damagedCreature, controllerId, new TriggerContext.DamageToCreature(damagedCreature, damageDealt,
                        damageSourceControllerId, sourceCard, sourcePermanentId));
    }

    /**
     * Hands the "whenever this is dealt damage" abilities {@code abilities} (one list of reads for each ability, each
     * one object on the stack, CR 603.3) of {@code damaged} (controlled by {@code controllerId}) to the stack door for
     * the damage {@code ctx} names: for noncombat damage as it is dealt, and for the combat damage step's (which reads
     * the abilities as the damage is dealt and hands them on once its creatures have died) with the permanent's last
     * known information. Each ability's event condition is judged as it triggers, and its intervening "if" and limit
     * by the door (CR 603.4), with the damage as the event's value, the source as the triggering permanent and its
     * controller as "that player" (Archfiend of Spite's "a source an opponent controls"). The ability is the damaged
     * permanent's (CR 113.7); its record names the damage as its event value ("that much", CR 120.8: no damage, no
     * trigger), the source of the damage as its triggering permanent (if it was one) with that source's controller as it
     * dealt the damage ("that source's controller"), and the permanent as it was dealt the damage, with the damage
     * keywords it had, as its source snapshot ("it deals that much damage" once it has died, CR 608.2h).
     */
    public void collectDealtDamageAbilities(GameData gameData, List<List<DamageAbilityReader.DamageEffect>> abilities,
                                            DamageWatch watch, Permanent damaged, UUID controllerId,
                                            TriggerContext.DamageToCreature ctx) {
        if (ctx.damageDealt() <= 0 || controllerId == null) return;
        ConditionContext conditionContext = ConditionContext.forPermanent(damaged, controllerId)
                .withTargetId(ctx.damageSourceControllerId())
                .withTriggeringPermanentId(ctx.sourcePermanentId())
                .withEventValue(ctx.damageDealt());
        Permanent lastKnown = ctx.damagedLastKnown() != null
                ? ctx.damagedLastKnown() : lastKnownDamageSource(gameData, damaged);
        // "Once for each source" (CR 603.2c): each damage event the collector is given has one source, or is the
        // combat damage step's to this permanent.
        for (List<DamageAbilityReader.DamageEffect> ability : abilities) {
            DamageAbilityReader.DamageEffect read = ability.getFirst();
            if (read.filter() != null) {
                throw new IllegalStateException("The collector of " + watch + " judges no filter: " + read.ability());
            }
            putEventAbility(gameData, read.ability(), damaged.getCard(), damaged, controllerId, conditionContext, ctx,
                    entry -> {
                        entry.setEventValue(ctx.damageDealt());
                        entry.setTriggeringPermanentId(ctx.sourcePermanentId());
                        entry.setTriggeringPermanentControllerId(ctx.damageSourceControllerId());
                        entry.setSourcePermanentSnapshot(lastKnown);
                    });
        }
    }

    /**
     * "Whenever enchanted (equipped) creature is dealt damage, …": the ability is the Aura's or Equipment's (CR 113.7),
     * whatever its effects are. Its record names the creature as the triggering permanent (its id, controller, power
     * and a snapshot with the damage keywords it had, its last known information once it has left, CR 608.2h) and the
     * damage dealt as the event value; "it" / "its controller" / "that much" read them as the ability resolves. A
     * targeted ability's target is chosen as it goes on the stack (CR 603.3d).
     */
    public void checkEnchantedCreatureDealtDamageTriggers(GameData gameData, Permanent damagedCreature, int damageDealt) {
        if (damageDealt <= 0) return;

        var ctx = new TriggerContext.DamageToCreature(damagedCreature, damageDealt, null);
        DamageWatch watch = DamageWatch.ON_ENCHANTED_CREATURE_DEALT_DAMAGE;
        gameData.forEachPermanent((auraOwnerId, perm) -> {
            if (!perm.isAttached() || !perm.getAttachedTo().equals(damagedCreature.getId())) {
                return;
            }
            for (TriggeredAbility ability : printedWatchedAbilities(gameData, perm, watch)) {
                requireNothingToJudge(ability, watch);
                putEventAbility(gameData, ability, perm.getCard(), perm, auraOwnerId,
                        ConditionContext.forPermanent(perm, auraOwnerId)
                                .withTriggeringPermanentId(damagedCreature.getId()).withEventValue(damageDealt), ctx,
                        damagedPermanentRecord(gameData, perm, damagedCreature,
                                gameQueryService.findPermanentController(gameData, damagedCreature.getId()),
                                damageDealt, true, false));
            }
        });
    }

    /**
     * The record of a "[a permanent] is dealt damage" ability of {@code watcher}, whatever its effects are: the damaged
     * permanent as its triggering permanent with its card ("that card"), its controller as it was dealt the damage
     * ("that creature's controller"), its last known information with the damage keywords it had ("it deals that much
     * damage", CR 608.2h) and, with {@code recordPower}, its power, the damage as its event value ("that much"), the
     * watcher as it was when the ability triggered, and, with {@code namedObject}, the damaged permanent as the
     * non-targeting "that creature" (CR 115.10a).
     */
    private TriggerRecord damagedPermanentRecord(GameData gameData, Permanent watcher, Permanent damaged,
                                                 UUID damagedControllerId, int damage, boolean recordPower,
                                                 boolean namedObject) {
        Permanent lastKnown = lastKnownDamageSource(gameData, damaged);
        Integer power = recordPower ? gameQueryService.getEffectivePower(gameData, damaged) : null;
        Permanent snapshot = new Permanent(watcher);
        return entry -> {
            entry.setNonTargeting(true);
            if (namedObject) {
                entry.setTargetId(damaged.getId());
            }
            entry.setEventValue(damage);
            entry.setTriggeringPermanentId(damaged.getId());
            entry.setTriggeringCardId(damaged.getCard().getId());
            entry.setTriggeringPermanentControllerId(damagedControllerId);
            entry.setTriggeringPermanent(lastKnown);
            if (power != null) {
                entry.setTriggeringPermanentPowerAtTrigger(power);
            }
            entry.setSourcePermanentSnapshot(snapshot);
        };
    }

    /**
     * Last known information for a creature whose own ability deals damage after it may have died:
     * the damage-relevant keywords it had from other permanents' static abilities are baked in.
     */
    private Permanent lastKnownDamageSource(GameData gameData, Permanent creature) {
        Permanent snapshot = new Permanent(creature);
        for (Keyword keyword : List.of(Keyword.LIFELINK, Keyword.DEATHTOUCH, Keyword.INFECT, Keyword.WITHER)) {
            if (gameQueryService.hasKeyword(gameData, creature, keyword)) {
                snapshot.getGrantedKeywords().add(keyword);
            }
        }
        return snapshot;
    }

    // ── Opponent-creature-dealt-damage triggers ──────────────────────────

    /**
     * "Whenever enchanted player is dealt damage" (Grievous Wound): the ability is the Curse's (CR 113.7), the player
     * its non-targeting "that player" and the damage its event value.
     */
    public void checkEnchantedPlayerDealtDamageTriggers(GameData gameData, UUID damagedPlayerId,
            int damageDealt) {
        if (damageDealt <= 0) return;

        var ctx = new TriggerContext.DamageToControllerAmount(damagedPlayerId, damageDealt);
        DamageWatch watch = DamageWatch.ON_ENCHANTED_PLAYER_DEALT_DAMAGE;
        gameData.forEachPermanent((auraOwnerId, perm) -> {
            if (!perm.isAttached() || !damagedPlayerId.equals(perm.getAttachedTo())) {
                return;
            }
            for (TriggeredAbility ability : printedWatchedAbilities(gameData, perm, watch)) {
                requireNothingToJudge(ability, watch);
                putEventAbility(gameData, ability, perm.getCard(), perm, auraOwnerId,
                        ConditionContext.forPermanent(perm, auraOwnerId).withTargetId(damagedPlayerId)
                                .withEventValue(damageDealt), ctx, entry -> {
                            entry.setTargetId(damagedPlayerId);
                            entry.setNonTargeting(true);
                            entry.setEventValue(damageDealt);
                        });
            }
        });
    }

    /**
     * Fires ON_OPPONENT_CREATURE_DEALT_DAMAGE triggers on permanents whose controller
     * is different from the damaged creature's controller (i.e. the damaged creature is
     * an opponent's creature from the perspective of the permanent's controller).
     * Called once per damaged creature — each call produces one trigger per listening permanent.
     */
    public void checkOpponentCreatureDealtDamageTriggers(GameData gameData, UUID damagedCreatureControllerId) {
        DamageWatch watch = DamageWatch.ON_OPPONENT_CREATURE_DEALT_DAMAGE;
        TriggerContext event = new TriggerContext.DamageToControllerAmount(damagedCreatureControllerId, 0);
        gameData.forEachPermanent((playerId, perm) -> {
            // Only fire when the damaged creature was controlled by an opponent of this permanent's controller
            if (playerId.equals(damagedCreatureControllerId)) return;
            for (TriggeredAbility ability : printedWatchedAbilities(gameData, perm, watch)) {
                requireNothingToJudge(ability, watch);
                putEventAbility(gameData, ability, perm.getCard(), perm, playerId,
                        ConditionContext.forPermanent(perm, playerId), event, entry -> {
                        });
            }
        });
    }

    /** Fires turn-scoped global triggers for damage dealt to an opponent's creature. */
    public void checkTemporaryGlobalOpponentCreatureDealtDamageTriggers(
            GameData gameData, Permanent damagedCreature, UUID damagedCreatureControllerId, int damageDealt) {
        if (damagedCreature == null || damagedCreatureControllerId == null || damageDealt <= 0) return;

        for (TemporaryGlobalTriggeredAbility watcher : List.copyOf(gameData.temporaryGlobalTriggeredAbilities)) {
            if (!watcher.ability().hasWatchedTrigger(DamageWatch.ON_OPPONENT_CREATURE_DEALT_DAMAGE.trigger())
                    || watcher.controllerId().equals(damagedCreatureControllerId)) {
                continue;
            }
            enqueueTemporaryGlobalTrigger(gameData, watcher, damagedCreature.getId(), damageDealt);
        }
    }

    /**
     * Fires excess-damage triggers on permanents whose controller is different from the damaged
     * permanent's controller. Triggers from the {@code ON_OPPONENT_CREATURE_OR_PLANESWALKER_DEALT_EXCESS_NONCOMBAT_DAMAGE}
     * slot fire only when {@code noncombatDamage} is true; combat excess damage still records the excess. The record
     * names the permanent dealt the excess damage as the non-targeting "that permanent" and triggering permanent, and
     * the excess as the event value; a target the card declares ("any target other than that permanent", Toralf) is
     * chosen as the ability goes on the stack (CR 603.3d).
     */
    public void checkOpponentPermanentDealtExcessDamageTriggers(GameData gameData,
                                                                Permanent damagedPermanent,
                                                                UUID damagedPermanentControllerId,
                                                                int excessDamage,
                                                                boolean noncombatDamage) {
        if (damagedPermanent == null || damagedPermanentControllerId == null || excessDamage <= 0
                || (!gameQueryService.isCreature(gameData, damagedPermanent)
                && !damagedPermanent.getCard().hasType(CardType.PLANESWALKER))) return;

        gameData.recordControllerOfPermanentDealtExcessDamageThisTurn(damagedPermanentControllerId);

        if (gameQueryService.isCreature(gameData, damagedPermanent)) {
            gameData.recordPermanentDealtExcessDamageThisTurn(damagedPermanent.getId());
        }

        List<DamageWatch> watches = noncombatDamage
                ? List.of(DamageWatch.ON_OPPONENT_CREATURE_OR_PLANESWALKER_DEALT_EXCESS_DAMAGE,
                        DamageWatch.ON_OPPONENT_CREATURE_OR_PLANESWALKER_DEALT_EXCESS_NONCOMBAT_DAMAGE)
                : List.of(DamageWatch.ON_OPPONENT_CREATURE_OR_PLANESWALKER_DEALT_EXCESS_DAMAGE);
        TriggerContext context = new TriggerContext.OpponentPermanentDealtExcessDamage(
                damagedPermanent, damagedPermanentControllerId, excessDamage);
        gameData.forEachPermanent((playerId, perm) -> {
            if (playerId.equals(damagedPermanentControllerId)) return;

            for (DamageWatch watch : watches) {
                List<TriggeredAbility> abilities = new ArrayList<>(printedWatchedAbilities(gameData, perm, watch));
                abilities.addAll(WatchedAbilities.temporary(perm, watch.trigger()));
                abilities.addAll(WatchedAbilities.persistent(perm, watch.trigger()));
                for (TriggeredAbility ability : abilities) {
                    requireNothingToJudge(ability, watch);
                    putEventAbility(gameData, ability, perm.getCard(), perm, playerId,
                            ConditionContext.forPermanent(perm, playerId).withTargetId(damagedPermanent.getId())
                                    .withTriggeringPermanentId(damagedPermanent.getId())
                                    .withEventValue(excessDamage), context, entry -> {
                                entry.setTargetId(damagedPermanent.getId());
                                entry.setNonTargeting(true);
                                entry.setEventValue(excessDamage);
                                entry.setTriggeringPermanentId(damagedPermanent.getId());
                                entry.setTriggeringPermanentControllerId(damagedPermanentControllerId);
                            });
                }
            }
        });
    }

    public void checkOpponentCreatureDealtExcessNoncombatDamageTriggers(GameData gameData,
                                                                          StackEntry damageEntry,
                                                                          Permanent damagedCreature,
                                                                          UUID damagedCreatureControllerId,
                                                                          int excessDamage) {
        if (damageEntry == null || damagedCreature == null || damagedCreatureControllerId == null
                || excessDamage <= 0 || !gameQueryService.isCreature(gameData, damagedCreature)) return;

        DamageWatch watch = DamageWatch.ON_OPPONENT_CREATURE_DEALT_EXCESS_NONCOMBAT_DAMAGE;
        TriggerContext context = new TriggerContext.OpponentPermanentDealtExcessDamage(
                damagedCreature, damagedCreatureControllerId, excessDamage);
        gameData.forEachPermanent((playerId, perm) -> {
            if (playerId.equals(damagedCreatureControllerId)) return;

            List<TriggeredAbility> abilities = new ArrayList<>(printedWatchedAbilities(gameData, perm, watch));
            abilities.addAll(WatchedAbilities.temporary(perm, watch.trigger()));
            abilities.addAll(WatchedAbilities.persistent(perm, watch.trigger()));
            // Each ability, once for the damage event ("one or more creatures"), marked by its place among the
            // permanent's abilities of the watch.
            for (int abilityIndex = 0; abilityIndex < abilities.size(); abilityIndex++) {
                TriggeredAbility ability = abilities.get(abilityIndex);
                requireNothingToJudge(ability, watch);
                if (!damageEntry.markNoncombatExcessDamageTriggerFired(perm.getId(), abilityIndex)) {
                    continue;
                }
                putEventAbility(gameData, ability, perm.getCard(), perm, playerId,
                        ConditionContext.forPermanent(perm, playerId).withTargetId(damagedCreature.getId()),
                        context, entry -> {
                            entry.setTargetId(damagedCreature.getId());
                            entry.setNonTargeting(true);
                        });
            }
        });
    }

    /**
     * Collects the "deals damage to a creature" abilities (the four {@link DamageWatch} {@code …DEALS_DAMAGE_TO_CREATURE}
     * watches) for one creature dealing damage to one creature: the abilities other permanents watch, then the damage
     * source's own. Called once per source/target/damage event; {@code combatDamage} says whether it was combat damage
     * for the combat-only triggers ({@link DamageTrigger#combatOnly()}).
     */
    public void checkAllyDealtDamageToCreatureTriggers(GameData gameData, Permanent damageSource,
            UUID damageSourceControllerId, UUID damagedCreatureControllerId, UUID damagedCreatureId,
            Permanent damagedCreature, int damage, boolean combatDamage) {
        checkWatchersDealtDamageToCreatureTriggers(gameData, damageSource, damageSourceControllerId,
                damagedCreatureControllerId, damagedCreatureId, damagedCreature, damage, combatDamage);
        checkSelfDealtDamageToCreatureTriggers(gameData, damageSource, damageSourceControllerId,
                damagedCreatureControllerId, damagedCreatureId, damagedCreature, damage, combatDamage);
    }

    /**
     * Collects the "deals damage to a creature" abilities of the permanents other than the damage source: "a creature
     * you control" on its controller's permanents, "equipped creature" on the Equipment attached to it (whoever
     * controls that Equipment) and "a creature" on every permanent. The source's own abilities are collected by
     * {@link #checkSelfDealtDamageToCreatureTriggers}.
     */
    public void checkWatchersDealtDamageToCreatureTriggers(GameData gameData, Permanent damageSource,
            UUID damageSourceControllerId, UUID damagedCreatureControllerId, UUID damagedCreatureId,
            Permanent damagedCreature, int damage, boolean combatDamage) {
        if (damageSource == null || damageSourceControllerId == null || damagedCreatureControllerId == null
                || damagedCreatureId == null || damage <= 0) {
            return;
        }
        DealsDamageToCreatureEvent event = new DealsDamageToCreatureEvent(damageSource, damageSourceControllerId,
                damagedCreatureId, damagedCreatureControllerId, damagedCreature, damage, combatDamage);
        gameData.forEachPermanent((watcherControllerId, watcher) -> {
            // The damage source watches itself below, whether or not it survived the damage.
            if (watcher.getId().equals(damageSource.getId())) return;
            List<DamageWatch> watches = new ArrayList<>(3);
            if (watcherControllerId.equals(damageSourceControllerId)) {
                watches.add(DamageWatch.ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE);
            }
            if (isEquippedBy(gameData, watcher, damageSource)) {
                watches.add(DamageWatch.ON_EQUIPPED_CREATURE_DEALS_DAMAGE_TO_CREATURE);
            }
            watches.add(DamageWatch.ON_ANY_CREATURE_DEALS_DAMAGE_TO_CREATURE);
            collectDealsDamageToCreatureAbilities(gameData, watcher, watcherControllerId, watches, false, event);
        });
    }

    /**
     * Collects the damage source's own "deals damage to a creature" abilities ("this creature", and "a creature you
     * control" and "a creature", which include it), the ones it has been granted among them. Combat damage calls this
     * before state-based actions so the grant is still present when lethal damage back from the blocker or a granting
     * permanent dying in the same step has already moved the source off the battlefield.
     */
    public void checkSelfDealtDamageToCreatureTriggers(GameData gameData, Permanent damageSource,
            UUID damageSourceControllerId, UUID damagedCreatureControllerId, UUID damagedCreatureId,
            Permanent damagedCreature, int damage, boolean combatDamage) {
        if (damageSource == null || damageSourceControllerId == null || damagedCreatureControllerId == null
                || damagedCreatureId == null || damage <= 0) {
            return;
        }
        collectDealsDamageToCreatureAbilities(gameData, damageSource, damageSourceControllerId,
                List.of(DamageWatch.ON_SELF_DEALS_DAMAGE_TO_CREATURE,
                        DamageWatch.ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE,
                        DamageWatch.ON_ANY_CREATURE_DEALS_DAMAGE_TO_CREATURE), true,
                new DealsDamageToCreatureEvent(damageSource, damageSourceControllerId, damagedCreatureId,
                        damagedCreatureControllerId, damagedCreature, damage, combatDamage));
    }

    /** Fires triggers watching a creature controlled by a player's creatures fighting. */
    public void checkAllyCreatureFightsTriggers(GameData gameData, Permanent fightingCreature,
                                                 UUID fightingCreatureControllerId) {
        if (fightingCreature == null || fightingCreatureControllerId == null) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(fightingCreatureControllerId);
        if (battlefield != null) {
            for (Permanent watcher : new ArrayList<>(battlefield)) {
                if (watcher.getId().equals(fightingCreature.getId())) continue;
                dispatchWatch(gameData, watcher, fightingCreatureControllerId,
                        ActionWatch.ON_ALLY_CREATURE_FIGHTS,
                        new TriggerContext.CreatureFights(fightingCreature));
            }
        }

        // A fighting creature's own trigger still triggers if it dies in the fight.
        dispatchWatch(gameData, fightingCreature, fightingCreatureControllerId,
                ActionWatch.ON_ALLY_CREATURE_FIGHTS,
                new TriggerContext.CreatureFights(fightingCreature));
    }

    public void checkAllyDealtDamageToPlaneswalkerTriggers(GameData gameData, Permanent damageSource,
            UUID damageSourceControllerId, UUID damagedPlaneswalkerId, int damage, boolean combatDamage) {
        checkCreatureDealsDamageToEnchantedPlaneswalkerTriggers(
                gameData, damageSource, damagedPlaneswalkerId, damage, combatDamage);
        if (damageSource == null || damageSourceControllerId == null || damagedPlaneswalkerId == null || damage <= 0) {
            return;
        }
        Permanent planeswalker = gameQueryService.findPermanentById(gameData, damagedPlaneswalkerId);
        if (planeswalker == null || !planeswalker.getCard().hasType(CardType.PLANESWALKER)
                || !gameQueryService.isCreature(gameData, damageSource)) return;

        TriggerContext context = new TriggerContext.CreatureDealsDamageToPlaneswalker(
                damageSource, damagedPlaneswalkerId, damage, combatDamage);
        // "Deals combat damage to a planeswalker" (Zagras, Thief of Heartbeats) only for combat damage.
        List<DamageWatch> watches = combatDamage
                ? List.of(DamageWatch.ON_ALLY_CREATURE_DEALS_DAMAGE_TO_PLANESWALKER,
                        DamageWatch.ON_ALLY_CREATURE_DEALS_COMBAT_DAMAGE_TO_PLANESWALKER)
                : List.of(DamageWatch.ON_ALLY_CREATURE_DEALS_DAMAGE_TO_PLANESWALKER);
        gameData.forEachPermanent((watcherControllerId, watcher) -> {
            if (!watcherControllerId.equals(damageSourceControllerId)) return;
            for (DamageWatch toPlaneswalker : watches) {
                List<TriggeredAbility> abilities = new ArrayList<>(
                        DamageAbilityReader.printed(watcher.getCard(), toPlaneswalker));
                abilities.addAll(WatchedAbilities.temporary(watcher, toPlaneswalker.trigger()));
                abilities.addAll(WatchedAbilities.persistent(watcher, toPlaneswalker.trigger()));
                for (TriggeredAbility ability : abilities) {
                    requireEachEvent(ability, toPlaneswalker);
                    // "Whenever a [filtered] creature you control deals damage to a planeswalker": on the creature.
                    PermanentPredicate filter = DamageAbilityReader.filter(ability, toPlaneswalker.trigger());
                    if (filter != null && !damageFilterMatches(gameData, watcher, watcherControllerId, filter,
                            damageSource)) {
                        continue;
                    }
                    // "Destroy that planeswalker" (Zagras): the planeswalker is the non-targeting "that
                    // planeswalker", the creature the triggering permanent.
                    putEventAbility(gameData, ability, watcher.getCard(), watcher, watcherControllerId,
                            ConditionContext.forPermanent(watcher, watcherControllerId)
                                    .withTriggeringPermanentId(damageSource.getId()).withEventValue(damage), context,
                            entry -> {
                                entry.setTargetId(damagedPlaneswalkerId);
                                entry.setTriggeringPermanentId(damageSource.getId());
                                entry.setNonTargeting(true);
                            });
                }
            }
        });
    }

    /**
     * "Whenever a creature deals damage to enchanted planeswalker, …": the creature is the non-targeting "that
     * creature" and the triggering permanent of each such ability of an Aura attached to the planeswalker.
     */
    public void checkCreatureDealsDamageToEnchantedPlaneswalkerTriggers(GameData gameData, Permanent damageSource,
            UUID damagedPlaneswalkerId, int damage, boolean combatDamage) {
        if (damageSource == null || damagedPlaneswalkerId == null || damage <= 0
                || !gameQueryService.isCreature(gameData, damageSource)) {
            return;
        }
        Permanent planeswalker = gameQueryService.findPermanentById(gameData, damagedPlaneswalkerId);
        if (planeswalker == null || !planeswalker.getCard().hasType(CardType.PLANESWALKER)) {
            return;
        }

        TriggerContext context = new TriggerContext.CreatureDealsDamageToPlaneswalker(
                damageSource, damagedPlaneswalkerId, damage, combatDamage);
        DamageWatch watch = DamageWatch.ON_CREATURE_DEALS_DAMAGE_TO_ENCHANTED_PLANESWALKER;
        gameData.forEachPermanent((watcherControllerId, watcher) -> {
            if (!damagedPlaneswalkerId.equals(watcher.getAttachedTo())) return;
            List<TriggeredAbility> abilities = new ArrayList<>(DamageAbilityReader.printed(watcher.getCard(), watch));
            abilities.addAll(WatchedAbilities.temporary(watcher, watch.trigger()));
            abilities.addAll(WatchedAbilities.persistent(watcher, watch.trigger()));
            abilities.addAll(grantedTriggeredAbilitySupport.grantedWatchedAbilities(gameData, watcher,
                    watcherControllerId, watch.trigger()));
            for (TriggeredAbility ability : abilities) {
                requireNothingToJudge(ability, watch);
                putEventAbility(gameData, ability, watcher.getCard(), watcher, watcherControllerId,
                        ConditionContext.forPermanent(watcher, watcherControllerId)
                                .withTriggeringPermanentId(damageSource.getId()).withEventValue(damage), context,
                        entry -> {
                            entry.setTargetId(damageSource.getId());
                            entry.setTriggeringPermanentId(damageSource.getId());
                            entry.setNonTargeting(true);
                        });
            }
        });
    }

    public void queueSourceDealsCombatDamageToPlayerOrPlaneswalkerTriggers(GameData gameData, Card sourceCard,
                                                                            UUID sourceControllerId,
                                                                            UUID sourcePermanentId,
                                                                            int totalDamage,
                                                                            int damageToPlayers,
                                                                            List<TriggeredAbility> snapshottedAbilities) {
        if (sourceCard == null || sourceControllerId == null || sourcePermanentId == null || totalDamage <= 0) return;
        dispatchSourceDealsCombatDamageTriggers(gameData, sourceCard, sourceControllerId, sourcePermanentId,
                totalDamage, damageToPlayers, snapshottedAbilities,
                CombatWatch.ON_SELF_DEALS_COMBAT_DAMAGE_TO_PLAYER_OR_PLANESWALKER);
    }

    public void queueSourceDealsCombatDamageToPlayerOrBattleTriggers(GameData gameData, Card sourceCard,
                                                                       UUID sourceControllerId,
                                                                       UUID sourcePermanentId,
                                                                       int totalDamage,
                                                                       int damageToPlayers,
                                                                       List<TriggeredAbility> snapshottedAbilities) {
        if (sourceCard == null || sourceControllerId == null || sourcePermanentId == null || totalDamage <= 0) return;
        dispatchSourceDealsCombatDamageTriggers(gameData, sourceCard, sourceControllerId, sourcePermanentId,
                totalDamage, damageToPlayers, snapshottedAbilities,
                CombatWatch.ON_SELF_DEALS_COMBAT_DAMAGE_TO_PLAYER_OR_BATTLE);
        queueEquippedCreatureDealsCombatDamageToPlayerOrBattleTriggers(
                gameData, sourceCard, sourceControllerId, sourcePermanentId, totalDamage, damageToPlayers);
    }

    /**
     * Hands the damage source's own "whenever this deals combat damage [to a player or planeswalker / battle]"
     * abilities to the stack door: its printed ones and those granted to it ({@code snapshottedAbilities}, as the
     * combat damage step read them as the damage was dealt, else as it has them now). The source may have died dealing
     * the damage: the ability keeps its last-known permanent as its source (CR 608.2h), and the damage dealt is its
     * event value. A "choose one" announces its mode (CR 603.3c) and a target is chosen (CR 603.3d) as it goes on the
     * stack.
     */
    private void dispatchSourceDealsCombatDamageTriggers(GameData gameData, Card sourceCard,
                                                         UUID sourceControllerId, UUID sourcePermanentId,
                                                         int totalDamage, int damageToPlayers,
                                                         List<TriggeredAbility> snapshottedAbilities,
                                                         CombatWatch watch) {
        var ctx = new TriggerContext.SourceDealsCombatDamage(
                sourceCard, sourceControllerId, sourcePermanentId, totalDamage, damageToPlayers);
        List<TriggeredAbility> abilities = new ArrayList<>(WatchedAbilities.printed(sourceCard, watch));
        Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (snapshottedAbilities != null) {
            // The granted abilities as the combat damage step snapshotted them.
            abilities.addAll(snapshottedAbilities);
        } else if (sourcePermanent != null) {
            abilities.addAll(WatchedAbilities.temporary(sourcePermanent, watch.trigger()));
            abilities.addAll(WatchedAbilities.persistent(sourcePermanent, watch.trigger()));
        }
        Permanent abilitySource = sourcePermanent != null ? sourcePermanent
                : lastKnownSource(gameData, sourceCard, sourcePermanentId);
        ConditionContext context = sourcePermanent != null
                ? ConditionContext.forPermanent(sourcePermanent, sourceControllerId)
                : ConditionContext.forCard(sourceCard, sourceControllerId);
        for (TriggeredAbility ability : abilities) {
            requirePlainCombatAbility(ability, watch);
            putEventAbility(gameData, ability, sourceCard, abilitySource, sourceControllerId,
                    context.withEventValue(totalDamage), ctx, entry -> entry.setEventValue(totalDamage));
        }
        for (TemporaryGlobalTriggeredAbility watcher : List.copyOf(gameData.temporaryGlobalTriggeredAbilities)) {
            if (!watcher.ability().hasWatchedTrigger(watch.trigger())
                    || !sourcePermanentId.equals(watcher.watchedPermanentId())) continue;
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    watcher.sourceCard(),
                    watcher.controllerId(),
                    watcher.sourceCard().getName() + "'s delayed trigger",
                    new ArrayList<>(CombatAbilityReader.effects(List.of(watcher.ability()), watch.trigger())),
                    null,
                    sourcePermanentId);
            entry.setEventValue(totalDamage);
            entry.setNonTargeting(true);
            gameData.enqueueTrigger(entry);
            gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
        }
    }

    /**
     * One creature dealing damage to one creature, as the "deals damage to a creature" collector reads it.
     *
     * @param damageSource                the creature that dealt the damage
     * @param damageSourceControllerId    its controller as it dealt the damage
     * @param damagedCreatureId           the creature dealt the damage
     * @param damagedCreatureControllerId that creature's controller as it was dealt the damage
     * @param damagedCreature             that creature, or its snapshot before state-based actions; may be {@code null}
     * @param damage                      the damage dealt
     * @param combatDamage                whether it was combat damage
     */
    private record DealsDamageToCreatureEvent(Permanent damageSource, UUID damageSourceControllerId,
                                              UUID damagedCreatureId, UUID damagedCreatureControllerId,
                                              Permanent damagedCreature, int damage, boolean combatDamage) {
    }

    /**
     * Collects {@code watcher}'s abilities of {@code watches} for {@code event}: the printed ones (none while face down,
     * CR 708.2), the ones granted until a time or until it leaves, and, when {@code readGrants} (the damage source's
     * own), the ones a static ability grants it, unless it has lost its abilities. Each ability triggers when its
     * trigger's filters match (the creature that dealt the damage, the creature dealt it, combat only) and its event
     * condition holds, judged with the creature dealt the damage as the context's triggering permanent (Maarika's "if
     * that creature was dealt excess damage this turn"), and goes to the stack door whole (CR 603.3): the creature dealt
     * the damage is its triggering permanent (with that creature's controller as it was dealt the damage, for "that
     * creature's controller" once it has left), the creature that dealt it its event damage source (for "that Archer
     * deals …"), the damage its event value; the effects read them as they resolve ("destroy that creature", "tap that
     * creature", "that much").
     */
    private void collectDealsDamageToCreatureAbilities(GameData gameData, Permanent watcher, UUID controllerId,
            List<DamageWatch> watches, boolean readGrants, DealsDamageToCreatureEvent event) {
        if (watcher.isLosesAllAbilitiesUntilEndOfTurn() || gameQueryService.hasLostPrintedAbilities(gameData, watcher)) {
            return;
        }
        ConditionContext context = ConditionContext.forPermanent(watcher, controllerId)
                .withTriggeringPermanentId(event.damagedCreatureId()).withEventValue(event.damage());
        TriggerContext triggerContext = new TriggerContext.CreatureDealsDamageToCreature(
                event.damageSource(), event.damagedCreatureId(), event.damage(), event.combatDamage(),
                event.damagedCreature(), event.damagedCreatureControllerId());
        Permanent snapshot = new Permanent(watcher);
        for (DamageWatch watch : watches) {
            Trigger watched = watch.trigger();
            List<TriggeredAbility> abilities = watcher.isFaceDown()
                    ? new ArrayList<>()
                    : new ArrayList<>(DamageAbilityReader.printed(watcher.getCard(), watch));
            abilities.addAll(WatchedAbilities.temporary(watcher, watched));
            abilities.addAll(WatchedAbilities.persistent(watcher, watched));
            if (readGrants) {
                abilities.addAll(grantedTriggeredAbilitySupport.grantedWatchedAbilities(
                        gameData, watcher, controllerId, watched));
            }
            for (TriggeredAbility ability : abilities) {
                requireEachEvent(ability, watch);
                DamageTrigger trigger = DamageAbilityReader.trigger(ability, watched);
                if (trigger.combatOnly() && !event.combatDamage()) continue;
                if (trigger.filter() != null && !damageFilterMatches(gameData, watcher, controllerId,
                        trigger.filter(), event.damageSource())) {
                    continue;
                }
                if (trigger.damagedFilter() != null && !damageFilterMatches(gameData, watcher, controllerId,
                        trigger.damagedFilter(), damagedCreatureOf(gameData, event))) {
                    continue;
                }
                putEventAbility(gameData, ability, watcher.getCard(), watcher, controllerId, context, triggerContext,
                        entry -> {
                            entry.setNonTargeting(true);
                            entry.setEventValue(event.damage());
                            entry.setTriggeringPermanentId(event.damagedCreatureId());
                            entry.setTriggeringPermanentControllerId(event.damagedCreatureControllerId());
                            entry.setEventDamageSource(event.damageSource(), event.damageSourceControllerId());
                            entry.setSourcePermanentSnapshot(snapshot);
                        });
            }
        }
    }

    /** The creature {@code event} dealt damage to, as it was dealt the damage, or as it is now. */
    private Permanent damagedCreatureOf(GameData gameData, DealsDamageToCreatureEvent event) {
        return event.damagedCreature() != null
                ? event.damagedCreature()
                : gameQueryService.findPermanentById(gameData, event.damagedCreatureId());
    }

    private boolean isEquippedBy(GameData gameData, Permanent equipment, Permanent host) {
        return isEquippedBy(gameData, equipment, host.getId());
    }

    private boolean isEquippedBy(GameData gameData, Permanent equipment, UUID hostId) {
        if (hostId.equals(equipment.getAttachedTo())) return true;
        // Only a host that already left the battlefield may be matched on last-known attachment.
        return hostId.equals(equipment.getLastAttachedTo())
                && gameQueryService.findPermanentById(gameData, hostId) == null;
    }

    // ── Any-creature-dealt-damage triggers ─────────────────────────────

    /**
     * Fires ON_ANY_CREATURE_DEALT_DAMAGE triggers on every permanent with that slot, regardless of
     * who controls the damaged creature. Called once per damaged creature.
     */
    public void checkAnyCreatureDealtDamageTriggers(GameData gameData, Permanent damagedCreature,
                                                    int damageDealt) {
        UUID damagedCreatureControllerId = gameQueryService.findPermanentController(gameData, damagedCreature.getId());
        checkAnyCreatureDealtDamageTriggers(gameData, damagedCreature, damagedCreatureControllerId, damageDealt);
    }

    /**
     * Variant for combat, where the damaged creature may already have left the battlefield by the
     * time its trigger is queued and its controller was captured during damage processing. "Whenever a [filtered]
     * creature is dealt damage, …", whatever the effects are: the record names the damaged creature as its triggering
     * permanent with its controller as it was dealt the damage and its last known information (CR 608.2h), the damage
     * as its event value ("that much") and the source's snapshot, and the creature as the non-targeting "that
     * creature"; the door marks "do this only once each turn" as the action is taken (CR 603.2h). A targeted ability,
     * "you may" or not, chooses its target as it goes on the stack (CR 603.3d; the "you may" is answered as it
     * resolves, CR 603.5).
     */
    public void checkAnyCreatureDealtDamageTriggers(GameData gameData, Permanent damagedCreature,
                                                    UUID damagedCreatureControllerId, int damageDealt) {
        if (damagedCreatureControllerId == null || damageDealt <= 0) return;

        var ctx = new TriggerContext.AnyCreatureDealtDamage(
                damagedCreature, damagedCreatureControllerId, damageDealt);
        DamageWatch creatureDealtDamage = DamageWatch.ON_ANY_CREATURE_DEALT_DAMAGE;
        Trigger watched = creatureDealtDamage.trigger();
        gameData.forEachPermanent((playerId, perm) -> {
            if (gameQueryService.hasLostAllAbilities(gameData, perm)) return;
            for (TriggeredAbility ability : WatchedAbilities.printed(LayerSystemService.currentCard(gameData, perm),
                    creatureDealtDamage)) {
                requireEachEvent(ability, creatureDealtDamage);
                // "Whenever a [filtered] creature is dealt damage": the filter is on the damaged creature.
                PermanentPredicate filter = DamageAbilityReader.filter(ability, watched);
                if (filter != null && !damageFilterMatches(gameData, perm, playerId, filter, damagedCreature)) {
                    continue;
                }
                putEventAbility(gameData, ability, perm.getCard(), perm, playerId,
                        ConditionContext.forPermanent(perm, playerId)
                                .withTriggeringPermanentId(damagedCreature.getId()).withEventValue(damageDealt), ctx,
                        damagedPermanentRecord(gameData, perm, damagedCreature, damagedCreatureControllerId,
                                damageDealt, false, true));
            }
        });

        collectTemporaryGlobalTriggers(gameData, DamageWatch.ON_ANY_CREATURE_DEALT_DAMAGE,
                damagedCreature.getId(), damageDealt);

        for (DelayedWatchedCreatureDealtDamage watch
                : gameData.getDelayedActions(DelayedWatchedCreatureDealtDamage.class)) {
            if (!watch.watchedPermanentId().equals(damagedCreature.getId())) {
                continue;
            }

            StackEntry trigger = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    watch.sourceCard(),
                    watch.controllerId(),
                    watch.sourceCard().getName() + "'s delayed trigger",
                    new ArrayList<>(watch.effects()),
                    (UUID) null,
                    watch.watchedPermanentId());
            trigger.setNonTargeting(true);
            trigger.setEventValue(damageDealt);
            trigger.setDamageSourceCard(damagedCreature.getCard());
            trigger.setDamageSourceControllerId(
                    gameQueryService.findPermanentController(gameData, damagedCreature.getId()));
            trigger.setSourcePermanentSnapshot(new Permanent(damagedCreature));
            gameData.enqueueTrigger(trigger);
            gameLogService.append(gameData, GameLog.abilityTriggers(watch.sourceCard()));
            log.info("Game {} - {} delayed trigger fires after damage to watched creature",
                    gameData.id, watch.sourceCard().getName());
        }
    }

    /**
     * Fires ON_ANY_PERMANENT_DEALT_DAMAGE triggers once for the damaged permanent: "whenever a permanent you control is
     * dealt damage, …" ("a Dinosaur you control", "this or a creature it's paired with": the trigger's filter on the
     * damaged permanent, the relation judged here). The ability is the watcher's (CR 113.7); its record names the
     * damaged permanent with its power ({@link #damagedPermanentRecord}).
     */
    public void checkAnyPermanentDealtDamageTriggers(GameData gameData, Permanent damagedPermanent,
                                                     int damageDealt) {
        if (damagedPermanent == null || damageDealt <= 0) return;

        UUID damagedPermanentControllerId = gameQueryService.findPermanentController(
                gameData, damagedPermanent.getId());
        if (damagedPermanentControllerId == null) return;

        var ctx = new TriggerContext.AnyPermanentDealtDamage(
                damagedPermanent, damagedPermanentControllerId, damageDealt);
        DamageWatch watch = DamageWatch.ON_ANY_PERMANENT_DEALT_DAMAGE;
        Trigger watched = watch.trigger();
        gameData.forEachPermanent((playerId, perm) -> {
            // "A permanent you control" (the trigger's relation): the damaged permanent's controller is the watcher's.
            if (!playerId.equals(damagedPermanentControllerId)) return;
            for (TriggeredAbility ability : watchedAbilities(gameData, perm, watch)) {
                requireEachEvent(ability, watch);
                // "Whenever a [filtered] permanent you control is dealt damage": the filter is on the damaged permanent.
                PermanentPredicate filter = DamageAbilityReader.filter(ability, watched);
                if (filter != null && !damageFilterMatches(gameData, perm, playerId, filter, damagedPermanent)) {
                    continue;
                }
                putEventAbility(gameData, ability, perm.getCard(), perm, playerId,
                        ConditionContext.forPermanent(perm, playerId)
                                .withTriggeringPermanentId(damagedPermanent.getId()).withEventValue(damageDealt), ctx,
                        damagedPermanentRecord(gameData, perm, damagedPermanent, damagedPermanentControllerId,
                                damageDealt, true, false));
            }
        });
    }

    public void checkTemporaryGlobalCreatureAttackTriggers(GameData gameData, Permanent attacker) {
        for (TemporaryGlobalTriggeredAbility watcher : List.copyOf(gameData.temporaryGlobalTriggeredAbilities)) {
            Trigger anyAttacks = CombatWatch.ON_ANY_CREATURE_ATTACKS.trigger();
            if (!watcher.ability().hasWatchedTrigger(anyAttacks)) continue;
            CombatAbilityReader.CombatEffect read = CombatAbilityReader.read(watcher.ability(), anyAttacks).getFirst();
            CombatAbilityReader.requireNoLimitOrMultiplicity(read, anyAttacks);
            // "Whenever a [filter] creature [you control] attacks this turn": the trigger's filter on the attacker, from
            // the registering player's point of view.
            if (read.filter() != null && !predicateEvaluationService.matchesPermanentPredicate(attacker, read.filter(),
                    FilterContext.of(gameData).withSourceControllerId(watcher.controllerId())
                            .withSourceCardId(watcher.sourceCard().getId()))) {
                continue;
            }
            // "Attacks alone" (The Last Ronin) is the ability's event condition (CR 603.2), with the attacker.
            if (!conditionEvaluationService.triggerConditionsMet(gameData, read.options(),
                    ConditionContext.forPermanent(attacker, watcher.controllerId()))) {
                continue;
            }
            CardEffect triggeredEffect = read.effect();
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, watcher.sourceCard(),
                    watcher.controllerId(), watcher.sourceCard().getName() + "'s ability",
                    new ArrayList<>(List.of(triggeredEffect)), null, attacker.getId());
            entry.setTargetId(attacker.getId());
            entry.setSourcePermanentSnapshot(new Permanent(attacker));
            entry.setNonTargeting(true);
            gameData.handOffTriggerRecord(entry);
            if (triggeredEffect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                    || triggeredEffect.targetSpec().admits(TargetPredicate.Kind.PLAYER)) {
                gameData.queueInteraction(new PermanentChoiceContext.AttackTriggerTarget(
                        watcher.sourceCard(), watcher.controllerId(),
                        new ArrayList<>(List.of(triggeredEffect)), null));
                gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
                continue;
            }
            gameData.enqueueTrigger(entry);
            gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
        }
    }

    /** Fires temporary global triggers watching a creature attack the registering player's
     * battlefield, either directly or through one of that player's planeswalkers. A watcher bound to
     * a permanent ("attacks you or Tasha") only fires for attacks on the player or on that permanent. */
    public void checkTemporaryGlobalCreatureAttacksYouTriggers(GameData gameData, Permanent attacker,
                                                               UUID attackedPlayerId, UUID attackedTargetId) {
        if (attacker == null || attackedPlayerId == null) {
            return;
        }
        for (TemporaryGlobalTriggeredAbility watcher : List.copyOf(gameData.temporaryGlobalTriggeredAbilities)) {
            if (!watcher.ability().hasWatchedTrigger(CombatWatch.ON_CREATURE_ATTACKS_YOU.trigger())
                    || !watcher.controllerId().equals(attackedPlayerId)) {
                continue;
            }
            if (watcher.watchedPermanentId() != null
                    && !watcher.watchedPermanentId().equals(attackedTargetId)
                    && !attackedPlayerId.equals(attackedTargetId)) {
                continue;
            }

            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    watcher.sourceCard(),
                    watcher.controllerId(),
                    watcher.sourceCard().getName() + "'s ability",
                    new ArrayList<>(CombatAbilityReader.effects(List.of(watcher.ability()),
                            CombatWatch.ON_CREATURE_ATTACKS_YOU.trigger())),
                    null,
                    attacker.getId());
            entry.setTargetId(attacker.getId());
            entry.setTriggeringPermanentId(attacker.getId());
            entry.setSourcePermanentSnapshot(new Permanent(attacker));
            entry.setNonTargeting(true);
            gameData.enqueueTrigger(entry);
            gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
            log.info("Game {} - {} temporary attacks-you trigger fires for {} attacking",
                    gameData.id, watcher.sourceCard().getName(), attacker.getCard().getName());
        }
    }

    /** Fires temporary triggers that watch one or more creatures controlled by their controller attack. */
    public void checkTemporaryGlobalAllyCreatureAttackTriggers(GameData gameData, UUID attackingPlayerId) {
        for (TemporaryGlobalTriggeredAbility watcher : List.copyOf(gameData.temporaryGlobalTriggeredAbilities)) {
            if (!watcher.ability().hasWatchedTrigger(CombatWatch.ON_ALLY_CREATURES_ATTACK.trigger())
                    || !watcher.controllerId().equals(attackingPlayerId)) {
                continue;
            }

            CardEffect triggeredEffect = CombatAbilityReader.effects(List.of(watcher.ability()),
                    CombatWatch.ON_ALLY_CREATURES_ATTACK.trigger()).getFirst();
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    watcher.sourceCard(), watcher.controllerId(),
                    watcher.sourceCard().getName() + "'s ability",
                    new ArrayList<>(List.of(triggeredEffect)));
            entry.setNonTargeting(true);
            gameData.handOffTriggerRecord(entry);
            if (triggeredEffect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                    || triggeredEffect.targetSpec().admits(TargetPredicate.Kind.PLAYER)) {
                gameData.queueInteraction(new PermanentChoiceContext.AttackTriggerTarget(
                        watcher.sourceCard(), watcher.controllerId(),
                        new ArrayList<>(List.of(triggeredEffect)), null));
            } else {
                gameData.enqueueTrigger(entry);
            }
            gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
            log.info("Game {} - {} temporary ally attack trigger fires",
                    gameData.id, watcher.sourceCard().getName());
        }
    }

    public void checkBendingTriggers(GameData gameData, UUID bendingPlayerId, BendingType type) {
        if (bendingPlayerId == null || type == null) {
            return;
        }
        gameData.recordBending(bendingPlayerId, type);
        TriggerContext context = new TriggerContext.Bending(bendingPlayerId, type);
        List<Permanent> battlefield = gameData.playerBattlefields.get(bendingPlayerId);
        if (battlefield == null) {
            return;
        }
        for (Permanent permanent : List.copyOf(battlefield)) {
            dispatchWatch(gameData, permanent, bendingPlayerId, ActionWatch.ON_CONTROLLER_BENDS, context);
        }
    }

    /** Records that {@code exertedCreatureId} was exerted this turn and fires its controller's exert triggers. */
    public void checkExertTriggers(GameData gameData, UUID exertingPlayerId, UUID exertedCreatureId) {
        if (exertingPlayerId == null || exertedCreatureId == null) {
            return;
        }
        gameData.permanentsExertedThisTurn.add(exertedCreatureId);
        List<Permanent> battlefield = gameData.playerBattlefields.get(exertingPlayerId);
        if (battlefield == null) {
            return;
        }
        TriggerContext context = new TriggerContext.Exert(exertingPlayerId, exertedCreatureId);
        for (Permanent permanent : List.copyOf(battlefield)) {
            dispatchWatch(gameData, permanent, exertingPlayerId, ActionWatch.ON_CONTROLLER_EXERTS, context);
        }
    }

    /**
     * "Whenever a creature you control attacking causes a triggered ability of that creature to trigger" (Firebender
     * Ascension, {@code Triggers.attackingCreatureYouControlAbilityTriggers()}) for {@code triggeredAbility}, an ability
     * of {@code attacker} that its attack made trigger. Each such ability of the attacker's controller's permanents goes
     * on the stack as written, one object of its effects (CR 603.3), above the ability that made it trigger, with
     * the attacker as its triggering permanent and a snapshot of that ability as its triggering spell snapshot: "copy
     * that ability" ({@code CopyTriggeringSpellOrAbilityEffect}) copies it as it was, even after it left the stack. A
     * "you may" is asked as the ability resolves (CR 603.5). A permanent that lost its abilities has none; an ability
     * with a filter, target, limit or multiplicity throws (no card has one).
     */
    public void checkAttackingCreatureTriggeredAbilityTriggers(GameData gameData, Permanent attacker,
                                                                StackEntry triggeredAbility) {
        if (attacker == null || triggeredAbility == null) {
            return;
        }
        UUID controllerId = gameQueryService.findPermanentController(gameData, attacker.getId());
        if (controllerId == null) {
            return;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return;
        }
        Trigger watched = CombatWatch.ON_ALLY_ATTACKING_CREATURE_ABILITY_TRIGGERS.trigger();
        for (Permanent watcher : List.copyOf(battlefield)) {
            List<TriggeredAbility> abilities = new ArrayList<>();
            if (!gameQueryService.hasLostAllAbilities(gameData, watcher)) {
                if (!watcher.isFaceDown() && !gameQueryService.hasLostPrintedAbilities(gameData, watcher)) {
                    abilities.addAll(WatchedAbilities.printed(watcher.getCard(),
                            CombatWatch.ON_ALLY_ATTACKING_CREATURE_ABILITY_TRIGGERS));
                }
                abilities.addAll(grantedTriggeredAbilitySupport.grantedWatchedAbilities(gameData, watcher, watched));
            }
            for (List<CombatAbilityReader.CombatEffect> ability : CombatAbilityReader.readByAbility(abilities,
                    watched)) {
                CombatAbilityReader.CombatEffect first = ability.getFirst();
                CombatAbilityReader.requireNoLimitOrMultiplicity(first, watched);
                if (first.filter() != null) {
                    throw new IllegalStateException("The attacking-creature ability reader judges no filter: "
                            + first.ability());
                }
                ConditionContext context = ConditionContext.forPermanent(watcher, controllerId)
                        .withTriggeringPermanentId(attacker.getId());
                if (!conditionEvaluationService.eventConditionMet(gameData, first.options(), context)) {
                    continue;
                }
                // The attacker and the ability it triggered ("copy that ability") are the event's, whatever the
                // effects are.
                boolean triggered = triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(
                        first.ability(), watcher.getCard(), watcher, controllerId,
                        watcher.getCard().getName() + "'s triggered ability", context, entry -> {
                            entry.setNonTargeting(true);
                            entry.setSourcePermanentSnapshot(new Permanent(watcher));
                            entry.setTriggeringPermanentId(attacker.getId());
                            entry.setTriggeringPermanentControllerId(controllerId);
                            entry.setTriggeringSpell(triggeredAbility);
                        }));
                if (!triggered) {
                    continue;
                }
                gameLogService.append(gameData, GameLog.abilityTriggers(watcher.getCard()));
                log.info("Game {} - {} triggers for {}'s attack-triggered ability",
                        gameData.id, watcher.getCard().getName(), attacker.getCard().getName());
            }
        }
    }

    /**
     * Collects turn-scoped global triggers for an event that identifies a permanent. {@code key} is the
     * {@code EffectSlot} or {@link TriggerWatch} the watchers were registered for.
     */
    public void collectTemporaryGlobalTriggers(GameData gameData, Object key, UUID targetId,
                                               int eventValue) {
        collectTemporaryGlobalTriggers(gameData, key, targetId, eventValue, null);
    }

    /** Collects turn-scoped global triggers while applying controller-relative permanent scopes. */
    public void collectTemporaryGlobalTriggers(GameData gameData, Object slot, UUID targetId,
                                               int eventValue, UUID targetControllerId) {
        for (TemporaryGlobalTriggeredAbility watcher : List.copyOf(gameData.temporaryGlobalTriggeredAbilities)) {
            if (!watches(watcher, slot) || watcher.watchedPermanentId() != null
                    || (slot == CombatWatch.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER
                    && !watcher.controllerId().equals(
                    gameQueryService.findPermanentController(gameData, targetId)))) {
                continue;
            }

            if (slot == TapWatch.ON_ALLY_PERMANENT_BECOMES_TAPPED
                    && (targetControllerId == null || !watcher.controllerId().equals(targetControllerId))) {
                continue;
            }
            if (slot == TapWatch.ON_OPPONENT_PERMANENT_BECOMES_TAPPED
                    && (targetControllerId == null || watcher.controllerId().equals(targetControllerId))) {
                continue;
            }
            // A registered tap ability's filter is on the tapped permanent ("a creature an opponent controls").
            TapAbilityReader.TapEffect tapRead = slot instanceof TapWatch tapWatch
                    ? TapAbilityReader.read(watcher.ability(), tapWatch.trigger()).getFirst() : null;
            if (tapRead != null) {
                TapAbilityReader.requireNotFirstTime(tapRead, ((TapWatch) slot).trigger());
                TapAbilityReader.requireNoOneOrMore(tapRead, ((TapWatch) slot).trigger());
                TapAbilityReader.requireNoLimit(tapRead, ((TapWatch) slot).trigger());
                if (tapRead.filter() != null) {
                    Permanent triggeringPermanent = targetId == null ? null
                            : gameQueryService.findPermanentById(gameData, targetId);
                    if (triggeringPermanent == null || !predicateEvaluationService.matchesPermanentPredicate(
                            triggeringPermanent, tapRead.filter(), FilterContext.of(gameData)
                                    .withSourceCardId(watcher.sourceCard().getId())
                                    .withSourceControllerId(watcher.controllerId()))) {
                        continue;
                    }
                }
            }

            // A registered "creature you control deals combat damage to a player" ability is collected here with its
            // plain effect. Any other combat ability's filter on the triggering creature is the trigger's
            // (Benefactor's Draught's "a creature an opponent controls blocks").
            CombatAbilityReader.CombatEffect combatRead = slot instanceof CombatWatch combatWatch
                    && slot != CombatWatch.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER
                    ? CombatAbilityReader.read(watcher.ability(), combatWatch.trigger()).getFirst() : null;
            if (combatRead != null) {
                CombatAbilityReader.requireNoLimitOrMultiplicity(combatRead, ((CombatWatch) slot).trigger());
                if (combatRead.filter() != null) {
                    Permanent triggeringPermanent = targetId == null ? null
                            : gameQueryService.findPermanentById(gameData, targetId);
                    if (triggeringPermanent == null || !predicateEvaluationService.matchesPermanentPredicate(
                            triggeringPermanent, combatRead.filter(), FilterContext.of(gameData)
                                    .withSourceCardId(watcher.sourceCard().getId())
                                    .withSourceControllerId(watcher.controllerId()))) {
                        continue;
                    }
                }
            }
            // A registered damage ability's filter is on the damaged permanent ("a creature an opponent controls").
            DamageAbilityReader.DamageEffect damageRead = slot instanceof DamageWatch damageWatch
                    ? DamageAbilityReader.read(watcher.ability(), damageWatch.trigger()).getFirst() : null;
            if (damageRead != null) {
                DamageAbilityReader.requireNoLimitOrMultiplicity(damageRead, ((DamageWatch) slot).trigger());
                if (damageRead.filter() != null) {
                    Permanent triggeringPermanent = targetId == null ? null
                            : gameQueryService.findPermanentById(gameData, targetId);
                    if (triggeringPermanent == null || !predicateEvaluationService.matchesPermanentPredicate(
                            triggeringPermanent, damageRead.filter(), FilterContext.of(gameData)
                                    .withSourceCardId(watcher.sourceCard().getId())
                                    .withSourceControllerId(watcher.controllerId()))) {
                        continue;
                    }
                }
            }
            // A registered life ability (False Cure's "whenever a player gains life") is read without a count or limit.
            CardEffect lifeEffect = slot instanceof LifeWatch lifeWatch
                    ? LifeAbilityReader.effects(List.of(watcher.ability()), lifeWatch.trigger()).getFirst() : null;
            CardEffect resolvedEffect = slot == CombatWatch.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER
                    ? watcher.effect() : combatRead != null ? combatRead.effect()
                    : damageRead != null ? damageRead.effect() : tapRead != null ? tapRead.effect()
                    : lifeEffect;
            if (resolvedEffect == null) {
                throw new IllegalStateException("No temporary global watcher is read for " + slot);
            }

            enqueueTemporaryGlobalTrigger(gameData, watcher, resolvedEffect, targetId, eventValue);
        }
    }

    /**
     * Whether {@code watcher} was registered for {@code key}, an {@code EffectSlot} or a {@link TriggerWatch} (found by
     * its trigger's watch key, as the watch reads a card's abilities).
     */
    private static boolean watches(TemporaryGlobalTriggeredAbility watcher, Object key) {
        return key instanceof TriggerWatch watch
                ? watcher.ability().hasWatchedTrigger(watch.trigger())
                : watcher.slot() == key;
    }

    private void enqueueTemporaryGlobalTrigger(GameData gameData,
                                                TemporaryGlobalTriggeredAbility watcher,
                                                UUID targetId, int eventValue) {
        enqueueTemporaryGlobalTrigger(gameData, watcher, watcher.effect(), targetId, eventValue);
    }

    private void enqueueTemporaryGlobalTrigger(GameData gameData,
                                                TemporaryGlobalTriggeredAbility watcher,
                                                CardEffect effect,
                                                UUID targetId, int eventValue) {
        enqueueTemporaryGlobalTrigger(gameData, watcher, effect, targetId, eventValue, null);
    }

    private void enqueueTemporaryGlobalTrigger(GameData gameData,
                                                TemporaryGlobalTriggeredAbility watcher,
                                                CardEffect effect,
                                                UUID targetId, int eventValue, UUID triggeringPermanentId) {
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                watcher.sourceCard(),
                watcher.controllerId(),
                watcher.sourceCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)));
        entry.setTargetId(targetId);
        entry.setEventValue(eventValue);
        entry.setLinkedExiledCardId(watcher.linkedExiledCardId());
        if (triggeringPermanentId != null) {
            entry.setTriggeringPermanentId(triggeringPermanentId);
        }
        if (watcher.ability().hasTrigger(TapWatch.ON_ANY_CREATURE_BECOMES_TAPPED.trigger())) {
            entry.setTriggeringPermanentId(targetId);
        }
        entry.setNonTargeting(true);
        if (watcher.ability().hasTrigger(TapWatch.ON_ALLY_PERMANENT_BECOMES_TAPPED.trigger())
                || watcher.ability().hasTrigger(TapWatch.ON_OPPONENT_PERMANENT_BECOMES_TAPPED.trigger())) {
            entry.setTriggeringPermanentId(targetId);
        }
        gameData.enqueueTrigger(entry);
        gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
        log.info("Game {} - {} temporary global {} trigger fires",
                gameData.id, watcher.sourceCard().getName(), LegacySlots.label(watcher.ability()));
    }


    // ── Enchanted-permanent-tap triggers ───────────────────────────────

    public void beginPermanentTapTriggerBatch(GameData gameData) {
        if (gameData.permanentTapTriggerBatchDepth++ == 0) {
            gameData.permanentTapTriggerBatchFiredAbilities.clear();
        }
    }

    public void endPermanentTapTriggerBatch(GameData gameData) {
        if (gameData.permanentTapTriggerBatchDepth <= 0) {
            throw new IllegalStateException("No permanent tap trigger batch is active");
        }
        if (--gameData.permanentTapTriggerBatchDepth == 0) {
            gameData.permanentTapTriggerBatchFiredAbilities.clear();
        }
    }

    public void checkEnchantedPermanentTapTriggers(GameData gameData, Permanent tappedPermanent) {
        UUID tappingPlayerId = gameData.currentlyResolvingControllerId;
        if (tappingPlayerId == null && gameData.pendingEffectResolutionEntry != null) {
            tappingPlayerId = gameData.pendingEffectResolutionEntry.getControllerId();
        }
        checkEnchantedPermanentTapTriggers(gameData, tappedPermanent, tappingPlayerId);
    }

    public void checkEnchantedPermanentTapTriggers(GameData gameData, Permanent tappedPermanent,
                                                   UUID tappingPlayerId) {
        UUID tappedPermanentControllerId = null;
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf != null && bf.contains(tappedPermanent)) {
                tappedPermanentControllerId = pid;
                break;
            }
        }
        if (tappedPermanentControllerId == null) return;

        final int creatureTapCount = gameQueryService.isCreature(gameData, tappedPermanent)
                ? gameData.creatureTapCountsThisTurn.merge(tappedPermanent.getId(), 1, Integer::sum)
                : 0;

        if (creatureTapCount > 0) {
            collectTemporaryGlobalTriggers(gameData, TapWatch.ON_ANY_CREATURE_BECOMES_TAPPED,
                    tappedPermanent.getId(), 0);
        }

        UUID controllerId = tappedPermanentControllerId;
        var ctx = new TriggerContext.EnchantedPermanentTap(tappedPermanent, controllerId);

        collectEmblemTapTriggers(gameData, tappedPermanent, controllerId);

        // "Whenever enchanted permanent becomes tapped": the ability is the Aura's (CR 113.7); the tapped permanent's
        // controller is the non-targeting "that player" ("that player mills three cards"), and a "choose one" (Relic
        // Bind) announces its mode, and a mode its target, as it goes on the stack (CR 603.3c).
        gameData.forEachPermanent((auraOwnerId, perm) -> {
            if (!perm.isAttached() || !perm.getAttachedTo().equals(tappedPermanent.getId())) {
                return;
            }
            for (TriggeredAbility ability : printedWatchedAbilities(gameData, perm,
                    TapWatch.ON_ENCHANTED_PERMANENT_TAPPED)) {
                TapAbilityReader.requireNothingToJudge(TapAbilityReader.read(ability,
                        TapWatch.ON_ENCHANTED_PERMANENT_TAPPED.trigger()).getFirst(),
                        TapWatch.ON_ENCHANTED_PERMANENT_TAPPED.trigger());
                putEventAbility(gameData, ability, perm.getCard(), perm, auraOwnerId,
                        ConditionContext.forPermanent(perm, auraOwnerId)
                                .withTriggeringPermanentId(tappedPermanent.getId()), ctx,
                        tappedRecord(perm, tappedPermanent, controllerId, controllerId));
            }
        });

        // "Whenever a permanent you control becomes tapped" triggers (e.g. Judge of Currents).
        gameData.forEachPermanent((ownerId, perm) -> {
            if (!ownerId.equals(controllerId)) return;
            for (TriggeredAbility ability : tapAbilitiesWithGrants(gameData, perm,
                    TapWatch.ON_ALLY_PERMANENT_BECOMES_TAPPED, LayerSystemService.currentCard(gameData, perm))) {
                TapAbilityReader.TapEffect read = TapAbilityReader.read(ability,
                        TapWatch.ON_ALLY_PERMANENT_BECOMES_TAPPED.trigger()).getFirst();
                // "For the first time during each of your turns": the creature's first tap this turn, during its
                // controller's turn, counting its taps before the source was on the battlefield.
                if (read.firstTime() && (!controllerId.equals(gameData.activePlayerId) || creatureTapCount != 1)) {
                    continue;
                }
                putTapAbility(gameData, perm, ownerId, read, tappedPermanent, controllerId, ctx);
            }
        });

        // "Whenever a permanent an opponent controls becomes tapped" triggers (e.g. Thoughtleech).
        gameData.forEachPermanent((ownerId, perm) -> {
            if (ownerId.equals(controllerId)) return;
            for (TriggeredAbility ability : printedWatchedAbilities(gameData, perm,
                    TapWatch.ON_OPPONENT_PERMANENT_BECOMES_TAPPED)) {
                putTapAbility(gameData, perm, ownerId, TapAbilityReader.read(ability,
                                TapWatch.ON_OPPONENT_PERMANENT_BECOMES_TAPPED.trigger()).getFirst(),
                        tappedPermanent, controllerId, ctx);
            }
        });

        collectTemporaryGlobalTriggers(gameData, TapWatch.ON_ALLY_PERMANENT_BECOMES_TAPPED,
                tappedPermanent.getId(), 0, controllerId);
        collectTemporaryGlobalTriggers(gameData, TapWatch.ON_OPPONENT_PERMANENT_BECOMES_TAPPED,
                tappedPermanent.getId(), 0, controllerId);

        // "Whenever you tap an untapped creature an opponent controls" triggers. The tapping
        // player is tracked separately from the tapped permanent's controller because an effect
        // may instruct another player to tap their own permanent.
        if (tappingPlayerId == null || tappingPlayerId.equals(controllerId)) {
            return;
        }
        gameData.forEachPermanent((ownerId, perm) -> {
            if (!ownerId.equals(tappingPlayerId)) return;
            if (gameQueryService.hasLostAllAbilities(gameData, perm)) return;
            for (TriggeredAbility ability : tapAbilitiesWithGrants(gameData, perm,
                    TapWatch.ON_CONTROLLER_TAPS_OPPONENT_PERMANENT, perm.getCard())) {
                putTapAbility(gameData, perm, ownerId, TapAbilityReader.read(ability,
                                TapWatch.ON_CONTROLLER_TAPS_OPPONENT_PERMANENT.trigger()).getFirst(),
                        tappedPermanent, controllerId, ctx);
            }
        });
    }

    /**
     * The abilities {@code watch} reads of {@code perm}: those printed on {@code printedCard} (its card as the layers
     * leave it; none once it has lost its printed abilities), those granted to it until end of turn or until it leaves,
     * and those other permanents' static abilities grant it, in that order.
     */
    private List<TriggeredAbility> tapAbilitiesWithGrants(GameData gameData, Permanent perm, TapWatch watch,
                                                          Card printedCard) {
        Trigger watched = watch.trigger();
        List<TriggeredAbility> abilities = gameQueryService.hasLostPrintedAbilities(gameData, perm)
                ? new ArrayList<>() : new ArrayList<>(TapAbilityReader.printed(printedCard, watch));
        abilities.addAll(WatchedAbilities.temporary(perm, watched));
        abilities.addAll(WatchedAbilities.persistent(perm, watched));
        abilities.addAll(grantedTriggeredAbilitySupport.grantedWatchedAbilities(gameData, perm, watched));
        return abilities;
    }

    /**
     * Hands the becomes-tapped ability of {@code read} of {@code source} to the stack door for {@code tapped}, which
     * {@code tappedControllerId} controls: its filter on the tapped permanent ("a Merfolk you control") and "one or
     * more" for the permanents tapped together are judged here, its limit and intervening "if" by the door, and a target
     * is chosen as it goes on the stack (CR 603.3d), a "you may pay" one too (Surgespanner); a "choose one" announces
     * its mode there (CR 603.3c).
     */
    private void putTapAbility(GameData gameData, Permanent source, UUID controllerId, TapAbilityReader.TapEffect read,
                               Permanent tapped, UUID tappedControllerId, TriggerContext event) {
        if (shouldSkipBatchedTapTrigger(gameData, source, read)
                || !tappedPermanentMatches(gameData, source, controllerId, tapped, read)) {
            return;
        }
        if (putEventAbility(gameData, read.ability(), source.getCard(), source, controllerId,
                ConditionContext.forPermanent(source, controllerId).withTriggeringPermanentId(tapped.getId()), event,
                tappedRecord(source, tapped, tappedControllerId, null))) {
            markBatchedTapTrigger(gameData, source, read);
        }
    }

    /**
     * The record of a becomes-tapped or becomes-untapped event, whatever the ability's effects are: the permanent as
     * its triggering permanent ("it", "that permanent's controller", Royal Decree; as it last existed on the
     * battlefield once it has left, CR 608.2h), {@code thatPlayerId} as the non-targeting "that player" (or none), and
     * the source as it was when the ability triggered.
     */
    private static TriggerRecord tappedRecord(Permanent source, Permanent tapped, UUID tappedControllerId,
                                              UUID thatPlayerId) {
        Permanent snapshot = new Permanent(source);
        return entry -> {
            entry.setNonTargeting(true);
            if (thatPlayerId != null) {
                entry.setTargetId(thatPlayerId);
            }
            recordTappedPermanent(entry, tapped, tappedControllerId);
            entry.setSourcePermanentSnapshot(snapshot);
        };
    }

    /**
     * Whether the tapped or untapped permanent {@code tapped} matches {@code read}'s filter ("a Merfolk you control",
     * "this permanent"), if it has one, judged with {@code source}, the ability's source controlled by
     * {@code sourceControllerId}, as the filter's source.
     */
    private boolean tappedPermanentMatches(GameData gameData, Permanent source, UUID sourceControllerId,
                                           Permanent tapped, TapAbilityReader.TapEffect read) {
        if (read.filter() == null) {
            return true;
        }
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(source.getOriginalCard().getId())
                .withSourceControllerId(sourceControllerId);
        return predicateEvaluationService.matchesPermanentPredicate(tapped, read.filter(), filterContext);
    }

    /**
     * The emblems' "whenever a [filter] permanent you control becomes tapped" abilities for {@code tappedPermanent},
     * which {@code tappedPermanentControllerId} controls (Tezzeret, Betrayer of Flesh's emblem). Abilities of emblems
     * function in the command zone (CR 114.4). Each ability whose trigger's filter the permanent matches goes on the
     * stack once as one object of its effects (CR 603.3), the tapped permanent as its triggering permanent. The reader
     * judges no options and puts no ability with a target on the stack; such an ability fails loudly instead.
     */
    private void collectEmblemTapTriggers(GameData gameData, Permanent tappedPermanent,
                                          UUID tappedPermanentControllerId) {
        for (Emblem emblem : List.copyOf(gameData.emblems)) {
            for (TriggeredAbility ability : emblem.triggeredAbilities()) {
                if (emblemSeesTapped(gameData, emblem, ability, tappedPermanent, tappedPermanentControllerId)) {
                    putEmblemAbilityOnStack(gameData, emblem, ability, tappedPermanent, tappedPermanentControllerId,
                            "becoming tapped");
                }
            }
        }
    }

    /** Whether {@code ability} of {@code emblem} triggers on {@code tapped} becoming tapped. */
    private boolean emblemSeesTapped(GameData gameData, Emblem emblem, TriggeredAbility ability, Permanent tapped,
                                     UUID tappedControllerId) {
        for (TriggerCondition condition : ability.conditions()) {
            if (!(condition.trigger() instanceof TapTrigger trigger)
                    || trigger.event() != TapTrigger.TapEvent.BECOMES_TAPPED) {
                continue;
            }
            if (!ability.options().equals(TriggerOptions.DEFAULT) || ability.effects().stream()
                    .anyMatch(registration -> registration.effect().targetSpec() != TargetSpec.NONE)) {
                throw new IllegalStateException(emblemName(emblem) + " has a tap ability its reader doesn't judge: "
                        + ability);
            }
            FilterContext context = emblemFilterContext(gameData, emblem);
            if (controlledByRelatedPlayer(trigger.relation(), emblem, tappedControllerId)
                    && (trigger.filter() == null
                    || predicateEvaluationService.matchesPermanentPredicate(tapped, trigger.filter(), context))
                    && (trigger.qualifier() == null
                    || predicateEvaluationService.matchesPermanentPredicate(tapped, trigger.qualifier(), context))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Whether {@code read}'s "one or more" ability already triggered on {@code source} for the permanents tapped
     * together in the open tap batch ({@link #beginPermanentTapTriggerBatch}): the mark is the ability's
     * ({@link TriggeredAbility#limitKey()}), so two "one or more" abilities of one permanent each trigger once.
     */
    private static boolean shouldSkipBatchedTapTrigger(GameData gameData, Permanent source,
                                                        TapAbilityReader.TapEffect read) {
        return read.oneOrMore()
                && gameData.permanentTapTriggerBatchDepth > 0
                && gameData.permanentTapTriggerBatchFiredAbilities.getOrDefault(source.getId(), Set.of())
                .contains(read.ability().limitKey());
    }

    private static void markBatchedTapTrigger(GameData gameData, Permanent source,
                                               TapAbilityReader.TapEffect read) {
        if (!read.oneOrMore() || gameData.permanentTapTriggerBatchDepth <= 0) {
            return;
        }
        gameData.permanentTapTriggerBatchFiredAbilities
                .computeIfAbsent(source.getId(), ignored -> ConcurrentHashMap.newKeySet())
                .add(read.ability().limitKey());
    }

    /**
     * Collects abilities that trigger when a creature pays a Vehicle's crew cost. The Vehicle is
     * carried as the triggering permanent so the triggered effect can resolve against that object.
     */
    public void checkCrewsVehicleTriggers(GameData gameData, Permanent crewingCreature, Permanent vehicle) {
        if (vehicle == null || !vehicle.getCard().getSubtypes().contains(CardSubtype.VEHICLE)) {
            return;
        }
        UUID controllerId = gameQueryService.findPermanentController(gameData, crewingCreature.getId());
        if (controllerId == null) {
            return;
        }
        if (gameQueryService.hasLostPrintedAbilities(gameData, crewingCreature)) {
            return;
        }
        // Each ability is one object of all its effects (CR 603.3).
        for (List<CardEffect> effects : MiscAbilityReader.printedEffectsByAbility(crewingCreature.getCard(),
                MiscWatch.ON_CREWS_VEHICLE)) {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    crewingCreature.getCard(),
                    controllerId,
                    crewingCreature.getCard().getName() + "'s ability",
                    new ArrayList<>(effects),
                    null,
                    crewingCreature.getId()
            );
            entry.setTriggeringPermanentId(vehicle.getId());
            entry.setSourcePermanentSnapshot(new Permanent(crewingCreature));
            gameData.pendingActivatedAbilityCostTriggers.add(entry);
            gameLogService.append(gameData, GameLog.abilityTriggers(crewingCreature.getCard()));
            log.info("Game {} - {} triggers when it crews {}",
                    gameData.id, crewingCreature.getCard().getName(), vehicle.getCard().getName());
        }
    }

    /** Collects abilities watching a creature controlled by a player crew a Vehicle. */
    public void checkAllyCreatureCrewsVehicleTriggers(GameData gameData, Permanent crewingCreature,
                                                       Permanent vehicle) {
        if (vehicle == null || !vehicle.getCard().getSubtypes().contains(CardSubtype.VEHICLE)) {
            return;
        }
        UUID controllerId = gameQueryService.findPermanentController(gameData, crewingCreature.getId());
        if (controllerId == null) {
            return;
        }
        TriggerContext context = new TriggerContext.CreatureCrewsVehicle(crewingCreature, vehicle);
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return;
        }
        for (Permanent watcher : List.copyOf(battlefield)) {
            dispatchWatch(gameData, watcher, controllerId,
                    MiscWatch.ON_ALLY_CREATURE_CREWS_VEHICLE, context);
        }
    }

    /** Collects abilities that trigger when a creature is tapped to pay a teamwork cost. */
    public void checkSelfTappedToPayTeamworkCostTriggers(GameData gameData, Permanent tappedCreature) {
        UUID controllerId = gameQueryService.findPermanentController(gameData, tappedCreature.getId());
        if (controllerId == null) {
            return;
        }
        TapWatch watch = TapWatch.ON_SELF_TAPPED_TO_PAY_TEAMWORK_COST;
        TriggerContext event = new TriggerContext.EnchantedPermanentTap(tappedCreature, controllerId);
        for (TriggeredAbility ability : watchedAbilities(gameData, tappedCreature, watch)) {
            TapAbilityReader.requireNothingToJudge(TapAbilityReader.read(ability, watch.trigger()).getFirst(),
                    watch.trigger());
            putEventAbility(gameData, ability, tappedCreature.getCard(), tappedCreature, controllerId,
                    ConditionContext.forPermanent(tappedCreature, controllerId), event,
                    eventRecord(tappedCreature, null, controllerId, null, null, null));
        }
    }

    /**
     * Records the permanent that became tapped on a becomes-tapped ability's entry whatever its effects are: "it" and
     * "that permanent's controller" (Royal Decree) as it is as the ability resolves, or as it last existed on the
     * battlefield once it has left (CR 608.2h).
     */
    private static void recordTappedPermanent(StackEntry entry, Permanent tappedPermanent, UUID tappedControllerId) {
        entry.setTriggeringPermanentId(tappedPermanent.getId());
        entry.setTriggeringPermanent(tappedPermanent);
        entry.setTriggeringPermanentControllerId(tappedControllerId);
    }

    // ── Becomes-untapped triggers ──────────────────────────────────────

    /**
     * "Whenever this permanent becomes untapped" triggers (e.g. Hollowsage). Called from the untap
     * call sites after a permanent transitions from tapped to untapped. Fires only on the permanent
     * that became untapped, its abilities (its own and those granted to it, Well Rested, whose source is the enchanted
     * creature) handed whole to the stack door with the permanent as their source; targeted effects choose targets as
     * the ability is put on the stack (CR 603.3d).
     */
    public void checkBecomesUntappedTriggers(GameData gameData, Permanent untappedPermanent) {
        UUID controllerId = null;
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf != null && bf.contains(untappedPermanent)) {
                controllerId = pid;
                break;
            }
        }
        if (controllerId == null) return;

        processDelayedDestroyTargetWhenSourceUntaps(gameData, untappedPermanent);

        TriggerContext event = new TriggerContext.SelfBecomesUntapped(controllerId);
        TapWatch selfUntap = TapWatch.ON_SELF_BECOMES_UNTAPPED;
        List<TriggeredAbility> own = new ArrayList<>(printedWatchedAbilities(gameData, untappedPermanent, selfUntap));
        // "This ability triggers only once each turn" (Well Rested) is the ability's limit; each granting Aura's
        // instance keeps its own (TriggeredAbility#instance()).
        own.addAll(grantedTriggeredAbilitySupport.grantedWatchedAbilities(gameData, untappedPermanent, controllerId,
                selfUntap.trigger()));
        for (TriggeredAbility ability : own) {
            TapAbilityReader.TapEffect read = TapAbilityReader.read(ability, selfUntap.trigger()).getFirst();
            TapAbilityReader.requireNoFilter(read, selfUntap.trigger());
            TapAbilityReader.requireNotFirstTime(read, selfUntap.trigger());
            TapAbilityReader.requireNoOneOrMore(read, selfUntap.trigger());
            putEventAbility(gameData, ability, untappedPermanent.getCard(), untappedPermanent, controllerId,
                    ConditionContext.forPermanent(untappedPermanent, controllerId), event,
                    eventRecord(untappedPermanent, null, controllerId, null, null, null));
        }

        // "Whenever a permanent you control becomes untapped" triggers (e.g. Wake Thrasher).
        UUID untappedControllerId = controllerId;
        gameData.forEachPermanent((ownerId, perm) -> {
            if (!ownerId.equals(untappedControllerId)) return;
            Trigger allyUntap = TapWatch.ON_ALLY_PERMANENT_BECOMES_UNTAPPED.trigger();
            for (TriggeredAbility ability : printedWatchedAbilities(gameData, perm,
                    TapWatch.ON_ALLY_PERMANENT_BECOMES_UNTAPPED)) {
                TapAbilityReader.TapEffect read = TapAbilityReader.read(ability, allyUntap).getFirst();
                TapAbilityReader.requireNoLimit(read, allyUntap);
                if (!tappedPermanentMatches(gameData, perm, ownerId, untappedPermanent, read)) continue;
                putEventAbility(gameData, ability, perm.getCard(), perm, ownerId,
                        ConditionContext.forPermanent(perm, ownerId)
                                .withTriggeringPermanentId(untappedPermanent.getId()), event,
                        tappedRecord(perm, untappedPermanent, untappedControllerId, null));
            }
        });

        gameData.forEachPermanent((ownerId, perm) -> {
            if (perm.isFaceDown()) return;
            for (TriggeredAbility ability : printedWatchedAbilities(gameData, perm,
                    TapWatch.ON_ANY_PERMANENT_BECOMES_UNTAPPED)) {
                TapAbilityReader.TapEffect read = TapAbilityReader.read(ability,
                        TapWatch.ON_ANY_PERMANENT_BECOMES_UNTAPPED.trigger()).getFirst();
                if (!tappedPermanentMatches(gameData, perm, ownerId, untappedPermanent, read)) continue;
                TriggerRecord untapped = tappedRecord(perm, untappedPermanent, untappedControllerId, null);
                putEventAbility(gameData, ability, perm.getCard(), perm, ownerId,
                        ConditionContext.forPermanent(perm, ownerId)
                                .withTriggeringPermanentId(untappedPermanent.getId()), event, entry -> {
                            untapped.stamp(entry);
                            entry.setEventPlayerIds(List.of(untappedControllerId));
                        });
            }
        });
    }

    /**
     * Checks triggers that care about one or more permanents untapping during the active player's
     * untap step: "whenever you untap one or more permanents during your untap step, …" (The Millennium Calendar's
     * "put that many time counters"), once for the untap step, with the number of permanents untapped as its event
     * value ("that many").
     */
    public void checkControllerUntapsDuringUntapStepTriggers(GameData gameData, UUID controllerId,
                                                               int untappedPermanentCount) {
        if (untappedPermanentCount <= 0) return;
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return;

        TriggerContext context = new TriggerContext.UntapStep(untappedPermanentCount);
        TapWatch watch = TapWatch.ON_CONTROLLER_UNTAPS_DURING_UNTAP_STEP;
        for (Permanent permanent : List.copyOf(battlefield)) {
            for (TriggeredAbility ability : watchedAbilities(gameData, permanent, watch)) {
                TapAbilityReader.requireNothingToJudge(TapAbilityReader.read(ability, watch.trigger()).getFirst(),
                        watch.trigger());
                putEventAbility(gameData, ability, permanent.getCard(), permanent, controllerId,
                        ConditionContext.forPermanent(permanent, controllerId).withEventValue(untappedPermanentCount),
                        context, eventRecord(permanent, null, controllerId, untappedPermanentCount, null, null));
            }
        }
    }

    /**
     * "When this creature becomes renowned" (Relic Seeker) and "whenever a creature you control
     * becomes renowned" (Valeron Wardens) triggers. Called from
     * {@code RenownEffectHandler} at the moment renown flips the creature from not-renowned to
     * renowned; a creature that was already renowned never reaches this point (CR 702.112c).
     *
     * @param gameData         the current game state to modify
     * @param renownedCreature the creature that just became renowned
     * @param controllerId     the player controlling it
     */
    public void checkBecomesRenownedTriggers(GameData gameData, Permanent renownedCreature, UUID controllerId) {
        TriggerContext event = new TriggerContext.CountersPlaced(controllerId, 0);
        putCounterAbilities(gameData, renownedCreature, controllerId, CounterWatch.ON_SELF_BECOMES_RENOWNED, event,
                ConditionContext.forPermanent(renownedCreature, controllerId),
                eventRecord(renownedCreature, null, controllerId, null, null, null));

        gameData.forEachPermanent((ownerId, perm) -> {
            if (!ownerId.equals(controllerId)) return;
            CounterWatch watch = CounterWatch.ON_ALLY_CREATURE_BECOMES_RENOWNED;
            for (TriggeredAbility ability : watchedAbilities(gameData, perm, watch)) {
                PermanentPredicate filter = CounterAbilityReader.trigger(ability, watch.trigger()).qualifier();
                // "Whenever a [filter] creature you control becomes renowned": the trigger's filter on the creature.
                if (filter != null && !predicateEvaluationService.matchesPermanentPredicate(renownedCreature, filter,
                        FilterContext.of(gameData)
                                .withSourceCardId(perm.getOriginalCard().getId())
                                .withSourceControllerId(ownerId))) {
                    continue;
                }
                putEventAbility(gameData, ability, perm.getCard(), perm, ownerId,
                        ConditionContext.forPermanent(perm, ownerId).withTriggeringPermanentId(renownedCreature.getId()),
                        event, eventRecord(perm, null, ownerId, null, renownedCreature, controllerId));
            }
        });
    }

    /** Fires abilities that trigger whenever a player becomes the monarch. */
    public void checkBecomesMonarchTriggers(GameData gameData, UUID monarchPlayerId) {
        gameData.forEachPermanent((ownerId, permanent) -> {
            boolean controllerBecameMonarch = ownerId.equals(monarchPlayerId);
            ActionWatch watch = controllerBecameMonarch
                    ? ActionWatch.ON_CONTROLLER_BECOMES_MONARCH
                    : ActionWatch.ON_OPPONENT_BECOMES_MONARCH;
            // Each ability is one object of all its effects (CR 603.3); a targeted one goes to its target choice whole.
            for (List<CardEffect> abilityEffects : byAbility(conditionEvaluationService.judge(gameData,
                            ConditionContext.forPermanent(permanent, ownerId))
                    .filter(ActionAbilityReader.printed(permanent.getCard(), watch)),
                    abilities -> ActionAbilityReader.effects(abilities, watch.trigger()))) {
                List<CardEffect> effects = new ArrayList<>(abilityEffects);
                CardEffect effect = effects.stream()
                        .filter(candidate -> candidate.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                                || candidate.targetSpec().admits(TargetPredicate.Kind.PERMANENT))
                        .findFirst().orElse(null);
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        permanent.getCard(),
                        ownerId,
                        permanent.getCard().getName() + "'s ability",
                        effects,
                        controllerBecameMonarch ? null : monarchPlayerId,
                        permanent.getId()
                );
                entry.setNonTargeting(true);
                entry.setTriggeringPlayerId(monarchPlayerId);
                entry.setSourcePermanentSnapshot(new Permanent(permanent));
                if (effect != null) {
                    int targetGroupIndex = permanent.getCard().getEffectTargetIndex(effect);
                    TargetFilter targetFilter = targetGroupIndex >= 0
                            && targetGroupIndex < permanent.getCard().getTargetGroups().size()
                            ? permanent.getCard().getTargetGroups().get(targetGroupIndex).getFilter()
                            : permanent.getCard().getTargetFilter();
                    boolean playerTargetOnly = effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                            && !effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT);
                    // "That player" is the player who became the monarch, the entry's triggering player.
                    queueTargetChoice(gameData, entry, new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                            permanent.getCard(), ownerId, effects, playerTargetOnly, targetFilter,
                            0, permanent.getId(), new Permanent(permanent), false, null,
                            null, ownerId).withTriggeringPlayer(monarchPlayerId));
                } else {
                    gameData.enqueueTrigger(entry);
                }
                gameLogService.append(gameData, GameLog.abilityTriggers(permanent.getCard()));
                log.info("Game {} - {} triggers when {} becomes monarch", gameData.id,
                        permanent.getCard().getName(), controllerBecameMonarch ? "its controller" : "an opponent");
            }
        });
        if (gameData.hasPendingInteraction(PermanentChoiceContext.SpellTargetTriggerAnyTarget.class)
                && !gameData.interaction.isAwaitingInput()) {
            processNextSpellTargetTrigger(gameData);
        }
    }

    /** Fires abilities that trigger whenever the controller solves a Case. */
    public void checkAllyCaseSolvesTriggers(GameData gameData, Permanent solvedCase, UUID controllerId) {
        gameData.forEachPermanent((ownerId, permanent) -> {
            if (!ownerId.equals(controllerId)) return;
            // Each ability is one object of all its effects (CR 603.3).
            for (List<CardEffect> effects : ActionAbilityReader.printedEffectsByAbility(permanent.getCard(),
                    ActionWatch.ON_ALLY_CASE_SOLVES)) {
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        permanent.getCard(),
                        controllerId,
                        permanent.getCard().getName() + "'s ability",
                        new ArrayList<>(effects),
                        null,
                        permanent.getId()
                );
                entry.setTriggeringPermanentId(solvedCase.getId());
                gameData.enqueueTrigger(entry);
                gameLogService.append(gameData, GameLog.abilityTriggers(permanent.getCard()));
                log.info("Game {} - {} triggers when a Case is solved", gameData.id,
                        permanent.getCard().getName());
            }
        });
    }

    /** Fires the newly monstrous permanent's own triggered abilities. */
    public void checkBecomesMonstrousTriggers(GameData gameData, Permanent monstrousPermanent,
                                               UUID controllerId) {
        checkBecomesMonstrousTriggers(gameData, monstrousPermanent, controllerId, 0);
    }

    /**
     * Hands each "when this becomes monstrous" ability of {@code monstrousPermanent} whole to the stack door (CR 603.3),
     * the X its monstrosity was activated with as the entry's X ("when this becomes monstrous, it deals X damage").
     */
    public void checkBecomesMonstrousTriggers(GameData gameData, Permanent monstrousPermanent,
                                               UUID controllerId, int xValue) {
        Permanent snapshot = new Permanent(monstrousPermanent);
        putCounterAbilities(gameData, monstrousPermanent, controllerId, CounterWatch.ON_SELF_BECOMES_MONSTROUS,
                new TriggerContext.SelfBecomesMonstrous(controllerId, xValue),
                ConditionContext.forPermanent(monstrousPermanent, controllerId).withXValue(xValue), entry -> {
                    entry.setXValue(xValue);
                    entry.setSourcePermanentSnapshot(snapshot);
                });
    }

    /**
     * "When this phases out" and "when this phases in" (Teferi's Imp): the ability as one stack object of its effects
     * (CR 603.3). A targeted phase-in ability (Shimmering Efreet) chooses its target as it is put on the stack, with
     * all its effects.
     */
    private boolean enqueuePhasingTriggers(GameData gameData, Permanent permanent, UUID controllerId,
                                           List<CardEffect> effects, boolean phasesIn) {
        String direction = phasesIn ? "in" : "out";
        if (effects.isEmpty()) {
            return false;
        }
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                permanent.getCard(),
                controllerId,
                permanent.getCard().getName() + "'s ability",
                new ArrayList<>(effects),
                null,
                permanent.getId()
        );
        gameData.handOffTriggerRecord(entry);
        if (phasesIn && effects.stream()
                .anyMatch(effect -> effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT))) {
            gameData.queueInteraction(new PermanentChoiceContext.PhasesInTriggerTarget(
                    permanent.getCard(), controllerId, new ArrayList<>(effects), permanent.getId()));
            gameLogService.append(gameData, GameLog.abilityTriggers(permanent.getCard()));
            log.info("Game {} - {} triggers on phasing {} (awaiting target)",
                    gameData.id, permanent.getCard().getName(), direction);
            return true;
        }
        gameData.enqueueTrigger(entry);
        gameLogService.append(gameData, GameLog.abilityTriggers(permanent.getCard()));
        log.info("Game {} - {} triggers on phasing {}", gameData.id, permanent.getCard().getName(), direction);
        return true;
    }

    /**
     * A single "when you next cast a spell with {X} in its mana cost or activate an ability with {X} in
     * its activation cost" delayed trigger (Magus Lucea Kane) is registered as one spell copy and one
     * ability copy; when the ability half fires, the spell half is used up with it.
     */
    private void consumeLinkedXSpellCopies(GameData gameData, UUID playerId, int count) {
        List<CopyNextSpellCastThisTurnEffect> pending = gameData.pendingNextFilteredSpellCopiesThisTurn.get(playerId);
        if (pending == null) {
            return;
        }
        List<CopyNextSpellCastThisTurnEffect> remaining = new ArrayList<>(pending);
        for (int i = 0; i < count; i++) {
            CopyNextSpellCastThisTurnEffect linked = remaining.stream()
                    .filter(copyEffect -> copyEffect.spellFilter() instanceof CardHasXInManaCostPredicate)
                    .findFirst().orElse(null);
            if (linked == null) {
                break;
            }
            remaining.remove(linked);
        }
        if (remaining.isEmpty()) {
            gameData.pendingNextFilteredSpellCopiesThisTurn.remove(playerId);
        } else {
            gameData.pendingNextFilteredSpellCopiesThisTurn.put(playerId, remaining);
        }
    }

    /** The spell half of a linked X delayed trigger used up its ability half; see {@link #consumeLinkedXSpellCopies}. */
    private void consumeLinkedXAbilityCopies(GameData gameData, UUID playerId, int count) {
        gameData.pendingNextXActivatedAbilityCopyThisTurnCount.computeIfPresent(playerId,
                (id, pendingCount) -> pendingCount - count > 0 ? pendingCount - count : null);
    }

    /**
     * Fires triggers for the creature tapped to saddle a Mount or crew a Vehicle during its
     * controller's main phase. The check runs once for each creature tapped to pay the cost.
     */
    public void checkSelfSaddlesOrCrewsDuringMainPhaseTriggers(GameData gameData, UUID activatingPlayerId,
                                                                Permanent tappedCreature,
                                                                UUID saddledOrCrewedPermanentId) {
        if (!activatingPlayerId.equals(gameData.activePlayerId)
                || (gameData.currentStep != TurnStep.PRECOMBAT_MAIN
                && gameData.currentStep != TurnStep.POSTCOMBAT_MAIN)) {
            return;
        }

        Permanent saddledOrCrewedPermanent = gameQueryService.findPermanentById(gameData, saddledOrCrewedPermanentId);
        if (saddledOrCrewedPermanent == null) return;
        if (!gameQueryService.hasEffectiveSubtype(gameData, saddledOrCrewedPermanent, CardSubtype.MOUNT)
                && !gameQueryService.hasEffectiveSubtype(gameData, saddledOrCrewedPermanent, CardSubtype.VEHICLE)) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(activatingPlayerId);
        if (battlefield == null) return;
        Permanent watcher = battlefield.stream()
                .filter(permanent -> permanent.getId().equals(tappedCreature.getId()))
                .findFirst()
                .orElse(null);
        if (watcher == null || watcher.isLosesAllAbilitiesUntilEndOfTurn()) return;

        MiscWatch watch = MiscWatch.ON_SELF_SADDLES_OR_CREWS_DURING_MAIN_PHASE;
        List<TriggeredAbility> abilities = new ArrayList<>(MiscAbilityReader.printed(watcher.getCard(), watch));
        abilities.addAll(grantedTriggeredAbilitySupport.grantedWatchedAbilities(gameData, watcher, watch.trigger()));
        List<List<CardEffect>> byAbility = MiscAbilityReader.effectsByAbility(abilities, watch.trigger());
        if (byAbility.isEmpty()) return;

        int triggerCopies = 1 +
                gameQueryService.countAdditionalTriggeredAbilityTriggers(gameData, activatingPlayerId, watcher);
        // Each printed or granted ability is one object of all its effects (CR 603.3).
        for (List<CardEffect> effects : byAbility) {
            StackEntry trigger = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    watcher.getCard(),
                    activatingPlayerId,
                    watcher.getCard().getName() + "'s ability",
                    new ArrayList<>(effects),
                    null,
                    watcher.getId());
            trigger.setNonTargeting(true);
            trigger.setSourcePermanentSnapshot(new Permanent(watcher));
            trigger.setTriggeringPermanentId(saddledOrCrewedPermanent.getId());
            gameData.pendingActivatedAbilityCostTriggers.add(trigger);
            for (int copy = 1; copy < triggerCopies; copy++) {
                gameData.pendingActivatedAbilityCostTriggers.add(new StackEntry(trigger));
            }
            gameLogService.append(gameData, GameLog.abilityTriggers(watcher.getCard()));
            log.info("Game {} - {} triggers when its controller saddles or crews {}",
                    gameData.id, watcher.getCard().getName(), saddledOrCrewedPermanent.getCard().getName());
        }
    }

    private boolean checkDiscardEventTriggers(GameData gameData, UUID discardingPlayerId,
                                              int discardedCount, List<Card> discardedCards) {
        List<Permanent> ownBattlefield = gameData.playerBattlefields.get(discardingPlayerId);
        if (ownBattlefield == null) {
            return false;
        }

        boolean[] triggered = {false};
        var ctx = new TriggerContext.DiscardEvent(discardingPlayerId, discardedCount, discardedCards);
        for (Permanent perm : List.copyOf(ownBattlefield)) {
            if (dispatchWatch(gameData, perm, discardingPlayerId,
                    LibraryWatch.ON_CONTROLLER_DISCARD_EVENT, ctx)) {
                triggered[0] = true;
            }
        }
        return triggered[0];
    }

    /**
     * Whether the filter of {@code effect}'s becomes-the-target ability ({@link TargetEffect#filter()}) admits
     * {@code targetPermanent}, judged from {@code watcher}'s point of view: always without a filter, never for a
     * player target (no permanent) with one.
     */
    private boolean targetFilterMatches(GameData gameData, Permanent watcher, UUID watcherControllerId,
                                        Permanent targetPermanent, TargetEffect effect) {
        if (effect.filter() == null) {
            return true;
        }
        return targetPermanent != null && predicateEvaluationService.matchesPermanentPredicate(targetPermanent,
                effect.filter(), watcherFilterContext(gameData, watcher, watcherControllerId));
    }

    /** The becomes-the-target abilities other permanents' static abilities grant {@code source} that {@code watch} reads. */
    private List<TriggeredAbility> grantedTargetAbilities(GameData gameData, Permanent source, TargetWatch watch) {
        return grantedTriggeredAbilitySupport.grantedWatchedAbilities(gameData, source, watch.trigger());
    }

    /**
     * Whether a static ability of another permanent grants {@code source} a "becomes the target of a spell or ability
     * an opponent controls" ability that acts on "that spell or ability" (a granted ward, CR 702.21a).
     */
    private boolean hasGrantedCounterUnlessTrigger(GameData gameData, Permanent source) {
        TargetWatch watch = TargetWatch.ON_BECOMES_TARGET_OF_OPPONENT_SPELL;
        return unjudgedTargetAbilities(grantedTargetAbilities(gameData, source, watch), watch).stream()
                .anyMatch(TriggerCollectionService::actsOnASpell);
    }

    /** The filter context of a predicate {@code watcher}, a permanent with a triggered ability, judges. */
    private static FilterContext watcherFilterContext(GameData gameData, Permanent watcher, UUID watcherControllerId) {
        return FilterContext.of(gameData)
                .withSourceCardId(watcher.getOriginalCard().getId())
                .withSourceControllerId(watcherControllerId)
                .withSourcePermanentId(watcher.getId())
                .withSourcePermanentSnapshot(watcher);
    }

    /**
     * An activation ability ("whenever [a player] activates an ability") of a permanent or of a card in a graveyard,
     * with the trigger condition the activation collector reads it by. The collector judges its event and player
     * ({@link #triggersOn}), its filters ({@link AbilityTrigger#kind()}, {@link AbilityTrigger#source()},
     * {@link AbilityTrigger#stackEntry()}, {@link AbilityTrigger#payment()}) and its options itself.
     */
    private record ActivationAbility(TriggeredAbility ability, AbilityTrigger trigger) {

        /**
         * Whether {@code activation} is an event of this ability's trigger for a watcher {@code watcherControllerId}
         * controls: the activating player is the one the trigger names and the ability is of the event's kind (any
         * ability, one that isn't a mana ability, an exhaust ability, an embalm or eternalize ability).
         */
        boolean triggersOn(Activation activation, UUID watcherControllerId) {
            boolean activatedByWatcherController = watcherControllerId.equals(activation.activatingPlayerId());
            boolean player = switch (trigger.player()) {
                case ANY -> true;
                case YOU -> activatedByWatcherController;
                case OPPONENT -> !activatedByWatcherController;
            };
            if (!player) {
                return false;
            }
            return switch (trigger.event()) {
                case ACTIVATED -> true;
                case NONMANA_ACTIVATED -> !activation.manaAbility();
                case EXHAUST_ACTIVATED -> activation.ability().isExhaustAbility();
                case ETERNALIZE_OR_EMBALM_ACTIVATED -> activation.ability().isEmbalmOrEternalize();
                case THIS_MANA_ABILITY_RESOLVES -> false;
            };
        }

    }

    /** The watches whose triggers the activation collector reads, in a zone each. */
    private static final List<SpellAbilityWatch> ACTIVATION_WATCHES = List.of(
            SpellAbilityWatch.ON_CONTROLLER_ACTIVATES_ABILITY,
            SpellAbilityWatch.ON_OPPONENT_ACTIVATES_ABILITY,
            SpellAbilityWatch.ON_CONTROLLER_ACTIVATES_NONMANA_ABILITY,
            SpellAbilityWatch.ON_OPPONENT_ACTIVATES_NONMANA_ABILITY,
            SpellAbilityWatch.ON_ANY_PLAYER_ACTIVATES_NONMANA_ABILITY,
            SpellAbilityWatch.ON_CONTROLLER_ACTIVATES_EXHAUST_ABILITY,
            SpellAbilityWatch.ON_CONTROLLER_ACTIVATES_ETERNALIZE_OR_EMBALM,
            SpellAbilityWatch.GRAVEYARD_ON_CONTROLLER_ACTIVATES_EXHAUST_ABILITY);

    /**
     * The activation abilities among {@code abilities} that function in {@code zone}, in the order they were added:
     * those with a trigger condition that is an activation watch's trigger in that zone, or narrows it.
     */
    private static List<ActivationAbility> activationAbilities(List<TriggeredAbility> abilities, Zone zone) {
        List<ActivationAbility> watching = new ArrayList<>();
        for (TriggeredAbility ability : abilities) {
            for (TriggerCondition condition : ability.conditions()) {
                if (condition.trigger() instanceof AbilityTrigger activation
                        && condition.zones().functionsIn(zone)
                        && ACTIVATION_WATCHES.stream().anyMatch(watch -> watch.zone() == zone
                        && watch.trigger().equals(activation.unfiltered()))) {
                    watching.add(new ActivationAbility(ability, activation));
                    break;
                }
            }
        }
        return watching;
    }

    /**
     * One activation of an activated ability, as it is once the ability is on the stack and its costs are paid, which
     * is when "whenever [a player] activates an ability" abilities trigger (CR 602.2b, CR 601.2i).
     *
     * @param activatingPlayerId             the player who activated the ability
     * @param abilityEntry                   the activated ability on the stack, with its targets and X; for a mana
     *                                       ability, which doesn't use the stack, the entry it would have been
     * @param ability                        the activated ability
     * @param activatedPermanent             the permanent whose ability it is, as it last existed when a cost removed
     *                                       it, or {@code null} for an ability of a card that isn't a permanent (in a
     *                                       hand, a graveyard, exile or the command zone; CR 109.2)
     * @param activatedPermanentControllerId that permanent's controller as the activation began; the activating
     *                                       player for an ability of a card in a hand or a graveyard
     * @param manaAbility                    whether it is a mana ability (CR 605.1a)
     */
    public record Activation(UUID activatingPlayerId, StackEntry abilityEntry, ActivatedAbility ability,
                             Permanent activatedPermanent, UUID activatedPermanentControllerId,
                             boolean manaAbility) {}

    /**
     * Hands the "whenever [a player] activates an ability" abilities {@code activation} makes trigger to the stack
     * door, each whole (CR 603.3). The one collector of every activation ability, called once per activation when the
     * activated ability is on the stack and its costs are paid (CR 602.2b, CR 601.2i): a permanent that left the
     * battlefield to pay them has no ability left to trigger. Every activation ability of every permanent, and of every
     * card in a graveyard that has one there, is read the same way whatever its effects are: its event and player are
     * matched against the activation ({@link ActivationAbility#triggersOn}), its filters and payment and its event
     * condition are judged by {@link #activationTriggers}, and its record names the activation
     * ({@link #activationRecord}); the door judges its limit and intervening "if" and chooses its targets as it goes on
     * the stack (CR 603.3d). The delayed "when you next activate ..., copy that ability" abilities trigger here too
     * ({@link #collectDelayedActivationCopies}).
     */
    public void collectActivationTriggers(GameData gameData, Activation activation) {
        if (activation.abilityEntry() == null) {
            return;
        }
        int lifePaid = gameData.activationLifePaid;
        if (!activation.manaAbility()) {
            gameData.trackingActivationLifePaid = false;
            gameData.activationLifePaid = 0;
        }
        collectDelayedActivationCopies(gameData, activation);
        boolean treasureMana = gameData.abilityActivationUsedTreasureMana
                .getOrDefault(activation.abilityEntry().getCard().getId(), false);
        gameData.forEachPermanent((ownerId, perm) -> {
            if (gameQueryService.hasLostPrintedAbilities(gameData, perm)) return;
            for (ActivationAbility watching : activationAbilities(perm.getCard().getTriggeredAbilities(),
                    Zone.BATTLEFIELD)) {
                if (!watching.triggersOn(activation, ownerId)) continue;
                ConditionContext context = ConditionContext.forPermanent(perm, ownerId)
                        .withTreasureManaSpentToActivate(treasureMana);
                if (!activationTriggers(gameData, perm, ownerId, watching, activation, lifePaid, context)) continue;
                boolean triggered = triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(
                        watching.ability(), perm.getCard(), perm, ownerId, perm.getCard().getName() + "'s ability",
                        context, activationRecord(watching, activation, lifePaid)));
                if (triggered) {
                    gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                }
            }
        });
        collectGraveyardActivationTriggers(gameData, activation, lifePaid);
        // An ability that chooses its targets as it goes on the stack (CR 603.3d) chooses them now, above the
        // activated ability; a mana ability's wait with it.
        if (!activation.manaAbility()) {
            beginStackDoorChoices(gameData);
        }
    }

    /**
     * The activation abilities that function in a graveyard (Afterburner Expert's "whenever you activate an exhaust
     * ability, return this card from your graveyard to the battlefield"), for the cards in every graveyard: their
     * owner is the "you" of the trigger. Such an ability goes to the stack door as written, with the same record as a
     * permanent's ({@link #activationRecord}); one with options, or with a filter judged from a permanent's point of
     * view, throws (no card has one).
     */
    private void collectGraveyardActivationTriggers(GameData gameData, Activation activation, int lifePaid) {
        for (UUID ownerId : gameData.orderedPlayerIds) {
            List<Card> graveyard = gameData.playerGraveyards.get(ownerId);
            if (graveyard == null) continue;
            for (Card card : new ArrayList<>(graveyard)) {
                List<TriggeredAbility> written = new ArrayList<>(card.getTriggeredAbilities());
                for (Ability granted : gameData.perpetualAbilities(card.getId())) {
                    if (granted instanceof TriggeredAbility triggered) {
                        written.add(triggered);
                    }
                }
                if (activationAbilities(written, Zone.GRAVEYARD).isEmpty()) continue;
                for (ActivationAbility watching : activationAbilities(
                        gameQueryService.getEffectiveGraveyardTriggeredAbilities(gameData, card), Zone.GRAVEYARD)) {
                    if (!watching.triggersOn(activation, ownerId)) continue;
                    AbilityTrigger trigger = watching.trigger();
                    if (trigger.source() != null || trigger.stackEntry() != null) {
                        throw new IllegalStateException(card.getName() + "'s graveyard activation ability has a "
                                + "filter judged from a permanent's point of view: " + watching.ability());
                    }
                    requireNoOptions(watching.ability(), card);
                    if (trigger.kind() != null && !trigger.kind().matches(activation.ability())) continue;
                    if (!activationPaymentMatches(trigger, activation.abilityEntry(), lifePaid)) continue;
                    boolean triggered = triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(
                            watching.ability(), card, null, ownerId, card.getName() + "'s ability",
                            ConditionContext.forCard(card, ownerId), activationRecord(watching, activation, lifePaid)));
                    if (triggered) {
                        gameLogService.append(gameData, GameLog.abilityTriggers(card));
                    }
                }
            }
        }
    }

    /**
     * The delayed triggered abilities an earlier resolution created that trigger on {@code activation} (CR 603.7): "when
     * you next activate an ability with {X} in its activation cost this turn, copy that ability" (Magus Lucea Kane),
     * and for an ability that isn't a mana ability "when you next activate a loyalty ability" and "... an exhaust
     * ability" and "until end of turn, whenever you activate an ability of a planeswalker of the chosen type" (Leori,
     * Sparktouched Hunter). Each is a stack object of its own (CR 603.7a) that copies the ability as it was activated.
     */
    private void collectDelayedActivationCopies(GameData gameData, Activation activation) {
        UUID activatingPlayerId = activation.activatingPlayerId();
        StackEntry abilityEntry = activation.abilityEntry();
        ActivatedAbility ability = activation.ability();

        Integer pendingXCopies = gameData.pendingNextXActivatedAbilityCopyThisTurnCount.get(activatingPlayerId);
        if (pendingXCopies != null && pendingXCopies > 0
                && ability.getManaCost() != null && new ManaCost(ability.getManaCost()).hasX()) {
            StackEntry snapshot = new StackEntry(abilityEntry);
            for (int i = 0; i < pendingXCopies; i++) {
                gameData.enqueueTrigger(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        abilityEntry.getCard(),
                        activatingPlayerId,
                        "Copy " + abilityEntry.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(
                                new CopyControllerActivatedAbilityEffect(snapshot, ability, activatingPlayerId)))
                ));
            }
            gameData.pendingNextXActivatedAbilityCopyThisTurnCount.remove(activatingPlayerId);
            consumeLinkedXSpellCopies(gameData, activatingPlayerId, pendingXCopies);
            gameLogService.append(gameData, GameLog.cardThen(abilityEntry.getCard(), "'s ability is copied."));
            log.info("Game {} - {} delayed X-ability copy trigger(s) queued for {}",
                    gameData.id, pendingXCopies, abilityEntry.getCard().getName());
        }
        if (activation.manaAbility()) {
            return;
        }

        Integer pendingLoyaltyCopies = gameData.pendingNextLoyaltyAbilityCopyThisTurnCount.get(activatingPlayerId);
        if (pendingLoyaltyCopies != null && pendingLoyaltyCopies > 0 && ability.getLoyaltyCost() != null) {
            StackEntry snapshot = new StackEntry(abilityEntry);
            List<GameData.PendingCopySource> sources =
                    gameData.pendingNextLoyaltyAbilityCopySourcesThisTurn.getOrDefault(activatingPlayerId, List.of());
            for (int i = 0; i < pendingLoyaltyCopies; i++) {
                GameData.PendingCopySource source = i < sources.size() ? sources.get(i) : null;
                Card copySourceCard = source != null ? source.card() : abilityEntry.getCard();
                UUID copySourcePermanentId = source != null ? source.sourcePermanentId() : null;
                gameData.enqueueTrigger(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        copySourceCard,
                        activatingPlayerId,
                        "Copy " + abilityEntry.getCard().getName() + "'s loyalty ability",
                        List.of(new CopyControllerActivatedAbilityEffect(snapshot, ability, activatingPlayerId)),
                        0,
                        copySourcePermanentId
                ));
            }
            gameData.pendingNextLoyaltyAbilityCopyThisTurnCount.remove(activatingPlayerId);
            gameData.pendingNextLoyaltyAbilityCopySourcesThisTurn.remove(activatingPlayerId);
            if (gameData.pendingNextSpellOrLoyaltyCopyLinkedThisTurn.remove(activatingPlayerId)) {
                gameData.pendingNextInstantSorceryCopyThisTurnCount.remove(activatingPlayerId);
                gameData.pendingNextInstantSorceryCopyThisTurnSingleTriggerCount.remove(activatingPlayerId);
            }
        }

        Integer pendingExhaustCopies = gameData.pendingNextExhaustAbilityCopyThisTurnCount.get(activatingPlayerId);
        if (pendingExhaustCopies != null && pendingExhaustCopies > 0 && ability.isExhaustAbility()) {
            StackEntry snapshot = new StackEntry(abilityEntry);
            List<GameData.PendingCopySource> sources = gameData.pendingNextExhaustAbilityCopySourcesThisTurn
                    .getOrDefault(activatingPlayerId, List.of());
            for (int i = 0; i < pendingExhaustCopies; i++) {
                GameData.PendingCopySource source = i < sources.size() ? sources.get(i) : null;
                gameData.enqueueTrigger(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        source != null ? source.card() : abilityEntry.getCard(),
                        activatingPlayerId,
                        "Copy " + abilityEntry.getCard().getName() + "'s exhaust ability",
                        new ArrayList<>(List.of(
                                new CopyControllerActivatedAbilityEffect(snapshot, ability, activatingPlayerId))),
                        0,
                        source != null ? source.sourcePermanentId() : null
                ));
            }
            gameData.pendingNextExhaustAbilityCopyThisTurnCount.remove(activatingPlayerId);
            gameData.pendingNextExhaustAbilityCopySourcesThisTurn.remove(activatingPlayerId);
            gameLogService.append(gameData, GameLog.cardThen(abilityEntry.getCard(), "'s exhaust ability is copied."));
            log.info("Game {} - {} delayed exhaust-ability copy trigger(s) queued for {}",
                    gameData.id, pendingExhaustCopies, abilityEntry.getCard().getName());
        }

        Permanent activatedPermanent = activation.activatedPermanent();
        for (GameData.ChosenSubtypeActivationCopy delayedCopy : gameData.chosenSubtypeActivationCopiesThisTurn) {
            if (!delayedCopy.controllerId().equals(activatingPlayerId) || activatedPermanent == null
                    || !predicateEvaluationService.matchesPermanentPredicate(activatedPermanent,
                    new PermanentAllOfPredicate(List.of(
                            new PermanentIsPlaneswalkerPredicate(),
                            new PermanentHasSubtypePredicate(delayedCopy.subtype()))),
                    FilterContext.of(gameData).withSourceControllerId(activatingPlayerId))) {
                continue;
            }
            gameData.enqueueTrigger(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    delayedCopy.sourceCard(),
                    activatingPlayerId,
                    delayedCopy.sourceCard().getName() + "'s delayed ability",
                    new ArrayList<>(List.of(new CopyControllerActivatedAbilityEffect(
                            new StackEntry(abilityEntry), ability, activatingPlayerId))),
                    null,
                    delayedCopy.sourcePermanentId()
            ));
            gameLogService.append(gameData, GameLog.abilityTriggers(delayedCopy.sourceCard()));
            log.info("Game {} - {} delayed trigger copies {}'s ability", gameData.id,
                    delayedCopy.sourceCard().getName(), abilityEntry.getCard().getName());
        }
    }

    /**
     * Whether {@code watching}, an ability of {@code watcher}, triggers on {@code activation}: the kind of ability ("a
     * loyalty ability", "without {T} in its activation cost"), the permanent whose ability it is ("an ability of an
     * artifact", judged as it last existed and from {@code watcher}'s point of view; an ability of a card that isn't a
     * permanent has none, CR 109.2), the activated ability on the stack with its targets ("that targets only Bill
     * Potts") and what was paid to activate it ("if life was paid to activate it", {@code lifePaid}) must match the
     * trigger's filters, and its event condition (CR 603.2) must be true in {@code context}. The stack door judges its
     * limit (CR 603.2h) and intervening "if" (CR 603.4); "if mana from a Treasure was spent to activate it" is a fact of
     * the activation, which {@code context} carries.
     */
    private boolean activationTriggers(GameData gameData, Permanent watcher, UUID watcherControllerId,
                                       ActivationAbility watching, Activation activation, int lifePaid,
                                       ConditionContext context) {
        AbilityTrigger trigger = watching.trigger();
        if (trigger.kind() != null && !trigger.kind().matches(activation.ability())) {
            return false;
        }
        if (trigger.source() != null) {
            if (activation.activatedPermanent() == null) return false;
            if (!predicateEvaluationService.matchesPermanentPredicate(activation.activatedPermanent(),
                    trigger.source(), activatedPermanentFilterContext(gameData, watcher, watcherControllerId,
                            activation.activatedPermanent()))) {
                return false;
            }
        }
        if (trigger.stackEntry() != null && !targetLegalityService.matchesStackEntryPredicate(
                gameData, activation.abilityEntry(), trigger.stackEntry(), watcherControllerId, watcher)) {
            return false;
        }
        if (!activationPaymentMatches(trigger, activation.abilityEntry(), lifePaid)) {
            return false;
        }
        TriggerOptions options = watching.ability().options();
        if (options.multiplicity() != TriggerMultiplicity.EACH) {
            throw new IllegalStateException(watcher.getCard().getName() + "'s activation ability has a multiplicity "
                    + "the activation collector doesn't judge: " + options);
        }
        return conditionEvaluationService.eventConditionMet(gameData, options, context);
    }

    /**
     * The filter context {@code watcher} judges {@code activatedPermanent}, the permanent whose ability was activated,
     * with. An Aura or Equipment that was attached to that permanent when paying the costs took it off the battlefield
     * is attached to nothing as the ability becomes activated (it stays on the battlefield until state-based actions
     * are checked, CR 704.5m and CR 704.5n), so its "an ability of enchanted artifact" or "of equipped creature"
     * names no permanent: it is judged as it is now, unattached, not by the permanent it was last attached to.
     */
    private FilterContext activatedPermanentFilterContext(GameData gameData, Permanent watcher,
                                                          UUID watcherControllerId, Permanent activatedPermanent) {
        if (!activatedPermanent.getId().equals(watcher.getAttachedTo())
                || gameQueryService.findPermanentById(gameData, activatedPermanent.getId()) != null) {
            return watcherFilterContext(gameData, watcher, watcherControllerId);
        }
        Permanent unattached = new Permanent(watcher);
        unattached.setAttachedTo(null);
        return FilterContext.of(gameData)
                .withSourceControllerId(watcherControllerId)
                .withSourcePermanentSnapshot(unattached);
    }

    /**
     * Whether the activation on the stack as {@code abilityEntry} paid what the payment filter of {@code trigger}
     * requires: life ({@code lifePaid}, Verrak, Warped Sengir), or one or more permanents sacrificed for its costs
     * (Ashnod the Uncaring): the entry records every one, the source, the chosen ones and the granting Equipment
     * alike; true without one.
     */
    private static boolean activationPaymentMatches(AbilityTrigger trigger, StackEntry abilityEntry, int lifePaid) {
        AbilityTrigger.ActivationPayment payment = trigger.payment();
        if (payment == null) {
            return true;
        }
        return switch (payment) {
            case LIFE -> lifePaid > 0;
            case SACRIFICE -> !abilityEntry.getSacrificedCardIds().isEmpty();
        };
    }

    /**
     * The record of {@code watching}, an activation ability, for {@code activation}, stamped on its entry whatever its
     * effects are: the activated ability on the stack as its triggering spell and the {@link ActivatedAbility} ("copy
     * that ability" copies it with its targets and X for this ability's controller, CR 707.10), the permanent whose
     * ability it is with its controller ("that artifact's controller", read as the ability resolves or as the
     * permanent last existed, CR 608.2h), the life paid to activate it as its event value ("pay that much life") and
     * the activating player as the non-targeting "that player" of an "an opponent activates" trigger. An effect that acts
     * on "that ability" is aimed at it by the stack door.
     */
    private static TriggerRecord activationRecord(ActivationAbility watching, Activation activation, int lifePaid) {
        return entry -> {
            entry.setNonTargeting(true);
            if (watching.trigger().player() == com.github.laxika.magicalvibes.model.trigger.PlayerRelation.OPPONENT) {
                entry.setTargetId(activation.activatingPlayerId());
            }
            entry.setTriggeringSpell(activation.abilityEntry());
            entry.setTriggeringActivatedAbility(activation.ability());
            if (lifePaid > 0) {
                entry.setEventValue(lifePaid);
            }
            if (activation.activatedPermanent() != null) {
                entry.setTriggeringPermanentId(activation.activatedPermanent().getId());
            }
            entry.setTriggeringPermanentControllerId(activation.activatedPermanentControllerId());
        };
    }

    // ── Life-loss triggers ─────────────────────────────────────────────

    public void checkLifeLossTriggers(GameData gameData, UUID losingPlayerId, int lifeLostAmount) {
        if (lifeLostAmount <= 0) return;

        // Accumulate life lost this turn (damage funnels through here too — "damage causes loss of
        // life"). Read by Wound Reflection at end of turn.
        gameData.lifeLostThisTurn.merge(losingPlayerId, lifeLostAmount, Integer::sum);
        increaseActivePlayerSpeedIfEligible(gameData, losingPlayerId);

        boolean[] anyTriggered = {false};
        var ctx = new TriggerContext.LifeLoss(losingPlayerId, lifeLostAmount);

        // Snapshot: abilities may modify the battlefield (e.g. Mindcrank mills → Undead Alchemist creates tokens)
        gameData.forEachBattlefield((playerId, battlefield) -> {
            if (playerId.equals(losingPlayerId)) return;

            for (Permanent perm : List.copyOf(battlefield)) {
                for (TriggeredAbility ability : printedWatchedAbilities(gameData, perm,
                        LifeWatch.ON_OPPONENT_LOSES_LIFE)) {
                    if (collectOpponentLifeLossAbility(gameData, perm, playerId, ability, ctx)) {
                        anyTriggered[0] = true;
                    }
                }
            }
        });

        // Controller-loses-life triggers (e.g. Lich's Mastery)
        // Snapshot: abilities may modify the battlefield (exile permanents)
        gameData.forEachBattlefield((playerId, battlefield) -> {
            if (!playerId.equals(losingPlayerId)) return;

            for (Permanent perm : List.copyOf(battlefield)) {
                for (TriggeredAbility ability : printedWatchedAbilities(gameData, perm,
                        LifeWatch.ON_CONTROLLER_LOSES_LIFE)) {
                    LifeTrigger trigger = LifeAbilityReader.trigger(ability, LifeWatch.ON_CONTROLLER_LOSES_LIFE.trigger());
                    // "For the first time each turn": the player's first loss of life this turn, counting those before
                    // the source was on the battlefield.
                    if (trigger.firstEachTurn() && gameData.lifeLostThisTurn.getOrDefault(playerId, 0) != lifeLostAmount) {
                        continue;
                    }
                    if (putLifeAbility(gameData, perm, playerId, ability, losingPlayerId, lifeLostAmount, ctx)) {
                        anyTriggered[0] = true;
                    }
                }
            }
        });

        if (anyTriggered[0]) {
            gameOutcomeService.checkWinCondition(gameData);
        }
    }

    /**
     * Hands one "whenever an opponent loses life" ability of {@code perm} to the stack door. "For the first time during
     * each of their turns" (Valgavoth) is judged here: the losing player is the active player, it is their first loss
     * of life this turn, counting those before the source was on the battlefield, and this ability hasn't triggered
     * for them this turn yet (the mark is the ability's, by its {@link TriggeredAbility#limitKey()} and the player, so
     * a second such ability of the permanent triggers too).
     */
    private boolean collectOpponentLifeLossAbility(GameData gameData, Permanent perm, UUID controllerId,
                                                   TriggeredAbility ability, TriggerContext.LifeLoss ctx) {
        LifeTrigger trigger = LifeAbilityReader.trigger(ability, LifeWatch.ON_OPPONENT_LOSES_LIFE.trigger());
        String firstLossKey = null;
        if (trigger.event() == LifeTrigger.LifeEvent.LOSES_FOR_FIRST_TIME_DURING_THEIR_TURN) {
            if (!ctx.losingPlayerId().equals(gameData.activePlayerId)
                    || gameData.lifeLostThisTurn.getOrDefault(ctx.losingPlayerId(), 0) - ctx.lifeLostAmount() > 0) {
                return false;
            }
            firstLossKey = ability.limitKey() + ":" + ctx.losingPlayerId();
            Set<String> fired = gameData.keyedOncePerTurnTriggersFiredThisTurn.get(perm.getId());
            if (fired != null && fired.contains(firstLossKey)) {
                return false;
            }
        }
        boolean triggered = putLifeAbility(gameData, perm, controllerId, ability, ctx.losingPlayerId(),
                ctx.lifeLostAmount(), ctx);
        if (triggered && firstLossKey != null) {
            gameData.keyedOncePerTurnTriggersFiredThisTurn.computeIfAbsent(perm.getId(),
                    ignored -> java.util.concurrent.ConcurrentHashMap.newKeySet()).add(firstLossKey);
        }
        return triggered;
    }

    /**
     * Hands {@code ability}, a life gain, loss or payment ability of {@code perm} controlled by {@code controllerId}, to
     * the stack door for {@code event}, {@code amount} life of {@code playerId}: its event condition ("during your
     * turn", a Class's level, "exactly 1 life") is judged with the player as "that player" and the amount as the event's
     * value. The record names, whatever the effects are, the player as the triggering player and the non-targeting
     * "that player" (which the {@code TRIGGERING_PLAYER} recipients read), the amount as the event's value ("that
     * much", "that many") and the source as it was when the ability triggered; the door marks "do this only once each
     * turn" as the action is taken (CR 603.2h) and chooses a target the card declares as it goes on the stack
     * (CR 603.3d).
     */
    private boolean putLifeAbility(GameData gameData, Permanent perm, UUID controllerId, TriggeredAbility ability,
                                   UUID playerId, int amount, TriggerContext event) {
        ConditionContext context = ConditionContext.forPermanent(perm, controllerId).withTargetId(playerId)
                .withEventValue(amount);
        Permanent snapshot = new Permanent(perm);
        return putEventAbility(gameData, ability, perm.getCard(), perm, controllerId, context, event, entry -> {
            entry.setNonTargeting(true);
            entry.setTargetId(playerId);
            entry.setEventValue(amount);
            entry.setTriggeringPlayerId(playerId);
            entry.setSourcePermanentSnapshot(snapshot);
        });
    }

    // ── Life-gain triggers ──────────────────────────────────────────────

    /** Fires triggers for life actually paid by a player, distinct from ordinary life loss. */
    public void checkLifePaymentTriggers(GameData gameData, UUID payingPlayerId, int lifePaidAmount) {
        if (lifePaidAmount <= 0) return;
        if (gameData.trackingActivationLifePaid) {
            gameData.activationLifePaid += lifePaidAmount;
        }

        var ctx = new TriggerContext.LifePayment(payingPlayerId, lifePaidAmount);
        List<Permanent> battlefield = gameData.playerBattlefields.get(payingPlayerId);
        if (battlefield == null) return;

        for (Permanent perm : List.copyOf(battlefield)) {
            for (TriggeredAbility ability : watchedAbilities(gameData, perm, LifeWatch.ON_CONTROLLER_PAYS_LIFE)) {
                putLifeAbility(gameData, perm, payingPlayerId, ability, payingPlayerId, lifePaidAmount, ctx);
            }
        }
    }

    /** Fires triggers when an opponent pays mana imposed by a spell or permanent controlled by a player. */
    public void checkTaxPaymentTriggers(GameData gameData, UUID payingPlayerId,
                                        UUID taxingPlayerId, int manaPaid) {
        if (payingPlayerId == null || taxingPlayerId == null
                || payingPlayerId.equals(taxingPlayerId) || manaPaid <= 0) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(taxingPlayerId);
        if (battlefield == null) return;

        var ctx = new TriggerContext.TaxPayment(payingPlayerId, taxingPlayerId, manaPaid);
        ActionWatch watch = ActionWatch.ON_OPPONENT_PAYS_TAX;
        for (Permanent perm : List.copyOf(battlefield)) {
            List<List<CardEffect>> abilities = new ArrayList<>(actionAbilities(perm.getCard(), watch));
            abilities.addAll(byAbility(
                    grantedTriggeredAbilitySupport.grantedWatchedAbilities(gameData, perm, watch.trigger()),
                    granted -> ActionAbilityReader.effects(granted, watch.trigger())));
            for (List<CardEffect> effects : abilities) {
                dispatch(new TriggerMatchContext(gameData, perm, taxingPlayerId, effects), watch, ctx);
            }
        }
    }

    private void increaseActivePlayerSpeedIfEligible(GameData gameData, UUID losingPlayerId) {
        UUID activePlayerId = gameData.activePlayerId;
        if (activePlayerId == null || activePlayerId.equals(losingPlayerId)) {
            return;
        }
        int speed = gameData.playerSpeeds.getOrDefault(activePlayerId, 0);
        if (speed < 1 || speed >= 4 || !gameData.playersWhoseSpeedIncreasedThisTurn.add(activePlayerId)) {
            return;
        }
        Card source = new Card();
        source.setName("Speed");
        source.setOwnerId(activePlayerId);
        source.freeze();
        StackEntry trigger = new StackEntry(StackEntryType.TRIGGERED_ABILITY, source, activePlayerId,
                "Increase speed", List.of(new com.github.laxika.magicalvibes.model.effect.IncreaseControllerSpeedEffect()),
                (UUID) null, (UUID) null);
        trigger.setNonTargeting(true);
        gameData.enqueueTrigger(trigger);
    }

    public void checkLifeGainTriggers(GameData gameData, UUID gainingPlayerId, int lifeGainedAmount) {
        checkLifeGainTriggers(gameData, gainingPlayerId, lifeGainedAmount, null, null);
    }

    public void checkLifeGainTriggers(GameData gameData, UUID gainingPlayerId, int lifeGainedAmount,
            Card sourceCard, StackEntryType sourceEntryType) {
        if (lifeGainedAmount <= 0) return;

        var ctx = new TriggerContext.LifeGain(gainingPlayerId, lifeGainedAmount, sourceCard, sourceEntryType);

        gameData.forEachBattlefield((playerId, battlefield) -> {
            if (!playerId.equals(gainingPlayerId)) return;

            LifeWatch watch = LifeWatch.ON_CONTROLLER_GAINS_LIFE;
            for (Permanent perm : List.copyOf(battlefield)) {
                for (TriggeredAbility ability : watchedAbilities(gameData, perm, watch)) {
                    LifeTrigger trigger = LifeAbilityReader.trigger(ability, watch.trigger());
                    if (trigger.firstEachTurn()
                            && gameData.getLifeGainedThisTurn(gainingPlayerId) != lifeGainedAmount) {
                        continue;
                    }
                    // "Whenever a [filter] spell causes you to gain life": the spell itself, not an ability of its card.
                    if (trigger.spellFilter() != null
                            && !lifeGainCausedBySpell(gameData, perm, ctx, trigger.spellFilter())) {
                        continue;
                    }
                    putLifeAbility(gameData, perm, playerId, ability, gainingPlayerId, lifeGainedAmount, ctx);
                }
            }
        });

        gameData.forEachBattlefield((playerId, battlefield) -> {
            if (playerId.equals(gainingPlayerId)) return;

            for (Permanent perm : List.copyOf(battlefield)) {
                for (TriggeredAbility ability : watchedAbilities(gameData, perm, LifeWatch.ON_OPPONENT_GAINS_LIFE)) {
                    putLifeAbility(gameData, perm, playerId, ability, gainingPlayerId, lifeGainedAmount, ctx);
                }
            }
        });

        collectGraveyardControllerLifeGainTriggers(gameData, gainingPlayerId, lifeGainedAmount);
        collectGraveyardOpponentLifeGainTriggers(gameData, gainingPlayerId, lifeGainedAmount);
        collectLifeGainOpponentLifeLossTriggers(gameData, gainingPlayerId, lifeGainedAmount);
        collectTemporaryGlobalTriggers(gameData, LifeWatch.ON_ANY_PLAYER_GAINS_LIFE, gainingPlayerId, lifeGainedAmount);
        collectEmblemLifeGainTriggers(gameData, gainingPlayerId, lifeGainedAmount);
    }

    /**
     * Whether the life gain {@code lifeGain} was caused by a spell matching {@code spellFilter}, judged from the
     * ability's source {@code perm}: the source of the life gain is a spell's stack entry (the spell resolving, or its
     * lifelink damage), not a triggered or activated ability of its card.
     */
    private boolean lifeGainCausedBySpell(GameData gameData, Permanent perm, TriggerContext.LifeGain lifeGain,
                                          CardPredicate spellFilter) {
        return lifeGain.sourceCard() != null && lifeGain.sourceEntryType() != null
                && lifeGain.sourceEntryType() != StackEntryType.TRIGGERED_ABILITY
                && lifeGain.sourceEntryType() != StackEntryType.ACTIVATED_ABILITY
                && predicateEvaluationService.matchesCardPredicate(lifeGain.sourceCard(), spellFilter,
                perm.getCard().getId(), gameData, lifeGain.sourceCard().getOwnerId(), perm.getId(), null);
    }

    /**
     * The emblems' "whenever you gain life" abilities ({@code Triggers.youGainLife()}; Professor Dellian Fel's emblem)
     * for {@code lifeGainedAmount} life {@code gainingPlayerId} gained. Abilities of emblems function in the command zone
     * (CR 114.4), so the permanent sweep of {@link #checkLifeGainTriggers} can't see them. Each one goes on the stack
     * once as one object of its effects (CR 603.3), the emblem's source card as its source, "that much" its event
     * value; a targeted ability ("target opponent", the ability's target filter) chooses its target as it goes on the
     * stack (CR 603.3d). An emblem isn't a permanent (CR 110.1), so nothing makes its ability trigger additional times.
     * The reader judges no options; an ability with one fails loudly instead of triggering wrongly.
     */
    private void collectEmblemLifeGainTriggers(GameData gameData, UUID gainingPlayerId,
                                                int lifeGainedAmount) {
        Trigger watched = Triggers.youGainLife();
        for (Emblem emblem : List.copyOf(gameData.emblems)) {
            if (!emblem.controllerId().equals(gainingPlayerId)) continue;

            for (TriggeredAbility ability : emblem.triggeredAbilities()) {
                if (ability.conditions().stream().noneMatch(condition -> condition.trigger().equals(watched))) {
                    continue;
                }
                if (!ability.options().equals(TriggerOptions.DEFAULT)) {
                    throw new IllegalStateException(emblemName(emblem) + " has a life-gain ability its reader "
                            + "doesn't judge: " + ability);
                }
                Card source = emblem.sourceCard();
                String description = emblemName(emblem);
                List<CardEffect> effects = ability.effects().stream().map(EffectRegistration::effect).toList();
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        source,
                        gainingPlayerId,
                        description,
                        new ArrayList<>(effects));
                entry.setEventValue(lifeGainedAmount);
                if (effects.stream().anyMatch(effect -> effect.targetSpec() != TargetSpec.NONE)) {
                    boolean playerTargetOnly = effects.stream()
                            .noneMatch(effect -> effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT));
                    queueTargetChoice(gameData, entry, new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                            source, gainingPlayerId, new ArrayList<>(effects), playerTargetOnly,
                            ability.targetFilter(), 0, null, null, false, null, null, gainingPlayerId,
                            (PlanarObject) null).withEventValue(lifeGainedAmount));
                } else {
                    gameData.enqueueTrigger(entry);
                }
                gameLogService.append(gameData, GameLog.text(description + " triggers."));
                log.info("Game {} - {} triggers on life gain ({} life)",
                        gameData.id, description, lifeGainedAmount);
            }
        }
        if (gameData.hasPendingInteraction(PermanentChoiceContext.SpellTargetTriggerAnyTarget.class)
                && !gameData.interaction.isAwaitingInput()) {
            processNextSpellTargetTrigger(gameData);
        }
    }

    /** Fires triggers for a player's positive energy-counter change. */
    public void checkEnergyGainTriggers(GameData gameData, UUID gainingPlayerId, int energyGainedAmount) {
        if (energyGainedAmount <= 0) return;

        var ctx = new TriggerContext.EnergyGain(gainingPlayerId, energyGainedAmount);
        List<Permanent> battlefield = gameData.playerBattlefields.get(gainingPlayerId);
        if (battlefield == null) return;

        for (Permanent perm : List.copyOf(battlefield)) {
            // "Whenever you get one or more {E}" (Territorial Gorger, Fabrication Module's target, Brotherhood
            // Scribe's intervening "if"): the player and the energy gained ("that much").
            putCounterAbilities(gameData, perm, gainingPlayerId, CounterWatch.ON_CONTROLLER_GETS_ENERGY, ctx,
                    ConditionContext.forPermanent(perm, gainingPlayerId).withEventValue(energyGainedAmount),
                    eventRecord(perm, null, gainingPlayerId, energyGainedAmount, null, null));
        }
    }

    /** Fires controller-scoped triggers once for each proliferate event. */
    public void checkProliferateTriggers(GameData gameData, UUID proliferatingPlayerId,
                                         int proliferateCount) {
        if (proliferateCount <= 0) return;

        TriggerContext.Proliferate ctx = new TriggerContext.Proliferate(proliferatingPlayerId);
        List<Permanent> battlefield = gameData.playerBattlefields.get(proliferatingPlayerId);
        if (battlefield != null) {
            for (Permanent perm : List.copyOf(battlefield)) {
                for (int i = 0; i < proliferateCount; i++) {
                    putCounterAbilities(gameData, perm, proliferatingPlayerId,
                            CounterWatch.ON_CONTROLLER_PROLIFERATES, ctx,
                            ConditionContext.forPermanent(perm, proliferatingPlayerId),
                            eventRecord(perm, null, proliferatingPlayerId, null, null, null));
                }
            }
        }

        List<Card> graveyard = gameData.playerGraveyards.get(proliferatingPlayerId);
        if (graveyard == null) return;

        for (Card card : new ArrayList<>(graveyard)) {
            for (int i = 0; i < proliferateCount; i++) {
                putGraveyardCounterAbilities(gameData, card, proliferatingPlayerId,
                        CounterWatch.GRAVEYARD_ON_CONTROLLER_PROLIFERATES, ctx);
            }
        }
    }

    /** Hands the "whenever an opponent gains life" abilities of the cards in the other players' graveyards to the door. */
    private void collectGraveyardOpponentLifeGainTriggers(GameData gameData, UUID gainingPlayerId,
                                                          int lifeGainedAmount) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(gainingPlayerId)) continue;

            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null) continue;

            LifeWatch watch = LifeWatch.GRAVEYARD_ON_OPPONENT_GAINS_LIFE;
            for (Card card : new ArrayList<>(graveyard)) {
                for (TriggeredAbility ability : graveyardLifeAbilities(gameData, card, watch)) {
                    putGraveyardLifeAbility(gameData, card, playerId, ability, gainingPlayerId, lifeGainedAmount);
                }
            }
        }
    }

    /**
     * Fires the turn-scoped "whenever you gain life this turn, each opponent loses that much life"
     * delayed triggers (Vizkopa Guildmage). One trigger per watcher whose controller is the player
     * who gained life; the amount rides on the entry's event value.
     */
    private void collectLifeGainOpponentLifeLossTriggers(GameData gameData, UUID gainingPlayerId,
                                                        int lifeGainedAmount) {
        for (LifeGainOpponentLifeLossWatcher watcher : List.copyOf(gameData.lifeGainOpponentLifeLossWatchers)) {
            if (!watcher.controllerId().equals(gainingPlayerId)) {
                continue;
            }
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    watcher.sourceCard(),
                    watcher.controllerId(),
                    watcher.sourceCard().getName() + "'s ability",
                    new ArrayList<>(List.of(new LoseLifeEffect(new EventValue(), LoseLifeRecipient.EACH_OPPONENT))),
                    (UUID) null,
                    (UUID) null);
            entry.setEventValue(lifeGainedAmount);
            gameData.enqueueTrigger(entry);
            gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
            log.info("Game {} - {} triggers (controller gained {} life), each opponent loses that much life",
                    gameData.id, watcher.sourceCard().getName(), lifeGainedAmount);
        }
    }

    // ── Creature-card-milled triggers ─────────────────────────────────

    // ── Noncombat-damage-to-opponent triggers ──────────────────────────

    public void checkNoncombatDamageToOpponentTriggers(GameData gameData, UUID damagedPlayerId) {
        checkNoncombatDamageToOpponentTriggers(gameData, damagedPlayerId, null, 0);
    }

    public void checkNoncombatDamageToOpponentTriggers(GameData gameData, UUID damagedPlayerId,
            UUID sourceControllerId, int damageAmount) {
        var ctx = new TriggerContext.NoncombatDamageToOpponent(
                damagedPlayerId, sourceControllerId, damageAmount);

        gameData.forEachBattlefield((playerId, battlefield) -> {
            if (playerId.equals(damagedPlayerId)) return;

            for (Permanent perm : List.copyOf(battlefield)) {
                putPlayerDamageAbilities(gameData, perm, playerId, DamageWatch.ON_OPPONENT_DEALT_NONCOMBAT_DAMAGE,
                        ctx, damagedPlayerId, damagedPlayerId, damageAmount, null, sourceControllerId);
            }
        });

        if (sourceControllerId == null || sourceControllerId.equals(damagedPlayerId)) return;

        putGraveyardDamageAbilities(gameData, sourceControllerId,
                DamageWatch.GRAVEYARD_ON_ALLY_SOURCE_DEALS_NONCOMBAT_DAMAGE_TO_OPPONENT, ctx);
    }

    /**
     * Hands each ability {@code watch} reads of the cards in {@code controllerId}'s graveyard (their own and their
     * perpetual ones; none while cards in graveyards have lost all abilities) to the stack door for {@code event}, the
     * record naming this graveyard incarnation of the card (CR 400.7). No graveyard damage ability is written with a
     * filter or a multiplicity; this throws on one.
     */
    private void putGraveyardDamageAbilities(GameData gameData, UUID controllerId, DamageWatch watch,
                                             TriggerContext event) {
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        if (graveyard == null) return;

        for (Card card : new ArrayList<>(graveyard)) {
            for (TriggeredAbility ability : WatchedAbilities.withWatchedTrigger(
                    gameQueryService.getEffectiveGraveyardTriggeredAbilities(gameData, card), watch.trigger(),
                    watch.zone())) {
                requireNothingToJudge(ability, watch);
                long graveyardEntryVersion = gameData.graveyardEntryVersion(card.getId());
                putEventAbility(gameData, ability, card, null, controllerId, ConditionContext.forCard(card, controllerId),
                        event, entry -> entry.setSourceGraveyardEntryVersion(graveyardEntryVersion));
            }
        }
    }

    /**
     * Chandra's Phoenix: "Whenever an opponent is dealt damage by a red instant or sorcery spell you
     * control or by a red planeswalker you control, return this card from your graveyard to your hand."
     *
     * <p>{@code entry} is the stack entry that dealt the damage. It qualifies when it is a red instant
     * or sorcery spell, or an ability whose source card is a red planeswalker. The graveyard scanned is
     * the entry controller's ("you control"), and the damaged player must be one of their opponents.
     */
    public void checkRedSpellOrPlaneswalkerDamageToOpponentTriggers(GameData gameData, UUID damagedPlayerId,
                                                                    StackEntry entry) {
        if (entry == null) return;

        UUID controllerId = entry.getControllerId();
        if (controllerId == null || controllerId.equals(damagedPlayerId)) return;

        Card sourceCard = entry.getEffectiveDamageSourceCard();
        if (sourceCard == null) return;
        // An ability's source permanent is judged by its current characteristics (a planeswalker
        // made blue by Prismatic Lace isn't red), falling back to last-known information.
        Permanent sourcePermanent = entry.getSourcePermanentId() == null ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (sourcePermanent == null) {
            sourcePermanent = entry.getSourcePermanentSnapshot();
        }
        boolean red = sourcePermanent != null
                ? gameQueryService.getEffectiveColors(gameData, sourcePermanent).contains(CardColor.RED)
                : sourceCard.getColors().contains(CardColor.RED);
        if (!red) return;

        boolean redSpell = entry.getEntryType() == StackEntryType.INSTANT_SPELL
                || entry.getEntryType() == StackEntryType.SORCERY_SPELL;
        boolean redPlaneswalker = sourcePermanent != null
                ? gameQueryService.isPlaneswalker(gameData, sourcePermanent)
                : sourceCard.hasType(CardType.PLANESWALKER);
        if (!redSpell && !redPlaneswalker) return;

        putGraveyardDamageAbilities(gameData, controllerId,
                DamageWatch.GRAVEYARD_ON_OPPONENT_DAMAGED_BY_RED_SPELL_OR_PLANESWALKER,
                new TriggerContext.DamageToControllerAmount(damagedPlayerId, 0));
    }

    /**
     * Whether {@code entry} is a backup ability: the "when this creature enters, put N +1/+1 counters on target
     * creature" ability of its card (CR 702.165a), not another triggered ability of a creature with backup.
     */
    private static boolean isBackupAbility(StackEntry entry) {
        return entry.getEffectsToResolve().stream().anyMatch(entry.getCard().getOwnEntryEffects()::contains);
    }

    private void collectAllyCreatureBecomesTargetOfBackupAbilityTriggers(
            GameData gameData, Permanent targetPermanent, UUID creatureControllerId, StackEntry triggeringEntry) {
        if (!gameQueryService.isCreature(gameData, targetPermanent)
                || triggeringEntry.getEntryType() != StackEntryType.TRIGGERED_ABILITY
                || triggeringEntry.getCard() == null
                || !triggeringEntry.getCard().getKeywords().contains(Keyword.BACKUP)
                || !isBackupAbility(triggeringEntry)) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(creatureControllerId);
        if (battlefield == null) return;

        for (Permanent source : List.copyOf(battlefield)) {
            TargetWatch watch = TargetWatch.ON_ALLY_CREATURE_BECOMES_TARGET_OF_BACKUP_ABILITY;
            List<TriggeredAbility> abilities = new ArrayList<>(TargetAbilityReader.printed(source.getCard(), watch));
            abilities.addAll(grantedTargetAbilities(gameData, source, watch));
            // "This ability triggers only once each turn" (Mirror-Shield Hoplite) is that ability's limit, judged and
            // marked by the door. "Copy that ability" is the backup ability, the record's non-targeting target.
            for (TargetEffect read : targetReads(abilities, watch)) {
                TargetAbilityReader.requireWatchedEvent(read, watch.trigger());
                TargetAbilityReader.requireNoFilter(read, watch.trigger());
                putTargetAbility(gameData, read.ability(), source, creatureControllerId, "'s triggered ability",
                        triggeringEntry, targetPermanent, creatureControllerId,
                        entry -> entry.setTargetId(triggeringEntry.getTargetableId()));
            }
        }
    }


    /**
     * Bloodfeather Phoenix: "Whenever an instant or sorcery spell you control deals damage to an
     * opponent or battle". The caller has already batched the spell's damage event and supplies
     * whether that event hit a qualifying recipient.
     */
    public void checkInstantOrSorceryDamageToOpponentOrBattleTriggers(GameData gameData,
                                                                       Card sourceCard,
                                                                       UUID sourceControllerId,
                                                                       boolean damagedOpponentOrBattle) {
        if (!damagedOpponentOrBattle || sourceCard == null || sourceControllerId == null
                || (!sourceCard.hasType(CardType.INSTANT) && !sourceCard.hasType(CardType.SORCERY))) {
            return;
        }
        putGraveyardDamageAbilities(gameData, sourceControllerId,
                DamageWatch.GRAVEYARD_ON_ALLY_INSTANT_OR_SORCERY_DEALS_DAMAGE_TO_OPPONENT_OR_BATTLE,
                new TriggerContext.SourceDealsDamage(sourceCard, sourceControllerId, null, 0, Map.of(), null,
                        Map.of(), false));
    }

    // ── Queue-processing delegates ─────────────────────────────────────

    public void processNextDeathTriggerTarget(GameData gameData) {
        triggeredAbilityQueueService.processNextDeathTriggerTarget(gameData);
    }

    public boolean beginDeathGraveyardTargetAfterPermanent(GameData gameData,
            PermanentChoiceContext.DeathTriggerTarget pending, UUID permanentTargetId) {
        return triggeredAbilityQueueService.beginDeathGraveyardTargetAfterPermanent(
                gameData, pending, permanentTargetId);
    }

    public void processNextAttackTriggerTarget(GameData gameData) {
        triggeredAbilityQueueService.processNextAttackTriggerTarget(gameData);
    }

    public TargetFilter targetFilterForTriggeredEffects(Card sourceCard, List<CardEffect> effects) {
        return triggeredAbilityQueueService.targetFilterForTriggeredEffects(sourceCard, effects);
    }

    /** Whether the one target of {@code effects}, an ability of {@code card}, is "up to one". */
    public boolean isOptionalSingleTarget(Card card, List<CardEffect> effects) {
        return triggeredAbilityQueueService.isOptionalSingleTarget(card, effects);
    }

    /** The target filter of {@code pending}'s ability: its own, which the stack door stamps on the record, else its card's. */
    public TargetFilter targetFilterOf(PermanentChoiceContext.AttackTriggerTarget pending) {
        return triggeredAbilityQueueService.targetFilterOf(pending);
    }

    public void processNextAttackCounterMoveFirstTarget(GameData gameData) {
        triggeredAbilityQueueService.processNextAttackCounterMoveFirstTarget(gameData);
    }

    public List<UUID> targetableCreaturesControlledBy(GameData gameData, UUID playerId,
                                                      Card sourceCard, UUID choosingPlayerId) {
        return triggeredAbilityQueueService.targetableCreaturesControlledBy(gameData, playerId, sourceCard, choosingPlayerId);
    }

    public void processNextEntersTriggerTarget(GameData gameData) {
        triggeredAbilityQueueService.processNextEntersTriggerTarget(gameData);
    }

    public void processNextSelfTriggeredAbilityTarget(GameData gameData) {
        triggeredAbilityQueueService.processNextSelfTriggeredAbilityTarget(gameData);
    }

    public void processNextTriggeredModalTrigger(GameData gameData) {
        triggeredAbilityQueueService.processNextTriggeredModalTrigger(gameData);
    }

    public void queueChosenTriggeredModalTrigger(GameData gameData, Card sourceCard, UUID controllerId,
            UUID sourcePermanentId, ChooseOneEffect.ChooseOneOption chosen) {
        triggeredAbilityQueueService.queueChosenTriggeredModalTrigger(gameData, sourceCard, controllerId,
                sourcePermanentId, chosen);
    }

    public void processNextDiscardSelfTrigger(GameData gameData) {
        triggeredAbilityQueueService.processNextDiscardSelfTrigger(gameData);
    }

    public void processNextDiscardControllerTriggerTarget(GameData gameData) {
        triggeredAbilityQueueService.processNextDiscardControllerTriggerTarget(gameData);
    }

    public void processNextSpellTargetTrigger(GameData gameData) {
        triggeredAbilityQueueService.processNextSpellTargetTrigger(gameData);
    }

    public boolean beginTriggerOrder(GameData gameData) {
        return triggeredAbilityQueueService.beginTriggerOrder(gameData);
    }

    public void processNextETBSpellTargetTrigger(GameData gameData) {
        etbTokenTargetService.processNextETBSpellTargetTrigger(gameData);
    }

    public void processNextSpellGraveyardTargetTrigger(GameData gameData) {
        triggeredAbilityQueueService.processNextSpellGraveyardTargetTrigger(gameData);
    }

    public void processNextEmblemTriggerTarget(GameData gameData) {
        triggeredAbilityQueueService.processNextEmblemTriggerTarget(gameData);
    }

    public void processNextLifeGainTriggerTarget(GameData gameData) {
        triggeredAbilityQueueService.processNextLifeGainTriggerTarget(gameData);
    }

    public void processNextDrawTriggerTarget(GameData gameData) {
        if (gameData.hasPendingInteraction(PermanentChoiceContext.DrawTriggerAnyTarget.class)) {
            triggeredAbilityQueueService.processNextDrawTriggerTarget(gameData);
        } else if (gameData.hasPendingInteraction(PermanentChoiceContext.DrawTriggerPermanentTarget.class)) {
            triggeredAbilityQueueService.processNextDrawTriggerPermanentTarget(gameData);
        }
    }

    /** Hands the "whenever you gain life" abilities of the cards in the gaining player's graveyard to the door. */
    private void collectGraveyardControllerLifeGainTriggers(GameData gameData, UUID gainingPlayerId, int lifeGainedAmount) {
        List<Card> graveyard = gameData.playerGraveyards.get(gainingPlayerId);
        if (graveyard == null) return;

        LifeWatch watch = LifeWatch.GRAVEYARD_ON_CONTROLLER_GAINS_LIFE;
        for (Card card : new ArrayList<>(graveyard)) {
            for (TriggeredAbility ability : graveyardLifeAbilities(gameData, card, watch)) {
                if (LifeAbilityReader.trigger(ability, watch.trigger()).firstEachTurn()
                        && gameData.getLifeGainedThisTurn(gainingPlayerId) != lifeGainedAmount) {
                    continue;
                }
                putGraveyardLifeAbility(gameData, card, gainingPlayerId, ability, gainingPlayerId, lifeGainedAmount);
            }
        }
    }

    /**
     * Hands {@code ability}, a life gain ability of {@code card} in its owner {@code controllerId}'s graveyard, to the
     * stack door for {@code amount} life {@code playerId} gained: the record names the player and the amount as
     * {@link #putLifeAbility} does, and this graveyard incarnation of the card (CR 400.7).
     */
    private boolean putGraveyardLifeAbility(GameData gameData, Card card, UUID controllerId, TriggeredAbility ability,
                                            UUID playerId, int amount) {
        ConditionContext context = ConditionContext.forCard(card, controllerId).withTargetId(playerId)
                .withEventValue(amount);
        long graveyardEntryVersion = gameData.graveyardEntryVersion(card.getId());
        return putEventAbility(gameData, ability, card, null, controllerId, context,
                new TriggerContext.LifeGain(playerId, amount, null, null), entry -> {
                    entry.setNonTargeting(true);
                    entry.setTargetId(playerId);
                    entry.setEventValue(amount);
                    entry.setTriggeringPlayerId(playerId);
                    entry.setSourceGraveyardEntryVersion(graveyardEntryVersion);
                });
    }

    /**
     * The effects of the abilities the graveyard action watch {@code watch} reads of {@code card} in a graveyard, its
     * own and those perpetually granted to it, one list for each ability (one object on the stack, CR 603.3), or none
     * while cards in graveyards have lost all abilities; its collectors judge no filter or limit.
     */
    private List<List<CardEffect>> graveyardActionAbilities(GameData gameData, Card card, ActionWatch watch) {
        return byAbility(WatchedAbilities.withWatchedTrigger(
                gameQueryService.getEffectiveGraveyardTriggeredAbilities(gameData, card), watch.trigger(),
                watch.zone()), abilities -> ActionAbilityReader.effects(abilities, watch.trigger()));
    }

    /**
     * The effects of the abilities the graveyard misc watch {@code watch} reads of {@code card} in a graveyard, its own
     * and those perpetually granted to it, one list for each ability (one object on the stack, CR 603.3), or none
     * while cards in graveyards have lost all abilities; its collectors judge no door, first time or limit.
     */
    private List<List<CardEffect>> graveyardMiscAbilities(GameData gameData, Card card, MiscWatch watch) {
        return byAbility(WatchedAbilities.withWatchedTrigger(
                gameQueryService.getEffectiveGraveyardTriggeredAbilities(gameData, card), watch.trigger(),
                watch.zone()), abilities -> MiscAbilityReader.effects(abilities, watch.trigger()));
    }

    /**
     * The abilities {@code watch} reads of {@code card} in a graveyard, its own and those perpetually granted to it,
     * or none while cards in graveyards have lost all abilities.
     */
    private List<TriggeredAbility> graveyardLifeAbilities(GameData gameData, Card card, LifeWatch watch) {
        return WatchedAbilities.withWatchedTrigger(gameQueryService.getEffectiveGraveyardTriggeredAbilities(gameData,
                card), watch.trigger(), watch.zone());
    }

    public void processNextSagaChapterTarget(GameData gameData) {
        triggeredAbilityQueueService.processNextSagaChapterTarget(gameData);
    }

    public void processNextSagaChapterPlayerTarget(GameData gameData) {
        triggeredAbilityQueueService.processNextSagaChapterPlayerTarget(gameData);
    }

    public void processNextSagaChapterGraveyardTarget(GameData gameData) {
        triggeredAbilityQueueService.processNextSagaChapterGraveyardTarget(gameData);
    }

    /** Fires triggers watching a Saga's final chapter ability after it has fully resolved. */
    public void checkSagaFinalChapterAbilityResolutionTriggers(GameData gameData,
                                                                UUID sagaControllerId, int sagaManaValue) {
        if (sagaControllerId == null) return;
        TriggerContext context = new TriggerContext.SagaFinalChapterAbilityResolved(
                sagaControllerId, sagaManaValue);
        gameData.forEachPermanent((controllerId, permanent) ->
                dispatchWatch(gameData, permanent, controllerId,
                        SagaWatch.ON_SAGA_FINAL_CHAPTER_ABILITY_RESOLVES, context));
    }

    /** Fires triggers watching a Saga's final chapter ability as it triggers (is put on the stack). */
    public void checkSagaFinalChapterAbilityTriggers(GameData gameData, UUID sagaControllerId, int sagaManaValue) {
        if (sagaControllerId == null) return;
        TriggerContext context = new TriggerContext.SagaFinalChapterAbilityResolved(
                sagaControllerId, sagaManaValue);
        gameData.forEachPermanent((controllerId, permanent) ->
                dispatchWatch(gameData, permanent, controllerId,
                        SagaWatch.ON_SAGA_FINAL_CHAPTER_ABILITY_TRIGGERS, context));
    }

    // ── Explore triggers ──────────────────────────────────────────────

    /**
     * Scans the exploring creature's controller's battlefield for permanents with
     * {@link ActionWatch#ON_ALLY_CREATURE_EXPLORES} abilities and queues each ability whose filter the explored card
     * matches ("whenever a creature you control explores a land card") as one object (CR 603.3), for target
     * selection or directly onto the stack.
     */
    public void checkExploreTriggers(GameData gameData, UUID controllerId, Card exploredCard) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return;

        ActionWatch watch = ActionWatch.ON_ALLY_CREATURE_EXPLORES;
        for (Permanent perm : battlefield) {
            if (perm.isLosesAllAbilitiesUntilEndOfTurn()) continue;
            for (TriggeredAbility ability : ActionAbilityReader.printed(perm.getCard(), watch)) {
                List<ActionAbilityReader.ActionEffect> read = ActionAbilityReader.read(ability, watch.trigger());
                ActionAbilityReader.ActionEffect first = read.getFirst();
                if (first.limit() != TriggerLimit.NONE) {
                    throw new IllegalStateException("The explore collector judges no limit: " + ability);
                }
                if (first.filter() != null && (exploredCard == null || !predicateEvaluationService.matchesCardPredicate(
                        exploredCard, first.filter(), perm.getCard().getId(), gameData, controllerId))) {
                    continue;
                }
                queueExploreAbility(gameData, perm, controllerId,
                        read.stream().map(ActionAbilityReader.ActionEffect::effect).toList());
            }
        }
    }

    /** Queues one explore ability of {@code perm}, {@code effects}, for target selection or directly onto the stack. */
    private void queueExploreAbility(GameData gameData, Permanent perm, UUID controllerId, List<CardEffect> effects) {
        boolean anyTargeting = effects.stream().anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.PERMANENT));
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                perm.getCard(),
                controllerId,
                perm.getCard().getName() + "'s ability",
                new ArrayList<>(effects),
                null,
                perm.getId()
        );
        gameData.handOffTriggerRecord(entry);
        if (anyTargeting) {
            gameData.queueInteraction(
                    new PermanentChoiceContext.ExploreTriggerTarget(
                            perm.getCard(), controllerId, new ArrayList<>(effects), perm.getId()));
        } else {
            gameData.stack.add(entry);
        }
        log.info("Game {} - {} explore trigger queued", gameData.id, perm.getCard().getName());
    }

    public void processNextExploreTriggerTarget(GameData gameData) {
        triggeredAbilityQueueService.processNextExploreTriggerTarget(gameData);
    }

    /** Fires triggers for a creature controlled by a player conniving. */
    public void checkAllyCreatureConniveTriggers(GameData gameData, Permanent connivingCreature) {
        if (connivingCreature == null || !gameQueryService.isCreature(gameData, connivingCreature)) {
            return;
        }
        UUID controllerId = gameQueryService.findPermanentController(gameData, connivingCreature.getId());
        if (controllerId == null) {
            return;
        }
        checkAllyCreatureConniveTriggers(gameData, connivingCreature, controllerId);
    }

    /**
     * Fires the same triggers for a creature that connived using its last known information, with the
     * last known controller (CR 701.50b).
     */
    public void checkAllyCreatureConniveTriggers(GameData gameData, Permanent connivingCreature,
                                                 UUID controllerId) {
        if (connivingCreature == null || controllerId == null) {
            return;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return;
        }

        TriggerContext context = new TriggerContext.CreatureConnives(connivingCreature, controllerId);
        for (Permanent watcher : List.copyOf(battlefield)) {
            dispatchWatch(gameData, watcher, controllerId, ActionWatch.ON_ALLY_CREATURE_CONNIVES, context);
        }
    }

    // ── Exploit triggers ──────────────────────────────────────────────

    /**
     * Queues this permanent's {@link ActionWatch#ON_EXPLOIT} ability after it successfully
     * exploited a creature. Uses the source card's LKI even if the permanent left the battlefield
     * (e.g. it sacrificed itself).
     */
    public void checkExploitTriggers(GameData gameData, Card sourceCard, UUID controllerId, UUID sourcePermanentId) {
        // Each "when this exploits a creature" ability is one object of all its effects (CR 603.3).
        for (List<CardEffect> effects : ActionAbilityReader.printedEffectsByAbility(sourceCard,
                ActionWatch.ON_EXPLOIT)) {
            queueExploitAbility(gameData, sourceCard, controllerId, sourcePermanentId, effects);
        }
    }

    private void queueExploitAbility(GameData gameData, Card sourceCard, UUID controllerId, UUID sourcePermanentId,
                                     List<CardEffect> effects) {
        StackEntry originalEntry = gameData.pendingEffectResolutionEntry;

        boolean targetsStack = effects.stream().anyMatch(EffectResolution::targetsSpellOnStack);
        boolean targetsPermanent = effects.stream().anyMatch(effect ->
                effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT));
        boolean targetsPlayer = effects.stream().anyMatch(effect ->
                effect.targetSpec().admits(TargetPredicate.Kind.PLAYER));
        boolean targetsGraveyard = effects.stream().anyMatch(effect ->
                effect.targetSpec().admits(com.github.laxika.magicalvibes.model.effect.TargetPredicate.Kind.GRAVEYARD_CARD));
        StackEntry triggeredEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                controllerId,
                sourceCard.getName() + "'s exploit ability",
                new ArrayList<>(effects),
                null,
                sourcePermanentId
        );
        if (originalEntry != null) {
            triggeredEntry.setSacrificedPermanentSnapshot(originalEntry.getSacrificedPermanentSnapshot());
            triggeredEntry.setSacrificedPower(originalEntry.getSacrificedPower());
            triggeredEntry.setSacrificedColorCount(originalEntry.getSacrificedColorCount());
            triggeredEntry.setSacrificedToughness(originalEntry.getSacrificedToughness());
        }
        gameData.handOffTriggerRecord(triggeredEntry);
        if (targetsGraveyard && !targetsStack && !targetsPermanent && !targetsPlayer) {
            gameData.queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                    sourceCard, controllerId, new ArrayList<>(effects)));
        } else if (targetsStack) {
            StackEntryPredicate stackFilter = null;
            boolean includeAbilities = false;
            if (sourceCard.getTargetFilter() instanceof com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter sf) {
                stackFilter = sf.predicate();
                includeAbilities = predicateContainsHasTarget(sf.predicate());
            }
            gameData.queueInteraction(new PermanentChoiceContext.ExploitTriggerTarget(
                    sourceCard, controllerId, new ArrayList<>(effects), sourcePermanentId,
                    stackFilter, includeAbilities, sourceCard.getTargetFilter(),
                    originalEntry == null ? null : originalEntry.getSacrificedPermanentSnapshot(),
                    originalEntry == null ? 0 : originalEntry.getSacrificedPower(),
                    originalEntry == null ? 0 : originalEntry.getSacrificedColorCount(),
                    originalEntry == null ? 0 : originalEntry.getSacrificedToughness()));
        } else if (targetsPermanent) {
            gameData.queueInteraction(new PermanentChoiceContext.ExploitPermanentTriggerTarget(
                    sourceCard, controllerId, new ArrayList<>(effects), sourcePermanentId,
                    originalEntry == null ? null : originalEntry.getSacrificedPermanentSnapshot(),
                    originalEntry == null ? 0 : originalEntry.getSacrificedPower(),
                    originalEntry == null ? 0 : originalEntry.getSacrificedColorCount(),
                    originalEntry == null ? 0 : originalEntry.getSacrificedToughness()));
        } else if (targetsPlayer) {
            gameData.queueInteraction(new PermanentChoiceContext.ExploitPlayerTriggerTarget(
                    sourceCard, controllerId, new ArrayList<>(effects), sourcePermanentId,
                    sourceCard.getTargetFilter(),
                    originalEntry == null ? null : originalEntry.getSacrificedPermanentSnapshot(),
                    originalEntry == null ? 0 : originalEntry.getSacrificedPower(),
                    originalEntry == null ? 0 : originalEntry.getSacrificedColorCount(),
                    originalEntry == null ? 0 : originalEntry.getSacrificedToughness()));
        } else {
            gameData.stack.add(triggeredEntry);
        }
        gameLogService.append(gameData,
                GameLog.cardThen(sourceCard, " exploits a creature."));
        log.info("Game {} - {} exploit trigger queued", gameData.id, sourceCard.getName());
    }

    public void processNextExploitTriggerTarget(GameData gameData) {
        triggeredAbilityQueueService.processNextExploitTriggerTarget(gameData);
    }

    /**
     * Fires effects that watch a creature controlled by the exploiting player exploit a
     * creature. The exploiting permanent is also checked from its last-known battlefield object so
     * sacrificing it for its own exploit still lets its other triggered ability trigger.
     */
    public void checkAllyCreatureExploitTriggers(GameData gameData, UUID exploitingPlayerId,
                                                  Permanent exploitingPermanent, Card exploitedCard,
                                                  int exploitedPower) {
        if (exploitingPermanent == null || exploitedCard == null) return;

        TriggerContext ctx = new TriggerContext.CreatureExploit(
                exploitingPlayerId, exploitingPermanent.getCard(), exploitedCard, exploitedPower);
        List<Permanent> battlefield = gameData.playerBattlefields.get(exploitingPlayerId);
        boolean sourceStillPresent = false;
        if (battlefield != null) {
            for (Permanent perm : new ArrayList<>(battlefield)) {
                if (perm.getId().equals(exploitingPermanent.getId())) {
                    sourceStillPresent = true;
                }
                dispatchWatch(gameData, perm, exploitingPlayerId, ActionWatch.ON_ALLY_CREATURE_EXPLOITS, ctx);
                if (!exploitedCard.isToken()) {
                    dispatchWatch(gameData, perm, exploitingPlayerId,
                            ActionWatch.ON_ALLY_CREATURE_EXPLOITS_NONTOKEN_CREATURE, ctx);
                }
            }
        }

        if (!sourceStillPresent) {
            dispatchLastKnownActionAbilities(gameData, exploitingPermanent, exploitingPlayerId,
                    ActionWatch.ON_ALLY_CREATURE_EXPLOITS, ctx);
            if (exploitedCard.isToken()) return;
            dispatchLastKnownActionAbilities(gameData, exploitingPermanent, exploitingPlayerId,
                    ActionWatch.ON_ALLY_CREATURE_EXPLOITS_NONTOKEN_CREATURE, ctx);
        }
    }

    /**
     * Dispatches the printed abilities {@code watch} reads of {@code permanent} as it last existed on the battlefield
     * (the exploiting creature that sacrificed itself), judging each one's filter and limit as
     * {@link #dispatchActionAbility} does.
     */
    private void dispatchLastKnownActionAbilities(GameData gameData, Permanent permanent, UUID controllerId,
                                                  ActionWatch watch, TriggerContext ctx) {
        for (TriggeredAbility ability : ActionAbilityReader.printed(permanent.getCard(), watch)) {
            dispatchActionAbility(gameData, permanent, controllerId, watch, ctx,
                    ActionAbilityReader.read(ability, watch.trigger()));
        }
    }

    public void processNextExploitPermanentTriggerTarget(GameData gameData) {
        triggeredAbilityQueueService.processNextExploitPermanentTriggerTarget(gameData);
    }

    public void processNextExploitPlayerTriggerTarget(GameData gameData) {
        if (!gameData.hasPendingInteraction(PermanentChoiceContext.ExploitPlayerTriggerTarget.class)) {
            return;
        }

        PermanentChoiceContext.ExploitPlayerTriggerTarget trigger =
                gameData.pollPendingInteraction(PermanentChoiceContext.ExploitPlayerTriggerTarget.class);
        List<UUID> validPlayerTargets = validTargetService.filterValidPlayerTargets(
                gameData, trigger.targetFilter(), new ArrayList<>(gameData.orderedPlayerIds), trigger.controllerId());

        if (validPlayerTargets.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.cardThen(trigger.sourceCard(), "'s exploit trigger has no valid targets."));
            log.info("Game {} - {} exploit player-target trigger skipped (no valid targets)",
                    gameData.id, trigger.sourceCard().getName());
            // The ability is removed from the stack, and its record with it.
            gameData.dropTriggerRecordInHand();
            processNextExploitPlayerTriggerTarget(gameData);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(trigger);
        playerInputService.beginAnyTargetChoice(gameData, trigger.controllerId(), List.of(), validPlayerTargets,
                trigger.sourceCard().getName() + "'s exploit ability — Choose target player.");
        log.info("Game {} - {} exploit trigger awaiting player target selection",
                gameData.id, trigger.sourceCard().getName());
    }

    public static boolean predicateContainsHasTarget(com.github.laxika.magicalvibes.model.filter.StackEntryPredicate predicate) {
        if (predicate instanceof com.github.laxika.magicalvibes.model.filter.StackEntryHasTargetPredicate) {
            return true;
        }
        if (predicate instanceof com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate typeIn) {
            return typeIn.spellTypes().contains(StackEntryType.ACTIVATED_ABILITY)
                    || typeIn.spellTypes().contains(StackEntryType.TRIGGERED_ABILITY);
        }
        if (predicate instanceof com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate allOf) {
            return allOf.predicates().stream().anyMatch(TriggerCollectionService::predicateContainsHasTarget);
        }
        if (predicate instanceof com.github.laxika.magicalvibes.model.filter.StackEntryAnyOfPredicate anyOf) {
            return anyOf.predicates().stream().anyMatch(TriggerCollectionService::predicateContainsHasTarget);
        }
        if (predicate instanceof com.github.laxika.magicalvibes.model.filter.StackEntryNotPredicate not) {
            return predicateContainsHasTarget(not.predicate());
        }
        return false;
    }

    // ── Clash ──────────────────────────────────────────────────────────

    /**
     * Performs a clash for {@code clashingPlayerId} against their (single, 2-player) opponent
     * (MTG rule 701.29): both players reveal the top card of their library, the clashing player
     * wins if their revealed card's mana value is strictly greater than the opponent's, and then
     * {@link ActionWatch#ON_CONTROLLER_CLASHES} triggers on the clashing player's permanents.
     *
     * <p>The caller subsequently offers each player a top-or-bottom placement choice for their
     * revealed card before continuing the clash effect.
     * The "whenever you clash" triggers fire after the clash ends. Invoked from a clash-source card's
     * effect resolution (see {@code ClashEffect}) or a test.
     *
     * @return {@code true} if the clashing player won the clash (their revealed card had a strictly
     *         greater mana value), so callers can apply an "if you won" reward.
     */
    public boolean performClash(GameData gameData, UUID clashingPlayerId) {
        UUID opponentId = gameData.orderedPlayerIds.stream()
                .filter(id -> !id.equals(clashingPlayerId))
                .findFirst().orElse(null);

        Card clashingCard = topCard(gameData, clashingPlayerId);
        Card opponentCard = opponentId != null ? topCard(gameData, opponentId) : null;

        GameLog.Builder clashLog = GameLog.builder()
                .text(gameData.playerIdToName.get(clashingPlayerId) + " clashes: reveals ");
        if (clashingCard != null) {
            clashLog.card(clashingCard);
        } else {
            clashLog.text("no card (empty library)");
        }
        clashLog.text("; opponent reveals ");
        if (opponentCard != null) {
            clashLog.card(opponentCard);
        } else {
            clashLog.text("no card (empty library)");
        }
        clashLog.text(".");
        gameLogService.append(gameData, clashLog.build());

        // 701.29c: win if your revealed card's mana value is higher than each other revealed card.
        boolean won = clashingCard != null
                && (opponentCard == null || clashingCard.getManaValue() > opponentCard.getManaValue());

        String outcome = won
                ? gameData.playerIdToName.get(clashingPlayerId) + " won the clash."
                : "No one won the clash.";
        gameLogService.append(gameData, GameLog.text(outcome));
        log.info("Game {} - {} clashes (won={})", gameData.id, clashingPlayerId, won);

        fireClashTriggers(gameData, clashingPlayerId, won);
        if (opponentId != null) {
            boolean opponentWon = opponentCard != null
                    && (clashingCard == null || opponentCard.getManaValue() > clashingCard.getManaValue());
            fireClashTriggers(gameData, opponentId, opponentWon);
        }
        return won;
    }

    private Card topCard(GameData gameData, UUID playerId) {
        List<Card> deck = gameData.playerDecks.get(playerId);
        return (deck == null || deck.isEmpty()) ? null : deck.getFirst();
    }

    /**
     * The "whenever you clash" abilities of {@code clashingPlayerId}'s permanents, which trigger after the clash ends.
     * The outcome is the ability's event value whatever its effects are: 1 if the player won (CR 701.30d), else 0.
     * "Whenever you clash and win" is the event condition {@code EventValueAtLeast(1)}, judged as the ability triggers;
     * "if you won" an "if" read from the entry as the ability resolves, so a later clash doesn't change it.
     */
    private void fireClashTriggers(GameData gameData, UUID clashingPlayerId, boolean won) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(clashingPlayerId);
        if (battlefield == null) return;
        int outcome = won ? 1 : 0;

        for (Permanent perm : new ArrayList<>(battlefield)) {
            if (perm.isLosesAllAbilitiesUntilEndOfTurn()) continue;
            TriggerJudge judge = conditionEvaluationService.judge(gameData,
                    ConditionContext.forPermanent(perm, clashingPlayerId), outcome);
            // Each ability is one object of all its effects (CR 603.3).
            for (List<CardEffect> effects : byAbility(judge.filter(ActionAbilityReader.printed(perm.getCard(),
                    ActionWatch.ON_CONTROLLER_CLASHES)), abilities -> ActionAbilityReader.effects(abilities,
                    ActionWatch.ON_CONTROLLER_CLASHES.trigger()))) {
                // Targeting clash triggers (Entangling Trap) route through the ClashTriggerTarget
                // interaction to pick an opponent's creature; non-targeting ones (Rebellion of the
                // Flamekin) go straight onto the stack as a triggered ability.
                boolean needsTarget = effects.stream()
                        .anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                                || e.targetSpec().admits(TargetPredicate.Kind.PLAYER));
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        clashingPlayerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(effects),
                        null,
                        perm.getId());
                entry.setEventValue(outcome);
                gameData.handOffTriggerRecord(entry);
                if (needsTarget) {
                    gameData.queueInteraction(new PermanentChoiceContext.ClashTriggerTarget(
                            perm.getCard(), clashingPlayerId, effects, perm.getId(), outcome));
                    log.info("Game {} - {} clash trigger queued", gameData.id, perm.getCard().getName());
                } else {
                    gameData.stack.add(entry);
                    log.info("Game {} - {} clash trigger pushed to stack", gameData.id, perm.getCard().getName());
                }
            }
        }

        triggeredAbilityQueueService.processNextClashTriggerTarget(gameData);
    }

    public void processNextClashTriggerTarget(GameData gameData) {
        triggeredAbilityQueueService.processNextClashTriggerTarget(gameData);
    }

    // ── Death / leaves-battlefield triggers ───────────────────────────

    /**
     * Puts one "whenever [an object] dies" ability of {@code perm} on the stack for {@code death} through
     * {@code watch}'s collector, which records the death whatever the effects are. The trigger pipeline has already
     * decided that the ability triggers: its relation, its filter, the zone it functions from and the look-back of
     * the event (CR 603.10a). {@code perm} is the source as the event sees it: on the battlefield, its last-known
     * copy if it left in the same event, or a stand-in for a card in a graveyard or in exile. The pipeline has also
     * checked the ability's limit and its intervening "if"; {@code limit} says whether accepting a "you may" uses up
     * "do this only once each turn" (CR 603.2h) and whether an ability that triggers only once each turn may be
     * made to trigger an additional time.
     *
     * @return whether anything was put on the stack or queued for it
     */
    public boolean triggerCreatureDeath(GameData gameData, CreatureDeathWatch watch, Permanent perm,
                                        UUID controllerId, List<CardEffect> effects, TriggerLimit limit, String actionKey,
                                        CreatureDeath death) {
        return switch (watch) {
            case ALLY -> collectAllyCreatureDeath(gameData, perm, controllerId, effects, limit, actionKey, death);
            case ANY -> collectAnyCreatureDeath(gameData, perm, controllerId, effects, death);
            case OPPONENT -> collectOpponentCreatureDeath(gameData, perm, controllerId, effects, limit, actionKey, death);
            case ALLY_NONTOKEN, ANY_NONTOKEN, ENCHANTED_PLAYER_NONTOKEN ->
                    collectNontokenCreatureDeath(gameData, watch, perm, controllerId, effects, death);
            case ALLY_CREATURE_OR_PLANESWALKER ->
                    collectAllyCreatureOrPlaneswalkerDeath(gameData, perm, controllerId, effects, death);
            case EQUIPPED -> collectEquippedCreatureDeath(gameData, perm, controllerId, effects, death);
            case DAMAGED -> collectDamagedCreatureDeath(gameData, perm, controllerId, effects, death);
            case HAUNTED -> collectHauntedCreatureDeath(gameData, perm.getCard(), controllerId, effects);
            case GRAVEYARD_ALLY -> collectGraveyardAllyCreatureDeath(gameData, perm.getCard(), controllerId,
                    effects, death);
            case GRAVEYARD_OPPONENT -> collectGraveyardOpponentCreatureDeath(gameData, perm.getCard(), controllerId,
                    effects, death);
            case ALLY_ARTIFACT_OR_CREATURE ->
                    triggerCreatureDeaths(gameData, watch, perm, controllerId, effects, limit, actionKey, List.of(death));
        };
    }

    /**
     * Puts a "whenever one or more [objects] die" ability of {@code perm} on the stack once for {@code deaths}, the
     * deaths of one event that met its trigger condition, in the order they happened (CR 603.2c). The first death
     * is the one its effects bind.
     *
     * @return whether anything was put on the stack or queued for it
     */
    public boolean triggerCreatureDeaths(GameData gameData, CreatureDeathWatch watch, Permanent perm,
                                         UUID controllerId, List<CardEffect> effects, TriggerLimit limit, String actionKey,
                                         List<CreatureDeath> deaths) {
        return switch (watch) {
            case GRAVEYARD_ALLY, GRAVEYARD_OPPONENT ->
                    collectGraveyardCreatureDeaths(gameData, perm.getCard(), controllerId, effects, deaths.getFirst());
            case ALLY_ARTIFACT_OR_CREATURE -> collectArtifactOrCreatureDeaths(gameData, perm, controllerId, effects,
                    deaths);
            case ANY -> collectAnyCreatureDeaths(gameData, perm, controllerId, effects, deaths);
            default -> collectAllyCreatureDeaths(gameData, watch, perm, controllerId, effects, limit, actionKey, deaths);
        };
    }

    /**
     * "Whenever a creature you control dies": the ability goes on the stack as one object of all its effects (CR
     * 603.3), a "you may" among them (asked as it resolves, CR 603.5), recording the death whatever they are
     * ({@link #recordCreatureDeath}); an ability with a target waits for it as it goes there (CR 603.3d). Effects that
     * make an ability trigger an additional time add copies, unless it triggers only once each turn.
     */
    private boolean collectAllyCreatureDeath(GameData gameData, Permanent perm, UUID controllerId,
                                             List<CardEffect> effects, TriggerLimit limit, String actionKey, CreatureDeath death) {
        if (effects.isEmpty()) {
            return false;
        }
        Permanent dyingPermanent = death.permanent();
        Card graveyardCard = dyingPermanent.getOriginalCard() != null ? dyingPermanent.getOriginalCard()
                : dyingPermanent.getCard();
        int dyingPower = Math.max(0, death.power());
        var context = new TriggerContext.CreatureDeath(dyingPermanent.getCard(), death.controllerId(),
                dyingPower, death.toughness(), dyingPermanent.getId(), dyingPermanent);
        int previousCopies = gameData.beginTriggeredAbilityCopies(limit == TriggerLimit.ONCE_EACH_TURN ? 1
                : 1 + gameQueryService.countAdditionalTriggeredAbilityTriggers(gameData, controllerId, perm, false,
                        context)
                + gameQueryService.countAdditionalCreatureDeathTriggeredAbilityTriggers(gameData, controllerId, perm));
        try {
            if (hasDeathTriggerTarget(effects, true)) {
                // The watching permanent's own target filter (e.g. opponent-only) is honoured; the card that went
                // to the graveyard is left out ("another target creature card"): for a copy, the physical card.
                PermanentChoiceContext.DeathTriggerTarget choice = new PermanentChoiceContext.DeathTriggerTarget(
                        perm.getCard(), controllerId, new ArrayList<>(effects),
                        dyingPower, new Permanent(perm), null, graveyardCard.getId(), true,
                        actionKey
                ).withTriggeringCard(graveyardCard.getId(), gameData.graveyardEntryVersion(graveyardCard.getId()))
                        .withTriggeringPermanent(dyingPermanent, death.attachments())
                        .withTriggeringPermanentController(death.controllerId())
                        .withGraveyardTargetMaxManaValue(graveyardTargetMaxManaValueAtDeath(
                                gameData, controllerId, effects, dyingPermanent));
                // The record is the entry of the ability that chooses nothing, of the card the choice names.
                StackEntry targetRecord = allyCreatureDeathEntry(gameData, perm.getCard(), perm, controllerId, effects,
                        actionKey, death);
                targetRecord.setGraveyardTargetMaxManaValue(choice.graveyardTargetMaxManaValue());
                gameData.handOffTriggerRecord(targetRecord);
                gameData.queueInteraction(choice);
            } else {
                Card watcherCard = perm.getOriginalCard() != null
                        && perm.getOriginalCard().getId().equals(perm.getCard().getId())
                        && perm.getOriginalCard().getName().equals(perm.getCard().getName())
                        ? perm.getOriginalCard() : perm.getCard();
                gameData.stack.add(allyCreatureDeathEntry(gameData, watcherCard, perm, controllerId, effects,
                        actionKey, death));
            }
        } finally {
            gameData.restoreTriggeredAbilityCopies(previousCopies);
        }
        gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
        log.info("Game {} - {} triggers (ally creature died)", gameData.id, perm.getCard().getName());
        return true;
    }

    /**
     * The entry of a "whenever a creature you control dies" ability of {@code perm} that chooses nothing, its source
     * card {@code watcherCard}, recording {@code death} ({@link #recordCreatureDeath}). It is also the record of such
     * an ability that chooses a target as it goes on the stack ({@code GameData.handOffTriggerRecord}).
     */
    private static StackEntry allyCreatureDeathEntry(GameData gameData, Card watcherCard, Permanent perm,
                                                     UUID controllerId, List<CardEffect> effects, String actionKey,
                                                     CreatureDeath death) {
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                watcherCard,
                controllerId,
                perm.getCard().getName() + "'s ability",
                new ArrayList<>(effects),
                null,
                perm.getId()
        );
        entry.setNonTargeting(true);
        entry.setSourcePermanentSnapshot(new Permanent(perm));
        entry.setOnceEachTurnActionKey(actionKey);
        recordCreatureDeath(gameData, entry, death);
        return entry;
    }

    /**
     * Records {@code death} on {@code entry}, whatever the ability's effects are: the card that went to the graveyard
     * as its triggering card with the graveyard incarnation it has now (CR 400.7), the creature as it last existed on
     * the battlefield (CR 603.10a) as its triggering permanent with its attachments, its controller, mana value and
     * toughness then (CR 608.2h), and its power: as it was (which can be negative, CR 107.1b) for a comparison, at
     * least 0 as the event value an amount reads.
     */
    private static void recordCreatureDeath(GameData gameData, StackEntry entry, CreatureDeath death) {
        Permanent dyingPermanent = death.permanent();
        Card graveyardCard = dyingPermanent.getOriginalCard() != null ? dyingPermanent.getOriginalCard()
                : dyingPermanent.getCard();
        entry.setTriggeringPermanentId(dyingPermanent.getId());
        entry.rememberLastKnownPermanentCard(dyingPermanent.getId(), dyingPermanent.getCard());
        entry.setTriggeringPermanentPowerAtTrigger(death.power());
        entry.setTriggeringCard(graveyardCard.getId(), gameData.graveyardEntryVersion(graveyardCard.getId()));
        entry.setTriggeringPermanent(dyingPermanent, death.attachments());
        entry.setTriggeringPermanentControllerId(death.controllerId());
        entry.setTriggeringPermanentToughnessAtTrigger(death.toughness());
        entry.setDyingPermanentManaValue(dyingPermanent.getCard().getManaValue());
        entry.setEventValue(Math.max(0, death.power()));
    }

    private boolean collectAnyCreatureDeath(GameData gameData, Permanent perm, UUID playerId,
                                            List<CardEffect> effects, CreatureDeath death) {
        Permanent dyingPermanent = death.permanent();
        Card dyingCard = dyingPermanent.getCard();
        var ctx = new TriggerContext.CreatureDeath(dyingCard, death.controllerId(),
                death.power(), death.toughness(), dyingPermanent.getId(), dyingPermanent, true,
                death.attachments());
        return dispatchCreatureDeath(new TriggerMatchContext(gameData, perm, playerId, effects),
                CreatureDeathWatch.ANY, ctx);
    }

    private boolean collectOpponentCreatureDeath(GameData gameData, Permanent perm, UUID playerId,
                                                 List<CardEffect> effects, TriggerLimit limit, String actionKey, CreatureDeath death) {
        Permanent dyingPermanent = death.permanent();
        Card dyingCard = dyingPermanent.getCard();
        UUID dyingCreatureControllerId = death.controllerId();
        var ctx = new TriggerContext.CreatureDeath(dyingCard, dyingCreatureControllerId,
                death.power(), death.toughness(), dyingPermanent.getId(), dyingPermanent);
        return dispatchCreatureDeath(new TriggerMatchContext(gameData, perm, playerId, effects, perm.getCard(), null,
                actionKey), CreatureDeathWatch.OPPONENT, ctx);
    }

    /**
     * "A nontoken creature you control", "a nontoken creature" and a Curse's "a nontoken creature enchanted player
     * controls": the ability through its handler, whole, read with the permanent's text changes (layer 3, CR 612.1).
     */
    private boolean collectNontokenCreatureDeath(GameData gameData, CreatureDeathWatch watch, Permanent perm,
                                                 UUID controllerId, List<CardEffect> effects, CreatureDeath death) {
        Permanent dyingPermanent = death.permanent();
        Card dyingCard = dyingPermanent.getCard();
        TriggerContext ctx = watch == CreatureDeathWatch.ANY_NONTOKEN
                ? new TriggerContext.CreatureDeath(dyingCard, death.ownerId(),
                        dyingCard.getPower() != null ? dyingCard.getPower() : 0,
                        dyingCard.getToughness() != null ? dyingCard.getToughness() : 0)
                : new TriggerContext.CreatureDeath(dyingCard, death.controllerId(), death.power(),
                        dyingPermanent.getEffectiveToughness(), dyingPermanent.getId(), dyingPermanent);
        List<TextReplacement> globalWordChange = TextChangeTransformer.globalColorWordReplacements(gameData);
        List<CardEffect> transformed = effects.stream()
                .map(effect -> TextChangeTransformer.transform(effect, perm.getTextReplacements(), globalWordChange))
                .toList();
        return dispatchCreatureDeath(new TriggerMatchContext(gameData, perm, controllerId, transformed), watch, ctx);
    }

    /**
     * "Whenever a creature or planeswalker you control dies": the ability's effects, a "you may" among them (asked as
     * it resolves, CR 603.5), go on the stack as one object (CR 603.3), recording the death whatever they are
     * ({@link #recordCreatureDeath}). Effects that make a creature's death trigger an ability an additional time
     * count only for a creature.
     */
    private boolean collectAllyCreatureOrPlaneswalkerDeath(GameData gameData, Permanent perm, UUID controllerId,
                                                           List<CardEffect> effects, CreatureDeath death) {
        if (effects.isEmpty()) {
            return false;
        }
        Permanent dyingPermanent = death.permanent();
        boolean wasCreature = dyingPermanent.getCard().hasType(CardType.CREATURE);
        var context = new TriggerContext.CreatureDeath(dyingPermanent.getCard(), death.controllerId(),
                death.power(), death.toughness(), dyingPermanent.getId(), dyingPermanent, wasCreature);
        int previousCopies = gameData.beginTriggeredAbilityCopies(1
                + gameQueryService.countAdditionalTriggeredAbilityTriggers(gameData, controllerId, perm, false,
                        context)
                + (wasCreature
                ? gameQueryService.countAdditionalCreatureDeathTriggeredAbilityTriggers(gameData, controllerId, perm)
                : 0));
        try {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    perm.getCard(),
                    controllerId,
                    perm.getCard().getName() + "'s ability",
                    new ArrayList<>(effects),
                    null,
                    perm.getId()
            );
            entry.setNonTargeting(true);
            entry.setSourcePermanentSnapshot(new Permanent(perm));
            recordCreatureDeath(gameData, entry, death);
            gameData.stack.add(entry);
        } finally {
            gameData.restoreTriggeredAbilityCopies(previousCopies);
        }
        gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
        log.info("Game {} - {} triggers (ally creature or planeswalker died)", gameData.id, perm.getCard().getName());
        return true;
    }

    private boolean collectEquippedCreatureDeath(GameData gameData, Permanent perm, UUID equipmentControllerId,
                                                 List<CardEffect> effects, CreatureDeath death) {
        Permanent dyingPermanent = death.permanent();
        Card dyingCard = death.card();
        var ctx = new TriggerContext.EquippedCreatureDeath(
                dyingPermanent.getId(), death.controllerId(), dyingCard, death.power(), new Permanent(dyingPermanent));
        return dispatchCreatureDeath(new TriggerMatchContext(gameData, perm, equipmentControllerId, effects),
                CreatureDeathWatch.EQUIPPED, ctx);
    }

    /**
     * "Whenever a creature dealt damage by this creature (or by equipped or enchanted creature) this turn dies",
     * with the dying creature's last-known information on the stack entry for the effects that read it: its toughness,
     * its card for a "may exile that card", its controller for "its controller loses N life".
     */
    private boolean collectDamagedCreatureDeath(GameData gameData, Permanent sourcePermanent, UUID controllerId,
                                                List<CardEffect> effects, CreatureDeath death) {
        if (effects.isEmpty()) {
            return false;
        }
        Card sourceCard = sourcePermanent.getCard();
        UUID sourcePermanentId = sourcePermanent.getId();
        Permanent dyingPermanent = death.permanent();
        Card dyingCreatureCard = death.card();
        UUID dyingControllerId = death.controllerId();
        UUID dyingCreatureCardId = dyingCreatureCard.getId();
        // The ability is one object of all its effects (CR 603.3); with a target, it waits for it (CR 603.3d).
        StackEntry triggerEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                controllerId,
                sourceCard.getName() + "'s ability",
                new ArrayList<>(effects),
                null,
                sourcePermanentId
        );
        // The dying creature's card id as last-known information, for effects that act on it
        // (e.g. Seraph returns "that card" at the next end step).
        // The graveyard visit this trigger refers to (CR 400.7): a later effect that acts on "that card"
        // must not act on the same card after it has left and returned to a graveyard again.
        triggerEntry.setTriggeringCard(dyingCreatureCardId, gameData.graveyardEntryVersion(dyingCreatureCardId));
        // "That creature's toughness" (Abattoir Ghoul) is its last-known toughness (CR 608.2h).
        triggerEntry.setTriggeringPermanent(dyingPermanent);
        triggerEntry.setTriggeringPermanentToughnessAtTrigger(dyingPermanent.getLastKnownToughness() != null
                ? dyingPermanent.getLastKnownToughness() : dyingPermanent.getEffectiveToughness());
        // "Its controller" is the dying creature's last-known controller (CR 608.2h), whatever the effects are.
        triggerEntry.setTriggeringPermanentControllerId(dyingControllerId);
        gameData.handOffTriggerRecord(triggerEntry);
        if (hasDeathTriggerTarget(effects, true)) {
            gameData.queueInteraction(new PermanentChoiceContext.SelfTriggeredAbilityTarget(
                    sourceCard, controllerId, new ArrayList<>(effects),
                    "damaged creature dies", sourcePermanentId));
            return true;
        }
        gameData.stack.add(triggerEntry);
        gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers (damaged creature died this turn)", gameData.id, sourceCard.getName());
        return true;
    }

    /**
     * Whether an ability of {@code effects} has a target to choose as it goes on the stack (CR 603.3d): a player or a
     * permanent, or with {@code graveyardCard} a card in a graveyard.
     */
    private static boolean hasDeathTriggerTarget(List<CardEffect> effects, boolean graveyardCard) {
        return effects.stream().anyMatch(effect -> effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                || effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                || graveyardCard && effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD));
    }

    /**
     * "When the creature it haunts dies", from exile (CR 702.55c). The ability is one object of all its effects
     * (CR 603.3). A targeted ability is one triggered ability (CR 603.3d): its other effects are skipped with it if no
     * target is legal. A modal ability's mode is chosen as it goes on the stack (CR 603.3c); no card writes a mode
     * choice beside other effects in one haunt ability, and one that did fails loudly instead of being split.
     */
    private boolean collectHauntedCreatureDeath(GameData gameData, Card hauntingCard, UUID ownerId,
                                                List<CardEffect> effects) {
        if (effects.isEmpty()) {
            return true;
        }
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                hauntingCard,
                ownerId,
                hauntingCard.getName() + "'s ability",
                new ArrayList<>(effects),
                null);
        if (effects.stream().anyMatch(TriggeredModalEffect.class::isInstance)) {
            if (effects.size() > 1) {
                throw new IllegalStateException("The haunt collector reads no mode choice beside other effects: "
                        + hauntingCard.getName());
            }
            gameData.handOffTriggerRecord(entry);
            gameData.queueInteraction(new PermanentChoiceContext.TriggeredModalTrigger(
                    hauntingCard, ownerId, ((TriggeredModalEffect) effects.getFirst()).choice(), null));
            gameLogService.append(gameData, GameLog.abilityTriggers(hauntingCard));
            return true;
        }
        if (hasDeathTriggerTarget(effects, true) || graveyardTargetingSupport.findTarget(effects) != null) {
            gameData.handOffTriggerRecord(entry);
            gameData.queueInteraction(new PermanentChoiceContext.DeathTriggerTarget(
                    hauntingCard, ownerId, new ArrayList<>(effects)));
        } else {
            gameData.stack.add(entry);
            gameLogService.append(gameData, GameLog.abilityTriggers(hauntingCard));
        }
        return true;
    }

    private boolean collectGraveyardAllyCreatureDeath(GameData gameData, Card card, UUID controllerId,
                                                      List<CardEffect> effects, CreatureDeath death) {
        if (effects.isEmpty()) {
            return false;
        }
        // The ability is one object of all its effects (CR 603.3), a "you may" like any other: it is asked as the
        // ability resolves (CR 603.5).
        StackEntry triggerEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                card,
                controllerId,
                card.getName() + "'s ability",
                new ArrayList<>(effects)
        );
        // CR 400.7: the ability refers to this graveyard object, not a later one.
        triggerEntry.setSourceGraveyardEntryVersion(gameData.graveyardEntryVersion(card.getId()));
        gameData.stack.add(triggerEntry);
        gameLogService.append(gameData, GameLog.abilityTriggers(card));
        log.info("Game {} - {} graveyard creature-death trigger queued", gameData.id, card.getName());
        return true;
    }

    private boolean collectGraveyardOpponentCreatureDeath(GameData gameData, Card sourceCard, UUID controllerId,
                                                          List<CardEffect> effects, CreatureDeath death) {
        if (effects.isEmpty()) {
            return false;
        }
        queueGraveyardCreatureDeathTrigger(gameData, sourceCard, controllerId,
                effects, death.dyingCard(), death.controllerId());
        return true;
    }

    /** "Whenever one or more [other] creatures die" from a graveyard: one stack object for its plain effects. */
    private boolean collectGraveyardCreatureDeaths(GameData gameData, Card sourceCard, UUID controllerId,
                                                   List<CardEffect> effects, CreatureDeath firstDeath) {
        if (effects.isEmpty()) {
            return false;
        }
        // The ability is one object of all its effects (CR 603.3), a "you may" like any other: it is asked as the
        // ability resolves (CR 603.5).
        gameData.stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                controllerId,
                sourceCard.getName() + "'s ability",
                new ArrayList<>(effects)));
        gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        return true;
    }

    /** "Whenever one or more artifacts and/or creatures you control are put into a graveyard from the battlefield". */
    private boolean collectArtifactOrCreatureDeaths(GameData gameData, Permanent perm, UUID controllerId,
                                                    List<CardEffect> effects, List<CreatureDeath> deaths) {
        CreatureDeath firstDeath = deaths.getFirst();
        Permanent dyingPermanent = firstDeath.permanent();
        Card dyingCard = dyingPermanent.getCard();
        boolean eventContainsCreature = deaths.stream()
                .anyMatch(death -> death.dyingCard().hasType(CardType.CREATURE));
        TriggerContext context = new TriggerContext.ArtifactOrCreatureDeath(
                dyingCard, controllerId,
                gameQueryService.findGraveyardOwnerById(gameData, dyingCard.getId()),
                dyingPermanent, firstDeath.power(), firstDeath.toughness(), eventContainsCreature);
        return dispatchCreatureDeath(new TriggerMatchContext(gameData, perm, controllerId, effects),
                CreatureDeathWatch.ALLY_ARTIFACT_OR_CREATURE, context);
    }

    /**
     * "Whenever one or more [other] creatures die": the ability goes on the stack once for the batch through the
     * watch's default, as "whenever a creature dies" does ({@link #collectAnyCreatureDeath}), whatever its effects
     * are: one object of all of them (CR 603.3) recording the first death, a target chosen as it goes there
     * (CR 603.3d) and a "you may" asked as it resolves (CR 603.5).
     */
    private boolean collectAnyCreatureDeaths(GameData gameData, Permanent watcher, UUID watcherControllerId,
                                             List<CardEffect> effects, List<CreatureDeath> deaths) {
        if (effects.isEmpty()) {
            return false;
        }
        return collectAnyCreatureDeath(gameData, watcher, watcherControllerId, effects, deaths.getFirst());
    }

    /**
     * "Whenever one or more [other] creatures you control die": the ability's targeted effects wait for their targets
     * as it goes on the stack (CR 603.3d), its other effects, a "you may" among them (CR 603.5), go on the stack as one
     * object, whatever they are. The entry records the first death ({@link #recordCreatureDeath}), the cards of every
     * death that met its trigger condition, for "return those cards", and the total power of those creatures as they
     * last existed on the battlefield (CR 603.10a) as its event value: The Skullspore Nexus's token.
     */
    private boolean collectAllyCreatureDeaths(GameData gameData, CreatureDeathWatch watch, Permanent watcher,
                                              UUID watcherControllerId, List<CardEffect> effects,
                                              TriggerLimit limit, String actionKey, List<CreatureDeath> deaths) {
        CreatureDeath firstDeath = deaths.getFirst();
        Permanent dyingPermanent = firstDeath.permanent();
        int dyingPower = Math.max(0, firstDeath.power());
        int totalPower = 0;
        for (CreatureDeath death : deaths) {
            totalPower = SaturatingMath.add(totalPower, death.power());
        }
        List<UUID> eventCardIds = deaths.stream().map(death -> death.dyingCard().getId()).toList();
        if (effects.isEmpty()) return false;
        // The ability is one object of all its effects (CR 603.3); with a target, it waits for it (CR 603.3d).
        StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, watcher.getCard(),
                watcherControllerId, watcher.getCard().getName() + "'s ability",
                new ArrayList<>(effects), null, watcher.getId());
        entry.setNonTargeting(true);
        entry.setSourcePermanentSnapshot(new Permanent(watcher));
        entry.setOnceEachTurnActionKey(actionKey);
        recordCreatureDeath(gameData, entry, firstDeath);
        entry.setEventCardIds(eventCardIds);
        entry.setEventValue(Math.max(0, totalPower));
        gameData.handOffTriggerRecord(entry);
        if (hasDeathTriggerTarget(effects, false)) {
            gameData.queueInteraction(new PermanentChoiceContext.DeathTriggerTarget(
                    watcher.getCard(), watcherControllerId, new ArrayList<>(effects), dyingPower,
                    null, null, null, true, actionKey).withTriggeringPermanent(dyingPermanent));
            return true;
        }
        gameData.stack.add(entry);
        gameLogService.append(gameData, GameLog.abilityTriggers(watcher.getCard()));
        return true;
    }

    /**
     * The creature-death triggered abilities that aren't abilities of an object the trigger pipeline indexes: the
     * global ones a resolved spell or ability registered (CR 603.7), emblems', the delayed "when that creature
     * dies" watchers and face-up planes' (CR 901.7). They trigger as {@code dyingPermanent}, which
     * {@code dyingControllerId} controlled, dies.
     */
    public void checkCreatureDeathRegisteredTriggers(GameData gameData, UUID dyingControllerId,
                                                     Permanent dyingPermanent, int dyingPowerAtDeath,
                                                     int dyingToughnessAtDeath) {
        Card dyingCard = dyingPermanent.getCard();
        var ctx = new TriggerContext.CreatureDeath(dyingCard, dyingControllerId,
                dyingPowerAtDeath, dyingToughnessAtDeath, dyingPermanent.getId(), dyingPermanent);
        for (TemporaryGlobalTriggeredAbility watcher : List.copyOf(gameData.temporaryGlobalTriggeredAbilities)) {
            collectRegisteredCreatureDeathTrigger(gameData, watcher, dyingControllerId, dyingPermanent,
                    dyingPowerAtDeath, dyingToughnessAtDeath);
        }
        collectEmblemCreatureDeathTriggers(gameData, dyingCard, ctx);
        collectCreatureDeathTriggerWatchers(gameData, dyingPermanent);
        collectPlanarCreatureDeathTriggers(gameData, dyingCard, dyingPermanent, dyingControllerId);
    }

    private void collectRegisteredCreatureDeathTrigger(GameData gameData, TemporaryGlobalTriggeredAbility watcher,
                                                       UUID dyingControllerId, Permanent dyingPermanent,
                                                       int dyingPowerAtDeath, int dyingToughnessAtDeath) {
        ZoneChangeTrigger trigger = creatureDeathTrigger(watcher.ability());
        if (trigger == null) {
            return;
        }
        Card dyingCard = dyingPermanent.getCard();
        if (watcher.watchedPermanentId() != null) {
            // "When it dies this turn": a delayed trigger watching one creature (Desperate Measures).
            if (trigger.relation() != ObjectRelation.THIS
                    || !dyingPermanent.getId().equals(watcher.watchedPermanentId())) {
                return;
            }
            CardEffect effect = ZoneChangeAbilityReader.strictEffect(watcher.ability());
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, watcher.sourceCard(),
                    watcher.controllerId(), watcher.sourceCard().getName() + "'s ability", List.of(effect));
            entry.setTargetId(dyingControllerId);
            entry.setTriggeringPermanentId(dyingPermanent.getId());
            entry.setTriggeringPermanentControllerId(dyingControllerId);
            entry.setNonTargeting(true);
            gameData.enqueueTrigger(entry);
            return;
        }
        if (trigger.relation() == ObjectRelation.THIS) {
            return;
        }
        if (trigger.relation().youControl() && !watcher.controllerId().equals(dyingControllerId)) {
            return;
        }
        // "Until end of turn, whenever a creature dies, its controller ...": the dying creature's controller rides
        // as the entry's non-targeting player, its toughness or power as the event value; "a nontoken creature
        // dies" binds the card that died (Consumed by History).
        CreatureDeathWatch watch = CreatureDeathWatch.of(trigger, Zone.BATTLEFIELD);
        switch (watch) {
            case ALLY -> enqueueRegisteredCreatureDeathTrigger(gameData, watcher, null, 0);
            case ANY -> enqueueRegisteredCreatureDeathTrigger(gameData, watcher, dyingControllerId,
                    Math.max(0, dyingToughnessAtDeath));
            case ALLY_NONTOKEN -> {
                if (!dyingCard.isToken()) {
                    enqueueRegisteredCreatureDeathTrigger(gameData, watcher, dyingControllerId,
                            Math.max(0, dyingPowerAtDeath));
                }
            }
            case ANY_NONTOKEN -> {
                if (!dyingCard.isToken()) {
                    enqueueRegisteredCreatureDeathTrigger(gameData, watcher, dyingCard.getId(), 0);
                }
            }
            default -> {
            }
        }
    }

    private void enqueueRegisteredCreatureDeathTrigger(GameData gameData, TemporaryGlobalTriggeredAbility watcher,
                                                       UUID targetId, int eventValue) {
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                watcher.sourceCard(),
                watcher.controllerId(),
                watcher.sourceCard().getName() + "'s ability",
                new ArrayList<>(List.of(ZoneChangeAbilityReader.strictEffect(watcher.ability()))));
        entry.setTargetId(targetId);
        entry.setEventValue(eventValue);
        entry.setNonTargeting(true);
        gameData.enqueueTrigger(entry);
        gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
        log.info("Game {} - {} temporary global creature-death trigger fires",
                gameData.id, watcher.sourceCard().getName());
    }

    /** The "whenever a creature dies" trigger condition of {@code ability}, or {@code null} when it has none. */
    private static ZoneChangeTrigger creatureDeathTrigger(TriggeredAbility ability) {
        for (TriggerCondition condition : ability.conditions()) {
            if (condition.trigger() instanceof ZoneChangeTrigger trigger
                    && trigger.from().equals(Set.of(Zone.BATTLEFIELD)) && trigger.to() == Zone.GRAVEYARD
                    && trigger.causes().isEmpty()) {
                return trigger;
            }
        }
        return null;
    }

    /**
     * The mana value limit of the graveyard target of {@code effect} ({@code ReturnCardFromGraveyardEffect
     * .dynamicMaxManaValue()}), evaluated as the ability triggers with {@code lastKnown}, the object that died as it
     * last existed, as the amount's source, so it looks back at the others that died with it (CR 603.10a): Soulshift X
     * counts the Kodama, "with lesser mana value" is the dying object's. The choice carries it to the stack entry.
     * {@code null} when the ability's effects have no such limit.
     */
    private Integer graveyardTargetMaxManaValueAtDeath(GameData gameData, UUID controllerId,
                                                       List<CardEffect> effects, Permanent lastKnown) {
        var target = graveyardTargetingSupport.findTarget(effects);
        if (target == null || target.maximumManaValue() == null || lastKnown == null) {
            return null;
        }
        return amountEvaluationService.evaluateAtDeath(gameData, target.maximumManaValue(), controllerId, lastKnown);
    }

    private void collectAllyCreatureOrCreatureSpellBecomesTargetOfOpponentTriggers(
            GameData gameData, Permanent targetPermanent, UUID creatureControllerId, StackEntry triggeringEntry) {
        if (!gameQueryService.isCreature(gameData, targetPermanent)) return;
        if (creatureControllerId.equals(triggeringEntry.getControllerId())) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(creatureControllerId);
        if (battlefield == null) return;

        for (Permanent source : List.copyOf(battlefield)) {
            TargetWatch watch = TargetWatch.ON_ALLY_CREATURE_OR_CREATURE_SPELL_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY;
            for (TriggeredAbility ability : targetAbilities(gameData, source, watch, true)) {
                putTargetAbility(gameData, ability, source, creatureControllerId, "'s triggered ability",
                        triggeringEntry, targetPermanent, creatureControllerId, NO_EXTRA_RECORD);
            }
        }
    }


    private void collectAllyCreatureOrCreatureSpellBecomesTargetOfOpponentTriggers(
            GameData gameData, StackEntry targetedEntry, StackEntry triggeringEntry) {
        if (targetedEntry == null || targetedEntry.getEntryType() != StackEntryType.CREATURE_SPELL) return;

        UUID creatureControllerId = targetedEntry.getControllerId();
        if (creatureControllerId == null || creatureControllerId.equals(triggeringEntry.getControllerId())) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(creatureControllerId);
        if (battlefield == null) return;

        for (Permanent source : List.copyOf(battlefield)) {
            TargetWatch watch = TargetWatch.ON_ALLY_CREATURE_OR_CREATURE_SPELL_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY;
            for (TriggeredAbility ability : targetAbilities(gameData, source, watch, true)) {
                putTargetAbility(gameData, ability, source, creatureControllerId, "'s triggered ability",
                        triggeringEntry, null, creatureControllerId, NO_EXTRA_RECORD);
            }
        }
    }


    /**
     * Records on the triggered abilities already on the stack whose source is an Aura enchanting the permanent that
     * dies the permanent's last-known controller (CR 608.2h) and the Aura as it is now, whatever their effects are:
     * "enchanted permanent's controller" can no longer be read from the permanent once it has left.
     */
    public void recordEnchantedPermanentDeparture(GameData gameData, UUID dyingPermanentId,
                                                  UUID dyingPermanentControllerId) {
        Map<UUID, Permanent> auras = new LinkedHashMap<>();
        gameData.forEachPermanent((playerId, perm) -> auras.put(perm.getId(), perm));
        gameData.simultaneousDyingPermanents.forEach(auras::putIfAbsent);
        for (Permanent perm : auras.values()) {
            if (!dyingPermanentId.equals(perm.getAttachedTo())
                    || perm.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)) {
                continue;
            }
            for (StackEntry pending : gameData.stack) {
                if (pending.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && perm.getId().equals(pending.getSourcePermanentId())) {
                    pending.rememberRemovedPermanentController(dyingPermanentId, dyingPermanentControllerId);
                    if (pending.getSourcePermanentSnapshot() == null) {
                        pending.setSourcePermanentSnapshot(new Permanent(perm));
                    }
                }
            }
        }
    }

    /**
     * The emblems' "whenever [a filter permanent] is put into your graveyard from the battlefield" abilities
     * ({@code Triggers.putIntoGraveyardFrom(BATTLEFIELD, filter, YOU_OWN)}; Daretti, Scrap Savant's "return that card to
     * the battlefield at the beginning of the next end step") for {@code departed}, which {@code controllerId}
     * controlled and which was just put into {@code graveyardOwnerId}'s graveyard as {@code card}. Abilities of emblems
     * function in the command zone (CR 114.4); a permanent goes to its owner's graveyard, so "your graveyard" is the
     * emblem controller's own. This is a leaves-the-battlefield ability (CR 603.6c): its filter is judged on the
     * permanent as it last existed on the battlefield (CR 603.10a), and a token triggers it too before it ceases to
     * exist (CR 111.7). Each matching ability goes on the stack once as one object of its effects (CR 603.3), the
     * emblem's source card as its source, the permanent's card ("that card", for this stay in the graveyard,
     * CR 400.7) and last-known permanent as its triggering card and permanent. The reader judges no other relation,
     * cause, option or target; an ability with one fails loudly instead of triggering wrongly. The emblems'
     * "whenever a creature dies" abilities are {@link #collectEmblemCreatureDeathTriggers}'.
     */
    public void checkEmblemPutIntoGraveyardFromBattlefieldTriggers(GameData gameData, UUID graveyardOwnerId,
                                                                   UUID controllerId, Card card,
                                                                   Permanent departed) {
        if (card == null || gameData.emblems.stream().allMatch(emblem -> emblem.triggeredAbilities().isEmpty())) {
            return;
        }
        Permanent lastKnown = departedAsItLastExisted(gameData, departed);
        for (Emblem emblem : List.copyOf(gameData.emblems)) {
            for (TriggeredAbility ability : emblem.triggeredAbilities()) {
                if (emblemSeesPutIntoYourGraveyardFromBattlefield(gameData, emblem, ability, graveyardOwnerId,
                        lastKnown)) {
                    putEmblemGraveyardFromBattlefieldAbilityOnStack(gameData, emblem, ability, card, lastKnown,
                            controllerId);
                }
            }
        }
    }

    /**
     * {@code departed} as the departure's rules event holds it, with the characteristics it left the battlefield
     * with; {@code departed} itself when the log no longer holds that event.
     */
    private static Permanent departedAsItLastExisted(GameData gameData, Permanent departed) {
        List<RulesEventBatch> pending = gameData.rulesEvents.pending();
        for (int index = pending.size() - 1; index >= 0; index--) {
            List<RulesEvent> events = pending.get(index).events();
            for (int eventIndex = events.size() - 1; eventIndex >= 0; eventIndex--) {
                if (events.get(eventIndex) instanceof ZoneChangeEvent zoneChange && zoneChange.leftBattlefield()
                        && zoneChange.permanent() != null
                        && zoneChange.permanent().getId().equals(departed.getId())) {
                    return zoneChange.permanent();
                }
            }
        }
        return departed;
    }

    /**
     * Whether {@code ability} of {@code emblem} triggers on {@code lastKnown} being put into {@code graveyardOwnerId}'s
     * graveyard from the battlefield. A battlefield-to-graveyard trigger whose relation isn't an owner's is the
     * creature-death reader's.
     */
    private boolean emblemSeesPutIntoYourGraveyardFromBattlefield(GameData gameData, Emblem emblem,
                                                                  TriggeredAbility ability, UUID graveyardOwnerId,
                                                                  Permanent lastKnown) {
        for (TriggerCondition condition : ability.conditions()) {
            if (!(condition.trigger() instanceof ZoneChangeTrigger trigger)
                    || !isPutIntoYourGraveyardFromBattlefield(trigger)) {
                continue;
            }
            if (!trigger.causes().isEmpty() || !trigger.excludedTo().isEmpty()
                    || trigger.causedBy() != com.github.laxika.magicalvibes.model.trigger.PlayerRelation.ANY
                    || trigger.duringYourTurn() || trigger.castingSpell() != null
                    || !ability.options().equals(TriggerOptions.DEFAULT) || ability.targetFilter() != null
                    || ability.effects().stream().anyMatch(effect -> effect.effect().targetSpec() != TargetSpec.NONE)) {
                throw new IllegalStateException(emblemName(emblem) + " has a graveyard ability its reader doesn't "
                        + "judge: " + ability);
            }
            if (emblem.controllerId().equals(graveyardOwnerId) && (trigger.filter() == null
                    || TriggeredAbilityStackService.departedFilterMatches(predicateEvaluationService,
                    trigger.filter(), lastKnown, DepartedAttachments.NONE, emblemFilterContext(gameData, emblem)))) {
                return true;
            }
        }
        return false;
    }

    /** "Whenever [a permanent] is put into your graveyard from the battlefield": an owner's battlefield-to-graveyard trigger. */
    private static boolean isPutIntoYourGraveyardFromBattlefield(ZoneChangeTrigger trigger) {
        return trigger.relation() == ObjectRelation.YOU_OWN && trigger.from().equals(Set.of(Zone.BATTLEFIELD))
                && trigger.to() == Zone.GRAVEYARD;
    }

    private void putEmblemGraveyardFromBattlefieldAbilityOnStack(GameData gameData, Emblem emblem,
                                                                 TriggeredAbility ability, Card card,
                                                                 Permanent lastKnown, UUID controllerId) {
        Card source = emblem.sourceCard() != null ? emblem.sourceCard() : card;
        String description = emblemName(emblem);
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                source,
                emblem.controllerId(),
                description,
                new ArrayList<>(ability.effects().stream().map(EffectRegistration::effect).toList()));
        entry.setTriggeringCard(card.getId(), gameData.graveyardEntryVersion(card.getId()));
        entry.setTriggeringPermanentId(lastKnown.getId());
        entry.setTriggeringPermanent(lastKnown);
        entry.setTriggeringPermanentControllerId(controllerId);
        entry.setNonTargeting(true);
        gameData.stack.add(entry);
        gameLogService.append(gameData, GameLog.text(description + " triggers for " + card.getName()
                + " put into a graveyard."));
        log.info("Game {} - {} triggers for {} put into a graveyard from the battlefield", gameData.id, description,
                card.getName());
    }

    /** Fires "whenever you win a coin flip" triggers for the winning player's battlefield. */
    public void checkControllerWinsCoinFlipTriggers(GameData gameData, UUID winningPlayerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(winningPlayerId);
        TriggerContext ctx = new TriggerContext.CoinFlipWon(winningPlayerId);
        if (battlefield != null) {
            for (Permanent perm : List.copyOf(battlefield)) {
                if (!hasActiveTriggeredAbilities(gameData, perm)) continue;
                for (List<CardEffect> effects : actionAbilities(perm.getCard(),
                        ActionWatch.ON_CONTROLLER_WINS_COIN_FLIP)) {
                    var match = new TriggerMatchContext(gameData, perm, winningPlayerId, effects);
                    registry.dispatch(match, ActionWatch.ON_CONTROLLER_WINS_COIN_FLIP, ctx);
                }
            }
        }

        gameData.forEachPermanent((controllerId, permanent) -> {
            if (!hasActiveTriggeredAbilities(gameData, permanent)) return;
            for (List<CardEffect> effects : actionAbilities(permanent.getCard(),
                    ActionWatch.ON_ANY_PLAYER_WINS_COIN_FLIP)) {
                var match = new TriggerMatchContext(gameData, permanent, controllerId, effects);
                registry.dispatch(match, ActionWatch.ON_ANY_PLAYER_WINS_COIN_FLIP, ctx);
            }
        });
    }

    /**
     * Whether a permanent's printed and granted abilities are still active, so its triggered
     * abilities can trigger. Mirrors the checks {@link #dispatchWatch} applies before dispatching.
     */
    private boolean hasActiveTriggeredAbilities(GameData gameData, Permanent perm) {
        if (perm.isLosesAllAbilitiesUntilEndOfTurn()) return false;
        GameQueryService.StaticBonus staticBonus = gameQueryService.computeStaticBonus(gameData, perm);
        return !staticBonus.losesAllAbilities() && !staticBonus.losesAllNonManaAbilities()
                && !gameQueryService.hasLostPrintedAbilities(gameData, perm);
    }

    /**
     * Queues one "copy that spell" delayed trigger per unit of instant/sorcery copy-rider mana
     * (Primal Wellspring, Pyromancer's Goggles) spent on this matching spell (CR 106.6a); the copy
     * may choose new targets.
     */
    private void queueManaLinkedSpellCopyTriggers(GameData gameData, Card spellCard, UUID castingPlayerId,
                                                  int copies) {
        if (copies <= 0) return;
        StackEntry spellEntry = null;
        for (StackEntry se : gameData.stack) {
            if (se.getTargetableId().equals(spellCard.getId())) {
                spellEntry = se;
                break;
            }
        }
        if (spellEntry == null) return;
        for (int copyNumber = 0; copyNumber < copies; copyNumber++) {
            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    spellCard,
                    castingPlayerId,
                    "Copy " + spellCard.getName(),
                    new ArrayList<>(List.of(new CopyControllerCastSpellEffect(
                            new StackEntry(spellEntry), castingPlayerId)))
            ));
        }
        gameLogService.append(gameData, GameLog.cardThen(spellCard, " is copied."));
        log.info("Game {} - {} mana-linked spell-copy trigger(s) queued for {}",
                gameData.id, copies, spellCard.getName());
    }

    /**
     * Queues one copy trigger per unit of copy-rider mana (Sunken Palace) spent to activate this
     * non-mana ability (CR 106.6a).
     */
    public void checkManaLinkedAbilityCopyTrigger(GameData gameData, UUID activatingPlayerId,
                                                   StackEntry abilityEntry, ActivatedAbility ability) {
        ManaPool pool = gameData.playerManaPools.get(activatingPlayerId);
        int pendingCopies = pool == null ? 0 : pool.takeSpentCopyGrantingMana(ManaCopyRider.SPELL_OR_ABILITY);
        if (pool != null) {
            pool.discardSpentCopyGrantingMana();
        }
        if (abilityEntry == null) return;
        for (int i = 0; i < pendingCopies; i++) {
            CardEffect copyEffect = new CopyControllerActivatedAbilityEffect(
                    new StackEntry(abilityEntry), ability, activatingPlayerId);
            gameData.enqueueTrigger(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    abilityEntry.getCard(),
                    activatingPlayerId,
                    abilityEntry.getCard().getName() + "'s ability",
                    new ArrayList<>(List.of(copyEffect))
            ));
            gameLogService.append(gameData,
                    GameLog.textCardText("A copy of ", abilityEntry.getCard(), "'s ability is created."));
            log.info("Game {} - mana-linked ability copy trigger queued for {}", gameData.id,
                    abilityEntry.getCard().getName());
        }
    }

    /**
     * Copy-rider mana spent to activate a mana ability triggers, but mana abilities can't be
     * copied, so the spent rider does nothing.
     */
    public void consumeManaLinkedCopyOnManaAbility(GameData gameData, UUID playerId) {
        ManaPool pool = gameData.playerManaPools.get(playerId);
        if (pool != null) {
            pool.discardSpentCopyGrantingMana();
        }
    }

    private boolean queueGraveyardCreaturePutTrigger(GameData gameData, Card sourceCard, UUID controllerId,
                                                      List<CardEffect> effects) {
        if (effects.isEmpty()) {
            return false;
        }
        long sourceEntryVersion = gameData.graveyardEntryVersion(sourceCard.getId());
        // The ability is one object of all its effects (CR 603.3); a "you may" or "you may pay" goes on the stack like
        // any other effect and is asked as it resolves (CR 603.5).
        gameData.stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                controllerId,
                sourceCard.getName() + "'s ability",
                new ArrayList<>(effects)
        ));
        // CR 400.7: the trigger refers to this stay of the card in the graveyard; once the card
        // leaves and returns, it is a new object the trigger can no longer move, and "if this card
        // is in your graveyard" is false.
        gameData.stack.getLast().setSourceGraveyardEntryVersion(sourceEntryVersion);
        gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} graveyard trigger fires when a creature enters a graveyard",
                gameData.id, sourceCard.getName());
        return true;
    }

    private void queueGraveyardCreatureDeathTrigger(GameData gameData, Card sourceCard, UUID controllerId,
                                                     List<CardEffect> effects, Card dyingCard,
                                                     UUID dyingCreatureControllerId) {
        // The ability is one object of all its effects (CR 603.3), a "you may" or "you may pay" like any other: it
        // is asked as the ability resolves (CR 603.5), and its entry records the creature that died too.
        StackEntry triggerEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                controllerId,
                sourceCard.getName() + "'s ability",
                new ArrayList<>(effects)
        );
        triggerEntry.setTriggeringCardId(dyingCard.getId());
        // "Its controller" is the dying creature's last-known controller (CR 608.2h), whatever the effects are.
        triggerEntry.setTriggeringPermanentControllerId(dyingCreatureControllerId);
        gameData.stack.add(triggerEntry);
        gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} graveyard creature-death trigger queued",
                gameData.id, sourceCard.getName());
    }

    /** Fires "whenever you lose a coin flip" triggers for the losing player's battlefield. */
    public void checkControllerLosesCoinFlipTriggers(GameData gameData, UUID losingPlayerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(losingPlayerId);
        if (battlefield == null) return;

        TriggerContext ctx = new TriggerContext.CoinFlipLost(losingPlayerId);
        for (Permanent perm : List.copyOf(battlefield)) {
            for (List<CardEffect> effects : actionAbilities(perm.getCard(),
                    ActionWatch.ON_CONTROLLER_LOSES_COIN_FLIP)) {
                var match = new TriggerMatchContext(gameData, perm, losingPlayerId, effects);
                registry.dispatch(match, ActionWatch.ON_CONTROLLER_LOSES_COIN_FLIP, ctx);
            }
        }
    }

    private void collectPlanarCreatureDeathTriggers(GameData gameData, Card dyingCard,
                                                    Permanent dyingPermanent,
                                                    UUID dyingCreatureControllerId) {
        if (gameData.planechase == null || gameData.planechase.controllerId == null) return;

        UUID planarControllerId = gameData.planechase.controllerId;
        for (var object : List.copyOf(gameData.planechase.faceUp)) {
            // "Whenever a white creature dies" (Agyrem): the trigger's filter on the creature as it last existed.
            FilterContext planarContext = FilterContext.of(gameData).withSourceControllerId(planarControllerId)
                    .withSourceCardId(object.getCard().getId());
            List<TriggeredAbility> abilities = ZoneChangeAbilityReader.printedNarrowing(object.getCard(),
                            Triggers.creatureDies(ObjectRelation.ANY)).stream()
                    .filter(narrowed -> narrowed.filter() == null || TriggeredAbilityStackService.departedFilterMatches(
                            predicateEvaluationService, narrowed.filter(), dyingPermanent, DepartedAttachments.NONE,
                            planarContext))
                    .map(ZoneChangeAbilityReader.NarrowedAbility::ability)
                    .toList();
            // Each ability of the plane is one object of all its effects (CR 603.3), its target chosen as it goes on
            // the stack when it has one (CR 603.3d).
            for (List<CardEffect> effects : byAbility(abilities, ZoneChangeAbilityReader::strictEffects)) {
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        object.getCard(),
                        planarControllerId,
                        object.getCard().getName() + "'s ability",
                        new ArrayList<>(effects));
                entry.setSourcePlanarObject(object.copy());
                entry.setTriggeringCard(dyingCard.getId(), gameData.graveyardEntryVersion(dyingCard.getId()));
                entry.setTriggeringPermanentId(dyingPermanent.getId());
                entry.setTriggeringPermanentControllerId(dyingCreatureControllerId);
                entry.setTriggeringPermanentPowerAtTrigger(Math.max(0, dyingPermanent.getEffectivePower()));
                entry.setEventValue(Math.max(0, dyingPermanent.getEffectivePower()));
                if (effects.stream().anyMatch(effect -> effect.targetSpec().targetPredicate() != null)) {
                    queueTargetChoice(gameData, entry, new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                            object.getCard(), planarControllerId, new ArrayList<>(effects), false,
                            object.getCard().getTargetFilter(), 0, null, null, false, null, null,
                            planarControllerId, object.copy()));
                    continue;
                }

                gameData.enqueueTrigger(entry);
            }
        }
    }
    /**
     * Face-up planes' "whenever a nonblack card is put into a graveyard from anywhere" abilities. A plane is no
     * object the trigger pipeline lists, so its abilities are still collected as the card arrives.
     */
    public void checkPlanarNonblackCardPutIntoGraveyardTriggers(GameData gameData, UUID graveyardOwnerId, Card card) {
        if (card.getColors() != null && card.getColors().contains(CardColor.BLACK) || gameData.planechase == null) {
            return;
        }
        var ctx = new TriggerContext.CardPutIntoGraveyard(card, graveyardOwnerId);
        var trigger = Triggers.putIntoGraveyardFromAnywhere(Triggers.NONBLACK_CARD, ObjectRelation.ANOTHER);
        for (PlanarObject object : List.copyOf(gameData.planechase.faceUp)) {
            for (List<CardEffect> effects : byAbility(ZoneChangeAbilityReader.printed(object.getCard(), trigger),
                    ZoneChangeAbilityReader::strictEffects)) {
                registry.dispatch(new TriggerMatchContext(gameData, null, gameData.planechase.controllerId,
                                effects, object.getCard(), object.copy()),
                        ZoneChangeWatch.ON_NONBLACK_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE, ctx);
            }
        }
    }


    /** Fires effects that care when the given player foretells a card. */
    public void checkControllerForetellTriggers(GameData gameData, UUID foretellingPlayerId,
                                                Card foretoldCard) {
        if (foretellingPlayerId == null || foretoldCard == null) return;
        List<Permanent> battlefield = gameData.playerBattlefields.get(foretellingPlayerId);
        if (battlefield == null) return;

        var ctx = new TriggerContext.Foretell(foretellingPlayerId, foretoldCard);
        for (Permanent perm : List.copyOf(battlefield)) {
            dispatchWatch(gameData, perm, foretellingPlayerId, ActionWatch.ON_CONTROLLER_FORETELLS, ctx);
        }
    }

    private void collectCreatureDeathTriggerWatchers(GameData gameData, Permanent dyingPermanent) {
        for (CreatureDeathTriggerWatcher watcher : List.copyOf(gameData.creatureDeathTriggerWatchers)) {
            if (watcher.onlyOthers() && dyingPermanent.getId().equals(watcher.sourcePermanentId())) {
                continue;
            }
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    watcher.sourceCard(),
                    watcher.controllerId(),
                    watcher.sourceCard().getName() + "'s ability",
                    new ArrayList<>(List.of(watcher.effect())),
                    (UUID) null,
                    watcher.sourcePermanentId());
            gameData.enqueueTrigger(entry);
            gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
            log.info("Game {} - {} triggers because a creature died",
                    gameData.id, watcher.sourceCard().getName());
        }
        for (TargetedCreatureDeathTriggerWatcher watcher
                : List.copyOf(gameData.targetedCreatureDeathTriggerWatchers)) {
            if (!watcher.watchedPermanentId().equals(dyingPermanent.getId())) {
                continue;
            }
            gameData.targetedCreatureDeathTriggerWatchers.remove(watcher);
            if (dyingPermanent.getCounterCount(watcher.counterType()) <= 0) {
                continue;
            }
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    watcher.sourceCard(),
                    watcher.controllerId(),
                    watcher.sourceCard().getName() + "'s ability",
                    new ArrayList<>(List.of(watcher.effect())),
                    (UUID) null,
                    (UUID) null);
            entry.setTriggeringPermanentId(dyingPermanent.getId());
            gameData.enqueueTrigger(entry);
            gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
            log.info("Game {} - {} triggers because its watched creature died",
                    gameData.id, watcher.sourceCard().getName());
        }
    }

    /** Fires delayed triggers registered for creatures damaged by a particular source object. */
    public void triggerDelayedDamagedCreatureDeathTriggers(GameData gameData, Card dyingCard,
                                                            UUID dyingCreatureControllerId) {
        if (dyingCard == null) {
            return;
        }
        UUID dyingCardId = dyingCard.getId();
        for (DamagedCreatureDeathTriggerWatcher watcher
                : List.copyOf(gameData.damagedCreatureDeathTriggerWatchers)) {
            if (!watcher.allControllers() && !watcher.controllerId().equals(dyingCreatureControllerId)
                    || !gameData.creatureCardsDamagedThisTurnBySource
                    .getOrDefault(watcher.damageSourceId(), Set.of()).contains(dyingCardId)) {
                continue;
            }
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    watcher.sourceCard(),
                    watcher.controllerId(),
                    watcher.sourceCard().getName() + "'s ability",
                    new ArrayList<>(List.of(watcher.effect())),
                    (UUID) null,
                    (UUID) null);
            entry.setTriggeringCardId(dyingCardId);
            // "Its controller" is the dying creature's last-known controller (CR 608.2h), whatever the effects are.
            entry.setTriggeringPermanentControllerId(dyingCreatureControllerId);
            gameData.enqueueTrigger(entry);
            gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
            log.info("Game {} - {} triggers because a creature it damaged died",
                    gameData.id, watcher.sourceCard().getName());
        }
    }

    /**
     * The emblems' "whenever a creature dies" abilities ({@code Triggers.creatureDies(ObjectRelation.ANY)}; Liliana,
     * Defiant Necromancer's "return it to the battlefield under your control at the beginning of the next end step",
     * Sephiroth, One-Winged Angel's "target opponent loses 1 life and you gain 1 life"). Abilities of emblems function
     * in the command zone (CR 114.4), so the permanent sweep never sees them. Each ability goes on the stack once as
     * one object of its effects (CR 603.3), the emblem's source card as its source, and records the creature that
     * died ("it": its card, its last-known permanent and controller) whatever its effects are; a targeted one's target
     * is chosen as it goes on the stack (CR 603.3d), restricted by the ability's own target filter. The reader judges
     * the trigger's filter on the creature as it last existed and no options; an ability with options or another
     * relation fails loudly instead of triggering wrongly. "Whenever [a permanent] is put into your graveyard from the
     * battlefield" is {@link #checkEmblemPutIntoGraveyardFromBattlefieldTriggers}'.
     */
    private void collectEmblemCreatureDeathTriggers(GameData gameData, Card dyingCard,
                                                     TriggerContext.CreatureDeath context) {
        for (Emblem emblem : List.copyOf(gameData.emblems)) {
            int previousCopies = gameData.beginTriggeredAbilityCopies(1 +
                    gameQueryService.countAdditionalCreatureDeathTriggeredAbilityTriggersForEmblem(
                            gameData, emblem.controllerId()));
            try {
                for (TriggeredAbility ability : emblem.triggeredAbilities()) {
                    if (emblemSeesCreatureDie(gameData, emblem, ability, context)) {
                        putEmblemDeathAbilityOnStack(gameData, emblem, ability, dyingCard, context);
                    }
                }
            } finally {
                gameData.restoreTriggeredAbilityCopies(previousCopies);
            }
        }
    }

    /** Whether {@code ability} of {@code emblem} triggers on the creature of {@code death} dying. */
    private boolean emblemSeesCreatureDie(GameData gameData, Emblem emblem, TriggeredAbility ability,
                                          TriggerContext.CreatureDeath death) {
        ZoneChangeTrigger trigger = creatureDeathTrigger(ability);
        if (trigger == null || isPutIntoYourGraveyardFromBattlefield(trigger)) {
            return false;
        }
        if (trigger.relation() != ObjectRelation.ANY || !ability.options().equals(TriggerOptions.DEFAULT)) {
            throw new IllegalStateException(emblemName(emblem) + " has a death ability its reader doesn't judge: "
                    + ability);
        }
        Permanent lastKnown = death.dyingPermanent();
        return trigger.filter() == null || lastKnown != null
                && predicateEvaluationService.matchesPermanentPredicate(lastKnown, trigger.filter(),
                FilterContext.of(gameData).withSourceControllerId(emblem.controllerId()));
    }

    private void putEmblemDeathAbilityOnStack(GameData gameData, Emblem emblem, TriggeredAbility ability,
                                              Card dyingCard, TriggerContext.CreatureDeath death) {
        Card source = emblem.sourceCard() != null ? emblem.sourceCard() : dyingCard;
        String description = emblemName(emblem);
        List<CardEffect> effects = ability.effects().stream().map(EffectRegistration::effect).toList();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                source,
                emblem.controllerId(),
                description,
                new ArrayList<>(effects));
        entry.setTriggeringCardId(dyingCard.getId());
        if (death.dyingPermanent() != null) {
            entry.setTriggeringPermanent(death.dyingPermanent());
        }
        entry.setTriggeringPermanentControllerId(death.dyingCreatureControllerId());
        entry.setNonTargeting(true);
        if (effects.stream().anyMatch(effect -> effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER))) {
            PermanentChoiceContext.DeathTriggerTarget choice = new PermanentChoiceContext.DeathTriggerTarget(
                    source, emblem.controllerId(), new ArrayList<>(effects), null, null, ability.targetFilter());
            gameData.handOffTriggerRecord(entry);
            gameData.queueInteraction(death.dyingPermanent() != null
                    ? choice.withTriggeringPermanent(death.dyingPermanent()) : choice);
            gameLogService.append(gameData, GameLog.text(description + " triggers (a creature died)."));
            log.info("Game {} - {} triggers for {} dying, awaiting its target", gameData.id, description,
                    dyingCard.getName());
            return;
        }
        if (effects.stream().anyMatch(effect -> effect.targetSpec() != TargetSpec.NONE)) {
            throw new IllegalStateException(description + " has a death ability whose target its reader doesn't "
                    + "choose: " + ability);
        }
        gameData.stack.add(entry);
        gameLogService.append(gameData, GameLog.text(description + " triggers (a creature died)."));
        log.info("Game {} - {} triggers for {} dying", gameData.id, description, dyingCard.getName());
    }

    /**
     * "Whenever a permanent is put into a graveyard from the battlefield": the ability through its handler, whole
     * (CR 603.3). The collector leaves the watcher's own move out, unless the ability watches "a permanent you
     * control".
     */
    private boolean dispatchAnyPermanentDeathTriggersForWatcher(GameData gameData, UUID watcherControllerId,
                                                                Permanent watcher, List<CardEffect> effects,
                                                                boolean ownMoveCounts, Card dyingCard,
                                                                TriggerContext.AnyPermanentGraveyard ctx) {
        if (!ownMoveCounts && watcher.getCard().getId().equals(dyingCard.getId())) {
            return false;
        }
        return !effects.isEmpty() && dispatch(new TriggerMatchContext(gameData, watcher, watcherControllerId, effects),
                ZoneChangeWatch.ON_ANY_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD, ctx);
    }

    /** Whether {@code candidate}'s ability watches "a permanent you control", which the source itself is too. */
    private static boolean watchesPermanentsYouControl(TriggerCandidate candidate) {
        return candidate.ability().conditions().stream()
                .anyMatch(condition -> condition.trigger() instanceof ZoneChangeTrigger move
                        && move.relation() == ObjectRelation.YOU_CONTROL);
    }

    /**
     * Fires the delayed "when that permanent leaves the battlefield" triggers that watch {@code target}
     * (CR 603.7): the destination-keyed {@code DelayedZoneChangeTrigger}s and the leaves-the-battlefield
     * abilities a resolving spell registered for it. The permanent's own leaves-the-battlefield abilities go
     * through the trigger pipeline.
     */
    public void checkDelayedLeavesBattlefieldTriggers(GameData gameData, Permanent target, Zone destination) {
        for (var delayed : gameData.drainDelayedActions(
                com.github.laxika.magicalvibes.model.action.DelayedZoneChangeTrigger.class,
                action -> action.watchedPermanentId().equals(target.getId()))) {
            CardEffect effect = delayed.effectsByDestination().get(destination);
            if (effect == null) continue;
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, delayed.sourceCard(),
                    delayed.controllerId(), delayed.sourceCard().getName() + "'s delayed trigger",
                    new ArrayList<>(List.of(effect)), null, delayed.sourcePermanentId());
            entry.setTriggeringCardId(target.getOriginalCard().getId());
            if (destination == Zone.EXILE) {
                entry.setTriggeringCardExileEntryVersion(
                        gameData.findExiledCard(entry.getTriggeringCardId()) == null ? -1L
                                : gameData.exileEntryVersions.getOrDefault(entry.getTriggeringCardId(), 0L));
            }
            entry.setTriggeringCardGraveyardEntryVersion(
                    destination == Zone.GRAVEYARD ? -1L : 0L);
            entry.setNonTargeting(true);
            gameData.enqueueTrigger(entry);
        }
        for (TemporaryGlobalTriggeredAbility watcher : List.copyOf(gameData.temporaryGlobalTriggeredAbilities)) {
            if (!watcher.ability().conditions().getFirst().trigger()
                    .equals(Triggers.leavesBattlefield(ObjectRelation.THIS))
                    || !target.getId().equals(watcher.watchedPermanentId())) {
                continue;
            }
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    watcher.sourceCard(),
                    watcher.controllerId(),
                    watcher.sourceCard().getName() + "'s delayed trigger",
                    new ArrayList<>(List.of(ZoneChangeAbilityReader.strictEffect(watcher.ability()))),
                    null,
                    target.getId());
            entry.setNonTargeting(true);
            gameData.enqueueTrigger(entry);
            gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
        }
    }

    /**
     * Fires delayed "when that creature leaves the battlefield this turn, sacrifice this creature"
     * triggers (Kjeldoran Elite Guard). Drains matching {@link DelayedSacrificeSourceWhenTargetLeaves}
     * entries and enqueues a {@link SacrificeSelfEffect} for each. Called from every leave path in
     * {@link com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService}.
     */
    public void processDelayedSacrificeSourceWhenTargetLeaves(GameData gameData, Permanent leavingPermanent) {
        if (!gameData.hasDelayedAction(DelayedSacrificeSourceWhenTargetLeaves.class)) {
            return;
        }
        UUID leavingId = leavingPermanent.getId();
        for (DelayedSacrificeSourceWhenTargetLeaves delayed : gameData.drainDelayedActions(
                DelayedSacrificeSourceWhenTargetLeaves.class,
                d -> leavingId.equals(d.watchedPermanentId()))) {
            StackEntry se = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    delayed.sourceCard(),
                    delayed.controllerId(),
                    delayed.sourceCard().getName() + "'s delayed trigger",
                    new ArrayList<>(List.of(new SacrificeSelfEffect())),
                    null,
                    delayed.sourcePermanentId());
            se.setNonTargeting(true);
            gameData.enqueueTrigger(se);
            gameLogService.append(gameData,
                    GameLog.text(delayed.sourceCard().getName() + "'s delayed trigger triggers."));
            log.info("Game {} - {} delayed leave-trigger fires (watched {} left); sacrifice source {}",
                    gameData.id, delayed.sourceCard().getName(), leavingPermanent.getCard().getName(),
                    delayed.sourcePermanentId());
        }
    }

    /**
     * Fires delayed "when this creature leaves the battlefield this turn, sacrifice that creature"
     * triggers (Phantasmal Mount). Drains matching {@link DelayedSacrificeTargetWhenSourceLeaves}
     * entries and enqueues a {@link SacrificeSelfEffect} against the pumped target for each. Called
     * from every leave path in
     * {@link com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService}.
     */
    public void processDelayedSacrificeTargetWhenSourceLeaves(GameData gameData, Permanent leavingPermanent) {
        if (!gameData.hasDelayedAction(DelayedSacrificeTargetWhenSourceLeaves.class)) {
            return;
        }
        UUID leavingId = leavingPermanent.getId();
        for (DelayedSacrificeTargetWhenSourceLeaves delayed : gameData.drainDelayedActions(
                DelayedSacrificeTargetWhenSourceLeaves.class,
                d -> leavingId.equals(d.watchedPermanentId()))) {
            StackEntry se = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    delayed.sourceCard(),
                    delayed.controllerId(),
                    delayed.sourceCard().getName() + "'s delayed trigger",
                    new ArrayList<>(List.of(new SacrificeSelfEffect())),
                    null,
                    delayed.targetPermanentId());
            se.setNonTargeting(true);
            gameData.enqueueTrigger(se);
            gameLogService.append(gameData,
                    GameLog.text(delayed.sourceCard().getName() + "'s delayed trigger triggers."));
            log.info("Game {} - {} delayed leave-trigger fires (source {} left); sacrifice target {}",
                    gameData.id, delayed.sourceCard().getName(), leavingPermanent.getCard().getName(),
                    delayed.targetPermanentId());
        }
    }

    /**
     * Fires delayed "when this artifact leaves the battlefield this turn, destroy that creature"
     * triggers (War Barge). Drains matching registrations and enqueues a non-targeting destruction
     * for each captured target. Called from every leave path in
     * {@link com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService}.
     */
    public void processDelayedDestroyTargetWhenSourceLeaves(GameData gameData, Permanent leavingPermanent) {
        if (!gameData.hasDelayedAction(DelayedDestroyTargetWhenSourceLeaves.class)) {
            return;
        }
        UUID leavingId = leavingPermanent.getId();
        for (DelayedDestroyTargetWhenSourceLeaves delayed : gameData.drainDelayedActions(
                DelayedDestroyTargetWhenSourceLeaves.class,
                d -> leavingId.equals(d.watchedPermanentId()))) {
            enqueueDelayedDestroyTarget(gameData, delayed, leavingPermanent);
        }
    }

    /**
     * Fires delayed "when this creature ... becomes untapped, destroy that creature" triggers (Merieke Ri
     * Berit) for the permanent that just became untapped. Each fires only once (CR 603.7b).
     */
    private void processDelayedDestroyTargetWhenSourceUntaps(GameData gameData, Permanent untappedPermanent) {
        if (!gameData.hasDelayedAction(DelayedDestroyTargetWhenSourceLeaves.class)) {
            return;
        }
        UUID untappedId = untappedPermanent.getId();
        for (DelayedDestroyTargetWhenSourceLeaves delayed : gameData.drainDelayedActions(
                DelayedDestroyTargetWhenSourceLeaves.class,
                d -> d.alsoWhenUntapped() && untappedId.equals(d.watchedPermanentId()))) {
            enqueueDelayedDestroyTarget(gameData, delayed, untappedPermanent);
        }
    }

    /**
     * Fires delayed "when you lose control of this creature, exile that creature" triggers (Coffin Queen)
     * for a permanent whose control just changed away from {@code previousControllerId}. Each fires only
     * once (CR 603.7b).
     */
    public void processDelayedTargetWhenSourceLosesControl(GameData gameData, Permanent permanent,
                                                           UUID previousControllerId) {
        if (!gameData.hasDelayedAction(DelayedDestroyTargetWhenSourceLeaves.class)) {
            return;
        }
        for (DelayedDestroyTargetWhenSourceLeaves delayed : gameData.drainDelayedActions(
                DelayedDestroyTargetWhenSourceLeaves.class,
                d -> d.alsoWhenControlLost() && permanent.getId().equals(d.watchedPermanentId())
                        && previousControllerId.equals(d.controllerId()))) {
            enqueueDelayedDestroyTarget(gameData, delayed, permanent);
        }
    }

    private void enqueueDelayedDestroyTarget(GameData gameData, DelayedDestroyTargetWhenSourceLeaves delayed,
                                             Permanent watchedPermanent) {
        StackEntry se = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                delayed.sourceCard(),
                delayed.controllerId(),
                delayed.sourceCard().getName() + "'s delayed trigger",
                new ArrayList<>(List.of(delayed.exile()
                        ? new com.github.laxika.magicalvibes.model.effect.RemoveLinkedPermanentEffect(
                                com.github.laxika.magicalvibes.model.effect.RemoveLinkedPermanentEffect.Mode.EXILE,
                                delayed.targetPermanentId())
                        : new DestroyLinkedPermanentEffect(true, delayed.targetPermanentId()))),
                null,
                List.of());
        se.setNonTargeting(true);
        gameData.enqueueTrigger(se);
        gameLogService.append(gameData,
                GameLog.text(delayed.sourceCard().getName() + "'s delayed trigger triggers."));
        log.info("Game {} - {} delayed trigger fires (source {}); destroy target {}",
                gameData.id, delayed.sourceCard().getName(), watchedPermanent.getCard().getName(),
                delayed.targetPermanentId());
    }

    /**
     * Fires delayed counter triggers for cards that return from exile when their source leaves the
     * battlefield. The return controller is compared with the controller captured when the
     * source's enter-the-battlefield ability resolved.
     */
    public void processDelayedExileReturnCounterTriggers(GameData gameData, UUID leavingPermanentId,
                                                          List<PendingExileReturn> pendingReturns,
                                                          List<UUID> returnedPermanentIds) {
        List<DelayedExileReturnCounterTrigger> delayedTriggers = gameData.drainDelayedActions(
                DelayedExileReturnCounterTrigger.class,
                trigger -> leavingPermanentId.equals(trigger.watchedPermanentId()));
        if (delayedTriggers.isEmpty()) {
            return;
        }

        for (int i = 0; i < pendingReturns.size(); i++) {
            PendingExileReturn pending = pendingReturns.get(i);
            UUID returnedPermanentId = returnedPermanentIds.get(i);
            if (returnedPermanentId == null) {
                continue;
            }
            for (DelayedExileReturnCounterTrigger delayed : delayedTriggers) {
                if (!pending.controllerId().equals(delayed.controllerId())) {
                    continue;
                }
                StackEntry trigger = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        delayed.sourceCard(),
                        delayed.controllerId(),
                        delayed.sourceCard().getName() + "'s delayed ability",
                        new ArrayList<>(List.of(new PutCounterOnTargetPermanentEffect(
                                delayed.counterType(), delayed.counterAmount()))),
                        returnedPermanentId,
                        (UUID) null);
                trigger.setNonTargeting(true);
                gameData.enqueueTrigger(trigger);
                gameLogService.append(gameData,
                        GameLog.cardThen(delayed.sourceCard(), "'s delayed ability triggers."));
                log.info("Game {} - {} delayed return counter trigger queued for {}",
                        gameData.id, delayed.sourceCard().getName(), pending.card().getName());
            }
        }
    }
    /**
     * Face-up planes' "whenever a creature leaves the battlefield" abilities (Takenuma). A plane is no object the
     * trigger pipeline lists, so its abilities are still collected as the creature leaves.
     */
    public void checkPlanarCreatureLeavesBattlefieldTriggers(GameData gameData, Permanent leavingPermanent,
                                                             boolean wasCreature, UUID leavingControllerId) {
        if (!wasCreature) return;
        UUID leavingId = leavingPermanent.getId();
        if (gameData.planechase == null || leavingControllerId == null) {
            return;
        }
        UUID planarControllerId = gameData.planechase.controllerId;
        if (planarControllerId == null) {
            return;
        }
        for (PlanarObject object : List.copyOf(gameData.planechase.faceUp)) {
            // Each ability of the plane is one object of all its effects (CR 603.3).
            for (List<CardEffect> effects : byAbility(ZoneChangeAbilityReader.printed(
                    object.getCard(), Triggers.leavesBattlefield(Triggers.CREATURE, ObjectRelation.ANOTHER)),
                    ZoneChangeAbilityReader::strictEffects)) {
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        object.getCard(),
                        planarControllerId,
                        object.getCard().getName() + "'s ability",
                        new ArrayList<>(effects));
                entry.setSourcePlanarObject(object.copy());
                entry.setTriggeringPermanentId(leavingId);
                entry.setTriggeringPermanentControllerId(leavingControllerId);
                entry.setNonTargeting(true);
                gameData.enqueueTrigger(entry);
                gameLogService.append(gameData, GameLog.text(object.getCard().getName() + "'s ability triggers."));
                log.info("Game {} - {} planar trigger on another creature leaving the battlefield ({})",
                        gameData.id, object.getCard().getName(), leavingPermanent.getCard().getName());
            }
        }
    }

    /**
     * "Whenever another permanent leaves the battlefield" triggers. Called from every
     * leave-the-battlefield path after the permanent has been removed. Permanents that leave in the
     * same simultaneous event as the leaving permanent still see it (CR 603.10a), so their
     * snapshots are checked as well; an ability that was lost before the event does not trigger.
     */
    /**
     * Face-up schemes' "whenever another permanent leaves the battlefield" abilities, which trigger from the command
     * zone (CR 314.4). A scheme is no object the trigger pipeline lists, so its abilities are still collected as the
     * permanent leaves.
     */
    public void checkSchemePermanentLeavesBattlefieldTriggers(GameData gameData, Permanent leavingPermanent) {
        UUID leavingId = leavingPermanent.getId();

        UUID leavingOwnerId = leavingPermanent.getCard().getOwnerId();
        boolean leavingHasStaticEffects = !leavingPermanent.getCard().getStaticEffects().isEmpty();
        BiConsumer<UUID, Permanent> collect = (ownerId, perm) -> {
            if (perm.getId().equals(leavingId)) return;
            if (perm.isLosesAllAbilitiesUntilEndOfTurn()
                    || gameQueryService.hasLostAllAbilities(gameData, perm)) return;
            // CR 603.10a: look back to just before the event. An ability removed only by the leaving
            // permanent's own static effect (Humility) was not there to trigger.
            if (leavingHasStaticEffects
                    && gameQueryService.findPermanentById(gameData, perm.getId()) != null
                    && gameQueryService.withEnteringPermanent(gameData, leavingOwnerId != null ? leavingOwnerId : ownerId, leavingPermanent,
                    () -> gameQueryService.hasLostAllAbilities(gameData, perm))) return;
            // Each ability of the scheme is one object of all its effects (CR 603.3).
            for (List<CardEffect> effects : schemeLeavesBattlefieldAbilities(gameData, perm, ownerId,
                    leavingPermanent)) {
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        ownerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(effects),
                        null,
                        perm.getId()
                );
                entry.setSourcePermanentSnapshot(new Permanent(perm));
                entry.setTriggeringPermanent(leavingPermanent);
                gameData.enqueueTrigger(entry);
                gameLogService.append(gameData, GameLog.text(perm.getCard().getName() + "'s ability triggers."));
                log.info("Game {} - {} triggers on another permanent leaving the battlefield ({})",
                        gameData.id, perm.getCard().getName(), leavingPermanent.getCard().getName());
            }
        };
        // CR 314.4: a face-up scheme's triggered abilities trigger from the command zone.
        gameData.forEachFaceUpSchemeSource(collect);
    }

    /**
     * The effects of each of a face-up scheme's "whenever another permanent leaves the battlefield" abilities whose
     * filter {@code leaving} matches ("whenever your commander leaves the battlefield"), with the scheme as the
     * filter's source: one list per ability (one object on the stack, CR 603.3).
     */
    private List<List<CardEffect>> schemeLeavesBattlefieldAbilities(GameData gameData, Permanent scheme, UUID ownerId,
                                                                    Permanent leaving) {
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(scheme.getOriginalCard().getId())
                .withSourceControllerId(ownerId)
                .withSourcePermanentId(scheme.getId())
                .withSourcePermanentSnapshot(scheme);
        List<TriggeredAbility> matching = new ArrayList<>();
        for (TriggeredAbility ability : ZoneChangeWatch.ON_ANOTHER_PERMANENT_LEAVES_BATTLEFIELD
                .abilitiesOf(scheme.getCard())) {
            boolean matches = ability.conditions().stream()
                    .map(TriggerCondition::trigger)
                    .anyMatch(trigger -> trigger instanceof ZoneChangeTrigger move && (move.filter() == null
                            || predicateEvaluationService.matchesPermanentPredicate(leaving, move.filter(),
                            filterContext)));
            if (matches) {
                matching.add(ability);
            }
        }
        return byAbility(matching, ZoneChangeAbilityReader::strictEffects);
    }

    private void enqueueAllyPermanentsLeaveBattlefieldTriggers(GameData gameData, Permanent source,
                                                               UUID controllerId, List<CardEffect> effects) {
        if (!effects.isEmpty()) {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY, source.getCard(), controllerId,
                    source.getCard().getName() + "'s ability", new ArrayList<>(effects),
                    null, source.getId());
            gameData.handOffTriggerRecord(entry);
            // An ability with a target chooses it as it goes on the stack (CR 603.3d), with all its effects.
            if (targetsPermanentOrPlayer(effects) || effects.stream()
                    .anyMatch(effect -> effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD))) {
                gameData.queueInteraction(new PermanentChoiceContext.SelfTriggeredAbilityTarget(
                        source.getCard(), controllerId, new ArrayList<>(effects)));
            } else {
                gameData.enqueueTrigger(entry);
            }
            gameLogService.append(gameData, GameLog.text(source.getCard().getName() + "'s ability triggers."));
            log.info("Game {} - {} triggers when one or more permanents they control leave the battlefield",
                    gameData.id, source.getCard().getName());
        }
    }

    private void enqueueCreatureLeavesBattlefieldWithoutDyingTriggers(
            GameData gameData, Permanent source, UUID controllerId, List<CardEffect> effects) {
        if (!effects.isEmpty()) {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY, source.getCard(), controllerId,
                    source.getCard().getName() + "'s ability", new ArrayList<>(effects),
                    null, source.getId());
            gameData.handOffTriggerRecord(entry);
            // An ability with a target chooses it as it goes on the stack (CR 603.3d), with all its effects.
            if (targetsPermanentOrPlayer(effects) || effects.stream()
                    .anyMatch(effect -> effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD))) {
                gameData.queueInteraction(new PermanentChoiceContext.SelfTriggeredAbilityTarget(
                        source.getCard(), controllerId, new ArrayList<>(effects)));
            } else {
                gameData.enqueueTrigger(entry);
            }
            gameLogService.append(gameData, GameLog.text(source.getCard().getName() + "'s ability triggers."));
            log.info("Game {} - {} triggers on a creature leaving without dying",
                    gameData.id, source.getCard().getName());
        }
    }

    public void queueChosenTriggeredModalTrigger(GameData gameData, Card sourceCard, UUID controllerId,
            UUID sourcePermanentId, List<ChooseOneEffect.ChooseOneOption> chosen) {
        triggeredAbilityQueueService.queueChosenTriggeredModalTrigger(gameData, sourceCard, controllerId,
                sourcePermanentId, chosen);
    }

    public void queueChosenTriggeredModalTrigger(GameData gameData, Card sourceCard, UUID controllerId,
            UUID sourcePermanentId, List<ChooseOneEffect.ChooseOneOption> chosen, UUID triggeringCardId) {
        triggeredAbilityQueueService.queueChosenTriggeredModalTrigger(gameData, sourceCard, controllerId,
                sourcePermanentId, chosen, triggeringCardId);
    }

    public void queueChosenTriggeredModalTrigger(GameData gameData, Card sourceCard, UUID controllerId,
            UUID sourcePermanentId, List<ChooseOneEffect.ChooseOneOption> chosen, UUID triggeringCardId,
            UUID attackedTargetId) {
        triggeredAbilityQueueService.queueChosenTriggeredModalTrigger(gameData, sourceCard, controllerId,
                sourcePermanentId, chosen, triggeringCardId, attackedTargetId);
    }

    public void queueChosenTriggeredModalTrigger(GameData gameData, Card sourceCard, UUID controllerId,
            UUID sourcePermanentId, List<ChooseOneEffect.ChooseOneOption> chosen, UUID triggeringCardId,
            UUID attackedTargetId, UUID triggeringPermanentId) {
        triggeredAbilityQueueService.queueChosenTriggeredModalTrigger(gameData, sourceCard, controllerId,
                sourcePermanentId, chosen, triggeringCardId, attackedTargetId, triggeringPermanentId);
    }

    /** As above, the entry carrying {@code triggerRecord}, the collector's record of the trigger event. */
    public void queueChosenTriggeredModalTrigger(GameData gameData, Card sourceCard, UUID controllerId,
            UUID sourcePermanentId, List<ChooseOneEffect.ChooseOneOption> chosen, UUID triggeringCardId,
            UUID attackedTargetId, UUID triggeringPermanentId, StackEntry triggerRecord) {
        triggeredAbilityQueueService.queueChosenTriggeredModalTrigger(gameData, sourceCard, controllerId,
                sourcePermanentId, chosen, triggeringCardId, attackedTargetId, triggeringPermanentId,
                triggerRecord);
    }

    public void triggerDelayedPoisonOnDeath(GameData gameData, UUID dyingCreatureCardId, UUID controllerId) {
        Integer poisonAmount = gameData.creatureGivingControllerPoisonOnDeathThisTurn.remove(dyingCreatureCardId);
        if (poisonAmount == null || poisonAmount <= 0) {
            return;
        }

        poisonAmount = gameQueryService.applyPoisonCounterReplacement(gameData, controllerId, poisonAmount);
        if (poisonAmount <= 0) return;

        poisonAmount = gameQueryService.replacePoisonCounters(gameData, controllerId, poisonAmount);
        if (poisonAmount <= 0) return;
        int currentPoison = gameData.playerPoisonCounters.getOrDefault(controllerId, 0);
        gameData.playerPoisonCounters.put(controllerId, currentPoison + poisonAmount);
        checkYouPutCountersTriggers(gameData, controllerId, poisonAmount);

        String playerName = gameData.playerIdToName.get(controllerId);
        String logEntry = playerName + " gets " + poisonAmount + " poison counter"
                + (poisonAmount > 1 ? "s" : "") + " (delayed trigger: creature died this turn).";
        gameLogService.append(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} gets {} poison counter(s) (delayed trigger: creature died this turn)",
                gameData.id, playerName, poisonAmount);
    }

    /**
     * Delayed "return that card when it dies this turn" (Graceful Reprieve, Supernatural Stamina,
     * Adarkar Valkyrie): if the dying creature's card was registered, push a triggered ability that
     * returns it from its owner's graveyard to the battlefield. Fires at most once per registration.
     */
    public void triggerDelayedReturnOnDeath(GameData gameData, UUID dyingPermanentCardId, Card graveyardCard, UUID ownerId) {
        List<DelayedReturnOnDeath> delayedReturns = gameData.creaturesReturnedToBattlefieldOnDeathThisTurn
                .remove(dyingPermanentCardId);
        if (delayedReturns == null || delayedReturns.isEmpty()) {
            return;
        }

        for (DelayedReturnOnDeath delayedReturn : delayedReturns) {
            if (delayedReturn.requireControllerGraveyard()
                    && !delayedReturn.controllerId().equals(ownerId)) {
                continue;
            }
            UUID triggerControllerId = delayedReturn.controllerId();
            Card triggerSource = delayedReturn.sourceCard() != null ? delayedReturn.sourceCard() : graveyardCard;

            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    triggerSource,
                    triggerControllerId,
                    "Return " + graveyardCard.getName() + " to the battlefield",
                    new ArrayList<>(List.of(new ReturnTriggeringCardFromGraveyardToBattlefieldEffect(
                            delayedReturn.enterTapped(), delayedReturn.returnUnderController())))
            );
            entry.setTriggeringCardId(graveyardCard.getId());
            entry.setTriggeringCardGraveyardEntryVersion(
                    gameData.graveyardEntryVersion(graveyardCard.getId()));
            gameData.stack.add(entry);

            gameLogService.append(gameData, GameLog.cardThen(graveyardCard, " will return to the battlefield (it died this turn)."));
            log.info("Game {} - Delayed return trigger: {} will return to the battlefield", gameData.id, graveyardCard.getName());
        }
    }

    /**
     * Resolves delayed effects registered for a permanent dying this turn. Creature-only registrations
     * and registrations that can also apply to other permanent types are kept separately, then combined
     * when the permanent dies. {@code dyingPermanent}, the permanent as it last existed on the battlefield
     * (CR 603.10a), is each entry's triggering permanent.
     */
    public void triggerDelayedEffectOnDeath(GameData gameData, UUID dyingPermanentCardId,
                                            UUID dyingPermanentControllerId, int dyingPermanentPower,
                                            int dyingPermanentManaValue, Permanent dyingPermanent) {
        UUID dyingPermanentId = dyingPermanent.getId();
        List<DelayedEffectOnDeath> registrations = new ArrayList<>();
        List<DelayedEffectOnDeath> creatureRegistrations =
                gameData.creatureTriggeringEffectOnDeathThisTurn.remove(dyingPermanentCardId);
        if (creatureRegistrations != null) {
            registrations.addAll(creatureRegistrations);
        }
        List<DelayedEffectOnDeath> permanentRegistrations =
                gameData.permanentTriggeringEffectOnDeathThisTurn.remove(dyingPermanentCardId);
        if (permanentRegistrations != null) {
            registrations.addAll(permanentRegistrations);
        }
        List<DelayedEffectOnDeath> graveyardRegistrations =
                gameData.permanentTriggeringEffectOnGraveyardEntry.remove(dyingPermanentCardId);
        if (graveyardRegistrations != null) {
            registrations.addAll(graveyardRegistrations);
        }
        if (registrations.isEmpty()) {
            return;
        }

        for (DelayedEffectOnDeath registration : registrations) {
            if (registration.targetPermanentId() != null
                    && !registration.targetPermanentId().equals(dyingPermanentId)) continue;

            CardEffect effect = registration.effect();
            // A delayed trigger's "if" (CR 603.7c: it is written as one effect) is checked as it triggers and again
            // as it resolves.
            if (effect instanceof ConditionalEffect conditional
                    && !conditionEvaluationService.isMet(gameData, conditional.condition(),
                    ConditionContext.forCard(registration.sourceCard(), registration.controllerId()))) {
                continue;
            }
            StackEntry delayedEntry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    registration.sourceCard(),
                    registration.controllerId(),
                    registration.sourceCard().getName() + " triggers (a creature dealt damage this way died)",
                    new ArrayList<>(List.of(effect)),
                    dyingPermanentControllerId,
                    registration.sourcePermanentId());
            delayedEntry.setEventValue(Math.max(0, dyingPermanentPower));
            delayedEntry.setDyingPermanentManaValue(Math.max(0, dyingPermanentManaValue));
            delayedEntry.setTriggeringCardId(dyingPermanentCardId);
            delayedEntry.setTriggeringCardGraveyardEntryVersion(
                    gameData.graveyardEntryVersion(dyingPermanentCardId));
            delayedEntry.setTriggeringPermanent(dyingPermanent);
            if (effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                    || effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
                gameData.handOffTriggerRecord(delayedEntry);
                gameData.queueInteraction(new PermanentChoiceContext.DeathTriggerTarget(
                        registration.sourceCard(), registration.controllerId(), List.of(effect),
                        null, null, false).withTriggeringPermanent(dyingPermanent));
                log.info("Game {} - Delayed death trigger: {} awaits a target",
                        gameData.id, registration.sourceCard().getName());
                continue;
            }

            gameData.stack.add(delayedEntry);

            log.info("Game {} - Delayed death trigger: {} triggers (a creature it damaged died this turn)",
                    gameData.id, registration.sourceCard().getName());
        }
    }

    // ── Enter-the-battlefield triggers ─────────────────────────────────

    public void checkDayNightChangeTriggers(GameData gameData, DayNight previous, DayNight current) {
        if (previous == current || previous == DayNight.NEITHER || current == DayNight.NEITHER) {
            return;
        }

        TriggerContext context = new TriggerContext.DayNightChange(previous, current);
        for (UUID controllerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
            if (battlefield != null) {
                for (Permanent permanent : new ArrayList<>(battlefield)) {
                    dispatchWatch(gameData, permanent, controllerId,
                            MiscWatch.ON_DAY_NIGHT_CHANGE, context);
                }
            }
            checkGraveyardDayNightChangeTriggers(gameData, controllerId);
        }
        triggeredAbilityQueueService.processNextDayNightTriggerTarget(gameData);
    }

    private void checkGraveyardDayNightChangeTriggers(GameData gameData, UUID controllerId) {
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        if (graveyard == null) return;

        for (Card card : new ArrayList<>(graveyard)) {
            for (List<CardEffect> effects : graveyardMiscAbilities(gameData, card,
                    MiscWatch.GRAVEYARD_ON_DAY_NIGHT_CHANGE)) {
                StackEntry triggerEntry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        card,
                        controllerId,
                        card.getName() + "'s ability",
                        new ArrayList<>(effects)
                );
                // CR 400.7: the ability refers to this graveyard object, not a later one.
                triggerEntry.setSourceGraveyardEntryVersion(gameData.graveyardEntryVersion(card.getId()));
                gameData.enqueueTrigger(triggerEntry);
                gameLogService.append(gameData, GameLog.abilityTriggers(card));
                log.info("Game {} - {} triggers from the graveyard when day/night changes",
                        gameData.id, card.getName());
            }
        }
    }

    public void processNextDayNightTriggerTarget(GameData gameData) {
        triggeredAbilityQueueService.processNextDayNightTriggerTarget(gameData);
    }

    /**
     * What a creature entering under {@code controllerId}'s control triggers besides the card abilities the trigger
     * pipeline matches and the emblems' abilities ({@link #checkEmblemEntersTriggers}): evolve (CR 702.100d), the
     * watchers and boons a resolved spell left, and the "whenever a creature you control enters" abilities a spell
     * registered until end of turn.
     */
    public void checkAllyCreatureEntersTriggers(GameData gameData, UUID controllerId, Card enteringCreature, int extraWizardTriggers) {
        if (!enteringPermanentHasType(gameData, enteringCreature, CardType.CREATURE)) return;

        Permanent enteringPermanent = findPermanentByCard(gameData, enteringCreature);
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        for (Permanent perm : new ArrayList<>(battlefield)) {
            if (perm.isLosesAllAbilitiesUntilEndOfTurn() || perm.isFaceDown() || perm.getCard() == enteringCreature) {
                continue;
            }
            if (enteringPermanent != null && gameQueryService.hasKeyword(gameData, perm, Keyword.EVOLVE)) {
                // CR 702.100d: each instance of evolve (printed or granted) triggers separately.
                int evolveInstances = gameQueryService.triggerKeywordInstances(gameData, perm, Keyword.EVOLVE);
                collectEvolveTrigger(gameData, controllerId, perm, enteringPermanent,
                        evolveInstances + extraWizardTriggers);
            }
        }

        for (CreatureEntersTriggerWatcher watcher
                : List.copyOf(gameData.allyCreatureEntersTriggerWatchers)) {
            if (!watcher.controllerId().equals(controllerId) || enteringPermanent == null) {
                continue;
            }
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    watcher.sourceCard(),
                    watcher.controllerId(),
                    watcher.sourceCard().getName() + "'s ability",
                    new ArrayList<>(List.of(watcher.effect())));
            entry.setTargetId(enteringPermanent.getId());
            entry.setNonTargeting(true);
            gameData.enqueueTrigger(entry);
            gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
        }

        if (enteringPermanent != null && !gameData.deferCreatureEntersBoons) {
            collectAllyBoonTriggers(gameData, controllerId, List.of(enteringPermanent.getId()), true);
        }

        for (TemporaryGlobalTriggeredAbility watcher : List.copyOf(gameData.temporaryGlobalTriggeredAbilities)) {
            if (!watches(watcher, ZoneChangeWatch.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD)
                    || !watcher.controllerId().equals(controllerId)) {
                continue;
            }

            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    watcher.sourceCard(),
                    watcher.controllerId(),
                    watcher.sourceCard().getName() + "'s ability",
                    new ArrayList<>(List.of(ZoneChangeAbilityReader.strictEffect(watcher.ability()))));
            entry.setNonTargeting(true);
            gameData.enqueueTrigger(entry);
            gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
            log.info("Game {} - {} temporary ally creature-enters trigger fires",
                    gameData.id, watcher.sourceCard().getName());
        }
    }

    /**
     * Whether a temporary global ability has a trigger condition {@code watch} stands for, or one that narrows it down
     * by a filter ("a nontoken creature an opponent controls"), which {@code entering} matches as it entered.
     */
    private boolean watchesEntering(GameData gameData, TemporaryGlobalTriggeredAbility watcher, ZoneChangeWatch watch,
                                    Permanent entering) {
        for (TriggerCondition condition : watcher.ability().conditions()) {
            if (!(condition.trigger() instanceof ZoneChangeTrigger trigger)
                    || ZoneChangeWatch.of(watcher.ability(), trigger, Zone.BATTLEFIELD) != watch) {
                continue;
            }
            if (trigger.filter() == null || predicateEvaluationService.matchesPermanentPredicate(entering,
                    trigger.filter(), FilterContext.of(gameData).withSourceControllerId(watcher.controllerId())
                            .withSourceCardId(watcher.sourceCard().getId()))) {
                return true;
            }
        }
        return false;
    }

    /** Whether a temporary global ability's trigger is one of those {@code watch} stands for. */
    private static boolean watches(TemporaryGlobalTriggeredAbility watcher, ZoneChangeWatch watch) {
        return watcher.ability().conditions().stream().anyMatch(condition -> watch.keys().stream()
                .anyMatch(key -> key.trigger().equals(condition.trigger())));
    }

    private void collectCreatureSpellBoonTriggers(GameData gameData, Card spellCard,
                                                 UUID castingPlayerId, boolean castFaceDown) {
        if (!castFaceDown && !spellCard.hasType(CardType.CREATURE)) return;
        for (Boon boon : List.copyOf(gameData.boons)) {
            if (boon.trigger() != com.github.laxika.magicalvibes.model.BoonTrigger.CREATURE_CAST
                    || !castingPlayerId.equals(boon.controllerId())) continue;
            gameData.boons.remove(boon);
            if (boon.remainingUses() > 1) {
                gameData.boons.add(boon.withRemainingUses(boon.remainingUses() - 1));
            }
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, boon.sourceCard(),
                    boon.controllerId(), boon.sourceCard().getName() + "'s boon",
                    new ArrayList<>(List.of(boon.effect())));
            entry.setTriggeringCardId(spellCard.getId());
            entry.setNonTargeting(true);
            gameData.enqueueTrigger(entry);
            gameLogService.append(gameData, GameLog.abilityTriggers(boon.sourceCard()));
        }
    }

    /**
     * Queues the controller's boons that fire when permanents enter. {@link BoonTrigger#CREATURE_ENTERS}
     * boons watch creatures only; {@link BoonTrigger#PERMANENT_ENTERS} boons watch every permanent, so the
     * creature entries ({@code creatureEntries}) and the noncreature entries are offered separately.
     */
    private void collectAllyBoonTriggers(GameData gameData, UUID controllerId,
                                         List<UUID> enteringPermanentIds, boolean creatureEntries) {
        if (enteringPermanentIds == null || enteringPermanentIds.isEmpty()) return;

        for (Boon boon : List.copyOf(gameData.boons)) {
            if ((boon.trigger() != com.github.laxika.magicalvibes.model.BoonTrigger.CREATURE_ENTERS
                    && boon.trigger() != com.github.laxika.magicalvibes.model.BoonTrigger.ONE_OR_MORE_CREATURES_ENTER
                    && boon.trigger() != com.github.laxika.magicalvibes.model.BoonTrigger.PERMANENT_ENTERS)
                    || !boon.controllerId().equals(controllerId)) continue;

            Permanent enteringPermanent = null;
            CardEffect resolved = null;
            List<UUID> matchingEnteringIds = new ArrayList<>();
            for (UUID enteringPermanentId : enteringPermanentIds) {
                Permanent candidate = gameQueryService.findPermanentById(gameData, enteringPermanentId);
                if (candidate == null) continue;
                boolean candidateIsCreature = gameQueryService.isCreature(gameData, candidate);
                if ((boon.trigger() == com.github.laxika.magicalvibes.model.BoonTrigger.CREATURE_ENTERS
                        || boon.trigger() == com.github.laxika.magicalvibes.model.BoonTrigger.ONE_OR_MORE_CREATURES_ENTER)
                        ? !candidateIsCreature
                        : candidateIsCreature != creatureEntries) {
                    continue;
                }
                // "Whenever one or more Merfolk enter under your control": the boon's filter on the entering card.
                if (boon.filter() != null && !predicateEvaluationService.matchesCardPredicate(candidate.getCard(),
                        boon.filter(), boon.sourceCard().getId(), gameData, controllerId)) {
                    continue;
                }
                if (enteringPermanent == null) {
                    enteringPermanent = candidate;
                    resolved = boon.effect();
                }
                matchingEnteringIds.add(candidate.getId());
            }
            if (enteringPermanent == null || resolved == null) continue;

            if (matchingEnteringIds.size() > 1 && boon.targetFilter() == null
                    && boon.trigger() != com.github.laxika.magicalvibes.model.BoonTrigger.ONE_OR_MORE_CREATURES_ENTER
                    && resolved.targetSpec().admits(TargetPredicate.Kind.PERMANENT)) {
                // "Whenever a creature you control enters, [do something to] it": every creature that entered
                // together triggers the boon separately and consumes its own use.
                gameData.boons.remove(boon);
                int usesLeft = boon.remainingUses();
                for (UUID matchingId : matchingEnteringIds) {
                    if (usesLeft <= 0) break;
                    Permanent matching = gameQueryService.findPermanentById(gameData, matchingId);
                    if (matching == null) continue;
                    usesLeft--;
                    StackEntry perCreatureEntry = new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            boon.sourceCard(),
                            boon.controllerId(),
                            boon.sourceCard().getName() + "'s boon",
                            new ArrayList<>(List.of(resolved)));
                    perCreatureEntry.setTargetId(matching.getId());
                    perCreatureEntry.setTriggeringPermanentId(matching.getId());
                    perCreatureEntry.setTriggeringCardId(matching.getCard().getId());
                    perCreatureEntry.setNonTargeting(true);
                    gameData.enqueueTrigger(perCreatureEntry);
                    gameLogService.append(gameData, GameLog.abilityTriggers(boon.sourceCard()));
                }
                if (usesLeft > 0) {
                    gameData.boons.add(boon.withRemainingUses(usesLeft));
                }
                continue;
            }

            gameData.boons.remove(boon);
            if (boon.remainingUses() > 1) {
                gameData.boons.add(boon.withRemainingUses(boon.remainingUses() - 1));
            }

            Permanent sourcePermanent = findPermanentByCard(gameData, boon.sourceCard());
            // "attached to one of them": when several creatures entered together the controller picks which.
            TargetFilter boonTargetFilter = boon.targetFilter();
            if (matchingEnteringIds.size() > 1
                    && resolved.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                    && (boonTargetFilter == null || boonTargetFilter instanceof PermanentPredicateTargetFilter)) {
                PermanentPredicate entered = new PermanentAnyOfPredicate(matchingEnteringIds.stream()
                        .<PermanentPredicate>map(PermanentIsSpecificPermanentPredicate::new).toList());
                boonTargetFilter = new PermanentPredicateTargetFilter(
                        boonTargetFilter instanceof PermanentPredicateTargetFilter existing
                                ? new PermanentAllOfPredicate(List.of(existing.predicate(), entered)) : entered,
                        "Target must be one of the creatures that entered");
            }
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    boon.sourceCard(),
                    boon.controllerId(),
                    boon.sourceCard().getName() + "'s boon",
                    new ArrayList<>(List.of(resolved)));
            entry.setTargetId(enteringPermanent.getId());
            entry.setTriggeringPermanentId(enteringPermanent.getId());
            entry.setTriggeringCardId(enteringPermanent.getCard().getId());
            entry.setNonTargeting(true);
            if (boonTargetFilter != null
                    && resolved.targetSpec().admits(TargetPredicate.Kind.PERMANENT)) {
                queueEntersTargetChoice(gameData, entry, new PermanentChoiceContext.EntersTriggerTarget(
                        boon.sourceCard(), boon.controllerId(), new ArrayList<>(List.of(resolved)),
                        sourcePermanent == null ? null : sourcePermanent.getId(), enteringPermanent.getId(),
                        null, boonTargetFilter));
                gameLogService.append(gameData, GameLog.abilityTriggers(boon.sourceCard()));
            } else {
                gameData.enqueueTrigger(entry);
                gameLogService.append(gameData, GameLog.abilityTriggers(boon.sourceCard()));
            }
        }
    }

    /** Collects creature-entry triggers from face-up planes, which are not battlefield permanents. */
    public void checkPlanarCreatureEntersTriggers(GameData gameData, UUID enteringCreatureControllerId,
                                                  Card enteringCreature) {
        if (gameData.planechase == null || gameData.planechase.controllerId == null
                || !enteringPermanentHasType(gameData, enteringCreature, CardType.CREATURE)) {
            return;
        }

        UUID planarControllerId = gameData.planechase.controllerId;
        Permanent enteringPermanent = findPermanentByCard(gameData, enteringCreature);
        UUID enteringPermanentId = enteringPermanent == null ? null : enteringPermanent.getId();
        TriggerContext.PermanentEnters context = new TriggerContext.PermanentEnters(
                enteringCreature, enteringCreatureControllerId, null, 1, enteringPermanentId);
        for (PlanarObject object : List.copyOf(gameData.planechase.faceUp)) {
            for (List<CardEffect> effects : planarEnteringAbilities(gameData, object.getCard(), planarControllerId,
                    enteringPermanent, ZoneChangeWatch.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD)) {
                registry.dispatch(new TriggerMatchContext(gameData, null, planarControllerId, effects,
                                object.getCard(), object.copy()),
                        ZoneChangeWatch.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD, context);
            }
        }
    }

    /**
     * The effects of each of {@code plane}'s "whenever [an object] enters" abilities that one of {@code watches}
     * collects and whose trigger's filter {@code entering} matches as it entered, one list per ability (a plane isn't
     * on the battlefield, so the trigger pipeline doesn't match its abilities).
     */
    private List<List<CardEffect>> planarEnteringAbilities(GameData gameData, Card plane, UUID planarControllerId,
                                                           Permanent entering, ZoneChangeWatch... watches) {
        List<ZoneChangeWatch> watched = List.of(watches);
        FilterContext context = FilterContext.of(gameData).withSourceControllerId(planarControllerId)
                .withSourceCardId(plane.getId());
        List<List<CardEffect>> effects = new ArrayList<>();
        for (TriggeredAbility ability : plane.getTriggeredAbilities()) {
            boolean matched = ability.conditions().stream().anyMatch(condition ->
                    condition.trigger() instanceof ZoneChangeTrigger trigger
                            && watches(watched, ZoneChangeWatch.of(ability, trigger, Zone.BATTLEFIELD))
                            && (trigger.filter() == null || entering != null && predicateEvaluationService
                            .matchesPermanentPredicate(entering, trigger.filter(), context)));
            if (matched) {
                effects.addAll(byAbility(List.of(ability), ZoneChangeAbilityReader::strictEffects));
            }
        }
        return effects;
    }

    /**
     * Whether {@code watch} is one of {@code watched}. A plane's trigger condition that no zone-change watch collects
     * (Agyrem's "whenever a creature dies", which the creature-death collectors read) has none.
     */
    private static boolean watches(List<ZoneChangeWatch> watched, ZoneChangeWatch watch) {
        return watch != null && watched.contains(watch);
    }

    /** Collects ally-permanent-entry triggers from face-up planes, which are not battlefield permanents. */
    public void checkPlanarAllyPermanentEntersTriggers(GameData gameData, UUID enteringControllerId,
                                                        Card enteringCard) {
        if (gameData.planechase == null || gameData.planechase.controllerId == null
                || !gameData.planechase.controllerId.equals(enteringControllerId)) {
            return;
        }

        UUID planarControllerId = gameData.planechase.controllerId;
        Permanent enteringPermanent = findPermanentByCard(gameData, enteringCard);
        UUID enteringPermanentId = enteringPermanent == null ? null : enteringPermanent.getId();
        TriggerContext.PermanentEnters context = new TriggerContext.PermanentEnters(
                enteringCard, enteringControllerId, null,
                1 + gameQueryService.countETBExtraTriggers(
                        gameData, planarControllerId, enteringControllerId, enteringCard),
                enteringPermanentId);
        for (PlanarObject object : List.copyOf(gameData.planechase.faceUp)) {
            // Nyx's "a nontoken enchantment you control" narrows the any-permanent trigger to the enchantment watch's.
            for (List<CardEffect> effects : planarEnteringAbilities(gameData, object.getCard(), planarControllerId,
                    enteringPermanent, ZoneChangeWatch.ON_ALLY_PERMANENT_ENTERS_BATTLEFIELD,
                    ZoneChangeWatch.ON_ALLY_ENCHANTMENT_ENTERS_BATTLEFIELD)) {
                registry.dispatch(new TriggerMatchContext(gameData, null, planarControllerId, effects,
                                object.getCard(), object.copy()),
                        ZoneChangeWatch.ON_ALLY_PERMANENT_ENTERS_BATTLEFIELD, context);
            }
        }
    }

    /**
     * Fires turn-scoped delayed triggers registered by Beck. Each registration is a separate
     * optional draw trigger, including for tokens and creatures controlled by the other player.
     */
    public void checkCreatureEntersThisTurnTriggers(GameData gameData, UUID enteringCreatureControllerId,
                                                    Card enteringCreature) {
        if (!enteringPermanentHasType(gameData, enteringCreature, CardType.CREATURE)) return;

        List<UUID> controllers = new ArrayList<>(gameData.orderedPlayerIds);
        int activePlayerIndex = controllers.indexOf(gameData.activePlayerId);
        if (activePlayerIndex > 0) {
            List<UUID> apnapControllers = new ArrayList<>(controllers.subList(activePlayerIndex, controllers.size()));
            apnapControllers.addAll(controllers.subList(0, activePlayerIndex));
            controllers = apnapControllers;
        }

        for (UUID controllerId : controllers) {
            List<Card> sources = gameData.creatureEntersDrawSourcesThisTurn.get(controllerId);
            if (sources == null) continue;
            for (Card sourceCard : new ArrayList<>(sources)) {
                gameData.queueMayAbility(sourceCard, controllerId,
                        new MayEffect(new DrawCardEffect(), "Draw a card?"));
                log.info("Game {} - {} triggers for creature entering (may draw)",
                        gameData.id, sourceCard.getName());
            }
        }
    }

    /**
     * The abilities a spell registered until end of turn that see a creature or an enchantment enter under their
     * controller's control.
     */
    public void checkAnyPermanentEntersTriggers(GameData gameData, UUID enteringControllerId, Card enteringCard) {
        if (!enteringPermanentHasType(gameData, enteringCard, CardType.CREATURE)
                && !enteringPermanentHasType(gameData, enteringCard, CardType.ENCHANTMENT)) {
            return;
        }
        for (TemporaryGlobalTriggeredAbility watcher : List.copyOf(gameData.temporaryGlobalTriggeredAbilities)) {
            if (!watcher.ability().hasTrigger(Triggers.enters(Triggers.CREATURE_OR_ENCHANTMENT,
                    ObjectRelation.YOU_CONTROL)) || !watcher.controllerId().equals(enteringControllerId)) {
                continue;
            }

            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    watcher.sourceCard(),
                    watcher.controllerId(),
                    watcher.sourceCard().getName() + "'s ability",
                    new ArrayList<>(List.of(ZoneChangeAbilityReader.strictEffect(watcher.ability()))));
            entry.setNonTargeting(true);
            gameData.enqueueTrigger(entry);
            gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
            log.info("Game {} - {} temporary creature-or-enchantment-enter trigger fires",
                    gameData.id, watcher.sourceCard().getName());
        }
    }

    /** The boons a noncreature permanent entering under {@code controllerId}'s control uses. */
    public void checkAllyPermanentEntersTriggers(GameData gameData, UUID controllerId, Card enteringCard) {
        Permanent enteringPermanent = findPermanentByCard(gameData, enteringCard);
        if (enteringPermanent != null && !gameQueryService.isCreature(gameData, enteringPermanent)) {
            collectAllyBoonTriggers(gameData, controllerId, List.of(enteringPermanent.getId()), false);
        }
    }

    /**
     * Puts a graveyard card's "whenever your commander enters" ability on the stack; the trigger's filter says whose
     * commander, whoever controls it.
     */
    private boolean queueGraveyardPermanentEntersTriggers(GameData gameData, Card card, UUID graveyardOwnerId,
                                                          List<CardEffect> effects, Permanent enteringPermanent) {
        return enqueueGraveyardEnterAbility(gameData, card, graveyardOwnerId, effects, enteringPermanent);
    }

    /** The abilities a spell registered until end of turn that see a creature enter under an opponent's control. */
    public void checkOpponentCreatureEntersTriggers(GameData gameData, UUID enteringCreatureControllerId, Card enteringCreature) {
        if (!enteringPermanentHasType(gameData, enteringCreature, CardType.CREATURE)) return;

        Permanent enteringPermanent = findPermanentByCard(gameData, enteringCreature);
        if (enteringPermanent == null) return;

        for (TemporaryGlobalTriggeredAbility watcher : List.copyOf(gameData.temporaryGlobalTriggeredAbilities)) {
            if (!watchesEntering(gameData, watcher, ZoneChangeWatch.ON_OPPONENT_CREATURE_ENTERS_BATTLEFIELD,
                    enteringPermanent)
                    || watcher.controllerId().equals(enteringCreatureControllerId)) {
                continue;
            }

            CardEffect resolvedEffect = ZoneChangeAbilityReader.strictEffect(watcher.ability());

            // "A copy of that creature" (Theoretical Duplication) is the stack entry's triggering permanent.
            enqueueTemporaryGlobalTrigger(gameData, watcher, resolvedEffect, enteringPermanent.getId(), 0,
                    enteringPermanent.getId());
        }
    }

    /** Handles "Whenever damage that would be dealt to you is prevented, ..." triggers: the damage prevented is "that much". */
    public void checkControllerDamagePreventedTriggers(GameData gameData, UUID damagedPlayerId, int amount) {
        if (amount <= 0) return;

        List<Permanent> damagedPlayerBattlefield = gameData.playerBattlefields.get(damagedPlayerId);
        if (damagedPlayerBattlefield == null) return;

        var ctx = new TriggerContext.DamageToControllerAmount(damagedPlayerId, amount, null, null);
        DamageWatch watch = DamageWatch.ON_CONTROLLER_DAMAGE_PREVENTED;
        for (Permanent perm : List.copyOf(damagedPlayerBattlefield)) {
            for (TriggeredAbility ability : printedWatchedAbilities(gameData, perm, watch)) {
                requireNothingToJudge(ability, watch);
                putEventAbility(gameData, ability, perm.getCard(), perm, damagedPlayerId,
                        ConditionContext.forPermanent(perm, damagedPlayerId).withEventValue(amount), ctx,
                        entry -> entry.setEventValue(amount));
            }
        }
    }

    /** Enqueues Judgment of Alexander's trigger for a creature-source damage event. */
    public void checkCommanderDamagePreventedTriggers(GameData gameData, UUID damagedPlayerId,
                                                       UUID preventedCreatureId, Card sourceCard) {
        if (damagedPlayerId == null || sourceCard == null) return;

        CardEffect effect = new CommanderCreaturesDealPowerDamageToPreventedCreatureEffect();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                damagedPlayerId,
                sourceCard.getName() + "'s triggered ability",
                List.of(effect),
                preventedCreatureId,
                (UUID) null);
        entry.setNonTargeting(true);
        gameData.enqueueTrigger(entry);
        gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
    }

    /** Fires Full Throttle's delayed untap trigger once for each registration at each combat. */
    public void checkUntapAttackedCreaturesEachCombatThisTurnTriggers(GameData gameData) {
        List<UUID> controllers = new ArrayList<>(gameData.orderedPlayerIds);
        int activePlayerIndex = controllers.indexOf(gameData.activePlayerId);
        if (activePlayerIndex > 0) {
            List<UUID> apnapControllers = new ArrayList<>(controllers.subList(activePlayerIndex, controllers.size()));
            apnapControllers.addAll(controllers.subList(0, activePlayerIndex));
            controllers = apnapControllers;
        }

        for (UUID controllerId : controllers) {
            List<Card> sources = gameData.untapAttackedCreaturesEachCombatThisTurnSources.get(controllerId);
            if (sources == null) continue;
            for (Card sourceCard : new ArrayList<>(sources)) {
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        sourceCard,
                        controllerId,
                        sourceCard.getName() + "'s delayed ability",
                        List.of(new UntapPermanentsEffect(TapUntapScope.ATTACKED_CREATURES)));
                entry.setNonTargeting(true);
                gameData.enqueueTrigger(entry);
                gameLogService.append(gameData, GameLog.cardThen(sourceCard,
                        "'s delayed ability triggers."));
                log.info("Game {} - {} delayed combat trigger queued", gameData.id, sourceCard.getName());
            }
        }
    }

    /**
     * The emblems' "whenever [a filter permanent] you control enters" abilities for {@code enteringCard}, which entered
     * under {@code enteringControllerId}'s control (Lukka, Wayward Bonder's, Chandra, Spark Hunter's, Koth, Fire of
     * Resistance's, Kiora, Master of the Depths' and Nissa, Vital Force's emblems). Abilities of emblems function in
     * the command zone (CR 114.4); an emblem isn't a permanent (CR 110.1), so an effect that makes a triggered ability
     * of a permanent trigger an additional time (Ancient Greenwarden, Panharmonicon) doesn't apply to it. Each ability
     * whose trigger's filter the permanent matches as it is now goes on the stack once as one object of its effects
     * (CR 603.3), the emblem's source card as its source and the entering permanent as its triggering permanent
     * ("it", read as the ability resolves); a targeted ability's target is chosen as it goes on the stack
     * (CR 603.3d), and a "you may" is asked as it resolves (CR 603.5). The reader judges no options, causes, origins
     * or turn; an ability with one fails loudly instead of triggering wrongly.
     */
    public void checkEmblemEntersTriggers(GameData gameData, UUID enteringControllerId, Card enteringCard) {
        if (gameData.emblems.isEmpty()) return;
        Permanent entering = findPermanentByCard(gameData, enteringCard);
        if (entering == null) return;
        for (Emblem emblem : List.copyOf(gameData.emblems)) {
            for (TriggeredAbility ability : emblem.triggeredAbilities()) {
                if (emblemSeesEntering(gameData, emblem, ability, enteringControllerId, entering)) {
                    putEmblemAbilityOnStack(gameData, emblem, ability, entering, enteringControllerId, "entering");
                }
            }
        }
    }

    /**
     * The emblems' "whenever a creature attacks you" abilities ({@code Triggers.creatureAttacksYou()}; Garruk, Apex
     * Predator's emblem) for {@code attacker}, which {@code attackerControllerId} declared as an attacker attacking
     * {@code attackedTargetId}. Abilities of emblems function in the command zone (CR 114.4); such an ability triggers
     * only when the creature attacks the emblem's controller, not a planeswalker or battle (CR 508.3a). Each one goes on
     * the stack once as one object of its effects (CR 603.3), the emblem's source card as its source and the attacking
     * creature as its triggering permanent ("it", read as the ability resolves). An emblem isn't a permanent (CR 110.1),
     * so nothing makes its ability trigger additional times. The reader judges no filter or options; an ability with
     * one fails loudly instead of triggering wrongly.
     */
    public void checkEmblemAttackTriggers(GameData gameData, Permanent attacker, UUID attackedTargetId,
                                          UUID attackerControllerId) {
        if (gameData.emblems.isEmpty() || attacker == null || attackedTargetId == null) return;
        for (Emblem emblem : List.copyOf(gameData.emblems)) {
            if (!emblem.controllerId().equals(attackedTargetId)) continue;
            for (TriggeredAbility ability : emblem.triggeredAbilities()) {
                boolean attacksYou = false;
                for (TriggerCondition condition : ability.conditions()) {
                    if (!(condition.trigger() instanceof CombatTrigger trigger)
                            || trigger.event() != CombatTrigger.CombatEvent.ATTACKS_YOU) {
                        continue;
                    }
                    if (trigger.relation() != ObjectRelation.ANY || trigger.filter() != null
                            || !ability.options().equals(TriggerOptions.DEFAULT)) {
                        throw new IllegalStateException(emblemName(emblem) + " has an attack ability its reader "
                                + "doesn't judge: " + ability);
                    }
                    attacksYou = true;
                }
                if (attacksYou) {
                    putEmblemAbilityOnStack(gameData, emblem, ability, attacker, attackerControllerId, "attacking");
                }
            }
        }
    }

    /**
     * Whether {@code ability} of {@code emblem} triggers on {@code entering} entering under
     * {@code enteringControllerId}'s control.
     */
    private boolean emblemSeesEntering(GameData gameData, Emblem emblem, TriggeredAbility ability,
                                       UUID enteringControllerId, Permanent entering) {
        for (TriggerCondition condition : ability.conditions()) {
            if (!(condition.trigger() instanceof ZoneChangeTrigger trigger) || !trigger.entersBattlefield()) {
                continue;
            }
            if (!trigger.from().isEmpty() || !trigger.excludedTo().isEmpty() || !trigger.causes().isEmpty()
                    || trigger.causedBy() != com.github.laxika.magicalvibes.model.trigger.PlayerRelation.ANY
                    || trigger.duringYourTurn() || !ability.options().equals(TriggerOptions.DEFAULT)) {
                throw new IllegalStateException(emblemName(emblem) + " has an enters ability its reader doesn't "
                        + "judge: " + ability);
            }
            if (controlledByRelatedPlayer(trigger.relation(), emblem, enteringControllerId)
                    && (trigger.filter() == null || predicateEvaluationService.matchesPermanentPredicate(entering,
                    trigger.filter(), emblemFilterContext(gameData, emblem)))) {
                return true;
            }
        }
        return false;
    }

    /** Whether a permanent {@code controllerId} controls relates to {@code emblem} as {@code relation} says. */
    private static boolean controlledByRelatedPlayer(ObjectRelation relation, Emblem emblem, UUID controllerId) {
        return switch (relation) {
            case YOU_CONTROL, ANOTHER_YOU_CONTROL -> emblem.controllerId().equals(controllerId);
            case OPPONENT_CONTROLS -> !emblem.controllerId().equals(controllerId);
            case ANY, ANOTHER -> true;
            default -> throw new IllegalStateException(emblemName(emblem) + " has a trigger relation its reader "
                    + "doesn't judge: " + relation);
        };
    }

    private static FilterContext emblemFilterContext(GameData gameData, Emblem emblem) {
        FilterContext context = FilterContext.of(gameData).withSourceControllerId(emblem.controllerId());
        return emblem.sourceCard() == null ? context : context.withSourceCardId(emblem.sourceCard().getId());
    }

    private static String emblemName(Emblem emblem) {
        return (emblem.sourceCard() != null ? emblem.sourceCard().getName() : "Emblem") + "'s emblem";
    }

    /**
     * Puts {@code ability} of {@code emblem} on the stack for the event {@code triggering} was about, as its triggering
     * permanent: a targeted ability through its target choice, which keeps the triggering permanent, any other one as
     * a non-targeting entry. An emblem without a source card (one a test built) is described as "Emblem's emblem",
     * with the triggering permanent's card as its source.
     */
    private void putEmblemAbilityOnStack(GameData gameData, Emblem emblem, TriggeredAbility ability,
                                         Permanent triggering, UUID triggeringControllerId, String event) {
        Card source = emblem.sourceCard() != null ? emblem.sourceCard() : triggering.getCard();
        String description = emblemName(emblem);
        List<CardEffect> effects = ability.effects().stream().map(EffectRegistration::effect).toList();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                source,
                emblem.controllerId(),
                description,
                new ArrayList<>(effects));
        entry.setTriggeringPermanentId(triggering.getId());
        entry.setTriggeringPermanentControllerId(triggeringControllerId);
        entry.setTriggeringCardId(triggering.getCard().getId());
        entry.setNonTargeting(true);
        if (effects.stream().anyMatch(effect -> effect.targetSpec() != TargetSpec.NONE)) {
            queueTargetChoice(gameData, entry, new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                    source, emblem.controllerId(), new ArrayList<>(effects), false, null, 0, null, null, false,
                    triggering.getId(), null, emblem.controllerId(), (PlanarObject) null));
        } else {
            gameData.enqueueTrigger(entry);
        }
        gameLogService.append(gameData, GameLog.text(description + " triggers for " + triggering.getCard().getName()
                + " " + event + "."));
        log.info("Game {} - {} triggers for {} {}", gameData.id, description, triggering.getCard().getName(), event);
    }

    /**
     * The emblems' "whenever an opponent is dealt combat damage by one or more creatures you control" abilities
     * ({@code Triggers.creaturesYouControlDealCombatDamageToOpponent()}; Lolth, Spider Queen's emblem) for the combat
     * damage {@code controllerId}'s creatures dealt {@code damagedPlayerId} in one combat damage step. Abilities of
     * emblems function in the command zone (CR 114.4). Each ability whose intervening "if" holds as it triggers (CR
     * 603.4: "if that player lost less than 8 life this turn", judged with that player as the context's target) goes
     * on the stack once as one object of its effects (CR 603.3), the emblem's source card as its source, "that player"
     * its non-targeting {@code targetId} and triggering player, the combat damage dealt to them its event value; the
     * intervening "if" wraps each effect, so it is checked again as the ability resolves. Nothing makes an emblem's
     * ability trigger additional times; an ability takes no limit, multiplicity or filter here.
     */
    public void checkEmblemCombatDamageTriggers(GameData gameData, UUID controllerId,
                                                UUID damagedPlayerId,
                                                Map<Permanent, Integer> combatDamageDealtToPlayer) {
        if (controllerId == null || damagedPlayerId == null || controllerId.equals(damagedPlayerId)
                || combatDamageDealtToPlayer == null
                || combatDamageDealtToPlayer.values().stream().noneMatch(damage -> damage > 0)) {
            return;
        }

        Card fallbackSource = combatDamageDealtToPlayer.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .map(entry -> entry.getKey().getCard())
                .findFirst()
                .orElse(null);
        int damage = combatDamageDealtToPlayer.values().stream().filter(amount -> amount > 0)
                .mapToInt(Integer::intValue).sum();
        Trigger watched = Triggers.creaturesYouControlDealCombatDamageToOpponent();

        for (Emblem emblem : gameData.emblems) {
            if (!controllerId.equals(emblem.controllerId())) continue;

            for (TriggeredAbility ability : emblem.triggeredAbilities()) {
                if (ability.conditions().stream().noneMatch(condition -> condition.trigger().equals(watched))) {
                    continue;
                }
                TriggerOptions options = ability.options();
                if (options.limit() != TriggerLimit.NONE || options.multiplicity() != TriggerMultiplicity.EACH) {
                    throw new IllegalStateException("The emblem combat damage reader judges no limit or "
                            + "multiplicity: " + ability);
                }
                Card source = emblem.sourceCard() != null ? emblem.sourceCard() : fallbackSource;
                if (source == null) continue;
                if (!conditionEvaluationService.triggerConditionsMet(gameData, options,
                        ConditionContext.forCard(source, controllerId).withTargetId(damagedPlayerId), damage)) {
                    continue;
                }

                String description = (emblem.sourceCard() != null ? emblem.sourceCard().getName() : "Emblem")
                        + "'s emblem";
                Condition interveningIf = options.interveningIf();
                List<CardEffect> effects = ability.effects().stream()
                        .map(EffectRegistration::effect)
                        .map(effect -> interveningIf == null ? effect : (CardEffect) new ConditionalEffect(interveningIf, effect))
                        .toList();
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        source,
                        controllerId,
                        description,
                        new ArrayList<>(effects),
                        damagedPlayerId,
                        (UUID) null);
                entry.setNonTargeting(true);
                entry.setTriggeringPlayerId(damagedPlayerId);
                entry.setEventValue(damage);
                gameData.enqueueTrigger(entry);
                gameLogService.append(gameData, GameLog.text(
                        description + " triggers for combat damage dealt to an opponent."));
                log.info("Game {} - {} triggers for combat damage dealt to an opponent",
                        gameData.id, description);
            }
        }
    }

    /**
     * Fires token-entry abilities for a token-creation batch. Ordinary abilities use one trigger
     * for the batch, while abilities that branch on their resolution count receive one trigger for
     * each token in the batch.
     */
    public void checkAllyTokenEntersTriggers(GameData gameData, UUID controllerId, int count) {
        checkAllyTokenEntersTriggers(gameData, controllerId, count, List.of());
    }

    private void collectTemporaryControllerSpellCastTriggers(GameData gameData, Card spellCard,
                                                               UUID castingPlayerId, Zone castZone,
                                                               UUID exiledSourcePermanentId) {
        for (TemporaryGlobalTriggeredAbility watcher : List.copyOf(gameData.temporaryGlobalTriggeredAbilities)) {
            if (!watcher.controllerId().equals(castingPlayerId)) {
                continue;
            }
            // A cast trigger with a spell filter ("until end of turn, whenever you cast an instant or sorcery spell")
            // is judged and put on the stack by the generic cast collector, which gives it the spell as "that spell";
            // its "a card exiled with this" (Apple of Eden, Isu Relic) names the permanent that registered it.
            SpellFilter filter = watchedSpellFilter(watcher.ability(), SpellAbilityWatch.ON_CONTROLLER_CASTS_SPELL,
                    true);
            if (filter != null) {
                requireNoLimit(watcher.ability(), watcher.sourceCard());
                spellCastTriggerCollectorService.collectCastAbility(
                        registeredMatch(gameData, watcher),
                        CastAbility.of(watcher.ability(), filter),
                        new TriggerContext.SpellCast(spellCard, castingPlayerId, castZone, exiledSourcePermanentId),
                        false);
                continue;
            }
            if (!watcher.ability().hasTrigger(SpellAbilityWatch.ON_CONTROLLER_CASTS_SPELL.trigger())) {
                continue;
            }

            StackEntry triggerEntry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY, watcher.sourceCard(), watcher.controllerId(),
                    watcher.sourceCard().getName() + "'s ability",
                    new ArrayList<>(List.of(watcher.effect())));
            triggerEntry.setNonTargeting(true);
            if (watcher.targetFilter() != null) {
                queueTargetChoice(gameData, triggerEntry, new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                        watcher.sourceCard(), watcher.controllerId(), List.of(watcher.effect()),
                        false, watcher.targetFilter(), 0, null, true));
            } else {
                gameData.stack.add(triggerEntry);
            }
            gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
        }
    }

    /**
     * The abilities a resolving ability registered that trigger whenever a player casts a spell: each goes on the stack
     * as written, recording the spell as its triggering spell and the caster as its triggering player and non-targeting
     * "that player", whatever the effects are. "Whenever they cast a spell" (Nuka-Nuke Launcher) is the filter
     * {@link StackEntryControlledByWatchedPlayerPredicate}, judged against the player the registration recorded; this
     * reader judges no other spell filter.
     */
    private void collectTemporaryGlobalSpellCastTriggers(GameData gameData, Card spellCard, UUID castingPlayerId) {
        for (TemporaryGlobalTriggeredAbility watcher
                : List.copyOf(gameData.temporaryGlobalTriggeredAbilities)) {
            SpellFilter filter = watchedSpellFilter(watcher.ability(), SpellAbilityWatch.ON_ANY_PLAYER_CASTS_SPELL,
                    true);
            if (filter == null && !watcher.ability().hasTrigger(SpellAbilityWatch.ON_ANY_PLAYER_CASTS_SPELL.trigger())) {
                continue;
            }
            if (filter != null) {
                if (!filter.equals(SpellFilter.ANY.withStackEntry(new StackEntryControlledByWatchedPlayerPredicate()))) {
                    throw new IllegalStateException("The registered any-player cast reader judges only the watched "
                            + "player: " + watcher.ability());
                }
                if (!castingPlayerId.equals(watcher.watchedPlayerId())) {
                    continue;
                }
            }
            requireNoLimit(watcher.ability(), watcher.sourceCard());

            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    watcher.sourceCard(),
                    watcher.controllerId(),
                    watcher.sourceCard().getName() + "'s ability",
                    new ArrayList<>(watcher.ability().effects().stream().map(EffectRegistration::effect).toList()));
            entry.setTargetId(castingPlayerId);
            entry.setTriggeringPlayerId(castingPlayerId);
            entry.setTriggeringCardId(spellCard.getId());
            StackEntry spellEntry = findStackEntryByCardId(gameData, spellCard.getId());
            if (spellEntry != null) {
                entry.setTriggeringSpell(spellEntry);
            }
            entry.setNonTargeting(true);
            gameData.enqueueTrigger(entry);
            gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
            log.info("Game {} - {} temporary global spell-cast trigger fires",
                    gameData.id, watcher.sourceCard().getName());
        }
    }

    public void checkAllyTokenEntersTriggers(GameData gameData, UUID controllerId,
                                              List<UUID> permanentIds) {
        checkAllyTokenEntersTriggers(gameData, controllerId, permanentIds.size(), permanentIds);
    }

    /** The boons the tokens entering under {@code controllerId}'s control use. */
    private void checkAllyTokenEntersTriggers(GameData gameData, UUID controllerId, int count,
                                               List<UUID> permanentIds) {
        if (count <= 0) return;
        collectAllyBoonTriggers(gameData, controllerId, permanentIds, true);
    }

    private List<UUID> tokenIdAt(List<UUID> permanentIds, int index) {
        return index < permanentIds.size() ? List.of(permanentIds.get(index)) : List.of();
    }

    /**
     * "Whenever an Aura becomes attached to this creature" (ON_AURA_ATTACHED_TO_SELF, e.g. Brood
     * Keeper). Fires on the newly enchanted permanent for its own controller, regardless of who
     * controls the Aura. Call this after the Aura's {@code attachedTo} has been set.
     */
    public void checkAuraAttachedTriggers(GameData gameData, Card auraCard, UUID enchantedPermanentId) {
        if (auraCard == null || enchantedPermanentId == null) return;
        if (!auraCard.getSubtypes().contains(CardSubtype.AURA)) return;

        Permanent enchanted = gameQueryService.findPermanentById(gameData, enchantedPermanentId);
        if (enchanted == null) return;
        UUID controllerId = gameQueryService.findPermanentController(gameData, enchantedPermanentId);
        if (controllerId == null) return;

        // Each ability is one object of all its effects (CR 603.3).
        for (List<CardEffect> effects : MiscAbilityReader.printedEffectsByAbility(enchanted.getCard(),
                MiscWatch.ON_AURA_ATTACHED_TO_SELF)) {
            gameData.enqueueTrigger(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    enchanted.getCard(),
                    controllerId,
                    enchanted.getCard().getName() + "'s ability",
                    new ArrayList<>(effects),
                    null,
                    enchanted.getId()
            ));
            gameLogService.append(gameData, GameLog.abilityTriggers(enchanted.getCard()));
            log.info("Game {} - {} triggers on {} becoming attached to it",
                    gameData.id, enchanted.getCard().getName(), auraCard.getName());
        }
    }

    public void checkAuraAttachedTriggers(GameData gameData, Permanent aura, UUID enchantedPermanentId) {
        if (aura == null) return;
        Card auraCard = aura.getCard();
        checkAuraAttachedTriggers(gameData, auraCard, enchantedPermanentId);
        Permanent enchanted = gameQueryService.findPermanentById(gameData, enchantedPermanentId);
        if (enchanted == null) return;
        UUID enchantedControllerId = gameQueryService.findPermanentController(gameData, enchantedPermanentId);
        UUID auraControllerId = gameQueryService.findPermanentController(gameData, aura.getId());
        if (auraControllerId != null && auraControllerId.equals(enchantedControllerId)
                && gameQueryService.isCreature(gameData, enchanted)) {
            List<Permanent> alliedBattlefield = gameData.playerBattlefields.get(auraControllerId);
            if (alliedBattlefield != null) {
                for (Permanent watcher : List.copyOf(alliedBattlefield)) {
                    if (gameQueryService.hasLostAllAbilities(gameData, watcher)) continue;
                    for (List<CardEffect> effects : MiscAbilityReader.printedEffectsByAbility(watcher.getCard(),
                            MiscWatch.ON_ALLY_AURA_ATTACHED_TO_ALLY_CREATURE)) {
                        StackEntry trigger = new StackEntry(StackEntryType.TRIGGERED_ABILITY,
                                watcher.getCard(), auraControllerId,
                                watcher.getCard().getName() + "'s ability",
                                new ArrayList<>(effects), null, watcher.getId());
                        trigger.setNonTargeting(true);
                        gameData.enqueueTrigger(trigger);
                        gameLogService.append(gameData, GameLog.abilityTriggers(watcher.getCard()));
                    }
                }
            }
        }
        if (auraControllerId == null || auraControllerId.equals(enchantedControllerId)
                || gameQueryService.isLand(gameData, enchanted)
                || enchanted.getCard().getManaValue() > auraCard.getManaValue()) {
            return;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(auraControllerId);
        if (battlefield == null) return;
        for (Permanent watcher : List.copyOf(battlefield)) {
            if (gameQueryService.hasLostAllAbilities(gameData, watcher)) continue;
            for (List<CardEffect> effects : MiscAbilityReader.printedEffectsByAbility(watcher.getCard(),
                    MiscWatch.ON_ALLY_AURA_ATTACHED_TO_OPPONENT_NONLAND_PERMANENT)) {
                StackEntry trigger = new StackEntry(StackEntryType.TRIGGERED_ABILITY,
                        watcher.getCard(), auraControllerId, watcher.getCard().getName() + "'s ability",
                        new ArrayList<>(effects), enchanted.getId(), aura.getId());
                trigger.setNonTargeting(true);
                gameData.enqueueTrigger(trigger);
                gameLogService.append(gameData, GameLog.abilityTriggers(watcher.getCard()));
            }
        }
    }

    /** Fires an Equipment's targeted "becomes attached" abilities after a new attachment. */
    public void checkEquipmentAttachedTriggers(GameData gameData, Permanent equipment, UUID oldAttachedTo) {
        if (equipment == null || equipment.getAttachedTo() == null
                || equipment.getAttachedTo().equals(oldAttachedTo)
                || !equipment.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)) {
            return;
        }

        Permanent equipped = gameQueryService.findPermanentById(gameData, equipment.getAttachedTo());
        if (equipped == null || !gameQueryService.isCreature(gameData, equipped)) return;

        UUID controllerId = gameQueryService.findPermanentController(gameData, equipment.getId());
        if (controllerId == null) return;

        MiscWatch watch = MiscWatch.ON_EQUIPMENT_ATTACHED;
        for (TriggeredAbility ability : MiscAbilityReader.printed(equipment.getCard(), watch)) {
            List<MiscAbilityReader.MiscEffect> reads = MiscAbilityReader.read(ability, watch.trigger());
            // "This ability triggers only once each turn" (Inchblade Companion) is judged and marked by the ability.
            if (reads.getFirst().limitReached(gameData, equipment.getId())) continue;
            boolean triggered = false;
            // The ability's effects but its as-attaches choices are one object on the stack (CR 603.3).
            List<CardEffect> stackEffects = new ArrayList<>();
            for (MiscAbilityReader.MiscEffect read : reads) {
                CardEffect effect = read.effect();

                if (effect instanceof ChooseColorForSourceEffect) {
                    // "As this Equipment becomes attached to a creature, choose a color" is not a triggered
                    // ability, so the choice is made during the attachment rather than put on the stack.
                    playerInputService.beginColorChoice(gameData, controllerId, equipment.getId(), null);
                    continue;
                }

                if (effect instanceof SequenceEffect sequence && sequence.steps().size() == 2
                        && sequence.steps().get(0) instanceof ChooseCardNameAtResolutionEffect nameChoice
                        && sequence.steps().get(1) instanceof ChooseSubtypeForSourceEffect subtypeChoice) {
                    // "As this Equipment becomes attached to a creature, choose a creature card name and a creature
                    // type" is not a triggered ability, so both choices are made during the attachment.
                    chooseCardNameHandler.begin(gameData, equipment.getCard(), controllerId, equipment.getId(),
                            nameChoice, subtypeChoice);
                    continue;
                }

                if (effect instanceof AttachedCreatureBecomesCopyOfExiledCreatureEffect copy && copy.asAttaches()) {
                    // "As this Equipment becomes attached to a creature, choose an exiled creature card ..." is not a
                    // triggered ability, so the copy is made as the equipment attaches, without another stack resolution.
                    StackEntry attachment = new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            equipment.getCard(),
                            controllerId,
                            equipment.getCard().getName() + "'s ability",
                            new ArrayList<>(List.of(effect)),
                            null,
                            equipment.getId());
                    attachment.setTriggeringPermanentId(equipped.getId());
                    attachedCreatureCopyHandler.resolve(gameData, attachment, effect);
                    continue;
                }

                stackEffects.add(effect);
            }
            if (!stackEffects.isEmpty()) {
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        equipment.getCard(),
                        controllerId,
                        equipment.getCard().getName() + "'s ability",
                        stackEffects,
                        null,
                        equipment.getId());
                entry.setTriggeringPermanentId(equipped.getId());
                entry.setSourcePermanentSnapshot(new Permanent(equipment));
                gameData.handOffTriggerRecord(entry);
                if (stackEffects.stream().anyMatch(effect -> effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                        || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER))) {
                    gameData.queueInteraction(new PermanentChoiceContext.ETBTokenTargetTrigger(
                            equipment.getCard(), controllerId, stackEffects,
                            equipment.getId(), equipment.getCard().getTargetFilter(), equipped.getId()));
                } else {
                    gameData.enqueueTrigger(entry);
                }
                triggered = true;
                gameLogService.append(gameData, GameLog.abilityTriggers(equipment.getCard()));
                log.info("Game {} - {} triggers when attached to {}", gameData.id,
                        equipment.getCard().getName(), equipped.getCard().getName());
            }
            if (triggered) {
                reads.getFirst().markLimit(gameData, equipment.getId());
            }
        }

        if (gameData.hasPendingInteraction(PermanentChoiceContext.ETBTokenTargetTrigger.class)
                && !gameData.interaction.isAwaitingInput()) {
            etbTokenTargetService.processNextETBTokenTargetTrigger(gameData);
        }
    }

    private boolean hasWardAbility(GameData gameData, Permanent source) {
        if (source.isFaceDown()
                && (source.isCloaked() || source.isFaceDownWard())
                && gameQueryService.hasKeyword(gameData, source, Keyword.WARD)) {
            return true;
        }
        if (gameQueryService.isCreature(gameData, source)
                && (gameQueryService.hasKeyword(gameData, source, Keyword.WARD)
                || hasGrantedCounterUnlessTrigger(gameData, source))) {
            return true;
        }
        if (source.isAttached() && source.getAttachedTo() != null) {
            Permanent attachedTo = gameQueryService.findPermanentById(gameData, source.getAttachedTo());
            return attachedTo != null && (attachedTo.isCloaked()
                    || (gameQueryService.isCreature(gameData, attachedTo)
                    && gameQueryService.hasKeyword(gameData, attachedTo, Keyword.WARD)));
        }
        return false;
    }

    /**
     * Collects abilities that trigger when the controller fully unlocks a Room. The caller must
     * invoke this only after the Room has actually transitioned from one unlocked door to both.
     */
    public void checkAllyRoomFullyUnlockedTriggers(GameData gameData, UUID controllerId, Permanent room) {
        if (controllerId == null || room == null
                || !room.getCard().hasType(CardType.ENCHANTMENT)
                || !room.getCard().getSubtypes().contains(CardSubtype.ROOM)) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null || battlefield.stream().noneMatch(
                permanent -> permanent.getId().equals(room.getId()))) {
            return;
        }

        TriggerContext context = new TriggerContext.RoomFullyUnlocked(room.getId());
        for (Permanent permanent : List.copyOf(battlefield)) {
            dispatchWatch(gameData, permanent, controllerId,
                    MiscWatch.ON_ALLY_ROOM_FULLY_UNLOCKED, context);
        }

        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        if (graveyard != null) {
            for (Card card : new ArrayList<>(graveyard)) {
                queueGraveyardEerieTriggers(gameData, controllerId, card);
            }
        }
    }

    /**
     * Collects abilities that trigger when a specified door of a Room becomes unlocked. This is
     * called for both the door chosen as a Room enters and a later door-unlocking action.
     */
    public void checkSelfRoomDoorUnlockedTriggers(GameData gameData, UUID controllerId,
                                                   Permanent room, int doorIndex) {
        if (controllerId == null || room == null || doorIndex < 0 || doorIndex > 1
                || !room.getCard().hasType(CardType.ENCHANTMENT)
                || !room.getCard().getSubtypes().contains(CardSubtype.ROOM)) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null || battlefield.stream().noneMatch(
                permanent -> permanent.getId().equals(room.getId()))) {
            return;
        }

        dispatchWatch(gameData, room, controllerId, MiscWatch.ON_SELF_ROOM_DOOR_UNLOCKED,
                new TriggerContext.RoomDoorUnlocked(room.getId(), doorIndex));
    }

    /**
     * "Whenever you play a land" (ON_CONTROLLER_PLAYS_LAND, e.g. Search the City, Juju Bubble) and
     * its opponent-side mirror ON_OPPONENT_PLAYS_LAND (Dirtcowl Wurm). Called from the land-play
     * sites only, so a land put onto the battlefield by an effect does not trigger either.
     * Dispatches through the collector registry so name-match gates (Search the City) and bare
     * effects (Juju Bubble) both resolve correctly.
     */
    public void checkControllerPlaysLandTriggers(GameData gameData, UUID playingPlayerId, Card landCard) {
        checkControllerPlaysLandTriggers(gameData, playingPlayerId, landCard, false);
    }

    public void checkControllerPlaysLandTriggers(GameData gameData, UUID playingPlayerId, Card landCard,
                                                 boolean fromExile) {
        checkControllerPlaysLandTriggers(gameData, playingPlayerId, landCard,
                fromExile ? Zone.EXILE : Zone.HAND);
    }

    public void checkControllerPlaysLandTriggers(GameData gameData, UUID playingPlayerId, Card landCard,
                                                 Zone playZone) {
        checkControllerPlaysLandTriggers(gameData, playingPlayerId, landCard, playZone, null);
    }

    public void checkControllerPlaysLandTriggers(GameData gameData, UUID playingPlayerId, Card landCard,
                                                 Zone playZone, UUID exiledSourcePermanentId) {
        var ctx = new TriggerContext.LandPlayed(playingPlayerId, landCard, playZone,
                exiledSourcePermanentId);
        if (playZone != Zone.HAND) {
            gameData.playersWhoPlayedOrCastFromOutsideHandThisTurn.add(playingPlayerId);
        }
        gameData.forEachPermanent((playerId, perm) -> {
            if (playerId.equals(playingPlayerId)) {
                dispatchWatch(gameData, perm, playerId, SpellAbilityWatch.ON_CONTROLLER_PLAYS_LAND, ctx);
                if (!perm.getOriginalCard().getId().equals(landCard.getId())) {
                    dispatchWatch(gameData, perm, playerId, SpellAbilityWatch.ON_CONTROLLER_PLAYS_ANOTHER_LAND, ctx);
                }
            } else {
                dispatchWatch(gameData, perm, playerId, SpellAbilityWatch.ON_OPPONENT_PLAYS_LAND, ctx);
            }
        });
        if (gameData.planechase != null && gameData.planechase.controllerId != null) {
            SpellAbilityWatch watch = gameData.planechase.controllerId.equals(playingPlayerId)
                    ? SpellAbilityWatch.ON_CONTROLLER_PLAYS_LAND : SpellAbilityWatch.ON_OPPONENT_PLAYS_LAND;
            for (PlanarObject object : List.copyOf(gameData.planechase.faceUp)) {
                dispatchCardSpellAbilities(gameData, object.getCard(), gameData.planechase.controllerId, watch, ctx,
                        object);
            }
        }
        collectTemporaryControllerLandPlayTriggers(gameData, playingPlayerId, landCard, playZone,
                exiledSourcePermanentId);
    }

    /**
     * The abilities a resolving ability registered that trigger whenever their controller plays a land, read as the
     * land-play collector reads a printed one ({@code PlayedCardNameTriggerCollectorService.collectLandPlayAbility}):
     * "a land exiled with this" (Apple of Eden, Isu Relic) names the permanent that registered the ability.
     */
    private void collectTemporaryControllerLandPlayTriggers(GameData gameData, UUID playingPlayerId, Card landCard,
                                                            Zone playZone, UUID exiledSourcePermanentId) {
        for (TemporaryGlobalTriggeredAbility watcher : List.copyOf(gameData.temporaryGlobalTriggeredAbilities)) {
            if (!watcher.controllerId().equals(playingPlayerId)) {
                continue;
            }
            LandPlayTrigger landPlay = watchedLandPlayTrigger(watcher.ability(),
                    SpellAbilityWatch.ON_CONTROLLER_PLAYS_LAND, true);
            if (landPlay == null) {
                if (!watcher.ability().hasTrigger(SpellAbilityWatch.ON_CONTROLLER_PLAYS_LAND.trigger())) {
                    continue;
                }
                landPlay = (LandPlayTrigger) SpellAbilityWatch.ON_CONTROLLER_PLAYS_LAND.trigger();
            }
            requireNoLimit(watcher.ability(), watcher.sourceCard());
            playedCardNameTriggerCollectorService.collectLandPlayAbility(registeredMatch(gameData, watcher),
                    watcher.ability(), landPlay,
                    new TriggerContext.LandPlayed(playingPlayerId, landCard, playZone, exiledSourcePermanentId));
        }
    }

    /**
     * The match context of an ability a resolving ability registered: no permanent of its own, its source card the
     * registering card, and the permanent that registered it as its "this" (CR 603.7e).
     */
    private static TriggerMatchContext registeredMatch(GameData gameData, TemporaryGlobalTriggeredAbility watcher) {
        return new TriggerMatchContext(gameData, null, watcher.controllerId(), List.of(watcher.effect()),
                watcher.sourceCard(), null, null, false, watcher.sourcePermanentId());
    }

    /** The exiled voyages that see a land enter under its controller's control. */
    public void checkAllyLandEntersTriggers(GameData gameData, UUID landControllerId, Card enteringLand) {
        collectExiledVoyageTriggers(gameData, landControllerId);
    }

    /** Collects land-entry triggers from face-up planes, which are not battlefield permanents. */
    public void checkPlanarLandEntersTriggers(GameData gameData, UUID landControllerId, Card enteringLand) {
        if (gameData.planechase == null || gameData.planechase.controllerId == null) return;

        UUID planarControllerId = gameData.planechase.controllerId;
        ZoneChangeWatch slot = planarControllerId.equals(landControllerId)
                ? ZoneChangeWatch.ON_ALLY_LAND_ENTERS_BATTLEFIELD
                : ZoneChangeWatch.ON_OPPONENT_LAND_ENTERS_BATTLEFIELD;

        for (PlanarObject object : List.copyOf(gameData.planechase.faceUp)) {
            // Each ability of the plane is one object of all its effects (CR 603.3).
            for (List<CardEffect> effects : byAbility(slot.abilitiesOf(object.getCard()),
                    ZoneChangeAbilityReader::strictEffects)) {
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        object.getCard(),
                        planarControllerId,
                        object.getCard().getName() + "'s ability",
                        new ArrayList<>(effects));
                entry.setTargetId(landControllerId);
                entry.setTriggeringPermanentControllerId(landControllerId);
                Permanent landPermanent = findPermanentByCard(gameData, enteringLand);
                if (landPermanent != null) entry.setTriggeringPermanentId(landPermanent.getId());
                entry.setSourcePlanarObject(object.copy());
                entry.setNonTargeting(true);
                gameData.enqueueTrigger(entry);
                gameLogService.append(gameData, GameLog.abilityTriggers(object.getCard()));
            }
        }
    }

    /** Collects once-per-combat attack triggers from face-up planes. */
    public void checkPlanarAllyCreaturesAttackTriggers(GameData gameData, UUID attackingPlayerId,
                                                       int attackerCount) {
        if (attackerCount <= 0 || gameData.planechase == null
                || gameData.planechase.controllerId == null
                || !gameData.planechase.controllerId.equals(attackingPlayerId)) {
            return;
        }

        UUID planarControllerId = gameData.planechase.controllerId;
        for (PlanarObject object : List.copyOf(gameData.planechase.faceUp)) {
            ConditionContext planarContext = ConditionContext.forCard(object.getCard(), planarControllerId)
                    .withXValue(attackerCount);
            // Each ability's event condition ("whenever you attack with exactly two creatures", Valor's Reach;
            // CR 603.2) and intervening "if" (CR 603.4), with the number of attackers. Each ability is one object of
            // all its effects (CR 603.3), its target chosen as it goes on the stack when it has one (CR 603.3d).
            Trigger youAttack = CombatWatch.ON_ALLY_CREATURES_ATTACK.trigger();
            for (List<CombatAbilityReader.CombatEffect> reads : CombatAbilityReader.readByAbility(
                    conditionEvaluationService.judge(gameData, planarContext).filter(
                            WatchedAbilities.printed(object.getCard(), CombatWatch.ON_ALLY_CREATURES_ATTACK)),
                    youAttack)) {
                CombatAbilityReader.CombatEffect read = reads.getFirst();
                CombatAbilityReader.requireNoLimitOrMultiplicity(read, youAttack);
                if (read.filter() != null) {
                    throw new IllegalStateException("The planar collector of " + youAttack + " judges no filter: "
                            + read.ability());
                }
                List<CardEffect> effects = reads.stream()
                        .map(CombatAbilityReader.CombatEffect::effectAsJudged).toList();

                CardEffect targeting = effects.stream()
                        .filter(effect -> effect.targetSpec().declaredTarget() != null).findFirst().orElse(null);
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        object.getCard(),
                        planarControllerId,
                        object.getCard().getName() + "'s ability",
                        new ArrayList<>(effects));
                entry.setXValue(attackerCount);
                entry.setSourcePlanarObject(object.copy());
                entry.setNonTargeting(true);
                // The creatures that attacked are the entry's attacking creatures whatever the effects are ("those
                // creatures gain double strike", Valor's Reach), fixed as the ability triggers.
                entry.setAttackingPermanentSnapshots(gameData.playerBattlefields
                        .getOrDefault(attackingPlayerId, List.of())
                        .stream()
                        .filter(Permanent::isAttacking)
                        .toList());
                if (targeting != null) {
                    int targetGroup = object.getCard().getEffectTargetIndex(targeting);
                    TargetFilter filter = targetGroup >= 0
                            ? object.getCard().getTargetGroups().get(targetGroup).getFilter() : null;
                    queueTargetChoice(gameData, entry, new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                            object.getCard(), planarControllerId, new ArrayList<>(effects),
                            !targeting.targetSpec().admits(TargetPredicate.Kind.PERMANENT), filter,
                            attackerCount, null, null, false, null, null, planarControllerId, object.copy()));
                    continue;
                }
                gameData.enqueueTrigger(entry);
                gameLogService.append(gameData, GameLog.abilityTriggers(object.getCard()));
            }
        }
    }

    /** Collects per-creature attack triggers from face-up planes. */
    public void checkPlanarAllyCreatureAttackTriggers(GameData gameData, Permanent attacker) {
        if (gameData.planechase == null || gameData.planechase.controllerId == null
                || attacker == null || attacker.getAttackTarget() == null) {
            return;
        }

        UUID attackerControllerId = gameQueryService.findPermanentController(gameData, attacker.getId());
        UUID planarControllerId = gameData.planechase.controllerId;
        for (PlanarObject object : List.copyOf(gameData.planechase.faceUp)) {
            List<List<CombatAbilityReader.CombatEffect>> abilities = new ArrayList<>(
                    CombatAbilityReader.readByAbility(
                            WatchedAbilities.printed(object.getCard(), CombatWatch.ON_ANY_CREATURE_ATTACKS),
                            CombatWatch.ON_ANY_CREATURE_ATTACKS.trigger()));
            if (planarControllerId.equals(attackerControllerId)) {
                abilities.addAll(CombatAbilityReader.readByAbility(
                        WatchedAbilities.printed(object.getCard(), CombatWatch.ON_ALLY_CREATURE_ATTACKS),
                        CombatWatch.ON_ALLY_CREATURE_ATTACKS.trigger()));
            }
            FilterContext attackerContext = FilterContext.of(gameData)
                    .withSourceCardId(object.getCard().getId())
                    .withSourceControllerId(planarControllerId)
                    .withSourcePermanentId(attacker.getId());
            // Each ability of the plane is one object of all its effects (CR 603.3), its filter judged once.
            for (List<CombatAbilityReader.CombatEffect> reads : abilities) {
                CombatAbilityReader.CombatEffect read = reads.getFirst();
                CombatAbilityReader.requireNoLimitOrMultiplicity(read, CombatWatch.ON_ANY_CREATURE_ATTACKS.trigger());
                // "Whenever a [filter] creature attacks": the trigger's filter on the attacker.
                if (read.filter() != null && !predicateEvaluationService.matchesPermanentPredicate(
                        attacker, read.filter(), attackerContext)) {
                    continue;
                }

                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        object.getCard(),
                        planarControllerId,
                        object.getCard().getName() + "'s ability",
                        new ArrayList<>(CombatAbilityReader.effectsOf(reads)),
                        attacker.getId(),
                        attacker.getId());
                entry.setAttackedTargetId(attacker.getAttackTarget());
                entry.setTriggeringPermanentId(attacker.getId());
                entry.setTriggeringPermanentControllerId(attackerControllerId);
                entry.setSourcePermanentSnapshot(new Permanent(attacker));
                entry.setSourcePlanarObject(object.copy());
                entry.setNonTargeting(true);
                gameData.enqueueTrigger(entry);
                gameLogService.append(gameData, GameLog.abilityTriggers(object.getCard()));
            }
        }
    }

    /** Collects ally-creature combat-damage triggers from face-up planes. */
    public void checkPlanarAllyCreatureCombatDamageToPlayerTriggers(
            GameData gameData, Permanent creature, UUID attackerId, UUID defenderId, int damageDealt,
            Set<String> firedBatchedAbilities) {
        if (damageDealt <= 0 || gameData.planechase == null || gameData.planechase.controllerId == null
                || creature == null || !gameData.planechase.controllerId.equals(attackerId)) {
            return;
        }

        UUID planarControllerId = gameData.planechase.controllerId;
        FilterContext triggerContext = FilterContext.of(gameData)
                .withSourceControllerId(planarControllerId);
        for (PlanarObject object : List.copyOf(gameData.planechase.faceUp)) {
            // Each ability of the plane is one object of all its effects (CR 603.3); its filter, conditions and "one or
            // more" are judged once, for the ability.
            List<List<CombatAbilityReader.CombatEffect>> abilities = CombatAbilityReader.readByAbility(
                    WatchedAbilities.printed(object.getCard(), CombatWatch.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER),
                    CombatWatch.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER.trigger());
            for (int abilityIndex = 0; abilityIndex < abilities.size(); abilityIndex++) {
                List<CombatAbilityReader.CombatEffect> reads = abilities.get(abilityIndex);
                CombatAbilityReader.CombatEffect read = reads.getFirst();
                if (read.limit() != TriggerLimit.NONE || read.eachBlocker()) {
                    throw new IllegalStateException("The planar combat damage collector reads no " + read.options()
                            + ": " + read.ability());
                }
                if (read.filter() != null
                        && !predicateEvaluationService.matchesPermanentPredicate(
                        creature, read.filter(), triggerContext)) {
                    continue;
                }

                if (!conditionEvaluationService.triggerConditionsMet(gameData, read.ability().options(),
                        ConditionContext.forCard(object.getCard(), planarControllerId))) {
                    continue;
                }
                if (read.oneOrMore() && !firedBatchedAbilities.add(object.getId() + ":" + abilityIndex)) {
                    continue;
                }
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        object.getCard(),
                        planarControllerId,
                        object.getCard().getName() + "'s triggered ability",
                        new ArrayList<>(CombatAbilityReader.effectsOf(reads)),
                        defenderId,
                        (UUID) null);
                if (!read.oneOrMore()) {
                    entry.setTriggeringPermanentId(creature.getId());
                    entry.setTriggeringPermanentControllerId(attackerId);
                    entry.setTriggeringPermanent(creature);
                }
                entry.setEventValue(damageDealt);
                entry.setSourcePlanarObject(object.copy());
                entry.setNonTargeting(true);
                gameData.enqueueTrigger(entry);
                gameLogService.append(gameData, GameLog.abilityTriggers(object.getCard()));
            }
        }
    }

    private boolean hasPlanarPermanentDamageToYouTrigger(GameData gameData, UUID damagedPlayerId) {
        return gameData.planechase != null
                && Objects.equals(gameData.planechase.controllerId, damagedPlayerId)
                && gameData.planechase.faceUp.stream()
                .anyMatch(object -> !DamageAbilityReader.printed(object.getCard(), DamageWatch.ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU).isEmpty());
    }

    private void checkPlanarPermanentDealsDamageToYouTriggers(
            GameData gameData, UUID damagedPlayerId, Permanent damageSource, boolean isCombatDamage) {
        if (!gameQueryService.isCreature(gameData, damageSource)
                || gameData.planechase == null
                || !Objects.equals(gameData.planechase.controllerId, damagedPlayerId)) {
            return;
        }

        TriggerContext context = new TriggerContext.DamageToController(
                damagedPlayerId, damageSource.getId(), isCombatDamage);
        DamageWatch watch = DamageWatch.ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU;
        UUID creatureControllerId = gameQueryService.findPermanentController(gameData, damageSource.getId());
        for (PlanarObject object : List.copyOf(gameData.planechase.faceUp)) {
            // Each ability of the plane, whole (CR 603.3; Stensia's "put a +1/+1 counter on it"): the plane its source
            // and the creature that dealt the damage its triggering permanent; "once each turn for each creature" is
            // the door's, by the creature.
            for (TriggeredAbility ability : DamageAbilityReader.printed(object.getCard(), watch)) {
                requireNothingToJudge(ability, watch);
                PlanarObject plane = object.copy();
                putEventAbility(gameData, ability, object.getCard(), null, damagedPlayerId,
                        ConditionContext.forCard(object.getCard(), damagedPlayerId)
                                .withTriggeringPermanentId(damageSource.getId()), context, entry -> {
                            entry.setTriggeringPermanentId(damageSource.getId());
                            entry.setTriggeringPermanentControllerId(creatureControllerId);
                            entry.setSourcePlanarObject(plane);
                            entry.setNonTargeting(true);
                        }, damageSource.getId());
            }
        }
    }

    private void checkPlanarAllySourceDealtDamageToOpponentTriggers(
            GameData gameData, UUID damagedPlayerId, UUID sourceControllerId,
            UUID sourcePermanentId, int amount) {
        if (gameData.planechase == null
                || !Objects.equals(gameData.planechase.controllerId, sourceControllerId)
                || sourcePermanentId == null) {
            return;
        }

        Permanent damageSource = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (damageSource == null || !gameQueryService.isCreature(gameData, damageSource)) return;

        TriggerContext context = new TriggerContext.DamageToControllerAmount(
                damagedPlayerId, amount, sourcePermanentId, sourceControllerId);
        for (PlanarObject object : List.copyOf(gameData.planechase.faceUp)) {
            putPlanarDamageAbilities(gameData, object, DamageWatch.ON_ALLY_SOURCE_DEALS_DAMAGE_TO_OPPONENT, context,
                    sourceControllerId, damagedPlayerId, amount, sourcePermanentId, sourceControllerId);
        }
    }

    private void checkPlanarSourceDealtDamageToItsControllerTriggers(
            GameData gameData, UUID damagedPlayerId, UUID sourcePermanentId, int amount) {
        if (gameData.planechase == null
                || gameData.planechase.controllerId == null
                || Objects.equals(gameData.planechase.controllerId, damagedPlayerId)
                || sourcePermanentId == null) {
            return;
        }

        Permanent damageSource = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (damageSource == null || !gameQueryService.isCreature(gameData, damageSource)) return;

        TriggerContext context = new TriggerContext.DamageToControllerAmount(
                damagedPlayerId, amount, sourcePermanentId, damagedPlayerId);
        for (PlanarObject object : List.copyOf(gameData.planechase.faceUp)) {
            putPlanarDamageAbilities(gameData, object, DamageWatch.ON_SOURCE_DEALS_DAMAGE_TO_ITS_CONTROLLER_PLANAR,
                    context, gameData.planechase.controllerId, damagedPlayerId, amount, sourcePermanentId,
                    damagedPlayerId);
        }
    }

    /**
     * Hands each damage ability {@code watch} reads of the face-up plane {@code object} to the stack door under
     * {@code controllerId}, whole (CR 603.3), recording the damage to the player ({@link #playerDamageRecord}, the plane
     * as the ability's source). "Once each turn for each creature" (Stensia) is the door's, by the creature that dealt
     * the damage.
     */
    private void putPlanarDamageAbilities(GameData gameData, PlanarObject object, DamageWatch watch,
                                          TriggerContext context, UUID controllerId, UUID damagedPlayerId, int amount,
                                          UUID sourcePermanentId, UUID sourceControllerId) {
        for (TriggeredAbility ability : DamageAbilityReader.printed(object.getCard(), watch)) {
            requireNothingToJudge(ability, watch);
            putEventAbility(gameData, ability, object.getCard(), null, controllerId,
                    ConditionContext.forCard(object.getCard(), controllerId).withTargetId(damagedPlayerId)
                            .withTriggeringPermanentId(sourcePermanentId).withEventValue(amount), context,
                    playerDamageRecord(null, object, damagedPlayerId, damagedPlayerId, amount, sourcePermanentId,
                            sourceControllerId), sourcePermanentId);
        }
    }

    private void collectExiledVoyageTriggers(GameData gameData, UUID landControllerId) {
        for (var exiled : new ArrayList<>(gameData.exiledCards)) {
            UUID cardId = exiled.card().getId();
            UUID controllerId = gameData.exiledVoyageControllerIds.get(cardId);
            if (!gameData.exiledVoyageCounters.containsKey(cardId)) {
                continue;
            }
            if (gameData.findExiledCard(cardId) == null || controllerId == null) {
                gameData.exiledVoyageCounters.remove(cardId);
                gameData.exiledVoyageControllerIds.remove(cardId);
                continue;
            }
            if (!landControllerId.equals(controllerId)) {
                continue;
            }

            gameData.queueMayAbility(exiled.card(), controllerId, new MayEffect(
                    new PutVoyageCounterOnExiledCardEffect(),
                    "Put a voyage counter on Cosima?",
                    new ReturnVoyagingCardFromExileEffect()));
            gameLogService.append(gameData, GameLog.abilityTriggers(exiled.card()));
            log.info("Game {} - {} exiled voyage landfall trigger queued", gameData.id,
                    exiled.card().getName());
        }
    }

    /**
     * Dispatches a "whenever [an object] enters" ability of {@code effects} with the limit {@code limit} to its watch's
     * handler, whole (CR 603.3). An ability limited to once each turn is one trigger per event, so a doubler
     * (Panharmonicon) gives it no second copy and nothing makes it trigger additional times; "do this only once each
     * turn" marks the source as its "you may" is accepted (CR 603.2h).
     */
    private boolean dispatchEntering(GameData gameData, Permanent perm, UUID controllerId, ZoneChangeWatch watch,
                                     List<CardEffect> effects, TriggerLimit limit, String actionKey,
                                     TriggerContext.PermanentEnters ctx) {
        boolean limited = limit == TriggerLimit.ONCE_EACH_TURN || limit == TriggerLimit.DO_ONLY_ONCE_EACH_TURN;
        var effectCtx = limited ? new TriggerContext.PermanentEnters(ctx.enteringCard(), ctx.enteringControllerId(),
                ctx.defaultTargetPlayerId(), 1, ctx.enteringPermanentId()) : ctx;
        var match = new TriggerMatchContext(gameData, perm, controllerId, effects,
                perm != null ? perm.getCard() : null, null, actionKey, limited);
        return dispatch(match, watch, effectCtx);
    }

    /**
     * Whether a landfall ability's trigger narrows the lands down beyond "a land" (Valakut, the Molten Pinnacle's "a
     * Mountain you control"), so a printed land with it sees its own entry.
     */
    private static boolean narrowedLandfall(TriggerCandidate candidate) {
        return candidate.ability().conditions().stream().anyMatch(condition ->
                condition.trigger() instanceof ZoneChangeTrigger trigger && trigger.filter() != null
                        && !trigger.filter().equals(Triggers.LAND));
    }

    /**
     * Dispatches {@code match}'s ability, its effects together, to {@code slot}'s handler, which puts it on the stack
     * as one object (CR 603.3), as many times as the effects that make abilities trigger additional times say.
     */
    private boolean dispatch(TriggerMatchContext match, Object slot, TriggerContext context) {
        return dispatch(match, slot, context, () -> registry.dispatch(match, slot, context));
    }

    /**
     * Whether {@code slot} collects "whenever [a permanent] is sacrificed" abilities. Sacrificing a creature makes
     * it die, but such an ability triggers on the sacrifice, so an effect that makes "a creature dying" trigger an
     * ability an additional time (Teysa Karlov) doesn't apply to it.
     */
    private static boolean triggeredBySacrifice(Object slot) {
        return slot instanceof ZoneChangeWatch watch && watch.keys().stream()
                .allMatch(key -> key.trigger() instanceof ZoneChangeTrigger move
                        && move.causes().equals(Set.of(ZoneChangeCause.SACRIFICE)));
    }

    /**
     * {@link #dispatch} for an effect of a creature-death ability the trigger pipeline matched: the handler is
     * {@code watch}'s, not a slot's.
     */
    private boolean dispatchCreatureDeath(TriggerMatchContext match, CreatureDeathWatch watch, TriggerContext context) {
        return dispatch(match, null, context,
                () -> deathTriggerCollectorService.dispatchCreatureDeath(watch, match, context));
    }

    private boolean dispatch(TriggerMatchContext match, Object slot, TriggerContext context,
                             java.util.function.BooleanSupplier handler) {
        return dispatch(match, slot, context, handler, false);
    }

    /**
     * {@link #dispatch(TriggerMatchContext, Object, TriggerContext, java.util.function.BooleanSupplier)}, where
     * {@code singleCopy} says that nothing makes the ability trigger additional times (a granted cascade).
     */
    private boolean dispatch(TriggerMatchContext match, Object slot, TriggerContext context,
                             java.util.function.BooleanSupplier handler, boolean singleCopy) {
        EffectSlot effectSlot = slot instanceof EffectSlot named ? named : null;
        if (context instanceof TriggerContext.PermanentEnters
                && gameQueryService.areOpponentPermanentETBTriggersSuppressed(
                match.gameData(), match.controllerId())) {
            return false;
        }
        boolean grantedSpellCascade = singleCopy;
        // "This ability triggers only once each turn" caps the ability at one trigger a turn, so an
        // effect that makes it trigger additional times (Clara Oswald) cannot add a second copy.
        boolean triggersOnlyOnceEachTurn = match.triggersOnlyOnceEachTurn();
        int previousCopies = match.gameData().beginTriggeredAbilityCopies(
                grantedSpellCascade || triggersOnlyOnceEachTurn ? 1 : 1 +
                gameQueryService.countAdditionalTriggeredAbilityTriggers(
                        match.gameData(), match.controllerId(), match.permanent(),
                        isDirectAttackTrigger(slot), context)
                + (context.causedByCreatureDying() && !triggeredBySacrifice(slot)
                ? gameQueryService.countAdditionalCreatureDeathTriggeredAbilityTriggers(
                        match.gameData(), match.controllerId(), match.permanent())
                : 0));
        GameData.CollectingTrigger previousCollecting = match.gameData().beginCollectingTrigger(
                new GameData.CollectingTrigger(effectSlot,
                        match.effects().isEmpty() ? null : match.effects().getFirst(), match.sourceCard()));
        Object activeBefore = match.gameData().interaction.activeInteraction();
        // The handler routes one ability: what it hands off as that ability's record is dropped when it returns.
        GameData.TriggerRouting previousRouting = match.gameData().beginTriggerRouting();
        try {
            boolean handled = handler.getAsBoolean();
            Object activeAfter = match.gameData().interaction.activeInteraction();
            if (activeAfter != null && activeAfter != activeBefore) {
                // A handler that asks its question at once begins the prompt instead of queueing it.
                match.gameData().observeTrigger(RulesObserver.Route.INTERACTION, activeAfter);
            }
            return handled;
        } finally {
            match.gameData().endTriggerRouting(previousRouting);
            match.gameData().endCollectingTrigger(previousCollecting);
            match.gameData().restoreTriggeredAbilityCopies(previousCopies);
        }
    }

    private boolean isDirectAttackTrigger(Object key) {
        if (!(key instanceof CombatWatch watch)) {
            return false;
        }
        return switch (watch) {
            case ON_ATTACK, ON_ALLY_CREATURES_ATTACK, ON_ALLY_CREATURES_ATTACK_PLAYER,
                    ON_ALLY_CREATURES_ATTACK_PLAYER_OR_PLANESWALKER,
                    ON_ALLY_CREATURE_ATTACKS,
                    ON_CREATURE_ATTACKS_YOU, ON_CREATURE_ATTACKS_YOU_DIRECTLY, ON_CREATURES_ATTACK_YOU,
                    ON_OPPONENT_CREATURES_ATTACK_YOU_UNBLOCKED,
                    ON_ANY_CREATURE_ATTACKS, ON_ANY_PLAYER_ATTACKS, ON_ANY_PLAYER_ATTACKS_YOUR_OPPONENT,
                    ON_OPPONENT_ATTACKS_ANOTHER_OPPONENT -> true;
            default -> false;
        };
    }

    private UUID instantOrSorceryCastOrCopyController(TriggerContext context) {
        Card spellCard = switch (context) {
            case TriggerContext.SpellCast spellCast -> spellCast.spellCard();
            case TriggerContext.SpellCopy spellCopy -> spellCopy.spellCard();
            default -> null;
        };
        if (spellCard == null
                || (!spellCard.hasType(CardType.INSTANT) && !spellCard.hasType(CardType.SORCERY))) {
            return null;
        }
        return switch (context) {
            case TriggerContext.SpellCast spellCast -> spellCast.castingPlayerId();
            case TriggerContext.SpellCopy spellCopy -> spellCopy.copyingPlayerId();
            default -> null;
        };
    }

    /**
     * Evolve (CR 702.100a): "whenever a creature you control enters, if that creature's power is greater than this
     * creature's power and/or that creature's toughness is greater than this creature's toughness, put a +1/+1 counter
     * on this creature". The intervening "if" is judged as the ability triggers and wraps its effect, so it is checked
     * again as it resolves (CR 603.4).
     */
    private void collectEvolveTrigger(GameData gameData, UUID controllerId, Permanent source,
                                      Permanent enteringPermanent, int triggerCount) {
        if (gameQueryService.areOpponentPermanentETBTriggersSuppressed(gameData, controllerId)) {
            return;
        }
        if (!conditionEvaluationService.isMet(gameData, EVOLVE_COMPARISON,
                ConditionContext.forPermanent(source, controllerId)
                        .withTriggeringPermanentId(enteringPermanent.getId()))) {
            return;
        }

        for (int i = 0; i < triggerCount; i++) {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    source.getCard(),
                    controllerId,
                    source.getCard().getName() + "'s ability",
                    new ArrayList<>(List.of(new ConditionalEffect(EVOLVE_COMPARISON, new EvolveEffect()))),
                    0,
                    source.getId());
            entry.setTriggeringPermanentId(enteringPermanent.getId());
            entry.setNonTargeting(true);
            gameData.enqueueTrigger(entry);
            gameLogService.append(gameData, GameLog.abilityTriggers(source.getCard()));
            log.info("Game {} - {} triggers evolve for {} entering", gameData.id,
                    source.getCard().getName(), enteringPermanent.getCard().getName());
        }
    }

    private Permanent findPermanentByCard(GameData gameData, Card card) {
        Permanent[] found = new Permanent[1];
        gameData.forEachPermanent((playerId, permanent) -> {
            if (found[0] == null && permanent.getCard() == card) {
                found[0] = permanent;
            }
        });
        return found[0];
    }

    private Card effectiveEnteringCharacteristics(GameData gameData, Card card, Permanent permanent) {
        Card characteristics = card.createRuntimeCopy();
        if (permanent == null) return characteristics;
        Set<CardType> types = new HashSet<>();
        for (CardType type : CardType.values()) {
            boolean hasType = switch (type) {
                case CREATURE -> gameQueryService.isCreature(gameData, permanent);
                case ARTIFACT -> gameQueryService.isArtifact(gameData, permanent);
                case ENCHANTMENT -> gameQueryService.isEnchantment(gameData, permanent);
                case LAND -> gameQueryService.isLand(gameData, permanent);
                case PLANESWALKER -> gameQueryService.isPlaneswalker(gameData, permanent);
                case BATTLE -> gameQueryService.isBattle(gameData, permanent);
                default -> card.hasType(type);
            };
            if (hasType) types.add(type);
        }
        characteristics.setType(types.stream().findFirst().orElse(card.getType()));
        characteristics.setAdditionalTypes(types);
        characteristics.setSubtypes(new ArrayList<>(gameQueryService.getEffectiveSubtypes(gameData, permanent)));
        List<CardColor> colors = new ArrayList<>(gameQueryService.getEffectiveColors(gameData, permanent));
        characteristics.setColors(colors);
        characteristics.setColor(colors.stream().findFirst().orElse(null));
        Set<Keyword> keywords = EnumSet.noneOf(Keyword.class);
        for (Keyword keyword : Keyword.values()) {
            if (gameQueryService.hasKeyword(gameData, permanent, keyword)) keywords.add(keyword);
        }
        characteristics.setKeywords(keywords);
        return characteristics;
    }

    private boolean enteringPermanentHasType(GameData gameData, Card card, CardType type) {
        Permanent permanent = findPermanentByCard(gameData, card);
        if (permanent == null) return card.hasType(type);
        return switch (type) {
            case CREATURE -> gameQueryService.isCreature(gameData, permanent);
            case ENCHANTMENT -> gameQueryService.isEnchantment(gameData, permanent);
            default -> card.hasType(type);
        };
    }

    // ── Internal dispatch ──────────────────────────────────────────────

    /**
     * Dispatches the abilities {@code watch} reads for each face-up scheme in the command zone whose owner passes
     * {@code ownerFilter}; a face-up scheme's triggered abilities trigger there (CR 314.4).
     */
    private void dispatchFaceUpSchemes(GameData gameData, TriggerWatch watch, TriggerContext ctx,
                                       Predicate<UUID> ownerFilter) {
        gameData.forEachFaceUpSchemeSource((ownerId, scheme) -> {
            if (ownerFilter.test(ownerId)) {
                dispatchWatch(gameData, scheme, ownerId, watch, ctx);
            }
        });
    }

    /**
     * "When you spend this mana to cast ..." riders on a mana ability are delayed triggers created when the
     * mana is produced (CR 106.6), so they still fire if the producing permanent has left the battlefield
     * by the time the mana is spent. Only those riders are collected from the departed source's
     * last-known information.
     */
    private void dispatchDepartedManaSourceSpellCastTriggers(GameData gameData, Card spellCard,
                                                              UUID castingPlayerId, TriggerContext ctx) {
        for (UUID sourceId : List.copyOf(gameData.spellCastManaSourceIds.getOrDefault(spellCard.getId(), Set.of()))) {
            if (gameQueryService.findPermanentById(gameData, sourceId) != null) continue;
            Permanent departed = gameData.departedPermanentSnapshots.values().stream()
                    .filter(snapshot -> snapshot.getId().equals(sourceId)).findFirst().orElse(null);
            if (departed == null) continue;
            for (TriggeredAbility ability : departed.getCard().getTriggeredAbilities()) {
                SpellFilter filter = watchedSpellFilter(ability, SpellAbilityWatch.ON_CONTROLLER_CASTS_SPELL, false);
                if (filter != null && filter.paidWithManaFromThis()) {
                    collectSpellAbility(gameData, departed, departed.getCard(), castingPlayerId,
                            SpellAbilityWatch.ON_CONTROLLER_CASTS_SPELL, ctx, ability, filter, null);
                }
            }
        }
    }

    /**
     * Dispatches the abilities {@code watch} reads: the permanent's own abilities with its trigger, and those other
     * permanents grant it, each dispatched to {@code watch}'s handlers.
     */
    private boolean dispatchWatch(GameData gameData, Permanent perm, UUID controllerId, TriggerWatch watch,
                                  TriggerContext ctx) {
        if (watch instanceof SpellAbilityWatch spellWatch) {
            return dispatchSpellWatch(gameData, perm, controllerId, spellWatch, ctx);
        }
        if (watch instanceof LibraryWatch libraryWatch) {
            return dispatchLibraryWatch(gameData, perm, controllerId, libraryWatch, ctx);
        }
        if (watch instanceof TapWatch tapWatch) {
            // The watches dispatched here ("a player taps a permanent for mana", the untap step) judge no first-time
            // event, "one or more" or limit; "a player taps a permanent for mana" judges the tap relation and the
            // filter ("an opponent taps an artifact": Treasure Nabber).
            Trigger watched = tapWatch.trigger();
            TriggerJudge judge = watchJudge(gameData, perm, controllerId, ctx);
            List<List<CardEffect>> printed = ctx instanceof TriggerContext.PermanentTapForMana tap
                    ? TapAbilityReader.readPrintedAsJudgedByAbility(perm.getCard(), tapWatch, judge).stream()
                            .filter(reads -> tapForManaMatches(gameData, reads.getFirst(), perm, perm.getCard(),
                                    controllerId, tap.tappingPlayerId(), tap.tappedPermanentId()))
                            .map(TapAbilityReader::effectsOf)
                            .toList()
                    : byAbility(judge.filter(TapAbilityReader.printed(perm.getCard(), tapWatch)),
                            abilities -> effectsOf(TapAbilityReader.registrations(abilities, watched)));
            return dispatchRegistrations(gameData, perm, controllerId, watch, ctx, printed,
                    staticBonus -> byAbility(judge.filter(grantedTriggeredAbilitySupport
                            .grantedTriggeredAbilities(gameData, perm, staticBonus,
                                    ability -> ability.hasWatchedTrigger(watched))),
                            abilities -> TapAbilityReader.effects(abilities, watched)));
        }
        if (watch instanceof SagaWatch sagaWatch) {
            return dispatchSagaWatch(gameData, perm, controllerId, sagaWatch, ctx);
        }
        if (watch instanceof ActionWatch actionWatch) {
            return dispatchActionWatch(gameData, perm, controllerId, actionWatch, ctx);
        }
        if (watch instanceof MiscWatch miscWatch) {
            return dispatchMiscWatch(gameData, perm, controllerId, miscWatch, ctx);
        }
        if (watch instanceof CombatWatch combatWatch) {
            Trigger watched = combatWatch.trigger();
            TriggerJudge judge = watchJudge(gameData, perm, controllerId, ctx);
            return dispatchRegistrations(gameData, perm, controllerId, watch, ctx,
                    byAbility(judge.filter(WatchedAbilities.printed(perm.getCard(), combatWatch)),
                            abilities -> effectsOf(CombatAbilityReader.registrations(abilities, watched))),
                    staticBonus -> byAbility(judge.filter(grantedTriggeredAbilitySupport
                            .grantedTriggeredAbilities(gameData, perm, staticBonus,
                                    ability -> ability.hasWatchedTrigger(watched))),
                            abilities -> CombatAbilityReader.effects(abilities, watched)));
        }
        throw new IllegalArgumentException("No collector dispatches " + watch + " here");
    }

    /**
     * The {@link TriggerJudge} of the abilities a watch dispatched for {@code perm} reads: each one's intervening "if"
     * (CR 603.4) and event condition (CR 603.2), read from {@code perm} and its controller, with the event's value
     * where the event has one (the result of the die roll, the damage dealt to the controller) and the player gaining
     * life as "that player" ("an opponent gains life during their turn").
     */
    private TriggerJudge watchJudge(GameData gameData, Permanent perm, UUID controllerId, TriggerContext ctx) {
        ConditionContext context = ConditionContext.forPermanent(perm, controllerId);
        return switch (ctx) {
            case TriggerContext.DiceRoll diceRoll -> conditionEvaluationService.judge(gameData, context,
                    diceRoll.result());
            case TriggerContext.DamageToControllerAmount damage -> conditionEvaluationService.judge(gameData, context,
                    damage.amount());
            case TriggerContext.LifeGain lifeGain -> conditionEvaluationService.judge(gameData,
                    context.withTargetId(lifeGain.gainingPlayerId()), lifeGain.lifeGainedAmount());
            // "Whenever an opponent loses exactly 1 life" (Ob Nixilis, Captive Kingpin): the life lost is the event's
            // value, which an event condition reads.
            case TriggerContext.LifeLoss lifeLoss -> conditionEvaluationService.judge(gameData,
                    context.withTargetId(lifeLoss.losingPlayerId()), lifeLoss.lifeLostAmount());
            case TriggerContext.LifePayment lifePayment -> conditionEvaluationService.judge(gameData,
                    context.withTargetId(lifePayment.payingPlayerId()), lifePayment.lifePaidAmount());
            // "Whenever you choose to put one or more cards on the bottom of your library while scrying" (The Temporal
            // Anchor): the number of cards put on the bottom is the event's value, which an event condition reads.
            case TriggerContext.Scry scry -> conditionEvaluationService.judge(gameData,
                    context.withTargetId(scry.scryingPlayerId()), scry.bottomedCardCount());
            case null, default -> conditionEvaluationService.judge(gameData, context);
        };
    }

    /**
     * Dispatches the abilities the Saga watch {@code watch} reads on {@code perm}, its own and those other permanents
     * grant it, to the watch's handlers, as {@link #dispatchRegistrations} does, judging each ability's "triggers only
     * once each turn" from the ability: it doesn't trigger once the limit is reached, and it is marked on {@code perm}
     * by the ability once one of its effects triggered (Tom Bombadil). These watches' collectors read no other option.
     */
    private boolean dispatchSagaWatch(GameData gameData, Permanent perm, UUID controllerId, SagaWatch watch,
                                      TriggerContext ctx) {
        if (perm.isLosesAllAbilitiesUntilEndOfTurn()) return false;
        GameQueryService.StaticBonus staticBonus = gameQueryService.computeStaticBonus(gameData, perm);
        if (staticBonus.losesAllAbilities() || staticBonus.losesAllNonManaAbilities()) return false;
        Trigger watched = watch.trigger();
        boolean triggered = false;
        // Reused for the granted-ability lookup below unless a printed ability was dispatched in between and may have
        // changed the game state.
        boolean staticBonusCurrent = true;
        List<TriggeredAbility> printed = SagaAbilityReader.printed(perm.getCard(), watch);
        if (!printed.isEmpty() && !gameQueryService.hasLostPrintedAbilities(gameData, perm)) {
            staticBonusCurrent = false;
            List<TextReplacement> globalWordChange = TextChangeTransformer.globalColorWordReplacements(gameData);
            for (TriggeredAbility ability : printed) {
                // Layer 3 (CR 612.1): the trigger condition is read from the permanent's text-changed words.
                triggered |= dispatchSagaAbility(gameData, perm, controllerId, watch, ctx,
                        SagaAbilityReader.read(ability, watched).stream().map(read -> read.withEffect(
                                TextChangeTransformer.transform(read.effect(), perm.getTextReplacements(),
                                        globalWordChange))).toList());
            }
        }
        for (TriggeredAbility ability : grantedTriggeredAbilitySupport.grantedTriggeredAbilities(gameData, perm,
                staticBonusCurrent ? staticBonus : null, ability -> ability.hasWatchedTrigger(watched))) {
            triggered |= dispatchSagaAbility(gameData, perm, controllerId, watch, ctx,
                    SagaAbilityReader.read(ability, watched));
        }
        return triggered;
    }

    /**
     * Dispatches the effects of one final-chapter ability, {@code effects}, judging and marking its limit on
     * {@code perm}.
     */
    private boolean dispatchSagaAbility(GameData gameData, Permanent perm, UUID controllerId, SagaWatch watch,
                                        TriggerContext ctx, List<SagaAbilityReader.SagaEffect> effects) {
        SagaAbilityReader.SagaEffect first = effects.getFirst();
        if (first.limitReached(gameData, perm.getId())) {
            return false;
        }
        boolean triggered = dispatchSlotEffect(gameData, perm, controllerId, watch, ctx,
                effects.stream().map(SagaAbilityReader.SagaEffect::effect).toList(), (String) null,
                first.triggersOnlyOnceEachTurn());
        if (triggered) {
            first.markLimit(gameData, perm.getId());
        }
        return triggered;
    }

    /**
     * Dispatches the abilities the player or keyword action watch {@code watch} reads on {@code perm}, its own and those
     * other permanents grant it, to the watch's handlers, as {@link #dispatchRegistrations} does, judging each ability's
     * trigger-level parts from the ability ({@link ActionAbilityReader}): the card the action is taken with must match
     * its filter, it doesn't trigger once its "triggers only once each turn" is reached, and it is marked on
     * {@code perm} by the ability once one of its effects triggered (the crime cards, Curator of Sun's Creation).
     */
    private boolean dispatchActionWatch(GameData gameData, Permanent perm, UUID controllerId, ActionWatch watch,
                                        TriggerContext ctx) {
        if (perm.isLosesAllAbilitiesUntilEndOfTurn()) return false;
        GameQueryService.StaticBonus staticBonus = gameQueryService.computeStaticBonus(gameData, perm);
        if (staticBonus.losesAllAbilities() || staticBonus.losesAllNonManaAbilities()) return false;
        Trigger watched = watch.trigger();
        boolean triggered = false;
        // Reused for the granted-ability lookup below unless a printed ability was dispatched in between and may have
        // changed the game state.
        boolean staticBonusCurrent = true;
        List<TriggeredAbility> printed = ActionAbilityReader.printed(perm.getCard(), watch);
        if (!printed.isEmpty() && !gameQueryService.hasLostPrintedAbilities(gameData, perm)) {
            staticBonusCurrent = false;
            List<TextReplacement> globalWordChange = TextChangeTransformer.globalColorWordReplacements(gameData);
            for (TriggeredAbility ability : printed) {
                // Layer 3 (CR 612.1): the trigger condition is read from the permanent's text-changed words.
                triggered |= dispatchActionAbility(gameData, perm, controllerId, watch, ctx,
                        ActionAbilityReader.read(ability, watched).stream().map(read -> read.withEffect(
                                TextChangeTransformer.transform(read.effect(), perm.getTextReplacements(),
                                        globalWordChange))).toList());
            }
        }
        for (TriggeredAbility ability : grantedTriggeredAbilitySupport.grantedTriggeredAbilities(gameData, perm,
                staticBonusCurrent ? staticBonus : null, ability -> ability.hasWatchedTrigger(watched))) {
            triggered |= dispatchActionAbility(gameData, perm, controllerId, watch, ctx,
                    ActionAbilityReader.read(ability, watched));
        }
        return triggered;
    }

    /**
     * Dispatches the effects of one player or keyword action ability, {@code effects}, judging its filter against the
     * card the action is taken with ({@link #actionCard}) and judging and marking its limit on {@code perm}. A "you set
     * a scheme in motion" ability is one stack object of all its effects (CR 603.3), put on the stack by
     * {@link #collectSchemeAbility}.
     */
    private boolean dispatchActionAbility(GameData gameData, Permanent perm, UUID controllerId, ActionWatch watch,
                                          TriggerContext ctx, List<ActionAbilityReader.ActionEffect> effects) {
        ActionAbilityReader.ActionEffect first = effects.getFirst();
        if (!watchJudge(gameData, perm, controllerId, ctx).triggers(first.ability())) {
            return false;
        }
        if (first.filter() != null) {
            Card actionCard = actionCard(ctx);
            if (actionCard == null) {
                throw new IllegalStateException("The collector of " + watch + " judges no filter: "
                        + first.ability());
            }
            if (!predicateEvaluationService.matchesCardPredicate(actionCard, first.filter(), perm.getCard().getId(),
                    gameData, controllerId)) {
                return false;
            }
        }
        if (first.limitReached(gameData, perm.getId())) {
            return false;
        }
        List<CardEffect> abilityEffects = effects.stream().map(ActionAbilityReader.ActionEffect::effect).toList();
        boolean triggered = watch == ActionWatch.ON_CONTROLLER_SETS_SCHEME_IN_MOTION
                ? dispatch(new TriggerMatchContext(gameData, perm, controllerId, abilityEffects), watch, ctx,
                        () -> collectSchemeAbility(gameData, perm, controllerId, abilityEffects,
                                (TriggerContext.SchemeSetInMotion) ctx))
                : dispatchSlotEffect(gameData, perm, controllerId, watch, ctx, abilityEffects, (String) null,
                        first.triggersOnlyOnceEachTurn());
        if (triggered) {
            first.markLimit(gameData, perm.getId());
        }
        return triggered;
    }

    /**
     * Dispatches the abilities the misc watch {@code watch} reads on {@code perm}, its own and those other permanents
     * grant it, to the watch's handlers, as {@link #dispatchRegistrations} does, judging each ability's trigger-level
     * parts from the ability ({@link MiscAbilityReader}): a door ability triggers only for its own door, and an ability
     * doesn't trigger once its "triggers only once each turn" is reached and is marked on {@code perm} by the ability
     * once one of its effects triggered.
     */
    private boolean dispatchMiscWatch(GameData gameData, Permanent perm, UUID controllerId, MiscWatch watch,
                                      TriggerContext ctx) {
        if (perm.isLosesAllAbilitiesUntilEndOfTurn()) return false;
        GameQueryService.StaticBonus staticBonus = gameQueryService.computeStaticBonus(gameData, perm);
        if (staticBonus.losesAllAbilities() || staticBonus.losesAllNonManaAbilities()) return false;
        Trigger watched = watch.trigger();
        boolean triggered = false;
        // Reused for the granted-ability lookup below unless a printed ability was dispatched in between and may have
        // changed the game state.
        boolean staticBonusCurrent = true;
        List<TriggeredAbility> printed = MiscAbilityReader.printed(perm.getCard(), watch);
        if (!printed.isEmpty() && !gameQueryService.hasLostPrintedAbilities(gameData, perm)) {
            staticBonusCurrent = false;
            List<TextReplacement> globalWordChange = TextChangeTransformer.globalColorWordReplacements(gameData);
            for (TriggeredAbility ability : printed) {
                // Layer 3 (CR 612.1): the trigger condition is read from the permanent's text-changed words.
                triggered |= dispatchMiscAbility(gameData, perm, controllerId, watch, ctx,
                        MiscAbilityReader.read(ability, watched).stream().map(read -> read.withEffect(
                                TextChangeTransformer.transform(read.effect(), perm.getTextReplacements(),
                                        globalWordChange))).toList());
            }
        }
        for (TriggeredAbility ability : grantedTriggeredAbilitySupport.grantedTriggeredAbilities(gameData, perm,
                staticBonusCurrent ? staticBonus : null, ability -> ability.hasWatchedTrigger(watched))) {
            triggered |= dispatchMiscAbility(gameData, perm, controllerId, watch, ctx,
                    MiscAbilityReader.read(ability, watched));
        }
        return triggered;
    }

    /**
     * Dispatches the effects of one misc ability, {@code effects}, judging its door against the door unlocked and
     * judging and marking its limit on {@code perm}. A "when you unlock this door" ability is one stack object of all
     * its effects (CR 603.3), put on the stack by {@link #collectDoorAbility}.
     */
    private boolean dispatchMiscAbility(GameData gameData, Permanent perm, UUID controllerId, MiscWatch watch,
                                        TriggerContext ctx, List<MiscAbilityReader.MiscEffect> effects) {
        MiscAbilityReader.MiscEffect first = effects.getFirst();
        if (!watchJudge(gameData, perm, controllerId, ctx).triggers(first.ability())) {
            return false;
        }
        if (watch == MiscWatch.ON_SELF_ROOM_DOOR_UNLOCKED) {
            if (first.door() == null) {
                throw new IllegalStateException("A \"when you unlock this door\" ability names its door: "
                        + first.ability());
            }
            if (!(ctx instanceof TriggerContext.RoomDoorUnlocked unlocked) || unlocked.doorIndex() != first.door()) {
                return false;
            }
        }
        if (first.limitReached(gameData, perm.getId())) {
            return false;
        }
        boolean triggered;
        List<CardEffect> abilityEffects = effects.stream().map(MiscAbilityReader.MiscEffect::effect).toList();
        if (watch == MiscWatch.ON_ALLY_CREATURE_CREWS_VEHICLE) {
            if (!(ctx instanceof TriggerContext.CreatureCrewsVehicle crew)
                    || first.objectFilter() != null && !predicateEvaluationService.matchesPermanentPredicate(
                    crew.crewingCreature(), first.objectFilter(), FilterContext.of(gameData)
                            .withSourceCardId(perm.getCard().getId())
                            .withSourceControllerId(controllerId)
                            .withSourcePermanentId(perm.getId())
                            .withSourcePermanentSnapshot(perm))) {
                return false;
            }
            triggered = dispatch(new TriggerMatchContext(gameData, perm, controllerId, abilityEffects), watch, ctx,
                    () -> collectCrewAbility(gameData, perm, controllerId, abilityEffects, crew.vehicle()));
        } else if (watch == MiscWatch.ON_SELF_ROOM_DOOR_UNLOCKED) {
            triggered = dispatch(new TriggerMatchContext(gameData, perm, controllerId, abilityEffects), watch, ctx,
                    () -> collectDoorAbility(gameData, perm, controllerId, abilityEffects, first.door()));
        } else {
            triggered = dispatchSlotEffect(gameData, perm, controllerId, watch, ctx, abilityEffects, (String) null,
                    first.triggersOnlyOnceEachTurn());
        }
        if (triggered) {
            first.markLimit(gameData, perm.getId());
        }
        return triggered;
    }

    /**
     * Puts a "whenever [a creature you control] crews a Vehicle" ability of {@code source} on the stack as it is
     * written, all its {@code effects} one object (CR 603.3), with the crewed {@code vehicle} as the entry's
     * triggering permanent ("that Vehicle"). It triggers as the crew cost is paid, so it waits with the other
     * cost-payment triggers until the crew ability is on the stack.
     */
    private boolean collectCrewAbility(GameData gameData, Permanent source, UUID controllerId,
                                       List<CardEffect> effects, Permanent vehicle) {
        if (effects.stream().anyMatch(effect -> effect.targetSpec().declaredTarget() != null
                || source.getCard().getEffectTargetIndex(effect) >= 0)) {
            throw new IllegalStateException("No crew collector chooses targets: " + source.getCard().getName());
        }
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                source.getCard(),
                controllerId,
                source.getCard().getName() + "'s ability",
                new ArrayList<>(effects),
                null,
                source.getId());
        entry.setTriggeringPermanent(vehicle);
        entry.setTriggeringPermanentId(vehicle.getId());
        entry.setNonTargeting(true);
        gameData.pendingActivatedAbilityCostTriggers.add(entry);
        gameLogService.append(gameData, GameLog.abilityTriggers(source.getCard()));
        log.info("Game {} - {} triggers as a creature crews {}", gameData.id, source.getCard().getName(),
                vehicle.getCard().getName());
        return true;
    }

    /**
     * Puts a "when you unlock this door" ability of the Room {@code room} on the stack, all its {@code effects} one
     * object (CR 603.3), once the door unlocks, as it enters or later (CR 709.5h); a targeted ability waits for its
     * targets.
     */
    private boolean collectDoorAbility(GameData gameData, Permanent room, UUID controllerId, List<CardEffect> effects,
                                       int doorIndex) {
        Card card = room.getCard();
        boolean needsTarget = effects.stream()
                .anyMatch(effect -> effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                        || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                        || effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)
                        || card.getEffectTargetIndex(effect) >= 0);
        StackEntry trigger = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                card,
                controllerId,
                card.getName() + "'s ability",
                new ArrayList<>(effects),
                null,
                room.getId());
        trigger.setSourcePermanentSnapshot(new Permanent(room));
        gameData.handOffTriggerRecord(trigger);
        if (needsTarget) {
            boolean multiTarget = card.getTargetGroups().stream()
                    .anyMatch(target -> target.getMaxTargets() > 1 || target.getMinTargets() == 0)
                    || card.getTargetGroups().size() > 1;
            if (multiTarget) {
                gameData.queueInteraction(new PermanentChoiceContext.ETBTokenMultiTargetTrigger(
                        card, controllerId, new ArrayList<>(effects), room.getId(),
                        List.of(), 0, 0, List.of(), 0));
            } else {
                gameData.queueInteraction(new PermanentChoiceContext.SelfTriggeredAbilityTarget(
                        card, controllerId, new ArrayList<>(effects),
                        "unlocks a Room door", room.getId(), null,
                        effects.stream().anyMatch(candidate ->
                                candidate instanceof OptionalTargetEffect || candidate.hasOptionalTarget())
                                || card.getTargetGroups().stream().anyMatch(target -> target.getMinTargets() == 0
                                && effects.stream().anyMatch(candidate ->
                                card.getEffectTargetIndex(candidate) == target.getIndex()))));
            }
        } else {
            gameData.enqueueTrigger(trigger);
        }
        gameLogService.append(gameData, GameLog.abilityTriggers(card));
        log.info("Game {} - {} triggers when Room door {} unlocks", gameData.id, card.getName(), doorIndex);
        return true;
    }

    /**
     * The card a player or keyword action is taken with, which an action ability's filter judges: the exploited
     * creature, or the scheme set in motion; {@code null} for an action whose collector judges no filter.
     */
    private static Card actionCard(TriggerContext ctx) {
        return switch (ctx) {
            case TriggerContext.CreatureExploit exploit -> exploit.exploitedCard();
            case TriggerContext.SchemeSetInMotion scheme ->
                    scheme.schemeEntry() == null ? null : scheme.schemeEntry().getCard();
            default -> null;
        };
    }

    /**
     * Puts a "whenever you set a scheme in motion" ability of {@code source} on the stack, all its {@code effects} one
     * object (CR 603.3), once its controller sets the scheme in motion; the entry's triggering spell is the scheme's
     * set-in-motion object as it was, which "set that scheme in motion again" reads.
     */
    private boolean collectSchemeAbility(GameData gameData, Permanent source, UUID controllerId,
                                         List<CardEffect> effects, TriggerContext.SchemeSetInMotion context) {
        StackEntry schemeEntry = context.schemeEntry();
        Card schemeCard = schemeEntry == null ? null : schemeEntry.getCard();
        if (schemeCard == null || !context.settingPlayerId().equals(controllerId)) {
            return false;
        }
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                source.getCard(),
                controllerId,
                source.getCard().getName() + "'s ability",
                new ArrayList<>(effects),
                null,
                source.getId());
        entry.setTriggeringCardId(schemeCard.getId());
        entry.setTriggeringSpell(schemeEntry);
        entry.setNonTargeting(true);
        gameData.enqueueTrigger(entry);
        return true;
    }

    /** Evolve's intervening "if" (CR 702.100a): the entering creature is bigger in power or toughness. */
    private static final Condition EVOLVE_COMPARISON = new TriggeringPermanentHasGreaterPowerOrToughnessThanSource();

    /** "A nonland card", the discarded card the "first nonland discard each turn" ability counts. */
    private static final CardPredicate NONLAND_CARD = new CardNotPredicate(new CardTypePredicate(CardType.LAND));

    /**
     * Dispatches the abilities the library watch {@code watch} reads on {@code perm}, its own and those other
     * permanents grant it, to the watch's handlers, as {@link #dispatchRegistrations} does, judging each ability from
     * the ability itself: the discarded cards' filter and "for the first time each turn" from the event
     * ({@link #watchedLibraryEventMatches}), then its limit ({@link #dispatchLibraryAbility}). These watches'
     * collectors read no narrower event.
     */
    private boolean dispatchLibraryWatch(GameData gameData, Permanent perm, UUID controllerId, LibraryWatch watch,
                                         TriggerContext ctx) {
        if (perm.isLosesAllAbilitiesUntilEndOfTurn()) return false;
        GameQueryService.StaticBonus staticBonus = gameQueryService.computeStaticBonus(gameData, perm);
        if (staticBonus.losesAllAbilities() || staticBonus.losesAllNonManaAbilities()) return false;
        Trigger watched = watch.trigger();
        boolean triggered = false;
        // Reused for the granted-ability lookup below unless a printed ability was dispatched in between and may have
        // changed the game state.
        boolean staticBonusCurrent = true;
        List<TriggeredAbility> printed = LibraryAbilityReader.printed(perm.getCard(), watch);
        if (!printed.isEmpty() && !gameQueryService.hasLostPrintedAbilities(gameData, perm)) {
            staticBonusCurrent = false;
            List<TextReplacement> globalWordChange = TextChangeTransformer.globalColorWordReplacements(gameData);
            for (TriggeredAbility ability : printed) {
                // Layer 3 (CR 612.1): the trigger condition is read from the permanent's text-changed words.
                List<LibraryAbilityReader.LibraryEffect> reads = LibraryAbilityReader.read(ability, watched).stream()
                        .map(read -> read.withEffect(TextChangeTransformer.transform(read.effect(),
                                perm.getTextReplacements(), globalWordChange))).toList();
                if (watchedLibraryEventMatches(gameData, controllerId, watch, ctx, reads.getFirst())) {
                    triggered |= dispatchLibraryAbility(gameData, perm, controllerId, watch, ctx, reads);
                }
            }
        }
        for (TriggeredAbility ability : grantedTriggeredAbilitySupport.grantedTriggeredAbilities(gameData, perm,
                staticBonusCurrent ? staticBonus : null, ability -> ability.hasWatchedTrigger(watched))) {
            List<LibraryAbilityReader.LibraryEffect> reads = LibraryAbilityReader.read(ability, watched);
            if (watchedLibraryEventMatches(gameData, controllerId, watch, ctx, reads.getFirst())) {
                triggered |= dispatchLibraryAbility(gameData, perm, controllerId, watch, ctx, reads);
            }
        }
        return triggered;
    }

    /**
     * Whether the event {@code ctx} matches {@code read}'s ability as the collector of {@code watch}, which reads no
     * narrower event, judges it: one of the discarded cards matches the filter ("whenever you discard one or more land
     * cards"), and "for the first time each turn" is the player's first discard event or surveil this turn, counting
     * those before the source was on the battlefield.
     */
    private boolean watchedLibraryEventMatches(GameData gameData, UUID controllerId, LibraryWatch watch,
                                               TriggerContext ctx, LibraryAbilityReader.LibraryEffect read) {
        LibraryAbilityReader.requireWatchedEvent(read, watch.trigger());
        if (read.filter() != null) {
            if (!(ctx instanceof TriggerContext.DiscardEvent discardEvent)) {
                throw new IllegalStateException("The collector of " + watch.trigger() + " judges no filter: "
                        + read.ability());
            }
            if (discardEvent.discardedCards().stream().noneMatch(card -> predicateEvaluationService
                    .matchesCardPredicate(card, read.filter(), null, gameData, controllerId))) {
                return false;
            }
        }
        if (read.counts().isEmpty()) {
            return true;
        }
        LibraryAbilityReader.requireNoCountButFirst(read, watch.trigger());
        if (ctx instanceof TriggerContext.DiscardEvent discardEvent) {
            return gameData.cardsDiscardedThisTurn.getOrDefault(controllerId, 0) == discardEvent.discardedCount();
        }
        if (ctx instanceof TriggerContext.Surveil surveil) {
            return surveil.firstThisTurn();
        }
        throw new IllegalStateException("The collector of " + watch.trigger() + " counts no events: " + read.ability());
    }

    /**
     * Dispatches the effects of one library ability, {@code effects}, whose filter, event and count matched, to
     * {@code watch}'s handlers: it doesn't trigger once its limit is reached, and the limit is marked on {@code perm}
     * by the ability once one of its effects triggered ("triggers only once each turn", "this ability triggers only
     * once"), or as the action is taken ("do this only once each turn", CR 603.2h).
     */
    private boolean dispatchLibraryAbility(GameData gameData, Permanent perm, UUID controllerId, LibraryWatch watch,
                                           TriggerContext ctx, List<LibraryAbilityReader.LibraryEffect> effects) {
        LibraryAbilityReader.LibraryEffect first = effects.getFirst();
        if (!watchJudge(gameData, perm, controllerId, ctx).triggers(first.ability())) {
            return false;
        }
        if (first.limitReached(gameData, perm.getId())) {
            return false;
        }
        // A limit or a first-time count is one trigger a turn, so nothing makes the ability trigger additional times.
        boolean triggersOnlyOnceEachTurn = first.limit() == TriggerLimit.ONCE_EACH_TURN
                || first.limit() == TriggerLimit.DO_ONLY_ONCE_EACH_TURN || first.firstTimeEachTurn();
        boolean triggered = dispatchSlotEffect(gameData, perm, controllerId, watch, ctx,
                LibraryAbilityReader.effectsOf(effects), first.onceEachTurnActionKey(), triggersOnlyOnceEachTurn);
        if (triggered) {
            first.markLimit(gameData, perm.getId());
        }
        return triggered;
    }

    /** Whether {@code read}'s filter, if any, matches {@code discardedCard}, judged for {@code controllerId}. */
    private boolean discardedCardMatches(GameData gameData, LibraryAbilityReader.LibraryEffect read,
                                         Card discardedCard, boolean characteristicsKnown, UUID controllerId) {
        if (read.filter() == null) {
            return true;
        }
        return characteristicsKnown && predicateEvaluationService.matchesCardPredicate(discardedCard, read.filter(),
                null, gameData, controllerId);
    }

    /**
     * Collects the "whenever you discard a card" abilities of {@code perm}, which its controller discarded
     * {@code discardedCard} under: each ability's filter, own event ("you cycle a card", "an opponent's spell or
     * ability causes you to discard a card"), "for the first time each turn", event condition (CR 603.2: "during your
     * turn") and intervening "if" (CR 603.4) are judged from the ability, then its limit, around its dispatch, whole
     * (CR 603.3). "You cycle a card" and "an opponent causes you to discard" put the ability on the stack as the
     * handlers of their old wrappers did.
     */
    private boolean collectControllerDiscardAbilities(GameData gameData, Permanent perm, UUID discardingPlayerId,
                                                      Card discardedCard, boolean cycled, boolean characteristicsKnown,
                                                      TriggerContext ctx) {
        LibraryWatch watch = LibraryWatch.ON_CONTROLLER_DISCARDS;
        boolean anyTriggered = false;
        for (TriggeredAbility ability : LibraryAbilityReader.printed(perm.getCard(), watch)) {
            List<LibraryAbilityReader.LibraryEffect> reads = LibraryAbilityReader.read(ability, watch.trigger());
            LibraryAbilityReader.LibraryEffect first = reads.getFirst();
            if (first.limit() == TriggerLimit.DO_ONLY_ONCE_EACH_TURN) {
                throw new IllegalStateException("The \"you discard a card\" collector marks no \"do this only once "
                        + "each turn\": " + ability);
            }
            if (!discardedCardMatches(gameData, first, discardedCard, characteristicsKnown, discardingPlayerId)
                    || !controllerDiscardEventMatches(gameData, first, discardingPlayerId, cycled)
                    || !watchJudge(gameData, perm, discardingPlayerId, ctx).triggers(ability)
                    || first.limitReached(gameData, perm.getId())) {
                continue;
            }
            var match = new TriggerMatchContext(gameData, perm, discardingPlayerId,
                    LibraryAbilityReader.effectsOf(reads));
            boolean triggered = switch (first.event()) {
                case CYCLES -> dispatch(match, watch, ctx,
                        () -> discardTriggerCollectorService.collectCycling(match, ctx));
                case DISCARDS_BY_OPPONENT -> dispatch(match, watch, ctx,
                        () -> discardTriggerCollectorService.collectOpponentCausedDiscard(match, ctx));
                default -> dispatch(match, watch, ctx);
            };
            if (triggered) {
                first.markLimit(gameData, perm.getId());
                anyTriggered = true;
            }
        }
        return anyTriggered;
    }

    /**
     * Whether a discard by {@code playerId} is the event of {@code read}'s ability, read by the "whenever you discard a
     * card" collector: any discard, a cycling ("whenever you cycle a card"), or a discard an opponent's spell or ability
     * causes; and, for "for the first time each turn", the player's first cycling or first nonland discard this turn,
     * counting those before the source was on the battlefield.
     */
    private static boolean controllerDiscardEventMatches(GameData gameData, LibraryAbilityReader.LibraryEffect read,
                                                         UUID playerId, boolean cycled) {
        boolean eventMatches = switch (read.event()) {
            case DISCARDS -> true;
            case CYCLES -> cycled;
            case DISCARDS_BY_OPPONENT -> gameData.discardCausedByOpponent;
            default -> throw new IllegalStateException("The \"you discard a card\" collector reads no "
                    + read.event() + ": " + read.ability());
        };
        if (!eventMatches || read.counts().isEmpty()) {
            return eventMatches;
        }
        LibraryAbilityReader.requireNoCountButFirst(read, LibraryWatch.ON_CONTROLLER_DISCARDS.trigger());
        if (read.event() == LibraryTrigger.LibraryEvent.CYCLES && read.filter() == null) {
            return gameData.cardsCycledThisTurn.getOrDefault(playerId, 0) == 1;
        }
        if (read.event() == LibraryTrigger.LibraryEvent.DISCARDS && NONLAND_CARD.equals(read.filter())) {
            return gameData.nonlandCardsDiscardedThisTurn.getOrDefault(playerId, 0) == 1;
        }
        throw new IllegalStateException("The \"you discard a card\" collector counts only the first cycling and the "
                + "first nonland discard of the turn: " + read.ability());
    }

    /**
     * {@code ability}'s land-play trigger condition that narrows {@code watch}'s trigger by a filter or a zone,
     * functioning in {@code watch}'s zone (or in any zone when {@code anyZone}, as a granted ability is read), or
     * {@code null} when the ability has none: then {@code watch}'s collectors read it by its effects, if it has
     * {@code watch}'s trigger.
     */
    private static LandPlayTrigger watchedLandPlayTrigger(TriggeredAbility ability, SpellAbilityWatch watch,
                                                          boolean anyZone) {
        for (TriggerCondition condition : ability.conditions()) {
            if (condition.trigger() instanceof LandPlayTrigger landPlay && landPlay.filtered()
                    && landPlay.unfiltered().equals(watch.trigger())
                    && (anyZone || condition.zones().functionsIn(watch.zone()))) {
                return landPlay;
            }
        }
        return null;
    }

    /** Whether {@code watch}'s collector reads {@code ability} itself: its cast, copy or land-play trigger filters. */
    private static boolean readsItself(TriggeredAbility ability, SpellAbilityWatch watch, boolean anyZone) {
        return watchedSpellFilter(ability, watch, anyZone) != null
                || watchedLandPlayTrigger(ability, watch, anyZone) != null;
    }

    /**
     * The filter of {@code ability}'s cast or copy trigger condition that narrows {@code watch}'s trigger, functioning
     * in {@code watch}'s zone (or in any zone when {@code anyZone}, as a granted ability is read), or {@code null} when
     * the ability has none: then {@code watch}'s collectors read it by its effects, if it has {@code watch}'s trigger.
     */
    private static SpellFilter watchedSpellFilter(TriggeredAbility ability, SpellAbilityWatch watch, boolean anyZone) {
        for (TriggerCondition condition : ability.conditions()) {
            if (condition.trigger() instanceof SpellTrigger spell && spell.filter() != null
                    && spell.unfiltered().equals(watch.trigger())
                    && (anyZone || condition.zones().functionsIn(watch.zone()))) {
                return spell.filter();
            }
        }
        return null;
    }

    /**
     * The filter the generic cast or copy collector reads {@code ability} with on {@code watch}: its trigger's filter
     * ({@link #watchedSpellFilter}), or {@link SpellFilter#ANY} for a cast trigger without one ("whenever an opponent
     * casts a spell": any spell, as the emblem reader reads it), so the collector records the spell, its caster and the
     * cast event's value whatever the effects are; {@code null} when neither applies.
     */
    private static SpellFilter collectedSpellFilter(TriggeredAbility ability, SpellAbilityWatch watch,
                                                    boolean anyZone) {
        SpellFilter filter = watchedSpellFilter(ability, watch, anyZone);
        if (filter == null && watch.trigger() instanceof SpellTrigger spell
                && spell.event() == SpellTrigger.SpellEvent.CAST
                && (anyZone ? ability.hasWatchedTrigger(watch.trigger())
                        : ability.hasWatchedTrigger(watch.trigger(), watch.zone()))) {
            return SpellFilter.ANY;
        }
        return filter;
    }

    /**
     * Collects the abilities a spell or land-play watch on the battlefield reads on {@code perm}, its own and those
     * other permanents grant it, in order: a cast ability (with a spell filter or none, "any spell") and a copy ability
     * with a spell filter go to the spell-cast or spell-copy collector, one whose land-play trigger has a filter or a
     * zone to the land-play collector, and any other with the watch's trigger to {@link #collectUnfilteredAbility}; each
     * judges what its event says and hands the ability whole to the stack door. The printed abilities' spell filters
     * and effects are read through the permanent's text changes (CR 612).
     */
    private boolean dispatchSpellWatch(GameData gameData, Permanent perm, UUID controllerId, SpellAbilityWatch watch,
                                       TriggerContext ctx) {
        if (perm.isLosesAllAbilitiesUntilEndOfTurn()) return false;
        GameQueryService.StaticBonus staticBonus = gameQueryService.computeStaticBonus(gameData, perm);
        if (staticBonus.losesAllAbilities() || staticBonus.losesAllNonManaAbilities()) return false;
        boolean triggered = false;
        // Reused for the granted-ability lookup below unless a printed ability was processed in between and may have
        // changed the game state.
        boolean staticBonusCurrent = true;
        Card card = perm.getCard();
        List<TriggeredAbility> printed = card.getTriggeredAbilities().stream()
                .filter(ability -> readsItself(ability, watch, false)
                        || ability.hasWatchedTrigger(watch.trigger(), watch.zone()))
                .toList();
        if (!printed.isEmpty() && !gameQueryService.hasLostPrintedAbilities(gameData, perm)) {
            staticBonusCurrent = false;
            List<TextReplacement> globalWordChange = TextChangeTransformer.globalColorWordReplacements(gameData);
            for (TriggeredAbility ability : printed) {
                SpellFilter filter = collectedSpellFilter(ability, watch, false);
                if (filter != null) {
                    filter = TextChangeTransformer.transformSpellFilter(filter, perm.getTextReplacements(),
                            globalWordChange);
                    triggered |= collectSpellAbility(gameData, perm, card, controllerId, watch, ctx, ability, filter,
                            null);
                    continue;
                }
                LandPlayTrigger landPlay = watchedLandPlayTrigger(ability, watch, false);
                if (landPlay != null) {
                    triggered |= collectLandPlayAbility(gameData, perm, card, controllerId, ctx, ability,
                            landPlay, null);
                    continue;
                }
                if (watch == SpellAbilityWatch.ON_CONTROLLER_SPELL_CAUSES_CARD_ADVANTAGE) {
                    triggered |= collectCardAdvantageAbility(gameData, perm, card, controllerId, ctx, ability);
                    continue;
                }
                // Layer 3 (CR 612.1): the trigger condition is read from the permanent's text-changed words.
                triggered |= collectUnfilteredAbility(gameData, perm, card, controllerId, ctx, ability,
                        effect -> TextChangeTransformer.transform(effect, perm.getTextReplacements(),
                                globalWordChange), null);
            }
        }
        // Triggered abilities granted continuously by another permanent (e.g. Pontiff of Blight giving other
        // creatures you control extort) trigger off the permanent that has them.
        for (TriggeredAbility ability : grantedTriggeredAbilitySupport.grantedTriggeredAbilities(gameData, perm,
                staticBonusCurrent ? staticBonus : null,
                candidate -> readsItself(candidate, watch, true)
                        || candidate.hasWatchedTrigger(watch.trigger()))) {
            SpellFilter filter = collectedSpellFilter(ability, watch, true);
            if (filter != null) {
                triggered |= collectSpellAbility(gameData, perm, card, controllerId, watch, ctx, ability, filter,
                        null);
                continue;
            }
            LandPlayTrigger landPlay = watchedLandPlayTrigger(ability, watch, true);
            if (landPlay != null) {
                triggered |= collectLandPlayAbility(gameData, perm, card, controllerId, ctx, ability, landPlay,
                        null);
                continue;
            }
            if (watch == SpellAbilityWatch.ON_CONTROLLER_SPELL_CAUSES_CARD_ADVANTAGE) {
                triggered |= collectCardAdvantageAbility(gameData, perm, card, controllerId, ctx, ability);
                continue;
            }
            triggered |= collectUnfilteredAbility(gameData, perm, card, controllerId, ctx, ability,
                    UnaryOperator.identity(), null);
        }
        return triggered;
    }

    /**
     * Collects {@code ability}, a "whenever a spell you control causes you to gain card advantage" ability of
     * {@code perm}, for the spell {@code ctx} names, through the cast collector, which hands it to the stack door.
     *
     * @return whether the ability triggered
     */
    private boolean collectCardAdvantageAbility(GameData gameData, Permanent perm, Card sourceCard, UUID controllerId,
                                                TriggerContext ctx, TriggeredAbility ability) {
        return spellCastTriggerCollectorService.collectCardAdvantageAbility(
                new TriggerMatchContext(gameData, perm, controllerId, List.of(), sourceCard), ability,
                (TriggerContext.SpellCardAdvantage) ctx);
    }

    /**
     * Collects the abilities of {@code card}, which has no permanent (a card in exile, a face-up plane or phenomenon,
     * {@code planarObject}), that {@code watch} reads in its zone, in order: a filtered cast or land-play ability goes
     * to the spell-cast or land-play collector, any other with the watch's trigger to {@link #collectUnfilteredAbility}.
     */
    private void dispatchCardSpellAbilities(GameData gameData, Card card, UUID controllerId, SpellAbilityWatch watch,
                                            TriggerContext ctx, PlanarObject planarObject) {
        for (TriggeredAbility ability : card.getTriggeredAbilities()) {
            SpellFilter filter = collectedSpellFilter(ability, watch, false);
            if (filter != null) {
                collectSpellAbility(gameData, null, card, controllerId, watch, ctx, ability, filter, planarObject);
                continue;
            }
            LandPlayTrigger landPlay = watchedLandPlayTrigger(ability, watch, false);
            if (landPlay != null) {
                collectLandPlayAbility(gameData, null, card, controllerId, ctx, ability, landPlay, planarObject);
                continue;
            }
            if (!ability.hasWatchedTrigger(watch.trigger(), watch.zone())) {
                continue;
            }
            collectUnfilteredAbility(gameData, null, card, controllerId, ctx, ability, UnaryOperator.identity(),
                    planarObject);
        }
    }

    /**
     * Collects {@code ability}, which has a spell, land-play or counter watch's own trigger (no spell filter, land
     * filter or zone), for the event {@code ctx}: its event condition is judged as it triggers (CR 603.2), and the stack
     * door judges its intervening "if" and limit and puts it on the stack as one object of its effects (CR 603.3),
     * read through {@code transform} (the permanent's text changes), with the event recorded whatever they are
     * ({@link #spellWatchRecord}). {@code perm} is the source on the battlefield, or {@code null} for
     * {@code sourceCard} in exile or a face-up plane or phenomenon ({@code planarObject}).
     *
     * @return whether the ability triggered
     */
    private boolean collectUnfilteredAbility(GameData gameData, Permanent perm, Card sourceCard, UUID controllerId,
                                             TriggerContext ctx, TriggeredAbility ability,
                                             UnaryOperator<CardEffect> transform, PlanarObject planarObject) {
        if (ability.options().multiplicity() != TriggerMultiplicity.EACH) {
            throw new IllegalStateException("No spell, land-play or activation reader reads "
                    + ability.options().multiplicity() + ": " + ability);
        }
        ConditionContext context = perm != null ? ConditionContext.forPermanent(perm, controllerId)
                : ConditionContext.forCard(sourceCard, controllerId);
        // A land-play ability's options read the land played as the triggering card ("if it shares a mana value").
        if (ctx instanceof TriggerContext.LandPlayed landPlayed) {
            context = context.withTriggeringCard(landPlayed.landCard());
        }
        if (!conditionEvaluationService.eventConditionMet(gameData, ability.options(), context)) {
            return false;
        }
        List<EffectRegistration> transformed = new ArrayList<>(ability.effects().size());
        boolean changed = false;
        for (EffectRegistration registration : ability.effects()) {
            CardEffect effect = transform.apply(registration.effect());
            changed |= effect != registration.effect();
            transformed.add(new EffectRegistration(effect, registration.simultaneousWithPrevious()));
        }
        boolean triggered = triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(
                changed ? ability.withEffects(transformed) : ability, sourceCard, perm, controllerId,
                sourceCard.getName() + "'s ability", context, spellWatchRecord(ctx, perm, planarObject))
                .withEvent(ctx));
        if (triggered) {
            gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        }
        return triggered;
    }

    /**
     * Hands {@code ability}, a triggered ability of {@code sourceCard} ({@code source} on the battlefield, or
     * {@code null} for a card in another zone) controlled by {@code controllerId}, to the stack door for
     * {@code event}: its event condition is judged now in {@code context} (CR 603.2), which carries what the event
     * names (its value, "that player", the triggering permanent); the door judges its intervening "if" in the same
     * context (CR 603.4) and its limit, counts the additional times it triggers, stamps {@code record} on its entry
     * whatever its effects are and chooses its targets as it goes on the stack (CR 603.3d).
     *
     * @return whether it triggered
     */
    private boolean putEventAbility(GameData gameData, TriggeredAbility ability, Card sourceCard, Permanent source,
                                    UUID controllerId, ConditionContext context, TriggerContext event,
                                    TriggerRecord record) {
        return putEventAbility(gameData, ability, sourceCard, source, controllerId, context, event, record, null);
    }

    /**
     * {@link #putEventAbility(GameData, TriggeredAbility, Card, Permanent, UUID, ConditionContext, TriggerContext,
     * TriggerRecord)} for an event about the creature {@code limitCreatureId}, by which "once each turn for each
     * creature" (Stensia) is judged and marked.
     */
    private boolean putEventAbility(GameData gameData, TriggeredAbility ability, Card sourceCard, Permanent source,
                                    UUID controllerId, ConditionContext context, TriggerContext event,
                                    TriggerRecord record, UUID limitCreatureId) {
        if (!conditionEvaluationService.eventConditionMet(gameData, ability.options(), context)) {
            return false;
        }
        boolean triggered = triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(ability,
                sourceCard, source, controllerId, sourceCard.getName() + "'s ability", context, record)
                .withEvent(event).withLimitCreature(limitCreatureId));
        if (triggered) {
            gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        }
        return triggered;
    }

    /**
     * Hands each ability the counter watch {@code watch} reads of {@code perm}, its own and those other permanents
     * grant it, to the stack door for {@code event}, judged in {@code context} and stamped with {@code record}. These
     * watches' collectors judge no filter or narrower event of their own; this throws on one.
     */
    private boolean putCounterAbilities(GameData gameData, Permanent perm, UUID controllerId, CounterWatch watch,
                                        TriggerContext event, ConditionContext context, TriggerRecord record) {
        boolean triggered = false;
        for (TriggeredAbility ability : watchedAbilities(gameData, perm, watch)) {
            CounterTrigger trigger = CounterAbilityReader.trigger(ability, watch.trigger());
            if (trigger.qualifier() != null || trigger.event() != ((CounterTrigger) watch.trigger()).event()) {
                throw new IllegalStateException("The collector of " + watch + " judges no filter or narrower event: "
                        + ability);
            }
            triggered |= putEventAbility(gameData, ability, perm.getCard(), perm, controllerId, context, event,
                    record);
        }
        return triggered;
    }

    /**
     * Hands each ability {@code watch} reads of {@code card} in its owner {@code controllerId}'s graveyard (its own and
     * its perpetual ones; none while cards in graveyards have lost all abilities) to the stack door for {@code event}:
     * the record names this graveyard incarnation of the card as its triggering card and its source (CR 400.7). No
     * graveyard counter ability is written with a filter or narrower event; this throws on one.
     */
    private void putGraveyardCounterAbilities(GameData gameData, Card card, UUID controllerId, CounterWatch watch,
                                              TriggerContext event) {
        for (TriggeredAbility ability : WatchedAbilities.withWatchedTrigger(
                gameQueryService.getEffectiveGraveyardTriggeredAbilities(gameData, card), watch.trigger(),
                watch.zone())) {
            CounterTrigger trigger = CounterAbilityReader.trigger(ability, watch.trigger());
            if (trigger.qualifier() != null || trigger.event() != ((CounterTrigger) watch.trigger()).event()) {
                throw new IllegalStateException("The collector of " + watch + " judges no filter or narrower event: "
                        + ability);
            }
            long graveyardEntryVersion = gameData.graveyardEntryVersion(card.getId());
            putEventAbility(gameData, ability, card, null, controllerId, ConditionContext.forCard(card, controllerId),
                    event, entry -> {
                        entry.setTriggeringCardId(card.getId());
                        entry.setTriggeringCardGraveyardEntryVersion(graveyardEntryVersion);
                        entry.setSourceGraveyardEntryVersion(graveyardEntryVersion);
                    });
        }
    }

    /**
     * The record of a player, counter or tap event, stamped on the ability's entry whatever its effects are:
     * {@code thatPlayerId} as the non-targeting "that player" ({@code null} for an event of the ability's controller),
     * the player the event was about as its triggering player, its count as its event value ("that much", "that many";
     * {@code null} for an event with none), the permanent it was about with that permanent's controller as it happened,
     * and the source as it was when the ability triggered.
     */
    private static TriggerRecord eventRecord(Permanent source, UUID thatPlayerId, UUID triggeringPlayerId,
                                             Integer eventValue, Permanent triggeringPermanent,
                                             UUID triggeringPermanentControllerId) {
        Permanent snapshot = source == null ? null : new Permanent(source);
        return entry -> {
            entry.setNonTargeting(true);
            if (thatPlayerId != null) {
                entry.setTargetId(thatPlayerId);
            }
            entry.setTriggeringPlayerId(triggeringPlayerId);
            if (eventValue != null) {
                entry.setEventValue(eventValue);
            }
            if (triggeringPermanent != null) {
                entry.setTriggeringPermanentId(triggeringPermanent.getId());
                entry.setTriggeringPermanentControllerId(triggeringPermanentControllerId);
            }
            if (snapshot != null) {
                entry.setSourcePermanentSnapshot(snapshot);
            }
        };
    }

    /**
     * The abilities {@code watch} reads of {@code perm}, each to be handed whole to the stack door: its printed ones,
     * read through its text changes (CR 612; none once it has lost them), then those other permanents' static
     * abilities grant it; none once it has lost all its abilities.
     */
    private List<TriggeredAbility> watchedAbilities(GameData gameData, Permanent perm, TriggerWatch watch) {
        if (perm.isLosesAllAbilitiesUntilEndOfTurn()) {
            return List.of();
        }
        GameQueryService.StaticBonus staticBonus = gameQueryService.computeStaticBonus(gameData, perm);
        if (staticBonus.losesAllAbilities() || staticBonus.losesAllNonManaAbilities()) {
            return List.of();
        }
        List<TriggeredAbility> abilities = new ArrayList<>(printedWatchedAbilities(gameData, perm, watch));
        abilities.addAll(grantedTriggeredAbilitySupport.grantedTriggeredAbilities(gameData, perm, staticBonus,
                ability -> ability.hasWatchedTrigger(watch.trigger())));
        return abilities;
    }

    /**
     * The printed abilities {@code watch} reads of {@code perm}, read through its text changes (Layer 3, CR 612.1:
     * Glamerdye on Riverfall Mimic); none once it has lost them.
     */
    private List<TriggeredAbility> printedWatchedAbilities(GameData gameData, Permanent perm, TriggerWatch watch) {
        List<TriggeredAbility> printed = WatchedAbilities.printed(perm.getCard(), watch);
        if (printed.isEmpty() || gameQueryService.hasLostPrintedAbilities(gameData, perm)) {
            return List.of();
        }
        List<TextReplacement> globalWordChange = TextChangeTransformer.globalColorWordReplacements(gameData);
        List<TriggeredAbility> abilities = new ArrayList<>(printed.size());
        for (TriggeredAbility ability : printed) {
            List<EffectRegistration> transformed = new ArrayList<>(ability.effects().size());
            boolean changed = false;
            for (EffectRegistration registration : ability.effects()) {
                CardEffect effect = TextChangeTransformer.transform(registration.effect(),
                        perm.getTextReplacements(), globalWordChange);
                changed |= effect != registration.effect();
                transformed.add(new EffectRegistration(effect, registration.simultaneousWithPrevious()));
            }
            abilities.add(changed ? ability.withEffects(transformed) : ability);
        }
        return abilities;
    }

    /**
     * The record of a spell, land-play, counter or mana-ability event, stamped on the ability's entry whatever its
     * effects are: the player who played the land as the non-targeting "that player" and the land as the triggering
     * card ("that card"), the player who activated the mana ability with the mana produced as the event's value, and
     * the spell countered or fizzled as the triggering spell, as it last existed on the stack (CR 608.2h); with the
     * source as it triggered and the face-up plane or phenomenon whose ability it is.
     */
    private static TriggerRecord spellWatchRecord(TriggerContext ctx, Permanent perm, PlanarObject planarObject) {
        return entry -> {
            entry.setNonTargeting(true);
            if (perm != null) {
                entry.setSourcePermanentSnapshot(new Permanent(perm));
            }
            if (planarObject != null) {
                entry.setSourcePlanarObject(planarObject.copy());
            }
            switch (ctx) {
                case TriggerContext.LandPlayed landPlayed -> {
                    entry.setTargetId(landPlayed.playingPlayerId());
                    entry.setTriggeringCardId(landPlayed.landCard().getId());
                    entry.setTriggeringCards(List.of(landPlayed.landCard()));
                }
                case TriggerContext.ManaAbilityResolved mana -> {
                    entry.setTriggeringPlayerId(mana.activatingPlayerId());
                    entry.setEventValue(mana.manaProduced());
                }
                case TriggerContext.SpellCounteredOrFizzled countered -> {
                    entry.setTriggeringCardId(countered.spellEntry().getCard().getId());
                    entry.setTriggeringSpell(countered.spellEntry());
                }
                default -> {
                }
            }
        };
    }

    /**
     * Collects {@code ability}, whose cast or copy trigger has {@code filter}, for the spell-cast or spell-copy event
     * {@code ctx}, through the spell-cast or spell-copy collector, which judges the filter and hands the ability to the
     * stack door. {@code perm} is its source on the battlefield (as it last existed, for a mana ability's "when you
     * spend this mana" rider), or {@code null} for {@code sourceCard} elsewhere; {@code planarObject} is the face-up
     * plane or phenomenon whose ability it is, or {@code null}.
     *
     * @return whether the ability triggered
     */
    private boolean collectSpellAbility(GameData gameData, Permanent perm, Card sourceCard, UUID controllerId,
                                        SpellAbilityWatch watch, TriggerContext ctx, TriggeredAbility ability,
                                        SpellFilter filter, PlanarObject planarObject) {
        TriggerMatchContext match = new TriggerMatchContext(gameData, perm, controllerId, List.of(), sourceCard,
                planarObject == null ? null : planarObject.copy());
        if (ctx instanceof TriggerContext.SpellCast spellCast) {
            return spellCastTriggerCollectorService.collectCastAbility(match, CastAbility.of(ability, filter),
                    spellCast, watch == SpellAbilityWatch.ON_CONTROLLER_CASTS_SPELL);
        }
        return spellCastTriggerCollectorService.collectCopyAbility(match, ability, filter,
                (TriggerContext.SpellCopy) ctx);
    }

    /**
     * Collects {@code ability}, whose land-play trigger {@code trigger} has a filter or a zone, for the land play
     * {@code ctx}, through the land-play collector, which judges the filters and hands the ability to the stack door.
     *
     * @return whether the ability triggered
     */
    private boolean collectLandPlayAbility(GameData gameData, Permanent perm, Card sourceCard, UUID controllerId,
                                           TriggerContext ctx, TriggeredAbility ability, LandPlayTrigger trigger,
                                           PlanarObject planarObject) {
        return playedCardNameTriggerCollectorService.collectLandPlayAbility(
                new TriggerMatchContext(gameData, perm, controllerId, List.of(), sourceCard,
                        planarObject == null ? null : planarObject.copy()),
                ability, trigger, (TriggerContext.LandPlayed) ctx);
    }

    /**
     * Dispatches each of {@code printedAbilities}, then each ability {@code granted} returns for the permanent's static
     * bonus ({@code null} when that bonus may be stale), to {@code key}'s handlers, each ability whole (CR 603.3): one
     * list of its effects as its reader read them.
     */
    private boolean dispatchRegistrations(GameData gameData, Permanent perm, UUID controllerId, Object key,
                                          TriggerContext ctx, List<List<CardEffect>> printedAbilities,
                                          java.util.function.Function<GameQueryService.StaticBonus,
                                                  List<List<CardEffect>>> granted) {
        if (perm.isLosesAllAbilitiesUntilEndOfTurn()) return false;
        GameQueryService.StaticBonus staticBonus = gameQueryService.computeStaticBonus(gameData, perm);
        if (staticBonus.losesAllAbilities() || staticBonus.losesAllNonManaAbilities()) return false;
        boolean triggered = false;
        // Reused for the granted-trigger lookup below unless a printed trigger was processed in
        // between and may have changed the game state.
        boolean staticBonusCurrent = true;
        if (!printedAbilities.isEmpty() && !gameQueryService.hasLostPrintedAbilities(gameData, perm)) {
            staticBonusCurrent = false;
            List<TextReplacement> globalWordChange = TextChangeTransformer.globalColorWordReplacements(gameData);
            for (List<CardEffect> ability : printedAbilities) {
                // Layer 3 (CR 612.1): the trigger condition is read from the permanent's
                // text-changed words (Glamerdye on Riverfall Mimic).
                List<CardEffect> effects = ability.stream().map(effect -> TextChangeTransformer.transform(
                        effect, perm.getTextReplacements(), globalWordChange)).toList();
                if (dispatchSlotEffect(gameData, perm, controllerId, key, ctx, effects)) {
                    triggered = true;
                }
            }
        }
        // Triggered abilities granted continuously by another permanent (e.g. Pontiff of Blight
        // giving other creatures you control extort) fire off the permanent that has them.
        for (List<CardEffect> ability : granted.apply(staticBonusCurrent ? staticBonus : null)) {
            if (dispatchSlotEffect(gameData, perm, controllerId, key, ctx, ability)) {
                triggered = true;
            }
        }
        return triggered;
    }

    /**
     * The effects each of {@code abilities} puts on the stack, one list per ability that has any, as {@code read}
     * reads an ability's: the flat readers' list of one ability.
     */
    private static List<CardEffect> effectsOf(List<EffectRegistration> registrations) {
        return registrations.stream().map(EffectRegistration::effect).toList();
    }

    private static List<List<CardEffect>> byAbility(List<TriggeredAbility> abilities,
                                                    java.util.function.Function<List<TriggeredAbility>,
                                                            List<CardEffect>> read) {
        List<List<CardEffect>> byAbility = new ArrayList<>();
        for (TriggeredAbility ability : abilities) {
            List<CardEffect> effects = read.apply(List.of(ability));
            if (!effects.isEmpty()) {
                byAbility.add(effects);
            }
        }
        return byAbility;
    }

    private boolean dispatchSlotEffect(GameData gameData, Permanent perm, UUID controllerId,
            Object slot, TriggerContext ctx, List<CardEffect> effects) {
        return dispatchSlotEffect(gameData, perm, controllerId, slot, ctx, effects, (String) null);
    }

    /**
     * {@link #dispatchSlotEffect(GameData, Permanent, UUID, Object, TriggerContext, List)}, where
     * {@code markOnAcceptance}, when not {@code null}, is the ability's limit key for a "do this only once each turn"
     * (CR 603.2h) that taking the action uses up.
     */
    private boolean dispatchSlotEffect(GameData gameData, Permanent perm, UUID controllerId,
            Object slot, TriggerContext ctx, List<CardEffect> effects, String markOnAcceptance) {
        return dispatchSlotEffect(gameData, perm, controllerId, slot, ctx, effects, markOnAcceptance, false);
    }

    /**
     * Dispatches the ability of {@code effects} of {@code perm} to {@code slot}'s handler, whole (CR 603.3), where
     * {@code triggersOnlyOnceEachTurn} says that the ability "triggers only once each turn", so nothing makes it trigger
     * additional times.
     */
    private boolean dispatchSlotEffect(GameData gameData, Permanent perm, UUID controllerId,
            Object slot, TriggerContext ctx, List<CardEffect> effects, String markOnAcceptance,
            boolean triggersOnlyOnceEachTurn) {
        return dispatch(new TriggerMatchContext(gameData, perm, controllerId, effects,
                        perm != null ? perm.getCard() : null, null, markOnAcceptance, triggersOnlyOnceEachTurn),
                slot, ctx);
    }

    /**
     * Increment keyword. For each permanent the casting player controls with the {@link Keyword#INCREMENT}
     * keyword, fire the trigger if the mana spent on the cast spell is greater than that creature's current
     * power or toughness (the intervening-if; re-checked again at resolution per CR 603.4). The mana spent is
     * snapshotted into the stack entry's {@code xValue} for the resolution handler.
     */
    private void collectIncrementTriggers(GameData gameData, Card spellCard, UUID castingPlayerId) {
        int manaSpent = gameData.getSpellCastManaSpent(spellCard.getId());
        gameData.forEachPermanent((playerId, perm) -> {
            if (!playerId.equals(castingPlayerId)) return;
            if (perm.isLosesAllAbilitiesUntilEndOfTurn()) return;
            if (!gameQueryService.hasKeyword(gameData, perm, Keyword.INCREMENT)) return;
            if (!gameQueryService.isCreature(gameData, perm)) return;
            if (manaSpent <= gameQueryService.getEffectivePower(gameData, perm)
                    && manaSpent <= gameQueryService.getEffectiveToughness(gameData, perm)) return;

            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    perm.getCard(),
                    castingPlayerId,
                    perm.getCard().getName() + "'s ability",
                    new ArrayList<>(List.of(new IncrementTriggerEffect())),
                    manaSpent,
                    perm.getId()
            ));
            log.info("Game {} - {} increment trigger queued (mana spent {})",
                    gameData.id, perm.getCard().getName(), manaSpent);
        });
    }

    /**
     * Evaluates a predicate built only from card types against an explicit set of card types, or
     * returns {@code null} when the predicate looks at anything else.
     */
    private static Boolean matchesCardTypesOnly(CardPredicate predicate, Set<CardType> cardTypes) {
        if (predicate instanceof CardTypePredicate type) {
            return cardTypes.contains(type.cardType());
        }
        if (predicate instanceof CardNotPredicate not) {
            Boolean inner = matchesCardTypesOnly(not.predicate(), cardTypes);
            return inner == null ? null : !inner;
        }
        if (predicate instanceof CardAllOfPredicate allOf) {
            boolean result = true;
            for (CardPredicate part : allOf.predicates()) {
                Boolean partMatch = matchesCardTypesOnly(part, cardTypes);
                if (partMatch == null) return null;
                result &= partMatch;
            }
            return result;
        }
        if (predicate instanceof CardAnyOfPredicate anyOf) {
            boolean result = false;
            for (CardPredicate part : anyOf.predicates()) {
                Boolean partMatch = matchesCardTypesOnly(part, cardTypes);
                if (partMatch == null) return null;
                result |= partMatch;
            }
            return result;
        }
        return null;
    }

    /** Evaluates a power or toughness predicate against last-known stats, or null for any other predicate. */
    public static Boolean matchesDyingStats(PermanentPredicate predicate, int power, int toughness) {
        if (predicate instanceof com.github.laxika.magicalvibes.model.filter.PermanentToughnessAtLeastPredicate p) {
            return toughness >= p.minToughness();
        }
        if (predicate instanceof com.github.laxika.magicalvibes.model.filter.PermanentToughnessAtMostPredicate p) {
            return toughness <= p.maxToughness();
        }
        if (predicate instanceof com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate p) {
            return power >= p.minPower();
        }
        if (predicate instanceof com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostPredicate p) {
            return power <= p.maxPower();
        }
        return null;
    }

    public void checkPlotTriggers(GameData gameData, UUID plottingPlayerId, Card plottedCard) {
        // Each ability is one object of all its effects (CR 603.3).
        for (List<CardEffect> effects : ActionAbilityReader.printedEffectsByAbility(plottedCard,
                ActionWatch.ON_SELF_BECOMES_PLOTTED)) {
            boolean needsAnyTarget = effects.stream().anyMatch(effect ->
                    effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                            || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER));
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, plottedCard,
                    plottingPlayerId, plottedCard.getName() + "'s ability", new ArrayList<>(effects));
            gameData.handOffTriggerRecord(entry);
            if (needsAnyTarget) {
                gameData.queueInteraction(new PermanentChoiceContext.PlotTriggerAnyTarget(
                        plottedCard, plottingPlayerId, new ArrayList<>(effects)));
            } else {
                gameData.enqueueTrigger(entry);
            }
            gameLogService.append(gameData, GameLog.cardThen(plottedCard,
                    " became plotted — its ability triggers!"));
        }
    }

    public void processNextPlotTrigger(GameData gameData) {
        triggeredAbilityQueueService.processNextPlotTrigger(gameData);
    }

    public void checkBecomesCrewedTriggers(GameData gameData, Permanent crewedPermanent,
                                           UUID controllerId) {
        dispatchPrintedMiscAbilities(gameData, crewedPermanent, controllerId, MiscWatch.ON_SELF_BECOMES_CREWED,
                new TriggerContext.SelfBecomesCrewed(controllerId));
    }

    /** Collects abilities on a Spacecraft that trigger when a creature stations it. */
    public void checkStationedTriggers(GameData gameData, Permanent spacecraft,
                                       Permanent stationingCreature, UUID controllerId) {
        if (spacecraft == null || stationingCreature == null || controllerId == null) {
            return;
        }
        dispatchPrintedMiscAbilities(gameData, spacecraft, controllerId, MiscWatch.ON_SELF_BECOMES_STATIONED,
                new TriggerContext.CreatureStationed(stationingCreature.getCard()));
    }

    public void checkBecomesSaddledTriggers(GameData gameData, Permanent saddledPermanent,
                                             UUID controllerId) {
        dispatchPrintedMiscAbilities(gameData, saddledPermanent, controllerId, MiscWatch.ON_SELF_BECOMES_SADDLED,
                new TriggerContext.SelfBecomesSaddled(controllerId));
    }

    /**
     * Dispatches the printed abilities {@code watch} reads of {@code perm} to the watch's handlers, judging each one's
     * limit from the ability ({@link MiscAbilityReader}): "becomes crewed / saddled for the first time each turn"
     * (Mindlink Mech, Stubborn Burrowfiend), judged and marked by the unkeyed once-each-turn mark on the Vehicle or
     * Mount as before, and "triggers only once each turn", by the ability.
     */
    private void dispatchPrintedMiscAbilities(GameData gameData, Permanent perm, UUID controllerId, MiscWatch watch,
                                              TriggerContext context) {
        for (TriggeredAbility ability : MiscAbilityReader.printed(perm.getCard(), watch)) {
            List<MiscAbilityReader.MiscEffect> reads = MiscAbilityReader.read(ability, watch.trigger());
            MiscAbilityReader.MiscEffect first = reads.getFirst();
            if (first.limitReached(gameData, perm.getId())) {
                continue;
            }
            boolean triggered = registry.dispatch(new TriggerMatchContext(gameData, perm, controllerId,
                    reads.stream().map(MiscAbilityReader.MiscEffect::effect).toList()), watch, context);
            if (triggered) {
                first.markLimit(gameData, perm.getId());
            }
        }
    }

    /** Fires mutate triggers for all cards that currently make up the mutated permanent. */
    public void checkMutateTriggers(GameData gameData, Permanent mutatedPermanent,
                                    List<Card> mutatedCards, UUID controllerId) {
        mutatedPermanent.recordMutation();
        // Each ability of each card of the mutated permanent, whole (CR 603.3).
        TriggerContext context = new TriggerContext.SelfMutates(controllerId);
        for (Card mutatedCard : mutatedCards) {
            for (List<CardEffect> effects : actionAbilities(mutatedCard, ActionWatch.ON_SELF_MUTATES)) {
                registry.dispatch(new TriggerMatchContext(gameData, mutatedPermanent, controllerId, effects,
                        mutatedCard), ActionWatch.ON_SELF_MUTATES, context);
            }
        }

        TriggerContext creatureMutatesContext = new TriggerContext.CreatureMutates(mutatedPermanent, controllerId);
        for (Card mutatedCard : mutatedCards) {
            for (List<CardEffect> effects : actionAbilities(mutatedCard, ActionWatch.ON_ALLY_CREATURE_MUTATES)) {
                registry.dispatch(new TriggerMatchContext(gameData, mutatedPermanent, controllerId, effects,
                        mutatedCard), ActionWatch.ON_ALLY_CREATURE_MUTATES, creatureMutatesContext);
            }
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield != null) {
            for (Permanent watcher : List.copyOf(battlefield)) {
                if (watcher.getId().equals(mutatedPermanent.getId())) continue;
                for (List<CardEffect> effects : actionAbilities(watcher.getCard(),
                        ActionWatch.ON_ALLY_CREATURE_MUTATES)) {
                    registry.dispatch(new TriggerMatchContext(gameData, watcher, controllerId, effects),
                            ActionWatch.ON_ALLY_CREATURE_MUTATES, creatureMutatesContext);
                }
            }
        }
    }

    /** The effects of each of {@code abilities} that {@code watch} reads, one list per ability ({@link #byAbility}). */
    private static List<List<CardEffect>> actionAbilities(List<TriggeredAbility> abilities, ActionWatch watch) {
        return byAbility(abilities, read -> ActionAbilityReader.effects(read, watch.trigger()));
    }

    /** The effects of each of {@code abilities} that {@code watch} reads, one list per ability ({@link #byAbility}). */
    private static List<List<CardEffect>> tapAbilities(List<TriggeredAbility> abilities, TapWatch watch) {
        return byAbility(abilities, read -> TapAbilityReader.effects(read, watch.trigger()));
    }

    /** The effects of each printed ability of {@code card} that {@code watch} reads, one list per ability. */
    private static List<List<CardEffect>> tapAbilities(Card card, TapWatch watch) {
        return tapAbilities(TapAbilityReader.printed(card, watch), watch);
    }

    /** The effects of each printed ability of {@code card} that {@code watch} reads, one list per ability. */
    private static List<List<CardEffect>> actionAbilities(Card card, ActionWatch watch) {
        return actionAbilities(ActionAbilityReader.printed(card, watch), watch);
    }

    /**
     * "Whenever a source you control deals noncombat damage to a creature": the watchers are the source's controller's
     * permanents, the damaged permanent a creature. Each ability's event condition is judged as the damage is dealt,
     * and its intervening "if" by the door (CR 603.4: Crude Abattoir's "if the second door is unlocked"), with the
     * damage as the event's value and the creature as the triggering permanent (Taii Wakeen's "equal to that
     * creature's toughness"). The record names the creature ({@link #damagedPermanentRecord}).
     */
    public void checkAllySourceDealtNoncombatDamageToCreatureTriggers(
            GameData gameData, UUID sourceControllerId, Permanent damagedCreature, int damageDealt) {
        if (sourceControllerId == null || damagedCreature == null || damageDealt <= 0
                || !gameQueryService.isCreature(gameData, damagedCreature)) return;
        List<Permanent> battlefield = gameData.playerBattlefields.get(sourceControllerId);
        if (battlefield == null) return;

        TriggerContext context = new TriggerContext.SourceDealsNoncombatDamageToCreature(
                damagedCreature, damageDealt, sourceControllerId);
        UUID damagedControllerId = gameQueryService.findPermanentController(gameData, damagedCreature.getId());
        DamageWatch watch = DamageWatch.ON_ALLY_SOURCE_DEALS_NONCOMBAT_DAMAGE_TO_CREATURE;
        for (Permanent watcher : List.copyOf(battlefield)) {
            for (TriggeredAbility ability : printedWatchedAbilities(gameData, watcher, watch)) {
                requireNothingToJudge(ability, watch);
                putEventAbility(gameData, ability, watcher.getCard(), watcher, sourceControllerId,
                        ConditionContext.forPermanent(watcher, sourceControllerId)
                                .withTriggeringPermanentId(damagedCreature.getId()).withEventValue(damageDealt),
                        context, damagedPermanentRecord(gameData, watcher, damagedCreature, damagedControllerId,
                                damageDealt, false, false));
            }
        }
    }

    /** Throws when {@code ability} has a filter the collector of {@code watch} doesn't judge, or a multiplicity. */
    private static void requireNothingToJudge(TriggeredAbility ability, DamageWatch watch) {
        requireEachEvent(ability, watch);
        if (DamageAbilityReader.filter(ability, watch.trigger()) != null) {
            throw new IllegalStateException("The collector of " + watch + " judges no filter: " + ability);
        }
    }

    /**
     * Throws when {@code ability} triggers other than once for each event its collector reports ("once for each
     * source", CR 603.2c, which only the combat damage step's collector reads).
     */
    private static void requireEachEvent(TriggeredAbility ability, DamageWatch watch) {
        if (ability.options().multiplicity() != TriggerMultiplicity.EACH) {
            throw new IllegalStateException("The collector of " + watch + " reads no "
                    + ability.options().multiplicity() + ": " + ability);
        }
    }

}
