package com.github.laxika.magicalvibes.service.trigger.pipeline;

import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.rules.RulesEvent;
import com.github.laxika.magicalvibes.model.rules.RulesEventBatch;
import com.github.laxika.magicalvibes.model.rules.RulesObserver;
import com.github.laxika.magicalvibes.model.rules.ZoneChangeEvent;
import com.github.laxika.magicalvibes.model.trigger.TriggerMultiplicity;
import com.github.laxika.magicalvibes.model.trigger.ZoneChangeTrigger;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
 * {@link LegacyTriggerBindings.Kind}: leaves the battlefield, arrives in a zone, dies, sacrificed. Their controller
 * orders them later all the same (CR 603.3b).
 *
 * <p>Abilities still held in an {@code EffectSlot} are the slot collectors' business. For the slots a test puts in
 * shadow mode ({@link RulesObserver#shadowedSlots()}), the pipeline also works out what those abilities would
 * trigger once their family moves, reading their wrappers through {@link LegacyTriggerOptions}, and reports it to
 * the observer instead of the stack, so the shadow recorder can compare the two paths.
 */
@Service
public class RulesEventPipeline {

    private final TriggerIndex triggerIndex;
    private final TriggeredAbilityStackService stackService;
    private final LegacyTriggerOptions legacyTriggerOptions;
    private final PredicateEvaluationService predicateEvaluationService;

    public RulesEventPipeline(TriggerIndex triggerIndex, TriggeredAbilityStackService stackService,
                              LegacyTriggerOptions legacyTriggerOptions,
                              @Lazy PredicateEvaluationService predicateEvaluationService) {
        this.triggerIndex = triggerIndex;
        this.stackService = stackService;
        this.legacyTriggerOptions = legacyTriggerOptions;
        this.predicateEvaluationService = predicateEvaluationService;
    }

    /** Matches the events of {@code batch}, one occurrence that just ended, and triggers what they trigger. */
    public void process(GameData gameData, RulesEventBatch batch) {
        List<ZoneChangeEvent> events = new ArrayList<>();
        for (RulesEvent event : batch.events()) {
            if (event instanceof ZoneChangeEvent zoneChange) {
                events.add(zoneChange);
            }
        }
        Set<OneOrMoreKey> triggeredForBatch = new HashSet<>();
        triggerObservers(gameData, events, triggeredForBatch);
        triggerMovingObjects(gameData, events, triggeredForBatch);
    }

    /** The abilities of other objects that see an object move: "whenever a creature dies". */
    private void triggerObservers(GameData gameData, List<ZoneChangeEvent> events,
                                  Set<OneOrMoreKey> triggeredForBatch) {
        Map<OneOrMoreKey, ObservedEvents> oneOrMore = new LinkedHashMap<>();
        for (ZoneChangeEvent event : events) {
            for (TriggerCandidate candidate : triggerIndex.observerCandidates(gameData, event, events)) {
                ZoneChangeTrigger trigger = TriggerMatcher.matchedTrigger(gameData, predicateEvaluationService,
                        candidate, event, false);
                if (trigger == null || TriggerMatcher.suppressed(candidate.ability(), event)) {
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
            matches.sort(Comparator.comparing(match -> LegacyTriggerBindings.kindOf(match.trigger())));
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
            LegacyTriggerOptions.Resolved resolved = legacyTriggerOptions.resolve(gameData,
                    LegacyTriggerBindings.kindOf(LegacySlotTriggers.of(slot, registration.effect())), event,
                    candidate, registration.effect());
            if (resolved != null) {
                observer.wouldTrigger(slot, candidate.sourceCard(), List.of(resolved.effect()));
            }
        }
    }

    private record OneOrMoreKey(UUID sourceId, TriggeredAbility ability) {
    }

    /** A candidate whose ability's trigger condition {@code trigger} an event met. */
    private record Match(TriggerCandidate candidate, ZoneChangeTrigger trigger) {
    }

    /** A "one or more" ability of another object and the events of the batch that met its {@code trigger}. */
    private record ObservedEvents(TriggerCandidate candidate, ZoneChangeTrigger trigger,
                                  List<ZoneChangeEvent> events) {
    }
}
