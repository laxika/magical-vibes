package com.github.laxika.magicalvibes.service.trigger.pipeline;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.TextReplacement;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.trigger.Trigger;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.effect.LayerSystemService;
import com.github.laxika.magicalvibes.service.effect.TextChangeTransformer;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityRequest.TriggerRecord;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * What the counter, tap and life matchers ({@link CounterTriggers}, {@link TapTriggers}, {@link LifeTriggers}) share:
 * the abilities an object has that watch a trigger, read from {@link TriggerIndex} as the layers leave them, and the
 * hand-off of a matched ability to the stack door with the record of its event.
 */
@Component
class EventAbilitySupport {

    private final TriggerIndex triggerIndex;
    private final TriggeredAbilityStackService stackService;
    private final ConditionEvaluationService conditionEvaluationService;
    private final GameLogService gameLogService;

    EventAbilitySupport(TriggerIndex triggerIndex, @Lazy TriggeredAbilityStackService stackService,
                        @Lazy ConditionEvaluationService conditionEvaluationService,
                        @Lazy GameLogService gameLogService) {
        this.triggerIndex = triggerIndex;
        this.stackService = stackService;
        this.conditionEvaluationService = conditionEvaluationService;
        this.gameLogService = gameLogService;
    }

    /**
     * The abilities of {@code permanent} with a trigger condition watched as {@code watched} that functions on the
     * battlefield, as the layers leave them ({@link TriggerIndex#permanentAbilities}): its card's, read through its
     * text changes (CR 612.1: Glamerdye on Riverfall Mimic), unless it is face down or lost them; those granted it for
     * a duration or until it leaves; those other permanents' static abilities grant it.
     */
    List<TriggeredAbility> abilities(GameData gameData, Permanent permanent, Trigger watched) {
        Card card = LayerSystemService.currentCard(gameData, permanent);
        Map<TriggeredAbility, Boolean> printed = new IdentityHashMap<>();
        card.getTriggeredAbilities().forEach(ability -> printed.put(ability, true));
        List<TriggeredAbility> abilities = new ArrayList<>();
        for (TriggeredAbility ability : triggerIndex.permanentAbilities(gameData, permanent)) {
            boolean own = printed.containsKey(ability);
            // A granted ability functions where its object is; a card's own one where its condition says.
            if (own ? ability.hasWatchedTrigger(watched, Zone.BATTLEFIELD) : ability.hasWatchedTrigger(watched)) {
                abilities.add(own ? textChanged(gameData, permanent, ability) : ability);
            }
        }
        return abilities;
    }

    /** The abilities of {@code card} in a graveyard with a trigger condition watched as {@code watched} that functions there. */
    List<TriggeredAbility> graveyardAbilities(GameData gameData, Card card, Trigger watched) {
        List<TriggeredAbility> abilities = new ArrayList<>();
        for (TriggeredAbility ability : triggerIndex.graveyardAbilities(gameData, card)) {
            if (ability.hasWatchedTrigger(watched, Zone.GRAVEYARD)) {
                abilities.add(ability);
            }
        }
        return abilities;
    }

    /** The abilities of {@code card} in exile with a trigger condition watched as {@code watched} that functions there. */
    List<TriggeredAbility> exileAbilities(GameData gameData, Card card, Trigger watched) {
        List<TriggeredAbility> abilities = new ArrayList<>();
        for (TriggeredAbility ability : triggerIndex.cardAbilities(gameData, card)) {
            if (ability.hasWatchedTrigger(watched, Zone.EXILE)) {
                abilities.add(ability);
            }
        }
        return abilities;
    }

    /** The abilities of a face-up planar object's {@code card} with a trigger condition watched as {@code watched}. */
    static List<TriggeredAbility> planarAbilities(Card card, Trigger watched) {
        return card.getTriggeredAbilities().stream().filter(ability -> ability.hasWatchedTrigger(watched)).toList();
    }

    /**
     * The trigger condition of {@code ability} of type {@code type} whose {@link Trigger#watchKey()} is
     * {@code watched}, or {@code null}.
     */
    static <T extends Trigger> T condition(TriggeredAbility ability, Class<T> type, Trigger watched) {
        for (TriggerCondition condition : ability.conditions()) {
            if (type.isInstance(condition.trigger()) && condition.trigger().watchKey().equals(watched)) {
                return type.cast(condition.trigger());
            }
        }
        return null;
    }

    /**
     * Hands {@code ability} of {@code source} (or of {@code sourceCard}, for a card that isn't a permanent) to the
     * stack door, controlled by {@code controllerId}: its event condition is judged here in {@code context} (CR
     * 603.2), its intervening "if" and limit by the door (CR 603.4, 603.2h; "once each turn for each creature" by
     * {@code limitCreatureId}), and {@code record} is stamped on its entry whatever its effects are. A triggered ability
     * is described as "[card]'s ability" and logged as triggering.
     *
     * @return whether it triggered
     */
    boolean put(GameData gameData, TriggeredAbility ability, Card sourceCard, Permanent source, UUID controllerId,
                ConditionContext context, TriggerRecord record, UUID limitCreatureId) {
        return put(gameData, ability, sourceCard, source, controllerId, context, record, limitCreatureId,
                sourceCard.getName() + "'s ability", GameLog.abilityTriggers(sourceCard));
    }

