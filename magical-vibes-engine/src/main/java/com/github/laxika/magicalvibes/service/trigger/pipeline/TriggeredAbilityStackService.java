package com.github.laxika.magicalvibes.service.trigger.pipeline;

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
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.rules.ZoneChangeEvent;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.ZoneChangeTrigger;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.effect.GraveyardTargetingSupport;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
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
 * An ability of one effect, which every card's ability of the moved families is until step 14 of
 * effect-slot-refactor.md, has its trigger-only wrappers read by {@link LegacyTriggerOptions} and goes on the
 * stack through {@link LegacyTriggerBindings}, with the event data its effect needs bound in. An ability of several
 * effects is one stack object (CR 603.3), with a "when this dies" ability's effects bound to the card that died: if it
 * targets, it waits for its targets as a
 * {@link PermanentChoiceContext.DeathTriggerTarget}, chosen through {@code TriggerTargetCollector} as it goes on
 * the stack and removed if none is legal (CR 603.3d), and otherwise it goes on the stack through
 * {@link GameData#enqueueTrigger}.
 * APNAP ordering (CR 603.3b) happens later, in {@code TriggeredAbilityQueueService.beginTriggerOrder}.
 */
@Service
public class TriggeredAbilityStackService {

    private final ConditionEvaluationService conditionEvaluationService;
    private final GameQueryService gameQueryService;
    private final GraveyardTargetingSupport graveyardTargetingSupport;
    private final GameLogService gameLogService;
    private final TriggerCollectionService triggerCollectionService;
    private final LegacyTriggerOptions legacyTriggerOptions;
    private final LegacyTriggerBindings legacyTriggerBindings;

    public TriggeredAbilityStackService(@Lazy ConditionEvaluationService conditionEvaluationService,
                                        @Lazy GameQueryService gameQueryService,
                                        @Lazy GraveyardTargetingSupport graveyardTargetingSupport,
                                        @Lazy GameLogService gameLogService,
                                        @Lazy TriggerCollectionService triggerCollectionService,
                                        LegacyTriggerOptions legacyTriggerOptions,
                                        LegacyTriggerBindings legacyTriggerBindings) {
        this.triggerCollectionService = triggerCollectionService;
        this.conditionEvaluationService = conditionEvaluationService;
        this.gameQueryService = gameQueryService;
        this.graveyardTargetingSupport = graveyardTargetingSupport;
        this.gameLogService = gameLogService;
        this.legacyTriggerOptions = legacyTriggerOptions;
        this.legacyTriggerBindings = legacyTriggerBindings;
    }

    /**
     * Triggers {@code candidate}'s ability for {@code event}, which met its trigger condition {@code trigger},
     * unless its options or wrappers keep it from triggering.
     */
    public void trigger(GameData gameData, TriggerCandidate candidate, ZoneChangeEvent event,
                        ZoneChangeTrigger trigger) {
        TriggerOptions options = candidate.ability().options();
        Permanent source = candidate.sourceAsPermanent();
        Condition interveningIf = options.interveningIf();
        if (interveningIf != null && !conditionEvaluationService.isMet(gameData, interveningIf,
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
                    interveningIf);
        } else {
            triggered = triggerAll(gameData, candidate, event, kind, registrations, interveningIf, options);
        }
        if (triggered) {
            markLimit(gameData, options.limit(), sourceId, limitKey);
        }
    }

    /**
     * An ability of one effect, as the old slot collectors put it on the stack: its wrappers taken off, its event
     * data bound and its targets or mode chosen by {@link LegacyTriggerBindings}.
     */
    private boolean triggerBound(GameData gameData, TriggerCandidate candidate, ZoneChangeEvent event,
                                 LegacyTriggerBindings.Kind kind, CardEffect authored, Condition interveningIf) {
        LegacyTriggerOptions.Resolved resolved = legacyTriggerOptions.resolve(gameData, kind, event, candidate,
                authored);
        if (resolved == null) {
            return false;
        }
        CardEffect effect = interveningIf == null ? resolved.effect()
                : new ConditionalEffect(interveningIf, resolved.effect(), true);
        // "This ability triggers only once each turn" can't be made to trigger an additional time (Clara Oswald).
        int copies = LegacyTriggerOptions.triggersOnlyOnceEachTurn(authored) ? 1
                : 1 + additionalTriggers(gameData, candidate, event, kind);
        int previousCopies = gameData.beginTriggeredAbilityCopies(copies);
        boolean triggered;
        try {
            triggered = legacyTriggerBindings.trigger(gameData, candidate, event, kind, effect);
        } finally {
            gameData.restoreTriggeredAbilityCopies(previousCopies);
        }
        if (triggered) {
            legacyTriggerOptions.markLimit(gameData, candidate, resolved);
        }
        return triggered;
    }

    /** An ability of several effects: one stack object (CR 603.3), with targets chosen as it goes on the stack. */
    private boolean triggerAll(GameData gameData, TriggerCandidate candidate, ZoneChangeEvent event,
                               LegacyTriggerBindings.Kind kind, List<EffectRegistration> registrations,
                               Condition interveningIf, TriggerOptions options) {
        Permanent source = candidate.sourceAsPermanent();
        UUID sourceId = candidate.sourceObjectId();
        List<CardEffect> effects = new ArrayList<>(registrations.size());
        for (EffectRegistration registration : registrations) {
            CardEffect effect = kind == LegacyTriggerBindings.Kind.DIES
                    ? triggerCollectionService.bindSelfDeathEffect(gameData, candidate.sourceCard(),
                    event.permanent(), event.attachments(), registration.effect())
                    : registration.effect();
            effects.add(interveningIf == null ? effect : new ConditionalEffect(interveningIf, effect, true));
        }
        boolean markOnAcceptance = options.limit() == TriggerLimit.DO_ONLY_ONCE_EACH_TURN;
        int lastKnownPower = event.permanent() != null && event.permanent().getLastKnownPower() != null
                ? event.permanent().getLastKnownPower() : 0;
        int previousCopies = gameData.beginTriggeredAbilityCopies(
                1 + additionalTriggers(gameData, candidate, event, kind));
        try {
            if (targets(effects)) {
                gameData.queueInteraction(new PermanentChoiceContext.DeathTriggerTarget(candidate.sourceCard(),
                        candidate.controllerId(), effects, lastKnownPower, new Permanent(source), null, null, false,
                        markOnAcceptance));
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
                    // A graveyard card that leaves and comes back before this resolves is a new object (CR 400.7).
                    entry.setTriggeringCardId(event.card().getId());
                    entry.setTriggeringCardGraveyardEntryVersion(gameData.graveyardEntryVersion(event.card().getId()));
                }
                gameData.enqueueTrigger(entry);
            }
        } finally {
            gameData.restoreTriggeredAbilityCopies(previousCopies);
        }
        gameLogService.append(gameData, GameLog.abilityTriggers(candidate.sourceCard()));
        return true;
    }

    /** The key the source's limits are kept under for this ability: equal abilities of one source share it. */
    private static String limitKey(TriggerCandidate candidate) {
        return "trigger:" + Integer.toHexString(candidate.ability().hashCode());
    }

    private static boolean limitReached(GameData gameData, TriggerLimit limit, UUID sourceId, String key) {
        return switch (limit) {
            case NONE -> false;
            case ONCE_EACH_TURN -> gameData.keyedOncePerTurnTriggersFiredThisTurn
                    .getOrDefault(sourceId, Set.of()).contains(key);
            // Taking the action (accepting it) marks the source, as OncePerTurnTriggerEffect's markOnAcceptance does.
            case DO_ONLY_ONCE_EACH_TURN -> gameData.oncePerTurnTriggersFiredThisTurn.contains(sourceId);
            case ONCE -> gameData.keyedOnceOnlyTriggersFired.getOrDefault(sourceId, Set.of()).contains(key);
        };
    }

    private static void markLimit(GameData gameData, TriggerLimit limit, UUID sourceId, String key) {
        switch (limit) {
            case ONCE_EACH_TURN -> gameData.keyedOncePerTurnTriggersFiredThisTurn
                    .computeIfAbsent(sourceId, ignored -> ConcurrentHashMap.newKeySet()).add(key);
            case ONCE -> gameData.keyedOnceOnlyTriggersFired
                    .computeIfAbsent(sourceId, ignored -> ConcurrentHashMap.newKeySet()).add(key);
            case NONE, DO_ONLY_ONCE_EACH_TURN -> {
            }
        }
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
        int additional = gameQueryService.countAdditionalTriggeredAbilityTriggers(
                gameData, candidate.controllerId(), source, false, null);
        if (creatureDied && kind != LegacyTriggerBindings.Kind.SACRIFICED) {
            additional += gameQueryService.countAdditionalCreatureDeathTriggeredAbilityTriggers(
                    gameData, candidate.controllerId(), source);
        }
        return additional;
    }
}
