package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.trigger.LibraryTrigger;
import com.github.laxika.magicalvibes.model.trigger.LibraryTrigger.LibraryEvent;
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
import java.util.stream.Collectors;

/**
 * Reads the library, draw and discard abilities (a {@link LibraryTrigger}: a player draws a card, CR 121.1, discards
 * one, CR 701.9a, or cycles one, scries, CR 701.22a, surveils, CR 701.25a, searches their library, CR 701.23a, or
 * shuffles it, CR 701.24a), as {@link WatchedAbilities} finds them, for the {@link LibraryWatch} collectors, which have
 * no legacy view of them. The collectors judge the ability's
 * trigger-level parts from the ability itself: the discarded card's filter ({@link LibraryEffect#filter()}, the
 * trigger's {@link LibraryTrigger#qualifier()}), the event a collector of another event reads
 * ({@link LibraryEffect#event()}: "you cycle a card", "an opponent causes you to discard a card", a draw in or out of a
 * draw step), which of the player's events this turn triggers it ({@link LibraryEffect#counts()}: "your second card
 * each turn", "for the first time each turn") and the limit ({@link LibraryEffect#limit()}, marked by
 * {@link TriggeredAbility#limitKey()} on the source).
 * <p>
 * The intervening "if" (CR 603.4) and the event condition (CR 603.2) are the ability's options: its collector judges
 * them as the ability triggers, in the context of its event, from the records' {@code ability()} or through a
 * {@link TriggerJudge} on the abilities an effect list is read from. The reader wraps the intervening "if" around each
 * effect as the {@link ConditionalEffect} that checks it again as the ability resolves; the event condition is never
 * checked again.
 */
public final class LibraryAbilityReader {

    private LibraryAbilityReader() {
    }

    /**
     * One effect of a library, draw or discard ability as its collector reads it.
     *
     * @param effect  the effect, wrapped in the ability's event condition and intervening "if" as the class says
     * @param ability the ability the effect belongs to
     * @param filter  what the discarded card must be for the watched condition (its {@link LibraryTrigger#qualifier()}),
     *                or {@code null}
     * @param event   the watched condition's own event, which may be narrower than the watch's
     *                ({@link LibraryTrigger#watchedEvent()})
     * @param counts  which of the player's events of the watched kind this turn trigger the ability (the
     *                {@link LibraryTrigger#nth()} of each watched condition: "your first or second card each turn" is
     *                {@code {1, 2}}), or none for every event
     * @param simultaneousWithPrevious the registration happens at the same time as the effect before it
     */
    public record LibraryEffect(CardEffect effect, TriggeredAbility ability, CardPredicate filter,
                                LibraryEvent event, Set<Integer> counts, boolean simultaneousWithPrevious) {

        public LibraryEffect {
            Objects.requireNonNull(effect, "effect");
            Objects.requireNonNull(ability, "ability");
            Objects.requireNonNull(event, "event");
            counts = Set.copyOf(Objects.requireNonNull(counts, "counts"));
        }

        public TriggerLimit limit() {
            return ability.options().limit();
        }

        /**
         * Whether the player's {@code count}th event of the watched kind this turn triggers the ability: every one
         * when it counts none, otherwise one of its {@link #counts()}.
         */
        public boolean countMatches(int count) {
            return counts.isEmpty() || counts.contains(count);
        }

        /**
         * Whether only the player's first event of the watched kind this turn triggers the ability ("for the first
         * time each turn"), counting the events before the source was on the battlefield.
         */
        public boolean firstTimeEachTurn() {
            return counts.equals(Set.of(1));
        }

        /**
         * Whether the ability's limit keeps it from triggering for {@code sourceId} now: "triggers only once each
         * turn", "do this only once each turn" once the action was taken, and "this ability triggers only once", as
         * {@link TriggeredAbility#limitKey()} marked them.
         */
        public boolean limitReached(GameData gameData, UUID sourceId) {
            return TriggeredAbilityStackService.limitReached(gameData, limit(), sourceId, ability.limitKey());
        }

        /** Marks the ability's limit as used by {@code sourceId}; "do this only once each turn" is marked on acceptance. */
        public void markLimit(GameData gameData, UUID sourceId) {
            TriggeredAbilityStackService.markLimit(gameData, limit(), sourceId, ability.limitKey());
        }

        /** Whether taking the action marks the ability's limit ("do this only once each turn", CR 603.2h). */
        public boolean marksLimitOnAcceptance() {
            return limit() == TriggerLimit.DO_ONLY_ONCE_EACH_TURN;
        }

        /** This read with {@code effect} in place of its effect. */
        public LibraryEffect withEffect(CardEffect effect) {
            return new LibraryEffect(effect, ability, filter, event, counts, simultaneousWithPrevious);
        }
    }

