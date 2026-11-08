package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeredManaAbilityEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TapTrigger;
import com.github.laxika.magicalvibes.model.trigger.TapTrigger.TapEvent;
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
import java.util.stream.Stream;

/**
 * Reads the tap and untap abilities (a {@link TapTrigger}: a permanent is tapped, CR 701.26a, or untapped,
 * CR 701.26b), as {@link WatchedAbilities} finds them, for the {@link TapWatch} collectors, which have no legacy view of
 * them. The collectors judge the ability's trigger-level parts from the
 * ability itself: what the tapped or untapped permanent must be ({@link TapEffect#filter()}: "this permanent" for a
 * {@link TapTrigger#narrowed()} trigger, its type, and the trigger's {@link TapTrigger#qualifier()}), "for the first
 * time during your turn" ({@link TapEffect#firstTime()}), "one or more" ({@link TapEffect#oneOrMore()}) and the limit
 * ({@link TapEffect#limit()}, marked by {@link TriggeredAbility#limitKey()} on the source).
 * <p>
 * The intervening "if" (CR 603.4) and the event condition (CR 603.2) are the ability's options: its collector judges
 * them as the ability triggers, in the context of its event, from the records' {@code ability()} or through a
 * {@link TriggerJudge} on the abilities an effect list is read from. The reader wraps the intervening "if" around each
 * effect as the {@link ConditionalEffect} that checks it again as the ability resolves; the event condition is never
 * checked again.
 */
public final class TapAbilityReader {

    private TapAbilityReader() {
    }

    /**
     * One effect of a tap or untap ability as its collector reads it.
     *
     * @param effect    the effect, wrapped in the ability's event condition and intervening "if" as the class says
     * @param ability   the ability the effect belongs to
     * @param filter    what the tapped or untapped permanent must be for the watched condition, or {@code null}:
     *                  "this permanent" ({@link PermanentIsSourceCardPredicate}) and the type of a
     *                  {@link TapTrigger#narrowed()} trigger, and its {@link TapTrigger#qualifier()}, all of them when
     *                  there are several. The collector judges it with the ability's source as the filter's source
     * @param firstTime whether only the creature's first tap during its controller's turn triggers the ability ("for
     *                  the first time during each of your turns"), counting its taps before the source was on the
     *                  battlefield
     * @param simultaneousWithPrevious the registration happens at the same time as the effect before it
     */
    public record TapEffect(CardEffect effect, TriggeredAbility ability, PermanentPredicate filter, boolean firstTime,
                            boolean simultaneousWithPrevious) {

        public TapEffect {
            Objects.requireNonNull(effect, "effect");
            Objects.requireNonNull(ability, "ability");
        }

        public TriggerLimit limit() {
            return ability.options().limit();
        }

        /** Whether the ability triggers once for the permanents tapped together ("one or more"). */
        public boolean oneOrMore() {
            return ability.options().multiplicity() == TriggerMultiplicity.ONE_OR_MORE;
        }

        /**
         * Whether the ability's limit ("triggers only once each turn", as {@link TriggeredAbility#limitKey()} marked
         * it) keeps it from triggering for {@code sourceId} now.
         */
        public boolean limitReached(GameData gameData, UUID sourceId) {
            return TriggeredAbilityStackService.limitReached(gameData, limit(), sourceId, ability.limitKey());
        }

        /** Marks the ability's limit as used by {@code sourceId}. */
        public void markLimit(GameData gameData, UUID sourceId) {
            TriggeredAbilityStackService.markLimit(gameData, limit(), sourceId, ability.limitKey());
        }

        /**
         * The effect as a collector that judged the ability's options hands it on: wrapped in the intervening "if" for
         * the check as the ability resolves (CR 603.4), but for a triggered mana ability
         * ({@link TapAbilityReader#isTriggeredManaAbility}), which doesn't go on the stack and resolves as soon as it
         * triggers (CR 605.4a), so the check as it triggered is that check and its wrapper comes off.
         */
        public CardEffect effectAsJudged() {
            Condition interveningIf = ability.options().interveningIf();
            if (interveningIf != null && effect instanceof ConditionalEffect conditional
                    && conditional.condition() == interveningIf && isTriggeredManaAbility(ability)) {
                return conditional.wrapped();
            }
            return effect;
        }

        /** This read with {@code effect} in place of its effect. */
        public TapEffect withEffect(CardEffect effect) {
            return new TapEffect(effect, ability, filter, firstTime, simultaneousWithPrevious);
        }
    }

