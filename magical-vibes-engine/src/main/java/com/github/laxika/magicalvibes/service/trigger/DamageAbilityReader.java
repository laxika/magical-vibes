package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.DamageTrigger;
import com.github.laxika.magicalvibes.model.trigger.Trigger;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerMultiplicity;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * Reads the damage abilities (a {@link DamageTrigger}: damage is dealt, CR 120.1, 120.2, or damage to you is prevented,
 * CR 615.1), as {@link WatchedAbilities} finds them, for the {@link DamageWatch} collectors, which have no legacy view
 * of them ({@link DamageTrigger#collectedAsAbility()}). The collectors judge the ability's trigger-level parts from the
 * ability itself: the filter on the permanent ({@link DamageEffect#filter()}: the damaged permanent for an "is dealt
 * damage" event and for "this deals damage", the dealer for the other "deals damage" events), the limit
 * ({@link DamageEffect#limit()}, marked by {@link TriggeredAbility#limitKey()} on the source, or for each creature) and
 * "once for each source" ({@link DamageEffect#eachSource()}, CR 603.2c).
 * <p>
 * The intervening "if" (CR 603.4) and the event condition (CR 603.2) reach the collectors as the
 * {@link ConditionalEffect}s their handlers judge in contexts of their own (the damage amount, the damaged permanent),
 * until step 15 of effect-slot-refactor.md makes the effects read the event: the intervening "if" is a
 * {@link ConditionalEffect} around each effect, checked as the ability triggers and again as it resolves, and the event
 * condition the {@link ConditionalEffect#atTriggerTime} inside it, checked only as it triggers.
 */
public final class DamageAbilityReader {

    private DamageAbilityReader() {
    }

    /**
     * One effect of a damage ability as its collector reads it.
     *
     * @param effect  the effect, wrapped in the ability's event condition and intervening "if" as the class says
     * @param mode    the registration's trigger mode
     * @param ability the ability the effect belongs to
     * @param filter  what the permanent must be for the watched condition (the filter its {@link Trigger#watchKey()}
     *                drops), or {@code null}
     * @param simultaneousWithPrevious the registration happens at the same time as the effect before it
     */
    public record DamageEffect(CardEffect effect, TriggerMode mode, TriggeredAbility ability,
                               PermanentPredicate filter, boolean simultaneousWithPrevious) {

        public DamageEffect {
            Objects.requireNonNull(effect, "effect");
            Objects.requireNonNull(mode, "mode");
            Objects.requireNonNull(ability, "ability");
        }

        public TriggerOptions options() {
            return ability.options();
        }

        public TriggerLimit limit() {
            return ability.options().limit();
        }

        public TriggerMultiplicity multiplicity() {
            return ability.options().multiplicity();
        }

        /** "Whenever a source deals damage to this": once for each source in one damage event (CR 603.2c). */
        public boolean eachSource() {
            return multiplicity() == TriggerMultiplicity.EACH_SOURCE;
        }

        /** "Do this only once each turn": the stack entry marks the source as the action is taken (CR 603.2h). */
        public boolean marksLimitOnAcceptance() {
            return limit() == TriggerLimit.DO_ONLY_ONCE_EACH_TURN;
        }

        /** This effect read as {@code effect} instead, as a collector binds it. */
        public DamageEffect withEffect(CardEffect effect) {
            return new DamageEffect(effect, mode, ability, filter, simultaneousWithPrevious);
        }

        /**
         * Whether the ability's limit keeps it from triggering for {@code sourceId} now: "triggers only once each
         * turn" and "this ability triggers only once" as {@link TriggeredAbility#limitKey()} marked them, "do this
         * only once each turn" as the source's controller took the action (CR 603.2h). "Once each turn for each
         * creature" is judged by {@link #limitReached(GameData, UUID, UUID)}.
         */
        public boolean limitReached(GameData gameData, UUID sourceId) {
            return TriggeredAbilityStackService.limitReached(gameData, limit(), sourceId, ability.limitKey());
        }

        /**
         * Whether the ability's limit keeps it from triggering for {@code sourceId} now, the event naming the creature
         * {@code creatureId}: "once each turn for each creature" as {@link #markLimit(GameData, UUID, UUID)} marked
         * the creature for the source, every other limit as {@link #limitReached(GameData, UUID)} says.
         */
        public boolean limitReached(GameData gameData, UUID sourceId, UUID creatureId) {
            if (limit() == TriggerLimit.ONCE_EACH_TURN_FOR_EACH_CREATURE) {
                return gameData.oncePerCreatureTriggersFiredThisTurn.getOrDefault(sourceId, Set.of())
                        .contains(creatureId);
            }
            return limitReached(gameData, sourceId);
        }

        /** Marks the ability's limit as used by {@code sourceId}; "do this only once each turn" marks on acceptance. */
        public void markLimit(GameData gameData, UUID sourceId) {
            TriggeredAbilityStackService.markLimit(gameData, limit(), sourceId, ability.limitKey());
        }

        /**
         * Marks the ability's limit as used by {@code sourceId} for the event naming the creature {@code creatureId}:
         * "once each turn for each creature" marks the creature for the source, every other limit as
         * {@link #markLimit(GameData, UUID)} says.
         */
        public void markLimit(GameData gameData, UUID sourceId, UUID creatureId) {
            if (limit() == TriggerLimit.ONCE_EACH_TURN_FOR_EACH_CREATURE) {
                gameData.oncePerCreatureTriggersFiredThisTurn
                        .computeIfAbsent(sourceId, ignored -> ConcurrentHashMap.newKeySet()).add(creatureId);
                return;
            }
            markLimit(gameData, sourceId);
        }
    }

    /** The abilities of {@code card} that {@code watch} reads, in the order they were added. */
    public static List<TriggeredAbility> printed(Card card, DamageWatch watch) {
        return WatchedAbilities.printed(card, watch);
    }

    /**
     * The effects of the abilities of {@code card} that {@code watch} reads, for a collector that reads no filter,
     * limit or multiplicity: none of the abilities it reads is written with one, and this throws on one.
     */
    public static List<CardEffect> printedEffects(Card card, DamageWatch watch) {
        return effects(printed(card, watch), watch.trigger());
    }

    /**
     * The effects of the abilities {@code watch} reads granted to {@code permanent} until end of turn, end of combat or
     * next upkeep, for a collector that reads no filter, limit or multiplicity (this throws on one).
     */
    public static List<CardEffect> temporaryEffects(Permanent permanent, DamageWatch watch) {
        return effects(WatchedAbilities.temporary(permanent, watch.trigger()), watch.trigger());
    }

    /**
     * The effects of the abilities {@code watch} reads granted to {@code permanent} until it leaves the battlefield,
     * for a collector that reads no filter, limit or multiplicity (this throws on one).
     */
    public static List<CardEffect> persistentEffects(Permanent permanent, DamageWatch watch) {
        return effects(WatchedAbilities.persistent(permanent, watch.trigger()), watch.trigger());
    }

    /** Every effect of {@code abilities}, as their collector reads them for the condition watched as {@code watched}. */
    public static List<DamageEffect> read(List<TriggeredAbility> abilities, Trigger watched) {
        List<DamageEffect> effects = new ArrayList<>();
        for (TriggeredAbility ability : abilities) {
            effects.addAll(read(ability, watched));
        }
        return effects;
    }

    /** The effects of {@code ability}, as its collector reads them for the condition watched as {@code watched}. */
    public static List<DamageEffect> read(TriggeredAbility ability, Trigger watched) {
        PermanentPredicate filter = filter(ability, watched);
        TriggerOptions options = ability.options();
        Condition eventCondition = options.eventCondition();
        Condition interveningIf = options.interveningIf();
        List<DamageEffect> effects = new ArrayList<>(ability.effects().size());
        for (EffectRegistration registration : ability.effects()) {
            CardEffect effect = registration.effect();
            if (eventCondition != null) {
                effect = ConditionalEffect.atTriggerTime(eventCondition, effect);
            }
            if (interveningIf != null) {
                effect = new ConditionalEffect(interveningIf, effect, true);
            }
            effects.add(new DamageEffect(effect, registration.triggerMode(), ability, filter,
                    registration.simultaneousWithPrevious()));
        }
        return effects;
    }

    /**
     * The effects of {@code abilities} for the condition watched as {@code watched}, for a collector that reads no
     * filter, limit or multiplicity: none of its abilities is written with one, and this throws on one.
     */
    public static List<CardEffect> effects(List<TriggeredAbility> abilities, Trigger watched) {
        return effects(abilities, watched, filter -> {
            throw new IllegalStateException("The collector of " + watched + " judges no filter: " + filter);
        });
    }

    /**
     * The effects of the abilities of {@code abilities} whose filter for the condition watched as {@code watched}
     * {@code filterMatches} accepts (an ability without one always triggers), for a collector that reads no limit or
     * multiplicity: none of its abilities is written with one, and this throws on one.
     */
    public static List<CardEffect> effects(List<TriggeredAbility> abilities, Trigger watched,
                                           Predicate<PermanentPredicate> filterMatches) {
        List<CardEffect> effects = new ArrayList<>();
        for (DamageEffect effect : read(abilities, watched)) {
            requireNoLimitOrMultiplicity(effect, watched);
            if (effect.filter() == null || filterMatches.test(effect.filter())) {
                effects.add(effect.effect());
            }
        }
        return effects;
    }

    /**
     * The registrations of {@code abilities} for the condition watched as {@code watched}, with their trigger modes,
     * for a collector that reads no filter, limit or multiplicity: none of its abilities is written with one, and this
     * throws on one.
     */
    public static List<EffectRegistration> registrations(List<TriggeredAbility> abilities, Trigger watched) {
        return registrations(abilities, watched, filter -> {
            throw new IllegalStateException("The collector of " + watched + " judges no filter: " + filter);
        });
    }

    /**
     * The registrations of the abilities of {@code abilities} whose filter for the condition watched as
     * {@code watched} {@code filterMatches} accepts (an ability without one always triggers), with their trigger
     * modes, for a collector that reads no limit or multiplicity: none of its abilities is written with one, and this
     * throws on one.
     */
    public static List<EffectRegistration> registrations(List<TriggeredAbility> abilities, Trigger watched,
                                                         Predicate<PermanentPredicate> filterMatches) {
        List<EffectRegistration> registrations = new ArrayList<>();
        for (DamageEffect effect : read(abilities, watched)) {
            requireNoLimitOrMultiplicity(effect, watched);
            if (effect.filter() == null || filterMatches.test(effect.filter())) {
                registrations.add(new EffectRegistration(effect.effect(), effect.mode(),
                        effect.simultaneousWithPrevious()));
            }
        }
        return registrations;
    }

    /** The effects of {@code effects}, in order. */
    public static List<CardEffect> effectsOf(List<DamageEffect> effects) {
        return effects.stream().map(DamageEffect::effect).toList();
    }

    /** Throws when {@code effect}'s ability has a limit or multiplicity, which the collector of {@code watched} doesn't read. */
    public static void requireNoLimitOrMultiplicity(DamageEffect effect, Trigger watched) {
        if (effect.limit() != TriggerLimit.NONE || effect.multiplicity() != TriggerMultiplicity.EACH) {
            throw new IllegalStateException("The collector of " + watched + " reads no " + effect.options() + ": "
                    + effect.ability());
        }
    }

    /**
     * The filter on the permanent of {@code ability}'s condition watched as {@code watched}: the filter that
     * condition's {@link Trigger#watchKey()} drops, or {@code null}.
     */
    public static PermanentPredicate filter(TriggeredAbility ability, Trigger watched) {
        for (TriggerCondition condition : ability.conditions()) {
            if (condition.trigger() instanceof DamageTrigger damage && damage.watchKey().equals(watched)) {
                return damage.filter();
            }
        }
        return null;
    }
}
