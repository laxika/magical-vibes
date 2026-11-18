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
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
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
import java.util.HashMap;
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

    /** The snapshot the abilities are read from inside {@link #asOf}, on this thread only. */
    private static final ThreadLocal<Scope> AS_OF = new ThreadLocal<>();

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
     * a duration or until it leaves; those other permanents' static abilities grant it. Inside
     * {@link #asOf(GameData, AbilitySnapshot, Runnable)}, a permanent the snapshot lists has them as it had them then.
     */
    List<TriggeredAbility> abilities(GameData gameData, Permanent permanent, Trigger watched) {
        return abilities(gameData, permanent, watched, null);
    }

    /**
     * As {@link #abilities(GameData, Permanent, Trigger)}, only the ones of {@code permanent}'s card (as text changes
     * leave them): those a reader judges by where its card is (an Aura's printed "enchanted creature" ability).
     */
    List<TriggeredAbility> ownAbilities(GameData gameData, Permanent permanent, Trigger watched) {
        return abilities(gameData, permanent, watched, true);
    }

    /**
     * As {@link #abilities(GameData, Permanent, Trigger)}, only the ones its card doesn't have: those granted it for a
     * duration or until it leaves, those other permanents' static abilities grant it, and its keywords'.
     */
    List<TriggeredAbility> grantedAbilities(GameData gameData, Permanent permanent, Trigger watched) {
        return abilities(gameData, permanent, watched, false);
    }

    private List<TriggeredAbility> abilities(GameData gameData, Permanent permanent, Trigger watched, Boolean own) {
        List<Listed> listed = null;
        Scope scope = AS_OF.get();
        if (scope != null && scope.gameData() == gameData) {
            listed = scope.snapshot().byPermanentId.get(permanent.getId());
        }
        if (listed == null) {
            listed = listed(gameData, permanent);
        }
        List<TriggeredAbility> abilities = new ArrayList<>();
        for (Listed ability : listed) {
            if (own != null && ability.own() != own) {
                continue;
            }
            // A granted ability functions where its object is; a card's own one where its condition says.
            if (ability.own() ? ability.ability().hasWatchedTrigger(watched, Zone.BATTLEFIELD)
                    : ability.ability().hasWatchedTrigger(watched)) {
                abilities.add(ability.ability());
            }
        }
        return abilities;
    }

    /** {@code permanent}'s triggered abilities as the layers leave it now, its card's own read through text changes. */
    private List<Listed> listed(GameData gameData, Permanent permanent) {
        Card card = LayerSystemService.currentCard(gameData, permanent);
        Map<TriggeredAbility, Boolean> printed = new IdentityHashMap<>();
        card.getTriggeredAbilities().forEach(ability -> printed.put(ability, true));
        List<Listed> listed = new ArrayList<>();
        for (TriggeredAbility ability : triggerIndex.permanentAbilities(gameData, permanent)) {
            boolean own = printed.containsKey(ability);
            listed.add(new Listed(own ? textChanged(gameData, permanent, ability) : ability, own));
        }
        return listed;
    }

    /** A triggered ability a permanent has, and whether it is its card's own (functioning where its condition says). */
    private record Listed(TriggeredAbility ability, boolean own) {
    }

    /**
     * The triggered abilities every permanent on the battlefield has at one moment, as the layers leave them, kept so
     * the abilities an event triggered are read as their objects had them when it happened (CR 603.2) after a
     * permanent left or changed before they are put on the stack: the combat damage step's, put after the
     * state-based actions its damage causes (CR 510.2, 704.3).
     */
    static final class AbilitySnapshot {
        private final Map<UUID, List<Listed>> byPermanentId;

        private AbilitySnapshot(Map<UUID, List<Listed>> byPermanentId) {
            this.byPermanentId = byPermanentId;
        }
    }

    /** What every permanent on the battlefield has now, for {@link #asOf(GameData, AbilitySnapshot, Runnable)}. */
    AbilitySnapshot snapshot(GameData gameData) {
        Map<UUID, List<Listed>> byPermanentId = new HashMap<>();
        gameData.forEachPermanent((controllerId, permanent) ->
                byPermanentId.put(permanent.getId(), List.copyOf(listed(gameData, permanent))));
        return new AbilitySnapshot(byPermanentId);
    }

    /**
     * Runs {@code work} with {@code snapshot} as what {@link #abilities(GameData, Permanent, Trigger)} reads of a
     * permanent it lists in {@code gameData}, whether or not that permanent is still on the battlefield.
     */
    void asOf(GameData gameData, AbilitySnapshot snapshot, Runnable work) {
        Scope previous = AS_OF.get();
        AS_OF.set(new Scope(gameData, snapshot));
        try {
            work.run();
        } finally {
            if (previous == null) {
                AS_OF.remove();
            } else {
                AS_OF.set(previous);
            }
        }
    }

    /** A snapshot {@link #abilities(GameData, Permanent, Trigger)} reads for {@code gameData}'s permanents. */
    private record Scope(GameData gameData, AbilitySnapshot snapshot) {
    }

    /**
     * The watchers whose abilities may see an event: the permanents on the battlefield, each under its controller, then
     * the emblems, whose abilities function in the command zone (CR 114.4), then the delayed triggered abilities
     * ({@link TriggerIndex#delayedAbilities}) that watch no particular object.
     */
    List<Watcher> watchers(GameData gameData) {
        List<Watcher> watchers = new ArrayList<>();
        gameData.forEachPermanent((controllerId, permanent) -> watchers.add(Watcher.of(permanent, controllerId)));
        for (Emblem emblem : List.copyOf(gameData.emblems)) {
            watchers.add(Watcher.of(emblem));
        }
        watchers.addAll(delayedWatchers(gameData));
        return watchers;
    }

    /** The delayed triggered abilities ({@link TriggerIndex#delayedAbilities}) that watch no particular object. */
    List<Watcher> delayedWatchers(GameData gameData) {
        return triggerIndex.delayedAbilities(gameData).stream()
                .filter(delayed -> delayed.watchedPermanentId() == null).map(Watcher::of).toList();
    }

    /**
     * The delayed triggered abilities about {@code permanentId} ("when that creature dies this turn", "whenever it
     * attacks this turn"), whose "this" is that permanent (CR 603.7c), as watchers.
     */
    List<Watcher> delayedWatching(GameData gameData, UUID permanentId) {
        return triggerIndex.delayedAbilitiesAbout(gameData, permanentId).stream().map(Watcher::of).toList();
    }

    /**
     * The abilities of {@code watcher} with a trigger condition watched as {@code watched}: a permanent's as the layers
     * leave them ({@link #abilities(GameData, Permanent, Trigger)}), an emblem's as it has them, a delayed ability
     * itself; an emblem's and a delayed ability's whatever zones their conditions name.
     */
    List<TriggeredAbility> abilities(GameData gameData, Watcher watcher, Trigger watched) {
        if (watcher.permanent() != null) {
            return abilities(gameData, watcher.permanent(), watched);
        }
        List<TriggeredAbility> own = watcher.emblem() != null ? watcher.emblem().triggeredAbilities()
                : List.of(watcher.delayed().ability());
        return own.stream().filter(ability -> ability.hasWatchedTrigger(watched)).toList();
    }

    /**
     * Hands {@code ability} of {@code watcher} to the stack door as
     * {@link #put(GameData, TriggeredAbility, Card, Permanent, UUID, ConditionContext, TriggerRecord)} does. An emblem's
     * ability has its emblem's card as its source and no source permanent; it is an emblem's (CR 114.4), which only the
     * effects that count an emblem's abilities make trigger an additional time, and its targets are only those its own
     * effects declare.
     *
     * @return whether it triggered
     */
    boolean put(GameData gameData, TriggeredAbility ability, Watcher watcher, ConditionContext context,
                TriggerRecord record) {
        if (watcher.permanent() != null) {
            return put(gameData, ability, watcher.card(), watcher.permanent(), watcher.controllerId(), context, record);
        }
        if (!conditionEvaluationService.eventConditionMet(gameData, ability.options(), context)) {
            return false;
        }
        if (watcher.delayed() != null) {
            return putDelayed(gameData, ability, watcher.delayed(), context, record);
        }
        String description = emblemName(watcher.emblem());
        boolean triggered = stackService.put(gameData, new TriggeredAbilityRequest(ability, watcher.card(), null,
                watcher.controllerId(), description, context, entry -> {
                    entry.setMadeByResolution(true);
                    record.stamp(entry);
                }).ofEmblem());
        if (triggered) {
            gameLogService.append(gameData, GameLog.text(description + " triggers."));
        }
        return triggered;
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
     * Hands {@code ability} of {@code delayed}, a delayed triggered ability a resolving spell or ability created
     * (CR 603.7a), to the stack door, its event condition already judged: its source is that spell's or ability's
     * card and it is controlled by that spell's or ability's controller (CR 603.7d, 603.7e), and it is no object's own
     * ability, so no "triggers an additional time" effect counts it (CR 603.2d). The door judges its intervening "if"
     * (CR 603.4) and chooses its targets (CR 603.3d), restricted by the registration's target filter. {@code record}
     * stamps the event as it does for any other watcher's ability, and the card its maker's source had exiled with its
     * linked ability is its linked exiled card (CR 603.7c).
     *
     * @return whether it triggered
     */
    boolean putDelayed(GameData gameData, TriggeredAbility ability, TemporaryGlobalTriggeredAbility delayed,
                       ConditionContext context, TriggerRecord record) {
        Card source = delayed.sourceCard();
        boolean triggered = stackService.put(gameData, new TriggeredAbilityRequest(ability, source, null,
                delayed.controllerId(), source.getName() + "'s ability", context, delayedRecord(delayed, record)));
        if (triggered) {
            gameLogService.append(gameData, GameLog.abilityTriggers(source));
        }
        return triggered;
    }

    /**
     * The request {@code ability} of {@code watcher} hands the stack door, its event condition judged by its matcher:
     * a permanent's, described as {@code description} after its card's name; an emblem's (CR 114.4), which only the
     * effects that count an emblem's abilities make trigger an additional time; a delayed ability's, as
     * {@link #putDelayed} hands it. {@code record} stamps the event whatever kind the watcher is.
     */
    static TriggeredAbilityRequest request(Watcher watcher, TriggeredAbility ability, String description,
                                           ConditionContext context, TriggerRecord record) {
        Card card = watcher.card();
        if (watcher.permanent() != null) {
            return new TriggeredAbilityRequest(ability, card, watcher.permanent(), watcher.controllerId(),
                    card.getName() + description, context, record);
        }
        if (watcher.emblem() != null) {
            return new TriggeredAbilityRequest(ability, card, null, watcher.controllerId(),
                    emblemName(watcher.emblem()), context, entry -> {
                        entry.setMadeByResolution(true);
                        record.stamp(entry);
                    }).ofEmblem();
        }
        return new TriggeredAbilityRequest(ability, card, null, watcher.controllerId(), card.getName() + "'s ability",
                context, delayedRecord(watcher.delayed(), record));
    }

    /**
     * {@code record} of an event of {@code delayed}'s ability, with what the delayed ability itself records: it was
     * made by a resolution, its maker's source's linked exiled card (CR 603.7c), and the
     * registration's target filter, which restricts the targets the door chooses (CR 603.3d). The door reads
     * {@link DelayedRecord#delayed} to end an ability that triggers only once (CR 603.7b).
     */
    static TriggerRecord delayedRecord(TemporaryGlobalTriggeredAbility delayed, TriggerRecord record) {
        return new DelayedRecord(delayed, record);
    }

    /** The record {@link #delayedRecord} stamps on an entry of the delayed triggered ability {@code delayed}. */
    record DelayedRecord(TemporaryGlobalTriggeredAbility delayed, TriggerRecord record) implements TriggerRecord {

        @Override
        public void stamp(StackEntry entry) {
            entry.setMadeByResolution(true);
            record.stamp(entry);
            if (delayed.linkedExiledCardId() != null) {
                entry.setLinkedExiledCardId(delayed.linkedExiledCardId());
            }
            if (delayed.targetFilter() != null) {
                entry.setTargetFilter(delayed.targetFilter());
            }
        }
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
        if (gameData.textReplacements(permanent.getId()).isEmpty() && globalWordChange.isEmpty()) {
            return ability;
        }
        List<EffectRegistration> transformed = new ArrayList<>(ability.effects().size());
        boolean changed = false;
        for (EffectRegistration registration : ability.effects()) {
            CardEffect effect = TextChangeTransformer.transform(registration.effect(),
                    gameData.textReplacements(permanent.getId()), globalWordChange);
            changed |= effect != registration.effect();
            transformed.add(new EffectRegistration(effect, registration.simultaneousWithPrevious()));
        }
        return changed ? ability.withEffects(transformed) : ability;
    }
}