    /** The abilities of {@code card} that {@code watch} reads, in the order they were added. */
    public static List<TriggeredAbility> printed(Card card, TapWatch watch) {
        return WatchedAbilities.printed(card, watch);
    }

    /** Every effect of the abilities of {@code card} that {@code watch} reads, as their collector reads them. */
    public static List<TapEffect> readPrinted(Card card, TapWatch watch) {
        return read(printed(card, watch), watch.trigger());
    }

    /**
     * The effects of the abilities of {@code card} that {@code watch} reads, for a collector that judges no filter,
     * first-time event, "one or more" or limit: none of the abilities it reads is written with one, and this throws on
     * one.
     */
    public static List<CardEffect> printedEffects(Card card, TapWatch watch) {
        return effects(printed(card, watch), watch.trigger());
    }

    /** {@link #printedEffects(Card, TapWatch)} of the abilities {@code judge} lets trigger. */
    public static List<CardEffect> printedEffects(Card card, TapWatch watch, TriggerJudge judge) {
        return effects(judge.filter(printed(card, watch)), watch.trigger());
    }

    /**
     * {@link #printedEffects(Card, TapWatch, TriggerJudge)} as {@link TapEffect#effectAsJudged()} hands them on: a
     * triggered mana ability's intervening "if", judged by {@code judge}, no longer wraps its effects.
     */
    public static List<CardEffect> printedEffectsAsJudged(Card card, TapWatch watch, TriggerJudge judge) {
        List<TapEffect> effects = read(judge.filter(printed(card, watch)), watch.trigger());
        effects.forEach(effect -> requireNothingToJudge(effect, watch.trigger()));
        return effects.stream().map(TapEffect::effectAsJudged).toList();
    }

    /**
     * Whether {@code ability} is a triggered mana ability (CR 605.1b): it triggers only from a permanent being tapped
     * for mana (a mana ability's activation), requires no target, and could add mana as it resolves (a
     * {@link TriggeredManaAbilityEffect}, alone or as a step of a {@link SequenceEffect}).
     */
    public static boolean isTriggeredManaAbility(TriggeredAbility ability) {
        boolean fromManaAbility = ability.conditions().stream().allMatch(condition
                -> condition.trigger() instanceof TapTrigger tap && tap.event() == TapEvent.TAPPED_FOR_MANA);
        List<CardEffect> effects = ability.effects().stream()
                .flatMap(registration -> registration.effect() instanceof SequenceEffect sequence
                        ? sequence.steps().stream() : Stream.of(registration.effect()))
                .toList();
        return fromManaAbility
                && effects.stream().noneMatch(effect -> effect.targetSpec().declaredTarget() != null)
                && effects.stream().anyMatch(TriggeredManaAbilityEffect.class::isInstance);
    }

    /**
     * The registrations of the abilities of {@code card} that {@code watch} reads, for a collector that judges no
     * filter, first-time event, "one or more" or limit (this throws on one).
     */
    public static List<EffectRegistration> printedRegistrations(Card card, TapWatch watch) {
        return registrations(printed(card, watch), watch.trigger());
    }

    /** {@link #printedRegistrations(Card, TapWatch)} of the abilities {@code judge} lets trigger. */
    public static List<EffectRegistration> printedRegistrations(Card card, TapWatch watch, TriggerJudge judge) {
        return registrations(judge.filter(printed(card, watch)), watch.trigger());
    }

    /**
     * The registrations of {@code abilities} for the condition watched as {@code watched}, for a collector that judges
     * no filter, first-time event, "one or more" or limit (this throws on one).
     */
    public static List<EffectRegistration> registrations(List<TriggeredAbility> abilities, Trigger watched) {
        List<TapEffect> effects = read(abilities, watched);
        effects.forEach(effect -> requireNothingToJudge(effect, watched));
        return effects.stream().map(effect -> new EffectRegistration(effect.effect(), effect.simultaneousWithPrevious())).toList();
    }

