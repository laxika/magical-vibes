package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.CounterTrigger;
import com.github.laxika.magicalvibes.model.trigger.CounterTrigger.CounterEvent;
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

/**
 * Reads the counter abilities (a {@link CounterTrigger}: counters are put on or removed from a permanent, CR 122.1, or
 * a keyword action about counters happens), as {@link WatchedAbilities} finds them, for the {@link CounterWatch}
 * collectors, which have no legacy view of them. The collectors judge the
 * ability's trigger-level parts from the ability itself: the further restriction on the permanent the counters are put
 * on ({@link CounterEffect#filter()}, the trigger's {@link CounterTrigger#qualifier()}), the event a collector of
 * another event reads ({@link CounterEffect#byController()}: "you put", "you remove"; {@link CounterEffect#eachCounter()}:
 * once for each +1/+1 counter) and the limit ({@link CounterEffect#limit()}, marked by
 * {@link TriggeredAbility#limitKey()} on the source, or for each creature).
 * <p>
 * The intervening "if" (CR 603.4) and the event condition (CR 603.2) are the ability's options: its collector judges
 * them as the ability triggers, in the context of its event, from the records' {@code ability()} or through a
 * {@link TriggerJudge} on the abilities an effect list is read from. The reader wraps the intervening "if" around each
 * effect as the {@link ConditionalEffect} that checks it again as the ability resolves; the event condition is never
 * checked again.
 */
public final class CounterAbilityReader {

    private CounterAbilityReader() {
    }