    /** The abilities of {@code card} that {@code watch} reads, in the order they were added. */
    public static List<TriggeredAbility> printed(Card card, LibraryWatch watch) {
        return WatchedAbilities.printed(card, watch);
    }

    /** Every effect of the abilities of {@code card} that {@code watch} reads, as their collector reads them. */
    public static List<LibraryEffect> readPrinted(Card card, LibraryWatch watch) {
        return read(printed(card, watch), watch.trigger());
    }

    /**
     * The effects of the abilities of {@code card} that {@code watch} reads, for a collector that reads no filter,
     * narrower event, count or limit: none of the abilities it reads is written with one, and this throws on one.
     */
    public static List<CardEffect> printedEffects(Card card, LibraryWatch watch) {
        return effects(printed(card, watch), watch.trigger());
    }

    /** {@link #printedEffects(Card, LibraryWatch)} of the abilities {@code judge} lets trigger. */
    public static List<CardEffect> printedEffects(Card card, LibraryWatch watch, TriggerJudge judge) {
        return effects(judge.filter(printed(card, watch)), watch.trigger());
    }

    /**
     * The registrations of the abilities of {@code card} that {@code watch} reads, for a collector
     * that reads no filter, narrower event, count or limit (this throws on one).
     */
    public static List<EffectRegistration> printedRegistrations(Card card, LibraryWatch watch) {
        List<LibraryEffect> effects = readPrinted(card, watch);
        effects.forEach(effect -> requireNothingToJudge(effect, watch.trigger()));
        return effects.stream().map(effect -> new EffectRegistration(effect.effect(), effect.simultaneousWithPrevious())).toList();
    }

    /**
     * Every effect of the abilities granted to {@code permanent} for a duration (until end of turn, and until it
     * leaves) that are watched as {@code watched}, as their collector reads them.
     */
    public static List<LibraryEffect> readGrantedToPermanent(Permanent permanent, Trigger watched) {
        List<TriggeredAbility> abilities = new ArrayList<>(WatchedAbilities.temporary(permanent, watched));
        abilities.addAll(WatchedAbilities.persistent(permanent, watched));
        return read(abilities, watched);
    }

    /**
     * The effects of {@code abilities}, one list for each ability, in order, as their collector reads them for the
     * condition watched as {@code watched}: each list is one object on the stack (CR 603.3), whose trigger-level parts
     * (filter, event, counts, limit, options) every read of it shares.
     */
    public static List<List<LibraryEffect>> readByAbility(List<TriggeredAbility> abilities, Trigger watched) {
        List<List<LibraryEffect>> byAbility = new ArrayList<>(abilities.size());
        for (TriggeredAbility ability : abilities) {
            List<LibraryEffect> effects = read(ability, watched);
            if (!effects.isEmpty()) {
                byAbility.add(effects);
            }
        }
        return byAbility;
    }

    /** {@link #readByAbility(List, Trigger)} of the abilities of {@code card} that {@code watch} reads. */
    public static List<List<LibraryEffect>> readPrintedByAbility(Card card, LibraryWatch watch) {
        return readByAbility(printed(card, watch), watch.trigger());
    }

