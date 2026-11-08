package com.github.laxika.magicalvibes.service.trigger.pipeline;

import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.rules.DepartedAttachments;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.condition.EntryFactCondition;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TargetSpec;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.rules.PermanentChangeEvent;
import com.github.laxika.magicalvibes.model.rules.ZoneChangeEvent;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.PermanentChangeTrigger;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerMultiplicity;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.ZoneChangeTrigger;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.effect.GraveyardTargetingSupport;
import com.github.laxika.magicalvibes.service.trigger.CreatureDeath;
import com.github.laxika.magicalvibes.service.trigger.CreatureDeathWatch;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.trigger.TriggerContext;
import com.github.laxika.magicalvibes.service.trigger.ZoneChangeWatch;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Puts a triggered ability that the pipeline matched on the stack, for every family moved off {@code EffectSlot}:
 *
 * <ul>
 *   <li>the intervening "if" is checked as it triggers, and wraps its effects so it is checked again as it
 *       resolves (CR 603.4);</li>
 *   <li>a limit stops it from triggering again: once each turn, "do this only once each turn" (CR 603.2h), which
 *       the source's controller uses up by taking the action, and once;</li>
 *   <li>effects that make abilities trigger an additional time (Teysa Karlov's, Panharmonicon-like ones) add
 *       copies.</li>
 * </ul>
 * An ability of one effect goes on the stack through {@link LegacyTriggerBindings}, with the event data its effect
 * needs bound in. Its options are the only trigger-level properties read: the source's own zone-change abilities
 * carry no trigger-only wrapper effects (step 14 of effect-slot-refactor.md). An ability of several
 * effects is one stack object (CR 603.3), with a "when this dies" ability's effects bound to the card that died: if it
 * targets, it waits for its targets as a
 * {@link PermanentChoiceContext.DeathTriggerTarget}, chosen through {@code TriggerTargetCollector} as it goes on
 * the stack and removed if none is legal (CR 603.3d), and otherwise it goes on the stack through
 * {@link GameData#enqueueTrigger}.
 * An ability that watches another object die ("whenever a creature you control dies", a relation other than
 * {@code THIS}) goes through {@link #triggerOnce} instead: its options are checked the same way, and its effects go on
 * the stack the way the old slot collector of its {@link CreatureDeathWatch} put them there. So does an ability of
 * one of the other zone-change families step 12a of effect-slot-refactor.md moved, through its
 * {@link ZoneChangeWatch}.
 * APNAP ordering (CR 603.3b) happens later, in {@code TriggeredAbilityQueueService.beginTriggerOrder}.
 */
@Service
public class TriggeredAbilityStackService {

    private final ConditionEvaluationService conditionEvaluationService;
    private final GameQueryService gameQueryService;
    private final GraveyardTargetingSupport graveyardTargetingSupport;
    private final GameLogService gameLogService;
    private final TriggerCollectionService triggerCollectionService;
    private final LegacyTriggerBindings legacyTriggerBindings;

    public TriggeredAbilityStackService(@Lazy ConditionEvaluationService conditionEvaluationService,
                                        @Lazy GameQueryService gameQueryService,
                                        @Lazy GraveyardTargetingSupport graveyardTargetingSupport,
                                        @Lazy GameLogService gameLogService,
                                        @Lazy TriggerCollectionService triggerCollectionService,
                                        LegacyTriggerBindings legacyTriggerBindings) {
        this.triggerCollectionService = triggerCollectionService;
        this.conditionEvaluationService = conditionEvaluationService;
        this.gameQueryService = gameQueryService;
        this.graveyardTargetingSupport = graveyardTargetingSupport;
        this.gameLogService = gameLogService;
        this.legacyTriggerBindings = legacyTriggerBindings;
    }

    /**
     * Triggers {@code candidate}'s ability for {@code event}, which met its trigger condition {@code trigger},
     * unless its options or wrappers keep it from triggering.
     */
    public void trigger(GameData gameData, TriggerCandidate candidate, ZoneChangeEvent event,
                        ZoneChangeTrigger trigger) {
        if (trigger.relation() != ObjectRelation.THIS
                || ZoneChangeWatch.of(candidate.ability(), trigger, candidate.zone()) != null) {
            triggerOnce(gameData, candidate, List.of(event), trigger);
            return;
        }
        TriggerOptions options = candidate.ability().options();
        Permanent source = candidate.sourceAsPermanent();
        Condition interveningIf = options.interveningIf();
        if (!conditionsMet(gameData, options,
                ConditionContext.forPermanent(source, candidate.controllerId()))) {
            return;
        }
        UUID sourceId = candidate.sourceObjectId();
        String limitKey = limitKey(candidate);
        if (limitReached(gameData, options.limit(), sourceId, limitKey)) {
            return;
        }
        List<EffectRegistration> registrations = candidate.ability().effects();
        LegacyTriggerBindings.Kind kind = LegacyTriggerBindings.kindOf(trigger);
        boolean triggered;
        if (registrations.size() == 1) {
            triggered = triggerBound(gameData, candidate, event, kind, registrations.getFirst().effect(),
                    interveningIf, options.limit());
        } else {
            triggered = triggerAll(gameData, candidate, event, kind, registrations, interveningIf, options);
        }
        if (triggered) {
            markLimit(gameData, options.limit(), sourceId, limitKey);
        }
    }

    /**
     * Triggers {@code candidate}'s "whenever [an object] dies" ability once for {@code events}, the deaths of one
     * batch that met its trigger condition {@code trigger}: once for the one death of an ability that triggers for
     * each, once for all of them for "one or more" (CR 603.2c). Its options are checked as for any ability; its
     * effects go on the stack the way the old slot collector of its {@link CreatureDeathWatch} put them there.
     */
    public void triggerOnce(GameData gameData, TriggerCandidate candidate, List<ZoneChangeEvent> events,
                            ZoneChangeTrigger trigger) {
        ZoneChangeWatch zoneChangeWatch = ZoneChangeWatch.of(candidate.ability(), trigger, candidate.zone());
        if (zoneChangeWatch == null && LegacyTriggerBindings.kindOf(trigger) != LegacyTriggerBindings.Kind.DIES) {
            return;
        }
        TriggerOptions options = candidate.ability().options();
        Permanent source = candidate.sourceAsPermanent();
        Condition interveningIf = options.interveningIf();
        if (!conditionsMet(gameData, options,
                triggeringContext(gameData, source, candidate.controllerId(), events.getFirst()))) {
            return;
        }
        UUID sourceId = candidate.sourceObjectId();
        String limitKey = limitKey(candidate);
        if (limitReached(gameData, options.limit(), sourceId, limitKey)) {
            return;
        }
        List<CardEffect> effects = new ArrayList<>();
        for (EffectRegistration registration : candidate.ability().effects()) {
            effects.add(withInterveningIf(interveningIf, registration.effect(),
                    trigger.to() == Zone.BATTLEFIELD));
        }
        if (zoneChangeWatch != null) {
            if (triggerCollectionService.triggerZoneChange(gameData, zoneChangeWatch, candidate, effects, events)) {
                markLimit(gameData, options.limit(), sourceId, limitKey);
            }
            return;
        }
        CreatureDeathWatch watch = CreatureDeathWatch.of(trigger, candidate.zone());
        List<CreatureDeath> deaths = events.stream().map(CreatureDeath::of).toList();
        boolean triggered = options.multiplicity() == TriggerMultiplicity.ONE_OR_MORE
                ? triggerCollectionService.triggerCreatureDeaths(gameData, watch, source, candidate.controllerId(),
                        effects, options.limit(), deaths)
                : triggerCollectionService.triggerCreatureDeath(gameData, watch, source, candidate.controllerId(),
                        effects, options.limit(), deaths.getFirst());
        if (triggered) {
            markLimit(gameData, options.limit(), sourceId, limitKey);
        }
    }

    /**
     * Triggers {@code candidate}'s ability once for {@code events}, the permanents phasing, transforming or being
     * turned face up in one batch that met its trigger condition {@code trigger}: one event for an ability that
     * triggers for each, all of them for "one or more". Its options are checked as for any ability; its effects go
     * on the stack the way the old slot collector of its {@link ZoneChangeWatch} put them there.
     */
    public void triggerPermanentChange(GameData gameData, TriggerCandidate candidate, List<PermanentChangeEvent> events,
                                       PermanentChangeTrigger trigger) {
        ZoneChangeWatch watch = ZoneChangeWatch.of(candidate.ability(), trigger, candidate.zone());
        if (watch == null) {
            return;
        }
        TriggerOptions options = candidate.ability().options();
        Condition interveningIf = options.interveningIf();
        // "If it's a creature" (Trail of Mystery) reads the permanent that changed, as the event's object.
        UUID changedId = events.getFirst().permanent().getId();
        if (!conditionsMet(gameData, options,
                ConditionContext.forPermanent(candidate.sourceAsPermanent(), candidate.controllerId())
                        .withTargetId(changedId).withTriggeringPermanentId(changedId))) {
            return;
        }
        UUID sourceId = candidate.sourceObjectId();
        String limitKey = limitKey(candidate);
        if (limitReached(gameData, options.limit(), sourceId, limitKey)) {
            return;
        }
        List<CardEffect> effects = new ArrayList<>();
        for (EffectRegistration registration : candidate.ability().effects()) {
            effects.add(withInterveningIf(interveningIf, registration.effect(), false));
        }
        if (triggerCollectionService.triggerPermanentChange(gameData, watch, candidate, effects, events)) {
            markLimit(gameData, options.limit(), sourceId, limitKey);
        }
    }

    /**
     * Whether the ability's intervening "if" (CR 603.4) and event condition (CR 603.2) let it trigger. Only the
     * intervening "if" is checked again as the ability resolves; the event condition is part of the trigger event.
     */
    private boolean conditionsMet(GameData gameData, TriggerOptions options, ConditionContext context) {
        return (options.interveningIf() == null
                || conditionEvaluationService.isMet(gameData, options.interveningIf(), context))
                && (options.eventCondition() == null
                || conditionEvaluationService.isMet(gameData, options.eventCondition(), context));
    }

    /**
     * An ability of one effect, as the old slot collectors put it on the stack: its event data bound and its targets
     * or mode chosen by {@link LegacyTriggerBindings}.
     */
    private boolean triggerBound(GameData gameData, TriggerCandidate candidate, ZoneChangeEvent event,
                                 LegacyTriggerBindings.Kind kind, CardEffect authored, Condition interveningIf,
                                 TriggerLimit limit) {
        CardEffect effect = withInterveningIf(interveningIf, authored, false);
        // "This ability triggers only once each turn" can't be made to trigger an additional time (Clara Oswald).
        int copies = limit == TriggerLimit.ONCE_EACH_TURN ? 1
                : 1 + additionalTriggers(gameData, candidate, event, kind);
        int previousCopies = gameData.beginTriggeredAbilityCopies(copies);
        try {
            return legacyTriggerBindings.trigger(gameData, candidate, event, kind, effect);
        } finally {
            gameData.restoreTriggeredAbilityCopies(previousCopies);
        }
    }

    /** An ability of several effects: one stack object (CR 603.3), with targets chosen as it goes on the stack. */
    private boolean triggerAll(GameData gameData, TriggerCandidate candidate, ZoneChangeEvent event,
                               LegacyTriggerBindings.Kind kind, List<EffectRegistration> registrations,
                               Condition interveningIf, TriggerOptions options) {
        Permanent source = candidate.sourceAsPermanent();
        UUID sourceId = candidate.sourceObjectId();
        List<CardEffect> effects = new ArrayList<>(registrations.size());
        for (EffectRegistration registration : registrations) {
            effects.add(withInterveningIf(interveningIf, registration.effect(), false));
        }
        boolean markOnAcceptance = options.limit() == TriggerLimit.DO_ONLY_ONCE_EACH_TURN;
        int lastKnownPower = event.permanent() != null && event.permanent().getLastKnownPower() != null
                ? event.permanent().getLastKnownPower() : 0;
        int previousCopies = gameData.beginTriggeredAbilityCopies(
                1 + additionalTriggers(gameData, candidate, event, kind));
        try {
            if (targets(effects)) {
                PermanentChoiceContext.DeathTriggerTarget target = new PermanentChoiceContext.DeathTriggerTarget(
                        candidate.sourceCard(), candidate.controllerId(), effects, lastKnownPower,
                        new Permanent(source), null, null, false, markOnAcceptance);
                if (event.to() == Zone.GRAVEYARD) {
                    target = target.withTriggeringCard(event.card().getId(),
                            gameData.graveyardEntryVersion(event.card().getId()));
                }
                gameData.queueInteraction(event.leftBattlefield()
                        ? target.withTriggeringPermanent(event.permanent(), event.attachments()) : target);
            } else {
                StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, candidate.sourceCard(),
                        candidate.controllerId(), candidate.sourceCard().getName() + "'s ability", effects,
                        null, candidate.sourcePermanent() == null ? null : sourceId);
                entry.setNonTargeting(true);
                entry.setEventValue(lastKnownPower);
                entry.setMarkSourceOncePerTurnOnAcceptance(markOnAcceptance);
                if (candidate.sourcePermanent() != null) {
                    entry.setSourcePermanentSnapshot(new Permanent(candidate.sourcePermanent()));
                }
                if (event.to() == Zone.GRAVEYARD) {
                    // "That card": a graveyard card that leaves and comes back before this resolves is a new object
                    // (CR 400.7).
                    entry.setTriggeringCard(event.card().getId(), gameData.graveyardEntryVersion(event.card().getId()));
                }
                if (event.leftBattlefield()) {
                    // "Its counters", "that creature": the permanent as it last existed on the battlefield
                    // (CR 603.10a).
                    entry.setTriggeringPermanent(event.permanent(), event.attachments());
                }
                gameData.enqueueTrigger(entry);
            }
        } finally {
            gameData.restoreTriggeredAbilityCopies(previousCopies);
        }
        gameLogService.append(gameData, GameLog.abilityTriggers(candidate.sourceCard()));
        return true;
    }

    /**
     * The context an intervening "if" is checked in as the ability triggers (CR 603.4): its source and controller,
     * and the permanent that moved, for "if that creature's power is greater than this creature's": with its
     * last-known power when it left the battlefield, and its power as the game sees it now when it entered.
     */
    private ConditionContext triggeringContext(GameData gameData, Permanent source, UUID controllerId,
                                               ZoneChangeEvent event) {
        ConditionContext context = ConditionContext.forPermanent(source, controllerId);
        Permanent moved = event.permanent();
        if (moved == null) {
            return context;
        }
        Permanent entered = event.leftBattlefield() ? null : gameQueryService.findPermanentById(gameData, moved.getId());
        int power = entered != null ? gameQueryService.getEffectivePower(gameData, entered)
                : moved.getLastKnownPower() != null ? moved.getLastKnownPower() : moved.getEffectivePower();
        return context.withTriggeringPermanentId(moved.getId())
                .withTriggeringPermanentPowerAtTrigger(Math.max(0, power));
    }

    /** The key the source's limits are kept under for this ability: equal abilities of one source share it. */
    private static String limitKey(TriggerCandidate candidate) {
        return candidate.ability().limitKey();
    }

    /**
     * Whether {@code departed}, a permanent's last-known copy, passes {@code filter} as it last existed on the
     * battlefield (CR 603.10a), as the pipeline judges a departure: for the readers outside it (a face-up plane's
     * "whenever a white creature dies", Agyrem).
     */
    public static boolean departedFilterMatches(PredicateEvaluationService predicates, PermanentPredicate filter,
                                                Permanent departed, DepartedAttachments attachments,
                                                FilterContext context) {
        return TriggerMatcher.departedFilterMatches(predicates, filter, departed, attachments, context);
    }

    /**
     * Whether {@code limit} stops the ability keyed {@code key} of the source {@code sourceId} from triggering again:
     * it triggered already this turn ("triggers only once each turn") or ever ("triggers only once"), or the source's
     * "do this only once each turn" action was taken this turn (CR 603.2h).
     */
    public static boolean limitReached(GameData gameData, TriggerLimit limit, UUID sourceId, String key) {
        return switch (limit) {
            case NONE -> false;
            case ONCE_EACH_TURN -> gameData.keyedOncePerTurnTriggersFiredThisTurn
                    .getOrDefault(sourceId, Set.of()).contains(key);
            // Taking the action (accepting it) marks the source.
            case DO_ONLY_ONCE_EACH_TURN -> gameData.oncePerTurnTriggersFiredThisTurn.contains(sourceId);
            case ONCE -> gameData.keyedOnceOnlyTriggersFired.getOrDefault(sourceId, Set.of()).contains(key);
            case ONCE_EACH_TURN_FOR_EACH_CREATURE -> throw new IllegalStateException(
                    "Once each turn for each creature is read only by the damage and counter collectors");
            // "For the first time each turn" counts the turn's events, which the collectors of those events judge.
            case FIRST_TIME_EACH_TURN -> false;
        };
    }

    /**
     * Marks the ability keyed {@code key} of the source {@code sourceId} as having triggered, for {@code limit}; "do
     * this only once each turn" is marked as the action is taken instead.
     */
    public static void markLimit(GameData gameData, TriggerLimit limit, UUID sourceId, String key) {
        switch (limit) {
            case ONCE_EACH_TURN -> gameData.keyedOncePerTurnTriggersFiredThisTurn
                    .computeIfAbsent(sourceId, ignored -> ConcurrentHashMap.newKeySet()).add(key);
            case ONCE -> gameData.keyedOnceOnlyTriggersFired
                    .computeIfAbsent(sourceId, ignored -> ConcurrentHashMap.newKeySet()).add(key);
            case NONE, DO_ONLY_ONCE_EACH_TURN, FIRST_TIME_EACH_TURN -> {
            }
        }
    }

    /**
     * Whether {@code limit} stops the ability keyed {@code key} of the source {@code sourceId} from triggering again for
     * an event that names the creature {@code creatureId}: "once each turn for each creature" as
     * {@link #markLimit(GameData, TriggerLimit, UUID, String, UUID)} marked the creature for the source, every other
     * limit as {@link #limitReached(GameData, TriggerLimit, UUID, String)} says.
     */
    public static boolean limitReached(GameData gameData, TriggerLimit limit, UUID sourceId, String key,
                                       UUID creatureId) {
        if (limit == TriggerLimit.ONCE_EACH_TURN_FOR_EACH_CREATURE) {
            return gameData.oncePerCreatureTriggersFiredThisTurn.getOrDefault(sourceId, Set.of()).contains(creatureId);
        }
        return limitReached(gameData, limit, sourceId, key);
    }

    /**
     * Marks the ability keyed {@code key} of the source {@code sourceId} as having triggered, for {@code limit}, for an
     * event that names the creature {@code creatureId}: "once each turn for each creature" marks the creature for the
     * source, every other limit as {@link #markLimit(GameData, TriggerLimit, UUID, String)} says.
     */
    public static void markLimit(GameData gameData, TriggerLimit limit, UUID sourceId, String key, UUID creatureId) {
        if (limit == TriggerLimit.ONCE_EACH_TURN_FOR_EACH_CREATURE) {
            gameData.oncePerCreatureTriggersFiredThisTurn
                    .computeIfAbsent(sourceId, ignored -> ConcurrentHashMap.newKeySet()).add(creatureId);
            return;
        }
        markLimit(gameData, limit, sourceId, key);
    }

    private boolean targets(List<CardEffect> effects) {
        for (CardEffect effect : effects) {
            if (effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                    || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                    || effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
                return true;
            }
        }
        return graveyardTargetingSupport.findTarget(effects) != null;
    }

    /**
     * How many additional times effects such as Teysa Karlov's make the ability trigger, as the slot collectors
     * count them. The death of a creature counts the creature-death ones too, except for "when you sacrifice this".
     */
    private int additionalTriggers(GameData gameData, TriggerCandidate candidate, ZoneChangeEvent event,
                                   LegacyTriggerBindings.Kind kind) {
        Permanent source = candidate.sourceAsPermanent();
        boolean creatureDied = event.died() && event.permanent() != null
                && event.permanent().getCard().hasType(CardType.CREATURE);
        // A creature dying is told apart, so "a creature you control" doublers (Delney, Streetwise Lookout) judge the
        // creature by its last-known controller, and see one that died in the same event (CR 603.10a).
        TriggerContext context = creatureDied ? new TriggerContext.CreatureDeath(event.permanent().getCard(),
                event.controllerId(), 0, 0, event.permanent().getId(), event.permanent()) : null;
        int additional = gameQueryService.countAdditionalTriggeredAbilityTriggers(
                gameData, candidate.controllerId(), source, false, context);
        if (creatureDied && kind != LegacyTriggerBindings.Kind.SACRIFICED) {
            additional += gameQueryService.countAdditionalCreatureDeathTriggeredAbilityTriggers(
                    gameData, candidate.controllerId(), source);
        }
        return additional;
    }

    /**
     * {@code effect} with the ability's intervening "if" {@code interveningIf} ({@code null} for none) around it, so it
     * is checked again as the ability resolves (CR 603.4). On an ability triggered by an object entering the
     * battlefield ({@code entering}) it goes inside a "you may" that targets nothing, whose collectors bind the choice to
     * the entering object; the "you may" isn't asked once the "if" is false ({@code EffectResolutionService}). A fact
     * fixed as the object entered ({@link EntryFactCondition}: "if you cast it", "if it was kicked") isn't wrapped: it
     * was judged as the ability triggered, the answer is the same as it resolves, and the stack entry can't always read
     * how the object was cast.
     */
    static CardEffect withInterveningIf(Condition interveningIf, CardEffect effect, boolean entering) {
        if (interveningIf == null || entering && EntryFactCondition.isEntryFact(interveningIf)) {
            return effect;
        }
        if (entering && effect instanceof MayEffect may && TargetSpec.NONE.equals(may.targetSpec())) {
            return new MayEffect(new ConditionalEffect(interveningIf, may.wrapped()), may.prompt(), may.elseEffect(),
                    may.choicePlayer(), may.choicePlayerId());
        }
        return new ConditionalEffect(interveningIf, effect);
    }
}
