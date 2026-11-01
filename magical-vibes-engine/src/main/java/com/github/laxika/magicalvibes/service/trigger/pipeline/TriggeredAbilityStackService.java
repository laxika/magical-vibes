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
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.effect.GraveyardTargetingSupport;
import com.github.laxika.magicalvibes.service.trigger.TriggerContext;
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
 *   <li>an ability that targets waits for its targets as a {@link PermanentChoiceContext.DeathTriggerTarget},
 *       chosen through {@code TriggerTargetCollector} as it goes on the stack, and is removed if none is legal
 *       (CR 603.3d); any other ability goes on the stack through {@link GameData#enqueueTrigger};</li>
 *   <li>effects that make abilities trigger an additional time (Teysa Karlov's, Panharmonicon-like ones) add
 *       copies.</li>
 * </ul>
 * APNAP ordering (CR 603.3b) happens later, in {@code TriggeredAbilityQueueService.beginTriggerOrder}.
 */
@Service
public class TriggeredAbilityStackService {

    private final ConditionEvaluationService conditionEvaluationService;
    private final GameQueryService gameQueryService;
    private final GraveyardTargetingSupport graveyardTargetingSupport;
    private final GameLogService gameLogService;

    public TriggeredAbilityStackService(@Lazy ConditionEvaluationService conditionEvaluationService,
                                        @Lazy GameQueryService gameQueryService,
                                        @Lazy GraveyardTargetingSupport graveyardTargetingSupport,
                                        @Lazy GameLogService gameLogService) {
        this.conditionEvaluationService = conditionEvaluationService;
        this.gameQueryService = gameQueryService;
        this.graveyardTargetingSupport = graveyardTargetingSupport;
        this.gameLogService = gameLogService;
    }

    /** Triggers {@code candidate}'s ability for {@code event}, unless its options keep it from triggering. */
    public void trigger(GameData gameData, TriggerCandidate candidate, ZoneChangeEvent event) {
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
        List<CardEffect> effects = new ArrayList<>(candidate.ability().effects().size());
        for (EffectRegistration registration : candidate.ability().effects()) {
            effects.add(interveningIf == null ? registration.effect()
                    : new ConditionalEffect(interveningIf, registration.effect(), true));
        }
        boolean markOnAcceptance = options.limit() == TriggerLimit.DO_ONLY_ONCE_EACH_TURN;
        int lastKnownPower = event.permanent() != null && event.permanent().getLastKnownPower() != null
                ? event.permanent().getLastKnownPower() : 0;
        int previousCopies = gameData.beginTriggeredAbilityCopies(1 + additionalTriggers(gameData, candidate, event));
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
        markLimit(gameData, options.limit(), sourceId, limitKey);
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
     * count them. The death of a creature counts the creature-death ones too.
     */
    private int additionalTriggers(GameData gameData, TriggerCandidate candidate, ZoneChangeEvent event) {
        Permanent source = candidate.sourceAsPermanent();
        boolean creatureDied = event.died() && event.permanent() != null
                && event.permanent().getCard().hasType(CardType.CREATURE);
        TriggerContext context = event.leftBattlefield()
                ? new TriggerContext.SelfLeaves(candidate.controllerId(), event.to(), false, creatureDied)
                : null;
        int additional = gameQueryService.countAdditionalTriggeredAbilityTriggers(
                gameData, candidate.controllerId(), source, false, context);
        if (creatureDied) {
            additional += gameQueryService.countAdditionalCreatureDeathTriggeredAbilityTriggers(
                    gameData, candidate.controllerId(), source);
        }
        return additional;
    }
}