    /**
     * One effect of a counter ability as its collector reads it.
     *
     * @param effect  the effect, wrapped in the ability's event condition and intervening "if" as the class says
     * @param ability the ability the effect belongs to
     * @param filter  what the permanent the counters are put on or removed from must also be for the watched condition
     *                (its {@link CounterTrigger#qualifier()}), or {@code null}
     * @param event   the watched condition's own event, which may be narrower than the watch's
     *                ({@link CounterTrigger#watchedEvent()})
     * @param simultaneousWithPrevious the registration happens at the same time as the effect before it
     */
    public record CounterEffect(CardEffect effect, TriggeredAbility ability,
                                PermanentPredicate filter, CounterEvent event, boolean simultaneousWithPrevious) {

        public CounterEffect {
            Objects.requireNonNull(effect, "effect");
            Objects.requireNonNull(ability, "ability");
            Objects.requireNonNull(event, "event");
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

        /**
         * Whether only the ability's controller puts or removes the counters: "you put one or more counters on a
         * creature you control", "you put one or more +1/+1 counters on this", "you remove a time counter from this
         * card while it's exiled", read by the collector of the event about any player.
         */
        public boolean byController() {
            return event == CounterEvent.YOU_PUT_COUNTERS || event == CounterEvent.YOU_PUT_PLUS_ONE_COUNTERS
                    || event == CounterEvent.YOU_REMOVE_TIME_COUNTER_IN_EXILE;
        }

        /**
         * Whether the ability triggers once for each +1/+1 counter put on this ("whenever a +1/+1 counter is put on
         * this"), read by the collector of "one or more +1/+1 counters are put on this".
         */
        public boolean eachCounter() {
            return event == CounterEvent.EACH_PLUS_ONE_COUNTER_PUT;
        }

        /** "Do this only once each turn": the stack entry marks the source as the action is taken (CR 603.2h). */
        public boolean marksLimitOnAcceptance() {
            return limit() == TriggerLimit.DO_ONLY_ONCE_EACH_TURN;
        }

        /** This effect read as {@code effect} instead, as a collector binds it. */
        public CounterEffect withEffect(CardEffect effect) {
            return new CounterEffect(effect, ability, filter, event, simultaneousWithPrevious);
        }

        /**
         * Whether the ability's limit keeps it from triggering for {@code sourceId} now: "triggers only once each
         * turn" and "this ability triggers only once" as {@link TriggeredAbility#limitKey()} marked them, "do this only
         * once each turn" as the source's controller took the action (CR 603.2h). "Once each turn for each creature"
         * is judged by {@link #limitReached(GameData, UUID, UUID)}.
         */
        public boolean limitReached(GameData gameData, UUID sourceId) {
            return TriggeredAbilityStackService.limitReached(gameData, limit(), sourceId, ability.limitKey());
        }

        /**
         * Whether the ability's limit keeps it from triggering for {@code sourceId} now, the counters being put on the
         * creature {@code creatureId}: "once each turn for each creature" as the creature was marked for the source,
         * every other limit as {@link #limitReached(GameData, UUID)} says.
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
         * Marks the ability's limit as used by {@code sourceId} for counters put on the creature {@code creatureId}:
         * "once each turn for each creature" marks the creature for the source, every other limit as
         * {@link #markLimit(GameData, UUID)} says.
         */
        public void markLimit(GameData gameData, UUID sourceId, UUID creatureId) {
            TriggeredAbilityStackService.markLimit(gameData, limit(), sourceId, ability.limitKey(), creatureId);
        }
    }

    /** The abilities of {@code card} that {@code watch} reads, in the order they were added. */
    public static List<TriggeredAbility> printed(Card card, CounterWatch watch) {
        return WatchedAbilities.printed(card, watch);
    }

    /** Every effect of the abilities of {@code card} that {@code watch} reads, as their collector reads them. */
    public static List<CounterEffect> readPrinted(Card card, CounterWatch watch) {
        return read(printed(card, watch), watch.trigger());
    }

    /**
     * The effects of the abilities of {@code card} that {@code watch} reads, for a collector that reads no filter,
     * narrower event or limit: none of the abilities it reads is written with one, and this throws on one.
     */
    public static List<CardEffect> printedEffects(Card card, CounterWatch watch) {
        return effects(printed(card, watch), watch.trigger());
    }

    /** {@link #printedEffects(Card, CounterWatch)} of the abilities {@code judge} lets trigger. */
    public static List<CardEffect> printedEffects(Card card, CounterWatch watch, TriggerJudge judge) {
        return effects(judge.filter(printed(card, watch)), watch.trigger());
    }

    /** Every effect of {@code abilities}, as their collector reads them for the condition watched as {@code watched}. */
    public static List<CounterEffect> read(List<TriggeredAbility> abilities, Trigger watched) {
        List<CounterEffect> effects = new ArrayList<>();
        for (TriggeredAbility ability : abilities) {
            effects.addAll(read(ability, watched));
        }
        return effects;
    }

    /** The effects of {@code ability}, as its collector reads them for the condition watched as {@code watched}. */
    public static List<CounterEffect> read(TriggeredAbility ability, Trigger watched) {
        CounterTrigger condition = watchedCondition(ability, watched);
        TriggerOptions options = ability.options();
        if (options.multiplicity() != TriggerMultiplicity.EACH) {
            throw new IllegalStateException("No counter collector reads " + options.multiplicity() + ": " + ability);
        }
        Condition interveningIf = options.interveningIf();
        List<CounterEffect> effects = new ArrayList<>(ability.effects().size());
        for (EffectRegistration registration : ability.effects()) {
            CardEffect effect = registration.effect();
            if (interveningIf != null) {
                effect = new ConditionalEffect(interveningIf, effect);
            }
            effects.add(new CounterEffect(effect, ability, condition.qualifier(),
                    condition.event(), registration.simultaneousWithPrevious()));
        }
        return effects;
    }

    /**
     * The effects of {@code abilities} for the condition watched as {@code watched}, for a collector that reads no
     * filter, narrower event or limit: none of its abilities is written with one, and this throws on one.
     */
    public static List<CardEffect> effects(List<TriggeredAbility> abilities, Trigger watched) {
        return effectsOf(strict(abilities, watched));
    }

    /**
     * The registrations of {@code abilities} for the condition watched as {@code watched},
     * for a collector that reads no filter, narrower event or limit (this throws on one).
     */
    public static List<EffectRegistration> registrations(List<TriggeredAbility> abilities, Trigger watched) {
        return strict(abilities, watched).stream()
                .map(effect -> new EffectRegistration(effect.effect(), effect.simultaneousWithPrevious()))
                .toList();
    }

    /** The effects of {@code effects}, in order. */
    public static List<CardEffect> effectsOf(List<CounterEffect> effects) {
        return effects.stream().map(CounterEffect::effect).toList();
    }

    /**
     * Throws when {@code effect}'s ability has a filter, a narrower event or a limit, which the collector of
     * {@code watched} doesn't read.
     */
    public static void requireNothingToJudge(CounterEffect effect, Trigger watched) {
        requireNoFilter(effect, watched);
        requireWatchedEvent(effect, watched);
        requireNoLimit(effect, watched);
    }

    /** Throws when {@code effect}'s ability has a filter, which the collector of {@code watched} doesn't read. */
    public static void requireNoFilter(CounterEffect effect, Trigger watched) {
        if (effect.filter() != null) {
            throw new IllegalStateException("The collector of " + watched + " judges no filter: " + effect.ability());
        }
    }

    /**
     * Throws when {@code effect}'s ability has an event narrower than {@code watched}'s ("you put", "each counter"),
     * which the collector of {@code watched} doesn't read.
     */
    public static void requireWatchedEvent(CounterEffect effect, Trigger watched) {
        if (effect.event() != ((CounterTrigger) watched).event()) {
            throw new IllegalStateException("The collector of " + watched + " reads no " + effect.event() + ": "
                    + effect.ability());
        }
    }

    /** Throws when {@code effect}'s ability has a limit, which the collector of {@code watched} doesn't read. */
    public static void requireNoLimit(CounterEffect effect, Trigger watched) {
        if (effect.limit() != TriggerLimit.NONE) {
            throw new IllegalStateException("The collector of " + watched + " judges no limit: " + effect.ability());
        }
    }

    private static List<CounterEffect> strict(List<TriggeredAbility> abilities, Trigger watched) {
        List<CounterEffect> effects = read(abilities, watched);
        effects.forEach(effect -> requireNothingToJudge(effect, watched));
        return effects;
    }

    /** {@code ability}'s counter condition watched as {@code watched}: the one whose {@link Trigger#watchKey()} it is. */
    private static CounterTrigger watchedCondition(TriggeredAbility ability, Trigger watched) {
        for (TriggerCondition condition : ability.conditions()) {
            if (condition.trigger() instanceof CounterTrigger counter && counter.watchKey().equals(watched)) {
                return counter;
            }
        }
        throw new IllegalArgumentException(ability + " has no counter condition watched as " + watched);
    }
}