    /**
     * The effects of the abilities of {@code card} that {@code watch} reads, one list for each ability (one object on
     * the stack, CR 603.3), for a collector that reads no filter, narrower event, count or limit: none of the abilities
     * it reads is written with one, and this throws on one.
     */
    public static List<List<CardEffect>> printedEffectsByAbility(Card card, LibraryWatch watch) {
        return effectsByAbility(printed(card, watch), watch.trigger());
    }

    /** {@link #printedEffectsByAbility(Card, LibraryWatch)} of the abilities {@code judge} lets trigger. */
    public static List<List<CardEffect>> printedEffectsByAbility(Card card, LibraryWatch watch, TriggerJudge judge) {
        return effectsByAbility(judge.filter(printed(card, watch)), watch.trigger());
    }

    private static List<List<CardEffect>> effectsByAbility(List<TriggeredAbility> abilities, Trigger watched) {
        List<List<CardEffect>> byAbility = new ArrayList<>();
        for (List<LibraryEffect> ability : readByAbility(abilities, watched)) {
            ability.forEach(effect -> requireNothingToJudge(effect, watched));
            byAbility.add(effectsOf(ability));
        }
        return byAbility;
    }

    /** Every effect of {@code abilities}, as their collector reads them for the condition watched as {@code watched}. */
    public static List<LibraryEffect> read(List<TriggeredAbility> abilities, Trigger watched) {
        List<LibraryEffect> effects = new ArrayList<>();
        for (TriggeredAbility ability : abilities) {
            effects.addAll(read(ability, watched));
        }
        return effects;
    }

    /**
     * The effects of {@code ability}, as its collector reads it for the condition watched as {@code watched}. The
     * library, draw and discard collectors trigger once for each card drawn or discarded and once for each other event,
     * by the event ("whenever you discard one or more cards" is an event of its own); they read no multiplicity, and
     * of the limits "triggers only once each turn", "do this only once each turn" and "this ability triggers only
     * once".
     */
    public static List<LibraryEffect> read(TriggeredAbility ability, Trigger watched) {
        List<LibraryTrigger> conditions = watchedConditions(ability, watched);
        LibraryTrigger condition = conditions.getFirst();
        Set<Integer> counts = conditions.stream().map(LibraryTrigger::nth).filter(nth -> nth > 0)
                .collect(Collectors.toSet());
        if (!counts.isEmpty() && conditions.stream().anyMatch(trigger -> trigger.nth() == 0)) {
            throw new IllegalStateException("The library conditions of one ability watched as " + watched
                    + " must all count the player's events or none: " + ability);
        }
        TriggerOptions options = ability.options();
        if (options.multiplicity() != TriggerMultiplicity.EACH) {
            throw new IllegalStateException("No library, draw or discard collector reads " + options.multiplicity()
                    + ": " + ability);
        }
        if (options.limit() == TriggerLimit.ONCE_EACH_TURN_FOR_EACH_CREATURE) {
            throw new IllegalStateException("No library, draw or discard collector reads " + options.limit() + ": "
                    + ability);
        }
        Condition interveningIf = options.interveningIf();
        List<LibraryEffect> effects = new ArrayList<>(ability.effects().size());
        for (EffectRegistration registration : ability.effects()) {
            CardEffect effect = registration.effect();
            if (interveningIf != null) {
                effect = new ConditionalEffect(interveningIf, effect);
            }
            effects.add(new LibraryEffect(effect, ability, condition.qualifier(),
                    condition.event(), counts, registration.simultaneousWithPrevious()));
        }
        return effects;
    }

    /**
     * The effects of {@code abilities} for the condition watched as {@code watched}, for a collector that reads no
     * filter, narrower event, count or limit: none of its abilities is written with one, and this throws on one.
     */
    public static List<CardEffect> effects(List<TriggeredAbility> abilities, Trigger watched) {
        List<LibraryEffect> effects = read(abilities, watched);
        effects.forEach(effect -> requireNothingToJudge(effect, watched));
        return effectsOf(effects);
    }

