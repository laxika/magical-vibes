package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.BendingType;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DelayedSpellCastTrigger;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CreatureDeathTriggerWatcher;
import com.github.laxika.magicalvibes.model.TargetedCreatureDeathTriggerWatcher;
import com.github.laxika.magicalvibes.model.CreatureEntersTriggerWatcher;
import com.github.laxika.magicalvibes.model.DamagedCreatureDeathTriggerWatcher;
import com.github.laxika.magicalvibes.model.Boon;
import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.DelayedEffectOnDeath;
import com.github.laxika.magicalvibes.model.DelayedReturnOnDeath;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.rules.DepartedAttachments;
import com.github.laxika.magicalvibes.model.rules.PermanentChange;
import com.github.laxika.magicalvibes.model.rules.PermanentChangeEvent;
import com.github.laxika.magicalvibes.model.rules.RulesObserver;
import com.github.laxika.magicalvibes.model.rules.ZoneChangeCause;
import com.github.laxika.magicalvibes.model.rules.RulesEvent;
import com.github.laxika.magicalvibes.model.rules.RulesEventBatch;
import com.github.laxika.magicalvibes.model.rules.ZoneChangeEvent;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Trigger;
import com.github.laxika.magicalvibes.model.trigger.LibraryTrigger;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.trigger.ZoneChangeTrigger;
import com.github.laxika.magicalvibes.model.trigger.TriggerMultiplicity;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.rules.CounterChangeEvent;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.LegacySlots;
import com.github.laxika.magicalvibes.model.ManaCopyRider;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.OpeningHandRevealTrigger;
import com.github.laxika.magicalvibes.model.Permanent;
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
import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.model.SacrificeBoonWatcher;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.event.GameEventAudience;
import com.github.laxika.magicalvibes.model.event.VisualCueType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.AttachedCreatureBecomesCopyOfExiledCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseColorForSourceEffect;
import com.github.laxika.magicalvibes.model.effect.AsThisBecomesAttachedEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.AttachedCreatureBecomesCopyOfExiledCreatureEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.ChooseCardNameAtResolutionEffectHandler;
import com.github.laxika.magicalvibes.model.effect.CommanderCreaturesDealPowerDamageToPreventedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.filter.CardHasXInManaCostPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSpecificPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
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
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseTargetsAtRandomEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.condition.Condition;
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
import com.github.laxika.magicalvibes.service.trigger.pipeline.SpellTriggers;
import com.github.laxika.magicalvibes.model.rules.SpellEvent;
import com.github.laxika.magicalvibes.model.rules.TargetEvent;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityRequest.TriggerRecord;
import com.github.laxika.magicalvibes.service.effect.LayerSystemService;
import com.github.laxika.magicalvibes.service.effect.normalfx.TargetRedirectionSupport;
import com.github.laxika.magicalvibes.model.effect.ReturnTriggeringCardFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.TargetSpec;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.TriggeredAbilityQueueService;
import com.github.laxika.magicalvibes.service.battlefield.ETBTokenTargetService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.effect.GrantedTriggeredAbilitySupport;
import com.github.laxika.magicalvibes.service.effect.GraveyardTargetingSupport;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.state.StateTriggerService;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import com.github.laxika.magicalvibes.service.target.ValidTargetService;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
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

    /** A record that names nothing beyond what the request's own fields name. */
    private static final TriggerRecord NO_EXTRA_RECORD = entry -> {
    };

    /** Prowess: "Whenever you cast a noncreature spell, this creature gets +1/+1 until end of turn" (CR 702.108a). */
    private static final TriggeredAbility PROWESS_ABILITY = new TriggeredAbility(Triggers.youCastSpell(),
            new com.github.laxika.magicalvibes.model.effect.BoostSelfEffect(1, 1));

    /**
     * Increment: "Whenever you cast a spell, if the amount of mana you spent to cast it is greater than this creature's
     * power or toughness, put a +1/+1 counter on it."
     */
    private static final TriggeredAbility INCREMENT_ABILITY = new TriggeredAbility(Triggers.youCastSpell(),
            new IncrementTriggerEffect());

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
    private SpellTriggers spellTriggers;

    public List<CardEffect> grantedTriggeredEffects(GameData gameData, Permanent permanent, EffectSlot slot) {
        return grantedTriggeredAbilitySupport.grantedTriggeredEffects(gameData, permanent, slot);
    }

    /** {@link GrantedTriggeredAbilitySupport#grantedOwnEntryAbilities}. */
    public List<TriggeredAbility> grantedOwnEntryAbilities(GameData gameData, Permanent permanent) {
        return grantedTriggeredAbilitySupport.grantedOwnEntryAbilities(gameData, permanent);
    }

    /** {@link GrantedTriggeredAbilitySupport#grantedWatchedAbilities(GameData, Permanent, Trigger)}. */
    public List<TriggeredAbility> grantedWatchedAbilities(GameData gameData, Permanent permanent, Trigger watched) {
        return grantedTriggeredAbilitySupport.grantedWatchedAbilities(gameData, permanent, watched);
    }

    /** {@link GrantedTriggeredAbilitySupport#grantedZoneChangeAbilities}. */
    public List<TriggeredAbility> grantedZoneChangeAbilities(GameData gameData, Permanent permanent) {
        return grantedTriggeredAbilitySupport.grantedZoneChangeAbilities(gameData, permanent);
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

    /**
     * "When you gain control of this from another player" and "when you lose control of this" ({@code watch}): each
     * ability of {@code permanent}, its printed ones and those granted to it for a duration or until it leaves, handed
     * to the stack door under {@code controllerId}, whole (CR 603.3), with the permanent as it was.
     */
    public void checkSelfControlChangeTriggers(GameData gameData, Permanent permanent, UUID controllerId,
                                               MiscWatch watch) {
        List<TriggeredAbility> abilities = new ArrayList<>(printedWatchedAbilities(gameData, permanent, watch));
        abilities.addAll(WatchedAbilities.temporary(permanent, watch.trigger()));
        abilities.addAll(WatchedAbilities.persistent(permanent, watch.trigger()));
        for (TriggeredAbility ability : abilities) {
            putEventAbility(gameData, ability, permanent.getCard(), permanent, controllerId,
                    ConditionContext.forPermanent(permanent, controllerId), null,
                    eventRecord(permanent, null, null, null, null, null));
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

            StackEntry entry = TriggeredAbilityStackService.madeAbility(delayed.sourceCard(), delayed.controllerId(),
                    delayed.sourceCard().getName() + "'s delayed trigger", delayed.resolvedEffects(),
                    (UUID) null, delayed.sourcePermanentId());
            entry.setTriggeringCardId(spellCard.getId());
            entry.setTriggeringSpell(spellEntry);
            if (spellEntry != null) {
                entry.setXValue(spellEntry.getXValue());
            }
            entry.setEventValue(spellManaValue(gameData, spellCard));
            entry.setSourcePermanentSnapshot(delayed.sourcePermanentSnapshot());
            entry.setNonTargeting(true);

            if (delayed.targetFilter() != null) {
                // Its target, restricted by the registration's filter, is chosen as it goes on the stack (CR 603.3d).
                entry.setTargetFilter(delayed.targetFilter());
                triggeredAbilityStackService.putMade(gameData, entry);
                gameLogService.append(gameData, GameLog.cardTextCard(
                        delayed.sourceCard(), "'s delayed trigger fires for ", spellCard, "."));
                log.info("Game {} - {} delayed spell-cast trigger awaits a target for {}",
                        gameData.id, delayed.sourceCard().getName(), spellCard.getName());
                continue;
            }

            triggeredAbilityStackService.putDelayed(gameData, entry);
            gameLogService.append(gameData, GameLog.cardTextCard(
                    delayed.sourceCard(), "'s delayed trigger fires for ", spellCard, "."));
            log.info("Game {} - {} delayed spell-cast trigger fires for {}",
                    gameData.id, delayed.sourceCard().getName(), spellCard.getName());
        }
    }

    /**
     * Uses up the loyalty-ability half of the delayed ability {@code link} names, whose instant-or-sorcery half just
     * triggered ("copy the next instant or sorcery spell you cast or loyalty ability you activate this turn").
     */
    private static void removeLinkedLoyaltyCopies(GameData gameData, UUID playerId, UUID link) {
        List<GameData.PendingCopySource> sources = gameData.pendingNextLoyaltyAbilityCopySourcesThisTurn.get(playerId);
        if (sources == null) {
            return;
        }
        int before = sources.size();
        sources.removeIf(source -> link.equals(source.link()));
        int removed = before - sources.size();
        if (removed > 0) {
            gameData.pendingNextLoyaltyAbilityCopyThisTurnCount.computeIfPresent(playerId,
                    (ignored, count) -> count - removed > 0 ? count - removed : null);
        }
        if (sources.isEmpty()) {
            gameData.pendingNextLoyaltyAbilityCopySourcesThisTurn.remove(playerId);
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
                putGraveyardFamilyAbilities(gameData, card, committingPlayerId,
                        ActionWatch.GRAVEYARD_ON_CONTROLLER_COMMITS_CRIME, ctx, TriggerRecord.NONE);
            }
        }

    }

    /**
     * Queues the "whenever you fully unlock a Room" abilities of {@code card} in its owner's graveyard (Fear of
     * Infinity), each ability as one object of its effects (CR 603.3); their collector judges no door, first time or
     * limit.
     */
    private void queueGraveyardEerieTriggers(GameData gameData, UUID controllerId, Card card) {
        putGraveyardFamilyAbilities(gameData, card, controllerId, MiscWatch.GRAVEYARD_ON_ALLY_ROOM_FULLY_UNLOCKED,
                null, TriggerRecord.NONE);
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
        collectCreatureSpellBoonTriggers(gameData, spellCard, castingPlayerId, castFaceDown);
        if (!castFaceDown && !spellCard.hasType(CardType.CREATURE)) {
            for (Permanent source : gameData.playerBattlefields.getOrDefault(castingPlayerId, List.of())) {
                int prowessInstances = gameQueryService.triggerKeywordInstances(gameData, source, Keyword.PROWESS);
                // Each instance of prowess triggers on its own (CR 702.108a); the door adds the times it triggers
                // additionally.
                for (int instance = 0; instance < prowessInstances; instance++) {
                    Permanent snapshot = new Permanent(source);
                    triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(PROWESS_ABILITY,
                            source.getCard(), source, castingPlayerId,
                            source.getCard().getName() + "'s prowess ability",
                            ConditionContext.forPermanent(source, castingPlayerId),
                            entry -> entry.setSourcePermanentSnapshot(snapshot)));
                }
            }
        }
        if (castZone != Zone.HAND) {
            gameData.recordSpellCastFromOutsideHand(castingPlayerId);
            gameData.playersWhoPlayedOrCastFromOutsideHandThisTurn.add(castingPlayerId);
        }

        // "The first spell you cast from anywhere other than your hand each turn has demonstrate" (The Twelfth Doctor):
        // demonstrate is the spell's triggered ability (CR 702.144a), whose "copy it" reads the spell it records.
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
                        putSpellKeywordAbility(gameData, spellCard, castingPlayerId, grantEffect, entry -> {
                            entry.setTriggeringCardId(spellCard.getId());
                            entry.setNonTargeting(true);
                        });
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
                // A delayed triggered ability of the revealed card (CR 603.7): "counter that spell unless that player
                // pays" acts on the spell, its triggering spell, without targeting it.
                StackEntry entry = TriggeredAbilityStackService.madeAbility(trigger.sourceCard(),
                        trigger.revealingPlayerId(), trigger.sourceCard().getName() + "'s ability",
                        List.of(trigger.effect()), spellCard.getId(), Zone.STACK);
                entry.setTriggeringCardId(spellCard.getId());
                entry.setTriggeringSpell(firstSpell);
                entry.setTriggeringPlayerId(castingPlayerId);
                entry.setNonTargeting(true);
                triggeredAbilityStackService.putDelayed(gameData, entry);
            }
        }

        // The spell became cast (CR 601.2i): the abilities that trigger on it are the rules event's.
        SpellEvent.Cast castEvent = new SpellEvent.Cast(spellCard, castingPlayerId, castZone, exiledSourcePermanentId);
        gameData.rulesEvents.separately(() -> gameData.rulesEvents.emit(castEvent));

        processDelayedControllerSpellCastTriggers(gameData, spellCard, castingPlayerId);

        // Increment keyword (CR keyword): "Whenever you cast a spell, if the amount of mana you spent
        // is greater than this creature's power or toughness, put a +1/+1 counter on it." Driven by the
        // Scryfall-loaded keyword (like Undying) rather than a per-card effect. Read before mana-spent
        // is cleared below.
        collectIncrementTriggers(gameData, spellCard, castingPlayerId);

        int pathOfAncestryScries = gameData.getSpellCastPathOfAncestryManaSpent(spellCard.getId());
        gameData.clearSpellCastPathOfAncestryManaSpent(spellCard.getId());
        for (int i = 0; i < pathOfAncestryScries; i++) {
            StackEntry entry = TriggeredAbilityStackService.madeAbility(spellCard,
                    castingPlayerId,
                    "Path of Ancestry's ability",
                    new ArrayList<>(List.of(new ScryEffect(1)))
            );
            entry.setTriggeringCardId(spellCard.getId());
            entry.setNonTargeting(true);
            triggeredAbilityStackService.putDelayed(gameData, entry);
        }
        if (pathOfAncestryScries > 0) {
            gameLogService.append(gameData, GameLog.text("Path of Ancestry triggers."));
        }

        boolean castUsingTreasureMana = gameData.spellCastUsedTreasureMana(spellCard.getId());
        gameData.clearSpellCastManaSpent(spellCard.getId());
        gameData.clearSpellCastManaSources(spellCard.getId());
        gameData.clearSpellCastTreasureMana(spellCard.getId());
        gameData.clearSpellCastCreatureManaSpent(spellCard.getId());

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
                        int manaValue = spellManaValue(gameData, spellCard);
                        putSpellKeywordAbility(gameData, spellCard, castingPlayerId, grantEffect, entry -> {
                            entry.setTriggeringCardId(spellCard.getId());
                            entry.setEventValue(manaValue);
                        });
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
            StackEntry cascadeTrigger = TriggeredAbilityStackService.madeAbility(spellCard,
                    castingPlayerId,
                    spellCard.getName() + "'s ability",
                    new ArrayList<>(List.of(new CascadeEffect())));
            cascadeTrigger.setTriggeringCardId(spellCard.getId());
            cascadeTrigger.setEventValue(spellManaValue(gameData, spellCard));
            triggeredAbilityStackService.putDelayed(gameData, cascadeTrigger);
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
            StackEntry delayedTrigger = TriggeredAbilityStackService.madeAbility(delayed.source(),
                    castingPlayerId,
                    delayed.source().getName() + "'s ability",
                    new ArrayList<>(List.of(delayed.effect())));
            delayedTrigger.setTriggeringCardId(spellCard.getId());
            triggeredAbilityStackService.putDelayed(gameData, delayedTrigger);
            gameLogService.append(gameData, GameLog.abilityTriggers(delayed.source()));
            log.info("Game {} - delayed cast trigger of {} queued for {}",
                    gameData.id, delayed.source().getName(), spellCard.getName());
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

        // "When you next cast an instant or sorcery spell this turn, copy that spell" (Chandra, the Firebrand's −2,
        // Teach by Example, Sea Gate Stormcaller): each pending delayed ability that applies to the spell triggers
        // and is used up, one stack object each (CR 603.7), with the object that made it as its source (CR 603.7e).
        List<GameData.PendingSpellCopy> pendingCopies = gameData.pendingNextInstantSorceryCopies.get(castingPlayerId);
        if (pendingCopies != null && !pendingCopies.isEmpty()
                && (spellCard.hasType(CardType.INSTANT) || spellCard.hasType(CardType.SORCERY))) {
            StackEntry spellEntry = null;
            for (StackEntry se : gameData.stack) {
                if (se.getTargetableId().equals(spellCard.getId())) {
                    spellEntry = se;
                    break;
                }
            }
            if (spellEntry != null) {
                int manaValue = spellCard.getManaValue();
                if (spellCard.getParsedManaCost() != null) {
                    manaValue += spellEntry.getXValue() * spellCard.getParsedManaCost().getXSymbolCount();
                }
                List<GameData.PendingSpellCopy> triggered = new ArrayList<>();
                synchronized (pendingCopies) {
                    for (GameData.PendingSpellCopy pending : List.copyOf(pendingCopies)) {
                        int limitsMet = 0;
                        for (int limit : pending.maxManaValues()) {
                            if (manaValue <= limit) {
                                limitsMet++;
                            }
                        }
                        if (!pending.maxManaValues().isEmpty() && limitsMet == 0) {
                            continue;
                        }
                        pendingCopies.remove(pending);
                        triggered.add(pending.maxManaValues().isEmpty() ? pending
                                : new GameData.PendingSpellCopy(pending.sourceCard(), pending.sourcePermanentId(),
                                limitsMet, null, List.of(), pending.resolution(), pending.link()));
                    }
                }
                StackEntry snapshot = new StackEntry(spellEntry);
                for (GameData.PendingSpellCopy copy : triggered) {
                    CopyControllerCastSpellEffect copyEffect = copy.dynamicCopies() != null
                            // How many copies it makes is counted as it resolves.
                            ? new CopyControllerCastSpellEffect(new StackEntry(spellEntry), castingPlayerId,
                            copy.dynamicCopies())
                            : new CopyControllerCastSpellEffect(snapshot, castingPlayerId, copy.copies());
                    triggeredAbilityStackService.putDelayed(gameData, TriggeredAbilityStackService.madeAbility(
                            copy.sourceCard() != null ? copy.sourceCard() : spellCard, castingPlayerId,
                            "Copy " + spellCard.getName(), List.of(copyEffect), 0, copy.sourcePermanentId()));
                    if (copy.link() != null) {
                        removeLinkedLoyaltyCopies(gameData, castingPlayerId, copy.link());
                    }
                }
                if (!triggered.isEmpty()) {
                    gameLogService.append(gameData, GameLog.cardThen(spellCard, " is copied."));
                    log.info("Game {} - {} delayed spell-copy trigger(s) queued for {}",
                            gameData.id, triggered.size(), spellCard.getName());
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
                    queueGrantedStorm(gameData, spellCard, castingPlayerId, spellEntry,
                            CopyTriggeringSpellEffect.storm());
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
                    triggeredAbilityStackService.putDelayed(gameData, TriggeredAbilityStackService.madeAbility(spellCard,
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
                Card spellOnStack = SpellTriggers.spellOnStackWithChosenX(
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
                        triggeredAbilityStackService.putDelayed(gameData, TriggeredAbilityStackService.madeAbility(spellCard,
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
                    triggeredAbilityStackService.putDelayed(gameData, TriggeredAbilityStackService.madeAbility(spellCard,
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
                    // Conspire's "when you cast this spell, if its conspire cost was paid, copy it" is the spell's
                    // triggered ability (CR 702.78a).
                    triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(
                            new TriggeredAbility(Triggers.youCastThisSpell(), copyEffect), spellCard, null,
                            castingPlayerId, "Copy " + spellCard.getName() + " (Conspire)",
                            ConditionContext.forCard(spellCard, castingPlayerId), NO_EXTRA_RECORD));
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
                    // Casualty's "when you cast this spell, if a casualty cost was paid for it, copy it" is the
                    // spell's triggered ability (CR 702.153a).
                    triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(
                            new TriggeredAbility(Triggers.youCastThisSpell(), copyEffect), spellCard, null,
                            castingPlayerId, "Copy " + spellCard.getName() + " (Casualty)",
                            ConditionContext.forCard(spellCard, castingPlayerId), NO_EXTRA_RECORD));
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
                    .filter(replicateCost -> spellCard.getTriggerEffects(Triggers.youCastThisSpell(), Zone.STACK).stream()
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
                    triggeredAbilityStackService.putDelayed(gameData, TriggeredAbilityStackService.madeAbility(spellCard,
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
        collectSelfCastAbilities(gameData, spellCard, castingPlayerId, castZone, selfCastSpellEntry, selfCastContext,
                selfCastAbilities);

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
                        int manaValue = spellManaValue(gameData, spellCard);
                        putSpellKeywordAbility(gameData, spellCard, castingPlayerId, grantEffect, entry -> {
                            entry.setTriggeringCardId(spellCard.getId());
                            entry.setEventValue(manaValue);
                        });
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
                        int manaValue = spellManaValue(gameData, spellCard);
                        putSpellKeywordAbility(gameData, spellCard, castingPlayerId, grantEffect, entry -> {
                            entry.setTriggeringCardId(spellCard.getId());
                            entry.setEventValue(manaValue);
                        });
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
                        StackEntry grantedCascadeSpell = findStackEntryByCardId(gameData, spellCard.getId());
                        int xValue = grantedCascadeSpell == null ? 0 : grantedCascadeSpell.getXValue();
                        putSpellKeywordAbility(gameData, spellCard, castingPlayerId, grantEffect,
                                entry -> entry.setXValue(xValue));
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
                        StackEntry grantedCascadeSpell = findStackEntryByCardId(gameData, spellCard.getId());
                        int xValue = grantedCascadeSpell == null ? 0 : grantedCascadeSpell.getXValue();
                        putSpellKeywordAbility(gameData, spellCard, castingPlayerId, grantEffect,
                                entry -> entry.setXValue(xValue));
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
                        StackEntry grantedCascadeSpell = findStackEntryByCardId(gameData, spellCard.getId());
                        int xValue = grantedCascadeSpell == null ? 0 : grantedCascadeSpell.getXValue();
                        putSpellKeywordAbility(gameData, spellCard, castingPlayerId, grantEffect,
                                entry -> entry.setXValue(xValue));
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
                        queueGrantedStorm(gameData, spellCard, castingPlayerId, spellEntry, grantedStorm);
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
     * Hands the storm an effect gave {@code spellCard}, the spell just cast ({@code spellEntry}), to the stack door:
     * storm is the spell's triggered ability (CR 702.40a), the granted {@code effect} as written, with that spell as its
     * triggering spell and the spell's places in the turn's cast order, from which its copy count is read as it
     * resolves.
     */
    private void queueGrantedStorm(GameData gameData, Card spellCard, UUID controllerId, StackEntry spellEntry,
                                   CardEffect effect) {
        putSpellKeywordAbility(gameData, spellCard, controllerId, effect,
                entry -> SpellTriggers.recordTriggeringSpell(gameData, entry, spellEntry));
        log.info("Game {} - granted Storm trigger queued for {}", gameData.id, spellEntry.getCard().getName());
    }

    /**
     * Hands {@code effect}, a keyword triggered ability of {@code spellCard} that a static ability or an effect gave the
     * spell just cast ("when you cast this spell": cascade, storm, demonstrate), to the stack door as the spell's own
     * ability (CR 702.85a, 702.40a, 702.144a), with no source permanent, stamped with {@code record}.
     */
    private void putSpellKeywordAbility(GameData gameData, Card spellCard, UUID controllerId, CardEffect effect,
                                        TriggerRecord record) {
        triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(
                new TriggeredAbility(Triggers.youCastThisSpell(), effect), spellCard, null, controllerId,
                spellCard.getName() + "'s ability", ConditionContext.forCard(spellCard, controllerId), record));
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
     * Hands {@code spellCard}'s keyword "when you cast this spell" abilities that a grant gave it to the stack door, each
     * one object (CR 603.3), {@code abilities} the effects of each: a granted replicate (CR 702.56a, its intervening "if"
     * wrapped around its copy) and demonstrate (CR 702.144a). Each is the spell's own ability, recorded as
     * {@link SpellTriggers#putSelfCastAbility} records one; a "you may" is asked as it resolves (CR 603.5).
     */
    private void collectSelfCastAbilities(GameData gameData, Card spellCard, UUID castingPlayerId, Zone castZone,
                                          StackEntry spellEntry, ConditionContext context,
                                          List<List<CardEffect>> abilities) {
        for (List<CardEffect> effects : abilities) {
            if (effects.isEmpty()) {
                continue;
            }
            spellTriggers.putSelfCastAbility(gameData, spellCard, castingPlayerId, castZone, spellEntry,
                    new TriggeredAbility(Triggers.youCastThisSpell(), effects.toArray(CardEffect[]::new)), context);
        }
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

    private static void requireNoLimit(TriggeredAbility ability, Card card) {
        if (ability.options().limit() != TriggerLimit.NONE) {
            throw new IllegalStateException(card.getName() + "'s triggered ability has a limit its reader doesn't "
                    + "judge: " + ability.options());
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

            for (Permanent perm : List.copyOf(battlefield)) {
                LibraryWatch watch = LibraryWatch.ON_OPPONENT_DISCARDS;
                for (TriggeredAbility ability : watchedAbilities(gameData, perm, watch)) {
                    LibraryAbilityReader.LibraryEffect first = LibraryAbilityReader.read(ability, watch.trigger())
                            .getFirst();
                    LibraryAbilityReader.requireWatchedEvent(first, watch.trigger());
                    LibraryAbilityReader.requireNoCount(first, watch.trigger());
                    // "Whenever an opponent discards a creature/land/noncreature-nonland card, …"
                    // (Waste Not) — the discarded card gates the trigger.
                    if (!discardedCardMatches(gameData, first, discardedCard, characteristicsKnown, playerId)) {
                        continue;
                    }
                    if (putJudgedFamilyAbility(gameData, perm, perm.getCard(), playerId, watch, ctx, ability)) {
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

        // The discarded card's own "when you discard this card" and "when a spell or ability an opponent controls
        // causes you to discard this card" abilities, each whole (CR 603.3), from the graveyard it went to: the record
        // names this card there as its triggering card and its source (CR 400.7).
        if (discardedCard != null) {
            putGraveyardFamilyAbilities(gameData, discardedCard, discardingPlayerId, LibraryWatch.ON_SELF_DISCARDED,
                    ctx, TriggerRecord.NONE);
            if (gameData.discardCausedByOpponent) {
                long stay = gameData.graveyardEntryVersion(discardedCard.getId());
                putGraveyardFamilyAbilities(gameData, discardedCard, discardingPlayerId,
                        LibraryWatch.ON_SELF_DISCARDED_BY_OPPONENT, ctx,
                        entry -> entry.setTriggeringCard(discardedCard.getId(), stay));
            }
        }
    }

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
        // Each "when you cycle this card" ability is one object of all its effects (CR 603.3), handed to the stack door
        // with the X paid for the cycling cost as its X; its targets, a spell's among them, are chosen as it goes on the
        // stack (CR 603.3d), but for the targets a cycling ability that announces this ability's targets announced
        // (Rampaging War Mammoth).
        for (TriggeredAbility ability : LibraryAbilityReader.printed(cycledCard, LibraryWatch.ON_SELF_CYCLED)) {
            List<UUID> announcedTargets = announcedTargetIds != null ? announcedTargetIds : List.of();
            boolean chooseOnTrigger = targetingCyclingAbility != null && announcedTargets.isEmpty()
                    && targetingCyclingAbility.isCyclingTriggerTargetsChosenOnTriggerIfOmitted();
            ConditionContext context = ConditionContext.forCard(cycledCard, cyclingPlayerId);
            if (targetingCyclingAbility == null || chooseOnTrigger) {
                putEventAbility(gameData, ability, cycledCard, null, cyclingPlayerId, context, null, entry -> {
                    entry.setNonTargeting(true);
                    entry.setXValue(cyclingXValue);
                });
                continue;
            }
            if (announcedTargets.isEmpty() && !targetingCyclingAbility.isCyclingTriggerWithoutTargets()
                    || !conditionEvaluationService.eventConditionMet(gameData, ability.options(), context)) {
                continue;
            }
            boolean triggered = triggeredAbilityStackService.putWithAnnouncedTargets(gameData,
                    new TriggeredAbilityRequest(ability, cycledCard, null, cyclingPlayerId,
                            cycledCard.getName() + "'s ability", context, entry -> {
                                entry.setXValue(cyclingXValue);
                                if (announcedTargets.isEmpty()) {
                                    entry.setNonTargeting(true);
                                    return;
                                }
                                entry.chooseTargetIds(announcedTargets);
                                if (announcedTargets.size() == 1) {
                                    entry.setTargetId(announcedTargets.getFirst());
                                }
                                entry.setTargetFilter(targetingCyclingAbility.getTargetFilter());
                                entry.setMultiTargetConstraint(targetingCyclingAbility.getMultiTargetConstraint());
                            }));
            if (triggered) {
                gameLogService.append(gameData, GameLog.abilityTriggers(cycledCard));
            }
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

    /**
     * Hands the battlefield's "whenever you search your library" abilities of {@code searchingPlayerId}'s permanents and
     * "whenever an opponent searches their library" abilities of the other players' permanents to the stack door for
     * that player's search ({@code LibraryEvent}); an opponent's search names the searching player as the
     * non-targeting "that player".
     */
    public void checkLibrarySearchTriggers(GameData gameData, UUID searchingPlayerId) {
        gameData.forEachBattlefield((controllerId, battlefield) -> {
            for (Permanent perm : List.copyOf(battlefield)) {
                if (perm.isLosesAllAbilitiesUntilEndOfTurn()) continue;
                boolean ownSearch = controllerId.equals(searchingPlayerId);
                LibraryWatch watch = ownSearch ? LibraryWatch.ON_CONTROLLER_SEARCHES_LIBRARY
                        : LibraryWatch.ON_OPPONENT_SEARCHES_LIBRARY;
                for (TriggeredAbility ability : LibraryAbilityReader.printed(perm.getCard(), watch)) {
                    putEventAbility(gameData, ability, perm.getCard(), perm, controllerId,
                            ConditionContext.forPermanent(perm, controllerId), null, entry -> {
                                entry.setNonTargeting(true);
                                if (!ownSearch) {
                                    entry.setTargetId(searchingPlayerId);
                                }
                            });
                }
            }
        });
    }

    /**
     * Hands the battlefield's shuffle abilities to the stack door for the shuffle of {@code shufflingPlayerId}'s
     * library ({@code LibraryEvent}): "whenever a player shuffles their library", an opponent's permanents' "whenever an
     * opponent shuffles their library", and, when a spell or ability {@code causingControllerId} controls made its
     * controller shuffle, "whenever a spell or ability causes its controller to shuffle their library". Each names the
     * shuffling player as the non-targeting "that player", with its source as it was when it triggered.
     */
    public void checkLibraryShuffleTriggers(GameData gameData, UUID shufflingPlayerId, UUID causingControllerId) {
        boolean causedByOwnSpellOrAbility = shufflingPlayerId.equals(causingControllerId);
        gameData.forEachPermanent((controllerId, perm) -> {
            // Each ability is one object of all its effects (CR 603.3).
            List<TriggeredAbility> abilities = new ArrayList<>(LibraryAbilityReader.printed(perm.getCard(),
                    LibraryWatch.ON_ANY_PLAYER_SHUFFLES_LIBRARY));
            if (!controllerId.equals(shufflingPlayerId)) {
                abilities.addAll(LibraryAbilityReader.printed(perm.getCard(), LibraryWatch.ON_OPPONENT_SHUFFLES_LIBRARY));
            }
            if (causedByOwnSpellOrAbility) {
                abilities.addAll(LibraryAbilityReader.printed(perm.getCard(),
                        LibraryWatch.ON_SPELL_OR_ABILITY_CAUSES_CONTROLLER_SHUFFLE));
            }
            for (TriggeredAbility ability : abilities) {
                ConditionContext context = ConditionContext.forPermanent(perm, controllerId);
                if (!conditionEvaluationService.eventConditionMet(gameData, ability.options(), context)) {
                    continue;
                }
                Permanent snapshot = new Permanent(perm);
                triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(ability, perm.getCard(), perm,
                        controllerId, perm.getCard().getName() + "'s ability", context, entry -> {
                            entry.setTargetId(shufflingPlayerId);
                            entry.setNonTargeting(true);
                            entry.setSourcePermanentSnapshot(snapshot);
                        }));
            }
        });
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

            StackEntry entry = TriggeredAbilityStackService.madeAbility(watcher.sourceCard(), watcher.controllerId(),
                    watcher.sourceCard().getName() + "'s ability", temporaryEffects(watcher));
            entry.setTargetId(discardingPlayerId);
            entry.setNonTargeting(true);
            if (discardedCard != null) {
                entry.setEventCardIds(List.of(discardedCard.getId()));
            }
            triggeredAbilityStackService.putDelayed(gameData, entry);
            gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
            log.info("Game {} - {} temporary global discard trigger fires", gameData.id,
                    watcher.sourceCard().getName());
            triggered = true;
        }
        return triggered;
    }

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
            PlanarObject planarSource = object.copy();
            for (TriggeredAbility ability : ActionAbilityReader.printed(object.getCard(),
                    ActionWatch.ON_CONTROLLER_ROLLS_ONE_OR_MORE_DICE)) {
                putEventAbility(gameData, ability, object.getCard(), null, planarControllerId,
                        ConditionContext.forCard(object.getCard(), planarControllerId).withEventValue(result), context,
                        entry -> {
                            entry.setNonTargeting(true);
                            entry.setEventValue(result);
                            entry.setSourcePlanarObject(planarSource);
                        });
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
            putGraveyardFamilyAbilities(gameData, card, rollingPlayerId,
                    ActionWatch.GRAVEYARD_ON_CONTROLLER_ROLLS_NATURAL_20, null, TriggerRecord.NONE);
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

        for (Card card : List.copyOf(graveyard)) {
            putGraveyardFamilyAbilities(gameData, card, surveilingPlayerId,
                    LibraryWatch.GRAVEYARD_ON_CONTROLLER_SURVEILS, ctx, familyEventRecord(gameData, surveilingPlayerId,
                            ctx));
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
            putGraveyardFamilyAbilities(gameData, card, completingPlayerId,
                    ActionWatch.GRAVEYARD_ON_CONTROLLER_COMPLETES_DUNGEON, null, TriggerRecord.NONE);
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
        TriggerContext conjure = new TriggerContext.Conjure(conjuringPlayerId, cardCount);
        for (Card card : List.copyOf(graveyard)) {
            putGraveyardFamilyAbilities(gameData, card, conjuringPlayerId, ActionWatch.GRAVEYARD_ON_CONTROLLER_CONJURES,
                    conjure, familyEventRecord(gameData, conjuringPlayerId, conjure));
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

            triggeredAbilityStackService.putDelayed(gameData, TriggeredAbilityStackService.madeAbility(watcher.sourceCard(),
                    watcher.controllerId(),
                    watcher.sourceCard().getName() + "'s ability",
                    new ArrayList<>(List.of(watcher.effect()))));
            gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
            log.info("Game {} - {} sacrifice boon triggers ({} use(s) remain)", gameData.id,
                    watcher.sourceCard().getName(), remainingUses);
        }
    }

    /**
     * The request {@code candidate}'s "whenever [another object] …" ability of {@code watch} (or, with {@code watch}
     * {@code null}, a "whenever [a creature] dies" ability) hands the stack door for {@code events}, the zone changes of
     * one batch that met its trigger condition: one for an ability that triggers for each, all of them for "one or
     * more" (CR 603.2c). The trigger pipeline has already judged its relation, its filter, the zone it functions from
     * and the look-back of the event (CR 603.10a); this judges what the watch adds ({@link #zoneChangeEventMatches})
     * and records the event whatever the effects are ({@link #zoneChangeRecord}). {@code null} when the event isn't the
     * ability's.
     */
    public TriggeredAbilityRequest zoneChangeRequest(GameData gameData, TriggerCandidate candidate,
                                                     ZoneChangeWatch watch, CreatureDeathWatch deathWatch,
                                                     List<ZoneChangeEvent> events, ConditionContext ifContext) {
        if (watch != null && !zoneChangeEventMatches(gameData, watch, candidate, events)) {
            return null;
        }
        ZoneChangeEvent event = events.getFirst();
        Card sourceCard = candidate.sourceCard();
        Permanent source = candidate.sourcePermanent() == null ? null : namedSourcePermanent(candidate);
        TriggeredAbilityRequest request = new TriggeredAbilityRequest(watchedAbility(gameData, candidate), sourceCard,
                source, candidate.controllerId(), sourceCard.getName() + "'s ability", ifContext,
                zoneChangeRecord(gameData, candidate, source, events, thatPlayer(gameData, watch, deathWatch, event),
                        watch == null && candidate.ability().options().multiplicity()
                                == TriggerMultiplicity.ONE_OR_MORE,
                        watch == ZoneChangeWatch.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD));
        if (event.enteredBattlefield() && event.permanent() != null && !createsTokens(candidate)) {
            Permanent entered = gameQueryService.findPermanentById(gameData, event.permanent().getId());
            request = request.withEvent(new TriggerContext.PermanentEnters(
                    entered != null ? entered.getCard() : event.permanent().getCard(), event.controllerId(), null, 1,
                    event.permanent().getId()));
        }
        if (event.died() && event.permanent() != null) {
            CreatureDeath death = CreatureDeath.of(event);
            boolean creature = event.permanent().getCard().hasType(CardType.CREATURE);
            request = request.withEvent(new TriggerContext.CreatureDeath(event.permanent().getCard(),
                    event.controllerId(), death.power(), death.toughness(), event.permanent().getId(),
                    event.permanent(), creature));
            if (creature && !triggersOnSacrifice(candidate)) {
                request = request.withCreatureDying();
            }
        }
        return request;
    }

    /**
     * The request {@code candidate}'s ability of {@code watch} hands the stack door for {@code events}, permanents
     * phasing, transforming or being turned face up in one batch: the permanent that changed as "it" and "that
     * creature", its controller, and the X a face-down permanent was turned up for.
     */
    public TriggeredAbilityRequest permanentChangeRequest(GameData gameData, TriggerCandidate candidate,
                                                          ZoneChangeWatch watch, List<PermanentChangeEvent> events,
                                                          ConditionContext ifContext) {
        PermanentChangeEvent event = events.getFirst();
        Permanent changed = event.change() == PermanentChange.PHASED_OUT ? event.permanent()
                : Objects.requireNonNullElse(gameQueryService.findPermanentById(gameData, event.permanent().getId()),
                event.permanent());
        Card sourceCard = candidate.sourceCard();
        Permanent source = candidate.sourcePermanent() == null ? null : namedSourcePermanent(candidate);
        Permanent snapshot = source == null ? null : new Permanent(source);
        long graveyardStay = candidate.zone() == Zone.GRAVEYARD ? gameData.graveyardEntryVersion(sourceCard.getId())
                : -1;
        // "Untap that creature", "it": the permanent the event was about, unless it is the source itself.
        UUID thatPermanentId = watch == ZoneChangeWatch.ON_TURNED_FACE_UP
                || watch == ZoneChangeWatch.ON_TRANSFORM_TO_BACK_FACE
                || watch == ZoneChangeWatch.ON_TRANSFORM_TO_FRONT_FACE
                || watch == ZoneChangeWatch.ON_SELF_PHASES_IN || watch == ZoneChangeWatch.ON_SELF_PHASES_OUT
                ? null : changed.getId();
        List<UUID> changedIds = events.stream().map(changeEvent -> changeEvent.permanent().getId()).toList();
        TriggerRecord record = entry -> {
            entry.setNonTargeting(true);
            if (snapshot != null) {
                entry.setSourcePermanentSnapshot(snapshot);
            }
            if (graveyardStay >= 0) {
                entry.setSourceGraveyardEntryVersion(graveyardStay);
            }
            entry.setTriggeringPermanentId(changed.getId());
            entry.setTriggeringPermanentIds(changedIds);
            entry.setTriggeringPermanentControllerId(event.controllerId());
            entry.rememberLastKnownPermanentCard(changed.getId(), changed.getCard());
            if (thatPermanentId != null) {
                entry.setTargetId(thatPermanentId);
            }
            if (watch == ZoneChangeWatch.ON_TURNED_FACE_UP) {
                entry.setXValue(event.xValue());
            }
        };
        TriggeredAbilityRequest request = new TriggeredAbilityRequest(watchedAbility(gameData, candidate), sourceCard,
                source, candidate.controllerId(), sourceCard.getName() + "'s ability", ifContext, record);
        // A permanent being turned face up is the event the "triggers an additional time" effects that count only
        // that event judge.
        return event.change() == PermanentChange.TURNED_FACE_UP
                ? request.withEvent(new TriggerContext.PermanentTurnsFaceUp(changed, event.controllerId()))
                : request;
    }

    /**
     * {@code candidate}'s ability with its effects read through its source's text changes (layer 3, CR 612.1), as the
     * old slot collectors dispatched them.
     */
    private TriggeredAbility watchedAbility(GameData gameData, TriggerCandidate candidate) {
        return candidate.sourcePermanent() == null ? candidate.ability()
                : textChanged(gameData, candidate.sourcePermanent(), candidate.ability());
    }

    /** Whether {@code candidate}'s ability triggers on tokens being created, not on permanents entering. */
    private static boolean createsTokens(TriggerCandidate candidate) {
        return candidate.ability().conditions().stream().anyMatch(condition ->
                condition.trigger() instanceof ZoneChangeTrigger trigger
                        && trigger.causes().contains(ZoneChangeCause.CREATE));
    }

    /**
     * Whether {@code candidate}'s ability triggers on a sacrifice ("whenever you sacrifice a creature"): a creature
     * dying doesn't cause it, so "a creature dying causes an ability to trigger an additional time" effects don't
     * apply to it (Teysa Karlov's ruling).
     */
    private static boolean triggersOnSacrifice(TriggerCandidate candidate) {
        return candidate.ability().conditions().stream().anyMatch(condition ->
                condition.trigger() instanceof ZoneChangeTrigger move
                        && move.causes().equals(Set.of(ZoneChangeCause.SACRIFICE)));
    }

    /**
     * Whether the zone changes {@code events} are {@code candidate}'s ability's event as the collector of
     * {@code watch} reads it, beyond what the trigger pipeline judged: a source left out of its own move, an entering
     * permanent's origin, the commander, "for the first time each turn", and the abilities that permanents entering
     * don't trigger while a static ability says so.
     */
    private boolean zoneChangeEventMatches(GameData gameData, ZoneChangeWatch watch, TriggerCandidate candidate,
                                           List<ZoneChangeEvent> events) {
        ZoneChangeEvent event = events.getFirst();
        UUID controllerId = candidate.controllerId();
        Permanent entered = event.enteredBattlefield() && event.permanent() != null
                ? gameQueryService.findPermanentById(gameData, event.permanent().getId()) : null;
        TriggerLimit limit = candidate.ability().options().limit();
        return switch (watch) {
            // The watcher's own move counts only for "a permanent you control".
            case ON_ANY_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD -> watchesPermanentsYouControl(candidate)
                    || !candidate.sourceCard().getId().equals(event.card().getId());
            case ON_YOUR_COMMANDER_PUT_INTO_COMMAND_ZONE -> candidate.sourcePermanent() != null
                    && isCommanderOf(gameData, event.card(), controllerId);
            // "The first time one or more land cards are put into your graveyard each turn": only if this event's
            // lands are all the land cards that entered your graveyard this turn.
            case ON_ALLY_LAND_PUT_INTO_GRAVEYARD_FROM_ANYWHERE -> limit != TriggerLimit.FIRST_TIME_EACH_TURN
                    || gameData.landCardGraveyardEntriesThisTurn.getOrDefault(controllerId, 0) <= events.size();
            // "Whenever you create one or more tokens for the first time each turn" (Akim, the Soaring Wind): only if
            // this event's tokens are all the tokens its controller created this turn.
            case ON_ALLY_TOKEN_ENTERS_BATTLEFIELD -> (limit != TriggerLimit.FIRST_TIME_EACH_TURN
                    || gameData.tokensCreatedThisTurn.getOrDefault(controllerId, 0) <= events.size())
                    && (createsTokens(candidate) || !entersSuppressed(gameData, controllerId));
            case ON_OPPONENT_TOKEN_ENTERS_BATTLEFIELD, ON_ALLY_ENCHANTMENT_ENTERS_BATTLEFIELD,
                 ON_SELF_OR_ALLY_ENCHANTMENT_ENTERS_BATTLEFIELD, ON_ENCHANTED_PLAYER_CREATURE_ENTERS_BATTLEFIELD,
                 ON_ENCHANTED_PLAYER_LAND_ENTERS_BATTLEFIELD -> !entersSuppressed(gameData, controllerId);
            // Landfall: a printed land doesn't see its own entry unless its trigger narrows the lands down (Valakut,
            // the Molten Pinnacle's "a Mountain"); a nonland permanent that is a land only through a continuous
            // effect (Scute Swarm under Ashaya) does.
            case ON_ALLY_LAND_ENTERS_BATTLEFIELD -> !(candidate.sourcePermanent() != null
                    && candidate.sourcePermanent().getId().equals(event.permanent().getId())
                    && event.card().hasType(CardType.LAND) && !narrowedLandfall(candidate))
                    && !entersSuppressed(gameData, event.controllerId());
            case ON_CREATURE_ENTERS_FROM_GRAVEYARD -> entered != null
                    && controllerId.equals(entered.getEnteredFromGraveyardOwnerId())
                    && !entersSuppressed(gameData, controllerId);
            case ON_PERMANENT_ENTERS_FROM_GRAVEYARD -> entered != null
                    && entered.getEnteredFromGraveyardOwnerId() != null && !entersSuppressed(gameData, controllerId);
            case ON_PERMANENT_ENTERS_FROM_EXILE -> entered != null && entered.isEnteredFromExile()
                    && !entersSuppressed(gameData, controllerId);
            case ON_SELF_ENTERS_FROM_GRAVEYARD -> entered != null && entered.getEnteredFromGraveyardOwnerId() != null
                    && !entersSuppressed(gameData, event.controllerId());
            case GRAVEYARD_ON_ALLY_PERMANENT_ENTERS_BATTLEFIELD -> entered != null;
            case GRAVEYARD_ON_CREATURE_ENTERS_FROM_GRAVEYARD_OR_CAST_FROM_GRAVEYARD -> entered != null
                    && (controllerId.equals(entered.getEnteredFromGraveyardOwnerId())
                    || entered.isCast() && entered.getCastFromZone() == Zone.GRAVEYARD
                    && controllerId.equals(event.controllerId()));
            default -> true;
        };
    }

    /**
     * Whether a static ability keeps permanents entering from triggering the abilities of {@code controllerId}'s
     * permanents (Torpor Orb's kind, read as the old enter collectors read it).
     */
    private boolean entersSuppressed(GameData gameData, UUID controllerId) {
        return gameQueryService.areOpponentPermanentETBTriggersSuppressed(gameData, controllerId);
    }

    /**
     * The player a zone-change ability of {@code watch} names as "that player", its non-targeting target (CR 115.10a):
     * the player who controlled the object that moved (a sacrificing or entering opponent, the controller of a creature
     * an opponent controlled that died, the enchanted permanent's controller), the player who put a permanent onto the
     * battlefield (the caster of the spell it resolved from, Nature's Wrath's ruling), or the owner of the graveyard or
     * hand a card went to; {@code null} for an ability whose event names none.
     */
    private UUID thatPlayer(GameData gameData, ZoneChangeWatch watch, CreatureDeathWatch deathWatch,
                            ZoneChangeEvent event) {
        if (watch == null) {
            return deathWatch == CreatureDeathWatch.OPPONENT ? event.controllerId() : null;
        }
        return switch (watch) {
            case ON_OPPONENT_PERMANENT_SACRIFICED, ON_OPPONENT_NONTOKEN_PERMANENT_SACRIFICED,
                 ON_ANY_CREATURE_SACRIFICED, EXILE_ON_CONTROLLER_TOKEN_SACRIFICED,
                 ON_OPPONENT_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
                 ON_ARTIFACT_PUT_INTO_OPPONENT_GRAVEYARD_FROM_BATTLEFIELD,
                 ON_OPPONENT_CREATURE_ENTERS_BATTLEFIELD, ON_OPPONENT_LAND_ENTERS_BATTLEFIELD,
                 ON_ENCHANTED_PLAYER_CREATURE_ENTERS_BATTLEFIELD, ON_ENCHANTED_PLAYER_LAND_ENTERS_BATTLEFIELD ->
                    event.controllerId();
            case ON_ANY_PERMANENT_ENTERS_BATTLEFIELD -> {
                Permanent entered = event.permanent() == null ? null
                        : gameQueryService.findPermanentById(gameData, event.permanent().getId());
                yield entered != null && entered.getCastControllerId() != null ? entered.getCastControllerId()
                        : event.controllerId();
            }
            case ON_ANY_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
                 ON_ANY_NONTOKEN_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
                 ON_PERMANENT_PUT_INTO_OPPONENT_GRAVEYARD_FROM_BATTLEFIELD,
                 ON_ARTIFACT_PUT_INTO_OPPONENT_OWNER_GRAVEYARD_FROM_BATTLEFIELD, ON_ANY_PERMANENT_RETURNED_TO_HAND,
                 ON_NONBLACK_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE, ON_BLACK_CARD_PUT_INTO_OPPONENT_GRAVEYARD_FROM_ANYWHERE,
                 ON_OPPONENT_CREATURE_CARD_MILLED, ON_OPPONENT_MILLS, ON_CARD_PUT_INTO_OPPONENT_GRAVEYARD_FROM_ANYWHERE ->
                    event.ownerId();
            default -> null;
        };
    }

    /**
     * The record of a zone-change ability's {@code events}, stamped on its entry whatever its effects are, with the
     * source as it was when the ability triggered (CR 608.2h) and the stay in the graveyard of a source card there
     * ("this card", CR 400.7):
     * <ul>
     *   <li>a permanent that left the battlefield: as it last existed (CR 603.10a) with what was attached to it, as the
     *       triggering permanent ("it", "its counters", "that creature"), its controller, power, toughness and mana
     *       value then, its power as the event value, and the card it left as with the graveyard or exile
     *       stay it went to ("that card");</li>
     *   <li>a permanent that entered: as "it", with its controller, power and toughness as it entered, the card it
     *       entered as and the player or planeswalker it entered attacking;</li>
     *   <li>a card that moved between other zones: as "that card" with its graveyard stay;</li>
     *   <li>for "one or more": every object of the batch ("them", "those cards") and how many there were as the event
     *       value, or, for creatures dying, their total power (The Skullspore Nexus);</li>
     *   <li>"that player" ({@code thatPlayerId}) as its triggering player and non-targeting target.</li>
     * </ul>
     */
    private TriggerRecord zoneChangeRecord(GameData gameData, TriggerCandidate candidate, Permanent source,
                                           List<ZoneChangeEvent> events, UUID thatPlayerId, boolean deathBatch,
                                           boolean enchantedPermanent) {
        ZoneChangeEvent event = events.getFirst();
        Permanent snapshot = source == null ? null : new Permanent(source);
        long sourceGraveyardStay = candidate.zone() == Zone.GRAVEYARD
                ? gameData.graveyardEntryVersion(candidate.sourceCard().getId()) : -1;
        boolean oneOrMore = candidate.ability().options().multiplicity() == TriggerMultiplicity.ONE_OR_MORE;
        List<Card> cards = events.stream().map(ZoneChangeEvent::card).toList();
        Map<UUID, Long> graveyardStays = new java.util.HashMap<>();
        for (ZoneChangeEvent moved : events) {
            Card card = movedCard(moved);
            if (moved.to() == Zone.GRAVEYARD) {
                graveyardStays.put(card.getId(), gameData.graveyardEntryVersion(card.getId()));
            }
        }
        Card movedCard = movedCard(event);
        // The card's stay in exile when it went there, or a replacement effect exiled it instead (Leyline of the
        // Void); -1 when it isn't in exile.
        long exileStay = event.to() == Zone.EXILE || gameQueryService.findCardInExileById(gameData, movedCard.getId())
                != null ? gameData.exileEntryVersions.getOrDefault(movedCard.getId(), 0L) : -1L;
        Permanent lastKnown = event.permanent();
        Permanent entered = event.enteredBattlefield() && lastKnown != null
                ? gameQueryService.findPermanentById(gameData, lastKnown.getId()) : null;
        UUID enteredControllerId = entered == null ? event.controllerId()
                : Objects.requireNonNullElse(gameQueryService.findPermanentController(gameData, entered.getId()),
                event.controllerId());
        int enteredPower = entered == null ? 0 : gameQueryService.getEffectivePower(gameData, entered);
        int enteredToughness = entered == null ? 0 : gameQueryService.getEffectiveToughness(gameData, entered);
        UUID attackedTargetId = entered != null && entered.isAttacking() ? entered.getAttackTarget() : null;
        UUID defendingPlayerId = attackedTargetId == null ? null : gameData.playerIds.contains(attackedTargetId)
                ? attackedTargetId : gameQueryService.findPermanentController(gameData, attackedTargetId);
        int totalPower = 0;
        for (ZoneChangeEvent moved : events) {
            if (moved.leftBattlefield() && moved.permanent() != null) {
                totalPower = SaturatingMath.add(totalPower, CreatureDeath.of(moved).power());
            }
        }
        int batchPower = totalPower;
        return entry -> {
            entry.setNonTargeting(true);
            if (snapshot != null) {
                entry.setSourcePermanentSnapshot(snapshot);
            }
            if (sourceGraveyardStay >= 0) {
                entry.setSourceGraveyardEntryVersion(sourceGraveyardStay);
            }
            if (thatPlayerId != null) {
                entry.setTargetId(thatPlayerId);
                entry.setTriggeringPlayerId(thatPlayerId);
            }
            if (event.leftBattlefield() && lastKnown != null) {
                CreatureDeath death = CreatureDeath.of(event);
                entry.setTriggeringPermanentId(lastKnown.getId());
                entry.rememberLastKnownPermanentCard(lastKnown.getId(), lastKnown.getCard());
                entry.setTriggeringPermanent(lastKnown, event.attachments());
                entry.setTriggeringPermanentControllerId(event.controllerId());
                entry.setTriggeringPermanentPowerAtTrigger(death.power());
                entry.setTriggeringPermanentToughnessAtTrigger(death.toughness());
                entry.setDyingPermanentManaValue(lastKnown.getCard().getManaValue());
                // As it last existed, which can be negative (CR 107.1b), for a comparison ("the difference").
                entry.setEventValue(death.power());
                if (event.to() == Zone.EXILE) {
                    entry.setTriggeringCardId(movedCard.getId());
                }
                entry.setTriggeringCardExileEntryVersion(exileStay);
                entry.setTriggeringPermanentOwnerId(event.ownerId());
                if (enchantedPermanent) {
                    // "Return that card": every card the enchanted permanent put into the graveyard (a merged
                    // permanent's too), each with its graveyard stay (CR 400.7). The Aura is still on the battlefield
                    // as its ability triggers and goes to its owner's graveyard with the state-based actions that
                    // follow (CR 704.5m): that stay is the one "return this card" means.
                    List<Card> leaving = lastKnown.cardsLeavingBattlefield();
                    entry.setTriggeringCards(leaving);
                    for (Card card : leaving) {
                        entry.getEventCardGraveyardEntryVersions().put(card.getId(),
                                gameData.graveyardEntryVersion(card.getId()));
                    }
                    entry.setAwaitingSourceGraveyardEntry(true);
                }
            } else if (event.enteredBattlefield() && lastKnown != null) {
                entry.setTriggeringCardSnapshot(event.card());
                entry.setTriggeringPermanentControllerId(enteredControllerId);
                if (entered != null) {
                    entry.setTriggeringPermanentId(entered.getId());
                    entry.setTriggeringCardId(entered.getCard().getId());
                    entry.setTriggeringPermanentPowerAtTrigger(enteredPower);
                    entry.setTriggeringPermanentToughnessAtTrigger(enteredToughness);
                    entry.rememberLastKnownPermanentCard(entered.getId(), entered.getCard());
                    entry.setAttachedPermanentSnapshot(new Permanent(entered));
                    entry.setAttackedTargetId(attackedTargetId);
                    entry.setDefendingPlayerId(defendingPlayerId);
                }
                entry.setTriggeringPermanentIds(events.stream().map(moved -> moved.permanent().getId()).toList());
                for (ZoneChangeEvent moved : events) {
                    entry.rememberLastKnownPermanentCard(moved.permanent().getId(), moved.permanent().getCard());
                }
            } else {
                entry.setTriggeringCardId(movedCard.getId());
                entry.setTriggeringCardSnapshot(event.card());
            }
            if (graveyardStays.containsKey(movedCard.getId())) {
                entry.setTriggeringCard(movedCard.getId(), graveyardStays.get(movedCard.getId()));
            }
            if (oneOrMore) {
                entry.setTriggeringCards(cards);
                entry.setEventCardIds(cards.stream().map(Card::getId).toList());
                entry.getEventCardGraveyardEntryVersions().putAll(graveyardStays);
                if (event.leftBattlefield()) {
                    entry.setTriggeringPermanentIds(events.stream().filter(moved -> moved.permanent() != null)
                            .map(moved -> moved.permanent().getId()).toList());
                }
                entry.setEventValue(deathBatch ? Math.max(0, batchPower) : events.size());
            }
        };
    }

    /** The card a zone change moved, as it is now: for a departure, the permanent's physical card. */
    private static Card movedCard(ZoneChangeEvent event) {
        return event.leftBattlefield() && event.permanent() != null && event.permanent().getOriginalCard() != null
                ? event.permanent().getOriginalCard() : event.card();
    }

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

    /** Whether {@code card} is one of {@code ownerId}'s commanders, either face of it. */
    private static boolean isCommanderOf(GameData gameData, Card card, UUID ownerId) {
        return gameData.playerCommanders.getOrDefault(ownerId, List.of()).stream()
                .anyMatch(commander -> commander.getId().equals(card.getId())
                        || commander.getBackFaceCard() != null
                        && commander.getBackFaceCard().getId().equals(card.getId()));
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

    /**
     * The players and permanents {@code spellEntry} targets became its targets (CR 601.2c): the rules event the
     * becomes-the-target abilities trigger on; then the choices of the abilities that went to the stack door begin.
     */
    public void checkBecomesTargetOfSpellTriggers(GameData gameData, StackEntry spellEntry) {
        TargetEvent event = new TargetEvent(TargetEvent.Kind.BECAME_TARGET_OF_SPELL, spellEntry);
        gameData.rulesEvents.separately(() -> gameData.rulesEvents.emit(event));

        if (gameData.hasPendingInteraction(PermanentChoiceContext.ETBTokenMultiTargetTrigger.class)
                && !gameData.interaction.isAwaitingInput()) {
            processNextETBTokenMultiTargetTrigger(gameData);
        }
        beginStackDoorChoices(gameData);
    }

    /**
     * Begins the next choice the stack door queued (CR 603.3c, 603.3d): its mode, its targets in one choice or group by
     * group, its graveyard cards; nothing while a choice is awaited. It runs where an action or a resolution that made
     * abilities trigger begins their choices: a spell cast, an ability activated, either targeted, the
     * trigger-target choices of the other routes. The answer's handler begins the one after it.
     */
    public void beginStackDoorChoices(GameData gameData) {
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
     * A player chose the targets of {@code targetEntry} (CR 601.2c, 603.3d): the rules event "whenever a player chooses
     * one or more targets" abilities trigger on. A spell or ability with no target, or one that targets nothing,
     * chose none.
     */
    public void checkTargetChoiceTriggers(GameData gameData, StackEntry targetEntry) {
        if (targetEntry == null || !targetEntry.hasAnyTarget() || targetEntry.isNonTargeting()) {
            return;
        }
        TargetEvent event = new TargetEvent(TargetEvent.Kind.TARGETS_CHOSEN, targetEntry);
        gameData.rulesEvents.separately(() -> gameData.rulesEvents.emit(event));
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
        TargetEvent event = new TargetEvent(TargetEvent.Kind.BECAME_TARGET_OF_ABILITY, abilityEntry);
        gameData.rulesEvents.separately(() -> gameData.rulesEvents.emit(event));

        beginStackDoorChoices(gameData);
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
     * The "when you do" of {@code exertedCreatureId}'s "you may exert this creature as it attacks" (CR 701.43d), the
     * triggered ability linked to it (CR 607.2h), once its controller exerted it as it attacked.
     */
    public void checkExertedAsItAttacksTriggers(GameData gameData, UUID exertingPlayerId, Permanent exertedCreature) {
        if (exertingPlayerId == null || exertedCreature == null) {
            return;
        }
        dispatchWatch(gameData, exertedCreature, exertingPlayerId, ActionWatch.ON_EXERTED_AS_IT_ATTACKS,
                new TriggerContext.Exert(exertingPlayerId, exertedCreature.getId()));
    }

    /**
     * Puts {@code watcher}'s ability, a delayed triggered ability with a stated duration (CR 603.7b), on the stack as
     * {@code effects} for the event about {@code targetId}, the object or player it acts on without targeting it.
     */
    private void enqueueTemporaryGlobalTrigger(GameData gameData,
                                                TemporaryGlobalTriggeredAbility watcher,
                                                List<CardEffect> effects,
                                                UUID targetId, int eventValue, UUID triggeringPermanentId) {
        StackEntry entry = TriggeredAbilityStackService.madeAbility(watcher.sourceCard(), watcher.controllerId(),
                watcher.sourceCard().getName() + "'s ability", effects);
        entry.setTargetId(targetId);
        entry.setEventValue(eventValue);
        entry.setLinkedExiledCardId(watcher.linkedExiledCardId());
        if (triggeringPermanentId != null) {
            entry.setTriggeringPermanentId(triggeringPermanentId);
        }
        entry.setNonTargeting(true);
        triggeredAbilityStackService.putDelayed(gameData, entry);
        gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
        log.info("Game {} - {} temporary global {} trigger fires",
                gameData.id, watcher.sourceCard().getName(), LegacySlots.label(watcher.ability()));
    }

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
        TriggerContext context = new TriggerContext.CreatureCrewsVehicle(crewingCreature, vehicle);
        holdForActivation(gameData, () -> {
            for (TriggeredAbility ability : printedWatchedAbilities(gameData, crewingCreature,
                    MiscWatch.ON_CREWS_VEHICLE)) {
                putFamilyAbility(gameData, crewingCreature, crewingCreature.getCard(), controllerId,
                        MiscWatch.ON_CREWS_VEHICLE, context, ability);
            }
        });
    }

    /**
     * Runs {@code collect}, which hands abilities that trigger as an activated ability's cost is paid to the stack door,
     * and holds what it put on the stack until that ability is on the stack, above which they go the next time a player
     * would receive priority (CR 603.3).
     */
    private void holdForActivation(GameData gameData, Runnable collect) {
        int before = gameData.stack.size();
        collect.run();
        if (gameData.stack.size() > before) {
            List<StackEntry> held = gameData.stack.subList(before, gameData.stack.size());
            gameData.pendingActivatedAbilityCostTriggers.addAll(new ArrayList<>(held));
            held.clear();
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
        holdForActivation(gameData, () -> {
            for (Permanent watcher : List.copyOf(battlefield)) {
                putFamilyAbilities(gameData, watcher, controllerId, MiscWatch.ON_ALLY_CREATURE_CREWS_VEHICLE,
                        context);
            }
        });
    }

    /**
     * Hands the abilities that trigger whenever a player becomes the monarch to the stack door, each whole (CR 603.3):
     * the new monarch is the triggering player, and for "whenever an opponent becomes the monarch" the non-targeting
     * "that player".
     */
    public void checkBecomesMonarchTriggers(GameData gameData, UUID monarchPlayerId) {
        gameData.forEachPermanent((ownerId, permanent) -> {
            boolean controllerBecameMonarch = ownerId.equals(monarchPlayerId);
            ActionWatch watch = controllerBecameMonarch
                    ? ActionWatch.ON_CONTROLLER_BECOMES_MONARCH
                    : ActionWatch.ON_OPPONENT_BECOMES_MONARCH;
            for (TriggeredAbility ability : watchedAbilities(gameData, permanent, watch)) {
                putEventAbility(gameData, ability, permanent.getCard(), permanent, ownerId,
                        ConditionContext.forPermanent(permanent, ownerId), null,
                        eventRecord(permanent, controllerBecameMonarch ? null : monarchPlayerId, monarchPlayerId,
                                null, null, null));
            }
        });
        beginStackDoorChoices(gameData);
    }

    /**
     * Hands the abilities that trigger whenever their controller solves a Case to the stack door, each whole (CR
     * 603.3), the Case solved as the triggering permanent.
     */
    public void checkAllyCaseSolvesTriggers(GameData gameData, Permanent solvedCase, UUID controllerId) {
        gameData.forEachPermanent((ownerId, permanent) -> {
            if (!ownerId.equals(controllerId)) return;
            for (TriggeredAbility ability : watchedAbilities(gameData, permanent, ActionWatch.ON_ALLY_CASE_SOLVES)) {
                putEventAbility(gameData, ability, permanent.getCard(), permanent, controllerId,
                        ConditionContext.forPermanent(permanent, controllerId), null,
                        eventRecord(permanent, null, null, null, solvedCase, controllerId));
            }
        });
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
        if (watcher == null) return;

        TriggerContext context = new TriggerContext.CreatureCrewsVehicle(watcher, saddledOrCrewedPermanent);
        holdForActivation(gameData, () -> putFamilyAbilities(gameData, watcher, activatingPlayerId,
                MiscWatch.ON_SELF_SADDLES_OR_CREWS_DURING_MAIN_PHASE, context));
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

    /** The filter context of a predicate {@code watcher}, a permanent with a triggered ability, judges. */
    private static FilterContext watcherFilterContext(GameData gameData, Permanent watcher, UUID watcherControllerId) {
        return FilterContext.of(gameData)
                .withSourceCardId(watcher.getOriginalCard().getId())
                .withSourceControllerId(watcherControllerId)
                .withSourcePermanentId(watcher.getId())
                .withSourcePermanentSnapshot(watcher);
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
     * An activated ability was activated: it is on the stack and its costs are paid (CR 602.2b, 601.2i). The delayed
     * "when you next activate ..., copy that ability" abilities trigger first ({@link #collectDelayedActivationCopies}),
     * then the activation is the rules event the "whenever [a player] activates an ability" abilities trigger on
     * ({@link SpellEvent.Activated}), with the life paid to activate it; then the abilities that choose their targets as
     * they go on the stack (CR 603.3d) choose them, above the activated ability.
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
        SpellEvent.Activated event = new SpellEvent.Activated(activation.activatingPlayerId(),
                activation.abilityEntry(), activation.ability(), activation.activatedPermanent(),
                activation.activatedPermanentControllerId(), activation.manaAbility(), lifePaid, treasureMana);
        gameData.rulesEvents.separately(() -> gameData.rulesEvents.emit(event));
        // An ability that chooses its targets as it goes on the stack (CR 603.3d) chooses them now, above the
        // activated ability; a mana ability's wait with it.
        if (!activation.manaAbility()) {
            beginStackDoorChoices(gameData);
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
                triggeredAbilityStackService.putDelayed(gameData, TriggeredAbilityStackService.madeAbility(abilityEntry.getCard(),
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
                triggeredAbilityStackService.putDelayed(gameData, TriggeredAbilityStackService.madeAbility(copySourceCard,
                        activatingPlayerId,
                        "Copy " + abilityEntry.getCard().getName() + "'s loyalty ability",
                        List.of(new CopyControllerActivatedAbilityEffect(snapshot, ability, activatingPlayerId)),
                        0,
                        copySourcePermanentId
                ));
            }
            gameData.pendingNextLoyaltyAbilityCopyThisTurnCount.remove(activatingPlayerId);
            gameData.pendingNextLoyaltyAbilityCopySourcesThisTurn.remove(activatingPlayerId);
            // A delayed ability shared with the next instant or sorcery spell is used up (Repeated Reverberation).
            Set<UUID> links = sources.stream().map(GameData.PendingCopySource::link)
                    .filter(java.util.Objects::nonNull).collect(java.util.stream.Collectors.toSet());
            List<GameData.PendingSpellCopy> spellCopies = gameData.pendingNextInstantSorceryCopies.get(activatingPlayerId);
            if (spellCopies != null && !links.isEmpty()) {
                spellCopies.removeIf(copy -> copy.link() != null && links.contains(copy.link()));
            }
        }

        Integer pendingExhaustCopies = gameData.pendingNextExhaustAbilityCopyThisTurnCount.get(activatingPlayerId);
        if (pendingExhaustCopies != null && pendingExhaustCopies > 0 && ability.isExhaustAbility()) {
            StackEntry snapshot = new StackEntry(abilityEntry);
            List<GameData.PendingCopySource> sources = gameData.pendingNextExhaustAbilityCopySourcesThisTurn
                    .getOrDefault(activatingPlayerId, List.of());
            for (int i = 0; i < pendingExhaustCopies; i++) {
                GameData.PendingCopySource source = i < sources.size() ? sources.get(i) : null;
                triggeredAbilityStackService.putDelayed(gameData, TriggeredAbilityStackService.madeAbility(source != null ? source.card() : abilityEntry.getCard(),
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
            triggeredAbilityStackService.putDelayed(gameData, TriggeredAbilityStackService.madeAbility(delayedCopy.sourceCard(),
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
            putFamilyAbilities(gameData, perm, taxingPlayerId, watch, ctx);
        }
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

    public void processNextTriggeredModalTrigger(GameData gameData) {
        triggeredAbilityQueueService.processNextTriggeredModalTrigger(gameData);
    }

    /**
     * Puts the chosen modes of {@code triggerRecord}'s ability, parked by the stack door for its mode, on the stack, or
     * queues the choice of their targets.
     */
    public void queueChosenTriggeredModalTrigger(GameData gameData, Card sourceCard, UUID controllerId,
            UUID sourcePermanentId, List<ChooseOneEffect.ChooseOneOption> chosen, UUID attackedTargetId,
            StackEntry triggerRecord) {
        triggeredAbilityQueueService.queueChosenTriggeredModalTrigger(gameData, sourceCard, controllerId,
                sourcePermanentId, chosen, attackedTargetId, triggerRecord);
    }

    public void processNextSpellTargetTrigger(GameData gameData) {
        triggeredAbilityQueueService.processNextSpellTargetTrigger(gameData);
        beginStackDoorChoices(gameData);
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
        for (Permanent perm : List.copyOf(battlefield)) {
            for (TriggeredAbility ability : watchedAbilities(gameData, perm, watch)) {
                ActionAbilityReader.ActionEffect first = ActionAbilityReader.read(ability, watch.trigger()).getFirst();
                if (first.filter() != null && (exploredCard == null || !predicateEvaluationService.matchesCardPredicate(
                        exploredCard, first.filter(), perm.getCard().getId(), gameData, controllerId))) {
                    continue;
                }
                putJudgedFamilyAbility(gameData, perm, perm.getCard(), controllerId, watch, null, ability);
            }
        }
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

    /**
     * Hands this permanent's "when this exploits a creature" abilities ({@link ActionWatch#ON_EXPLOIT}) to the stack
     * door after it exploited a creature, each whole (CR 603.3), with the creature it sacrificed recorded ("the
     * sacrificed creature's power"). The source is the permanent as it last existed when it sacrificed itself. Its
     * targets, a spell's among them ("counter target creature spell", Silumgar Sorcerer), are chosen as it goes on the
     * stack (CR 603.3d).
     */
    public void checkExploitTriggers(GameData gameData, Card sourceCard, UUID controllerId, UUID sourcePermanentId) {
        StackEntry originalEntry = gameData.pendingEffectResolutionEntry;
        Permanent source = sourcePermanentId == null ? null : gameQueryService.findPermanentById(gameData,
                sourcePermanentId);
        if (source == null && sourcePermanentId != null) {
            source = gameData.departedPermanentSnapshots.values().stream()
                    .filter(snapshot -> snapshot.getId().equals(sourcePermanentId)).findFirst().orElse(null);
        }
        ActionWatch watch = ActionWatch.ON_EXPLOIT;
        for (TriggeredAbility ability : ActionAbilityReader.printed(sourceCard, watch)) {
            Permanent snapshot = source == null ? null : new Permanent(source);
            boolean triggered = putJudgedExploitAbility(gameData, ability, sourceCard, source, controllerId, entry -> {
                entry.setNonTargeting(true);
                if (snapshot != null) {
                    entry.setSourcePermanentSnapshot(snapshot);
                }
                if (originalEntry != null) {
                    entry.setSacrificedPermanentSnapshot(originalEntry.getSacrificedPermanentSnapshot());
                    entry.setSacrificedPower(originalEntry.getSacrificedPower());
                    entry.setSacrificedColorCount(originalEntry.getSacrificedColorCount());
                    entry.setSacrificedToughness(originalEntry.getSacrificedToughness());
                }
            });
            if (triggered) {
                log.info("Game {} - {} exploit trigger queued", gameData.id, sourceCard.getName());
            }
        }
    }

    private boolean putJudgedExploitAbility(GameData gameData, TriggeredAbility ability, Card sourceCard,
                                            Permanent source, UUID controllerId, TriggerRecord record) {
        ConditionContext context = source != null ? ConditionContext.forPermanent(source, controllerId)
                : ConditionContext.forCard(sourceCard, controllerId);
        return putEventAbility(gameData, ability, sourceCard, source, controllerId, context, null, record);
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
     * Hands the printed abilities {@code watch} reads of {@code permanent} as it last existed on the battlefield (the
     * exploiting creature that sacrificed itself) to the stack door, as {@link #putFamilyAbility} says.
     */
    private void dispatchLastKnownActionAbilities(GameData gameData, Permanent permanent, UUID controllerId,
                                                  ActionWatch watch, TriggerContext ctx) {
        for (TriggeredAbility ability : ActionAbilityReader.printed(permanent.getCard(), watch)) {
            putFamilyAbility(gameData, permanent, permanent.getCard(), controllerId, watch, ctx, ability);
        }
    }

    public static boolean predicateContainsHasTarget(com.github.laxika.magicalvibes.model.filter.StackEntryPredicate predicate) {
        if (predicate instanceof com.github.laxika.magicalvibes.model.filter.StackEntryHasTargetPredicate) {
            return true;
        }
        if (predicate instanceof com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate typeIn) {
            return typeIn.spellTypes().stream().anyMatch(StackEntryType::isAbility);
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

        ActionWatch watch = ActionWatch.ON_CONTROLLER_CLASHES;
        for (Permanent perm : new ArrayList<>(battlefield)) {
            // "Whenever you clash, if you won": whether the clash was won is the event's value, which the event
            // condition reads; the entry records it.
            for (TriggeredAbility ability : watchedAbilities(gameData, perm, watch)) {
                putEventAbility(gameData, ability, perm.getCard(), perm, clashingPlayerId,
                        ConditionContext.forPermanent(perm, clashingPlayerId).withEventValue(outcome), null,
                        eventRecord(perm, null, clashingPlayerId, outcome, null, null));
            }
        }
        beginStackDoorChoices(gameData);
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
            StackEntry entry = TriggeredAbilityStackService.madeAbility(watcher.sourceCard(), watcher.controllerId(),
                    watcher.sourceCard().getName() + "'s ability", temporaryEffects(watcher));
            entry.setTargetId(dyingControllerId);
            entry.setTriggeringPermanentId(dyingPermanent.getId());
            entry.setTriggeringPermanentControllerId(dyingControllerId);
            entry.setNonTargeting(true);
            triggeredAbilityStackService.putDelayed(gameData, entry);
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
        StackEntry entry = TriggeredAbilityStackService.madeAbility(watcher.sourceCard(), watcher.controllerId(),
                watcher.sourceCard().getName() + "'s ability", temporaryEffects(watcher));
        entry.setTargetId(targetId);
        entry.setEventValue(eventValue);
        entry.setNonTargeting(true);
        triggeredAbilityStackService.putDelayed(gameData, entry);
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
        long graveyardEntryVersion = gameData.graveyardEntryVersion(card.getId());
        putEmblemAbility(gameData, emblem, ability, source, description,
                ConditionContext.forCard(source, emblem.controllerId()), entry -> {
                    entry.setTriggeringCard(card.getId(), graveyardEntryVersion);
                    entry.setTriggeringPermanentId(lastKnown.getId());
                    entry.setTriggeringPermanent(lastKnown);
                    entry.setTriggeringPermanentControllerId(controllerId);
                    entry.setNonTargeting(true);
                }, false);
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
                putFamilyAbilities(gameData, perm, winningPlayerId, ActionWatch.ON_CONTROLLER_WINS_COIN_FLIP, ctx);
            }
        }

        gameData.forEachPermanent((controllerId, permanent) ->
                putFamilyAbilities(gameData, permanent, controllerId, ActionWatch.ON_ANY_PLAYER_WINS_COIN_FLIP, ctx));
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
            triggeredAbilityStackService.putDelayed(gameData, TriggeredAbilityStackService.madeAbility(spellCard,
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
            triggeredAbilityStackService.putDelayed(gameData, TriggeredAbilityStackService.madeAbility(abilityEntry.getCard(),
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

    /** Fires "whenever you lose a coin flip" triggers for the losing player's battlefield. */
    public void checkControllerLosesCoinFlipTriggers(GameData gameData, UUID losingPlayerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(losingPlayerId);
        if (battlefield == null) return;

        TriggerContext ctx = new TriggerContext.CoinFlipLost(losingPlayerId);
        for (Permanent perm : List.copyOf(battlefield)) {
            putFamilyAbilities(gameData, perm, losingPlayerId, ActionWatch.ON_CONTROLLER_LOSES_COIN_FLIP, ctx);
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
            // Each ability of the plane goes on the stack whole (CR 603.3), its target chosen as it goes there
            // (CR 603.3d).
            Card plane = object.getCard();
            int power = Math.max(0, dyingPermanent.getEffectivePower());
            long graveyardVersion = gameData.graveyardEntryVersion(dyingCard.getId());
            for (TriggeredAbility ability : abilities) {
                PlanarObject source = object.copy();
                triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(ability, plane, null,
                        planarControllerId, plane.getName() + "'s ability",
                        ConditionContext.forCard(plane, planarControllerId), entry -> {
                            entry.setSourcePlanarObject(source);
                            entry.setTriggeringCard(dyingCard.getId(), graveyardVersion);
                            entry.setTriggeringPermanentId(dyingPermanent.getId());
                            entry.setTriggeringPermanentControllerId(dyingCreatureControllerId);
                            entry.setTriggeringPermanentPowerAtTrigger(power);
                            entry.setEventValue(power);
                        }).withCreatureDying());
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
            StackEntry entry = TriggeredAbilityStackService.madeAbility(watcher.sourceCard(),
                    watcher.controllerId(),
                    watcher.sourceCard().getName() + "'s ability",
                    new ArrayList<>(List.of(watcher.effect())),
                    (UUID) null,
                    watcher.sourcePermanentId());
            triggeredAbilityStackService.putDelayed(gameData, entry);
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
            StackEntry entry = TriggeredAbilityStackService.madeAbility(watcher.sourceCard(),
                    watcher.controllerId(),
                    watcher.sourceCard().getName() + "'s ability",
                    new ArrayList<>(List.of(watcher.effect())),
                    (UUID) null,
                    (UUID) null);
            entry.setTriggeringPermanentId(dyingPermanent.getId());
            triggeredAbilityStackService.putDelayed(gameData, entry);
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
            StackEntry entry = TriggeredAbilityStackService.madeAbility(watcher.sourceCard(),
                    watcher.controllerId(),
                    watcher.sourceCard().getName() + "'s ability",
                    new ArrayList<>(List.of(watcher.effect())),
                    (UUID) null,
                    (UUID) null);
            entry.setTriggeringCardId(dyingCardId);
            // "Its controller" is the dying creature's last-known controller (CR 608.2h), whatever the effects are.
            entry.setTriggeringPermanentControllerId(dyingCreatureControllerId);
            triggeredAbilityStackService.putDelayed(gameData, entry);
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
            for (TriggeredAbility ability : emblem.triggeredAbilities()) {
                if (emblemSeesCreatureDie(gameData, emblem, ability, context)) {
                    putEmblemDeathAbilityOnStack(gameData, emblem, ability, dyingCard, context);
                }
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
        // "If a creature dying causes a triggered ability of an emblem you control to trigger, it triggers an
        // additional time": the stack door counts it for an emblem's creature-death ability.
        putEmblemAbility(gameData, emblem, ability, source, description,
                ConditionContext.forCard(source, emblem.controllerId()), entry -> {
                    entry.setTriggeringCardId(dyingCard.getId());
                    if (death.dyingPermanent() != null) {
                        entry.setTriggeringPermanent(death.dyingPermanent());
                    }
                    entry.setTriggeringPermanentControllerId(death.dyingCreatureControllerId());
                    entry.setNonTargeting(true);
                }, true);
        gameLogService.append(gameData, GameLog.text(description + " triggers (a creature died)."));
        log.info("Game {} - {} triggers for {} dying", gameData.id, description, dyingCard.getName());
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
            StackEntry entry = TriggeredAbilityStackService.madeAbility(delayed.sourceCard(),
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
            triggeredAbilityStackService.putDelayed(gameData, entry);
        }
        for (TemporaryGlobalTriggeredAbility watcher : List.copyOf(gameData.temporaryGlobalTriggeredAbilities)) {
            if (!watcher.ability().conditions().getFirst().trigger()
                    .equals(Triggers.leavesBattlefield(ObjectRelation.THIS))
                    || !target.getId().equals(watcher.watchedPermanentId())) {
                continue;
            }
            StackEntry entry = TriggeredAbilityStackService.madeAbility(watcher.sourceCard(), watcher.controllerId(),
                    watcher.sourceCard().getName() + "'s delayed trigger", temporaryEffects(watcher), (UUID) null,
                    target.getId());
            entry.setNonTargeting(true);
            triggeredAbilityStackService.putDelayed(gameData, entry);
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
            StackEntry se = TriggeredAbilityStackService.madeAbility(delayed.sourceCard(),
                    delayed.controllerId(),
                    delayed.sourceCard().getName() + "'s delayed trigger",
                    new ArrayList<>(List.of(new SacrificeSelfEffect())),
                    null,
                    delayed.sourcePermanentId());
            se.setNonTargeting(true);
            triggeredAbilityStackService.putDelayed(gameData, se);
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
            StackEntry se = TriggeredAbilityStackService.madeAbility(delayed.sourceCard(),
                    delayed.controllerId(),
                    delayed.sourceCard().getName() + "'s delayed trigger",
                    new ArrayList<>(List.of(new SacrificeSelfEffect())),
                    null,
                    delayed.targetPermanentId());
            se.setNonTargeting(true);
            triggeredAbilityStackService.putDelayed(gameData, se);
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
    public void processDelayedDestroyTargetWhenSourceUntaps(GameData gameData, Permanent untappedPermanent) {
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
        StackEntry se = TriggeredAbilityStackService.madeAbility(
                delayed.sourceCard(),
                delayed.controllerId(),
                delayed.sourceCard().getName() + "'s delayed trigger",
                List.of(delayed.exile()
                        ? new com.github.laxika.magicalvibes.model.effect.RemoveLinkedPermanentEffect(
                                com.github.laxika.magicalvibes.model.effect.RemoveLinkedPermanentEffect.Mode.EXILE,
                                delayed.targetPermanentId())
                        : new DestroyLinkedPermanentEffect(true, delayed.targetPermanentId())));
        se.setNonTargeting(true);
        triggeredAbilityStackService.putDelayed(gameData, se);
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
                StackEntry trigger = TriggeredAbilityStackService.madeAbility(delayed.sourceCard(),
                        delayed.controllerId(),
                        delayed.sourceCard().getName() + "'s delayed ability",
                        new ArrayList<>(List.of(new PutCounterOnTargetPermanentEffect(
                                delayed.counterType(), delayed.counterAmount()))),
                        returnedPermanentId,
                        (UUID) null);
                trigger.setNonTargeting(true);
                triggeredAbilityStackService.putDelayed(gameData, trigger);
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
            // Each ability of the plane goes on the stack whole (CR 603.3).
            Card plane = object.getCard();
            for (TriggeredAbility ability : ZoneChangeAbilityReader.printed(
                    plane, Triggers.leavesBattlefield(Triggers.CREATURE, ObjectRelation.ANOTHER))) {
                PlanarObject source = object.copy();
                triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(ability, plane, null,
                        planarControllerId, plane.getName() + "'s ability",
                        ConditionContext.forCard(plane, planarControllerId), entry -> {
                            entry.setSourcePlanarObject(source);
                            entry.setTriggeringPermanentId(leavingId);
                            entry.setTriggeringPermanentControllerId(leavingControllerId);
                            entry.setNonTargeting(true);
                        }));
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
            // Each ability of the scheme goes on the stack whole (CR 603.3).
            for (TriggeredAbility ability : schemeLeavesBattlefieldAbilities(gameData, perm, ownerId,
                    leavingPermanent)) {
                Permanent snapshot = new Permanent(perm);
                triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(ability, perm.getCard(), perm,
                        ownerId, perm.getCard().getName() + "'s ability", ConditionContext.forPermanent(perm, ownerId),
                        entry -> {
                            entry.setSourcePermanentSnapshot(snapshot);
                            entry.setTriggeringPermanent(leavingPermanent);
                        }));
                gameLogService.append(gameData, GameLog.text(perm.getCard().getName() + "'s ability triggers."));
                log.info("Game {} - {} triggers on another permanent leaving the battlefield ({})",
                        gameData.id, perm.getCard().getName(), leavingPermanent.getCard().getName());
            }
        };
        // CR 314.4: a face-up scheme's triggered abilities trigger from the command zone.
        gameData.forEachFaceUpSchemeSource(collect);
    }

    /**
     * A face-up scheme's "whenever another permanent leaves the battlefield" abilities whose filter {@code leaving}
     * matches ("whenever your commander leaves the battlefield"), with the scheme as the filter's source.
     */
    private List<TriggeredAbility> schemeLeavesBattlefieldAbilities(GameData gameData, Permanent scheme, UUID ownerId,
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
        return matching;
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
        gameData.rulesEvents.occur(CounterChangeEvent.ofPlayer(CounterChangeEvent.Kind.YOU_PUT_COUNTERS, controllerId,
                poisonAmount));

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

            StackEntry entry = TriggeredAbilityStackService.madeAbility(triggerSource,
                    triggerControllerId,
                    "Return " + graveyardCard.getName() + " to the battlefield",
                    new ArrayList<>(List.of(new ReturnTriggeringCardFromGraveyardToBattlefieldEffect(
                            delayedReturn.enterTapped(), delayedReturn.returnUnderController())))
            );
            entry.setTriggeringCardId(graveyardCard.getId());
            entry.setTriggeringCardGraveyardEntryVersion(
                    gameData.graveyardEntryVersion(graveyardCard.getId()));
            triggeredAbilityStackService.putDelayed(gameData, entry);

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
            StackEntry delayedEntry = TriggeredAbilityStackService.madeAbility(registration.sourceCard(),
                    registration.controllerId(),
                    registration.sourceCard().getName() + " triggers (a creature dealt damage this way died)",
                    List.of(effect), dyingPermanentControllerId, registration.sourcePermanentId());
            delayedEntry.setEventValue(Math.max(0, dyingPermanentPower));
            delayedEntry.setDyingPermanentManaValue(Math.max(0, dyingPermanentManaValue));
            delayedEntry.setTriggeringCardId(dyingPermanentCardId);
            delayedEntry.setTriggeringCardGraveyardEntryVersion(
                    gameData.graveyardEntryVersion(dyingPermanentCardId));
            delayedEntry.setTriggeringPermanent(dyingPermanent);
            if (effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                    || effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
                // A target of its own is chosen as it goes on the stack (CR 603.3d), not the dying creature's
                // controller.
                delayedEntry.setTargetId(null);
                triggeredAbilityStackService.putMade(gameData, delayedEntry);
                continue;
            }

            triggeredAbilityStackService.putDelayed(gameData, delayedEntry);

            log.info("Game {} - Delayed death trigger: {} triggers (a creature it damaged died this turn)",
                    gameData.id, registration.sourceCard().getName());
        }
    }

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
    }

    private void checkGraveyardDayNightChangeTriggers(GameData gameData, UUID controllerId) {
        for (Card card : new ArrayList<>(gameData.playerGraveyards.getOrDefault(controllerId, List.of()))) {
            putGraveyardFamilyAbilities(gameData, card, controllerId, MiscWatch.GRAVEYARD_ON_DAY_NIGHT_CHANGE, null,
                    TriggerRecord.NONE);
        }
    }

    /**
     * Hands each ability the graveyard watch {@code watch} reads of {@code card} in its owner {@code ownerId}'s
     * graveyard (its own and its perpetual ones; none while cards in graveyards have lost all abilities) to the stack
     * door for {@code event}, whole (CR 603.3): the record names this graveyard incarnation of the card as its source
     * (CR 400.7), with what {@code record} stamps of the event.
     */
    private void putGraveyardFamilyAbilities(GameData gameData, Card card, UUID ownerId, TriggerWatch watch,
                                             TriggerContext event, TriggerRecord record) {
        for (TriggeredAbility ability : WatchedAbilities.withWatchedTrigger(
                gameQueryService.getEffectiveGraveyardTriggeredAbilities(gameData, card), watch.trigger(),
                watch.zone())) {
            long graveyardEntryVersion = gameData.graveyardEntryVersion(card.getId());
            putEventAbility(gameData, ability, card, null, ownerId, ConditionContext.forCard(card, ownerId), event,
                    entry -> {
                        entry.setNonTargeting(true);
                        entry.setSourceGraveyardEntryVersion(graveyardEntryVersion);
                        record.stamp(entry);
                    });
        }
    }

    public void processNextDayNightTriggerTarget(GameData gameData) {
        triggeredAbilityQueueService.processNextDayNightTriggerTarget(gameData);
        beginStackDoorChoices(gameData);
    }

    /**
     * Asks for the player an Aura transformed by day or night attaches to ("as this transforms into Curse of Leeches,
     * attach it to a player"), as it transforms; the triggered abilities' choices wait for the next pump.
     */
    public void beginDayNightTransformAttachment(GameData gameData) {
        triggeredAbilityQueueService.processNextDayNightTriggerTarget(gameData);
    }

    /**
     * What a creature entering under {@code controllerId}'s control triggers besides the card abilities the trigger
     * pipeline matches and the emblems' abilities ({@link #checkEmblemEntersTriggers}): evolve (CR 702.100d), the
     * watchers and boons a resolved spell left, and the "whenever a creature you control enters" abilities a spell
     * registered until end of turn.
     */
    public void checkAllyCreatureEntersTriggers(GameData gameData, UUID controllerId, Card enteringCreature) {
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
                for (int i = 0; i < evolveInstances; i++) {
                    putEvolveAbility(gameData, controllerId, perm, enteringPermanent);
                }
            }
        }

        for (CreatureEntersTriggerWatcher watcher
                : List.copyOf(gameData.allyCreatureEntersTriggerWatchers)) {
            if (!watcher.controllerId().equals(controllerId) || enteringPermanent == null) {
                continue;
            }
            StackEntry entry = TriggeredAbilityStackService.madeAbility(watcher.sourceCard(),
                    watcher.controllerId(),
                    watcher.sourceCard().getName() + "'s ability",
                    new ArrayList<>(List.of(watcher.effect())));
            entry.setTargetId(enteringPermanent.getId());
            entry.setNonTargeting(true);
            triggeredAbilityStackService.putDelayed(gameData, entry);
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

            StackEntry entry = TriggeredAbilityStackService.madeAbility(watcher.sourceCard(), watcher.controllerId(),
                    watcher.sourceCard().getName() + "'s ability", temporaryEffects(watcher));
            entry.setNonTargeting(true);
            triggeredAbilityStackService.putDelayed(gameData, entry);
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
            StackEntry entry = TriggeredAbilityStackService.madeAbility(boon.sourceCard(),
                    boon.controllerId(), boon.sourceCard().getName() + "'s boon",
                    new ArrayList<>(List.of(boon.effect())));
            entry.setTriggeringCardId(spellCard.getId());
            entry.setNonTargeting(true);
            triggeredAbilityStackService.putDelayed(gameData, entry);
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
                    StackEntry perCreatureEntry = TriggeredAbilityStackService.madeAbility(boon.sourceCard(),
                            boon.controllerId(), boon.sourceCard().getName() + "'s boon", List.of(resolved));
                    perCreatureEntry.setTargetId(matching.getId());
                    perCreatureEntry.setTriggeringPermanentId(matching.getId());
                    perCreatureEntry.setTriggeringCardId(matching.getCard().getId());
                    perCreatureEntry.setNonTargeting(true);
                    triggeredAbilityStackService.putDelayed(gameData, perCreatureEntry);
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
            StackEntry entry = TriggeredAbilityStackService.madeAbility(boon.sourceCard(), boon.controllerId(),
                    boon.sourceCard().getName() + "'s boon", List.of(resolved), enteringPermanent.getId(),
                    sourcePermanent == null ? null : sourcePermanent.getId());
            entry.setTriggeringPermanentId(enteringPermanent.getId());
            entry.setTriggeringPermanentControllerId(controllerId);
            entry.setTriggeringCardId(enteringPermanent.getCard().getId());
            entry.rememberLastKnownPermanentCard(enteringPermanent.getId(), enteringPermanent.getCard());
            entry.setNonTargeting(true);
            if (boonTargetFilter != null
                    && resolved.targetSpec().admits(TargetPredicate.Kind.PERMANENT)) {
                // "Attached to one of them": its target, restricted by the boon's filter, is chosen as it goes on the
                // stack (CR 603.3d).
                entry.setTargetFilter(boonTargetFilter);
                triggeredAbilityStackService.putMade(gameData, entry);
            } else {
                triggeredAbilityStackService.putDelayed(gameData, entry);
            }
            gameLogService.append(gameData, GameLog.abilityTriggers(boon.sourceCard()));
        }
    }

    /** Collects creature-entry triggers from face-up planes, which are not battlefield permanents. */
    public void checkPlanarCreatureEntersTriggers(GameData gameData, UUID enteringCreatureControllerId,
                                                  Card enteringCreature) {
        if (gameData.planechase == null || gameData.planechase.controllerId == null
                || !enteringPermanentHasType(gameData, enteringCreature, CardType.CREATURE)) {
            return;
        }
        putPlanarEnteringAbilities(gameData, enteringCreatureControllerId, enteringCreature,
                ZoneChangeWatch.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD);
    }

    /**
     * Puts each of the face-up planes' "whenever [an object] enters" abilities that one of {@code watches} collects and
     * whose trigger's filter the entering permanent matches on the stack through the stack door (a plane isn't on the
     * battlefield, so the trigger pipeline doesn't match its abilities, and no "triggers an additional time" effect for
     * a permanent's abilities counts it): its event condition is judged here, and its record names the permanent that
     * entered ({@link #recordEnteringPermanent}) and the plane as its source.
     */
    private void putPlanarEnteringAbilities(GameData gameData, UUID enteringControllerId, Card enteringCard,
                                            ZoneChangeWatch... watches) {
        UUID planarControllerId = gameData.planechase.controllerId;
        Permanent enteringPermanent = findPermanentByCard(gameData, enteringCard);
        UUID enteringPermanentId = enteringPermanent == null ? null : enteringPermanent.getId();
        for (PlanarObject object : List.copyOf(gameData.planechase.faceUp)) {
            Card plane = object.getCard();
            ConditionContext context = ConditionContext.forCard(plane, planarControllerId)
                    .withTriggeringPermanentId(enteringPermanentId);
            for (TriggeredAbility ability : planarEnteringAbilities(gameData, plane, planarControllerId,
                    enteringPermanent, watches)) {
                if (!conditionEvaluationService.eventConditionMet(gameData, ability.options(), context)) {
                    continue;
                }
                PlanarObject source = object.copy();
                boolean triggered = triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(ability,
                        plane, null, planarControllerId, plane.getName() + "'s ability", context, entry -> {
                            entry.setNonTargeting(true);
                            entry.setSourcePlanarObject(source);
                            recordEnteringPermanent(gameData, entry, enteringCard, enteringPermanent,
                                    enteringControllerId);
                        }).withEvent(new TriggerContext.PermanentEnters(enteringCard, enteringControllerId, null, 1,
                        enteringPermanentId)));
                if (triggered) {
                    gameLogService.append(gameData, GameLog.abilityTriggers(plane));
                }
            }
        }
    }

    /**
     * Records the permanent an enter event was about on {@code entry}: "it" and "that creature" (the triggering
     * permanent, read as the ability resolves), "its controller", its power and toughness as the ability triggered,
     * the card it entered as, and that card as it last existed on the battlefield should it leave before the ability
     * resolves (CR 608.2h), and the player or planeswalker it entered attacking.
     *
     * @param entering             the entering permanent, or {@code null} when it already left the battlefield (only
     *                             the card it entered as and the player it entered under are recorded then)
     * @param enteringControllerId the player it entered under, its controller when it can't be looked up
     */
    private void recordEnteringPermanent(GameData gameData, StackEntry entry, Card enteringCard, Permanent entering,
                                         UUID enteringControllerId) {
        entry.setTriggeringCardSnapshot(enteringCard);
        if (entering == null) {
            entry.setTriggeringPermanentControllerId(enteringControllerId);
            return;
        }
        UUID controllerId = gameQueryService.findPermanentController(gameData, entering.getId());
        entry.setTriggeringPermanentId(entering.getId());
        entry.setTriggeringPermanentIds(List.of(entering.getId()));
        entry.setTriggeringCardId(entering.getCard().getId());
        entry.setTriggeringPermanentControllerId(controllerId != null ? controllerId : enteringControllerId);
        entry.setTriggeringPermanentPowerAtTrigger(gameQueryService.getEffectivePower(gameData, entering));
        entry.setTriggeringPermanentToughnessAtTrigger(gameQueryService.getEffectiveToughness(gameData, entering));
        entry.rememberLastKnownPermanentCard(entering.getId(), entering.getCard());
        entry.setAttachedPermanentSnapshot(new Permanent(entering));
        if (entering.isAttacking()) {
            UUID attackedTargetId = entering.getAttackTarget();
            entry.setAttackedTargetId(attackedTargetId);
            entry.setDefendingPlayerId(gameQueryService.defendingPlayerOfAttackTarget(gameData, attackedTargetId));
        }
    }

    /**
     * Each of {@code plane}'s "whenever [an object] enters" abilities that one of {@code watches} collects and whose
     * trigger's filter {@code entering} matches as it entered.
     */
    private List<TriggeredAbility> planarEnteringAbilities(GameData gameData, Card plane, UUID planarControllerId,
                                                           Permanent entering, ZoneChangeWatch... watches) {
        List<ZoneChangeWatch> watched = List.of(watches);
        FilterContext context = FilterContext.of(gameData).withSourceControllerId(planarControllerId)
                .withSourceCardId(plane.getId());
        List<TriggeredAbility> abilities = new ArrayList<>();
        for (TriggeredAbility ability : plane.getTriggeredAbilities()) {
            boolean matched = ability.conditions().stream().anyMatch(condition ->
                    condition.trigger() instanceof ZoneChangeTrigger trigger
                            && watches(watched, ZoneChangeWatch.of(ability, trigger, Zone.BATTLEFIELD))
                            && (trigger.filter() == null || entering != null && predicateEvaluationService
                            .matchesPermanentPredicate(entering, trigger.filter(), context)));
            if (matched) {
                abilities.add(ability);
            }
        }
        return abilities;
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
        // Nyx's "a nontoken enchantment you control" narrows the any-permanent trigger to the enchantment watch's.
        putPlanarEnteringAbilities(gameData, enteringControllerId, enteringCard,
                ZoneChangeWatch.ON_ALLY_PERMANENT_ENTERS_BATTLEFIELD,
                ZoneChangeWatch.ON_ALLY_ENCHANTMENT_ENTERS_BATTLEFIELD);
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
                // The delayed ability the spell made ("whenever a creature enters this turn, you may draw a card"),
                // its "you may" asked as it resolves (CR 603.5).
                StackEntry entry = TriggeredAbilityStackService.madeAbility(sourceCard, controllerId,
                        sourceCard.getName() + "'s ability",
                        List.of(new MayEffect(new DrawCardEffect(), "Draw a card?")));
                entry.setNonTargeting(true);
                triggeredAbilityStackService.putDelayed(gameData, entry);
                gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
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

            StackEntry entry = TriggeredAbilityStackService.madeAbility(watcher.sourceCard(), watcher.controllerId(),
                    watcher.sourceCard().getName() + "'s ability", temporaryEffects(watcher));
            entry.setNonTargeting(true);
            triggeredAbilityStackService.putDelayed(gameData, entry);
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

            List<CardEffect> resolvedEffects = temporaryEffects(watcher);

            // "A copy of that creature" (Theoretical Duplication) is the stack entry's triggering permanent.
            enqueueTemporaryGlobalTrigger(gameData, watcher, resolvedEffects, enteringPermanent.getId(), 0,
                    enteringPermanent.getId());
        }
    }

    /**
     * Puts Judgment of Alexander's "whenever damage from a creature is prevented this way, each commander creature you
     * control deals damage equal to its power to that creature" on the stack: a delayed triggered ability the spell made
     * as it resolved, with the spell as its source and its controller, the protected player, as its controller
     * (CR 603.7d). "That creature" is the creature whose damage was prevented, which it doesn't target.
     */
    public void checkCommanderDamagePreventedTriggers(GameData gameData, UUID damagedPlayerId,
                                                       UUID preventedCreatureId, Card sourceCard) {
        if (damagedPlayerId == null || sourceCard == null) return;

        StackEntry entry = TriggeredAbilityStackService.madeAbility(sourceCard, damagedPlayerId,
                sourceCard.getName() + "'s triggered ability",
                List.of(new CommanderCreaturesDealPowerDamageToPreventedCreatureEffect()), preventedCreatureId,
                (UUID) null);
        entry.setNonTargeting(true);
        triggeredAbilityStackService.putDelayed(gameData, entry);
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
                StackEntry entry = TriggeredAbilityStackService.madeAbility(sourceCard,
                        controllerId,
                        sourceCard.getName() + "'s delayed ability",
                        List.of(new UntapPermanentsEffect(TapUntapScope.ATTACKED_CREATURES)));
                entry.setNonTargeting(true);
                triggeredAbilityStackService.putDelayed(gameData, entry);
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
     * Puts {@code ability} of {@code emblem} on the stack through the stack door: an ability of an emblem functions in
     * the command zone (CR 114.4), {@code source} its source, and it records the event with {@code record}. Its targets
     * are only those its effects or its own target filter declare: no ability of the card that made the emblem is it.
     */
    private void putEmblemAbility(GameData gameData, Emblem emblem, TriggeredAbility ability, Card source,
                                  String description, ConditionContext ifContext, TriggerRecord record,
                                  boolean creatureDying) {
        TriggeredAbilityRequest request = new TriggeredAbilityRequest(ability, source, null, emblem.controllerId(),
                description, ifContext, entry -> {
                    entry.setMadeByResolution(true);
                    record.stamp(entry);
                }).ofEmblem();
        triggeredAbilityStackService.put(gameData, creatureDying ? request.withCreatureDying() : request);
    }

    /**
     * The effects of {@code watcher}'s ability, every one of them as written (CR 603.3), its intervening "if" wrapped
     * around each to be judged again as it resolves (CR 603.4).
     */
    private static List<CardEffect> temporaryEffects(TemporaryGlobalTriggeredAbility watcher) {
        Condition interveningIf = watcher.ability().options().interveningIf();
        return watcher.ability().effects().stream()
                .map(EffectRegistration::effect)
                .map(effect -> interveningIf == null ? effect : (CardEffect) new ConditionalEffect(interveningIf, effect))
                .toList();
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
        putEmblemAbility(gameData, emblem, ability, source, description,
                ConditionContext.forCard(source, emblem.controllerId()).withTriggeringPermanentId(triggering.getId()),
                entry -> {
                    entry.setTriggeringPermanentId(triggering.getId());
                    entry.setTriggeringPermanentControllerId(triggeringControllerId);
                    entry.setTriggeringCardId(triggering.getCard().getId());
                    entry.setNonTargeting(true);
                }, false);
        gameLogService.append(gameData, GameLog.text(description + " triggers for " + triggering.getCard().getName()
                + " " + event + "."));
        log.info("Game {} - {} triggers for {} {}", gameData.id, description, triggering.getCard().getName(), event);
    }

    /**
     * Fires token-entry abilities for a token-creation batch. Ordinary abilities use one trigger
     * for the batch, while abilities that branch on their resolution count receive one trigger for
     * each token in the batch.
     */
    public void checkAllyTokenEntersTriggers(GameData gameData, UUID controllerId, int count) {
        checkAllyTokenEntersTriggers(gameData, controllerId, count, List.of());
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

        for (TriggeredAbility ability : watchedAbilities(gameData, enchanted, MiscWatch.ON_AURA_ATTACHED_TO_SELF)) {
            putEventAbility(gameData, ability, enchanted.getCard(), enchanted, controllerId,
                    ConditionContext.forPermanent(enchanted, controllerId), null,
                    eventRecord(enchanted, null, null, null, null, null));
        }
    }

    /**
     * "Whenever an Aura becomes attached to this creature", then "whenever an Aura you control becomes attached to a
     * creature you control" (Siona) and "whenever an Aura you control becomes attached to a nonland permanent an
     * opponent controls with mana value less than or equal to that Aura's mana value" (Eriette): each ability handed to
     * the stack door whole (CR 603.3), the Aura as the triggering permanent ("that Aura") and, for Eriette's, the
     * enchanted permanent as the non-targeting "that permanent".
     */
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
            for (Permanent watcher : List.copyOf(gameData.playerBattlefields.getOrDefault(auraControllerId,
                    List.of()))) {
                for (TriggeredAbility ability : watchedAbilities(gameData, watcher,
                        MiscWatch.ON_ALLY_AURA_ATTACHED_TO_ALLY_CREATURE)) {
                    putEventAbility(gameData, ability, watcher.getCard(), watcher, auraControllerId,
                            ConditionContext.forPermanent(watcher, auraControllerId), null,
                            eventRecord(watcher, null, null, null, aura, auraControllerId));
                }
            }
        }
        if (auraControllerId == null || auraControllerId.equals(enchantedControllerId)
                || gameQueryService.isLand(gameData, enchanted)
                || enchanted.getCard().getManaValue() > auraCard.getManaValue()) {
            return;
        }
        for (Permanent watcher : List.copyOf(gameData.playerBattlefields.getOrDefault(auraControllerId, List.of()))) {
            for (TriggeredAbility ability : watchedAbilities(gameData, watcher,
                    MiscWatch.ON_ALLY_AURA_ATTACHED_TO_OPPONENT_NONLAND_PERMANENT)) {
                putEventAbility(gameData, ability, watcher.getCard(), watcher, auraControllerId,
                        ConditionContext.forPermanent(watcher, auraControllerId), null,
                        eventRecord(watcher, enchanted.getId(), null, null, aura, auraControllerId));
            }
        }
    }

    /**
     * After {@code equipment} became attached to a creature: its "as this Equipment becomes attached to a creature"
     * abilities apply while it becomes attached, without using the stack ({@link AsThisBecomesAttachedEffect}: a color,
     * a creature card name and a creature type, a copy of a creature card exiled with it), and its "whenever this
     * Equipment becomes attached to a creature" abilities trigger.
     */
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

        for (CardEffect effect : gameQueryService.getActiveStaticEffects(gameData, equipment)) {
            if (effect instanceof AsThisBecomesAttachedEffect asAttaches) {
                applyAsBecomesAttached(gameData, equipment, equipped, controllerId, asAttaches.effect());
            }
        }

        MiscWatch watch = MiscWatch.ON_EQUIPMENT_ATTACHED;
        for (TriggeredAbility ability : MiscAbilityReader.printed(equipment.getCard(), watch)) {
            // The door judges and marks "this ability triggers only once each turn" (Inchblade Companion).
            putEventAbility(gameData, ability, equipment.getCard(), equipment,
                    controllerId, ConditionContext.forPermanent(equipment, controllerId), null,
                    eventRecord(equipment, null, null, null, equipped, controllerId));
        }
        beginStackDoorChoices(gameData);
    }

    /**
     * Applies {@code effect}, an "as this Equipment becomes attached to a creature" ability of {@code equipment}, as it
     * becomes attached to {@code equipped}: its controller chooses a color, or a creature card name and then a creature
     * type, for the Equipment; or the creature becomes a copy of a creature card exiled with the Equipment.
     */
    private void applyAsBecomesAttached(GameData gameData, Permanent equipment, Permanent equipped,
                                        UUID controllerId, CardEffect effect) {
        if (effect instanceof ChooseColorForSourceEffect) {
            playerInputService.beginColorChoice(gameData, controllerId, equipment.getId(), null);
        } else if (effect instanceof SequenceEffect sequence && sequence.steps().size() == 2
                && sequence.steps().get(0) instanceof ChooseCardNameAtResolutionEffect nameChoice
                && sequence.steps().get(1) instanceof ChooseSubtypeForSourceEffect subtypeChoice) {
            chooseCardNameHandler.begin(gameData, equipment.getCard(), controllerId, equipment.getId(),
                    nameChoice, subtypeChoice);
        } else if (effect instanceof AttachedCreatureBecomesCopyOfExiledCreatureEffect) {
            // The attachment's record, never put on the stack: the Equipment and the creature it became attached to.
            StackEntry attachment = StackEntry.carrier(equipment.getCard(), controllerId,
                    equipment.getCard().getName() + "'s ability", List.of(effect), null, equipment.getId());
            attachment.setTriggeringPermanentId(equipped.getId());
            attachedCreatureCopyHandler.resolve(gameData, attachment, effect);
        } else {
            throw new IllegalStateException(equipment.getCard().getName()
                    + " has an \"as this becomes attached\" ability nothing applies: " + effect);
        }
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
        if (playZone != Zone.HAND) {
            gameData.playersWhoPlayedOrCastFromOutsideHandThisTurn.add(playingPlayerId);
        }
        SpellEvent.LandPlayed event = new SpellEvent.LandPlayed(playingPlayerId, landCard, playZone,
                exiledSourcePermanentId);
        gameData.rulesEvents.separately(() -> gameData.rulesEvents.emit(event));
    }

    /**
     * The match context of an ability a resolving ability registered: no permanent of its own, its source card the
     * registering card, and the permanent that registered it as its "this" (CR 603.7e).
     */
    private static TriggerMatchContext registeredMatch(GameData gameData, TemporaryGlobalTriggeredAbility watcher) {
        return new TriggerMatchContext(gameData, null, watcher.controllerId(), temporaryEffects(watcher),
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

        Permanent landPermanent = findPermanentByCard(gameData, enteringLand);
        for (PlanarObject object : List.copyOf(gameData.planechase.faceUp)) {
            // Each ability of the plane goes on the stack whole (CR 603.3); "that player" is the land's controller.
            Card plane = object.getCard();
            for (TriggeredAbility ability : slot.abilitiesOf(plane)) {
                PlanarObject source = object.copy();
                boolean triggered = triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(ability,
                        plane, null, planarControllerId, plane.getName() + "'s ability",
                        ConditionContext.forCard(plane, planarControllerId).withTargetId(landControllerId), entry -> {
                            entry.setTargetId(landControllerId);
                            entry.setTriggeringPermanentControllerId(landControllerId);
                            if (landPermanent != null) entry.setTriggeringPermanentId(landPermanent.getId());
                            entry.setSourcePlanarObject(source);
                            entry.setNonTargeting(true);
                        }));
                if (triggered) {
                    gameLogService.append(gameData, GameLog.abilityTriggers(plane));
                }
            }
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

            StackEntry voyage = TriggeredAbilityStackService.madeAbility(exiled.card(), controllerId,
                    exiled.card().getName() + "'s ability", List.of(new MayEffect(
                            new PutVoyageCounterOnExiledCardEffect(),
                            "Put a voyage counter on Cosima?",
                            new ReturnVoyagingCardFromExileEffect())));
            voyage.setNonTargeting(true);
            triggeredAbilityStackService.putDelayed(gameData, voyage);
            gameLogService.append(gameData, GameLog.abilityTriggers(exiled.card()));
            log.info("Game {} - {} exiled voyage landfall trigger queued", gameData.id,
                    exiled.card().getName());
        }
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
                        match.gameData(), match.controllerId(), match.permanent(), false, context)
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

    /**
     * Puts one instance of {@code source}'s evolve (CR 702.100a) on the stack through the stack door for
     * {@code enteringPermanent}, a creature that entered under its controller's control: the door judges its
     * intervening "if" as it triggers and wraps its effect so it is checked again as it resolves (CR 603.4), and counts
     * the additional times it triggers for the entering creature (an entering creature causing it, Panharmonicon).
     */
    private void putEvolveAbility(GameData gameData, UUID controllerId, Permanent source,
                                  Permanent enteringPermanent) {
        if (gameQueryService.areOpponentPermanentETBTriggersSuppressed(gameData, controllerId)) {
            return;
        }
        UUID enteringId = enteringPermanent.getId();
        boolean triggered = triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(EVOLVE_ABILITY,
                source.getCard(), source, controllerId, source.getCard().getName() + "'s ability",
                ConditionContext.forPermanent(source, controllerId).withTriggeringPermanentId(enteringId),
                entry -> {
                    entry.setTriggeringPermanentId(enteringId);
                    entry.setNonTargeting(true);
                }).withEvent(new TriggerContext.PermanentEnters(enteringPermanent.getCard(), controllerId, null, 1,
                enteringId)));
        if (triggered) {
            gameLogService.append(gameData, GameLog.abilityTriggers(source.getCard()));
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

    private boolean enteringPermanentHasType(GameData gameData, Card card, CardType type) {
        Permanent permanent = findPermanentByCard(gameData, card);
        if (permanent == null) return card.hasType(type);
        return switch (type) {
            case CREATURE -> gameQueryService.isCreature(gameData, permanent);
            case ENCHANTMENT -> gameQueryService.isEnchantment(gameData, permanent);
            default -> card.hasType(type);
        };
    }

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
     * Dispatches the abilities {@code watch} reads: the permanent's own abilities with its trigger, and those other
     * permanents grant it, each dispatched to {@code watch}'s handlers.
     */
    private boolean dispatchWatch(GameData gameData, Permanent perm, UUID controllerId, TriggerWatch watch,
                                  TriggerContext ctx) {
        if (watch instanceof LibraryWatch || watch instanceof ActionWatch || watch instanceof MiscWatch
                || watch instanceof SagaWatch) {
            return putFamilyAbilities(gameData, perm, controllerId, watch, ctx);
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
            // "Whenever you choose to put one or more cards on the bottom of your library while scrying" (The Temporal
            // Anchor): the number of cards put on the bottom is the event's value, which an event condition reads.
            case TriggerContext.Scry scry -> conditionEvaluationService.judge(gameData,
                    context.withTargetId(scry.scryingPlayerId()), scry.bottomedCardCount());
            case null, default -> conditionEvaluationService.judge(gameData, context);
        };
    }

    /**
     * Hands each ability the library, player or keyword action, misc or Saga watch {@code watch} reads of {@code perm}
     * (its printed ones through its text changes, then those other permanents' static abilities grant it; none once it
     * has lost them) to the stack door for the event {@code ctx}, as {@link #putFamilyAbility} says.
     *
     * @return whether one triggered
     */
    private boolean putFamilyAbilities(GameData gameData, Permanent perm, UUID controllerId, TriggerWatch watch,
                                       TriggerContext ctx) {
        boolean triggered = false;
        for (TriggeredAbility ability : watchedAbilities(gameData, perm, watch)) {
            triggered |= putFamilyAbility(gameData, perm, perm.getCard(), controllerId, watch, ctx, ability);
        }
        return triggered;
    }

    /**
     * Hands {@code ability} of {@code sourceCard} ({@code perm} on the battlefield, or {@code null} for a card in
     * another zone), controlled by {@code controllerId}, to the stack door for the event {@code ctx} of the library,
     * player or keyword action, misc or Saga watch {@code watch}, once the trigger-level parts its reader reads match the
     * event ({@link #familyEventMatches}), as {@link #putJudgedFamilyAbility} says.
     *
     * @return whether it triggered
     */
    private boolean putFamilyAbility(GameData gameData, Permanent perm, Card sourceCard, UUID controllerId,
                                     TriggerWatch watch, TriggerContext ctx, TriggeredAbility ability) {
        return familyEventMatches(gameData, perm, sourceCard, controllerId, watch, ctx, ability)
                && putJudgedFamilyAbility(gameData, perm, sourceCard, controllerId, watch, ctx, ability);
    }

    /**
     * Hands {@code ability}, whose trigger-level parts its collector judged against the event {@code ctx}, to the stack
     * door: its event condition is judged as it triggers in {@link #familyContext} (CR 603.2), the door judges its
     * intervening "if" (CR 603.4) and limit, counts the additional times it triggers and chooses its targets as it goes
     * on the stack (CR 603.3d), and its entry records the event whatever its effects are ({@link #familyRecord}).
     * "Becomes crewed / saddled for the first time each turn" is the ability's own mark, judged and marked here.
     *
     * @return whether it triggered
     */
    private boolean putJudgedFamilyAbility(GameData gameData, Permanent perm, Card sourceCard, UUID controllerId,
                                           TriggerWatch watch, TriggerContext ctx, TriggeredAbility ability) {
        UUID sourceId = perm != null ? perm.getId() : sourceCard.getId();
        MiscAbilityReader.MiscEffect firstTime = watch instanceof MiscWatch miscWatch
                ? MiscAbilityReader.read(ability, miscWatch.trigger()).getFirst() : null;
        if (firstTime != null && firstTime.firstTimeReached(gameData, sourceId)) {
            return false;
        }
        boolean triggered = putEventAbility(gameData, ability, sourceCard, perm, controllerId,
                familyContext(perm, sourceCard, controllerId, ctx), ctx,
                familyRecord(gameData, perm, controllerId, ctx));
        if (triggered && firstTime != null) {
            firstTime.markFirstTime(gameData, sourceId);
        }
        return triggered;
    }

    /**
     * The context a library, player or keyword action, misc or Saga ability's event condition and intervening "if" are
     * judged in as it triggers: its source and controller, with the result of the die roll as the event's value, and
     * the scrying player as "that player" with the number of cards put on the bottom as the event's value ("whenever
     * you choose to put one or more cards on the bottom of your library while scrying", The Temporal Anchor).
     */
    private static ConditionContext familyContext(Permanent perm, Card sourceCard, UUID controllerId,
                                                  TriggerContext ctx) {
        ConditionContext context = perm != null ? ConditionContext.forPermanent(perm, controllerId)
                : ConditionContext.forCard(sourceCard, controllerId);
        return switch (ctx) {
            case TriggerContext.DiceRoll diceRoll -> context.withEventValue(diceRoll.result());
            case TriggerContext.Scry scry -> context.withTargetId(scry.scryingPlayerId())
                    .withEventValue(scry.bottomedCardCount());
            case null, default -> context;
        };
    }

    /**
     * Whether the event {@code ctx} matches {@code ability} as the collector of the library, player or keyword action,
     * misc or Saga watch {@code watch} reads it: the discarded cards' filter and "for the first time each turn"
     * ({@link #watchedLibraryEventMatches}); the card the action is taken with against the action's filter
     * ({@link #actionCard}: the exploited creature, the scheme set in motion); a scheme set in motion by the ability's
     * controller; a Ring-bearer other than the source being chosen; a door ability's own door; the crewing creature
     * against "this or another legendary creature you control"; the Saga's controller for "a Saga you control".
     */
    private boolean familyEventMatches(GameData gameData, Permanent perm, Card sourceCard, UUID controllerId,
                                       TriggerWatch watch, TriggerContext ctx, TriggeredAbility ability) {
        switch (watch) {
            case LibraryWatch libraryWatch -> {
                return watchedLibraryEventMatches(gameData, controllerId, libraryWatch, ctx,
                        LibraryAbilityReader.read(ability, libraryWatch.trigger()).getFirst());
            }
            case ActionWatch actionWatch -> {
                ActionAbilityReader.ActionEffect first = ActionAbilityReader.read(ability, actionWatch.trigger())
                        .getFirst();
                if (first.filter() != null) {
                    Card actionCard = actionCard(ctx);
                    if (actionCard == null) {
                        throw new IllegalStateException("The collector of " + watch + " judges no filter: "
                                + ability);
                    }
                    if (!predicateEvaluationService.matchesCardPredicate(actionCard, first.filter(),
                            sourceCard.getId(), gameData, controllerId)) {
                        return false;
                    }
                }
                return switch (ctx) {
                    case TriggerContext.SchemeSetInMotion scheme -> scheme.schemeEntry() != null
                            && scheme.schemeEntry().getCard() != null
                            && scheme.settingPlayerId().equals(controllerId);
                    case TriggerContext.RingTempted tempted -> perm != null
                            && !perm.getId().equals(tempted.ringBearerId());
                    default -> true;
                };
            }
            case MiscWatch miscWatch -> {
                MiscAbilityReader.MiscEffect first = MiscAbilityReader.read(ability, miscWatch.trigger()).getFirst();
                if (miscWatch == MiscWatch.ON_SELF_ROOM_DOOR_UNLOCKED) {
                    if (first.door() == null) {
                        throw new IllegalStateException("A \"when you unlock this door\" ability names its door: "
                                + ability);
                    }
                    return ctx instanceof TriggerContext.RoomDoorUnlocked unlocked
                            && unlocked.doorIndex() == first.door();
                }
                if (miscWatch == MiscWatch.ON_ALLY_CREATURE_CREWS_VEHICLE) {
                    return ctx instanceof TriggerContext.CreatureCrewsVehicle crew && (first.objectFilter() == null
                            || predicateEvaluationService.matchesPermanentPredicate(crew.crewingCreature(),
                            first.objectFilter(), FilterContext.of(gameData)
                                    .withSourceCardId(sourceCard.getId())
                                    .withSourceControllerId(controllerId)
                                    .withSourcePermanentId(perm.getId())
                                    .withSourcePermanentSnapshot(perm)));
                }
                return true;
            }
            case SagaWatch ignored -> {
                return ctx instanceof TriggerContext.SagaFinalChapterAbilityResolved resolved
                        && controllerId.equals(resolved.sagaControllerId());
            }
            default -> throw new IllegalArgumentException("Not a library, action, misc or Saga watch: " + watch);
        }
    }

    /**
     * The record of a library, player or keyword action, misc or Saga event, stamped on the ability's entry whatever
     * its effects are, with the source as it was when the ability triggered (CR 608.2h):
     * <ul>
     *   <li>the player who discarded, cycled (CR 702.29a), scried, surveilled, sought, collected evidence,
     *       investigated, foretold or paid the tax as its triggering player, and an opponent's one as its non-targeting
     *       "that player" (a discard's discarder; a scry's, a surveil's and a tax payment's opponent);</li>
     *   <li>the cards discarded, the cards sought, the card foretold and the card manifest dread put into the
     *       graveyard as its triggering cards, each with the graveyard stay it went to (CR 400.7);</li>
     *   <li>the number of cards discarded at once, put on the bottom while scrying, conjured, the discover value, the
     *       die roll's result, the mana paid for the tax, the exploited creature's power and the Saga's mana value as
     *       its event value;</li>
     *   <li>the permanent the event was about, as "that creature" or "that Vehicle": the exerted, conniving, mutated,
     *       stationing and changed-control one (with its new controller), the Vehicle crewed, the creature that
     *       fought, the Room fully unlocked;</li>
     *   <li>the scheme set in motion as its triggering spell, and the vote as the players finished it.</li>
     * </ul>
     */
    private static TriggerRecord familyRecord(GameData gameData, Permanent source, UUID controllerId,
                                              TriggerContext ctx) {
        Permanent snapshot = source == null ? null : new Permanent(source);
        TriggerRecord event = familyEventRecord(gameData, controllerId, ctx);
        return entry -> {
            entry.setNonTargeting(true);
            if (snapshot != null) {
                entry.setSourcePermanentSnapshot(snapshot);
            }
            event.stamp(entry);
        };
    }

    /** What {@link #familyRecord} stamps of the event {@code ctx} itself. */
    private static TriggerRecord familyEventRecord(GameData gameData, UUID controllerId, TriggerContext ctx) {
        return switch (ctx) {
            case TriggerContext.Discard ignored -> DiscardRecords.record(gameData, ctx);
            case TriggerContext.Cycle ignored -> DiscardRecords.record(gameData, ctx);
            case TriggerContext.DiscardEvent ignored -> DiscardRecords.record(gameData, ctx);
            case TriggerContext.Scry scry -> entry -> {
                entry.setTriggeringPlayerId(scry.scryingPlayerId());
                entry.setEventValue(scry.bottomedCardCount());
                entry.setCardsLookedAtWhileScrying(scry.cardsLookedAt());
                if (!scry.scryingPlayerId().equals(controllerId)) {
                    entry.setTargetId(scry.scryingPlayerId());
                }
            };
            case TriggerContext.Surveil surveil -> entry -> {
                entry.setTriggeringPlayerId(surveil.surveilingPlayerId());
                if (!surveil.surveilingPlayerId().equals(controllerId)) {
                    entry.setTargetId(surveil.surveilingPlayerId());
                }
            };
            case TriggerContext.Seek seek -> entry -> {
                entry.setTriggeringPlayerId(seek.seekingPlayerId());
                entry.setTriggeringCards(seek.soughtCards());
            };
            case TriggerContext.CollectEvidence evidence ->
                    entry -> entry.setTriggeringPlayerId(evidence.collectingPlayerId());
            case TriggerContext.Investigate investigate ->
                    entry -> entry.setTriggeringPlayerId(investigate.investigatingPlayerId());
            case TriggerContext.Foretell foretell -> entry -> {
                entry.setTriggeringPlayerId(foretell.foretellingPlayerId());
                entry.setTriggeringCardId(foretell.foretoldCard().getId());
            };
            case TriggerContext.TaxPayment tax -> entry -> {
                entry.setTriggeringPlayerId(tax.payingPlayerId());
                entry.setTargetId(tax.payingPlayerId());
                entry.setEventValue(tax.manaPaid());
            };
            case TriggerContext.PermanentControlChanged change -> entry -> {
                entry.setTriggeringPlayerId(change.newControllerId());
                entry.setTargetId(change.newControllerId());
                entry.setTriggeringPermanentId(change.changedPermanent().getId());
                entry.setTriggeringPermanentControllerId(change.newControllerId());
            };
            case TriggerContext.ManifestDread manifestDread -> {
                Card graveyardCard = manifestDread.cardPutIntoGraveyard();
                long stay = graveyardCard == null ? 0L : gameData.graveyardEntryVersion(graveyardCard.getId());
                yield entry -> {
                    if (graveyardCard != null) {
                        entry.setTriggeringCard(graveyardCard.getId(), stay);
                    }
                };
            }
            case TriggerContext.DiceRoll diceRoll -> entry -> entry.setEventValue(diceRoll.result());
            case TriggerContext.Conjure conjure -> entry -> entry.setEventValue(conjure.cardCount());
            case TriggerContext.Discover discover -> entry -> entry.setEventValue(discover.discoverValue());
            case TriggerContext.CreatureExploit exploit -> entry -> entry.setEventValue(exploit.exploitedPower());
            case TriggerContext.SagaFinalChapterAbilityResolved resolved ->
                    entry -> entry.setEventValue(resolved.sagaManaValue());
            case TriggerContext.Exert exert -> entry -> entry.setTargetId(exert.exertedCreatureId());
            case TriggerContext.CreatureConnives connive -> entry -> {
                entry.setTargetId(connive.connivingCreature().getId());
                entry.setTriggeringPermanentId(connive.connivingCreature().getId());
            };
            case TriggerContext.CreatureMutates mutation -> entry -> {
                entry.setTargetId(mutation.mutatedPermanent().getId());
                entry.setTriggeringPermanentId(mutation.mutatedPermanent().getId());
            };
            case TriggerContext.CreatureStationed stationed ->
                    entry -> entry.setTargetId(stationed.creatureCard().getId());
            case TriggerContext.CreatureCrewsVehicle crew -> entry -> {
                entry.setTriggeringPermanent(crew.vehicle());
                entry.setTriggeringPermanentId(crew.vehicle().getId());
            };
            case TriggerContext.SchemeSetInMotion scheme -> entry -> {
                entry.setTriggeringCardId(scheme.schemeEntry().getCard().getId());
                entry.setTriggeringSpell(scheme.schemeEntry());
            };
            case TriggerContext.VotingFinished voting -> entry -> entry.setTriggeringVotingResult(voting.result());
            case TriggerContext.CreatureFights fights ->
                    entry -> entry.setTriggeringPermanentId(fights.fightingCreature().getId());
            case TriggerContext.RoomFullyUnlocked room -> entry -> entry.setTriggeringPermanentId(room.roomPermanentId());
            case null, default -> entry -> {
            };
        };
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

    /** Evolve's intervening "if" (CR 702.100a): the entering creature is bigger in power or toughness. */
    private static final Condition EVOLVE_COMPARISON = new TriggeringPermanentHasGreaterPowerOrToughnessThanSource();

    /**
     * Evolve (CR 702.100a): "Whenever a creature you control enters, if that creature's power is greater than this
     * creature's power and/or that creature's toughness is greater than this creature's toughness, put a +1/+1 counter
     * on this creature." The keyword's ability, which no card writes; each instance triggers separately (CR 702.100d).
     */
    private static final TriggeredAbility EVOLVE_ABILITY = new TriggeredAbility(
            Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
            TriggerOptions.DEFAULT.withInterveningIf(EVOLVE_COMPARISON), new EvolveEffect());

    /** "A nonland card", the discarded card the "first nonland discard each turn" ability counts. */
    private static final CardPredicate NONLAND_CARD = new CardNotPredicate(new CardTypePredicate(CardType.LAND));

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
     * Hands the "whenever you discard a card" abilities of {@code perm}, whose controller discarded
     * {@code discardedCard}, to the stack door: each ability's filter, own event ("you cycle a card", "an opponent's
     * spell or ability causes you to discard a card") and "for the first time each turn" are judged from the ability
     * ({@link #controllerDiscardEventMatches}); the door judges the rest ({@link #putJudgedFamilyAbility}).
     */
    private boolean collectControllerDiscardAbilities(GameData gameData, Permanent perm, UUID discardingPlayerId,
                                                      Card discardedCard, boolean cycled, boolean characteristicsKnown,
                                                      TriggerContext ctx) {
        LibraryWatch watch = LibraryWatch.ON_CONTROLLER_DISCARDS;
        boolean anyTriggered = false;
        for (TriggeredAbility ability : watchedAbilities(gameData, perm, watch)) {
            LibraryAbilityReader.LibraryEffect first = LibraryAbilityReader.read(ability, watch.trigger()).getFirst();
            if (discardedCardMatches(gameData, first, discardedCard, characteristicsKnown, discardingPlayerId)
                    && controllerDiscardEventMatches(gameData, first, discardingPlayerId, cycled)
                    && putJudgedFamilyAbility(gameData, perm, perm.getCard(), discardingPlayerId, watch, ctx,
                    ability)) {
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
        List<TriggeredAbility> printed = WatchedAbilities.printed(LayerSystemService.currentCard(gameData, perm), watch);
        if (printed.isEmpty() || gameQueryService.hasLostPrintedAbilities(gameData, perm)) {
            return List.of();
        }
        List<TriggeredAbility> abilities = new ArrayList<>(printed.size());
        for (TriggeredAbility ability : printed) {
            abilities.add(textChanged(gameData, perm, ability));
        }
        return abilities;
    }

    /** {@code ability} of {@code perm} read through its text changes (Layer 3, CR 612.1). */
    private static TriggeredAbility textChanged(GameData gameData, Permanent perm, TriggeredAbility ability) {
        List<TextReplacement> globalWordChange = TextChangeTransformer.globalColorWordReplacements(gameData);
        if (perm.getTextReplacements().isEmpty() && globalWordChange.isEmpty()) {
            return ability;
        }
        List<EffectRegistration> transformed = new ArrayList<>(ability.effects().size());
        boolean changed = false;
        for (EffectRegistration registration : ability.effects()) {
            CardEffect effect = TextChangeTransformer.transform(registration.effect(), perm.getTextReplacements(),
                    globalWordChange);
            changed |= effect != registration.effect();
            transformed.add(new EffectRegistration(effect, registration.simultaneousWithPrevious()));
        }
        return changed ? ability.withEffects(transformed) : ability;
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
     * power or toughness (the intervening-if; re-checked again at resolution per CR 603.4): the ability goes to the
     * stack door whole, which counts the times it triggers additionally. The mana spent is recorded as the entry's
     * {@code xValue} for the resolution handler.
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

            triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(INCREMENT_ABILITY, perm.getCard(),
                    perm, castingPlayerId, perm.getCard().getName() + "'s ability",
                    ConditionContext.forPermanent(perm, castingPlayerId), entry -> entry.setXValue(manaSpent)));
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

    /** Hands each "when this card becomes plotted" ability of {@code plottedCard} to the stack door, whole (CR 603.3). */
    public void checkPlotTriggers(GameData gameData, UUID plottingPlayerId, Card plottedCard) {
        for (TriggeredAbility ability : ActionAbilityReader.printed(plottedCard, ActionWatch.ON_SELF_BECOMES_PLOTTED)) {
            putEventAbility(gameData, ability, plottedCard, null, plottingPlayerId,
                    ConditionContext.forCard(plottedCard, plottingPlayerId), null, TriggerRecord.NONE);
        }
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
        holdForActivation(gameData, () -> dispatchPrintedMiscAbilities(gameData, spacecraft, controllerId,
                MiscWatch.ON_SELF_BECOMES_STATIONED, new TriggerContext.CreatureStationed(stationingCreature.getCard())));
    }

    public void checkBecomesSaddledTriggers(GameData gameData, Permanent saddledPermanent,
                                             UUID controllerId) {
        dispatchPrintedMiscAbilities(gameData, saddledPermanent, controllerId, MiscWatch.ON_SELF_BECOMES_SADDLED,
                new TriggerContext.SelfBecomesSaddled(controllerId));
    }

    /**
     * Hands the printed abilities {@code watch} reads of {@code perm} to the stack door, as {@link #putFamilyAbility}
     * says: "becomes crewed / saddled for the first time each turn" (Mindlink Mech, Stubborn Burrowfiend) is judged
     * and marked by the ability.
     */
    private void dispatchPrintedMiscAbilities(GameData gameData, Permanent perm, UUID controllerId, MiscWatch watch,
                                              TriggerContext context) {
        for (TriggeredAbility ability : printedWatchedAbilities(gameData, perm, watch)) {
            putFamilyAbility(gameData, perm, perm.getCard(), controllerId, watch, context, ability);
        }
    }

    /** Fires mutate triggers for all cards that currently make up the mutated permanent. */
    public void checkMutateTriggers(GameData gameData, Permanent mutatedPermanent,
                                    List<Card> mutatedCards, UUID controllerId) {
        mutatedPermanent.recordMutation();
        // Each ability of each card of the mutated permanent, whole (CR 603.3).
        TriggerContext context = new TriggerContext.SelfMutates(controllerId);
        for (Card mutatedCard : mutatedCards) {
            for (TriggeredAbility ability : ActionAbilityReader.printed(mutatedCard, ActionWatch.ON_SELF_MUTATES)) {
                putFamilyAbility(gameData, mutatedPermanent, mutatedCard, controllerId, ActionWatch.ON_SELF_MUTATES,
                        context, ability);
            }
        }

        TriggerContext creatureMutatesContext = new TriggerContext.CreatureMutates(mutatedPermanent, controllerId);
        for (Card mutatedCard : mutatedCards) {
            for (TriggeredAbility ability : ActionAbilityReader.printed(mutatedCard,
                    ActionWatch.ON_ALLY_CREATURE_MUTATES)) {
                putFamilyAbility(gameData, mutatedPermanent, mutatedCard, controllerId,
                        ActionWatch.ON_ALLY_CREATURE_MUTATES, creatureMutatesContext, ability);
            }
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield != null) {
            for (Permanent watcher : List.copyOf(battlefield)) {
                if (watcher.getId().equals(mutatedPermanent.getId())) continue;
                putFamilyAbilities(gameData, watcher, controllerId, ActionWatch.ON_ALLY_CREATURE_MUTATES,
                        creatureMutatesContext);
            }
        }
    }

}
