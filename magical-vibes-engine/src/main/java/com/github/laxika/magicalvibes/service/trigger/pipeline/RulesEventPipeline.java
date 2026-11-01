package com.github.laxika.magicalvibes.service.trigger.pipeline;

import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.rules.RulesEvent;
import com.github.laxika.magicalvibes.model.rules.RulesEventBatch;
import com.github.laxika.magicalvibes.model.rules.RulesObserver;
import com.github.laxika.magicalvibes.model.rules.ZoneChangeEvent;
import com.github.laxika.magicalvibes.model.trigger.TriggerMultiplicity;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * The generic trigger pipeline: {@code GameData} hands it each rules event batch as the batch closes, it asks the
 * {@link TriggerIndex} for each event's candidate abilities, matches them ({@link TriggerMatcher}) and puts every
 * ability that triggers on the stack through {@link TriggeredAbilityStackService}. An ability with
 * {@link TriggerMultiplicity#EACH} triggers once for each event of the batch that matches it, and one with
 * {@link TriggerMultiplicity#ONE_OR_MORE} once for the batch (CR 603.2c).
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

    public RulesEventPipeline(TriggerIndex triggerIndex, TriggeredAbilityStackService stackService,
                              LegacyTriggerOptions legacyTriggerOptions) {
        this.triggerIndex = triggerIndex;
        this.stackService = stackService;
        this.legacyTriggerOptions = legacyTriggerOptions;
    }

    /** Matches the events of {@code batch}, one occurrence that just ended, and triggers what they trigger. */
    public void process(GameData gameData, RulesEventBatch batch) {
        RulesObserver observer = gameData.rulesObserver;
        Set<EffectSlot> shadowedSlots = observer == null ? Set.of() : observer.shadowedSlots();
        Set<OneOrMoreKey> triggeredForBatch = new HashSet<>();
        for (RulesEvent event : batch.events()) {
            if (!(event instanceof ZoneChangeEvent zoneChange)) {
                continue;
            }
            for (TriggerCandidate candidate : triggerIndex.movingObjectCandidates(gameData, zoneChange)) {
                EffectSlot slot = TriggerMatcher.legacySlot(candidate);
                if (slot == null) {
                    if (TriggerMatcher.matches(candidate, zoneChange)
                            && !TriggerMatcher.suppressed(candidate.ability(), zoneChange)
                            && (candidate.ability().options().multiplicity() == TriggerMultiplicity.EACH
                            || triggeredForBatch.add(new OneOrMoreKey(candidate.sourceObjectId(),
                                    candidate.ability())))) {
                        stackService.trigger(gameData, candidate, zoneChange);
                    }
                } else if (shadowedSlots.contains(slot)) {
                    reportWouldTrigger(gameData, observer, slot, candidate, zoneChange);
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
            CardEffect effect = legacyTriggerOptions.resolve(gameData, slot, event, candidate, registration.effect());
            if (effect != null) {
                observer.wouldTrigger(slot, candidate.sourceCard(), List.of(effect));
            }
        }
    }

    private record OneOrMoreKey(UUID sourceId, TriggeredAbility ability) {
    }
}