    /**
     * Every effect of the abilities granted to {@code permanent} for a duration (until end of turn, and until it
     * leaves) that are watched as {@code watched}, as their collector reads them.
     */
    public static List<TapEffect> readGrantedToPermanent(Permanent permanent, Trigger watched) {
        List<TriggeredAbility> abilities = new ArrayList<>(WatchedAbilities.temporary(permanent, watched));
        abilities.addAll(WatchedAbilities.persistent(permanent, watched));
        return read(abilities, watched);
    }

    /** Every effect of {@code abilities}, as their collector reads them for the condition watched as {@code watched}. */
    public static List<TapEffect> read(List<TriggeredAbility> abilities, Trigger watched) {
        List<TapEffect> effects = new ArrayList<>();
        for (TriggeredAbility ability : abilities) {
            effects.addAll(read(ability, watched));
        }
        return effects;
    }

    /**
     * The effects of {@code ability}, as its collector reads it for the condition watched as {@code watched}. The tap
     * and untap collectors read "one or more" (the ability checks as it is built that its collector batches the
     * permanents tapped together) and of the limits only "triggers only once each turn", which they don't read next
     * to "for the first time during your turn".
     */
    public static List<TapEffect> read(TriggeredAbility ability, Trigger watched) {
        TapTrigger condition = watchedCondition(ability, watched);
        TriggerOptions options = ability.options();
        if (options.limit() != TriggerLimit.NONE && options.limit() != TriggerLimit.ONCE_EACH_TURN) {
            throw new IllegalStateException("No tap or untap collector reads " + options.limit() + ": " + ability);
        }
        boolean firstTime = condition.event() == TapEvent.BECOMES_TAPPED_FOR_FIRST_TIME_DURING_YOUR_TURN;
        if (firstTime && options.limit() != TriggerLimit.NONE) {
            throw new IllegalStateException("No tap collector reads a limit next to \"for the first time during your "
                    + "turn\": " + ability);
        }
        PermanentPredicate filter = filter(condition);
        Condition interveningIf = options.interveningIf();
        List<TapEffect> effects = new ArrayList<>(ability.effects().size());
        for (EffectRegistration registration : ability.effects()) {
            CardEffect effect = registration.effect();
            if (interveningIf != null) {
                effect = new ConditionalEffect(interveningIf, effect);
            }
            effects.add(new TapEffect(effect, ability, filter, firstTime, registration.simultaneousWithPrevious()));
        }
        return effects;
    }

    /**
     * The effects of {@code abilities} for the condition watched as {@code watched}, for a collector that judges no
     * filter, first-time event, "one or more" or limit: none of its abilities is written with one, and this throws on
     * one.
     */
    public static List<CardEffect> effects(List<TriggeredAbility> abilities, Trigger watched) {
        List<TapEffect> effects = read(abilities, watched);
        effects.forEach(effect -> requireNothingToJudge(effect, watched));
        return effectsOf(effects);
    }

    /** The effects of {@code effects}, in order. */
    public static List<CardEffect> effectsOf(List<TapEffect> effects) {
        return effects.stream().map(TapEffect::effect).toList();
    }

    /**
     * Throws when {@code effect}'s ability has a filter, the first-time event, "one or more" or a limit, which the
     * collector of {@code watched} doesn't read.
     */
    public static void requireNothingToJudge(TapEffect effect, Trigger watched) {
        requireNoFilter(effect, watched);
        requireNotFirstTime(effect, watched);
        requireNoOneOrMore(effect, watched);
        requireNoLimit(effect, watched);
    }

    /** Throws when {@code effect}'s ability has a filter, which the collector of {@code watched} doesn't read. */
    public static void requireNoFilter(TapEffect effect, Trigger watched) {
        if (effect.filter() != null) {
            throw new IllegalStateException("The collector of " + watched + " judges no filter: " + effect.ability());
        }
    }