    /** The effects of {@code effects}, in order. */
    public static List<CardEffect> effectsOf(List<LibraryEffect> effects) {
        return effects.stream().map(LibraryEffect::effect).toList();
    }

    /**
     * Throws when {@code effect}'s ability has a filter, a narrower event, a count or a limit, which the collector of
     * {@code watched} doesn't read.
     */
    public static void requireNothingToJudge(LibraryEffect effect, Trigger watched) {
        requireNoFilter(effect, watched);
        requireWatchedEvent(effect, watched);
        requireNoCount(effect, watched);
        requireNoLimit(effect, watched);
    }

    /** Throws when {@code effect}'s ability has a filter, which the collector of {@code watched} doesn't read. */
    public static void requireNoFilter(LibraryEffect effect, Trigger watched) {
        if (effect.filter() != null) {
            throw new IllegalStateException("The collector of " + watched + " judges no filter: " + effect.ability());
        }
    }

    /**
     * Throws when {@code effect}'s ability has an event narrower than {@code watched}'s ("you cycle a card"), which the
     * collector of {@code watched} doesn't read.
     */
    public static void requireWatchedEvent(LibraryEffect effect, Trigger watched) {
        if (effect.event() != ((LibraryTrigger) watched).event()) {
            throw new IllegalStateException("The collector of " + watched + " reads no " + effect.event() + ": "
                    + effect.ability());
        }
    }

    /**
     * Throws when {@code effect}'s ability counts the player's events this turn, which the collector of {@code watched}
     * doesn't read.
     */
    public static void requireNoCount(LibraryEffect effect, Trigger watched) {
        if (!effect.counts().isEmpty()) {
            throw new IllegalStateException("The collector of " + watched + " counts no events: " + effect.ability());
        }
    }

    /**
     * Throws when {@code effect}'s ability counts the player's events this turn other than "for the first time each
     * turn", the only count the collector of {@code watched} reads.
     */
    public static void requireNoCountButFirst(LibraryEffect effect, Trigger watched) {
        if (!effect.counts().isEmpty() && !effect.firstTimeEachTurn()) {
            throw new IllegalStateException("The collector of " + watched + " counts only the first event of the "
                    + "turn: " + effect.ability());
        }
    }

    /** Throws when {@code effect}'s ability has a limit, which the collector of {@code watched} doesn't read. */
    public static void requireNoLimit(LibraryEffect effect, Trigger watched) {
        if (effect.limit() != TriggerLimit.NONE) {
            throw new IllegalStateException("The collector of " + watched + " judges no limit: " + effect.ability());
        }
    }

    /**
     * {@code ability}'s library conditions watched as {@code watched}: those whose {@link Trigger#watchKey()} it is.
     * They are read as one, so they must have the same event, player and filter; only their counts may differ ("your
     * first or second card each turn").
     */
    private static List<LibraryTrigger> watchedConditions(TriggeredAbility ability, Trigger watched) {
        List<LibraryTrigger> conditions = ability.conditions().stream().map(TriggerCondition::trigger)
                .filter(LibraryTrigger.class::isInstance).map(LibraryTrigger.class::cast)
                .filter(library -> library.watchKey().equals(watched)).distinct().toList();
        if (conditions.isEmpty()) {
            throw new IllegalArgumentException(ability + " has no library condition watched as " + watched);
        }
        LibraryTrigger first = conditions.getFirst();
        if (conditions.stream().anyMatch(library -> library.event() != first.event()
                || library.player() != first.player() || !Objects.equals(library.qualifier(), first.qualifier()))) {
            throw new IllegalStateException("The library conditions of one ability watched as " + watched
                    + " must read alike but for their counts: " + ability);
        }
        return conditions;
    }
}