    /** As {@link #put(GameData, TriggeredAbility, Card, Permanent, UUID, ConditionContext, TriggerRecord, UUID)}. */
    boolean put(GameData gameData, TriggeredAbility ability, Card sourceCard, Permanent source, UUID controllerId,
                ConditionContext context, TriggerRecord record) {
        return put(gameData, ability, sourceCard, source, controllerId, context, record, null);
    }

    /**
     * As {@link #put(GameData, TriggeredAbility, Card, Permanent, UUID, ConditionContext, TriggerRecord, UUID)}, with
     * the stack object's {@code description} and the {@code log} entry written as it triggers.
     */
    boolean put(GameData gameData, TriggeredAbility ability, Card sourceCard, Permanent source, UUID controllerId,
                ConditionContext context, TriggerRecord record, UUID limitCreatureId, String description,
                GameLogEntry log) {
        if (!conditionEvaluationService.eventConditionMet(gameData, ability.options(), context)) {
            return false;
        }
        return putJudged(gameData, ability, sourceCard, source, controllerId, context, record, limitCreatureId,
                description, log);
    }

    /** As {@link #put(GameData, TriggeredAbility, Card, Permanent, UUID, ConditionContext, TriggerRecord, UUID, String, GameLog)}, its event condition already judged. */
    boolean putJudged(GameData gameData, TriggeredAbility ability, Card sourceCard, Permanent source,
                      UUID controllerId, ConditionContext context, TriggerRecord record, UUID limitCreatureId,
                      String description, GameLogEntry log) {
        boolean triggered = stackService.put(gameData, new TriggeredAbilityRequest(ability, sourceCard, source,
                controllerId, description, context, record).withLimitCreature(limitCreatureId));
        if (triggered) {
            gameLogService.append(gameData, log);
        }
        return triggered;
    }

    /**
     * Puts {@code ability} of {@code emblem} on the stack through the stack door: an ability of an emblem functions in
     * the command zone (CR 114.4), {@code source} its source, and it records the event with {@code record}.
     */
    void putEmblemAbility(GameData gameData, Emblem emblem, TriggeredAbility ability, Card source, String description,
                          ConditionContext ifContext, TriggerRecord record) {
        stackService.put(gameData, new TriggeredAbilityRequest(ability, source, null, emblem.controllerId(),
                description, ifContext, entry -> {
                    entry.setMadeByResolution(true);
                    record.stamp(entry);
                }).ofEmblem());
    }

    static String emblemName(Emblem emblem) {
        return (emblem.sourceCard() != null ? emblem.sourceCard().getName() : "Emblem") + "'s emblem";
    }

    /**
     * Puts {@code watcher}'s ability, a delayed triggered ability with a stated duration (CR 603.7b), on the stack as
     * its effects (its intervening "if" wrapped around each, CR 603.4) for the event about {@code targetId}, the object
     * or player it acts on without targeting it, {@code triggeringPermanentId} the permanent the event was about.
     */
    void putTemporaryGlobal(GameData gameData, TemporaryGlobalTriggeredAbility watcher, UUID targetId,
                            int eventValue, UUID triggeringPermanentId) {
        Condition interveningIf = watcher.ability().options().interveningIf();
        List<CardEffect> effects = watcher.ability().effects().stream()
                .map(EffectRegistration::effect)
                .map(effect -> interveningIf == null ? effect : (CardEffect) new ConditionalEffect(interveningIf, effect))
                .toList();
        StackEntry entry = TriggeredAbilityStackService.madeAbility(watcher.sourceCard(), watcher.controllerId(),
                watcher.sourceCard().getName() + "'s ability", new ArrayList<>(effects));
        entry.setTargetId(targetId);
        entry.setEventValue(eventValue);
        entry.setLinkedExiledCardId(watcher.linkedExiledCardId());
        if (triggeringPermanentId != null) {
            entry.setTriggeringPermanentId(triggeringPermanentId);
        }
        entry.setNonTargeting(true);
        stackService.putDelayed(gameData, entry);
        gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
    }

    /**
     * The record of a player, counter or tap event, stamped on the ability's entry whatever its effects are:
     * {@code thatPlayerId} as the non-targeting "that player" ({@code null} for an event of the ability's controller),
     * the player the event was about as its triggering player, its count as its event value ("that much", "that many";
     * {@code null} for an event with none), the permanent it was about with that permanent's controller as it happened,
     * and the source as it was when the ability triggered.
     */
    static TriggerRecord eventRecord(Permanent source, UUID thatPlayerId, UUID triggeringPlayerId,
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

    /** {@code ability} of {@code permanent} read through its text changes (Layer 3, CR 612.1). */
    private static TriggeredAbility textChanged(GameData gameData, Permanent permanent, TriggeredAbility ability) {
        List<TextReplacement> globalWordChange = TextChangeTransformer.globalColorWordReplacements(gameData);
        if (permanent.getTextReplacements().isEmpty() && globalWordChange.isEmpty()) {
            return ability;
        }
        List<EffectRegistration> transformed = new ArrayList<>(ability.effects().size());
        boolean changed = false;
        for (EffectRegistration registration : ability.effects()) {
            CardEffect effect = TextChangeTransformer.transform(registration.effect(),
                    permanent.getTextReplacements(), globalWordChange);
            changed |= effect != registration.effect();
            transformed.add(new EffectRegistration(effect, registration.simultaneousWithPrevious()));
        }
        return changed ? ability.withEffects(transformed) : ability;
    }
}
