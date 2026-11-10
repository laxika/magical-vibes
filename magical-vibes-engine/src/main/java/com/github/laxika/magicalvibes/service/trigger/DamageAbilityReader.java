package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
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
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Reads the damage abilities (a {@link DamageTrigger}: damage is dealt, CR 120.1, 120.2, or damage to you is prevented,
 * CR 615.1), as {@link WatchedAbilities} finds them, for the {@link DamageWatch} collectors, which have no legacy view
 * of them. The collectors judge the ability's trigger-level parts from the
 * ability itself: the filter on the permanent ({@link DamageEffect#filter()}: the damaged permanent for an "is dealt
 * damage" event and for "this deals damage", the dealer for the other "deals damage" events), the limit
 * ({@link DamageEffect#limit()}, marked by {@link TriggeredAbility#limitKey()} on the source, or for each creature) and
 * "once for each source" ({@link DamageEffect#eachSource()}, CR 603.2c).
 * <p>
 * The intervening "if" (CR 603.4) and the event condition (CR 603.2) are the ability's options: its collector judges
 * them as the ability triggers, in the context of its event, from the records' {@code ability()} or through a
 * {@link TriggerJudge} on the abilities an effect list is read from. The reader wraps the intervening "if" around each
 * effect as the {@link ConditionalEffect} that checks it again as the ability resolves; the event condition is never
 * checked again.
 */
public final class DamageAbilityReader {

    private DamageAbilityReader() {
    }

    /**
     * One effect of a damage ability as its collector reads it.
     *
     * @param effect  the effect, wrapped in the ability's event condition and intervening "if" as the class says
     * @param ability the ability the effect belongs to
     * @param filter  what the permanent must be for the watched condition (the filter its {@link Trigger#watchKey()}
     *                drops), or {@code null}
     * @param simultaneousWithPrevious the registration happens at the same time as the effect before it
     */
    public record DamageEffect(CardEffect effect, TriggeredAbility ability,
                               PermanentPredicate filter, boolean simultaneousWithPrevious) {

        public DamageEffect {
            Objects.requireNonNull(effect, "effect");
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
            return new DamageEffect(effect, ability, filter, simultaneousWithPrevious);
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
            return TriggeredAbilityStackService.limitReached(gameData, limit(), sourceId, ability.limitKey(),
                    creatureId);
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
            TriggeredAbilityStackService.markLimit(gameData, limit(), sourceId, ability.limitKey(), creatureId);
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

    /** {@link #printedEffects(Card, DamageWatch)} of the abilities {@code judge} lets trigger. */
    public static List<CardEffect> printedEffects(Card card, DamageWatch watch, TriggerJudge judge) {
        return effects(judge.filter(printed(card, watch)), watch.trigger());
    }

    /** {@link #temporaryEffects(Permanent, DamageWatch)} of the abilities {@code judge} lets trigger. */
    public static List<CardEffect> temporaryEffects(Permanent permanent, DamageWatch watch, TriggerJudge judge) {
        return effects(judge.filter(WatchedAbilities.temporary(permanent, watch.trigger())), watch.trigger());
    }

    /** {@link #persistentEffects(Permanent, DamageWatch)} of the abilities {@code judge} lets trigger. */
    public static List<CardEffect> persistentEffects(Permanent permanent, DamageWatch watch, TriggerJudge judge) {
        return effects(judge.filter(WatchedAbilities.persistent(permanent, watch.trigger())), watch.trigger());
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

    /**
     * {@code reads}, a flat list of abilities' reads in order, one list for each ability: the consecutive reads of one
     * ability object, each list one object on the stack (CR 603.3).
     */
    public static List<List<DamageEffect>> byAbility(List<DamageEffect> reads) {
        List<List<DamageEffect>> byAbility = new ArrayList<>();
        List<DamageEffect> current = null;
        for (DamageEffect read : reads) {
            if (current == null || current.getFirst().ability() != read.ability()) {
                current = new ArrayList<>();
                byAbility.add(current);
            }
            current.add(read);
        }
        return byAbility;
    }

    /**
     * The effects of {@code abilities}, one list for each ability, in order, as their collector reads them for the
     * condition watched as {@code watched}: each list is one object on the stack (CR 603.3), whose trigger-level parts
     * (filter, limit, multiplicity, options) every read of it shares.
     */
    public static List<List<DamageEffect>> readByAbility(List<TriggeredAbility> abilities, Trigger watched) {
        List<List<DamageEffect>> byAbility = new ArrayList<>(abilities.size());
        for (TriggeredAbility ability : abilities) {
            List<DamageEffect> effects = read(ability, watched);
            if (!effects.isEmpty()) {
                byAbility.add(effects);
            }
        }
        return byAbility;
    }

    /** The effects of {@code ability}, as its collector reads them for the condition watched as {@code watched}. */
    public static List<DamageEffect> read(TriggeredAbility ability, Trigger watched) {
        PermanentPredicate filter = filter(ability, watched);
        TriggerOptions options = ability.options();
        Condition interveningIf = options.interveningIf();
        List<DamageEffect> effects = new ArrayList<>(ability.effects().size());
        for (EffectRegistration registration : ability.effects()) {
            CardEffect effect = registration.effect();
            if (interveningIf != null) {
                effect = new ConditionalEffect(interveningIf, effect);
            }
            effects.add(new DamageEffect(effect, ability, filter,
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
     * The registrations of {@code abilities} for the condition watched as {@code watched},
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
                registrations.add(new EffectRegistration(effect.effect(), effect.simultaneousWithPrevious()));
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
        DamageTrigger damage = trigger(ability, watched);
        return damage == null ? null : damage.filter();
    }

    /**
     * The condition of {@code ability} watched as {@code watched}, with its filters ({@link DamageTrigger#filter()},
     * {@link DamageTrigger#damagedFilter()}, {@link DamageTrigger#combatOnly()}), or {@code null}.
     */
    public static DamageTrigger trigger(TriggeredAbility ability, Trigger watched) {
        for (TriggerCondition condition : ability.conditions()) {
            if (condition.trigger() instanceof DamageTrigger damage && damage.watchKey().equals(watched)) {
                return damage;
            }
        }
        return null;
    }
}
