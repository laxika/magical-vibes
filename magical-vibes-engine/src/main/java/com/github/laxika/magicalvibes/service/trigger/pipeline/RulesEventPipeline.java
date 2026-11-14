package com.github.laxika.magicalvibes.service.trigger.pipeline;

import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.rules.AttackEvent;
import com.github.laxika.magicalvibes.model.rules.BlockEvent;
import com.github.laxika.magicalvibes.model.rules.LibraryEvent;
import com.github.laxika.magicalvibes.model.rules.PermanentChangeEvent;
import com.github.laxika.magicalvibes.model.rules.RulesEvent;
import com.github.laxika.magicalvibes.model.rules.RulesEventBatch;
import com.github.laxika.magicalvibes.model.rules.RulesObserver;
import com.github.laxika.magicalvibes.model.rules.StepEvent;
import com.github.laxika.magicalvibes.model.trigger.StepTrigger;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.trigger.pipeline.StepMatch.StepGroup;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityRequest.TriggerRecord;
import com.github.laxika.magicalvibes.model.rules.ZoneChangeCause;
import com.github.laxika.magicalvibes.model.rules.ZoneChangeEvent;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.PermanentChangeTrigger;
import com.github.laxika.magicalvibes.model.trigger.TriggerMultiplicity;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.trigger.ZoneChangeTrigger;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.trigger.ZoneChangeWatch;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * The generic trigger pipeline: {@code GameData} hands it each rules event batch as the batch closes, it asks the
 * {@link TriggerIndex} for each event's candidate abilities, matches them ({@link TriggerMatcher}) and puts every
 * ability that triggers on the stack through {@link TriggeredAbilityStackService}. An ability with
 * {@link TriggerMultiplicity#EACH} triggers once for each event of the batch that matches it, and one with
 * {@link TriggerMultiplicity#ONE_OR_MORE} once for the batch (CR 603.2c).
 *
 * <p>The abilities of the objects that see another object move (a creature dies) are put on the stack first, those
 * that trigger for each event in event order and then the "one or more" ones, as the old slot collectors queued
 * them while the objects were still moving. The moving objects' own abilities follow, each event's in the order of
 * {@link TriggeredAbilityStackService.OwnZoneChange}: leaves the battlefield, arrives in a zone, dies, sacrificed. Their controller
 * orders them later all the same (CR 603.3b).
 *
 * <p>A permanent entering is seen by every permanent on the battlefield after the event, the newcomers and the
 * entering permanent itself included (CR 603.6a). Those abilities, but for the token watches, are matched as the
 * entering permanent's own enters abilities are processed ({@link #triggerEnteringObservers}), where the old slot
 * collectors ran, once the permanents that entered with it are all on the battlefield. The entering permanent's own
 * "when this enters" abilities are put on the stack there too, by {@code EtbTriggerService}, below the others. A permanent phasing, transforming or being turned face up is a {@link PermanentChangeEvent},
 * matched against permanent-change triggers: the "one or more" abilities once for the batch, then each event's.
 *
 * <p>Abilities still held in an {@code EffectSlot} are the slot collectors' business. For the slots a test puts in
 * shadow mode ({@link RulesObserver#shadowedSlots()}), the pipeline also works out what those abilities would
 * trigger once their family moves, and reports it to
 * the observer instead of the stack, so the shadow recorder can compare the two paths.
 */
@Service
public class RulesEventPipeline {

    /**
     * "When this enters": the entering permanent's own enters abilities, which {@code EtbTriggerService} puts on the
     * stack as it processes the permanent's entry, before the abilities that see it enter.
     */
    private static final ZoneChangeTrigger OWN_ENTRY = Triggers.enters(ObjectRelation.THIS);

    private final TriggerIndex triggerIndex;
    private final TriggeredAbilityStackService stackService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PermanentRemovalService permanentRemovalService;
    private final TriggerCollectionService triggerCollectionService;
    private final GameQueryService gameQueryService;
    private final ConditionEvaluationService conditionEvaluationService;
    private final GameLogService gameLogService;
    private final AttackTriggers attackTriggers;
    private final BlockTriggers blockTriggers;

    public RulesEventPipeline(TriggerIndex triggerIndex, TriggeredAbilityStackService stackService,
                              @Lazy PredicateEvaluationService predicateEvaluationService,
                              @Lazy PermanentRemovalService permanentRemovalService,
                              @Lazy TriggerCollectionService triggerCollectionService,
                              @Lazy GameQueryService gameQueryService,
                              @Lazy ConditionEvaluationService conditionEvaluationService,
                              @Lazy GameLogService gameLogService,
                              @Lazy AttackTriggers attackTriggers,
                              @Lazy BlockTriggers blockTriggers) {
        this.triggerIndex = triggerIndex;
        this.stackService = stackService;
        this.predicateEvaluationService = predicateEvaluationService;
        this.permanentRemovalService = permanentRemovalService;
        this.triggerCollectionService = triggerCollectionService;
        this.gameQueryService = gameQueryService;
        this.conditionEvaluationService = conditionEvaluationService;
        this.gameLogService = gameLogService;
        this.attackTriggers = attackTriggers;
        this.blockTriggers = blockTriggers;
    }

    /**
     * The order a step's abilities go on the stack in, by how their sources relate to the step; the groups a step
     * doesn't name follow, in {@link StepGroup} order. Within a group the abilities keep {@link TriggerIndex}'s order
     * (APNAP, then each player's objects). The stack door puts each where it is, so a later one resolves first.
     */
    private static final Map<StepTrigger.Moment, List<List<StepGroup>>> STEP_ORDER = Map.of(
            StepTrigger.Moment.DRAW_STEP, List.of(List.of(StepGroup.EMBLEM), List.of(StepGroup.PLANE),
                    List.of(StepGroup.YOU_BATTLEFIELD), List.of(StepGroup.ENCHANTED_CONTROLLER),
                    List.of(StepGroup.ENCHANTED_PLAYER),
                    List.of(StepGroup.EACH_BATTLEFIELD, StepGroup.OPPONENT_BATTLEFIELD)),
            StepTrigger.Moment.POSTCOMBAT_MAIN_PHASE, List.of(List.of(StepGroup.YOU_BATTLEFIELD),
                    List.of(StepGroup.PLANE)),
            StepTrigger.Moment.END_OF_COMBAT, List.of(List.of(StepGroup.EMBLEM), List.of(StepGroup.PLANE)),
            StepTrigger.Moment.END_STEP, List.of(List.of(StepGroup.PLANE), List.of(StepGroup.EMBLEM),
                    List.of(StepGroup.YOU_COMMAND), List.of(StepGroup.EACH_BATTLEFIELD),
                    List.of(StepGroup.EACH_COMMAND), List.of(StepGroup.EACH_GRAVEYARD, StepGroup.YOU_GRAVEYARD),
                    List.of(StepGroup.YOU_BATTLEFIELD), List.of(StepGroup.OPPONENT_BATTLEFIELD),
                    List.of(StepGroup.ENCHANTED_CONTROLLER), List.of(StepGroup.ENCHANTED_PLAYER)));

    /**
     * The abilities a step or phase beginning triggers (CR 603.2b), each handed whole to the stack door with the
     * step's record, in the step's order ({@link #STEP_ORDER}). An ability's event condition (CR 603.2) is judged in
     * the context of its source and, for an ability that names "that player", that player; the door judges the rest
     * (CR 603.4, 603.2h). A step begins once, so a step ability triggers once (CR 603.2c) and is never written with a
     * "one or more" multiplicity or a limit but "only once"; this throws on one.
     */
    private void triggerStep(GameData gameData, StepEvent event) {
        List<StepMatch> matches = new ArrayList<>();
        for (StepCandidate candidate : triggerIndex.stepCandidates(gameData, event)) {
            StepMatch match = TriggerMatcher.matchedStep(gameData, gameQueryService, candidate, event);
            if (match != null) {
                matches.add(match);
            }
        }
        List<List<StepGroup>> order = STEP_ORDER.getOrDefault(event.moment(), List.of());
        matches.sort(Comparator.comparingInt(match -> stepRank(order, match.group())));
        if (event.moment() == StepTrigger.Moment.PRECOMBAT_MAIN_PHASE) {
            reverseEachSourcesOwnAbilities(matches);
        }
        for (StepMatch match : matches) {
            putStepAbility(gameData, event, match);
        }
    }

    private static int stepRank(List<List<StepGroup>> order, StepGroup group) {
        for (int rank = 0; rank < order.size(); rank++) {
            if (order.get(rank).contains(group)) {
                return rank;
            }
        }
        return order.size() + group.ordinal();
    }

    /**
     * "At the beginning of your precombat main phase": a permanent's abilities are put on the stack with its first one
     * on top, its controller's order (CR 603.3b).
     */
    private static void reverseEachSourcesOwnAbilities(List<StepMatch> matches) {
        int start = 0;
        while (start < matches.size()) {
            StepMatch first = matches.get(start);
            int end = start + 1;
            if (first.group() == StepGroup.YOU_BATTLEFIELD) {
                while (end < matches.size() && matches.get(end).group() == StepGroup.YOU_BATTLEFIELD
                        && matches.get(end).candidate().candidate().sourcePermanent()
                        == first.candidate().candidate().sourcePermanent()) {
                    end++;
                }
                Collections.reverse(matches.subList(start, end));
            }
            start = end;
        }
    }

    private void putStepAbility(GameData gameData, StepEvent event, StepMatch match) {
        TriggerCandidate candidate = match.candidate().candidate();
        TriggerOptions options = candidate.ability().options();
        if (options.multiplicity() != TriggerMultiplicity.EACH) {
            throw new IllegalStateException("A step or phase begins once; no step ability triggers "
                    + options.multiplicity() + ": " + candidate.ability());
        }
        if (options.limit() != TriggerLimit.NONE && options.limit() != TriggerLimit.ONCE) {
            throw new IllegalStateException("No step ability is written with the limit " + options.limit() + ": "
                    + candidate.ability());
        }
        Card card = candidate.sourceCard();
        Permanent source = candidate.sourcePermanent();
        // A face-up scheme isn't on the battlefield: its conditions are judged on its card.
        boolean permanentSource = source != null && gameData.findFaceUpSchemeBySourceId(source.getId()) == null;
        ConditionContext context = permanentSource ? ConditionContext.forPermanent(source, candidate.controllerId())
                : ConditionContext.forCard(card, candidate.controllerId());
        if (namesThatPlayer(match.group(), event)) {
            context = context.withTargetId(match.thatPlayerId());
        }
        if (!conditionEvaluationService.eventConditionMet(gameData, options, context)) {
            return;
        }
        StepCandidate.ExileCounterAbility counterAbility = match.candidate().counterAbility();
        String kind = counterAbility != null ? counterAbility.kind() : stepAbilityKind(event.moment(), match.group());
        TriggerRecord record = counterAbility != null ? TriggerRecord.NONE : stepRecord(gameData, event, match);
        TriggeredAbilityRequest request = new TriggeredAbilityRequest(candidate.ability(), card, source,
                candidate.controllerId(), card.getName() + "'s " + kind, context, record);
        if (stackService.put(gameData, request) && match.group() != StepGroup.PLANE) {
            gameLogService.append(gameData, match.group() == StepGroup.EMBLEM
                    ? GameLog.text(card.getName() + "'s emblem triggers.")
                    : GameLog.cardThen(card, counterAbility != null ? counterAbility.logText()
                    : "'s " + kind + " triggers."));
        }
    }

    /**
     * Whether an ability of {@code group} names "that player", the player its record and event condition read: the
     * player whose step it is for "each" (but "at end of combat", which names none) and "each opponent's" steps, the
     * enchanted player for a Curse's.
     */
    private static boolean namesThatPlayer(StepGroup group, StepEvent event) {
        return switch (group) {
            case EACH_BATTLEFIELD -> event.moment() != StepTrigger.Moment.END_OF_COMBAT;
            case OPPONENT_BATTLEFIELD, ENCHANTED_PLAYER, EACH_EXILE, EMBLEM -> true;
            default -> false;
        };
    }

    /** What a step ability is called on the stack and in the log: "Howling Mine's draw step ability". */
    private static String stepAbilityKind(StepTrigger.Moment moment, StepGroup group) {
        if (group == StepGroup.PLANE) {
            return "ability";
        }
        if (group == StepGroup.EMBLEM) {
            return "emblem";
        }
        return switch (moment) {
            case UPKEEP -> switch (group) {
                case YOU_COMMAND -> "command zone upkeep ability";
                case YOU_EXILE -> "exile upkeep ability";
                case EACH_EXILE -> "suspended ability";
                default -> "upkeep ability";
            };
            case DRAW_STEP -> "draw step ability";
            case PRECOMBAT_MAIN_PHASE -> group == StepGroup.YOU_GRAVEYARD ? "precombat main ability" : "ability";
            case BEGINNING_OF_COMBAT -> "combat ability";
            case END_STEP -> "end step ability";
            default -> "ability";
        };
    }

    /**
     * What a step ability's stack entry records of the step, whatever its effects are: the player whose step it is
     * as the active player; a permanent source as it was and what it is attached to (read as the ability resolves,
     * and as last known once it has left, CR 608.2h); a graveyard source's graveyard stay (CR 400.7). An ability that
     * names "that player" records that player as its triggering player and non-targeting target (CR 115.10a). An
     * Aura's ability that acts on the enchanted permanent's controller names that permanent and its controller, the
     * player at the upkeep and draw step and the permanent at the end step ("destroy enchanted creature", "its
     * controller sacrifices it"); "at end of combat" names no object. A planar object's ability names the planar
     * object it is (CR 901.6).
     */
    private TriggerRecord stepRecord(GameData gameData, StepEvent event, StepMatch match) {
        TriggerCandidate candidate = match.candidate().candidate();
        Permanent source = candidate.sourcePermanent();
        UUID activeId = event.activePlayerId();
        Card card = candidate.sourceCard();
        Permanent attached = source != null && source.isAttached()
                ? gameQueryService.findPermanentById(gameData, source.getAttachedTo()) : null;
        Permanent enchanted = match.enchantedId() == null ? null
                : gameQueryService.findPermanentById(gameData, match.enchantedId());
        int enchantedPower = enchanted == null ? 0 : gameQueryService.getEffectivePower(gameData, enchanted);
        long graveyardVersion = candidate.zone() == Zone.GRAVEYARD ? gameData.graveyardEntryVersion(card.getId()) : -1;
        return entry -> {
            switch (match.group()) {
                case PLANE -> {
                    entry.setSourcePlanarObject(match.candidate().plane().copy());
                    return;
                }
                case ENCHANTED_CONTROLLER -> {
                    // "Destroy enchanted creature" at the end step names the permanent; "that player" at upkeep
                    // and draw step is its controller as the ability triggered.
                    boolean endStep = event.moment() == StepTrigger.Moment.END_STEP;
                    entry.setTargetId(endStep ? match.enchantedId() : match.thatPlayerId());
                    entry.setNonTargeting(true);
                    if (endStep) {
                        entry.setTriggeringPermanentId(match.enchantedId());
                    }
                    entry.setTriggeringPermanentControllerId(match.thatPlayerId());
                    entry.setTriggeringPermanentPowerAtTrigger(enchantedPower);
                }
                default -> {
                    if (namesThatPlayer(match.group(), event)) {
                        entry.setTriggeringPlayerId(match.thatPlayerId());
                        entry.setTargetId(match.thatPlayerId());
                        entry.setNonTargeting(true);
                    } else if (match.group() == StepGroup.EACH_BATTLEFIELD) {
                        entry.setNonTargeting(true);
                    }
                }
            }
            entry.setActivePlayerId(activeId);
            if (source != null && match.group() != StepGroup.EMBLEM) {
                entry.setSourcePermanentSnapshot(new Permanent(source));
            }
            if (attached != null) {
                entry.setAttachedPermanentSnapshot(new Permanent(attached));
            }
            if (graveyardVersion >= 0) {
                entry.setSourceGraveyardEntryVersion(graveyardVersion);
                entry.setTriggeringCardId(card.getId());
                entry.setTriggeringCardGraveyardEntryVersion(graveyardVersion);
            }
        };
    }

    /** Matches the events of {@code batch}, one occurrence that just ended, and triggers what they trigger. */
    public void process(GameData gameData, RulesEventBatch batch) {
        List<ZoneChangeEvent> events = new ArrayList<>();
        List<PermanentChangeEvent> permanentChanges = new ArrayList<>();
        for (RulesEvent event : batch.events()) {
            if (event instanceof ZoneChangeEvent zoneChange && !triggerIndex.entryCausesNoTriggers(gameData, zoneChange)) {
                events.add(zoneChange);
            } else if (event instanceof PermanentChangeEvent permanentChange) {
                permanentChanges.add(permanentChange);
            } else if (event instanceof LibraryEvent library) {
                triggerLibraryEvent(gameData, library);
            } else if (event instanceof StepEvent step) {
                triggerStep(gameData, step);
            } else if (event instanceof AttackEvent attack) {
                attackTriggers.trigger(gameData, attack);
            } else if (event instanceof BlockEvent block) {
                blockTriggers.trigger(gameData, block);
            }
        }
        if (!permanentChanges.isEmpty()) {
            triggerPermanentChanges(gameData, permanentChanges);
        }
        if (events.isEmpty()) {
            return;
        }
        // CR 603.10a: leaves-the-battlefield abilities look back in time, so a condition that counts a graveyard
        // (Decaying Soil's threshold grant) doesn't count the cards that just died.
        Set<UUID> lookBack = new HashSet<>();
        for (ZoneChangeEvent event : events) {
            if (event.died() && gameData.graveyardLookBackExcludedCardIds.add(event.card().getId())) {
                lookBack.add(event.card().getId());
            }
        }
        try {
            Set<OneOrMoreKey> triggeredForBatch = new HashSet<>();
            triggerObservers(gameData, events, triggeredForBatch);
            triggerMovingObjects(gameData, events, triggeredForBatch);
        } finally {
            gameData.graveyardLookBackExcludedCardIds.removeAll(lookBack);
        }
        // An ability that refers to a card that went to a graveyard refers to that stay (CR 400.7). The card arrived
        // before the batch was matched, so its arrival couldn't stamp the abilities queued for it.
        for (ZoneChangeEvent event : events) {
            if (event.to() == Zone.GRAVEYARD) {
                gameData.stampGraveyardEntryOnPendingTriggers(event.card().getId());
            }
        }
    }

    /**
     * The abilities a library search or shuffle triggers, collected by their library collectors
     * ({@link TriggerCollectionService#checkLibrarySearchTriggers}, {@link TriggerCollectionService#checkLibraryShuffleTriggers}),
     * which hand each to the stack door.
     */
    private void triggerLibraryEvent(GameData gameData, LibraryEvent event) {
        switch (event.kind()) {
            case SEARCHED -> triggerCollectionService.checkLibrarySearchTriggers(gameData, event.playerId());
            case SHUFFLED -> triggerCollectionService.checkLibraryShuffleTriggers(gameData, event.playerId(),
                    event.causingControllerId());
        }
    }

    /**
     * The abilities that see a permanent phase, transform or be turned face up. The "one or more" abilities trigger
     * once for the batch first, then, for each event in order, the other permanents' abilities and the changed
     * permanent's own, as the old slot collectors queued them.
     */
    private void triggerPermanentChanges(GameData gameData, List<PermanentChangeEvent> events) {
        Map<OneOrMoreKey, ObservedChanges> oneOrMore = new LinkedHashMap<>();
        List<ChangeMatch> each = new ArrayList<>();
        for (PermanentChangeEvent event : events) {
            List<ChangeMatch> own = new ArrayList<>();
            for (TriggerCandidate candidate : triggerIndex.permanentChangeCandidates(gameData, event)) {
                PermanentChangeTrigger trigger = TriggerMatcher.matchedPermanentChange(gameData,
                        predicateEvaluationService, candidate, event);
                if (trigger == null) {
                    continue;
                }
                if (candidate.ability().options().multiplicity() == TriggerMultiplicity.ONE_OR_MORE) {
                    oneOrMore.computeIfAbsent(new OneOrMoreKey(candidate.sourceObjectId(), candidate.ability()),
                            ignored -> new ObservedChanges(candidate, trigger, new ArrayList<>())).events().add(event);
                } else if (trigger.relation() == ObjectRelation.THIS) {
                    own.add(new ChangeMatch(candidate, trigger, event));
                } else {
                    each.add(new ChangeMatch(candidate, trigger, event));
                }
            }
            each.addAll(own);
        }
        oneOrMore.values().forEach(observed -> stackService.triggerPermanentChange(gameData, observed.candidate(),
                observed.events(), observed.trigger()));
        for (ChangeMatch match : each) {
            stackService.triggerPermanentChange(gameData, match.candidate(), List.of(match.event()), match.trigger());
        }
    }

    /** The abilities of other objects that see an object move: "whenever a creature dies". */
    private void triggerObservers(GameData gameData, List<ZoneChangeEvent> events,
                                  Set<OneOrMoreKey> triggeredForBatch) {
        Map<OneOrMoreKey, ObservedEvents> oneOrMore = new LinkedHashMap<>();
        for (ZoneChangeEvent event : events) {
            for (TriggerCandidate candidate : triggerIndex.observerCandidates(gameData, event, events)) {
                ZoneChangeTrigger trigger = TriggerMatcher.matchedTrigger(gameData, predicateEvaluationService,
                        candidate, event, false);
                if (trigger == null || TriggerMatcher.suppressed(candidate.ability(), event)
                        || matchedAsItsEntryIsProcessed(candidate, trigger)) {
                    continue;
                }
                if (candidate.ability().options().multiplicity() == TriggerMultiplicity.EACH) {
                    stackService.trigger(gameData, candidate, event, trigger);
                } else {
                    oneOrMore.computeIfAbsent(new OneOrMoreKey(candidate.sourceObjectId(), candidate.ability()),
                            ignored -> new ObservedEvents(candidate, trigger, new ArrayList<>())).events().add(event);
                }
            }
        }
        oneOrMore.forEach((key, observed) -> {
            triggeredForBatch.add(key);
            stackService.triggerOnce(gameData, observed.candidate(), observed.events(), observed.trigger());
        });
    }

    /**
     * Whether an ability that sees another permanent enter is matched as the entering permanent's enters abilities
     * are processed ({@link #triggerEnteringObservers}) rather than as its entry's batch closes: every one but the
     * token watches, which see a token creation as one event.
     */
    private static boolean matchedAsItsEntryIsProcessed(TriggerCandidate candidate, ZoneChangeTrigger trigger) {
        if (!trigger.entersBattlefield()) {
            return false;
        }
        ZoneChangeWatch watch = ZoneChangeWatch.of(candidate.ability(), trigger, candidate.zone());
        return watch != ZoneChangeWatch.ON_ALLY_TOKEN_ENTERS_BATTLEFIELD
                && watch != ZoneChangeWatch.ON_OPPONENT_TOKEN_ENTERS_BATTLEFIELD;
    }

    /**
     * Triggers the "whenever [an object] enters" abilities that see {@code entering} enter, as its own enters
     * abilities are processed: once every permanent that entered with it is on the battlefield and their entry
     * choices are made, which is where the mass-entry paths process the permanents of one event together. Every
     * permanent on the battlefield then is checked, the newcomers and {@code entering} itself included (CR 603.6a).
     * The entry is the rules event the log still holds for it, with the permanent as it entered. While
     * {@code GameData.collectingSimultaneousCreatureEntryTriggers} marks such an event, a "one or more" ability that
     * already triggered for it names this permanent too instead of triggering again (CR 603.2c).
     */
    public void triggerEnteringObservers(GameData gameData, Permanent entering, UUID controllerId) {
        List<ZoneChangeEvent> batch = entryBatch(gameData, entering, controllerId);
        // The game checks it as it is now that it has entered and its entry choices are made (CR 603.6a): its effective
        // characteristics, which the triggers' filters are judged on.
        Permanent asEntered = new Permanent(entering);
        asEntered.setCard(permanentRemovalService.snapshotEffectivePermanentCard(gameData, entering));
        ZoneChangeEvent event = batch.stream()
                .filter(candidate -> candidate.to() == Zone.BATTLEFIELD && candidate.permanent() != null
                        && candidate.permanent().getId().equals(entering.getId()))
                .reduce((first, second) -> second).orElseThrow().withPermanent(asEntered);
        for (TriggerCandidate candidate : triggerIndex.observerCandidates(gameData, event, batch)) {
            ZoneChangeTrigger trigger = TriggerMatcher.matchedTrigger(gameData, predicateEvaluationService,
                    candidate, event, false);
            if (trigger == null || !matchedAsItsEntryIsProcessed(candidate, trigger) || trigger.equals(OWN_ENTRY)) {
                continue;
            }
            if (candidate.ability().options().multiplicity() == TriggerMultiplicity.ONE_OR_MORE
                    && gameData.collectingSimultaneousCreatureEntryTriggers) {
                // Keyed by the whole ability, which triggers once for the event (CR 603.2c); another ability of the
                // source triggers on its own.
                if (!gameData.simultaneousCreatureEntryTriggers
                        .computeIfAbsent(candidate.sourceObjectId(), ignored -> new HashSet<>())
                        .add(candidate.ability())) {
                    joinTriggeredEntry(gameData, candidate, entering.getId());
                    continue;
                }
            }
            stackService.trigger(gameData, candidate, event, trigger);
        }
    }

    /**
     * The batch the log holds {@code entering}'s entry in, newest first; a permanent whose entry the log no longer
     * holds is described as it is now.
     */
    private List<ZoneChangeEvent> entryBatch(GameData gameData, Permanent entering, UUID controllerId) {
        List<RulesEventBatch> pending = gameData.rulesEvents.pending();
        for (int index = pending.size() - 1; index >= 0; index--) {
            List<ZoneChangeEvent> events = new ArrayList<>();
            boolean found = false;
            for (RulesEvent rulesEvent : pending.get(index).events()) {
                if (rulesEvent instanceof ZoneChangeEvent zoneChange) {
                    events.add(zoneChange);
                    found |= zoneChange.to() == Zone.BATTLEFIELD && zoneChange.permanent() != null
                            && zoneChange.permanent().getId().equals(entering.getId());
                }
            }
            if (found) {
                return events;
            }
        }
        return List.of(new ZoneChangeEvent(entering.getOriginalCard(), gameData.ownerOf(entering.getOriginalCard()),
                entering.getEnteredFromZone(), Zone.BATTLEFIELD, ZoneChangeCause.OTHER, new Permanent(entering),
                controllerId));
    }

    /**
     * Names {@code enteringId} on the stack object the "one or more" ability of {@code candidate} already put there: an
     * entry of its source whose effects are that ability's (its intervening "if" put around them or inside their "you
     * may" as its collector read them), never another ability's.
     */
    private static void joinTriggeredEntry(GameData gameData, TriggerCandidate candidate, UUID enteringId) {
        List<CardEffect> effects = candidate.ability().effects().stream().map(EffectRegistration::effect).toList();
        Condition interveningIf = candidate.ability().options().interveningIf();
        for (StackEntry entry : gameData.stack) {
            if (entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                    && candidate.sourceObjectId().equals(entry.getSourcePermanentId())
                    && entry.getEffectsToResolve().stream()
                    .map(effect -> withoutInterveningIf(effect, interveningIf)).toList().equals(effects)
                    && !entry.getTriggeringPermanentIds().isEmpty()
                    && !entry.getTriggeringPermanentIds().contains(enteringId)) {
                List<UUID> group = new ArrayList<>(entry.getTriggeringPermanentIds());
                group.add(enteringId);
                entry.setTriggeringPermanentIds(group);
            }
        }
    }

    /** {@code effect} without the ability's intervening "if" its collector put around it or inside its "you may". */
    private static CardEffect withoutInterveningIf(CardEffect effect, Condition interveningIf) {
        if (interveningIf == null) {
            return effect;
        }
        if (effect instanceof ConditionalEffect conditional && conditional.condition().equals(interveningIf)) {
            return withoutInterveningIf(conditional.wrapped(), interveningIf);
        }
        if (effect instanceof MayEffect may) {
            CardEffect wrapped = withoutInterveningIf(may.wrapped(), interveningIf);
            return wrapped == may.wrapped() ? may
                    : new MayEffect(wrapped, may.prompt(), may.elseEffect(), may.choicePlayer(), may.choicePlayerId(),
                            may.targetOfTheirChoice());
        }
        return effect;
    }

    /** The moving objects' own abilities, and the slot abilities shadow mode reports. */
    private void triggerMovingObjects(GameData gameData, List<ZoneChangeEvent> events,
                                      Set<OneOrMoreKey> triggeredForBatch) {
        RulesObserver observer = gameData.rulesObserver;
        Set<EffectSlot> shadowedSlots = observer == null ? Set.of() : observer.shadowedSlots();
        for (ZoneChangeEvent zoneChange : events) {
            List<Match> matches = new ArrayList<>();
            for (TriggerCandidate candidate : triggerIndex.movingObjectCandidates(gameData, zoneChange)) {
                EffectSlot slot = TriggerMatcher.legacySlot(candidate);
                if (slot == null) {
                    ZoneChangeTrigger trigger = TriggerMatcher.matchedTrigger(gameData, predicateEvaluationService,
                            candidate, zoneChange, true);
                    if (trigger != null && !TriggerMatcher.suppressed(candidate.ability(), zoneChange)) {
                        matches.add(new Match(candidate, trigger));
                    }
                } else if (shadowedSlots.contains(slot)) {
                    reportWouldTrigger(gameData, observer, slot, candidate, zoneChange);
                }
            }
            matches.sort(Comparator.comparing(match -> TriggeredAbilityStackService.ownZoneChangeOf(match.trigger())));
            for (Match match : matches) {
                TriggerCandidate candidate = match.candidate();
                if (candidate.ability().options().multiplicity() == TriggerMultiplicity.EACH
                        || triggeredForBatch.add(new OneOrMoreKey(candidate.sourceObjectId(), candidate.ability()))) {
                    stackService.trigger(gameData, candidate, zoneChange, match.trigger());
                }
            }
        }
    }

    /** Reports each of a slot ability's registrations that would trigger, as its slot's collector treats each one. */
    private void reportWouldTrigger(GameData gameData, RulesObserver observer, EffectSlot slot,
                                    TriggerCandidate candidate, ZoneChangeEvent event) {
        for (EffectRegistration registration : candidate.ability().effects()) {
            if (!TriggerMatcher.matchesAsMoved(slot, registration.effect(), candidate, event)
                    || TriggerMatcher.suppressedAsMoved(slot, registration.effect(), event)) {
                continue;
            }
            observer.wouldTrigger(slot, candidate.sourceCard(), List.of(registration.effect()));
        }
    }

    private record OneOrMoreKey(UUID sourceId, TriggeredAbility ability) {
    }

    /** A candidate whose ability's permanent-change trigger condition {@code trigger} {@code event} met. */
    private record ChangeMatch(TriggerCandidate candidate, PermanentChangeTrigger trigger,
                               PermanentChangeEvent event) {
    }

    /** A "one or more" permanent-change ability and the events of the batch that met its {@code trigger}. */
    private record ObservedChanges(TriggerCandidate candidate, PermanentChangeTrigger trigger,
                                   List<PermanentChangeEvent> events) {
    }

    /** A candidate whose ability's trigger condition {@code trigger} an event met. */
    private record Match(TriggerCandidate candidate, ZoneChangeTrigger trigger) {
    }

    /** A "one or more" ability of another object and the events of the batch that met its {@code trigger}. */
    private record ObservedEvents(TriggerCandidate candidate, ZoneChangeTrigger trigger,
                                  List<ZoneChangeEvent> events) {
    }
}