    /**
     * Throws when {@code effect}'s ability is "for the first time during your turn", which the collector of
     * {@code watched} doesn't read.
     */
    public static void requireNotFirstTime(TapEffect effect, Trigger watched) {
        if (effect.firstTime()) {
            throw new IllegalStateException("The collector of " + watched + " reads no \"for the first time during "
                    + "your turn\": " + effect.ability());
        }
    }

    /** Throws when {@code effect}'s ability is "one or more", which the collector of {@code watched} doesn't read. */
    public static void requireNoOneOrMore(TapEffect effect, Trigger watched) {
        if (effect.oneOrMore()) {
            throw new IllegalStateException("The collector of " + watched + " reads no \"one or more\": "
                    + effect.ability());
        }
    }

    /** Throws when {@code effect}'s ability has a limit, which the collector of {@code watched} doesn't read. */
    public static void requireNoLimit(TapEffect effect, Trigger watched) {
        if (effect.limit() != TriggerLimit.NONE) {
            throw new IllegalStateException("The collector of " + watched + " judges no limit: " + effect.ability());
        }
    }

    /**
     * {@code ability}'s tap condition watched as {@code watched} (one whose {@link Trigger#watchKey()} it is). Several
     * are read as one, so they must have the same filter and event.
     */
    private static TapTrigger watchedCondition(TriggeredAbility ability, Trigger watched) {
        List<TapTrigger> conditions = ability.conditions().stream().map(TriggerCondition::trigger)
                .filter(TapTrigger.class::isInstance).map(TapTrigger.class::cast)
                .filter(tap -> tap.watchKey().equals(watched)).distinct().toList();
        if (conditions.isEmpty()) {
            throw new IllegalArgumentException(ability + " has no tap condition watched as " + watched);
        }
        TapTrigger first = conditions.getFirst();
        if (conditions.stream().anyMatch(tap -> tap.event() != first.event()
                || !Objects.equals(filter(tap), filter(first)))) {
            throw new IllegalStateException("The tap conditions of one ability watched as " + watched
                    + " must have the same filter and event: " + ability);
        }
        return first;
    }

    /**
     * The filter the collector of {@code trigger}'s watch judges, or {@code null} for none: "this permanent" for a
     * {@link TapTrigger#narrowed()} trigger on {@link ObjectRelation#THIS}, the type its watch key drops, and the
     * {@link TapTrigger#qualifier()}, all of them when there are several. A qualifier on a trigger whose collector
     * judges none throws.
     */
    private static PermanentPredicate filter(TapTrigger trigger) {
        if (trigger.qualifier() != null && !judgesFilter(trigger)) {
            throw new IllegalStateException("No tap or untap collector judges a filter on " + trigger);
        }
        List<PermanentPredicate> parts = new ArrayList<>();
        if (trigger.narrowed() && trigger.relation() == ObjectRelation.THIS) {
            parts.add(new PermanentIsSourceCardPredicate());
        }
        if (trigger.narrowed() && trigger.filter() != null) {
            parts.add(trigger.filter());
        }
        if (trigger.qualifier() != null) {
            parts.add(trigger.qualifier());
        }
        return switch (parts.size()) {
            case 0 -> null;
            case 1 -> parts.getFirst();
            default -> new PermanentAllOfPredicate(List.copyOf(parts));
        };
    }

    /**
     * Whether the collector that reads {@code trigger}'s abilities judges a filter on the tapped or untapped permanent:
     * "a permanent you control / an opponent controls becomes tapped" (and the {@link TapTrigger#narrowed()} triggers
     * it reads), "you tap a permanent an opponent controls", and "a permanent [you control] becomes untapped".
     */
    private static boolean judgesFilter(TapTrigger trigger) {
        if (trigger.narrowed()) {
            return true;
        }
        return switch (trigger.event()) {
            case BECOMES_TAPPED -> trigger.relation() == ObjectRelation.YOU_CONTROL
                    || trigger.relation() == ObjectRelation.OPPONENT_CONTROLS;
            case YOU_TAP -> trigger.relation() == ObjectRelation.OPPONENT_CONTROLS;
            case BECOMES_UNTAPPED -> trigger.relation() == ObjectRelation.YOU_CONTROL
                    || trigger.relation() == ObjectRelation.ANY;
            default -> false;
        };
    }
}
