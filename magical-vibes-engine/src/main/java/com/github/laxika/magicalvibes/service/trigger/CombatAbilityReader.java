package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.condition.DeclaredAttackCondition;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeredModalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.CombatTrigger;
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
 * Reads the combat abilities (a {@link CombatTrigger}: attacks, blocks, becomes blocked, combat damage; CR 508.1,
 * 509.1, 510.2), as {@link WatchedAbilities} finds them, for the attack, block and combat damage collectors, which have no legacy view of them
 *. The collectors judge the ability's trigger-level parts from the
 * ability itself: the filter on the creature ({@link CombatEffect#filter()}), the limit
 * ({@link CombatEffect#limit()}, marked by {@link TriggeredAbility#limitKey()} on the source) and the multiplicity
 * ({@link CombatEffect#multiplicity()}: "blocks" once each combat and "one or more creatures deal combat damage" are
 * {@code ONE_OR_MORE}, "becomes blocked by a creature" {@code EACH_BLOCKER}; CR 509.3a, 509.3d).
 * <p>
 * The intervening "if" (CR 603.4) and the event condition (CR 603.2) are the ability's options: its collector judges
 * them as the ability triggers, in the context of its event, from the records' {@code ability()} or through a
 * {@link TriggerJudge} on the abilities an effect list is read from. The reader wraps the intervening "if" around each
 * effect as the {@link ConditionalEffect} that checks it again as the ability resolves; the event condition is never
 * checked again.
 */
public final class CombatAbilityReader {

    private CombatAbilityReader() {
    }

    /**
     * One effect of a combat ability as its collector reads it.
     *
     * @param effect  the effect, wrapped in the ability's event condition and intervening "if" as the class says
     * @param ability the ability the effect belongs to
     * @param filter  what the creature must be for the watched condition (the filter its {@link Trigger#watchKey()}
     *                drops), or {@code null}
     * @param simultaneousWithPrevious the registration happens at the same time as the effect before it
     */
    public record CombatEffect(CardEffect effect, TriggeredAbility ability,
                               PermanentPredicate filter, boolean simultaneousWithPrevious) {

        public CombatEffect {
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

        /** "One or more": once each combat, step or batch however many creatures the event involves. */
        public boolean oneOrMore() {
            return multiplicity() == TriggerMultiplicity.ONE_OR_MORE;
        }

        /** "Becomes blocked by a creature": once for each creature blocking it (CR 509.3d). */
        public boolean eachBlocker() {
            return multiplicity() == TriggerMultiplicity.EACH_BLOCKER;
        }

        /**
         * The effect as a collector that judged the ability's options puts it on the stack: wrapped in the intervening
         * "if" for the check as the ability resolves (CR 603.4), but for one about the attack as declared
         * ({@link DeclaredAttackCondition#isDeclaredAttackFact}), whose answer then is the one judged as the ability
         * triggered, so its wrapper comes off rather than recount attackers that may have left combat since. A modal
         * ability whose mode is chosen as it goes on the stack (CR 603.3c, {@link TriggeredModalEffect}) loses the
         * wrapper too: the chosen mode is what resolves, and the card writes the intervening "if" into each mode for
         * that check (Jin Sakai, Ghost of Tsushima).
         */
        public CardEffect effectAsJudged() {
            Condition interveningIf = ability.options().interveningIf();
            if (interveningIf != null && effect instanceof ConditionalEffect conditional
                    && conditional.condition() == interveningIf
                    && (DeclaredAttackCondition.isDeclaredAttackFact(interveningIf)
                    || conditional.wrapped() instanceof TriggeredModalEffect)) {
                return conditional.wrapped();
            }
            return effect;
        }

        /** This effect read as {@code effect} instead, as a collector snapshots or binds it. */
        public CombatEffect withEffect(CardEffect effect) {
            return new CombatEffect(effect, ability, filter, simultaneousWithPrevious);
        }

        /**
         * Whether the ability's limit keeps it from triggering for {@code sourceId} now: "triggers only once each
         * turn" and "this ability triggers only once" as {@link TriggeredAbility#limitKey()} marked them, "do this
         * only once each turn" as the source's controller took the action (CR 603.2h).
         */
        public boolean limitReached(GameData gameData, UUID sourceId) {
            return TriggeredAbilityStackService.limitReached(gameData, limit(), sourceId, ability.limitKey());
        }

        /** Marks the ability's limit as used by {@code sourceId}; "do this only once each turn" marks on acceptance. */
        public void markLimit(GameData gameData, UUID sourceId) {
            TriggeredAbilityStackService.markLimit(gameData, limit(), sourceId, ability.limitKey());
        }

        /** "Do this only once each turn": the stack entry marks the source as the action is taken. */
        public boolean marksLimitOnAcceptance() {
            return limit() == TriggerLimit.DO_ONLY_ONCE_EACH_TURN;
        }
    }

    /** Every effect of {@code abilities}, as their collector reads them for the condition watched as {@code watched}. */
    public static List<CombatEffect> read(List<TriggeredAbility> abilities, Trigger watched) {
        List<CombatEffect> effects = new ArrayList<>();
        for (TriggeredAbility ability : abilities) {
            effects.addAll(read(ability, watched));
        }
        return effects;
    }

    /**
     * The effects of {@code abilities}, one list for each ability that has any, in order, as their collector reads them
     * for the condition watched as {@code watched}: each list is one object on the stack (CR 603.3), whose
     * trigger-level parts (filter, limit, multiplicity, options) every read of it shares. Two equal abilities, or one
     * ability a permanent has twice, are two lists.
     */
    public static List<List<CombatEffect>> readByAbility(List<TriggeredAbility> abilities, Trigger watched) {
        List<List<CombatEffect>> byAbility = new ArrayList<>(abilities.size());
        for (TriggeredAbility ability : abilities) {
            List<CombatEffect> effects = read(ability, watched);
            if (!effects.isEmpty()) {
                byAbility.add(effects);
            }
        }
        return byAbility;
    }

    /** The effects of {@code ability}, as its collector reads them for the condition watched as {@code watched}. */
    public static List<CombatEffect> read(TriggeredAbility ability, Trigger watched) {
        PermanentPredicate filter = filter(ability, watched);
        TriggerOptions options = ability.options();
        Condition interveningIf = options.interveningIf();
        List<CombatEffect> effects = new ArrayList<>(ability.effects().size());
        for (EffectRegistration registration : ability.effects()) {
            CardEffect effect = registration.effect();
            if (interveningIf != null) {
                effect = new ConditionalEffect(interveningIf, effect);
            }
            effects.add(new CombatEffect(effect, ability, filter, registration.simultaneousWithPrevious()));
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
        for (CombatEffect effect : read(abilities, watched)) {
            requireNoLimitOrMultiplicity(effect, watched);
            if (effect.filter() == null || filterMatches.test(effect.filter())) {
                effects.add(effect.effect());
            }
        }
        return effects;
    }

    /**
     * The registrations of {@code abilities} for the condition watched as {@code watched}, for a
     * collector that reads no filter, limit or multiplicity: none of its abilities is written with one, and this
     * throws on one.
     */
    public static List<EffectRegistration> registrations(List<TriggeredAbility> abilities, Trigger watched) {
        List<CombatEffect> reads = read(abilities, watched);
        for (CombatEffect effect : reads) {
            requireNoLimitOrMultiplicity(effect, watched);
            if (effect.filter() != null) {
                throw new IllegalStateException("The collector of " + watched + " judges no filter: "
                        + effect.ability());
            }
        }
        return registrationsOf(reads);
    }

    /**
     * {@code effects} grouped by the ability they belong to, the abilities in the order their first effect comes, so
     * that each triggered ability goes on the stack as one object (CR 603.3). Abilities are told apart by identity:
     * a card may have two equal abilities ("myriad, myriad").
     */
    public static List<List<CombatEffect>> byAbility(List<CombatEffect> effects) {
        List<List<CombatEffect>> groups = new ArrayList<>();
        List<TriggeredAbility> abilities = new ArrayList<>();
        for (CombatEffect effect : effects) {
            int index = -1;
            for (int i = 0; i < abilities.size(); i++) {
                if (abilities.get(i) == effect.ability()) {
                    index = i;
                    break;
                }
            }
            if (index < 0) {
                abilities.add(effect.ability());
                groups.add(new ArrayList<>());
                index = groups.size() - 1;
            }
            groups.get(index).add(effect);
        }
        return groups;
    }

    /** The effects of {@code effects}, in order. */
    public static List<CardEffect> effectsOf(List<CombatEffect> effects) {
        return effects.stream().map(CombatEffect::effect).toList();
    }

    /**
     * The effects of one ability's reads in the form its collector puts them on the stack once it judged the ability's
     * options ({@link CombatEffect#effectAsJudged()}), in order: the effects of one stack object (CR 603.3).
     */
    public static List<CardEffect> effectsAsJudged(List<CombatEffect> ability) {
        List<CardEffect> effects = new ArrayList<>(ability.size());
        for (CombatEffect effect : ability) {
            effects.add(effect.effectAsJudged());
        }
        return effects;
    }

    /** The registrations of {@code effects}, in order. */
    public static List<EffectRegistration> registrationsOf(List<CombatEffect> effects) {
        return effects.stream().map(effect -> new EffectRegistration(effect.effect(), effect.simultaneousWithPrevious())).toList();
    }

    /** Throws when {@code effect}'s ability has a limit or multiplicity, which the collector of {@code watched} doesn't read. */
    public static void requireNoLimitOrMultiplicity(CombatEffect effect, Trigger watched) {
        if (effect.limit() != TriggerLimit.NONE || effect.multiplicity() != TriggerMultiplicity.EACH) {
            throw new IllegalStateException("The collector of " + watched + " reads no " + effect.options() + ": "
                    + effect.ability());
        }
    }

    /**
     * The filter on the creature of {@code ability}'s condition watched as {@code watched}: the filter that
     * condition's {@link Trigger#watchKey()} drops, or {@code null}. The same-name and goaded filters stay in their
     * watch keys, so their collectors read them through the trigger they watch.
     */
    public static PermanentPredicate filter(TriggeredAbility ability, Trigger watched) {
        for (TriggerCondition condition : ability.conditions()) {
            if (condition.trigger() instanceof CombatTrigger combat && combat.watchKey().equals(watched)
                    && !combat.equals(watched)) {
                return combat.filter();
            }
        }
        return null;
    }
}
